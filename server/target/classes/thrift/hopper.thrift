namespace java com.hopper.thrift

/**
 * RetryException indicates the client should retry after special period
 */
exception RetryException{
    1:i32 period
}

/**
 * AuthenticationException indicates the client login failure
 */
exception AuthenticationException {
    1:string message
}

/**
 * State CAS exception
 */
exception CASException {
	1:i32 type
}

exception NoStateNodeException {
    1:string key
}

/**
 * Hopper service interface
 */
service HopperService{
    /**
     * Login the client with userName/password
     */
    string login(1:string userName,2:string password) throws(1:RetryException re,2:AuthenticationException ae),

    /**
     * Re-Login with previous sessionId
     */
    void reLogin(1:string sessionId) throws(1:RetryException e),

    /**
     * logout the session identified by sessionId
     */
    void logout(1:string sessionId),

    /**
     * ping server
     */
    void ping(),

    /**
     * Create a state with initial value
     */
    void create(1:string key, 2:string owner, 3:i32 initStatus, 4:i32 invalidateStatus) throws(1:RetryException e),

    /**
     * Update the status bound with key with CAS condition
     */
    void updateStatus(1:string key, 2:i32 expectStatus, 3:i32 newStatus, 4:string owner,
    5:i32 lease) throws(1:RetryException re,2:CASException se),

    /**
     * Update the lease property bound with key with CAS condition
     */
    void expandLease(1:string key, 2:i32 expectStatus, 3:string owner, 4:i32 lease) throws(1:RetryException re,
    2:CASException se,3:NoStateNodeException nse),

    /**
     * Watch the special status(add a listener)
     */
    void watch(1:string key, 2:i32 expectStatus) throws(1:RetryException re,
    2:CASException ese,3:NoStateNodeException nse) ,

    /**
     * Callback method by server for notifying the status change
     */
    oneway void statusChange(1:i32 oldStatus,2:i32 newStatus)
}