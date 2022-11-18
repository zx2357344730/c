package com.cresign.login.service;

import com.alibaba.fastjson.JSONArray;
import com.cresign.tools.apires.ApiResponse;

/**
 * ##description: role权限的结构
 * @author JackSon
 * @updated 2020-12-26 11:25
 * @ver 1.0
 */
public interface RoleService {


    /**
     * 获取role的数据
     * @author JackSon
     * @param id_U      用户id
     * @param id_C      公司id
     * @ver 1.0
     * @updated 2021-03-04 16:16
     * @return java.lang.String
     */
//    ApiResponse getRole(String id_U, String id_C, String grpU);

    /**
     * 查看role数据
     * @author JackSon
     * @param id_U          用户id
     * @param id_C          公司id
     * @param listType      列表类型
     * @param grp           组别
     * @param grpU          职位
     * @ver 1.0
     * @updated 2020-12-16 15:27
     * @return java.lang.String
     */
//    ApiResponse getRoleData(String id_U, String id_C, String listType, String grp, String grpU);

    /**
     * 修改role的权限读写或者不能看
     * @author JackSon
     * @param id_U         用户id
     * @param id_C         公司id
     * @param listType     列表类型
     * @param grp          组别
     * @param dataName     card名称或者batch名称，取决于authType
     * @param authType     要修改的类型(card,batch,logType)
     * @param auth         权限(0:不可看,1:可读,2:写)
     * @param grpU         用户的职位
     * @ver 1.0
     * @updated 2020-12-17 9:12
     * @return java.lang.String
     */
    ApiResponse updateRole(String id_U, String id_C, String listType, String grp, String dataName, String authType, Integer auth, String grpU);


//    /**
//     * 获取该公司的每个列表类型的的分组
//     * @author JackSon
//     * @param id_U
//     * @param id_C
//     * @ver 1.0
//     * @updated 2021-01-16 9:44
//     * @return java.lang.String
//     */
//    ApiResponse getListTypeGrp(String id_U, String id_C);

    /**
     * 创建 新组别 使用该方法，会赋予卡片和按钮权限
     * @author Jevon
     * @param id_U
     * @param id_C
     * @param listType
     * @param grp
     * @param grpU
     * @ver 1.0
     * @createDate: 2021/7/5 9:06
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse getRoleDataByGrpUAndGrp(String id_U, String id_C, String listType, String grp, String grpU);

    /**
     * 更新最新角色的卡片和按钮权限,不可修改原有参数（比如他本来改好这个卡片是可读的，重置时按照用户原来参数）
     * @author Jevon
     * @param id_U
     * @param id_C
     * @param listType
     * @param grp
     * @param grpU
     * @ver 1.0
     * @createDate: 2021/7/5 9:15
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse updateNewestRole(String id_C, String listType, String grp, String grpU);


    /**
     * 批量修改组别的写读
     * @author JackSon
     * @param id_U
     * @param id_C
     * @param listType
     * @param grp
     * @param upAuth
     * @param upType
     * @param grpU
     * @ver 1.0
     * @updated 2021-02-05 10:32
     * @return java.lang.String
     */
    ApiResponse upRoleAllAuth(String id_U, String id_C, String listType, String grp, Integer upAuth, String upType, String grpU);


    /**
     * 拷贝某一个组别的权限到其他组别上
     * @author JackSon
     * @param id_U          用户id
     * @param id_C          公司id
     * @param listType      列表类型
     * @param copy_grp      拷贝组别
     * @param to_grp        目标组别
     * @ver 1.0
     * @updated 2021-02-05 14:13
     * @return java.lang.String
     */
    ApiResponse copyGrpRoleToOtherGrp(String id_U, String id_C, String listType, String copy_grp, JSONArray to_grp, String grpU);

    ApiResponse copyGrpU(String id_U, String id_C, String to_grpU, String grpU);

}
