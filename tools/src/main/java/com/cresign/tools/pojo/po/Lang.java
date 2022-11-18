package com.cresign.tools.pojo.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author kevin
 * @ClassName Lang
 * @Description
 * @updated 2022/10/1 2:44 PM
 * @return
 * @ver 1.0.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "Lang")
@Data

public class Lang {


    private String cn;
    private String en;
    private String jp;

}
