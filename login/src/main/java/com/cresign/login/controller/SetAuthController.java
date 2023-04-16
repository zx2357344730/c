package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.SetAuthService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;


@RequestMapping("setAuth")
@RestController
public class SetAuthController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getTokenOfUserId;

    @Autowired
    private SetAuthService setAuthService;

    @SecurityParameter
    @PostMapping("/v1/get_my_batch_list")
    public ApiResponse getMyBatchList(@RequestBody JSONObject reqJson) throws IOException {

        return setAuthService.getMyBatchList(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                reqJson.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getJSONArray("grp"));
    }

    @SecurityParameter
    @PostMapping("/v1/get_up_card_list")
    public ApiResponse getMyUpdateCardList(@RequestBody JSONObject reqJson) throws IOException {

        return setAuthService.getMyUpdateCardList(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                reqJson.getString("id_C"),
                reqJson.getString("listType"),
                reqJson.getString("grp"));
    }

    @SecurityParameter
    @PostMapping("/v1/switchComp")
    public ApiResponse switchComp(
            @RequestBody Map<String, Object> reqMap) {

        try {
            return setAuthService.switchComp(
                    getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqMap.get("id_C").toString(),
                    request.getHeader("clientType"));
        } catch(Exception e)
        {
            return null;
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setAUN")
    public ApiResponse setAUN(
            @RequestBody Map<String, Object> reqMap) {

        return setAuthService.setAUN(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                reqMap.get("id_AUN").toString());
    }

    @SecurityParameter
    @PostMapping("/v1/get_my_switch")
    public ApiResponse getMySwitchComp() {
        return setAuthService.getMySwitchComp(
                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                request.getHeader("lang"));
    }
//    @SecurityParameter
//    @PostMapping("/v1/get_my_switch1")
//    public ApiResponse getMySwitchComp1() {
//        return setAuthService.getMySwitchComp1(
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                request.getHeader("lang"));
//    }

//
//    @SecurityParameter
//    @PostMapping("/v1/ud_card_def")
//    public ApiResponse updateDefCard(@RequestBody JSONObject reqJson) {
//        return setAuthService.updateDefCard(
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getJSONObject("defData")
//        );
//    }

}