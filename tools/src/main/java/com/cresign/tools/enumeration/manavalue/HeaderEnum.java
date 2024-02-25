package com.cresign.tools.enumeration.manavalue;

import lombok.Getter;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/8/5 11:36
 * @ver 1.0
 */
@Getter
public enum  HeaderEnum {

    /**
     * clientType
     *
     */
    CLIENTTYPE("clientType"),

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
    private final String headerName;

    /**
     * @param headerName 客户端类型
     */
    HeaderEnum(String headerName){
        this.headerName = headerName;

    }

}