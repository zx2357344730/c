package com.cresign.tools.pojo.po.orderCard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName OrderOQc
 * @Date 2023/6/26
 * @ver 1.0.0
 */
@Data
@Document(collection = "Order")
//生成全参数构造函数
@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderOQc {

    private int score;

    private int foCount;

}
