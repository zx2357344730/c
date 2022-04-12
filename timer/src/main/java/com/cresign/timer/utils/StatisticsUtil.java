package com.cresign.timer.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StatisticsUtil {
    @Autowired
    private MongoTemplate mongoTemplate;


    //初始化
    public List<HashMap<String, Object>> initialTotalOCount(String id_O) {
        List<HashMap<String, Object>> objData = new LinkedList<>();
        //查询id_O和oItem.objItem.id_P的对象
        Criteria criteria = new Criteria();
        criteria.and("_id").is(id_O);
        criteria.and("oItem").exists(true);
        Query query = new Query(criteria);
        query.fields().include("oItem.objItem");
        Order orders = mongoTemplate.findOne(query, Order.class);
        if (orders == null) {
            return null;
        }
        List<Object> arr = (List<Object>) orders.getOItem().get("objItem");
        if (arr.size() == 0) {
            return null;
        } else {
            for (int k = 0; k < arr.size(); k++) {
                HashMap<String, Object> strHashMap = new HashMap<>();
                Map<String, Object> mongo = (Map<String, Object>) arr.get(k);
                strHashMap.put("id_P", mongo.get("id_P"));

                objData.add(strHashMap);
            }
        }
        return objData;
    }

    //累加统计
    public List<HashMap<String, Object>> totalOCount(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        for (int i = 0; i < headobjData.size(); i++) {

            HashMap<String, Object> strHashMap = new HashMap<>();

            JSONObject head = (JSONObject) JSON.toJSON(headobjData.get(i));

            Integer number = 0;


            //拿redis的num加给表头的num
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int j = 0; j < jsonArray.size(); j++) {
                    if (jsonArray.get(j).equals("totalOCount")) {
                        JSONObject redis = (JSONObject) jsonObject.get("data");
                        //结果集的id_P和当天完成数的id_P,相同的话，累加 num
                        if (head.get("id_P").equals(redis.get("id_P"))) {
                            Integer num = (Integer) redis.get("num");
                            number += num;
                        }
                    } else {
                        continue;
                    }
                }
            }
            //判断结果集里面有没有这个字段，有的话就不是初次，有就结果集的num和完成数的num  累加
            if (head.containsKey("num")) {
                Integer num = (Integer) head.get("num");
                number += num;
            }
            //这里累加
            //把每一个id_P的数据存放在一个数组对象中
            strHashMap.put("num", number);
            strHashMap.put("id_P", head.get("id_P"));

            objData.add(strHashMap);
        }
        return objData;

    }

    public List<HashMap<String, Object>> timingTotalOCount(List range, List<HashMap<String, Object>> headobjData, String id_O) {

        List<HashMap<String, Object>> objData = new LinkedList<>();

        for (int i = 0; i < headobjData.size(); i++) {

            HashMap<String, Object> strHashMap = new HashMap<>();

            JSONObject head = (JSONObject) JSON.toJSON(headobjData.get(i));

            Integer number = 0;

            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int j = 0; j < jsonArray.size(); j++) {
                    if (jsonArray.get(j).equals("totalOCount")) {
                        JSONObject redis = (JSONObject) jsonObject.get("data");
                        if (head.get("id_P").equals(redis.get("id_P"))) {
                            Integer num = (Integer) redis.get("num");
                            number += num;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (head.containsKey("num")) {
                Integer num = (Integer) head.get("num");
                number += num;
            }

            //查询id_O和oItem.objItem.id_P的对象
            Criteria criteria = new Criteria();
            criteria.and("_id").is(id_O).and("oItem.objItem.id_P").is(head.get("id_P"));//.and("oItem.objItem.$.id_P").is(head.get("id_P"))
            Query query = new Query(criteria);
            query.fields().include("oItem.objItem.$");
            Order orders = mongoTemplate.findOne(query, Order.class);
            if (orders == null) {
                continue;
            }
            List<Object> arr = (List<Object>) orders.getOItem().get("objItem");


            Integer wn4qtynow = null;

            for (int k = 0; k < arr.size(); k++) {
                Map<String, Object> jsonObj2 = (Map<String, Object>) arr.get(k);
                Map<String, Object> jsonObj3 = (Map<String, Object>) jsonObj2.get("objPart");

                //现在数量
                Integer total = (Integer) jsonObj3.get("wn4qtynow");
                //剩余完成数  总数  - 累计完成数
                //  800  =    1000 - 200
                wn4qtynow = total - number;
                Update update = new Update();
                //更改wn4qtynow字段  现在数量
                update.set("oItem.objItem.$.objPart.wn4qtynow", wn4qtynow);
                //修改现在数量键   wn4qtynow
                mongoTemplate.updateFirst(query, update, Order.class);
                strHashMap.put("num", number);
                strHashMap.put("wn4qtynow", wn4qtynow);
                strHashMap.put("id_P", head.get("id_P"));
                strHashMap.put("wn4qtyneed", jsonObj3.get("wn4qtyneed"));

                objData.add(strHashMap);
            }

        }
        return objData;

    }

    //初始化
    public List<HashMap<String, Object>> initialTotalOCount1() {
        List<HashMap<String, Object>> objData = new LinkedList<>();
        HashMap<String, Object> strHashMap = new HashMap<>();
        strHashMap.put("whole", 0);
        objData.add(strHashMap);
        return objData;
    }

    //定时
    public List<HashMap<String, Object>> timingtotalOCount1(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        HashMap<String, Object> strHashMap = new HashMap<>();

        for (int j = 0; j < headobjData.size(); j++) {

            JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));

            Integer wn0cntUser = 0;

            //其他日志类型
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int k = 0; k < jsonArray.size(); k++) {
                    if (jsonArray.get(k).equals("totalOCount1")) {
                        JSONObject redis = (JSONObject) jsonObject.get("data");
                        Integer num = (Integer) redis.get("wn0cntUser");
                        wn0cntUser += num;
                    } else {
                        continue;
                    }
                }
            }
            //判断结果集里面有没有这个字段，有的话就不是初次，  累加
            if (jsonObjData.containsKey("whole")) {
                Integer num = (Integer) jsonObjData.get("whole");
                wn0cntUser += num;
            }
            strHashMap.put("wn0cntUser", wn0cntUser);
            //strHashMap.put("logType", "totalOCount1");
            //strHashMap.put("logSumm", start);
            //strHashMap.put("logSumm", 1);
            objData.add(strHashMap);

        }
        return objData;
    }

    //初始化 crud
    public List<HashMap<String, Object>> initialcrud(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        //有表头时
        if (headobjData.size() > 0) {

            //遍历吧redis中的公司放set1中
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                set1.add((String) jsonObject.get("id_C"));
            }
            //遍历吧redis中的表头公司放set2中
            for (int j = 0; j < headobjData.size(); j++) {
                JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));
                set2.add((String) jsonObjData.get("id_C"));

            }
            //利用  差集来判断公司是否相同，如果有不相同的则添加进表头
            set1.removeAll(set2);

        } else {
            //没表头
            //遍历吧redis中的公司初始化
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");

                for (int k = 0; k < jsonArray.size(); k++) {
                    if (jsonArray.get(k).equals("crud")) {
                        set1.add((String) jsonObject.get("id_C"));
                    }
                }
            }
        }
        for (String str : set1) {
            HashMap<String, Object> strHashMap = new HashMap<>();
            strHashMap.put("id_C", str);
            strHashMap.put("wn0cntUser", 0);
            objData.add(strHashMap);
        }
        return objData;
    }

    //计算  crud
    public List<HashMap<String, Object>> crud(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        for (int j = 0; j < headobjData.size(); j++) {

            HashMap<String, Object> strHashMap = new HashMap<>();

            JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));
            Integer wn0cntUser = 0;

            HashSet<String> hashSet = new HashSet<>();
            //其他日志类型
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i).equals("crud")) {
                        if (jsonObjData.get("id_C").equals(jsonObject.get("id_C"))) {
                            wn0cntUser++;
                            hashSet.add((String) jsonObject.get("id_U"));
                        }
                    } else {
                        continue;
                    }
                }


            }
            //判断结果集里面有没有这个字段，有的话就不是初次，  累加
            if (jsonObjData.containsKey("wn0cntUser")) {
                Integer num = (Integer) jsonObjData.get("wn0cntUser");
                wn0cntUser += num;
            }
            if (jsonObjData.containsKey("id_U")) {
                JSONArray jsonArray = (JSONArray) jsonObjData.get("id_U");
                for (int i = 0; i < jsonArray.size(); i++) {
                    hashSet.add((String) jsonArray.get(i));
                }
            }

            strHashMap.put("wn0cntUser", wn0cntUser);
            strHashMap.put("id_C", jsonObjData.get("id_C"));
            strHashMap.put("id_U", hashSet);
            strHashMap.put("userNumber", hashSet.size());
            objData.add(strHashMap);
        }
        return objData;
    }

    //初始化  register
    public List<HashMap<String, Object>> initialregister(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        //有表头时
        if (headobjData.size() > 0) {

            //遍历吧redis中的公司放set1中
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                set1.add((String) jsonObject.get("id_C"));
            }
            //遍历吧redis中的表头公司放set2中
            for (int j = 0; j < headobjData.size(); j++) {
                JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));
                set2.add((String) jsonObjData.get("id_C"));
            }
            //利用  差集来判断公司是否相同，如果有不相同的则添加进表头
            set1.removeAll(set2);

        } else {
            //没表头
            //遍历吧redis中的公司初始化
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int k = 0; k < jsonArray.size(); k++) {
                    if (jsonArray.get(k).equals("register")) {
                        set1.add((String) jsonObject.get("id_C"));
                    }
                }
            }
        }
        for (String str : set1) {
            HashMap<String, Object> strHashMap = new HashMap<>();
            strHashMap.put("id_C", str);
            strHashMap.put("wn0cntUser", 0);
            objData.add(strHashMap);
        }
        return objData;
    }

    //计算  register
    public List<HashMap<String, Object>> register(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        for (int j = 0; j < headobjData.size(); j++) {

            HashMap<String, Object> strHashMap = new HashMap<>();
            JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));

            HashSet<String> hashSet = new HashSet<>();

            //其他日志类型
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int k = 0; k < jsonArray.size(); k++) {
                    if (jsonArray.get(k).equals("register")) {

                        if (jsonObjData.get("id_C").equals(jsonObject.get("id_C"))) {

                            hashSet.add((String) jsonObject.get("id_U"));
                        }

                    } else {
                        continue;
                    }
                }
            }
            //判断结果集里面有没有这个字段，有的话就不是初次，  累加

            if (jsonObjData.containsKey("id_U")) {
                JSONArray jsonArray = (JSONArray) jsonObjData.get("id_U");
                for (int i = 0; i < jsonArray.size(); i++) {
                    hashSet.add((String) jsonArray.get(i));
                }
            }
            strHashMap.put("id_C", jsonObjData.get("id_C"));
            strHashMap.put("id_U", hashSet);
            strHashMap.put("wn0cntUser", hashSet.size());
            objData.add(strHashMap);
        }
        return objData;
    }


    //初始化  usage
    public List<HashMap<String, Object>> initialusage(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();

        //有表头时
        if (headobjData.size() > 0) {

            //遍历吧redis中的公司放set1中
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());

                set1.add((String) jsonObject.get("id_C"));
            }
            //遍历吧redis中的表头公司放set2中
            for (int j = 0; j < headobjData.size(); j++) {
                JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));
                set2.add((String) jsonObjData.get("id_C"));
            }
            //利用  差集来判断公司是否相同，如果有不相同的则添加进表头
            set1.removeAll(set2);

        } else {
            //没表头
            //遍历吧redis中的公司初始化
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i).equals("usage")) {
                        set1.add((String) jsonObject.get("id_C"));
                    }
                }
            }
        }
        for (String str : set1) {
            HashMap<String, Object> strHashMap = new HashMap<>();
            strHashMap.put("id_C", str);
            strHashMap.put("wn0cntUser", 0);
            objData.add(strHashMap);
        }
        return objData;
    }

    //计算  usage
    public List<HashMap<String, Object>> usage(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        for (int j = 0; j < headobjData.size(); j++) {

            HashMap<String, Object> strHashMap = new HashMap<>();
            JSONObject jsonObjData = (JSONObject) JSON.toJSON(headobjData.get(j));

            HashSet<String> hashSet = new HashSet<>();

            //其他日志类型
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                for (int i = 0; i < jsonArray.size(); i++) {
                    if (jsonArray.get(i).equals("usage")) {

                        if (jsonObjData.get("id_C").equals(jsonObject.get("id_C"))) {

                            hashSet.add((String) jsonObject.get("id_U"));
                        }

                    } else {
                        continue;
                    }
                }
            }
            //判断结果集里面有没有这个字段，有的话就不是初次，  累加

            if (jsonObjData.containsKey("id_U")) {
                JSONArray jsonArray = (JSONArray) jsonObjData.get("id_U");
                for (int i = 0; i < jsonArray.size(); i++) {
                    hashSet.add((String) jsonArray.get(i));
                }
            }
            strHashMap.put("id_C", jsonObjData.get("id_C"));
            strHashMap.put("id_U", hashSet);
            strHashMap.put("wn0cntUser", hashSet.size());
            objData.add(strHashMap);
        }
        return objData;
    }

    //初始化  info
    public List<HashMap<String, Object>> initialinfo(List range) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        //遍历吧redis中的公司初始化
        for (int y = 0; y < range.size(); y++) {

            JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
            JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i).equals("info")) {
                    HashMap<String, Object> strHashMap = new HashMap<>();
                    JSONObject redis = (JSONObject) jsonObject.get("data");
                    strHashMap.put("id", redis.get("id"));
                    strHashMap.put("compID", redis.get("compID"));
                    objData.add(strHashMap);
                }
            }
        }
        return objData;
    }
    //
    public Integer computingTime(String id_C, String modeName) {
//        Query query = new Query(new Criteria("_id").is("a-module-" + id_C).and("modList.data.modeName").is(modeName));
//        query.fields().include("modList.data.$");
//        //query.fields().include("oItem.objItem.$");
//        Asset Asset = mongoTemplate.findOne(query, Asset.class);
//        ArrayList array = (ArrayList) Asset.getModList().get("data");
//        //boolean judge = false;
//        int dateNum = 0;
//        for (int t = 0; t < array.size(); t++) {
//            HashMap Object = (HashMap) array.get(t);
//
//            if (modeName.equals(Object.get("modeName"))) {
//
//                 dateNum = (int) Object.get("dateNum");
//                //(--dateNum + "   ----------------" + dateNum--);
//                Update update = new Update();
//                //更改dateNum字段  天数减一
//                update.set("modList.data.$.dateNum", --dateNum);
//                //
//                mongoTemplate.updateFirst(query, update, Asset.class);
//                //judge = true;
//            }
//        }
//        //return judge;
//        return dateNum;
        return null;
    }


    //初始化 createComp  并包含计算
    public List<HashMap<String, Object>> initiacreateComp(List range, List<HashMap<String, Object>> headobjData) {
        List<HashMap<String, Object>> objData = new LinkedList<>();

        List<HashMap<String, Object>> linkedList = new LinkedList<>();

        //有表头时
        if (headobjData.size() > 0) {
            //有表头时，拿表头的长度去查redis,拿redis长度后面的数据，看是否还有createComp的日志类型，有的话就加进linkedList中
            //遍历吧redis中的公司放set1中
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                HashMap<String, Object> data =new HashMap<>();
                for (int k = 0; k < jsonArray.size(); k++) {
                    if (jsonArray.get(k).equals("createComp")) {
                        JSONObject Object = (JSONObject)jsonObject.get("data");
                        data.put("id_C",Object.get("id_C"));
                        data.put("id_U",Object.get("id_U"));
                        linkedList.add(data);
                    }
                }
            }
        } else {

            //没表头  redis长度为0，从0到-1，查所有createComp日志并加到linkedList中
            //遍历吧redis中的公司初始化
            for (int y = 0; y < range.size(); y++) {
                JSONObject jsonObject = (JSONObject) JSONObject.parse(range.get(y).toString());
                JSONArray jsonArray = (JSONArray) jsonObject.get("logType");
                HashMap<String, Object> data =new HashMap<>();
                for (int k = 0; k < jsonArray.size(); k++) {
                    if (jsonArray.get(k).equals("createComp")) {
                        JSONObject Object = (JSONObject)jsonObject.get("data");
                        data.put("id_C",Object.get("id_C"));
                        data.put("id_U",Object.get("id_U"));
                        linkedList.add(data);

                    }
                }
            }
        }
        //把所有数据遍历存放objData
        for (int i = 0; i < linkedList.size(); i++) {
            objData.add(linkedList.get(i));
        }
        return objData;
    }


}