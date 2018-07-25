package com.enniu.cloud.services.fc.loan.platform.utils.varmanager;

import lombok.Getter;

/**
 * @author liuyihan
 * @since 2018/7/25
 */
public class Pair <L,R>{

    @Getter
    private L left;
    @Getter
    private R right;

    Pair(L left, R right){
        this.left = left;
        this.right = right;
    }
}
