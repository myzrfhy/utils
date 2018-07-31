package com.enniu.cloud.services.fc.loan.platform.utils.thread;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerThreadWrapper;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import org.junit.Test;
import org.mockito.Mock;
import testutil.MockBase;
import testutil.TestUtils;
import testutil.TestUtils.Print;

/**
 * 测试VarManager的wrap功能.
 * @author liuyihan
 * @since 2018/7/31
 */
public class VarManagerThreadWrapperTest extends MockBase{

    @Mock
    Print print;

    @Test
    public void wrap_runnable(){
        String key = "aaa";
        String value = "bbb";

        when(print.print(eq(key))).thenReturn(value);
        Runnable r = ()->{
            VarManager.apply(key, ()->print.print(key));
            print.print(VarManager.get(key));
        };
        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(VarManagerThreadWrapper.wrap(r), latch);

        verify(print).print(eq(value));
    }

    @Test
    public void common_runnable(){
        String key = "aaa";
        String value = "bbb";

        when(print.print(eq(key))).thenReturn(value);
        Runnable r = ()->{
            VarManager.apply(key, ()->print.print(key));
            print.print(VarManager.get(key));
        };
        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(r, latch);

        verify(print, times(0)).print(eq(value));
    }

    @Test
    public void wrap_callable(){
        String key = "aaa";
        String value = "bbb";

        when(print.print(eq(key))).thenReturn(value);
        Callable r = ()->{
            VarManager.apply(key, ()->print.print(key));
            print.print(VarManager.get(key));
            return 1;
        };
        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(VarManagerThreadWrapper.wrap(r), latch);

        verify(print).print(eq(value));
    }

    @Test
    public void wrap_pool(){
        String key = "aaa";
        String value = "bbb";

        when(print.print(eq(key))).thenReturn(value);

        CountDownLatch latch = new CountDownLatch(1);
        Runnable r = ()->{
            try {
                VarManager.apply(key, () -> print.print(key));
                print.print(VarManager.get(key));
            }finally {
                latch.countDown();
            }
        };


        ForkJoinPool pool = VarManagerThreadWrapper.wrap(new ForkJoinPool());
        pool.submit(r);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(print).print(eq(value));
    }
}
