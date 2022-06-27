package com.cresign.login.service;

import com.alibaba.fastjson.JSONArray;
import com.cresign.tools.apires.ApiResponse;

/**
 * ##description: role权限的结构
 * ##author: JackSon
 * ##updated: 2020-12-26 11:25
 * ##version: 1.0
 */
public interface RoleService {


    /**
     * 获取role的数据
     * ##author: JackSon
     * ##Params: id_U      用户id
     * ##Params: id_C      公司id
     * ##version: 1.0
     * ##updated: 2021-03-04 16:16
     * ##Return: java.lang.String
     */
    ApiResponse getRole(String id_U, String id_C, String grpU);

    /**
     * 查看role数据
     * ##author: JackSon
     * ##Params: id_U          用户id
     * ##Params: id_C          公司id
     * ##Params: listType      列表类型
     * ##Params: grp           组别
     * ##Params: grpU          职位
     * ##version: 1.0
     * ##updated: 2020-12-16 15:27
     * ##Return: java.lang.String
     */
    ApiResponse getRoleData(String id_U, String id_C, String listType, String grp, String grpU);

    /**
     * 修改role的权限读写或者不能看
     * ##author: JackSon
     * ##Params: id_U         用户id
     * ##Params: id_C         公司id
     * ##Params: listType     列表类型
     * ##Params: grp          组别
     * ##Params: dataName     card名称或者batch名称，取决于authType
     * ##Params: authType     要修改的类型(card,batch,logType)
     * ##Params: auth         权限(0:不可看,1:可读,2:写)
     * ##Params: grpU         用户的职位
     * ##version: 1.0
     * ##updated: 2020-12-17 9:12
     * ##Return: java.lang.String
     */
    ApiResponse updateRole(String id_U, String id_C, String listType, String grp, String dataName, String authType, Integer auth, String grpU);


    /**
     * 获取该公司的每个列表类型的的分组
     * ##author: JackSon
     * ##Params: id_U
     * ##Params: id_C
     * ##version: 1.0
     * ##updated: 2021-01-16 9:44
     * ##Return: java.lang.String
     */
    ApiResponse getListTypeGrp(String id_U, String id_C);

    /**
     * 创建 新组别 使用该方法，会赋予卡片和按钮权限
     * @author: Jevon
     * ##param id_U
     * ##param id_C
     * ##param listType
     * ##param grp
     * ##param grpU
     * @version: 1.0
     * @createDate: 2021/7/5 9:06
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse getRoleDataByGrpUAndGrp(String id_U, String id_C, String listType, String grp, String grpU);

    /**
     * 更新最新角色的卡片和按钮权限,不可修改原有参数（比如他本来改好这个卡片是可读的，重置时按照用户原来参数）
     * @author: Jevon
     * ##param id_U
     * ##param id_C
     * ##param listType
     * ##param grp
     * ##param grpU
     * @version: 1.0
     * @createDate: 2021/7/5 9:15
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse updateNewestRole(String id_U, String id_C, String listType, String grp, String grpU);


    /**
     * 批量修改组别的写读
     * ##author: JackSon
     * ##Params: id_U
     * ##Params: id_C
     * ##Params: listType
     * ##Params: grp
     * ##Params: upAuth
     * ##Params: upType
     * ##Params: grpU
     * ##version: 1.0
     * ##updated: 2021-02-05 10:32
     * ##Return: java.lang.String
     */
    ApiResponse upRoleOfAuth(String id_U, String id_C, String listType, String grp, Integer upAuth, String upType, String grpU);


    /**
     * 拷贝某一个组别的权限到其他组别上
     * ##author: JackSon
     * ##Params: id_U          用户id
     * ##Params: id_C          公司id
     * ##Params: listType      列表类型
     * ##Params: copy_grp      拷贝组别
     * ##Params: to_grp        目标组别
     * ##version: 1.0
     * ##updated: 2021-02-05 14:13
     * ##Return: java.lang.String
     */
    ApiResponse copyGrpRoleToOtherGrp(String id_U, String id_C, String listType, String copy_grp, JSONArray to_grp, String grpU);

    ApiResponse copyGrpU(String id_U, String id_C, String to_grpU, String grpU);

}
