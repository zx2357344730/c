package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "Init")
public class InitJava {

    private String id;

    private Integer ver = 1;

//    private JSONObject newComp;

//    private JSONObject newSpace;

//    private JSONObject newCompFake;

//    private JSONObject newUser;

//    private JSONObject newAsset;
//
//    private JSONObject newProd;

    private JSONObject ipList;
//    private JSONArray devList;

    private JSONObject analysis;

//    private JSONObject idJson;


//    private JSONObject newProbOrder;
//
//    private JSONObject newMsgOrder;
//
//    private JSONObject newCusmsgOrder;
//
//
//    private JSONObject newStorageOrder;
//
//    private JSONObject newMoneyOrder;


    private JSONObject cardInit;

    private JSONObject batchInit;

    private JSONObject logInit;

    private JSONObject modInit;


    private JSONObject listTypeInit;

    private JSONObject cardListAllow;

    private Integer tvs = 1;

//    private JSONObject statInit;

    //monthly update ES list
//    private JSONObject flowInit;

//    private JSONObject pdfInit;

//    private JSONObject logic;



}
