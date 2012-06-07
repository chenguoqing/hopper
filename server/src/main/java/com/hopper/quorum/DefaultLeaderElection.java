package com.hopper.quorum;

import com.hopper.GlobalConfiguration;
import com.hopper.MessageService;
import com.hopper.server.Endpoint;
import com.hopper.server.Server.ElectionState;
import com.hopper.server.Verb;
import com.hopper.server.handler.*;
import com.hopper.session.Message;
import com.hopper.session.OutgoingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DefaultLeaderElection implements LeaderElection {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DefaultLeaderElection.class);
    /**
     * Singleton instance
     */
    private GlobalConfiguration config = GlobalConfiguration.getInstance();
    /**
     * Singleton instance
     */
    private Paxos paxos = config.getDefaultServer().getPaxos();

    @Override
    public void startElecting() {

        if (config.getDefaultServer().getElectionState() == ElectionState.LOOKING) {
            return;
        }

        config.getDefaultServer().setElectionState(ElectionState.LOOKING);

        int repeated = 0;
        boolean retry = false;

        do {
            repeated++;

            logger.debug("Retrying to start election  [" + repeated + "]...");

            try {
                List<QueryLeader> replyResult = new ArrayList<QueryLeader>();

                boolean initElection = canInitElection(replyResult);

                // Can initialize a new election?
                if (initElection) {
                    // Await completion for current running election
                    awaitIfNecessary();

                    // Start paxos progress normally
                    startPaxos();
                } else {

                    // Retrieve the candidate leader
                    int candidateLeader = getCandidateLeader(replyResult);

                    // if the candidate is alive, takes it as local leader.
                    // Otherwise, waiting a next period
                    testCandidateLeader(candidateLeader);

                    // if the candidate is alive, takes it as local leader.
                    shutdownPaxosWithNewLeader(candidateLeader);
                }
            } catch (NoQuorumException e) {
                logger.debug("No enough nodes are alive, waiting for other nodes joining...");
                retry = waitingNextElection(config.getPeriodForJoin());
            } catch (PaxosRejectedException e) {
                // If ballot is lower, it indicates other nodes are in progress, so it must wait for a moment
                if (e.reject == PaxosRejectedException.BALLOT_REJECT) {
                    logger.debug("Current ballot is lower, re-starts the paxos progress just a moment");
                    retry = waitingNextElection(config.getPeriodForPaxosRejected());
                    // If instance number is lower, it indicates other nodes had undergone some elections
                } else {
                    logger.debug("Current instance is lower, re-starts the paxos immediately.");
                    retry = true;
                }
            } catch (ElectionTerminatedException e) {
                retry = false;
            } catch (TestLeaderFailureException e) {
                retry = waitingNextElection(config.getPeriodForJoin());
            } catch (TimeoutException e) {
                retry = waitingNextElection(config.getPeriodForJoin());
            } catch (Exception e) {
                retry = waitingNextElection(config.getPeriodForJoin());
            }
        } while (retry);
    }

    /**
     * Whether current node can initialize a new election? It will communication
     * with all endpoints, if majority of them believe they can, it can initial;
     * otherwise, <b>must</b> be waiting.
     */
    private boolean canInitElection(final List<QueryLeader> replyResult) throws NoQuorumException,
            PaxosRejectedException {

        List<QueryLeader> queryResults = queryLeaders();

        // No majority nodes are alive
        if (queryResults.size() < config.getQuorumSize() - 1) {
            throw new NoQuorumException();
        }

        QueryLeader highestResult = queryResults.get(0);

        int localHighestEpoch = paxos.getEpoch();

        // This indicating that other nodes had undergone some election instances. This will occurs when the heart
        // beat timeout is very long.
        // If the higher instance number has been found, re-starting the paxos progress.
        if (highestResult.getEpoch() > localHighestEpoch) {
            localHighestEpoch = highestResult.getEpoch();
            paxos.updateInstance(localHighestEpoch);

            throw new PaxosRejectedException(PaxosRejectedException.INSTANCE_REJECT);
        }

        int numMissingLeader = 0;

        // All results that missing leader or epochs are less than localHighestEpoch will be taken as "missing leader"
        for (QueryLeader queryResult : queryResults) {
            if (!queryResult.hasLeader() || queryResult.getEpoch() < localHighestEpoch) {
                numMissingLeader++;
            }
        }

        replyResult.addAll(queryResults);

        // If majority misses leader, must re-start election
        return numMissingLeader >= config.getQuorumSize() - 1;
    }

    private List<QueryLeader> queryLeaders() {
        Message message = new Message();
        message.setVerb(Verb.QUERY_LEADER);
        message.setId(Message.nextId());

        List<Message> replies = MessageService.sendMessageToQuorum(message, 0);

        List<QueryLeader> leaders = new ArrayList<QueryLeader>(replies.size());

        for (Message reply : replies) {
            leaders.add((QueryLeader) reply.getBody());
        }

        // Sort leaders by epoch
        Collections.sort(leaders, new Comparator<QueryLeader>() {
            @Override
            public int compare(QueryLeader leader1, QueryLeader leader2) {
                return leader2.getEpoch() - leader1.getEpoch();
            }
        });

        return leaders;
    }

    private void shutdownPaxosWithNewLeader(int candidateLeader) throws Exception {

        // Active the heart beat to leader
        activeLeaderSession(candidateLeader);

        // set the new leader
        config.getDefaultServer().setLeader(candidateLeader);

        // close current instance
        paxos.closeInstance();

        // unbound the election status
        config.getDefaultServer().setElectionState(ElectionState.FOLLOWING);
    }

    /**
     * Start to mutation some asynchronous works for leader session (starting for heart beat)
     */
    private void activeLeaderSession(int leader) throws Exception {
        Endpoint leaderEndpoint = config.getEndpoint(leader);
        OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(leaderEndpoint);

        // start heart beat
        session.background();
    }

    /**
     * Await completion for current running election. The behind idea is
     * avoiding competing.
     */
    private void awaitIfNecessary() throws TimeoutException {
        // It indicating that local server has voted for current instance, as
        // optimization it may not start a new election, instead of waiting the
        // electing complete.
        if (paxos.isVoted()) {
            // It indicating that election is running
            if (!config.getDefaultServer().isKnownLeader()) {
                // Waiting for the election complete
                config.getDefaultServer().getLeaderWithLock(config.getWaitingPeriodForElectionComplete());
            }
        }
    }

    /**
     * Starting paxos algorithm for leader election
     */
    private void startPaxos() {

        int leader = phase1();

        // If the serverId from Phase1b is not a local server, it indicating
        // there some contention and may be other servers have completed Phase1.
        // As optimization current server may abandon the subsequent election
        // steps.
        if (leader != config.getDefaultServer().getServerEndpoint().serverId) {
        }

        // starting phase2
        phase2(leader);
    }

    private int phase1() {

        // Executing Phase1a(Prepare)
        List<Message> replies = prepare();

        int promiseCount = 0;
        int rejectBallot = -1;
        int rejectEpoch = -1;

        // Sort all promise results by vrnd(voted round)
        Collections.sort(replies, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                Promise p1 = (Promise) m1.getBody();
                Promise p2 = (Promise) m2.getBody();

                return p2.getVrnd() - p1.getVrnd();
            }
        });

        for (Message reply : replies) {
            Promise promise = (Promise) reply.getBody();
            if (promise.getStatus() == Promise.PROMISE) {
                promiseCount++;
            } else if (promise.getStatus() == Promise.REJECT_BALLOT) {
                rejectBallot = Math.max(rejectBallot, promise.getRnd());
            } else {
                rejectEpoch = Math.max(rejectEpoch, promise.getEpoch());
            }
        }

        // if majority has promised the prepare
        if (promiseCount >= config.getQuorumSize()) {
            int leader = ((Promise) replies.get(0).getBody()).getVval();

            // Majority no chosen any value, we can decide any value, otherwise,
            // it must pick up the first value
            if (leader == -1) {
                leader = paxos.getVval() > 0 ? paxos.getVval() : config.getDefaultServer().getServerEndpoint().serverId;
            }

            return leader;
        }

        int reject = 0;

        // Majority has rejected the prepare
        // if local epoch is smaller
        if (rejectEpoch > 0) {
            reject = PaxosRejectedException.INSTANCE_REJECT;
            paxos.updateInstance(rejectEpoch);

            // local ballot is smaller
        } else if (rejectBallot > 0) {
            reject = PaxosRejectedException.BALLOT_REJECT;
            paxos.setRnd(rejectBallot);
        }

        // restart phase1
        throw new PaxosRejectedException(reject);
    }

    /**
     * Execute Paxos prepare(Phase1a)
     *
     * @throws NoQuorumException           If no enough no-fault nodes are alive
     * @throws ElectionTerminatedException If election has been terminated
     */
    private List<Message> prepare() throws NoQuorumException, ElectionTerminatedException {

        if (config.getDefaultServer().getElectionState() != ElectionState.LOOKING) {
            throw new ElectionTerminatedException();
        }

        // send prepare(Phase1a) message
        Message phase1 = new Message();
        phase1.setId(Message.nextId());
        phase1.setVerb(Verb.PAXOS_PREPARE);

        Prepare prepare = new Prepare();

        int ballot = BallotGenerator.generateBallot(config.getLocalBallotServerId(),
                config.getConfigedEndpoints().length, paxos.getRnd());

        paxos.setRnd(ballot);

        prepare.setBallot(ballot);
        prepare.setEpoch(paxos.getEpoch());
        phase1.setBody(prepare);

        // Receive the promise(Phase1b) message
        List<Message> replies = MessageService.sendMessageToQuorum(phase1, 0);

        // if no majority responses, it can't work
        if (replies.size() < config.getQuorumSize() - 1) {
            throw new NoQuorumException();
        }

        return replies;
    }

    private void phase2(int leader) {

        if (config.getDefaultServer().getElectionState() != ElectionState.LOOKING) {
            throw new ElectionTerminatedException();
        }

        Message message = new Message();
        message.setVerb(Verb.PAXOS_ACCEPT);
        message.setId(Message.nextId());

        Accept accept = new Accept();
        accept.setEpoch(paxos.getEpoch());
        accept.setVval(leader);
        accept.setBallot(paxos.getRnd());

        message.setBody(accept);

        List<Message> replies = MessageService.sendMessageToQuorum(message, 0);

        // No majority are alive
        if (replies.size() < config.getQuorumSize()) {
            throw new NoQuorumException();
        }

        int numAccepted = 0;

        int rejectedRnd = -1;
        int rejectedEpoch = -1;

        for (Message reply : replies) {
            Accepted accepted = (Accepted) reply.getBody();
            if (accepted.getStatus() == Accepted.ACCEPTED) {
                numAccepted++;
            } else if (accepted.getStatus() == Accepted.REJECT_BALLOT) {
                rejectedEpoch = Math.max(rejectedEpoch, accepted.getRnd());
            } else if (accepted.getStatus() == Accepted.REJECT_EPOCH) {
                rejectedEpoch = Math.max(rejectedEpoch, accepted.getEpoch());
            }
        }

        // Majority has reached consensus, send LEARN message
        if (numAccepted > config.getQuorumSize() - 1) {

            Message learnMessage = makeLearnMessage();
            // Send Learn message to all nodes
            MessageService.sendLearnMessage(learnMessage);
            return;
        }

        if (rejectedEpoch != -1) {
            paxos.updateInstance(rejectedEpoch);
            throw new PaxosRejectedException(PaxosRejectedException.INSTANCE_REJECT);
        } else if (rejectedRnd != -1) {
            paxos.setRnd(rejectedRnd);
            throw new PaxosRejectedException(PaxosRejectedException.BALLOT_REJECT);
        }
    }

    /**
     * Make a learn message
     */
    private Message makeLearnMessage() {

        Message message = new Message();
        message.setVerb(Verb.PAXOS_LEARN);
        message.setId(Message.nextId());

        Learn learn = new Learn();
        learn.setEpoch(paxos.getEpoch());
        learn.setProposer(config.getDefaultServer().getEndPoint().serverId);
        learn.setVval(paxos.getVval());

        return message;
    }

    /**
     * Test the candidate leader, if can communicate with the candidate leader
     * and the candidate owns the leadership, then setting the candidate as the
     * local leader. Otherwise, waiting the next communication period for
     * retrying.
     */
    private void testCandidateLeader(int leader) {

        Endpoint leaderEndpoint = config.getEndpoint(leader);

        try {
            OutgoingSession session = config.getSessionManager().createLocalOutgoingSession(leaderEndpoint);

            Message request = new Message();
            request.setVerb(Verb.TEST_LEADER);
            request.setId(Message.nextId());

            Future<Message> future = session.send(request);

            Message reply = future.get(config.getRpcTimeout(), TimeUnit.MILLISECONDS);
            byte[] body = (byte[]) reply.getBody();

            boolean isLeader = body[0] == 0;

            // Candidate is not a leader
            if (!isLeader) {
                throw new TestLeaderFailureException();
            }
        } catch (TestLeaderFailureException e) {
            logger.debug("Test leader failure,Candidate leader has loosen leadership.");
            throw e;
        } catch (Exception e) {
            logger.debug("Failed to test leader " + leaderEndpoint, e);
            throw new TestLeaderFailureException();
        }
    }

    /**
     * If majority nodes believe the own leader is alive(may be different from
     * each other), we takes the leader of holds maximum xid as the candidate
     * leader
     */
    private int getCandidateLeader(List<QueryLeader> replies) {

        for (QueryLeader queryResult : replies) {
            if (queryResult.hasLeader()) {
                return queryResult.getLeader();
            }
        }

        return -1;
    }

    private boolean waitingNodesJoining() {
        try {
            Thread.sleep(config.getPeriodForRetryElection());
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    private boolean waitingNextElection(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }
}
