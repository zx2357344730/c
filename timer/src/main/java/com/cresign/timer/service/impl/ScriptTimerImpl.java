package com.cresign.timer.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.timer.client.WSFilterClient;
import com.cresign.timer.service.ScriptTimer;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.logger.LogUtil;
import com.cresign.tools.pojo.po.*;
import com.cresign.tools.reflectTools.ApplicationContextTools;
import com.mongodb.client.result.UpdateResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.script.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScriptTimerImpl implements ScriptTimer {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LogUtil logUtil;

    @Autowired
    private WSFilterClient wsClient;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RetResult retResult;

    @Override
    public Object scriptEngine(JSONObject jsonTrigger) throws ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        String id_O = jsonTrigger.getString("id_O");
        String id_A = jsonTrigger.getString("id_A");
        JSONObject jsonObjVar = jsonTrigger.getJSONObject("objVar");
        System.out.println("jsonObjVar=" + jsonObjVar);
        JSONObject jsonObjGlobal = jsonTrigger.getJSONObject("objGlobal");
        System.out.println("jsonObjGlobal=" + jsonObjGlobal);
        if (jsonObjGlobal == null) {
            jsonObjGlobal = jsonObjVar;
        }
        jsonObjGlobal.putAll(jsonObjVar);
        //遍历计算jsonObjVar的值
        if (id_O != null) {
            for (Map.Entry<String, Object> entry : jsonObjVar.entrySet()) {
                Object var = this.scriptEngineVar(jsonObjVar.getString(entry.getKey()), jsonObjGlobal);
                jsonObjVar.put(entry.getKey(), var);
            }
        }
        //Either id_O or id_A
        else if (id_A != null) {
            for (Map.Entry<String, Object> entry : jsonObjVar.entrySet()) {
                Object var = this.scriptEngineVar(jsonObjVar.getString(entry.getKey()), jsonObjVar);
                jsonObjVar.put(entry.getKey(), var);
            }
        }
        System.out.println("jsonObjVar=" + jsonObjVar);

        JSONObject jsonObjIf = jsonTrigger.getJSONObject("objIf");
        String script = jsonObjIf.getString("script");
        if (script.startsWith("##")) {
            String scriptResult = scriptEngineIf(script, jsonObjVar, jsonTrigger.getJSONObject("objExec"));
        } else {
            //KEV all isDone change to isActive;
            // if isActive == null, this is a routine and will do it every time, never stop
            if (jsonObjIf.getBoolean("isActive") == null) {
                //判断执行哪个exec
                String scriptResult = scriptEngineIf(script, jsonObjVar, new JSONObject());
                System.out.println("scriptResult=" + scriptResult);
                JSONArray arrayObjExec = jsonTrigger.getJSONObject("objExec").getJSONArray(scriptResult);
                //if's result is either null or a String
                if (arrayObjExec != null) {
                    //执行
                    this.scriptEngineExec(arrayObjExec, jsonObjVar);
                }
            }
            //isActive为true执行
            else if (jsonObjIf.getBoolean("isActive")) {
                //判断执行哪个exec
                String scriptResult = scriptEngineIf(script, jsonObjVar, new JSONObject());
                System.out.println("scriptResult=" + scriptResult);
                JSONArray arrayObjExec = jsonTrigger.getJSONObject("objExec").getJSONArray(scriptResult);
                if (arrayObjExec != null) {
                    //执行
                    this.scriptEngineExec(arrayObjExec, jsonObjVar);
                    // After execute, set the Active to false so next time it won't exec again
                    if (id_O != null) {
//                        Query queryOrder = new Query(new Criteria("_id").is(id_O));
//                        Update updateOrder = new Update();
//                        updateOrder.set("oTrigger." + jsonTrigger.getString("log") + ".objIf." + j + ".isActive", false);
//                        UpdateResult updateResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
                    }
                    if (id_A != null) {
                        Query queryAsset = new Query(new Criteria("_id").is(id_A));
                        Update updateAsset = new Update();
                        updateAsset.set("cTrigger.objData.objIf." + jsonObjIf.getString("ref") + ".isActive", false);
                        UpdateResult updateResult = mongoTemplate.updateFirst(queryAsset, updateAsset, Asset.class);
                    }
                }
            }
        }
        return null;
    }

