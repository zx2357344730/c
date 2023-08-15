package com.cresign.login.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.RedirectService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * ##description:
 * @author JackSon
 * @updated 2021-05-03 13:57
 * @ver 1.0
 */
@RestController
@RequestMapping("redirect")
public class RedirectController {

    @Autowired
    private GetUserIdByToken getUserIdByToken;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedirectService redirectService;

    /**
     * 获取发送日志二维码方法
     * @param reqJson 请求数据
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/createSaleChkinCode")
    public ApiResponse generateSaleChkinCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.generateSaleChkinCode(
                    reqJson.getString("id_C"),
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"))
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.generateSaleChkinCode", e);
        }
    }

    /**
     * 扫码（扫描）发送日志二维码后请求的方法
     * @param reqJson 请求数据
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/5/29
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/scanSaleChkinCode")
    public ApiResponse scanSaleChkinCode(@RequestBody JSONObject reqJson) {
        try {
            System.out.println("进入扫码后的方法:");
            return redirectService.scanSaleChkinCode(
                    reqJson.getString("token"),
                    reqJson.getString("longitude"), reqJson.getString("latitude")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.scanSaleChkinCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/generateProdCode")
    public ApiResponse generateProdCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.generateProdCode(
                    reqJson.getString("id_C"),
                    reqJson.getString("id_P"),
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("mode"),
                    reqJson.getJSONObject("data")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.generateProdCode", e);
        }
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"),"core",1)
    }



    @SecurityParameter
    @PostMapping("/v1/resetProdCode")
    public ApiResponse resetProdCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.resetProdCode(
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("id_P"),
                    reqJson.getString("id_C")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.resetProdCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/generateUserCode")
    public ApiResponse generateUserCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.generateUserCode(
                    reqJson.getString("id_C"),
                    //reqJson.getString("id_P"),
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("mode"),
                    reqJson.getJSONObject("data")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.generateUserCode", e);
        }
    }
    @SecurityParameter
    @PostMapping("/v1/generateCompCode")
    public ApiResponse generateCompCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.generateCompCode(
                    reqJson.getString("id_C"),
                    //reqJson.getString("id_P"),
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("mode"),
                    reqJson.getJSONObject("data")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.generateCompCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/generateOrderCode")
    public ApiResponse generateOrderCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.generateOrderCode(
                    reqJson.getString("id_C"),
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),

                    reqJson.getString("id_O"),
                    reqJson.getString("listType"),
                    reqJson.getString("mode"),
                    reqJson.getJSONObject("data")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.generateOrderCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/scanCode")
    public ApiResponse scanCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.scanCode(
                    reqJson.getString("token"),
                    reqJson.getString("listType"),
                    //"5f28bf314f65cc7dc2e60262"
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"))
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.scanCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/resetUserCode")
    public ApiResponse resetUserCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.resetUserCode(
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    //reqJson.getString("id_P"),
                    reqJson.getString("id_C")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.resetUserCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/resetCompCode")
    public ApiResponse resetCompCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.resetCompCode(
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    //reqJson.getString("id_P"),
                    reqJson.getString("id_C")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.resetCompCode", e);
        }
    }
    @SecurityParameter
    @PostMapping("/v1/resetOrderCode")
    public ApiResponse resetOrderCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.resetOrderCode(
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("id_O"),
                    reqJson.getString("id_C")
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.resetOrderCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/create_joincomp")
    public ApiResponse generateJoinCompCode(@RequestBody JSONObject reqJson) {
        try {
            return redirectService.generateJoinCompCode(
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("id_C"),
                    reqJson.getString("mode"),
                    reqJson.getJSONObject("data"));
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.generateJoinCompCode", e);
        }
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"),"core",1)
    }

    @SecurityParameter
    @PostMapping("/v1/scan_joincomp")
    public ApiResponse scanJoinCompCode(@RequestBody JSONObject reqJson) throws IOException {
        try {
            return redirectService.scanJoinCompCode(
                    reqJson.getString("token"),
                    //"t3RCVlkDOMTuyNeNhIx",
                    //"60dd20a5d8555e3fdbba4ccc"
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"))
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.scanJoinCompCode", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/reset_joincomp_code")
    public ApiResponse resetJoinCompCode(@RequestBody JSONObject reqJson) throws IOException {
        try {
            return redirectService.resetJoinCompCode(
                    getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                "5f28bf314f65cc7dc2e60386",
                    reqJson.getString("id_C")
//                getUserIdByToken.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType"),"core",1)
            );
        } catch (Exception e) {
            return getUserIdByToken.err(reqJson, "RedirectController.resetJoinCompCode", e);
        }
    }


}