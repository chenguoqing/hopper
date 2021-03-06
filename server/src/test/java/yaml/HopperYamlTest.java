package yaml;

import junit.framework.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-6-8
 * Time: 下午1:28
 * To change this template use File | Settings | File Templates.
 */
public class HopperYamlTest {

    @Test
    public void test() throws Exception {
        Yaml yaml = new Yaml();

        InputStream in = getClass().getResourceAsStream("/hopper.yaml");

        Assert.assertNotNull(in);

        Object o = yaml.load(in);

        Assert.assertNotNull(o);

        Object v = ((Map<String, Object>) o).get("rpc_tcp");
        System.out.println(o);
    }
}
