package com.hopper.session;

import com.hopper.lifecycle.LifecycleProxy;
import com.hopper.server.ComponentManager;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.server.Endpoint;
import com.hopper.util.ScheduleManager;
import com.hopper.verb.Verb;
import com.hopper.verb.handler.HeartBeat;

/**
 * Snoops the outgoing session with fixed period
 */
public class SessionSnooper extends LifecycleProxy {

    private final ComponentManager componentManager = ComponentManagerFactory.getComponentManager();
    private final MessageService messageService = componentManager.getMessageService();
    private final ScheduleManager scheduleManager = componentManager.getScheduleManager();

    /**
     * Heart beat task
     */
    private final Runnable heartBeatTask = new HeartBeatTask();

    @Override
    protected void doStart() throws Exception {
        scheduleManager.schedule(heartBeatTask, 0, componentManager.getGlobalConfiguration().getPingPeriod());
    }

    @Override
    protected void doShutdown() throws Exception {
        scheduleManager.removeTask(heartBeatTask);
    }

    @Override
    public String getInfo() {
        return "session snooper";
    }

    private class HeartBeatTask implements Runnable {

        @Override
        public void run() {
            Message message = new Message();
            message.setVerb(Verb.HEART_BEAT);

            HeartBeat beat = new HeartBeat();
            beat.setLeader(componentManager.getDefaultServer().isLeader());
            beat.setMaxXid(componentManager.getStateStorage().getMaxXid());

            message.setBody(beat);

            for (Endpoint endpoint : componentManager.getGlobalConfiguration().getGroupEndpoints()) {
                if (!componentManager.getGlobalConfiguration().isLocalEndpoint(endpoint)) {
                    messageService.sendOneway(message, endpoint.serverId);
                }
            }
        }
    }
}
