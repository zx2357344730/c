package com.cresign.chat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.service.impl.TimeZjServiceImpl;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName NewObj
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/4/18 10:36
 * @Version 1.0.0
 */
public class Obj {

    /**
     *
     * @param bcdStatus	递归状态: == 1 111，== 2 222
     * @param bispush
     * @param bisactivate
     * @param id_O
     * @param id_OP
     * @param index
     * @param bmdpt
     * @param sumSonCount
     * @param id_ONext
     * @param indONext
     * @param id_OUpper
     * @param indOUpper
     * @param indexOnly
     * @param wn2qtynow
     * @param wn2qtyfin
     * @param cn
     * @param wn2qtyneed
     * @param priority
     * @param isZp
     * @param zcnprep
     * @param merge
     * @param dep
     * @param grpB
     * @param subParts
     * @param teDate
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/4/18 10:31
     */
    public static JSONObject newActZ(int bcdStatus, int bispush, int bisactivate, String id_O, String id_OP, int index, int bmdpt
            , Integer sumSonCount, String id_ONext, Integer indONext, String id_OUpper, Integer indOUpper
            , int indexOnly, int wn2qtynow, int wn2qtyfin, String cn, double wn2qtyneed
            , int priority, int isZp, String zcnprep, int merge, String dep, String grpB
            , JSONArray subParts, JSONObject teDate){
        JSONObject actZ = new JSONObject();
        actZ.put("bcdStatus",bcdStatus);
        actZ.put("bispush",bispush);
        actZ.put("bisactivate",bisactivate);
        actZ.put("id_O",id_O);
        if (null != id_OP) {
            actZ.put("id_OP",id_OP);
        }
        actZ.put("index",index);
        actZ.put("bmdpt",bmdpt);
        if (null != sumSonCount) {
            actZ.put("sumSonCount",sumSonCount);
        }
        if (null != id_ONext) {
            actZ.put("id_ONext",id_ONext);
        }
        if (null != indONext) {
            actZ.put("indONext",indONext);
        }
        if (null != id_OUpper) {
            actZ.put("id_OUpper",id_OUpper);
        }
        if (null != indOUpper) {
            actZ.put("indOUpper",indOUpper);
        }
        actZ.put("indexOnly",indexOnly);
        actZ.put("wn2qtynow",wn2qtynow);
        actZ.put("wn2qtyfin",wn2qtyfin);
        JSONObject wcnNPMerge = new JSONObject();
        wcnNPMerge.put("cn",cn);
        actZ.put("wcnNPMerge",wcnNPMerge);
        actZ.put("wn2qtyneed",wn2qtyneed);
        actZ.put("priority",priority);
        actZ.put("isZp",isZp);
        actZ.put("zcnprep",zcnprep);
        actZ.put("merge",merge);
        actZ.put("dep",dep);
        actZ.put("grpB",grpB);
        if (null != subParts) {
            actZ.put("subParts",subParts);
        }
        if (null != teDate) {
            actZ.put("teDate",teDate);
        }
        return actZ;
    }

    public static JSONObject newSubZ(int index,String id_O){
        JSONObject subZ = new JSONObject();
        subZ.put("index",index);
        subZ.put("id_O",id_O);
        return subZ;
    }

