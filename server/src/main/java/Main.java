import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-4-27
 * Time: 下午6:25
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        int[] a1 = {2, 3, 4, 7, 12, 13};
        int[] a2 = {3, 5, 7, 8, 9, 13};

        int i = 0;
        int j = 0;

        List<Integer> l1 = new ArrayList<Integer>();
        List<Integer> l2 = new ArrayList<Integer>();
        List<Integer> l3 = new ArrayList<Integer>();

        while (i < a1.length && j < a2.length) {
            if (a1[i] == a2[j]) {
                l1.add(a1[i]);
                i++;
                j++;
            } else if (a1[i] < a2[j]) {
                l2.add(a1[i]);
                i++;
            } else {
                l3.add(a2[j]);
                j++;
            }
        }

        if (i < a1.length) {
            for (; i < a1.length; i++) {
                l2.add(a1[i]);
            }
        } else if (j < a2.length) {
            for (; j < a2.length; j++) {
                l3.add(a2[j]);
            }
        }

        System.out.println("====Equal======");
        System.out.println(l1);
        System.out.println("====Added======");
        System.out.println(l2);
        System.out.println("====Removed======");
        System.out.println(l3);
    }
}
