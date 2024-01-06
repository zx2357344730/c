package com.cresign.tools.enumeration;

import lombok.Getter;

@Getter
public enum ErrEnum {

    /**
     * 没有设置二维码
     */
    NO_SET_MODE("1080"),

    /**
     * 二维码已过期
     */
    QRCODE_NOT_FOUND("1081"),

    /**
     * 分享视图没有设置
     */
    VIEWR_NOT_SET("1083"),


    // 日志操作成功
    LOG_SUCCESS("200"),

    // 日志操作失败
    LOG_ERROR("1005"),

    // 日志操作无权限失败
    LOG_PERMISSION_IS_NULL("403"),

    // 获取数据库数据为空    : tang
    LOG_GET_DATA_NULL("50404"),

    // 失败
    LOG_FAILF("5000"),

    // 新增日志失败
    LOG_ADD_LIST_ERROR("5750"),

    //不存在
    NO_HAVE("6002"),

    //refAuto不存在
    REFAUTO_ERROR("6002"),

    //公司为空或者lST大于8
    COMP_IS_NULL_LST("6009"),

    //公司为空或者lAT不等于2、数量不等空
    COMP_IS_NULL_LAT("6009"),

    //标题与数据库不符
    TITLE_NO_NULL("6010"),


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
    //assset不存在     Login,Purchase服务在用
    ASSET_NOT_FOUND("021005"),
    //公司不存在     Login,Purchase服务在用
    COMP_NOT_FOUND("021006"),
    //卡片不存在
    CARD_NO_HAVE("021007"),
    //redis订单不存在       Login服务在用
    REDIS_ORDER_NO_HAVE("021008"),
    //用户已经不在该公司了     Login服务在用
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
    //资产负载不相等
    PRICE_DIFFERENT("022029"),
    //产品不同
    PROD_ERROR("022030"),
    //公司不同
    COMP_ERROR("022031"),
    //货架上还有物品
    LOCADDR_EXIST("022032"),
    //过程不同
    PROC_ERROR("022033"),

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

    DB_ERROR("029001"),

    SAVE_DB_ERROR("029003"),

    ES_DB_ERROR("029002"),

    VALUE_IS_NULL("029009"),

    //图片压缩出错
    PICTURE_COMPRESS_ERROR("030001"),
    //文件类型错误
    FILE_TYPE_ERROR("030002"),
    //对象不存在
    ASSET_OBJECT_NO_HAVE("030003"),
    //文件对象不存在
    FILE_OBJECT_NO_HAVE("030004"),
    //bcdNet错误 = 1
    BCDNET_ERROR("030006"),
    //公司文件容量不足
    POWER_NOT_ENOUGH("030011"),

    //传入的参数错误       Login服务在用
    KEY_IS_NOT_FOUND("060001"),
    // 查找的数据为空       Login服务在用
    DATA_IS_NULL("060002"),
    //init_java不存在       Login服务在用
    INIT_IS_NULL("060003"),
    //用户不存在       Login服务在用
    USER_IS_NO_FOUND("060005"),
    //用户已加入到该公司       Login服务在用
    USER_JOIN_IS_HAVE("060007"),
    //用户加入公司错误       Login服务在用
    USER_JOIN_COMP_ERROR("060008"),
    //不能删除默认组别       Login服务在用
    DEFAULT_KEY("060009"),
    //统计分组字段为空       Login服务在用
    EXCELFIELD_IS_NULL("060010"),
    //统计数据大于1000条       Login服务在用
    STAT_LENGTH_GT("060011"),
    //文件名小于3字       Login服务在用
    FILENAME_LENGTH_LT("060012"),

    /**
     QR
     */
    //二维码防伪失败       Login服务在用
    QR_CODE_Anti_ERROR("061001"),
    //加入公司二维码已过期       Login服务在用
    JOIN_COMP_CODE_OVERDUE("061002"),
    //加入公司二维码已存在       Login服务在用
    JOINCOMP_CODE_EXIST("061003"),
    //产品二维码已经存在       Login服务在用
    PROD_CODE_IS_EXIT("061004"),
    //产品二维码已过期       Login服务在用
    PROD_CODE_OVERDUE("061005"),
    //统计结果为空       Login服务在用
    STAT_IS_NULL("060013"),


    //jobName已存在
    JOBNAME_IS_EXIST("070001"),
    //jobName不存在
    JOBNAME_NOT_EXIST("070002"),
    //新建索引错误
    ADDINDEX_ERROR("070003"),

