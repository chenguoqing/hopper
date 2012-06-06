package com.hopper.server.handler;

import com.hopper.GlobalConfiguration;
import com.hopper.quorum.Paxos;
import com.hopper.server.VerbHandler;
import com.hopper.session.Message;

public class AcceptVerbHandler implements VerbHandler {

	private GlobalConfiguration config = GlobalConfiguration.getInstance();
	private Paxos paxos = config.getDefaultServer().getPaxos();

	@Override
	public void doVerb(Message message) {

		Accept accpet = (Accept) message.getBody();

		if (accpet.getBallot() < paxos.getRnd()) {

		}
	}
}
