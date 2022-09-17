package com.cresign.purchase.common;

import lombok.Getter;

/**
 * @ClassName ChatEnum
 * @Description chat服务状态码类
 * @Author tang
 * @Date 2021/6/23 15:41
 * @Version 1.0.0
 */
@Getter
public enum ChatEnum {

    /**
     * 不识别请求
     */
    ERR_DONT_RECOGNIZE("01001"),
    /**
     * 获取订单信息为空
     */
    ERR_GET_ORDER_NULL("01002"),
    /**
     * 获取用户信息为空
     */
    ERR_GET_USER_NULL("01003"),
    /**
     * 获取数据为空
     */
    ERR_GET_DATA_NULL("01004"),
    /**
     * 操作已被处理
     */
    ERR_OPERATION_IS_PROCESSED("01005"),
    /**
     * 订单不存在
     */
    ORDER_NOT_EXIST("01006"),
    /**
     * 递归结果为空
     */
    ERR_RECURSION_RESULT_IS_NULL("01007"),
    /**
     * 产品数据为空
     */
    ERR_PROD_DATA_IS_NULL("01008"),
    /**
     * 排除后的产品信息为空
     */
    ERR_AFTER_EXCLUSION_PROD_IS_NULL("01009"),
    /**
     * 无递归零件
     */
    ERR_NO_RECURSION_PART("01010"),
    /**
     * 产品不存在
     */
    ERR_PROD_NOT_EXIST("01011"),

    /**
     * 产品无零件
     */
    ERR_PROD_NO_PART("01012"),
    /**
     * 零件信息为空
     */
    ERR_PART_IS_NULL("01013"),
    /**
     * 操作失败
     */
    ERR_OPERATION_FAILED("01014"),
    /**
     * 公司没有客服
     */
    ERR_COMP_NO_CUSTOMER_SERVICE("01015"),
    /**
     * 个人信息有误
     */
    ERR_INCORRECT_PERSONAL_INFORMATION("01016"),
    /**
     * 订单被确认
     */
    ERR_ORDER_CONFIRMED("01017"),
    /**
     * 出现错误
     */
    ERR_AN_ERROR_OCCURRED("01018"),
    /**
     * 主公司编号为空
     */
    ERR_MAIN_COMP_ID_IS_NULL("01019"),
    /**
     * 供应商编号为空
     */
    ERR_SUPPLIER_ID_IS_NULL("01020"),
    /**
     * 不是主公司也不是供应商
     */
    ERR_NO_MAIN_COMP_AND_NO_SUPPLIER("01021"),
    /**
     * 没有主公司关联该订单
     */
    ERR_NO_MAIN_COMP_RELATION_ORDER("01022"),
    /**
     * 没有供应商关联该订单
     */
    ERR_NO_SUPPLIER_RELATION_ORDER("01023"),
    /**
     * 订单未合并
     */
    ERR_ORDER_NOT_MERGE("01024"),
    /**
     * 主订单编号为空
     */
    ERR_MAIN_ORDER_ID_IS_NULL("01025"),
    /**
     * 不可操作该订单
     */
    ERR_NO_OPERATION_ORDER("01026"),
    /**
     * 子订单为空
     */
    ERR_SON_ORDER_IS_NULL("01027"),
    /**
     * es获取数据为空
     */
    ERR_ES_GET_DATA_IS_NULL("01028"),


    /**
     * 需要Final 确认
     */
    ERR_ORDER_NEED_FINAL("01029"),

    /**
     * DG 无限Loop 了
     */
    ERR_PROD_RECURRED("01030"),

    /**
     * gpIo已经被绑定
     */
    ERR_IO_ALREADY_BINDED("01101"),
    /**
     * gpIo已经被解绑
     */
    ERR_IO_ALREADY_UNBIND("01102"),
    /**
     * 该公司没有assetId
     */
    ERR_NO_ASSET_ID("01103"),
    /**
     * 该公司没有asset
     */
    ERR_NO_ASSET("01104"),
    /**
     * rpi卡片异常
     */
    ERR_RPI_K("01105"),
    /**
     * rpi的token数据不存在
     */
    ERR_RPI_T_DATA_NO("01106"),
    /**
     * 没有rpi卡片
     */
    ERR_NO_RPI_K("01107"),
    /**
     * rpi卡片没有对应的rname
     */
    ERR_NO_RPI_R_NAME_K("01108"),
    /**
     * 未知异常
     */
    ERR_WZ("01109"),
    /**
     * 已经被绑定
     */
    ERR_Y_BIND("01110"),
    /**
     * 机器已经被绑定
     */
    ERR_PI_B_BIND("01111"),
    /**
     * 机器信息为空，操作失败
     */
    ERR_PI_X_K("01112"),
    /**
     * 该机器不属于你们公司，操作失败
     */
    ERR_PI_B_NO("01113"),
    /**
     * rpi卡片异常-rpi-基础信息为空
     */
    ERR_PI_X_NO("01114"),

    /**
     * 该公司没有control卡片
     */
    ERR_NO_CONTROL_K("01115"),
    /**
     * 该公司control卡片异常
     */
    ERR_CONTROL_K("01116"),
    /**
     * 该公司没有这个模块功能
     */
    ERR_AUTH_NO_MOD("01117"),
    /**
     * 当前用户这个模块功能权限不够
     */
    ERR_AUTH_NO_LEV("01118"),
    /**
     * 该公司没有role卡片
     */
    ERR_NO_ROLE_K("01119"),
    /**
     * 该公司role卡片异常
     */
    ERR_ROLE_K("01120"),
    /**
     * 无匹配的modRef
     */
    ERR_NO_MATCHING_MOD_REF("01121"),
    ;


    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    ChatEnum(String code){
        this.code = code;
    }

}
