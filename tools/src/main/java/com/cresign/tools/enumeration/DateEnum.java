package com.cresign.tools.enumeration;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/22
 * ##version: 1.0.0
 * ##description: 时间异常枚举类
 */
public enum  DateEnum {

    /**
     * 定义常用日期
     */
    DATE_H_M_S("HH:mm:ss","日期格式：时/分/秒"),

    DATE_H_M("HH:mm","日期格式：时/分"),

    DATE_ONE("yyyy/MM/dd","日期格式：年/月/日"),

    ONE_DATE("yyyy-MM-dd","日期格式：年-月-日"),

    DATE_TWO("yyyy/MM/dd HH:mm:ss","日期格式：年/月/日 时:分:秒"),

    DATE_S_S_S("yyyy/MM/dd HH:mm:ss SSS","日期格式：年/月/日 时:分:秒 毫秒"),

    DATE_ONE_H("yyyy/MM/dd","日期格式：年/月/日"),

//    DATE_YYYYMMMDDHHMMSS("yyyy/MM/dd HH:mm:ss SSS","日期格式：年/月/日 时:分:秒 毫秒"),

    DATE_YYYYMMMDDHHMMSS("yyyy/MM/dd HH:mm:ss","日期格式：年/月/日 时:分:秒"),

//    MONDAY("星期一","星期"),
//    TUESDAY("星期二","星期"),
//    WEDNESDAY("星期三","星期"),
//    THURSDAY("星期四","星期"),
//    FRIDAY("星期五","星期"),
//    SATURDAY("星期六","星期"),
//    SUNDAY("星期日","星期"),






    DIN_ONE("定时任务1","定时器名称"),
    DIN_TWO("定时任务2","定时器名称"),
    DIN_THREE("定时任务3","定时器名称"),
    DIN_FOUR("定时任务4","定时器名称"),

    DIN_STATISTICS("登录统计","定时器名称");

    private String date;

    private String introduce;

    DateEnum(String date,String introduce){
        this.date = date;
        this.introduce = introduce;
    }

    public String getDate(){
        return this.date;
    }

    public String getIntroduce(){
        return this.introduce;
    }

}
