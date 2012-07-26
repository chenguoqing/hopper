package com.hopper.storage;

public class StateRegisterException extends RuntimeException {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -3102384840011250707L;
	public final int expectStatus;
	public final int actualStatus;

	public StateRegisterException(int expectStatus, int actualStatus) {
		this.expectStatus = expectStatus;
		this.actualStatus = actualStatus;
	}
}
