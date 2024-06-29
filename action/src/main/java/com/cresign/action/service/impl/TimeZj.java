package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.*;
import com.cresign.action.utils.GsThisInfo;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.chkin.Task;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description 时间处理主类，包含基本的工具
 * @ClassName TimeZj
 * @Author tang
 * @Date 2022/10/11
 * @Version 1.0.0
 */
@Service
public class TimeZj {

    @Resource
    protected Qt qt;

    @Resource
    protected TimeZjService timeZjService;

    @Resource
    protected TimeZjServiceEmptyInsert timeZjServiceEmptyInsert;

    @Resource
    protected TimeZjServiceTimeConflict timeZjServiceTimeConflict;

    @Resource
    protected TimeZjServiceComprehensive timeZjServiceComprehensive;

    @Resource
    protected TimeZjServiceNew timeZjServiceNew;

//    protected final String timeCard = "aArrange2";
    protected final String timeCard = "aArrange";

    /* 以下五个是记录时间处理出现的问题,并且使用了全局唯一编号 */
    /**
     * 跳天强制停止参数
     */
    public static final JSONObject yiShu = new JSONObject();
    /**
     * 空插冲突强制停止参数
     */
    public static final JSONObject leiW = new JSONObject();
    /**
     * isJ强制停止参数
     */
    public static final JSONObject xin = new JSONObject();
    /**
     * 强制停止出现后的记录参数
     */
    public static final JSONObject isQzTz = new JSONObject();

    public static int isZ = 6;

    /**
     * 获取任务综合信息方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param isUseTimeStamp	is = 0 使用random，grpB，dep获取当前时间戳、is = 1 使用teS为时间戳
     * @param teS	当前时间戳
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @param objTaskAll	全局任务信息
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    protected Map<String,Object> getJumpDay(String random,String grpB,String dep,int isUseTimeStamp,Long teS
            ,Integer isGetTaskPattern,String id_C,JSONObject xbAndSbAll,JSONObject objTaskAll
            ,JSONObject allImageTotalTime,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp,JSONObject depAllTime){
//        System.out.println(" <- 获取新任务列表 -> ");
        // 定义返回对象
        Map<String,Object> result;
        // 定义任务集合
        List<Task> tasks;
        // 定义任务余剩时间
        long zon;
        // 判断是使用random，grpB，dep获取当前时间戳
        if (isUseTimeStamp == 0) {
            // 判断是获取数据库任务信息
            if (isGetTaskPattern == 0) {
                System.out.println("进入jump-1:"+getTeS(random, grpB, dep,onlyFirstTimeStamp
                        ,newestLastCurrentTimestamp));
                // 获取指定的全局任务信息方法
                JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep,onlyFirstTimeStamp
                        ,newestLastCurrentTimestamp), grpB, dep, id_C,objTaskAll,depAllTime);
                // 获取任务信息
                Object[] tasksIs = isTasksNull(tasksAndZon,depAllTime.getJSONObject(dep).getJSONObject(grpB)
                        .getLong(getTeS(random, grpB, dep,onlyFirstTimeStamp
                                ,newestLastCurrentTimestamp)+""));
//                System.out.println("isTasksNull:");
//                System.out.println(JSON.toJSONString(tasksIs));
                tasks = (List<Task>) tasksIs[0];
                zon = (long) tasksIs[1];
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 创建返回对象
                    result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                    // 添加返回值，任务余剩时间
                    result.put("zon",zon);
                    // 添加返回值，任务集合
                    result.put("tasks",tasks);
                } else {
                    // 调用方法获取返回对象信息
                    result = getChkInJumpDayByRandom(random,grpB,dep,id_C,xbAndSbAll,onlyFirstTimeStamp
                            ,newestLastCurrentTimestamp,getTeS(random, grpB, dep,onlyFirstTimeStamp
                                    ,newestLastCurrentTimestamp));
                    System.out.println("getChkInJumpDayByRandom:");
                    System.out.println(JSON.toJSONString(result));
                }
            }
            else {
                System.out.println("进入jump-2:");
                // 创建返回对象
                result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
//                System.out.println("newest:");
//                System.out.println(JSON.toJSONString(newestLastCurrentTimestamp));
//                System.out.println(random);
                // 调用获取镜像任务信息获取任务集合
                tasks = getImageTasks(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),grpB,dep,allImageTasks);
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    System.out.println("进入jump-2-1:");
//                    // 调用获取镜像任务余剩时间获取任务余剩时间
//                    zon = getImageZon(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),grpB,dep,allImageTotalTime);
                    long zuiZon = 86400;
                    for (Task task : tasks) {
                        zuiZon -= task.getWntDurTotal();
                    }
                    zon = zuiZon;
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep,onlyFirstTimeStamp
                            ,newestLastCurrentTimestamp), grpB, dep, id_C,objTaskAll,depAllTime);
                    List<Task> tasksNew;
                    System.out.println("tasksAndZon:"+dep);
                    System.out.println(JSON.toJSONString(tasksAndZon));
                    System.out.println(JSON.toJSONString(depAllTime));
                    Object[] tasksIs = isTasksNull(tasksAndZon,depAllTime.getJSONObject(dep).getJSONObject(grpB)
                            .getLong(getTeS(random, grpB, dep,onlyFirstTimeStamp
                                    ,newestLastCurrentTimestamp)+""));
                    tasksNew = (List<Task>) tasksIs[0];
                    zon = (long) tasksIs[1];
                    // 判断任务集合不为空
                    if (tasksNew.size() != 0) {
                        System.out.println("进入jump-2-2:");
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        // 深度拷贝tasks1数组到tasks
                        CollectionUtils.addAll(tasks, new Object[tasksNew.size()]);
                        Collections.copy(tasks,tasksNew);
                    } else {
                        System.out.println("进入jump-2-3:");
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp));
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
//                        zon = getImageZon(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp),grpB,dep,allImageTotalTime);
//                        System.out.println(zon);
                        // 等于0是为了重新计算总时间
                        zon = 0;
                        Object[] objects = generateTasks(chkIn, zon, offWork, random, grpB, dep
                                , onlyFirstTimeStamp, newestLastCurrentTimestamp, id_C);
                        tasks = (List<Task>) objects[0];
                        zon = (long) objects[1];
//                        System.out.println(zon);
                    }
                }
                // 设置返回对象信息
                result.put("zon",zon);
                result.put("tasks",tasks);
            }
        }
        else {
            // 判断是获取数据库任务信息
            if (isGetTaskPattern == 0) {
                System.out.println("进入jump-3:");
                JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C,objTaskAll,depAllTime);
                Object[] tasksIs = isTasksNull(tasksAndZon,depAllTime.getJSONObject(dep).getJSONObject(grpB)
                        .getLong(teS+""));
                tasks = (List<Task>) tasksIs[0];
                zon = (long) tasksIs[1];
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 创建返回对象
                    result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                    // 添加返回值
                    result.put("zon",zon);
                    result.put("tasks",tasks);
                } else {
                    // 调用方法获取返回对象信息
                    result = getChkInJumpDayByTeS(teS, grpB, dep,id_C,xbAndSbAll);
                }
            }
            else {
                System.out.println("进入jump-4:");
                // 创建返回对象
                result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                // 调用获取镜像任务信息获取任务集合
                tasks = getImageTasks(teS,grpB,dep,allImageTasks);
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    System.out.println("进入jump-4-1:");
//                    // 调用获取镜像任务余剩时间获取任务余剩时间
//                    zon = getImageZon(teS,grpB,dep,allImageTotalTime);
                    long zuiZon = 86400;
                    for (Task task : tasks) {
                        zuiZon -= task.getWntDurTotal();
                    }
                    zon = zuiZon;
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C,objTaskAll,depAllTime);
                    List<Task> tasksNew = new ArrayList<>();
                    if (null == tasksAndZon) {
                        zon = depAllTime.getJSONObject(dep).getJSONObject(grpB).getLong(teS+"");
                    } else {
                        Object[] objects = copyTasks(tasksAndZon);
                        tasksNew = (List<Task>) objects[0];
                        zon = (long) objects[1];
                    }
                    // 判断任务集合不为空
                    if (tasksNew.size() != 0) {
                        System.out.println("进入jump-4-2:");
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        // 深度拷贝tasks1数组到tasks
                        CollectionUtils.addAll(tasks, new Object[tasksNew.size()]);
                        Collections.copy(tasks,tasksNew);
                    } else {
                        System.out.println("进入jump-4-3:");
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp));
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
                        zon = getImageZon(teS,grpB,dep,allImageTotalTime,depAllTime);
                        Object[] objects = generateSystemTasks(chkIn, offWork, zon, teS, id_C);
                        tasks = (List<Task>) objects[0];
                        zon = (long) objects[1];
                    }
                }
                // 设置返回对象信息
                result.put("zon",zon);
                result.put("tasks",tasks);
            }
        }
        // 返回结果
        return result;
    }

    /**
     * 根据(random,grpB,dep获取当前时间戳)当前时间戳获取数据库全新的的任务综合信息方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    protected Map<String,Object> getChkInJumpDayByRandom(String random,String grpB,String dep,String id_C
            ,JSONObject xbAndSbAll,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp,long teS){
        // 创建返回对象
        Map<String,Object> result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll,teS);
        // 调用获取上班时间
        JSONArray chkIn = xbAndSb.getJSONArray("sb");
        // 调用获取不上班时间
        JSONArray offWork = xbAndSb.getJSONArray("xb");
        Object[] objects = generateTasks(chkIn, 0L, offWork, random, grpB, dep, onlyFirstTimeStamp, newestLastCurrentTimestamp, id_C);
        List<Task> tasks = (List<Task>) objects[0];
        long zon = (long) objects[1];
        // 添加返回信息
        result.put("zon",zon);
        result.put("tasks",tasks);
        // 返回结果
        return result;
    }

    /**
     * 根据teS获取数据库全新的的任务综合信息方法
     * @param teS	当前时间戳
     * @param grpB	组别
     * @param dep	部门
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    protected Map<String,Object> getChkInJumpDayByTeS(Long teS,String grpB,String dep,String id_C
            ,JSONObject xbAndSbAll){
        // 创建返回对象
        Map<String,Object> result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll,teS);
        // 调用获取上班时间
        JSONArray chkIn = xbAndSb.getJSONArray("sb");
        // 调用获取不上班时间
        JSONArray offWork = xbAndSb.getJSONArray("xb");
        Object[] objects = generateSystemTasks(chkIn, offWork, 0L, teS, id_C);
        List<Task> tasks = (List<Task>) objects[0];
        long zon = (long) objects[1];
        // 添加返回信息
        result.put("zon",zon);
        result.put("tasks",tasks);
        return result;
    }

    /**
     * 根据teStart，grpB，dep获取镜像任务信息方法
     * @param teS	当前时间戳
     * @param grpBNext	组别
     * @param depNext	部门
     * @param allImageTasks	全局镜像任务列表信息
     * @return java.util.List<com.cresign.tools.pojo.po.chkin.Task>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected List<Task> getImageTasks(Long teS,String grpBNext,String depNext
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks){
        // 定义任务集合
        List<Task> tasks = null;
        boolean isAddTasks = false;
        Map<String, Map<Long, List<Task>>> depKeyTask;
        // 判断获取部门任务为空
        if (null == allImageTasks.get(depNext)) {
            // 创建部门任务
            depKeyTask = new HashMap<>();
            isAddTasks = true;
        } else {
            // 根据部门获取部门任务
            depKeyTask = allImageTasks.get(depNext);
            // 判断组别任务为空
            if (null == depKeyTask.get(grpBNext)) {
                isAddTasks = true;
            } else {
                // 根据部门获取时间任务对象
                Map<Long, List<Task>> grpBKeyTask = depKeyTask.get(grpBNext);
                // 判断时间任务对象为空
                if (null == grpBKeyTask.get(teS)) {
                    // 创建一个新的任务集合
                    tasks = new ArrayList<>();
                    // 添加任务集合
                    grpBKeyTask.put(teS,tasks);
                    // 添加组别任务对象
                    depKeyTask.put(grpBNext,grpBKeyTask);
                    // 添加部门任务对象
                    allImageTasks.put(depNext,depKeyTask);
                } else {
                    // 根据当前时间获取任务集合
                    tasks = grpBKeyTask.get(teS);
//                    System.out.println("获取镜像任务列表-1:");
//                    System.out.println(JSON.toJSONString(tasks));
                    // 返回任务集合
                    return tasks;
                }
            }
        }
        if (isAddTasks) {
            // 创建组别任务对象
            Map<Long,List<Task>> grpBKeyTask = new HashMap<>();
            // 创建一个新的任务集合
            tasks = new ArrayList<>();
            // 添加任务集合
            grpBKeyTask.put(teS,tasks);
            // 添加组别任务对象
            depKeyTask.put(grpBNext,grpBKeyTask);
            // 添加部门任务对象
            allImageTasks.put(depNext,depKeyTask);
        }
//        System.out.println("获取镜像任务列表-2:");
//        System.out.println(JSON.toJSONString(tasks));
        // 返回任务集合
        return tasks;
    }

    /**
     * 根据teStart，grpB，dep获取镜像任务余剩时间信息方法
     * @param teS	当前时间戳
     * @param grpBNext	组别
     * @param depNext	部门
     * @param allImageTotalTime	全局镜像任务余剩时间信息
     * @return java.lang.Long  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected Long getImageZon(Long teS,String grpBNext,String depNext,JSONObject allImageTotalTime,JSONObject depAllTime){
        Long zon;
        JSONObject depKeyZon = allImageTotalTime.getJSONObject(depNext);
        if (null == depKeyZon) {
            depKeyZon = new JSONObject();
            JSONObject grpBKeyZon = new JSONObject();
            zon = depAllTime.getJSONObject(depNext).getJSONObject(grpBNext).getLong(teS+"");
            grpBKeyZon.put(teS+"",zon);
            depKeyZon.put(grpBNext,grpBKeyZon);
            allImageTotalTime.put(depNext,depKeyZon);
        } else {
            JSONObject grpBKeyZon = depKeyZon.getJSONObject(grpBNext);
            if (null == grpBKeyZon) {
                grpBKeyZon = new JSONObject();
                zon = depAllTime.getJSONObject(depNext).getJSONObject(grpBNext).getLong(teS+"");
                grpBKeyZon.put(teS+"",zon);
                depKeyZon.put(grpBNext,grpBKeyZon);
                allImageTotalTime.put(depNext,depKeyZon);
            } else {
                if (null == grpBKeyZon.get(teS+"")) {
                    zon = depAllTime.getJSONObject(depNext).getJSONObject(grpBNext).getLong(teS+"");
                    grpBKeyZon.put(teS+"",zon);
                    depKeyZon.put(grpBNext,grpBKeyZon);
                    allImageTotalTime.put(depNext,depKeyZon);
                } else {
                    zon = grpBKeyZon.getLong(teS+"");
//                    System.out.println("获取镜像任务余剩时间-1:"+zon);
                    return zon;
                }
            }
        }
//        System.out.println("获取镜像任务余剩时间-2:"+zon);
        return zon;
    }

    /**
     * 根据id_O，index获取统一id_O和index存储记录状态信息方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param recordId_OIndexState	统一id_O和index存储记录状态信息
     * @return java.lang.Integer  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected Integer getStorage(String id_O,Integer index,JSONObject recordId_OIndexState){
        // 创建返回结果并赋值，re == 0 正常状态存储、re == 1 冲突状态存储、re == 2 调用时间处理状态存储
        Integer result = 0;
        JSONObject id_ORecord = recordId_OIndexState.getJSONObject(id_O);
        // 判断存储记录状态信息为空
        if (null == id_ORecord) {
            // 创建存储记录状态信息
            JSONObject id_ORecordNew = new JSONObject();
            // 添加订单下标记录状态
            id_ORecordNew.put(index.toString(),result);
            // 添加订单编号记录状态
            recordId_OIndexState.put(id_O,id_ORecordNew);
        } else {
            // 根据订单编号获取存储记录状态信息
            Integer indexRecord = id_ORecord.getInteger(index.toString());
            // 判断存储记录状态信息为空
            if (null == indexRecord) {
                // 添加订单下标记录状态
                id_ORecord.put(index.toString(),result);
                // 添加订单编号记录状态
                recordId_OIndexState.put(id_O,id_ORecord);
            } else {
                // 赋值返回结果
                return id_ORecord.getInteger(index.toString());
            }
        }
        return result;
    }
    /**
     * 获取全局上班下班信息存储方法
     * @param grpB	组别
     * @param dep	部门
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected JSONObject getXbAndSb(String grpB,String dep,String id_C,JSONObject xbAndSbAll,long teS){
        System.out.println("获取不上班打卡时间:"+teS+"-"+grpB+"-"+dep+"- id_C:"+id_C);
        // 获取全局上班下班信息的部门信息
        JSONObject depXbSb = xbAndSbAll.getJSONObject(dep);
        JSONObject grpBXbSb = null;
        // 存储为空状态
        int isStorageEmpty;
        // 判断部门信息不为空
        if (null != depXbSb) {
            // 获取全局上班下班信息的组别信息
            grpBXbSb = depXbSb.getJSONObject(grpB);
            // 判断组别信息不为空
            if (null != grpBXbSb) {
                System.out.println("直接返回:");
                JSONObject teSXb = grpBXbSb.getJSONObject(teS + "");
                if (null != teSXb) {
                    return teSXb;
                } else {
                    isStorageEmpty = 3;
                }
//                System.out.println(JSON.toJSONString(grpBXbSb));

            } else {
                isStorageEmpty = 2;
            }
        } else {
            isStorageEmpty = 1;
        }
        // 创建存储返回结果
        JSONObject result = new JSONObject();
        Asset assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
        JSONObject chkGrpB;
        if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
            chkGrpB = TaskObj.getChkinJava();
            System.out.println("获取系统:");
        } else {
            JSONObject chkin = assetDep.getChkin();
            JSONObject objChkin = chkin.getJSONObject("objChkin");
//            JSONObject chkDep = objChkin.getJSONObject(dep);
            chkGrpB = objChkin.getJSONObject(grpB);
            System.out.println("获取数据库:");
        }
        JSONArray arrTime = null;
        if (chkGrpB.containsKey("whWeek")) {
            System.out.println("teS-1:"+teS);
            boolean isWeek = true;
            if (chkGrpB.containsKey("whDate")) {
                JSONObject whDate = chkGrpB.getJSONObject("whDate");
                if (whDate.containsKey(teS + "")) {
                    isWeek = false;
                    JSONObject wData = whDate.getJSONObject(teS + "");
                    boolean isWork = false;
                    if (wData.getInteger("workType") >= 1) {
                        isWork = true;
                    } else {
                        arrTime = chkGrpB.getJSONArray("arrTime");
                    }
                    if (wData.containsKey("arrTime")&&isWork) {
                        arrTime = wData.getJSONArray("arrTime");
                    }
                }
            }
            if (isWeek) {
                JSONObject whWeek = chkGrpB.getJSONObject("whWeek");
                JSONObject weekData = whWeek.getJSONObject(dateToWeek(teS) + "");
                boolean isWork = false;
                if (weekData.getInteger("workType") >= 1) {
                    isWork = true;
                } else {
                    arrTime = chkGrpB.getJSONArray("arrTime");
                }
                if (weekData.containsKey("arrTime")&&isWork) {
                    arrTime = weekData.getJSONArray("arrTime");
                }
            }
        } else {
            arrTime = chkGrpB.getJSONArray("arrTime");
        }
        JSONArray objSb = new JSONArray();
        JSONArray objXb = new JSONArray();

        timeZjService.getArrTime(arrTime,objSb,objXb);

        result.put("xb",objXb);
        result.put("sb",objSb);

        System.out.println("获取不上班和上班时间:"+"-"+grpB+"-"+dep+"- id_C:"+id_C);

        if (isStorageEmpty == 1) {
            depXbSb = new JSONObject();
        }
        if (null == grpBXbSb) {
            grpBXbSb = new JSONObject();
        }
        grpBXbSb.put(teS+"",result);
        depXbSb.put(grpB, grpBXbSb);
        xbAndSbAll.put(dep, depXbSb);
        return result;
    }

    /**
     * 日期转星期
     */
    public static int dateToWeek(long timestamp) {
        Date dateTime = new Date(timestamp);
        SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd");
//        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        Calendar cal = Calendar.getInstance(); // 获得一个日历
        Date date;
        try {
            String format = f.format(dateTime);
            date = f.parse(format);
            cal.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0)
            w = 0;
        return w;
    }