    /**
     * 获取全新的任务信息对象方法
     * @param tePStart	预计开始时间
     * @param tePFinish	预计完成时间
     * @param id_O	订单编号
     * @param index	零件位置，配合订单编号使用
     * @param teDurTotal	总共时间
     * @param priority	优先级
     * @param wrdNs	用户名称
     * @param prep	准备时间
     * @param teDelayDate	延迟总时间
     * @return com.cresign.tools.pojo.po.chkin.Task  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 16:58
     */
    public static Task getTask(Long tePStart, Long tePFinish, String id_O, Integer index
            , Long teDurTotal, Integer priority, String wrdNs, Long prep, Long teDelayDate
            , String id_C,Long teCsStart,Long teCsSonOneStart){
        Task task1 = new Task();
        task1.setTePStart(tePStart);
        task1.setTePFinish(tePFinish);
        task1.setId_O(id_O);
        task1.setIndex(index);
        task1.setPriority(priority);
        task1.setId_C(id_C);
        JSONObject wrdN = new JSONObject();
        wrdN.put("cn",wrdNs);
        task1.setWrdN(wrdN);
        task1.setPrep(prep);
        task1.setTeDurTotal(teDurTotal);
//        task1.setTeDelayDate(teDelayDate);
        if (priority == -1) {
            task1.setTeCsStart(0L);
            task1.setTeCsSonOneStart(0L);
            task1.setTeDelayDate(0L);
        } else {
            task1.setTeCsStart(teCsStart);
            task1.setTeCsSonOneStart(teCsSonOneStart);
            task1.setTeDelayDate((tePStart-teCsStart));
        }
        return task1;
    }

    /**
     * 获取全新的任务信息对象方法-新
     * @param tePStart	预计开始时间
     * @param tePFinish	预计完成时间
     * @param teDurTotal 任务总时间
     * @return com.cresign.tools.pojo.po.chkin.Task  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 16:58
     */
    public static Task getTaskX(Long tePStart, Long tePFinish,Long teDurTotal,Task task){
        Task task1 = new Task();
        task1.setTePStart(tePStart);
        task1.setTePFinish(tePFinish);
        task1.setTeDurTotal(teDurTotal);
        task1.setId_O(task.getId_O());
        task1.setIndex(task.getIndex());
        task1.setPriority(task.getPriority());
        task1.setId_C(task.getId_C());
        JSONObject wrdN = new JSONObject();
        wrdN.put("cn",task.getWrdN().getString("cn"));
        task1.setWrdN(wrdN);
        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(teDelayDate);
        if (task1.getPriority() == -1) {
            task1.setTeCsStart(0L);
            task1.setTeCsSonOneStart(0L);
            task1.setTeDelayDate(0L);
        } else {
            task1.setTeCsStart(task.getTeCsStart());
            task1.setTeCsSonOneStart(task.getTeCsSonOneStart());
            task1.setTeDelayDate((task1.getTePStart()-task1.getTeCsStart()));
        }
        return task1;
    }

    public static Task getTaskL(Long tePStart, Long tePFinish,String wrdNs,Task task){
        Task task1 = getTaskC(task,0);
        task1.setTePStart(tePStart);
        task1.setTePFinish(tePFinish);
        JSONObject wrdN = new JSONObject();
        wrdN.put("cn",wrdNs);
        task1.setWrdN(wrdN);

//        task1.setId_O(task.getId_O());
//        task1.setIndex(task.getIndex());
//        task1.setTeDurTotal(task.getTeDurTotal());
//        task1.setPriority(task.getPriority());
//        task1.setId_C(task.getId_C());
//        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(task.getTeDelayDate());
        return task1;
    }

    public static Task getTaskY(Task task){
//        System.out.println("进入-taskY:");
//        System.out.println(JSON.toJSONString(task));
        Task task1 = getTaskC(task,1);
        task1.setTePStart(task.getTePStart());
        task1.setTePFinish(task.getTePFinish());
        task1.setWrdN(task.getWrdN());
        task1.setTeDurTotal(task.getTeDurTotal());
        task1.setTeCsStart(task.getTeCsStart());
        task1.setTeCsSonOneStart(task.getTeCsSonOneStart());
        task1.setTeDelayDate(task.getTePStart()-task.getTeCsStart());

//        task1.setId_O(task.getId_O());
//        task1.setIndex(task.getIndex());
//        task1.setTeDurTotal(task.getTeDurTotal());
//        task1.setPriority(task.getPriority());
//        task1.setId_C(task.getId_C());
//        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(task.getTeDelayDate());
        return task1;
    }

