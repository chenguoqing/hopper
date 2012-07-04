package com.hopper.session;

import junit.framework.Assert;
import org.jboss.netty.buffer.ChannelBuffer;
import org.junit.Test;

/**
 * Test cases for {@link BufferDataOutput}
 */
public class BufferDataOutputTest {

    @Test
    public void test() throws Exception {
        BufferDataOutput output = new BufferDataOutput();
        output.writeByte((byte) 1);
        output.writeBoolean(false);
        output.writeBoolean(true);
        output.writeUTF("hello你好");
        output.writeUTF(null);

        output.complete();

        final ChannelBuffer buffer = output.buffer();

        BufferDataInput input = new BufferDataInput(buffer);

        Assert.assertEquals(input.readByte(), 1);
        Assert.assertFalse(input.readBoolean());
        Assert.assertTrue(input.readBoolean());
        Assert.assertEquals(input.readUTF(), "hello你好");
        Assert.assertNull(input.readUTF());
    }
}
