package com.hopper.quorum;

/**
 * The exception occurs when the ballot or epoch is rejected because of smaller than other nodes  .
 * The exception indicates the election should be re-start after some period.
 */
public class PaxosRejectedException extends RuntimeException {
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -8219717711088633182L;

    /**
     * Reject reason : lower instance number
     */
    public static final int INSTANCE_REJECT = 0;
    /**
     * Reject reason: lower ballot number
     */
    public static final int BALLOT_REJECT = 1;

    public final int reject;

    public PaxosRejectedException(int reject) {
        this.reject = reject;
    }
}
