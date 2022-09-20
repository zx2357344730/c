package com.cresign.tools.enumeration;

import lombok.Getter;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/8/6 10:33
 * @ver 1.0
 */
@Getter
public enum CodeEnum {

    FORBIDDEN("403"),

    OK("200"),

    ALREADY_LOCAL("220"),

    BAD_REQUEST("400"),

    NOT_FOUND("404"),

    INTERNAL_SERVER_ERROR("500"),


    ;


    private String code;

    CodeEnum(String code){
        this.code = code;

    }


}
