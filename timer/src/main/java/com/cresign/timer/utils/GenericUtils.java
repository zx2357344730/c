package com.cresign.timer.utils;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.common.Constants;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.LogFlow;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ##author: tangzejin
 * ##updated: 2019/6/27
 * ##version: 1.0.0
 * ##description: 通用工具类
 */
@Service
@Slf4j
public class GenericUtils {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private MongoTemplate mongoTemplate;

    //汇率
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static String userAgent =  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";




    // 平均半径,单位：m；不是赤道半径。赤道为6378左右
    private static final double EARTH_RADIUS = 6371393;

    /**
     * 给时间加上几个小时
     * ##Params: day 当前时间 格式：yyyy-MM-dd HH:mm:ss
     * ##Params: hour 需要加的时间
     * ##return: 增加后的结果
     */
    public static String dateAdd(String day, int hour){
        SimpleDateFormat format = new SimpleDateFormat(DateEnum.DATE_TWO.getDate());
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

    /**
     * 反余弦计算两个经纬度的差
     * ##Params: lat1  精度1
     * ##Params: lng1  纬度1
     * ##Params: lat2  精度2
     * ##Params: lng2  纬度2
     * ##return:  结果米数
     */
    public static double getDistance(Double lat1,Double lng1,Double lat2,Double lng2) {
        // 经纬度（角度）转弧度。弧度用作参数，以调用Math.cos和Math.sin
        // A经弧度
        double radiansAx = Math.toRadians(lng1);
        // A纬弧度
        double radiansAy = Math.toRadians(lat1);
        // B经弧度
        double radiansBx = Math.toRadians(lng2);
        // B纬弧度
        double radiansBy = Math.toRadians(lat2);

        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAy) * Math.cos(radiansBy) * Math.cos(radiansAx - radiansBx)
                + Math.sin(radiansAy) * Math.sin(radiansBy);
        // 反余弦值
        double acos = Math.acos(cos);
        // 最终结果
        return EARTH_RADIUS * acos;
    }

    /**
     * 用于去掉s里面小数点后面不需要的0
     * ##Params: s 数字字符串
     * ##return: 结果字符串
     */
    public static String getRemoveBehindZero(String s) {
        BigDecimal value = new BigDecimal(s);
        BigDecimal noZeros = value.stripTrailingZeros();
        return noZeros.toPlainString();
    }

