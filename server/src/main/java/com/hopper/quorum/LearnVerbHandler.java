package com.hopper.quorum;

import com.hopper.GlobalConfiguration;
import com.hopper.future.LatchFuture;
import com.hopper.future.LatchFutureListener;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Server;
import com.hopper.session.ClientSession;
import com.hopper.session.Message;
import com.hopper.session.MessageService;
import com.hopper.sync.DataSyncService;
import com.hopper.sync.DiffResult;
import com.hopper.sync.SyncException;
import com.hopper.verb.Verb;
import com.hopper.verb.VerbHandler;
import com.hopper.verb.handler.BatchMultiplexerSessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {@link LearnVerbHandler} processes the learn message
 */
public class LearnVerbHandler implements VerbHandler {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(LearnVerbHandler.class);
    /**
     * Singleton instance
     */
    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private final GlobalConfiguration config = componentManager.getGlobalConfiguration();

    private final Server server = componentManager.getDefaultServer();
    /**
     * Data synchronization service
     */
    private final DataSyncService dataSyncService = componentManager.getDataSyncService();
    /**
     * Paxos instance
     */
    private final Paxos paxos = componentManager.getLeaderElection().getPaxos();

    private final Set<Integer> learnedInstances = new HashSet<Integer>();

    @Override
    public void doVerb(Message message) {

        Learn learn = (Learn) message.getBody();

        logger.info("Received the learn message {}", message);

        // Ignoring the smaller epoch or learned instance
        if (learn.getEpoch() < paxos.getEpoch() || learnedInstances.contains(learn.getEpoch())) {
            logger.info("Discard the learn message {}", message);
            return;
        }

        learnedInstances.add(learn.getEpoch());

        logger.info("The election instance {} has chosen leader {}", learn.getEpoch(), learn.getVval());

        // TODO:
        if (learn.getEpoch() > paxos.getEpoch()) {
            paxos.setEpoch(learn.getEpoch());
        }

        try {
            logger.info("Start data synchronization...");
            learnElectedLeader(server.getLeader(), learn.getVval(), true);
            logger.info("Success to synchronize data.");
        } catch (Exception e) {
            logger.error("Failed to synchronize data, leader:{}", learn.getVval(), e);
            componentManager.getLeaderElection().startElecting();
        }
    }

    public void learnElectedLeader(int olderLeader, int newLeader, boolean closeInstance) throws Exception {

        // running on SYNC status
        server.setElectionState(Server.ElectionState.SYNC);

        server.setLeader(newLeader);

        if (closeInstance) {
            paxos.closeInstance();
        }

        // clear learned instances
        learnedInstances.clear();

        try {
            if (server.isLeader()) {
                logger.info("Current server has been chosen as leader, takes leader ship...");

                componentManager.getDefaultServer().takeLeadership();
                traceLeaderDataSync();

                server.setElectionState(Server.ElectionState.LEADING);
            } else {
                logger.info("Current server is follower, accept the new leader {},transfers the client sessions to " +
                        "new leader", newLeader);
                transferClientSession(newLeader);
                server.setElectionState(Server.ElectionState.FOLLOWING);
            }
        } catch (Exception e) {
            logger.error("Failed to process learn message.", e);
            server.setElectionState(Server.ElectionState.SYNC_FAILED);
            throw e;
        }
    }

    private void traceLeaderDataSync() throws Exception {

        long t = System.currentTimeMillis();
        startLeaderDataSync();
        long syncTime = System.currentTimeMillis() - t;

        long rpcPeriod = componentManager.getGlobalConfiguration().getRpcTimeout();
        long waitingPeriod = 2 * rpcPeriod;

        // Although the election process has completed, but the followers might not communicate to leader
        // immediately, so the election monitor might start election progress again. To avoid the case,
        // it allows setting a barrier with a period for election monitor.
        logger.info("Sync time={}, waiting time={}", syncTime, (waitingPeriod - syncTime));
        if (syncTime < waitingPeriod) {
            componentManager.getElectionMonitor().setBarrier(waitingPeriod - syncTime);
        }
    }

