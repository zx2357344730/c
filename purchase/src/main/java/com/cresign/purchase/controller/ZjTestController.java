package com.cresign.purchase.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ZjTestService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import com.cresign.tools.pojo.po.LogFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestController
 * @Date 2023/8/10
 * @ver 1.0.0
 */
@RestController
@RequestMapping("zj")
public class ZjTestController {
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private ZjTestService zjService;

    @SecurityParameter
    @PostMapping("/v1/getMdSetEs")
    public ApiResponse getMdSetEs(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getMdSetEs(
                    reqJson.getString("key"),
                    reqJson.getString("esIndex"),
                    reqJson.getString("condition"),
                    reqJson.getString("val"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ZjTestController.getMdSetEs", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLog")
    public ApiResponse sendLog(@RequestBody LogFlow logData) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLog(logData);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLog", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLogSp")
    public ApiResponse sendLogSp(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLogSp(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLogSp", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/sendLogXj")
    public ApiResponse sendLogXj(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.sendLogXj(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLogXj", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLog")
    public ApiResponse getLog(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getLog(reqJson.getString("id").equals("")?null:reqJson.getString("id")
                    , reqJson.getString("logType").equals("")?null:reqJson.getString("logType")
                    , reqJson.getString("subType").equals("")?null:reqJson.getString("subType")
                    , reqJson.getString("id_SP").equals("")?null:reqJson.getString("id_SP"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.sendLog", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/shareSave")
    public ApiResponse shareSave(@RequestBody JSONObject data) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareSave(data);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareSave", e);
        }
    }

//    @GetMapping("/v1/shareOpen")
    @SecurityParameter
    @PostMapping("/v1/shareOpen")
    public ApiResponse shareOpen(@RequestBody JSONObject data) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareOpen(data.getString("shareId"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareOpen", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/shareOpen/{shareId}")
    public ApiResponse shareOpenPath(@PathParam("shareId") String shareId) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.shareOpen(shareId);
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.shareOpenPath", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/initFC")
    public ApiResponse initFC(@RequestBody JSONObject data) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.initFC(data.getString("id_C"),tokData.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.initFC", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getFCAuth")
    public ApiResponse getFCAuth(@RequestBody JSONObject data) {
        try {
            return zjService.getFCAuth(data.getString("id_C"),data.getString("id"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getFCAuth", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setFCAuth")
    public ApiResponse setFCAuth(@RequestBody JSONObject data) {
        try {
            return zjService.setFCAuth(data.getString("id_C"),data.getString("id"),data.getJSONObject("users"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.setFCAuth", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getFCAuthByUser")
    public ApiResponse getFCAuthByUser(@RequestBody JSONObject data) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.getFCAuthByUser(data.getString("id_C"),tokData.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getFCAuthByUser", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLSProdShareId")
    public ApiResponse getLSProdShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLSProdShareId(data.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLSProdShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLSInfoShareId")
    public ApiResponse getLSInfoShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLSInfoShareId(data.getString("id_I"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLSInfoShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLNUserShareId")
    public ApiResponse getLNUserShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLNUserShareId(data.getString("id_U"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLNUserShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLNCompShareId")
    public ApiResponse getLNCompShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLNCompShareId(data.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLNCompShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLBProdShareId")
    public ApiResponse getLBProdShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLBProdShareId(data.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLBProdShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getLBInfoShareId")
    public ApiResponse getLBInfoShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getLBInfoShareId(data.getString("id_I"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getLBInfoShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/saveProdEncryption")
    public ApiResponse saveProdEncryption(@RequestBody JSONObject data) {
        try {
            return zjService.saveProdEncryption(data.getJSONObject("en"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.saveProdEncryption", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getProdEncryption")
    public ApiResponse getProdEncryption(@RequestBody JSONObject data) {
        try {
            return zjService.getProdEncryption(data.getString("id_P"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getProdEncryption", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getShareId")
    public ApiResponse getShareId(@RequestBody JSONObject data) {
        try {
            return zjService.getShareId(data.getString("shareId"), data.getString("type"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.getShareId", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/applyForView")
    public ApiResponse applyForView(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.applyForView(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"), reqJson.getInteger("imp"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.applyForView", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/applyForAgreeWith")
    public ApiResponse applyForAgreeWith(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.applyForAgreeWith(tokData.getString("id_U"), tokData.getString("id_C"),reqJson.getString("id")
                    , reqJson.getString("logType"), reqJson.getString("subType")
                    , reqJson.getString("zcnDesc"), reqJson.getJSONObject("data"), reqJson.getInteger("imp"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.applyForAgreeWith", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/statisticsChKin")
    public ApiResponse statisticsChKin(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return zjService.statisticsChKin(reqJson.getString("id_C"),reqJson.getJSONArray("sumDates"));
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "ZjTestController.statisticsChKin", e);
        }
    }
}
