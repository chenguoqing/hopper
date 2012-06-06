package com.hopper.server.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.future.DefaultLatchFuture;
import com.hopper.server.VerbHandler;
import com.hopper.session.Message;

import java.util.concurrent.Future;

/**
 * The common response ver handler, it simply put the response message to
 * {@link Future}
 * 
 * @author chenguoqing
 * 
 */
public class ReplyVerbHandler implements VerbHandler {

	@Override
	public void doVerb(Message message) {
		DefaultLatchFuture future = GlobalConfiguration.getInstance().getCacheManager().get(message.getId());

		if (future != null) {
			future.set(message);
		}
	}
}
