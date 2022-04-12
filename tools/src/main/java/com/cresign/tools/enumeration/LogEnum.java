package com.cresign.tools.enumeration;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/20
 * ##version: 1.0.0
 * ##description: Log异常枚举类
 */
public enum LogEnum {

    /**
     * 用来拼接"_"用
     */
    UL("_", "拼接用的"),
    LOG_SUMMARY("summary", "通知日志类型"),
    LOG_ACTION("action", "递归日志类型"),
    LOG_MSG("msg", "日志消息类型"),
    LOG_CHK_IN("chkin", "打卡消息类型"),
    LOG_MONEY("money", "钱日志类型"),
    LOG_MONEY_MONEY_NOW("moneyNow", "钱计算方式"),
    LOG_PROB("prob", "问题日志类型"),
    LOG_PROD_QTYSAFE("prodsafe", "安全库存数日志类型"),

    LOG_TOTALOCOUNT("totalOCount", "完成数日志类型"),

    LOG_STATE("state", "状态日志类型"),
    LOG_TIME("time", "时间日志类型"),
    LOG_RATING("rating", "评分日志类型"),
    LOG_QTY("qty", "数量日志类型"),
    LOG_IS_SET_NO("0", "不写入日志"),
    LOG_IS_SET_YES("1", "写入日志"),
    LOG_REGISTER("register","用户注册类型"),

    LOG_CREATECOMP("createComp","创建公司"),

    LOG_INFO("info","修改info信息"),

    LOG_USAGE("usage", "用户登录类型"),
    LOG_CRUD("crud", "用户curd操作"),
    LOG_ERROR("error", "错误信息"),

    LOG_Attendance_HRFB("hrFb", "考勤反馈"),

    LOG_SCORE_SOFTFB("softFb", "用户对软件的反馈"),
    SUB_LOG_404("404", "子日志类型,404"),

    LOG_DEVC_CODE("devc", "动态码"),

    LOG_UPDATEMOD("updateMod","模塊的CRUD");


    /**
     * 类型
     */
    private String type;

    /**
     * 描述
     */
    private String introduce;

    /**
     * 带参构造方法
     * ##Params: type  日志类型
     * ##Params: introduce 日志描述
     */
    LogEnum(String type,String introduce){
        this.type = type;
        this.introduce = introduce;
    }

    public String getType(){
        return this.type;
    }

    public String getIntroduce(){
        return this.introduce;
    }
}
