package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.exception.VarManagerException;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import lombok.experimental.UtilityClass;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@UtilityClass
public class VarManagerThreadWrapper {

    @SuppressWarnings("unchecked")
    Runnable wrap(Runnable r) {
        return (Runnable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            new VarManagerInvocationHandler(r));
    }

    @SuppressWarnings("unchecked")
    <C extends VarManagerInvocationHandler> Runnable wrap(Runnable r, Class<C> invocationHandlerClass) {
        return (Runnable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            invocationHandlerInit(r, invocationHandlerClass));
    }

    @SuppressWarnings("unchecked")
    Callable wrap(Callable r) {
        return (Callable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            new VarManagerInvocationHandler(r));
    }

    @SuppressWarnings("unchecked")
    <C extends VarManagerInvocationHandler> Callable wrap(Callable r, Class<C> invocationHandlerClass) {
        return (Callable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            invocationHandlerInit(r, invocationHandlerClass));
    }

    private <C extends VarManagerInvocationHandler> VarManagerInvocationHandler invocationHandlerInit(Object r,
        Class<C> invocationHandlerClass) {
        C invocationHandler;
        try {
            invocationHandler = invocationHandlerClass.newInstance();
            invocationHandler.setTarget(r);
            return invocationHandler;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new VarManagerException("init err,class:" + invocationHandlerClass.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new VarManagerException(
                "check constructor access level,class:" + invocationHandlerClass.getSimpleName());
        }
    }
}
