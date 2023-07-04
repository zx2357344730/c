package com.cresign.tools.dbTools;

import com.cresign.tools.common.Constants;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class DateUtils {


    /**
     * 根据type获取格式化日期
     * @param type 日志格式
     * @return 格式化结果
     */
    public static String getDateNow(String type) {

        // 创建时间格式化对象
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(type);

        // 返回格式化结果
        return LocalDateTime.now().format(formatter);
    }

    public static String getDateNowAddSecond(String type,String dateStr,int second){
        SimpleDateFormat df = new SimpleDateFormat(type);
        Date date;
        try {
            date = df.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
//        System.out.println(df.format(date));
        date.setTime(date.getTime() + (second * 1000L));
//        System.out.println(df.format(date));
        return df.format(date);
    }

    public static long getTimeStamp() {

        try {
            String time = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = sdf.parse(time);

            return date.getTime();

        } catch (Exception e)
        {
            e.printStackTrace();
            return 0L;
        }
    }


    /**
     * 获取两个日期相差的月数
     */
    public static int getMonthDiff(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        int year1 = c1.get(Calendar.YEAR);
        int year2 = c2.get(Calendar.YEAR);
        int month1 = c1.get(Calendar.MONTH);
        int month2 = c2.get(Calendar.MONTH);
        int day1 = c1.get(Calendar.DAY_OF_MONTH);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        // 获取年的差值 
        int yearInterval = year1 - year2;
        // 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
        if (month1 < month2 || month1 == month2 && day1 < day2) {
            yearInterval--;
        }
        // 获取月数差值
        int monthInterval = (month1 + 12) - month2;
        if (day1 < day2) {
            monthInterval--;
        }
        monthInterval %= 12;
        int monthsDiff = Math.abs(yearInterval * 12 + monthInterval);
        return monthsDiff;
    }

    /**
     * 给时间加上几个小时
     * @param day	当前时间 格式：yyyy-MM-dd HH:mm:ss
     * @param hour	需要加的时间
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:18
     */
    public static String dateAdd(String day, int hour){
        SimpleDateFormat format = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        Date date = null;
        try {
            date = format.parse(day);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (date == null)
            return "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hour);// 24小时制
        date = cal.getTime();
        return format.format(date);

    }
    public static String strOndToDateFormat(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        formatter.setLenient(false);
        Date newDate= formatter.parse(date);
        formatter = new SimpleDateFormat("yyyy-MM");
        return formatter.format(newDate);
    }
    /**
     * 判断date1和date2的大小
     * @param date	时间格式
     * @param date1	第一个时间
     * @param date2	第二个时间
     * @param date3	第三个个时间
     * @param is	判断方式(0 = 判断是否等于,1 = 大于等于,
     *              2 = 判断第一个时间大于等于第二个并且小于等于第三个,
     *              10 = 判断小于,11 = 判断第一个小于第二个或者大于第三个,
     *              否则 = 小于等于)
     * @return boolean  返回结果: 判断结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:21
     */
    public static boolean judgeTimeSize(String date, String date1, String date2, String date3, int is) {

        // 比较 年 月 日
        //创建日期转换对象：年月日 时分秒
        SimpleDateFormat sdf = new SimpleDateFormat(date);
        String[] s = date1.split(Constants.STRING_BLANK_SPACE);
        int qu = Ut.getLength(s);
        boolean flag = false;
        try {
            //转换为 date 类型 Debug：Sun Nov 11 11:11:11 CST 2018
            Date dateD = sdf.parse(s[qu]);
            Date dateD2 = sdf.parse(date2);
            Date dateD3 = null;
            if (null != date3) {
                dateD3 = sdf.parse(date3);
            }
            if (is == 0) {
                flag = dateD.getTime() == dateD2.getTime();
            } else if (is == Constants.INT_ONE) {
                flag = dateD.getTime() >= dateD2.getTime();
            } else if (is == Constants.INT_TWO) {
                if (null != dateD3) {
                    flag = dateD.getTime() >= dateD2.getTime() && dateD.getTime() <= dateD3.getTime();
                }
            } else if (is == Constants.INT_TEN) {
                flag = dateD.getTime() < dateD2.getTime();
            } else if (is == Constants.INT_ELEVEN) {
                if (null != dateD3) {
                    flag = dateD.getTime() < dateD2.getTime() || dateD.getTime() > dateD3.getTime();
                }
            } else {
                flag = dateD.getTime() <= dateD2.getTime();
            }
        } catch (ParseException e1) {
        }
        return flag;
    }


    /**
     * 某一年某个月的每一天
     * @param year	年份
     * @param month	月份
     * @param day	天
     * @return java.util.List<java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:26
     */
    public static List<String> getMonthFullDay(int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_ONLY.getDate());
        List<String> fullDayList = new ArrayList<>();
        if (day <= 0) {
            day = 1;
        }
        // 获得当前日期对象
        Calendar cal = Calendar.getInstance();
        cal.clear();// 清除信息
        cal.set(Calendar.YEAR, year);
        // 1月从0开始
        cal.set(Calendar.MONTH, month - Constants.INT_ONE);
        // 设置为1号,当前日期既为本月第一天
        cal.set(Calendar.DAY_OF_MONTH, day);
        int count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int j = 0; j <= (count - Constants.INT_ONE); ) {
            if (sdf.format(cal.getTime()).equals(DateUtils.getLastDay(year, month, sdf))) {
                break;
            }
            cal.add(Calendar.DAY_OF_MONTH, j == 0 ? +0 : +Constants.INT_ONE);
            j++;
            fullDayList.add(sdf.format(cal.getTime()));
        }
        return fullDayList;
    }

    /**
     * 将date转换为数字减去shu
     * @param date	时间字符串
     * @param shu	需要减去的数
     * @param addOrReduce	判断条件
     * @return java.lang.String  返回结果: 结果字符串
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:20
     */
    public static String splitDateString(String date, int shu, int addOrReduce) {
        String[] split = date.split(":");
        int h = Integer.parseInt(split[0]);
        int m = Integer.parseInt(split[Constants.INT_ONE]);
        StringBuilder sb = new StringBuilder();
        if (shu < 0) {
            shu = Math.abs(shu);
        }
        if (addOrReduce == 0) {
            if (m >= shu) {
                sb.append(Ut.addZero(h));
                sb.append(":");
                sb.append(Ut.addZero((m - shu)));
            } else {
                sb.append(Ut.addZero((h - Constants.INT_ONE)));
                sb.append(":");
                sb.append(Ut.addZero(((m + Constants.DATE_SIXTY) - shu)));
            }
        } else {
            int l = m + shu;
            if (l >= Constants.DATE_SIXTY) {
                sb.append(Ut.addZero((h + Constants.INT_ONE)));
                sb.append(":");
                sb.append(Ut.addZero((l - Constants.DATE_SIXTY)));
            } else {
                sb.append(Ut.addZero(h));
                sb.append(":");
                sb.append(Ut.addZero(l));
            }
        }
        sb.append(":");
        sb.append(split[Constants.INT_TWO]);
        return sb.toString();
    }

    public static String getPastDate(int past,String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(date);
        return format.format(today);
    }
    /**
     * 获取两个日期之间的所有日期
     * @param startTime	开始日期
     * @param endTime	结束日期
     * @return java.util.List<java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:26
     */

    public static List<String> getDays(String startTime, String endTime) {

        // 返回的日期集合
        List<String> days = new ArrayList<>();

        DateFormat dateFormat = new SimpleDateFormat(DateEnum.DATE_ONLY.getDate());
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    /**
     * 获取指定year、month通过sdf格式化成字符串后的日期
     * @param year	年
     * @param month	月
     * @param sdf	格式化对象
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:26
     */
    public static synchronized String getLastDay(int year, int month, SimpleDateFormat sdf) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 0);
        return sdf.format(cal.getTime());
    }


    //获取当前时区
    public static String getTimeZone(){
        Calendar cal = Calendar.getInstance();
        int offset = cal.get(Calendar.ZONE_OFFSET);
        cal.add(Calendar.MILLISECOND, -offset);
        Long timeStampUTC = cal.getTimeInMillis();
        Long timeStamp = System.currentTimeMillis();
        Long timeZone = (timeStamp - timeStampUTC) / (1000 * 3600);
        return String.valueOf(timeZone);

    }

    //当前时间加上月数，返回新的日期 Jeovn
    public static String getEndTime(String time,int day) throws ParseException {
        // 定义calendar对象
        Calendar calendar = new GregorianCalendar();
        //定义日期格式
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
        // 把时间赋值给calendar
        calendar.setTime(DateUtils.strTurnDate(time));
        // 在日期中增加月数
        calendar.add(calendar.MONTH, day);
        // 把calendar转换回日期格式
        Date date = calendar.getTime();
        //日期格式化
        String dateStr=sdf.format(date);
        return dateStr;
    }

    //String时间类型转Date类型   Jevon
    public static Date strTurnDate(String time) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date utilDate = sdf.parse(time);
        //(utilDate);//查看utilDate的值
        Date date = new java.sql.Date(utilDate.getTime());
        //(date);//查看date的值
        return date;
    }


    public static String getSpecialDayAdd(String date, int count) {
        SimpleDateFormat d = new SimpleDateFormat("yyyy/MM/dd");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(d.parse(date));
            c.add(Calendar.DAY_OF_MONTH, count);
            String t = d.format(c.getTime());
            return t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *补全给定起止时间区间内的所有日期
     * @param startTime
     * @param endTime
     * @param isIncludeStartTime
     * @return
     * Jevon
     */
    public static List<String> getBetweenDates(String startTime, String endTime, boolean isIncludeStartTime){
        List<String> result = new ArrayList<>();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            Date d1 = new SimpleDateFormat("yyyy/MM/dd").parse(startTime);//定义起始日期
            Date d2 = new SimpleDateFormat("yyyy/MM/dd").parse(endTime);//定义结束日期  可以去当前月也可以手动写日期。
            Calendar dd = Calendar.getInstance();//定义日期实例
            dd.setTime(d1);//设置日期起始时间
            if(isIncludeStartTime) {
                result.add(format.format(d1));
            }
            while (dd.getTime().before(d2)) {//判断是否到结束日期
                dd.add(Calendar.DATE, 1);//进行当前日期加1
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                String str = sdf.format(dd.getTime());
                result.add(str);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     *##description:      对比两个日期的天数
     *@param
     *@return
     *@author           JackSon
     *@updated             2020/4/20 21:51
     */
    public static int nDaysBetweenTwoDate(String firstString, String secondString) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        Date firstDate = null;
        Date secondDate = null;
        try {
            firstDate = df.parse(firstString);
            secondDate = df.parse(secondString);
        } catch (Exception e) {
            // 日期型字符串格式错误

        }
        int nDay = (int) ((secondDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000));
        return nDay;
    }


    /**
     * 最近一周的所有日期
     * @return
     * Jevon
     */
    public static List<String> getNearlyWeekDates() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        Calendar c = Calendar.getInstance();
        //过去七天
        c.setTime(new Date());
        String today = format.format(new Date());
        c.add(Calendar.DATE, - 7);
        Date d = c.getTime();
        String day = format.format(d);
        List<String> result = getBetweenDates(day,today,false);
        return result;
    }

    /**
     * 获取过去或者未来 任意天内的日期数组
     *
     * @param intervals intervals天内
     * @return 日期数组
     * Jevon
     */
    public static ArrayList<String> timeArray(int intervals) {
        ArrayList<String> pastDaysList = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            pastDaysList.add(getPastDate(i));
        }
        return pastDaysList;
    }

    /**
     * 转换endDate，nowDate为日期，然后使用endDate减去nowDate计算两个日期之间的差
     * @param endDate	减数日期
     * @param nowDate	被减数日期
     * @return java.util.Map<java.lang.String,java.lang.Long>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:31
     */
    public static Map<String, Long> getDatePoor(String endDate, String nowDate) {

//        //创建日期格式化对象
//        SimpleDateFormat sDateFormat=new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate()); //加上时间
//            // 计算差多少天
//            long day = diff / nd;

//            //存储差的天数
//            map.put("day",day);

        //加上时间
        SimpleDateFormat sDateFormat = new SimpleDateFormat(DateEnum.TIME_ONLY.getDate());

        String[] str1 = endDate.split(Constants.STRING_BLANK_SPACE);
        String[] str2 = nowDate.split(Constants.STRING_BLANK_SPACE);

        //定义减数日期
        Date endDateZ = null;

        //定义被减数日期
        Date nowDateZ = null;

        int qu1 = Ut.getLength(str1);

        int qu2 = Ut.getLength(str2);

        //必须捕获异常
        try {

            //转换endDate为日期对象
            endDateZ = sDateFormat.parse(str1[qu1]);

            //转换nowDate为日期对象
            nowDateZ = sDateFormat.parse(str2[qu2]);

        } catch (ParseException px) {

            //抛出异常信息
            px.printStackTrace();
        }

        //判断减数日期和被减数日期是否为空
        if (endDateZ != null && nowDateZ != null) {
            //不为空则继续处理

            //定义计算天数数值
            long nd = Constants.DATE_ONE_THOUSAND * Constants.DATE_TWENTY_FOUR * Constants.DATE_SIXTY * Constants.DATE_SIXTY;

            //定义计算小时数值
            long nh = Constants.DATE_ONE_THOUSAND * Constants.DATE_SIXTY * Constants.DATE_SIXTY;

            //定义计算分钟数值
            long nm = Constants.DATE_ONE_THOUSAND * Constants.DATE_SIXTY;

            //定义计算秒数值
            long ns = Constants.DATE_ONE_THOUSAND;

            // 获得减数日期和被减数日期的毫秒时间差异
            long diff = endDateZ.getTime() - nowDateZ.getTime();

            // 计算差多少小时
            long hour = diff % nd / nh;

            // 计算差多少分钟
            long min = diff % nd % nh / nm;

            // 计算差多少秒
            long sec = diff % nd % nh % nm / ns;

            //创建map存储计算结果
            Map<String, Long> map = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

            //存储差的小时
            map.put(Constants.ADD_HOUR, hour);

            //存储差的分钟
            map.put(Constants.ADD_MIN, min);

            //存储差的秒数
            map.put(Constants.ADD_SEC, sec);

            //返回map结果
            return map;
        }

        //为空则返回null
        return null;
    }

    /**
     * 根据date获取date是属于星期几
     * @param date	时间
     * @return int  返回结果: 星期几
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:29
     */
    public static int getWeek2(Date date) {
        int[] weeks = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - Constants.INT_ONE;
        if (weekIndex < 0) {
            weekIndex = 0;
        }
        return weeks[weekIndex];
    }

    /**
     * 获取过去第几天的日期
     * @param past	指定前几天
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:29
     */
    public static String getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(DateEnum.DATE_ONLY.getDate());
        return format.format(today);
    }

    /**
     * 获取过去第几个月的日期
     * @param past	指定前几个月
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:30
     */
    public static String getPastDateMonth(int past) {
        //当前时间
        Date dNow = new Date();
        Date dBefore;
        //得到日历
        Calendar calendar = Calendar.getInstance();
        //把当前时间赋给日历
        calendar.setTime(dNow);
        //设置为前3月
        calendar.add(Calendar.MONTH, -past);
        //得到前3月的时间
        dBefore = calendar.getTime();
        //设置时间格式
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_ONLY.getDate());
        return sdf.format(dBefore);
    }

    /**
     * 获取未来 第 past 天的日期
     * @param past	指定后几天
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:30
     */
    public static String getFetureDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(DateEnum.DATE_ONLY.getDate());
        return format.format(today);
    }

    /**
     * date2比date1多的天数
     * @param str1	第一个日期
     * @param str2	第二个日期
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:30
     */
    public static int differentDays(String str1,String str2) {
        SimpleDateFormat sdf=new SimpleDateFormat(DateEnum.DATE_ONLY.getDate());
        System.out.println("yr"+str1);

        System.out.println("yr"+str2);

        try {
            Calendar cal1 = Calendar.getInstance();
            System.out.println("yr"+str2);

            cal1.setTime(sdf.parse(str1));
            System.out.println("yr"+str2);


            Calendar cal2 = Calendar.getInstance();
            System.out.println("yr"+str2);

            cal2.setTime(sdf.parse(str2));
            System.out.println("yr"+str2);

            int day1= cal1.get(Calendar.DAY_OF_YEAR);
            int day2 = cal2.get(Calendar.DAY_OF_YEAR);
            System.out.println("yr"+str2);


            int year1 = cal1.get(Calendar.YEAR);
            int year2 = cal2.get(Calendar.YEAR);
            System.out.println("yr"+year1+year2);
            if(year1 != year2)   //同一年
            {
                int timeDistance = 0 ;
                for(int i = year1 ; i < year2 ; i ++)
                {
                    if(i%4==0 && i%100!=0 || i%400==0)    //闰年
                    {
                        timeDistance += 366;
                    }
                    else    //不是闰年
                    {
                        timeDistance += 365;
                    }
                }

                return timeDistance + (day2-day1) ;
            }
            else    //不同年
            {
//            ("判断day2 - day1 : " + (day2-day1));
                return day2-day1;
            }
        } catch (Exception e)
        {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.BAD_REQUEST.getCode(), null);
        }

    }


    /**
     * 时间转换将2020/02/21 07:53:51 120  转成  07:53
     *
     * @param date 时间
     * @return 结果
     * Jevon
     */
    public static String getStringDate1(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        Date dates = null;
        try {
            dates = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(dates);
    }

    /**
     * 将date从typeZ格式转成typeResult格式
     * @param date	时间
     * @param typeZ	时间格式
     * @param typeResult	结果格式
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:33
     */
    public static String getStringDate2(String date,String typeZ,String typeResult) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(typeZ);
        Date dates = null;
        try {
            dates = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(typeResult);
        return formatter.format(dates);
    }

    /**
     * 根据i获取星期
     * @param i	数字星期
     * @return java.lang.String  返回结果: 中文星期
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:19
     */
    public static String getWeekInChinese(int i) {
        switch (i) {
            case 1:
                return "星期一";
            case 2:
                return "星期二";
            case 3:
                return "星期三";
            case 4:
                return "星期四";
            case 5:
                return "星期五";
            case 6:
                return "星期六";
            case 7:
                return "星期天";
            default:
//                log.debug("错误");
                return null;
        }
    }



}
