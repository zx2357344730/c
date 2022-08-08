package com.cresign.details.service;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;
import java.util.List;

/**
 * 群聊
 * ##description:
 * ##author: Jevon
 * ##updated: 2021-05-20 14:30
 * ##version: 1.0
 */
public interface GroupService {

    /**
     * 添加群
     * ##author: Jevon
     * ##Params: id_U  用户id
     * ##Params: id_C  公司id
     * ##Params: requestJson     群信息
     * ##version: 1.0
     * ##updated: 2021/3/2 15:56
     * ##Return: java.lang.String
     */
    ApiResponse addFC (String id_U, String id_C, JSONObject requestJson) throws IOException;

    /**
     * 删除群
     * ##author: Jevon
     * ##Params: id_U 用户id
     * ##Params: id_C  公司id
     * ##Params: id    群id
     * ##version: 1.0
     * ##updated: 2021/3/2 16:05
     * ##Return: java.lang.String
     */
    ApiResponse deleteGroup (String id_U, String id_C, String id);


    /**
     * 修改群
     * ##author: Jevon
     * ##Params: id_U  用户id
     * ##Params: id_C  公司id
     * ##Params: requestJson   群信息
     * ##version: 1.0
     * ##updated: 2021/3/2 15:57
     * ##Return: java.lang.String
     */
    ApiResponse updateGroup(String id_A, String id_U, String id_C,JSONObject requestJson) throws IOException;

    /**
     * 添加符合objUser方法
     * ##author: Jevon
     * ##Params: id_C   公司id
     * ##Params: id     群id
     * ##Params: grpU   群职位
     * ##version: 1.0
     * ##updated: 2021/3/2 15:57
     * ##Return: void
     */
//    JSONArray setFlowControlUserList( String id_C, String id, JSONArray grpU) throws IOException;

    /**
     * 如果对方是真公司，用这个按钮发送在策划订单给对方共同策划
     * 1 把grp/grpB 改为 1001
     * 2 在对应的cusmsg群内发createTask
     * @param id_O
     * @param tokData
     * @return
     */
    ApiResponse orderRelease(String id_O, JSONObject tokData) throws IOException;


    /**
     * 获取当前公司对应类型和符合职位的聊天室   現在用這個
     * ##author: Jevon
     * ##Params: id_C      id_C
     * ##Params: id_U      用户id
     * ##Params: type      类型
     * ##version: 1.0
     * ##updated: 2020/9/26 11:32
     * ##Return: java.lang.String
     */
    ApiResponse getFlowControl(String id_C, String id_U, List<String> type);


    /**
     * 这个是辞退、离职，要把rolex的所有module卸下，和flowControl objMod 删了变更职位
     * @author: Jevon
     * ##param id_U        用户id
     * ##param id_C       公司id
     * ##param grpUTarget    职位
     * ##param uid          变更人
     * @version: 1.0
     * @createDate: 2021/7/14 9:23
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse changeUserGrp(String id_U,String id_C,String grpUTarget ,String uid) throws IOException;


    /**
     * 获取我关注的公司列表  group
     * @author: Jevon
     * ##Params: id_U          用户id
     * @version: 1.0
     * @createDate: 2021-05-17 9:10
     * ##Return: java.lang.String
     */
    ApiResponse getMyFavComp(String id_U);

    /**
     * 添加我关注的公司
     * @author: Jevon
     * ##Params: id_U          用户id
     * ##Params: id_C          关注公司id
     * @version: 1.0
     * @createDate: 2021-05-17 9:10
     * ##Return: java.lang.String
     */
    ApiResponse addFav(String id_U,String id_C);

    /**
     * 删除我关注的公司
     * @author: Jevon
     * ##Params: id_U          用户id
     * ##Params: id_C          关注公司id
     * @version: 1.0
     * @createDate: 2021-05-17 9:10
     * ##Return: java.lang.String
     */
    ApiResponse delFav(String id_U,String id_C);




    //ApiResponse changeUserGrp(String id_U,String id_C,String grp,String listType,String id) throws IOException;
    /**
     *  改组别  改成删除组别（1020），当改成1019模版时，添加Asset def.id_temp
     * ##author: Jevon
     * ##Params: id_U  用户id
     * ##Params: id_C  公司id
     * ##Params: grp   组别
     * ##Params: listType  列表
     * ##Params: id    要操作的id
     * ##version: 1.0
     * ##updated: 2021/4/9 9:44
     * ##Return: java.lang.String
     */
    ApiResponse changeAssetGrp(String id_U, String id_C, String grp, String listType, String id) throws IOException;

    ApiResponse changeProdGrp(String id_U,String id_C,String grp,String listType,String id) throws IOException;

    ApiResponse changeOrderGrp(String id_U,String id_C,String grp,String listType,String id) throws IOException;

    ApiResponse changeCompGrp(String id_U,String id_C,String grp,String listType,String id) throws IOException;




//    ApiResponse restoreUser(String id_U,String id_C,String uid) throws IOException;
    /**
     * 回退组别，把之前的1020组别改回来
     * ##author: Jevon
     * ##Params: id_U      用户id
     * ##Params: id_C      公司id
     * ##Params: listType   列表类型
     * ##Params: id          要操作的id
     * ##version: 1.0
     * ##updated: 2021/4/21 15:01
     * ##Return: java.lang.String
     */
    ApiResponse restoreAssetGrp(String id_U,String id_C,String listType,String id) throws IOException;

    ApiResponse restoreProdGrp(String id_U,String id_C,String listType,String id) throws IOException;

    ApiResponse restoreOrderGrp(String id_U,String id_C,String listType,String id) throws IOException;

    ApiResponse restoreCompGrp(String id_U,String id_C,String listType,String id) throws IOException;

    ApiResponse updateCascadeInfo(String id_O, JSONObject changes) throws IOException;



}
