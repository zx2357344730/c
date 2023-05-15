package com.cresign.action.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.impl.TimeZjServiceImplX;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;

import java.util.ArrayList;
import java.util.List;
/**
 * @ClassName NewObj
 * @Description 作者很懒什么也没写
 * @author tang
 * @Date 2022/4/18 10:36
 * @ver 1.0.0
 */
public class TaskObj {

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
//            actZ.put("id_ONext",id_ONext);
            JSONArray prtNext = new JSONArray();
            JSONObject nextObj = new JSONObject();
            nextObj.put("index",indONext);
            nextObj.put("id_O",id_ONext);
            prtNext.add(nextObj);
            actZ.put("prtNext",prtNext);
        }
//        if (null != indONext) {
//            actZ.put("indONext",indONext);
//        }
        if (null != id_OUpper) {
//            actZ.put("id_OUpper",id_OUpper);
            JSONArray upPrnts = new JSONArray();
            JSONObject upObj = new JSONObject();
            upObj.put("index",indOUpper);
            upObj.put("id_O",id_OUpper);
            JSONObject wrdN = new JSONObject();
            wrdN.put("cn","测试-父名称");
            upObj.put("wrdN",wrdN);
            upObj.put("wn2qtyneed","1.0");
            upPrnts.add(upObj);
            actZ.put("upPrnts",upPrnts);
        }
//        if (null != indOUpper) {
//            actZ.put("indOUpper",indOUpper);
//        }
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

    public static JSONObject getTaskThis(Integer priority,String id_O,Integer index,Long tePStart
            ,Long prep,String wrdNStr,Long teDurTotal,Long teDelayDate,String id_C,Long teCsStart
            ,Long teCsSonOneStart){
        JSONObject task = new JSONObject();
        task.put("priority",priority);
        task.put("id_O",id_O);
        task.put("index",index);
        task.put("tePStart",tePStart);
        task.put("prep",prep);
        JSONObject wrdN = new JSONObject();
        wrdN.put("cn",wrdNStr);
        task.put("wrdN",wrdN);
        task.put("teDurTotal",teDurTotal);
        task.put("teDelayDate",teDelayDate);
        task.put("id_C",id_C);
        task.put("teCsStart",teCsStart);
        task.put("teCsSonOneStart",teCsSonOneStart);
        return task;
    }

    public static JSONObject getDateThis(Double wn2qtyneed,String id_C,Integer kaiJie,Integer csSta
            ,String grpB,Integer priority,Long teDur,Long teDurTotal,Long teStart,Long taFin,Long tePrep
            ,String id_O,Integer index,Long teDelayDate,String id_PF,String id_P,Integer priorItem
            ,boolean empty){
        JSONObject date = new JSONObject();
        date.put("wn2qtyneed",wn2qtyneed);
        date.put("id_C",id_C);
        date.put("kaiJie",kaiJie);
        date.put("csSta",csSta);
        date.put("grpB",grpB);
        date.put("priority",priority);
        date.put("teDur",teDur);
        date.put("teDurTotal",teDurTotal);
        date.put("teStart",teStart);
        date.put("taFin",taFin);
        date.put("tePrep",tePrep);
        date.put("id_O",id_O);
        date.put("index",index);
        date.put("teDelayDate",teDelayDate);
        date.put("id_PF",id_PF);
        date.put("id_P",id_P);
        date.put("priorItem",priorItem);
        date.put("empty",empty);
        return date;
    }

    /**
     * 获取全新的任务信息对象方法-只用于测试
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
     * @ver 1.0.0
     * @date 2022/2/18 16:58
     */
    public static Task getTask(Long tePStart, Long tePFinish, String id_O, Integer index
            , Long teDurTotal, Integer priority, String wrdNs, Long prep
            , Long teDelayDate
            , String id_C,Long teCsStart,Long teCsSonOneStart,Integer dateIndex,boolean isNextPart){
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
        task1.setDateIndex(dateIndex);
        task1.setIsNextPart(isNextPart);
        return task1;
    }

    /**
     * 获取全新的任务信息对象方法-计算真实使用的
     * @param tePStart	预计开始时间
     * @param tePFinish	预计完成时间
     * @param teDurTotal 任务总时间
     * @return com.cresign.tools.pojo.po.chkin.Task  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/18 16:58
     */
    public static Task getTaskX(Long tePStart, Long tePFinish,Long teDurTotal,Task task){
        Task taskNew = new Task();
        taskNew.setTePStart(tePStart);
        taskNew.setTePFinish(tePFinish);
        taskNew.setTeDurTotal(teDurTotal);
        taskNew.setId_O(task.getId_O());
        taskNew.setIndex(task.getIndex());
        taskNew.setPriority(task.getPriority());
        taskNew.setId_C(task.getId_C());
        JSONObject wrdN = new JSONObject();
        JSONObject wrdNX = task.getWrdN();
        String cn = wrdNX.getString("cn");
        wrdN.put("cn", cn);
        taskNew.setWrdN(wrdN);

//        taskNew.setWrdN(task.getWrdN());

        taskNew.setPrep(task.getPrep());
        taskNew.setDateIndex(task.getDateIndex());
        if (null != task.getMergeTasks() && task.getMergeTasks().size() > 0) {
            taskNew.setMergeTasks(task.getMergeTasks());
        }
//        taskNew.setTeDelayDate(teDelayDate);
        if (taskNew.getPriority() == -1) {
            taskNew.setTeCsStart(0L);
            taskNew.setTeCsSonOneStart(0L);
            taskNew.setTeDelayDate(0L);
            taskNew.setTaOver(0L);
            taskNew.setIsNextPart(false);
        } else {
            taskNew.setTeCsStart(task.getTeCsStart());
            taskNew.setTeCsSonOneStart(task.getTeCsSonOneStart());
            taskNew.setTeDelayDate((taskNew.getTePStart()-task.getTeCsStart()));
            taskNew.setTaOver(task.getTaOver());
            taskNew.setIsNextPart(task.getIsNextPart());
        }
        return taskNew;
    }