    /**
     * 写入镜像任务集合方法
     * @param tasks	任务集合
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param allImageTasks	全局镜像任务列表信息
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void setImageTasks(List<Task> tasks,String grpBNext,String depNext,Long teS
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks){
//        System.out.println("写入镜像总上班时间任务:"+teS);
//        System.out.println(JSON.toJSONString(tasks));
        if (null == allImageTasks.get(depNext)) {
            Map<String, Map<Long, List<Task>>> depTasks = new HashMap<>();
            Map<Long, List<Task>> grpBTasks = new HashMap<>();
            grpBTasks.put(teS,tasks);
            depTasks.put(grpBNext,grpBTasks);
            allImageTasks.put(depNext,depTasks);
        } else {
            Map<String,Map<Long,List<Task>>> depTasks = allImageTasks.get(depNext);
            Map<Long, List<Task>> grpBTasks;
            if (null == depTasks.get(grpBNext)) {
                grpBTasks = new HashMap<>();
            } else {
                grpBTasks = depTasks.get(grpBNext);
            }
            List<Task> tasksNew = new ArrayList<>();
            for (Task task : tasks) {
                tasksNew.add(TaskObj.getTaskY(task));
            }
            grpBTasks.put(teS,tasksNew);
            depTasks.put(grpBNext,grpBTasks);
            allImageTasks.put(depNext,depTasks);
        }
    }

    /**
     * 写入镜像任务余剩时间方法
     * @param zon	任务余剩时间
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void setImageZon(Long zon,String grpBNext,String depNext,Long teS,JSONObject allImageTotalTime){
//        System.out.println("写入镜像总上班时间:"+zon);
        JSONObject depZon = allImageTotalTime.getJSONObject(depNext);
        if (null == depZon) {
            depZon = new JSONObject();
            JSONObject grpBZon = new JSONObject();
            grpBZon.put(teS+"",zon);
            depZon.put(grpBNext,grpBZon);
        } else {
            JSONObject grpBZon = depZon.getJSONObject(grpBNext);
            if (null == grpBZon) {
                grpBZon = new JSONObject();
            }
            grpBZon.put(teS+"",zon);
            depZon.put(grpBNext,grpBZon);
        }
        allImageTotalTime.put(depNext,depZon);
    }

    /**
     * 写入id_O，index和存储记录状态方法
     * @param num	状态
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param recordId_OIndexState	统一id_O和index存储记录状态信息
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void setStorage(Integer num,String id_O,Integer index,JSONObject recordId_OIndexState){
//        System.out.println("写入镜像存储重置:"+num);
        JSONObject id_ORecord = recordId_OIndexState.getJSONObject(id_O);
        if (null == id_ORecord) {
            JSONObject indexRecord = new JSONObject();
            indexRecord.put(index.toString(),num);
            recordId_OIndexState.put(id_O,indexRecord);
        } else {
            id_ORecord.put(index.toString(),num);
            recordId_OIndexState.put(id_O,id_ORecord);
        }
    }

    /**
     * 写入当前时间戳方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teS	当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void setTeS(String random,String grpB,String dep,Long teS,JSONObject newestLastCurrentTimestamp){
        JSONObject rand;
        if (null == newestLastCurrentTimestamp.getJSONObject(random)) {
            rand = new JSONObject();
            JSONObject onlyDep = new JSONObject();
            onlyDep.put(grpB,teS);
            rand.put(dep,onlyDep);
            newestLastCurrentTimestamp.put(random,rand);
        } else {
            rand = newestLastCurrentTimestamp.getJSONObject(random);
            JSONObject onlyDep;
            if (null == rand.getJSONObject(dep)) {
                onlyDep = new JSONObject();
                onlyDep.put(grpB,teS);
                rand.put(dep,onlyDep);
                newestLastCurrentTimestamp.put(random,rand);
            } else {
                onlyDep = rand.getJSONObject(dep);
                if (null == onlyDep.getLong(grpB)) {
                    onlyDep.put(grpB,teS);
                    rand.put(dep,onlyDep);
                    newestLastCurrentTimestamp.put(random,rand);
                }
            }
        }
    }

    /**
     * 获取指定的全局任务信息方法
     * @param teStart	时间戳
     * @param grpB	组别
     * @param dep	部门
     * @param id_C	公司编号
     * @param objTaskAll	全局任务信息
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public JSONObject getTasksAndZon(Long teStart,String grpB,String dep,String id_C
            ,JSONObject objTaskAll,JSONObject depAllTime){
//        System.out.println("进入获取全局任务方法:");
//        System.out.println("teStart:"+teStart+",grpB:"+grpB+",dep:"+dep+",id_C:"+id_C);
        // 获取部门任务信息
        JSONObject depTask = objTaskAll.getJSONObject(dep);
        // 定义存储组别任务信息
        JSONObject grpBTask = null;
        // 定义存储为空状态
        int isStorageEmpty;
        // 判断部门任务信息不为空
        if (null != depTask) {
            // 获取组别任务信息
            grpBTask = depTask.getJSONObject(grpB);
            // 判断组别任务信息不为空
            if (null != grpBTask) {
                // 获取时间戳任务信息
                JSONObject teSTask = grpBTask.getJSONObject(teStart + "");
                // 判断时间差任务信息不为空
                if (null != teSTask) {
                    if (!depAllTime.containsKey(dep)||!depAllTime.getJSONObject(dep).containsKey(grpB)
                            ||!depAllTime.getJSONObject(dep).getJSONObject(grpB).containsKey(teStart+"")) {
                        Asset asset = qt.getConfig(id_C,"d-"+dep, "chkin");
                        JSONObject depData = depAllTime.getJSONObject(dep);
                        if (null == depData) {
                            depData = new JSONObject();
                        }
                        int teDur = 0;
                        if (null != asset.getChkin() && null!=asset.getChkin().getJSONObject("objChkin")
                                && null!=asset.getChkin().getJSONObject("objChkin").getJSONObject(grpB)) {
                            JSONObject grpBData = asset.getChkin().getJSONObject("objChkin").getJSONObject(grpB);
                            if (grpBData.containsKey("whWeek")) {
                                if (grpBData.containsKey("whDate") && grpBData.getJSONObject("whDate").containsKey(teStart+"")) {
                                    JSONObject teSData = grpBData.getJSONObject("whDate").getJSONObject(teStart + "");
                                    boolean isWntWork = teSData.containsKey("wntWork");
                                    boolean isWntOver = teSData.containsKey("wntOver");
                                    if (teSData.getInteger("workType") == 1) {
                                        if (isWntWork) {
                                            teDur = teSData.getInteger("wntWork");
                                        } else {
                                            teDur = grpBData.getInteger("teDur");
                                        }
                                    } else if (teSData.getInteger("workType") == 2) {
                                        if (isWntWork) {
                                            teDur = teSData.getInteger("wntWork");
                                        } else {
                                            teDur = grpBData.getInteger("teDur");
                                        }
                                        if (isWntOver) {
                                            teDur+=teSData.getInteger("wntOver");
                                        } else {
                                            teDur+=grpBData.getInteger("wntOver")==null?0:grpBData.getInteger("wntOver");
                                        }
                                    }
                                } else {
                                    JSONObject whWeek = grpBData.getJSONObject("whWeek");
                                    JSONObject weekData = whWeek.getJSONObject(dateToWeek(teStart) + "");
                                    boolean isWntWork = weekData.containsKey("wntWork");
                                    boolean isWntOver = weekData.containsKey("wntOver");
                                    if (weekData.getInteger("workType") == 1) {
                                        if (isWntWork) {
                                            teDur = weekData.getInteger("wntWork");
                                        } else {
                                            teDur = grpBData.getInteger("teDur");
                                        }
                                    } else if (weekData.getInteger("workType") == 2) {
                                        if (isWntWork) {
                                            teDur = weekData.getInteger("wntWork");
                                        } else {
                                            teDur = grpBData.getInteger("teDur");
                                        }
                                        if (isWntOver) {
                                            teDur+=weekData.getInteger("wntOver");
                                        } else {
                                            teDur+=grpBData.getInteger("wntOver")==null?0:grpBData.getInteger("wntOver");
                                        }
                                    }
                                }
                            } else {
                                teDur = grpBData.getInteger("teDur");
                            }

                            JSONObject grpBInfo = depData.getJSONObject(grpB);
                            if (null == grpBInfo) {
                                grpBInfo = new JSONObject();
                            }
                            grpBInfo.put(teStart+"",teDur==0?0L:(teDur* 60L)*60);
                            depData.put(grpB,grpBInfo);
                            depAllTime.put(dep,depData);
                            System.out.println("进入拿数据库:");
                        } else {
                            JSONObject grpBInfo = depData.getJSONObject(grpB);
                            if (null == grpBInfo) {
                                grpBInfo = new JSONObject();
                            }
                            if (!grpBInfo.containsKey(teStart + "")) {
                                grpBInfo.put(teStart+"",0);
                                depData.put(grpB,grpBInfo);
                                depAllTime.put(dep,depData);
                            }
                            System.out.println("进入拿本地:");
                        }
                    }
                    // 直接返回结果
                    return teSTask;
                } else {
                    // 设置为空状态为3
                    isStorageEmpty = 3;
                }
            } else {
                // 设置为空状态为2
                isStorageEmpty = 2;
            }
        } else {
            // 设置为空状态为1
            isStorageEmpty = 1;
        }
//        System.out.println("为空状态:"+isStorageEmpty);
//        Asset asset = qt.getConfig(id_C,"a-chkin", timeCard);
        Asset asset = qt.getConfig(id_C,"d-"+dep, qt.strList(timeCard,"chkin"));
        int teDur = 0;
        JSONObject depData = depAllTime.getJSONObject(dep);
        if (null == depData) {
            depData = new JSONObject();
        }
        if (null != asset.getChkin() && null!=asset.getChkin().getJSONObject("objChkin")
                && null!=asset.getChkin().getJSONObject("objChkin").getJSONObject(grpB)) {
            JSONObject grpBData = asset.getChkin().getJSONObject("objChkin").getJSONObject(grpB);
            if (grpBData.containsKey("whWeek")) {
                if (grpBData.containsKey("whDate") && grpBData.getJSONObject("whDate").containsKey(teStart+"")) {
                    JSONObject teSData = grpBData.getJSONObject("whDate").getJSONObject(teStart + "");
                    boolean isWntWork = teSData.containsKey("wntWork");
                    boolean isWntOver = teSData.containsKey("wntOver");
                    if (teSData.getInteger("workType") == 1) {
                        if (isWntWork) {
                            teDur = teSData.getInteger("wntWork");
                        } else {
                            teDur = grpBData.getInteger("teDur");
                        }
                    } else if (teSData.getInteger("workType") == 2) {
                        if (isWntWork) {
                            teDur = teSData.getInteger("wntWork");
                        } else {
                            teDur = grpBData.getInteger("teDur");
                        }
                        if (isWntOver) {
                            teDur+=teSData.getInteger("wntOver");
                        } else {
                            teDur+=grpBData.getInteger("wntOver")==null?0:grpBData.getInteger("wntOver");
                        }
                    }
                } else {
                    JSONObject whWeek = grpBData.getJSONObject("whWeek");
                    JSONObject weekData = whWeek.getJSONObject(dateToWeek(teStart) + "");
                    boolean isWntWork = weekData.containsKey("wntWork");
                    boolean isWntOver = weekData.containsKey("wntOver");
                    if (weekData.getInteger("workType") == 1) {
                        if (isWntWork) {
                            teDur = weekData.getInteger("wntWork");
                        } else {
                            teDur = grpBData.getInteger("teDur");
                        }
                    } else if (weekData.getInteger("workType") == 2) {
                        if (isWntWork) {
                            teDur = weekData.getInteger("wntWork");
                        } else {
                            teDur = grpBData.getInteger("teDur");
                        }
                        if (isWntOver) {
                            teDur+=weekData.getInteger("wntOver");
                        } else {
                            teDur+=grpBData.getInteger("wntOver")==null?0:grpBData.getInteger("wntOver");
                        }
                    }
                }
            } else {
                teDur = grpBData.getInteger("teDur");
            }

            JSONObject grpBInfo = depData.getJSONObject(grpB);
            if (null == grpBInfo) {
                grpBInfo = new JSONObject();
            }
            grpBInfo.put(teStart+"",teDur==0?0L:(teDur* 60L)*60);
            depData.put(grpB,grpBInfo);
            depAllTime.put(dep,depData);
            System.out.println("进入拿数据库:");
        } else {
            JSONObject grpBInfo = depData.getJSONObject(grpB);
            if (null == grpBInfo) {
                grpBInfo = new JSONObject();
            }
            if (!grpBInfo.containsKey(teStart + "")) {
                grpBInfo.put(teStart+"",0);
                depData.put(grpB,grpBInfo);
                depAllTime.put(dep,depData);
            }
            System.out.println("进入拿本地:");
        }

//        System.out.println(JSON.toJSONString(asset));
//        System.out.println("获取任务集合:"+teStart+" - "+grpB+" - "+dep);
//        System.out.println("查询数据库:");
        // 获取时间处理卡片信息
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
//        System.out.println(JSON.toJSONString(aArrange));
        // 判断为空
        if (null == aArrange) {
            // 添加一个空任务信息
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart
                    , depAllTime.getJSONObject(dep).getJSONObject(grpB).getLong(teStart+""), objTaskAll);
            return null;
        }
        JSONObject objTasks = aArrange.getJSONObject("objTask");
        if (null == objTasks) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,depAllTime.getJSONObject(dep)
                    .getJSONObject(grpB).getLong(teStart+""),objTaskAll);
            return null;
        }
//        JSONObject objDep = objTasks.getJSONObject(dep);
//        if (null == objDep) {
//            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,28800L,objTaskAll);
//            return null;
//        }
        JSONObject objGrpB = objTasks.getJSONObject(grpB);
        if (null == objGrpB) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,depAllTime.getJSONObject(dep)
                    .getJSONObject(grpB).getLong(teStart+""),objTaskAll);
            return null;
        }
        // 创建存储返回任务信息字典
        JSONObject result = objGrpB.getJSONObject(teStart + "");
        // 判断为空状态为2
        if (isStorageEmpty == 2) {
            // 创建
            grpBTask = new JSONObject();
        } else if (isStorageEmpty == 1) {
            grpBTask = new JSONObject();
            depTask = new JSONObject();
        }
        // 添加任务信息
        grpBTask.put(teStart+"",result);
        depTask.put(grpB,grpBTask);
        objTaskAll.put(dep, depTask);
        return result;
    }

    /**
     * 写入任务到全局任务信息方法
     * @param tasks	任务列表
     * @param grpB	组别
     * @param dep	部门
     * @param teStart	时间戳
     * @param zon	余剩总时间
     * @param objTaskAll	全局任务信息
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public static void setTasksAndZon(List<Task> tasks, String grpB, String dep, Long teStart
            , Long zon,JSONObject objTaskAll){
//        System.out.println("写入数据库任务列表:"+teStart);
//        System.out.println(JSON.toJSONString(tasks));
        // 获取部门任务信息
        JSONObject depTask = objTaskAll.getJSONObject(dep);
        // 定义存储组别任务信息
        JSONObject grpBTask;
        // 存储当前要写入的任务信息
        JSONObject result = new JSONObject();
        // 添加当前任务信息
        result.put("tasks",tasks);
        result.put("zon",zon);
        // 判断部门任务信息不为空
        if (null != depTask) {
            // 获取组别任务信息
            grpBTask = depTask.getJSONObject(grpB);
            // 判断组别任务信息为空
            if (null == grpBTask) {
                // 创建
                grpBTask = new JSONObject();
            }
        } else {
            // 创建
            depTask = new JSONObject();
            grpBTask = new JSONObject();
        }
        // 写入任务信息
        grpBTask.put(teStart+"",result);
        depTask.put(grpB,grpBTask);
        objTaskAll.put(dep,depTask);
    }
    public static void setTasksAndZonWntTotal(List<Task> tasks, String grpB, String dep, Long teStart
            , Long zon,JSONObject objTaskAll,long wntTotal){
//        System.out.println("写入数据库任务列表:"+teStart);
//        System.out.println(JSON.toJSONString(tasks));
        // 获取部门任务信息
        JSONObject depTask = objTaskAll.getJSONObject(dep);
        // 定义存储组别任务信息
        JSONObject grpBTask;
        // 存储当前要写入的任务信息
        JSONObject result = new JSONObject();
        // 添加当前任务信息
        result.put("tasks",tasks);
        result.put("zon",zon);
        result.put("wntTotal",wntTotal);
        // 判断部门任务信息不为空
        if (null != depTask) {
            // 获取组别任务信息
            grpBTask = depTask.getJSONObject(grpB);
            // 判断组别任务信息为空
            if (null == grpBTask) {
                // 创建
                grpBTask = new JSONObject();
            }
        } else {
            // 创建
            depTask = new JSONObject();
            grpBTask = new JSONObject();
        }
        // 写入任务信息
        grpBTask.put(teStart+"",result);
        depTask.put(grpB,grpBTask);
        objTaskAll.put(dep,depTask);
    }

    /**
     * 根据公司编号清空所有任务信息
     * @param id_C	公司编号
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 2:07
     */
    public void setTaskAndZonKai(String id_C){
        JSONArray array = qt.setArray("515973212487","665591821169","684744667311","326269832536","1xx2","1xx1");
        for (int i = 0; i < array.size(); i++) {
            String dep = array.getString(i);
            Asset asset = qt.getConfig(id_C,"d-"+dep,timeCard);
            // 获取任务卡片信息
            JSONObject aArrange = getAArrangeNew(asset);
            // 判断任务卡片为空
            if (null == aArrange) {
                // 创建任务卡片
                aArrange = new JSONObject();
            }
            // 清空所有任务信息
            aArrange.put("objTask",new JSONObject());
            qt.setMDContent(asset.getId(),qt.setJson(timeCard,aArrange), Asset.class);
        }
        Asset asset = qt.getConfig(id_C,"a-chkin",timeCard);
        // 获取任务卡片信息
        JSONObject aArrange = getAArrangeNew(asset);
        // 判断任务卡片为空
        if (null == aArrange) {
            // 创建任务卡片
            aArrange = new JSONObject();
        }
        // 清空所有任务信息
        aArrange.put("objTask",new JSONObject());
        qt.setMDContent(asset.getId(),qt.setJson(timeCard,aArrange), Asset.class);
    }

    /**
     * 获取当前时间戳方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return java.lang.Long  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public Long getTeS(String random,String grpB,String dep,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp){
        JSONObject rand;
        long getTeS;
        if (null == newestLastCurrentTimestamp.getJSONObject(random)) {
            rand = new JSONObject();
            JSONObject onlyDep = new JSONObject();
            onlyDep.put(grpB,onlyFirstTimeStamp.getLong(random));
            getTeS = onlyFirstTimeStamp.getLong(random);
            rand.put(dep,onlyDep);
            newestLastCurrentTimestamp.put(random,rand);
        } else {
            rand = newestLastCurrentTimestamp.getJSONObject(random);
            JSONObject onlyDep;
            if (null == rand.getJSONObject(dep)) {
                onlyDep = new JSONObject();
                onlyDep.put(grpB,onlyFirstTimeStamp.getLong(random));
                getTeS = onlyFirstTimeStamp.getLong(random);
                rand.put(dep,onlyDep);
                newestLastCurrentTimestamp.put(random,rand);
            } else {
                onlyDep = rand.getJSONObject(dep);
//                System.out.println("输出：");
//                System.out.println(JSON.toJSONString(onlyDep));
//                System.out.println("grpB:"+grpB+" - dep:"+dep);
//                System.out.println(JSON.toJSONString(newestLastCurrentTimestamp));
//                System.out.println(JSON.toJSONString(onlyFirstTimeStamp));
                if (null == onlyDep.getLong(grpB)) {
                    onlyDep.put(grpB,onlyFirstTimeStamp.getLong(random));
                    rand.put(dep,onlyDep);
                    newestLastCurrentTimestamp.put(random,rand);
                    getTeS = onlyFirstTimeStamp.getLong(random);
                } else {
                    getTeS = onlyDep.getLong(grpB);
                }
            }
        }
        return getTeS;
    }

    /**
     * 新增或者修改任务的所在日期对象状态方法
     * @param teS	当前时间戳
     * @param teDate	当前处理的任务的所在日期对象
     * @param wntDurTotal	任务总时间
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void addOrUpdateTeDate(Long teS,JSONObject teDate,Long wntDurTotal){
        Long teSInfo = teDate.getLong(teS + "");
        if (null == teSInfo) {
            // 添加当前处理的任务的所在日期对象状态
            teDate.put(teS+"",wntDurTotal);
        } else {
            // 添加当前处理的任务的所在日期对象状态
            teDate.put(teS+"",(teSInfo+wntDurTotal));
        }
    }

    /**
     * 根据id_O，index获取任务所在日期方法
     * @param id_O	订单编号
     * @param dateIndex	任务唯一下标
     * @param storageTaskWhereTime	存储任务所在日期
     * @return java.util.List<java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected List<String> getTaskWhereDate(String id_O,Integer dateIndex,JSONObject storageTaskWhereTime){
        List<String> result = new ArrayList<>();
        JSONObject id_OStorage = storageTaskWhereTime.getJSONObject(id_O);
        if (null == id_OStorage) {
            JSONObject id_OStorageNew = new JSONObject();
            id_OStorageNew.put(dateIndex.toString(),new JSONObject());
            storageTaskWhereTime.put(id_O,id_OStorageNew);
        } else {
            JSONObject indexStorage = id_OStorage.getJSONObject(dateIndex.toString());
            if (null == indexStorage) {
                id_OStorage.put(dateIndex.toString(),new JSONObject());
                storageTaskWhereTime.put(id_O,id_OStorage);
            } else {
                return new ArrayList<>(indexStorage.keySet());
            }
        }
//        System.out.println("获取镜像所在日期列表:-id_O:"+id_O+",-index:"+index);
        return result;
    }

    /**
     * 判断产品状态再调用写入任务所在日期方法的方法
     * @param id_O  订单编号
     * @param dateIndex 任务唯一下标
     * @param teS   当前时间戳
     * @param prodState  产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
     * @param storageTaskWhereTime	存储任务所在日期
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void putTeDate(String id_O,Integer dateIndex,Long teS,Integer prodState,JSONObject storageTaskWhereTime){
        // 判断状态不等于当前递归产品
        if (prodState != -1) {
//            System.out.println("setTaskWhereDate-写入:"+id_O+" - "+dateIndex);
//            System.out.println("添加记录时间-id_O:"+id_O+",index:"+index+",teS:"+teS);
            // 调用写入任务所在日期方法
            setTaskWhereDate(id_O,dateIndex,teS,storageTaskWhereTime);
//            System.out.println(JSON.toJSONString(storageTaskWhereTime));
        }
    }

    /**
     * 写入任务所在日期方法
     * @param id_O	订单编号
     * @param dateIndex	任务唯一下标
     * @param teS	当前时间戳
     * @param storageTaskWhereTime	存储任务所在日期
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void setTaskWhereDate(String id_O, Integer dateIndex, Long teS,JSONObject storageTaskWhereTime){
        JSONObject id_OStorage = storageTaskWhereTime.getJSONObject(id_O);
        if (null == id_OStorage) {
            id_OStorage = new JSONObject();
            JSONObject indexStorage = new JSONObject();
            indexStorage.put(teS.toString(),0);
            id_OStorage.put(dateIndex.toString(),indexStorage);
            storageTaskWhereTime.put(id_O,id_OStorage);
        } else {
            JSONObject indexStorage = id_OStorage.getJSONObject(dateIndex.toString());
            if (null == indexStorage) {
                indexStorage = new JSONObject();
            }
            indexStorage.put(teS.toString(),0);
            id_OStorage.put(dateIndex.toString(),indexStorage);
            storageTaskWhereTime.put(id_O,id_OStorage);
        }
    }

    /**
     * 添加记录进入哪个未操作到的地方
     * @param randomAll	全局唯一编号
     * @param text	位置信息
     * @param recordNoOperation	存储记录字典
     * @author tang
     * @ver 1.0.0 record
     * @date 2022/6/9 1:55
     */
    protected void addRecordGetIntoOperation(String randomAll,String text,JSONObject recordNoOperation){
        // 根据全局唯一编号获取操作存储字典
        JSONArray recordArray = recordNoOperation.getJSONArray(randomAll);
        // 添加位置信息
        recordArray.add(text);
        // 更新信息
        recordNoOperation.put(randomAll,recordArray);
    }

