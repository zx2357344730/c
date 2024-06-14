package com.cresign.action.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.GetUserIdByToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName TaskController
 * @Date 2022/10/30
 * @ver 1.0.0
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    @Resource
    private HttpServletRequest request;
    @Resource
    private GetUserIdByToken getUserToken;
    @Resource
    private TimeZjService timeZjService;

    /**
     * 获取物料预计开始时间
     * @param reqJson 请求数据
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/17
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/getEstimateStartTime")
    public ApiResponse getEstimateStartTime(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getEstimateStartTime(reqJson.getString("id_O"),reqJson.getString("id_C")
                    , reqJson.getLong("teStart"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getEstimateStartTime", e);
        }
    }

    /**
     * 时间处理方法
     * @param reqJson 请求参数
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/getAtFirst")
    public ApiResponse getAtFirst(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getAtFirst(reqJson.getString("id_O"), reqJson.getLong("teStart"),
                    reqJson.getString("id_C"), reqJson.getInteger("wn0TPrior"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getAtFirst", e);
        }
    }

    /**
     * 根据主订单和对应公司编号，删除时间处理信息
     * @param reqJson 请求参数
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/removeTime")
    public ApiResponse removeTime(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.removeTime(reqJson.getString("id_O"),
                    reqJson.getString("id_C"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.removeTime", e);
        }
    }

    /**
     * 任务实际结束时间处理接口
     * @param reqJson 请求数据信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/timeSortFromNew")
    public ApiResponse timeSortFromNew(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.timeSortFromNew(reqJson.getString("dep"),reqJson.getString("grpB")
                    ,reqJson.getLong("currentTime"),reqJson.getInteger("index")
                    ,reqJson.getString("id_C"),reqJson.getLong("taPFinish"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.timeSortFromNew", e);
        }
    }

    /**
     * 根据订单编号与下标获取剩余数量的预计完成时间
     * @param reqJson 请求数据
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2022/11/6
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/timeCalculation")
    public ApiResponse timeCalculation(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.timeCalculation(reqJson.getString("id_O"),reqJson.getInteger("index")
                    ,reqJson.getInteger("number"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.timeCalculation", e);
        }
    }

    /**
     * 删除或者新增aArrange卡片信息
     * @param reqJson 请求数据信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/delOrAddAArrange")
    public ApiResponse delOrAddAArrange(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.delOrAddAArrange(reqJson.getString("id_C"), reqJson.getString("dep"), reqJson.getJSONObject("object"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.delOrAddAArrange", e);
        }
    }

    /**
     * 多订单时间处理方法
     * @param reqJson 请求参数
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     */
    @SecurityParameter
    @PostMapping("/v1/getAtFirstList")
    public ApiResponse getAtFirstList(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getAtFirstList(
                    reqJson.getString("id_C"), reqJson.getJSONArray("orderList"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getAtFirstList", e);
        }
    }

    /**
     * 获取计算物料时间后的开始时间
     * @param reqJson 请求参数
     * @return  开始时间
     */
    @SecurityParameter
    @PostMapping("/v1/getTeStart")
    public ApiResponse getTeStart(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getTeStart(reqJson.getString("id_O"), reqJson.getLong("teStart"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getTeStart", e);
        }
    }

    /**
     * 获取多订单一起计算物料时间后的开始时间
     * @param reqJson 请求参数
     * @return  开始时间
     */
    @SecurityParameter
    @PostMapping("/v1/getTeStartTotal")
    public ApiResponse getTeStartTotal(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getTeStartTotal(reqJson.getJSONArray("id_OArr"), reqJson.getLong("teStart"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getTeStartTotal", e);
        }
    }

    /**
     * 获取多订单分开计算物料时间后的开始时间
     * @param reqJson  请求参数
     * @return  开始时间
     */
    @SecurityParameter
    @PostMapping("/v1/getTeStartList")
    public ApiResponse getTeStartList(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getTeStartList(reqJson.getJSONArray("orderInfo"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getTeStartList", e);
        }
    }

    /**
     *
     * @return  开始时间
     */
    @SecurityParameter
    @PostMapping("/v1/updateODate")
    public ApiResponse updateODate(){
        try {
            return timeZjService.updateODate();
        } catch (Exception e) {
            return getUserToken.err(null, "TaskController.updateODate", e);
        }
    }

    /**
     *
     * @return  开始时间
     */
    @SecurityParameter
    @PostMapping("/v1/delExcessiveODateField")
    public ApiResponse delExcessiveODateField(){
        try {
            return timeZjService.delExcessiveODateField();
        } catch (Exception e) {
            return getUserToken.err(null, "TaskController.delExcessiveODateField", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getClearOldTask")
    public ApiResponse getClearOldTask(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getClearOldTask(reqJson.getString("id_O"), reqJson.getInteger("dateIndex")
                    , reqJson.getString("id_C"), reqJson.getString("layer"), reqJson.getString("id_PF"));
        } catch (Exception e) {
            return getUserToken.err(null, "TaskController.getClearOldTask", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/getAtFirstEasy")
    public ApiResponse getAtFirstEasy(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.getAtFirstEasy(reqJson.getString("id_O"), reqJson.getLong("teStart"),
                    reqJson.getString("id_C"),reqJson.getBoolean("setNew"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.getAtFirstEasy", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setAtFirstEasy")
    public ApiResponse setAtFirstEasy(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.setAtFirstEasy(reqJson.getString("id_O"), reqJson.getLong("teStart"),
                    reqJson.getString("id_C"),reqJson.getInteger("dateIndex")
                    ,reqJson.getString("layer"),reqJson.getString("id_PF"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.setAtFirstEasy", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setOrderUserCount")
    public ApiResponse setOrderUserCount(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.setOrderUserCount(reqJson.getString("id_O"),
                    reqJson.getString("id_C"), reqJson.getLong("teStart"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.setOrderUserCount", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/clearThisDayTaskAndSave")
    public ApiResponse clearThisDayTaskAndSave(@RequestBody JSONObject reqJson){
        try {
            System.out.println(JSON.toJSONString(reqJson));
            return timeZjService.clearThisDayTaskAndSave(reqJson.getString("id_C"),reqJson.getString("dep"),
                    reqJson.getString("grpB"),reqJson.getLong("thisDay"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.clearThisDayTaskAndSave", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/clearThisDayEasyTaskAndSave")
    public ApiResponse clearThisDayEasyTaskAndSave(@RequestBody JSONObject reqJson){
        try {
            System.out.println(JSON.toJSONString(reqJson));
            return timeZjService.clearThisDayEasyTaskAndSave(reqJson.getString("id_C"),reqJson.getString("dep"),
                    reqJson.getLong("thisDay"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.clearThisDayEasyTaskAndSave", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setAtFirst")
    public ApiResponse setAtFirst(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.setAtFirst(reqJson.getString("id_O"),reqJson.getLong("teStart"),
                    reqJson.getString("id_C"),reqJson.getInteger("wn0TPrior")
                    ,reqJson.getInteger("dateIndex"),reqJson.getString("layer"),reqJson.getString("id_PF"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.setAtFirst", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/setAtFirstList")
    public ApiResponse setAtFirstList(@RequestBody JSONObject reqJson){
        try {
            return timeZjService.setAtFirstList(reqJson.getString("id_C"),reqJson.getJSONArray("setList"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.setAtFirstList", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/clearOrderAllTaskAndSave")
    public ApiResponse clearOrderAllTaskAndSave(@RequestBody JSONObject reqJson){
        try {
            System.out.println(JSON.toJSONString(reqJson));
            return timeZjService.clearOrderAllTaskAndSave(reqJson.getString("id_C"),reqJson.getString("id_O"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.clearOrderAllTaskAndSave", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/clearThisDayTaskAndSaveNew")
    public ApiResponse clearThisDayTaskAndSaveNew(@RequestBody JSONObject reqJson){
        try {
            System.out.println(JSON.toJSONString(reqJson));
            return timeZjService.clearThisDayTaskAndSaveNew(reqJson.getString("id_C"),reqJson.getString("dep"),
                    reqJson.getString("grpB"),reqJson.getLong("thisDay"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.clearThisDayTaskAndSaveNew", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/clearThisDayEasyTaskAndSaveNew")
    public ApiResponse clearThisDayEasyTaskAndSaveNew(@RequestBody JSONObject reqJson){
        try {
            System.out.println(JSON.toJSONString(reqJson));
            return timeZjService.clearThisDayEasyTaskAndSaveNew(reqJson.getString("id_C"),reqJson.getString("dep"),
                    reqJson.getLong("thisDay"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.clearThisDayEasyTaskAndSaveNew", e);
        }
    }

    @SecurityParameter
    @PostMapping("/v1/clearThisDayEasyTaskAndSaveByEnd")
    public ApiResponse clearThisDayEasyTaskAndSaveByEnd(@RequestBody JSONObject reqJson){
        try {
            System.out.println(JSON.toJSONString(reqJson));
            return timeZjService.clearThisDayEasyTaskAndSaveByEnd(reqJson.getString("id_C"),reqJson.getString("dep"),
                    reqJson.getLong("thisDay"));
        } catch (Exception e) {
            return getUserToken.err(reqJson, "TaskController.clearThisDayEasyTaskAndSaveByEnd", e);
        }
    }

}
