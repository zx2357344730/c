package com.cresign.tools.pojo.po.assetCard;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 前端传入的主菜单json
 * ##description:
 * ##author: JackSon
 * ##updated: 2020-12-28 10:51
 * ##version: 1.0
 */
@Data
public class MainMenuBO {

    // 主菜单唯一标识
    private String ref;

    // 主菜单名称
    private Map<String, Object> wrdN;

    // 子菜单
    private List<String> subMenus;

}