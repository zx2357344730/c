package com.cresign.purchase.enumeration;

import lombok.Getter;

/**
 * ##author: Jeovn
 * ##updated: 2020/5/23
 * ##version: 1.1.0
 * ##description: Module异常枚举类
 */
@Getter
public enum ModuleEnum {




    // 不是负责人
    PR_NO_CHARGE_USER("043001"),

    //redis订单不存在
    REDIS_ORDER_NO_HAVE("043002"),




    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    ModuleEnum(String code){
        this.code = code;

    }




}