    /**
     * 添加或更新产品状态方法
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param id_OGet	被获取产品状态的订单编号
     * @param indexGet	被获取产品状态的订单下标
     * @param id_OAdd	添加产品状态的订单编号
     * @param indexAdd	添加产品状态的订单下标
     * @param setUpState    teSq == 0 设置状态为不是被第一个处理时间的产品、teSq == 1 设置状态为被获取产品状态的状态
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void addSho(JSONObject sho,String id_OGet,String indexGet,String id_OAdd,String indexAdd,Integer setUpState){
        JSONObject id_OInfoGet = sho.getJSONObject(id_OGet);
        JSONObject indexInfoGet = id_OInfoGet.getJSONObject(indexGet);
        // 获取产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer prodState = indexInfoGet.getInteger("prodState");
        // 判断产品状态为当前递归产品
        if (prodState == -1) {
            JSONObject id_OAddInfo = sho.getJSONObject(id_OAdd);
            JSONObject indexAddInfo;
            if (null == id_OAddInfo) {
                id_OAddInfo = new JSONObject();
                indexAddInfo = new JSONObject();
            } else {
                indexAddInfo = id_OAddInfo.getJSONObject(indexAdd);
                if (null == indexAddInfo) {
                    indexAddInfo = new JSONObject();
                }
            }
            // 添加产品状态为第一个产品
            indexAddInfo.put("prodState",1);
            // 添加当前订单编号和订单下标记录
            indexAddInfo.put("z",id_OAdd+"+"+indexAdd);
            // 添加订单下标存储产品状态对象
            id_OAddInfo.put(indexAdd,indexAddInfo);
            // 添加订单编号存储产品状态对象
            sho.put(id_OAdd,id_OAddInfo);
//            // 创建订单编号存储产品状态对象
//            JSONObject id_OStateObj = new JSONObject();
//            // 创建订单下标存储产品状态对象
//            JSONObject indexStateObj = new JSONObject();
//            // 添加产品状态为第一个产品
//            indexStateObj.put("prodState",1);
//            // 添加当前订单编号和订单下标记录
//            indexStateObj.put("z",id_OAdd+"+"+indexAdd);
//            // 添加订单下标存储产品状态对象
//            id_OStateObj.put(indexAdd,indexStateObj);
//            // 添加订单编号存储产品状态对象
//            sho.put(id_OAdd,id_OStateObj);
        } else {
            // 获取被获取产品状态的产品订单编号和订单下标记录
            String z = indexInfoGet.getString("z");
            // 获取添加产品状态的订单编号产品状态信息
            JSONObject id_OInfoAdd = sho.getJSONObject(id_OAdd);
            // teSq == 1 设置状态为被获取产品状态的状态
            if (setUpState == 1) {
                // 判断添加产品状态的订单编号状态信息为空
                if (null == id_OInfoAdd) {
                    // 创建订单编号存储产品状态对象
                    JSONObject id_OStateObj = new JSONObject();
                    // 创建订单下标存储产品状态对象
                    JSONObject indexStateObj = new JSONObject();
                    // 添加产品状态为被获取产品状态的状态
                    indexStateObj.put("prodState",prodState);
                    // 添加被获取产品状态的订单编号和订单下标记录
                    indexStateObj.put("z",z);
                    // 添加订单下标存储产品状态对象
                    id_OStateObj.put(indexAdd,indexStateObj);
                    // 添加订单编号存储产品状态对象
                    sho.put(id_OAdd,id_OStateObj);
                } else {
                    // 获取添加产品状态的订单下标产品状态信息
                    JSONObject indexInfoAdd = id_OInfoAdd.getJSONObject(indexAdd);
                    // 判断添加产品状态的订单下标状态信息为空
                    if (null == indexInfoAdd) {
                        // 创建订单下标存储产品状态对象
                        JSONObject indexStateObj = new JSONObject();
                        // 添加产品状态为被获取产品状态的状态
                        indexStateObj.put("prodState",prodState);
                        // 添加被获取产品状态的订单编号和订单下标记录
                        indexStateObj.put("z",z);
                        // 添加订单下标存储产品状态对象
                        id_OInfoAdd.put(indexAdd,indexStateObj);
                        // 添加订单编号存储产品状态对象
                        sho.put(id_OAdd,id_OInfoAdd);
                    } else {
                        // 判断添加产品状态的订单编号和订单下标记录不等于被获取产品状态的订单编号和订单下标记录
                        if (!indexInfoAdd.getString("z").equals(z)) {
                            // 创建订单编号存储产品状态对象
                            JSONObject id_OStateObj = new JSONObject();
                            // 创建订单下标存储产品状态对象
                            JSONObject indexStateObj = new JSONObject();
                            // 添加产品状态为被获取产品状态的状态
                            indexStateObj.put("prodState",prodState);
                            // 添加被获取产品状态的订单编号和订单下标记录
                            indexStateObj.put("z",z);
                            // 添加订单下标存储产品状态对象
                            id_OStateObj.put(indexAdd,indexStateObj);
                            // 添加订单编号存储产品状态对象
                            sho.put(id_OAdd,id_OStateObj);
                        }
                    }
                }
            } else {
                // teSq == 0 设置状态为不是被第一个处理时间的产品

                // 判断添加产品状态的订单编号状态信息为空
                if (null == id_OInfoAdd) {
                    // 创建订单编号存储产品状态对象
                    JSONObject id_OStateObj = new JSONObject();
                    // 创建订单下标存储产品状态对象
                    JSONObject indexStateObj = new JSONObject();
                    // 添加产品状态为不是被第一个处理时间的产品
                    indexStateObj.put("prodState",2);
                    // 添加被获取产品状态的订单编号和订单下标记录
                    indexStateObj.put("z",z);
                    // 添加订单下标存储产品状态对象
                    id_OStateObj.put(indexAdd,indexStateObj);
                    // 添加订单编号存储产品状态对象
                    sho.put(id_OAdd,id_OStateObj);
                } else {
                    // 获取添加产品状态的订单下标产品状态信息
                    JSONObject indexInfoAdd = id_OInfoAdd.getJSONObject(indexAdd);
                    // 判断添加产品状态的订单下标状态信息为空
                    if (null == indexInfoAdd) {
                        // 创建订单下标存储产品状态对象
                        JSONObject indexStateObj = new JSONObject();
                        // 添加产品状态为不是被第一个处理时间的产品
                        indexStateObj.put("prodState",2);
                        // 添加被获取产品状态的订单编号和订单下标记录
                        indexStateObj.put("z",z);
                        // 添加订单下标存储产品状态对象
                        id_OInfoAdd.put(indexAdd,indexStateObj);
                        // 添加订单编号存储产品状态对象
                        sho.put(id_OAdd,id_OInfoAdd);
                    } else {
                        // 判断添加产品状态的订单编号和订单下标记录不等于被获取产品状态的订单编号和订单下标记录
                        if (!indexInfoAdd.getString("z").equals(z)) {
                            // 创建订单编号存储产品状态对象
                            JSONObject id_OStateObj = new JSONObject();
                            // 创建订单下标存储产品状态对象
                            JSONObject indexStateObj = new JSONObject();
                            // 添加产品状态为不是被第一个处理时间的产品
                            indexStateObj.put("prodState",2);
                            // 添加被获取产品状态的订单编号和订单下标记录
                            indexStateObj.put("z",z);
                            // 添加订单下标存储产品状态对象
                            id_OStateObj.put(indexAdd,indexStateObj);
                            // 添加订单编号存储产品状态对象
                            sho.put(id_OAdd,id_OStateObj);
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断tasksAndZon是否为空，为空则创建，否则拷贝并返回
     * @param tasksAndZon	全局任务信息
     * @return 返回结果: {@link Object[]}
     * @author tang
     * @date 创建时间: 2022/10/24
     * @ver 版本号: 1.0.0
     */
    @SuppressWarnings("unchecked")
    Object[] isTasksNull(JSONObject tasksAndZon, long allTime){
        List<Task> tasks;
        long zon;
        if (null == tasksAndZon) {
            tasks = new ArrayList<>();
            zon = allTime;
        } else {
            Object[] objects = copyTasks(tasksAndZon);
            tasks = (List<Task>) objects[0];
            zon = (long) objects[1];
        }
        return new Object[]{tasks, zon};
    }

    /**
     * 生成任务列表方法
     * @param chkIn	上班时间
     * @param zon	余剩时间
     * @param offWork	下班时间
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param id_C	公司编号
     * @return 返回结果: {@link Object[]}
     * @author tang
     * @date 创建时间: 2022/10/24
     * @ver 版本号: 1.0.0
     */
    private Object[] generateTasks(JSONArray chkIn,long zon,JSONArray offWork,String random,String grpB
            ,String dep,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp,String id_C){
        // 创建任务集合
        List<Task> tasks = new ArrayList<>();
        // 遍历上班时间
        for (int i = 0; i < chkIn.size(); i++) {
            // 根据下标i获取上班时间段
            JSONObject chkInZon = chkIn.getJSONObject(i);
            // 获取上班时间的所有时间并累加到任务余剩时间
            zon += chkInZon.getLong("zon");
        }
        // 任务集合添加任务信息
        tasks.add(TaskObj.getTask(getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                ,"",-1,0L,-1,"休息",0L,0L,id_C
                ,0L,0L,-1,false));
        // 遍历不上班时间
        for (int i = 0; i < offWork.size(); i++) {
            // 根据下标i获取不上班时间段
            JSONObject offWorkInfo = offWork.getJSONObject(i);
            // 任务集合添加任务信息
            tasks.add(TaskObj.getTask((getTeS(random,grpB,dep,onlyFirstTimeStamp
                    ,newestLastCurrentTimestamp) + offWorkInfo.getLong("tePStart"))
                    , (getTeS(random,grpB,dep,onlyFirstTimeStamp
                            ,newestLastCurrentTimestamp) + offWorkInfo.getLong("tePFinish"))
                    , "", -1, offWorkInfo.getLong("zon")
                    , offWorkInfo.getInteger("priority"), "休息", 0L,0L,id_C
                    ,(getTeS(random,grpB,dep,onlyFirstTimeStamp
                            ,newestLastCurrentTimestamp) + offWorkInfo.getLong("tePStart")),0L,-1,false));
        }
        return new Object[]{tasks,zon};
    }

    /**
     * 拷贝任务方法
     * @param tasksAndZon	需要拷贝的数据
     * @return 返回结果: {@link Object[]}
     * @author tang
     * @date 创建时间: 2022/10/24
     * @ver 版本号: 1.0.0
     */
    private Object[] copyTasks(JSONObject tasksAndZon){
        JSONArray tasks = tasksAndZon.getJSONArray("tasks");
        List<Task> finalTasks = new ArrayList<>();
        tasks.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
        List<Task> tasksCopy = new ArrayList<>(finalTasks);
        long zon = tasksAndZon.getLong("zon");
        return new Object[]{tasksCopy, zon};
    }

    /**
     * 生成系统任务列表方法
     * @param chkIn	上班时间
     * @param offWork	下班时间
     * @param zon	余剩时间
     * @param teS	任务余剩时间
     * @param id_C	公司编号
     * @return 返回结果: {@link Object[]}
     * @author tang
     * @date 创建时间: 2022/10/24
     * @ver 版本号: 1.0.0
     */
    private Object[] generateSystemTasks(JSONArray chkIn,JSONArray offWork,long zon,long teS,String id_C){
        // 创建任务集合
        List<Task> tasks = new ArrayList<>();
        // 遍历上班时间
        for (int i = 0; i < chkIn.size(); i++) {
            // 根据下标i获取上班时间段
            JSONObject chkInZon = chkIn.getJSONObject(i);
            // 获取上班时间的所有时间并累加到任务余剩时间
            zon += chkInZon.getLong("zon");
        }
        // 任务集合添加任务信息
        tasks.add(TaskObj.getTask(teS,teS,"",-1,0L,-1
                ,"休息",0L,0L,id_C,0L,0L
                ,-1,false));
        // 遍历不上班时间
        for (int i = 0; i < offWork.size(); i++) {
            // 根据下标i获取不上班时间段
            JSONObject offWorkInfo = offWork.getJSONObject(i);
            // 任务集合添加任务信息
            tasks.add(TaskObj.getTask((teS + offWorkInfo.getLong("tePStart"))
                    , (teS + offWorkInfo.getLong("tePFinish"))
                    , "", -1, offWorkInfo.getLong("zon")
                    , offWorkInfo.getInteger("priority"), "休息", 0L,0L
                    ,id_C,(teS + offWorkInfo.getLong("tePStart")),0L,-1,false));
        }
        return new Object[]{tasks,zon};
    }

