package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function;

/**
 * @author liuyihan
 * @since 2018/6/29
 */
@FunctionalInterface
public interface ThrowableFunction<T> extends VarManagerFunction{
    T apply() throws Throwable;
}
