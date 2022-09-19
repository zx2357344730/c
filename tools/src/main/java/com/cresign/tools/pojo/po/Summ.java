package com.cresign.tools.pojo.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * @author tangzejin
 * @updated 2019/10/23
 * @ver 1.0.0
 * ##description: 结果类
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Summ")
@Data
public class Summ {
    private String id;
    private String id_C;
    private String logType;
    private String date;


    /**
     * 日志子类型
     */
    private String subType;
    private String tmk;
    private String calculationType;
    private Map<String, Object> data;

    private Integer tvs = 1;


}
