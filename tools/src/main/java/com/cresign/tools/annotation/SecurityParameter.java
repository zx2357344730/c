package com.cresign.tools.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;


/**
 * @author monkey
 * @desc 请求混合数据解密
 * ##Updated: 2018/10/25 20:17
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Mapping
@Documented
/*@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)*/
public @interface SecurityParameter {

    /**
     * 入参是否解密，默认解密
     */
    boolean inDecode() default true;

    /**
     * 出参是否加密，默认加密
     */
    boolean outEncode() default true;
}