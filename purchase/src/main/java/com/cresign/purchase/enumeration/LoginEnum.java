package com.cresign.purchase.enumeration;

import lombok.Getter;

/**
 * ##author: Jeovn
 * ##updated: 2020/5/23
 * ##version: 1.1.0
 * ##description: 通用异常枚举类
 */
@Getter
public enum LoginEnum {

    // 成功
    SUCCESS("200"),

    // 无权限，拒绝访问
    PERMISSION_IS_NULL("403"),



    // 系统错误
    ALL_SYSTEM_ERROR("500"),

    // 不是负责人
    NO_CHARGE_USER("4031"),

    // 失败
    FAILF("5000"),

    // 请联系管理员
    CONTACT_ABMIN("5001"),

    // 修改失败
    ALL_ERROR("5010"),

    // 查找的数据为空
    DATA_IS_NULL("5404"),

    //已存在
    IS_HAVE("6001"),

    //不存在
    NO_HAVE("6002"),



    //模块添加人数出错
    MODULE_ADDUSSER_ERROR("6003"),

    //模块添加人数超出，200人数为一次
    MODULE_ADDUSSER_GOBEYOND("6004"),

    //redis订单不存在
    REDIS_NO_HAVE("6005"),

    //添加模块出错
    ADD_MODULE_ERROR("6007"),

    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    LoginEnum(String code){
        this.code = code;

    }




}