package com.cresign.tools.enumeration;

import lombok.Getter;

/**
 * 用户行为日志类型
 * @author Jevon
 * @ver 1.0
 * @updated 2020/8/21 9:49
 * @return
 */
@Getter
public enum LogTypeEnum {

    /**
     * 通知日志类型
     */
    SUMMARY("summary"),
    /**
     * 递归日志类型
     */
    ACTION("action"),
    /**
     *  日志消息类型
     */
    MSG("msg"),
    /**
     * 打卡消息类型
     */
    CHKIN("chkin"),
    /**
     * 考勤反馈类型
     */
    HRFB("hrfb"),
    /**
     * 问题日志类型
     */
    PROB("prob"),
    /**
     * 用户注册类型
     */
    REGISTER("register"),
    /**
     * 创建公司
     */
    CREATECOMP("createcomp"),
    /**
     * 用户登录类型
     */
    LOGIN("login"),

    /**
     * 用户对软件的反馈
     */
    SOFTFB("softfb"),
    /**
     * 用户增加
     */
    ADDCOUPA("addcoupa"),
    /**
     * 用户删除
     */
    DELETECOUPA("deletecoupa"),
    /**
     * 用户修改
     */
    UPDATECOUPA("updatacoupa"),

    /**
     * 用户上传文件
     */
    SUMMFILE("summfile"),

    ;

    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    LogTypeEnum(String code){
        this.code = code;

    }


}
