package com.cresign.timer.utils;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//Java日期转换成Cron日期表达式工具类
@Component
public class CronUtil {

    /***
     *
     * ##param date
     * ##param dateFormat : yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String formatDateByPattern(Date date, String dateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String formatTimeStr = null;
        if (date != null) {
            formatTimeStr = sdf.format(date);
        }
        return formatTimeStr;
    }

    /***
     * convert Date to cron ,eg. "21 25 17 07 01 ?"
     *
     * ##param date : 时间点
     * @return
     */
    public static String getCron(java.util.Date date) {
        String dateFormat = "ss mm HH dd MM ?";
        return formatDateByPattern(date, dateFormat);
    }


    public static Date getNowDate(String time) throws ParseException {
           SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return formatter.parse(time);
         }


}
