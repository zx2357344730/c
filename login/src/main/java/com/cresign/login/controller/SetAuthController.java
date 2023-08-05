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
    private GetUserIdByToken getUserToken;

    @Autowired
    private SetAuthService setAuthService;

    @SecurityParameter
    @PostMapping("/v1/get_my_batch_list")
    public ApiResponse getMyBatchList(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

            return setAuthService.getMyBatchList(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    reqJson.getString("listType"),
                    reqJson.getJSONArray("grp"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "SetAuthController.getMyBatchList", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/get_up_card_list")
    public ApiResponse getMyUpdateCardList(@RequestBody JSONObject reqJson) throws IOException {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

            return setAuthService.getMyUpdateCardList(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    reqJson.getString("listType"),
                    reqJson.getString("grp"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "SetAuthController.getMyUpdateCardList", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/switchComp")
    public ApiResponse switchComp(@RequestBody Map<String, Object> reqMap) {

        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        try {
            return setAuthService.switchComp(
                    tokData.getString("id_U"),
                    reqMap.get("id_C").toString(),
                    request.getHeader("clientType"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SetAuthController.switchComp", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setAUN")
    public ApiResponse setAUN(
            @RequestBody Map<String, Object> reqMap) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

            return setAuthService.setAUN(
                    tokData.getString("id_U"),
                    reqMap.get("id_AUN").toString());
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SetAuthController.setAUN", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/get_my_switch")
    public ApiResponse getMySwitchComp() {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);


        try {
            return setAuthService.getMySwitchComp(
                    tokData.getString("id_U"),
                    request.getHeader("lang"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SetAuthController.getMySwitchComp", e);
        }
    }

    /**
     * 更新User的Info卡片的pic字段
     * @param reqJson	请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/21
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/setUserPic")
    public ApiResponse setUserPic(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        try {
            return setAuthService.setUserPic(tokData.getString("id_U"), reqJson.getString("pic"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SetAuthController.setUserPic", e);
        }
    }

    /**
     * 更新用户cid，推送id方法
     * @param reqJson 请求参数体
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/31
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/setUserCid")
    public ApiResponse setUserCid(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
        try {
            return setAuthService.setUserCid(tokData.getString("id_U"), reqJson.getString("cid"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "SetAuthController.setUserCid", e);
        }
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