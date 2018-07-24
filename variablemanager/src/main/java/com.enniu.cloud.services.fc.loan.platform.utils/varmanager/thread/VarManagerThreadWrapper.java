package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.exception.VarManagerException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Callable;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@UtilityClass
public class VarManagerThreadWrapper {

    @SuppressWarnings("unchecked")
    public Runnable wrap(Runnable r) {
        return (Runnable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            new VarManagerInvocationHandler(r));
    }

    @SuppressWarnings("unchecked")
    public <C extends VarManagerInvocationHandler> Runnable wrap(Runnable r, Class<C> invocationHandlerClass) {
        return (Runnable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            invocationHandlerInit(r, invocationHandlerClass));
    }

    @SuppressWarnings("unchecked")
    public Callable wrap(Callable r) {
        return (Callable) Proxy.newProxyInstance(r.getClass().getClassLoader(), r.getClass().getInterfaces(),
            new VarManagerInvocationHandler(r));
    }

    @SuppressWarnings("unchecked")
    public <C extends VarManagerInvocationHandler> Callable wrap(Callable r, Class<C> invocationHandlerClass) {
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
            throw new VarManagerException("init err,class:" + invocationHandlerClass.getSimpleName());
        } catch (IllegalAccessException e) {
            throw new VarManagerException(
                "check constructor access level,class:" + invocationHandlerClass.getSimpleName());
        }
    }


    public Runnable multiSharing(Runnable r){
        //parent线程与child线程分享共享变量
        Map<String,Object> sharingVarMap = VarManager.getOrInitShareMap();
        return wrap(new MultiSharingRunnable(r, sharingVarMap));
    }

    @AllArgsConstructor
    private class MultiSharingRunnable implements Runnable{

        Runnable r;
        Map<String,Object> sharingVarMap;

        @Override
        public void run() {
            VarManager.initShareMap(sharingVarMap);
            r.run();
        }
    }
}
