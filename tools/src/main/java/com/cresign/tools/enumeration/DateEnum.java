package com.cresign.tools.enumeration;

/**
 * @author tangzejin
 * @updated 2019/8/22
 * @ver 1.0.0
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

    DATE_YYYYMMMDDHHMMSS("yyyy/MM/dd HH:mm:ss","日期格式：年/月/日 时:分:秒");

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
