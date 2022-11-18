package com.cresign.tools.pojo.po;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "quartz_jobs")
@Data
public class QuartzJobs {

    private String id;

    private String keyName;

    private String keyGroup;

    private String jobDescription;

    private String jobClass;

    private Boolean durability;

    private Boolean requestsRecovery;

    private String id_C;

    private JSONObject params;
}