    /**
     * 清理旧任务方法
     * @param id_O	订单编号
     * @param dateIndex	下标
     * @param id_C	公司编号
     * @param objThisTaskAll	全局任务信息
     * @param clearStatus	清理状态信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTeDate    镜像任务所在日期
     * @param thisTasks 当前处理任务信息列表
     * @param isSetImage    是否写入镜像状态信息
     * @return 返回结果: {@link JSONObject} 清理后的任务信息
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected JSONObject clearOldTask(String id_O,int dateIndex,String id_C,JSONObject objThisTaskAll
            ,JSONObject clearStatus,Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks
            ,JSONObject allImageTotalTime,JSONObject allImageTeDate
            ,List<Task> thisTasks,JSONObject isSetImage,JSONObject depAllTime
            ,String random,JSONObject onlyFirstTimeStamp,JSONObject actionIdO,JSONObject thisInfo
            ,int layer,String id_PF
    ){
        // 下标加一，从下一个开始
        dateIndex++;
        // 调用写入清理状态方法
        GsThisInfo.setClearStatus(id_O,dateIndex,clearStatus,0);
        // 获取当前订单编号的任务所在日期
        JSONObject imgId_O = allImageTeDate.getJSONObject(id_O);
        boolean isUpdateImageTask = false;
        // 判断不为空
        if (null != imgId_O) {
            // 定义存储当前处理的任务的所有下一个任务的所在位置
            JSONObject depAndGrpB = new JSONObject();
            int dateIndexNew = dateIndex;
            // 开启循环
            while (true) {
                // 获取指定下标的镜像任务所在位置信息
                JSONObject imgDateIndex = imgId_O.getJSONObject(dateIndexNew + "");
                // 判断为空
                if (null == imgDateIndex) {
                    break;
                }
                // 获取所在日期信息
                JSONObject date = imgDateIndex.getJSONObject("date");
                if (null == date) {
                    break;
                }
                // 获取所在日期键
                Set<String> imgKeys = date.keySet();
                if (imgKeys.size() == 0) {
                    break;
                }
                // 获取位置部门
                String dep = imgDateIndex.getString("dep");
                // 获取指定部门信息
                JSONObject depInfo = depAndGrpB.getJSONObject(dep);
                // 判断为空
                if (null == depInfo) {
                    // 创建部门信息
                    depInfo = new JSONObject();
                }
                // 获取位置组别
                String grpB = imgDateIndex.getString("grpB");
                // 定义存储组别信息
                JSONObject grpBInfo;
                // 判断指定组别信息为空
                if (null == depInfo.getJSONObject(grpB)) {
                    // 创建组别信息
                    grpBInfo = new JSONObject();
                } else {
                    // 获取组别信息
                    grpBInfo = depInfo.getJSONObject(grpB);
                }
                // 遍历所在日期键
                imgKeys.forEach(key -> {
                    // 判断组别信息存在当前所在日期
                    if (grpBInfo.containsKey(key)) {
                        // 添加当前日期信息，原本信息加上当前日期信息
                        grpBInfo.put(key,(grpBInfo.getLong(key)+date.getLong(key)));
                    } else {
                        // 添加当前日期信息
                        grpBInfo.put(key,date.getLong(key));
                    }
                });
                // 添加信息
                depInfo.put(grpB,grpBInfo);
                depAndGrpB.put(dep,depInfo);
                // 获取下一个加一
                dateIndexNew++;
            }
//            System.out.println("清理镜像前:");
//            System.out.println(JSON.toJSONString(depAndGrpB));
//            System.out.println(JSON.toJSONString(allImageTasks));
            // 遍历需要删除的位置信息
            for (String dep : depAndGrpB.keySet()) {
                // 根据指定部门获取部门信息
                JSONObject depInfo = depAndGrpB.getJSONObject(dep);
                // 遍历部门信息
                for (String grpB : depInfo.keySet()) {
                    // 根据指定组别获取组别信息
                    JSONObject grpBInfo = depInfo.getJSONObject(grpB);
                    // 遍历组别信息
                    for (String time : grpBInfo.keySet()) {
                        // 获取所在日期
                        long timeLong = Long.parseLong(time);
                        // 获取镜像任务列表
                        List<Task> tasks = getImageTasks(timeLong,grpB,dep,allImageTasks);
                        // 获取镜像任务余剩时间
                        Long zon = getImageZon(timeLong,grpB,dep,allImageTotalTime,depAllTime);
                        // 创建存储删除集合
                        JSONArray removeIndex = new JSONArray();
                        // 遍历镜像任务列表
                        for (int i = 0; i < tasks.size(); i++) {
                            // 获取任务
                            Task task = tasks.get(i);
                            // 判断不是系统任务
                            if (task.getPriority() != -1) {
                                // 判断当前订单编号等于处理的订单编号，并且下标对应信息不为空
                                if (id_O.equals(task.getId_O()) && null != imgId_O.getJSONObject(task.getDateIndex() + "")) {
                                    // 创建删除信息存储
                                    JSONObject removeInfo = new JSONObject();
                                    // 添加信息
                                    removeInfo.put("index",i);
                                    removeInfo.put("wntDurTotal",task.getWntDurTotal());
                                    removeIndex.add(removeInfo);
                                }
                            }
                        }
                        // 遍历删除集合
                        for (int r = removeIndex.size()-1; r >= 0; r--) {
                            // 获取删除信息
                            JSONObject indexJson = removeIndex.getJSONObject(r);
                            // 获取删除下标
                            int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                            // 删除任务列表对应下标的任务
                            tasks.remove(indexNewThis);
                            // 累加总时间
                            zon+=indexJson.getLong("wntDurTotal");
                        }
                        // 将新的镜像任务列表写入镜像
                        setImageTasks(tasks,grpB,dep,timeLong,allImageTasks);
                        setImageZon(zon,grpB,dep,timeLong,allImageTotalTime);
                    }
                }
            }
            System.out.println("清理镜像后:");
            System.out.println(JSON.toJSONString(allImageTasks));
        } else {
            if (allImageTeDate.size() != 0) {
                System.out.println("清理方法 - 进入新的写入镜像");
                isUpdateImageTask = true;
            }
        }
        // 创建返回结果
        JSONObject result = new JSONObject();
        JSONObject resultNew = new JSONObject();
        String id_OP = sonGetOrderFatherId(id_O, id_C, thisInfo, actionIdO, new JSONObject());
        System.out.println("进入清理方法:"+TimeZj.isZ+" - "+id_O+" - "+dateIndex+",id_OP:"+id_OP+",layer:"+layer+",id_PF:"+id_PF);
        System.out.println(JSON.toJSONString(allImageTasks));
        System.out.println(JSON.toJSONString(actionIdO));
        // 获取进度的oDates字段信息
        JSONArray oDates = actionIdO.getJSONObject(id_OP).getJSONObject(layer+"")
                .getJSONObject(id_PF).getJSONArray("oDates");
        Map<String,Asset> assetMap = new HashMap<>();
        // 遍历时间处理信息集合
        for (int i = dateIndex; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取订单编号
            String id_OThis = oDate.getString("id_O");
            Integer indexThis = oDate.getInteger("index");
//            if (id_OThis.equals(id_O) && i == (dateIndex - 1)) {
//                continue;
//            }
            System.out.println("oDate:");
            System.out.println(JSON.toJSONString(oDate));
            // 获取时间处理的组别
            String grpBNew = oDate.getString("grpB");
            String depNew = oDate.getString("dep");
            // 根据组别获取部门
            Asset asset;
            if (assetMap.containsKey(depNew)) {
                asset = assetMap.get(depNew);
            } else {
                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
                assetMap.put(depNew,asset);
            }
            if (null == asset) {
                // 返回为空错误信息
                System.out.println();
                System.out.println("-查询为空!-"+depNew);
                System.out.println();
                continue;
            }
            JSONObject aArrange = getAArrangeNew(asset);
            JSONObject objTask;
            if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
                // 返回为空错误信息
                System.out.println();
                System.out.println("-查询为空!-"+depNew+"-objTask");
                System.out.println(JSON.toJSONString(objThisTaskAll));
                System.out.println(JSON.toJSONString(allImageTasks));
                System.out.println();
                objTask  = objThisTaskAll.getJSONObject(depNew);
            } else {
                // 获取数据库任务信息
                objTask = aArrange.getJSONObject("objTask");
            }
            // 根据订单编号和获取镜像任务所在日期信息
            JSONObject imgTeDateId_O = allImageTeDate.getJSONObject(id_OThis);
            // 判断为空
            if (null == imgTeDateId_O) {
                // 创建一个
                imgTeDateId_O = new JSONObject();
            }
            // 创建镜像任务所在日期信息字典
            JSONObject imgDate = new JSONObject();
            // 添加数据
            imgDate.put("grpB",grpBNew);
            imgDate.put("dep",depNew);
            imgDate.put("date",new JSONObject());
            imgTeDateId_O.put(i+"",imgDate);
            allImageTeDate.put(id_OThis,imgTeDateId_O);

//            // 获取下标
//            System.out.println("indexThis:"+indexThis);
//            // 根据指定下标获取进度信息
//            JSONObject actionIndex = order.getAction().getJSONArray("objAction").getJSONObject(indexThis);
            // 获取任务所在时间
            JSONObject teDateNext = oDate.getJSONObject("teDate");
            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                    +" - teDateNext:"+JSON.toJSONString(teDateNext));
            if (null == teDateNext) {
                teDateNext = new JSONObject();
                teDateNext.put(onlyFirstTimeStamp.getString(random),0);
            }
            // 根据部门信息获取指定组别的信息
            JSONObject grpBTask = objTask.getJSONObject(grpBNew);
            // 判断为空
            if (null == grpBTask) {
                System.out.println("输出全局信息:");
                System.out.println(JSON.toJSONString(allImageTasks));
                System.out.println(JSON.toJSONString(objThisTaskAll));
                if (null == objThisTaskAll.getJSONObject(depNew) || null == objThisTaskAll.getJSONObject(depNew).getJSONObject(grpBNew)) {
                    continue;
                }
                // 获取全局任务信息
                grpBTask = objThisTaskAll.getJSONObject(depNew).getJSONObject(grpBNew);
            }
            // 遍历任务所在时间
            for (String time : teDateNext.keySet()) {
                // 获取所在时间任务信息
                JSONObject timeTask = grpBTask.getJSONObject(time);
                // 获取任务列表
                JSONArray tasksNew = timeTask.getJSONArray("tasks");
                // 获取任务余剩时间
                Long zon = timeTask.getLong("zon");
                // 创建存储删除信息
                JSONArray removeIndex = new JSONArray();
                boolean isOne = false;
                // 遍历任务列表
                for (int t = 1; t < tasksNew.size(); t++) {
                    // 获取任务信息
                    JSONObject taskInside = tasksNew.getJSONObject(t);
                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                    if (taskInside.getString("id_O").equals(id_OThis)
                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                        // 创建删除信息
                        JSONObject removeInfo = new JSONObject();
                        // 添加删除信息
                        removeInfo.put("index",t);
                        removeInfo.put("wntDurTotal",taskInside.getLong("wntDurTotal"));
                        removeIndex.add(removeInfo);
                        // 定义存储是否存在
                        boolean isNormal = true;
                        // 获取订单编号信息
                        JSONObject id_OInfo = result.getJSONObject(sonGetOrderFatherId(id_OThis,id_C,thisInfo,actionIdO,new JSONObject()));
                        // 定义下标信息
                        JSONObject indexInfo;
                        // 判断订单信息为空
                        if (null == id_OInfo) {
                            // 创建信息
                            id_OInfo = new JSONObject();
                            indexInfo = new JSONObject();
                            // 更新状态
                            isNormal = false;
                        } else {
                            // 获取下标信息
                            indexInfo = id_OInfo.getJSONObject(i+"");
                            // 判断为空
                            if (null == indexInfo) {
                                // 创建信息
                                indexInfo = new JSONObject();
                                // 更新状态
                                isNormal = false;
                            }
                        }
                        // 判断存在
                        if (isNormal) {
                            // 累加任务总时间
                            indexInfo.put("wntDurTotal",indexInfo.getLong("wntDurTotal")+taskInside.getLong("wntDurTotal"));
                            // 更新数据
                            id_OInfo.put(i+"",indexInfo);
                        } else {
                            // 更新数据
                            id_OInfo.put(i+"",taskInside);
                        }
                        isOne = true;
                        result.put(sonGetOrderFatherId(id_OThis,id_C,thisInfo,actionIdO,new JSONObject()),id_OInfo);
                    } else {
                        System.out.println("进入清理方法的result-else:");
                        System.out.println(taskInside.getString("id_O")+"___"+id_OThis+"___"+
                                taskInside.getInteger("index")+"___"+indexThis);
                    }
                }
                // 遍历删除集合
                for (int r = removeIndex.size()-1; r >= 0; r--) {
                    // 获取删除信息
                    JSONObject indexJson = removeIndex.getJSONObject(r);
                    // 获取删除下标
                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                    // 删除任务列表对应下标的任务
                    tasksNew.remove(indexNewThis);
                    // 累加任务总时间
                    zon+=indexJson.getLong("wntDurTotal");
                }
                zon = 86400L;
                for (int t = 0; t < tasksNew.size(); t++) {
                    JSONObject taskInside = tasksNew.getJSONObject(t);
                    zon-=taskInside.getLong("wntDurTotal");
                }
                // 添加信息
                timeTask.put("tasks",tasksNew);
                timeTask.put("zon",zon);
                // 创建新任务集合
                List<Task> tasksList = new ArrayList<>();
                // 遍历任务列表
                for (int t = 0; t < tasksNew.size(); t++) {
                    // 把任务转换为类存入新任务集合
                    tasksList.add(JSONObject.parseObject(JSON.toJSONString(tasksNew.getJSONObject(t)),Task.class));
                }
                System.out.println("tasksNew-刚删除:");
                System.out.println(JSON.toJSONString(tasksNew));
                // 是否写入镜像任务
                boolean isSetImageEnd = true;
                // 判断不为空
                if (null != isSetImage){
                    // 获取部门信息
                    JSONObject depSetImage = isSetImage.getJSONObject(depNew);
                    // 判断不为空
                    if (null != depSetImage) {
                        // 获取组别信息
                        long grpBSetImage = depSetImage.getLong(grpBNew) == null ? 0 : depSetImage.getLong(grpBNew);
                        // 判断状态不为0，并且时间不等于当前循环时间
                        if (0 != grpBSetImage && grpBSetImage==Long.parseLong(time)) {
                            // 更新状态
                            isSetImageEnd = false;
                            // 判断当前订单编号的任务所在日期不为空，并且状态不为0，并且循环时间大于当前组别时间
                        } else if (null != imgId_O && (0 != grpBSetImage && Long.parseLong(time)>grpBSetImage)) {
                            System.out.println("进入特殊条件跳入写入数据:");
                            // 更新状态
                            isSetImageEnd = false;
                        }
                    }
                }
                if (isSetImageEnd && null != imgId_O) {
                    System.out.println("--第三不写入条件--");
                    // 更新状态
                    isSetImageEnd = false;
                }
                if (isOne) {
                    isSetImageEnd = true;
                    System.out.println("--isOne 写入条件--");
                }
                // 判断写入
                if (isSetImageEnd) {
                    if (isUpdateImageTask) {
                        Map<String, Map<Long, List<Task>>> depImageTask = allImageTasks.get(depNew);
                        if (null != depImageTask) {
                            Map<Long, List<Task>> grpBImageTask = depImageTask.get(grpBNew);
                            if (null != grpBImageTask) {
                                List<Task> tasks = grpBImageTask.get(Long.parseLong(time));
                                if (null != tasks) {
                                    long zonImage = 86400L;
                                    for (Task task : tasks) {
                                        zonImage -= task.getWntDurTotal();
                                    }
                                    // 创建存储删除信息
                                    removeIndex = new JSONArray();
                                    // 遍历任务列表
                                    for (int t = 1; t < tasks.size(); t++) {
                                        // 获取任务信息
                                        Task taskInside = tasks.get(t);
                                        // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                                        if (taskInside.getId_O().equals(id_OThis)
                                                && Objects.equals(taskInside.getIndex(), indexThis)) {
                                            // 创建删除信息
                                            JSONObject removeInfo = new JSONObject();
                                            // 添加删除信息
                                            removeInfo.put("index",t);
                                            removeInfo.put("wntDurTotal",taskInside.getWntDurTotal());
                                            removeIndex.add(removeInfo);
                                            // 定义存储是否存在
                                            boolean isNormal = true;
                                            // 获取订单编号信息
                                            JSONObject id_OInfo = resultNew.getJSONObject(id_OThis);
                                            // 定义下标信息
                                            JSONObject indexInfo;
                                            // 判断订单信息为空
                                            if (null == id_OInfo) {
                                                // 创建信息
                                                id_OInfo = new JSONObject();
                                                indexInfo = new JSONObject();
                                                // 更新状态
                                                isNormal = false;
                                            } else {
                                                // 获取下标信息
                                                indexInfo = id_OInfo.getJSONObject(indexThis+"");
                                                // 判断为空
                                                if (null == indexInfo) {
                                                    // 创建信息
                                                    indexInfo = new JSONObject();
                                                    // 更新状态
                                                    isNormal = false;
                                                }
                                            }
                                            // 判断存在
                                            if (isNormal) {
                                                // 累加任务总时间
                                                indexInfo.put("wntDurTotal",indexInfo.getLong("wntDurTotal")+taskInside.getWntDurTotal());
                                                // 更新数据
                                                id_OInfo.put(indexThis+"",indexInfo);
                                            } else {
                                                // 更新数据
                                                id_OInfo.put(indexThis+"",taskInside);
                                            }
                                            resultNew.put(id_OThis,id_OInfo);
                                        }
                                    }
                                    // 遍历删除集合
                                    for (int r = removeIndex.size()-1; r >= 0; r--) {
                                        // 获取删除信息
                                        JSONObject indexJson = removeIndex.getJSONObject(r);
                                        // 获取删除下标
                                        int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                                        // 删除任务列表对应下标的任务
                                        tasks.remove(indexNewThis);
                                        // 累加任务总时间
                                        zonImage+=indexJson.getLong("wntDurTotal");
                                    }
//                                    System.out.println("tasks: -写入镜像:"+Long.valueOf(time)+" - "+grpBNew+" - "+depNew);
//                                    System.out.println(JSON.toJSONString(tasks));
                                    // 写入镜像任务
                                    setImageTasks(tasks,grpBNew,depNew, Long.valueOf(time),allImageTasks);
                                    setImageZon(zonImage,grpBNew,depNew, Long.valueOf(time),allImageTotalTime);
                                }
                            }
                        }
                    } else {
                        // 写入镜像任务
                        setImageTasks(tasksList,grpBNew,depNew, Long.valueOf(time),allImageTasks);
                        setImageZon(zon,grpBNew,depNew, Long.valueOf(time),allImageTotalTime);
                    }
//                    System.out.println("进入覆盖当前任务列表:");
                    // 写入任务到全局任务信息
                    setTasksAndZon(tasksList,grpBNew,depNew,Long.valueOf(time),zon,objThisTaskAll);
                } else {
                    System.out.println("tasksList: -跳过写入镜像:"+Long.valueOf(time)+" - "+grpBNew+" - "+depNew);
//                    System.out.println(JSON.toJSONString(tasksList));
                }
                grpBTask.put(time,timeTask);
            }
            // 定义存储删除信息
            JSONArray removeIndex = new JSONArray();
            // 遍历当前处理任务信息列表
            for (int t = 1; t < thisTasks.size(); t++) {
                // 获取任务信息
                Task taskInside = thisTasks.get(t);
                // 判断循环订单编号等于当前处理订单编号，并且循环下标等于当前处理下标
                if (taskInside.getId_O().equals(id_OThis)
                        && Objects.equals(taskInside.getIndex(), indexThis)) {
                    // 添加删除下标
                    removeIndex.add(t);
                }
            }
            // 遍历删除信息
            for (int r = removeIndex.size()-1; r >= 0; r--) {
                // 转换信息为int
                int indexNewThis = Integer.parseInt(removeIndex.getString(r));
                // 删除指定下标的任务
                thisTasks.remove(indexNewThis);
            }
            objTask.put(grpBNew,grpBTask);
//            System.out.println("是否拿当前的: - "+(isGetThisTaskAll?"是":"不是"));
//            System.out.println("清理状态:"+clearStatusThis);
        }
        if (layer > 1) {
            JSONObject thisInfoClearOPLayer = GsThisInfo.getThisInfoClearOPLayer(thisInfo);
            boolean isGetUpLayer = false;
            if (null != thisInfoClearOPLayer) {
                JSONObject opInfo = thisInfoClearOPLayer.getJSONObject(id_OP);
                if (null != opInfo) {
                    if (!opInfo.containsKey((layer-1)+"")) {
                        opInfo.put((layer-1)+"",new JSONObject());
                        thisInfoClearOPLayer.put(id_OP,opInfo);
                        isGetUpLayer = true;
                    }
                } else {
                    thisInfoClearOPLayer.put(id_OP,qt.setJson((layer-1)+"",new JSONObject()));
                    isGetUpLayer = true;
                }
            } else {
                thisInfoClearOPLayer = new JSONObject();
                thisInfoClearOPLayer.put(id_OP,qt.setJson((layer-1)+"",new JSONObject()));
                isGetUpLayer = true;
            }
            if (isGetUpLayer) {
                GsThisInfo.setThisInfoClearOPLayer(thisInfo,thisInfoClearOPLayer);
                JSONObject layerInfo = actionIdO.getJSONObject(id_OP).getJSONObject((layer - 1) + "");
                GsThisInfo.setThisInfoSonLayerInfo(thisInfo,qt.setJson(id_OP,qt.setJson(layer+"",qt.setJson("layerCount",1,"layerMaxData",new JSONObject()))));
                JSONObject clearPfDate = new JSONObject();
                for (String pf : layerInfo.keySet()) {
                    oDates = layerInfo.getJSONObject(pf).getJSONArray("oDates");
                    JSONObject oDateSta = oDates.getJSONObject(0);
                    clearPfDate.put(pf,clearOldTask(oDateSta.getString("id_O"),-1,id_C,objThisTaskAll,clearStatus
                            ,allImageTasks,allImageTotalTime,allImageTeDate,thisTasks,isSetImage,depAllTime
                            ,random,onlyFirstTimeStamp,actionIdO,thisInfo,(layer-1),pf));
                }

                thisInfoClearOPLayer = GsThisInfo.getThisInfoClearOPLayer(thisInfo);
                JSONObject opInfo = thisInfoClearOPLayer.getJSONObject(id_OP);
                opInfo.put((layer-1)+"",clearPfDate);
                thisInfoClearOPLayer.put(id_OP,opInfo);
                GsThisInfo.setThisInfoClearOPLayer(thisInfo,thisInfoClearOPLayer);
            } else {
                JSONObject thisInfoSonLayerInfo = GsThisInfo.getThisInfoSonLayerInfo(thisInfo);
                JSONObject opInfo = thisInfoSonLayerInfo.getJSONObject(id_OP);
                JSONObject layerInfo = opInfo.getJSONObject(layer + "");
                layerInfo.put("layerCount",layerInfo.getInteger("layerCount")+1);
                opInfo.put(layer + "",layerInfo);
                thisInfoSonLayerInfo.put(id_OP,opInfo);
                GsThisInfo.setThisInfoSonLayerInfo(thisInfo,thisInfoSonLayerInfo);
            }
        }
        System.out.println("写入的objTask:");
//        System.out.println(JSON.toJSONString(objTask));
//        System.out.println();
        System.out.println(JSON.toJSONString(result));
//        System.out.println();
//        System.out.println(JSON.toJSONString(objThisTaskAll));
        System.out.println();
        System.out.println(JSON.toJSONString(allImageTasks));
//        System.out.println();
//        System.out.println(JSON.toJSONString(allImageTotalTime));
//        System.out.println("isSetObjTask:"+isSetObjTask);
//        System.out.println();
//        System.out.println(JSON.toJSONString(result));
        return result;
    }

    protected void clearThisDayEasyTaskAndSaveFc(String id_C,String dep,long thisDay){
        System.out.println("进入Api清理方法:"+" - "+dep+" - "+thisDay);
        Asset as = qt.getConfig(id_C, "d-" + dep, "aArrange");
        System.out.println(JSON.toJSONString(as));
        if (null == as || null == as.getAArrange() || null == as.getAArrange().getJSONObject("objEasy")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 获取进度的oDates字段信息
        JSONObject objEasy = as.getAArrange().getJSONObject("objEasy");
        if (null == objEasy) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject dayData = objEasy.getJSONObject(thisDay+"");
        if (null == dayData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        long wntLeft = dayData.getLong("wntLeft");
        JSONArray easyTasks = dayData.getJSONArray("easyTasks");
        JSONArray clearTasks = new JSONArray();
        JSONObject id_OPs = new JSONObject();
        // 遍历时间处理信息集合
        for (int i = 0; i < easyTasks.size(); i++) {
            JSONObject task = easyTasks.getJSONObject(i);
            wntLeft+=task.getLong("timeTotal");
            if (!id_OPs.containsKey(task.getString("id_PF"))) {
                id_OPs.put(task.getString("id_PF"),1);
                clearTasks.add(task);
            }
        }
        dayData.put("wntLeft",wntLeft);
        dayData.put("easyTasks",new JSONArray());
        objEasy.put(thisDay+"",dayData);
        dgJumpDayEasyClear(clearTasks,id_OPs,objEasy,thisDay);
        qt.setMDContent(as.getId(),qt.setJson(
//                "aArrange.objEasy",objEasy,
                "aArrange.objEasyTaskClear",clearTasks
                ,"aArrange.objEasyTaskClearId_OP",id_OPs), Asset.class);
        System.out.println("清理后输出-clearThisDayEasyTaskAndSaveFc:");
        System.out.println(JSON.toJSONString(objEasy));
        System.out.println(JSON.toJSONString(clearTasks));
        System.out.println(JSON.toJSONString(id_OPs));
    }
    private void dgJumpDayEasyClear(JSONArray clearTasks,JSONObject id_OPs,JSONObject objEasy,long thisDay){
        thisDay+=86400;
        JSONObject dayData = objEasy.getJSONObject(thisDay+"");
        if (null == dayData) {
            return;
        }
        long wntLeft = dayData.getLong("wntLeft");
        JSONArray easyTasks = dayData.getJSONArray("easyTasks");
        // 遍历时间处理信息集合
        for (int i = 0; i < easyTasks.size(); i++) {
            JSONObject task = easyTasks.getJSONObject(i);
            wntLeft+=task.getLong("timeTotal");
            if (!id_OPs.containsKey(task.getString("id_PF"))) {
                id_OPs.put(task.getString("id_PF"),1);
                clearTasks.add(task);
            }
        }
        dayData.put("wntLeft",wntLeft);
        dayData.put("easyTasks",new JSONArray());
        objEasy.put(thisDay+"",dayData);
        dgJumpDayEasyClear(clearTasks,id_OPs,objEasy,thisDay);
    }

    /**
     * 清理指定层级、父产品编号、oDates下标的任务方法
     * @param id_O  订单编号
     * @param dateIndex oDates下标
     * @param id_C  公司编号
     * @param layer oDateObj层级
     * @param id_PF oDateObj父产品编号
     * @return  处理结果
     */
    protected JSONObject clearOldTaskNew(String id_O,int dateIndex,String id_C,String layer,String id_PF){
        System.out.println("进入Api清理方法:"+" - "+id_O+" - "+dateIndex);
        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        Map<String,Asset> assetMap = new HashMap<>();
        // 获取进度的oDates字段信息
        JSONObject oDateObj = order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = dateIndex; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取订单编号
            String id_OThis = oDate.getString("id_O");
            Integer indexThis = oDate.getInteger("index");
            // 获取时间处理的组别
            String grpBNew = oDate.getString("grpB");
            String depNew = oDate.getString("dep");
            // 根据组别获取部门
            Asset asset;
            if (assetMap.containsKey(depNew)) {
                asset = assetMap.get(depNew);
            } else {
                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
                System.out.println("查询数据-1："+depNew);
                assetMap.put(depNew,asset);
            }
            if (null == asset) {
                // 返回为空错误信息
                System.out.println();
                System.out.println("-查询为空!-"+depNew);
                System.out.println();
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            JSONObject aArrange = getAArrangeNew(asset);
            JSONObject objTask;
            if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            } else {
                // 获取数据库任务信息
                objTask = aArrange.getJSONObject("objTask");
            }
            // 获取任务所在时间
            JSONObject teDateNext = oDate.getJSONObject("teDate");
            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                    +" - teDateNext:"+JSON.toJSONString(teDateNext));
            if (null == teDateNext) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            // 根据部门信息获取指定组别的信息
            JSONObject grpBTask = objTask.getJSONObject(grpBNew);
            // 判断为空
            if (null == grpBTask) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            // 遍历任务所在时间
            for (String time : teDateNext.keySet()) {
                // 获取所在时间任务信息
                JSONObject timeTask = grpBTask.getJSONObject(time);
                // 获取任务列表
                JSONArray tasksNew = timeTask.getJSONArray("tasks");
                // 获取任务余剩时间
                Long zon = timeTask.getLong("zon");
                // 创建存储删除信息
                JSONArray removeIndex = new JSONArray();
                // 遍历任务列表
                for (int t = 1; t < tasksNew.size(); t++) {
                    // 获取任务信息
                    JSONObject taskInside = tasksNew.getJSONObject(t);
                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                    if (taskInside.getString("id_O").equals(id_OThis)
                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                        // 创建删除信息
                        JSONObject removeInfo = new JSONObject();
                        // 添加删除信息
                        removeInfo.put("index",t);
                        removeInfo.put("wntDurTotal",taskInside.getLong("wntDurTotal"));
                        removeIndex.add(removeInfo);
                        if ((tasksNew.size() - 1) <= (t + 1)) {
                            JSONObject taskInsideNew = tasksNew.getJSONObject((t + 1));
                            if (taskInsideNew.getInteger("priority") != -1 && taskInsideNew.getLong("teDelayDate") > 0) {
                                taskInsideNew.put("updateTime",true);
                            }
                        }
                    }
                }
                // 遍历删除集合
                for (int r = removeIndex.size()-1; r >= 0; r--) {
                    // 获取删除信息
                    JSONObject indexJson = removeIndex.getJSONObject(r);
                    // 获取删除下标
                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                    // 删除任务列表对应下标的任务
                    tasksNew.remove(indexNewThis);
                    // 累加任务总时间
                    zon+=indexJson.getLong("wntDurTotal");
                }
                // 添加信息
                timeTask.put("tasks",tasksNew);
                timeTask.put("zon",zon);
                grpBTask.put(time,timeTask);
            }
            objTask.put(grpBNew,grpBTask);
            aArrange.put("objTask",objTask);
            asset.setAArrange(aArrange);
            assetMap.put(depNew,asset);
        }
        System.out.println("进入前:");
        System.out.println(JSON.toJSONString(assetMap));
        dgClearOldTask(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C);
        System.out.println("清理后输出:");
        System.out.println(JSON.toJSONString(assetMap));
        List<JSONObject> list = new ArrayList<>();
        JSONObject id_As = new JSONObject();
        assetMap.forEach((key,val)->{
            list.add(qt.setJson("id",val.getId(),"updateData"
                    ,qt.setJson("aArrange.objTask",val.getAArrange().getJSONObject("objTask"))));
            id_As.put(key,val.getId());
        });
        qt.setMDContentFast(list, Asset.class);
        return id_As;
    }

    /**
     * 递归清理指定层级的上一层方法
     * @param oDateObj  所有零件信息
     * @param layer oDateObj层级
     * @param id_PF oDateObj父产品编号
     * @param assetMap  查询过的资产信息
     * @param id_C  公司编号
     */
    private void dgClearOldTask(JSONObject oDateObj,String layer,String id_PF
            ,Map<String,Asset> assetMap,String id_C){
        if ("0".equals(layer)) {
            return;
        }
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = 0; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取订单编号
            String id_OThis = oDate.getString("id_O");
            Integer indexThis = oDate.getInteger("index");
            // 获取时间处理的组别
            String grpBNew = oDate.getString("grpB");
            String depNew = oDate.getString("dep");
            // 根据组别获取部门
            Asset asset;
            if (assetMap.containsKey(depNew)) {
                asset = assetMap.get(depNew);
            } else {
                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
                System.out.println("查询数据-2："+depNew);
                assetMap.put(depNew,asset);
            }
            if (null == asset) {
                // 返回为空错误信息
                System.out.println();
                System.out.println("-查询为空!-"+depNew);
                System.out.println();
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            JSONObject aArrange = getAArrangeNew(asset);
            JSONObject objTask;
            if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            } else {
                // 获取数据库任务信息
                objTask = aArrange.getJSONObject("objTask");
            }
            // 获取任务所在时间
            JSONObject teDateNext = oDate.getJSONObject("teDate");
            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                    +" - teDateNext:"+JSON.toJSONString(teDateNext));
            if (null == teDateNext) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            // 根据部门信息获取指定组别的信息
            JSONObject grpBTask = objTask.getJSONObject(grpBNew);
            // 判断为空
            if (null == grpBTask) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            // 遍历任务所在时间
            for (String time : teDateNext.keySet()) {
                // 获取所在时间任务信息
                JSONObject timeTask = grpBTask.getJSONObject(time);
                // 获取任务列表
                JSONArray tasksNew = timeTask.getJSONArray("tasks");
                // 获取任务余剩时间
                Long zon = timeTask.getLong("zon");
                // 创建存储删除信息
                JSONArray removeIndex = new JSONArray();
                // 遍历任务列表
                for (int t = 1; t < tasksNew.size(); t++) {
                    // 获取任务信息
                    JSONObject taskInside = tasksNew.getJSONObject(t);
                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                    if (taskInside.getString("id_O").equals(id_OThis)
                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                        // 创建删除信息
                        JSONObject removeInfo = new JSONObject();
                        // 添加删除信息
                        removeInfo.put("index",t);
                        removeInfo.put("wntDurTotal",taskInside.getLong("wntDurTotal"));
                        removeIndex.add(removeInfo);
                        if ((tasksNew.size() - 1) <= (t + 1)) {
                            JSONObject taskInsideNew = tasksNew.getJSONObject((t + 1));
                            if (taskInsideNew.getInteger("priority") != -1 && taskInsideNew.getLong("teDelayDate") > 0) {
                                taskInsideNew.put("updateTime",true);
                            }
                        }
                    }
                }
                // 遍历删除集合
                for (int r = removeIndex.size()-1; r >= 0; r--) {
                    // 获取删除信息
                    JSONObject indexJson = removeIndex.getJSONObject(r);
                    // 获取删除下标
                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                    // 删除任务列表对应下标的任务
                    tasksNew.remove(indexNewThis);
                    // 累加任务总时间
                    zon+=indexJson.getLong("wntDurTotal");
                }
                // 添加信息
                timeTask.put("tasks",tasksNew);
                timeTask.put("zon",zon);
                grpBTask.put(time,timeTask);
            }
            objTask.put(grpBNew,grpBTask);
            aArrange.put("objTask",objTask);
            asset.setAArrange(aArrange);
            assetMap.put(depNew,asset);
        }
        dgClearOldTask(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C);
    }

    /**
     * 清理简化任务的指定层级、父产品编号、oDates下标的任务方法
     * @param id_O  订单编号
     * @param dateIndex oDates下标
     * @param id_C  公司编号
     * @param layer oDateObj层级
     * @param id_PF oDateObj父产品编号
     * @return  处理结果
     */
    protected JSONObject clearOldTaskEasy(String id_O,int dateIndex,String id_C,String layer,String id_PF){
        System.out.println("进入Api清理Easy方法:"+" - "+id_O+" - "+dateIndex);
        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        Map<String,Asset> assetMap = new HashMap<>();
        // 获取进度的oDates字段信息
        JSONObject oDateObj = order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = dateIndex; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取订单编号
            String id_OThis = oDate.getString("id_O");
            Integer indexThis = oDate.getInteger("index");
            // 获取时间处理的组别
            String grpBNew = oDate.getString("grpB");
            String depNew = oDate.getString("dep");
            // 根据组别获取部门
            Asset asset;
            if (assetMap.containsKey(depNew)) {
                asset = assetMap.get(depNew);
            } else {
                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
                System.out.println("查询数据-1："+depNew);
                assetMap.put(depNew,asset);
            }
            if (null == asset) {
                // 返回为空错误信息
                System.out.println();
                System.out.println("-查询为空!-"+depNew);
                System.out.println();
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            JSONObject aArrange = getAArrangeNew(asset);
            JSONObject objEasy;
            if (null == aArrange || null == aArrange.getJSONObject("objEasy")) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            } else {
                // 获取数据库任务信息
                objEasy = aArrange.getJSONObject("objEasy");
            }
            // 获取任务所在时间
            JSONObject teEasyDate = oDate.getJSONObject("teEasyDate");
            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                    +" - teEasyDate:"+JSON.toJSONString(teEasyDate));
            if (null == teEasyDate) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            // 遍历任务所在时间
            for (String time : teEasyDate.keySet()) {
                // 获取所在时间任务信息
                JSONObject timeTask = objEasy.getJSONObject(time);
                // 获取任务列表
                JSONArray easyTasks = timeTask.getJSONArray("easyTasks");
                // 获取任务余剩时间
                Long wntLeft = timeTask.getLong("wntLeft");
                // 创建存储删除信息
                JSONArray removeIndex = new JSONArray();
                // 遍历任务列表
                for (int t = 0; t < easyTasks.size(); t++) {
                    // 获取任务信息
                    JSONObject taskInside = easyTasks.getJSONObject(t);
                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                    if (taskInside.getString("id_O").equals(id_OThis)
                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                        // 创建删除信息
                        JSONObject removeInfo = new JSONObject();
                        // 添加删除信息
                        removeInfo.put("index",t);
                        removeInfo.put("timeTotal",taskInside.getLong("timeTotal"));
                        removeIndex.add(removeInfo);
                        if (easyTasks.size() > 1 && (t + 1) <= (easyTasks.size() - 1)) {
                            JSONObject taskInsideNew = easyTasks.getJSONObject((t + 1));
                            if (taskInsideNew.getLong("timeTotal") > 0) {
                                taskInsideNew.put("updateTime",true);
                            }
                        }
                    }
                }
                // 遍历删除集合
                for (int r = removeIndex.size()-1; r >= 0; r--) {
                    // 获取删除信息
                    JSONObject indexJson = removeIndex.getJSONObject(r);
                    // 获取删除下标
                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                    // 删除任务列表对应下标的任务
                    easyTasks.remove(indexNewThis);
                    // 累加任务总时间
                    wntLeft+=indexJson.getLong("timeTotal");
                }
                // 添加信息
                timeTask.put("easyTasks",easyTasks);
                timeTask.put("wntLeft",wntLeft);
                objEasy.put(time,timeTask);
            }
            aArrange.put("objEasy",objEasy);
            asset.setAArrange(aArrange);
            assetMap.put(depNew,asset);
        }
        System.out.println("进入前:");
        System.out.println(JSON.toJSONString(assetMap));
        dgClearOldTaskEasy(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C);
        System.out.println("清理后输出:");
        System.out.println(JSON.toJSONString(assetMap));
        List<JSONObject> list = new ArrayList<>();
        JSONObject id_As = new JSONObject();
        assetMap.forEach((key,val)->{
            list.add(qt.setJson("id",val.getId(),"updateData"
                    ,qt.setJson("aArrange.objEasy",val.getAArrange().getJSONObject("objEasy"))));
            id_As.put(key,val.getId());
        });
        qt.setMDContentFast(list, Asset.class);
        return id_As;
    }
    /**
     * 递归清理简化任务的指定层级的上一层方法
     * @param oDateObj  所有零件信息
     * @param layer oDateObj层级
     * @param id_PF oDateObj父产品编号
     * @param assetMap  查询过的资产信息
     * @param id_C  公司编号
     */
    private void dgClearOldTaskEasy(JSONObject oDateObj,String layer,String id_PF
            ,Map<String,Asset> assetMap,String id_C){
        if ("0".equals(layer)) {
            return;
        }
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = 0; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取订单编号
            String id_OThis = oDate.getString("id_O");
            Integer indexThis = oDate.getInteger("index");
            // 获取时间处理的组别
            String grpBNew = oDate.getString("grpB");
            String depNew = oDate.getString("dep");
            // 根据组别获取部门
            Asset asset;
            if (assetMap.containsKey(depNew)) {
                asset = assetMap.get(depNew);
            } else {
                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
                System.out.println("查询数据-2："+depNew);
                assetMap.put(depNew,asset);
            }
            if (null == asset) {
                // 返回为空错误信息
                System.out.println();
                System.out.println("-查询为空!-"+depNew);
                System.out.println();
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            JSONObject aArrange = getAArrangeNew(asset);
            JSONObject objEasy;
            if (null == aArrange || null == aArrange.getJSONObject("objEasy")) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            } else {
                // 获取数据库任务信息
                objEasy = aArrange.getJSONObject("objEasy");
            }
            // 获取任务所在时间
            JSONObject teEasyDate = oDate.getJSONObject("teEasyDate");
            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                    +" - teDateNext:"+JSON.toJSONString(teEasyDate));
            if (null == teEasyDate) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
            }
            // 遍历任务所在时间
            for (String time : teEasyDate.keySet()) {
                // 获取所在时间任务信息
                JSONObject timeTask = objEasy.getJSONObject(time);
                // 获取任务列表
                JSONArray easyTasks = timeTask.getJSONArray("easyTasks");
                // 获取任务余剩时间
                Long wntLeft = timeTask.getLong("wntLeft");
                // 创建存储删除信息
                JSONArray removeIndex = new JSONArray();
                // 遍历任务列表
                for (int t = 0; t < easyTasks.size(); t++) {
                    // 获取任务信息
                    JSONObject taskInside = easyTasks.getJSONObject(t);
                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                    if (taskInside.getString("id_O").equals(id_OThis)
                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                        // 创建删除信息
                        JSONObject removeInfo = new JSONObject();
                        // 添加删除信息
                        removeInfo.put("index",t);
                        removeInfo.put("timeTotal",taskInside.getLong("timeTotal"));
                        removeIndex.add(removeInfo);
                        if (easyTasks.size() > 1 && (t + 1) <= (easyTasks.size() - 1)) {
                            JSONObject taskInsideNew = easyTasks.getJSONObject((t + 1));
                            if (taskInsideNew.getLong("timeTotal") > 0) {
                                taskInsideNew.put("updateTime",true);
                            }
                        }
                    }
                }
                // 遍历删除集合
                for (int r = removeIndex.size()-1; r >= 0; r--) {
                    // 获取删除信息
                    JSONObject indexJson = removeIndex.getJSONObject(r);
                    // 获取删除下标
                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                    // 删除任务列表对应下标的任务
                    easyTasks.remove(indexNewThis);
                    // 累加任务总时间
                    wntLeft+=indexJson.getLong("timeTotal");
                }
                // 添加信息
                timeTask.put("easyTasks",easyTasks);
                timeTask.put("wntLeft",wntLeft);
                objEasy.put(time,timeTask);
            }
            aArrange.put("objEasy",objEasy);
            asset.setAArrange(aArrange);
            assetMap.put(depNew,asset);
        }
        dgClearOldTaskEasy(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C);
    }


    protected void clearThisDayTaskAndSaveClearFollowUpMethod(String id_C,String dep,String grpB,long thisDay){
        System.out.println("完整版任务清理-进入Api清理方法:"+" - "+dep+" - "+thisDay);
        Asset as = qt.getConfig(id_C, "d-" + dep, "aArrange");
        if (null == as || null == as.getAArrange() || null == as.getAArrange().getJSONObject("objTask")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 获取进度的oDates字段信息
        JSONObject grpBData = as.getAArrange().getJSONObject("objTask").getJSONObject(grpB);
        if (null == grpBData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject dayData = grpBData.getJSONObject(thisDay+"");
        if (null == dayData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONArray tasks = dayData.getJSONArray("tasks");
        JSONArray clearTasks = new JSONArray();
        JSONObject id_OPs = new JSONObject();
        JSONObject clearTasksReal = new JSONObject();
        JSONObject clearDep = new JSONObject();
        // 遍历时间处理信息集合
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            if (task.getInteger("priority") != -1) {
                addClearTask(task,clearTasks,id_OPs,clearDep,clearTasksReal,dep,grpB);
            }
        }
        dgJumpDayClear(clearTasks,id_OPs,grpBData,thisDay,clearDep,clearTasksReal,dep,grpB);
        System.out.println("输出结果-clearThisDayTaskAndSaveFc:");
        System.out.println(JSON.toJSONString(clearTasks));
        JSONObject id_AsAll = new JSONObject();
        for (int i = 0; i < clearTasks.size(); i++) {
            JSONObject clearData = clearTasks.getJSONObject(i);
            JSONObject id_As = clearOldTaskNewAndClosing(clearData.getString("id_OP"), clearData.getInteger("dateIndex")
                    , clearData.getString("id_C"), clearData.getString("layer")
                    , clearData.getString("id_PF"),clearTasks,id_OPs,clearDep,clearTasksReal);
            id_AsAll.putAll(id_As);
        }
        System.out.println("清理后输出-clearThisDayTaskAndSaveFc-1:");
        System.out.println("clearTasks:");
        System.out.println(JSON.toJSONString(clearTasks));
        System.out.println("clearTasksReal:");
        System.out.println(JSON.toJSONString(clearTasksReal));
        System.out.println("id_OPs:");
        System.out.println(JSON.toJSONString(id_OPs));
        System.out.println("clearDep:");
        System.out.println(JSON.toJSONString(clearDep));
        System.out.println("id_AsAll:");
        System.out.println(JSON.toJSONString(id_AsAll));
        JSONObject depClearTasks = new JSONObject();
        for (String id_OP : clearTasksReal.keySet()) {
            JSONObject opData = clearTasksReal.getJSONObject(id_OP);
            for (String layerIn : opData.keySet()) {
                JSONObject layerData = opData.getJSONObject(layerIn);
                for (String id_PFIn : layerData.keySet()) {
                    JSONObject taskIn = layerData.getJSONObject(id_PFIn);
                    String depIn = taskIn.getString("dep");
                    JSONArray tasksIn;
                    if (depClearTasks.containsKey(depIn)) {
                        tasksIn = depClearTasks.getJSONArray(depIn);
                    } else {
                        tasksIn = new JSONArray();
                    }
                    taskIn.remove("dep");
                    taskIn.remove("grpB");
                    tasksIn.add(qt.cloneObj(taskIn));
                    depClearTasks.put(depIn,tasksIn);
                }
            }
        }
        System.out.println("depClearTasks:");
        System.out.println(JSON.toJSONString(depClearTasks));
        for (String depInside : clearDep.keySet()) {
            String id_A = id_AsAll.getString(depInside);
            Asset asset = qt.getMDContent(id_A, "aArrange", Asset.class);
            if (null == asset || null == asset.getAArrange()) {
                System.out.println("写入Asset为空，id是:"+id_A);
                continue;
            }
            JSONArray objTaskClear = asset.getAArrange().getJSONArray("objTaskClear");
            if (null == objTaskClear) {
                objTaskClear = new JSONArray();
            }
            objTaskClear.addAll(depClearTasks.getJSONArray(depInside));
            JSONObject objTaskClearId_OP = asset.getAArrange().getJSONObject("objTaskClearId_OP");
            if (null == objTaskClearId_OP) {
                objTaskClearId_OP = new JSONObject();
            }
            for (String id_OP : id_OPs.keySet()) {
                if (!objTaskClearId_OP.containsKey(id_OP)) {
                    objTaskClearId_OP.put(id_OP,id_OPs.getInteger(id_OP));
                }
            }
            qt.setMDContent(id_A,qt.setJson("aArrange.objTaskClear",objTaskClear
                    ,"aArrange.objTaskClearId_OP",objTaskClearId_OP), Asset.class);
            if (!depInside.equals(dep)) {
                JSONObject depInfo = clearDep.getJSONObject(depInside);
                for (String grpBInside : depInfo.keySet()) {
                    if (!grpBInside.equals(grpB)) {
                        clearThisDayTaskAndSaveFc(id_C,depInside,grpBInside,(thisDay+86400L));
                    }
                }
            }
        }
    }
    protected JSONObject clearOldTaskNewAndClosing(String id_O,int dateIndex,String id_C,String layer
            ,String id_PF,JSONArray clearTasks,JSONObject id_OPs
            ,JSONObject clearDep,JSONObject clearTasksReal){
        System.out.println("完整版任务清理-子-进入Api清理方法:"+" - "+id_O+" - "+dateIndex);
        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        Map<String,Asset> assetMap = new HashMap<>();
        // 获取进度的oDates字段信息
        JSONObject oDateObj = order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = dateIndex; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            clearOldTaskNewAndClosingSon(oDate,assetMap,id_C,i,clearTasks,id_OPs,clearDep,clearTasksReal);
        }
        System.out.println("进入前:");
        System.out.println(JSON.toJSONString(assetMap));
        dgClearOldTaskAndClosing(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C,clearTasks,id_OPs,clearDep,clearTasksReal);
        System.out.println("清理后输出:");
        System.out.println(JSON.toJSONString(assetMap));
        List<JSONObject> list = new ArrayList<>();
        JSONObject id_As = new JSONObject();
        assetMap.forEach((key,val)->{
            list.add(qt.setJson("id",val.getId(),"updateData"
                    ,qt.setJson("aArrange.objTask",val.getAArrange().getJSONObject("objTask"))));
            id_As.put(key,val.getId());
        });
        qt.setMDContentFast(list, Asset.class);
        return id_As;
    }
    protected void clearOldTaskNewAndClosingSon(JSONObject oDate,Map<String,Asset> assetMap,String id_C,int i
            ,JSONArray clearTasks,JSONObject id_OPs,JSONObject clearDep,JSONObject clearTasksReal){
        // 获取订单编号
        String id_OThis = oDate.getString("id_O");
        Integer indexThis = oDate.getInteger("index");
        // 获取时间处理的组别
        String grpBNew = oDate.getString("grpB");
        String depNew = oDate.getString("dep");
        // 根据组别获取部门
        Asset asset;
        if (assetMap.containsKey(depNew)) {
            asset = assetMap.get(depNew);
        } else {
            asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
            System.out.println("查询数据-1："+depNew);
            assetMap.put(depNew,asset);
        }
        if (null == asset) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-"+depNew);
            System.out.println();
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject aArrange = getAArrangeNew(asset);
        JSONObject objTask;
        if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        } else {
            // 获取数据库任务信息
            objTask = aArrange.getJSONObject("objTask");
        }
        // 获取任务所在时间
        JSONObject teDateNext = oDate.getJSONObject("teDate");
        System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                +" - teDateNext:"+JSON.toJSONString(teDateNext));
        if (null == teDateNext) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 根据部门信息获取指定组别的信息
        JSONObject grpBTask = objTask.getJSONObject(grpBNew);
        // 判断为空
        if (null == grpBTask) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 遍历任务所在时间
        for (String time : teDateNext.keySet()) {
            // 获取所在时间任务信息
            JSONObject timeTask = grpBTask.getJSONObject(time);
            // 获取任务列表
            JSONArray tasksNew = timeTask.getJSONArray("tasks");
            // 获取任务余剩时间
            Long zon = timeTask.getLong("zon");
            // 创建存储删除信息
            JSONArray removeIndex = new JSONArray();
            int reIndex = -1;
            // 遍历任务列表
            for (int t = 1; t < tasksNew.size(); t++) {
                // 获取任务信息
                JSONObject taskInside = tasksNew.getJSONObject(t);
                // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                if (taskInside.getString("id_O").equals(id_OThis)
                        && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                    // 添加删除信息
                    removeIndex.add(qt.setJson("index",t,"wntDurTotal",taskInside.getLong("wntDurTotal")));
                    reIndex = t;
                    addClearTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew,grpBNew);
                    if ((tasksNew.size() - 1) <= (t + 1)) {
                        JSONObject taskInsideNew = tasksNew.getJSONObject((t + 1));
                        if (taskInsideNew.getInteger("priority") != -1 && taskInsideNew.getLong("teDelayDate") > 0) {
                            taskInsideNew.put("updateTime",true);
                        }
                    }
                }
                else {
                    if (reIndex != -1 && t > reIndex && taskInside.getInteger("priority") != -1) {
                        // 添加删除信息
                        removeIndex.add(qt.setJson("index",t,"wntDurTotal",taskInside.getLong("wntDurTotal")));
                        addClearTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew,grpBNew);
                    }
                }
            }
            // 遍历删除集合
            for (int r = removeIndex.size()-1; r >= 0; r--) {
                // 获取删除信息
                JSONObject indexJson = removeIndex.getJSONObject(r);
                // 获取删除下标
                int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                // 删除任务列表对应下标的任务
                tasksNew.remove(indexNewThis);
                // 累加任务总时间
                zon+=indexJson.getLong("wntDurTotal");
            }
            // 添加信息
            timeTask.put("tasks",tasksNew);
            timeTask.put("zon",zon);
            grpBTask.put(time,timeTask);
        }
        objTask.put(grpBNew,grpBTask);
        aArrange.put("objTask",objTask);
        asset.setAArrange(aArrange);
        assetMap.put(depNew,asset);
    }
    private void dgClearOldTaskAndClosing(JSONObject oDateObj,String layer,String id_PF
            ,Map<String,Asset> assetMap,String id_C,JSONArray clearTasks,JSONObject id_OPs
            ,JSONObject clearDep,JSONObject clearTasksReal){
        if ("0".equals(layer)) {
            return;
        }
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = 0; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            clearOldTaskNewAndClosingSon(oDate,assetMap,id_C,i,clearTasks,id_OPs,clearDep,clearTasksReal);
//            // 获取订单编号
//            String id_OThis = oDate.getString("id_O");
//            Integer indexThis = oDate.getInteger("index");
//            // 获取时间处理的组别
//            String grpBNew = oDate.getString("grpB");
//            String depNew = oDate.getString("dep");
//            // 根据组别获取部门
//            Asset asset;
//            if (assetMap.containsKey(depNew)) {
//                asset = assetMap.get(depNew);
//            } else {
//                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
//                System.out.println("查询数据-2："+depNew);
//                assetMap.put(depNew,asset);
//            }
//            if (null == asset) {
//                // 返回为空错误信息
//                System.out.println();
//                System.out.println("-查询为空!-"+depNew);
//                System.out.println();
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            }
//            JSONObject aArrange = getAArrangeNew(asset);
//            JSONObject objTask;
//            if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            } else {
//                // 获取数据库任务信息
//                objTask = aArrange.getJSONObject("objTask");
//            }
//            // 获取任务所在时间
//            JSONObject teDateNext = oDate.getJSONObject("teDate");
//            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
//                    +" - teDateNext:"+JSON.toJSONString(teDateNext));
//            if (null == teDateNext) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            }
//            // 根据部门信息获取指定组别的信息
//            JSONObject grpBTask = objTask.getJSONObject(grpBNew);
//            // 判断为空
//            if (null == grpBTask) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            }
//            // 遍历任务所在时间
//            for (String time : teDateNext.keySet()) {
//                // 获取所在时间任务信息
//                JSONObject timeTask = grpBTask.getJSONObject(time);
//                // 获取任务列表
//                JSONArray tasksNew = timeTask.getJSONArray("tasks");
//                // 获取任务余剩时间
//                Long zon = timeTask.getLong("zon");
//                // 创建存储删除信息
//                JSONArray removeIndex = new JSONArray();
//                int reIndex = -1;
//                // 遍历任务列表
//                for (int t = 1; t < tasksNew.size(); t++) {
//                    // 获取任务信息
//                    JSONObject taskInside = tasksNew.getJSONObject(t);
//                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
//                    if (taskInside.getString("id_O").equals(id_OThis)
//                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
//                        // 创建删除信息
//                        JSONObject removeInfo = new JSONObject();
//                        // 添加删除信息
//                        removeInfo.put("index",t);
//                        removeInfo.put("wntDurTotal",taskInside.getLong("wntDurTotal"));
//                        removeIndex.add(removeInfo);
//                        reIndex = t;
//                        addClearTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew,grpBNew);
//                        if ((tasksNew.size() - 1) <= (t + 1)) {
//                            JSONObject taskInsideNew = tasksNew.getJSONObject((t + 1));
//                            if (taskInsideNew.getInteger("priority") != -1 && taskInsideNew.getLong("teDelayDate") > 0) {
//                                taskInsideNew.put("updateTime",true);
//                            }
//                        }
//                    }
//                    else {
//                        if (reIndex != -1 && t > reIndex && taskInside.getInteger("priority") != -1) {
//                            // 添加删除信息
//                            removeIndex.add(qt.setJson("index",t,"wntDurTotal",taskInside.getLong("wntDurTotal")));
//                            addClearTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew,grpBNew);
//                        }
//                    }
//                }
//                // 遍历删除集合
//                for (int r = removeIndex.size()-1; r >= 0; r--) {
//                    // 获取删除信息
//                    JSONObject indexJson = removeIndex.getJSONObject(r);
//                    // 获取删除下标
//                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
//                    // 删除任务列表对应下标的任务
//                    tasksNew.remove(indexNewThis);
//                    // 累加任务总时间
//                    zon+=indexJson.getLong("wntDurTotal");
//                }
//                // 添加信息
//                timeTask.put("tasks",tasksNew);
//                timeTask.put("zon",zon);
//                grpBTask.put(time,timeTask);
//            }
//            objTask.put(grpBNew,grpBTask);
//            aArrange.put("objTask",objTask);
//            asset.setAArrange(aArrange);
//            assetMap.put(depNew,asset);
        }
        dgClearOldTaskAndClosing(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C,clearTasks,id_OPs,clearDep,clearTasksReal);
    }
    private void addClearTask(JSONObject task,JSONArray clearTasks,JSONObject id_OPs,JSONObject clearDep
            ,JSONObject clearTasksReal,String dep,String grpB){
        Integer layer = task.getInteger("layer");
        String id_PF = task.getString("id_PF");
        String id_OP = task.getString("id_OP");
        JSONObject clearTask = qt.setJson("id_OP", task.getString("id_OP"), "dateIndex"
                , task.getInteger("dateIndex"), "id_PF", task.getString("id_PF")
                , "id_C", task.getString("id_C"), "layer", task.getInteger("layer")
                , "priority", task.getInteger("priority"), "wrdN", qt.cloneObj(task.getJSONObject("wrdN"))
                , "index", task.getInteger("index"), "id_O", task.getString("id_O")
                , "wntDurTotal", task.getLong("wntDurTotal"));
        clearTask.put("dep",dep);
        clearTask.put("grpB",grpB);
        if (!id_OPs.containsKey(id_OP)) {
            id_OPs.put(id_OP,task.getInteger("dateIndex"));
            clearTasks.add(clearTask);
        }
        if (!clearDep.containsKey(dep)) {
            clearDep.put(dep,qt.setJson(grpB,0));
        } else {
            JSONObject depInfo = clearDep.getJSONObject(dep);
            if (!depInfo.containsKey(grpB)) {
                depInfo.put(grpB,0);
                clearDep.put(dep,depInfo);
            }
        }
        JSONObject orderData;
        if (!clearTasksReal.containsKey(id_OP)) {
            orderData = new JSONObject();
            JSONObject layerData = new JSONObject();
            layerData.put(id_PF,clearTask);
            orderData.put(layer+"",layerData);
            clearTasksReal.put(id_OP,orderData);
        }
        else {
            orderData = clearTasksReal.getJSONObject(id_OP);
            JSONObject layerData;
            if (!orderData.containsKey(layer+"")) {
                layerData = new JSONObject();
                layerData.put(id_PF,clearTask);
                orderData.put(layer+"",layerData);
                clearTasksReal.put(id_OP,orderData);
            } else {
                layerData = orderData.getJSONObject(layer+"");
                if (!layerData.containsKey(id_PF)) {
                    layerData.put(id_PF,clearTask);
                    orderData.put(layer+"",layerData);
                    clearTasksReal.put(id_OP,orderData);
                }
            }
        }
    }

    protected void clearOrderAllTaskAndSaveFc(String id_C,String id_O){
        System.out.println("进入Api清理Order全任务方法:"+" - "+id_O);
        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject oDateObj = order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        if (null == oDateObj) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        List<Integer> layers = new ArrayList<>();
        for (String layerStr : oDateObj.keySet()) {
            int layerInt = Integer.parseInt(layerStr);
            layers.add(layerInt);
        }
        layers.sort(Comparator.reverseOrder());
        JSONObject layerInfo = oDateObj.getJSONObject(layers.get(0).toString());
        JSONArray clearTasks = new JSONArray();
        for (String id_PF : layerInfo.keySet()) {
            clearTasks.add(qt.setJson("id_OP",id_O,"dateIndex",0,"id_C",id_C
                    ,"layer",layers.get(0).toString(),"id_PF",id_PF,"priority",1));
        }
        System.out.println("输出结果-clearOrderAllTaskAndSaveFc:");
        System.out.println(JSON.toJSONString(clearTasks));
        if (clearTasks.size() > 0) {
            JSONObject id_AsAll = new JSONObject();
            for (int i = 0; i < clearTasks.size(); i++) {
                JSONObject clearData = clearTasks.getJSONObject(i);
                JSONObject id_As = clearOldTaskNew(clearData.getString("id_OP"), clearData.getInteger("dateIndex")
                        , clearData.getString("id_C"), clearData.getString("layer"), clearData.getString("id_PF"));
                id_AsAll.putAll(id_As);
            }
            id_AsAll.forEach((key,val)->
                    qt.setMDContent(val.toString(),qt.setJson("aArrange.objTaskClear",clearTasks
                    ,"aArrange.objTaskClearId_OP",qt.setJson(id_O,0)), Asset.class));
            System.out.println("清理后输出-clearOrderAllTaskAndSaveFc:");
            System.out.println(JSON.toJSONString(clearTasks));
        }
    }

    protected void clearThisDayTaskAndSaveFc(String id_C,String dep,String grpB,long thisDay){
        System.out.println("clearThisDayTaskAndSaveFc-进入Api清理方法:"+" - "+dep+" - "+thisDay);
        Asset as = qt.getConfig(id_C, "d-" + dep, "aArrange");
        if (null == as || null == as.getAArrange() || null == as.getAArrange().getJSONObject("objTask")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 获取进度的oDates字段信息
        JSONObject grpBData = as.getAArrange().getJSONObject("objTask").getJSONObject(grpB);
        if (null == grpBData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject dayData = grpBData.getJSONObject(thisDay+"");
        if (null == dayData) {
            // 返回为空错误信息
            System.out.println("跳天:"+thisDay+"-为空return.");
            return;
        }
        JSONArray tasks = dayData.getJSONArray("tasks");
        JSONArray clearTasks = new JSONArray();
        JSONObject id_OPs = new JSONObject();
        JSONObject clearTasksReal = new JSONObject();
        JSONObject clearDep = new JSONObject();
        // 遍历时间处理信息集合
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            if (task.getInteger("priority") != -1) {
                addClearTask(task,clearTasks,id_OPs,clearDep,clearTasksReal,dep,grpB);
            }
        }
        dgJumpDayClear(clearTasks,id_OPs,grpBData,thisDay,clearDep,clearTasksReal,dep,grpB);
        System.out.println("输出结果-clearThisDayTaskAndSaveFc:");
        System.out.println(JSON.toJSONString(clearTasks));
        for (int i = 0; i < clearTasks.size(); i++) {
            JSONObject clearData = clearTasks.getJSONObject(i);
            clearOldTaskNew(clearData.getString("id_OP"),clearData.getInteger("dateIndex")
                    ,clearData.getString("id_C"),clearData.getString("layer"),clearData.getString("id_PF"));
        }
        System.out.println("清理后输出-clearThisDayTaskAndSaveFc-2:");
        System.out.println("clearTasks:");
        System.out.println(JSON.toJSONString(clearTasks));
        System.out.println("clearTasksReal:");
        System.out.println(JSON.toJSONString(clearTasksReal));
        System.out.println("id_OPs:");
        System.out.println(JSON.toJSONString(id_OPs));
        System.out.println("clearDep:");
        System.out.println(JSON.toJSONString(clearDep));
        JSONObject depClearTasks = new JSONObject();
        for (String id_OP : clearTasksReal.keySet()) {
            JSONObject opData = clearTasksReal.getJSONObject(id_OP);
            for (String layerIn : opData.keySet()) {
                JSONObject layerData = opData.getJSONObject(layerIn);
                for (String id_PFIn : layerData.keySet()) {
                    JSONObject taskIn = layerData.getJSONObject(id_PFIn);
                    String depIn = taskIn.getString("dep");
                    JSONArray tasksIn;
                    if (depClearTasks.containsKey(depIn)) {
                        tasksIn = depClearTasks.getJSONArray(depIn);
                    } else {
                        tasksIn = new JSONArray();
                    }
                    taskIn.remove("dep");
                    taskIn.remove("grpB");
                    tasksIn.add(qt.cloneObj(taskIn));
                    depClearTasks.put(depIn,tasksIn);
                }
            }
        }
        System.out.println("depClearTasks:");
        System.out.println(JSON.toJSONString(depClearTasks));
        JSONArray objTaskClear = as.getAArrange().getJSONArray("objTaskClear");
        if (depClearTasks.containsKey(dep)) {
            if (null == objTaskClear) {
                objTaskClear = new JSONArray();
            }
            objTaskClear.add(depClearTasks.getJSONObject(dep));
        }
        JSONObject objTaskClearId_OP = as.getAArrange().getJSONObject("objTaskClearId_OP");
        if (null == objTaskClearId_OP) {
            objTaskClearId_OP = new JSONObject();
        }
        for (String id_OP : id_OPs.keySet()) {
            if (!objTaskClearId_OP.containsKey(id_OP)) {
                objTaskClearId_OP.put(id_OP,id_OPs.getInteger(id_OP));
            }
        }
        qt.setMDContent(as.getId(),qt.setJson("aArrange.objTaskClear",objTaskClear
                ,"aArrange.objTaskClearId_OP",objTaskClearId_OP), Asset.class);
    }
    private void dgJumpDayClear(JSONArray clearTasks,JSONObject id_OPs,JSONObject grpBData
            ,long thisDay,JSONObject clearDep,JSONObject clearTasksReal,String dep, String grpB){
        thisDay+=86400;
        JSONObject dayData = grpBData.getJSONObject(thisDay+"");
        if (null == dayData) {
            return;
        }
        JSONArray tasks = dayData.getJSONArray("tasks");
        // 遍历时间处理信息集合
        for (int i = 0; i < tasks.size(); i++) {
            JSONObject task = tasks.getJSONObject(i);
            if (task.getInteger("priority") != -1) {
                addClearTask(task,clearTasks,id_OPs,clearDep,clearTasksReal,dep,grpB);
            }
        }
        dgJumpDayClear(clearTasks,id_OPs,grpBData,thisDay,clearDep,clearTasksReal,dep,grpB);
    }

    protected void clearThisDayEasyTaskAndSaveClearFollowUpMethod(String id_C,String dep,long thisDay){
        System.out.println("完整版Easy清理-进入Api清理方法:"+" - "+dep+" - "+thisDay);
        Asset as = qt.getConfig(id_C, "d-" + dep, "aArrange");
        System.out.println(JSON.toJSONString(as));
        if (null == as || null == as.getAArrange() || null == as.getAArrange().getJSONObject("objEasy")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 获取进度的oDates字段信息
        JSONObject objEasy = as.getAArrange().getJSONObject("objEasy");
        if (null == objEasy) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject dayData = objEasy.getJSONObject(thisDay+"");
        if (null == dayData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        Map<String,Asset> assetMap = new HashMap<>();
        assetMap.put(dep,as);
        Map<String,Order> orderMap = new HashMap<>();
        JSONObject setData = new JSONObject();
        JSONArray easyTasks = dayData.getJSONArray("easyTasks");
        JSONArray clearTasks = new JSONArray();
        JSONObject id_OPs = new JSONObject();
        JSONObject clearTasksReal = new JSONObject();
        JSONObject clearDep = new JSONObject();
        // 遍历时间处理信息集合
        for (int i = 0; i < easyTasks.size(); i++) {
            JSONObject task = easyTasks.getJSONObject(i);
            addClearEasyTask(task,clearTasks,id_OPs,clearDep,clearTasksReal,dep);
        }
        dgJumpDayEasyClearNew(clearTasks,id_OPs,objEasy,thisDay,clearTasksReal,clearDep,dep);
        JSONObject id_AsAll = new JSONObject();
        id_AsAll.put(dep,as.getId());
        for (int i = 0; i < clearTasks.size(); i++) {
            JSONObject clearData = clearTasks.getJSONObject(i);
            JSONObject id_As = clearOldTaskEasyClosing(clearData.getString("id_OP")
                    ,clearData.getInteger("dateIndex"),id_C,clearData.getString("layer")
                    ,clearData.getString("id_PF"),clearTasks,id_OPs,clearDep
                    ,clearTasksReal,assetMap,orderMap,setData);
            id_AsAll.putAll(id_As);
        }
        List<JSONObject> list = new ArrayList<>();
        for (String depIn : setData.keySet()) {
            list.add(setData.getJSONObject(depIn));
        }
        qt.setMDContentFast(list, Asset.class);
        System.out.println("清理后输出-clearThisDayEasyTaskAndSaveFcEnd:");
        System.out.println("clearTasks:");
        System.out.println(JSON.toJSONString(clearTasks));
        System.out.println("clearTasksReal:");
        System.out.println(JSON.toJSONString(clearTasksReal));
        System.out.println("id_OPs:");
        System.out.println(JSON.toJSONString(id_OPs));
        System.out.println("clearDep:");
        System.out.println(JSON.toJSONString(clearDep));
        System.out.println("id_AsAll:");
        System.out.println(JSON.toJSONString(id_AsAll));
        JSONObject depClearTasks = new JSONObject();
        for (String id_OP : clearTasksReal.keySet()) {
            JSONObject opData = clearTasksReal.getJSONObject(id_OP);
            for (String layerIn : opData.keySet()) {
                JSONObject layerData = opData.getJSONObject(layerIn);
                for (String id_PFIn : layerData.keySet()) {
                    JSONObject taskIn = layerData.getJSONObject(id_PFIn);
                    String depIn = taskIn.getString("dep");
                    JSONArray tasks;
                    if (depClearTasks.containsKey(depIn)) {
                        tasks = depClearTasks.getJSONArray(depIn);
                    } else {
                        tasks = new JSONArray();
                    }
                    taskIn.remove("dep");
                    tasks.add(qt.cloneObj(taskIn));
                    depClearTasks.put(depIn,tasks);
                }
            }
        }
        System.out.println("depClearTasks:");
        System.out.println(JSON.toJSONString(depClearTasks));
        for (String depInside : clearDep.keySet()) {
            String id_A = id_AsAll.getString(depInside);
            Asset asset = qt.getMDContent(id_A, "aArrange", Asset.class);
            if (null == asset || null == asset.getAArrange()) {
                System.out.println("写入Asset为空，id是:"+id_A);
                continue;
            }
            JSONArray objEasyTaskClear = asset.getAArrange().getJSONArray("objEasyTaskClear");
            if (depClearTasks.containsKey(depInside)) {
                if (null == objEasyTaskClear) {
                    objEasyTaskClear = new JSONArray();
                }
                objEasyTaskClear.addAll(depClearTasks.getJSONArray(depInside));
            }
            JSONObject objEasyTaskClearId_OP = asset.getAArrange().getJSONObject("objEasyTaskClearId_OP");
            if (null == objEasyTaskClearId_OP) {
                objEasyTaskClearId_OP = new JSONObject();
            }
            for (String id_OP : id_OPs.keySet()) {
                if (!objEasyTaskClearId_OP.containsKey(id_OP)) {
                    objEasyTaskClearId_OP.put(id_OP,id_OPs.getInteger(id_OP));
                }
            }
            qt.setMDContent(id_A,qt.setJson("aArrange.objEasyTaskClear",objEasyTaskClear
                    ,"aArrange.objEasyTaskClearId_OP",objEasyTaskClearId_OP), Asset.class);
            if (!depInside.equals(dep)) {
                clearThisDayEasyTaskAndSaveFcNew(id_C,depInside,(thisDay+86400L));
            }
        }
    }
    protected JSONObject clearOldTaskEasyClosing(String id_O,int dateIndex,String id_C,String layer,String id_PF
            ,JSONArray clearTasks,JSONObject id_OPs,JSONObject clearDep,JSONObject clearTasksReal
            ,Map<String,Asset> assetMap,Map<String,Order> orderMap,JSONObject setData){
        System.out.println("完整版Easy清理-子-进入Api清理方法:"+" - "+id_O+" - "+dateIndex);
        Order order;
        if (orderMap.containsKey(id_O)) {
            order = orderMap.get(id_O);
        } else {
            order = qt.getMDContent(id_O, "casItemx", Order.class);
            orderMap.put(id_O,order);
        }
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            orderMap.remove(id_O);
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
//        Map<String,Asset> assetMap = new HashMap<>();
        // 获取进度的oDates字段信息
        JSONObject oDateObj = order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = dateIndex; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            clearOldTaskEasyClosingSon(oDate,assetMap,id_C,i,clearTasks,id_OPs,clearDep,clearTasksReal);
        }
        System.out.println("进入前:");
        System.out.println(JSON.toJSONString(assetMap));
        dgClearOldTaskEasyClosing(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C,clearTasks,id_OPs,clearDep,clearTasksReal);
        System.out.println("清理后输出:");
        System.out.println(JSON.toJSONString(assetMap));
//        List<JSONObject> list = new ArrayList<>();
        JSONObject id_As = new JSONObject();
        assetMap.forEach((key,val)->{
//            list.add(qt.setJson("id",val.getId(),"updateData"
//                    ,qt.setJson("aArrange.objEasy",val.getAArrange().getJSONObject("objEasy"))));
            setData.put(key,qt.setJson("id",val.getId(),"updateData"
                    ,qt.setJson("aArrange.objEasy",val.getAArrange().getJSONObject("objEasy"))));
            id_As.put(key,val.getId());
        });
//        qt.setMDContentFast(list, Asset.class);
        return id_As;
    }
    protected void clearOldTaskEasyClosingSon(JSONObject oDate,Map<String,Asset> assetMap,String id_C,int i
            ,JSONArray clearTasks,JSONObject id_OPs,JSONObject clearDep,JSONObject clearTasksReal){
        // 获取订单编号
        String id_OThis = oDate.getString("id_O");
        Integer indexThis = oDate.getInteger("index");
        // 获取时间处理的组别
        String grpBNew = oDate.getString("grpB");
        String depNew = oDate.getString("dep");
        // 根据组别获取部门
        Asset asset;
        if (assetMap.containsKey(depNew)) {
            asset = assetMap.get(depNew);
        } else {
            asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
            System.out.println("查询数据-1："+depNew);
            assetMap.put(depNew,asset);
        }
        if (null == asset) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-"+depNew);
            System.out.println();
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject aArrange = getAArrangeNew(asset);
        JSONObject objEasy;
        if (null == aArrange || null == aArrange.getJSONObject("objEasy")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        } else {
            // 获取数据库任务信息
            objEasy = aArrange.getJSONObject("objEasy");
        }
        // 获取任务所在时间
        JSONObject teEasyDate = oDate.getJSONObject("teEasyDate");
        System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
                +" - teEasyDate:"+JSON.toJSONString(teEasyDate));
        if (null == teEasyDate) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 遍历任务所在时间
        for (String time : teEasyDate.keySet()) {
            // 获取所在时间任务信息
            JSONObject timeTask = objEasy.getJSONObject(time);
            // 获取任务列表
            JSONArray easyTasks = timeTask.getJSONArray("easyTasks");
            // 获取任务余剩时间
            Long wntLeft = timeTask.getLong("wntLeft");
            int reIndex = -1;
            // 创建存储删除信息
            JSONArray removeIndex = new JSONArray();
            // 遍历任务列表
            for (int t = 0; t < easyTasks.size(); t++) {
                // 获取任务信息
                JSONObject taskInside = easyTasks.getJSONObject(t);
                // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
                if (taskInside.getString("id_O").equals(id_OThis)
                        && Objects.equals(taskInside.getInteger("index"), indexThis)) {
                    // 创建删除信息
                    JSONObject removeInfo = new JSONObject();
                    // 添加删除信息
                    removeInfo.put("index",t);
                    removeInfo.put("timeTotal",taskInside.getLong("timeTotal"));
                    reIndex = t;
                    removeIndex.add(removeInfo);
                    addClearEasyTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew);
                    if (easyTasks.size() > 1 && (t + 1) <= (easyTasks.size() - 1)) {
                        JSONObject taskInsideNew = easyTasks.getJSONObject((t + 1));
                        if (taskInsideNew.getLong("timeTotal") > 0) {
                            taskInsideNew.put("updateTime",true);
                        }
                    }
                }
                else {
                    if (reIndex != -1 && t > reIndex) {
                        // 添加删除信息
                        removeIndex.add(qt.setJson("index",t,"timeTotal",taskInside.getLong("timeTotal")));
                        addClearEasyTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew);
                    }
                }
            }
            // 遍历删除集合
            for (int r = removeIndex.size()-1; r >= 0; r--) {
                // 获取删除信息
                JSONObject indexJson = removeIndex.getJSONObject(r);
                // 获取删除下标
                int indexNewThis = Integer.parseInt(indexJson.getString("index"));
                // 删除任务列表对应下标的任务
                easyTasks.remove(indexNewThis);
                // 累加任务总时间
                wntLeft+=indexJson.getLong("timeTotal");
            }
            // 添加信息
            timeTask.put("easyTasks",easyTasks);
            timeTask.put("wntLeft",wntLeft);
            objEasy.put(time,timeTask);
        }
        aArrange.put("objEasy",objEasy);
        asset.setAArrange(aArrange);
        assetMap.put(depNew,asset);
    }
    private void dgClearOldTaskEasyClosing(JSONObject oDateObj,String layer,String id_PF
            ,Map<String,Asset> assetMap,String id_C,JSONArray clearTasks
            ,JSONObject id_OPs,JSONObject clearDep,JSONObject clearTasksReal){
        if ("0".equals(layer)) {
            return;
        }
        JSONObject layerData = oDateObj.getJSONObject(layer);
        JSONObject pfData = layerData.getJSONObject(id_PF);
        JSONArray oDates = pfData.getJSONArray("oDates");
        // 遍历时间处理信息集合
        for (int i = 0; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            clearOldTaskEasyClosingSon(oDate,assetMap,id_C,i,clearTasks,id_OPs,clearDep,clearTasksReal);
//            // 获取订单编号
//            String id_OThis = oDate.getString("id_O");
//            Integer indexThis = oDate.getInteger("index");
//            // 获取时间处理的组别
//            String grpBNew = oDate.getString("grpB");
//            String depNew = oDate.getString("dep");
//            // 根据组别获取部门
//            Asset asset;
//            if (assetMap.containsKey(depNew)) {
//                asset = assetMap.get(depNew);
//            } else {
//                asset = qt.getConfig(id_C,"d-"+depNew,timeCard);
//                System.out.println("查询数据-2："+depNew);
//                assetMap.put(depNew,asset);
//            }
//            if (null == asset) {
//                // 返回为空错误信息
//                System.out.println();
//                System.out.println("-查询为空!-"+depNew);
//                System.out.println();
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            }
//            JSONObject aArrange = getAArrangeNew(asset);
//            JSONObject objEasy;
//            if (null == aArrange || null == aArrange.getJSONObject("objEasy")) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            } else {
//                // 获取数据库任务信息
//                objEasy = aArrange.getJSONObject("objEasy");
//            }
//            // 获取任务所在时间
//            JSONObject teEasyDate = oDate.getJSONObject("teEasyDate");
//            System.out.println(id_OThis+" - "+indexThis+" - "+i+" - "+grpBNew+" - "+depNew
//                    +" - teDateNext:"+JSON.toJSONString(teEasyDate));
//            if (null == teEasyDate) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
//            }
//            // 遍历任务所在时间
//            for (String time : teEasyDate.keySet()) {
//                // 获取所在时间任务信息
//                JSONObject timeTask = objEasy.getJSONObject(time);
//                // 获取任务列表
//                JSONArray easyTasks = timeTask.getJSONArray("easyTasks");
//                // 获取任务余剩时间
//                Long wntLeft = timeTask.getLong("wntLeft");
//                int reIndex = -1;
//                // 创建存储删除信息
//                JSONArray removeIndex = new JSONArray();
//                // 遍历任务列表
//                for (int t = 0; t < easyTasks.size(); t++) {
//                    // 获取任务信息
//                    JSONObject taskInside = easyTasks.getJSONObject(t);
//                    // 判断循环任务订单编号等于当前处理任务编号，并且循环任务下标等于当前处理任务下标
//                    if (taskInside.getString("id_O").equals(id_OThis)
//                            && Objects.equals(taskInside.getInteger("index"), indexThis)) {
//                        // 创建删除信息
//                        JSONObject removeInfo = new JSONObject();
//                        // 添加删除信息
//                        removeInfo.put("index",t);
//                        removeInfo.put("timeTotal",taskInside.getLong("timeTotal"));
//                        reIndex = t;
//                        removeIndex.add(removeInfo);
//                        addClearEasyTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew);
//                        if (easyTasks.size() > 1 && (t + 1) <= (easyTasks.size() - 1)) {
//                            JSONObject taskInsideNew = easyTasks.getJSONObject((t + 1));
//                            if (taskInsideNew.getLong("timeTotal") > 0) {
//                                taskInsideNew.put("updateTime",true);
//                            }
//                        }
//                    }
//                    else {
//                        if (reIndex != -1 && t > reIndex) {
//                            // 添加删除信息
//                            removeIndex.add(qt.setJson("index",t,"timeTotal",taskInside.getLong("timeTotal")));
//                            addClearEasyTask(taskInside,clearTasks,id_OPs,clearDep,clearTasksReal,depNew);
//                        }
//                    }
//                }
//                // 遍历删除集合
//                for (int r = removeIndex.size()-1; r >= 0; r--) {
//                    // 获取删除信息
//                    JSONObject indexJson = removeIndex.getJSONObject(r);
//                    // 获取删除下标
//                    int indexNewThis = Integer.parseInt(indexJson.getString("index"));
//                    // 删除任务列表对应下标的任务
//                    easyTasks.remove(indexNewThis);
//                    // 累加任务总时间
//                    wntLeft+=indexJson.getLong("timeTotal");
//                }
//                // 添加信息
//                timeTask.put("easyTasks",easyTasks);
//                timeTask.put("wntLeft",wntLeft);
//                objEasy.put(time,timeTask);
//            }
//            aArrange.put("objEasy",objEasy);
//            asset.setAArrange(aArrange);
//            assetMap.put(depNew,asset);
        }
        dgClearOldTaskEasyClosing(oDateObj,pfData.getString("layer"),pfData.getString("id_PF")
                ,assetMap,id_C,clearTasks,id_OPs,clearDep,clearTasksReal);
    }
    private void addClearEasyTask(JSONObject task,JSONArray clearTasks,JSONObject id_OPs,JSONObject clearDep
            ,JSONObject clearTasksReal,String dep){
        Integer layer = task.getInteger("layer");
        String id_PF = task.getString("id_PF");
        String id_OP = task.getString("id_OP");
        task.put("dep",dep);
        if (!id_OPs.containsKey(id_OP)) {
            id_OPs.put(id_OP,task.getInteger("dateIndex"));
            clearTasks.add(task);
        }
        if (!clearDep.containsKey(dep)) {
            clearDep.put(dep,0);
        }

        JSONObject orderData;
        if (!clearTasksReal.containsKey(id_OP)) {
            orderData = new JSONObject();
            JSONObject layerData = new JSONObject();
            layerData.put(id_PF,task);
            orderData.put(layer+"",layerData);
            clearTasksReal.put(id_OP,orderData);
        }
        else {
            orderData = clearTasksReal.getJSONObject(id_OP);
            JSONObject layerData;
            if (!orderData.containsKey(layer+"")) {
                layerData = new JSONObject();
                layerData.put(id_PF,task);
                orderData.put(layer+"",layerData);
                clearTasksReal.put(id_OP,orderData);
            } else {
                layerData = orderData.getJSONObject(layer+"");
                if (!layerData.containsKey(id_PF)) {
                    layerData.put(id_PF,task);
                    orderData.put(layer+"",layerData);
                    clearTasksReal.put(id_OP,orderData);
                }
            }
        }
    }

    protected void clearThisDayEasyTaskAndSaveFcNew(String id_C,String dep,long thisDay){
        System.out.println("clearThisDayEasyTaskAndSaveFcNew-进入Api清理方法:"+" - "+dep+" - "+thisDay);
        Asset as = qt.getConfig(id_C, "d-" + dep, "aArrange");
        System.out.println(JSON.toJSONString(as));
        if (null == as || null == as.getAArrange() || null == as.getAArrange().getJSONObject("objEasy")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        // 获取进度的oDates字段信息
        JSONObject objEasy = as.getAArrange().getJSONObject("objEasy");
        if (null == objEasy) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ASSET_NOT_FOUND.getCode(), "asset不存在");
        }
        JSONObject dayData = objEasy.getJSONObject(thisDay+"");
        if (null == dayData) {
            // 返回为空错误信息
            System.out.println("跳天-Easy:"+thisDay+"-为空return.");
            return;
        }
        JSONArray easyTasks = dayData.getJSONArray("easyTasks");
        JSONArray clearTasks = new JSONArray();
        JSONObject id_OPs = new JSONObject();
        JSONObject clearTasksReal = new JSONObject();
        JSONObject clearDep = new JSONObject();
        // 遍历时间处理信息集合
        for (int i = 0; i < easyTasks.size(); i++) {
            JSONObject task = easyTasks.getJSONObject(i);
            if (!id_OPs.containsKey(task.getString("id_OP"))) {
                addClearEasyTask(task,clearTasks,id_OPs,clearDep,clearTasksReal,dep);
            }
        }
        dgJumpDayEasyClearNew(clearTasks,id_OPs,objEasy,thisDay,clearTasksReal,clearDep,dep);
        for (int i = 0; i < clearTasks.size(); i++) {
            JSONObject clearData = clearTasks.getJSONObject(i);
            clearOldTaskEasy(clearData.getString("id_O"),clearData.getInteger("dateIndex")
                    ,clearData.getString("id_C"),clearData.getString("layer")
                    ,clearData.getString("id_PF"));
        }
        System.out.println("清理后输出-clearThisDayEasyTaskAndSaveFcNew:");
        System.out.println("clearTasks:");
        System.out.println(JSON.toJSONString(clearTasks));
        System.out.println("clearTasksReal:");
        System.out.println(JSON.toJSONString(clearTasksReal));
        System.out.println("id_OPs:");
        System.out.println(JSON.toJSONString(id_OPs));
        System.out.println("clearDep:");
        System.out.println(JSON.toJSONString(clearDep));
        JSONObject depClearTasks = new JSONObject();
        for (String id_OP : clearTasksReal.keySet()) {
            JSONObject opData = clearTasksReal.getJSONObject(id_OP);
            for (String layerIn : opData.keySet()) {
                JSONObject layerData = opData.getJSONObject(layerIn);
                for (String id_PFIn : layerData.keySet()) {
                    JSONObject taskIn = layerData.getJSONObject(id_PFIn);
                    String depIn = taskIn.getString("dep");
                    JSONArray tasks;
                    if (depClearTasks.containsKey(depIn)) {
                        tasks = depClearTasks.getJSONArray(depIn);
                    } else {
                        tasks = new JSONArray();
                    }
                    taskIn.remove("dep");
                    tasks.add(qt.cloneObj(taskIn));
                    depClearTasks.put(depIn,tasks);
                }
            }
        }
        System.out.println("depClearTasks:");
        System.out.println(JSON.toJSONString(depClearTasks));
        JSONArray objEasyTaskClear = as.getAArrange().getJSONArray("objEasyTaskClear");
        if (depClearTasks.containsKey(dep)) {
            if (null == objEasyTaskClear) {
                objEasyTaskClear = new JSONArray();
            }
            objEasyTaskClear.add(depClearTasks.getJSONObject(dep));
        }
        JSONObject objEasyTaskClearId_OP = as.getAArrange().getJSONObject("objEasyTaskClearId_OP");
        if (null == objEasyTaskClearId_OP) {
            objEasyTaskClearId_OP = new JSONObject();
        }
        for (String id_OP : id_OPs.keySet()) {
            if (!objEasyTaskClearId_OP.containsKey(id_OP)) {
                objEasyTaskClearId_OP.put(id_OP,id_OPs.getInteger(id_OP));
            }
        }
        qt.setMDContent(as.getId(),qt.setJson(
                "aArrange.objEasyTaskClear",objEasyTaskClear
                ,"aArrange.objEasyTaskClearId_OP",objEasyTaskClearId_OP), Asset.class);
    }
    private void dgJumpDayEasyClearNew(JSONArray clearTasks,JSONObject id_OPs,JSONObject objEasy,long thisDay
            ,JSONObject clearTasksReal,JSONObject clearDep,String dep){
        thisDay+=86400;
        JSONObject dayData = objEasy.getJSONObject(thisDay+"");
        if (null == dayData) {
            return;
        }
        JSONArray easyTasks = dayData.getJSONArray("easyTasks");
        // 遍历时间处理信息集合
        for (int i = 0; i < easyTasks.size(); i++) {
            JSONObject task = easyTasks.getJSONObject(i);
            addClearEasyTask(task,clearTasks,id_OPs,clearDep,clearTasksReal,dep);
        }
        dgJumpDayEasyClearNew(clearTasks,id_OPs,objEasy,thisDay,clearTasksReal,clearDep,dep);
    }

    /**
     * 更新镜像的所在时间方法
     * @param id_O	订单编号
     * @param dateIndex	时间处理的唯一下标
     * @param time	时间
     * @param wntDurTotal	当前用时
     * @param allImageTeDate	镜像任务所在日期
     * @param isGetTaskPattern   isGetTaskPattern – = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected void setAllImageTeDateAndDate(String id_O,int dateIndex,long time,long wntDurTotal,JSONObject allImageTeDate
            , Integer isGetTaskPattern,long endTime){
        // 判断是镜像处理
        if (isGetTaskPattern == 1) {
//            System.out.println("time-allImage:"+time+" - endTime:"+endTime+" - :"+(time == endTime));
            if (time != endTime) {
                time = endTime;
            }
            // 获取订单编号对应的信息
            JSONObject id_OInfo = allImageTeDate.getJSONObject(id_O);
            // 判断不为空
            if (null != id_OInfo) {
                // 获取下标对应信息
                JSONObject dateIndexInfo = id_OInfo.getJSONObject(dateIndex + "");
                // 判断不为空
                if (null != dateIndexInfo) {
                    // 获取时间信息
                    JSONObject date = dateIndexInfo.getJSONObject("date");
                    // 判断当前时间存在
                    if (date.containsKey(time + "")) {
                        // 更新用时时间
                        date.put(time+"",(date.getLong(time+"")+wntDurTotal));
                    } else {
                        // 添加用时时间
                        date.put(time+"",wntDurTotal);
                    }
                    // 写入所在时间
                    dateIndexInfo.put("date",date);
                    id_OInfo.put(dateIndex+"",dateIndexInfo);
                    allImageTeDate.put(id_O,id_OInfo);
                }
            }
        }
    }

    /**
     * 获取清理状态方法
     * @param id_O	订单编号
     * @param dateIndex	数据下标
     * @param clearStatus	清理状态信息
     * @return 返回结果: {@link int}
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected int getClearStatus(String id_O,int dateIndex,JSONObject clearStatus){
        // 获取订单编号清理信息
        JSONObject clearId_O = clearStatus.getJSONObject(id_O);
        // 定义存储数据下标清理信息
        JSONObject clearIndex;
        // 判断订单编号清理信息为空
        if (null == clearId_O) {
            // 创建信息
            clearId_O = new JSONObject();
            clearIndex = new JSONObject();
            // 赋值并且返回
            clearIndex.put("status",0);
            clearId_O.put(dateIndex+"",clearIndex);
            clearStatus.put(id_O,clearId_O);
            return 0;
        } else {
            // 不为空，获取数据下标清理信息
            clearIndex = clearId_O.getJSONObject(dateIndex + "");
            // 判断数据下标清理信息为空
            if (null == clearIndex) {
                // 创建信息
                clearIndex = new JSONObject();
                // 赋值并且返回
                clearIndex.put("status",0);
                clearId_O.put(dateIndex+"",clearIndex);
                clearStatus.put(id_O,clearId_O);
                return 0;
            }
            return clearIndex.getInteger("status");
        }
    }

    /**
     * 合并任务，以同部门同组别同序号进行合并
     * @param oDateObj	时间处理任务数据
     * @return 返回结果: {@link JSONObject}
     * @author tang
     * @date 创建时间: 2023/2/20
     * @ver 版本号: 1.0.0
     */
    protected JSONObject mergeTaskByPriorODateObj(JSONObject oDateObj){
        // 创建存储部门序号信息字典
        JSONObject depPrior = new JSONObject();
        // 创建存储删除下标
        JSONArray delDateIndex = new JSONArray();
        System.out.println("开始输出:");
        System.out.println(JSON.toJSONString(oDateObj));
        // 创建存储更新任务信息
        JSONObject updateTask = new JSONObject();
        Map<String,Order> orderMap = new HashMap<>();
        for (String layer : oDateObj.keySet()) {
            JSONObject prodObj = oDateObj.getJSONObject(layer);
            for (String prodId : prodObj.keySet()) {
                JSONObject pratInfo = prodObj.getJSONObject(prodId);
                JSONArray oDates = pratInfo.getJSONArray("oDates");
                // 遍历获取递归存储的时间处理信息
                for (int i = 0; i < oDates.size(); i++) {
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 根据组别获取部门
                    int priorItem = oDate.getInteger("priorItem");
                    oDate.put("grpUNum",oDate.getInteger("grpUNum")==null?1:oDate.getInteger("grpUNum"));
                    // 获取类别
                    Integer bmdpt = oDate.getInteger("bmdpt");
                    if (bmdpt == 3 || (oDate.getBoolean("isSto") != null && oDate.getBoolean("isSto"))) {
                        // 添加当前下标
                        delDateIndex.add(i);
                        System.out.println("开始输出:物料返回：");
                        continue;
                    } else if (oDate.getBoolean("empty")) {
                        // 添加当前下标
                        delDateIndex.add(i);
                        System.out.println("开始输出:空任务返回：");
                        continue;
                    }

                    int bcdStatus;
                    Order orderData;
                    String id_O = oDate.getString("id_O");
                    // 判断不为空
                    if (!orderMap.containsKey(id_O)) {
                        // 调用方法获取订单信息
                        orderData = qt.getMDContent(id_O,"action", Order.class);
                        orderMap.put(id_O,orderData);
                    } else {
                        orderData = orderMap.get(id_O);
                    }
                    if (null == orderData || null == orderData.getAction()
                            || null == orderData.getAction().getJSONArray("objAction")
                            || null == orderData.getAction().getJSONArray("objAction")
                            .getJSONObject(oDate.getInteger("index"))) {
                        System.out.println("开始输出:为空返回："+id_O);
                    } else {
                        // 获取进度信息
                        bcdStatus = orderData.getAction().getJSONArray("objAction")
                                .getJSONObject(oDate.getInteger("index")).getInteger("bcdStatus");
                        if (bcdStatus == 2 || bcdStatus == 8) {
                            // 添加当前下标
                            delDateIndex.add(i);
                            System.out.println("开始输出:完成后返回：");
                            continue;
                        }
                    }

                    Long wntDur = oDate.getLong("wntDur");
                    Double wn2qtyneed = oDate.getDouble("wn2qtyneed");
                    // 存储任务总时间
                    long taskTotalTime = (long)(wntDur * wn2qtyneed);
                    long wntDurTotal;
                    // 计算总时间
                    if (taskTotalTime % oDate.getInteger("grpUNum") == 0) {
                        wntDurTotal = taskTotalTime / oDate.getInteger("grpUNum");
                    } else {
                        wntDurTotal = (long) Math.ceil((double) (taskTotalTime / oDate.getInteger("grpUNum")));
                    }
                    oDate.put("wntDurTotal",wntDurTotal);
                    if (bmdpt == 2) {
                        continue;
                    }
                    // 获取部门的信息
                    JSONObject grpBPrior = depPrior.getJSONObject(dep);
                    // 定义存储任务数据和任务信息
                    JSONObject priorMap;
                    // 判断为空
                    if (null == grpBPrior) {
                        // 添加新数据
                        grpBPrior = new JSONObject();
                        priorMap = new JSONObject();
                        JSONObject priorInfo = new JSONObject();
                        priorInfo.put("oDate",oDate);
                        priorInfo.put("index",i);
                        priorMap.put(priorItem + "",priorInfo);
                    } else {
                        // 获取组别的信息
                        priorMap = grpBPrior.getJSONObject(grpB);
                        // 判断为空
                        if (null == priorMap) {
                            // 创建新的
                            priorMap = new JSONObject();
                            JSONObject priorInfo = new JSONObject();
                            priorInfo.put("oDate",oDate);
                            priorInfo.put("index",i);
                            priorMap.put(priorItem + "",priorInfo);
                        } else {
                            // 获取序号信息
                            JSONObject priorInfo = priorMap.getJSONObject(priorItem + "");
                            // 判断为空
                            if (null == priorInfo) {
                                priorInfo = new JSONObject();
                                priorInfo.put("oDate",oDate);
                                priorInfo.put("index",i);
                            } else {
                                JSONObject datePrior = priorInfo.getJSONObject("oDate");
                                // 获取合并任务数据列表
                                JSONArray mergeDates = datePrior.getJSONArray("mergeDates");
                                if (null == mergeDates || mergeDates.size() == 0) {
                                    // 创建列表
                                    mergeDates = new JSONArray();
                                    // 添加之前的任务数据信息
                                    mergeDates.add(qt.setJson("id_O",datePrior.getString("id_O")
                                            ,"index",datePrior.getInteger("index")
                                            ,"wntDurTotal",datePrior.getLong("wntDurTotal")
                                            ,"wntPrep",datePrior.getLong("wntPrep")));
                                }
                                // 添加当前下标
                                delDateIndex.add(i);
                                // 添加当前任务数据信息
                                mergeDates.add(qt.setJson("id_O",oDate.getString("id_O")
                                        ,"index",oDate.getInteger("index")
                                        ,"wntDurTotal",oDate.getLong("wntDurTotal")
                                        ,"wntPrep",oDate.getLong("wntPrep")));
                                datePrior.put("mergeDates",mergeDates);
                                // 更新准备时间
                                datePrior.put("wntPrep",datePrior.getLong("wntPrep")+oDate.getLong("wntPrep"));
                                // 更新任务总时间
                                datePrior.put("wntDurTotal",datePrior.getLong("wntDurTotal")+oDate.getLong("wntDurTotal"));
                                priorInfo.put("oDate",datePrior);
                                // 获取之前存储下标
                                int index = priorInfo.getInteger("index");
                                // 添加下标到更新
                                updateTask.put(index+"",JSONObject.parseObject(JSON.toJSONString(datePrior)));
                            }
                            priorMap.put(priorItem + "",priorInfo);
                        }
                    }
                    // 更新信息
                    grpBPrior.put(grpB,priorMap);
                    depPrior.put(dep,grpBPrior);
                    oDates.set(i,oDate);
                }
                // 遍历更新任务
                for (String s : updateTask.keySet()) {
                    oDates.set(Integer.parseInt(s),updateTask.getJSONObject(s));
                }
                // 遍历删除任务
                for (int i = delDateIndex.size()-1; i >= 0; i--) {
                    int index = delDateIndex.getInteger(i);
                    oDates.remove(index);
                }
                pratInfo.put("oDates",oDates);
                prodObj.put(prodId,pratInfo);
                oDateObj.put(layer,prodObj);
            }
        }
        return oDateObj;
    }
    protected void mergeDelSumMaxTimeByODateObj(String id_OP,JSONObject oDateObj){
        // 创建存储部门序号信息字典
        JSONObject depPrior = new JSONObject();
        System.out.println("开始oDateObj-3:");
        System.out.println(JSON.toJSONString(oDateObj));
        Map<String,Order> orderMap = new HashMap<>();
        for (String layer : oDateObj.keySet()) {
            JSONObject prodObj = oDateObj.getJSONObject(layer);
            System.out.println("layer:"+layer);
            for (String prodId : prodObj.keySet()) {
                // 创建存储更新任务信息
                JSONObject updateTask = new JSONObject();
                JSONObject pratInfo = prodObj.getJSONObject(prodId);
                System.out.println("id_PF:"+prodId);
                JSONArray oDates = pratInfo.getJSONArray("oDates");
                // 创建存储删除下标
                JSONArray delDateIndex = new JSONArray();
                System.out.println("oDates-开始:");
                System.out.println(JSON.toJSONString(oDates));
                // 遍历获取递归存储的时间处理信息
                for (int i = 0; i < oDates.size(); i++) {
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 根据组别获取部门
                    int priorItem = oDate.getInteger("priorItem");
                    oDate.put("grpUNum",oDate.getInteger("grpUNum")==null?1:oDate.getInteger("grpUNum"));
                    // 获取类别
                    Integer bmdpt = oDate.getInteger("bmdpt");
                    if (bmdpt == 3) {
                        // 添加当前下标
                        delDateIndex.add(qt.setJson("index",i,"type",0));
                        System.out.println("输出:物料返回");
                        continue;
                    }
                    if ((oDate.getBoolean("isSto") != null && oDate.getBoolean("isSto"))) {
                        delDateIndex.add(qt.setJson("index",i,"type",1));
                        System.out.println("输出:使用库存返回");
                        continue;
                    }

                    int bcdStatus;
                    Order orderData;
                    String id_O = oDate.getString("id_O");
                    // 判断不为空
                    if (!orderMap.containsKey(id_O)) {
                        // 调用方法获取订单信息
                        orderData = qt.getMDContent(id_O,"action", Order.class);
                        orderMap.put(id_O,orderData);
                    } else {
                        orderData = orderMap.get(id_O);
                    }
                    if (null != orderData && null != orderData.getAction()
                            && null != orderData.getAction().getJSONArray("objAction")
                            && null != orderData.getAction().getJSONArray("objAction")
                            .getJSONObject(oDate.getInteger("index"))) {
                        // 获取进度信息
                        bcdStatus = orderData.getAction().getJSONArray("objAction")
                                .getJSONObject(oDate.getInteger("index")).getInteger("bcdStatus");
                        if (bcdStatus == 2 || bcdStatus == 8) {
                            // 添加当前下标
                            delDateIndex.add(qt.setJson("index",i,"type",2));
                            System.out.println("输出:进度已完成返回");
                            continue;
                        }
                    }

                    if (bmdpt == 2) {
                        oDates.set(i,oDate);
                        continue;
                    }
                    Long wntDur = oDate.getLong("wntDur");
                    Double wn2qtyneed = oDate.getDouble("wn2qtyneed");
                    // 存储任务总时间
                    long taskTotalTime = (long)(wntDur * wn2qtyneed);
                    long wntDurTotal;
                    // 计算总时间
                    if (taskTotalTime % oDate.getInteger("grpUNum") == 0) {
                        wntDurTotal = taskTotalTime / oDate.getInteger("grpUNum");
                    } else {
                        wntDurTotal = (long) Math.ceil((double) (taskTotalTime / oDate.getInteger("grpUNum")));
                    }
                    oDate.put("wntDurTotal",wntDurTotal);

                    // 获取部门的信息
                    JSONObject grpBPrior = depPrior.getJSONObject(dep);
                    // 定义存储任务数据和任务信息
                    JSONObject priorMap;
                    // 判断为空
                    if (null == grpBPrior) {
                        // 添加新数据
                        grpBPrior = new JSONObject();
                        priorMap = new JSONObject();
                        JSONObject priorInfo = new JSONObject();
                        priorInfo.put("oDate",oDate);
                        priorInfo.put("index",i);
                        priorMap.put(priorItem + "",priorInfo);
                    }
                    else {
                        // 获取组别的信息
                        priorMap = grpBPrior.getJSONObject(grpB);
                        // 判断为空
                        if (null == priorMap) {
                            // 创建新的
                            priorMap = new JSONObject();
                            JSONObject priorInfo = new JSONObject();
                            priorInfo.put("oDate",oDate);
                            priorInfo.put("index",i);
                            priorMap.put(priorItem + "",priorInfo);
                        } else {
                            // 获取序号信息
                            JSONObject priorInfo = priorMap.getJSONObject(priorItem + "");
                            // 判断为空
                            if (null == priorInfo) {
                                priorInfo = new JSONObject();
                                priorInfo.put("oDate",oDate);
                                priorInfo.put("index",i);
                            } else {
                                System.out.println("进入合并信息:"+i);
                                System.out.println(JSON.toJSONString(oDate));
                                System.out.println(JSON.toJSONString(priorInfo));
                                JSONObject datePrior = priorInfo.getJSONObject("oDate");
                                // 获取合并任务数据列表
                                JSONArray mergeDates = datePrior.getJSONArray("mergeDates");
                                if (null == mergeDates || mergeDates.size() == 0) {
                                    // 创建列表
                                    mergeDates = new JSONArray();
                                    // 添加之前的任务数据信息
                                    mergeDates.add(qt.setJson("id_O",datePrior.getString("id_O")
                                            ,"index",datePrior.getInteger("index")
                                            ,"wntDurTotal",datePrior.getLong("wntDurTotal")
                                            ,"wntPrep",datePrior.getLong("wntPrep")));
                                }
                                // 添加当前下标
                                delDateIndex.add(qt.setJson("index",i,"type",3));
                                // 添加当前任务数据信息
                                mergeDates.add(qt.setJson("id_O",oDate.getString("id_O")
                                        ,"index",oDate.getInteger("index")
                                        ,"wntDurTotal",oDate.getLong("wntDurTotal")
                                        ,"wntPrep",oDate.getLong("wntPrep")));
                                datePrior.put("mergeDates",mergeDates);
                                // 更新准备时间
                                datePrior.put("wntPrep",datePrior.getLong("wntPrep")+oDate.getLong("wntPrep"));
                                // 更新任务总时间
                                datePrior.put("wntDurTotal",datePrior.getLong("wntDurTotal")+oDate.getLong("wntDurTotal"));
                                priorInfo.put("oDate",datePrior);
                                // 获取之前存储下标
                                int index = priorInfo.getInteger("index");
                                // 添加下标到更新
                                updateTask.put(index+"",datePrior);
                            }
                            priorMap.put(priorItem + "",priorInfo);
                        }
                    }
                    // 更新信息
                    grpBPrior.put(grpB,priorMap);
                    depPrior.put(dep,grpBPrior);
                    oDates.set(i,oDate);
                }
                System.out.println("oDates-结束:");
                System.out.println(JSON.toJSONString(oDates));
                System.out.println("updateTask:");
                System.out.println(JSON.toJSONString(updateTask));
                // 遍历更新任务
                for (String s : updateTask.keySet()) {
                    oDates.set(Integer.parseInt(s),updateTask.getJSONObject(s));
                }
                // 创建存储最大时间
                long maxTeDurTotal = 0;
                // 遍历删除任务
                for (int i = delDateIndex.size()-1; i >= 0; i--) {
                    JSONObject delInfo = delDateIndex.getJSONObject(i);
                    int type = delInfo.getInteger("type");
                    int index = delInfo.getInteger("index");
                    if (type == 0) {
                        // 获取时间处理数据
                        JSONObject oDate = oDates.getJSONObject(index);
                        // 获取时间处理的实际准备时间
                        Long wntPrep = oDate.getLong("wntPrep");
                        Long wntDur = oDate.getLong("wntDur");
                        Double wn2qtyneed = oDate.getDouble("wn2qtyneed");
                        // 存储任务总时间
                        long taskTotalTime = (long)(wntDur * wn2qtyneed);
                        oDate.put("grpUNum",oDate.getInteger("grpUNum")==null?1:oDate.getInteger("grpUNum"));
                        long grpUNum;
                        // 计算总时间
                        if (taskTotalTime % oDate.getInteger("grpUNum") == 0) {
                            grpUNum = taskTotalTime / oDate.getInteger("grpUNum");
                        } else {
                            grpUNum = (long) Math.ceil((double) (taskTotalTime / oDate.getInteger("grpUNum")));
                        }
                        // 获取时间处理的总任务时间
                        long wntDurTotal = grpUNum+wntPrep;
                        if (wntDurTotal > maxTeDurTotal) {
                            maxTeDurTotal = wntDurTotal;
                        }
                    }
                    oDates.remove(index);
                }
                System.out.println("oDates-zui:");
                System.out.println(JSON.toJSONString(oDates));
                pratInfo.put("oDates",oDates);
                pratInfo.put("maxTeDurTotal",maxTeDurTotal);
                prodObj.put(prodId,pratInfo);
            }
            oDateObj.put(layer,prodObj);
        }
        System.out.println("depPrior:");
        System.out.println(JSON.toJSONString(depPrior));
        System.out.println("结束oDateObj:");
        System.out.println(JSON.toJSONString(oDateObj));
        qt.setMDContent(id_OP,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
    }

    /**
     * 获取指定aArrange卡片方法
     * @param asset	资产信息
     * @return 返回结果: {@link JSONObject}
     * @author tang
     * @date 创建时间: 2023/3/4
     * @ver 版本号: 1.0.0
     */
    protected JSONObject getAArrangeNew(Asset asset){
//        return asset.getAArrange2();
        return asset.getAArrange();
    }

    /**
     * 根据条件添加当前的冲突信息的状态
     * @param status    状态
     * @param thisInfo  当前处理通用信息
     */
    protected void addThisConflictInfoStatus(int status,int dateIndex,JSONObject thisInfo){
        JSONObject thisInfoConflictInfo = GsThisInfo.getThisInfoConflictInfo(thisInfo);
        if (null != thisInfoConflictInfo) {
            JSONObject dateIndexInfo = thisInfoConflictInfo.getJSONObject(dateIndex + "");
            if (null != dateIndexInfo) {
                if (dateIndexInfo.getInteger("status") == 0) {
                    dateIndexInfo.put("status",status);
                    thisInfoConflictInfo.put(dateIndex + "",dateIndexInfo);
                    GsThisInfo.setThisInfoConflictInfo(thisInfo,thisInfoConflictInfo);
                }
            }
        }
    }
    /**
     * 根据条件添加当前的冲突信息的状态
     * @param id_O    冲突的订单编号
     * @param thisInfo  当前处理通用信息
     */
    protected void addThisConflictLastODate(String id_O,String id_C,JSONObject thisInfo,JSONObject actionIdO
            ,String layer,String id_PF){
        JSONObject thisInfoConflictLastODate = GsThisInfo.getThisInfoConflictLastODate(thisInfo);
        if (null == thisInfoConflictLastODate) {
            thisInfoConflictLastODate = new JSONObject();
        }
        String id_OP = sonGetOrderFatherId(id_O, id_C, thisInfo, actionIdO, new JSONObject());
        if (!thisInfoConflictLastODate.containsKey(id_OP)) {
            JSONArray oDates = actionIdO.getJSONObject(id_OP).getJSONObject(layer).getJSONObject(id_PF).getJSONArray("oDates");
            thisInfoConflictLastODate.put(id_OP,qt.setJson("dateIndex",oDates.size()-1,"layer",layer,"id_PF",id_PF));
            GsThisInfo.setThisInfoConflictLastODate(thisInfo,thisInfoConflictLastODate);
        }
    }
    /**
     * 更新全局任务信息，将镜像全局任务写入全局任务
     * @param objTaskAll    全局任务
     * @param allImageTasks 镜像全局任务
     * @param allImageTotalTime 镜像全局任务余剩时间
     */
    protected void updateObjTaskAllSetAllImageTasks(JSONObject objTaskAll
            ,Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks,JSONObject allImageTotalTime){
        // 获取任务信息镜像所有键（部门）
        Set<String> allImageTasksAllKey = allImageTasks.keySet();
        // 遍历键（部门）
        allImageTasksAllKey.forEach(dep -> {
            // 根据键（部门）获取组别对应的任务信息
            Map<String, Map<Long, List<Task>>> grpBTaskInfo = allImageTasks.get(dep);
            // 根据键（部门）获取组别任务当天余剩时间
            JSONObject sameDayRemainingTime = allImageTotalTime.getJSONObject(dep);
            // 遍历键（组别）
            grpBTaskInfo.keySet().forEach(grpB->{
                // 根据键（组别）获取当前任务信息
                Map<Long, List<Task>> sameDayTaskInfo = grpBTaskInfo.get(grpB);
                // 根据键（组别）获取任务当天余剩时间
                JSONObject taskSameDayRemainingTime = sameDayRemainingTime.getJSONObject(grpB);
                // 遍历键（当前）
                sameDayTaskInfo.keySet().forEach(date->{
                    // 根据键（当天）获取当天任务余剩时间
                    Long zon = taskSameDayRemainingTime.getLong(date+"");
                    // 获取当前任务信息
                    List<Task> tasks = sameDayTaskInfo.get(date);
                    if (null != tasks && tasks.size() > 0) {
                        // 调用写入任务到全局任务信息方法
                        setTasksAndZon(tasks,grpB,dep,date,zon,objTaskAll);
                    }
                });
            });
        });
    }

    /**
     * 更新镜像全局任务信息，将全局任务写入镜像全局任务
     * @param objTaskAll    全局任务
     * @param allImageTasks 镜像全局任务
     * @param allImageTotalTime 镜像全局任务余剩时间
     */
    protected void updateAllImageTasksSetObjTaskAll(JSONObject objTaskAll
            ,Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks,JSONObject allImageTotalTime){
        for (String dep : objTaskAll.keySet()) {
            JSONObject depAll = objTaskAll.getJSONObject(dep);
            for (String grpB : depAll.keySet()) {
                JSONObject grpBAll = depAll.getJSONObject(grpB);
                for (String time : grpBAll.keySet()) {
                    JSONObject timeAll = grpBAll.getJSONObject(time);
                    JSONArray tasks = timeAll.getJSONArray("tasks");
                    Long zon = timeAll.getLong("zon");
                    List<Task> taskList = new ArrayList<>();
                    for (int i = 0; i < tasks.size(); i++) {
                        taskList.add(JSONObject.parseObject(JSON.toJSONString(tasks.getJSONObject(i)), Task.class));
                    }
                    setImageTasks(taskList,grpB,dep,Long.parseLong(time),allImageTasks);
                    setImageZon(zon,grpB,dep,Long.parseLong(time),allImageTotalTime);
                }
            }
        }
    }

    /**
     * 子订单获取父订单编号方法
     * @param id_O  子订单编号
     * @param id_C  当前公司编号
     * @param thisInfo  当前处理通用信息
     * @param actionIdO 存储所有订单的oDates信息
     * @param grpUNumAll 存储部门对应组别的职位总人数
     * @return  父订单编号
     */
    protected String sonGetOrderFatherId(String id_O,String id_C,JSONObject thisInfo,JSONObject actionIdO,JSONObject grpUNumAll){
        JSONObject thisInfoOrderFatherId = GsThisInfo.getThisInfoOrderFatherId(thisInfo);
//        System.out.println("sonGetOrderFatherId:"+id_O);
//        System.out.println(JSON.toJSONString(thisInfoOrderFatherId));
        if (null == thisInfoOrderFatherId) {
            thisInfoOrderFatherId = new JSONObject();
        } else {
            if (thisInfoOrderFatherId.containsKey(id_O)) {
                return thisInfoOrderFatherId.getString(id_O);
            }
        }
        Order order = qt.getMDContent(id_O,qt.strList("info"), Order.class);
        // 判断订单是否为空
        if (null == order || null == order.getInfo()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        if (null != order.getInfo().getId_OP()) {
            id_O = order.getInfo().getId_OP();
            order = qt.getMDContent(id_O,qt.strList("casItemx"), Order.class);
            // 判断订单是否为空
            if (null == order || null == order.getCasItemx()) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "父订单不存在");
            }
            JSONObject java = order.getCasItemx().getJSONObject("java");
            JSONObject compOrderInfo = order.getCasItemx().getJSONObject(id_C);
            // 判断订单是否为空
            if (null == java || null == compOrderInfo) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "父订单不存在");
            }
            JSONObject oDateObj = mergeTaskByPriorODateObj(java.getJSONObject("oDateObj"));
            qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
            System.out.println("sonGetOrderFatherId:添加前:");
            System.out.println(JSON.toJSONString(actionIdO));
            actionIdO.put(id_O,oDateObj);
            System.out.println("sonGetOrderFatherId:添加后:");
            System.out.println(JSON.toJSONString(actionIdO));
            JSONArray objOrder = compOrderInfo.getJSONArray("objOrder");
            for (int i = 0; i < objOrder.size(); i++) {
                JSONObject obj = objOrder.getJSONObject(i);
                thisInfoOrderFatherId.put(obj.getString("id_O"),id_O);
            }
            thisInfoOrderFatherId.put(id_O,id_O);
            GsThisInfo.setThisInfoOrderFatherId(thisInfo,thisInfoOrderFatherId);
            return thisInfoOrderFatherId.getString(id_O);
        }
        return null;
    }

    /**
     * 父订单写入oDates信息方法
     * @param id_O  父订单编号
     * @param thisInfo  当前处理通用信息
     * @param actionIdO 存储所有订单的oDates信息
     * @param objOrder 子订单信息
     * @param oDateObj oDateObj信息
     */
    protected void setOrderFatherId(String id_O,JSONObject thisInfo,JSONObject actionIdO
            ,JSONArray objOrder,JSONObject oDateObj
    ){
        System.out.println("setOrderFatherId:添加前:");
        System.out.println(JSON.toJSONString(actionIdO));
        actionIdO.put(id_O,oDateObj);
        System.out.println("setOrderFatherId:添加后:");
        System.out.println(JSON.toJSONString(actionIdO));
        JSONObject thisInfoOrderFatherId = GsThisInfo.getThisInfoOrderFatherId(thisInfo);
        if (null == thisInfoOrderFatherId) {
            thisInfoOrderFatherId = new JSONObject();
        }
        System.out.println("objOrder:");
        System.out.println(JSON.toJSONString(objOrder));
        for (int i = 0; i < objOrder.size() - 1; i++) {
            String id_OInside = objOrder.getJSONObject(i).getString("id_O");
            thisInfoOrderFatherId.put(id_OInside,id_O);
        }
        thisInfoOrderFatherId.put(id_O,id_O);
        System.out.println("thisInfoOrderFatherId:");
        System.out.println(JSON.toJSONString(thisInfoOrderFatherId));
        GsThisInfo.setThisInfoOrderFatherId(thisInfo,thisInfoOrderFatherId);
    }

    protected int getQuiltConflictInfoAddIndex(JSONObject thisInfo){
        JSONObject thisInfoQuiltConflictInfo = GsThisInfo.getThisInfoQuiltConflictInfo(thisInfo);
        if (null == thisInfoQuiltConflictInfo) {
            return 0;
        } else {
            return thisInfoQuiltConflictInfo.getInteger("addIndex")+1;
        }
    }

    /**
     * 获取当前处理的冲突下标
     * @param thisInfo  当前处理通用信息存储
     * @return  冲突下标
     */
    protected int getQuiltConflictInfoOperateIndex(JSONObject thisInfo){
        JSONObject thisInfoQuiltConflictInfo = GsThisInfo.getThisInfoQuiltConflictInfo(thisInfo);
        int result = 0;
        if (null != thisInfoQuiltConflictInfo.getInteger("operateIndex")) {
            result = thisInfoQuiltConflictInfo.getInteger("operateIndex")+1;
        }
        thisInfoQuiltConflictInfo.put("operateIndex",result);
        GsThisInfo.setThisInfoQuiltConflictInfo(thisInfo,thisInfoQuiltConflictInfo);
        return result;
    }
}
