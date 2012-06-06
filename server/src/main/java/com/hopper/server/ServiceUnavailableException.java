package com.hopper.server;

/**
 * ServiceUnavailableException indicates current server is unavailable for service,
 * if the server is in electing or starting, the exception will occurred.
 */
public class ServiceUnavailableException extends Exception {
    /**
     * Empty constructor
     */
    public ServiceUnavailableException() {
    }

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
