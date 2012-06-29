import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: chenguoqing
 * Date: 12-6-29
 * Time: 下午3:43
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    private static final int COUNT_BITS = Integer.SIZE - 3;
    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    private static final int RUNNING = -1 << COUNT_BITS;
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    private static final int STOP = 1 << COUNT_BITS;
    private static final int TIDYING = 2 << COUNT_BITS;
    private static final int TERMINATED = 3 << COUNT_BITS;

    // Packing and unpacking ctl
    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }

    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }

    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }
    /**
     * Attempt to CAS-increment the workerCount field of ctl.
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    public static void main(String[] args) {

        new Test().test();

    }

    private void test() {
        int c = ctl.get();
        System.out.println(workerCountOf(c));
        System.out.println(isRunning(c));

        int rs = runStateOf(c);
        System.out.println(">shutdown1--" + (rs >= SHUTDOWN));

        compareAndIncrementWorkerCount(c);

        rs = runStateOf(c);
        System.out.println(">shutdown2--" + (rs >= SHUTDOWN));

    }

}
