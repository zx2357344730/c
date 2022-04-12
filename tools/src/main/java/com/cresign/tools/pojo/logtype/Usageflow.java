//package com.cresign.tools.pojo.logtype;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//@JsonInclude(JsonInclude.Include.NON_NULL)//即如果加该注解的字段为null,那么就不序列化这个字段了
//@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法。
////@AllArgsConstructor//注解在类上，为类提供一个全参的构造方法。
//@Data
//@Document(collection = "Usageflow")
//public class Usageflow {
//    public Usageflow(String grpU, String grp, String grpA, JSONObject wrddesc, Integer bmdPay, String tmd, String tmk, String id_A, String id_C, String id_CB, String id_O, double wn2qtychg, Integer subtype) {
//        this.grpU = grpU;
//        this.grp = grp;
//        this.grpA = grpA;
//        this.wrddesc = wrddesc;
//        this.bmdPay = bmdPay;
//        this.tmd = tmd;
//        this.tmk = tmk;
//        this.id_A = id_A;
//        this.id_C = id_C;
//        this.id_CB = id_CB;
//        this.id_O = id_O;
//        this.wn2qtychg = wn2qtychg;
//        this.subtype = subtype;
//    }
//
//    private String grpU;
//
//    private String grp;
//
//    private String grpA;
//
//    private JSONObject wrddesc;
//
//    private Integer bmdPay;
//
//    private String tmd;
//
//    private String tmk;
//
//    private String id_A;
//
//    private String id_C;
//
//    private String id_CB;
//
//    private String id_O;
//
//    private double wn2qtychg;
//
//    private Integer subtype;
//
//
//
//
//
//}
