package com.hopper.server.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.MessageService;
import com.hopper.future.LatchFuture;
import com.hopper.future.LatchFutureListener;
import com.hopper.quorum.Paxos;
import com.hopper.server.Endpoint;
import com.hopper.server.Server;
import com.hopper.server.Verb;
import com.hopper.server.VerbHandler;
import com.hopper.server.sync.DataSyncService;
import com.hopper.server.sync.DiffResult;
import com.hopper.server.sync.SyncException;
import com.hopper.session.ClientSession;
import com.hopper.session.Message;
import com.hopper.session.OutgoingServerSession;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LearnVerbHandler implements VerbHandler {

    /**
     * Singleton instance
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();

    private final Server server = config.getDefaultServer();
    /**
     * Data synchronization service
     */
    private final DataSyncService dataSyncService = config.getDataSyncService();
    /**
     * Paxos instance
     */
    private final Paxos paxos = server.getPaxos();

    private final Set<Integer> learnedInstances = new HashSet<Integer>();

    @Override
    public synchronized void doVerb(Message message) {

        Learn learn = (Learn) message.getBody();

        // Ignoring the smaller epoch or learned instance
        if (learn.getEpoch() < paxos.getEpoch() || learnedInstances.contains(learn.getEpoch())) {
            return;
        }

        learnedInstances.add(learn.getEpoch());

        if (learn.getEpoch() > paxos.getEpoch()) {
            paxos.setEpoch(learn.getEpoch());
        }

        server.setElectionState(Server.ElectionState.SYNC);

        paxos.closeInstance();

        int olderLeader = server.getLeader();
        server.setLeader(learn.getVval());

        try {
            if (server.isLeader()) {
                takeLeadership();
                server.setElectionState(Server.ElectionState.LEADING);
            } else {
                transferLeader(olderLeader, learn.getVval());
                server.setElectionState(Server.ElectionState.FOLLOWING);
            }
        } catch (Exception e) {
            config.getLeaderElection().startElecting();
        }
    }

    /**
     * Take over the leader ship
     */
    private void takeLeadership() {
        config.getDefaultServer().takeLeadership();
        startLeaderDataSync();
    }

    /**
     * Leader starts data synchronization
     */
    private void startLeaderDataSync() {
        Message message = new Message();
        message.setVerb(Verb.QUERY_MAX_XID);
        message.setId(Message.nextId());

        List<Message> replies = MessageService.sendMessageToQuorum(message, 1);

        int repliesNum = replies.size();

        // No majority response, starts a new election
        // Subsequently, the ElectionMonitor will execute the new election
        if (repliesNum < config.getQuorumSize() - 1) {
            config.getDefaultServer().setElectionState(Server.ElectionState.SYNC_FAILED);
            return;
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
        if (server.getStorage().getMaxXid() < result.getMaxXid()) {
            try {
                // Retrieve the diff from remote server
                LatchFuture<DiffResult> future = dataSyncService.diff(result.getServerId());
                DiffResult diff = future.get();

                // Apply the diff to local
                dataSyncService.applyDiff(diff);

                // Retrieve all stale servers(need synchronize to up-to-date)
                Integer[] staleServers = getStaleServers(server.getStorage().getMaxXid(), maxXidResult);

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
            } catch (InterruptedException e) {
                return;
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

    private void transferLeader(int olderLeader, int newLeader) throws Exception {

        // Inactive older leader
        inActiveOlderLeader(olderLeader);

        // Active new leader
        activeNewLeader(newLeader);

        // transfer all client sessions to new leader
        transferClientSession(newLeader);
    }

    /**
     * Inactive the older leader, close all bound sessions
     */
    private void inActiveOlderLeader(int oldLeader) {
        if (oldLeader < 0) {
            return;
        }

        // close all session
        config.getSessionManager().closeServerSession(config.getEndpoint(oldLeader));
    }

    /**
     * Start to execute some asynchronous works for leader session (starting for heart beat)
     */
    private void activeNewLeader(int leader) throws Exception {
        Endpoint leaderEndpoint = config.getEndpoint(leader);
        OutgoingServerSession session = config.getSessionManager().createLocalOutgoingSession(leaderEndpoint);

        // start heart beat
        session.background();
    }

    /**
     * Transfer all client session(if any) to new leader
     */
    private void transferClientSession(int newLeader) {

        BatchSessionCreator batchCreator = new BatchSessionCreator();

        ClientSession[] clientSessions = config.getSessionManager().getAllClientSessions();

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
            message.setId(Message.nextId());

            message.setBody(batchCreator);

            OutgoingServerSession serverSession = config.getSessionManager().getLocalOutgoingServerSession(config
                    .getEndpoint(newLeader));
            if (serverSession != null) {
                serverSession.sendOneway(message);
            }
        }
    }
}