//    @Override
//    public Object scriptEngine(JSONArray arrayTrigger) throws ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
//        Integer count = 0;
//        for (int i = 0; i < arrayTrigger.size(); i++) {
//            //Each Trigger go get all Var calculated
//            JSONObject jsonTrigger = arrayTrigger.getJSONObject(i);
//            String id_O = jsonTrigger.getString("id_O");
//            String id_A = jsonTrigger.getString("id_A");
//            JSONObject jsonObjVar = jsonTrigger.getJSONObject("objVar");
//            System.out.println("jsonObjVar=" + jsonObjVar);
//            JSONObject jsonObjGlobal = jsonTrigger.getJSONObject("objGlobal");
//            System.out.println("jsonObjGlobal=" + jsonObjGlobal);
//            if (jsonObjGlobal == null) {
//                jsonObjGlobal = jsonObjVar;
//            }
//            jsonObjGlobal.putAll(jsonObjVar);
//            //遍历计算jsonObjVar的值
//            if (id_O != null) {
//                for (Map.Entry<String, Object> entry : jsonObjVar.entrySet()) {
//                    System.out.println("count=" + count);
//                    Object var = this.scriptEngineVar(jsonObjVar.getString(entry.getKey()), jsonObjGlobal);
//                    count ++;
//                    jsonObjVar.put(entry.getKey(), var);
//                }
//            }
//            //Either id_O or id_A
//            else if (id_A != null) {
//                for (Map.Entry<String, Object> entry : jsonObjVar.entrySet()) {
//                    System.out.println("count=" + count);
//                    Object var = this.scriptEngineVar(jsonObjVar.getString(entry.getKey()), jsonObjVar);
//                    count ++;
//                    jsonObjVar.put(entry.getKey(), var);
//                }
//            }
//            System.out.println("jsonObjVar=" + jsonObjVar);
//            JSONArray arrayObjIf = jsonTrigger.getJSONArray("objIf");
//            for (int j = 0; j < arrayObjIf.size(); j++) {
//                JSONObject jsonObjIf = arrayObjIf.getJSONObject(j);
//                String script = jsonObjIf.getString("script");
//                if (script.startsWith("##")) {
//                    String scriptResult = scriptEngineIf(script, jsonObjVar, jsonTrigger.getJSONObject("objExec"));
//                } else {
//                    //KEV all isDone change to isActive;
//                    // if isActive == null, this is a routine and will do it every time, never stop
//                    if (jsonObjIf.getBoolean("isActive") == null) {
//                        //判断执行哪个exec
//                        String scriptResult = scriptEngineIf(script, jsonObjVar, new JSONObject());
//                        System.out.println("scriptResult=" + scriptResult);
//                        JSONArray arrayObjExec = jsonTrigger.getJSONObject("objExec").getJSONArray(scriptResult);
//                        //if's result is either null or a String
//                        if (arrayObjExec != null) {
//                            //执行
//                            this.scriptEngineExec(arrayObjExec, jsonObjVar);
//                        }
//                    }
//                    //isActive为true执行
//                    else if (jsonObjIf.getBoolean("isActive")) {
//                        //判断执行哪个exec
//                        String scriptResult = scriptEngineIf(script, jsonObjVar, new JSONObject());
//                        System.out.println("scriptResult=" + scriptResult);
//                        JSONArray arrayObjExec = jsonTrigger.getJSONObject("objExec").getJSONArray(scriptResult);
//                        if (arrayObjExec != null) {
//                            //执行
//                            this.scriptEngineExec(arrayObjExec, jsonObjVar);
//                            // After execute, set the Active to false so next time it won't exec again
//                            if (id_O != null) {
//                                Query queryOrder = new Query(new Criteria("_id").is(id_O));
//                                Update updateOrder = new Update();
//                                updateOrder.set("oTrigger." + jsonTrigger.getString("log") + ".objIf." + j + ".isActive", false);
//                                UpdateResult updateResult = mongoTemplate.updateFirst(queryOrder, updateOrder, Order.class);
//                            }
//                            if (id_A != null) {
//                                Query queryAsset = new Query(new Criteria("_id").is(id_A));
//                                Update updateAsset = new Update();
//                                updateAsset.set("cTrigger.objData.objIf." + jsonObjIf.getString("ref") + ".isActive", false);
//                                UpdateResult updateResult = mongoTemplate.updateFirst(queryAsset, updateAsset, Asset.class);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

