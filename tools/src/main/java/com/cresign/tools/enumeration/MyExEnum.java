package com.cresign.tools.enumeration;

import lombok.Getter;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 通用异常枚举类
 */
@Getter
public enum MyExEnum {


    // 530 添加失败
    // 540 修改失败
    // 550 删除失败
    // 4004 后端查询资源为空
    // 4000 前端传参错误或者为空
    // 601 验证编号失败
    // 602 替换零件失败

    DIY_ERROR("4433","/","出现异常"),

    NO_PERSONAL_JURISDICTION("4403","DG","无个人权限!"),

    /**
     * 其他异常信息
     */
    TOKEN_IS_NULL("401","/","请求头token为空"),

    EXCEL_IMPORT_SUCCESS("200","/","excel导入成功"),

    PERMISSION_IS_NULL("403","/","无权限，拒绝访问"),

    GET_PARAMETER_IS_NULL("404.1","/","请求参数为空"),

    REDIS_TOKEN_IS_NULL("401.1","/","数据库无Token"),

    DATE_TRANSFORMATION_ERROR("510","/","日期转换错误"),

    ALL_SYSTEM_ERROR("5000","无","系统内部出现异常"),

    DEL_ALL_SUCCESS("200", "", "删除成功"),

    DEL_ALL_ERROR("550","","删除失败"),

    UPDATE_ALL_SUCCESS("200", "", "修改成功"),

    UPDATE_ALL_ERROR("540","","修改失败"),

    VERSION_NOT("3001", "", "版本错误"),

    UPDATE_ALL_REPEAT("230","","已重复修改"),

    KEY_IS_NOT_FOUND("4000", "", "传入的参数错误"),

    COPY_ALL_SUCCESS("200", "", "拷贝成功"),

    COPY_ALL_FAIL("560", "", "拷贝失败"),

    LOOKUP_SUCCESS("200", "", "查询成功"),

    LOOKUP_NOT_FOUND("4004", "", "查询为空"),

    LOOKUP_FAIL("250", "", "查询失败"),

    COVER_SUCCESS("200", "", "卡片覆盖成功"),

    COVER_FAIL("520", "", "卡片覆盖失败"),

    RELOAD_SUCCESS("200", "", "刷新成功"),

    RELOAD_ERROR("570", "", "刷新失败"),

    PARAMS_NOT_FOUND("4005", "", "参数为空"),


    /**
     * SumChkinx
     */

    GET_USERCHKINX_NOTFOUND("4004", "", "没有查找到您的最近十天的打卡信息"),

    /**
     * Init 信息
     */

    GET_INIT_SUCCESS("220", "", "获取成功不需要刷新"),

    GET_INIT_TMD_ERROR("1003", "", "时间不相同重新获取"),


    /**
     * sms短信验证码发送
     */
    SMS_SEND_SUCCESS("200", "", "发送成功"),

    SMS_SEND_ERROR("680", "", "发送失败"),

    SMS_GET_SUCCESS("200", "", "验证码验证成功"),

    SMS_GET_ERROR("670", "", "验证码不正确"),

    SMS_GET_TIMEOUT("690", "", "验证码已过期"),


    /**
     * 动态码
     */
    DYNAMICCODE_SUCCESS("200", "", "识别正确"),
    DYNAMICCODE_ERROR("450", "", "识别错误，请重新再扫"),
    DYNAMICCODE_NOT_FOUND("440", "", "该机子没有注册"),
    DYNAMICCODE_BIND_SUCCESS("200", "", "namespace: dev"),
    DYNAMICCODE_BIND_ERROR("5005", "", "绑定失败，请联系客服或者重新绑定一次"),
    DYNAMICCODE_BIND_HAVE("5006", "", "该端已经绑定过了"),
    DYNAMICCODE_BIND_NOT_FOUND("5006", "", "该端没有进行绑定，请先绑定"),
    DYNAMICCODE_SAVE_SUCCESS("200", "", "该端录入成功"),

    DYNAMICCODE_UPDATE_ERROR("5008", "", "修改失败，请联系客服"),
    DYNAMICCODE_UPDATE_HAVE("5003", "", "已经修改过了，请勿再修改"),



    /**
     * 扫码防伪 异常信息
     */
    QRANTI_IS_FALSE("640", "", "防伪失败"),

    QRANTI_IS_TRUE("200", "", "防伪成功"),

    /**
     * 操作COUP表异常信息
     */

    OPERATION_COUP_SUCCESS("200", "", "操作成功"),
    OPERATION_COUP_FAIL("580", "", "操作失败"),
    /**
     * token 异常信息
     */
    REFRESH_TOKEN_SUCCESS("200", "", "重新获取token成功"),

