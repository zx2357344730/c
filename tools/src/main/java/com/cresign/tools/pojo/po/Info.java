package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.infoCard.InfoInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author kevin
 * @updated 2021/6/1
 * @ver 1.0.0
 * ##description: 产品实体类
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Info")
@Data
public class Info {

    private String id;

    private InfoInfo info;

    private JSONArray view;

    private JSONObject subInfo;

    private JSONObject spec;

    private JSONObject file00s;

    private JSONObject text00s;

    private JSONObject table00s;

    private JSONObject grid;

    private JSONObject summ00s;

    private JSONObject summx;

    private JSONObject form00s;

    private JSONObject picroll00s;

    private JSONObject link00s;   // 连接

    private JSONObject ch00s;

//    private JSONObject qrShareCode;

    private JSONObject logList00s;

    private JSONObject tempa;

    private JSONObject tag;

    private JSONObject jsonInfo;

    private JSONObject en;

    private Integer tvs = 1;

    private Long wn0fsize;



}
