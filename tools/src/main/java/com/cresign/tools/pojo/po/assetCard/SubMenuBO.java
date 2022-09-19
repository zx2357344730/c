package com.cresign.tools.pojo.po.assetCard;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * ##description:
 * @author JackSon
 * @updated 2020-12-29 8:55
 * @ver 1.0
 */
@Data
public class SubMenuBO {

    // 子菜单唯一标识
    private String ref;

    // 子菜单名称
    private Map<String, Object> wrdN;

    // 列表类型
    private String bmdlistType;

    private List<String> grpProd;

    private List<String> grpComp;


    // 可看组别
    private List<String> grp;

    // 可看列
    private List<String> col;

    // 筛选的列内容
    private List<JSONObject> colFilter;


}