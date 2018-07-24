package testutil;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import lombok.experimental.UtilityClass;

/**
 * @author liuyihan
 * @since 2018/7/24
 */
@UtilityClass
public class TestUtils {

    public void submit(Callable c, CountDownLatch latch) {
        submit(null, c, latch);
    }

    public void submit(Runnable r, CountDownLatch latch) {
        submit(r, null, latch);
    }

    public void submit(Runnable r, Callable c, CountDownLatch latch) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        if (r != null) {
            forkJoinPool.submit(countDownLatchWrap(r, latch));
        } else if (c != null) {
            forkJoinPool.submit(countDownLatchWrap(c, latch));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable countDownLatchWrap(Runnable r, CountDownLatch latch) {
        return () -> {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        };
    }

    private Callable countDownLatchWrap(Callable c, CountDownLatch latch) {
        return () -> {
            try {
                return c.call();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
            return null;
        };
    }
}
