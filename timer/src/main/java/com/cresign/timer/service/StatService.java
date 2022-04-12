package com.cresign.timer.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

public interface StatService {

    /**
     * 统计es日志数据
     * @Author Rachel
     * @Date 2021/10/28
     * ##param id_C 公司id
     * ##param startDate 开始时间
     * ##param endDate 结束时间
     * ##param second 分组秒数: 0:不分组 / 1:秒 / 60:分 / 3600:时 / 86400:日
     * ##param id_A 资产id
     * ##param excelField 分组字段
     * ##param outputType 导出类型：excel:写excel / card:写卡片
     * ##param fileName 名称：写excel:文件名 / 写卡片:卡片名
     * ##param logType 日志类型
     * ##param statField 结果字段
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse getStatistic(String id_C, String startDate, String endDate, String subType, Integer second, String id_A, JSONArray excelField, String outputType, String fileName, String logType, JSONArray statField) throws IOException, ParseException;


//    JSONObject statisticRecursion(JSONArray arrayBuckets, JSONArray excelField, Integer index, String key, JSONObject statField, JSONObject jsonStatistic);


    JSONArray getStatArrayByEs(JSONObject termField, JSONObject rangeField, JSONObject second, JSONArray excelField, String logType, JSONArray statField) throws IOException, ParseException;


    Object getStatValueByEs(JSONObject termField, JSONObject rangeField, String logType, JSONObject statField) throws IOException;


    JSONObject statFilter(String id_C, JSONArray excelField, String logType, Integer titleType, JSONArray arrayStatistic);


    /**
     * 递归处理es统计数据(有时间分组)
     * @Author Rachel
     * @Date 2021/10/21
     * ##param arrayBuckets es统计数据
     * ##param excelField 分组字段
     * ##param index 下标
     * ##param cell 处理后的单条数据
     * ##param arrayStatistic 处理后返回的数据
     * ##param statField 结果字段
     * @Return com.alibaba.fastjson.JSONArray
     * @Card
     **/
    JSONArray statisticRecursionHasTmd(JSONArray arrayBuckets, JSONArray excelField, Integer index, String cell, JSONArray statField, JSONArray arrayStatistic);

    /**
     * 递归处理es统计数据(无时间分组)
     * @Author Rachel
     * @Date 2021/10/30
     * ##param arrayBuckets es统计数据
     * ##param excelField 分组字段
     * ##param index 下标
     * ##param cell 处理后的单条数据
     * ##param arrayStatistic 处理后返回的数据
     * ##param statField 结果字段
     * @Return com.alibaba.fastjson.JSONArray
     * @Card
     **/
    JSONArray statisticRecursionNoTmd(JSONArray arrayBuckets, JSONArray excelField, Integer index, String cell, JSONArray statField, JSONArray arrayStatistic);

    /**
     * 获取mongo统计数据
     * @Author Rachel
     * @Date 2021/11/10
     * ##param id_C 公司id
     * ##param startDate 开始时间
     * ##param endDate 结束时间
     * ##param second 分组秒数
     * ##param excelField 分组字段
     * @Return com.alibaba.fastjson.JSONObject
     * @Card
     **/
    JSONObject getStatisticByMongo(String id_C, String startDate, String endDate, Integer second, JSONArray excelField) throws ParseException, IOException;

    /**
     * es统计数据写入mongo
     * @Author Rachel
     * @Date 2021/11/23
     * ##param arrayExcel
     * ##param id_C 公司id
     * ##param startDate 开始时间
     * ##param endDate 结束时间
     * ##param second 分组秒数
     * ##param excelField 分组字段
     * ##param fileName 卡片名
     * @Return java.lang.Boolean
     * @Card
     **/
    Boolean setStatisticByMongo(JSONArray arrayExcel, String id_C, String startDate, String endDate, Integer second, JSONArray excelField, String fileName) throws IOException, ParseException;


    Object getSumm00s(String id_C, Integer index) throws IOException, ParseException;


//    void setSumm00s(String id_C, JSONObject jsonObjData) throws IOException, ParseException;


}
