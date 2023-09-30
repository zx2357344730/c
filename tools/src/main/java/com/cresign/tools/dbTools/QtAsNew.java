package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.config.async.ActionEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Prod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName QtAs
 * @Date 2023/9/23
 * @ver 1.0.0
 */
@Component
public class QtAsNew {
    @Autowired
    private Qt qt;

    private void checkUtilCore(JSONArray pidList, String id_P, String id_C
            , JSONObject objectMap, JSONArray isRecurred
            , JSONArray isEmpty, JSONObject stat, HashSet<String> id_Ps) {
        try {
            // 根据父编号获取父产品信息
            Prod thisItem = qt.getMDContent(id_P, qt.strList("info", "part"), Prod.class);
//            System.out.println("thiItem" + thisItem);
            // 层级加一
            stat.put("layer", stat.getInteger("layer") + 1);

            boolean isConflict = false;
            JSONArray checkList = new JSONArray();

            // 判断父产品不为空，部件父产品零件不为空
            if (thisItem != null) {
                for (int i = 0; i < pidList.size(); i++) {
//                System.out.println("冲突Check" + id_P);
                    // 判断编号与当前的有冲突
                    if (pidList.getString(i).equals(id_P)) {
                        // 创建零件信息
                        JSONObject conflictProd = new JSONObject();
                        // 添加零件信息
                        conflictProd.put("id_P", id_P);
                        conflictProd.put("layer", (stat.getInteger("layer") + 1));
                        conflictProd.put("index", i);
                        // 添加到结果存储
                        isRecurred.add(conflictProd);
                        // 设置为有冲突
                        isConflict = true;
                        // 结束
                        break;
                    }
                }

                if (!isConflict) {
                    checkList = (JSONArray) pidList.clone();
                    checkList.add(id_P);
                }

                // 获取prod的part信息
                if (!isConflict &&
                        null != thisItem.getPart() &&
                        thisItem.getInfo().getId_C().equals(id_C) &&
                        null != thisItem.getPart().get("objItem")) {
                    JSONArray nextItem = thisItem.getPart().getJSONArray("objItem");
//                    for (int i = 0; i < nextItem.size(); i++) {
//                        String id_p = nextItem.getJSONObject(i).getString("id_P");
//                        if (id_p.equals("62fcaae0cb15c454f3170c65")) {
//                            System.out.println("p:"+id_P);
//                        }
//                    }
                    // 遍历零件信息1
                    for (int j = 0; j < nextItem.size(); j++) {
                        // 判断零件不为空并且零件编号不为空
                        stat.put("count", stat.getInteger("count") + 1);
//                    System.out.println("count " + stat.getInteger("count"));
                        if (null != nextItem.get(j) && null != nextItem.getJSONObject(j).get("id_P")) {

                            // 继续调用验证方法
//                        System.out.println("判断无冲突" + isConflict);
                            if (nextItem.getJSONObject(j).getDouble("wn4qtyneed") == null ||
                                    nextItem.getJSONObject(j).getDouble("wn2qty") == null ||
                                    nextItem.getJSONObject(j).getDouble("wn2port") == null) {
                                if (null == objectMap) {
                                    objectMap = new JSONObject();
                                }
                                System.out.println("为空-1");
                                objectMap.put("errDesc", "数量为空！");
                                isEmpty.add(objectMap);
                            } else {
                                String id_PNew = nextItem.getJSONObject(j).getString("id_P");
                                if (id_Ps.contains(id_PNew)) {
                                    continue;
                                }
                                id_Ps.add(id_PNew);
                                checkUtilCore(checkList, id_PNew, id_C, nextItem.getJSONObject(j)
                                        , isRecurred, isEmpty, stat, id_Ps);
                            }
                        } else {
                            if (null != objectMap) {
                                System.out.println("为空-2");
                                objectMap.put("errDesc", "产品不存在！");
                                isEmpty.add(objectMap);
                            }
                        }
                    }
                }
            } else if (!id_P.equals("")) {
                System.out.println("为空-3");
                System.out.println("问题输出:"+id_P);
                objectMap.put("errDesc", "产品不存在！");
                isEmpty.add(objectMap);
            }
        } catch (Exception ex) {
            System.out.println("出现异常:" + ex.getMessage());
            ex.printStackTrace();
        }
//        if (null != thisItem) {
//            prodMap.put(id_P,thisItem);
//            for (int i = 0; i < pidList.size(); i++) {
////                System.out.println("冲突Check" + id_P);
//                // 判断编号与当前的有冲突
//                if (pidList.getString(i).equals(id_P)) {
//                    // 创建零件信息
//                    JSONObject conflictProd = new JSONObject();
//                    // 添加零件信息
//                    conflictProd.put("id_P", id_P);
//                    conflictProd.put("layer", (stat.getInteger("layer") + 1));
//                    conflictProd.put("index", i);
//                    // 添加到结果存储
//                    isRecurred.add(conflictProd);
//                    // 设置为有冲突
//                    isConflict = true;
//                    // 结束
//                    break;
//                }
//            }
//
//            if (!isConflict) {
//                checkList = (JSONArray) pidList.clone();
//                checkList.add(id_P);
//            }
//
//            // 获取prod的part信息
//            if (!isConflict &&
//                    null != thisItem.getPart() &&
//                    thisItem.getInfo().getId_C().equals(id_C) &&
//                    null != thisItem.getPart().get("objItem")) {
//                JSONArray nextItem = thisItem.getPart().getJSONArray("objItem");
//                // 遍历零件信息1
//                for (int j = 0; j < nextItem.size(); j++) {
//                    // 判断零件不为空并且零件编号不为空
//                    stat.put("count", stat.getInteger("count") + 1);
////                    System.out.println("count " + stat.getInteger("count"));
//                    if (null != nextItem.get(j) && null != nextItem.getJSONObject(j).get("id_P")) {
//
//                        // 继续调用验证方法
////                        System.out.println("判断无冲突" + isConflict);
//                        if (nextItem.getJSONObject(j).getDouble("wn4qtyneed") == null ||
//                                nextItem.getJSONObject(j).getDouble("wn2qty") == null ||
//                                nextItem.getJSONObject(j).getDouble("wn2port") == null) {
//                            objectMap.put("errDesc", "数量为空！");
//                            isEmpty.add(objectMap);
//                        } else {
//                            id_Ps.put(nextItem.getJSONObject(j).getString("id_P"),0);
//                            this.dgCheckUtil(checkList, nextItem.getJSONObject(j).getString("id_P"), id_C, nextItem.getJSONObject(j)
//                                    , isRecurred, isEmpty, stat,id_Ps,prodMap);
//                        }
//                    } else {
//                        if (null != objectMap) {
//                            objectMap.put("errDesc", "产品不存在！");
//                            isEmpty.add(objectMap);
//                        }
//                    }
//                }
//            }
//        } else if (!id_P.equals("")) {
//            objectMap.put("errDesc", "产品不存在！");
//            isEmpty.add(objectMap);
//        }
    }
    public void checkUtil(HashSet<String> id_Ps, JSONArray item, String myCompId){
        for (int i = 0; i < item.size(); i++) {
            String id_P = item.getJSONObject(i).getString("id_P");
            if (id_Ps.contains(id_P)) {
                continue;
            }
            // 创建异常信息存储
            JSONArray isRecurred = new JSONArray();
            // 创建产品信息存储
            JSONArray isEmpty = new JSONArray();
            // 创建零件id集合
            JSONArray pidList = new JSONArray();
            JSONObject nextPart = new JSONObject();

            JSONObject stat = new JSONObject();
            stat.put("layer", 0);
            stat.put("count", 0);

            // ******调用验证方法******
            id_Ps.add(id_P);
            checkUtilCore(pidList, id_P, myCompId, nextPart, isRecurred, isEmpty, stat, id_Ps);

            if (isRecurred.size() > 0) {
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_RECURRED.getCode(), id_P);
            }
            if (isEmpty.size() > 0) {
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NOT_EXIST.getCode(), id_P);
            }
        }
    }

    private <T> void mdManyUtilQuery(JSONArray id_Ps,List<T> list, Class<T> classType){
        List<T> mdContentMany = qt.getMDContentMany2(id_Ps , qt.strList("info", "part"), classType);
        list.addAll(mdContentMany);
    }

    public <T> List<T> mdManyUtil(JSONArray id_Ps, Class<T> classType){
        int forProd = id_Ps.size() / 6;
        List<T> list = new ArrayList<>();
        JSONArray item5 = new JSONArray();
        JSONArray item6 = new JSONArray();
        if (forProd > 5) {
            JSONArray item1 = new JSONArray();
            JSONArray item2 = new JSONArray();
            JSONArray item3 = new JSONArray();
            JSONArray item4 = new JSONArray();
            int lei = 0;
            for (int i = 0; i < forProd; i++) {
                item1.add(id_Ps.getString(lei));
                item2.add(id_Ps.getString(lei+1));
                item3.add(id_Ps.getString(lei+2));
                item4.add(id_Ps.getString(lei+3));
                item5.add(id_Ps.getString(lei+4));
                item6.add(id_Ps.getString(lei+5));
                lei+=6;
            }
            int jie = id_Ps.size()-(forProd*6);
            if (jie > 0) {
                if (jie == 1) {
                    item1.add(id_Ps.getString((forProd*6)));
                } else if (jie == 2) {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                } else if (jie == 3) {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                    item3.add(id_Ps.getString((forProd*6)+2));
                } else if (jie == 4) {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                    item3.add(id_Ps.getString((forProd*6)+2));
                    item4.add(id_Ps.getString((forProd*6)+3));
                } else {
                    item1.add(id_Ps.getString((forProd*6)));
                    item2.add(id_Ps.getString((forProd*6)+1));
                    item3.add(id_Ps.getString((forProd*6)+2));
                    item4.add(id_Ps.getString((forProd*6)+3));
                    item5.add(id_Ps.getString((forProd*6)+4));
                }
            }
            Future<String> future1 = testMdMany1(item1, list,classType);
            Future<String> future2 = testMdMany2(item2, list,classType);
            Future<String> future3 = testMdMany3(item3, list,classType);
            Future<String> future4 = testMdMany4(item4, list,classType);
            Future<String> future5 = testMdMany5(item5, list,classType);
//        Future<String> future6 = qtAs.testMdMany6(item6, dgProd);
            mdManyUtilQuery(item6, list,classType);
            while (true) {
                if (future1.isDone() && future2.isDone() &&
                        future3.isDone() && future4.isDone() && future5.isDone()
//                    && future6.isDone()
                ) {
                    break;
                }
            }
            System.out.println("大小:");
            System.out.println(list.size());
        } else {
            if (id_Ps.size() / 2 > 7) {
                boolean isAdd = true;
                for (int i = 0; i < id_Ps.size(); i++) {
                    if (isAdd) {
                        isAdd = false;
                        item5.add(id_Ps.getJSONObject(i));
                    } else {
                        isAdd = true;
                        item6.add(id_Ps.getJSONObject(i));
                    }
                }
                Future<String> future5 = testMdMany5(item5, list,classType);
                mdManyUtilQuery(item6, list,classType);
                while (true) {
                    if (future5.isDone()
//                    && future6.isDone()
                    ) {
                        break;
                    }
                }
                System.out.println("大小:");
                System.out.println(list.size());
            } else {
                mdManyUtilQuery(id_Ps, list,classType);
            }
        }
        return list;
    }

    @Async
    public Future<String> testResult1(HashSet<String> id_Ps, JSONArray item, String myCompId) {
        Future<String> future;
        try {
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 1 ---");
        return future;
    }

    @Async
    public Future<String> testResult2(HashSet<String> id_Ps,JSONArray item,String myCompId) {
        Future<String> future;
        try {
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 2 ---");
        return future;
    }

    @Async
    public Future<String> testResult3(HashSet<String> id_Ps,JSONArray item,String myCompId) {
        Future<String> future;
        try {
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch (Exception ex){
            future = new AsyncResult<>("success:");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("--- 3 ---");
        return future;
    }

    @Async
    public Future<String> testResult4(HashSet<String> id_Ps,JSONArray item,String myCompId) {
        Future<String> future;
        try {
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 4 ---");
        return future;
    }

    @Async
    public Future<String> testResult5(HashSet<String> id_Ps,JSONArray item,String myCompId) {
        Future<String> future;
        try {
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 5 ---");
        return future;
    }

    @Async
    public Future<String> testResult6(HashSet<String> id_Ps,JSONArray item,String myCompId) {
        Future<String> future;
        try {
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 6 ---");
        return future;
    }

//    @Async
//    public Future<String> testResult7(HashSet<String> id_Ps,JSONArray item,String myCompId) {
//        Future<String> future;
//        try {
//            checkUtil(id_Ps,item,myCompId);
//            future = new AsyncResult<>("success:");
//        } catch(IllegalArgumentException e){
//            future = new AsyncResult<>("error-IllegalArgumentException");
//        }
//        System.out.println("--- 7 ---");
//        return future;
//    }

    @Async
    public <T> Future<String> testMdMany1(JSONArray id_Ps,List<T> list, Class<T> classType){
        Future<String> future;
        try {
            mdManyUtilQuery(id_Ps, list,classType);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 1 ---");
        return future;
    }

    @Async
    public <T> Future<String> testMdMany2(JSONArray id_Ps,List<T> list, Class<T> classType){
        Future<String> future;
        try {
            mdManyUtilQuery(id_Ps, list,classType);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 2 ---");
        return future;
    }

    @Async
    public <T> Future<String> testMdMany3(JSONArray id_Ps,List<T> list, Class<T> classType){
        Future<String> future;
        try {
            mdManyUtilQuery(id_Ps, list,classType);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 3 ---");
        return future;
    }

    @Async
    public <T> Future<String> testMdMany4(JSONArray id_Ps,List<T> list, Class<T> classType){
        Future<String> future;
        try {
            mdManyUtilQuery(id_Ps, list,classType);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 4 ---");
        return future;
    }

    @Async
    public <T> Future<String> testMdMany5(JSONArray id_Ps,List<T> list, Class<T> classType){
        Future<String> future;
        try {
            mdManyUtilQuery(id_Ps, list,classType);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- 5 ---");
        return future;
    }

//    @Async
//    public Future<String> testMdMany6(JSONArray id_Ps,Map<String, Prod> dgProd){
//        Future<String> future;
//        try {
//            mdManyUtil(id_Ps, dgProd);
//            future = new AsyncResult<>("success:");
//        } catch(IllegalArgumentException e){
//            future = new AsyncResult<>("error-IllegalArgumentException");
//        }
//        System.out.println("--- 6 ---");
//        return future;
//    }
}
