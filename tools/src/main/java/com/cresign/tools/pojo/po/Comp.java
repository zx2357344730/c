package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.compCard.CompInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "Comp")
public class Comp {

    private String id;

    private JSONObject file00s;


    private JSONObject contact;

    private CompInfo info;


    private JSONArray view;

    private JSONObject summ00s;

    private JSONObject spec;

    private JSONObject text00s;

//    private JSONObject task00s;

    private JSONObject picroll00s;

    private JSONObject saleCyclex;

    private JSONObject link00s;

//    private JSONObject devc;

    private JSONObject recentOP;

    private JSONObject qrShareCode;

    private JSONObject joinCode;

//    private JSONObject qrCodeType;


    private Integer bcdNet ;

    private Integer tvs = 1;

//    private JSONObject aList;






}
