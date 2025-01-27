package com.cresign.purchase.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

/**
 * 添加模块
 * ##description:
 * @author Jevon
 * @updated 2021-05-20 14:30
 * @ver 1.0
 */
public interface ModuleService {

    /**
     * 更新a-auth内日志权限信息，更新全部
     * @param id_C	公司编号
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @param auth	更新的状态
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    ApiResponse modifyLogAuthAll(String id_C,String grpU,String listType,String grp,Integer auth);

    /**
     * 更新a-auth内日志权限信息，指定更新
     * @param id_C	公司编号
     * @param grpU	用户组别
     * @param listType	集合类型
     * @param grp	组别
     * @param auth	更新的状态
     * @param modRef	指定的模块权限
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    ApiResponse modifyLogAuth(String id_C,String grpU,String listType,String grp,Integer auth,String modRef);

    /**
     * 新增或修改init内日志模块
     * @param objLogMod	新增或修改信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    ApiResponse addOrUpdateInitMod(JSONObject objLogMod);

    /**
     * 根据id_C修改asset的a-auth内role对应的grpU+listType+grp
     * @param id_C  公司编号
     * @param grpU  用户组别
     * @param listType  集合类型
     * @param grp   组别
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/9/19
     */
    ApiResponse updateLogAuth(String id_C,String grpU,String listType,String grp);

//    /**
//     * 单翻译 - 只能翻译一个字段
//     * @param data	需要翻译的数据
//     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/8/19
//     */
//    ApiResponse singleTranslate(JSONObject data);

    /**
     * 多翻译 - 按照指定格式请求，可以翻译所有的字段
     * @param data	需要翻译的数据
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse manyTranslate(JSONObject data);

    /**
     * es的lsprod转lbprod
     * @param id_P	产品编号
     * @param id_C	公司编号
     * @param isMove	是否删除lsprod
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse lSProdTurnLBProd(String id_P,String id_C,Boolean isMove);

    /**
     * 新增或删除用户的模块使用权
     * @param id_C	公司编号
     * @param objUser	用户信息集合
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse modSetUser(String id_C,JSONObject objUser);

    /**
     * 根据公司编号操作公司资产的模块信息
     * @param id_C  公司编号
     * @param objModQ   操作的模块信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse modSetControl(String id_C,JSONObject objModQ);

    /**
     * 根据id_C获取模块信息
     * @param id_C	公司编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse modGetControl(String id_C);

    /**
     * 建立连接关系
     * @param id_C  公司编号
     * @param id_CP 公司编号？
     * @param id_CB 供应商编号
     * @param id_CBP 供应商编号？
     * @param wrdNC 公司名称
     * @param wrddesc 公司描述
     * @param wrdNCB 供应商名称
     * @param wrddescB 供应商描述
     * @param grp 公司组别
     * @param grpB 供应商组别
     * @param refC 公司编号
     * @param refCB 供应商编号
     * @param picC 公司图片
     * @param picCB 供应商图片
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse modAddLSBComp(String id_C,String id_CP,String id_CB,String id_CBP
            ,JSONObject wrdNC,JSONObject wrddesc,JSONObject wrdNCB,JSONObject wrddescB
            ,String grp,String grpB,String refC,String refCB,String picC,String picCB);

    /**
     * 添加模块信息
     * @author Jevon
     * @param id_U      用户id
     * @param oid       redis订单号
     * @param id_C      公司id
     * @param ref       模块编号
     * @param bcdLevel  模块等级
     * @ver 1.0
     * @updated 2021/3/5 15:24
     * @return java.lang.String
     */
    ApiResponse addModule (String id_U, String oid, String id_C, String ref, Integer bcdLevel) throws IOException;

    /**
     * 新建公司并添加默认模块
     * @param uid   用户id
     * @param reqJson   公司基本资料
     * @author Jevon
     * @ver 1.0
     * @updated 2020/08/05 08:32:53
     * @return String
     */
    ApiResponse addBlankComp(String uid, JSONObject reqJson) throws IOException;

}
