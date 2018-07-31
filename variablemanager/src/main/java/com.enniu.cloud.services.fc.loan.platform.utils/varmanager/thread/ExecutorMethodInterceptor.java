package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

/**
 * @author liuyihan
 * @since 2018/7/31
 */
@AllArgsConstructor
public class ExecutorMethodInterceptor implements MethodInterceptor {

    @Setter
    private Object target;
    private Boolean isMultiShare;

    public Object getInstance(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        methodProxy.invokeSuper(o, argsHandler(args));
        return null;
    }

    private Object[] argsHandler(Object[] args){
        return Arrays.stream(args).map(o->{
            if(o instanceof Runnable || o instanceof Callable){
                return wrap(o);
            }
            if(o instanceof List){
                return collectionWrap(o, Collectors.toList());
            }
            if(o instanceof Set){
                return collectionWrap(o, Collectors.toSet());
            }
            return o;
        }).toArray();
    }

    @SuppressWarnings("unchecked")
    private Object collectionWrap(Object o, Collector collector){
        return ((Collection) o).stream().map(obj->{
            if(obj instanceof Runnable || obj instanceof Callable){
                return wrap(obj);
            }
            return obj;
        }).collect(collector);
    }

    private Object wrap(Object o){
        return isMultiShare?VarManagerThreadWrapper.multiShareWrap(o):VarManagerThreadWrapper.wrap(o);
    }
}
