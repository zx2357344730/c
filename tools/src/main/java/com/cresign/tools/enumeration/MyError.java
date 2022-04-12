package com.cresign.tools.enumeration;

import lombok.Getter;

/**
 * ##author: Jeovn
 * ##updated: 2020/5/23
 * ##version: 1.1.0
 * ##description: 通用异常枚举类
 */
@Getter
public enum MyError {


    /**
     *
     * !!!!!!!!【添加状态码前缀数字说明】!!!!!!!!
     * !!!!!!!!【使用之前必须先查找你想添加的状态码是否已经有了，保证状态码是唯一的】!!!!!!!!
     * !!!!!!!!【在这里添加完状态码后，请在数据库Init表中中添加】!!!!!!!!
     *
     * 1xx  Informational（信息性状态码）      接受的请求正在处理
     * 2xx  Success（成功状态码）             请求正常处理完毕
     * 3xx  Redirection（重定向状态码）	   需要进行附加操作以完成请求
     * 4xx  Client Error（客户端错误状态码）  服务器无法处理请求
     * 5xx  Server Error（服务器错误状态码）  服务器处理请求出错
     * 51xx Comp表中的错误状态码
     * 52xx Order表中的错误状态码
     * 53xx User表中的错误状态码
     * 54xx Prod表中的错误状态码
     * 55xx Asset表中的错误状态码
     * 57xx Log表中的错误状态码
     */


    /*
         -------------- 500 errCode start --------------------
     */

    // 失败
    FAILF("5000"),



    /*
         -------------- 500 errCode end --------------------
     */



    /*
        ---------------------------- start 公共状态码---------------------------------
     */

    // 成功
    SUCCESS("200"),

    // 开始
    START("2001"),

    // 请重新登录
    REFRESH_TOKEN_NOT_TRUE("402"),

    // 无权限，拒绝访问
    PERMISSION_IS_NULL("403"),

    // 请求参数为空
    GET_PARAMETER_IS_NULL("4104"),

    // 查找的数据为空
    DATA_IS_NULL("5404"),


    // 获取数据库数据为空    : tang
    GET_DATA_NULL("50404"),

    // 文件删除失败
    FILE_DEL_ERROR( "50651"),

    // 文件类型错误
    FILE_TYPE_ERROR( "50652"),

    // 拷贝失败
    COPY_ALL_FAIL("5061"),

    // 覆盖失败
    COVER_FAIL("5062"),

    // 发送失败
    SEND_ERROR("5063"),

    // 验证码不正确
    GET_ERROR("5064"),

    // 验证码已过期
    SMS_GET_TIMEOUT("5065"),

    SMS_SEND_ERROR("5066"),

    // 绑定失败
    BIND_PHONE_ERROR("5071"),

    // 解除绑定失败
    RELIEVE_BIND_ERROR("5072"),

    // 修改失败
    ALL_ERROR("5010"),

    //传入的参数错误
    KEY_IS_NOT_FOUND("4000"),

    //已存在
    IS_HAVE("6001"),

    //不存在
    NO_HAVE("6002"),

    // 请联系管理员
    CONTACT_ABMIN("5001"),

    // 请勿重复操作
    NO_REPEAT_OPERATION("5002"),

    // 相同版本不需要发送数据给前端，message 等于null
    GET_IDENTICAL_SUCCESS("220"),

    // 系统错误
    ALL_SYSTEM_ERROR("500"),

    // 版本修改错误
    UPDATE_VER_ERROR("5008"),

    // 二维码防伪失败      $
    QRANTI_IS_FALSE("50060"),

    // 不是负责人
    NO_CHARGE_USER("4031"),



    /*
        ---------------------------- end 公共状态码 ---------------------------------
     */


    /*
        ---------------------------- start Comp表状态码 -------------------------------
     */

    // 添加公司失败
    LSBCOMP_ADD_ERROR("5151"),

    // 删除公司失败
    LSBCOMP_DEL_ERROR("5152"),

    // 查找公司失败
    LSBCOMP_LOOKUP_ERROR("5153"),

    // 修改公司失败
    LSBCOMP_ALL_ERROR("5154"),

    /*
        ---------------------------- end Comp表状态码 -------------------------------
     */




    /*
        ---------------------------- start Order表状态码 -------------------------------
     */

    // 添加订单失败
    ORDER_ADD_ERROR("5251"),

    // 删除订单失败
    ORDER_DEL_ERROR("5252"),

    // 查找订单失败
    ORDER_LOOKUP_ERROR("5253"),

    // 修改订单失败
    ORDER_ALL_ERROR("5254"),

    /*
        ---------------------------- end Order表状态码 -------------------------------
     */



    /*
        ---------------------------- start User表状态码 -------------------------------
     */

    // 添加员工失败
    USER_ADD_ERROR("5351"),

    // 删除员工失败
    USER_DEL_ERROR("5352"),

    // 查找员工失败
    USER_LOOKUP_ERROR("5353"),

    // 修改员工失败
    USER_ALL_ERROR("5354"),

    /*
        ---------------------------- end User表状态码 -------------------------------
     */


    /*
       ---------------------------- start Prod表状态码 -------------------------------
    */

    // 添加产品失败
    PROD_ADD_ERROR("5451"),

    // 删除产品失败
    PROD_DEL_ERROR("5452"),

    // 查找产品失败
    PROD_LOOKUP_ERROR("5453"),

    // 修改产品失败
    PROD_ALL_ERROR("5454"),

     /*
        ---------------------------- end Prod表状态码 -------------------------------
     */



     /*
        ---------------------------- start Asset表状态码 -------------------------------
     */

    // 添加内部失败
    ASSET_ADD_ERROR("5551"),

    // 删除内部失败
    ASSET_DEL_ERROR("5552"),

    // 查找内部失败
    ASSET_LOOKUP_ERROR("5553"),

    // 修改内部失败
    ASSET_ALL_ERROR("5554"),

     /*
        ---------------------------- end Asset表状态码 -------------------------------
     */



    /*
        ---------------------------- start Log表状态码 -------------------------------
     */

    // 新增日志失败
    LOG_ADD_LIST_ERROR("5750"),

    /*
        ----------------------------end Log表状态码 -------------------------------
     */

    // 添加列表失败
    LIST_ADD_ERROR("5611"),

    // 删除列表失败
    LIST_DEL_ERROR("5612"),

    // 查找列表失败
    LIST_LOOKUP_ERROR("5613"),

    // 修改列表失败
    LIST_ALL_ERROR("5614"),




    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    MyError(String code){
        this.code = code;

    }




}