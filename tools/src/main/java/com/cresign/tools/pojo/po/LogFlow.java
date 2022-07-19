package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

//import java.io.Serializable;
//import java.util.List;

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
        this.index = index;
        this.id_C = logType.endsWith("SL") ? id_CB : tokData.getString("id_C");
        this.id_CS = oItem.getString("id_C");
        this.wrdN = oItem.getJSONObject("wrdN");
        this.imp = imp;
        this.wrdNU = tokData.getJSONObject("wrdNU");
        this.pic = oItem.getString("pic");
        this.lang = "cn";
        this.zcndesc = zcndesc;
        this.tzone = 8;
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    public LogFlow(String logType, String id_FC, String id_FS,  String subType,
                   String id_U, String grpU, String id_P, String grpPB, String grpPS, String id_O, Integer index, String id_C, String id_CS,
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
        this.tmd = DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate());
    }

    /**
     * 1、线程安全性保证:线程安全的
     * 2、性能:好
     * 3、延迟加载:延迟加载
     * ##return:  日志
     */
    public static LogFlow getInstance(){
        return LogFlow.Hod.instance;
    }

    private static class Hod{
        private static final LogFlow instance = new LogFlow();
    }

    /**
     *  如果Log是action / prob，data内加
     * private String bcdStatus; 现在的状态
     * private String type; 是普通 0/ 管理员 1/ 自动trigger 2
     * errId / errIndex / pan
     */
//    public void setActionData(Integer bisactivate, Integer bcdStatus, JSONArray id_Us, Double priority,String id_P,
//                              String id_OP, Integer ind_OP, String refOP, Integer bmdpt, JSONObject wrdNP,JSONObject wrdN) {
//        JSONObject data = new JSONObject();
//        data.put("bisactivate",bisactivate);
//        data.put("bcdStatus",bcdStatus);
//        data.put("bmdpt", bmdpt);
//        data.put("id_O", id_OP);
//        data.put("index",ind_OP);
//        data.put("id_OP", ind_OP);
//        data.put("refOP", refOP);
//        data.put("id_P",id_P);
//        data.put("priority",priority);
//        data.put("ex_wrdNP",wrdNP);
//        data.put("ex_wrdN",wrdN);
//
//
//        this.data = data;
//    }

    public void setLogData_action (OrderAction orderAction, OrderOItem orderOItem) {
        JSONObject data = new JSONObject();
        data.put("bisactivate",orderAction.getBisactivate());
        data.put("bcdStatus",orderAction.getBcdStatus());
        data.put("bmdpt", orderOItem.getBmdpt());
        data.put("id_P", orderOItem.getId_P());
        data.put("id_O", orderAction.getId_O());
        data.put("id_OP", orderAction.getId_OP());
        data.put("refOP", orderAction.getRefOP());
        data.put("index",orderAction.getIndex());
        data.put("priority",orderAction.getPriority());
        data.put("ex_wrdNP",orderAction.getWrdNP());
        data.put("ex_wrdN",orderAction.getWrdN());

        this.data = data;
    }

    public void setLogData_money (String id_A, String id_Afrom, Double wn2mny) {
        JSONObject data = new JSONObject();
        data.put("id_A", id_A);
        data.put("id_Afrom",id_Afrom);
        data.put("wn2mny", wn2mny);

        this.data = data;
    }
    public void setLogData_assetflow (Double qtynow, Double price, String id_A, Integer bcdStatus) {
        JSONObject data = new JSONObject();
        data.put("wn2qtynow",qtynow);
        data.put("wn4price", price);
        data.put("bcdStatus",bcdStatus);
        data.put("id_A", id_A);

        this.data = data;
    }
    public void setLogData_usage (OrderAction orderAction, OrderOItem orderOItem) {
        JSONObject data = new JSONObject();
        data.put("bisactivate",orderAction.getBisactivate());
        data.put("bcdStatus",orderAction.getBcdStatus());
        data.put("bmdpt", orderOItem.getBmdpt());
        data.put("id_O", orderAction.getId_O());
        data.put("index",orderAction.getIndex());
        data.put("priority",orderAction.getPriority());
        data.put("wn2qtyneed",orderOItem.getWn2qtyneed());
        data.put("ex_wrdNP",orderAction.getWrdNP());
        data.put("ex_wrdN",orderAction.getWrdN());

        if (null != orderAction.getId_Us()) {
            // 添加返回值
            data.put("id_Us",orderAction.getId_Us());
        }
        this.data = data;
    }


    /**
     * FlowControl标识 ID
     */
    private String id;
    private String id_FS;

    /**
     * 公司id
     */
    private String id_C;
    private String id_CS;

//    private String grpC;
//    private String grpCS;

    /**
     * 发送日志的用户id+grp
     * dep = 部门
     */
    private String id_U;
    private String dep;
    private String grpU;

    private String pic = "";

    /**
     * 日志对应的order+oItemIndex
     * 订单内的第Index个任务 （订单已final，oItem不能改
     * 有可能为空
     */
    private String id_O;
    private Integer index;
//    private String grpO;
//    private String grpOS;


    /**
     * oItem内对应的id_P
     * 有可能为空
     */
    private String id_P;
    private String grpB;
    private String grp;

    private JSONArray arrTime;


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
     * 日志具体内容数据
     */
    private JSONObject data = new JSONObject();

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


    //private String IpAddress;   // 用户ip

}