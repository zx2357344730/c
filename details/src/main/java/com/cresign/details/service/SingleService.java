package com.cresign.details.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

/**
 * ##Author: JackSon
 * ##version: 1.0
 * ##description: 用于对 COUPA 的某一个进行增删改查
 * ##updated: 2020-03-23 13:43
 */
public interface SingleService {


    /**
     * 获取coupa某个详细资料
     * ##author: JackSon
     * ##Params: id            coupa的id
     * ##Params: listType      列表类型
     * ##Params: id_C          公司id
     * ##Params: tvs           版本号
     * ##Params: id_U          id_U
     * ##version: 1.0
     * ##updated: 2020/8/10 11:32
     * ##Return: java.lang.String
     */
    ApiResponse getSingle(String id, String listType, String id_C, String reqJsonC, Integer tvs, String id_U, String grp, String grpU);

    
//    ApiResponse updateSingle2(String id_C, JSONObject data, String listType, String impChg, String id_U, String grp, String authType) throws IOException;

    /**
     * 修改coupa某个详细资料
     * ##Author:: Jevon
     * ##Params: id_C      公司id
     * ##Params: data      卡片数据
     * ##Params: listType  列表类型
     * ##Params: impChg    卡片数组（字符串拼接）
     * ##Params: listCol   ES数据对象
     * ##Params: id_U      用户id
     * ##Params: grp       组别
     * ##Params: authType  类型（卡片、按钮）
     * ##version:: 1.0
     * ##updated: 2021/6/1 15:50
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse updateSingle(String id_C, JSONObject data, String listType, String impChg,JSONObject listCol, String id_U, String grp, String authType) throws IOException;

    /**
     * 修改自己信息并同步所有公司有关自己的信息
     * ##Author:: Jevon
     * ##Params: id_U      用户id
     * ##Params: upJson    卡片数据
     * ##Params: listCol   ES数据对象
     * ##Params: impChg    卡片数组（字符串拼接）
     * ##version:: 1.0
     * ##updated: 2021/6/1 15:52
     * ##Return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse updateMyUserDetail(String id_U, JSONObject upJson,JSONObject listCol, String impChg) throws IOException;

    /**
     * 修改公司信息并同步所有公司
     * ##Author:: Jevon
     * ##param id_C      公司id
     * ##param id_U      用户id
     * ##param upJson    卡片数据
     * ##param listColC  id_C,ES数据对象
     * ##param listColCB  id_CB,ES数据对象
     * ##param impChg    卡片数组（字符串拼接）
* ##version:: 1.0
* ##updated: 2021/6/1 15:53
     * @return
     */
    ApiResponse updateMyCompDetail(String id_C, String id_U, String grpU, JSONObject upJson, JSONObject listColC, JSONObject listColCB, String impChg) throws IOException;


    /**
     * 查看单个字段的值
     * ##author: JackSon
     * ##Params: id_U          用户id
     * ##Params: reqJson       请求参数
     * ##version: 1.0
     * ##updated: 2020/9/10 16:04
     * ##Return: java.lang.String
     */
//    String getSingleMini(String id_U, Map<String, Object> reqJson) throws IllegalAccessException;



    /**
     * 新增单个默认信息
     * ##Params: id_U  用户id
     * ##Params: grp  组别
     * ##Params: id_C    公司id
     * ##Params: listType  列表类型
     * ##Params: data      新增数据
     * ##author: Jevon
     * ##version: 1.0
     * ##updated: 2020/9/10 9:52
     * ##Return: java.lang.String
     */
    ApiResponse addEmptyCoup (String id_U,String grp,String id_C, String listType, String data) throws IOException;

    //内部增加lSAsset，可其他API调用
    JSONObject addlSProd(String id_C,String id_U, String id ,String ref,String data) throws IOException;
    //内部增加lBProd，可其他API调用
    JSONObject addlBProd(String id_C, String id_U, String id , String ref, String data) throws IOException;
    //内部增加lSAsset，可其他API调用
    JSONObject addlSAsset(String id_C,String id_U, String id ,String ref,String data) throws IOException;
    //内部增加lSOrder，可其他API调用
    JSONObject addlSOrder(String id_C,String id_U, String id ,String ref,String data) throws IOException;
    //内部增加lBOrder，可其他API调用
    JSONObject addlBOrder(String id_C,String id_U, String id ,String ref,String data) throws IOException;
    //内部增加lSComp，可其他API调用
    JSONObject addlSComp(String id_C,String id_U, String id ,String ref,String data) throws IOException;
    //内部增加lBComp，可其他API调用
    JSONObject addlBComp(String id_C,String id_U, String id ,String ref,String data) throws IOException;
    //自动编号排序
    String refAuto (String id_C,String listType,String grp);




