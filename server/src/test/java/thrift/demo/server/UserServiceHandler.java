package thrift.demo.server;

import org.apache.thrift.TException;
import thrift.demo.gen.User;
import thrift.demo.gen.UserNotFoundException;
import thrift.demo.gen.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-5-29
 * Time: 下午5:34
 * To change this template use File | Settings | File Templates.
 */
public class UserServiceHandler implements UserService.Iface {
    @Override
    public List<User> getUsers() throws TException {
        List<User> list = new ArrayList<User>();
        User user = new User();
        user.setId(1);
        user.setUsername("user1");
        user.setPassword("pwd1");
        list.add(user);
        User user2 = new User();
        user2.setId(1);
        user2.setUsername("user2");
        user2.setPassword("pwd2");
        list.add(user2);

        return list;
    }

    @Override
    public User getUserByName(String username) throws UserNotFoundException, TException {
        if ("user1".equals(username)) {
            User user = new User();
            user.setId(1);
            user.setUsername("user1");
            user.setPassword("pwd1");
            return user;
        }
        throw new UserNotFoundException("test exception");
    }
}
