//package com.cresign.tools.dbTools;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.tools.config.async.ActionEnum;
//import com.cresign.tools.exception.ErrorResponseException;
//import com.cresign.tools.pojo.po.Prod;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.AsyncResult;
//import org.springframework.stereotype.Component;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.Future;
//
///**
// * @author tang
// * @Description 作者很懒什么也没写
// * @ClassName QtAs
// * @Date 2023/9/23
// * @ver 1.0.0
// */
//@Component
//public class QtAs {
//    @Autowired
//    private Qt qt;
//
//    /**
//     * 递归验证核心方法
//     * @param pidList   零件id集合
//     * @param id_P  父零件id
//     * @param id_C  公司编号
//     * @param objectMap 下一个零件信息
//     * @param isRecurred    异常信息存储
//     * @param isEmpty   产品信息存储
//     * @param stat  ？
//     * @param id_Ps 递归所有id存储
//     */
//    public void checkUtilCore(JSONArray pidList, String id_P, String id_C
//            , JSONObject objectMap, JSONArray isRecurred
//            , JSONArray isEmpty, JSONObject stat, HashSet<String> id_Ps) {
//        try {
//            // 根据父编号获取父产品信息
//            Prod thisItem = qt.getMDContent(id_P, qt.strList("info", "part"), Prod.class);
////            System.out.println("thiItem" + thisItem);
//            // 层级加一
//            stat.put("layer", stat.getInteger("layer") + 1);
//
//            boolean isConflict = false;
//            JSONArray checkList = new JSONArray();
//
//            // 判断父产品不为空，部件父产品零件不为空
//            if (thisItem != null) {
//                for (int i = 0; i < pidList.size(); i++) {
////                System.out.println("冲突Check" + id_P);
//                    // 判断编号与当前的有冲突
//                    if (pidList.getString(i).equals(id_P)) {
//                        // 创建零件信息
//                        JSONObject conflictProd = new JSONObject();
//                        // 添加零件信息
//                        conflictProd.put("id_P", id_P);
//                        conflictProd.put("layer", (stat.getInteger("layer") + 1));
//                        conflictProd.put("index", i);
//                        // 添加到结果存储
//                        isRecurred.add(conflictProd);
//                        // 设置为有冲突
//                        isConflict = true;
//                        // 结束
//                        break;
//                    }
//                }
//
//                if (!isConflict) {
//                    checkList = (JSONArray) pidList.clone();
//                    checkList.add(id_P);
//                }
//
//                // 获取prod的part信息
//                if (!isConflict &&
//                        null != thisItem.getPart() &&
//                        thisItem.getInfo().getId_C().equals(id_C) &&
//                        null != thisItem.getPart().get("objItem")) {
//                    JSONArray nextItem = thisItem.getPart().getJSONArray("objItem");
////                    for (int i = 0; i < nextItem.size(); i++) {
////                        String id_p = nextItem.getJSONObject(i).getString("id_P");
////                        if (id_p.equals("62fcaae0cb15c454f3170c65")) {
////                            System.out.println("p:"+id_P);
////                        }
////                    }
//                    // 遍历零件信息1
//                    for (int j = 0; j < nextItem.size(); j++) {
//                        // 判断零件不为空并且零件编号不为空
//                        stat.put("count", stat.getInteger("count") + 1);
////                    System.out.println("count " + stat.getInteger("count"));
//                        if (null != nextItem.get(j) && null != nextItem.getJSONObject(j).get("id_P")) {
//
//                            // 继续调用验证方法
////                        System.out.println("判断无冲突" + isConflict);
//                            if (nextItem.getJSONObject(j).getDouble("wn4qtyneed") == null ||
//                                    nextItem.getJSONObject(j).getDouble("wn2qty") == null ||
//                                    nextItem.getJSONObject(j).getDouble("wn2port") == null) {
//                                if (null == objectMap) {
//                                    objectMap = new JSONObject();
//                                }
//                                System.out.println("为空-1");
//                                objectMap.put("errDesc", "数量为空！");
//                                isEmpty.add(objectMap);
//                            } else {
//                                String id_PNew = nextItem.getJSONObject(j).getString("id_P");
//                                if (id_Ps.contains(id_PNew)) {
//                                    continue;
//                                }
//                                id_Ps.add(id_PNew);
//                                checkUtilCore(checkList, id_PNew, id_C, nextItem.getJSONObject(j)
//                                        , isRecurred, isEmpty, stat, id_Ps);
//                            }
//                        } else {
//                            if (null != objectMap) {
//                                System.out.println("为空-2");
//                                objectMap.put("errDesc", "产品不存在！");
//                                isEmpty.add(objectMap);
//                            }
//                        }
//                    }
//                }
//            } else if (!id_P.equals("")) {
//                System.out.println("为空-3");
//                System.out.println("问题输出:"+id_P);
//                objectMap.put("errDesc", "产品不存在！");
//                isEmpty.add(objectMap);
//            }
//        } catch (Exception ex) {
//            System.out.println("出现异常:" + ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
//
//    /**
//     * 递归验证方法（JSONArray版本）
//     * @param id_Ps 递归所有id存储
//     * @param item  当前产品列表
//     * @param myCompId  公司编号
//     */
//    public void checkUtil(HashSet<String> id_Ps, JSONArray item, String myCompId){
//        for (int i = 0; i < item.size(); i++) {
//            String id_P = item.getJSONObject(i).getString("id_P");
//            if (id_Ps.contains(id_P)) {
//                continue;
//            }
//            // 创建异常信息存储
//            JSONArray isRecurred = new JSONArray();
//            // 创建产品信息存储
//            JSONArray isEmpty = new JSONArray();
//            // 创建零件id集合
//            JSONArray pidList = new JSONArray();
//            JSONObject nextPart = new JSONObject();
//
//            JSONObject stat = new JSONObject();
//            stat.put("layer", 0);
//            stat.put("count", 0);
//
//            // ******调用验证方法******
//            id_Ps.add(id_P);
//            checkUtilCore(pidList, id_P, myCompId, nextPart, isRecurred, isEmpty, stat, id_Ps);
//
//            if (isRecurred.size() > 0) {
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_RECURRED.getCode(), id_P);
//            }
//            if (isEmpty.size() > 0) {
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NOT_EXIST.getCode(), id_P);
//            }
//        }
//    }
//    /**
//     * 递归验证方法（List<JSONObject>版本）
//     * @param id_Ps 递归所有id存储
//     * @param item  当前产品列表
//     * @param myCompId  公司编号
//     */
//    public void checkUtil(HashSet<String> id_Ps, List<JSONObject> item, String myCompId){
//        for (JSONObject object : item) {
//            String id_P = object.getString("id_P");
//            if (id_Ps.contains(id_P)) {
//                continue;
//            }
//            // 创建异常信息存储
//            JSONArray isRecurred = new JSONArray();
//            // 创建产品信息存储
//            JSONArray isEmpty = new JSONArray();
//            // 创建零件id集合
//            JSONArray pidList = new JSONArray();
//            JSONObject nextPart = new JSONObject();
//
//            JSONObject stat = new JSONObject();
//            stat.put("layer", 0);
//            stat.put("count", 0);
//
//            // ******调用验证方法******
//            id_Ps.add(id_P);
//            checkUtilCore(pidList, id_P, myCompId, nextPart, isRecurred, isEmpty, stat, id_Ps);
//
//            if (isRecurred.size() > 0) {
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_RECURRED.getCode(), id_P);
//            }
//            if (isEmpty.size() > 0) {
//                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NOT_EXIST.getCode(), id_P);
//            }
//        }
//    }
//
//    /**
//     * 开启多线程递归产品方法（JSONArray版本）
//     * @param id_Ps 递归所有id存储
//     * @param item  当前产品列表
//     * @param myCompId  公司编号
//     * @return  线程处理结果
//     */
//    @Async
//    public Future<String> testResult(HashSet<String> id_Ps, JSONArray item, String myCompId) {
//        Future<String> future;
//        try {
//            // 调用递归验证方法
//            checkUtil(id_Ps,item,myCompId);
//            future = new AsyncResult<>("success:");
//        } catch(IllegalArgumentException e){
//            future = new AsyncResult<>("error-IllegalArgumentException");
//        }
//        System.out.println("--- "+getThreadId()+" ---");
//        return future;
//    }
//    /**
//     * 开启多线程递归产品方法（List<JSONObject>版本）
//     * @param id_Ps 递归所有id存储
//     * @param item  当前产品列表
//     * @param myCompId  公司编号
//     * @return  线程处理结果
//     */
//    @Async
//    public Future<String> testResult(HashSet<String> id_Ps, List<JSONObject> item, String myCompId) {
//        Future<String> future;
//        System.out.println("--- "+getThreadId()+" ---");
//        try {
//            // 调用递归验证方法
//            checkUtil(id_Ps,item,myCompId);
//            future = new AsyncResult<>("success:");
//        } catch(IllegalArgumentException e){
//            future = new AsyncResult<>("error-IllegalArgumentException");
//        }
//        System.out.println("+++ "+getThreadId()+" +++");
//        return future;
//    }
//
//    /**
//     * 获取当前线程id
//     * @return  线程id
//     */
//    public long getThreadId(){
//        Thread curr = Thread.currentThread();
//        return curr.getId();
//    }
//}
package com.cresign.action.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.config.async.ActionEnum;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Prod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName QtAs
 * @Date 2023/9/23
 * @ver 1.0.0
 */
