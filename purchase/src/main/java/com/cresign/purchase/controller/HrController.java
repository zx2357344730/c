package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.HrService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName HrController
 * @Date 2023/11/11
 * @ver 1.0.0
 */
@RestController
@RequestMapping("hr")
public class HrController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private HrService hrService;

    @SecurityParameter
    @PostMapping("/v1/statisticsChKin")
    public ApiResponse statisticsChKin(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return hrService.statisticsChKin(reqJson.getString("id_C"),reqJson.getJSONArray("id_Us")
                    ,reqJson.getJSONArray("sumDates"),reqJson.getInteger("chkInMode")
                    , reqJson.getBoolean("isAllSpecialTime"),reqJson.getBoolean("isAutoCardReplacement")
                    ,reqJson.getBoolean("isSumSpecialTime"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.statisticsChKin", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/statisticsChKinMonth")
    public ApiResponse statisticsChKinMonth(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return hrService.statisticsChKinMonth(reqJson.getString("id_C"),tokData.getString("id_U")
                    ,reqJson.getInteger("year"),reqJson.getJSONArray("months"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.statisticsChKin", e);
        }
    }

}
