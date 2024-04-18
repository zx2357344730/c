package com.cresign.action.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

public interface UsageService {
   ApiResponse setFav(String id_U, String id_C, JSONObject content);
   ApiResponse setFavInfo(String id_U, String id_C, String id, String listType, String grp, String pic, JSONObject wrdN);

    ApiResponse getFav(String id_U, String type);

    ApiResponse delFav(String id_U, String id_O, Integer index, String id, String id_FS);

    ApiResponse delFavInfo(String id_U, String id);

        //    ApiResponse appointTask(JSONArray arrayId_U, String id_C, String id_O, Integer index, String id, String id_FS) throws IOException;
    ApiResponse appointTask(JSONArray arrayId_U, String id_UManager, String id_C, JSONObject content) throws IOException;


    ApiResponse setRefAuto(String id_C, String type, JSONObject jsonRefAuto);

    ApiResponse getRefAuto(String id_C, String type);

    ApiResponse setCookiex(String id_U, String id_C, String type, JSONArray arrayCookiex);

    ApiResponse getCookiex(String id_U, String id_C, String type);

    ApiResponse getNacosStatus();

    ApiResponse notifyLog(String id_U, String id_C, JSONObject wrdNU, String id, String id_I, JSONObject objLink, JSONObject wrdN, JSONObject wrddesc);

    ApiResponse updateIp(String ip, String id_U);

    /**
     * 连接真公司
     * @author Rachel
     * @Date 2022/03/14
     * @Param id_C
     * @Param id_CB
     * @Param bool true:连接客户 / false:连接供应商
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
//    ApiResponse connectionComp(String id_C, String id_CB, Boolean isCB) throws IOException;
//
//    /**
//     * 连接真产品
//     * @author Rachel
//     * @Date 2022/03/14
//     * @Param id_C
//     * @Param id_P
//     * @Return com.cresign.tools.apires.ApiResponse
//     * @Card
//     **/
//    ApiResponse connectionProd(String id_C, String id_P) throws IOException;

   ApiResponse setPowerup(String id_C, JSONObject capacity);

   ApiResponse getPowerup(String id_C);

}
