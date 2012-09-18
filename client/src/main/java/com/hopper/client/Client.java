package com.hopper.client;

import com.hopper.client.thrift.HopperServiceCallback;
import com.hopper.client.thrift.MessageReceiverThread;
import com.hopper.thrift.HopperService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-9-13
 * Time: 下午4:34
 * To change this template use File | Settings | File Templates.
 */
public class Client {
    private static final int LOGIN_RETRY_COUNT = 3;
    private final Timer timer = new Timer();

    private final String address;
    private final int port;
    private final String userName;
    private final String password;
    private final HopperServiceCallback callback;
    private final TTransport transport;
    private final TProtocol protocol;
    private final HopperService.Iface serviceProxy;
    private final MessageReceiverThread thread;
    private String sessionId;
    private final AtomicBoolean started = new AtomicBoolean();

    public Client(String host, int port, HopperServiceCallback callback) {
        this(host, port, null, null, callback);
    }

    public Client(String host, int port, String userName, String password, HopperServiceCallback callback) {
        this.address = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.callback = callback;

        this.transport = new TSocket(host, port);
        this.protocol = new TBinaryProtocol(transport);
        this.serviceProxy = new HopperService.Client(protocol);
        thread = new MessageReceiverThread(protocol, callback);
    }

    public void start() throws Exception {
        if (started.compareAndSet(false, true)) {
            this.transport.open();

            login();

            //this.timer.schedule(new HeartbeatTask(), 0, 1000);
            this.thread.start();
        } else {
            throw new InvalidStateException("Client has started.");
        }
    }

    private void login() throws Exception {
        RetryTaskExecutor.execute(LOGIN_RETRY_COUNT, new LoginCallback());
    }

    public void shutdown() {
        if (started.compareAndSet(false, true)) {
            this.timer.cancel();
            this.thread.shutdown();
            this.transport.close();
        } else {
            throw new InvalidStateException("Client has shutdown.");
        }
    }

    public HopperService.Iface getServiceProxy() {

        if (!started.get()) {
            throw new InvalidStateException("Client has not started yet.");
        }
        return serviceProxy;
    }

    class HeartbeatTask extends TimerTask {
        @Override
        public void run() {
            try {
                serviceProxy.ping();
            } catch (TException e) {

            }
        }
    }

    class LoginCallback implements Callable<String> {
        @Override
        public String call() throws Exception {
            return serviceProxy.login(userName, password);
        }
    }
}