//    @Override
    public Object scriptEngineVar(String var, JSONObject jsonObjGlobal) throws ScriptException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("");
        System.out.println("var=" + var);
        System.out.println("jsonObjGlobal=" + jsonObjGlobal);
        if (var.startsWith("##")) {
            //Get from card
            if (var.startsWith("##C")) {
                String[] scriptSplit = var.split("\\.");
                Query query = new Query(new Criteria("_id").is(scriptSplit[2]));
                List list = new ArrayList();
                //See which COUPA
                switch (scriptSplit[1]) {
                    case "Comp":
                        list = mongoTemplate.find(query, Comp.class);
                        break;
                    case "Order":
                        list = mongoTemplate.find(query, Order.class);
                        break;
                    case "User":
                        list = mongoTemplate.find(query, User.class);
                        break;
                    case "Prod":
                        list = mongoTemplate.find(query, Prod.class);
                        break;
                    case "Asset":
                        list = mongoTemplate.find(query, Asset.class);
                        break;
                }
                if (list.get(0) == null) {
                    return null;
                }
                JSONObject jsonList = (JSONObject) JSON.toJSON(list.get(0));
                if (jsonList.getJSONObject(scriptSplit[3]) == null) {
                    return null;
                }
                JSONObject jsonVar = jsonList.getJSONObject(scriptSplit[3]);
                for (int k = 4; k < scriptSplit.length - 1; k++) {
                    //根据[拆分
                    //break up array
                    String[] ifArray = scriptSplit[k].split("\\[");
                    System.out.println("length=" + ifArray.length);
                    //拆分成一份类型为jsonObject
                    if (ifArray.length == 1) {
                        if (jsonVar.getJSONObject(ifArray[0]) == null) {
                            return null;
                        }
                        jsonVar = jsonVar.getJSONObject(ifArray[0]);
                    }
                    //拆分成两份类型为jsonArray
                    if (ifArray.length == 2) {
                        String[] index = ifArray[1].split("]");
                        if (jsonVar.getJSONArray(ifArray[0]).getJSONObject(Integer.parseInt(index[0])) == null) {
                            return null;
                        }
                        jsonVar = jsonVar.getJSONArray(ifArray[0]).getJSONObject(Integer.parseInt(index[0]));
                    }
                }
                System.out.println("jsonVar=" + jsonVar);
                if (jsonVar.get(scriptSplit[scriptSplit.length - 1]) == null) {
                    return null;
                }
                if (var.startsWith("##CB")) {
                    Boolean scriptResult = jsonVar.getBoolean(scriptSplit[scriptSplit.length - 1]);
                    return scriptResult;
                }
                if (var.startsWith("##CS")) {
                    String scriptResult = jsonVar.getString(scriptSplit[scriptSplit.length - 1]);
                    return scriptResult;
                }
                if (var.startsWith("##CI")) {
                    Integer scriptResult = jsonVar.getInteger(scriptSplit[scriptSplit.length - 1]);
                    return scriptResult;
                }
                if (var.startsWith("##CN")) {
                    Double scriptResult = jsonVar.getDouble(scriptSplit[scriptSplit.length - 1]);
                    return scriptResult;
                }
                if (var.startsWith("##CA")) {
                    JSONArray scriptResult = jsonVar.getJSONArray(scriptSplit[scriptSplit.length - 1]);
                    return scriptResult;
                }
                if (var.startsWith("##CO")) {
                    System.out.println("test=" + jsonVar.getJSONObject(scriptSplit[scriptSplit.length - 1]));
                    JSONObject scriptResult = jsonVar.getJSONObject(scriptSplit[scriptSplit.length - 1]);
                    return scriptResult;
                }
            }
            //T means it is a counter
            else if (var.startsWith("##T")) {
                String[] scriptSplit = var.split("\\.");
                Query query = new Query(new Criteria("_id").is(scriptSplit[1]));
                Asset asset = mongoTemplate.findOne(query, Asset.class);
                if (asset == null || asset.getRefAuto() == null || asset.getRefAuto().getJSONObject("objCounter") == null ||
                        asset.getRefAuto().getJSONObject("objCounter").getJSONObject(scriptSplit[2]) == null) {
                    return null;
                }
                JSONObject jsonCounter = asset.getRefAuto().getJSONObject("objCounter").getJSONObject(scriptSplit[2]);
                Integer count = jsonCounter.getInteger("count");
                Integer max = jsonCounter.getInteger("max");
                Integer digit = jsonCounter.getInteger("digit");
                int length = digit - String.valueOf(count).length();
                System.out.println("length=" + length);
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < length; i++) {
                    sb.append("0");
                }
                String strCount = String.valueOf(sb);
                strCount += count;
                System.out.println("strCount=" + strCount);
                if (count == max) {
                    count = 1;
                } else {
                    count ++;
                }
                System.out.println("count=" + count);
                Update update = new Update();
                update.set("refAuto.objCounter." + scriptSplit[2] + ".count", count);
                UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Asset.class);
                System.out.println("updateResult=" + updateResult);
                return strCount;
            }
            //F = it is a function, then break the string and recall myself to calculate
            else if (var.startsWith("##F")) {
                String varSubstring = var.substring(4);
                String[] varSplit = varSubstring.split("##");
                System.out.println("##F=" + varSplit[0] + "," + varSplit[1] + "," + varSplit[2]);
                String result = (String) scriptEngineVar(jsonObjGlobal.getString(varSplit[2]), jsonObjGlobal);
                System.out.println("result=" + result);
                System.out.println("resultType=" + result.getClass().getSimpleName());
                JSONObject jsonResult = JSONObject.parseObject(result);
                Class<?> clazz = Class.forName(varSplit[0]);
                Object bean = ApplicationContextTools.getBean(clazz);
                Method method1 = clazz.getMethod(varSplit[1], new Class[]{JSONObject.class});
                System.out.println("varSplit[1]=" + varSplit[1]);
                System.out.println("method1=" + method1);
                //invoke....
                Object invoke = method1.invoke(bean, jsonResult);
                System.out.println("invoke=" + invoke);
                return invoke;
            }
            //D = date formates
            else if (var.startsWith("##D")) {
                String varSubstring = var.substring(4);
                System.out.println("varSubstring=" + varSubstring);
                String[] varSplit = varSubstring.split("##");
                SimpleDateFormat sdf = null;
                Calendar calendar = Calendar.getInstance();
                for (int i = varSplit.length - 1; i >= 0; i--) {
                    String partTime = varSplit[i];
                    if (partTime.equals("*")) {
                        switch (i) {
                            case 0:
                                sdf = new SimpleDateFormat("yyyy");
                                varSplit[0] = sdf.format(calendar.getTime());
                                break;
                            case 1:
                                sdf = new SimpleDateFormat("MM");
                                varSplit[1] = sdf.format(calendar.getTime());
                                break;
                            case 2:
                                sdf = new SimpleDateFormat("dd");
                                varSplit[2] = sdf.format(calendar.getTime());
                                break;
                            case 3:
                                sdf = new SimpleDateFormat("HH");
                                varSplit[3] = sdf.format(calendar.getTime());
                                break;
                            case 4:
                                sdf = new SimpleDateFormat("mm");
                                varSplit[4] = sdf.format(calendar.getTime());
                                break;
                            case 5:
                                sdf = new SimpleDateFormat("ss");
                                varSplit[5] = sdf.format(calendar.getTime());
                                break;
                        }
                    }
                    else if (partTime.startsWith("+") || partTime.startsWith("-")) {
                        int part = Integer.parseInt(partTime);
                        System.out.println("part=" + part);
                        switch (i) {
                            case 0:
                                calendar.add(Calendar.YEAR, part);
                                sdf = new SimpleDateFormat("yyyy");
                                varSplit[0] = sdf.format(calendar.getTime());
                                break;
                            case 1:
                                calendar.add(Calendar.MONTH, part);
                                sdf = new SimpleDateFormat("MM");
                                varSplit[1] = sdf.format(calendar.getTime());
                                break;
                            case 2:
                                calendar.add(Calendar.DATE, part);
                                sdf = new SimpleDateFormat("dd");
                                varSplit[2] = sdf.format(calendar.getTime());
                                break;
                            case 3:
                                calendar.add(Calendar.HOUR_OF_DAY, part);
                                sdf = new SimpleDateFormat("HH");
                                varSplit[3] = sdf.format(calendar.getTime());
                                break;
                            case 4:
                                calendar.add(Calendar.MINUTE, part);
                                sdf = new SimpleDateFormat("mm");
                                varSplit[4] = sdf.format(calendar.getTime());
                                break;
                            case 5:
                                calendar.add(Calendar.SECOND, part);
                                sdf = new SimpleDateFormat("ss");
                                varSplit[5] = sdf.format(calendar.getTime());
                                break;
                        }
                    }
                    System.out.println("i=" + i +",varSplit=" + varSplit[i]);
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer = stringBuffer.append(varSplit[0]).append("/").append(varSplit[1]).append("/").append(varSplit[2])
                        .append(" ").append(varSplit[3]).append(":").append(varSplit[4]).append(":").append(varSplit[5]);
                return stringBuffer;
            }
            // else this is not a ##, use script engine to get result
            else {
                String varSubString = var.substring(4);
                if (varSubString.contains("for")) {
                    return null;
                }
                ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
                Compilable compilable = (Compilable) scriptEngine;
                Bindings bindings = scriptEngine.createBindings();
                CompiledScript compiledScript = compilable.compile(varSubString);
                String[] varSplit = var.split("\\(");
                if (varSplit[varSplit.length - 1].split("\\)").length > 0) {
                    String scriptVar = varSplit[varSplit.length - 1].split("\\)")[0];
                    System.out.println("scriptVar=" + scriptVar);
                    String[] arrayKey = scriptVar.split(",");
                    for (int i = 0; i < arrayKey.length; i++) {
                        String key = arrayKey[i];
                        System.out.println("key=" + key);
                        // I am calling myself again here
                        Object result = scriptEngineVar(jsonObjGlobal.getString(key), jsonObjGlobal);
                        System.out.println(i + ":" + result);
                        bindings.put(key, result);
                    }
                }

                //after created all compile data, here is actual eval
                if (var.startsWith("##B")) {
                    Boolean scriptResult = (Boolean) compiledScript.eval(bindings);
                    return scriptResult;
                }
                if (var.startsWith("##S")) {
                    String scriptResult = compiledScript.eval(bindings).toString();
                    return scriptResult;
                }
                if (var.startsWith("##I")) {
                    Integer scriptResult = (Integer) compiledScript.eval(bindings);
                    System.out.println("##I=" + scriptResult);
                    return scriptResult;
                }
                if (var.startsWith("##N")) {
                    Double scriptResult = (Double) compiledScript.eval(bindings);
                    System.out.println("##N=" + scriptResult);
                    return scriptResult;
                }
                if (var.startsWith("##A")) {
                    JSONArray scriptResult = (JSONArray) JSON.toJSON(compiledScript.eval(bindings));
                    return scriptResult;
                }
                if (var.startsWith("##O")) {
                    String result = JSON.toJSONString(compiledScript.eval(bindings));
                    System.out.println("##O=" + result);
                    result = result.replace("\\", "");
                    System.out.println(result);
                    result = result.substring(1, result.length() - 1);
                    System.out.println(result);
                    JSONObject scriptResult = JSON.parseObject(result);
                    System.out.println(scriptResult);

                    return scriptResult;
                }
            }
        }
        // if they are not B/S/N/A/O, it's NO ##, so it's either number or text
        //正则判断是否数字
        Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(var);
        //判断字符串是否是数字
        if (matcher.matches()) {
            return Double.parseDouble(var);
        }
        return var;
    }

