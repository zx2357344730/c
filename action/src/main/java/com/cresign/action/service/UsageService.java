package com.cresign.action.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

public interface UsageService {

    ApiResponse setFav(String id_U, String id_C, String id_O, Integer index, String id, String id_FS);

    ApiResponse setRecentTask(String id_U, String id_C, String id_O, Integer index, String id, String id_FS);

    ApiResponse getFav(String id_U, String id_C);

    ApiResponse setFavAppoint(JSONArray arrayId_U, String id_C, String id_O, Integer index, String id, String id_FS);

    ApiResponse setRefAuto(String id_C, String type, JSONObject jsonRefAuto);

    ApiResponse getRefAuto(String id_C, String type);

    ApiResponse setCookiex(String id_U, String id_C, String type, JSONArray arrayCookiex);

    ApiResponse getCookiex(String id_U, String id_C, String type);

    ApiResponse getNacosStatus();

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
    ApiResponse connectionComp(String id_C, String id_CB, Boolean isCB) throws IOException;

    /**
     * 连接真产品
     * @author Rachel
     * @Date 2022/03/14
     * @Param id_C
     * @Param id_P
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse connectionProd(String id_C, String id_P) throws IOException;

}
