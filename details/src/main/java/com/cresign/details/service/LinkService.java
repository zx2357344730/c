package com.cresign.details.service;

import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

public interface LinkService {


    /**
     * 链接公司  ， 放ES
     * ##author: Jevon
     * ##Params: id_U  用户id
     * ##Params: id_C  公司id
     * ##Params: id_CB 对方公司id
     * ##Params: grp   组别
     * ##Params: grpB  组别
     * ##Params: listType  列表
     * ##version: 1.0
     * ##updated: 2021/1/15 16:43
     * ##Return: java.lang.String
     */
    ApiResponse setCompLink(String id_U, String id_C, String id_CB, String grp, String grpB, String listType) throws IOException;


    /**
     * 链接别人的产品
     * @author: Jevon
     * ##param id_U  用户id
     * ##param id_C  自己公司id
     * ##param id_P  别人产品id
     * ##param grp   自己组别（校验权限）
     * @version: 1.0
     * @createDate: 2021/6/8 10:34
     * @return: com.cresign.tools.apires.ApiResponse
     */
    ApiResponse setProdLink(String id_U, String id_C, String id_P, String grp) throws IOException;



    /**
     * 产品与零件互转
     * ##author: Jevon
     * ##Params: id_U
     * ##Params: id_C
     * ##Params: grp
     * ##Params: listType
     * ##Params: id_P
     * ##version: 1.0
     * ##updated: 2021/4/8 10:39
     * ##Return: java.lang.String
     */
    ApiResponse updateProdListType(String id_U, String id_C, String grp, String listType, String id_P) throws IOException;


}
