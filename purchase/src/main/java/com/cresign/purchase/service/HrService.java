package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONArray;
import com.cresign.tools.apires.ApiResponse;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName HrService
 * @Date 2023/11/30
 * @ver 1.0.0
 */
public interface HrService {

    ApiResponse statisticsChKin(String id_C, JSONArray id_Us, JSONArray sumDates, int chkInMode
            , boolean isAllSpecialTime, boolean isAutoCardReplacement, boolean isSumSpecialTime);

    ApiResponse statisticsChKinMonth(String id_C,String id_U, int year,JSONArray months);

}
