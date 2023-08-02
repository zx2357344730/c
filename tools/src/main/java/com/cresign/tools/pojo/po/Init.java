package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "Init")
public class Init {

    private String id;

    private Integer ver = 1;

    private JSONObject col;

    private JSONObject card;

    private JSONObject batch;

    private Object lang;

    private String tmd;

    private JSONObject error;

    private JSONObject logType;

    private JSONObject listType;

    private JSONObject list;

    private Integer tvs = 1;

    private String hdKey;

    private JSONObject logInit;

    private JSONObject pdfInit;

    private JSONArray seMethod;

}
