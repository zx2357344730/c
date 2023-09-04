package com.cresign.tools.pojo.po.chkin;

import com.alibaba.fastjson.JSONArray;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName Chkin
 * @Date 2023/8/15
 * @ver 1.0.0
 */
@Data
//生成全参数构造函数
@AllArgsConstructor
//注解在类上，为类提供一个无参的构造方法
@NoArgsConstructor
public class Chkin {
    /**
     * 打卡方式
     */
    private String type;
    /**
     * 打卡类型
     */
    private String chkType;
    /**
     * 打卡地点-经度
     */
    private String locLat;
    /**
     * 打卡地点-纬度
     */
    private String locLong;
    /**
     * 加班开始时间-补卡时间段
     */
    private String teStart;
    /**
     * 加班结束时间-补卡时间段
     */
    private String teEnd;
    /**
     * 打卡时间
     */
    private String date;
    /**
     * 加班人 -补卡用户-请假人
     */
    private String id_UC;
    /**
     * 加班总时间
     */
    private int teDur;
    /**
     * 状态（提问，拒绝，同意）
     */
    private String state;
    /**
     * 请假日期数组
     */
    private JSONArray arrDayMiss;
}
