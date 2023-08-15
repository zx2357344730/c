package com.cresign.login.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.MenuService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.authFilt.GetUserIdByToken;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.assetCard.MainMenuBO;
import com.cresign.tools.pojo.po.assetCard.SubMenuBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 * @author JackSon
 * @updated 2020-12-26 11:45
 * @ver 1.0
 */
@RequestMapping("menu")
@RestController
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private GetUserIdByToken getTokenOfUserId;

    @Autowired
    private AuthCheck authCheck;

    @Autowired
    private HttpServletRequest request;

    @SecurityParameter
    @PostMapping("/v1/get_menus")
    public ApiResponse getMenusAndSubMenus(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            JSONArray canUpdate = authCheck.getUserSelectAuth(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    tokData.getString("grpU"),
                    "lSAsset", "1003", "card");
            if (!canUpdate.contains("menu"))
            {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode() , null);
            }
            return menuService.getMenusAndSubMenus(tokData.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.getMenusAndSubMenus", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/get_grpU_menuData")
    public ApiResponse getGrpUForMenusInRole(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            JSONArray canUpdate = authCheck.getUserSelectAuth(
                    tokData.getString("id_U"), tokData.getString("id_C"),tokData.getString("grpU"),
                    "lSAsset", "1003", "card");
            if (!canUpdate.contains("menu"))
            {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode() , null);
            }

            return menuService.getGrpUForMenusInRole(
                    reqJson.getString("id_C"),
                    reqJson.getString("grpU")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.getGrpUForMenusInRole", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getMenuGrp")
    public ApiResponse getMenuGrp(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            JSONArray canUpdate = authCheck.getUserSelectAuth(
                    tokData.getString("id_U"), tokData.getString("id_C"),tokData.getString("grpU"),
                    "lSAsset", "1003", "card");
            if (!canUpdate.contains("menu"))
            {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode() , null);
            }
            return menuService.getMenuGrp(
                    tokData.getString("id_C"),
                    reqJson.getString("ref"),
                    reqJson.getString("grpType")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.getMenuGrp", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/get_menuData")
    public ApiResponse getMenuListByGrpU(@RequestBody JSONObject reqJson){
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            return menuService.getMenuListByGrpU(
                    tokData.getString("id_C"),
                    tokData.getString("grpU")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.getMenuListByGrpU", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/ud_grpU_mainMenu")
    public ApiResponse updateMenuData(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);

            return menuService.updateMenuData(
                    tokData.getString("id_U"),
                    tokData.getString("id_C"),
                    reqJson.getString("grpU"),
                    reqJson.getJSONArray("data").toJavaList(MainMenuBO.class)
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.updateMenuData", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/get_subMenu")
    public ApiResponse getSubMenusData(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"),"core",1);
            JSONArray canUpdate = authCheck.getUserSelectAuth(
                    tokData.getString("id_U"), tokData.getString("id_C"),tokData.getString("grpU"),
                    "lSAsset", "1003", "card");
            if (!canUpdate.contains("menu"))
            {
                throw new ErrorResponseException(HttpStatus.FORBIDDEN, CodeEnum.FORBIDDEN.getCode() , null);
            }
            return menuService.getSubMenusData(
                    tokData.getString("id_C")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.getSubMenusData", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/ud_subMenu")
    public ApiResponse updateSubMenuData(@RequestBody JSONObject reqJson) {
        try {
            return menuService.updateSubMenuData(
                    getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
                    reqJson.getString("id_C"),
                    reqJson.getJSONArray("data").toJavaList(SubMenuBO.class)
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.updateSubMenuData", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/get_def")
    public ApiResponse getDefListType(@RequestBody JSONObject reqJson) {
        try {
            return menuService.getDefListType(
                    reqJson.getString("id_C")
            );
        } catch (Exception e) {
            return getUserToken.err(reqJson, "MenuController.getDefListType", e);
        }
    }

//
//    @SecurityParameter
//    @PostMapping("/v1/check_submenu")
//    public ApiResponse checkSubMenuUse(@RequestBody JSONObject reqJson) {
//        return menuService.checkSubMenuUse(
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref")
//
//        );
//    }
//
//    @SecurityParameter
//    @PostMapping("/v1/del_submenu")
//    public ApiResponse delSubMenu(@RequestBody JSONObject reqJson) {
//        return menuService.delSubMenu(
//                getTokenOfUserId.getTokenOfUserId(request.getHeader("authorization"), request.getHeader("clientType")),
//                reqJson.getString("id_C"),
//                reqJson.getString("ref")
//
//                );
//    }


}