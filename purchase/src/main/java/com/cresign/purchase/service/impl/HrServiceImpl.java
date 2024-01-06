package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.HrService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ut;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName HrServiceImpl
 * @Date 2023/11/30
 * @ver 1.0.0
 */
@Service
public class HrServiceImpl implements HrService {

    @Autowired
    private Qt qt;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Ws ws;

    /**
     * 根据统计日期统计打卡时间
     * @param id_C  公司编号
     * @param sumDates  统计日期集合
     * @param chkInMode 打卡模式
     * @param isAllSpecialTime 是否全部特殊时间
     * @param isAutoCardReplacement 是否自动系统补卡
     * @param isSumSpecialTime 是否统计特殊时间
     * @return  返回结果
     */
    @Override
    public ApiResponse statisticsChKin(String id_C, JSONArray id_Us, JSONArray sumDates, int chkInMode
            , boolean isAllSpecialTime, boolean isAutoCardReplacement, boolean isSumSpecialTime) {
        Asset asset = qt.getConfig(id_C, "a-chkin", "chkin");
        if (null == asset || null == asset.getChkin()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        for (int i = 0; i < id_Us.size(); i++) {
            String id_U = id_Us.getString(i);
            JSONArray es = qt.getES("lBUser", qt.setESFilt("id_U", id_U,"id_CB",id_C));
            if (null == es || es.size() == 0) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                        ASSET_NOT_FOUND.getCode(),"");
            }
            JSONObject userLBUserInfo = es.getJSONObject(0);
            JSONObject chkin = asset.getChkin();
            JSONObject objChkin = chkin.getJSONObject("objChkin");
            if (null == objChkin) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                        ASSET_NOT_FOUND.getCode(),"");
            }
            JSONObject depInfo = objChkin.getJSONObject(userLBUserInfo.getString("dep"));
            if (null == depInfo) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                        ASSET_NOT_FOUND.getCode(),"");
            }
            JSONObject grpInfo = depInfo.getJSONObject(userLBUserInfo.getString("grpU"));
            if (null == grpInfo) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                        ASSET_NOT_FOUND.getCode(),"");
            }
            // 获取上下班时间
            JSONArray arrTime = grpInfo.getJSONArray("arrTime");
            // 获取上班前打卡时间范围
            long tPre = grpInfo.getInteger("tPre") * 60;
            // 获取下班后打卡时间范围
            long tPost = grpInfo.getInteger("tPost") * 60;
            // 获取严重迟到时间
            long tLate = grpInfo.getInteger("tLate") * 60;
            // 获取矿工迟到时间
            long tAbsent = grpInfo.getInteger("tAbsent") * 60;
            // 获取正常上班总时间
            long teDur = grpInfo.getInteger("teDur") * 60 *60;
            // 获取考勤类型（0 = 固定班、1 = 自由时间）
            String chkType = grpInfo.getString("chkType");
            // 记录加班时间，自己加一获取
            JSONArray ovt = grpInfo.getJSONArray("ovt");
            JSONObject chkInObj = new JSONObject();
            for (int j = 0; j < sumDates.size(); j++) {
                String thisDate = sumDates.getString(j);
                JSONArray chkInEs = qt.getES("chkin", qt.setESFilt("id_U", id_U,"data.theSameDay",thisDate));
                if (null == chkInEs || chkInEs.size() == 0) {
                    continue;
                }
                chkInObj.put(thisDate,chkInEs);
            }
            // 判断全部是特殊时间处理
            if (chkType.equals("1") || isAllSpecialTime) {
                for (String thisDate : chkInObj.keySet()) {
                    JSONArray chkInEs = chkInObj.getJSONArray(thisDate);
                    // 获取用户的long上班时间
                    JSONArray userDates1 = getUserDates(chkInEs);
                    // 调用全部是特殊时间的处理方法
                    JSONObject testSpecialTime1 = testSpecialTime(userDates1, teDur,thisDate);
                    System.out.println("testSpecialTime1:");
                    System.out.println(JSON.toJSONString(testSpecialTime1));
                }
                return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
            }
            // 判断打卡类型为正常固定班
            if (chkInMode == 0 || (chkInMode == -1 && chkType.equals("0"))) {
                for (String thisDate : chkInObj.keySet()) {
                    // 获取日期格式化后的正常上班时间集合
                    JSONArray normalWorkTime = getArrTime(arrTime, thisDate);
                    JSONArray chkInEs = chkInObj.getJSONArray(thisDate);
                    // 获取用户的long上班时间
                    JSONArray userDates1 = getUserDates(chkInEs);
                    // 调用正常统计用户打卡时间方法
                    JSONObject testChkInSum1 = testChkInSum(normalWorkTime, userDates1, id_U, teDur
                            , isAutoCardReplacement, isSumSpecialTime, ovt, tPre, tPost, tLate, tAbsent,thisDate,chkType,id_C);
                    System.out.println("testChkInSum1:");
                    System.out.println(JSON.toJSONString(testChkInSum1));
                }
                return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
                // 判断打卡类型为首尾固定班
            } else if (chkInMode == 1) {
                for (String thisDate : chkInObj.keySet()) {
                    // 获取日期格式化后的正常上班时间集合
                    JSONArray normalWorkTime = getArrTime(arrTime, thisDate);
                    JSONArray chkInEs = chkInObj.getJSONArray(thisDate);
                    JSONArray userDates1 = getUserDates(chkInEs);
                    // 调用首尾统计用户打卡时间方法
                    JSONObject testChkInSumHeadTail1 = testChkInSumHeadTail(normalWorkTime, userDates1, id_U, teDur
                            , isAutoCardReplacement, isSumSpecialTime, ovt, tPre, tPost, tLate, tAbsent,thisDate,chkType,id_C);
                    System.out.println("testChkInSumHeadTail1:");
                    System.out.println(JSON.toJSONString(testChkInSumHeadTail1));
                }
                return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    /**
     * 按月统计打卡信息
     * @param id_C  公司编号
     * @param id_U  用户编号
     * @return  统计结果
     */
    @Override
    public ApiResponse statisticsChKinMonth(String id_C,String id_U, int year,JSONArray months) {
        JSONObject result = new JSONObject();
        Asset asset = qt.getConfig(id_C, "a-chkin", "chkin");
        if (null == asset || null == asset.getChkin()) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONArray es = qt.getES("lBUser", qt.setESFilt("id_U", id_U,"id_CB",id_C));
        if (null == es || es.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                    ASSET_NOT_FOUND.getCode(),"");
        }
        JSONObject userLBUserInfo = es.getJSONObject(0);
        JSONObject chkin = asset.getChkin();
        JSONObject objChkin = chkin.getJSONObject("objChkin");
        JSONObject depInfo = objChkin.getJSONObject(userLBUserInfo.getString("dep"));
        JSONObject grpInfo = depInfo.getJSONObject(userLBUserInfo.getString("grpU"));
        // 获取默认班日( 0（1到5），1(1到6)，2（大小周），3（按月放假天）)
        String dayType = grpInfo.getString("dayType");
//        // 按月放假天数
//        int dayOff = grpInfo.getInteger("dayOff");
        // 必须打卡日期
        JSONArray dayMust = grpInfo.getJSONArray("dayMust");
        // 无须打卡日期
        JSONArray dayMiss = grpInfo.getJSONArray("dayMiss");
        JSONObject yearChkIn = new JSONObject();
        for (int i = 0; i < months.size(); i++) {
            Integer month = months.getInteger(i);
            JSONArray usageflow = qt.getES("usageflow", qt.setESFilt("id_U", id_U,"id_C",id_C,"subType","dayChkin"
                    ,"year",year,"month",month));
            if (null == usageflow || usageflow.size() == 0) {
                yearChkIn.put(month+"",null);
                continue;
            }
            yearChkIn.put(month+"",usageflow);
        }
        for (int m = 0; m < months.size(); m++) {
            Integer month = months.getInteger(m);
            JSONArray usageflow = yearChkIn.getJSONArray(month + "");
            if (null == usageflow) {
                continue;
            }
            JSONArray flowNew = new JSONArray();
            for (int i = 0; i < usageflow.size(); i++) {
                JSONObject jsonObject = usageflow.getJSONObject(i);
                JSONObject data = jsonObject.getJSONObject("data");
                if (data.getInteger("month").equals(month)
                        && data.getInteger("year").equals(year)) {
                    flowNew.add(jsonObject);
                }
            }
            JSONObject dates = new JSONObject();
            for (int i = 0; i < flowNew.size(); i++) {
                JSONObject jsonObject = flowNew.getJSONObject(i);
                LogFlow logFlow = JSONObject.parseObject(JSON.toJSONString(jsonObject),LogFlow.class);
                JSONObject data = logFlow.getData();
                dates.put(data.getString("theSameDay"),jsonObject.getJSONObject("data").getJSONObject("chkInData"));
            }
            // 调用方法获取指定年和月的一个月所有日期
            JSONArray thisMonth = getThisMonth(year, month);
            // 每天打卡记录存储
            JSONObject userChkInData = new JSONObject();
            // 当月总打卡记录存储
            JSONObject userChkInMonthData = new JSONObject();
            // 所有上班时间
            long totalTimeAll = 0;
            // 所有早退时间
            long earlyTimeAll = 0;
            // 所有早退次数
            int earlySumAll = 0;
            // 所有迟到时间
            long lateTimeAll = 0;
            // 所有迟到次数
            int lateSumAll = 0;
            // 所有缺勤时间
            long absenceTimeAll = 0;
            // 总缺旷工勤数
            long AEMSum = 0;
            // 总缺勤数
            long missSumAll = 0;
            // 所有公司每天天需要上班的总时间
            long ordinaryWorkTimeAll = 0;
            // 所有加班时间
            long overtimeAll = 0;
            // 所有特殊上班时间
            long taExtraAll = 0;
            for (int i = 0; i < thisMonth.size(); i++) {
                // 获取当前for循环日期
                String thisDate = thisMonth.getString(i);
                // 定义一开始，需要打卡
                boolean isMiss = false;
                // 遍历无需打卡日期列表
                for (int j = 0; j < dayMiss.size(); j++) {
                    // 获取当前循环无需打卡日期
                    String missDate = dayMiss.getString(j);
                    // 判断等于当前循环日期
                    if (thisDate.equals(missDate)) {
                        // 设置为，无需打卡
                        isMiss = true;
                        break;
                    }
                }
                // 判断无需打卡
                if (isMiss) {
                    // 开始下一次循环
                    continue;
                }
                // 定义不必须打卡
                boolean isMust = false;
                // 遍历必须打卡日期列表
                for (int j = 0; j < dayMust.size(); j++) {
                    // 获取当前必须打卡日期
                    String mustDate = dayMust.getString(j);
                    // 判断日期等于当前循环日期
                    if (thisDate.equals(mustDate)) {
                        // 设置为必须打卡
                        isMust = true;
                        break;
                    }
                }
                // 定义设置为不打卡
                boolean isContinue = false;
                // 判断是必需打卡
                if (isMust) {
                    // 设置为要打卡
                    isContinue = true;
                } else {
                    // 判断默认班日类型为0
                    if (dayType.equals("0")) {
                        // 获取当前日期的星期
                        int week = dateToWeek(thisDate);
                        // 判断不是星期天和星期六
                        if (week != 0 && week != 6) {
                            // 设置为要打卡
                            isContinue = true;
                        }
                    // 判断默认班日类型为1
                    } else if (dayType.equals("1")) {
                        // 获取当前日期的星期
                        int week = dateToWeek(thisDate);
                        // 判断不是星期天
                        if (week != 0) {
                            // 设置为要打卡
                            isContinue = true;
                        }
                    }
                }
                // 判断要打卡
                if (isContinue) {
                    JSONObject userChkInInfo = dates.getJSONObject(thisDate);
                    JSONObject thisChkInInfo = new JSONObject();
                    if (null == userChkInInfo) {
                        AEMSum++;
                        thisChkInInfo.put("isAEM",true);
                        userChkInData.put(thisDate,thisChkInInfo);
                        continue;
                    }
                    System.out.println("userChkInInfo:"+thisDate);
                    System.out.println(JSON.toJSONString(userChkInInfo));
//                    JSONObject thisChkInInfo = new JSONObject();
                    thisChkInInfo.put("arrTime",userChkInInfo.getJSONArray("arrTime"));
                    long taAll = userChkInInfo.getLong("taAll");
                    totalTimeAll+=taAll;
                    thisChkInInfo.put("taAll",taAll);
                    long taOver = userChkInInfo.getLong("taOver");
                    overtimeAll+=taOver;
                    thisChkInInfo.put("taOver",taOver);
                    long taLate = userChkInInfo.getLong("taLate");
                    lateTimeAll+=taLate;
                    thisChkInInfo.put("taLate",taLate);
                    int taLateSum = userChkInInfo.getInteger("taLateSum");
                    lateSumAll+=taLateSum;
                    thisChkInInfo.put("taLateSum",taLateSum);
                    long taPre = userChkInInfo.getLong("taPre");
                    earlyTimeAll+=taPre;
                    thisChkInInfo.put("taPre",taPre);
                    int taPreSum = userChkInInfo.getInteger("taPreSum");
                    earlySumAll+=taPreSum;
                    thisChkInInfo.put("taPreSum",taPreSum);
                    long taMiss = userChkInInfo.getLong("taMiss");
                    absenceTimeAll+=taMiss;
                    thisChkInInfo.put("taMiss",taMiss);
                    int taMissSum = userChkInInfo.getInteger("taMissSum");
                    missSumAll+=taMissSum;
                    thisChkInInfo.put("taMissSum",taMissSum);
                    long taDur = userChkInInfo.getLong("taDur");
                    ordinaryWorkTimeAll+=taDur;
                    thisChkInInfo.put("taDur",taDur);
                    boolean isAEM = userChkInInfo.getBoolean("isAEM");
                    if (isAEM) {
                        AEMSum++;
                    }
                    thisChkInInfo.put("isAEM",isAEM);
                    if (userChkInInfo.containsKey("taExtra")) {
                        long taExtra = userChkInInfo.getLong("taExtra");
                        taExtraAll+=taExtra;
                        thisChkInInfo.put("taExtra",taExtra);
                    }
                    userChkInData.put(thisDate,thisChkInInfo);
                }
            }
            userChkInMonthData.put("taTimeAll",totalTimeAll);
            userChkInMonthData.put("taPreAll",earlyTimeAll);
            userChkInMonthData.put("taLateAll",lateTimeAll);
            userChkInMonthData.put("taMissAll",absenceTimeAll);
            userChkInMonthData.put("AEMSum",AEMSum);
            userChkInMonthData.put("taDurAll",ordinaryWorkTimeAll);
            userChkInMonthData.put("taOverAll",overtimeAll);
            userChkInMonthData.put("earlySumAll",earlySumAll);
            userChkInMonthData.put("lateSumAll",lateSumAll);
            userChkInMonthData.put("taMissSumAll",missSumAll);
            userChkInMonthData.put("taExtraAll",taExtraAll);
            result.put("userChkInData",userChkInData);
            result.put("userChkInMonthData",userChkInMonthData);
            sendMsg(id_C,id_U,"monthChkin",null,result,null,year,month);
        }
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 获取当前年份和月份
     * @return  [0] = 年份，[1] = 月份
     */
    private static int[] getThisYearAndMonth(){
        LocalDate date = LocalDate.now();
        int year = date.getYear();
        int month = date.getMonthValue();
        return new int[]{year,month};
    }
    /**
     * 获取指定年和月的一个月所有日期
     * @param year  年份
     * @param month 月份
     * @return  一个月的所有日期
     */
    private static JSONArray getThisMonth(int year,int month){
        LocalDate currentDate = LocalDate.of(year,month,1);
        LocalDate firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth());
        JSONArray dates = new JSONArray();
        for (LocalDate date = firstDayOfMonth; date.isBefore(lastDayOfMonth.plusDays(1)); date = date.plusDays(1)) {
            dates.add(date.getYear()+"/"+date.getMonthValue()+"/"+ Ut.addZero(date.getDayOfMonth()));
        }
        return dates;
    }
    /**
     * 日期转星期
     */
    public static int dateToWeek(String datetime) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");
