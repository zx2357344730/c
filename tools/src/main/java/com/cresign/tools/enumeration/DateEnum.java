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
    TIME_ONLY("HH:mm:ss","日期格式：时/分/秒"),

    TIME_MIN("HH:mm","日期格式：时/分"),

    DATE_ONLY("yyyy/MM/dd","日期格式：年/月/日"),

    DATE_FOLDER("yyyy-MM-dd","日期格式：年-月-日"),

    DATE_TIME_SSS("yyyy/MM/dd HH:mm:ss SSS","日期格式：年/月/日 时:分:秒 毫秒"),

    DATE_TIME_FULL("yyyy/MM/dd HH:mm:ss","日期格式：年/月/日 时:分:秒");

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
