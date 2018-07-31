package com.enniu.cloud.services.fc.loan.platform.utils.thread;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerThreadWrapper;
import com.google.common.collect.Lists;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import org.junit.Test;
import testutil.MockBase;
import testutil.TestUtils;
import testutil.TestUtils.Print;

/**
 * 多线程、线程池共享变量功能测试.
 * @author liuyihan
 * @since 2018/7/24
 */
public class MultiSharingTest extends MockBase{

    /**
     * 子类继承父类共享变量.
     */
    @Test
    public void multiSharing_extend() {
        String key = "aaa";
        String value = "val";
        VarManager.open();
        VarManager.put(key, value);

        Print print = mock(Print.class);
        doReturn("111").when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        Runnable r = VarManagerThreadWrapper.multiShareWrap(()->print.print(VarManager.get(key)));
        TestUtils.submit(r, latch);

        verify(print).print(eq(value));
    }

    /**
     * 父类获得子类共享的变量.
     */
    @Test
    public void multiSharing_child_share() {
        String key = "aaa";
        String value = "val";

        VarManager.open();

        Print print = mock(Print.class);
        doReturn("111").when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit( VarManagerThreadWrapper.multiShareWrap((Runnable)() -> VarManager.put(key, value)), latch);

        print.print(VarManager.get(key));
        verify(print).print(eq(value));
    }

    /**
     * 子类移除共享变量,父类感知.
     */
    @Test
    public void multiSharing_child_remove() {
        String key = "aaa";
        String value = "val";
        VarManager.open();
        VarManager.put(key, value);

        Print print = mock(Print.class);
        doReturn("111").when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        Runnable r = VarManagerThreadWrapper.multiShareWrap(()->VarManager.remove(key));
        TestUtils.submit(r, latch);

        print.print(VarManager.get(key));
        verify(print).print(eq(null));
    }

    @Test
    public void multiSharing_share(){
        VarManager.clear();

        String key = "aaa";
        String value = "val";
        VarManager.open();

        Print print = mock(Print.class);
        doReturn("111").when(print).print(eq(value));

        CountDownLatch latch = new CountDownLatch(5);
        Runnable r = VarManagerThreadWrapper.multiShareWrap(()->VarManager.apply(key, ()->print.print(value)));
        TestUtils.submit(Lists.newArrayList(r,r,r,r,r), latch);

        verify(print).print(eq(value));

    }

    @Test
    public void multiSharing_pool() {
        VarManager.clear();

        String key = "aaa";
        String value = "val";
        VarManager.open();

        Print print = mock(Print.class);
        doReturn("111").when(print).print(eq(value));

        CountDownLatch latch = new CountDownLatch(5);
        Runnable r = () -> {
            try {
                VarManager.apply(key, () -> print.print(value));
            }finally {
                latch.countDown();
            }
        };
        ExecutorService pool = VarManagerThreadWrapper.multiShareWrap(new ForkJoinPool());
        for (int i = 0; i < 5; i++) {
            pool.submit(r);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(print).print(eq(value));
    }
}
