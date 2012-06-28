package com.hopper.quorum;

import com.hopper.lifecycle.LifecycleEvent;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Endpoint;
import com.hopper.server.Server;
import com.hopper.server.Server.ElectionState;
import com.hopper.session.IncomingSession;

public class ElectionMonitor extends LifecycleProxy implements LifecycleListener {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private Server server = componentManager.getDefaultServer();

    private ElectionMonitorTask monitorTask;

    @Override
    protected void doInit() {
        monitorTask = new ElectionMonitorTask();
    }

    @Override
    protected void doStart() {
        componentManager.getScheduleManager().schedule(monitorTask, componentManager.getGlobalConfiguration()
                .getRpcTimeout(), componentManager.getGlobalConfiguration().getRpcTimeout());
    }

    @Override
    protected void doShutdown() {
        componentManager.getScheduleManager().removeTask(monitorTask);
    }

    @Override
    public String getInfo() {
        return "Election monitor";
    }

    class ElectionMonitorTask implements Runnable {

        @Override
        public void run() {

            // Discards all heart beat when looking for avoiding multiple
            // starting election
            if (server.getElectionState() == ElectionState.LOOKING || server.getElectionState() == ElectionState.SYNC) {
                return;
            }

            final int groupSize = componentManager.getGlobalConfiguration().getGroupEndpoints().length;
            final int quorumSize = componentManager.getGlobalConfiguration().getQuorumSize();

            IncomingSession[] sessions = componentManager.getSessionManager().getAllIncomingSessions();

            int missingCounter = groupSize - sessions.length;

            for (IncomingSession session : sessions) {
                if (!session.isAlive()) {
                    processSessionClose(session);
                    if (isMissingFollower(session)) {
                        missingCounter++;
                    }
                }
            }

            if (server.isLeader()) {

                // If leader is disconnecting from quorum follower
                if (missingCounter >= quorumSize) {

                    // unbound local leader
                    server.abandonLeadership();

                    // starting leader electing
                    componentManager.getLeaderElection().startElecting();
                }
            } else {

                // If current node is follower and has not found leader,
                // starting election
                if (!server.isKnownLeader()) {
                    // starting leader electing
                    componentManager.getLeaderElection().startElecting();
                }
            }
        }
    }

    private void processSessionClose(IncomingSession session) {
        Endpoint source = session.getConnection().getSourceEndpoint();

        // if local is follower and remote is leader, it indicating that current is disconnecting from
        // leader, it must start a leader election
        if (server.isFollower() && server.isLeader(source.serverId)) {

            // unbound local leader
            server.clearLeader();

            // close all session associated with source endpoint
            componentManager.getSessionManager().closeServerSession(source);

            // If local is follower and remote is leader, it indicating that current is disconnecting from a
            // follower node
        } else if (server.isFollower() && server.isFollower(source)) {

            // close all session associated with source endpoint
            componentManager.getSessionManager().closeServerSession(source);

            // If local is leader and remote is follower, it indicating that leader is disconnecting from follower
        } else if (server.isLeader() && server.isFollower(source)) {

            // close all session associated with source endpoint
            componentManager.getSessionManager().closeServerSession(source);

            // Both local and remote are leaders
        } else {

            // close all session associated with source endpoint
            componentManager.getSessionManager().closeServerSession(source);
        }
    }

    private boolean isMissingFollower(IncomingSession session) {
        Endpoint source = session.getConnection().getSourceEndpoint();
        return server.isLeader() && server.isFollower(source);
    }

    @Override
    public boolean support(LifecycleEvent.EventType eventType) {
        return eventType == LifecycleEvent.EventType.SHUTDOWN;
    }

    @Override
    public void lifecycle(LifecycleEvent event) {
        IncomingSession session = (IncomingSession) event.getSource();
        processSessionClose(session);
    }
}
