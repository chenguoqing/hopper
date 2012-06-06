package thrift.demo.client;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import thrift.demo.gen.User;
import thrift.demo.gen.UserNotFoundException;
import thrift.demo.gen.UserService;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-29
 * Time: 下午5:41
 * To change this template use File | Settings | File Templates.
 */
public class UserClient {
    public static void main(String[] arsg) {

        String address = "127.0.0.1";
        int port = 8989;
        int clientTimeout = 30000;
        TTransport transport = new TFramedTransport(new TSocket(address, port, clientTimeout));
        TProtocol protocol = new TCompactProtocol(transport);
        UserService.Client client = new UserService.Client(protocol);

        try {
            transport.open();
            System.out.println(client.getUserByName("user3"));

            List<User> users = client.getUsers();
            for (User user : users) {
                System.out.println(user);
            }
        } catch (TApplicationException e) {
            System.out.println(e.getMessage() + " " + e.getType());
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        } catch (TException e) {
            e.printStackTrace();
        }
        transport.close();
    }
}