//    @Override
    // Here in If you can also need script engine to compile
    public String scriptEngineIf(String script, JSONObject jsonObjVar, JSONObject jsonObjExec) throws ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException {
        if (script.startsWith("##")) {
            String[] scriptSplit = script.split("##");
            System.out.println("\n\n\nscriptSplit=");
            for (int i = 0; i < scriptSplit.length; i++) {
                System.out.println(i + ":" + scriptSplit[i]);
            }
            String valueFor = scriptSplit[1];
            String scriptSubString = scriptSplit[2];
            System.out.println("valueFor" + valueFor + ",scriptSubString=" + scriptSubString);
            if (scriptSubString.contains("for")) {
                return null;
            }
            JSONArray arrayValue = (JSONArray) JSON.toJSON(jsonObjVar.get(valueFor));
            for (int i = 0; i < arrayValue.size(); i++) {
                JSONObject value = arrayValue.getJSONObject(i);
                System.out.println(value);
                ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
                Compilable compilable = (Compilable) scriptEngine;
                Bindings bindings = scriptEngine.createBindings();
                CompiledScript compiledScript = compilable.compile(scriptSubString);
                //从script获取参数列表
                String[] scriptValue = script.split("\\(");
                if (scriptValue[scriptValue.length - 1].split("\\)").length > 0) {
                    String scriptVar = scriptValue[scriptValue.length - 1].split("\\)")[0];
                    System.out.println("scriptVar=" + scriptVar);
                    String[] arrayKey = scriptVar.split(",");
                    //传参
                    for (int k = 0; k < arrayKey.length; k++) {
                        String key = arrayKey[k];
                        System.out.println("key=" + key);
                        if (valueFor.equals(key)) {
                            bindings.put(key, value);
                        } else {
                            bindings.put(key, jsonObjVar.get(key));
                        }
                    }
                }
                String scriptResult = String.valueOf(compiledScript.eval(bindings));
                System.out.println("Result=" + scriptResult);
                JSONArray arrayObjExec = jsonObjExec.getJSONArray(scriptResult);
                //KEV WHY? in If you call Exec directly?
                //Need to redefine how "Array of lBUser works"
                if (arrayObjExec != null) {
                    JSONObject jsonObjVarClone = (JSONObject) jsonObjVar.clone();
                    jsonObjVarClone.put(valueFor, value);
                    System.out.println("===");
                    System.out.println(value);
                    System.out.println("===");
                    scriptEngineExec(arrayObjExec, jsonObjVarClone);
                }
            }
            return "for";
        } else {
            // Stop any for or other script illegal text, I can use a cn_java to do a map
            if (script.contains("for")) {
                return null;
            }
            ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
            Compilable compilable = (Compilable) scriptEngine;
            Bindings bindings = scriptEngine.createBindings();
            CompiledScript compiledScript = compilable.compile(script);
            //从script获取参数列表
            String[] scriptSplit = script.split("\\(");
            if (scriptSplit[scriptSplit.length - 1].split("\\)").length > 0) {
                String scriptVar = scriptSplit[scriptSplit.length - 1].split("\\)")[0];
                System.out.println("scriptVar=" + scriptVar);
                String[] arrayKey = scriptVar.split(",");
                //传参
                for (int k = 0; k < arrayKey.length; k++) {
                    String key = arrayKey[k];
                    System.out.println("key=" + key);
                    bindings.put(key, jsonObjVar.get(key));
                }
            }
            //This if statement will always return a String, and never bool...
            String scriptResult = String.valueOf(compiledScript.eval(bindings));
            return scriptResult;
        }
    }

