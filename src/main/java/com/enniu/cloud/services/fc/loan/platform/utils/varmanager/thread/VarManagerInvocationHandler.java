package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.thread;

import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.ThreadLocalManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class VarManagerInvocationHandler implements InvocationHandler {

    @Setter
    private Object target;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ThreadLocalManager.open();
        if (log.isDebugEnabled()) {
            log.debug("[线程变量管理器]开启成功");
        }
        Object result;
        try {
            beforeInvoke();
            result = method.invoke(target, args);
            afterInvoke();
        } finally {
            ThreadLocalManager.clear();
            ThreadLocalManager.close();
            if (log.isDebugEnabled()) {
                log.debug("[线程变量管理器]清除并关闭成功");
            }
        }
        return result;
    }

    protected void beforeInvoke() {
    }

    protected void afterInvoke() {

    }
}
