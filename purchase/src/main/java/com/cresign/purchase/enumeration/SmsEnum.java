package com.cresign.purchase.enumeration;

import lombok.Getter;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/8/24 18:50
 * @ver 1.0
 */
@Getter
public enum SmsEnum {


    /**
     * 验证码不正确
     */
    SMS_NOT_TRUE("1210"),



    ;


    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    SmsEnum(String code){
        this.code = code;
    }

}
