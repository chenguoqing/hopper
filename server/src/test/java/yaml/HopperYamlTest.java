package yaml;

import junit.framework.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

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

        InputStream in = getClass().getResourceAsStream("/conf/hopper.yaml");

        Assert.assertNotNull(in);

        Object o = yaml.load(in);

        Assert.assertNotNull(o);

        System.out.println(o);
    }
}