//    public static Task getTaskL(Long tePStart, Long tePFinish,String wrdNs,Task task){
//        Task task1 = getTaskC(task,0);
//        task1.setTePStart(tePStart);
//        task1.setTePFinish(tePFinish);
//        JSONObject wrdN = new JSONObject();
//        wrdN.put("cn",wrdNs);
//        task1.setWrdN(wrdN);
//
////        task1.setId_O(task.getId_O());
////        task1.setIndex(task.getIndex());
////        task1.setTeDurTotal(task.getTeDurTotal());
////        task1.setPriority(task.getPriority());
////        task1.setId_C(task.getId_C());
////        task1.setPrep(task.getPrep());
////        task1.setTeDelayDate(task.getTeDelayDate());
//        return task1;
//    }

    public static Task getTaskY(Task task){
//        System.out.println("进入-taskY:");
//        System.out.println(JSON.toJSONString(task));
        Task taskNew = getTaskC(task,1);
        taskNew.setTePStart(task.getTePStart());
        taskNew.setTePFinish(task.getTePFinish());
        taskNew.setWrdN(task.getWrdN());
        taskNew.setTeDurTotal(task.getTeDurTotal());
        taskNew.setTeCsStart(task.getTeCsStart());
        taskNew.setTeCsSonOneStart(task.getTeCsSonOneStart());
        taskNew.setTeDelayDate(task.getTePStart()-task.getTeCsStart());

//        task1.setId_O(task.getId_O());
//        task1.setIndex(task.getIndex());
//        task1.setTeDurTotal(task.getTeDurTotal());
//        task1.setPriority(task.getPriority());
//        task1.setId_C(task.getId_C());
//        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(task.getTeDelayDate());
        return taskNew;
    }

    public static Task getTaskWr(Task task){
//        System.out.println("进入-taskWr:");
//        System.out.println(JSON.toJSONString(task));
        Task taskNew = getTaskC(task,2);
        taskNew.setTePStart(task.getTePStart());
        taskNew.setTePFinish(task.getTePFinish());
        taskNew.setTeDurTotal(task.getTeDurTotal());
        taskNew.setTeCsStart(task.getTeCsStart());
        taskNew.setTeCsSonOneStart(task.getTeCsSonOneStart());
        JSONObject wrdN = new JSONObject();
        wrdN.put("cn",task.getWrdN().getString("cn"));
        taskNew.setWrdN(wrdN);
        taskNew.setTeDelayDate(task.getTePStart()-task.getTeCsStart());

//        task1.setId_O(task.getId_O());
//        task1.setIndex(task.getIndex());
//        task1.setTeDurTotal(task.getTeDurTotal());
//        task1.setPriority(task.getPriority());
//        task1.setId_C(task.getId_C());
//        task1.setPrep(task.getPrep());
//        task1.setTeDelayDate(task.getTeDelayDate());
        return taskNew;
    }

    public static Task getTaskC(Task task,int isP){
        Task taskNew = new Task();
        if (isP != 0) {
            taskNew.setTePStart(task.getTePStart());
            taskNew.setTePFinish(task.getTePFinish());
            if (isP == 1) {
                taskNew.setWrdN(task.getWrdN());
            }
        }
        taskNew.setId_O(task.getId_O());
        taskNew.setIndex(task.getIndex());
        taskNew.setTeDurTotal(task.getTeDurTotal());
        taskNew.setPriority(task.getPriority());
        taskNew.setId_C(task.getId_C());
        taskNew.setPrep(task.getPrep());
        taskNew.setDateIndex(task.getDateIndex());
//        task1.setTeDelayDate(task.getTeDelayDate());
        if (taskNew.getPriority() == -1) {
            taskNew.setTeCsStart(0L);
            taskNew.setTeCsSonOneStart(0L);
            taskNew.setTeDelayDate(0L);
            taskNew.setTaOver(0L);
            taskNew.setIsNextPart(false);
        } else {
            taskNew.setTeCsStart(task.getTeCsStart());
            taskNew.setTeCsSonOneStart(task.getTeCsSonOneStart());
            taskNew.setTeDelayDate(taskNew.getTePStart()-taskNew.getTeCsStart());
            taskNew.setTaOver(task.getTaOver());
            taskNew.setIsNextPart(task.getIsNextPart());
        }
        return taskNew;
    }

    /**
     * 添加模拟订单方法
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/17 21:01
     */
    public static void addOrder(Long teS, Qt qt){
        Order order;
        JSONObject action;
        JSONArray objAction;
        JSONObject actZ;
        JSONArray subParts;
        JSONObject teDate;

        order = new Order();
        order.setId("t");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId_C("6076a1c7f3861e40c87fd294");
        order.setInfo(orderInfo);

        subParts = new JSONArray();
        subParts.add(TaskObj.newSubZ(0,"t-1"));
        subParts.add(TaskObj.newSubZ(1,"t-1"));
        subParts.add(TaskObj.newSubZ(2,"t-1"));
        actZ = TaskObj.newActZ(0,0,0,"t",null,0,5,3
                ,null,null,null,null,0,0,0
                ,"t-主产品",210.0,0,0
                ,null,0,null,null,subParts,null);

        objAction = new JSONArray();
        objAction.add(actZ);
        action = new JSONObject();
        action.put("objAction",objAction);

        JSONObject grpBGroup = new JSONObject();
        JSONObject grpBInfo = new JSONObject();
        grpBInfo.put("dep","1xx1");
        grpBGroup.put("1001",grpBInfo);
        grpBInfo = new JSONObject();
        grpBInfo.put("dep","0xx0");
        grpBGroup.put("1000",grpBInfo);
        action.put("grpBGroup",grpBGroup);

        JSONArray oDates = new JSONArray();
        JSONArray oTasks = new JSONArray();

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1001"
                ,1,12000L,0L,0L,0L,600L,"t-1",0
                ,0L,"t","t-1-1",0,false));

        oTasks.add(getTaskThis(1,"t-1",0,0L,600L,"t-1-1",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1000"
                ,1,12000L,0L,0L,0L,600L,"t-1",1
                ,0L,"t","t-1-2",0,false));

        oTasks.add(getTaskThis(1,"t-1",1,0L,600L,"t-1-2",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1000"
                ,1,12000L,0L,0L,0L,600L,"t-1",2
                ,0L,"t","t-1-3",0,false));

        oTasks.add(getTaskThis(1,"t-1",2,0L,600L,"t-1-3",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        action.put("oDates",oDates);
        action.put("oTasks",oTasks);

        order.setAction(action);
//        coupaUtil.delOrder(order.getId());
//        coupaUtil.saveOrder(order);
        qt.delMD(order.getId(),Order.class);
        qt.addMD(order);

//        System.out.println("-------------------------- 分割线 --------------------------");

        objAction = new JSONArray();
        action = new JSONObject();
        order = new Order();
        order.setId("t-1");
        orderInfo = new OrderInfo();
        orderInfo.setId_C("6076a1c7f3861e40c87fd294");
        orderInfo.setId_OP("t");
        order.setInfo(orderInfo);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = TaskObj.newActZ(0,1,0,"t-1","t",0,3,null
                ,"t-1",1,"t",0,1,1,1
                ,"t-子产品-1",210.0,0,1,""
                ,2,"1xx1","1001",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = TaskObj.newActZ(0,0,0,"t-1","t",1,1,null
                ,"t-1",2,"t",0,2,1,1
                ,"t-子产品-2",210.0,0,1,""
                ,2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        teDate.put((teS+86400L)+"",0);
        actZ = TaskObj.newActZ(0,0,0,"t-1","t",2,3,null
                ,null,null,"t",0,3,1,1
                ,"t-子产品-3",210.0,0,1,""
                ,2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        grpBGroup = new JSONObject();
        grpBInfo = new JSONObject();
        grpBInfo.put("dep","1xx1");
        grpBGroup.put("1001",grpBInfo);
        grpBInfo = new JSONObject();
        grpBInfo.put("dep","0xx0");
        grpBGroup.put("1000",grpBInfo);
        action.put("grpBGroup",grpBGroup);

        action.put("objAction",objAction);
        order.setAction(action);
//        coupaUtil.delOrder(order.getId());
//        coupaUtil.saveOrder(order);
        qt.delMD(order.getId(),Order.class);
        System.out.println("新增的order-t-1:");
        System.out.println(JSON.toJSONString(order));
        qt.addMD(order);
    }

    /**
     * 添加模拟订单方法2
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/17 21:01
     */
    public static void addOrder2(Long teS,Qt qt){
        Order order;
        JSONObject action;
        JSONArray objAction;
        JSONObject actZ;
        JSONArray subParts;
        JSONObject teDate;

        order = new Order();
        order.setId("t2");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId_C("6076a1c7f3861e40c87fd294");
        order.setInfo(orderInfo);

        subParts = new JSONArray();
        subParts.add(TaskObj.newSubZ(0,"t-2"));
        actZ = TaskObj.newActZ(0,0,0,"t2",null,0,5,1
                ,null,null,null,null,0,0,0
                ,"t2-主产品",210.0,0,0
                ,null,0,null,null,subParts,null);

        objAction = new JSONArray();
        objAction.add(actZ);
        action = new JSONObject();
        action.put("objAction",objAction);

        JSONObject grpBGroup = new JSONObject();
        JSONObject grpBInfo = new JSONObject();
        grpBInfo.put("dep","1xx1");
        grpBGroup.put("1001",grpBInfo);
        action.put("grpBGroup",grpBGroup);

        JSONArray oDates = new JSONArray();
        JSONArray oTasks = new JSONArray();

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1001"
                ,1,12000L,0L,0L,0L,600L,"t-2",0
                ,0L,"t2","t-2-1",0,false));

        oTasks.add(getTaskThis(1,"t-2",0,0L,600L,"t-2-1",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        action.put("oDates",oDates);
        action.put("oTasks",oTasks);

        order.setAction(action);

//        coupaUtil.delOrder(order.getId());
//        coupaUtil.saveOrder(order);
        qt.delMD(order.getId(),Order.class);
        qt.addMD(order);

//        System.out.println("-------------------------- 分割线 --------------------------");

        objAction = new JSONArray();
        action = new JSONObject();
        order = new Order();
        order.setId("t-2");
        orderInfo = new OrderInfo();
        orderInfo.setId_C("6076a1c7f3861e40c87fd294");
        orderInfo.setId_OP("t2");
        order.setInfo(orderInfo);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = TaskObj.newActZ(0,1,0,"t-2","t2",0,3,null
                ,null,null,"t2",0,1,1,1
                ,"t2-子产品-1",210.0,0,1
                ,"",2,"1xx1","1001",null,teDate);

        objAction.add(actZ);

        grpBGroup = new JSONObject();
        grpBInfo = new JSONObject();
        grpBInfo.put("dep","1xx1");
        grpBGroup.put("1001",grpBInfo);
        action.put("grpBGroup",grpBGroup);

        action.put("objAction",objAction);
        order.setAction(action);
//        coupaUtil.delOrder(order.getId());
//        coupaUtil.saveOrder(order);
        qt.delMD(order.getId(),Order.class);
        qt.addMD(order);
    }

    /**
     * 添加模拟订单方法3
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/17 21:01
     */
    public static void addOrder3(Long teS, Qt qt){
        teS += (86400L + 86400);
        Order order;
        JSONObject action;
        JSONArray objAction;
        JSONObject actZ;
        JSONArray subParts;
        JSONObject teDate;

        order = new Order();
        order.setId("t3");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId_C("6076a1c7f3861e40c87fd294");
        order.setInfo(orderInfo);

        subParts = new JSONArray();
        subParts.add(TaskObj.newSubZ(0,"t-3"));
        subParts.add(TaskObj.newSubZ(1,"t-3"));
        subParts.add(TaskObj.newSubZ(2,"t-3"));
        actZ = TaskObj.newActZ(0,0,0,"t3",null,0,5,3
                ,null,null,null,null,0,0,0
                ,"t3-主产品",210.0,0,0
                ,null,0,null,null,subParts,null);

        objAction = new JSONArray();
        objAction.add(actZ);
        action = new JSONObject();
        action.put("objAction",objAction);

        JSONObject grpBGroup = new JSONObject();
        JSONObject grpBInfo = new JSONObject();
        grpBInfo.put("dep","0xx0");
        grpBGroup.put("1000",grpBInfo);
        action.put("grpBGroup",grpBGroup);

        JSONArray oDates = new JSONArray();
        JSONArray oTasks = new JSONArray();

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1000"
                ,1,12000L,0L,0L,0L,600L,"t-3",0
                ,0L,"t3","t-3-1",0,false));

        oTasks.add(getTaskThis(1,"t-3",0,0L,600L,"t-3-1",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1000"
                ,1,12000L,0L,0L,0L,600L,"t-3",1
                ,0L,"t3","t-3-2",0,false));

        oTasks.add(getTaskThis(1,"t-3",1,0L,600L,"t-3-2",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        oDates.add(getDateThis(1.0,"6076a1c7f3861e40c87fd294",2,0,"1000"
                ,1,12000L,0L,0L,0L,600L,"t-3",2
                ,0L,"t3","t-3-3",0,false));

        oTasks.add(getTaskThis(1,"t-3",2,0L,600L,"t-3-3",0L
                ,0L,"6076a1c7f3861e40c87fd294",0L,0L));

        action.put("oDates",oDates);
        action.put("oTasks",oTasks);

        order.setAction(action);
//        coupaUtil.delOrder(order.getId());
//        coupaUtil.saveOrder(order);
        qt.delMD(order.getId(),Order.class);
        qt.addMD(order);

//        System.out.println("-------------------------- 分割线 --------------------------");

        objAction = new JSONArray();
        action = new JSONObject();
        order = new Order();
        order.setId("t-3");
        orderInfo = new OrderInfo();
        orderInfo.setId_C("6076a1c7f3861e40c87fd294");
        orderInfo.setId_OP("t3");
        order.setInfo(orderInfo);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = TaskObj.newActZ(0,1,0,"t-3","t3",0,3,null
                ,"t-3",1,"t3",0,1,1,1
                ,"t3-子产品-1",210.0,0,1
                ,"",2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        actZ = TaskObj.newActZ(0,0,0,"t-3","t3",1,1,null
                ,"t-3",2,"t3",0,2,1,1
                ,"t3-子产品-1",210.0,0,1
                ,"",2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        teDate = new JSONObject();
        // 添加当前处理的任务的所在日期对象状态
        teDate.put(teS+"",0);
        teDate.put((teS+86400L)+"",0);
        actZ = TaskObj.newActZ(0,0,0,"t-3","t3",2,3,null
                ,null,null,"t3",0,3,1,1
                ,"t3-子产品-1",210.0,0,1
                ,"",2,"0xx0","1000",null,teDate);

        objAction.add(actZ);

        grpBGroup = new JSONObject();
        grpBInfo = new JSONObject();
        grpBInfo.put("dep","0xx0");
        grpBGroup.put("1000",grpBInfo);
        action.put("grpBGroup",grpBGroup);

        action.put("objAction",objAction);
        order.setAction(action);
//        coupaUtil.delOrder(order.getId());
//        coupaUtil.saveOrder(order);
        qt.delMD(order.getId(),Order.class);
        qt.addMD(order);
    }

    /**
     * 根据teS添加teS当天任务
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/17 21:02
     */
    public static void addTasksAndOrder(Long teS,String id_C,JSONObject objTaskAll){
        List<Task> tasks;
        long zon;

        tasks = new ArrayList<>();
//        String id_C = "6076a1c7f3861e40c87fd294";
        tasks.add(TaskObj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask(teS,(teS+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS+41400),(teS+43200),"t-1",1,1800L,1,"我-1",0L,0L,id_C,(teS+41400),0L,1,true));
        tasks.add(TaskObj.getTask((teS+43200),(teS+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS+50400),(teS+61200),"t-1",1,10800L,1,"我-1",0L,0L,id_C,(teS+50400),0L,1,true));
        tasks.add(TaskObj.getTask((teS+61200),(teS+64800),"t-1",2,3600L,1,"我-1",0L,0L,id_C,(teS+61200),0L,2,false));
        tasks.add(TaskObj.getTask((teS+64800),(teS+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        long teCsSta = (teS+61200);
        zon = 12600L;
        TimeZjServiceImplX.setTasksAndZon(tasks,"1000","0xx0",teS,zon,objTaskAll);

//        System.out.println("-------------------------- 分割线 --------------------------");

        long teS2 = teS + 86400;
        tasks = new ArrayList<>();
        tasks.add(TaskObj.getTask(teS2,teS2,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask(teS2,(teS2+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS2+28800),(teS2+37800),"t-1",2,9000L,1,"我-1",0L,0L,id_C,teCsSta,0L,2,false));
        tasks.add(TaskObj.getTask((teS2+43200),(teS2+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS2+64800),(teS2+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        zon = 19800L;
        TimeZjServiceImplX.setTasksAndZon(tasks,"1000","0xx0",teS2,zon,objTaskAll);
    }

    /**
     * 根据teS添加teS当天任务2
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/17 21:02
     */
    public static void addTasksAndOrder3(Long teS,String id_C,JSONObject objTaskAll){
        List<Task> tasks;
        long zon;

        teS += (86400 + 86400);

        tasks = new ArrayList<>();
//        String id_C = "6076a1c7f3861e40c87fd294";
        tasks.add(TaskObj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask(teS,(teS+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS+28800),(teS+41400),"t-3",0,12600L,2,"我-3",0L,0L,id_C,(teS+28800),0L,0,true));
        tasks.add(TaskObj.getTask((teS+41400),(teS+43200),"t-3",1,1800L,2,"我-3",0L,0L,id_C,(teS+41400),0L,1,true));
        tasks.add(TaskObj.getTask((teS+43200),(teS+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS+50400),(teS+61200),"t-3",1,10800L,2,"我-3",0L,0L,id_C,(teS+50400),0L,1,true));
        tasks.add(TaskObj.getTask((teS+61200),(teS+64800),"t-3",2,3600L,2,"我-3",0L,0L,id_C,(teS+61200),0L,2,false));
        tasks.add(TaskObj.getTask((teS+64800),(teS+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        long teCsSta = (teS+61200);
        zon = 0L;
//        zon = 16200L;
        TimeZjServiceImplX.setTasksAndZon(tasks,"1000","0xx0",teS,zon,objTaskAll);

//        System.out.println("-------------------------- 分割线 --------------------------");

        long teS2 = teS + 86400;
        tasks = new ArrayList<>();
        tasks.add(TaskObj.getTask(teS2,teS2,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask(teS2,(teS2+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS2+28800),(teS2+37800),"t-3",2,9000L,2,"我-3",0L,0L,id_C,teCsSta,0L,2,false));
        tasks.add(TaskObj.getTask((teS2+43200),(teS2+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS2+64800),(teS2+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));

        zon = 19800L;
        TimeZjServiceImplX.setTasksAndZon(tasks,"1000","0xx0",teS2,zon,objTaskAll);
    }

    /**
     * 根据teS，grpB，dep添加任务信息
     * @param teS	当前时间戳
     * @param grpB	组别
     * @param dep	部门
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/2/17 21:03
     */
    public static void addTasks(Long teS,String grpB,String dep,String id_C,JSONObject objTaskAll){
        System.out.println("-----进入添加模拟信息-----");
//        System.out.println("teS:"+teS+",-- grpB:"+grpB+",-- dep:"+dep);
        List<Task> tasks;

        tasks = new ArrayList<>();
//        String id_C = "6076a1c7f3861e40c87fd294";
        tasks.add(TaskObj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask(teS,(teS+28800),"",-1,28800L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS+28800),(teS+41400),"t-1",0,12600L,1,"我-1",0L,0L,id_C,(teS+28800),0L,0,true));
        tasks.add(TaskObj.getTask((teS+41400),(teS+43200),"t-2",0,1800L,1,"我-2",0L,0L,id_C,(teS+41400),0L,0,false));
        tasks.add(TaskObj.getTask((teS+43200),(teS+50400),"",-1,7200L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));
        tasks.add(TaskObj.getTask((teS+64800),(teS+86400),"",-1,21600L,-1,"电脑",0L,0L,id_C,0L,0L,-1,false));

        long zon;
//        zon = 16200L;
        zon = 14400L;
        TimeZjServiceImplX.setTasksAndZon(tasks,grpB,dep,teS,zon,objTaskAll);
    }

    public static JSONArray getXbJson(){
        JSONArray objXb = new JSONArray();
        JSONObject objXbZ = new JSONObject();
        objXbZ.put("priority",-1);
        objXbZ.put("tePStart",0L);
        objXbZ.put("tePFinish",28800L);
        objXbZ.put("zon",28800L);
        objXb.add(objXbZ);
        objXbZ = new JSONObject();
        objXbZ.put("priority",-1);
        objXbZ.put("tePStart",43200L);
        objXbZ.put("tePFinish",50400L);
        objXbZ.put("zon",7200L);
        objXb.add(objXbZ);
        objXbZ = new JSONObject();
        objXbZ.put("priority",-1);
        objXbZ.put("tePStart",64800L);
        objXbZ.put("tePFinish",86400L);
        objXbZ.put("zon",21600L);
        objXb.add(objXbZ);
        return objXb;
    }

    public static JSONArray getSbJson(){
        JSONArray objSb = new JSONArray();
        JSONObject objSbZ = new JSONObject();
        objSbZ.put("priority",0);
        objSbZ.put("tePStart",28800L);
        objSbZ.put("tePFinish",43200L);
        objSbZ.put("zon",14400L);
        objSb.add(objSbZ);
        objSbZ = new JSONObject();
        objSbZ.put("priority",1);
        objSbZ.put("tePStart",50400L);
        objSbZ.put("tePFinish",64800L);
        objSbZ.put("zon",14400L);
        objSb.add(objSbZ);
        return objSb;
    }

}
