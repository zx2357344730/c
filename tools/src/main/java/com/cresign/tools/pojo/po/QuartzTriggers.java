package com.cresign.tools.pojo.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "quartz_triggers")
@Data
public class QuartzTriggers {

    private String id;

    private String state;

    private String calendarName;

    private String jobId;

    private String keyName;

    private String keyGroup;

    private Integer misfireInstruction;

    private String cronExpression;

    private String timezone;
}