    /**
     * 根据i获取星期
     * ##Params: i 数字星期
     * ##return: 中文星期
     */
    public static String getWeekAndChinese(int i) {
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
                log.debug("错误");
                return null;
        }
    }

    /**
     * String转double类型
     * ##Params: s String数据
     * ##return: 结果
     */
    public static double getDouble(String s) {
        return Double.parseDouble(s);
    }

    /**
     * 将数字补零,只限用于时间
     * ##Params: b 需要补零的数字
     * ##return: 补零结果
     */
    public static String getBl(int b) {
        if (b > Constants.INT_NINE) {
            return b + Constants.STRING_EMPTY;
        } else {
            return Constants.STRING_NUMBER_ZERO + b;
        }
    }

    /**
     * 获取0到shu的随机数
     * ##Params: shu   随机数的最大值
     * ##return:  之间的随机数
     */
    public static int getMathRandom(int shu){
        return (int)(Math.random()*shu);
    }

    /**
     * 将date转换为数字减去shu
     *
     * ##Params: date 时间字符串
     * ##Params: shu  需要减去的数
     * ##return: 结果字符串
     */
    public static String splitDateString(String date, int shu, int addOrReduce) {
        String[] split = date.split(Constants.COLON);
        int h = Integer.parseInt(split[Constants.INT_ZERO]);
        int m = Integer.parseInt(split[Constants.INT_ONE]);
        StringBuilder sb = new StringBuilder();
        if (shu < Constants.INT_ZERO) {
            shu = Math.abs(shu);
        }
        if (addOrReduce == Constants.INT_ZERO) {
            if (m >= shu) {
                sb.append(getBl(h));
                sb.append(Constants.COLON);
                sb.append(getBl((m - shu)));
            } else {
                sb.append(getBl((h - Constants.INT_ONE)));
                sb.append(Constants.COLON);
                sb.append(getBl(((m + Constants.DATE_SIXTY) - shu)));
            }
        } else {
            int l = m + shu;
            if (l >= Constants.DATE_SIXTY) {
                sb.append(getBl((h + Constants.INT_ONE)));
                sb.append(Constants.COLON);
                sb.append(getBl((l - Constants.DATE_SIXTY)));
            } else {
                sb.append(getBl(h));
                sb.append(Constants.COLON);
                sb.append(getBl(l));
            }
        }
        sb.append(Constants.COLON);
        sb.append(split[Constants.INT_TWO]);
        return sb.toString();
    }

    private static int getIndex(String[] s) {
        if (s.length == Constants.INT_TWO) {
            return 1;
        } else if (s.length == Constants.INT_THREE) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 判断date1和date2的大小
     * ##Params: date1 第一个时间
     * ##Params: date2 第二个时间
     * ##return: 判断结果
     */
    public static boolean judgeTimeSize(String date, String date1, String date2, String date3, int is) {

        // 比较 年 月 日
        //创建日期转换对象：年月日 时分秒
        SimpleDateFormat sdf = new SimpleDateFormat(date);
        String[] s = date1.split(Constants.STRING_BLANK_SPACE);
        int qu = getIndex(s);
        boolean flag = false;
        try {
            //转换为 date 类型 Debug：Sun Nov 11 11:11:11 CST 2018
            Date dateD = sdf.parse(s[qu]);
            Date dateD2 = sdf.parse(date2);
            Date dateD3 = null;
            if (null != date3) {
                dateD3 = sdf.parse(date3);
            }
            if (is == Constants.INT_ZERO) {
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
            // TODO Auto-generated catch block
            log.debug("转换出现错误:" + e1.getMessage());
        }
        return flag;
    }

    /**
     * 获取v的负数
     * ##Params: v 正数值
     * ##return: v的负数
     */
    public static int getNegative(int v) {
        return v - (v * Constants.INT_TWO);
    }

    /**
     * 获取d保留两位小数并四舍五入
     * ##Params: d 数值
     * ##return: 结果
     */
    public static double getDoubleByDigitAndRounding(double d) {
        DecimalFormat df = new DecimalFormat(Constants.DIGIT_ROUNDING);
        return Double.parseDouble(df.format(d));
    }

    /**
     * 获取f保留position位小数并四舍五入
     * ##Params: f        数值
     * ##Params: position 小数位
     * ##return: 结果
     */
    public static float getFloatByDigitAndRounding(float f, int position) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(position, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 某一年某个月的每一天
     */
    public static List<String> getMonthFullDay(int year, int month, int day) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_ONE.getDate());
        List<String> fullDayList = new ArrayList<>();
        if (day <= Constants.INT_ZERO) {
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
            if (sdf.format(cal.getTime()).equals(getLastDay(year, month, sdf))) {
                break;
            }
            cal.add(Calendar.DAY_OF_MONTH, j == Constants.INT_ZERO ? +Constants.INT_ZERO : +Constants.INT_ONE);
            j++;
            fullDayList.add(sdf.format(cal.getTime()));
        }
        return fullDayList;
    }

    private static synchronized String getLastDay(int year, int month, SimpleDateFormat sdf) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, Constants.INT_ZERO);
        return sdf.format(cal.getTime());
    }

    /**
     * 根据type获取格式化日期
     * ##Params: type 日志格式
     * ##return: 格式化结果
     */
    public static String getDateByT(String type) {

        // 创建时间格式化对象
        SimpleDateFormat formatter = new SimpleDateFormat(type);

        // 返回格式化结果
        return formatter.format(new Date());
    }

    /**
     * 判断字符串是否为空，是返回true
     * ##Params: name 判断的字符串
     */
    public static boolean stringIsNull(String name) {
        return null == name || Constants.STRING_EMPTY.equals(name) || name.length() == Constants.INT_ZERO;
    }

    /**
     * ..
     *
     * ##Params: obj 参数
     * ##return: 结果
     */
    public static Integer objToInteger(Object obj) {
        if (obj != null) {
            return Integer.parseInt(obj.toString());
        }
        return 0;
    }

    /**
     * 根据date获取date是属于星期几
     * ##Params: date 时间
     * ##return: 星期几
     */
    public static int getWeek2(Date date) {
        int[] weeks = {7, 1, 2, 3, 4, 5, 6};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekIndex = cal.get(Calendar.DAY_OF_WEEK) - Constants.INT_ONE;
        if (weekIndex < Constants.INT_ZERO) {
            weekIndex = 0;
        }
        return weeks[weekIndex];
    }

    /**
     * 获取过去第几天的日期
     *
     * ##Params: past 指定前几天
     * ##return: 结果
     */
    public static String getPastDate(int past,String date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(date);
        return format.format(today);
    }

    /**
     * 转换endDate，nowDate为日期，然后使用endDate减去nowDate计算两个日期之间的差
     *
     * ##Params: endDate 减数日期
     * ##Params: nowDate 被减数日期
     * ##return: Map对象，day代表差的天数，hour代表差的小时，min代表差的分钟，sec代表差的秒数
     */
    public static Map<String, Long> getDatePoor(String endDate, String nowDate) {

//        //创建日期格式化对象
//        SimpleDateFormat sDateFormat=new SimpleDateFormat(DateEnum.DATE_TWO.getDate()); //加上时间
//            // 计算差多少天
//            long day = diff / nd;

//            //存储差的天数
//            map.put("day",day);

        //加上时间
        SimpleDateFormat sDateFormat = new SimpleDateFormat(DateEnum.DATE_H_M_S.getDate());

        String[] str1 = endDate.split(Constants.STRING_BLANK_SPACE);
        String[] str2 = nowDate.split(Constants.STRING_BLANK_SPACE);

        //定义减数日期
        Date endDateZ = null;

        //定义被减数日期
        Date nowDateZ = null;

        int qu1 = getIndex(str1);

        int qu2 = getIndex(str2);

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

    private static void sortIs(int is, List<LogFlow> list, String dateType) {
        //重写原list的排序方法
        list.sort((o1, o2) -> {

            //创建日期格式化对象，并添加格式化日期的格式
            SimpleDateFormat format = new SimpleDateFormat(dateType);

            //捕捉异常
            try {

                //将第一个对象的tmd字段转换成日期
                Date dt1 = format.parse(o1.getTmd());

                //将第二个对象的tmd字段转换成日期
                Date dt2 = format.parse(o2.getTmd());

                return listSortResult(is, dt1, dt2);
            } catch (Exception e /*捕捉所有异常*/) {

                //抛出异常信息
                log.debug("出现错误：" + e.getMessage());
            }

            //返回结果
            return 0;
        });

    }

    /**
     * 对dt1和dt2进行判断并返回值
     *
     * ##Params: is  需要的返回结果：1是降序，2是升序
     * ##Params: dt1 比较值1
     * ##Params: dt2 比较值2
     * ##return: 结果
     */
    public static int listSortResult(int is, Date dt1, Date dt2) {
        if (is == Constants.INT_ONE) {
            //判断第一个时间戳小于第二个时间戳
            if (dt1.getTime() < dt2.getTime()) {

                //返回结果
                return 1;

                //判断第一个时间戳大于第二个时间戳
            } else if (dt1.getTime() > dt2.getTime()) {

                //返回结果
                return -1;
            }
        } else {
            //判断第一个时间戳小于第二个时间戳
            if (dt1.getTime() > dt2.getTime()) {

                //返回结果
                return 1;

                //判断第一个时间戳大于第二个时间戳
            } else if (dt1.getTime() < dt2.getTime()) {

                //返回结果
                return -1;
            }
        }
        return 0;
    }

    /**
     * 对list集合进行排序
     *
     * ##Params: list 集合
     */
    public static void listSort2(List<LogFlow> list) {
        sortIs(Constants.INT_TWO, list, DateEnum.DATE_TWO.getDate());
    }

    /**
     * 将date从typeZ格式转成typeResult格式
     * ##Params: date          时间
     * ##Params: typeZ         时间格式
     * ##Params: typeResult    结果格式
     * ##return:  结果
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
     *补全给定起止时间区间内的所有日期
     * ##Params: startTime
     * ##Params: endTime
     * ##Params: isIncludeStartTime
     * ##return:
     * Jevon
     */
    public static List<String> getBetweenDates(String startTime, String endTime,boolean isIncludeStartTime){
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
     * 最近一周的所有日期
     * ##return:
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
        return getBetweenDates(day,today,false);
    }

    /**
     *将字符串格式yyyy/MM/dd的字符串转为日期，格式"yyyy-MM"
     * ##Params: date 日期字符串
     * ##return: 返回格式化的日期
     * @throws ParseException 分析时意外地出现了错误异常
     * Jevon
     */
    public static String strOndToDateFormat(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        formatter.setLenient(false);
        Date newDate= formatter.parse(date);
        formatter = new SimpleDateFormat("yyyy-MM");
        return formatter.format(newDate);
    }

    /**
     * 获取文件大小
     * ##author: Jevon
     * ##Params: size
     * ##version: 1.0
     * ##updated: 2020/9/22 9:50
     * ##Return: java.lang.String
     */
    public static String getNetFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    /**
     * 转换
     * ##author: Jevon
     * ##Params: strUrl 请求地址
     * ##Params: params 请求参数
     * ##Params: method 请求方法
     * ##return:  网络请求字符串
     * ##exception:
     */
    public static String net(String strUrl, Map params,String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if(method==null || method.equals("GET")){
                strUrl = strUrl+"?"+urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                    out.writeBytes(urlencode(params));
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    /**
     * 将map型转为请求参数型
     * ##author: Jevon
     * ##Params: data
     * ##version: 1.0
     * ##updated: 2020/11/16 22:23
     * ##Return: java.lang.String
     */
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 新增ES日志
     * ##author: Jevon
     * ##Params: infoObject
     * ##version: 1.0
     * ##updated: 2020/10/26 8:30
     * ##Return: void
     */
    public  void addES(JSONObject infoObject , String indexes ) throws IOException {

        //指定ES索引
        IndexRequest request = new IndexRequest(indexes);
        //ES列表
        request.source(infoObject, XContentType.JSON);

        restHighLevelClient.index(request, RequestOptions.DEFAULT);

    }

    public boolean isSameMonth (String tmd) throws ParseException {

        //转换时间格式（String转Date）
        SimpleDateFormat df = new SimpleDateFormat(DateEnum.DATE_TWO.getDate());
        //数据库时间
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(df.parse(tmd));
        //当前时间
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date());
        //判断两个月份时间是否一致
        return cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }



}