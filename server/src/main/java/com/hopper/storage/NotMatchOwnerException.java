package com.hopper.storage;

/**
 * Created with IntelliJ IDEA. User: chenguoqing Date: 12-6-1 Time: 下午5:59 To
 * change this template use File | Settings | File Templates.
 */
public class NotMatchOwnerException extends RuntimeException {
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 38171081207116388L;
	public final String expectOwner;
	public final String actualOwner;

	public NotMatchOwnerException(String expectOwner, String actualOwner) {
		this.expectOwner = expectOwner;
		this.actualOwner = actualOwner;
	}
}
