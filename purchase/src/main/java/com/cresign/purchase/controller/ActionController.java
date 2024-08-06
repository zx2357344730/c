package com.cresign.purchase.controller;


import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.service.ActionService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import com.cresign.tools.enumeration.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * ##description:
 *
 * @author JackSon
 * @updated 2020/8/6 10:05
 * @ver 1.0
 */
@RestController
@RequestMapping("action")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private GetUserIdByToken getUserToken;

    @Autowired
    private RetResult retResult;


    /**
     * 递归发日志 改isPush
     *
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:03
     */
    @SecurityParameter
    @PostMapping("/v1/dgActivate")
    public ApiResponse dgActivate(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return actionService.dgActivate(
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    tokData.getJSONObject("wrdNU"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.dgActivate", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/dgActivateAll")
    public ApiResponse dgActivateAll(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            return actionService.dgActivateAll(
                    reqJson.getString("id_O"),
                    tokData);

        } catch (Exception e) {

            return getUserToken.err(reqJson, "ActionController.dgActivateAll", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/dgActivateStorage")
    public ApiResponse dgActivateStorage(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            return actionService.dgActivateStorage(
                    tokData,
                    reqJson.getString("id_O"));

        } catch (Exception e) {

            return getUserToken.err(reqJson, "ActionController.dgActivateAll", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/mergeAllAndStorage")
    public ApiResponse mergeAllAndStorage(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
        try {
            return actionService.mergeAllAndStorage(reqJson.getString("id_O"), tokData);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.mergeAllAndStorage", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/dgActivateStoSingle")
    public ApiResponse dgActivateStoSingle(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            String re =  actionService.dgActivateStoSingle(
                    tokData,
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"));
            return retResult.ok(CodeEnum.OK.getCode(), re);

        } catch (Exception e) {

            return getUserToken.err(reqJson, "ActionController.dgActivateAll", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/taskToProd")
    public ApiResponse taskToProd(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            return actionService.taskToProd(
                    tokData,
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    reqJson.getString("id_P"));

        } catch (Exception e) {
            return getUserToken.err(reqJson, "actionService.task2Prod", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/dgActivateSingle")
    public ApiResponse dgActivateSingle(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));

        try {

            return actionService.dgActivateSingle(
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    tokData.getJSONObject("wrdNU"));

        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.dgActivateSingle", e);
        }
    }


    /**
     * 双方确认订单
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/10/27 9:03
     */
    @SecurityParameter
    @PostMapping("/v1/confirmOrder")
    public ApiResponse confirmOrder(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            Integer result = actionService.confirmOrder(
                    tokData,
                    reqJson.getString("id_O"));
            return retResult.ok(CodeEnum.OK.getCode(), result);

        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.confirmOrder", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/cancelOrder")
    public ApiResponse cancelOrder(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.cancelOrder(
                    tokData.getString("id_C"),
                    reqJson.getString("id_O"),
                    reqJson.getInteger("lST"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.cancelOrder", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/rePush")
    public ApiResponse rePush(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.rePush(
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    tokData);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.rePush", e);
        }
    }

    /**
     * 通用日志方法(action,prob,msg)
     *
     * @return java.lang.String  返回结果: 日志结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:08
     * 100 = cannot start
     * 0 = ready to go
     * 1 = processing
     * 2 = finish
     * 3 =
     * 4 =
     * 8 =
     * cancelled
     * bmdpt: 1= Process; 2 = part; 3 = Material; 4 = ProcessBatch; 5=SalesProduct,
     * no id_P oItem = ?
     */
    @SecurityParameter
    @PostMapping("/v2/statusChange")
    public ApiResponse statusChange(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
        try {
            JSONObject res = actionService.changeActionStatus(
                    null,
                    reqJson.getString("logType"),
                    reqJson.getInteger("status"),
                    reqJson.getString("msg"),
                    reqJson.getInteger("index"),
                    reqJson.getString("id_O"),
                    reqJson.getBoolean("isLink"),
                    reqJson.getString("id_FC"),
                    reqJson.getString("id_FS"),
                    tokData);
            return retResult.ok(CodeEnum.OK.getCode(), res);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "statusChg", e);
        }
    }

//    @SecurityParameter
//    @PostMapping("/v1/changeActionStatusNew")
//    public ApiResponse changeActionStatusNew(@RequestBody JSONObject reqJson) {
//        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
//        try {
//            return actionService.changeActionStatusNew(
//                    reqJson.getString("logType"),
//                    reqJson.getInteger("status"),
//                    reqJson.getString("msg"),
//                    reqJson.getInteger("index"),
//                    reqJson.getString("id_O"),
//                    reqJson.getBoolean("isLink"),
//                    reqJson.getString("id_FC"),
//                    reqJson.getString("id_FS"),
//                    tokData, reqJson.getJSONArray("id_Us"));
//        } catch (Exception e) {
//            return getUserToken.err(reqJson, "statusChg", e);
//        }
//    }


    /**
     * for a "component", batch change the status of all its subParts
     * isLink means whether this will control the next step
     * statusType 0 - all stop, 1 - all start, 2 - all finish
     *
     */
    @SecurityParameter
    @PostMapping("/v1/subStatusChange")
    public ApiResponse subStatusChange(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
            return actionService.subStatusChange(
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    reqJson.getBoolean("isLink"),
                    reqJson.getInteger("statusType"),
                    tokData);
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.subStatusChange", e);
        }
    }

//    @SecurityParameter
//    @PostMapping("/v1/getRefOPList")
//    public ApiResponse getRefOPList(@RequestBody JSONObject reqJson) {
//        try {
//            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
//            return actionService.getRefOPList(
//                    reqJson.getString("id_Flow"),
//                    reqJson.getBoolean("isSL"),
//                    tokData.getString("id_C"));
//        } catch (Exception e) {
//            return getUserToken.err(reqJson, "ActionController.getRefOPList", e);
//        }
//    }


    @SecurityParameter
    @PostMapping("/v1/createTask")
    public ApiResponse createTask(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);

        try {
            String result = actionService.createTask(
                    tokData,
                    reqJson.getString("logType"),
                    reqJson.getString("id_FC"),
                    reqJson.getString("id_O"),
                    reqJson.getJSONObject("oItemData"));
            return retResult.ok(CodeEnum.OK.getCode(), result);

        } catch (Exception e) {
            return getUserToken.err(reqJson, "createTask", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/createTaskNew")
    public ApiResponse createTaskNew(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);

        try {
            return actionService.createTaskNew(
                    reqJson.getString("logType"),
                    reqJson.getString("id"),
                    reqJson.getString("id_FS"),
                    reqJson.getString("id_O"),
                    tokData.getString("id_C"),
                    tokData.getString("id_U"),
                    tokData.getString("grpU"),
                    tokData.getString("dep"),
                    reqJson.getJSONObject("oItemData"),
                    tokData.getJSONObject("wrdNU"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "createTask", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/up_FC_action_grpB")
    public ApiResponse up_FC_action_grpB(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.up_FC_action_grpB(
                    tokData.getString("id_C"),
                    reqJson.getString("id_O"),
                    reqJson.getString("dep"),
                    reqJson.getString("depMain"),
                    reqJson.getString("logType"),
                    reqJson.getString("id_Flow"),
                    reqJson.getJSONObject("wrdFC"),
                    reqJson.getJSONArray("grpB"),
                    reqJson.getJSONArray("wrdGrpB"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.up_FC_action_grpB", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/createQuest")
    public ApiResponse createQuest(@RequestBody JSONObject reqJson) {
        JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);

        try {
            String re = actionService.createQuest(
                    null,
                    tokData,
                    reqJson.getString("id_O"),
                    reqJson.getInteger("index"),
                    reqJson.getString("id_Prob"),
                    reqJson.getString("id_FC"),
                    reqJson.getString("id_FQ"),
                    reqJson.getJSONObject("probData"));

            return retResult.ok(CodeEnum.OK.getCode(), re);

        } catch (Exception e) {
            return getUserToken.err(reqJson, "createQuest", e);
        }
    }

    /**
     * 更新Order的grpBGroup字段
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2021/1/19 10:05
     */
    @SecurityParameter
    @PostMapping("/v1/changeDepAndFlow")
    public ApiResponse changeDepAndFlow(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);

            if (reqJson.getBoolean("isSL")) {
                return actionService.changeDepAndFlowSL(
                        reqJson.getString("id_O"),
                        reqJson.getString("grpB"),
                        reqJson.getJSONObject("grpBOld"),
                        reqJson.getJSONObject("grpBNew"),
                        tokData.getString("id_C"),
                        tokData.getString("id_U"),
                        tokData.getString("grpU"),
                        tokData.getJSONObject("wrdNU"));
            } else {
                return actionService.changeDepAndFlow(
                        reqJson.getString("id_O"),
                        reqJson.getString("grpB"),
                        reqJson.getJSONObject("grpBOld"),
                        reqJson.getJSONObject("grpBNew"),
                        tokData.getString("id_C"),
                        tokData.getString("id_U"),
                        tokData.getString("grpU"),
                        tokData.getJSONObject("wrdNU"));
            }
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.changeDepAndFlow", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v2/dgConfirmOrder")
    public ApiResponse dgConfirmOrder(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.dgConfirmOrder(
                    tokData,
                    reqJson.getJSONArray("casList"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.dgConfirmOrder", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v2/getFlowList")
    public ApiResponse getFlowList(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.getFlowList(
                    tokData.getString("id_C"),
                    reqJson.getString("grpB"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.getFlowList", e);
        }
    }


    @SecurityParameter
    @PostMapping("/v1/actionChart")
    public ApiResponse actionChart(@RequestBody JSONObject json) {
        try {
            return actionService.actionChart(
                    json.getString("id_O")
            );
        } catch (Exception e) {
            return getUserToken.err(json, "ActionController.actionChart", e);
        }
    }

    /**
     * 客服向顾客申请评分api
     *
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/applyForScore")
    public ApiResponse applyForScore(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.applyForScore(reqJson.getString("id_O"), reqJson.getInteger("index")
                    , reqJson.getString("id"), reqJson.getString("id_FS")
                    , tokData, reqJson.getJSONArray("id_Us"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.applyForScore", e);
        }
    }

    /**
     * 顾客评分api
     *
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/haveScore")
    public ApiResponse haveScore(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.haveScore(reqJson.getString("id_O"), reqJson.getInteger("index"), reqJson.getInteger("score")
                    , reqJson.getString("id"), reqJson.getString("id_FS"), tokData, reqJson.getJSONArray("id_Us"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.haveScore", e);
        }
    }

    /**
     * 客服回访顾客api
     *
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/foCount")
    public ApiResponse foCount(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.foCount(reqJson.getString("id_O"), reqJson.getInteger("index")
                    , reqJson.getString("id"), reqJson.getString("id_FS"), tokData
                    , reqJson.getInteger("type"), reqJson.getString("dataInfo"), reqJson.getJSONArray("id_Us"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.foCount", e);
        }
    }

    /**
     * 操作群的默认回复api
     *
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/updateDefReply")
    public ApiResponse updateDefReply(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.updateDefReply(tokData.getString("id_C")
                    , reqJson.getString("logId"), reqJson.getJSONArray("defReply"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.updateDefReply", e);
        }
    }

    /**
     * 发送日志api
     *
     * @param reqJson 请求参数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/7/1
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/sendMsgByOnly")
    public ApiResponse sendMsgByOnly(@RequestBody JSONObject reqJson) {
        try {
            JSONObject tokData = getUserToken.getTokenDataX(request.getHeader("authorization"), request.getHeader("clientType"), "core", 1);
            return actionService.sendMsgByOnly(reqJson.getString("logType"),
                    reqJson.getString("dataInfo"),
                    reqJson.getInteger("index"),
                    reqJson.getString("id_O"),
                    reqJson.getString("id"),
                    reqJson.getString("id_FS"),
                    tokData,
                    reqJson.getInteger("type"), reqJson.getJSONArray("id_Us"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "ActionController.sendMsgByOnly", e);
        }
    }

}