    // ActionEnum
    /**
     * 不识别请求    Action,Chat服务在用
     */
    ERR_DONT_RECOGNIZE("01001"),
    /**
     * 获取订单信息为空     Action,Chat服务在用
     */
    ERR_GET_ORDER_NULL("01002"),
    /**
     * 获取用户信息为空     Action,Chat服务在用
     */
    ERR_GET_USER_NULL("01003"),
    /**
     * 获取数据为空     Action,Chat服务在用
     */
    ERR_GET_DATA_NULL("01004"),
    /**
     * 操作已被处理     Action,Chat服务在用
     */
    ERR_OPERATION_IS_PROCESSED("01005"),
    /**
     * 订单不存在     Action,Chat服务在用
     */
    ORDER_NOT_EXIST("01006"),
    /**
     * 递归结果为空     Action,Chat服务在用
     */
    ERR_RECURSION_RESULT_IS_NULL("01007"),
    /**
     * 产品数据为空     Action,Chat服务在用
     */
    ERR_PROD_DATA_IS_NULL("01008"),
    /**
     * 排除后的产品信息为空     Action,Chat服务在用
     */
    ERR_AFTER_EXCLUSION_PROD_IS_NULL("01009"),
    /**
     * 无递归零件     Action,Chat服务在用
     */
    ERR_NO_RECURSION_PART("01010"),
    /**
     * 产品不存在     Action,Chat服务在用
     */
    ERR_PROD_NOT_EXIST("01011"),

    /**
     * 产品无零件     Action,Chat服务在用
     */
    ERR_PROD_NO_PART("01012"),
    /**
     * 零件信息为空     Action,Chat服务在用
     */
    ERR_PART_IS_NULL("01013"),
    /**
     * 操作失败     Action,Chat,Purchase服务在用
     */
    ERR_OPERATION_FAILED("01014"),
    /**
     * 公司没有客服     Action,Chat服务在用
     */
    ERR_COMP_NO_CUSTOMER_SERVICE("01015"),
    /**
     * 个人信息有误     Action,Chat服务在用
     */
    ERR_INCORRECT_PERSONAL_INFORMATION("01016"),
    /**
     * 订单被确认     Action,Chat服务在用
     */
    ERR_ORDER_CONFIRMED("01017"),
    /**
     * 出现错误     Action,Chat服务在用
     */
    ERR_AN_ERROR_OCCURRED("01018"),
    /**
     * 主公司编号为空     Action,Chat服务在用
     */
    ERR_MAIN_COMP_ID_IS_NULL("01019"),
    /**
     * 供应商编号为空     Action,Chat服务在用
     */
    ERR_SUPPLIER_ID_IS_NULL("01020"),
    /**
     * 不是主公司也不是供应商     Action,Chat服务在用
     */
    ERR_NO_MAIN_COMP_AND_NO_SUPPLIER("01021"),
    /**
     * 没有主公司关联该订单     Action,Chat服务在用
     */
    ERR_NO_MAIN_COMP_RELATION_ORDER("01022"),
    /**
     * 没有供应商关联该订单     Action,Chat服务在用
     */
    ERR_NO_SUPPLIER_RELATION_ORDER("01023"),
    /**
     * 订单未合并     Action,Chat服务在用
     */
    ERR_ORDER_NOT_MERGE("01024"),
    /**
     * 主订单编号为空     Action,Chat服务在用
     */
    ERR_MAIN_ORDER_ID_IS_NULL("01025"),
    /**
     * 不可操作该订单     Action,Chat服务在用
     */
    ERR_NO_OPERATION_ORDER("01026"),
    /**
     * 子订单为空     Action,Chat服务在用
     */
    ERR_SON_ORDER_IS_NULL("01027"),
    /**
     * es获取数据为空     Action,Chat服务在用
     */
    ERR_ES_GET_DATA_IS_NULL("01028"),


    /**
     * 需要Final 确认     Action,Chat服务在用
     */
    ERR_ORDER_NEED_FINAL("01029"),

    /**
     * DG 无限Loop 了     Action,Chat服务在用
     */
    ERR_PROD_RECURRED("01030"),

