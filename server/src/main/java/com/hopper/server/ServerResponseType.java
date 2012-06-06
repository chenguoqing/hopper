package com.hopper.server;

public enum ServerResponseType {
	/**
	 * Ping result
	 */
	PING_REQ(0),
	/**
	 * Duplicate response
	 */
	DUPLICATE_REQ(1),
	/**
	 * Leader ship loss
	 */
	LEADERSHIP_LOSS(2),
	/**
	 * The leader is older
	 */
	OLDER_LEADER(3),
	/**
	 * Proposed instance number is lower
	 */
	PAXOS_PHAE1B(4),
	/**
	 * Paxos phase2b
	 */
	PAXOS_PHASE2B(5),
	/**
	 * Learn response
	 */
	PAXOS_LEARN_REQ(6);

	public final int type;

	ServerResponseType(int type) {
		this.type = type;
	}

	public static ServerResponseType getVerb(int type) {
		for (ServerResponseType v : ServerResponseType.values()) {
			if (v.type == type) {

				return v;
			}
		}
		return null;
	}
}
