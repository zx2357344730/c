package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.*;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    protected CoupaUtil coupaUtil;

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
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp){
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
                System.out.println("进入jump-1:");
                // 获取指定的全局任务信息方法
                JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep,onlyFirstTimeStamp
                        ,newestLastCurrentTimestamp), grpB, dep, id_C,objTaskAll);
                // 获取任务信息
                Object[] tasksIs = isTasksNull(tasksAndZon);
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
                    result = getChkInJumpDayByRandom(random,grpB,dep,id_C,xbAndSbAll,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                }
            } else {
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
                        zuiZon -= task.getTeDurTotal();
                    }
                    zon = zuiZon;
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep,onlyFirstTimeStamp
                            ,newestLastCurrentTimestamp), grpB, dep, id_C,objTaskAll);
                    List<Task> tasksNew;
                    Object[] tasksIs = isTasksNull(tasksAndZon);
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
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
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
        } else {
            // 判断是获取数据库任务信息
            if (isGetTaskPattern == 0) {
                System.out.println("进入jump-3:");
                JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C,objTaskAll);
                Object[] tasksIs = isTasksNull(tasksAndZon);
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
            } else {
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
                        zuiZon -= task.getTeDurTotal();
                    }
                    zon = zuiZon;
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C,objTaskAll);
                    List<Task> tasksNew = new ArrayList<>();
                    if (null == tasksAndZon) {
                        zon = 28800L;
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
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
                        zon = getImageZon(teS,grpB,dep,allImageTotalTime);
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
            ,JSONObject xbAndSbAll,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp){
        // 创建返回对象
        Map<String,Object> result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
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
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
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
    protected Long getImageZon(Long teS,String grpBNext,String depNext,JSONObject allImageTotalTime){
        Long zon;
        JSONObject depKeyZon = allImageTotalTime.getJSONObject(depNext);
        if (null == depKeyZon) {
            depKeyZon = new JSONObject();
            JSONObject grpBKeyZon = new JSONObject();
            zon = 28800L;
            grpBKeyZon.put(teS+"",zon);
            depKeyZon.put(grpBNext,grpBKeyZon);
            allImageTotalTime.put(depNext,depKeyZon);
        } else {
            JSONObject grpBKeyZon = depKeyZon.getJSONObject(grpBNext);
            if (null == grpBKeyZon) {
                grpBKeyZon = new JSONObject();
                zon = 28800L;
                grpBKeyZon.put(teS+"",zon);
                depKeyZon.put(grpBNext,grpBKeyZon);
                allImageTotalTime.put(depNext,depKeyZon);
            } else {
                if (null == grpBKeyZon.get(teS+"")) {
                    zon = 28800L;
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
     * 根据id_O，index获取任务所在日期方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param storageTaskWhereTime	存储任务所在日期
     * @return java.util.List<java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected List<String> getTaskWhereDate(String id_O,Integer index,JSONObject storageTaskWhereTime){
        List<String> result = new ArrayList<>();
        JSONObject id_OStorage = storageTaskWhereTime.getJSONObject(id_O);
        if (null == id_OStorage) {
            JSONObject id_OStorageNew = new JSONObject();
            id_OStorageNew.put(index.toString(),new JSONObject());
            storageTaskWhereTime.put(id_O,id_OStorageNew);
        } else {
            JSONObject indexStorage = id_OStorage.getJSONObject(index.toString());
            if (null == indexStorage) {
                id_OStorage.put(index.toString(),new JSONObject());
                storageTaskWhereTime.put(id_O,id_OStorage);
            } else {
                return new ArrayList<>(indexStorage.keySet());
            }
        }
//        System.out.println("获取镜像所在日期列表:-id_O:"+id_O+",-index:"+index);
        return result;
    }

    /**
     * 根据grpB，dep获取数据库职位总人数方法
     * @param grpB  职位
     * @param dep   部门
     * @param id_C  公司编号
     * @param grpUNumAll    全局职位人数信息
     * @return java.lang.Integer  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public Integer getObjGrpUNum(String grpB,String dep,String id_C,JSONObject grpUNumAll){
//        System.out.println("获取职位总人数:"+grpB+"-"+dep+"- id_C:"+id_C);
        // 获取全局职位人数的部门信息
        JSONObject depGrpUNum = grpUNumAll.getJSONObject(dep);
        // 存储为空状态
        int isStorageEmpty;
        // 判断不为空
        if (null != depGrpUNum) {
            // 根据组别获取职位人数
            Integer positionNum = depGrpUNum.getInteger(grpB);
            // 判断职位人数不为空
            if (null != positionNum) {
                // 直接返回结果
                return positionNum;
            } else {
                isStorageEmpty = 2;
            }
        } else {
            isStorageEmpty = 1;
        }

        System.out.println("获取职位总人数:"+grpB+"-"+dep+"- id_C:"+id_C);
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的打卡卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));

        // 打卡卡片信息
        JSONObject chkin00s = asset.getChkin00s();
        // 获取职位人数信息
        JSONObject objZw = chkin00s.getJSONObject("objZw");
        // 判断职位人数信息为空
        if (null == objZw) {
            // 创建
            objZw = new JSONObject();
        }
        // 获取职位人数的部门信息
        JSONObject depObj = objZw.getJSONObject(dep);
        // 判断为空
        if (null == depObj) {
            System.out.println("grpUNum-为空-dep:"+dep+" , grpB:"+grpB);
            // 创建部门信息
            depObj = new JSONObject();
            // 添加职位人数
            depObj.put(grpB,1);
            objZw.put(dep,depObj);
            chkin00s.put("objZw",objZw);
            // 创建请求参数存储字典
            JSONObject mapKey = new JSONObject();
            // 添加请求参数
            mapKey.put("chkin00s",chkin00s);
            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
            if (isStorageEmpty == 1) {
                depGrpUNum = new JSONObject();
            }
            depGrpUNum.put(grpB, 1);
            grpUNumAll.put(dep, depGrpUNum);
            return 1;
        } else {
            Integer grpBObj = depObj.getInteger(grpB);
            if (null == grpBObj){
                grpBObj = 1;
                System.out.println("grpUNum-为空-grpB:"+grpB);
                depObj.put(grpB,grpBObj);
                objZw.put(dep,depObj);
                chkin00s.put("objZw",objZw);
                // 创建请求参数存储字典
                JSONObject mapKey = new JSONObject();
                // 添加请求参数
                mapKey.put("chkin00s",chkin00s);
                coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
            }
            if (isStorageEmpty == 1) {
                depGrpUNum = new JSONObject();
            }
            depGrpUNum.put(grpB, grpBObj);
            grpUNumAll.put(dep, depGrpUNum);
            return grpBObj;
        }
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
    protected JSONObject getXbAndSb(String grpB,String dep,String id_C,JSONObject xbAndSbAll){
//        System.out.println("获取不上班打卡时间:"+teStart+"-"+grpB+"-"+dep+"- id_C:"+id_C);
        // 获取全局上班下班信息的部门信息
        JSONObject depXbSb = xbAndSbAll.getJSONObject(dep);
        // 存储为空状态
        int isStorageEmpty;
        // 判断部门信息不为空
        if (null != depXbSb) {
            // 获取全局上班下班信息的组别信息
            JSONObject grpBXbSb = depXbSb.getJSONObject(grpB);
            // 判断组别信息不为空
            if (null != grpBXbSb) {
                return grpBXbSb;
            } else {
                isStorageEmpty = 2;
            }
        } else {
            isStorageEmpty = 1;
        }
        // 创建存储返回结果
        JSONObject result = new JSONObject();
        // 定义存储上班下班部门信息
        JSONObject depWork;
        // 存储时间处理打卡信息下标
        int chKinIndex = 0;
        // 存储是否存在时间处理打卡信息
        int isTimeChKin = 0;
        // 存储打卡信息为空状态
        int chKinEmpty = 0;
        // 存储打卡信息部门组别下标状态
        int chKinDepGrpBIndex = 0;
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的打卡卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
        // 获取打卡卡片信息
        JSONObject chkin00s = asset.getChkin00s();
        // 获取打卡信息
        JSONArray objData = chkin00s.getJSONArray("objData");
        // 判断打卡信息为空
        if (null == objData) {
            // 创建打卡信息
            objData = new JSONArray();
            // 创建时间处理打卡信息
            JSONObject dataChKin = new JSONObject();
            // 添加信息
            dataChKin.put("timeP",1);
            dataChKin.put("objXb", TaskObj.getXbJson());
            dataChKin.put("objSb", TaskObj.getSbJson());
            objData.add(dataChKin);
            chKinEmpty = 1;
        } else {
            // 遍历打卡信息
            for (int i = 0; i < objData.size(); i++) {
                JSONObject dataZ = objData.getJSONObject(i);
                // 判断存储时间处理打卡信息
                if (null != dataZ.getInteger("timeP")) {
                    // 存在则赋值
                    chKinIndex = i;
                    isTimeChKin = 1;
                    break;
                }
            }
            // 判断不存在时间处理打卡信息
            if (isTimeChKin == 0) {
                // 创建时间处理打卡信息
                JSONObject dataChKin = new JSONObject();
                // 添加信息
                dataChKin.put("timeP",1);
                dataChKin.put("objXb", TaskObj.getXbJson());
                dataChKin.put("objSb", TaskObj.getSbJson());
                objData.add(dataChKin);
                chKinIndex = objData.size()-1;
                chKinEmpty = 2;
            }
        }
        // 获取部门组别存储打卡信息下标信息
        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
        // 定义存储组别下标信息
        Integer objGrpB;
        // 判断为空
        if (null == objWorkTime) {
            // 创建并添加信息
            objWorkTime = new JSONObject();
            depWork = new JSONObject();
            depWork.put(grpB,chKinIndex);
            objWorkTime.put(dep,depWork);
            objGrpB = chKinIndex;
            chKinDepGrpBIndex = 1;
        } else {
            JSONObject objDep = objWorkTime.getJSONObject(dep);
            if (null == objDep) {
                objDep = new JSONObject();
                objDep.put(grpB,chKinIndex);
                objWorkTime.put(dep,objDep);
                objGrpB = chKinIndex;
                chKinDepGrpBIndex = 2;
            } else {
                objGrpB = objDep.getInteger(grpB);
                if (null == objGrpB) {
                    objDep.put(grpB,chKinIndex);
                    objWorkTime.put(dep,objDep);
                    objGrpB = chKinIndex;
                    chKinDepGrpBIndex = 3;
                } else if (objGrpB != chKinIndex) {
                    objDep.put(grpB,chKinIndex);
                    objWorkTime.put(dep,objDep);
                    objGrpB = chKinIndex;
                    chKinDepGrpBIndex = 4;
                }
            }
        }
        // 判断打卡信息为空，或者部门组别存储打卡信息下标信息为空
        if (chKinEmpty > 0 || chKinDepGrpBIndex > 0) {
            if (chKinDepGrpBIndex > 0) {
                chkin00s.put("objWorkTime",objWorkTime);
            }
            if (chKinEmpty > 0) {
                chkin00s.put("objData",objData);
            }
            // 创建请求参数存储字典
            JSONObject mapKey = new JSONObject();
            // 添加请求参数
            mapKey.put("chkin00s",chkin00s);
            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
        }
        JSONObject workInfo = objData.getJSONObject(objGrpB);
        result.put("xb",workInfo.getJSONArray("objXb"));
        result.put("sb",workInfo.getJSONArray("objSb"));

        System.out.println("获取不上班和上班时间:"+"-"+grpB+"-"+dep+"- id_C:"+id_C);

        if (isStorageEmpty == 1) {
            depXbSb = new JSONObject();
        }
        depXbSb.put(grpB, result);
        xbAndSbAll.put(dep, depXbSb);
        return result;
    }

    /**
     * 写入镜像任务集合方法
     * @param tasks	任务集合
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param allImageTasks	全局镜像任务列表信息
     * @return void  返回结果: 结果
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
     * @return void  返回结果: 结果
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
     * @return void  返回结果: 结果
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
     * @return void  返回结果: 结果
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
            ,JSONObject objTaskAll){
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
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的时间处理卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList(timeCard));
//        System.out.println("获取任务集合:"+teStart+" - "+grpB+" - "+dep);
//        System.out.println("查询数据库:");
        // 获取时间处理卡片信息
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
        // 判断为空
        if (null == aArrange) {
            // 添加一个空任务信息
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,28800L,objTaskAll);
            return null;
        }
        JSONObject objTasks = aArrange.getJSONObject("objTask");
        if (null == objTasks) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,28800L,objTaskAll);
            return null;
        }
        JSONObject objDep = objTasks.getJSONObject(dep);
        if (null == objDep) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,28800L,objTaskAll);
            return null;
        }
        JSONObject objGrpB = objDep.getJSONObject(grpB);
        if (null == objGrpB) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,28800L,objTaskAll);
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
     * @return void  返回结果: 结果
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

    /**
     * 根据公司编号清空所有任务信息
     * @param id_C	公司编号
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 2:07
     */
    public void setTaskAndZonKai(String id_C){
        // 根据公司编号获取assetId
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据assetId获取asset的任务卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList(timeCard));
        // 获取任务卡片信息
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
        // 判断任务卡片为空
        if (null == aArrange) {
            // 创建任务卡片
            aArrange = new JSONObject();
        }
        // 清空所有任务信息
        aArrange.put("objTask",new JSONObject());
        // 创建请求参数存储字典
        JSONObject mapKey = new JSONObject();
        // 添加请求参数
        mapKey.put(timeCard,aArrange);
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
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
     * @param teDurTotal	任务总时间
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void addOrUpdateTeDate(Long teS,JSONObject teDate,Long teDurTotal){
        Long teSInfo = teDate.getLong(teS + "");
        if (null == teSInfo) {
            // 添加当前处理的任务的所在日期对象状态
            teDate.put(teS+"",teDurTotal);
        } else {
            // 添加当前处理的任务的所在日期对象状态
            teDate.put(teS+"",(teSInfo+teDurTotal));
        }
    }

    /**
     * 判断产品状态再调用写入任务所在日期方法的方法
     * @param id_O  订单编号
     * @param index 订单下标
     * @param teS   当前时间戳
     * @param prodState  产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
     * @param storageTaskWhereTime	存储任务所在日期
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void putTeDate(String id_O,Integer index,Long teS,Integer prodState,JSONObject storageTaskWhereTime){
        // 判断状态不等于当前递归产品
        if (prodState != -1) {
//            System.out.println("添加记录时间-id_O:"+id_O+",index:"+index+",teS:"+teS);
            // 调用写入任务所在日期方法
            setTaskWhereDate(id_O,index,teS,storageTaskWhereTime);
        }
    }

    /**
     * 写入任务所在日期方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param teS	当前时间戳
     * @param storageTaskWhereTime	存储任务所在日期
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    protected void setTaskWhereDate(String id_O, Integer index, Long teS,JSONObject storageTaskWhereTime){
        JSONObject id_OStorage = storageTaskWhereTime.getJSONObject(id_O);
        if (null == id_OStorage) {
            JSONObject indexStorage = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(teS.toString(),0);
            indexStorage.put(index.toString(),jsonObject);
            storageTaskWhereTime.put(id_O,indexStorage);
        } else {
            JSONObject indexStorage = id_OStorage.getJSONObject(index.toString());
            indexStorage.put(teS.toString(),0);
            id_OStorage.put(index.toString(),indexStorage);
            storageTaskWhereTime.put(id_O,id_OStorage);
        }
    }

    /**
     * 添加记录进入哪个未操作到的地方
     * @param randomAll	全局唯一编号
     * @param text	位置信息
     * @param recordNoOperation	存储记录字典
     * @return void  返回结果: 结果
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
     * @return void  返回结果: 结果
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
            // 创建订单编号存储产品状态对象
            JSONObject id_OStateObj = new JSONObject();
            // 创建订单下标存储产品状态对象
            JSONObject indexStateObj = new JSONObject();
            // 添加产品状态为第一个产品
            indexStateObj.put("prodState",1);
            // 添加当前订单编号和订单下标记录
            indexStateObj.put("z",id_OAdd+"+"+indexAdd);
            // 添加订单下标存储产品状态对象
            id_OStateObj.put(indexAdd,indexStateObj);
            // 添加订单编号存储产品状态对象
            sho.put(id_OAdd,id_OStateObj);
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
    private Object[] isTasksNull(JSONObject tasksAndZon){
        List<Task> tasks;
        long zon;
        if (null == tasksAndZon) {
            tasks = new ArrayList<>();
            zon = 28800L;
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
                ,"",-1,0L,-1,"电脑",0L,0L,id_C
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
                    , offWorkInfo.getInteger("priority"), "电脑", 0L,0L,id_C
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
                ,"电脑",0L,0L,id_C,0L,0L
                ,-1,false));
        // 遍历不上班时间
        for (int i = 0; i < offWork.size(); i++) {
            // 根据下标i获取不上班时间段
            JSONObject offWorkInfo = offWork.getJSONObject(i);
            // 任务集合添加任务信息
            tasks.add(TaskObj.getTask((teS + offWorkInfo.getLong("tePStart"))
                    , (teS + offWorkInfo.getLong("tePFinish"))
                    , "", -1, offWorkInfo.getLong("zon")
                    , offWorkInfo.getInteger("priority"), "电脑", 0L,0L
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
            ,List<Task> thisTasks,JSONObject isSetImage
    ){
        // 下班加一，从下一个开始
        dateIndex++;
        System.out.println("进入清理方法:"+TimeZj.isZ+" - "+id_O+" - "+dateIndex);
//        System.out.println(JSON.toJSONString(allImageTeDate));
//        System.out.println(JSON.toJSONString(allImageTasks));
        // 调用写入清理状态方法
        setClearStatus(id_O,dateIndex,clearStatus,0);
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
            // 遍历需要删除的位置信息爱
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
                        Long zon = getImageZon(timeLong,grpB,dep,allImageTotalTime);
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
                                    removeInfo.put("teDurTotal",task.getTeDurTotal());
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
                            zon+=indexJson.getLong("teDurTotal");
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
        // 根据订单编号查询action卡片信息
        Order insideAction = coupaUtil.getOrderByListKey(id_O, Arrays.asList("action","info"));
        // 判断订单是否为空
        if (null == insideAction || null == insideAction.getInfo()
                || null == insideAction.getAction()|| null == insideAction.getAction().getJSONObject("grpBGroup")
                || null == insideAction.getAction().getJSONArray("objAction")) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-");
            System.out.println();
            return null;
        }
        // 调用方法获取订单信息
        Order salesOrderData = coupaUtil.getOrderByListKey(
                insideAction.getInfo().getId_OP(), Collections.singletonList("action"));
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction()
                || null == salesOrderData.getAction().getJSONArray("oDates")) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-");
            System.out.println();
            return null;
        }
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        if (null == assetId) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-");
            System.out.println();
            return null;
        }
        // 根据asset编号获取asset的打卡卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Arrays.asList(timeCard,"chkin00s"));
        if (null == asset) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-");
            System.out.println();
            return null;
        }
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
        if (null == aArrange || null == aArrange.getJSONObject("objTask")) {
            // 返回为空错误信息
            System.out.println();
            System.out.println("-查询为空!-");
            System.out.println();
            return null;
        }
        // 获取数据库任务信息
        JSONObject objTask = aArrange.getJSONObject("objTask");
        // 获取进度的oDates字段信息
        JSONArray oDates = salesOrderData.getAction().getJSONArray("oDates");
//        boolean isOneDel = true;
        // 创建存储部门和组别信息字典
        Map<String,String> grpBAndDep = new HashMap<>();
        // 获取递归信息
        JSONArray objAction = insideAction.getAction().getJSONArray("objAction");
        // 获取组别对应部门信息
        JSONObject grpBGroup = insideAction.getAction().getJSONObject("grpBGroup");
        // 遍历组别对应部门信息
        for (String grpB : grpBGroup.keySet()) {
            // 获取组别对应信息
            JSONObject grpBInfo = grpBGroup.getJSONObject(grpB);
            // 获取组别的部门
            String depNew = grpBInfo.getString("dep");
            grpBAndDep.put(grpB,depNew);
        }
//        System.out.println("初始objTask:");
//        System.out.println(JSON.toJSONString(objTask));
//        System.out.println(JSON.toJSONString(objThisTaskAll));
//        System.out.println(JSON.toJSONString(storageTaskWhereTime));
//        System.out.println(JSON.toJSONString(allImageTasks));
        try {
            // 遍历时间处理信息集合
            for (int i = dateIndex; i < oDates.size(); i++) {
                // 获取i对应的时间处理信息
                JSONObject oDate = oDates.getJSONObject(i);
                // 获取时间处理的组别
                String grpBNew = oDate.getString("grpB");
                // 根据组别获取部门
                String depNew = grpBAndDep.get(grpBNew);
                // 获取订单编号
                String id_OThis = oDate.getString("id_O");
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

                // 获取下标
                Integer indexThis = oDate.getInteger("index");
                // 根据指定下标获取进度信息
                JSONObject actionIndex = objAction.getJSONObject(indexThis);
                // 获取任务所在时间
                JSONObject teDateNext = actionIndex.getJSONObject("teDate");
                System.out.println(id_OThis+" - "+indexThis+" - "+grpBNew+" - "+depNew
                        +" - teDateNext:"+JSON.toJSONString(teDateNext));
//                List<String> taskWhereDate = getTaskWhereDate(id_OThis, indexThis, storageTaskWhereTime);
//                System.out.println(JSON.toJSONString(taskWhereDate));

                // 定义存储是否是拿全局任务，默认不是
                boolean isGetThisTaskAll = false;
                // 根据部门获取指定数据库任务信息的部门信息
                JSONObject depTask = objTask.getJSONObject(depNew);
                // 判断为空
                if (null == depTask) {
                    // 获取当前全局任务信息的部门信息
                    depTask = objThisTaskAll.getJSONObject(depNew);
                    // 更新状态
                    isGetThisTaskAll = true;
                }
                // 根据部门信息获取指定组别的信息
                JSONObject grpBTask = depTask.getJSONObject(grpBNew);
                // 判断为空
                if (null == grpBTask) {
                    // 获取全局任务信息
                    depTask = objThisTaskAll.getJSONObject(depNew);
                    grpBTask = depTask.getJSONObject(grpBNew);
                    // 更新状态
                    isGetThisTaskAll = true;
                }
                // 遍历任务所在时间
                for (String time : teDateNext.keySet()) {
                    // 获取所在时间任务信息
                    JSONObject timeTask = grpBTask.getJSONObject(time);
                    // 获取任务列表
                    JSONArray tasksNew = timeTask.getJSONArray("tasks");
//                    System.out.println("tasksList: - time:"+time);
//                    System.out.println(JSON.toJSONString(tasksNew));
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
//                            System.out.println("清理任务的内部输出:");
//                            System.out.println(JSON.toJSONString(taskInside));
                            // 创建删除信息
                            JSONObject removeInfo = new JSONObject();
                            // 添加删除信息
                            removeInfo.put("index",t);
                            removeInfo.put("teDurTotal",taskInside.getLong("teDurTotal"));
                            removeIndex.add(removeInfo);
                            // 定义存储是否存在
                            boolean isNormal = true;
                            // 获取订单编号信息
                            JSONObject id_OInfo = result.getJSONObject(id_OThis);
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
                                indexInfo.put("teDurTotal",indexInfo.getLong("teDurTotal")+taskInside.getLong("teDurTotal"));
                                // 更新数据
                                id_OInfo.put(indexThis+"",indexInfo);
                            } else {
                                // 更新数据
                                id_OInfo.put(indexThis+"",taskInside);
                            }
                            result.put(id_OThis,id_OInfo);
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
                        zon+=indexJson.getLong("teDurTotal");
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
                                            zonImage -= task.getTeDurTotal();
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
//                                                System.out.println("清理任务的内部输出:");
//                                                System.out.println(JSON.toJSONString(taskInside));
                                                // 创建删除信息
                                                JSONObject removeInfo = new JSONObject();
                                                // 添加删除信息
                                                removeInfo.put("index",t);
                                                removeInfo.put("teDurTotal",taskInside.getTeDurTotal());
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
                                                    indexInfo.put("teDurTotal",indexInfo.getLong("teDurTotal")+taskInside.getTeDurTotal());
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
                                            zonImage+=indexJson.getLong("teDurTotal");
                                        }
//                                        System.out.println("tasks: -写入镜像:"+Long.valueOf(time)+" - "+grpBNew+" - "+depNew);
//                                        System.out.println(JSON.toJSONString(tasks));
                                        // 写入镜像任务
                                        setImageTasks(tasks,grpBNew,depNew, Long.valueOf(time),allImageTasks);
                                        setImageZon(zonImage,grpBNew,depNew, Long.valueOf(time),allImageTotalTime);
                                    }
                                }
                            }
                        } else {
//                            System.out.println("tasksList: -写入镜像:"+Long.valueOf(time)+" - "+grpBNew+" - "+depNew);
//                            System.out.println(JSON.toJSONString(tasksList));
                            // 写入镜像任务
                            setImageTasks(tasksList,grpBNew,depNew, Long.valueOf(time),allImageTasks);
                            setImageZon(zon,grpBNew,depNew, Long.valueOf(time),allImageTotalTime);
                        }
//                        System.out.println("进入覆盖当前任务列表:");
                        // 写入任务到全局任务信息
                        setTasksAndZon(tasksList,grpBNew,depNew,Long.valueOf(time),zon,objThisTaskAll);
                    } else {
                        System.out.println("tasksList: -跳过写入镜像:"+Long.valueOf(time)+" - "+grpBNew+" - "+depNew);
//                        System.out.println(JSON.toJSONString(tasksList));
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
                depTask.put(grpBNew,grpBTask);
//                System.out.println("是否拿当前的: - "+(isGetThisTaskAll?"是":"不是"));
//                System.out.println("清理状态:"+clearStatusThis);
            }
//            setClearData(id_O,dateIndex,clearStatus,result);
        } catch (Exception ex) {
            System.out.println("循环问题:"+ex.getMessage());
            System.out.println(ex.getLocalizedMessage());
        }
        System.out.println("写入的objTask:");
//        System.out.println(JSON.toJSONString(objTask));
//        System.out.println();
        System.out.println(JSON.toJSONString(result));
//        System.out.println();
//        System.out.println(JSON.toJSONString(objThisTaskAll));
//        System.out.println();
//        System.out.println(JSON.toJSONString(allImageTasks));
//        System.out.println();
//        System.out.println(JSON.toJSONString(allImageTotalTime));
//        System.out.println("isSetObjTask:"+isSetObjTask);
//        System.out.println();
//        System.out.println(JSON.toJSONString(result));
        return result;
    }

    /**
     * 更新镜像的所在时间方法
     * @param id_O	订单编号
     * @param dateIndex	下标
     * @param time	时间
     * @param teDurTotal	当前用时
     * @param allImageTeDate	镜像任务所在日期
     * @param isGetTaskPattern   isGetTaskPattern – = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @return 返回结果:
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected void setAllImageTeDateAndDate(String id_O,int dateIndex,long time,long teDurTotal,JSONObject allImageTeDate
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
                        date.put(time+"",(date.getLong(time+"")+teDurTotal));
                    } else {
                        // 添加用时时间
                        date.put(time+"",teDurTotal);
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
     * 写入清理状态方法
     * @param id_O	订单编号
     * @param dateIndex	数据下标
     * @param clearStatus	清理状态信息
     * @param status	更新状态
     * @return 返回结果:
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected void setClearStatus(String id_O,int dateIndex,JSONObject clearStatus,int status){
        // 获取订单编号清理信息
        JSONObject clearId_O = clearStatus.getJSONObject(id_O);
        // 定义存储数据下标清理信息
        JSONObject clearIndex;
        // 判断订单编号清理信息为空
        if (null == clearId_O) {
            // 创建信息
            clearId_O = new JSONObject();
            clearIndex = new JSONObject();
        } else {
            // 获取数据下标清理信息
            clearIndex = clearId_O.getJSONObject(dateIndex + "");
            // 判断数据下标清理信息为空
            if (null == clearIndex) {
                // 创建信息
                clearIndex = new JSONObject();
            }
        }
        // 更新状态
//        clearIndex.put(dateIndex+"",status);
        clearIndex.put("status",status);
//        clearId_O.put(id_O,clearIndex);
        clearId_O.put(dateIndex+"", clearIndex);
        clearStatus.put(id_O,clearId_O);
    }

    /**
     * 更新当前信息ref方法
     * @param thisInfo	当前处理通用信息存储
     * @param ref	信息名称
     * @return 返回结果:
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected void setThisInfoRef(JSONObject thisInfo,String ref){
        // 更新ref
        thisInfo.put("thisRef",ref);
    }

    /**
     * 更新当前信息ref方法
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    protected String getThisInfoRef(JSONObject thisInfo){
        return thisInfo.getString("thisRef");
    }

    /**
     * 合并任务，以同部门同组别同序号进行合并
     * @param oDates	时间处理任务数据
     * @param oTasks	时间处理任务列表
     * @param grpBGroupIdOJ	根据组别存储部门信息
     * @param grpUNumAll	存储部门对应组别的职位总人数
     * @return 返回结果: {@link JSONObject}
     * @author tang
     * @date 创建时间: 2023/2/20
     * @ver 版本号: 1.0.0
     */
    protected JSONObject mergeTaskByPrior(JSONArray oDates,JSONArray oTasks,JSONObject grpBGroupIdOJ
            ,JSONObject grpUNumAll){
        // 创建存储部门序号信息字典
        JSONObject depPrior = new JSONObject();
        // 创建存储删除下标
        JSONArray delDateIndex = new JSONArray();
        System.out.println("开始输出:");
        System.out.println(JSON.toJSONString(oDates));
        System.out.println(JSON.toJSONString(oTasks));
        // 创建存储更新任务信息
        JSONObject updateTask = new JSONObject();
        // 遍历获取递归存储的时间处理信息
        for (int i = 0; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDate = oDates.getJSONObject(i);
            // 获取i对应的时间处理任务信息
            JSONObject oTask = oTasks.getJSONObject(i);
            String grpB = oDate.getString("grpB");
            // 根据组别获取部门
            String dep = grpBGroupIdOJ.getJSONObject(grpB).getString("dep");
            int priorItem = oDate.getInteger("priorItem");
            oDate.put("dep",dep);
            oDate.put("grpUNum",getObjGrpUNum(grpB,dep,oDate.getString("id_C"),grpUNumAll));

            // 获取类别
            Integer bmdpt = oDate.getInteger("bmdpt");
            // 判断不为空
            if (null == bmdpt) {
                // 调用方法获取订单信息
                Order orderData = coupaUtil.getOrderByListKey(
                        oDate.getString("id_O"), Collections.singletonList("action"));
                // 获取进度卡片数据
                JSONObject actionNew = orderData.getAction();
                // 获取进度信息
                JSONArray objAction = actionNew.getJSONArray("objAction");
                // 获取类别
                bmdpt = objAction.getJSONObject(oDate.getInteger("index")).getInteger("bmdpt");
            }
            // 判断是物料
            if (bmdpt == 3) {
                // 处理下标
                oTask.put("teDurTotal",0L);
                oTask.put("prep",0L);
                oTasks.set(i,oTask);
                oDates.set(i,oDate);
            } else if (bmdpt == 2) {
                // 处理下标
//                oTask.put("teDurTotal",0L);
//                oTask.put("prep",0L);
                oTasks.set(i,oTask);
                oDates.set(i,oDate);
            } else {
                Long teDur = oDate.getLong("teDur");
                Double wn2qtyneed = oDate.getDouble("wn2qtyneed");
                // 存储任务总时间
                long taskTotalTime = (long)(teDur * wn2qtyneed);
                long teDurTotal;
                // 计算总时间
                if (taskTotalTime % oDate.getInteger("grpUNum") == 0) {
                    teDurTotal = taskTotalTime / oDate.getInteger("grpUNum");
                } else {
                    teDurTotal = (long) Math.ceil((double) (taskTotalTime / oDate.getInteger("grpUNum")));
                }
                oTask.put("teDurTotal",teDurTotal);

                // 获取部门的信息
                JSONObject grpBPrior = depPrior.getJSONObject(dep);
                // 定义存储任务数据和任务信息
                JSONObject priorMap;
                // 判断为空
                if (null == grpBPrior) {
//                    System.out.println("进入-1");
                    // 添加新数据
                    grpBPrior = new JSONObject();
                    priorMap = new JSONObject();
                    JSONObject priorInfo = new JSONObject();
                    priorInfo.put("oDate",oDate);
                    priorInfo.put("oTask",oTask);
                    priorInfo.put("index",i);
                    priorMap.put(priorItem + "",priorInfo);
                } else {
                    // 获取组别的信息
                    priorMap = grpBPrior.getJSONObject(grpB);
                    // 判断为空
                    if (null == priorMap) {
//                        System.out.println("进入-2");
                        // 创建新的
                        priorMap = new JSONObject();
                        JSONObject priorInfo = new JSONObject();
                        priorInfo.put("oDate",oDate);
                        priorInfo.put("oTask",oTask);
                        priorInfo.put("index",i);
                        priorMap.put(priorItem + "",priorInfo);
                    } else {
                        // 获取序号信息
                        JSONObject priorInfo = priorMap.getJSONObject(priorItem + "");
                        // 判断为空
                        if (null == priorInfo) {
//                            System.out.println("进入-3");
                            priorInfo = new JSONObject();
                            priorInfo.put("oDate",oDate);
                            priorInfo.put("oTask",oTask);
                            priorInfo.put("index",i);
                        } else {
//                            System.out.println("进入-4");
                            // 获取之前存储的任务信息，并且转换为任务类
                            Task taskPrior = JSON.parseObject(JSON.toJSONString(priorInfo.getJSONObject("oTask")),Task.class);
                            JSONObject datePrior = priorInfo.getJSONObject("oDate");
                            // 获取合并任务列表
                            JSONArray mergeTasks = taskPrior.getMergeTasks();
                            // 获取当前任务信息，并且转换为任务类
                            Task task = JSON.parseObject(JSON.toJSONString(oTask),Task.class);
                            // 判断列表为空
                            if (null == mergeTasks || mergeTasks.size() == 0) {
                                // 创建列表
                                mergeTasks = new JSONArray();
                                // 添加之前的任务信息
                                mergeTasks.add(JSONObject.parseObject(JSON.toJSONString(taskPrior)));
                            }
                            // 添加当前下标
                            delDateIndex.add(i);
                            // 添加当前任务信息
                            mergeTasks.add(JSONObject.parseObject(JSON.toJSONString(task)));
                            // 更新列表
                            taskPrior.setMergeTasks(mergeTasks);
                            // 复制任务
                            Task taskX = TaskObj.getTaskX(0L, 0L, taskPrior.getTeDurTotal(), taskPrior);
                            // 更新准备时间
                            taskX.setPrep(taskPrior.getPrep()+task.getPrep());
                            // 更新任务总时间
                            taskX.setTeDurTotal(taskPrior.getTeDurTotal()+task.getTeDurTotal());
                            // 获取合并任务数据列表
                            JSONArray mergeDates = datePrior.getJSONArray("mergeDates");
                            if (null == mergeDates || mergeDates.size() == 0) {
                                // 创建列表
                                mergeDates = new JSONArray();
                                // 添加之前的任务数据信息
                                mergeDates.add(JSONObject.parseObject(JSON.toJSONString(datePrior)));
                            }
                            // 添加当前任务数据信息
                            mergeDates.add(oDate);
                            datePrior.put("mergeDates",mergeDates);
                            // 更新任务信息
                            priorInfo.put("oTask",JSONObject.parseObject(JSON.toJSONString(taskX)));
                            priorInfo.put("oDate",datePrior);
                            // 获取之前存储下标
                            int index = priorInfo.getInteger("index");
                            // 添加下标到更新
                            updateTask.put(index+"",JSONObject.parseObject(JSON.toJSONString(taskX)));
                        }
                        priorMap.put(priorItem + "",priorInfo);
                    }
                }
                // 更新信息
                grpBPrior.put(grpB,priorMap);
                depPrior.put(dep,grpBPrior);
                oDates.set(i,oDate);
            }
        }
        // 遍历更新任务
        for (String s : updateTask.keySet()) {
            oTasks.set(Integer.parseInt(s),updateTask.getJSONObject(s));
        }
        // 遍历删除任务
        for (int i = delDateIndex.size()-1; i >= 0; i--) {
            int index = delDateIndex.getInteger(i);
            oDates.remove(index);
            oTasks.remove(index);
        }
        System.out.println("删除后输出:");
        System.out.println(JSON.toJSONString(oDates));
        System.out.println(JSON.toJSONString(oTasks));
        JSONObject result = new JSONObject();
        result.put("oDates",oDates);
        result.put("oTasks",oTasks);
        return result;
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
}
