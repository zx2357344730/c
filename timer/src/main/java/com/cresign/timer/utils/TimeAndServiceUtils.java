//package com.cresign.timer.utils;
//
//import com.cresign.tools.common.Constants;
//import com.cresign.tools.pojo.po.Comp;
//import com.cresign.tools.pojo.po.Summ;
//
//import java.util.*;
//
///**
// * ##author: tangzejin
// * ##updated: 2020/3/21
// * ##version: 1.0.0
// * ##description: 定时器和Service共用工具类
// */
//public class TimeAndServiceUtils {
//
//    /**
//     * 用于根据monthFullDay获取zonMap内最大长度的值
//     * ##Params: monthFullDay	一个月的日期
//     * ##Params: zonMap	打卡总结果信息存储
//     * ##return: java.lang.Integer  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/7 11:29
//     */
//    @SuppressWarnings("unchecked")
//    public static Integer getMaxLength(List<String> monthFullDay
//            ,Map<String,Summ> zonMap){
//        int zui = 0;
//
//        // 遍历键集合
//        for (int j = 0; j < monthFullDay.size(); j++){
//            // 根据键获取当天的结果记录
//            Summ mapList = zonMap.get(monthFullDay.get(j));
//
//            if (null != mapList && null != mapList.getData() && null != mapList.getData().get(Constants.GET_ALL_CHK_IN)) {
//                // 获取结果记录里的data数据
//                List<Map<String,Object>> maps = (List<Map<String,Object>>) mapList.getData().get(Constants.GET_ALL_CHK_IN);
//
//                // 循环data数据
//                for (int i = 0; i < maps.size(); i++){
//                    // 获取data的打卡时间
//                    List<Map<String, Object>> allChk = (List<Map<String, Object>>) maps.get(i).get(Constants.GET_CHK_TIME);
//
//                    // 判断打卡时间的长度是否大于zui
//                    if (allChk.size() > zui) {
//
//                        // 大于则赋值给zui
//                        zui = allChk.size();
//                    }
//                }
//            }
//        }
//        return zui;
//    }
//
//    /**
//     * 处理map1表格头部
//     * ##Params: map1	初始表格头部
//     * ##Params: zui	最长打卡位数
//     * ##Params: title	新表格头部
//     * ##return: void  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/7 11:30
//     */
//    public static void getTemporaryTitleData(Map<String,String> map1
//            ,int zui,Map<String,String> title){
//        // 循环处理临时的表格头部
//        for (String str : map1.keySet()) {
//
//            // 判断是否等于这个键
//            if (Constants.GET_CHK_TIME.equals(str)) {
//
//                // 定义循环计数
//                int ji = 1;
//
//                // 根据zui循环创建表格头部
//                for (int i = 0; i < zui; i+= Constants.INT_TWO) {
//
//                    // 建立上班头部
//                    title.put(Constants.ADD_CHK_TIME+i,"上班"+(ji));
//
//                    // 建立下班头部
//                    title.put(Constants.ADD_CHK_TIME+(i+ Constants.INT_ONE),"下班"+(ji));
//                    ji++;
//                }
//            } else {
//
//                // 不等于则直接添加
//                title.put(str,map1.get(str));
//            }
//        }
//    }
//
//    /**
//     * 根据zui处理maps的data打卡时间补空
//     * ##Params: maps	打卡结果
//     * ##Params: zui	最长打卡位数
//     * ##return: void  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/7 11:30
//     */
//    @SuppressWarnings("unchecked")
//    public static void getData(List<Map<String,Object>> maps,int zui){
//        // 循环处理data
//        for (int m = 0; m < maps.size(); m++){
////        for (Map<String, Object> map : maps) {
//
//            // 获取所有打卡时间
//            List<Map<String, Object>> allChk = (List<Map<String, Object>>) maps.get(m).get(Constants.GET_CHK_TIME);
//
//            // 并删除所有打卡时间
//            maps.get(m).remove(Constants.GET_CHK_TIME);
//
//            // 循环zui
//            for (int j = 0; j < zui; j++) {
//
//                // 判断j是否小于总长度
//                if (j < allChk.size()) {
//
//                    // 不小于则直接添加
//                    maps.get(m).put(Constants.ADD_CHK_TIME + j, allChk.get(j).get(Constants.GET_DATE));
//                } else {
//
//                    // 小于则添加为空
//                    maps.get(m).put(Constants.ADD_CHK_TIME + j, Constants.STRING_EMPTY);
//                }
//            }
//        }
//    }
//
//    /**
//     * 判断打卡时间map获取s是否为空，为空list则添加空
//     * ##Params: map	打卡时间
//     * ##Params: s	键
//     * ##Params: list	行数据
//     * ##return: void  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/7 11:30
//     */
//    public static void isList(Map<String, Object> map,String s,List<String> list){
//        if (null != map.get(s)) {
//            // 获取键
//            String s1 = map.get(s).toString();
//
//            // 判断键是否为0
//            if (s1.equals(Constants.STRING_NUMBER_ZERO)
//                    || s1.equals(Constants.STRING_DOUBLE_ZERO)) {
//
//                // 为0则设置为空
//                list.add(Constants.STRING_EMPTY);
//
//                // 判断是否有小数点
//            } else if (s1.contains(Constants.SPOT)) {
//
//                // 有则将多余的小数点的0去掉
//                list.add(GenericUtils.getRemoveBehindZero(s1));
//            } else {
//
//                // 直接添加值
//                list.add(s1);
//            }
//        } else {
//            list.add(Constants.STRING_EMPTY);
//        }
//    }
//
//    /**
//     * 键map和list进行分组
//     * ##Params: map	打卡时间
//     * ##Params: group	分组存储
//     * ##Params: list	行数据
//     * ##return: void  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/7 11:30
//     */
//    public static void getGroup(Map<String, Object> map
//            ,Map<String,List<List<String>>> group
//            ,List<String> list){
//        Object o = map.get(Constants.GET_ID_U);
//        if (null != o) {
//            List<List<String>> lists = group.get(o.toString());
//            if (null == lists) {
//                lists = new ArrayList<>();
//            }
//            lists.add(list);
//            group.put(o.toString(),lists);
//        }
//    }
//
//    /**
//     * 根据参数生成excel表格，并返回生成结果路径
//     * ##Params: monthFullDay	一个月所有日期
//     * ##Params: zonMap	excel表格总数据
//     * ##Params: id	公司id
//     * ##Params: year	年份
//     * ##Params: month	月份
//     * ##Params: comp	公司信息
//     * ##return: java.lang.String  返回结果: 结果
//     * ##Author: tang
//     * ##version: 1.0.0
//     * ##Updated: 2020/8/7 11:31
//     */
//    @SuppressWarnings("unchecked")
//    public static String getExcel(List<String> monthFullDay, Map<String, Summ> zonMap, String id
//            , int year, int month, Comp comp){
//        // 创建Excel工具类
//        ExcelTang excelTang = new ExcelTang();
//
//        // 创建存储临时的表格头部
//        Map<String, String> map1 = new LinkedHashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
//
//        // 设置表格头部字符串
//        map1.put(Constants.ADD_REF_U, "编号");
//        map1.put(Constants.ADD_WCN_N, "用户名");
//        map1.put(Constants.ADD_WCN_N_REAL, "姓名");
//        map1.put(Constants.ADD_TMD, "考勤日期");
//        map1.put(Constants.ADD_WEEK, "星期");
//        map1.put(Constants.ADD_CHK_TIME, "时间");
//        map1.put(Constants.ADD_W_N_2_HR, "白班(时)");
//        map1.put(Constants.ADD_W_N_2_EX_HR, "平时加班(时)");
//        map1.put(Constants.ADD_W_N_2_WK_HR, "公休加班(时)");
//        map1.put(Constants.ADD_Z_N_1_LATE, "迟到(分)");
//        map1.put(Constants.ADD_LEAVE_EARLY, "早退(分)");
//        map1.put(Constants.ADD_Z_N_1_HOUR_LESS, "缺勤(时)");
//        map1.put(Constants.ADD_BC_D_ERROR, "状态");
//        map1.put(Constants.ADD_BCD_DIST, "位置");
//        map1.put("wcnLocation", "打卡地址");
//
//        // 创建表格头部存储
//        Map<String, String> title = new LinkedHashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
//
//        // 定义存储最多的打卡次数
//        int zui = TimeAndServiceUtils.getMaxLength(monthFullDay, zonMap);
//
//        // 创建表格内容存储
//        List<List<String>> rows = new ArrayList<>();
//
//        Map<String, List<List<String>>> group = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
//
//        // 遍历键集合
//        monthFullDay.forEach(zonStr -> {
//            // 根据键获取当前记录的结果
//            Summ mapList = zonMap.get(zonStr);
//
//            if (null != mapList && null != mapList.getData() && null != mapList.getData().get(Constants.GET_ALL_CHK_IN)) {
//                // 获取记录结果的data
//                List<Map<String, Object>> maps = (List<Map<String, Object>>) mapList.getData().get(Constants.GET_ALL_CHK_IN);
//
//                TimeAndServiceUtils.getTemporaryTitleData(map1, zui, title);
//
//                TimeAndServiceUtils.getData(maps, zui);
//
//                // 循环处理data
//                maps.forEach(map -> {
//                    // 创建存储集合
//                    List<String> list = new ArrayList<>();
//
//                    // 遍历表格头部键
//                    for (String s : title.keySet()) {
//
//                        // 判断键名
//                        switch (s) {
//                            case Constants.GET_TMD:
//
//                                // 直接添加值
//                                list.add(mapList.getTmk());
//                                break;
//                            case Constants.GET_WEEK:
//
//                                // 添加星期
//                                list.add(GenericUtils
//                                        .getWeekAndChinese(GenericUtils
//                                                .objToInteger(mapList.getData().get(Constants.GET_WEEK))));
//                                break;
//                            case Constants.GET_BC_D_ERROR:
//                            case Constants.GET_WCN_N:
//                            case Constants.GET_REF_U:
//                            case Constants.GET_BCD_DIST:
//                            case Constants.GET_WCN_N_REAL:
//                                if (null != map.get(s)) {
//                                    // 添加原本值
//                                    list.add(map.get(s).toString());
//                                } else {
//                                    list.add(Constants.STRING_EMPTY);
//                                }
//                                break;
//                            default:
//
//                                TimeAndServiceUtils.isList(map, s, list);
//                                break;
//                        }
//                    }
//                    TimeAndServiceUtils.getGroup(map, group, list);
//                });
//            }
//        });
//
//        List<Integer> pagingRow = new ArrayList<>();
//
//        group.keySet().forEach(g -> {
//            List<List<String>> lists = group.get(g);
//            rows.addAll(lists);
//            pagingRow.add(rows.size());
//        });
//
//        List<String> title2 = new ArrayList<>();
//        title.keySet().forEach(ti -> title2.add(title.get(ti)));
//        // 根据最终计算结果生成Excel表格
//        return excelTang.getExcelAndDirect(rows, title2, (id + "/sumChkin/")
//                , (year + "-" + GenericUtils.getBl(month)
//                        + "_" + comp.getInfo().getWrdN()
//                        + "_考勤"), "日出勤明细表", pagingRow);
//    }
//
//}
