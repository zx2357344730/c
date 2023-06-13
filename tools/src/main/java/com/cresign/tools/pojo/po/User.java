package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "User")
@Data
public class User {

    private String id;

    private UserInfo info;

    private JSONObject contactu;

    private JSONObject cookiex;  //公司资料缓存

    private JSONArray view;

    private JSONObject spec;

    private JSONObject file00s;

    private JSONObject rolex;

//    private JSONObject sumChkinx;

    private JSONObject text00s;

    private JSONObject task00s;

    private JSONObject table00s;

    private JSONObject grid;

    @JsonProperty("uDate")
    private JSONObject uDate;

    private JSONObject picroll00s;

    private JSONObject link00s;

    private JSONObject qrShareCode;

    private JSONObject summ00s;

    private JSONObject summx;  //公司资料缓存

    private JSONObject fav;

    private Integer tvs = 1;


}
