package com.cresign.details.enumeration;

import lombok.Getter;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/10/13 9:49
 * ##version: 1.0
 */
@Getter
public enum QrShareCodeEnum {

    /**
     * 没有设置二维码
     */
    NO_SET_MODE("1080"),

    /**
     * 二维码已过期
     */
    QRCODE_NOT_FOUND("1081"),

    /**
     * 分享视图没有设置
     */
    VIEWR_NOT_SET("1083"),



    ;
    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    QrShareCodeEnum(String code){
        this.code = code;

    }
}
