package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

/**
 * @ClassName RpiService
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2022/8/17
 * @ver 1.0.0
 */
public interface RpiService {

    /**
     * 删除机器绑定公司接口
     * @param rname 树莓派id
     * @param id_C  公司编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/17
     */
    ApiResponse delPi(String rname, String id_C);

    /**
     * 获取生成二维码数据api接口
     * @param rname 树莓派id
     * @param id_C  公司编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 16个gpio二维码数据
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/17
     */
    ApiResponse rpiCode(String rname,String id_C);

    /**
     * 解除绑定RPI接口
     * @param token	gpio的token
     * @param id_C	公司编号
     * @param id_U	操作用户编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/17
     */
    ApiResponse relieveRpi(String token,String id_C,String id_U);

    /**
     * RPI二维码扫码后请求的api，获取RPI的绑定状态接口
     * @param token gpio的token
     * @param id_C  公司编号
     * @param id_U  操作用户编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 请求状态
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/17
     */
    ApiResponse requestRpiStatus(String token,String id_C,String id_U);

    /**
     * 绑定RPI接口
     * @param token gpio的token
     * @param id_C  公司编号
     * @param id_U  操作用户编号
     * @param grpU  用户组别
     * @param oIndex    订单对应下标
     * @param wrdNU 名称
     * @param imp   未知，
     * @param id_O  订单编号
     * @param tzone 未知，
     * @param lang  语言？
     * @param id_P  产品编号
     * @param pic   图片
     * @param wn2qtynow 数量
     * @param grpB  产品组别？
     * @param fields    未知，
     * @param wrdNP 名称
     * @param wrdN  名称
     * @param dep   部门
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/17
     */
    ApiResponse bindingRpi(String token, String id_C, String id_U, String grpU, Integer oIndex
            , JSONObject wrdNU, Integer imp, String id_O, Integer tzone, String lang, String id_P
            , String pic, Integer wn2qtynow, String grpB, JSONObject fields, JSONObject wrdNP
            , JSONObject wrdN, String dep);

}
