package com.enniu.cloud.services.fc.loan.platform.utils.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerThreadWrapper;
import com.google.common.collect.Lists;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import testutil.PowerMockBase;
import testutil.TestInvocationHandler;
import testutil.TestUtils;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
public class VarManagerThreadWrapperTest extends PowerMockBase {

    @Test
    @SuppressWarnings("unchecked")
    public void wrap_runnable() {
        Runnable r = () -> System.out.println("test");
        runAndVerify(VarManagerThreadWrapper.wrap(r));
    }

    @Test
    public void wrap_runnable_extend() {
        runAndVerify(VarManagerThreadWrapper.wrap((Runnable)() -> System.out.println("test"), TestInvocationHandler.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void wrap_callable() {
        Callable c = () -> {
            System.out.println("test");
            return 1;
        };
        runAndVerify(VarManagerThreadWrapper.wrap(c));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void wrap_callable_extend() {
        runAndVerify(VarManagerThreadWrapper.wrap((Callable)() -> {
            System.out.println("test");
            return 1;
        }, TestInvocationHandler.class));
    }

    private void runAndVerify(Runnable r) {
        runAndVerify(r, null);
    }

    private void runAndVerify(Callable c) {
        runAndVerify(null, c);
    }

    private void runAndVerify(Runnable r, Callable c) {
        CountDownLatch latch = new CountDownLatch(1);
        TestUtils.submit(Lists.newArrayList(r), Lists.newArrayList(c), latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PowerMockito.verifyStatic();
    }

}
