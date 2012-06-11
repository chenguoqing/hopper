package com.hopper;

import com.hopper.server.Endpoint;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-6-11
 * Time: 下午1:43
 * To change this template use File | Settings | File Templates.
 */
public class GlobalConfigurationTest {

    @Test
    public void test() throws Exception {
        GlobalConfiguration configuration = new GlobalConfiguration();
        configuration.initialize();
        configuration.start();

        Map<String, Object> rpcTcpSettings = configuration.getRpcTcpSettings();
        Assert.assertNotNull(rpcTcpSettings);

        Assert.assertEquals(rpcTcpSettings.size(), 3);

        Assert.assertNotNull(rpcTcpSettings.get("child.tcpNoDelay"));
        Assert.assertEquals(rpcTcpSettings.get("child.tcpNoDelay"), true);

        Assert.assertNotNull(configuration.getGroupEndpoints());
        Assert.assertEquals(configuration.getGroupEndpoints().length, 3);
        int index = 1;
        for (Endpoint endpoint : configuration.getGroupEndpoints()) {
            Assert.assertEquals(endpoint.serverId, index++);
        }

        Assert.assertEquals(configuration.getQuorumSize(), 2);
    }
}
