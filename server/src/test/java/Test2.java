import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-7-23
 * Time: 下午5:45
 * To change this template use File | Settings | File Templates.
 */
public class Test2 {
    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        Class<?> cz = list.getClass();

        TypeVariable<?>[] tvs = cz.getTypeParameters();
        for (TypeVariable<?> tv : tvs) {
            System.out.println(tv.getBounds());
        }
    }
}
