package com.cresign.chat.common;

import lombok.Getter;

/**
 * @ClassName ChatEnum
 * @Description chat服务状态码类
 * @authortang
 * @Date 2021/6/23 15:41
 * @ver 1.0.0
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
     * 无assetId
     */
    ERR_NO_ASSET_ID("01103"),
    /**
     * 无asset
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
    ;


    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    ChatEnum(String code){
        this.code = code;
    }

}