    /**
     * ES有数据，mongdb没有的则删除
     * ##author: Jevon
     * ##Params: id_U  用户id
     * ##Params: id_C  公司id
     * ##Params: id    要操作id
     * ##Params: listType   列表
     * ##version: 1.0
     * ##updated: 2021/4/26 14:45
     * ##Return: java.lang.String
     */
    ApiResponse delUseless(String id_U,String id_C,String id,String listType) throws IOException;

    /**
     * 获取part卡片数据
     * @author: JackSon
     * ##Params: id_U
     * ##Params: id_C
     * ##Params: id_P
     * @version: 1.0
     * @createDate: 2021-04-12 8:26
     * ##Return: java.lang.String
     */
    String getPartCardData(String id_U, String id_C, String id_P);

    /**
     * 往oItem数组添加对象
     * @Author Rachel
     * @Data 2021/08/05
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param id_O 订单id
     * ##param oItemData 订单对象
     * ##param listType 列表类型
     * ##param grp 组别
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse setOItem (String id_U, String id_C, String id_O, JSONObject oItemData, String listType, String grp);

//    /**
//     * 根据id_C查询control.ref对应的id_A存入redis
//     * @Author Rachel
//     * @Data 2021/08/06
//     * ##param id_C 公司id
//     * @Return com.cresign.tools.apires.ApiResponse
//     **/
//    ApiResponse updateModuleId(String id_C);

    /**
     * 根据id_O获取objItem数组下标为index的对象
     * @Author Rachel
     * @Data 2021/08/09
     * ##param id_O 订单id
     * ##param index 订单产品的下标
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse getOItemDetail(String id_O, Integer index, JSONArray cardList);

    /**
     * 根据id_C和id_U删除指定公司所有数据
     * @Author Rachel
     * @Data 2021/08/09
     * ##param id_C 公司id
     * ##param id_U 用户id
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse delComp(String id_U, String id_C) throws IOException;

    /**
     * 修改订单指定下标的bcdStatus状态
     * @Author Rachel
     * @Data 2021/08/24
     * ##param id_C 公司id
     * ##param id_O 订单id
     * ##param index 下标
     * ##param bcdStatus 状态
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse setActionStatus(String id_C, String id_O, Integer index, Integer bcdStatus);


    /**
     * 新增/修改指定的卡片数组对象
     * @Author Rachel
     * @Data 2021/09/02
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_O 订单id
     * ##param card 新增/修改哪张卡片
     * ##param objName 卡片下的数组
     * ##param index 数组下标
     * ##param content 新增/修改的内容
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse setOItemRelated(String id_U, String id_C, String listType, String grp, String id_O, String card, String objName, Integer index, JSONObject content);

    /**
     * 获取指定的卡片数组对象
     * @Author Rachel
     * @Data 2021/09/02
     * ##param id_O 订单id
     * ##param card 查询哪张卡片
     * ##param objName 卡片下的数组
     * ##param index 数组下标
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse getOItemRelated(String id_U, String id_C, String listType, String grp, String id_O, String card, String objName, Integer index);


    /**
     * 判断是否真公司
     * @Author Rachel
     * @Data 2021/09/14
     * ##param id_C 公司id
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse checkBcdNet(String id_C);
//
//    /**
//     * 统计指定的时间范围内生产数量总数
//     * @Author Rachel
//     * @Data 2021/10/06
//     * ##param id_C 公司id
//     * ##param startDate 开始时间
//     * ##param endDate 结束时间
//     * ##param dateGroup 日期分组: SECOND:秒 / MINUTE:分 / HOUR:时 / DAY:日 / WEEK:周 / MONTH:月 / QUARTER:季 / YEAR:年
//     * ##param groupKey 类型分组键名：id_O / id_P / grpU
//     * ##param groupValue 类型分组值：groupKey对应的值
//     * @Return com.cresign.tools.apires.ApiResponse
//     * @Card
//     **/
//    ApiResponse statisticsSum(String id_C, String startDate, String endDate, String dateGroup, String groupKey, JSONArray groupValue) throws IOException;
//
//    /**
//     * 统计指定的时间范围内生产数量平均数
//     * @Author Rachel
//     * @Data 2021/10/06
//     * ##param id_C 公司id
//     * ##param startDate 开始时间
//     * ##param endDate 结束时间
//     * ##param second 分组秒数: 1:秒 / 60:分 / 3600:时
//     * ##param groupKey 类型分组键名：id_O / id_P / grpU
//     * ##param groupValue 类型分组值：groupKey对应的值
//     * @Return com.cresign.tools.apires.ApiResponse
//     * @Card
//     **/
//    ApiResponse statisticsAvg(String id_C, String startDate, String endDate, Integer second, String groupKey, JSONArray groupValue) throws IOException;