@Component
public class DgCheckUtil {
    @Autowired
    private Qt qt;

    /**
     * 递归验证核心方法
     * @param pidList   零件id集合
     * @param id_P  父零件id
     * @param id_C  公司编号
     * @param objectMap 下一个零件信息
     * @param isRecurred    异常信息存储
     * @param isEmpty   产品信息存储
     * @param stat  ？
     * @param id_Ps 递归所有id存储
     */
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
    }

    /**
     * 递归验证方法（JSONArray版本）
     * @param id_Ps 递归所有id存储
     * @param item  当前产品列表
     * @param myCompId  公司编号
     */
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
    /**
     * 递归验证方法（List<JSONObject>版本）
     * @param id_Ps 递归所有id存储
     * @param item  当前产品列表
     * @param myCompId  公司编号
     */
    public void checkUtil(HashSet<String> id_Ps, List<JSONObject> item, String myCompId){
        for (JSONObject object : item) {
            String id_P = object.getString("id_P");
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

    /**
     * 开启多线程递归产品方法（JSONArray版本）
     * @param id_Ps 递归所有id存储
     * @param item  当前产品列表
     * @param myCompId  公司编号
     * @return  线程处理结果
     */
    @Async
    public Future<String> testResult(HashSet<String> id_Ps, JSONArray item, String myCompId) {
        Future<String> future;
        try {
            // 调用递归验证方法
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("--- "+getThreadId()+" ---");
        return future;
    }
    /**
     * 开启多线程递归产品方法（List<JSONObject>版本）
     * @param id_Ps 递归所有id存储
     * @param item  当前产品列表
     * @param myCompId  公司编号
     * @return  线程处理结果
     */
    @Async
    public Future<String> testResult(HashSet<String> id_Ps, List<JSONObject> item, String myCompId) {
        Future<String> future;
        System.out.println("--- "+getThreadId()+" ---");
        try {
            // 调用递归验证方法
            checkUtil(id_Ps,item,myCompId);
            future = new AsyncResult<>("success:");
        } catch(IllegalArgumentException e){
            future = new AsyncResult<>("error-IllegalArgumentException");
        }
        System.out.println("+++ "+getThreadId()+" +++");
        return future;
    }

    /**
     * 获取当前线程id
     * @return  线程id
     */
    public long getThreadId(){
        Thread curr = Thread.currentThread();
        return curr.getId();
    }
}
