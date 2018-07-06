package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function;

/**
 * @author liuyihan
 * @since 2018/6/29
 */
@FunctionalInterface
public interface ApplyFunction<T> extends VarManagerFunction{
    T apply();
}
