package com.hopper.quorum;

/**
 * {@link BallotGenerator} will generate a total order ballot number for every
 * server.
 * 
 * In a system with n replicas, assign each replica r a unique id i<sub>r</sub>
 * between 0 and n-1. Replica r picks the smallest sequence number s larger than
 * any it has seen such that s mod n = i<sub>r</sub>.(or, s = k*n+i<sub>r</sub>,
 * which from google)
 * 
 * @author chenguoqing
 */
public class BallotGenerator {

	/**
	 * Generate a ballot for serverId by above rules.
	 * 
	 * @param serverBallotId
	 *            server ballot id that will generate ballot(0<=serverBallotId<=
	 *            <tt>serverCount</tt>-1)
	 * @param serverCount
	 *            total server count
	 * @param initialBallot
	 *            the initial ballot, the new ballot should great than it.
	 * @return new ballot
	 */
	public static final int generateBallot(int serverBallotId, int serverCount, int initialBallot) {

		if (initialBallot <= 0) {
			return serverBallotId;
		}

		int k = (initialBallot - serverBallotId) / serverCount + 1;

		return k * serverCount + serverBallotId;
	}
}
