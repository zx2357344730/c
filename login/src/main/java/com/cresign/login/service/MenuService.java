package com.cresign.login.service;

import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.assetCard.MainMenuBO;
import com.cresign.tools.pojo.po.assetCard.SubMenuBO;

import java.io.IOException;
import java.util.List;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020-12-26 11:33
 * ##version: 1.0
 */
public interface MenuService {


    /**
     * 获得菜单和子菜单数据
     * ##author: JackSon
     * ##Params: id_U      用户id
     * ##Params: id_C      公司id
     * ##version: 1.0
     * ##updated: 2021-03-04 16:10
     * ##Return: java.lang.String
     */
    ApiResponse getMenusAndSubMenus(String id_C);

    /**
     * 通过grp在主菜单编辑页面中获取该职位的主菜单和子菜单数据
     * ##author: JackSon
     * ##Params: id_U          用户id
     * ##Params: id_C          公司id
     * ##Params: grpU          职位
     * ##version: 1.0
     * ##updated: 2020-12-26 11:37
     * ##Return: java.lang.String
     */
    ApiResponse getGrpUForMenusInRole(String id_C, String grpU);

    /**
     *  当前菜单数组的限定组别
     * @author: Jevon
     * ##param id_U
     * ##param id_C
     * ##param ref       菜单编号
     * ##param grpType   类型（grp/grpProd）
     * @version: 1.0
     * @createDate: 2021/7/8 15:04
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse getMenuGrp(String id_C,String ref, String grpType);



    /**
     * 获取菜单数据
     * ##author: JackSon
     * ##Params: id_U      用户id
     * ##Params: id_C      公司id
     * ##version: 1.0
     * ##updated: 2020-12-30 13:47
     * ##Return: java.lang.String
     */
    ApiResponse getMenuListByGrpU(String id_C, String grpU) throws IOException;



    /**
     * 在主菜单编辑页面中修改该职位的主菜单和子菜单数据
     * ##author: JackSon
     * ##Params: id_U          用户id
     * ##Params: id_C          公司id
     * ##Params: grpU          职位
     * ##version: 1.0
     * ##updated: 2020-12-28 10:38
     * ##Return: java.lang.String
     */
    ApiResponse updateMenuData(String id_U, String id_C, String grpU, List<MainMenuBO> mainMenusData);


    /**
     * 获取子菜单数据
     * ##author: JackSon
     * ##Params: id_U       用户id
     * ##Params: id_C       公司id
     * ##version: 1.0
     * ##updated: 2020-12-30 8:42
     * ##Return: java.lang.String
     */
    ApiResponse getSubMenusData(String id_C);

    /**
     * 更新子菜单数据api
     * ##author: JackSon
     * ##Params: id_U          用户id
     * ##Params: id_C          公司id
     * ##Params: subMenuBOS    更新的数据
     * ##version: 1.0
     * ##updated: 2020-12-29 8:57
     * ##Return: java.lang.String
     */
    ApiResponse updateSubMenuData(String id_U, String id_C, List<SubMenuBO> subMenuBOS);


    /**
     * 获取该公司def的listType
     * ##author: JackSon
     * ##Params: id_C
     * ##version: 1.0
     * ##updated: 2021-02-01 9:49
     * ##Return: java.lang.String
     */
    ApiResponse getDefListType(String id_C);



//    /**
//     * 检查子菜单是否有主菜单在使用
//     * ##author: JackSon
//     * ##Params: id_U
//     * ##Params: id_C
//     * ##Params: ref
//     * ##version: 1.0
//     * ##updated: 2021/5/13 11:21
//     * ##Return: com.cresign.tools.apires.ApiResponse
//     */
//    ApiResponse checkSubMenuUse(String id_U, String id_C, String ref);
//
//    /**
//     * 删除子菜单
//     * ##author: JackSon
//     * ##Params: id_U
//     * ##Params: id_C
//     * ##Params: ref    子菜单的ref
//     * ##version: 1.0
//     * ##updated: 2021/5/7 12:21
//     * ##Return: com.cresign.tools.apires.ApiResponse
//     */
//    ApiResponse delSubMenu(String id_U, String id_C, String ref);


}