//    @Override
    public Object scriptEngineExec(JSONArray arrayObjExec, JSONObject jsonObjVar) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        for (int i = 0; i < arrayObjExec.size(); i++) {
            JSONObject jsonObjExec = arrayObjExec.getJSONObject(i);
            String method = jsonObjExec.getString("method");
            System.out.println("method=" + method);

            //just break down params
            JSONObject jsonParams = (JSONObject) jsonObjExec.getJSONObject("params").clone();
            for (Map.Entry <String, Object> entry : jsonParams.entrySet()) {
                System.out.println("key=" + entry.getKey());
                System.out.println("value=" + entry.getValue());
                String dataType = jsonParams.get(entry.getKey()).getClass().getSimpleName();
                if (dataType.equals("String")) {
                    String param = jsonParams.getString(entry.getKey());
                    if (param.startsWith("##OP")) {
                        String[] paramSplit = param.split("\\.");
                        System.out.println("paramSplit.length=" + paramSplit.length);
                        if (paramSplit.length == 2) {
                            jsonParams.put(entry.getKey(), jsonObjVar.get(paramSplit[1]));
                        } else if (paramSplit.length > 2) {

                            jsonParams.put(entry.getKey(), jsonObjVar.getJSONObject(paramSplit[1]).get(paramSplit[2]));
                        }
                    }
                }
            }

            if (method.startsWith("com.cresign")) {
                //调用方法
                String[] methodSplit = method.split("##");
                Class<?> clazz = Class.forName(methodSplit[0]);
                Object bean = ApplicationContextTools.getBean(clazz);
                Method method1 = clazz.getMethod(methodSplit[1], new Class[]{JSONObject.class});

                //Key!! invoke here with bean + params
                Object invoke = method1.invoke(bean, jsonParams);
                System.out.println("invoke=" + invoke);
            } else {
                //发日志
                //Else, send log in "method" logFlow
                jsonParams.put("tmd", dateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                dbUtils.addES(jsonParams, method);
//                logUtil.sendLogByFilebeat(method, jsonParams);
//                LogFlow log = JSONObject.parseObject(JSON.toJSONString(jsonParams),LogFlow.class);
//                wsClient.sendLogWS(log);
            }
        }
        return null;
    }

    @Override
    public Object cTrigTest(String time) throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        SimpleDateFormat sdf = new SimpleDateFormat("Z");
