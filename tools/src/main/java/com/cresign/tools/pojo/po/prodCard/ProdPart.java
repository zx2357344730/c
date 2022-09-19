//package com.cresign.tools.pojo.po.cardData;
//
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.mongodb.core.mapping.Document;
//
///**
// * ##ClassName: ProdOItem
// * ##description: prod的oitem类
// * @author tang
// * ##Updated: 2020/10/17 16:49
// * @ver 1.0.0
// */
//@Document(collection = "Prod")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class ProdPart {
//
//    public ProdPart(String id_P, String id_OP, String id_CP, String id_C, String id_CB, String id_O, Integer index,
//                      Integer bmdpt, String ref, String refB, String grp, String grpB, Integer wn0prior,
//                      String pic, Integer lUT, Integer lCR, Double wn2qtyneed, Double wn4price,
//                      JSONObject wrdNP, JSONObject wrdN, JSONObject wrddesc, JSONObject wrdprep) {
//
//        JSONObject wrdEmpty = new JSONObject();
//        wrdEmpty.put("cn","");
//
//        this.id_P = id_P;
//        this.id_OP = id_OP;
//        this.id_CP = id_CP == null || id_CP == "" ? id_C: id_CP;
//        this.id_C = id_C;
//        this.id_CB = id_CB;
//        this.id_O = id_O;
//        this.index = index;
//
//        this.bmdpt = bmdpt;
//        this.ref = ref == null? "": ref;
//        this.refB = refB == null? "": refB;
//
//        this.grp = grp == null? "1000": grp;
//        this.grpB = grpB == null? "1000": grpB;
//        this.wn0prior = wn0prior;
//
//        this.wrdNP = wrdNP == null? wrdEmpty: wrdNP;
//        this.wrdN = wrdN  == null? wrdEmpty: wrdN;
//        this.pic = pic  == null? "": pic;
//        this.lUT = lUT  == null? 0: lUT;
//        this.lCR = lCR  == null? 0: lCR;
//        this.wn2qtyneed = wn2qtyneed == null? 1: wn2qtyneed;
//        this.wn4price = wn4price == null? 0: wn4price;
//        this.wrddesc = wrddesc  == null? wrdEmpty: wrddesc;
//        this.wrdprep = wrdprep == null? wrdEmpty: wrdprep;
//    }
//
//
//    private static class Hod{
//        private static final ProdPart instance = new ProdPart();
//    }
//
//    public static ProdPart getInstance(){
//        return ProdPart.Hod.instance;
//    }
//
////    private String id;
//
//    /**
//     * 产品id
//     */
//    private String id_P;
//
//    /**
//     * 零件次序
//     */
//    private Integer wn0prior = 0;
//
//    /**
//     * 零件父公司id
//     */
//    private String id_CP;
//
//    private String id_OP;
//
//    /**
//     * 零件编号
//     */
//    private String ref;
//    private String refB;
//
//    /**
//     * 所有父产品名称拼接存放
//     */
//    private JSONObject wrdNP;
//
//    /**
//     * 零件公司id
//     */
//    private String id_C;
//
//    /**
//     * 零件图片
//     */
//    private String pic;
//
//    /**
//     * 零件用量单位，例：个，只，件
//     */
//    @JsonProperty("lUT")
//    private Integer lUT;
//
//    /**
//     * 零件的货币类型，例：人民币，美元
//     */
//    @JsonProperty("lCR")
//    private Integer lCR;
//
//    /**
//     * 零件用户组别
//     */
////    private String grpU;
//
//    /**
//     * ### -零件制作用量,需要数量- 不用
//     * 数量
//     */
//    private Double wn2qtyneed;
//
//
//    /**
//     * 零件单价
//     */
//    private Double wn4price;
//
//
//    /**
//     * 产品组别
//     */
//    private String grpB;
//    private String grp;
//
//
//    /**
//     * 是否合并: 0 : 未合并，1 : 合并，2 : 被合并
//     */
//    private Integer merge = 0;
//    private Integer bmdpt = 0;
//
//    private Integer index = 0;
//
//    /**
//     *
//     */
//    private JSONObject wrdN;
//
//    /**
//     *
//     */
//    private JSONObject wrddesc;
//
//    private JSONObject wrdprep;
//
//    /**
//     * 我 （id_C/id_CB== 我 = 工序）
//     */
//    private String id_CB;
//    private String id_O;
//
//    /**
//     *
//     */
//    private String seq = "1";
//
//    private String tmd;
//
//    private Integer tvs;
//}