    /**
     * 从一个账户转账到另一个账户
     * @Author Rachel
     * @Date 2021/11/24
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param fromId_A 转账前的账户id
     * ##param toId_A 转账后的账户id
     * ##param money 金额
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse moveMoney(String id_U, String id_C, String listType, String grp, String fromId_A, String toId_A, Double money);

    /**
     * 从订单取钱到账户
     * @Author Rachel
     * @Date 2021/11/25
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_A 账户id
     * ##param id_O 订单id
     * ##param money 金额
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse pushMoney(String id_U, String id_C, String listType, String grp, String id_A, String id_O, Double money);

    /**
     * 从账户打钱进订单
     * @Author Rachel
     * @Date 2021/11/25
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_A 账户id
     * ##param id_O 订单id
     * ##param money 金额
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse popMoney(String id_U, String id_C, String listType, String grp, String id_A, String id_O, Double money);

    /**
     * 注入资产
     * @Author Rachel
     * @Date 2021/12/10
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param ref 资源组别
     * ##param id_A 资产id
     * ##param id_P 产品id
     * ##param seq 执行状态
     * ##param lCR 货币
     * ##param lUT 单位
     * ##param wn2qtyneed 产品数量
     * ##param wn4price 产品单价
     * ##param wn0prior 执行次序
     * ##param locAddr 货架
     * ##param locSpace 货架格子数组
     * ##param spaceQty 货架格子存放数量数组
     * @Return java.lang.Object
     * @Card
     **/
//    ApiResponse addAsset(String id_U, String id_C, String listType, String grp, String ref, String id_A, String id_P, Integer seq, Integer lCR, Integer lUT,
//                    Double wn2qtyneed, Double wn4price, Integer wn0prior, String locAddr, JSONArray locSpace, JSONArray spaceQty) throws IOException;

    /**
     * 新增个人公司
     * @Author Rachel
     * @Date 2021/12/10
     * ##param id_U 用户id
     * ##param reqJson
     * @Return java.lang.Object
     * @Card
     **/
    ApiResponse addMySpace(String id_U, JSONObject reqJson) throws IOException;


    /**
     * cTrigger开启 / 关闭
     * @Author Rachel
     * @Date 2021/12/13
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param ref cTrigger的key
     * ##param activate 该cTrigger是否开启
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse cTriggerToTimeflow(String id_U, String id_C, String listType, String grp, Integer index, Boolean activate) throws IOException;

    /**
     * 获取产品的工序，部件，物料三个数组
     * @Author Rachel
     * @Date 2022/03/02
     * @Param id_P 产品id
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse prodPart(String id_P);


    /**
     * 连接真公司
     * @Author Rachel
     * @Date 2022/03/14
     * @Param id_C
     * @Param id_CB
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse connectionComp(String id_C, String id_CB) throws IOException;

    /**
     * 连接真产品
     * @Author Rachel
     * @Date 2022/03/14
     * @Param id_C
     * @Param id_P
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse connectionProd(String id_C, String id_P) throws IOException;

    /**
     * copy卡片到另外的id
     * @Author Rachel
     * @Date 2022/03/14
     * @Param fromId
     * @Param toId
     * @Param card
     * @Param table
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
//    ApiResponse copyCard(String fromId, JSONArray toId, JSONArray card, String table);

//    Double updateFinish(JSONArray arrayObjItem, JSONArray arrayObjStock);

    /**
     * 移动订单产品到另一张订单
     * @Author Rachel
     * @Date 2022/03/18
     * @Param id_C
     * @Param fromId_O
     * @Param toId_O
     * @Param fromIndex
     * @Param toIndex
     * @Param wn2qtynow
     * @Return java.lang.Object
     * @Card
     **/
//    ApiResponse moveOStock(String id_C, String fromId_O, String toId_O, Integer fromIndex, Integer toIndex, Double wn2qtynow);

    /**
     * 合并订单
     * @Author Rachel
     * @Date 2022/04/10
     * @Param id_O 主订单id
     * @Param arrayMergeId_O merge订单id
     * @Param index 主订单index
     * @Param arrayMergeIndex
     * @Return java.lang.Object
     * @Card
     **/
//    ApiResponse mergeOrder(String id_O, JSONArray arrayMergeId_O, Integer index, JSONArray arrayMergeIndex);


    /**
     * 拆分订单
     * @Author Rachel
     * @Date 2022/04/20
     * @Param id_O 订单id
     * @Param arrayIndex 下标数组
     * @Return java.lang.Object
     * @Card
     **/
    ApiResponse splitOrder(String id_O, JSONArray arrayIndex);



}
