package com.hopper.session;

import com.hopper.future.LatchFuture;
import com.hopper.lifecycle.LifecycleListener;
import com.hopper.lifecycle.LifecycleMBeanProxy;
import com.hopper.server.ComponentManagerFactory;

public abstract class SessionProxy extends LifecycleMBeanProxy implements Session {
    /**
     * Singleton SessionManager instance
     */
    protected SessionManager sessionManager = ComponentManagerFactory.getComponentManager().getSessionManager();

    /**
     * Associated connection
     */
    private Connection connection;

    /**
     * Unique session id
     */
    private String id;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public void sendOneway(Message message) {
        if (connection == null) {
            throw new IllegalStateException("Not bound connection.");
        }

        connection.sendOneway(message);
    }

    @Override
    public void sendOnewayUntilComplete(Message message) {
        if (connection == null) {
            throw new IllegalStateException("Not bound connection.");
        }

        connection.sendOnewayUntilComplete(message);
    }

    @Override
    public LatchFuture<Message> send(Message message) {
        if (connection == null) {
            throw new IllegalStateException("Not bound connection.");
        }

        return connection.send(message);
    }

    @Override
    protected void doShutdown() {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public void setSessionManager(SessionManager manager) {
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setConnection(Connection connection) {
        if (connection != null) {
            this.connection = connection;
            if (this instanceof LifecycleListener) {
                this.connection.addListener((LifecycleListener) this);
            }
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String getInfo() {
        return getClass().getSimpleName();
    }
}
