package com.cresign.tools.pojo.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "quartz_locks")
@Data
public class QuartzLocks {

    private String id;

    private String type;

    private String keyGroup;

    private String keyName;

    private String instanceId;

    private Date time;
}
