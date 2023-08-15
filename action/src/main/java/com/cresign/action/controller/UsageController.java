package com.cresign.action.controller;

import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.UsageService;
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
 * @author kevin
 * @ClassName UsageController
 * @Description
 * @updated 2022/11/16 10:51 PM
 * @return
 * @ver 1.0.0
 **/
@RestController
@RequestMapping("usage")
public class UsageController {

    @Autowired
    private UsageService usageService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @SecurityParameter
    @PostMapping("/v1/getNacosStatus")
    public ApiResponse getNacosStatus() {
        try {
            return usageService.getNacosStatus();
        } catch (Exception e) {
            return getUserToken.err(new JSONObject(), "UsageController.getNacosStatus", e);
        }
    }


//    @SecurityParameter
//    @PostMapping("/v1/setRecentTask")
//    public ApiResponse setRecentTask(@RequestBody JSONObject json) {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
//        return usageService.setRecentTask(
//                tokData.getString("id_U"),
//                tokData.getString("id_C"),
//                json.getString("id_O"),
//                json.getInteger("index"),
//                json.getString("id"),
//                json.getString("id_FS")
//        );
//    }

    @SecurityParameter
    @PostMapping("/v1/setFav")
    public ApiResponse setFav(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.setFav(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    json.getJSONObject("content")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.setFav", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setFavInfo")
    public ApiResponse setFavInfo(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.setFavInfo(
                    tokData.getString("id_U"),
                    json.getString("id_C"),
                    json.getString("id"),
                    json.getString("listType"),
                    json.getString("grp"),
                    json.getString("pic"),
                    json.getJSONObject("wrdN")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.setFavInfo", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getFav")
    public ApiResponse getFav(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.getFav(
                    tokData.getString("id_U")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.getFav", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/delFav")
    public ApiResponse delFav(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.delFav(
                    tokData.getString("id_U"),
                    json.getString("id_O"),
                    json.getInteger("index"),
                    json.getString("id"),
                    json.getString("id_FS")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.delFav", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/delFavInfo")
    public ApiResponse delFavInfo(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.delFavInfo(
                    tokData.getString("id_U"),
                    json.getString("id")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.delFavInfo", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/appointTask")
    public ApiResponse appointTask(@RequestBody JSONObject json) throws IOException {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.appointTask(
                    json.getJSONArray("array_U"),
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    json.getJSONObject("content")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.appointTask", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setPowerup")
    public ApiResponse setPowerup(@RequestBody JSONObject json) throws IOException {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.setPowerup(
                    tokData.getString("id_C"),
                    json.getJSONObject("capacity")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.setPowerup", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getPowerup")
    public ApiResponse getPowerup(@RequestBody JSONObject json) throws IOException {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.getPowerup(
                    tokData.getString("id_C"),
                    json.getString("ref")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.getPowerup", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setRefAuto")
    public ApiResponse setRefAuto(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.setRefAuto(
                    tokData.getString("id_C"),
                    json.getString("type"),
                    json.getJSONObject("jsonRefAuto")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.setRefAuto", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getRefAuto")
    public ApiResponse getRefAuto(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.getRefAuto(
                    tokData.getString("id_C"),
                    json.getString("type")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.getRefAuto", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setCookiex")
    public ApiResponse setCookiex(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.setCookiex(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    json.getString("type"),
                    json.getJSONArray("arrayCookiex")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.setCookiex", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getCookiex")
    public ApiResponse getCookiex(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.getCookiex(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    json.getString("type")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.getCookiex", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/notify")
    public ApiResponse notify(@RequestBody JSONObject json) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return usageService.notify(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    tokData.getJSONObject("wrdNU"),
                    json.getString("id"),
                    json.getJSONObject("wrdN"),
                    json.getJSONObject("wrddesc")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "UsageController.notify", e);
        }
    }

//    @SecurityParameter
//    @PostMapping("/v1/connectionComp")
//    public ApiResponse connectionComp(@RequestBody JSONObject json) throws IOException {
//        return usageService.connectionComp(
//                json.getString("id_C"),
//                json.getString("id_CB"),
//                json.getBoolean("isCB")
//        );
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/connectionProd")
//    public ApiResponse connectionProd(@RequestBody JSONObject json) throws IOException {
//        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
//
//        return usageService.connectionProd(
//                tokData.getString("id_C"),
//                json.getString("id_P")
//        );
//    }
}
