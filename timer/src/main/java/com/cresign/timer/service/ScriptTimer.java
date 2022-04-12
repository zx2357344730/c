package com.cresign.timer.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface ScriptTimer {


        /**
         * scriptEngine
         * @Author Rachel
         * @Date 2021/11/13
         * ##param arrayTrigger trigger数组
         * ##param jsonStat 统计数据
         * @Return java.lang.Object
         * @Card
         **/
        Object scriptEngine(JSONArray arrayTrigger) throws ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, IOException;

        /**
         * 处理trigger变量
         * @Author Rachel
         * @Date 2021/12/14
         * ##param var
         * ##param jsonObjGlobal
         * ##param jsonStat
         * @Return java.lang.Object
         * @Card
         **/
//        Object scriptEngineVar(String var, JSONObject jsonObjGlobal) throws ScriptException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;
//
//        /**
//         * trigger判断
//         * @Author Rachel
//         * @Date 2021/11/10
//         * ##param jsonTrigger trigger对象
//         * @Return java.lang.Object
//         * @Card
//         **/
//        String scriptEngineIf(String script, JSONObject jsonObjVar, JSONObject jsonObjExec) throws ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException;
//
//        /**
//         * trigger执行
//         * @Author Rachel
//         * @Date 2021/11/10
//         * ##param arrayExecute 单个trigger的执行数组
//         * @Return java.lang.Object
//         * @Card
//         **/
//        Object scriptEngineExec(JSONArray arrayObjExec, JSONObject jsonObjVar) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;
//

        Object cTrigTest(String time) throws IOException, ScriptException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException;
    }