    /**
     * gpIo已经被绑定     Action,Chat服务在用
     */
    ERR_IO_ALREADY_BINDED("01101"),
    /**
     * gpIo已经被解绑     Action,Chat服务在用
     */
    ERR_IO_ALREADY_UNBIND("01102"),
    /**
     * 资产为空
     */
    ERR_ASSET_NULL("01103"),
    /**
     * 资产内Arrange为空
     */
    ERR_ASSET_ARRANGE_NULL("01104"),
    /**
     * 资产内Arrange内objTask为空
     */
    ERR_ASSET_ARRANGE_OBJ_TASK_NULL("01105"),
    /**
     * 资产内已存在
     */
    ERR_ASSET_EXISTS_NULL("01106"),
    /**
     * 资产内Info为空
     */
    ERR_ASSET_INFO_NULL("01107"),
    /**
     * 公司编号不匹配
     */
    ERR_ID_C_NO_MATCHING("01108"),
    /**
     * 资产编号为空
     */
    ERR_ASSET_ID_NULL("01109"),
    /**
     * 资产时间正在处理中
     */
    ERR_ASSET_TASK_PROCESSING("01110"),
    /**
     * 订单为空
     */
    ERR_ORDER_NULL("01111"),
    /**
     * 订单卡片oItem为空
     */
    ERR_ORDER_O_ITEM_NULL("01112"),
    /**
     * 订单卡片oItem内objItem为空
     */
    ERR_ORDER_O_ITEM_OBJ_ITEM_NULL("01113"),
    /**
     * 资产内chkin00s为空
     */
    ERR_ASSET_CHK_IN_00_S_NULL("01114"),
    /**
     * 订单卡片oStock为空
     */
    ERR_ORDER_O_STOCK_NULL("01115"),
    /**
     * 订单卡片oStock内objData为空
     */
    ERR_ORDER_O_STOCK_OBJ_DATA_NULL("01116"),
    /**
     * 未知异常
     */
    ERR_UNKNOWN("01117"),

    // ChatEnum
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

    // LoginEnum
    // 密码错误
    LOGIN_PWD_ERROR("041001"),
    // 微信没有绑定
    WX_NOT_BIND("041002"),
    // facebook没有绑定
    FACEBOOK_NOT_BIND("041003"),
    // linked没有绑定
    LINKED_NOT_BIND("041004"),
    //refreshToken过期或者不存在
    REFRESHTOKEN_NOT_FOUND("041005"),
    //登出成功
    LOGOUT_SUCCESS("041006"),
    //登出失败
    LOGOUT_ERROR("041007"),
    //您已注册过了
    REGISTER_USER_IS_HAVE("041008"),
    //注册失败
    REGISTER_USER_ERROR("041009"),
    //登录二维码不存在或者已过期
    LOGIN_CODE_OVERDUE("041010"),

    // 没有找到该用户
    LOGIN_NOTFOUND_USER("042000"),
    //用户已经在注销了
    USER_REG_OFF("042003"),



    //主菜单有子菜单数据
    MAINMENU_USE_SUBMENU("043000"),
    //默认菜单不可删除
    REF_DEL_ERROR("043001"),
    //菜单不存在
    MENU_DEL_ERROR("043002"),
    //JWT用户验证错误
    JWT_USER_VALIDATE_ERROR("043003"),
    //JWT用户过期
    JWT_USER_OVERDUE("043004"),


    //没有设置职位权限
    ROLE_NOT_SET("044000"),
    //权限修改出现错误
    ROLE_UP_ERROR("044001"),
    //grp没有找到权限
    GRP_NOT_AUTH("044002"),
    //没有找到该权限
    NOT_FOUND_AUTH("044003"),
    //没有Control objMod 权限
    NOT_FOUND_MODULE("044004"),
    /**
     SMS系列
     */
    // 验证码发送错误
    SMS_SEND_CODE_ERROR("045001"),
    // 短信码不存在
    SMS_CODE_NOT_FOUND("045002"),
    // 验证码不正确
    SMS_CODE_NOT_CORRECT("045003"),
    // 短信发送成功
    SMS_SEND_SUCCESS("045004"),
    // 短信登录失败
    SMS_LOGIN_FAIL("045005"),

    // Purchase PurchaseEnum
    //沒有找到该公司
    PR_COMP_DATA_NO_FOUND("051001"),
    // 不是负责人
    NO_CHARGE_USER("051003"),
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
    //lSProd不存在
    LS_PROD_NOT_FOUND("051010"),
    //lBProd不存在
    LB_PROD_NOT_FOUND("051011"),
    //lSBProd不存在
    LSB_PROD_NOT_FOUND("051018"),
    //lSInfo不存在
    LS_INFO_NOT_FOUND("051012"),
    //lBInfo不存在
    LB_INFO_NOT_FOUND("051013"),
    //lNUser不存在
    LN_USER_NOT_FOUND("051014"),
    //lNComp不存在
    LN_COMP_NOT_FOUND("051015"),
    // 类型不存在
    TYPE_NOT_FOUND("051016"),
    //chkin不存在
    CHK_IN_NOT_FOUND("051017"),

    // 订单已过期 : tang
    ERR_ORDER_BE_OVERDUE("05001"),
    // 购买信息为空 : tang
    ERR_PURCHASE_INFO_IS_NULL("05003"),
    // 购买失败 : tang
    ERR_PURCHASE_FAIL("05004"),
    ERR_KEY_IS_NULL("05005"),
    ERR_SHARE_NULL("05006"),
    ;
    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    ErrEnum(String code){
        this.code = code;

    }

}