    public static Task getTaskWr(Task task){
//        System.out.println("进入-taskWr:");
//        System.out.println(JSON.toJSONString(task));
        Task task1 = getTaskC(task,2);
        task1.setTePStart(task.getTePStart());
        task1.setTePFinish(task.getTePFinish());
        task1.setTeDurTotal(task.getTeDurTotal());
        task1.setTeCsStart(task.getTeCsStart());
        task1.setTeCsSonOneStart(task.getTeCsSonOneStart());
        JSONObject wrdN = new JSONObject();
        wrdN.put("cn",task.getWrdN().getString("cn"));
        task1.setWrdN(wrdN);
        task1.setTeDelayDate(task.getTePStart()-task.getTeCsStart());

//        task1.setId_O(task.getId_O());
//        task1.setIndex(task.getIndex());
//        task1.setTeDurTotal(task.getTeDurTotal());
//        task1.setPriority(task.getPriority());
//        task1.setId_C(task.getId_C());
//        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(task.getTeDelayDate());
        return task1;
    }

    public static Task getTaskC(Task task,int isP){
        Task task1 = new Task();
        if (isP != 0) {
            task1.setTePStart(task.getTePStart());
            task1.setTePFinish(task.getTePFinish());
            if (isP == 1) {
                task1.setWrdN(task.getWrdN());
            }
        }
        task1.setId_O(task.getId_O());
        task1.setIndex(task.getIndex());
        task1.setTeDurTotal(task.getTeDurTotal());
        task1.setPriority(task.getPriority());
        task1.setId_C(task.getId_C());
        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(task.getTeDelayDate());
        if (task1.getPriority() == -1) {
            task1.setTeCsStart(0L);
            task1.setTeCsSonOneStart(0L);
            task1.setTeDelayDate(0L);
        } else {
            task1.setTeCsStart(task.getTeCsStart());
            task1.setTeCsSonOneStart(task.getTeCsSonOneStart());
            task1.setTeDelayDate(task1.getTePStart()-task1.getTeCsStart());
        }
        return task1;
    }

