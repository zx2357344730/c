package com.cresign.action.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjService;
import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.token.GetUserIdByToken;
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
//    @SecurityParameter
    @PostMapping("/v1/getEstimateStartTime")
    public ApiResponse getEstimateStartTime(@RequestBody JSONObject reqJson){
        return timeZjService.getEstimateStartTime(reqJson.getString("id_O"),reqJson.getString("id_C")
                , reqJson.getLong("teStart"));
    }

//    @SecurityParameter
    @PostMapping("/v1/getAtFirst")
    public ApiResponse getAtFirst(@RequestBody JSONObject reqJson){
        return timeZjService.getAtFirst(reqJson.getString("id_O"), reqJson.getLong("teStart"),
                reqJson.getString("id_C"), reqJson.getInteger("wn0TPrior"));
    }

    @SecurityParameter
    @PostMapping("/v1/removeTime")
    public ApiResponse removeTime(@RequestBody JSONObject reqJson){
        return timeZjService.removeTime(reqJson.getString("id_O"),
                reqJson.getString("id_C"));
    }

    /**
     * 任务实际结束时间处理接口
     * @param reqJson 请求数据信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
//    @SecurityParameter
    @PostMapping("/v1/timeSortFromNew")
    public ApiResponse timeSortFromNew(@RequestBody JSONObject reqJson){
        System.out.println("请求信息:");
        System.out.println(JSON.toJSONString(reqJson));
        return timeZjService.timeSortFromNew(reqJson.getString("dep"),reqJson.getString("grpB")
                ,reqJson.getLong("currentTime"),reqJson.getInteger("index")
                ,reqJson.getString("id_C"),reqJson.getLong("taPFinish"));
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
        return timeZjService.timeCalculation(reqJson.getString("id_O"),reqJson.getInteger("index")
                ,reqJson.getInteger("number"));
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
        return timeZjService.delOrAddAArrange(reqJson.getString("id_C"),reqJson.getJSONObject("object"));
    }

}
