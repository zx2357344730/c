package com.cresign.timer.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.common.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ##author: tangzejin
 * ##updated: 2019/6/28
 * ##version: 1.0.0
 * ##description: excel总体工具类
 */
public class ExcelTang {

    /**
     * 生成excel方法
     * ##Params: titles	表格头部数据
     * ##Params: rows	表格体数据
     * ##Params: path	路径
     * ##Params: name	excel名称
     * ##Params: mergeTitle	页签名称
     * ##Params: pagingRow 分页行
     * ##return: java.lang.String  返回结果: 生成结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:25
     */
    private String getExcelData(List<String> titles
            , List<List<String>> rows
            , String path
            , String name, String mergeTitle,List<Integer> pagingRow){

        //创建excel数据存储类
        ExcelData data = new ExcelData();

        //页签名称
        data.setName(mergeTitle);

        //设置头部数据
        data.setTitles(titles);

        //设置行数据
        data.setRows(rows);

        List<String> excelFormat = new ArrayList<>();

        excelFormat.add(Constants.ADD_FORM_BODY);

        data.setExcelFormat(excelFormat);

        List<Map<String,Object>> dataKey = new ArrayList<>();

        Map<String,Object> map = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        map.put(Constants.ADD_IS_FIXED,true);
        map.put(Constants.ADD_PAGING_ROW,pagingRow);
        dataKey.add(map);

//        dataKey.add(null);

        data.setDataKey(dataKey);

        //创建url存储生成结果路径
        String url = null;
        try{

            //生成excel表格
            url = ExcelUtils.generateExcel(data,path,name);
        } catch (Exception e /*捕捉一切异常*/) {

            //在控制台打印出异常种类，错误信息和出错位置等
            e.printStackTrace();
        }

        //返回生成路径
        return url;
    }

    /**
     * 生成excel
     * ##Params: rows	数据体
     * ##Params: title	需要显示的头部值
     * ##Params: path	路径
     * ##Params: name	excel名称
     * ##Params: mergeTitle	页签名称
     * ##Params: pagingRow	分页行
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:26
     */
    public String getExcelAndDirect(List<List<String>> rows
            , List<String> title
            , String path, String name,String mergeTitle,List<Integer> pagingRow){

        // 返回excel生成路径
        return getExcelData(title,rows,path,name,mergeTitle,pagingRow);
    }

    /**
     * 用来递归json数据并处理json的结果
     * ##Params: map	用来存储object处理结果
     * ##Params: object	json数据
     * ##Params: layer	层次
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:28
     */
    private static void getDgF(Map<String,Object> map,JSONObject object, int layer){

        // 层数加一
        layer++;

        //遍历json所有键
        for (String str : object.keySet()) {

            //根据当前键获取键对应的json数据
            Object o = object.get(str);

            // 判断o不为空
            if (o != null) {

                // 继续调用递归方法
                ExcelTang.getDgI(o,map,layer,str);
            }
        }
    }

    /**
     * 用来处理数据的json
     * ##Params: map	mapJson对象
     * ##Params: array	数组json对象
     * ##Params: layer	层次
     * ##Params: key	普通键
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:28
     */
    private static void getDgArray(Map<String,Object> map, JSONArray array, int layer, String key){

        // 层数加一
        layer++;

        // 定义数组下标
        int index = 0;

        //遍历数组
        for (int i = 0; i < array.size(); i++){
//        for (Object o : array) {

            // 判断o不为空
            if (array.get(i) != null) {

                // 继续调用递归方法
                ExcelTang.getDgI(array.get(i),map,layer,key+index);

                // 下标加一
                index++;
            }
        }
    }

    /**
     * 用来递归判断o属于什么数据
     * ##Params: o	数据
     * ##Params: map	结果集合
     * ##Params: layer	层数
     * ##Params: key	键名
     * ##return: java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:28
     */
    private static Map<String,Object> getDgI(Object o, Map<String,Object> map, int layer, String key){

        //获取数组的值，并判断数组值是什么类型
        int dg = getDg(o);

        //1是数组类型
        if (dg == Constants.INT_ONE) {

            //将值转换为数组json
            JSONArray array2 = JSONArray.parseArray(o.toString());

            // 判断数组不为空
            if (array2 != null) {

                //继续处理数组
                getDgArray(map, array2, layer,key);
            }

            //2是对象类型
        } else if (dg == Constants.INT_TWO) {

            //键结果转换为对象json
            JSONObject object1 = JSONObject.parseObject(o.toString());

            // 判断对象不为空
            if (object1 != null) {

                //继续处理
                getDgF(map, object1, layer);
            }
        } else {

            //把集合添加回去
            map.put(key,o);
        }

        // 返回结果
        return map;
    }

    /**
     * 判断o属于json的哪种类型
     * ##Params: o	要判断的数据
     * ##return: int  返回结果: 结果：1：是数组，2：是对象，3：键值对
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:29
     */
    private static int getDg(Object o){

        //判断o是否是数组
        if (isArrayJson(o)) {

            //是返回1
            return 1;

        //判断o是否是对象
        } else if (isDuiJson(o)) {

            //是返回2
            return 2;
        } else {

            //否则就是键值对则返回0
            return 0;
        }
    }

    /**
     * 用来判断o是否是数组类型json
     * ##Params: o	json数据
     * ##return: boolean  返回结果: 结果：true是，false不是
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:29
     */
    private static boolean isArrayJson(Object o){

        //将o转换为String
        String json = o.toString();

        // 捕捉异常
        try {

            //将json转换为数组
            JSONArray.parseArray(json);

            //成功则是数组
            return true;
        } catch (Exception e) {

            //出错则不是数组
            return false;
        }
    }

    /**
     * 用来判断o是否是对象类型json
     * ##Params: o	json数据
     * ##return: boolean  返回结果: 结果:true是，false不是
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 15:29
     */
    private static boolean isDuiJson(Object o){

        //将o转换为String
        String json = o.toString();
        try {

            //将json转换为对象json
            JSONObject.parseObject(json);

            //成功则是对象类型
            return true;
        } catch (Exception e) {

            //出错则不是对象类型
            return false;
        }
    }

}
