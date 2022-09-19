package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName Mod
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2022/7/25
 * @ver 1.0.0
 */
@Data
//生成全参数构造函数
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Mod {

//    public Mod(JSONArray id_U,String tfin,String ref,String tmk,Integer wn0buyUser,Integer wn2EstPrice,Integer bcdState
//            ,Integer wn2PaidPrice,String lCR,String wcnN,String id_P,Integer bcdLevel){
//        this.id_U = id_U;
//        this.tfin = tfin;
//        this.ref = ref;
//        this.tmk = tmk;
//        this.wn0buyUser = wn0buyUser;
//        this.wn2EstPrice = wn2EstPrice;
//        this.bcdState = bcdState;
//        this.wn2PaidPrice = wn2PaidPrice;
//        this.lCR = lCR;
//        this.wcnN = wcnN;
//        this.id_P = id_P;
//        this.bcdLevel = bcdLevel;
//    }

    /*
    {
  "id_U" : [],
  "tfin" : "2099/03/4 00:00:00",
  "ref" : "a-core",
  "tmk" : "2020/05/3 00:00:00",
  "wn0buyUser" : 8,
  "wn2EstPrice" : 0,
  "bcdState" : 1,
  "wn2PaidPrice" : 0,
  "lCR" : "1",
  "wcnN" : "基础ERP",
  "id_P" : "5f88ec9141f7d22cf400c4ab",
  "bcdLevel" : 1
    }
    */
    private JSONArray id_U;
    private String tfin;
    private String ref;
    private String tmk;
    private Integer wn0buyUser;
    private Integer wn2EstPrice;
    private Integer bcdState;
    private Integer wn2PaidPrice;
    private String lCR;
    private String wcnN;
    private String id_P;
    private Integer bcdLevel;
}
