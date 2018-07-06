package com.enniu.cloud.services.fc.loan.platform.utils.varmanager.exception;

import lombok.NoArgsConstructor;

/**
 * @author liuyihan
 * @since 2018/7/6
 */
@NoArgsConstructor
public class VarManagerException extends RuntimeException{

    public VarManagerException(String message){
        super(message);
    }
}
