package test;

import org.apache.commons.modeler.Registry;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-8-15
 * Time: 下午2:31
 * To change this template use File | Settings | File Templates.
 */
public class TestModeler {

    public static void main(String[] args) throws Exception {

        final TestUser user = new TestUser();
        user.setName("zhangsan");
        user.setPhone("15900231564");

        final ObjectName on = new ObjectName("Test:name=" + user.getName());
        Registry.getRegistry(null, null).registerComponent(user, on, null);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Set<ObjectInstance> objs = Registry.getRegistry(null, null).getMBeanServer().queryMBeans(on, null);

                    System.out.println(objs == null);

                    for (ObjectInstance oi : objs) {
                        System.out.println(oi.getObjectName());
                    }
                    System.out.println(Registry.getRegistry(null, null).getMBeanServer().getAttribute(on, "phone"));
                    System.out.println(Registry.getRegistry(null, null).getMBeanServer().getAttribute(on, "name"));
                    String[] domains = Registry.getRegistry(null, null).getMBeanServer().getDomains();

                    if (domains != null) {
                        for (String domain : domains) {
                            System.out.println(domain);
                        }
                    }
                    Thread.sleep(1000000L);
                } catch (Exception e) {
                }
            }
        });

        t.start();
    }

}
