package com.cresign.purchase.enumeration;

import lombok.Getter;

/**
 * @author Jeovn
 * @updated 2020/5/23
 * @ver 1.1.0
 * ##description: Details异常枚举类
 */
@Getter
public enum DetailsEnum {

    /**
     不存在系列   021开头
     */
    //对象不存在
    OBJECT_IS_NULL("021001"),
    //产品不存在
    PROD_NOT_FOUND("021002"),
    //用户不存在
    USER_NOT_FOUND("021003"),
    //订单不存在
    ORDER_NOT_FOUND("021004"),
    //assset不存在
    ASSET_NOT_FOUND("021005"),
    //公司不存在
    COMP_NOT_FOUND("021006"),
    //卡片不存在
    CARD_NO_HAVE("021007"),
    //redis订单不存在
    REDIS_ORDER_NO_HAVE("021008"),
    //用户已经不在该公司了
    USER_NOT_IN_COMP("021009"),
    //ES对象不存在
    LIST_OBJECT_NO_HAVE("021010"),
    //余额不足
    MONEY_NOT_HAVE("021011"),
    //找不到索引
    INDEXES_NO_HAVE("021012"),
    //格子不存在
    SPACE_NO_HAVE("021013"),
    //名字不存在
    WRDN_NO_HAVE("021014"),


    /**
     混杂系列   022开头
     */
    //对象已存在
    OBJECT_IS_HAVE("022001"),
    //不是负责人
    PR_NO_CHARGE_USER("022002"),
    //修改详情数据成功
    UPDATE_SINGLE_SUCCESS("022003"),
    //没有该详情的数据
    NOT_FOUND_SINGLE_MSG("022004"),
    //版本已被修改过，请刷新页面
    ALREADY_UPDATED("022005"),
    //切换公司出现异常
    SWITCH_COMP_ERROR("022006"),
    //更新详细信息出现错误
    UPDATE_SINGLE_ERROR("022007"),
    //不属于你的子公司
    id_C_NOT_ERROR("022008"),
    //已确认，不可删除
    CONFIRMED_ORDER_CANT_UPDATE("022009"),
    //类型不是0，错误，2代表是物品
    TYPE_ERROR("022010"),
    //ref不一致
    REF_ERROR("022011"),
    //键类型错误、或者为空
    KEY_TYPE_ERROR("022012"),
    //拆分数量大于原始数量
    WN2QTY_ERROR("022013"),
    //公司已存在
    COMP_IS_FOUND("022014"),
    //入库金额不能大于订单金额
    WN2CASH_ERROR("022015"),
    //用户的分组权限错误
    GRP_NOT_MATCH("022016"),
    //格子不为空且物品种类不同
    SPACE_OCCUPIED("022017"),
    //产品数量不足
    PROD_NOT_ENOUGH("022018"),
    //仓库描述为空
    LOCSETUP_IS_NULL("022019"),
    // tvs不相等
    TVS_NOT_EQUAL("022020"),
    //移动产品不同
    MOVE_PROD_DIFFERENT("022021"),
    //移动前后产品数量不同
    MOVE_NUM_DIFFERENT("022022"),
    //合并订单的公司不同
    MERGEORDER_COMP_DIFFERENT("022023"),
    //part层级错误
    PART_LEVEL_ERROR("022024"),
    //公司是假公司
    BCDNET_IS_0("022025"),
    //公司是真公司
    BCDNET_IS_1("022026"),
    //公司已连接
    CONNECT_COMP_EXIST("022027"),
    //产品已连接
    CONNECT_PROD_EXIST("022028"),
    //
    COOKIEX_ERROR("022029"),
    //资产负载不相等
    PRICE_DIFFERENT("022030"),
    //产品不同
    PROD_ERROR("022031"),
    //公司不同
    COMP_ERROR("022032"),
    //货架还有物品
    LOCADDR_EXIST("022033"),

    /**
      add COUPA系列   023开头
     */
    // 添加公司失败
    COMP_ADD_ERROR("023001"),
    // 添加客户失败
    LSCOMP_ADD_ERROR("023002"),
    // 添加供应商失败
    LBCOMP_ADD_ERROR("023003"),
    // 添加订单失败
    ORDER_ADD_ERROR("023004"),
    // 修改订单失败
    ORDER_ALL_ERROR("023005"),
    // 添加零件失败
    LBPROD_ADD_ERROR("023006"),
    // 添加产品失败
    LSPROD_ADD_ERROR("023007"),
    // 修改产品失败
    PROD_ALL_ERROR("023008"),
    // 添加内部失败
    ASSET_ADD_ERROR("023009"),
    // 添加列表失败
    LIST_ADD_ERROR("023010"),
    //修改资产失败
    ASSET_ALL_ERROR("023011"),

    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    DetailsEnum(String code){
        this.code = code;

    }
}