    REFRESH_TOKEN_NOT_TRUE("402", "", "请重新登录"),


    /**
     * 图片上传 异常信息
     */
    FILE_UPLOAD_SUCCESS("200", "", "图片上传成功"),

    FILE_UPLOAD_ERROR("660", "", "图片上传失败"),

    FILE_DEL_SUCCESS("200", "", "文件删除成功"),

    FILE_DEL_ERROR("662", "", "文件删除失败"),

    FILE_SELECT_TOKEN("200", "", "获取查看文件令牌成功"),

    FILE_UPLOAD_TOKEN("200", "", "获取上传文件令牌成功"),


    /**
     * 微信 异常信息
     */
    WX_NOT_NUMBER_BIND("603", "","没有绑定微信"),

    WX_BIN_SUCCESS("200", "","微信绑定成功"),

    WX_BIND_ERROR("604", "","微信绑定失败"),

    WX_RELIEVE_BIND_SUCCESS("200", "", "微信解除绑定成功"),

    WX_RELIEVE_BIND_ERROR("605", "", "微信解除绑定失败"),

    WX_BIND_HAVE("611", "", "该账号已经绑定了微信"),

    WX_DECRYPT_SUCCESS("200", "", "解密成功"),

    WX_DECRYPT_ERROR("620", "", "解密失败"),

    WX_CODE_IS_NULL("4004","","code不能为空"),


    /**
     * Login 异常信息
     */
    LOGIN_USER_SUCCESS("200", "/loginF/getAuth", "用户登录成功"),

    LOGIN_USER_PWD_ERROR("500.1","","密码错误"),



    /**
     * 用户注册 异常信息
     */
    register_USER_SUCCESS("200", "","注册成功"),

    register_USER_ERROR("6001", "","注册失败"),


    /**
     * 加入公司 异常信息
     */
    JOIN_COMP_IS_HAVE("6451", "","您已加入过该公司了"),

    JOIN_COMP_SUCCESS("200", "","加入该公司成功"),

    JOIN_COMP_ERROR("6214", "","加入该公司失败"),



    /**
     * List 异常信息
     */
    LIST_GET_SUCCESS("200", "/compF/getList", "获取列表成功"),

    LIST_GET_NOTFOUND("4004", "/compF/getList", "当前菜单列表下没有数据"),

    LIST_POST_SUCCESS("200", "", "获取数据成功"),

    LIST_POST_NOTFOUND("4004", "", "查找数据为空"),

    /**
     * SwitchComp 异常信息
     */
    SWITCH_COMP_SUCCESS("200", "", "切换公司成功"),
    SWITCH_COMP_ERROR("5008", "", "切换公司失败"),



    /**
     * Single 异常信息
     */
    SINGLE_GET_SUCCESS("200", "/singleF/getSingle", "获取详细信息成功"),

    SINGLE_GET_NOTFOUND("4004", "/singleF/getSingle", "获取详细信息失败"),


    /**
     * Log 异常信息
     */
    LOG_ADD_MSG("200", "WebSocket:/log/setLog", "msg日志新增操作"),

    LOG_GET("200", "WebSocket:/log/getLog", "根据角色权限获取日志操作"),

    LOG_ADD_CHK_IN("200", "Service:/logCh/setLogChD", "chkin日志新增操作"),

    LOG_GET_TYPE_COUNT("200", "WebSocket:/log/getCountByLog", "获取指定日志数量操作"),

    LOG_GET_USER_INFO_SUCCESS("200", "", "获取用户info信息成功"),

    LOG_ADD_LIST_SUCCESS("200", "", "新增日志成功"),

    LOG_ADD_LIST_ERROR("500", "", "新增日志失败"),


    /**
     * Comp 异常信息
     */
    COMP_IS_NULL("4004", "", "请求公司数据库信息为空"),

    COMP_ADD_SUCCESS("200", "", "添加公司成功"),

    COMP_ADD_ERROR("530", "", "添加公司失败"),

    COMP_DEL_SUCCESS("200", "", "公司删除成功"),

    COMP_DEL_ERROR("550", "", "公司删除失败"),

    COMP_COVER_SUCCESS("200", "", "公司卡片覆盖成功"),

    COMP_COVER_LIST_SUCCESS("200", "", "公司列表覆盖成功"),

    COMP_CHK_IN_ZON_IS_NULL("500", "", "公司打卡总结果为空！"),

    COMP_CHK_IN_EXCEL_SUCCESS("200", "", "公司打卡excel打印成功"),

