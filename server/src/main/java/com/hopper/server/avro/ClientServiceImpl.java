package com.hopper.server.avro;

import com.hopper.GlobalConfiguration;
import com.hopper.server.Server;
import com.hopper.server.thrift.ChannelBound;
import com.hopper.session.ClientConnection;
import com.hopper.session.ClientSession;
import com.hopper.session.SessionIdGenerator;
import com.hopper.storage.StateNode;
import com.hopper.storage.StateStorage;
import com.hopper.storage.StatusNoMatchException;
import org.apache.avro.AvroRemoteException;

/**
 * The implementation of StateService for defining a high level interface for state storage interactive.
 */
public class ClientServiceImpl implements ClientService {
    /**
     * Singleton
     */
    private final GlobalConfiguration config = GlobalConfiguration.getInstance();

    private final Server server = config.getDefaultServer();
    /**
     * Singleton
     */
    private final StateStorage storage = config.getDefaultServer().getStorage();

    /**
     * Allocates the session id for connected client
     */
    @Override
    public CharSequence connect() throws AvroRemoteException {

        ClientSession session = config.getSessionManager().getClientSession(ChannelBound.get());

        if (session != null) {
            return session.getId();
        }

        String sessionId = SessionIdGenerator.generateSessionId();

        ClientConnection conn = new ClientConnection(ChannelBound.get());
        session = new ClientSession();
        session.setId(sessionId);
        session.setConnection(conn);
        conn.setSession(session);

        // Add session to session manager
        session.getSessionManager().addClientSession(session);

        return sessionId;
    }

    @Override
    public int reconnect(CharSequence sessionId) throws AvroRemoteException {

        if (sessionId == null || sessionId.length() == 0) {
            throw new AvroRemoteException("sessionId is null.");
        }

        config.getSessionManager().removeClientSession(sessionId.toString());
        ClientConnection conn = new ClientConnection(ChannelBound.get());
        ClientSession session = new ClientSession();
        session.setId(sessionId.toString());
        session.setConnection(conn);
        conn.setSession(session);

        // Add session to session manager
        session.getSessionManager().addClientSession(session);
        return 0;
    }

    @Override
    public void disconnect() {
        ClientSession session = config.getSessionManager().getClientSession(ChannelBound.get());

        if (session != null) {
            session.getSessionManager().removeClientSession(session.getId());
            session.close();
        }
    }

    @Override
    public CharSequence ping() throws AvroRemoteException {
        ClientSession session = config.getSessionManager().getClientSession(ChannelBound.get());
        session.heartBeat();
        return "0";
    }

    /**
     * Create a state node, delegates the operation to StateStorage
     */
    @Override
    public int create(CharSequence key, CharSequence owner, int initStatus,
                      int invalidateStatus) throws AvroRemoteException {
        StateNode node = new StateNode(key.toString(), StateNode.TYPE_TEMP, StateNode.DEFAULT_STATUS,
                StateNode.DEFAULT_INVALIDATE_STATUS, config.getDefaultServer().getPaxos().getEpoch());
        storage.put(node);
        return 0;
    }

    /**
     * Update the state status, delegates to StateNode
     */
    @Override
    public int updateStatus(CharSequence key, int expectStatus, int newStatus, CharSequence owner,
                            int lease) throws AvroRemoteException {
        String sKey = key.toString().intern();
        StateNode node = getAndCreate(key.toString());

//        boolean r = node.setStatus(expectStatus, newStatus, owner == null ? null : owner.toString(), lease);
//        return r ? 0 : 1;
        return 0;
    }

    /**
     * Update the state node's lease, delegates to StateNode
     */
    @Override
    public int updateLease(CharSequence key, int expectStatus, CharSequence owner,
                           int lease) throws AvroRemoteException {
        StateNode node = getAndCreate(key.toString());

        boolean r = node.expandLease(expectStatus, owner == null ? null : owner.toString(), lease);
        return r ? 0 : 1;
    }

    /**
     * Watches the state node(add state listener for this node)
     */
    @Override
    public int watch(CharSequence key, int expectStatus) throws AvroRemoteException {
        StateNode node = getAndCreate(key.toString());
        try {
            node.watch(expectStatus);
        } catch (StatusNoMatchException e) {
            return 1;
        }
        return 0;
    }

    private StateNode getAndCreate(String key) {
        StateNode node = storage.get(key);

        if (node == null) {
            synchronized (key) {
                node = storage.get(key);
                node = new StateNode(key, config.getDefaultServer().getPaxos().getEpoch());
                storage.put(node);
            }
        }

        return node;
    }

    //    private boolean needTransfer()
}
