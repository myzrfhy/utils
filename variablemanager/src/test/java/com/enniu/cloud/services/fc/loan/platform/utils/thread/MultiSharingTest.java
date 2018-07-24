package com.enniu.cloud.services.fc.loan.platform.utils.thread;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.ThreadLocalManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerThreadWrapper;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import testutil.MockBase;
import testutil.TestUtils;

/**
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
        ThreadLocalManager.open();
        ThreadLocalManager.put(key, value);

        Print print = mock(Print.class);
        doNothing().when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(VarManagerThreadWrapper.multiSharing(()->print.print(ThreadLocalManager.get(key))), latch);

        verify(print).print(eq(value));
    }

    /**
     * 父类获得子类共享的变量.
     */
    @Test
    public void multiSharing_child_share() {
        String key = "aaa";
        String value = "val";

        ThreadLocalManager.open();

        Print print = mock(Print.class);
        doNothing().when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(VarManagerThreadWrapper.multiSharing(() -> ThreadLocalManager.put(key, value)), latch);

        print.print(ThreadLocalManager.get(key));
        verify(print).print(eq(value));
    }

    /**
     * 子类移除共享变量,父类感知.
     */
    @Test
    public void multiSharing_child_remove() {
        String key = "aaa";
        String value = "val";
        ThreadLocalManager.open();
        ThreadLocalManager.put(key, value);

        Print print = mock(Print.class);
        doNothing().when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(VarManagerThreadWrapper.multiSharing(()->ThreadLocalManager.remove(key)), latch);

        print.print(ThreadLocalManager.get(key));
        verify(print).print(eq(null));
    }

    private class Print{
        void print(String key){}
    }
}