    /**
     * Leader starts data synchronization
     */
    private void startLeaderDataSync() throws Exception {
        logger.info("Starting leader data synchronization...");

        Message message = new Message();
        message.setVerb(Verb.QUERY_MAX_XID);

        List<Message> replies = componentManager.getMessageService().sendMessageToQuorum(message,
                MessageService.WAITING_MODE_QUORUM);

        logger.info("Received the query result {}", replies);
        int repliesNum = replies.size();

        // No majority response, starts a new election
        if (repliesNum < config.getQuorumSize() - 1) {
            logger.info("No majority nodes response query maxid, retry election...");
            throw new NoQuorumException();
        }

        List<QueryMaxXid> maxXidResult = new ArrayList<QueryMaxXid>();

        for (Message reply : replies) {
            maxXidResult.add((QueryMaxXid) reply.getBody());
        }

        Collections.sort(maxXidResult, new Comparator<QueryMaxXid>() {
            @Override
            public int compare(QueryMaxXid o1, QueryMaxXid o2) {
                return (int) (o2.getMaxXid() - o1.getMaxXid());
            }
        });

        QueryMaxXid result = (QueryMaxXid) replies.get(0).getBody();

        // Local data is up-to-date
        if (componentManager.getStateStorage().getMaxXid() < result.getMaxXid()) {
            try {
                // Retrieve the diff from remote server
                LatchFuture<DiffResult> future = dataSyncService.diff(result.getServerId());
                DiffResult diff = future.get();

                // Apply the diff to local
                dataSyncService.applyDiff(diff);

                // Retrieve all stale servers(need synchronize to up-to-date)
                Integer[] staleServers = getStaleServers(componentManager.getStateStorage().getMaxXid(), maxXidResult);

                int upToDateNum = repliesNum - staleServers.length;

                // If existing some server are stale, it should synchronize them with leader, if the up-to-date servers
                // are majority, synchronize the stale server with asynchronous mode; otherwise, with synchronous mode
                if (staleServers.length > 0) {

                    // Synchronize leader data to stale nodes with asynchronous mode
                    List<LatchFuture<Boolean>> futures = dataSyncService.syncDataToRemote(staleServers);

                    // If no enough node are up-to-date, awaiting with synchronous mode
                    if (upToDateNum < config.getQuorumSize()) {
                        final CountDownLatch latch = new CountDownLatch(config.getQuorumSize() - upToDateNum);
                        for (LatchFuture<Boolean> syncFuture : futures) {
                            syncFuture.addListener(new LatchFutureListener() {
                                @Override
                                public void complete(LatchFuture future) {
                                    if (future.isSuccess()) {
                                        latch.countDown();
                                    }
                                }
                            });
                        }

                        latch.await(config.getSyncTimeout(), TimeUnit.MILLISECONDS);
                    }
                }
            } catch (ExecutionException e) {
                throw new SyncException(e.getCause());
            }
        }
    }

    /**
     * Return all nodes that data are stale than leader
     */
    private Integer[] getStaleServers(long leaderMaxXid, List<QueryMaxXid> results) {
        List<Integer> stales = new ArrayList<Integer>();

        for (QueryMaxXid xid : results) {
            if (xid.getMaxXid() < leaderMaxXid) {
                stales.add(xid.getServerId());
            }
        }
        return stales.toArray(new Integer[0]);
    }

    /**
     * Transfer all client session(if any) to new leader
     */
    private void transferClientSession(int newLeader) {

        BatchMultiplexerSessions batchCreator = new BatchMultiplexerSessions();

        ClientSession[] clientSessions = componentManager.getSessionManager().getAllClientSessions();

        if (clientSessions != null) {
            for (ClientSession session : clientSessions) {
                if (session.isAlive()) {
                    batchCreator.add(session.getId());
                }
            }
        }

        if (batchCreator.containsSessions()) {
            Message message = new Message();
            message.setVerb(Verb.BOUND_MULTIPLEXER_SESSION);
            message.setBody(batchCreator);

            componentManager.getMessageService().sendOneway(message, newLeader);
        }
    }
}
