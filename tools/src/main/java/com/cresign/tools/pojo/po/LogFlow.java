package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DoubleUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.chkin.Chkin;
import com.cresign.tools.pojo.po.chkin.Hr;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;



@Data
@Document(collection = "LogFlow")
//生成全参数构造函数
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogFlow {


    public LogFlow(JSONObject tokData, JSONObject oItem, JSONObject action, String id_CB, String id_O, Integer index, String logType, String subType, String zcndesc, Integer imp) {

        this.id = action.getJSONObject("grpBGroup").getJSONObject(oItem.getString("grpB")) == null ||
                action.getJSONObject("grpBGroup").getJSONObject(oItem.getString("grpB")).getString("id_Flow") == null ?
                "" : action.getJSONObject("grpBGroup").getJSONObject(oItem.getString("grpB")).getString("id_Flow");
        this.id_FS = action.getJSONObject("grpGroup").getJSONObject(oItem.getString("grp")) == null ||
                action.getJSONObject("grpGroup").getJSONObject(oItem.getString("grp")).getString("id_Flow") == null ?
                "" : action.getJSONObject("grpGroup").getJSONObject(oItem.getString("grp")).getString("id_Flow");
        this.logType = logType;
        this.subType = subType;
        this.dep = tokData.getString("dep");
        this.grpU = tokData.getString("grpU");
        this.grpB = oItem.getString("grpB");
        this.grp = oItem.getString("grp");
        this.id_U = tokData.getString("id_U");
        this.id_P = oItem.getString("id_P");
        this.id_O = id_O;
        this.id_OP = action.getJSONArray("objAction").getJSONObject(index).getString("id_OP");
        this.index = index;
        this.id_C = logType.endsWith("SL") ? id_CB : tokData.getString("id_C");
        this.id_CS = oItem.getString("id_C");
        this.wrdN = oItem.getJSONObject("wrdN");
        this.imp = imp;
        this.wrdNU = tokData.getJSONObject("wrdNReal");
        this.pic = oItem.getString("pic");
        this.lang = "cn";
        this.zcndesc = zcndesc;
        this.tzone = 8;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }

//    public LogFlow(JSONObject tokData, String id_P, String id_O, String logType, String subType, String zcndesc, Integer imp) {
//
//        // for Usageflow
//        this.id = "";
//        this.id_FS = "";
//        this.logType = logType;
//        this.subType = subType;
//        this.dep = tokData.getString("dep");
//        this.grpU = tokData.getString("grpU");
//        this.grpB = "";
//        this.grp = "";
//        this.id_U = tokData.getString("id_U");
//        this.id_P = id_P;
//        this.id_O = id_O;
//        this.id_OP = "";
//        this.index = 0;
//        this.id_C = tokData.getString("id_C");
//        this.id_CS = tokData.getString("id_C");
//        JSONObject wrdN = new JSONObject();
//        wrdN.put("cn", zcndesc);
//        this.wrdN = wrdN;
//        this.imp = imp;
//        this.wrdNU = tokData.getJSONObject("wrdNReal");
//        this.pic = "";
//        this.lang = "cn";
//        this.zcndesc = zcndesc;
//        this.tzone = 8;
//        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
//    }

    public LogFlow(String logType, String id_FC, String id_FS,  String subType,
                   String id_U, String grpU, String id_P, String grpPB, String grpPS, String id_OP, String id_O, Integer index, String id_C, String id_CS,
                   String pic,String dep, String zcndesc, Integer imp,
                   JSONObject wrdN, JSONObject wrdNU) {
        this.id = id_FC == null ? "" : id_FC;
        this.id_FS = id_FS == null ? "" : id_FS;
        this.logType = logType;
        this.subType = subType;
        this.dep = dep;
        this.grpU = grpU;
        this.grpB = grpPB;
        this.grp = grpPS;
        this.id_U = id_U;
        this.id_P = id_P;
        this.id_O = id_O;
        this.id_OP = id_OP;
        this.index = index;
        this.id_C = id_C;
        this.id_CS = id_CS;
        this.wrdN = wrdN;
        this.imp = imp;
        this.wrdNU = wrdNU;
        this.pic = pic;
        this.lang = "cn";
        this.zcndesc = zcndesc;
        this.tzone = 8;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }

    /**
     * 1、线程安全性保证:线程安全的
     * 2、性能:好
     * 3、延迟加载:延迟加载
     * @return  日志
     */
    public static LogFlow getInstance(){
        return LogFlow.Hod.instance;
    }

    private static class Hod{
        private static final LogFlow instance = new LogFlow();
    }

    public void setLogData_Chkin(Chkin chKin){
        JSONObject data = new JSONObject();
        data.put("type",chKin.getType());
        data.put("chkType",chKin.getChkType());
        data.put("locLat", chKin.getLocLat());
        data.put("locLong", chKin.getLocLong());
        data.put("teStart", chKin.getTeStart());
        data.put("teEnd", chKin.getTeEnd());
        data.put("date", chKin.getDate());
        data.put("id_UC",chKin.getId_UC());
        data.put("wntDur",chKin.getWntDur());
        data.put("state",chKin.getState());
        data.put("arrDayMiss",chKin.getArrDayMiss());
        this.data = data;
    }
    public void setLogData_Hr(Hr hr){
        JSONObject data = new JSONObject();
        data.put("grpUNow",hr.getGrpUNow());
        data.put("wn0Hire",hr.getWn0Hire());
        data.put("grpUNew", hr.getGrpUNew());
        data.put("id_UA", hr.getId_UA());
        data.put("wrddesc", hr.getWrddesc());
        data.put("wrdappv", hr.getWrdappv());
        data.put("dep", hr.getDep());
        this.data = data;
    }

    public void setLogData_action (OrderAction orderAction, OrderOItem orderOItem) {
        JSONObject data = new JSONObject();
        data.put("bisactivate",orderAction.getBisactivate());
        data.put("bcdStatus",orderAction.getBcdStatus());
        data.put("bmdpt", orderAction.getBmdpt());
        data.put("id_P", orderOItem.getId_P());
        data.put("id_O", orderOItem.getId_O());
        data.put("id_OP", orderOItem.getId_OP());
        data.put("refOP", orderAction.getRefOP());
        data.put("index",orderAction.getIndex());
        data.put("priority",orderOItem.getPriority());
        data.put("ex_wrdNP",orderAction.getWrdNP());
        data.put("ex_wrdN",orderAction.getWrdN());

        this.id_OP = orderOItem.getId_OP();
        this.setLogType("action");

        this.data = data;
    }


    public void setSysLog(String id_C, String subType, String zcndesc, Integer imp, JSONObject wrdN) {

        this.id = "BNyYCj2P4j3zBCzSafJz6aei";
        this.id_FS = "";
        this.logType = "usageflow";
        this.subType = subType;
        this.dep = "";
        this.grpU = "1001";
        this.grpB = "1000";
        this.grp = "1000";
        this.id_U = "6459fcb946c4cb3525b63b8a";
        this.id_P = "";
        this.id_O = "";
        this.id_OP = "";
        this.index = 0;
        this.id_C = id_C;
        this.id_CS = "";
        this.wrdN = wrdN;
        this.imp = imp;
        JSONObject bot = new JSONObject();
        bot.put("cn", "小银【系统】");
        this.wrdNU = bot;
        this.pic = "https://cresign-1253919880.cos.ap-guangzhou.myqcloud.com/avatar/cresignbot.jpg";
        this.lang = "cn";
        this.zcndesc = zcndesc;
        this.tzone = 8;
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }

    public void setUsageLog(JSONObject tokData, String updateType, String zcndesc, Integer imp, String id, String listType, JSONObject wrdN, String grp, String info)
    {
        this.id = "BNyYCj2P4j3zBCzSafJz6aei";
        this.id_FS = "";
        this.logType = "usageflow";
        this.subType = "update";
        this.dep = "";
        this.grpU = tokData.getString("grpU");
        this.grpB = "1000";
        this.grp = "1000";
        this.id_U = tokData.getString("id_U");
        this.id_P = "";
        this.id_O = "";
        this.id_OP = "";
        this.index = 0;
        this.id_C = tokData.getString("id_C");
        this.id_CS = "";

        this.wrdN = wrdN;
        this.imp = imp;
        this.wrdNU = tokData.getJSONObject("wrdNReal");
        this.pic = tokData.getString("pic");
        this.lang = "cn";
        this.zcndesc = zcndesc;
        this.tzone = 8;
        JSONObject data = this.getData();
        data.put("upType", updateType);
        data.put("id", id);
        data.put("listType", listType);
        data.put("grp", grp);
        data.put("info", info);
        this.tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
    }


    public void setActionTime (Long taStart, Long taFin, String type) {
        JSONObject data = this.getData();
        if (type.equals("push"))
        {
            data.put("taPush", taStart); //推送时间
        } else {
            data.put("taStart", taStart); //开始
            data.put("taFin", taFin); //完成
        }
        data.put("type", type);
    }

    public void setLogData_money (String id_A, String id_Afrom, Double wn2mny) {
        JSONObject data = new JSONObject();
        data.put("id_A", id_A);
        data.put("id_Afrom",id_Afrom);
        data.put("wn2mny", wn2mny);

        this.data = data;
    }

//    public void setLogData_duraflow (Long taStart, Long taFin, String type) {
//        JSONObject data = new JSONObject();
//        data.put("taStart", taStart);
//        data.put("taFin",taFin);
//        data.put("type", type);
//        this.data = data;
//    }

    public void setLogData_assetflow (Double qtynow, Double price, String id_A, String grpA) {
        JSONObject data = new JSONObject();
        data.put("wn2qtynow",qtynow);
        data.put("wn4price", price);
        data.put("wn4value", DoubleUtils.multiply(qtynow, price));
        data.put("id_A", id_A);
        data.put("grpA", grpA);

        this.setLogType("assetflow");
        this.data = data;
    }

    public void setLogData_cusmsg (Double qtynow, Double price, String id_A, String grpA) {
        JSONObject data = new JSONObject();


        this.setLogType("cusmsg");
        this.data = data;
    }
    public void setLogData_saleflow (Double qtynow, Double price, String id_A, String grpA) {
        JSONObject data = new JSONObject();
        data.put("wn2qtynow",qtynow);
        data.put("wn4price", price);
//        data.put("bcdStatus",bcdStatus);
        data.put("id_A", id_A);
        data.put("grpA", grpA);

        this.setLogType("saleflow");
        this.data = data;
    }


    /**
     * FlowControl标识 ID
     * id = 自己群ID = idFlow
     * id_FS = 我是对外的 id_Flow
     */
    //自己群ID = idFlow
    private String id = "";
    private String id_FS = "";

    /**
     * 日志具体内容数据
     */
    private JSONObject data = new JSONObject();

    /**
     * 公司id
     */
    private String id_C;
    private String id_CS;

    /**
     * 发送日志的用户id+grp
     * dep = 部门
     */
    private String id_U; //tokData.getString("id_U")

    private JSONArray id_Us = new JSONArray();
    private JSONArray id_APPs = new JSONArray();
    private String dep; //tokData.getString("dep")
    private String grpU; //tokData.getString("grpU")

    private String pic = "";

    /**
     * 日志对应的order+oItemIndex
     * 订单内的第Index个任务 （订单已final，oItem不能改
     * 有可能为空
     */
    private String id_O;

    private String id_OP;

    private Integer index;

    /**
     * oItem内对应的id_P
     * 有可能为空
     */
    private String id_P;
    private String grpB;
    private String grp;

    /**
     * 日志副类别 ： msg / statusChg / dataChg / manageChg / file(pic/video/voice/file) / link(COUPA) / addOItem(新任务)
     */
    private String subType;

    /**
     * 日志类型
     * ****** 改为 string
     */
    private String logType;

    /**
     * 语言 default = cn
     */
    private String lang = "cn";

    /**
     * 发送时间
     */
    private String tmd;


    /**
     * 日志内容
     */
    private String zcndesc = "";

    /**
     * 时区
     */
    private Integer tzone;

    /**
     * 用户名
     */
    private JSONObject wrdNU = new JSONObject();
    private JSONObject wrdN = new JSONObject();
    private JSONObject wrdNP = new JSONObject();


    /**
     * 重要度（对应flowControl 的 switch，控制是否推送）
     */
    private Integer imp;

    private String locate;

/**
 * assetflow 用来处理数量更改，prob/action 专注状态bcdstatus 的改变
 * 如果Log是assetflow的，data内加
 *  private int wn2qtychg;  //    * 数量
 *  private String id_Afrom;
 *  private String id_Ato;
 *  lUT //单位
 *  private String locAddr;
 *  private JSONArray locSpace; //     * 库存位置 改为 地址+仓位号
 *  private String id_UB;
 *  private String grpUB;
 *  private String wrdNUB; //。。。 其他需要显示的可以传
 *
 */

/**
 *  * 如果Log是moneyflow，data内加
     private Integer bmdPay; //
     private String id_CB; //?? 按需加
     private double wn4price;
 */

/**
 *  如果Log是usageflow，data内加
 * private String ipAddress; 用户ip
 * private String apiType;
 * private String code;
 * private JSONArray cardList;
 */

}