package com.hopper.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-6-14
 * Time: 下午1:35
 * To change this template use File | Settings | File Templates.
 */
public class MurmurHashTest {

    @Test
    public void testHash() {

        List<Integer> hashs = new ArrayList<Integer>(1000000);
        long t = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            String str = "abcad013256489700054123sdjklfasfjskdfasfkhasfkjhsdfk;jfkljasfaklsjdfhkl" + i;

//            int hash = str.hashCode();
            int hash = MurmurHash.hash(str);
            hashs.add(hash);
        }

        System.out.println(System.currentTimeMillis() - t);

        Collections.sort(hashs);

        int collisionCount = 0;
        for (int i = 1; i < hashs.size(); i++) {
            int c1 = hashs.get(i - 1);
            int c2 = hashs.get(i);

            if (c2 == c1) {
                collisionCount++;
            }
        }


        System.out.println("collision count:"+collisionCount);
    }
}
