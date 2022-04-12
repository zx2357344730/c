package com.cresign.login.enumeration;

import lombok.Getter;

/**
 * ##author: Jeovn
 * ##updated: 2020/5/23
 * ##version: 1.1.0
 * ##description: 通用异常枚举类
 */
@Getter
public enum LoginEnum {


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
    LOGINOUT_SUCCESS("041006"),
    //登出失败
    LOGINOUT_ERROR("041007"),
    //您已注册过了
    REGISTER_USER_IS_HAVE("041008"),
    //注册失败
    REGISTER_USER_ERROR("041009"),
    //登录二维码不存在或者已过期
    LOGIN_CODE_OVERDUE("041010"),

    // 没有找到该用户
    LOGIN_NOTFOUND_USER("042000"),
    //没有找到该公司
    COMP_NOT_FOUND("042001"),
    //用户已经不在该公司了
    USER_NOT_IN_COMP("042002"),




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