//        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        Date date;
        try {
            date = f.parse(datetime);
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0)
            w = 0;
        return w;
    }

    /**
     * 正常统计用户打卡时间方法
     * @param normalWorkTime   公司规定正常上班时间集合
     * @param userDates 用户实际上班时间集合
     * @param id_U  用户编号
     * @param teDur 公司规定一天需要上班的总时间
     * @param isAutoCardReplacement 是否自动补卡
     * @param isSumSpecialTime  是否统计特殊时间
     * @param ovt   公司规定加班时间段
     * @param tPre  上班前打卡时间范围
     * @param tPost 下班后打卡时间范围
     * @param tLate 严重迟到时间
     * @param tAbsent   矿工迟到时间
     */
    public JSONObject testChkInSum(JSONArray normalWorkTime,JSONArray userDates,String id_U
            ,long teDur,boolean isAutoCardReplacement,boolean isSumSpecialTime,JSONArray ovt
            ,long tPre,long tPost,long tLate,long tAbsent,String theSameDay,String chkType,String id_C){
        // 用户上班时间分段集合
        JSONArray originTimeSegmentList = new JSONArray();
        // 公司上班时间分段集合
        JSONArray normalWorkSegmentTime = new JSONArray();
        // 用户上班时间纠正分段集合
        JSONArray correctTimeSegmentList = new JSONArray();
        // 用户上班时间集合
        JSONArray originTimeList = new JSONArray();
        // 用户上班时间纠正集合
        JSONArray correctTimeList = new JSONArray();
        // 迟到时间
        long lateTime = 0;
        // 迟到次数
        int lateSum = 0;
        // 早退时间
        long earlyTime = 0;
        // 早退次数
        int earlySum = 0;
        for (int i = 0; i < normalWorkTime.size(); i+=2) {
            // 获取规定上班时间
            Long upper = normalWorkTime.getLong(i);
            // 获取规定下班时间
            Long below = normalWorkTime.getLong(i + 1);
            // 定义存储规定分段时间
            JSONArray normalWorkList = new JSONArray();
            // 添加规定上下班时间
            normalWorkList.add(upper);
            normalWorkList.add(below);
            // 添加规定上班分段时间
            normalWorkSegmentTime.add(normalWorkList);
            // 定义存储用户上班纠正分段时间
            JSONArray correctList = new JSONArray();
            // 定义存储用户上班分段时间
            JSONArray originList = new JSONArray();
            // 获取upper和below之间的时间集合
            JSONArray upperBelowBetween = getUpperBelowBetween(upper, below, userDates, tPre, tPost);
            // 判断集合长度为0
            if (upperBelowBetween.size() == 0) {
                // 将空时间添加到正常分段集合
                originTimeSegmentList.add(originList);
                // 将空时间添加到纠正分段集合
                correctTimeSegmentList.add(correctList);
                // 开始下一次循环
                continue;
            }
            // 判断长度大于4
            if (upperBelowBetween.size() > 4) {
                // 获取超出长度
                int reduce = upperBelowBetween.size() - 4;
                // 遍历超出长度
                for (int j = 0; j < reduce; j++) {
                    // 清理超出的上班数据
                    upperBelowBetween.remove(upperBelowBetween.size()-1);
                }
            }
            long[] evenBetweenTime = getEvenBetweenTime(upperBelowBetween, originTimeList, originList
                    , correctTimeList, correctList, upper, below, isAutoCardReplacement
                    , normalWorkList,lateTime,earlyTime,lateSum,earlySum);
            lateTime = evenBetweenTime[0];
            earlyTime = evenBetweenTime[1];
            lateSum = Integer.parseInt(evenBetweenTime[2]+"");
            earlySum = Integer.parseInt(evenBetweenTime[3]+"");
            // 将正常分段时间添加到正常分段集合
            originTimeSegmentList.add(originList);
            // 将纠正分段时间添加到纠正分段集合
            correctTimeSegmentList.add(correctList);
        }
        return getSumChkInResult(id_U,id_C,correctTimeList,originTimeList,correctTimeSegmentList,originTimeSegmentList
                ,normalWorkSegmentTime,ovt,lateTime,earlyTime,tLate,tAbsent,teDur,isSumSpecialTime
                ,normalWorkTime,userDates,tPre,tPost,theSameDay,chkType,lateSum,earlySum);
    }

    /**
     * 首尾统计用户打卡时间方法
     * @param normalWorkTime    公司规定正常上班时间集合
     * @param userDates 用户实际上班时间集合
     * @param id_U  用户编号
     * @param teDur 公司规定一天需要上班的总时间
     * @param isAutoCardReplacement 是否自动补卡
     * @param isSumSpecialTime  是否统计特殊时间
     * @param ovt   公司规定加班时间段
     * @param tPre  上班前打卡时间范围
     * @param tPost 下班后打卡时间范围
     * @param tLate 严重迟到时间
     * @param tAbsent   矿工迟到时间
     */
    public JSONObject testChkInSumHeadTail(JSONArray normalWorkTime,JSONArray userDates,String id_U
            ,long teDur,boolean isAutoCardReplacement,boolean isSumSpecialTime,JSONArray ovt
            ,long tPre,long tPost,long tLate,long tAbsent,String theSameDay,String chkType,String id_C){
        // 用户上班时间分段集合
        JSONArray originTimeSegmentList = new JSONArray();
        // 公司上班时间分段集合
        JSONArray normalWorkSegmentTime = new JSONArray();
        // 用户上班时间纠正分段集合
        JSONArray correctTimeSegmentList = new JSONArray();
        // 用户上班时间集合
        JSONArray originTimeList = new JSONArray();
        // 用户上班时间纠正集合
        JSONArray correctTimeList = new JSONArray();
        // 迟到时间
        long lateTime = 0;
        // 迟到次数
        int lateSum = 0;
        // 早退时间
        long earlyTime = 0;
        // 早退次数
        int earlySum = 0;
        for (int i = 0; i < normalWorkTime.size(); i+=2) {
            // 获取规定上班时间
            Long upper = normalWorkTime.getLong(i);
            // 获取规定下班时间
            Long below = normalWorkTime.getLong(i + 1);
            // 定义存储规定分段时间
            JSONArray normalWorkList = new JSONArray();
            // 添加规定上下班时间
            normalWorkList.add(upper);
            normalWorkList.add(below);
            // 添加规定上班分段时间
            normalWorkSegmentTime.add(normalWorkList);
            // 定义存储用户上班分段时间
            JSONArray originList = new JSONArray();
            // 定义存储用户上班纠正分段时间
            JSONArray correctList = new JSONArray();
            // 获取upper和below之间的时间集合
            JSONArray upperBelowBetween = getUpperBelowBetween(upper, below, userDates, tPre, tPost);
            // 判断集合长度为0
            if (upperBelowBetween.size() == 0) {
                // 将正常分段时间添加到正常分段集合
                originTimeSegmentList.add(originList);
                // 将纠正分段时间添加到纠正分段集合
                correctTimeSegmentList.add(correctList);
                // 开始下一次循环
                continue;
            }
            // 定义存储新的范围时间集合
            JSONArray upperBelowBetweenNew = new JSONArray();
            // 判断旧范围时间长度大于2
            if (upperBelowBetween.size() > 2) {
                // 取旧范围时间的首尾时间
                upperBelowBetweenNew.add(upperBelowBetween.getLong(0));
                upperBelowBetweenNew.add(upperBelowBetween.getLong(upperBelowBetween.size()-1));
            } else {
                // 直接添加范围时间
                upperBelowBetweenNew.addAll(upperBelowBetween);
            }
            long[] evenBetweenTime = getEvenBetweenTime(upperBelowBetweenNew, originTimeList, originList
                    , correctTimeList, correctList, upper, below, isAutoCardReplacement
                    , normalWorkList,lateTime,earlyTime,lateSum,earlySum);
            lateTime = evenBetweenTime[0];
            earlyTime = evenBetweenTime[1];
            lateSum = Integer.parseInt(evenBetweenTime[2]+"");
            earlySum = Integer.parseInt(evenBetweenTime[3]+"");
            // 将正常分段时间添加到正常分段集合
            originTimeSegmentList.add(originList);
            // 将纠正分段时间添加到纠正分段集合
            correctTimeSegmentList.add(correctList);
        }
        return getSumChkInResult(id_U,id_C,correctTimeList,originTimeList,correctTimeSegmentList,originTimeSegmentList
                ,normalWorkSegmentTime,ovt,lateTime,earlyTime,tLate,tAbsent,teDur,isSumSpecialTime
                ,normalWorkTime,userDates,tPre,tPost,theSameDay,chkType,lateSum,earlySum);
    }

    /**
     * 获取打卡的统计结果信息
     * @param id_U  用户编号
     * @param correctTimeList   用户上班时间纠正集合
     * @param originTimeList    用户上班时间集合
     * @param correctTimeSegmentList    用户上班时间纠正分段集合
     * @param originTimeSegmentList 用户上班时间分段集合
     * @param normalWorkSegmentTime 公司上班时间分段集合
     * @param ovt   公司规定加班时间段
     * @param lateTime  迟到时间
     * @param earlyTime 早退时间
     * @param tLate 严重迟到时间
     * @param tAbsent   矿工迟到时间
     * @param teDur 公司规定一天需要上班的总时间
     * @param isSumSpecialTime  是否统计特殊时间
     * @param normalWorkTime    公司规定正常上班时间集合
     * @param userDates 用户实际上班时间集合
     * @param tPre  上班前打卡时间范围
     * @param tPost 下班后打卡时间范围
     */
    public JSONObject getSumChkInResult(String id_U,String id_C,JSONArray correctTimeList,JSONArray originTimeList
            ,JSONArray correctTimeSegmentList
            ,JSONArray originTimeSegmentList,JSONArray normalWorkSegmentTime,JSONArray ovt
            ,long lateTime,long earlyTime,long tLate,long tAbsent,long teDur,boolean isSumSpecialTime
            ,JSONArray normalWorkTime,JSONArray userDates,long tPre,long tPost,String theSameDay
            ,String chkType,int lateSum,int earlySum){
        System.out.println();
        System.out.println("-------------------- "+id_U+" --------------------");
        System.out.println("正常时间纠正 - correctTimeList:");
        System.out.println(JSON.toJSONString(getTimeToStr(correctTimeList)));
        System.out.println("正常时间 - originTimeList:");
        System.out.println(JSON.toJSONString(getTimeToStr(originTimeList)));
        System.out.println("++++++++++++++++++++++++++++++++");
        System.out.println("正常每段时间纠正 - correctTimeSegmentList:");
        getTimeToListStr(correctTimeSegmentList);
        System.out.println("++++++++++++++++++++++++++++++++");
        System.out.println("正常每段时间 - originTimeSegmentList:");
        getTimeToListStr(originTimeSegmentList);
        // 定义存储总上班时间
        long totalTime = 0;
        // 定义存储加班时间
        long overtime = 0;
        // 定义存储缺勤时间
        long absenceTime = 0;
        // 存储缺勤次数
        int absenceSum = 0;
        // 定义存储普通上班时间
        long ordinaryWorkTime;
        // 遍历纠正时间集合
        for (int i = 0; i < correctTimeList.size(); i+=2) {
            // 获取上班时间
            long ownUpper = correctTimeList.getLong(i);
            // 获取下班时间
            long ownBelow = correctTimeList.getLong(i+1);
            // 判断其中一个时间为0
            if (ownUpper == 0 || ownBelow == 0) {
                // 进入下一次循环
                continue;
            }
            // 用下班时间减上班时间得到总上班时间
            totalTime += ownBelow - ownUpper;
        }
        // 遍历规定分段上班时间
        for (int i = 0; i < normalWorkSegmentTime.size(); i++) {
            // 获取对应的规定上班时间段时间集合
            JSONArray normalWork = normalWorkSegmentTime.getJSONArray(i);
            // 获取对应的纠正上班时间段时间集合
            JSONArray correctList = correctTimeSegmentList.getJSONArray(i);
            // 定义存储，是否是缺勤时间
            boolean isAbsence = false;
            // 判断纠正上班时间为空
            if (correctList.size() == 0) {
                // 设置为是缺勤
                isAbsence = true;
            } else if (correctList.size() == 2) {
                // 获取纠正的上班时间
                Long upper = correctList.getLong(0);
                // 获取纠正的下班时间
                Long below = correctList.getLong(1);
                // 判断其中一个时间为0
                if (upper == 0 || below == 0) {
                    // 设置为是缺勤
                    isAbsence = true;
                }
            }
            // 判断是缺勤
            if (isAbsence) {
                // 获取规定上班时间
                Long ownUpper = normalWork.getLong(0);
                // 获取规定下班时间
                Long ownBelow = normalWork.getLong(1);
                // 使用规定下标时间减去规定上班时间得到缺勤时间
                absenceTime += ownBelow - ownUpper;
                absenceSum++;
            }
        }
        // 遍历规定加班时间段
        for (int i = 0; i < ovt.size(); i++) {
            // 获取加班时间段下标
            Integer ovtIndex = ovt.getInteger(i);
            // 获取纠正时间段的对应时间段集合
            JSONArray correctList = correctTimeSegmentList.getJSONArray(ovtIndex/2);
            // 判断纠正时间集合不为空
            if (correctList.size() > 0) {
                // 遍历纠正时间集合
                for (int j = 0; j < correctList.size(); j+=2) {
                    // 获取纠正上班时间
                    Long upper = correctList.getLong(j);
                    // 获取纠正下班时间
                    Long below = correctList.getLong(j + 1);
                    // 判断其中一个时间为0
                    if (upper == 0 || below == 0) {
                        // 进入下一次循环
                        continue;
                    }
                    // 使用下班时间减去上班时间得到加班总时间
                    overtime += below - upper;
                }
            }
        }
        // 使用总上班时间减去加班时间得到普通上班时间
        ordinaryWorkTime = totalTime - overtime;
        // 判断迟到时间大于严重迟到时间
        if (lateTime >= tLate) {
            // 设置为缺勤时间
            absenceTime += lateTime;
            absenceSum++;
        }
        // 判断早退时间大于严重迟到时间
        if (earlyTime >= tLate) {
            // 设置为缺勤时间
            absenceTime += earlyTime;
            absenceSum++;
        }
        // 判断缺勤时间大于矿工迟到时间，为true说明旷工，为false说明正常
        boolean isAbsenteeism = absenceTime >= tAbsent;
        System.out.println("是否旷工 - isAbsenteeism: "+(isAbsenteeism?"+ 是 +":"- 否 -"));
        System.out.println("总上班时间 - totalTime:"+totalTime);
        System.out.println("普通上班时间: - ordinaryWorkTime:"+ordinaryWorkTime);
        System.out.println("迟到时间 - lateTime:"+lateTime);
        System.out.println("早退时间 - earlyTime:"+earlyTime);
        System.out.println("加班时间 - overtime:"+overtime);
        System.out.println("缺勤时间 - absenceTime:"+absenceTime);
        System.out.println("总要求上班时间 - teDur:"+teDur);
        System.out.println("当天时间 - theSameDay:"+theSameDay);
        System.out.println();
        JSONObject result = new JSONObject();
        // 判断是计算特殊时间
        if (isSumSpecialTime) {
            // 定义存储特殊时间
            long specialTime = 0;
            // 获取第一个规定上班时间
            long start = normalWorkTime.getLong(0);
            // 获取最后一个规定上班时间
            long end = normalWorkTime.getLong(normalWorkTime.size()-1);
            // 开始的特殊时间
            JSONArray startSpecialList = new JSONArray();
            // 结束的特殊时间
            JSONArray endSpecialList = new JSONArray();
            // 遍历用户上班时间
            for (int i = 0; i < userDates.size(); i++) {
                long da = userDates.getLong(i);
                // 判断小于最开始的时间加宽容时间
                if (da < (start-tPre)) {
                    // 添加到开始的特殊时间集合
                    startSpecialList.add(da);
                    // 判断大于最后的时间加宽容时间
                } else if (da > (end+tPost)) {
                    // 添加到结束的特殊时间集合
                    endSpecialList.add(da);
                }
            }
            // 判断开始特殊时间集合长度不为偶数
            if (startSpecialList.size() > 0 && startSpecialList.size() % 2 != 0) {
                // 删除最后一个时间
                startSpecialList.remove(startSpecialList.size()-1);
            }
            // 判断结束特殊时间集合长度不为偶数
            if (endSpecialList.size() > 0 && endSpecialList.size() % 2 != 0) {
                // 删除最后一个时间
                endSpecialList.remove(startSpecialList.size()-1);
            }
            // 遍历开始特殊时间集合，获取开始特殊时间
            for (int i = 0; i < startSpecialList.size(); i+=2) {
                Long ownUpper = startSpecialList.getLong(i);
                Long ownBelow = startSpecialList.getLong(i + 1);
                specialTime += ownBelow - ownUpper;
            }
            // 遍历结束特殊时间集合，获取结束特殊时间
            for (int i = 0; i < endSpecialList.size(); i+=2) {
                Long ownUpper = endSpecialList.getLong(i);
                Long ownBelow = endSpecialList.getLong(i + 1);
                specialTime += ownBelow - ownUpper;
            }
            System.out.println("特殊- 开始 -时间 - startSpecialList:");
            System.out.println(JSON.toJSONString(getTimeToStr(startSpecialList)));
            System.out.println("特殊= 结束 =时间 - endSpecialList:");
            System.out.println(JSON.toJSONString(getTimeToStr(endSpecialList)));
            System.out.println("特殊上班时间 - specialTime:"+specialTime);
            // 添加特殊上班时间
            result.put("taExtra",specialTime);
            // 添加特殊上班开始时间集合
            result.put("taPex",startSpecialList);
            // 添加特殊上班结束时间集合
            result.put("taNex",endSpecialList);
        }
//        result.put("correctTimeList",correctTimeList);
        // 添加用户正常上班时间集合
        result.put("arrTime",originTimeList);
//        result.put("correctTimeSegmentList",correctTimeSegmentList);
//        result.put("originTimeSegmentList",originTimeSegmentList);
        // 添加是否旷工
        result.put("isAEM",isAbsenteeism);
        // 添加总上班时间
        result.put("taAll",totalTime);
        // 添加普通上班时间
        result.put("taDur",ordinaryWorkTime);
        // 添加迟到时间
        result.put("taLate",lateTime);
        // 添加迟到次数
        result.put("taLateSum",lateSum);
        // 添加早退时间
        result.put("taPre",earlyTime);
        // 添加早退次数
        result.put("taPreSum",earlySum);
        // 添加加班时间
        result.put("taOver",overtime);
        // 添加缺勤时间
        result.put("taMiss",absenceTime);
        // 添加缺勤次数
        result.put("taMissSum",absenceSum);
        // 添加公司规定一天需要上班的总时间
        result.put("teDur",teDur);
        sendMsg(id_C,id_U,"dayChkin",theSameDay,result,chkType,0,0);
        return result;
    }

    /**
     * 处理范围时间方法
     * @param upperBelowBetween 范围时间集合
     * @param originTimeList    用户上班时间集合
     * @param originList    存储用户上班分段时间
     * @param correctTimeList   用户上班时间纠正集合
     * @param correctList   存储用户上班纠正分段时间
     * @param upper 规定上班时间
     * @param below 规定下班时间
     * @param isAutoCardReplacement 是否自动补卡
     * @param normalWorkList    存储规定分段时间
     * @return  返回迟到和早退时间数组，数组[0] = 迟到、数组[1] = 早退
     */
    public long[] getEvenBetweenTime(JSONArray upperBelowBetween,JSONArray originTimeList
            ,JSONArray originList,JSONArray correctTimeList,JSONArray correctList,Long upper
            ,Long below,boolean isAutoCardReplacement,JSONArray normalWorkList,long lateTime
            ,long earlyTime,int lateSum,int earlySum){
        // 定义存储返回结果时间数组
        long[] resultTime = new long[4];
        // 判断范围时间长度为偶数
        if (upperBelowBetween.size() % 2 == 0) {
            // 添加正常上班时间集合
            originTimeList.addAll(upperBelowBetween);
            // 添加正常上班时间分段集合
            originList.addAll(upperBelowBetween);
            // 判断上班时间小于规定上班时间
            if (upperBelowBetween.getLong(0) <= upper) {
                // 添加规定上班时间到纠正时间集合
                correctTimeList.add(upper);
                // 添加规定上班时间到纠正时间分段集合
                correctList.add(upper);
            } else {
                // 迟到时间累加
                lateTime += upperBelowBetween.getLong(0) - upper;
                lateSum++;
                // 添加正常上班时间到纠正时间集合
                correctTimeList.add(upperBelowBetween.getLong(0));
                // 添加正常上班时间到纠正时间分段集合
                correctList.add(upperBelowBetween.getLong(0));
            }
            // 判断范围时间长度为4
            if (upperBelowBetween.size() == 4) {
                // 直接添加中间时间到纠正上班时间集合
                correctTimeList.add(upperBelowBetween.getLong(1));
                correctTimeList.add(upperBelowBetween.getLong(2));
                correctList.add(upperBelowBetween.getLong(1));
                correctList.add(upperBelowBetween.getLong(2));
            }
            // 判断下班时间大于规定上班时间
            if (upperBelowBetween.getLong(upperBelowBetween.size() - 1) >= below) {
                // 添加规定下班时间到纠正时间集合
                correctTimeList.add(below);
                // 添加规定下班时间到纠正时间分段集合
                correctList.add(below);
            } else {
                // 早退时间累加
                earlyTime += below - upperBelowBetween.getLong(upperBelowBetween.size() - 1);
                earlySum++;
                // 添加正常下班时间到纠正时间集合
                correctTimeList.add(upperBelowBetween.getLong(upperBelowBetween.size() - 1));
                // 添加正常下班时间到纠正时间分段集合
                correctList.add(upperBelowBetween.getLong(upperBelowBetween.size() - 1));
            }
        }
        // 否则是奇数
        else {
            // 定义存储单时间
            long oneTime;
            // 定义存储单时间是否是上班时间
            boolean isNormalUpper = false;
            // 定义存储单时间是否是下班时间
            boolean isNormalBelow = false;
            // 判断范围时间长度为3
            if (upperBelowBetween.size() == 3) {
                // 判断第0个时间为上班时间
                if (upperBelowBetween.getLong(0) <= upper) {
                    // 添加规定上班时间到纠正时间集合
                    correctTimeList.add(upper);
                    // 添加规定上班时间到纠正时间分段集合
                    correctList.add(upper);
                    // 添加单时间到正常上班时间集合
                    originTimeList.add(upperBelowBetween.getLong(0));
                    // 添加单时间到正常上班时间分段集合
                    originList.add(upperBelowBetween.getLong(0));
                } else {
                    // 判断开启自动补卡
                    if (isAutoCardReplacement) {
                        // 添加规定上班时间到纠正时间集合，算补卡
                        correctTimeList.add(upper);
                        // 添加规定上班时间到纠正时间分段集合，算补卡
                        correctList.add(upper);
                        // 添加为零时间到正常上班时间集合
                        originTimeList.add(upper);
                        // 添加为零时间到正常上班时间分段集合
                        originList.add(upper);
                    } else {
                        // 迟到时间累加
                        lateTime += upperBelowBetween.getLong(0) - upper;
                        lateSum++;
                        // 添加为零上班时间到纠正时间集合
                        correctTimeList.add(0L);
                        // 添加为零上班时间到纠正时间分段集合
                        correctList.add(0L);
                        // 添加为零时间到正常上班时间集合
                        originTimeList.add(0L);
                        // 添加为零时间到正常上班时间分段集合
                        originList.add(0L);
                    }
                    // 遍历范围时间集合
                    for (int i = 0; i < upperBelowBetween.size(); i++) {
                        long betweenTime = upperBelowBetween.getLong(i);
                        // 添加范围时间到正常时间集合
                        originTimeList.add(betweenTime);
                        // 添加范围时间到正常时间分段集合
                        originList.add(betweenTime);
                        // 添加范围时间到纠正时间集合
                        correctTimeList.add(betweenTime);
                        // 添加范围时间到纠正时间分段集合
                        correctList.add(betweenTime);
                    }
                    // 获取中间下班时间
                    Long centreTime1 = upperBelowBetween.getLong(0);
                    // 获取中间上班时间
                    Long centreTime2 = upperBelowBetween.getLong(1);
                    // 使用中间上班时间减中间下班时间，得到中间时间差
                    long l = centreTime2 - centreTime1;
                    if (l > 0) {
                        earlyTime += centreTime2 - centreTime1;
                        earlySum++;
                    }
                    resultTime[0] = lateTime;
                    resultTime[1] = earlyTime;
                    resultTime[2] = lateSum;
                    resultTime[3] = earlySum;
                    return resultTime;
                }
                // 判断最后一个时间为下班时间
                if (upperBelowBetween.getLong(upperBelowBetween.size() - 1) >= below) {
                    // 添加规定下班时间到纠正时间集合
                    correctTimeList.add(below);
                    // 添加规定下班时间到纠正时间分段集合
                    correctList.add(below);
                    // 添加最后一个时间到正常时间集合
                    originTimeList.add(upperBelowBetween.getLong(upperBelowBetween.size() - 1));
                    // 添加最后一个时间到正常时间分段集合
                    originList.add(upperBelowBetween.getLong(upperBelowBetween.size() - 1));
                } else {
                    // 遍历范围时间集合
                    for (int j = 1; j < upperBelowBetween.size(); j++) {
                        // 添加范围时间到纠正时间集合
                        originTimeList.add(upperBelowBetween.getLong(j));
                        // 添加范围时间到纠正时间分段集合
                        originList.add(upperBelowBetween.getLong(j));
                        // 添加范围时间到正常时间集合
                        correctTimeList.add(upperBelowBetween.getLong(j));
                        // 添加范围时间到正常时间分段集合
                        correctList.add(upperBelowBetween.getLong(j));
                    }
                    // 判断是自动补卡
                    if (isAutoCardReplacement) {
                        // 添加规定下班时间到纠正时间集合
                        correctTimeList.add(below);
                        // 添加规定下班时间到纠正时间分段集合
                        correctList.add(below);
                        // 添加为零时间到正常上班时间集合
                        originTimeList.add(below);
                        // 添加为零时间到正常上班时间分段集合
                        originList.add(below);
                    } else {
                        // 获取早退时间
                        earlyTime += below - upperBelowBetween.getLong(upperBelowBetween.size() - 1);
                        earlySum++;
                        // 添加为零上班时间到纠正时间集合
                        correctTimeList.add(0L);
                        // 添加为零上班时间到纠正时间分段集合
                        correctList.add(0L);
                        // 添加为零时间到正常上班时间集合
                        originTimeList.add(0L);
                        // 添加为零时间到正常上班时间分段集合
                        originList.add(0L);
                    }
                    // 获取中间下班时间
                    Long centreTime1 = upperBelowBetween.getLong(1);
                    // 获取中间上班时间
                    Long centreTime2 = upperBelowBetween.getLong(2);
                    // 使用中间上班时间减中间下班时间，得到中间时间差
                    long l = centreTime2 - centreTime1;
                    if (l > 0) {
                        earlyTime += centreTime2 - centreTime1;
                        earlySum++;
                    }
                }
                resultTime[1] = earlyTime;
                resultTime[3] = earlySum;
                return resultTime;
            } else {
                // 获取单时间
                oneTime = upperBelowBetween.getLong(0);
                // 判断单时间为上班时间
                if (oneTime <= upper) {
                    // 设置为上班时间
                    isNormalUpper = true;
                } else {
                    // 判断单时间为下班时间
                    if (oneTime >= below) {
                        // 设置为下班时间
                        isNormalBelow = true;
                    }
                }
            }
            // 获取单时间最近规定时间下标
            int recentlyIndex = getContrastChkInTimeNew(oneTime, normalWorkList);
            // 定义获取单时间最近规定时间下标的反下标
            int reverseIndex;
            // isUpper是对应reverseIndex的上下班时间
            boolean isUpper = true;
            // 判断单时间最近规定时间下标为偶数
            if(recentlyIndex % 2 == 0){
                // 获取反下标
                reverseIndex = recentlyIndex+1;
                // 设置为不是上班时间
                isUpper = false;
            } else {
                // 获取反下标
                reverseIndex = recentlyIndex-1;
            }
            // 获取最近规定时间
            Long recentlyTime = normalWorkList.getLong(recentlyIndex);
            // 定义存储纠正上班时间
            long correctUpper = 0;
            // 定义存储纠正下班时间
            long correctBelow = 0;
            // 定义存储正常上班时间
            long originUpper = 0;
            // 定义存储正常下班时间
            long originBelow = 0;
            // 判断是自动补卡
            if (isAutoCardReplacement) {
                // 获取最近反规定时间
                Long reverseTime = normalWorkList.getLong(reverseIndex);
                // 判断单时间是上班时间
                if (isNormalUpper) {
                    // 赋值最近规定时间到纠正上班时间
                    correctUpper = recentlyTime;
                    // 赋值最近反规定时间到纠正下班时间
                    correctBelow = reverseTime;
                    // 赋值单时间到正常上班时间
                    originUpper = oneTime;
                    originBelow = reverseTime;
                } else {
                    // 判断单时间是下班时间
                    if (isNormalBelow) {
                        // 赋值最近反规定时间到纠正上班时间
                        correctUpper = reverseTime;
                        // 赋值最近规定时间到纠正下班时间
                        correctBelow = recentlyTime;
                        originUpper = reverseTime;
                        // 赋值单时间到正常下班时间
                        originBelow = oneTime;
                    } else {
                        // 判断单时间小于最近规定时间
                        if (oneTime < recentlyTime) {
                            // 赋值单时间到纠正上班时间
                            correctUpper = oneTime;
                            // 赋值最近规定时间到纠正下班时间
                            correctBelow = recentlyTime;
                            // 赋值单时间到正常上班时间
                            originUpper = oneTime;
                            originBelow = recentlyTime;
                        } else {
                            // 赋值最近规定时间到纠正上班时间
                            correctUpper = recentlyTime;
                            // 赋值单时间到纠正下班时间
                            correctBelow = oneTime;
                            originUpper = recentlyTime;
                            // 赋值单时间到正常下班时间
                            originBelow = oneTime;
                        }
                        // 判断最近规定时间是上班时间
                        if (isUpper) {
                            // 获取迟到时间
                            lateTime += (oneTime - reverseTime);
                            lateSum++;
                        } else {
                            // 获取早退时间
                            earlyTime += (reverseTime - oneTime);
                            earlySum++;
                        }
                    }
                }
            } else {
                if (oneTime < recentlyTime) {
                    // 赋值单时间到纠正上班时间
                    correctUpper = oneTime;
                    // 赋值单时间到正常上班时间
                    originUpper = oneTime;
                } else {
                    // 赋值单时间到纠正下班时间
                    correctBelow = oneTime;
                    // 赋值单时间到正常下班时间
                    originBelow = oneTime;
                }
            }
            // 添加纠正上班时间到纠正时间集合
            correctTimeList.add(correctUpper);
            // 添加纠正下班时间到纠正时间集合
            correctTimeList.add(correctBelow);
            // 添加纠正上班时间到纠正分段时间集合
            correctList.add(correctUpper);
            // 添加纠正下班时间到纠正分段时间集合
            correctList.add(correctBelow);
            // 添加正常上班时间到正常时间集合
            originTimeList.add(originUpper);
            // 添加正常下班时间到正常时间集合
            originTimeList.add(originBelow);
            // 添加正常上班时间到正常分段时间集合
            originList.add(originUpper);
            // 添加正常下班时间到正常分段时间集合
            originList.add(originBelow);
        }
        resultTime[0] = lateTime;
        resultTime[1] = earlyTime;
        resultTime[2] = lateSum;
        resultTime[3] = earlySum;
        return resultTime;
    }

    /**
     * 全部是特殊时间的处理方法
     * @param userDates 用户的上班时间
     * @param teDur 规定的总需要上班时间
     */
    public JSONObject testSpecialTime(JSONArray userDates,long teDur,String theSameDay){
        if (null == userDates || userDates.size() == 0) {
            return null;
        }
        JSONObject result = new JSONObject();
        // 判断用户时间为奇数
        if (userDates.size() % 2 != 0) {
            // 删除最后一个时间
            userDates.remove(userDates.size()-1);
        }
        // 定义存储特殊时间
        long specialTime = 0;
        // 定义存储缺勤时间
        long absenceTime = 0;
        // 定义存储加班时间
        long overtime = 0;
        // 遍历用户上班时间集合
        for (int i = 0; i < userDates.size(); i+=2) {
            // 获取上班时间
            Long upper = userDates.getLong(i);
            // 获取下班时间
            Long below = userDates.getLong(i + 1);
            // 获取缺勤时间
            specialTime += below - upper;
        }
        // 判断特殊总时间大于规定上班时间
        if (specialTime > teDur) {
            // 获取超出的时间为加班时间
            overtime = specialTime - teDur;
            // 纠正正常上班时间
            specialTime = teDur;
        } else {
            // 获取缺勤时间
            absenceTime = teDur - specialTime;
        }
        System.out.println();
        System.out.println("用户上班时间 - userDates:");
        System.out.println(JSON.toJSONString(getTimeToStr(userDates)));
        System.out.println();
        System.out.println("特殊上班时间 - specialTime:"+specialTime);
        System.out.println("缺勤时间 - absenceTime:"+absenceTime);
        System.out.println("加班时间 - overtime:"+overtime);
        System.out.println("总要求上班时间 - teDur:"+teDur);
        System.out.println("当天时间 - theSameDay:"+theSameDay);
        System.out.println();
        result.put("userDates",userDates);
        result.put("specialTime",specialTime);
        result.put("absenceTime",absenceTime);
        result.put("overtime",overtime);
        result.put("teDur",teDur);
        return result;
    }

    /**
     * 获取用户的long上班时间
     * @param chkInEs   用户上班时间
     * @return  用户的long（按秒）上班时间
     */
    public JSONArray getUserDates(JSONArray chkInEs){
        // 定义存储转换后的log集合
        List<LogFlow> logFlows = new ArrayList<>();
        // 遍历用户上班时间
        for (int i = 0; i < chkInEs.size(); i++) {
            // 将用户上班时间转换为log对象，并且添加到log集合
            logFlows.add(JSONObject.parseObject(JSON.toJSONString(chkInEs.getJSONObject(i)), LogFlow.class));
        }
        //lambda表达式实现List接口sort方法按照log对象data字段内的date字段进行排序
        logFlows.sort(Comparator.comparing(num -> num.getData().getString("date")));
        // 定义存储转换后的long集合
        JSONArray dates = new JSONArray();
        // 遍历log集合
        for (LogFlow logFlow : logFlows) {
            // 获取data字段
            JSONObject data = logFlow.getData();
            // 获取打卡时间
            String date1 = data.getString("date");
            // 调用转换时间方法并且添加时间
            dates.add(getDeLong(date1));
        }
        // 返回结果
        return dates;
    }

    /**
     * 获取根据theSameDay日期格式化后的正常上班时间
     * @param arrTime   正常上班时间
     * @param theSameDay    当前日期
     * @return  格式化后计算的long正常上班时间
     */
    public JSONArray getArrTime(JSONArray arrTime,String theSameDay){
        // 定义存储格式化后的正常上班时间集合
        JSONArray arrTimeLong = new JSONArray();
        // 遍历正常上班时间
        for (int i = 0; i < arrTime.size(); i++) {
            // 调用转换方法转换为long时间，然后添加到格式化后的正常上班时间集合
            arrTimeLong.add(getDeLong(theSameDay+" "+arrTime.getString(i)));
        }
        // 返回结果
        return arrTimeLong;
    }

    /**
     * 将long时间转换成字符串时间
     * @param timeList  long时间集合
     * @return  转换后的字符串时间集合
     */
    public JSONArray getTimeToStr(JSONArray timeList){
        JSONArray correctWorkDateStr = new JSONArray();
        for (int i = 0; i < timeList.size(); i++) {
            correctWorkDateStr.add(getDeDate(timeList.getLong(i)));
        }
        return correctWorkDateStr;
    }
    /**
     * 将long二维时间集合转换成字符串时间输出
     * @param timeList  long二维时间集合
     */
    public void getTimeToListStr(JSONArray timeList){
        for (int i = 0; i < timeList.size(); i++) {
            JSONArray longs = timeList.getJSONArray(i);
            JSONArray correctWorkDateStr = new JSONArray();
            for (int j = 0; j < longs.size(); j++) {
                correctWorkDateStr.add(getDeDate(longs.getLong(j)));
            }
            System.out.println(JSON.toJSONString(correctWorkDateStr));
        }
        System.out.println("---------------------");
    }

    /**
     * 将date时间转换成按秒的时间long
     * @param date  需要转换的时间
     * @return  转换结果
     */
    public long getDeLong(String date){
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        try {
            Date dateNew = sdf.parse(date);
            long timestamp = dateNew.getTime();
            return (timestamp/1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 将date转换成字符串时间
     * @param date  long时间
     * @return  字符串时间
     */
    public String getDeDate(long date){
        if (date == 0) {
            return "0";
        }
        Date dateNew = new Date(date*1000);
        SimpleDateFormat sdf = new SimpleDateFormat(DateEnum.DATE_TIME_FULL.getDate());
        return sdf.format(dateNew);
    }

    /**
     * 获取normalWorkList集合内距离oneTime最近的时间下标
     * @param oneTime   单时间
     * @param normalWorkList    公司规定上班时间
     * @return  距离oneTime最近的时间下标
     */
    public int getContrastChkInTimeNew(long oneTime,JSONArray normalWorkList){
        // 定义存储被单时间减后时间
        JSONArray countList = new JSONArray();
        // 遍历公司规定上班时间
        for (int i = 0; i < normalWorkList.size(); i++) {
            // 获取单时间减了规定上班时间后时间
            long re = oneTime - normalWorkList.getLong(i);
            // 对减后时间做正正处理，并且添加到集合
            countList.add((re < 0) ? -re : re);
        }
        // 定义存储距离oneTime最近的时间下标
        int index = 0;
        // 获取第一个时间，做开始对比时间
        long mix = countList.getLong(0);
        for (int i = 1; i < countList.size(); i++) {
            // 判断遍历时间小于第一时间
            if (countList.getLong(i) < mix) {
                // 替换成小于的时间
                mix = countList.getLong(i);
                // 并且更新小于下标
                index = i;
            }
        }
        // 返回下标
        return index;
    }

    /**
     * 获取originList集合属于upper和below之间的时间集合
     * @param upper 上班时间
     * @param below 下标时间
     * @param userDates    用户上班时间
     * @param upperRange    上班宽容扫描时间
     * @param belowRange    下标宽容扫描时间
     * @return  upper和below之间的时间集合
     */
    public JSONArray getUpperBelowBetween(long upper,long below,JSONArray userDates,long upperRange,long belowRange){
        // 定义存储upper和below之间的时间集合
        JSONArray resultList = new JSONArray();
        // 上班时间宽容
        upper -= upperRange;
        // 下标时间宽容
        below += belowRange;
        // 遍历用户上班时间
        for (int i = 0; i < userDates.size(); i++) {
            long origin = userDates.getLong(i);
            // 判断当前用户上班时间在upper和below之间
            if (origin >= upper && origin <= below) {
                // 添加到集合
                resultList.add(origin);
            }
        }
        // 返回集合结果
        return resultList;
    }

    /**
     * 发送统计结果日志方法
     * @param id_C  公司编号
     * @param id_U  用户编号
     * @param subType   子日志类型
     * @param theSameDay    当前日期
     * @param chkInData 统计打卡数据
     * @param chkType   统计类型
     */
    public void sendMsg(String id_C,String id_U,String subType,String theSameDay
            ,JSONObject chkInData,String chkType,int year,int month){
        if (id_U.equals("test")) {
            return;
        }
        JSONArray usageflow = qt.getES("usageflow", qt.setESFilt("id_U", id_U,"id_C",id_C,"subType",subType
//                ,"data.theSameDay",theSameDay
        ));
        if (null != usageflow && usageflow.size() > 0) {
            for (int i = 0; i < usageflow.size(); i++) {
                JSONObject jsonObject = usageflow.getJSONObject(i);
                qt.delES("usageflow",jsonObject.getString("id_ES"));
            }
        }
        LogFlow logFlow = new LogFlow();
        logFlow.setId_U(id_U);
        logFlow.setId_C(id_C);
        logFlow.setId("");
        logFlow.setLogType("usageflow");
        logFlow.setSubType(subType);
        logFlow.setZcndesc(subType.equals("dayChkin")?"当天打卡记录":"当月打卡记录");
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        logFlow.setImp(1);
        JSONObject data = new JSONObject();
        if (subType.equals("dayChkin")) {
            data.put("theSameDay",theSameDay);
            data.put("chkInData",chkInData);
            LocalDate date = getDate(theSameDay);
            data.put("month",(date.getMonthValue())+"");
            data.put("year",(date.getYear())+"");
        } else {
            data.put("month",month+"");
            data.put("year",year+"");
            data.put("chkInData",chkInData);
        }
        if (null != chkType) {
            data.put("chkType",chkType);
        }
        logFlow.setData(data);
        logFlow.setId_Us(qt.setArray(id_U));
        ws.sendWS(logFlow);
    }

    public LocalDate getDate(String dateStr){
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }
}
