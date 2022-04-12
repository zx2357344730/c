package com.cresign.purchase.enumeration;

import lombok.Getter;

/**
 * ##ClassName: PurchaseEnum
 * ##description: 购买模块异常枚举类
 * ##Author: tang
 * ##Updated: 2020/8/8 8:26
 * ##version: 1.0.0
 */
@Getter
public enum PurchaseEnum {

//    // 日志操作成功
//    PR_SUCCESS("200"),
//
//    // 日志操作失败
//    PR_ERROR("1005"),
//
//    // 日志操作无权限失败
//    PR_PERMISSION_IS_NULL("403"),
//
//    // 获取数据库数据为空    : tang
//    PR_GET_DATA_NULL("50404"),
//
//    // 获取数据为空    : tang
//    PR_GET_NULL("5404"),
//
//    // 失败
//    PR_FAILF("5000"),
//
//    // 新增日志失败
//    PR_ADD_LIST_ERROR("5750"),
//===========================================
    //沒有找到该公司
    PR_COMP_DATA_NO_FOUND("051001"),
    //assset不存在
    ASSET_NOT_FOUND("051002"),
    // 不是负责人
    NO_CHARGE_USER("051003"),
    //redis订单不存在
    REDIS_ORDER_NO_HAVE("051004"),
    //验证码不正确
    SMS_NOT_TRUE("051005"),
    //该模块已购买过
    PR_MODULE_IS_BUY_HAVE("051006"),
    //该模块不存在
    MODUL_NO_HAVE("051007"),
    //模块添加人数出错
    MODULE_ADDUSSER_ERROR("051008"),
    //模块添加人数超出，200人数为一次
    MODULE_ADDUSSER_GOBEYOND("051009"),

    // 订单已过期 : tang
    ERR_ORDER_BE_OVERDUE("05001"),
    // 操作失败 : tang
    ERR_OPERATION_FAILED("05002"),
    // 购买信息为空 : tang
    ERR_PURCHASE_INFO_IS_NULL("05003"),
    // 购买失败 : tang
    ERR_PURCHASE_FAIL("05004"),
    ;
    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    PurchaseEnum(String code){
        this.code = code;
    }

}
