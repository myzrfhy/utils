package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.aop;


import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.ThreadLocalManager;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.annotations.TLCache;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * @author liuyihan
 * @since 2018/6/29
 */
@Component
@Aspect
@Slf4j
public class ThreadLocalManagerAspect {

    @Around("@annotation(com.enniu.cloud.services.fc.loan.platform.utils.varmanager.annotations.TLCache")
    public Object aroundTLCache(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        method.setAccessible(true);
        TLCache tlCache = method.getAnnotation(TLCache.class);

        String key = tlCache.key();
        //如果没有指定key，则用类名+方法名+参数组合key
        if (StringUtils.isBlank(key)) {
            StringBuilder sb = new StringBuilder();
            sb.append(joinPoint.getTarget().getClass().getSimpleName()).append(":").append(method.getName()).append(":");
            for (Object o : joinPoint.getArgs()) {
                sb.append(o.toString()).append(":");
            }
            key = sb.toString();
        }

        return ThreadLocalManager.applyThrow(key, joinPoint::proceed);
    }
}
