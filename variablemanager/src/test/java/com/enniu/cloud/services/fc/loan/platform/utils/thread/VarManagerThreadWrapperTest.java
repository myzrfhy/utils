package com.enniu.cloud.services.fc.loan.platform.utils.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread.VarManagerThreadWrapper;
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
        runAndVerify(VarManagerThreadWrapper.wrap(() -> System.out.println("test")));
    }

    @Test
    public void wrap_runnable_extend() {
        runAndVerify(VarManagerThreadWrapper.wrap(() -> System.out.println("test"), TestInvocationHandler.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void wrap_callable() {
        runAndVerify(VarManagerThreadWrapper.wrap(() -> {
            System.out.println("test");
            return 1;
        }));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void wrap_callable_extend() {
        runAndVerify(VarManagerThreadWrapper.wrap(() -> {
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
        TestUtils.submit(r, c, latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PowerMockito.verifyStatic();
    }

}
