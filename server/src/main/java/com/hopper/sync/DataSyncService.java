package com.hopper.sync;

import com.hopper.GlobalConfiguration;
import com.hopper.future.LatchFuture;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.Endpoint;
import com.hopper.session.Message;
import com.hopper.session.MessageService;
import com.hopper.session.OutgoingSession;
import com.hopper.stage.Stage;
import com.hopper.stage.StageManager;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateNodeSnapshot;
import com.hopper.storage.StateStorage;
import com.hopper.storage.merkle.Difference;
import com.hopper.storage.merkle.MerkleTree;
import com.hopper.verb.Verb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link DataSyncService} provides supports for data synchronization between nodes.
 */
public class DataSyncService extends LifecycleProxy {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DataSyncService.class);
    /**
     * Global configuration reference
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * Storage reference
     */
    private final StateStorage storage = config.getDefaultServer().getStorage();
    /**
     * Data synchronization thread pool
     */
    private ThreadPoolExecutor threadPool;

    @Override
    protected void doInit() {
        threadPool = StageManager.getThreadPool(Stage.SYNC);
    }

    @Override
    protected void doShutdown() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    /**
     * Compares the local merkle tree with remote(the remote is fresh) and return the comparison result.
     */
    public LatchFuture<DiffResult> diff(int remoteServerId) {
        RequireRemoteDiffTask task = new RequireRemoteDiffTask(remoteServerId);
        return (LatchFuture<DiffResult>) threadPool.submit(task);
    }

    /**
     * Executes the comparison result on local storage
     */
    public void applyDiff(DiffResult diff) {

        if (diff.getMaxXid() <= storage.getMaxXid()) {
            logger.info("Ignoring the diff result, because oft he target xid {0} is smaller than local {1}",
                    new Object[]{diff.getMaxXid(), storage.getMaxXid()});
            return;
        }

        Difference difference = diff.getDifference();

        if (!difference.hasDifferences()) {
            return;
        }

        for (StateNodeSnapshot snapshot : difference.addedList) {
            StateNode node = new StateNode(snapshot.key, snapshot.version);
            node.update(snapshot);

            storage.put(node);
        }

        for (StateNodeSnapshot snapshot : difference.removedList) {
            storage.remove(snapshot.key);
        }

        for (StateNodeSnapshot snapshot : difference.updatedList) {
            StateNode node = storage.get(snapshot.key);
            if (node != null) {
                if (snapshot.version > node.getVersion()) {
                    node.update(snapshot);
                }
            }
        }
    }

    /**
     * Pull data from remote server(remote is fresh) with asynchronization
     */
    public void syncDataFromRemote(final int serverId) {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    DiffResult diffResult = syncDiff(serverId);
                    applyDiff(diffResult);
                } catch (Exception e) {
                    logger.error("Failed to pull data from remote[server:{0}]", new Object[]{serverId, e});
                }
            }
        };

        threadPool.execute(task);
    }

    /**
     * Push the local data to remote servers(local data is fresh)
     */
    public List<LatchFuture<Boolean>> syncDataToRemote(Integer[] remoteServers) {
        RequireRemoteSyncTask[] tasks = new RequireRemoteSyncTask[remoteServers.length];
        List<LatchFuture<Boolean>> futures = new ArrayList<LatchFuture<Boolean>>(remoteServers.length);
        for (int i = 0; i < tasks.length; i++) {
            RequireRemoteSyncTask task = new RequireRemoteSyncTask(config.getEndpoint(remoteServers[i]));
            LatchFuture<Boolean> future = (LatchFuture<Boolean>) threadPool.submit(task);
            futures.add(future);
        }

        return futures;
    }

    /**
     * {@link com.hopper.sync.DataSyncService.RequireRemoteDiffTask} will send local data to remote
     * server, and requires the remote server to return the diff data.
     */
    private class RequireRemoteDiffTask implements Callable<DiffResult> {
        final GlobalConfiguration config = GlobalConfiguration.getInstance();
        final StateStorage storage = config.getDefaultServer().getStorage();
        final int remoteServerId;

        private RequireRemoteDiffTask(int remoteServerId) {
            this.remoteServerId = remoteServerId;
        }

        @Override
        public DiffResult call() throws Exception {
            return syncDiff(remoteServerId);
        }
    }

    private DiffResult syncDiff(int remoteServerId) throws Exception {
        Message message = new Message();
        message.setVerb(Verb.DIFF_RESULT);
        message.setId(Message.nextId());

        RequireDiff diff = new RequireDiff();
        diff.setMaxXid(storage.getMaxXid());
        diff.setTree(storage.getHashTree());

        message.setBody(diff);

        Future<Message> future = MessageService.send(message, remoteServerId);
        Message reply = future.get(config.getSyncTimeout(), TimeUnit.MILLISECONDS);

        return (DiffResult) reply.getBody();
    }

    private class RequireRemoteSyncTask implements Callable<Boolean> {
        final Endpoint remoteServer;

        private RequireRemoteSyncTask(Endpoint remoteServer) {
            this.remoteServer = remoteServer;
        }

        @Override
        public Boolean call() throws Exception {
            Message request = new Message();
            request.setVerb(Verb.REQUIRE_TREE);
            request.setId(Message.nextId());

            OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(remoteServer);

            Future<Message> future = session.send(request);

            Message reply = future.get(config.getRpcTimeout(), TimeUnit.MILLISECONDS);

            MerkleTree tree = (MerkleTree) reply.getBody();

            storage.getHashTree().loadHash();
            Difference difference = storage.getHashTree().difference(tree);

            request = new Message();
            request.setVerb(Verb.APPLY_DIFF);
            request.setId(Message.nextId());

            request.setBody(difference);

            future = session.send(request);

            reply = future.get(config.getRpcTimeout(), TimeUnit.MILLISECONDS);

            byte[] body = (byte[]) reply.getBody();

            return body[0] == 0;
        }
    }
}
