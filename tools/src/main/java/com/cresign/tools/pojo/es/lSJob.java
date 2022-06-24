package com.cresign.tools.pojo.es;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
//@AllArgsConstructor//全参构造
@NoArgsConstructor//无参构造
@Document("lSJob")
public class lSJob {

    public lSJob(String jobName, String id_C, String cron, JSONObject wrdN, JSONObject wrddesc) {
        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");

        this.jobName = jobName;
        this.id_C = id_C;
        this.cron = cron;
        this.wrdN = wrdN == null ? (JSONObject) wrdEmpty.clone() : wrdN;
        this.wrddesc = wrddesc == null ? (JSONObject) wrdEmpty.clone() : wrddesc;
    }

    private String jobName;

    private String id_C;

    private String cron;

    private JSONObject wrdN;

    private JSONObject wrddesc;
}
