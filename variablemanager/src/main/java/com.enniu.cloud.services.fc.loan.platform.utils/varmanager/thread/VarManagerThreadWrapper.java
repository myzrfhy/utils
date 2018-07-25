package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.Pair;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.VarManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.exception.VarManagerException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@UtilityClass
public class VarManagerThreadWrapper {

    @SuppressWarnings("unchecked")
    public <T> T wrap(T o){
        if(o instanceof Runnable){
            return (T)wrap((Runnable) o);
        } else if (o instanceof Callable){
            return (T)wrap((Callable) o);
        }
        return o;
    }

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

    public ExecutorService wrap(ExecutorService s){
        return (ExecutorService) Proxy.newProxyInstance(s.getClass().getClassLoader(), s.getClass().getInterfaces(),
            new ExecutorInvocationHandler(s, false));
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

    @SuppressWarnings("unchecked")
    public <T> T multiShareWrap(T o){
        if(o instanceof Runnable){
            return (T)multiShareWrap((Runnable) o);
        } else if (o instanceof Callable){
            return (T)multiShareWrap((Callable) o);
        }
        return o;
    }


    public Runnable multiShareWrap(Runnable r){
        //parent线程与child线程分享共享变量
        Pair<Map<String,Object>,ReentrantReadWriteLock> pair = VarManager.getOrInitShareMap();
        return wrap(new MultiSharingRunnable(r, pair.getLeft(), pair.getRight()));
    }

    public Callable multiShareWrap(Callable r){
        //parent线程与child线程分享共享变量
        Pair<Map<String,Object>,ReentrantReadWriteLock> pair = VarManager.getOrInitShareMap();
        return wrap(new MultiSharingCallable(r, pair.getLeft(), pair.getRight()));
    }

    public ExecutorService multiShareWrap(ExecutorService s){
        return (ExecutorService) Proxy.newProxyInstance(s.getClass().getClassLoader(), s.getClass().getInterfaces(),
            new ExecutorInvocationHandler(s, true));
    }

    @AllArgsConstructor
    private class MultiSharingRunnable implements Runnable{

        Runnable r;
        Map<String,Object> sharingVarMap;
        ReentrantReadWriteLock lock;

        @Override
        public void run() {
            VarManager.initShareMap(sharingVarMap);
            VarManager.initLock(lock);
            r.run();
        }
    }

    @AllArgsConstructor
    private class MultiSharingCallable<T> implements Callable<T>{

        Callable c;
        Map<String,Object> sharingVarMap;
        ReentrantReadWriteLock lock;

        @Override
        @SuppressWarnings("unchecked")
        public T call() throws Exception {
            VarManager.initShareMap(sharingVarMap);
            VarManager.initLock(lock);
            return (T)c.call();
        }
    }
}
