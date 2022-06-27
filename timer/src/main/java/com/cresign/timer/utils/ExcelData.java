//package com.cresign.timer.utils;
//
//import lombok.Data;
//
//import java.io.Serializable;
//import java.util.List;
//import java.util.Map;
//
///**
// * ##author: tangzejin
// * ##updated: 2019/8/23
// * ##version: 1.0.0
// * ##description: excel实体类
// */
//@Data
//public class ExcelData implements Serializable {
//
//    private static final long serialVersionUID = 6133772627258154184L;
//
//    /**
//     * 表头数据存储
//     */
//    private List<String> titles;
//
//    /**
//     * 数据体数据存储
//     */
//    private List<List<String>> rows;
//
//    /**
//     * 页签名称
//     */
//    private String name = "abc";
//
//    /**
//     * 生成规则
//     */
//    private List<String> excelFormat;
//
//    /**
//     * 生成规则对应的要求
//     */
//    private List<Map<String,Object>> dataKey;
//}