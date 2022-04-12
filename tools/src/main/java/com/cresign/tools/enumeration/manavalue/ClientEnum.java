package com.cresign.tools.enumeration.manavalue;

import lombok.Getter;

/**
 * ##author: JackSon
 * ##updated: 2020/5/23
 * ##version: 1.1.0
 * ##description: ClientEnum魔法值枚举类
 */
@Getter
public enum ClientEnum {


    // web端
    WEB_CLIENT("web"),

    // 微信小程序端
    WX_CLIENT("wx"),

    // 手机端
    APP_CLIENT("app"),


    ;
    /**
     * 异常状态码
     */
    private final String clientType;

    /**
     * ##Params: clientType 客户端类型
     */
    ClientEnum(String clientType){
        this.clientType = clientType;

    }




}