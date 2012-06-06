package com.hopper.quorum;

/**
 * {@link NoQuorumException} represents one case that there are no enough no-faulty nodes ,
 * the election algorithm doesn't work.
 * <p/>
 * If the exception occurs, it must wait until enough nodes are joining
 */
public class NoQuorumException extends RuntimeException {

    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -4480302035154736459L;

}
