package com.cresign.tools.pojo.po.orderCard;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * ##ClassName: ProdAction
 * ##description: prod的action类
 * @author tang
 * ##Updated: 2020/10/17 16:49
 * @ver 1.0.0
 */
@Data
@Document(collection = "Order")
//生成全参数构造函数
//@AllArgsConstructor
@NoArgsConstructor//注解在类上，为类提供一个无参的构造方法
@JsonInclude(JsonInclude.Include.NON_NULL)

public class OrderStock {

    public OrderStock(String id_P, String rKey, Integer index, Double qtynow, Double qtymade) {

        JSONObject wrdEmpty = new JSONObject();
        wrdEmpty.put("cn","");
        this.id_P = id_P;
        this.index = index;
        this.wn2qtynow = qtynow;
        this.wn2qtymade = qtymade;
        this.rKey = rKey;

    }


    private static class Hod{
        private static final OrderStock instance = new OrderStock();
    }

    public static OrderStock getInstance(){
        return OrderStock.Hod.instance;
    }

    private String id_P;

    private String rKey;

    private Integer index;

    private JSONArray objShip = new JSONArray();

    private JSONObject resvQty = new JSONObject();

    private Double wn2qtynow;
    private Double wn2qtymade;

}