    /**
     * Prod 异常信息
     */
    PROD_LIST_NULL("4004", "", "获取列表为空,任务开始操作失败"),

    PROD_IS_NULL("4004", "", "获取产品信息为空"),

    PROD_PART_IS_NULL("4004", "", "获取产品零件信息为空"),

    PROD_GET_CAS_ITEM_X_SUCCESS("200", "", "获取订单CasItemX成功"),

    PROD_DG_CAS_ITEM_X_SUCCESS("200", "", "递归订单CasItemX成功"),

    PROD_DG_ALL_PART_SUCCESS("200", "", "递归产品allpart成功"),

    PROD_DG_OK("200", "", "递归操作成功"),

    PROD_ADD_SUCCESS("200", "", "产品添加成功"),

    PROD_ADD_ERROR("530", "", "产品添加失败"),

    PROD_DEL_SUCCESS("200", "", "产品删除成功"),

    PROD_DEL_ERROR("550", "", "产品删除失败"),

    PROD_UPDATE_SUCCESS("200", "", "产品更新成功"),

    PROD_COVER_SUCCESS("200", "", "产品卡片覆盖成功"),

    PROD_COVER_LIST_SUCCESS("200", "", "产品列表覆盖成功"),

    PROD_PDF_SUCCESS("200", "", "获取Pdf数据成功"),

    PROD_QTYSAFE_SAFE("200", "", "现库存量还未低于安全库存量"),

    PROD_QTYSAFE_NOSAFE("590", "", "现库存量低于安全库存量,警告！！！自动已发送报告信息！"),

    /**
     * ProdDG
     */
    PROD_DG_RESULT_NULL("4004", "", "递归结果为空"),


    /**
     * User 异常信息
     */
    USER_IS_HAVE("630", "", "用户已存在"),

    USER_IS_NULL("4004", "", "用户不存在"),

    USER_ADD_ERROR("530", "", "添加用户失败"),

    USER_DEL_SUCCESS("200", "", "用户删除成功"),

    USER_DEL_ERROR("550", "", "用户删除失败"),

    USER_COVER_LIST_SUCCESS("200", "","用户列表覆盖成功"),

    /**
     * Order 异常信息
     */
    ORDER_START_SUCCESS("200","WebSocket:/orderWS4/getKai","开始任务"),

    ORDER_ERR("200","","零件出现问题"),

    ORDER_ERR_HANDLE("200","","零件问题正在解决中..."),

    ORDER_ERR_SOLVE("200","","零件问题已解决"),

    ORDER_End_SUCCESS("200","WebSocket:/orderWS4/getJie","结束任务"),

    ORDER_ADD_SUCCESS("200", "", "订单添加成功"),

    ORDER_ADD_ERROR("530","","订单添加失败"),

    ORDER_IS_NULL("4004", "", "订单不存在"),

    ORDER_DEL_ERROR("550", "", "订单删除失败"),

    ORDER_GET_O_ITEM_IS_NULL("4004","","订单无递归信息"),

    ORDER_CAS_ITEM_X_IS_NULL("4004","","获取的订单casItemx为空"),

    ORDER_CAS_ITEM_X_IS_OK("200","","获取casItemx成功！"),

    ORDER_RECOVERY_IS_OK("200","","订单恢复成功"),

    ORDER_ERR_RECOVERY_IS_OK("200","","异常订单恢复成功"),

    ORDER_ITEM_RECOVERY_IS_OK("200","","订单零件恢复成功"),

    ORDER_ITEM_STOP_IS_OK("200","","订单零件停止成功"),

    ORDER_ERR_IS_NULL("4004","","订单问题数据为空"),

    ORDER_T_ERR("200","","订单提出问题"),

    ORDER_T_ERR_HANDLE("200","","订单问题正在解决中..."),

    ORDER_T_ERR_REPULSE("200","","订单问题已打回"),

    ORDER_T_ERR_SOLVE("200","","订单问题已解决"),

    ORDER_COVER_SUCCESS("200","","订单卡片覆盖成功"),

    ORDER_COVER_LIST_SUCCESS("200","","订单卡片覆盖成功"),

    ORDER_OPERATION_BE_PROCESSED("200","","该操作已被处理"),

    ORDER_DG_PRICE("200","","成本计算成功!"),

    ORDER_BY_PDF_SUCCESS("200","","获取PDF信息成功!"),

    /**
     * lSProd 异常信息
     */
    LSPROD_ADD_SUCCESS("200", "", "产品添加成功,产品列表添加成功"),

    LSPROD_ADD_ERROR("530", "", "产品列表添加失败"),

    LSPROD_DEL_SUCCESS("200", "", "产品删除成功，产品列表删除成功"),

