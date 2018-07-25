package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import java.lang.reflect.InvocationHandler;
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

/**
 * @author liuyihan
 * @since 2018/7/25
 */
@AllArgsConstructor
class ExecutorInvocationHandler implements InvocationHandler{

    @Setter
    private Object target;
    private Boolean isMultiShare;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(target, argsHandler(args));
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
