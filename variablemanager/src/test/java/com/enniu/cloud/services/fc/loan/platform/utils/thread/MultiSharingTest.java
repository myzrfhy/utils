package com.enniu.cloud.services.fc.loan.platform.utils.thread;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerThreadWrapper;
import com.google.common.collect.Lists;
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
        VarManager.open();
        VarManager.put(key, value);

        Print print = mock(Print.class);
        doReturn("111").when(print).print(eq(key));

        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(VarManagerThreadWrapper.multiShareWrap(()->print.print(VarManager.get(key))), latch);

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
        TestUtils.submit(VarManagerThreadWrapper.multiShareWrap(() -> VarManager.put(key, value)), latch);

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
        TestUtils.submit(VarManagerThreadWrapper.multiShareWrap(()->VarManager.remove(key)), latch);

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
        Runnable r = VarManagerThreadWrapper.multiShareWrap(()->{VarManager.apply(key, ()->print.print(value));});
        TestUtils.submit(Lists.newArrayList(r,r,r,r,r), latch);

        verify(print).print(eq(value));

    }

    private class Print{
        String print(String key){
            System.out.println("test");
            return key;
        }
    }
}
