package com.cresign.tools.logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.LogFlow;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;


/**
 * 调用指定日志级别并发送日志
 * @author: Jevon
 * @version: 1.0
 * @createDate: 2021/6/26 13:04
 * @return:
 */
@Component
public class LogUtil {

    @Autowired
    private RestHighLevelClient client;


    //内部静态方法，获取日志名称和日记级别
   private static JSONObject selectLogType(String logType){

       JSONObject logObj = new JSONObject();
       switch(logType)
       {
           case "moneyflow":
               logObj.put("name","MONEYFLOW");logObj.put("intValue",301);
                break;
           case "usageflow":
               logObj.put("name","USAGEFLOW");logObj.put("intValue",302);
               break;
           case "assetflow" :
               logObj.put("name","ASSETFLOW");logObj.put("intValue",303);
               break;
           case "action":
               logObj.put("name","ACTION");logObj.put("intValue",304);
               break;
//           case "prob":
//               logObj.put("name","PROB");logObj.put("intValue",305);
//               break;
           case "msg":
               logObj.put("name","MSG");logObj.put("intValue",306);
               break;
           case "cusmsg":
               logObj.put("name","CUSMSG");logObj.put("intValue",307);
               break;
           default :
               throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.NOT_FOUND.getCode(), null);

       }


       return logObj;
   }


   private static Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

   public void sendLog(String logType,LogFlow data){

//       this.sendLogByES(logType, data);

       this.sendLogByFilebeat(logType,JSON.toJSONString(data));
       
   }

    private void sendLogByFilebeat(String logType,Object data){

        JSONObject logObj = selectLogType(logType);
        logger.log(Level.forName(logObj.getString("name"),logObj.getInteger("intValue")),  data);
    }

    private void sendLogByES(String logType, LogFlow logFlow){

        GetIndexRequest request = new GetIndexRequest(logType);
        try {
            // 获取结果
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            // 判断结果
            if (!exists) {
                // 1、创建索引请求
                CreateIndexRequest requestC = new CreateIndexRequest(logType);
                // 新增索引
                client.indices().create(requestC, RequestOptions.DEFAULT);
            }

            // 创建插入数据请求
            IndexRequest requestI = new IndexRequest(logType);

            // 将我们的数据放入请求 json
            requestI.source(JSON.toJSONString(logFlow), XContentType.JSON);
            // 写入完成立即刷新
            requestI.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
            // 写入数据
            client.index(requestI, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void moneyflow(Object objLogInfo){
        //1.第一个参数是指定日志级别，第二个数值是枚举类StandardLevel（可以理解为级别数，info的级别数是400，DEBUG是500，介于他们之间）
        //2.message要记录的消息对象。
        logger.log(Level.forName("MONEYFLOW",301),  objLogInfo);
    }


    public static void usageflow(Object objLogInfo){

        logger.log(Level.forName("USAGEFLOW",302),  objLogInfo);
    }
    public static void assetflow(Object objLogInfo){

        logger.log(Level.forName("ASSETFLOW",303),  objLogInfo);
    }
    public static void action(Object objLogInfo){

        logger.log(Level.forName("ACTION",304),  objLogInfo);
    }
//    public static void prob(Object objLogInfo){
//
//        logger.log(Level.forName("PROB" ,305),  objLogInfo);
//    }
    public static void msg(Object objLogInfo){

        logger.log(Level.forName("MSG",306),  objLogInfo);
    }



}
