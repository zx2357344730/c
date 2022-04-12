//package com.cresign.tools.pojo.po.orderCard;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.mongodb.core.mapping.Document;
//
///**
// * ##ClassName: ProdAction
// * ##description: prod的action类
// * ##Author: tang
// * ##Updated: 2020/10/17 16:49
// * ##version: 1.0.0
// */
//@Data
//@Document(collection = "Order")
////生成全参数构造函数
//@AllArgsConstructor
//@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
//@JsonInclude(JsonInclude.Include.NON_NULL)
//
//public class OrderStock {
//
//    public OrderStock(String id_P, Integer qtynow, Integer qtymade) {
//
//        JSONObject wrdEmpty = new JSONObject();
//        wrdEmpty.put("cn","");
//
//    }
//
//
//    private static class Hod{
//        private static final OrderStock instance = new OrderStock();
//    }
//
//    public static OrderStock getInstance(){
//        return OrderStock.Hod.instance;
//    }
//
//    private String id_P;
//
//    private Double wn2qtynow;
//    private Double wn2qtymade;
//
//}
