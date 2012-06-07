package com.hopper.session;

import com.hopper.common.lifecycle.Lifecycle;
import com.hopper.future.LatchFuture;
import com.hopper.server.Endpoint;
import org.jboss.netty.channel.Channel;

import java.util.concurrent.Future;

/**
 * <p>
 * The {@link Connection} representing a TCP communications between server and
 * server or client and server. In server-to-server, there are two connections:
 * incoming connection and outgoing connection. Incoming connection will only be
 * used for sending messages; Outgoing connection only be used for receiving
 * messages. But we use the {@link Connection} interface representing the two
 * different connections, and the "incoming" and "outgoing" characteristics will
 * be implemented in {@link IncomingSession} and
 * {@link OutgoingSession}.
 * </p>
 * 
 * <p>
 * In order to check the target whether or not is alive, it must send some heart
 * beats to target,for simplifying the communication mechanism, it only hears
 * beat on Outgoing connection({@link OutgoingSession})
 * </p>
 * 
 * @author chenguoqing
 * 
 */
public interface Connection extends Lifecycle {

	/**
	 * Retrieve the associated {@link Channel} object
	 */
	Channel getChannel();

	/**
	 * Initialize the connection with the associated {@link Session}
	 */
	void setSession(Session session);

	/**
	 * Return the source end point
	 */
	Endpoint getSourceEndpoint();

	/**
	 * Return the target end point
	 */
	Endpoint getDestEndpoint();

	/**
	 * Construct the TCP connection between source end point and target.
	 */
	void connect();

	/**
	 * Re-construct the connection between source and target. And the existing
	 * sessions will be keep in new connection.
	 */
	void reconnect();

	/**
	 * Close the connection
	 */
	void close();

	/**
	 * Check if the connection is alive. The method should not check for
	 * instance(sending message to target), it should return the asynchronous
	 * heart beat result. result
	 */
	boolean validate();

	/**
	 * Returns true if this connection is secure.
	 * 
	 * @return true if the connection is secure (e.g. SSL/TLS)
	 */
	boolean isSecure();

	/**
	 * Send asynchronous message (without waiting for operation complete), if
	 * exceptions occurs, simply logging it.
	 */
	void sendOneway(Message message);

	/**
	 * Send synchronous message( waiting until operation complete), if
	 * exceptions occurs, {@link RuntimeException} will be thrown.
	 */
	void sendOnwayUntilComplete(Message message);

	/**
	 * Send asynchronous message(without waiting for complete) and return the
	 * {@link Future} immediately. if exceptions occurs(includes local and
	 * remote exceptions), {@link RuntimeException} will be thrown when
	 * {@link Future#get()} method has been invoked.
	 */
	LatchFuture<Message> send(Message message);

	/**
	 * Starting the background thread for asynchronous processing for the
	 * connection.
	 */
	void background();
}
