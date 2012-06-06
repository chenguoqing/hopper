package com.hopper.session;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * The common interface for serializer
 * 
 * @author chenguoqing
 * 
 */
public interface Serializer {

	/**
	 * Serialize <code>this</code> object to byte array(high-order)
	 */
	void serialize(DataOutput out) throws IOException;

	/**
	 * Deserialize the byte array to current instance
	 */
	void deserialize(DataInput in) throws IOException;
}
