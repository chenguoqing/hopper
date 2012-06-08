package com.hopper.session;

import com.hopper.lifecycle.LifecycleEvent;
import com.hopper.lifecycle.LifecycleEvent.EventType;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.server.ServerFactory;
import com.hopper.verb.Verb;
import com.hopper.verb.handler.HeartBeat;
import com.hopper.utils.ScheduleManager;

public class LocalOutgoingSession extends SessionProxy implements OutgoingSession, LifecycleListener {

    /**
     * Schedule manager
     */
    private static final ScheduleManager scheduleManager = config.getScheduleManager();
    /**
     * Heart beat task
     */
    private final Runnable heartBeatTask = new HeartBeatTask();

    private volatile boolean isStartedBackground;

    @Override
    public boolean isAlive() {
        if (getConnection() == null) {
            return false;
        }

        return getConnection().validate();
    }

    @Override
    public boolean support(EventType eventType) {
        return eventType == EventType.SHUTDOWNING;
    }

    /**
     * When shutdowns {@link OutgoingSession}, should send close message
     * to remote server, then release local resources.
     */
    @Override
    public void lifecycle(LifecycleEvent event) {
        Message message = new Message();
        message.setId(Message.nextId());
        message.setVerb(Verb.UNBOUND_MULTIPLEXER_SESSION);
        message.setSessionId(getId());

        // notify endpoint
        this.sendOnwayUntilComplete(message);

        // remove self from session manager
        sessionManager.removeOutgoingServerSession(this);
    }

    @Override
    public void close() {

        super.close();

        scheduleManager.removeTask(heartBeatTask);

        isStartedBackground = false;
    }

    @Override
    public void background() {
        if (getConnection() == null || !isAlive()) {
            throw new IllegalStateException("Not bound connection or connetction has been closed.");
        }

        if (!isStartedBackground) {
            isStartedBackground = true;
            scheduleManager.schedule(heartBeatTask, config.getPingPeriod(), config.getPingPeriod());
            // start connection background tasks
            getConnection().background();
        }
    }

    private class HeartBeatTask implements Runnable {

        @Override
        public void run() {
            Message message = new Message();
            message.setId(Message.nextId());
            message.setVerb(Verb.HEART_BEAT);

            HeartBeat beat = new HeartBeat();
            beat.setLeader(ServerFactory.getDefaultServer().isLeader());
            beat.setMaxXid(ServerFactory.getDefaultServer().getStorage().getMaxXid());

            sendOneway(message);
        }
    }
}