//        String format = sdf.format(new Date());
//        String timeZone = null;
//        if (format.startsWith("+")) {
//            timeZone = "-" + format.substring(1, 3);
//        } else {
//            timeZone = "+" + format.substring(1, 3);
//        }
//        System.out.println("format=" + format + "," + timeZone);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
//        calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeZone));
        System.out.println("日期=" + calendar.getTime());
        //月要加1
        System.out.println("月=" + (calendar.get(Calendar.MONTH) + 1));
        //周从周日开始，周日1，周六7
        System.out.println("周=" + calendar.get(Calendar.DAY_OF_WEEK));
        System.out.println("日=" + calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println("时=" + calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println("分=" + calendar.get(Calendar.MINUTE));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder monthQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder weekQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder dayQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder hourQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder minuteQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder monthExistsQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder weekExistsQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder dayExistsQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder hourExistsQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder minuteExistsQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder timeExistsQueryBuilder = new BoolQueryBuilder();
        monthExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.month"));
        weekExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.week"));
        dayExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.day"));
        hourExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.hour"));
        minuteExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.minute"));
        timeExistsQueryBuilder.mustNot(QueryBuilders.existsQuery("data.time"));
        monthQueryBuilder.should(QueryBuilders.termQuery("data.month", calendar.get(Calendar.MONTH) + 1)).should(monthExistsQueryBuilder);
        weekQueryBuilder.should(QueryBuilders.termQuery("data.week", calendar.get(Calendar.DAY_OF_WEEK))).should(weekExistsQueryBuilder);
        dayQueryBuilder.should(QueryBuilders.termQuery("data.day", calendar.get(Calendar.DAY_OF_MONTH))).should(dayExistsQueryBuilder);
        hourQueryBuilder.should(QueryBuilders.termQuery("data.hour", calendar.get(Calendar.HOUR_OF_DAY))).should(hourExistsQueryBuilder);
        minuteQueryBuilder.should(QueryBuilders.termQuery("data.minute", calendar.get(Calendar.MINUTE))).should(minuteExistsQueryBuilder);
        queryBuilder.must(monthQueryBuilder).must(weekQueryBuilder).must(dayQueryBuilder).must(hourQueryBuilder).must(minuteQueryBuilder)
                .must(timeExistsQueryBuilder).must(QueryBuilders.termQuery("subType", "timer"));
        sourceBuilder.query(queryBuilder).from(0).size(10000);
        SearchRequest searchRequest = new SearchRequest("timeflow").source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("searchResponse=" + searchResponse);
        SearchHit[] hits = searchResponse.getHits().getHits();
        System.out.println("hits=" + hits);
        long start = System.currentTimeMillis();
        HashSet setId_A = new HashSet();
        JSONObject jsonId_A = new JSONObject();
        System.out.println("length=" + hits.length);
        for (int i = 0; i < hits.length; i++) {
            JSONObject jsonHit = (JSONObject) JSON.parse(hits[i].getSourceAsString());
            String id_A = jsonHit.getString("id_A");
            String ref = jsonHit.getJSONObject("data").getString("ref");
            System.out.println("ref=" + ref);
            if (jsonId_A.getJSONArray(id_A) == null) {
                JSONArray arrayRef = new JSONArray();
                arrayRef.add(ref);
                jsonId_A.put(id_A, arrayRef);
            } else {
                JSONArray arrayRef = jsonId_A.getJSONArray(id_A);
                arrayRef.add(ref);
                jsonId_A.put(id_A, arrayRef);
            }
            setId_A.add(id_A);
        }
        System.out.println("setId_A=" + setId_A);
        System.out.println("jsonId_A=" + jsonId_A);

        Query queryAsset = new Query(new Criteria("_id").in(setId_A));
        List<Asset> assets = mongoTemplate.find(queryAsset, Asset.class);
        JSONArray arrayTrigger = new JSONArray();
        for (int i = 0; i < assets.size(); i++) {
            JSONObject jsonTrigger = new JSONObject();
            Asset asset = assets.get(i);
            jsonTrigger.put("id_A", asset.getId());
            JSONObject jsonObjData = asset.getCTrigger().getJSONObject("objData");
            jsonTrigger.putAll(jsonObjData);
            jsonTrigger.remove("objInfo");
            JSONObject jsonObjIf = jsonObjData.getJSONObject("objIf");
            JSONArray arrayIf = jsonId_A.getJSONArray(asset.getId());
            JSONArray arrayObjIf = new JSONArray();
            for (int j = 0; j < arrayIf.size(); j++) {
                JSONObject jsonIf = jsonObjIf.getJSONObject(arrayIf.getString(j));
                jsonIf.put("ref", arrayIf.getString(j));
                arrayObjIf.add(jsonIf);
            }
            jsonTrigger.put("objIf", arrayObjIf);
            arrayTrigger.add(jsonTrigger);
        }
        System.out.println("arrayTrigger=" + arrayTrigger);
//        for (int i = 0; i < assets.size(); i++) {
//            JSONArray arrayRef = jsonId_A.getJSONArray(assets.get(i).getId());
//            for (int j = 0; j < arrayRef.size(); j++) {
//                String ref = arrayRef.getString(j);
//                JSONObject jsonTrigger = assets.get(i).getCTrigger().getJSONObject("objData").getJSONObject(ref);
//                if (jsonTrigger.getBoolean("isActive") != null) {
//                    if (jsonTrigger.getBoolean("isActive")) {
//                        jsonTrigger.put("id", assets.get(i).getId());
//                        jsonTrigger.put("table", "Asset");
//                        jsonTrigger.put("key", "cTrigger.objData." + ref);
//                        arrayTrigger.add(jsonTrigger);
//                    }
//                } else {
//                    arrayTrigger.add(jsonTrigger);
//                }
//            }
//        }
        long end = System.currentTimeMillis();
        System.out.println("time=" + (end - start) + "ms");
//        Object o = scriptEngine(arrayTrigger);
//        System.out.println("o=" + o);
        return arrayTrigger;
    }
}