    LSPROD_DEL_ERROR("550", "", "产品列表删除失败"),


    /**
     * lBProd 异常信息
     */
    LBPROD_ADD_SUCCESS("200", "", "零件添加成功,零件列表添加成功"),

    LBPROD_ADD_ERROR("530", "", "零件列表添加失败"),

    LBPROD_DEL_SUCCESS("200", "","零件删除成功，零件列表删除成功"),

    LBPROD_DEL_ERROR("550", "", "零件列表删除失败"),



    /**
     * lBUser 异常信息
     */
    LBUSER_ADD_SUCCESS("200", "","用户添加成功,用户列表添加成功"),

    LBUSER_ADD_ERROR("530", "","用户列表添加失败"),

    LBUSER_DEL_SUCCESS("200", "","员工删除成功，员工列表删除成功"),

    LBUSER_DEL_ERROR("550","","员工列表删除失败"),

    LBUSER_COVER_SUCCESS("200","","用户卡片覆盖成功"),
    /**
     * lSComp 异常信息
     */
    LSCOMP_ADD_SUCCESS("200", "","添加卖公司成功,添加卖家列表成功"),

    LSCOMP_ADD_ERROR("530", "","添加卖家列表失败"),

    LSCOMP_DEL_SUCCESS("200","","公司删除成功,产品列表删除成功"),

    LSCOMP_DEL_ERROR("550","","公司列表删除失败"),

    LSCOMP_COVER_SUCCESS("200", "","公司覆盖成功"),

    /**
     * lBComp 异常信息
     */
    LBCOMP_ADD_SUCCESS("200", "", "添加公司成功,添加买家列表成功"),

    LBCOMP_ADD_ERROR("530", "","添加买家列表失败"),

    LBCOMP_DEL_SUCCESS("200","","供应商删除成功,供应商列表删除成功"),

    LBCOMP_DEL_ERROR("550","","供应商列表删除失败"),


    /**
     * lSOrder 异常信息
     */
    LSORDER_ADD_SUCCESS("200", "", "添加卖家订单成功，添加卖家订单列表成功"),

    LSORDER_ADD_ERROR("530", "", "添加卖家订单列表失败"),

    LSORDER_DEL_SUCCESS("550","","订单删除成功,公司订单列表删除成功"),

    LSORDER_DEL_ERROR("550", "","公司订单列表删除失败"),


    /**
     * lBOrder 异常信息
     */
    LBORDER_DEL_SUCCESS("200","","订单删除成功，卖家订单删除成功"),

    LBORDER_DEL_ERROR("550","","卖家订单删除失败"),


    /**
     * 验证编号 ref
     */
    REF_IS_NULL("200", "", "该编号可以使用"),

    REF_IS_TRUE("601", "", "该编号已存在,不可使用"),


    /**
     * 切换零件 异常信息
     */

    REPLACE_PARTS_SUCCESS("200" ,"","替换成零件成功"),

    REPLACE_PARTS_ERROR("602", "","替换零件失败"),


    /**
     * 文件生成异常
     */
    FILE_PDF_SUCCESS("200", "", "pdf打印成功"),

    FILE_PDF_ERROR("5001", "", "pdf打印错误"),

    FILE_PDF_RESULT_NULL("4004","","pdf打印结果为空"),

    FILE_PDF_ERROR_IMG("5001","","pdf打印二维码图片错误"),

    FILE_PDF_ERROR_TO_IMG("5001","","pdf打印头部图片错误"),

    FILE_PDF_ERROR_TO_TABLE_1("5001","","pdf打印toTable1表格错误"),

    FILE_PDF_ERROR_TO_TABLE_3("5001","","pdf打印toTable3表格错误"),

    FILE_EXCEL_SUCCESS("200", "", "excel打印成功"),


    /**
     * WX绑定手机状态码
     */

    WX_BIND_PHONE_SUCCESS("200","", "绑定成功"),
    WX_BIND_PHONE_ERROR("625","", "绑定失败"),
    WX_BIND_PHONE_HAVE("615", "", "用户已经绑定过手机了"),

    /**
     * 二维码APP登录
     */
    APP_LOGIN_QRCODE_GET_SUCCESS("200", "", "获取二维码成功"),

    ;

    /**
     * 异常状态码
     */
    private String code;

    /**
     * 发生的方法，位置等
     */
    private String method;

    /**
     * 描述
     */
    private String descInfo;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     * ##Params: method    异常位置
     * ##Params: descInfo  异常描述
     */
    MyExEnum(String code, String method, String descInfo){
        this.code = code;
        this.method = method;
        this.descInfo = descInfo;
    }



}
