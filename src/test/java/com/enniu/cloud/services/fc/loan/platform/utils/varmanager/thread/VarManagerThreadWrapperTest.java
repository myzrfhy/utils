package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import java.util.concurrent.ForkJoinPool;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import testutil.PowerMockBase;
import testutil.TestInvocationHandler;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
public class VarManagerThreadWrapperTest extends PowerMockBase {

    @Test
    public void wrap_runnable(){
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(VarManagerThreadWrapper.wrap(()->{
            System.out.println("test");
        }));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PowerMockito.verifyStatic();
    }

    @Test
    public void wrap_runnable_extend(){
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(VarManagerThreadWrapper.wrap(()->{
            System.out.println("test");
        }, TestInvocationHandler.class));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PowerMockito.verifyStatic();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void wrap_callable(){
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(VarManagerThreadWrapper.wrap(()->{
            System.out.println("test");
            return 1;
        }));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PowerMockito.verifyStatic();
    }

    @Test
    public void wrap_callable_extend(){
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(VarManagerThreadWrapper.wrap(()->{
            System.out.println("test");
            return 1;
        }, TestInvocationHandler.class));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PowerMockito.verifyStatic();
    }

}