    /**
     * 添加模拟订单方法
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 21:01
     */
    public static void addOrder(Long teS, CoupaUtil coupaUtil){
        Order order;
        JSONObject action;
        JSONArray objAction;
        JSONObject actZ;
        JSONArray subParts;
        JSONObject teDate;

        order = new Order();
        order.setId("t");

        subParts = new JSONArray();
        subParts.add(Obj.newSubZ(0,"t-1"));
        subParts.add(Obj.newSubZ(1,"t-1"));
        subParts.add(Obj.newSubZ(2,"t-1"));
        actZ = Obj.newActZ(0,0,0,"t",null,0,5,3
                ,null,null,null,null,0,0,0
                ,"t-主产品",210.0,0,0
                ,null,0,null,null,subParts,null);

        objAction = new JSONArray();
        objAction.add(actZ);
        action = new JSONObject();
        action.put("objAction",objAction);

        order.setAction(action);
        coupaUtil.saveOrder(order);

//        System.out.println("-------------------------- 分割线 --------------------------");

        objAction = new JSONArray();
        action = new JSONObject();
        order = new Order();
        order.setId("t-1");

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = Obj.newActZ(0,1,0,"t-1","t",0,3,null
                ,"t-1",1,"t",0,1,1,1
                ,"t-子产品-1",210.0,0,1,""
                ,2,"1xx1","1001",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = Obj.newActZ(0,0,0,"t-1","t",1,1,null
                ,"t-1",2,"t",0,2,1,1
                ,"t-子产品-2",210.0,0,1,""
                ,2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        teDate.put((teS+86400L)+"",0);
        actZ = Obj.newActZ(0,0,0,"t-1","t",2,3,null
                ,null,null,"t",0,3,1,1
                ,"t-子产品-3",210.0,0,1,""
                ,2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        action.put("objAction",objAction);
        order.setAction(action);
        coupaUtil.saveOrder(order);
    }

    /**
     * 添加模拟订单方法2
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 21:01
     */
    public static void addOrder2(Long teS, CoupaUtil coupaUtil){
        Order order;
        JSONObject action;
        JSONArray objAction;
        JSONObject actZ;
        JSONArray subParts;
        JSONObject teDate;

        order = new Order();
        order.setId("t2");

        subParts = new JSONArray();
        subParts.add(Obj.newSubZ(0,"t-2"));
        actZ = Obj.newActZ(0,0,0,"t2",null,0,5,1
                ,null,null,null,null,0,0,0
                ,"t2-主产品",210.0,0,0
                ,null,0,null,null,subParts,null);

        objAction = new JSONArray();
        objAction.add(actZ);
        action = new JSONObject();
        action.put("objAction",objAction);
        order.setAction(action);
        coupaUtil.saveOrder(order);

//        System.out.println("-------------------------- 分割线 --------------------------");

        objAction = new JSONArray();
        action = new JSONObject();
        order = new Order();
        order.setId("t-2");

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = Obj.newActZ(0,1,0,"t-2","t2",0,3,null
                ,null,null,"t2",0,1,1,1
                ,"t2-子产品-1",210.0,0,1
                ,"",2,"1xx1","1001",null,teDate);

        objAction.add(actZ);

        action.put("objAction",objAction);
        order.setAction(action);
        coupaUtil.saveOrder(order);
    }

    /**
     * 添加模拟订单方法3
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 21:01
     */
    public static void addOrder3(Long teS, CoupaUtil coupaUtil){
        teS += (86400L + 86400);
        Order order;
        JSONObject action;
        JSONArray objAction;
        JSONObject actZ;
        JSONArray subParts;
        JSONObject teDate;

        order = new Order();
        order.setId("t3");

        subParts = new JSONArray();
        subParts.add(Obj.newSubZ(0,"t-3"));
        subParts.add(Obj.newSubZ(1,"t-3"));
        subParts.add(Obj.newSubZ(2,"t-3"));
        actZ = Obj.newActZ(0,0,0,"t3",null,0,5,3
                ,null,null,null,null,0,0,0
                ,"t3-主产品",210.0,0,0
                ,null,0,null,null,subParts,null);

        objAction = new JSONArray();
        objAction.add(actZ);
        action = new JSONObject();
        action.put("objAction",objAction);
        order.setAction(action);
        coupaUtil.saveOrder(order);

//        System.out.println("-------------------------- 分割线 --------------------------");

        objAction = new JSONArray();
        action = new JSONObject();
        order = new Order();
        order.setId("t-3");

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = Obj.newActZ(0,1,0,"t-3","t3",0,3,null
                ,"t-3",1,"t3",0,1,1,1
                ,"t3-子产品-1",210.0,0,1
                ,"",2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = Obj.newActZ(0,0,0,"t-3","t3",1,1,null
                ,"t-3",2,"t3",0,2,1,1
                ,"t3-子产品-1",210.0,0,1
                ,"",2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        teDate.put((teS+86400L)+"",0);
        actZ = Obj.newActZ(0,0,0,"t-3","t3",2,3,null
                ,null,null,"t3",0,3,1,1
                ,"t3-子产品-1",210.0,0,1
                ,"",2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        action.put("objAction",objAction);
        order.setAction(action);
        coupaUtil.saveOrder(order);
    }

    /**
     * 根据teS添加teS当天任务
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 21:02
     */
    public static void addTasksAndOrder(Long teS,String id_C, CoupaUtil coupaUtil){
        List<Task> tasks;
        long zon;

        tasks = new ArrayList<>();
//        String id_C = "6076a1c7f3861e40c87fd294";
        tasks.add(Obj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask(teS,(teS+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS+41400),(teS+43200),"t-1",1,1800L,1,"我-1",0L,0L,id_C,(teS+41400),0L));
        tasks.add(Obj.getTask((teS+43200),(teS+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS+50400),(teS+61200),"t-1",1,10800L,1,"我-1",0L,0L,id_C,(teS+50400),0L));
        tasks.add(Obj.getTask((teS+61200),(teS+64800),"t-1",2,3600L,1,"我-1",0L,0L,id_C,(teS+61200),0L));
        tasks.add(Obj.getTask((teS+64800),(teS+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L));
        zon = 12600L;
        TimeZjServiceImpl.setTasksAndZon(tasks,"1000","0xx0",teS,id_C,false,zon,coupaUtil);

//        System.out.println("-------------------------- 分割线 --------------------------");

        long teS2 = teS + 86400;
        tasks = new ArrayList<>();
        tasks.add(Obj.getTask(teS2,teS2,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask(teS2,(teS2+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS2+28800),(teS2+37800),"t-1",2,9000L,1,"我-1",0L,0L,id_C,(teS2+28800),0L));
        tasks.add(Obj.getTask((teS2+43200),(teS2+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS2+64800),(teS2+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L));
        zon = 19800L;
        TimeZjServiceImpl.setTasksAndZon(tasks,"1000","0xx0",teS2,id_C,false,zon,coupaUtil);
    }

    /**
     * 根据teS添加teS当天任务2
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 21:02
     */
    public static void addTasksAndOrder3(Long teS,String id_C, CoupaUtil coupaUtil){
        List<Task> tasks;
        long zon;

        teS += (86400 + 86400);

        tasks = new ArrayList<>();
//        String id_C = "6076a1c7f3861e40c87fd294";
        tasks.add(Obj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask(teS,(teS+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS+28800),(teS+41400),"t-3",0,12600L,2,"我-3",0L,0L,id_C,(teS+28800),0L));
        tasks.add(Obj.getTask((teS+41400),(teS+43200),"t-3",1,1800L,2,"我-3",0L,0L,id_C,(teS+41400),0L));
        tasks.add(Obj.getTask((teS+43200),(teS+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS+50400),(teS+61200),"t-3",1,10800L,2,"我-3",0L,0L,id_C,(teS+50400),0L));
        tasks.add(Obj.getTask((teS+61200),(teS+64800),"t-3",2,3600L,2,"我-3",0L,0L,id_C,(teS+61200),0L));
        tasks.add(Obj.getTask((teS+64800),(teS+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L));

        zon = 0L;
//        zon = 16200L;
        TimeZjServiceImpl.setTasksAndZon(tasks,"1000","0xx0",teS,id_C,false,zon,coupaUtil);

//        System.out.println("-------------------------- 分割线 --------------------------");

        long teS2 = teS + 86400;
        tasks = new ArrayList<>();
        tasks.add(Obj.getTask(teS2,teS2,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask(teS2,(teS2+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS2+28800),(teS2+37800),"t-3",2,9000L,2,"我-3",0L,0L,id_C,(teS2+28800),0L));
        tasks.add(Obj.getTask((teS2+43200),(teS2+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS2+64800),(teS2+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L));

        zon = 19800L;
        TimeZjServiceImpl.setTasksAndZon(tasks,"1000","0xx0",teS2,id_C,false,zon,coupaUtil);
    }

    /**
     * 根据teS，grpB，dep添加任务信息
     * @param teS	当前时间戳
     * @param grpB	组别
     * @param dep	部门
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 21:03
     */
    public static void addTasks(Long teS,String grpB,String dep,String id_C, CoupaUtil coupaUtil){
        System.out.println("-----进入添加模拟信息-----");
//        System.out.println("teS:"+teS+",-- grpB:"+grpB+",-- dep:"+dep);
        List<Task> tasks;

        tasks = new ArrayList<>();
//        String id_C = "6076a1c7f3861e40c87fd294";
        tasks.add(Obj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask(teS,(teS+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS+28800),(teS+41400),"t-1",0,12600L,1,"我-1",0L,0L,id_C,(teS+28800),0L));
        tasks.add(Obj.getTask((teS+41400),(teS+43200),"t-2",0,1800L,1,"我-2",0L,0L,id_C,(teS+41400),0L));
        tasks.add(Obj.getTask((teS+43200),(teS+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L));
        tasks.add(Obj.getTask((teS+64800),(teS+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L));

        long zon;
//        zon = 16200L;
        zon = 14400L;
        TimeZjServiceImpl.setTasksAndZon(tasks,grpB,dep,teS,id_C,true,zon,coupaUtil);
    }

}
