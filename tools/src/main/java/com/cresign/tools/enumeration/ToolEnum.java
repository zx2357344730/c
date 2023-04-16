package com.cresign.tools.enumeration;

import lombok.Getter;

@Getter
public enum ToolEnum {

    //assset不存在
    ASSET_NOT_FOUND("021005"),

    DB_ERROR("029001"),
    //公司文件容量不足
    POWER_NOT_ENOUGH("030011"),

    //产品数量不足
    PROD_NOT_ENOUGH("022018"),
    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    ToolEnum(String code){
        this.code = code;

    }
}
