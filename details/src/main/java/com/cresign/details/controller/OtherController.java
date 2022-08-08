package com.cresign.details.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.details.enumeration.DetailsEnum;
import com.cresign.details.service.OtherService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Ut;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/other")
public class OtherController {


    @Autowired
    private GetUserIdByToken getUserIdByToken;

    @Autowired
    private OtherService otherService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 供应商支付
     * ##Author:: Jevon
     * ##Params: requestJson
     * ##Params: request
     * ##version:: 1.0
     * ##updated: 2020/10/31 11:00
     * ##Return: java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/paymentOrder")
    public ApiResponse paymentOrder(@RequestBody Map<String, Object> requestJson, HttpServletRequest request) throws IOException {

        Object wn2mnyPaid = Ut.isNumber(requestJson.get("wn2mnyPaid").toString());

        if (wn2mnyPaid.equals(false)){

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);

        }

        return otherService.paymentOrder(
                requestJson.get("id_C").toString(),
                //"5f28bf314f65cc7dc2e60386",
                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                requestJson.get("grp").toString(),
                requestJson.get("listType").toString(),
                requestJson.get("id_O").toString(),
                requestJson.get("id_A").toString(),
                Double.parseDouble(wn2mnyPaid.toString())

        );
    }
    /**
     * 卖家收款
     * ##Author:: Jevon
     * ##Params: requestJson
     * ##Params: request
     * ##version:: 1.0
     * ##updated: 2020/10/31 11:00
     * ##Return: java.lang.String
     */
    @SecurityParameter
    @PostMapping("/v1/collectionOrder")
    public ApiResponse collectionOrder(@RequestBody JSONObject requestJson, HttpServletRequest request) throws IOException {


        Object wn2mnyReceive = Ut.isNumber(requestJson.get("wn2mnyReceive").toString());

        if (wn2mnyReceive.equals(false)){

            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.KEY_TYPE_ERROR.getCode(), null);

        }

        return otherService.collectionOrder(
                requestJson.get("id_C").toString(),
                //"5f28bf314f65cc7dc2e60386",
                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                requestJson.get("grp").toString(),
                requestJson.get("listType").toString(),
                requestJson.get("id_O").toString(),
                requestJson.get("id_A").toString(),
                Double.parseDouble(wn2mnyReceive.toString())
        );
    }


    @SecurityParameter
    @PostMapping("/v1/setLog")
    public ApiResponse setLog(@RequestBody JSONObject reqMap) throws IOException {

        return otherService.setLog(
                reqMap.get("id_C").toString(),
                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                reqMap.get("logType").toString(),
                reqMap.getJSONObject("data"));
    }

    @PostMapping("/v1/giftComp")
    @SecurityParameter
    public ApiResponse rootToPrntC(@RequestBody JSONObject reqJson){

        return otherService.rootToPrntC(
                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                //"5f28bf314f65cc7dc2e60387",
                reqJson.getString("uid"),
                reqJson.getString("id_C"),
                reqJson.getString("ref"));
    }

    @PostMapping("/v1/scriptEngine")
    @SecurityParameter
    public ApiResponse scriptEngine(@RequestBody JSONObject reqJson){

        return otherService.scriptEngine(
                reqJson.getString("script"),
                reqJson.getJSONObject("map"));
    }


}
