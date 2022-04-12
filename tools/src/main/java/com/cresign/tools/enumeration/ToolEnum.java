package com.cresign.tools.enumeration;

import lombok.Getter;

@Getter
public enum ToolEnum {

    //assset不存在
    ASSET_NOT_FOUND("021005"),


    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    ToolEnum(String code){
        this.code = code;

    }
}
