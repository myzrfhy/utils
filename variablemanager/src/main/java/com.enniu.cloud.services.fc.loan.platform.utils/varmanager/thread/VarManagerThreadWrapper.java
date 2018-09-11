package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.exception.VarManagerException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@UtilityClass
public class VarManagerThreadWrapper {

    @SuppressWarnings("unchecked")
    public <T> T wrap(T o) {
        Objects.requireNonNull(o);
        if (o instanceof ExecutorService) {
            return (T) new ExecutorMethodInterceptor(o, false).getInstance();
        }

        if (o instanceof Runnable || o instanceof Callable) {
            return (T) Proxy.newProxyInstance(o.getClass().getClassLoader(), o.getClass().getInterfaces(),
                new VarManagerInvocationHandler(o));
        }
        return o;
    }

    @SuppressWarnings("unchecked")
    public <T, C extends VarManagerInvocationHandler> T wrap(T r, Class<C> invocationHandlerClass) {
        return (T) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            invocationHandlerInit(r, invocationHandlerClass));
    }

    @SuppressWarnings("unchecked")
    public <T> T multiShareWrap(T o) {
        Objects.requireNonNull(o);
        if (o instanceof ExecutorService) {
            return (T) new ExecutorMethodInterceptor(o, true).getInstance();
        }

        //parent线程与child线程分享共享变量
        Map<String, Object> map = VarManager.getOrInitShareMap();
        if (o instanceof Runnable) {
            return (T) wrap(new MultiSharingRunnable((Runnable) o, map));
        } else if (o instanceof Callable) {
            return (T) wrap(new MultiSharingCallable((Callable) o, map));
        }
        return o;
    }


    private <C extends VarManagerInvocationHandler> VarManagerInvocationHandler invocationHandlerInit(Object r,
        Class<C> invocationHandlerClass) {
        C invocationHandler;
        try {
            invocationHandler = invocationHandlerClass.newInstance();
            invocationHandler.setTarget(r);
            return invocationHandler;
        } catch (InstantiationException e) {
            throw new VarManagerException("init err,class:" + invocationHandlerClass.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new VarManagerException(
                "check constructor access level,class:" + invocationHandlerClass.getSimpleName());
        }
    }

    @AllArgsConstructor
    private class MultiSharingRunnable implements Runnable {

        Runnable r;
        Map<String, Object> sharingVarMap;

        @Override
        public void run() {
            VarManager.initShareMap(sharingVarMap);
            r.run();
        }
    }

    @AllArgsConstructor
    private class MultiSharingCallable<T> implements Callable<T> {

        Callable c;
        Map<String, Object> sharingVarMap;

        @Override
        @SuppressWarnings("unchecked")
        public T call() throws Exception {
            VarManager.initShareMap(sharingVarMap);
            return (T) c.call();
        }
    }
}
