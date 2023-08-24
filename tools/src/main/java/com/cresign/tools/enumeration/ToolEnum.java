package com.cresign.tools.enumeration;

import lombok.Getter;

@Getter
public enum ToolEnum {

    //assset不存在

    DB_ERROR("029001"),

    SAVE_DB_ERROR("029003"),

    ES_DB_ERROR("029002"),

    VALUE_IS_NULL("029009"),

    //公司文件容量不足
    POWER_NOT_ENOUGH("030011"),

    //产品数量不足
    PROD_NOT_ENOUGH("022018"),
    ;
    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    ToolEnum(String code){
        this.code = code;

    }
}
