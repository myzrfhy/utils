package testutil;

import com.google.common.collect.Lists;
import java.util.List;
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
        submit(null, Lists.newArrayList(c), latch);
    }

    public void submit(Runnable r, CountDownLatch latch) {
        submit(Lists.newArrayList(r), null, latch);
    }

    public void submit(List<Runnable> rList, CountDownLatch latch) {
        submit(rList, null, latch);
    }

    public void submit(List<Runnable> rList, List<Callable> cList, CountDownLatch latch) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        if (rList != null && rList.size() != 0) {
            rList.forEach(r -> {
                forkJoinPool.submit(countDownLatchWrap(r, latch));
                //用于测试共享变量
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        } else if (cList != null && cList.size() != 0) {
            cList.forEach(c -> forkJoinPool.submit(countDownLatchWrap(c, latch)));
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
