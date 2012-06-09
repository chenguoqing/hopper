package com.hopper.verb.handler;

import com.hopper.future.DefaultLatchFuture;
import com.hopper.server.ComponentManagerFactory;
import com.hopper.session.Message;
import com.hopper.verb.VerbHandler;

import java.util.concurrent.Future;

/**
 * The common response ver handler, it simply put the response message to
 * {@link Future}
 *
 * @author chenguoqing
 */
public class ReplyVerbHandler implements VerbHandler {

    @Override
    public void doVerb(Message message) {
        DefaultLatchFuture future = ComponentManagerFactory.getComponentManager().getCacheManager().get(message.getId
                ());

        if (future != null) {
            future.set(message);
        }
    }
}
