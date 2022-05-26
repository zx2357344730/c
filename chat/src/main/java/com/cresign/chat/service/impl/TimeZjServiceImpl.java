package com.cresign.chat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.service.TimeZjService;
import com.cresign.chat.utils.Obj;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName TimeServiceImpl
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2021/12/20 11:10
 * @Version 1.0.0
 */
@Service
public class TimeZjServiceImpl implements TimeZjService {

    @Resource
    private CoupaUtil coupaUtil;
    /**
     * 存储当前唯一编号的第一个当前时间戳
     */
    public static final JSONObject onlyStartMap = new JSONObject();
    /**
     * 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     */
    public static final JSONObject onlyStartMapAndDep = new JSONObject();
    /**
     * 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     */
    public static final JSONObject onlyIsDs = new JSONObject();
    /**
     * 存储记录是否有未操作到的情况被处理
     */
    public static final List<String> jiLJ = new ArrayList<>();
    /**
     * 后端存储任务信息镜像
     */
    public static final Map<String,Map<String,Map<String,Map<Long,List<Task>>>>> tasksFAll = new HashMap<>();
    /**
     * 后端存储任务余剩时间镜像
     */
    public static final Map<String,Map<String,Map<String,Map<Long,Long>>>> zonFAll = new HashMap<>();
    /**
     * 统一随机数存储对象
     */
    public static final Map<String,String> randoms = new HashMap<>();
    /**
     * 订单id和订单index统一存储记录状态-根据random存储
     */
    public static final Map<String,Map<String,Map<String,Integer>>> storageReset = new HashMap<>();
    /**
     * 任务所在日期存储
     */
    public static final Map<String,Map<String,Map<String,Map<String,Integer>>>> teDateFAll = new HashMap<>();
    /**
     * 跳天强制停止参数
     */
    private Integer yiShu = 0;
    /**
     * 空插冲突强制停止参数
     */
    private Integer lei = 0;
    /**
     * isJ强制停止参数
     */
    private Integer xin = 0;
    /**
     * 强制停止出现后的记录参数
     */
    private Integer isQzTz = 0;
    /**
     * 定义存储当前时间处理的订单信息
     */
    public static final JSONObject randomAction = new JSONObject();

    /**
     * 任务最后处理方法
     * @param teDaF 时间冲突的副本
     * @param random 当前任务唯一参数
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 19:26
     */
    public void setZui(JSONObject teDaF,String random,String id_C){
        System.out.println();
        System.out.println("！！！-最后输出这里-！！！:");
        System.out.println();
//        System.out.println(JSON.toJSONString(teDaF));

        System.out.println("输出--是否有未操作到的情况被处理--");
        System.out.println(jiLJ.size()==0?"无输出":"有这种情况");
        for (int i = 0; i < jiLJ.size(); i++) {
            System.out.println(jiLJ.get(i)+"---"+i);
        }
        if (jiLJ.size() > 0) {
            System.out.println();
        }

        // 获取时间冲突副本的所有键（订单id）
        Set<String> strings1 = teDaF.keySet();
        // 遍历键（订单id）
        strings1.forEach(k -> {
            // 获取键（订单id）对应的值
            JSONObject jsonObject = teDaF.getJSONObject(k);
            // 根据键获取订单信息
            Order order = coupaUtil.getOrderByListKey(k, Collections.singletonList("action"));
            // 获取action卡片信息
            JSONObject action = order.getAction();
            // 获取订单的所有action递归信息
            JSONArray objAction = action.getJSONArray("objAction");
            // 获取对应键（订单id）的所有键（index）
            Set<String> strings = jsonObject.keySet();
            // 遍历键（index）
            strings.forEach(v -> {
                // 根据键（index）获取递归所在时间
                JSONObject jsonObject1 = jsonObject.getJSONObject(v);
                // 根据键（index）获取递归信息
                JSONObject actZ = objAction.getJSONObject(Integer.parseInt(v));
                // 更新递归信息的所在时间
                actZ.put("teDate",jsonObject1);
                // 更新递归信息
                objAction.set(Integer.parseInt(v),actZ);
                action.put("objAction",objAction);
                order.setAction(action);
                coupaUtil.saveOrder(order);
            });
        });
        // 获取统一random对应的random唯一id，再根据唯一id获取任务所在时间对象
        Map<String, Map<String, Map<String,Integer>>> stringMapMap1 = teDateFAll.get(randoms.get(random));
        if (null != stringMapMap1) {
            // 获取所有键（订单id）
            Set<String> strings = stringMapMap1.keySet();
            // 遍历键（订单id）
            for (String string : strings) {
                // 根据键（订单id）获取信息
                Map<String, Map<String,Integer>> stringMapMap = stringMapMap1.get(string);
                // 获取键（index）
                Set<String> strings2 = stringMapMap.keySet();
                // 遍历键（index）
                for (String s : strings2) {
                    // 根据键（index）获取任务所在时间对象
                    Map<String,Integer> teDateF = stringMapMap.get(s);
                    // 根据键（订单id）获取订单信息
                    Order order = coupaUtil.getOrderByListKey(string, Collections.singletonList("action"));
                    // 获取订单action卡片信息
                    JSONObject action = order.getAction();
                    // 获取所有递归信息
                    JSONArray objAction = action.getJSONArray("objAction");
                    // 根据键（index）获取对应的递归信息
                    JSONObject actZ = objAction.getJSONObject(Integer.parseInt(s));
                    // 更新信息
                    actZ.put("teDate",teDateF);
                    objAction.set(Integer.parseInt(s),actZ);
                    action.put("objAction",objAction);
                    order.setAction(action);
                    coupaUtil.saveOrder(order);
                }
            }
        }

        if (null != tasksFAll.get(randoms.get(random))) {
            // 部门，组别，当天
            // 获取统一random对应的random唯一id，再根据唯一id获取任务信息镜像，再获取所有键（部门）
            Set<String> strings2 = tasksFAll.get(randoms.get(random)).keySet();
            // 遍历键（部门）
            strings2.forEach(k -> {
                // 根据键（部门）获取组别对应的任务信息
                Map<String, Map<Long, List<Task>>> stringMapMap = tasksFAll.get(randoms.get(random)).get(k);
                // 根据键（部门）获取组别任务当天余剩时间
                Map<String, Map<Long, Long>> stringMapMapZ = zonFAll.get(randoms.get(random)).get(k);
                // 遍历键（组别）
                stringMapMap.keySet().forEach(v->{
                    // 根据键（组别）获取当前任务信息
                    Map<Long, List<Task>> longListMap = stringMapMap.get(v);
                    // 根据键（组别）获取任务当天余剩时间
                    Map<Long, Long> longLongMapZ = stringMapMapZ.get(v);
                    // 遍历键（当前）
                    longListMap.keySet().forEach(z->{
                        // 获取当前任务信息
                        List<Task> tasks = longListMap.get(z);
                        // 根据键（当天）获取当天任务余剩时间
                        Long zon = longLongMapZ.get(z);
                        setTasksAndZon(tasks,v,k,z,id_C,false,zon,coupaUtil);
                    });
                });
            });
        }

//        String assetId = coupaUtil.getAssetId("6076a1c7f3861e40c87fd294", "a-chkin");
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
        JSONObject chkin00s = asset.getChkin00s();
        System.out.println();
        System.out.println("排序前-Tasks:");
        System.out.println(JSON.toJSONString(chkin00s.getJSONObject("objTask")));
        System.out.println();

        // 判断是否有出息强制停止
        if (isQzTz == 1) {
            System.out.println("-----出现强制停止-----");
            System.out.println();
        }
    }

    public JSONObject getTasksAndZon(Long teStart,String grpB,String dep,String id_C){
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
//        System.out.println("获取任务集合:"+teStart+" - "+grpB+" - "+dep);
        if (null == asset) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,id_C,false,28800L,coupaUtil);
            return null;
        }
        JSONObject chkin00s = asset.getChkin00s();
        if (null == chkin00s) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,id_C,false,28800L,coupaUtil);
            return null;
        }
        JSONObject objTasks = chkin00s.getJSONObject("objTask");
        if (null == objTasks) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,id_C,false,28800L,coupaUtil);
            return null;
        }
        JSONObject objDep = objTasks.getJSONObject(dep);
        if (null == objDep) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,id_C,false,28800L,coupaUtil);
            return null;
        }
        JSONObject objGrp = objDep.getJSONObject(grpB);
        if (null == objGrp) {
            setTasksAndZon(new ArrayList<>(),grpB,dep,teStart,id_C,false,28800L,coupaUtil);
            return null;
        }
        return objGrp.getJSONObject(teStart + "");
    }

    /**
     * 根据teStart，grpB，dep，random获取镜像任务信息方法
     * @param teS	当前时间戳
     * @param grpBNext	组别
     * @param depNext	部门
     * @param random	当前唯一编号
     * @return java.util.List<com.cresign.tools.pojo.po.chkin.Task>  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:08
     */
    private List<Task> getTasksF(Long teS,String grpBNext,String depNext,String random){
        // 根据random获取唯一编号集合对应的random，再获取镜像任务信息
        Map<String, Map<String, Map<Long, List<Task>>>> tf = tasksFAll.computeIfAbsent(randoms.get(random), k -> new HashMap<>());
        // 定义任务集合
        List<Task> tasks;
        // 判断获取部门任务为空
        if (null == tf.get(depNext)) {
            // 创建部门任务
            Map<String,Map<Long,List<Task>>> stringMapMap = new HashMap<>();
            // 创建组别任务
            Map<Long,List<Task>> longListMap = new HashMap<>();
            // 创建一个新的任务集合
            tasks = new ArrayList<>();
            // 添加任务集合
            longListMap.put(teS,tasks);
            // 添加组别任务对象
            stringMapMap.put(grpBNext,longListMap);
            // 添加部门任务对象
            tf.put(depNext,stringMapMap);
        } else {
            // 根据部门获取部门任务
            Map<String, Map<Long, List<Task>>> stringMapMap = tf.get(depNext);
            // 判断组别任务为空
            if (null == stringMapMap.get(grpBNext)) {
                // 创建组别任务对象
                Map<Long,List<Task>> longListMap = new HashMap<>();
                // 创建一个新的任务集合
                tasks = new ArrayList<>();
                // 添加任务集合
                longListMap.put(teS,tasks);
                // 添加组别任务对象
                stringMapMap.put(grpBNext,longListMap);
                // 添加部门任务对象
                tf.put(depNext,stringMapMap);
            } else {
                // 根据部门获取时间任务对象
                Map<Long, List<Task>> longListMap = stringMapMap.get(grpBNext);
                // 判断时间任务对象为空
                if (null == longListMap.get(teS)) {
                    // 创建一个新的任务集合
                    tasks = new ArrayList<>();
                    // 添加任务集合
                    longListMap.put(teS,tasks);
                    // 添加组别任务对象
                    stringMapMap.put(grpBNext,longListMap);
                    // 添加部门任务对象
                    tf.put(depNext,stringMapMap);
                } else {
                    // 根据当前时间获取任务集合
                    tasks = longListMap.get(teS);
                    // 返回任务集合
                    return tasks;
                }
            }
        }
        // 镜像任务对象添加任务
        tasksFAll.put(randoms.get(random),tf);
        // 返回任务集合
        return tasks;
    }

    /**
     * 根据teStart，grpB，dep，random获取镜像任务余剩时间信息方法
     * @param teS	当前时间戳
     * @param grpBNext	组别
     * @param depNext	部门
     * @param random	当前唯一编号
     * @return java.lang.Long  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:29
     */
    private Long getZonF(Long teS,String grpBNext,String depNext,String random){
        Map<String, Map<String, Map<Long, Long>>> zf = zonFAll.computeIfAbsent(randoms.get(random), k -> new HashMap<>());
        Long zon;
        if (null == zf.get(depNext)) {
            Map<String, Map<Long, Long>> stringMapMap = new HashMap<>();
            Map<Long, Long> longLongMap = new HashMap<>();
            zon = 28800L;
            longLongMap.put(teS,zon);
            stringMapMap.put(grpBNext,longLongMap);
            zf.put(depNext,stringMapMap);
        } else {
            Map<String, Map<Long, Long>> stringMapMap = zf.get(depNext);
            if (null == stringMapMap.get(grpBNext)) {
                Map<Long, Long> longLongMap = new HashMap<>();
                zon = 28800L;
                longLongMap.put(teS,zon);
                stringMapMap.put(grpBNext,longLongMap);
                zf.put(depNext,stringMapMap);
            } else {
                Map<Long, Long> longLongMap = stringMapMap.get(grpBNext);
                if (null == longLongMap.get(teS)) {
                    zon = 28800L;
                    longLongMap.put(teS,zon);
                    stringMapMap.put(grpBNext,longLongMap);
                    zf.put(depNext,stringMapMap);
                } else {
                    zon = longLongMap.get(teS);
                    return zon;
                }
            }
        }
        zonFAll.put(randoms.get(random),zf);
        return zon;
    }

    /**
     * 根据id_O，index，random获取统一id_O和index存储记录状态信息方法
     * @param id_O  订单编号
     * @param index 订单编号对应的下标
     * @param random    当前唯一编号
     * @return java.lang.Integer  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:30
     */
    private Integer getStorage(String id_O,Integer index,String random){
        // 创建返回结果并赋值，re == 0 正常状态存储、re == 1 冲突状态存储、re == 2 调用时间处理状态存储
        Integer re = 0;
        // 根据当前唯一编号获取统一唯一编号存储对应的父编号再获取id_O和index存储记录状态信息
        Map<String, Map<String, Integer>> stringMapMap = storageReset.computeIfAbsent(randoms.get(random),k->new HashMap<>());
        // 判断存储记录状态信息为空
        if (null == stringMapMap.get(id_O)) {
            // 创建存储记录状态信息
            Map<String, Integer> map = new HashMap<>();
            // 添加订单下标记录状态
            map.put(index.toString(),re);
            // 添加订单编号记录状态
            stringMapMap.put(id_O,map);
            // 添加当前唯一编号获取的对应唯一编号记录状态
            storageReset.put(randoms.get(random),stringMapMap);
        } else {
            // 根据订单编号获取存储记录状态信息
            Map<String, Integer> map = stringMapMap.get(id_O);
            // 判断存储记录状态信息为空
            if (null == map.get(index.toString())) {
                // 添加订单下标记录状态
                map.put(index.toString(),re);
                // 添加订单编号记录状态
                stringMapMap.put(id_O,map);
                // 添加当前唯一编号获取的对应唯一编号记录状态
                storageReset.put(randoms.get(random),stringMapMap);
            } else {
                // 赋值返回结果
                re = map.get(index.toString());
            }
        }
        return re;
    }

    /**
     * 根据id_O，index，random获取任务所在日期方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param random	当前唯一编号
     * @return java.util.List<java.lang.String>  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:37
     */
    private List<String> getTeDateF(String id_O,Integer index,String random){
        Map<String, Map<String, Map<String,Integer>>> stringMapMap = teDateFAll.computeIfAbsent(randoms.get(random), k -> new HashMap<>());
        List<String> re = new ArrayList<>();
        if (null == stringMapMap.get(id_O)) {
            Map<String, Map<String,Integer>> map = new HashMap<>();
            map.put(index.toString(),new HashMap<>());
            stringMapMap.put(id_O,map);
            teDateFAll.put(randoms.get(random),stringMapMap);
        } else {
            Map<String, Map<String,Integer>> map = stringMapMap.get(id_O);
            if (null == map.get(index.toString())) {
                map.put(index.toString(),new HashMap<>());
                stringMapMap.put(id_O,map);
                teDateFAll.put(randoms.get(random),stringMapMap);
            } else {
                re = new ArrayList<>(map.get(index.toString()).keySet());
            }
        }
//        System.out.println("获取镜像所在日期列表:-id_O:"+id_O+",-index:"+index);
        return re;
    }

    /**
     * 根据grpB，dep获取数据库职位总人数方法 - 已对接数据库
     * @param grpB	职位
     * @param dep	部门
     * @return java.lang.Integer  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:39
     */
    public Integer getObjGrpUNum(String grpB,String dep,String id_C){
//        System.out.println("获取职位总人数:"+grpB+"-"+dep+"- id_C:"+id_C);
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
        JSONObject chkin00s = asset.getChkin00s();
        JSONObject objSb = chkin00s.getJSONObject("objZw");
        JSONObject depObj = objSb.getJSONObject(dep);
        return depObj.getInteger(grpB);
    }

    private JSONObject getXbAndSb(String grpB,String dep,String id_C){
//        System.out.println("获取不上班打卡时间:"+teStart+"-"+grpB+"-"+dep+"- id_C:"+id_C);
        JSONObject re = new JSONObject();
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
        JSONObject chkin00s = asset.getChkin00s();
        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
        JSONObject objDep = objWorkTime.getJSONObject(dep);
        Integer objGrpB = objDep.getInteger(grpB);
        JSONArray objData = chkin00s.getJSONArray("objData");
        JSONObject jsObj = objData.getJSONObject(objGrpB);
        re.put("xb",jsObj.getJSONArray("objXb"));
        re.put("sb",jsObj.getJSONArray("objSb"));
        return re;
    }

    public static void setTasksAndZon(List<Task> tasks, String grpB, String dep, Long teStart, String id_C, boolean isD, Long zon, CoupaUtil coupaUtil){
//        System.out.println("写入数据库任务列表:"+zon+"-"+teStart+"-"+grpB+"-"+dep+"-,id_C:"+id_C);
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
        JSONObject chkin00s = asset.getChkin00s();
        JSONObject objTask = chkin00s.getJSONObject("objTask");
        if (null == objTask) {
            objTask = new JSONObject();
        }
        JSONObject objDep = objTask.getJSONObject(dep);
        if (null == objDep) {
            objDep = new JSONObject();
        }
        JSONObject objGrpB = objDep.getJSONObject(grpB);
        if (null == objGrpB) {
            objGrpB = new JSONObject();
        }
        if (isD) {
            objTask = new JSONObject();
            objDep = new JSONObject();
            objGrpB = new JSONObject();
        }
        JSONObject teSZ = new JSONObject();
        teSZ.put("tasks",tasks);
        teSZ.put("zon",zon);
        objGrpB.put(teStart+"",teSZ);
        objDep.put(grpB,objGrpB);
        objTask.put(dep,objDep);
        chkin00s.put("objTask",objTask);
        // 创建请求参数存储字典
        JSONObject mapKey = new JSONObject();
        // 添加请求参数
        mapKey.put("chkin00s",chkin00s);
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
    }

    /**
     * 写入镜像任务集合方法
     * @param tasks	任务集合
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param random	当前唯一编号
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:44
     */
    private void setTasksF(List<Task> tasks,String grpBNext,String depNext,Long teS,String random){
//        System.out.println("写入镜像任务列表:-dep:"+depNext+",-grp:"+grpBNext);
        Map<String, Map<String, Map<Long, List<Task>>>> tasksF = tasksFAll.computeIfAbsent(randoms.get(random), k -> new HashMap<>());
        if (null == tasksF.get(depNext)) {
            Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
            Map<Long, List<Task>> tasksFz2 = new HashMap<>();
            tasksFz2.put(teS,tasks);
            tasksFz.put(grpBNext,tasksFz2);
            tasksF.put(depNext,tasksFz);
        } else {
            Map<String,Map<Long,List<Task>>> tasksFz = tasksF.get(depNext);
            Map<Long, List<Task>> tasksFz2;
            if (null == tasksFz.get(grpBNext)) {
                tasksFz2 = new HashMap<>();
            } else {
                tasksFz2 = tasksFz.get(grpBNext);
            }
            List<Task> tasks1 = new ArrayList<>();
            for (Task task : tasks) {
                tasks1.add(Obj.getTaskY(task));
            }
            tasksFz2.put(teS,tasks1);
            tasksFz.put(grpBNext,tasksFz2);
            tasksF.put(depNext,tasksFz);
        }
        tasksFAll.put(randoms.get(random),tasksF);
    }

    /**
     * 写入镜像任务余剩时间方法
     * @param zon	任务余剩时间
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param random	当前唯一编号
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:46
     */
    private void setZonF(Long zon,String grpBNext,String depNext,Long teS,String random){
//        System.out.println("写入镜像总上班时间:"+zon);
        Map<String, Map<String, Map<Long, Long>>> zonF = zonFAll.computeIfAbsent(randoms.get(random), k -> new HashMap<>());
        if (null == zonF.get(depNext)) {
            Map<String, Map<Long, Long>> zonFz = new HashMap<>();
            Map<Long, Long> zonFz2 = new HashMap<>();
            zonFz2.put(teS,zon);
            zonFz.put(grpBNext,zonFz2);
            zonF.put(depNext,zonFz);
        } else {
            Map<String,Map<Long,Long>> zonFz = zonF.get(depNext);
            if (null == zonFz.get(grpBNext)) {
                Map<Long, Long> zonFz2 = new HashMap<>();
                zonFz2.put(teS,zon);
                zonFz.put(grpBNext,zonFz2);
                zonF.put(depNext,zonFz);
            } else {
                Map<Long,Long> zonFz2 = zonFz.get(grpBNext);
                zonFz2.put(teS,zon);
                zonFz.put(grpBNext,zonFz2);
                zonF.put(depNext,zonFz);
            }
        }
        zonFAll.put(randoms.get(random),zonF);
    }

    /**
     * 写入id_O，index和存储记录状态方法
     * @param num   状态
     * @param id_O  订单编号
     * @param index 订单编号对应的下标
     * @param random    当前唯一编号
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:46
     */
    private void setStorage(Integer num,String id_O,Integer index,String random){
//        System.out.println("写入镜像存储重置:"+num);
        Map<String, Map<String, Integer>> stringMapMap = storageReset.computeIfAbsent(randoms.get(random),k->new HashMap<>());
        if (null == stringMapMap.get(id_O)) {
            Map<String,Integer> map = new HashMap<>();
            map.put(index.toString(),num);
            stringMapMap.put(id_O,map);
        } else {
            Map<String, Integer> map = stringMapMap.get(id_O);
            map.put(index.toString(),num);
            stringMapMap.put(id_O,map);
        }
        storageReset.put(randoms.get(random),stringMapMap);
    }

    /**
     * 写入任务所在日期方法
     * @param id_O  订单编号
     * @param index 订单编号对应的下标
     * @param random    当前唯一编号
     * @param teS   当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:50
     */
    private void setTeDateF(String id_O, Integer index, String random, Long teS){
        Map<String, Map<String, Map<String,Integer>>> stringMapMap = teDateFAll.computeIfAbsent(randoms.get(random), k -> new HashMap<>());
        if (null == stringMapMap.get(id_O)) {
            Map<String, Map<String,Integer>> map = new HashMap<>();
            map.put(index.toString(),new HashMap<>());
            stringMapMap.put(id_O,map);
        } else {
            Map<String, Map<String,Integer>> map = stringMapMap.get(id_O);
            Map<String, Integer> map1 = map.get(index.toString());
            map1.put(teS.toString(),0);
            map.put(index.toString(),map1);
            stringMapMap.put(id_O,map);
        }
        teDateFAll.put(randoms.get(random),stringMapMap);
    }

    /**
     * 获取当前时间戳方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @return java.lang.Long  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/17 23:59
     */
    public Long getTeS(String random,String grpB,String dep){
        JSONObject rand;
        long getTe;
        if (null == onlyStartMapAndDep.getJSONObject(random)) {
            rand = new JSONObject();
            JSONObject onlyDep = new JSONObject();
            onlyDep.put(grpB,onlyStartMap.getLong(random));
            getTe = onlyStartMap.getLong(random);
            rand.put(dep,onlyDep);
            onlyStartMapAndDep.put(random,rand);
        } else {
            rand = onlyStartMapAndDep.getJSONObject(random);
            JSONObject onlyDep;
            if (null == rand.getJSONObject(dep)) {
                onlyDep = new JSONObject();
                onlyDep.put(grpB,onlyStartMap.getLong(random));
                getTe = onlyStartMap.getLong(random);
                rand.put(dep,onlyDep);
                onlyStartMapAndDep.put(random,rand);
            } else {
                onlyDep = rand.getJSONObject(dep);
                if (null == onlyDep.getLong(grpB)) {
                    onlyDep.put(grpB,onlyStartMap.getLong(random));
                    rand.put(dep,onlyDep);
                    onlyStartMapAndDep.put(random,rand);
                    getTe = onlyStartMap.getLong(random);
                } else {
                    getTe = onlyDep.getLong(grpB);
                }
            }
        }
        return getTe;
    }

    /**
     * 写入当前时间戳方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teS	当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 0:04
     */
    private void setTeS(String random,String grpB,String dep,Long teS){
        JSONObject rand;
        if (null == onlyStartMapAndDep.getJSONObject(random)) {
            rand = new JSONObject();
            JSONObject onlyDep = new JSONObject();
            onlyDep.put(grpB,teS);
            rand.put(dep,onlyDep);
            onlyStartMapAndDep.put(random,rand);
        } else {
            rand = onlyStartMapAndDep.getJSONObject(random);
            JSONObject onlyDep;
            if (null == rand.getJSONObject(dep)) {
                onlyDep = new JSONObject();
                onlyDep.put(grpB,teS);
                rand.put(dep,onlyDep);
                onlyStartMapAndDep.put(random,rand);
            } else {
                onlyDep = rand.getJSONObject(dep);
                if (null == onlyDep.getLong(grpB)) {
                    onlyDep.put(grpB,teS);
                    rand.put(dep,onlyDep);
                    onlyStartMapAndDep.put(random,rand);
                }
            }
        }
    }

    /**
     * 获取任务综合信息方法
     * @param random    当前唯一编号
     * @param grpB  组别
     * @param dep   部门
     * @param is    is = 0 使用random，grpB，dep获取当前时间戳、is = 1 使用teS为时间戳
     * @param teS   当前时间戳
     * @param isC   isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 0:04
     */
    private Map<String,Object> getJumpDay(String random,String grpB,String dep,int is,Long teS
            ,Integer isC,String id_C){
        // 定义返回对象
        Map<String,Object> re;
        // 定义任务集合
        List<Task> tasks;
        // 定义任务余剩时间
        long zon;
        // 判断是使用random，grpB，dep获取当前时间戳
        if (is == 0) {
            // 判断是获取数据库任务信息
            if (isC == 0) {
//                System.out.println("进入jump-1:");
                JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep), grpB, dep, id_C);
                if (null == tasksAndZon) {
                    tasks = new ArrayList<>();
                    zon = 28800L;
                } else {
                    JSONArray tasks1 = tasksAndZon.getJSONArray("tasks");
                    List<Task> finalTasks = new ArrayList<>();
                    tasks1.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                    tasks = new ArrayList<>(finalTasks);
                    zon = tasksAndZon.getLong("zon");
                }
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 创建返回对象
                    re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                    // 添加返回值，任务余剩时间
                    re.put("zon",zon);
                    // 添加返回值，任务集合
                    re.put("tasks",tasks);
                } else {
                    // 调用方法获取返回对象信息
                    re = getChkInJumpDay1(random,grpB,dep,id_C);
                }
            } else {
//                System.out.println("进入jump-2:");
                // 创建返回对象
                re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                // 调用获取镜像任务信息获取任务集合
                tasks = getTasksF(getTeS(random,grpB,dep),grpB,dep,random);
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 调用获取镜像任务余剩时间获取任务余剩时间
                    zon = getZonF(getTeS(random,grpB,dep),grpB,dep,random);
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep), grpB, dep, id_C);
                    List<Task> tasks1;
                    if (null == tasksAndZon) {
                        tasks1 = new ArrayList<>();
                        zon = 28800L;
                    } else {
                        JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                        List<Task> finalTasks = new ArrayList<>();
                        tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                        tasks1 = new ArrayList<>(finalTasks);
                        zon = tasksAndZon.getLong("zon");
                    }
                    // 判断任务集合不为空
                    if (tasks1.size() != 0) {
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        // 深度拷贝tasks1数组到tasks
                        CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                        Collections.copy(tasks,tasks1);
                    } else {
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C);
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
                        zon = getZonF(getTeS(random,grpB,dep),grpB,dep,random);
//                        // 强制设置任务余剩时间为0，仅为测试
//                        zon = 0L;
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        // 遍历上班时间
                        for (int i = 0; i < chkIn.size(); i++) {
                            // 根据下标i获取上班时间段
                            JSONObject jsonObject = chkIn.getJSONObject(i);
                            // 获取上班时间的所有时间并累加到任务余剩时间
                            zon += jsonObject.getLong("zon");
                        }
                        // 任务集合添加任务信息
                        tasks.add(Obj.getTask(getTeS(random,grpB,dep),getTeS(random,grpB,dep),"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
                        // 遍历不上班时间
                        for (int i = 0; i < offWork.size(); i++) {
                            // 根据下标i获取不上班时间段
                            JSONObject jsonObject = offWork.getJSONObject(i);
                            // 任务集合添加任务信息
                            tasks.add(Obj.getTask((getTeS(random,grpB,dep) + jsonObject.getLong("tePStart"))
                                    , (getTeS(random,grpB,dep) + jsonObject.getLong("tePFinish"))
                                    , "", -1, jsonObject.getLong("zon")
                                    , jsonObject.getInteger("priority"), "电脑", 0L,0L,id_C
                                    ,(getTeS(random,grpB,dep) + jsonObject.getLong("tePStart")),0L));
                        }
                    }
                }
                // 设置返回对象信息
                re.put("zon",zon);
                re.put("tasks",tasks);
            }
        } else {
            // 判断是获取数据库任务信息
            if (isC == 0) {
//                System.out.println("进入jump-3:");
                JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C);
                if (null == tasksAndZon) {
                    // 调用获取数据库任务信息获取任务集合
                    tasks = new ArrayList<>();
                    zon = 28800L;
                } else {
                    JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                    List<Task> finalTasks = new ArrayList<>();
                    tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                    tasks = new ArrayList<>(finalTasks);
                    zon = tasksAndZon.getLong("zon");
                }
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 创建返回对象
                    re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                    // 添加返回值
                    re.put("zon",zon);
                    re.put("tasks",tasks);
                } else {
                    // 调用方法获取返回对象信息
                    re = getChkInJumpDay2(teS, grpB, dep,id_C);
                }
            } else {
//                System.out.println("进入jump-4:");
                // 创建返回对象
                re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                // 调用获取镜像任务信息获取任务集合
                tasks = getTasksF(teS,grpB,dep,random);
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 调用获取镜像任务余剩时间获取任务余剩时间
                    zon = getZonF(teS,grpB,dep,random);
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C);
                    List<Task> tasks1 = new ArrayList<>();
                    if (null == tasksAndZon) {
                        zon = 28800L;
                    } else {
                        JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                        List<Task> finalTasks = new ArrayList<>();
                        tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                        tasks1 = new ArrayList<>(finalTasks);
                        zon = tasksAndZon.getLong("zon");
                    }
                    // 判断任务集合不为空
                    if (tasks1.size() != 0) {
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        // 深度拷贝tasks1数组到tasks
                        CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                        Collections.copy(tasks,tasks1);
                    } else {
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C);
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
                        zon = getZonF(teS,grpB,dep,random);
//                        // 强制设置任务余剩时间为0，仅为测试
//                        zon = 0L;
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        // 遍历上班时间
                        for (int i = 0; i < chkIn.size(); i++) {
                            // 根据下标i获取上班时间段
                            JSONObject jsonObject = chkIn.getJSONObject(i);
                            // 获取上班时间的所有时间并累加到任务余剩时间
                            zon += jsonObject.getLong("zon");
                        }
                        // 任务集合添加任务信息
                        tasks.add(Obj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
                        // 遍历不上班时间
                        for (int i = 0; i < offWork.size(); i++) {
                            // 根据下标i获取不上班时间段
                            JSONObject jsonObject = offWork.getJSONObject(i);
                            // 任务集合添加任务信息
                            tasks.add(Obj.getTask((teS + jsonObject.getLong("tePStart"))
                                    , (teS + jsonObject.getLong("tePFinish"))
                                    , "", -1, jsonObject.getLong("zon")
                                    , jsonObject.getInteger("priority"), "电脑", 0L,0L,id_C,(teS + jsonObject.getLong("tePStart")),0L));
                        }
                    }
                }
                // 设置返回对象信息
                re.put("zon",zon);
                re.put("tasks",tasks);
            }
        }
        // 返回结果
        return re;
    }

    /**
     * 根据(random,grpB,dep获取当前时间戳)当前时间戳获取数据库全新的的任务综合信息方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 0:58
     */
    private Map<String,Object> getChkInJumpDay1(String random,String grpB,String dep,String id_C){
        // 创建返回对象
        Map<String,Object> re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 定义任务余剩时间
        long zon;
        // 定义任务集合
        List<Task> tasks;
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C);
        // 调用获取上班时间
        JSONArray chkIn = xbAndSb.getJSONArray("sb");
        // 调用获取不上班时间
        JSONArray offWork = xbAndSb.getJSONArray("xb");
        // 强制设置任务余剩时间为0，仅为测试
        zon = 0L;
        // 创建任务集合
        tasks = new ArrayList<>();
        // 遍历上班时间
        for (int i = 0; i < chkIn.size(); i++) {
            // 根据下标i获取上班时间段
            JSONObject jsonObject = chkIn.getJSONObject(i);
            // 获取上班时间的所有时间并累加到任务余剩时间
            zon += jsonObject.getLong("zon");
        }
        // 任务集合添加任务信息
        tasks.add(Obj.getTask(getTeS(random,grpB,dep),getTeS(random,grpB,dep),"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        // 遍历不上班时间
        for (int i = 0; i < offWork.size(); i++) {
            // 根据下标i获取不上班时间段
            JSONObject jsonObject = offWork.getJSONObject(i);
            // 任务集合添加任务信息
            tasks.add(Obj.getTask((getTeS(random,grpB,dep) + jsonObject.getLong("tePStart"))
                    , (getTeS(random,grpB,dep) + jsonObject.getLong("tePFinish"))
                    , "", -1, jsonObject.getLong("zon")
                    , jsonObject.getInteger("priority"), "电脑", 0L,0L,id_C,(getTeS(random,grpB,dep) + jsonObject.getLong("tePStart")),0L));
        }
        // 添加返回信息
        re.put("zon",zon);
        re.put("tasks",tasks);
        // 返回结果
        return re;
    }

    /**
     * 根据teS获取数据库全新的的任务综合信息方法
     * @param teS	当前时间戳
     * @param grpB	组别
     * @param dep	部门
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 1:07
     */
    private Map<String,Object> getChkInJumpDay2(Long teS,String grpB,String dep,String id_C){
        // 创建返回对象
        Map<String,Object> re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 定义任务余剩时间
        long zon;
        // 定义任务集合
        List<Task> tasks;
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C);
        // 调用获取上班时间
        JSONArray chkIn = xbAndSb.getJSONArray("sb");
        // 调用获取不上班时间
        JSONArray offWork = xbAndSb.getJSONArray("xb");
        // 强制设置任务余剩时间为0，仅为测试
        zon = 0L;
        // 创建任务集合
        tasks = new ArrayList<>();
        // 遍历上班时间
        for (int i = 0; i < chkIn.size(); i++) {
            // 根据下标i获取上班时间段
            JSONObject jsonObject = chkIn.getJSONObject(i);
            // 获取上班时间的所有时间并累加到任务余剩时间
            zon += jsonObject.getLong("zon");
        }
        // 任务集合添加任务信息
        tasks.add(Obj.getTask(teS,teS,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        // 遍历不上班时间
        for (int i = 0; i < offWork.size(); i++) {
            // 根据下标i获取不上班时间段
            JSONObject jsonObject = offWork.getJSONObject(i);
            // 任务集合添加任务信息
            tasks.add(Obj.getTask((teS + jsonObject.getLong("tePStart"))
                    , (teS + jsonObject.getLong("tePFinish"))
                    , "", -1, jsonObject.getLong("zon")
                    , jsonObject.getInteger("priority"), "电脑", 0L,0L,id_C,(teS + jsonObject.getLong("tePStart")),0L));
        }
        // 添加返回信息
        re.put("zon",zon);
        re.put("tasks",tasks);
        return re;
    }

    /**
     * 获取冲突处理方法，原方法
     * @param id_oNext	当前处理订单编号
     * @param indONext	当前处理订单编号对应的下标
     * // @param yTeFin	最后结束时间戳
     * // @param wrdNs	当前操作用户名
     * @param teDaF	当前任务所在时间
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param isR	isR = 0 不是空插处理、isR = 1 是空插处理
     * @param lei	累加的每个任务时间戳
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param randomJ	旧的当前唯一编号
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 1:12
     */
    private JSONObject getBcCtH(String id_oNext,Integer indONext,JSONObject teDaF
            ,Integer isC,Integer isR,Long lei,JSONObject sho,String randomJ,String id_C,int csSta){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
//        System.out.println("yTeFin:"+yTeFin);
//        System.out.println("id_oNext:"+id_oNext);
//        System.out.println("indONext:"+indONext);
////        System.out.println("lei:"+lei);
////        System.out.println("danLei:"+danLei);
//        System.out.println("isR:"+isR);
//        System.out.println("isC:"+isC);
        // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
        int isPd = 0;
        // 判断当前处理订单编号为空
        if (null == id_oNext) {
            // 设置问题为订单编号为空
            isPd = 1;
            // 添加返回信息
            re.put("lei",0L);
            re.put("isPd",isPd);
            return re;
        }
//        System.out.println(JSON.toJSONString(sho));
//        System.out.println(JSON.toJSONString(tasksFAll.get(randoms.get(randomJ))));
        // 根据订单编号获取当前产品冲突信息
        JSONObject jsonObject2 = sho.getJSONObject(id_oNext);
//        System.out.println(JSON.toJSONString(jsonObject2));
        // 根据订单编号对应的下标获取当前产品冲突信息
        JSONObject jsonObject3 = jsonObject2.getJSONObject(indONext.toString());
        // 判断产品状态不为当前递归产品
        if (jsonObject3.getInteger("zOk") != -1) {
            // 获取主产品信息
            String z = jsonObject3.getString("z");
            // 将主产品信息进行拆分
            String[] split = z.split("\\+");
            // 获取主产品下标并判断下标为0
            if (split[1].equals("0")) {
                // 设置下标为1
                split[1] = "1";
            }
            // 判断主产品订单编号等于当前订单编号，并且订单下标等于当前订单下标
            if (split[0].equals(id_oNext) && split[1].equals(indONext.toString())) {
                // 设置不是空插处理
                isR = 0;
//                System.out.println("重置所有镜像时间信息:");
                // 重置所有任务所在日期
                teDateFAll.put(randoms.get(randomJ),new HashMap<>());
            } else {
                // 设置是空插处理
                isR = 1;
            }
        }
        // 获取订单递归信息
        JSONArray objActionNext = randomAction.getJSONObject(randoms.get(randomJ)).getJSONArray(id_oNext);
        if (null == objActionNext) {
//            System.out.println("为空-获取数据库:"+id_oNext);
            // 根据当前订单编号获取订单信息
            Order orderNext = coupaUtil.getOrderByListKey(id_oNext, Collections.singletonList("action"));
            // 获取订单递归信息
            objActionNext = orderNext.getAction().getJSONArray("objAction");
            JSONObject oMap = randomAction.getJSONObject(randoms.get(randomJ));
            oMap.put(id_oNext,objActionNext);
            randomAction.put(randoms.get(randomJ),oMap);
        }
        // 根据当前订单下标获取订单递归信息
        JSONObject actZNext = objActionNext.getJSONObject(indONext);
        // 判断订单递归状态等于5（主部件）
        if (actZNext.getInteger(Constants.GET_BM_D_PT) == Constants.INT_FIVE) {
//            System.out.println("-是-主生产部件停止-");
            // 设置问题为主生产部件
            isPd = 2;
            // 添加返回信息
            re.put("lei",0L);
            re.put("isPd",isPd);
//            System.out.println(JSON.toJSONString(re));
            return re;
        }
        // 获取当前订单部门
        String depNext = actZNext.getString("dep");
        // 获取当前订单组别
        String grpBNext = actZNext.getString("grpB");
        // 定义存储当前递归信息的下一个产品订单编号或父产品订单编号
        String id_OY;
        // 定义存储当前递归信息的下一个产品订单下标或父产品订单下标
        Integer indOY;
        // 调用获取任务所在日期
        List<String> p = getTeDateF(id_oNext,indONext,randomJ);
        // 判断任务所在日期为空
        if (p.size() == 0) {
            // 获取当前递归信息的任务所在日期
            JSONObject teDateNext = actZNext.getJSONObject("teDate");
//            System.out.println("teDateNext:");
//            System.out.println(JSON.toJSONString(teDateNext));
            // 获取任务所在日期键
            Set<String> strings = teDateNext.keySet();
            // 转换集合
            p = new ArrayList<>(strings);
        }
        // 将任务所在日期键进行升序排序
        p.sort(Comparator.naturalOrder());
        // 设置任务信息为空
        Task task = null;
        // isJr = 0 获取任务所有信息、isJr = 1 获取任务总时间进行累加
        int isJr = 0;
        // 遍历任务所在日期键
        for (String pz : p) {
            // 转换键为当前时间戳
            long teS = Long.parseLong(pz);
            // 定义任务余剩时间
            Long zon;
            // 定义任务集合
            List<Task> tasks;
            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 0) {
                // isR = 0 不是空插处理、isR = 1 是空插处理
                if (isR == 0) {
                    // 创建任务集合
                    tasks = new ArrayList<>();
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C);
                    List<Task> tasks1 = new ArrayList<>();
                    if (null == tasksAndZon) {
                        zon = 28800L;
                    } else {
                        JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                        List<Task> finalTasks = new ArrayList<>();
                        tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                        tasks1 = new ArrayList<>(finalTasks);
                        zon = tasksAndZon.getLong("zon");
                    }
                    // 判断任务集合为空
                    if (tasks1.size() == 0){continue;}
                    // 深度拷贝tasks1数组到tasks
                    CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                    Collections.copy(tasks,tasks1);
                } else {
                    // 调用获取镜像任务集合方法
                    tasks = getTasksF(teS,grpBNext,depNext,randomJ);
                    // 判断任务集合不等于空
                    if (tasks.size() != 0) {
                        // 调用获取镜像任务余剩时间方法
                        zon = getZonF(teS,grpBNext,depNext,randomJ);
                    } else {
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C);
                        List<Task> tasks1 = new ArrayList<>();
                        if (null == tasksAndZon) {
                            zon = 28800L;
                        } else {
                            JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                            List<Task> finalTasks = new ArrayList<>();
                            tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                            tasks1 = new ArrayList<>(finalTasks);
                            zon = tasksAndZon.getLong("zon");
                        }
                        // 判断任务集合为空
                        if (tasks1.size() == 0){continue;}
                        // 深度拷贝tasks1数组到tasks
                        CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                        Collections.copy(tasks,tasks1);
                    }
                }
            } else {
                // 调用获取镜像任务集合方法
                tasks = getTasksF(teS,grpBNext,depNext,randomJ);
                // 判断任务集合不等于空
                if (tasks.size() != 0) {
                    // 调用获取镜像任务余剩时间方法
                    zon = getZonF(teS,grpBNext,depNext,randomJ);
                } else {
                    // 创建任务集合
                    tasks = new ArrayList<>();
                    // 调用获取数据库任务集合方法
                    List<Task> tasks1 = new ArrayList<>();
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C);
                    if (null == tasksAndZon) {
                        zon = 28800L;
                    } else {
                        JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                        List<Task> finalTasks = new ArrayList<>();
                        tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                        tasks1 = new ArrayList<>(finalTasks);
                        zon = tasksAndZon.getLong("zon");
                    }
                    // 判断任务集合为空
                    if (tasks1.size() == 0){continue;}
                    // 深度拷贝tasks1数组到tasks
                    CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                    Collections.copy(tasks,tasks1);
                }
            }
            // 定义存储循环下标集合
            List<Integer> indexS = new ArrayList<>();
            // 遍历任务集合
            for (int i = 0; i < tasks.size(); i++) {
                // 根据i（下标）获取任务信息
                Task task1 = tasks.get(i);
                // 判断任务信息订单编号等于当前订单编号，并且任务信息订单下标等于当前订单下标
                if (task1.getId_O().equals(id_oNext) && task1.getIndex().equals(indONext)) {
                    // 添加当前循环下标
                    indexS.add(i);
                    // 累加任务总时间
                    zon += task1.getTeDurTotal();
                    // isJr = 0 获取任务所有信息、isJr = 1 获取任务总时间进行累加
                    if (isJr == 0) {
                        isJr = 1;
                        // 赋值任务信息
                        task = Obj.getTaskY(task1);
//                        // 设置任务延迟总时间（（最后时间+累加时间）-任务开始时间）
//                        task.setTeDelayDate(((yTeFin+lei) - task.getTePStart()));
//                        System.out.println("这里输出task-带延迟时间-1:");
//                        System.out.println(JSON.toJSONString(task));
                    }
//                    else {
////                        System.out.println("这里输出task-带延迟时间-2:");
//                        // 设置任务总共时间（当前任务总时间+循环任务总时间）
//                        task.setTeDurTotal(task.getTeDurTotal()+task1.getTeDurTotal());
//                    }
                }
            }
            // 升序排序循环存储下标
            indexS.sort(Comparator.reverseOrder());
            // 遍历循环存储下标
            for (Integer integer : indexS) {
                // 任务集合删除循环存储下标对应的任务信息
                tasks.remove(integer==null?0:integer);
            }
//            System.out.println("删除后的tasks1:");
//            System.out.println(JSON.toJSONString(tasks));
            // isR = 0 不是空插处理、isR = 1 是空插处理
            if (isR == 0) {
                // 创建当前唯一编号的镜像任务信息对象
                Map<String, Map<String, Map<Long, List<Task>>>> tasksF = new HashMap<>();
                // 创建任务部门对象
                Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
                // 创建任务组别对象
                Map<Long, List<Task>> tasksFz2 = new HashMap<>();
                // 添加任务集合
                tasksFz2.put(teS,tasks);
                // 添加任务组别对象
                tasksFz.put(grpBNext,tasksFz2);
                // 添加任务部门对象
                tasksF.put(depNext,tasksFz);
                // 添加唯一编号镜像任务对象
                tasksFAll.put(randoms.get(randomJ),tasksF);

                // 创建当前唯一编号的镜像任务余剩时间对象
                Map<String, Map<String, Map<Long, Long>>> zonF = new HashMap<>();
                // 创建任务余剩时间部门对象
                Map<String, Map<Long, Long>> zonFz = new HashMap<>();
                // 创建任务余剩时间组别对象
                Map<Long, Long> zonFz2 = new HashMap<>();
                // 添加任务余剩时间
                zonFz2.put(teS,zon);
                // 添加任务余剩时间组别对象
                zonFz.put(grpBNext,zonFz2);
                // 添加任务余剩时间部门对象
                zonF.put(depNext,zonFz);
                // 添加当前唯一编号的镜像任务余剩时间对象
                zonFAll.put(randoms.get(randomJ),zonF);

                // 重置id_O，index统一的存储状态
                Map<String,Map<String,Integer>> stoZ = new HashMap<>();
                // 添加id_O，index重置状态信息
                storageReset.put(randoms.get(randomJ),stoZ);
            } else {
                // 调用写入任务余剩时间镜像方法
                setZonF(zon,grpBNext,depNext,teS,randomJ);
                // 调用写入任务集合镜像方法
                setTasksF(tasks,grpBNext,depNext,teS,randomJ);
            }
        }
//        System.out.println("这里输出task-带延迟加所有时间:");
//        System.out.println(JSON.toJSONString(task));
        // 保存是否出现任务为空异常:isYx = 0 正常操作未出现异常、isYx = 1 出现拿出任务为空异常镜像|数据库
        int isYx = 0;
        // 判断任务信息为空
        if (null == task) {
            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 1) {
//                System.out.println("出现拿出任务为空异常-进入:");
                // isJr = 0 获取任务所有信息、isJr = 1 获取任务总时间进行累加
                isJr = 0;
                // 遍历任务所在日期键
                for (String pz : p) {
                    // 转换键为当前时间戳
                    long teS = Long.parseLong(pz);
                    // 定义任务余剩时间
                    Long zon;
                    // 定义任务集合
                    List<Task> tasks = new ArrayList<>();
                    // 调用获取数据库任务集合方法
                    List<Task> tasks1 = new ArrayList<>();
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C);
                    if (null == tasksAndZon) {
                        zon = 28800L;
                    } else {
                        JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                        List<Task> finalTasks = new ArrayList<>();
                        tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                        tasks1 = new ArrayList<>(finalTasks);
                        zon = tasksAndZon.getLong("zon");
                    }
                    // 深度拷贝tasks1数组到tasks
                    CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                    Collections.copy(tasks,tasks1);
                    // 定义存储循环下标集合
                    List<Integer> indexS = new ArrayList<>();
                    // 遍历任务集合
                    for (int i = 0; i < tasks.size(); i++) {
                        // 根据i（下标）获取任务信息
                        Task task1 = tasks.get(i);
                        // 判断任务信息订单编号等于当前订单编号，并且任务信息订单下标等于当前订单下标
                        if (task1.getId_O().equals(id_oNext) && task1.getIndex().equals(indONext)) {
                            // 添加当前循环下标
                            indexS.add(i);
                            // 累加任务总时间
                            zon += task1.getTeDurTotal();
                            // isJr = 0 获取任务所有信息、isJr = 1 获取任务总时间进行累加
                            if (isJr == 0) {
                                isJr = 1;
                                // 赋值任务信息
                                task = Obj.getTaskY(task1);
//                                // 设置任务延迟总时间（（最后时间+累加时间）-任务开始时间）
//                                task.setTeDelayDate(((yTeFin+lei) - task.getTePStart()));
//                                System.out.println("这里输出task-带延迟时间-1-2:");
//                                System.out.println(JSON.toJSONString(task));
                            }
//                            else {
//                                // 设置任务总共时间（当前任务总时间+循环任务总时间）
//                                task.setTeDurTotal(task.getTeDurTotal()+task1.getTeDurTotal());
//                            }
                        }
                    }
                    // 升序排序循环存储下标
                    indexS.sort(Comparator.reverseOrder());
                    // 遍历循环存储下标
                    for (Integer integer : indexS) {
                        // 任务集合删除循环存储下标对应的任务信息
                        tasks.remove(integer==null?0:integer);
                    }
//                    System.out.println("删除后的tasks1-2:");
//                    System.out.println(JSON.toJSONString(tasks));
                    // isR = 0 不是空插处理、isR = 1 是空插处理
                    if (isR == 0) {
                        // 创建当前唯一编号的镜像任务信息对象
                        Map<String, Map<String, Map<Long, List<Task>>>> tasksF = new HashMap<>();
                        // 创建任务部门对象
                        Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
                        // 创建任务组别对象
                        Map<Long, List<Task>> tasksFz2 = new HashMap<>();
                        // 添加任务集合
                        tasksFz2.put(teS,tasks);
                        // 添加任务组别对象
                        tasksFz.put(grpBNext,tasksFz2);
                        // 添加任务部门对象
                        tasksF.put(depNext,tasksFz);
                        // 添加唯一编号镜像任务对象
                        tasksFAll.put(randoms.get(randomJ),tasksF);

                        // 创建当前唯一编号的镜像任务余剩时间对象
                        Map<String, Map<String, Map<Long, Long>>> zonF = new HashMap<>();
                        // 创建任务余剩时间部门对象
                        Map<String, Map<Long, Long>> zonFz = new HashMap<>();
                        // 创建任务余剩时间组别对象
                        Map<Long, Long> zonFz2 = new HashMap<>();
                        // 添加任务余剩时间
                        zonFz2.put(teS,zon);
                        // 添加任务余剩时间组别对象
                        zonFz.put(grpBNext,zonFz2);
                        // 添加任务余剩时间部门对象
                        zonF.put(depNext,zonFz);
                        // 添加当前唯一编号的镜像任务余剩时间对象
                        zonFAll.put(randoms.get(randomJ),zonF);

                        // 重置id_O，index统一的存储状态
                        Map<String,Map<String,Integer>> stoZ = new HashMap<>();
                        // 添加id_O，index重置状态信息
                        storageReset.put(randoms.get(randomJ),stoZ);
                    } else {
                        // 调用写入任务余剩时间镜像方法
                        setZonF(zon,grpBNext,depNext,teS,randomJ);
                        // 调用写入任务集合镜像方法
                        setTasksF(tasks,grpBNext,depNext,teS,randomJ);
                    }
                }
//                System.out.println("这里输出task-带延迟加所有时间-2:");
//                System.out.println(JSON.toJSONString(task));
            }
            else {
                // isYx = 1 出现拿出任务为空异常镜像|数据库
                isYx = 1;
                // 根据订单编号获取当前产品冲突信息
                JSONObject jsonObject = sho.getJSONObject(id_oNext);
                // 根据订单编号对应的下标获取当前产品冲突信息
                JSONObject jsonObject1 = jsonObject.getJSONObject(indONext.toString());
                // 判断产品状态是第一个当前递归产品
                if (jsonObject1.getInteger("zOk") == 1) {
//                    System.out.println("出现拿出任务为空异常-进入-1:");
                    // isJr = 0 获取任务所有信息、isJr = 1 获取任务总时间进行累加
                    isJr = 0;
                    // 遍历任务所在日期键
                    for (String pz : p) {
                        // 转换键为当前时间戳
                        long teS = Long.parseLong(pz);
                        // 定义任务余剩时间
                        Long zon;
                        // 定义任务集合
                        List<Task> tasks = new ArrayList<>();
                        // 调用获取数据库任务集合方法
                        List<Task> tasks1 = new ArrayList<>();
                        JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C);
                        if (null == tasksAndZon) {
                            zon = 28800L;
                        } else {
                            JSONArray tasks2 = tasksAndZon.getJSONArray("tasks");
                            List<Task> finalTasks = new ArrayList<>();
                            tasks2.forEach(t -> finalTasks.add(JSONObject.parseObject(JSON.toJSONString(t),Task.class)));
                            tasks1 = new ArrayList<>(finalTasks);
                            zon = tasksAndZon.getLong("zon");
                        }
                        // 深度拷贝tasks1数组到tasks
                        CollectionUtils.addAll(tasks, new Object[tasks1.size()]);
                        Collections.copy(tasks,tasks1);
//                        System.out.println("tasks1-3:");
//                        System.out.println(JSON.toJSONString(tasks));
                        // 定义存储循环下标集合
                        List<Integer> indexS = new ArrayList<>();
                        // 遍历任务集合
                        for (int i = 0; i < tasks.size(); i++) {
                            // 根据i（下标）获取任务信息
                            Task task1 = tasks.get(i);
                            // 判断任务信息订单编号等于当前订单编号，并且任务信息订单下标等于当前订单下标
                            if (task1.getId_O().equals(id_oNext) && task1.getIndex().equals(indONext)) {
                                // 添加当前循环下标
                                indexS.add(i);
                                // 累加任务总时间
                                zon += task1.getTeDurTotal();
                                // isJr = 0 获取任务所有信息、isJr = 1 获取任务总时间进行累加
                                if (isJr == 0) {
                                    isJr = 1;
                                    // 赋值任务信息
                                    task = Obj.getTaskY(task1);
//                                    // 设置任务延迟总时间（（最后时间+累加时间）-任务开始时间）
//                                    task.setTeDelayDate(((yTeFin+lei) - task.getTePStart()));
                                }
//                                else {
//                                    // 设置任务总共时间（当前任务总时间+循环任务总时间）
//                                    task.setTeDurTotal(task.getTeDurTotal()+task1.getTeDurTotal());
//                                }
                            }
                        }
                        // 升序排序循环存储下标
                        indexS.sort(Comparator.reverseOrder());
                        // 遍历循环存储下标
                        for (Integer integer : indexS) {
                            // 任务集合删除循环存储下标对应的任务信息
                            tasks.remove(integer==null?0:integer);
                        }
//                        System.out.println("删除后的tasks1-3:");
//                        System.out.println(JSON.toJSONString(tasks));

                        // 创建当前唯一编号的镜像任务信息对象
                        Map<String, Map<String, Map<Long, List<Task>>>> tasksF = new HashMap<>();
                        // 创建任务部门对象
                        Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
                        // 创建任务组别对象
                        Map<Long, List<Task>> tasksFz2 = new HashMap<>();
                        // 添加任务集合
                        tasksFz2.put(teS,tasks);
                        // 添加任务组别对象
                        tasksFz.put(grpBNext,tasksFz2);
                        // 添加任务部门对象
                        tasksF.put(depNext,tasksFz);
                        // 添加唯一编号镜像任务对象
                        tasksFAll.put(randoms.get(randomJ),tasksF);

                        // 创建当前唯一编号的镜像任务余剩时间对象
                        Map<String, Map<String, Map<Long, Long>>> zonF = new HashMap<>();
                        // 创建任务余剩时间部门对象
                        Map<String, Map<Long, Long>> zonFz = new HashMap<>();
                        // 创建任务余剩时间组别对象
                        Map<Long, Long> zonFz2 = new HashMap<>();
                        // 添加任务余剩时间
                        zonFz2.put(teS,zon);
                        // 添加任务余剩时间组别对象
                        zonFz.put(grpBNext,zonFz2);
                        // 添加任务余剩时间部门对象
                        zonF.put(depNext,zonFz);
                        // 添加当前唯一编号的镜像任务余剩时间对象
                        zonFAll.put(randoms.get(randomJ),zonF);

                        // 重置id_O，index统一的存储状态
                        Map<String,Map<String,Integer>> stoZ = new HashMap<>();
                        // 添加id_O，index重置状态信息
                        storageReset.put(randoms.get(randomJ),stoZ);
                    }
//                    System.out.println("这里输出task-带延迟加所有时间-3:");
//                    System.out.println(JSON.toJSONString(task));
                }
            }
        }
        // 判断订单信息不为空
        if (null != task) {
            // 赋值存储任务总时间
            long dur = task.getTeDurTotal();
            // 累加任务总时间
            lei += dur;
            // 获取唯一下标
            String random = MongoUtils.GetObjectId();
            // 添加唯一编号状态
            onlyIsDs.put(random,1);
            // 根据键获取当前时间戳
            long teS = Long.parseLong(p.get(0));
//            // 赋值延迟总时间
//            Long teDelayDate = task.getTeDelayDate();
//            // 设置任务开始时间（任务开始时间+延迟总时间）
//            task.setTePStart(task.getTePStart()+teDelayDate);
//            // 设置任务结束时间（任务结束时间+延迟总时间）
//            task.setTePFinish(task.getTePStart()+teDelayDate);
            // 添加唯一标识的当前时间戳
            onlyStartMap.put(random,teS);
            // 调用写入当前时间戳
            setTeS(random,grpBNext,depNext,teS);
            // 设置当前唯一标识的最初始唯一标识
            randoms.put(random,randoms.get(randomJ));
            // 创建当前递归任务所在日期对象
            JSONObject teDate = new JSONObject();
            // 调用时间处理方法
            chkInJi(task,teS,grpBNext,depNext,id_oNext,indONext,0,random,1,teDate,teDaF
                    ,1,sho,isYx,csSta,true);
            // 根据当前唯一标识删除信息
            onlyIsDs.remove(random);
            onlyStartMapAndDep.remove(random);
            onlyStartMap.remove(random);

//            System.out.println("进入获取下一个零件:");
            // 定义存储当前递归信息的下一个产品订单编号或父产品订单编号
            id_OY = actZNext.getString("id_ONext");
            if (null != id_OY) {
                // 定义存储当前递归信息的下一个产品订单下标或父产品订单下标
                indOY = actZNext.getInteger("indONext");
            } else {
                id_OY = actZNext.getString("id_OUpper");
                indOY = actZNext.getInteger("indOUpper");
            }
            // 调用添加或更新产品状态方法
            addSho(sho,id_oNext,indONext.toString(),id_OY,indOY.toString(),0);
            // 调用获取冲突处理方法，原方法
            JSONObject reX = getBcCtH(id_OY, indOY, teDaF, 0, 1, lei,sho,random,task.getId_C(),csSta);
            // 赋值累加时间
            lei = reX.getLong("lei");
            // 赋值问题存储
            isPd = reX.getInteger("isPd");
        }
        // 添加返回信息
        re.put("lei",lei);
        re.put("isPd",isPd);
        return re;
    }

    /**
     * 处理冲突核心方法
     * @param i 当前任务对应循环下标
     * @param tasks 任务集合
     * @param conflict  用于存储被冲突的任务集合
     * @param zon   当前任务余剩时间
     * @param random 当前唯一编号
     * @param dep 部门
     * @param grpB  组别
     * @param teDaF 当前任务所在日期
     * @param isC   isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param ts    ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho   用于存储判断镜像是否是第一个被冲突的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 13:43
     */
    private JSONObject getBqCt(int i,List<Task> tasks,List<Task> conflict,Long zon
            ,String random,String dep,String grpB,JSONObject teDaF
            ,Integer isC,Integer ts,JSONObject sho,int csSta){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
        // 创建存储被冲突的任务当前处理下标
        int conflictInd = 0;
        // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
        int isPd = 0;
        // 判断当前任务下标加1小于总任务集合数量
        if ((i + 1) < tasks.size()) {
            // 判断被冲突的任务当前处理下标等于最后一个或大于最后一个
            if (conflictInd >= conflict.size()) {
//                System.out.println("这里退出-");
                // 添加返回结果
                re.put("zon",zon);
                re.put("isPd",isPd);
                return re;
            }
            // 根据被冲突的任务当前处理下标获取被冲突任务信息
            Task taskC = conflict.get(conflictInd);
            // 深度复制被冲突任务信息
            Task taskX = Obj.getTaskY(taskC);
            // 调用获取被冲突任务所在日期
            List<String> teDateYKeyZ = getTeDateF(taskX.getId_O(), taskX.getIndex(),random);
            // 获取递归信息
            JSONArray objAction = randomAction.getJSONObject(randoms.get(random)).getJSONArray(taskX.getId_O());
            if (null == objAction) {
//                System.out.println("为空-获取数据库-2:"+taskX.getId_O());
                // 根据被冲突任务订单编号获取订单信息
                Order order = coupaUtil.getOrderByListKey(taskX.getId_O(), Collections.singletonList("action"));
                // 获取递归信息
                objAction = order.getAction().getJSONArray("objAction");
                JSONObject oMap = randomAction.getJSONObject(randoms.get(random));
                oMap.put(taskX.getId_O(),objAction);
                randomAction.put(randoms.get(random),oMap);
            }
            // 根据被冲突的任务订单下标获取递归信息
            JSONObject actZ = objAction.getJSONObject(taskX.getIndex());
            // 调用添加或更新产品状态方法
            addSho(sho,taskX.getId_O(),taskX.getIndex().toString(),actZ.getString("id_ONext"), actZ.getString("indONext"),1);
            // 创建任务所在时间对象
            JSONObject teDateO = new JSONObject();
            // 被冲突任务所在日期为空
            if (teDateYKeyZ.size() == 0) {
                // 获取递归信息里的任务所在时间
                JSONObject teDateY = actZ.getJSONObject("teDate");
                // 获取任务所在时间键
                Set<String> teDateYKey = teDateY.keySet();
                // 转换键为集合类型存储
                teDateYKeyZ = new ArrayList<>(teDateYKey);
            }
//            System.out.println("排序前:");
//            System.out.println(JSON.toJSONString(teDateYKeyZ));
            // 判断任务所在时间键大于1
            if (teDateYKeyZ.size() > 1) {
                // 对任务所在时间键进行排序
                Collections.sort(teDateYKeyZ);
//                System.out.println("排序后:");
//                System.out.println(JSON.toJSONString(teDateYKeyZ));
            }
            // 获取任务所在时间键的第一个键的值（时间戳）
            long yTeD = Long.parseLong(teDateYKeyZ.get(0));
            // 调用写入刚获取的时间戳
            setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD);
            // 创建订单下标存储任务所在时间对象
            JSONObject teDaFz = new JSONObject();
            // 添加任务所在时间对象
            teDaFz.put(taskX.getIndex()+"",teDateO);
            // 添加订单下标存储任务所在时间对象
            teDaF.put(taskX.getId_O(),teDaFz);
            // 获取产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
            Integer zOk = sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk");
            if (zOk != -1) {
                // 调用获取获取统一id_O和index存储记录状态信息方法
                Integer storage = getStorage(taskX.getId_O(), taskX.getIndex(), random);
                // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
                if (storage == 0) {
                    // 调用写入存储记录状态方法
                    setStorage(1, taskX.getId_O(), taskX.getIndex(),random);
                }
            }
            // isT2 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
            int isT2 = 0;
            // 遍历任务下标+1以后的任务集合
            for (int i1 = (i+1); i1 < tasks.size(); i1++) {
                // 判断被冲突任务到了最后一个
                if (conflictInd >= conflict.size()) {
                    // 结束循环
                    break;
                }
                // 获取第一个任务信息
                Task task1X = tasks.get(i1);
                // 判断当前任务下标加1小于总任务集合
                if ((i1 + 1) < tasks.size()) {
                    // 获取第二个任务信息
                    Task task2X = tasks.get(i1+1);
//                    System.out.println("task1X:");
//                    System.out.println(JSON.toJSONString(task1X));
//                    System.out.println("task2X:");
//                    System.out.println(JSON.toJSONString(task2X));
                    // 调用空插冲突处理方法
                    JSONObject json = konChaConflict(taskX, task1X, task2X, tasks, i1, conflictInd, zon, conflict,teDateO
                            ,random,dep,grpB,objAction,actZ,yTeD,teDaF,isC,sho,csSta);
                    // 获取任务余剩时间
                    zon = json.getLong("zon");
                    // 更新冲突任务集合下标为冲突下标（conflictInd）指定的冲突任务
                    conflict.set(conflictInd,Obj.getTaskWr(taskX));
                    // 获取冲突下标
                    conflictInd = json.getInteger("conflictInd");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    yTeD = json.getLong("yTeD");
                    // 判断冲突下标小于冲突集合长度
                    if (conflictInd < conflict.size()) {
                        // 获取当前任务订单编号
                        String id_OD = taskX.getId_O();
                        // 获取当前任务订单下标
                        int indexO = taskX.getIndex();
                        // 根据冲突下标获取冲突任务信息
                        Task taskC2 = conflict.get(conflictInd);
                        // 深度复制冲突任务信息
                        Task taskXH = Obj.getTaskY(taskC2);
//                        System.out.println("tasks-bq-1:");
//                        System.out.println(JSON.toJSONString(tasks));
                        // 判断当前任务订单编号不等于冲突任务订单编号，或者当前任务订单下标不等于冲突任务订单下标
                        if (!id_OD.equals(taskXH.getId_O()) || indexO != taskXH.getIndex()) {
                            // 根据当前任务订单编号获取任务所在日期
                            JSONObject jsonObject = teDaF.getJSONObject(taskX.getId_O());
                            // 根据当前任务订单下标添加任务所在日期
                            jsonObject.put(taskX.getIndex()+"",teDateO);
                            // 根据当前任务订单编号添加任务所在日期
                            teDaF.put(taskX.getId_O(),jsonObject);
                            // 调用获取冲突处理方法，原方法
                            JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF
                                    , isC, 0, 0L , sho, random,taskX.getId_C(),csSta);
                            // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                            isPd = bcCtH.getInteger("isPd");
                            // 创建任务所在时间对象
                            teDateO = new JSONObject();
                            // 判断当前任务订单编号不等于冲突任务订单编号
                            if (!id_OD.equals(taskXH.getId_O())) {
                                // 获取所有递归信息
                                objAction = randomAction.getJSONObject(randoms.get(random)).getJSONArray(taskXH.getId_O());
                                if (null == objAction) {
//                                    System.out.println("为空-获取数据库-3:"+taskXH.getId_O());
                                    // 根据冲突任务订单编号获取订单信息
                                    Order order = coupaUtil.getOrderByListKey(taskXH.getId_O(), Collections.singletonList("action"));
                                    // 获取所有递归信息
                                    objAction = order.getAction().getJSONArray("objAction");
                                    JSONObject oMap = randomAction.getJSONObject(randoms.get(random));
                                    oMap.put(taskXH.getId_O(),objAction);
                                    randomAction.put(randoms.get(random),oMap);
                                }
                            }
                            // 根据冲突任务订单下标获取递归信息
                            actZ = objAction.getJSONObject(taskXH.getIndex());
                            // 获取任务所在日期对象
                            JSONObject teDateY1 = actZ.getJSONObject("teDate");
                            // 获取任务所在日期键集合
                            Set<String> teDateYKey1 = teDateY1.keySet();
                            // 转换任务所在日期键集合
                            List<String> teDateYKeyZ1 = new ArrayList<>(teDateYKey1);
                            // 获取任务所在时间键的第一个键的值（时间戳）
                            yTeD = Long.parseLong(teDateYKeyZ1.get(0));
                            // 调用写入当前时间戳方法
                            setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD);
                            // 创建订单下标存储任务所在时间对象
                            teDaFz = new JSONObject();
                            // 添加订单下标存储任务所在时间对象
                            teDaFz.put(taskXH.getIndex()+"",teDateO);
                            // 添加订单编号存储任务所在时间对象
                            teDaF.put(taskXH.getId_O(),teDaFz);
                        }
                        // 根据冲突任务下标获取冲突任务信息
                        taskX = conflict.get(conflictInd);
//                        System.out.println("换taskX:"+zon);
                    } else {
//                        System.out.println("tasks-bq-2:");
//                        System.out.println(JSON.toJSONString(tasks));
                        // 根据冲突任务订单编号获取任务所在日期对象
                        JSONObject jsonObject = teDaF.getJSONObject(taskX.getId_O());
                        // 根据冲突任务订单下标添加任务所在日期对象
                        jsonObject.put(taskX.getIndex()+"",teDateO);
                        // 根据冲突任务订单编号添加任务所在日期对象
                        teDaF.put(taskX.getId_O(),jsonObject);
                        // 调用获取冲突处理方法，原方法
                        JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF
                                , isC, 0, 0L , sho, random,taskX.getId_C(),csSta);
                        // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                        isPd = bcCtH.getInteger("isPd");
                    }
                    // isT2 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
                    isT2 = json.getInteger("isT2");
                    // 空插冲突强制停止参数累加
                    lei++;
                    // 判断空插强制停止参数等于60
                    if (lei == 60) {
//                        System.out.println("----进入强制停止空差冲突方法-1----");
                        // 强制停止出现后的记录参数赋值等于1
                        isQzTz = 1;
                        // 停止方法
                        break;
                    }
                }
            }
            // 定义时间戳
            long teS;
            // ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
            if (ts == 1) {
                // 根据当前唯一编号获取最新的（最后一个）当前时间戳
                JSONObject rand = onlyStartMapAndDep.getJSONObject(random);
                // 根据部门获取获取最新的（最后一个）当前时间戳
                JSONObject onlyDep = rand.getJSONObject(dep);
                // 根据组别获取获取最新的（最后一个）当前时间戳
                teS = onlyDep.getLong(grpB);
            } else {
                // 根据当前唯一编号获取存储当前唯一编号的第一个当前时间戳
                teS = onlyStartMap.getLong(random);
            }
            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 1) {
//                System.out.println("进入这里写入:");
                // 调用写入镜像任务集合方法
                setTasksF(tasks,grpB,dep,teS,random);
                // 调用写入镜像任务余剩时间方法
                setZonF(zon,grpB,dep,teS,random);
            }
            // 调用空插方法，获取存储问题状态
            isPd = kc(tasks, conflictInd, conflict, taskX, zon, grpB, dep, random, isT2, teS, 0, teDateO
                    , objAction, actZ, yTeD, teDaF, isC, sho, isPd,csSta);
        }
        // 添加返回结果
        re.put("zon",zon);
        re.put("isPd",isPd);
        return re;
    }

    /**
     * 空插处理方法
     * @param tasks	任务集合
     * @param conflictInd	冲突任务下标
     * @param conflict	冲突任务集合
     * @param taskX	当前处理任务
     * @param zon	任务余剩时间
     * @param grpB	组别
     * @param dep	部门
     * @param random	当前唯一编号
     * @param isT2	isT2 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
     * @param teS	当前时间戳
     * @param is	is == 0 正常第一次调用空插处理方法、is == 1 空插处理方法调用空插处理方法
     * @param teDate	当前处理任务所在时间对象
     * @param objAction	所有递归信息
     * @param actZ	递归信息
     * @param yTeD	任务所在时间键的第一个键的值（时间戳）
     * @param teDaF	当前任务所在日期
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isPd	存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
     * @return int  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 19:44
     */
    @SuppressWarnings("unchecked")
    private int kc(List<Task> tasks,int conflictInd,List<Task> conflict,Task taskX,Long zon,String grpB
            ,String dep,String random,int isT2,Long teS,int is,JSONObject teDate,JSONArray objAction
            ,JSONObject actZ,Long yTeD,JSONObject teDaF,Integer isC,JSONObject sho,int isPd,int csSta){
        // isT2 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
        if (isT2 == 1) {
            // 当前时间戳加一天
            teS += 86400L;
            // 任务所在时间键的第一个键的值（时间戳）加一天
            yTeD += 86400L;
            System.out.println("--进入空插冲突跳天--");
            // 调用获取任务综合信息方法
            Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,1,teS,isC,taskX.getId_C());
            // 获取任务集合
            List<Task> tasks2 = (List<Task>) jumpDay.get("tasks");
            // 获取任务余剩时间
            long zon2 = (long) jumpDay.get("zon");
            // isT22 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
            int isT22 = 0;
            // 存储问题状态参数: isPd2 = 0 正常、isPd2 = 1 订单编号为空、isPd2 = 2 主生产部件
            int isPd2 = 0;
            // 遍历任务集合
            for (int i1 = 0; i1 < tasks2.size(); i1++) {
                // 判断冲突任务下标大于等于冲突任务集合长度
                if (conflictInd >= conflict.size()) {
                    // 结束循环
                    break;
                }
                // 获取对比任务1
                Task task1X = tasks2.get(i1);
                // 判断任务下标加1小于任务集合长度
                if ((i1 + 1) < tasks2.size()) {
                    // 获取对比任务2
                    Task task2X = tasks2.get(i1+1);
//                    System.out.println("task1X:");
//                    System.out.println(JSON.toJSONString(task1X));
//                    System.out.println("task2X:");
//                    System.out.println(JSON.toJSONString(task2X));
                    // 调用空插冲突处理方法
                    JSONObject json = konChaConflict(taskX, task1X, task2X, tasks2, i1, conflictInd, zon2, conflict,teDate,random
                            ,dep,grpB,objAction,actZ,yTeD,teDaF,isC,sho,csSta);
                    // 获取任务余剩时间
                    zon2 = json.getLong("zon");
                    // 更新冲突任务集合下标为冲突下标的冲突任务信息
                    conflict.set(conflictInd,Obj.getTaskX(taskX.getTePStart(),taskX.getTePFinish(),taskX.getTeDurTotal(),taskX));
                    // 获取冲突任务下标
                    conflictInd = json.getInteger("conflictInd");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    yTeD = json.getLong("yTeD");
//                    System.out.println("出来后赋值-conflictInd-kc:"+conflictInd);
                    // 判断冲突任务下标小于冲突任务集合的长度
                    if (conflictInd < conflict.size()) {
                        // 获取当前任务订单编号
                        String id_OD = taskX.getId_O();
                        // 获取当前任务订单下标
                        int indexO = taskX.getIndex();
                        // 根据冲突任务下标获取冲突任务对象
                        Task taskC3 = conflict.get(conflictInd);
                        // 深度复制冲突任务对象
                        Task taskXH = Obj.getTaskX(taskC3.getTePStart(),taskC3.getTePFinish(),taskC3.getTeDurTotal(),taskC3);

//                        System.out.println("tasks2-kc-1:");
//                        System.out.println(JSON.toJSONString(tasks2));
                        // 判断当前任务订单编号不等于冲突任务订单编号，或者当前任务订单下标不等于冲突任务订单下标
                        if (!id_OD.equals(taskXH.getId_O()) || indexO != taskXH.getIndex()) {
                            // 根据当前任务订单编号获取任务所在日期对象
                            JSONObject jsonObject = teDaF.getJSONObject(taskX.getId_O());
                            // 根据当前任务订单下标添加任务所在日期
                            jsonObject.put(taskX.getIndex()+"",teDate);
                            // 根据当前任务订单编号添加任务所在日期对象
                            teDaF.put(taskX.getId_O(),jsonObject);
                            // 调用获取冲突处理方法，原方法
                            JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF
                                    , isC, 1, 0L , sho, random,taskX.getId_C(),csSta);
                            // 获取存储问题状态参数
                            isPd2 = bcCtH.getInteger("isPd");
                            // 创建任务所在日期对象
                            teDate = new JSONObject();
                            // 判断当前任务订单编号不等于冲突任务订单编号
                            if (!id_OD.equals(taskXH.getId_O())) {
                                // 获取进度卡片的所有递归信息
                                objAction = randomAction.getJSONObject(randoms.get(random)).getJSONArray(taskXH.getId_O());
                                if (null == objAction) {
//                                    System.out.println("为空-获取数据库-4:"+taskXH.getId_O());
                                    // 根据冲突任务订单编号获取订单信息 - t
                                    Order order = coupaUtil.getOrderByListKey(taskXH.getId_O(), Collections.singletonList("action"));
                                    // 获取进度卡片的所有递归信息
                                    objAction = order.getAction().getJSONArray("objAction");
                                    JSONObject oMap = randomAction.getJSONObject(randoms.get(random));
                                    oMap.put(taskXH.getId_O(),objAction);
                                    randomAction.put(randoms.get(random),oMap);
                                }
                            }
                            // 根据冲突任务订单下标获取递归信息
                            actZ = objAction.getJSONObject(taskXH.getIndex());
                            // 获取递归信息的任务所在日期对象
                            JSONObject teDateY = actZ.getJSONObject("teDate");
                            // 获取任务所在日期的所有键
                            Set<String> teDateYKey = teDateY.keySet();
                            // 将设置日期键转换成集合类型
                            List<String> teDateYKeyZ = new ArrayList<>(teDateYKey);
                            // 获取任务所在时间键的第一个键的值（时间戳）
                            yTeD = Long.parseLong(teDateYKeyZ.get(0));
                            // 调用写入当前时间戳方法
                            setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD);
                            // 创建任务所在日期存储对象
                            JSONObject teDaFz = new JSONObject();
                            // 根据冲突任务订单下标添加任务所在日期
                            teDaFz.put(taskXH.getIndex()+"",teDate);
                            // 根据冲突任务订单编号添加任务所在日期
                            teDaF.put(taskXH.getId_O(),teDaFz);
                        }
                        // 根据冲突任务下标获取冲突任务信息
                        taskX = conflict.get(conflictInd);
//                        System.out.println("换taskX:"+zon2);
                    } else {
//                        System.out.println("tasks2-kc-2:");
//                        System.out.println(JSON.toJSONString(tasks2));
                        // 根据当前任务订单编号获取任务所在日期对象
                        JSONObject jsonObject = teDaF.getJSONObject(taskX.getId_O());
                        // 根据当前任务订单下标添加任务所在日期
                        jsonObject.put(taskX.getIndex()+"",teDate);
                        // 根据当前任务订单编号添加任务所在日期对象
                        teDaF.put(taskX.getId_O(),jsonObject);
                        // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
                        if (isC == 1) {
                            // 调用写入镜像任务集合方法
                            setTasksF(tasks2,grpB,dep,teS,random);
                            // 调用写入镜像任务余剩时间
                            setZonF(zon2,grpB,dep,teS,random);
                        }
                        // 调用获取冲突处理方法，原方法
                        JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF
                                , isC, 1, 0L, sho, random,taskX.getId_C(),csSta);
                        // 获取存储问题状态参数
                        isPd2 = bcCtH.getInteger("isPd");
                    }
//                    System.out.println("taskX-kc-2:");
//                    System.out.println(JSON.toJSONString(taskX));
                    // isT22 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
                    isT22 = json.getInteger("isT2");
                    // 空插冲突强制停止参数累加
                    lei++;
                    // 判断空插冲突强制停止参数等于60
                    if (lei == 60) {
                        System.out.println("----进入强制停止空差冲突方法-2----");
                        // 赋值强制停止出现后的记录参数等于1
                        isQzTz = 1;
                        break;
                    }
                }
            }
            // 判断空插冲突强制停止参数小于61
            if (lei < 61) {
                System.out.println("---这里问题---:"+isPd2);
                // 调用空插处理方法
                kc(tasks2,conflictInd,conflict,taskX,zon2,grpB,dep,random,isT22,teS,1,teDate
                        ,objAction,actZ,yTeD,teDaF,isC,sho,isPd2,csSta);
                return isPd2;
            } else {
                System.out.println("----进入强制停止空差冲突方法-2-1----");
                // 赋值强制停止出现后的记录参数等于1
                isQzTz = 1;
            }
        }
        else {
            // is == 0 正常第一次调用空插处理方法、is == 1 空插处理方法调用空插处理方法
            if (is == 1) {
                // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
                if (isC == 0) {
                    setTasksAndZon(tasks,grpB,dep,teS,taskX.getId_C(),false,zon,coupaUtil);
                } else {
                    // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                    if (isPd != 2) {
                        // 调用写入镜像任务集合方法
                        setTasksF(tasks,grpB,dep,teS,random);
                        // 调用写入镜像任务余剩时间方法
                        setZonF(zon,grpB,dep,teS,random);
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 处理冲突核心方法2
     * @param conflictInd	被冲突任务下标
     * @param tasks	任务集合
     * @param conflict	被冲突任务集合
     * @param taskX	当前处理任务
     * @param zon	任务余剩时间
     * @param teDate	当前处理的任务的所在日期对象
     * @param random	当前唯一编号
     * @param objAction	所有递归信息
     * @param actZ	递归信息
     * @param i1	任务下标
     * @param dep	部门
     * @param grpB	组别
     * @param yTeD	任务所在时间键的第一个键的值（时间戳）
     * @param teDaF	当前任务所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 16:39
     */
    private JSONObject getBqCt2(int conflictInd,List<Task> tasks,List<Task> conflict,Task taskX,Long zon
            ,JSONObject teDate,String random,JSONArray objAction,JSONObject actZ
            ,int i1,String dep,String grpB,Long yTeD,JSONObject teDaF,Integer isC,JSONObject sho,int csSta){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
        // 判断冲突任务下标小于冲突任务集合总长度
        if (conflictInd < conflict.size()) {
            // 获取冲突任务信息
            taskX = conflict.get(conflictInd);
        }
        // 遍历任务集合
        for (int i2 = i1+1; i2 < tasks.size(); i2++) {
            // 判断冲突任务下标大于等于冲突任务集合总长度
            if (conflictInd >= conflict.size()) {
                break;
            }
            // 获取对比任务1
            Task task1X2 = tasks.get(i2);
            // 判断（i2（当前任务下标） + 1）小于任务集合总长度
            if ((i2 + 1) < tasks.size()) {
                // 获取对比任务2
                Task task2X2 = tasks.get(i2 + 1);
                // 调用空插冲突处理方法
                JSONObject json = konChaConflict(taskX, task1X2, task2X2, tasks, i2, conflictInd, zon, conflict,teDate
                        ,random, dep,grpB,objAction,actZ,yTeD,teDaF,isC,sho,csSta);
                // 获取任务余剩时间
                zon = json.getLong("zon");
                // 更新冲突任务集合指定下标（冲突任务下标）的冲突任务信息
                conflict.set(conflictInd,Obj.getTaskX(taskX.getTePStart(),taskX.getTePFinish(),taskX.getTeDurTotal(),taskX));
                // 获取冲突任务下标
                conflictInd = json.getInteger("conflictInd");
                // 获取任务所在时间键的第一个键的值（时间戳）
                yTeD = json.getLong("yTeD");
//                System.out.println("出来后赋值-conflictInd-2-2:"+conflictInd);
//                System.out.println(JSON.toJSONString(conflict));
                // 判断冲突任务下标小于冲突任务集合的长度
                if (conflictInd < conflict.size()) {
                    // 获取当前任务订单编号
                    String id_OD = taskX.getId_O();
                    // 获取当前任务订单下标
                    int indexO = taskX.getIndex();
                    // 根据冲突任务下标获取冲突任务对象
                    Task taskC = conflict.get(conflictInd);
                    // 深度复制冲突任务对象
                    Task taskXH = Obj.getTaskX(taskC.getTePStart(),taskC.getTePFinish(),taskC.getTeDurTotal(),taskC);

//                    System.out.println("tasks-bq2-1:");
//                    System.out.println(JSON.toJSONString(tasks));
                    // 判断当前任务订单编号不等于冲突任务订单编号，或者当前任务订单下标不等于冲突任务订单下标
                    if (!id_OD.equals(taskXH.getId_O()) || indexO != taskXH.getIndex()) {
                        // 根据当前任务订单编号获取任务所在日期对象
                        JSONObject jsonObject = teDaF.getJSONObject(taskX.getId_O());
                        // 根据当前任务订单下标添加任务所在日期
                        jsonObject.put(taskX.getIndex()+"",teDate);
                        // 根据当前任务订单编号添加任务所在日期对象
                        teDaF.put(taskX.getId_O(),jsonObject);
                        // 调用获取冲突处理方法，原方法
                        getBcCtH(actZ.getString("id_ONext"),actZ.getInteger("indONext"),teDaF
                                ,isC,0,0L,sho,random,taskX.getId_C(),csSta);
                        // 创建任务所在日期对象
                        teDate = new JSONObject();
                        // 判断当前任务订单编号不等于冲突任务订单编号
                        if (!id_OD.equals(taskXH.getId_O())) {
                            // 获取进度卡片的所有递归信息
                            objAction = randomAction.getJSONObject(randoms.get(random)).getJSONArray(taskXH.getId_O());
                            if (null == objAction) {
//                                System.out.println("为空-获取数据库-5:"+taskXH.getId_O());
                                // 根据冲突任务订单编号获取订单信息 - t
                                Order order = coupaUtil.getOrderByListKey(taskXH.getId_O(), Collections.singletonList("action"));
                                // 获取进度卡片的所有递归信息
                                objAction = order.getAction().getJSONArray("objAction");
                                JSONObject oMap = randomAction.getJSONObject(randoms.get(random));
                                oMap.put(taskXH.getId_O(),objAction);
                                randomAction.put(randoms.get(random),oMap);
                            }
                        }
                        // 根据冲突任务订单下标获取递归信息
                        actZ = objAction.getJSONObject(taskXH.getIndex());
                        // 获取递归信息的任务所在日期对象
                        JSONObject teDateY = actZ.getJSONObject("teDate");
                        // 获取任务所在日期的所有键
                        Set<String> teDateYKey = teDateY.keySet();
                        // 将设置日期键转换成集合类型
                        List<String> teDateYKeyZ = new ArrayList<>(teDateYKey);
                        // 获取任务所在时间键的第一个键的值（时间戳）
                        yTeD = Long.parseLong(teDateYKeyZ.get(0));
                        // 调用写入当前时间戳方法
                        setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD);
                        // 创建任务所在日期存储对象
                        JSONObject teDaFz = new JSONObject();
                        // 根据冲突任务订单下标添加任务所在日期
                        teDaFz.put(taskXH.getIndex()+"",teDate);
                        // 根据冲突任务订单编号添加任务所在日期
                        teDaF.put(taskXH.getId_O(),teDaFz);
                    }
                    // 根据冲突任务下标获取冲突任务信息
                    taskX = conflict.get(conflictInd);
//                    System.out.println("换taskX:"+zon);
                } else {
//                    System.out.println("tasks-bq2-2:");
//                    System.out.println(JSON.toJSONString(tasks));
                    // 根据当前任务订单编号获取任务所在日期对象
                    JSONObject jsonObject = teDaF.getJSONObject(taskX.getId_O());
                    // 根据当前任务订单下标添加任务所在日期
                    jsonObject.put(taskX.getIndex()+"",teDate);
                    // 根据当前任务订单编号添加任务所在日期对象
                    teDaF.put(taskX.getId_O(),jsonObject);
                    // 调用获取冲突处理方法，原方法
                    getBcCtH(actZ.getString("id_ONext"),actZ.getInteger("indONext"),teDaF
                            ,isC,0,0L,sho,random,taskX.getId_C(),csSta);
                }
//                System.out.println(JSON.toJSONString(taskX));
            }
        }
        re.put("conflictInd",conflictInd);
        re.put("zon",zon);
        re.put("yTeD",yTeD);
        return re;
    }

    /**
     * 处理时间冲突方法
     * @param task	当前处理任务信息
     * @param task1	对比任务1
     * @param task2	对比任务2
     * @param zon	任务余剩时间
     * @param tasks	任务集合
     * @param i	任务下标
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teDate	当前处理的任务的所在日期对象
     * @param teDaF	当前任务所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param ts	ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/19 1:33
     */
    @SuppressWarnings("unchecked")
    private JSONObject getCt1(Task task,Task task1,Task task2,Long zon,List<Task> tasks,int i,String random
            ,String grpB,String dep,JSONObject teDate,JSONObject teDaF
            ,Integer isC,Integer ts,JSONObject sho,int csSta){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
//        System.out.println("进入-最开始-这里-1");
        // 创建冲突任务集合
        List<Task> conflict = new ArrayList<>();
        // 调用冲突处理核心方法
        JSONObject conflictHandle = conflictHandle(task, task1, task2, zon, tasks, i, conflict
                ,getTeS(random, grpB, dep),random,grpB,dep,teDate,isC,ts,sho,csSta);
        // 获取任务余剩时间
        zon = conflictHandle.getLong("zon");
        // 获取存储任务是否被处理完状态参数：isJ == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        int isJ = conflictHandle.getInteger("isJ");
        // 获取当前时间戳
        long teSB = conflictHandle.getLong("teSB");
//        System.out.println("isJ外:"+isJ);
//        System.out.println("tasks:");
//        System.out.println(JSON.toJSONString(tasks));
        // 获取存储任务是否被处理完状态参数：isJ == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        if (isJ == 0) {
            // 开启循环
            do {
                // isJ强制停止参数累加
                xin++;
                // 当前时间戳累加
                teSB += 86400L;
                // 调用获取任务综合信息方法
                Map<String, Object> jumpDay = getJumpDay(random, grpB, dep, 1, teSB,isC,task.getId_C());
                // 获取任务集合
                List<Task> tasks2 = (List<Task>) jumpDay.get("tasks");
                // 获取任务余剩时间
                long zon2 = (long) jumpDay.get("zon");
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                int zb = 0;
                // 遍历任务集合
                for (int i1 = 0; i1 < tasks2.size(); i1++) {
                    // 获取对比任务1
                    Task task1X = tasks2.get(i1);
                    // 判断（i1（当前任务下标）+1）小于任务集合总长度
                    if ((i1 + 1) < tasks2.size()) {
                        // 获取对比任务2
                        Task task2X = tasks2.get(i1 + 1);
//                        System.out.println("这里进入-1--2");

                        conflictHandle = conflictHandle(task, task1X, task2X, zon2, tasks2, i1, conflict
                                , teSB,random,grpB,dep,teDate,isC,0,sho,csSta);
                        // 获取任务余剩时间
                        zon2 = conflictHandle.getLong("zon");
                        // 获取存储任务是否被处理完状态参数：isJ == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
                        isJ = conflictHandle.getInteger("isJ");
                        // 获取当前时间戳
                        teSB = conflictHandle.getLong("teSB");
//                        System.out.println("isJ:" + isJ);
                        // 获取存储任务是否被处理完状态参数：isJ == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
                        if (isJ == 1) {
                            zb = 1;
                            // isC – isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
                            if (isC == 0) {
                                setTasksAndZon(tasks2,grpB,dep,teSB,task.getId_C(),false,zon2,coupaUtil);
                            } else {
//                                System.out.println("进入-最开始-这里-1-写入镜像:");
                                // 调用写入镜像任务集合方法
                                setTasksF(tasks,grpB,dep,teSB,random);
                                // 调用写入镜像任务余剩时间方法
                                setZonF(zon,grpB,dep,teSB,random);
                            }
                            break;
                        }
                    }
                }
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                if (zb == 1) {
                    break;
                }
                // 判断isJ强制停止参数等于10
                if (xin == 10) {
                    System.out.println("进入isJ强制结束!!!");
                    // 赋值强制停止出现后的记录参数
                    isQzTz = 1;
                    break;
                }
            } while (true);
        }

        // 调用处理冲突核心方法
        JSONObject re2 = getBqCt(i,tasks,conflict,zon,random,dep,grpB,teDaF,isC,ts,sho,csSta);
        re.put("zon",re2.getLong("zon"));
        // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
        re.put("isPd",re2.getInteger("isPd"));
        return re;
    }

    /**
     * 处理时间冲突方法复刻方法（比起原方法，少了很多操作）
     * @param task	当前处理任务信息
     * @param task2X	对比任务1
     * @param task3	对比任务2
     * @param zon	任务余剩时间
     * @param tasks	任务集合
     * @param i	上一个任务下标
     * @param i1	当前任务下标
     * @param conflict	被冲突任务集合
     * @param teSB	当前时间戳
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teDate	当前处理的任务的所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param ts	ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/21 2:59
     */
    @SuppressWarnings("unchecked")
    private JSONObject getCt1F(Task task,Task task2X,Task task3,Long zon,List<Task> tasks,int i,int i1
            ,List<Task> conflict,Long teSB,String random,String grpB,String dep
            ,JSONObject teDate,Integer isC,Integer ts,JSONObject sho,int csSta){
        // 调用冲突处理核心方法
        JSONObject conflictHandle = conflictHandle(task, task2X, task3, zon, tasks, i1, conflict
                ,teSB,random,grpB,dep,teDate,isC,ts,sho,csSta);
        // 获取任务余剩时间
        zon = conflictHandle.getLong("zon");
        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
        int isJ2 = conflictHandle.getInteger("isJ");
        // 获取当前时间戳
        long teSB2 = conflictHandle.getLong("teSB");
//        System.out.println("isJ2外:"+isJ2);
//        System.out.println("tasks:");
//        System.out.println(JSON.toJSONString(tasks));
        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
        if (isJ2 == 0) {
            // 开启循环
            do {
                // 当前时间戳累加
                teSB2 += 86400L;
                // 调用获取任务综合信息方法
                Map<String, Object> jumpDay = getJumpDay(random, grpB, dep, 1, teSB,isC,task.getId_C());
                // 获取任务集合
                List<Task> tasks2 = (List<Task>) jumpDay.get("tasks");
                // 获取任务余剩时间
                long zon2 = (long) jumpDay.get("zon");
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                int zb = 0;
                // 遍历任务集合
                for (int i2 = 0; i2 < tasks2.size(); i2++) {
                    // 获取对比任务1
                    Task task1Xx = tasks.get(i);
                    // 判断（i2（当前任务下标）+1）小于任务集合总长度
                    if ((i2 + 1) < tasks.size()) {
                        // 获取对比任务2
                        Task task2Xx = tasks.get(i2 + 1);
                        // 调用冲突处理核心方法
                        conflictHandle = conflictHandle(task, task1Xx, task2Xx, zon2, tasks2, i2, conflict
                                , teSB2,random,grpB,dep,teDate,isC,0,sho,csSta);
                        // 获取任务余剩时间
                        zon2 = conflictHandle.getLong("zon");
                        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
                        isJ2 = conflictHandle.getInteger("isJ");
                        // 获取当前时间戳
                        teSB2 = conflictHandle.getLong("teSB");
//                        System.out.println("isJ:" + isJ2);
                        // 获取存储任务是否被处理完状态参数：isJ2 == 0 任务没有被处理完、isJ2 == 1 任务已经被处理完了
                        if (isJ2 == 1) {
                            zb = 1;
                            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
                            if (isC == 0) {
                                setTasksAndZon(tasks,grpB,dep,teSB,task.getId_C(),false,zon,coupaUtil);
                            } else {
                                // 调用写入镜像任务集合方法
                                setTasksF(tasks,grpB,dep,teSB,random);
                                // 调用写入镜像任务余剩时间方法
                                setZonF(zon,grpB,dep,teSB,random);
                            }
                            break;
                        }
                    }
                }
                // 控制结束外层循环参数，zb == 0 不结束外层循环、zb == 1 结束外层循环
                if (zb == 1) {
                    break;
                }
                // 判断isJ强制停止参数等于10
                if (xin == 10) {
                    System.out.println("进入isJ强制结束!!!");
                    // 赋值强制停止出现后的记录参数
                    isQzTz = 1;
                    break;
                }
            } while (true);
        }
        // 创建返回结果对象
        JSONObject re = new JSONObject();
        re.put("zon",zon);
        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
        re.put("jie",conflictHandle.getInteger("jie"));
        return re;
    }

    /**
     * 判断产品状态再调用写入任务所在日期方法的方法
     * @param id_O  订单编号
     * @param index 订单下标
     * @param teS   当前时间戳
     * @param zOk  产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
     * @param random    当前唯一编号
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 16:27
     */
    private void putTeDate(String id_O,Integer index,Long teS,Integer zOk,String random){
        // 判断状态不等于当前递归产品
        if (zOk != -1) {
//            System.out.println("添加记录时间-id_O:"+id_O+",index:"+index+",teS:"+teS);
            // 调用写入任务所在日期方法
            setTeDateF(id_O,index,random,teS);
        }
    }

    /**
     * 时间处理方法
     * @param task	当前处理任务信息
     * @param hTeStart	任务最初始开始时间
     * @param grpB	组别
     * @param dep	部门
     * @param id_O	订单编号
     * @param index	订单下标
     * @param isT	保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
     * @param random	当前唯一编号
     * @param isK	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param teDate	当前处理的任务的所在日期对象
     * @param teDaF	当前任务所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isYx	保存是否出现任务为空异常:isYx = 0 正常操作未出现异常、isYx = 1 出现拿出任务为空异常镜像|数据库
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 22:27
     */
    @SuppressWarnings("unchecked")
    public JSONObject chkInJi(Task task,Long hTeStart,String grpB
            ,String dep,String id_O,Integer index,Integer isT,String random
            ,Integer isK,JSONObject teDate,JSONObject teDaF
            ,Integer isC,JSONObject sho,int isYx,int csSta,boolean isD){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
        // 跳天强制停止参数累加
        yiShu++;
        // 调用获取任务综合信息方法
        Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,0,0L,isC,task.getId_C());
        // 获取任务集合
        List<Task> tasks = (List<Task>) jumpDay.get("tasks");
        // 获取任务余剩时间
        long zon = (long) jumpDay.get("zon");
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer zOk = sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk");
        if (zOk != -1) {
            // 调用获取获取统一id_O和index存储记录状态信息方法
            Integer storage = getStorage(task.getId_O(), task.getIndex(), random);
            // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
            if (storage == 0) {
                // 调用写入存储记录状态方法
                setStorage(2, task.getId_O(), task.getIndex(),random);
            }
        }
        // 调用时间处理方法2
        JSONObject jsonObject = chkInJi2(zon, hTeStart, tasks, grpB, dep, id_O, index, task
                , isT, random, isK, teDate, teDaF, isC, sho,isYx,csSta);
        // 获取任务最初始开始时间
        hTeStart = jsonObject.getLong("hTeStart");
        re.put("hTeStart",hTeStart);
        re.put("xFin",jsonObject.getLong("xFin"));
        return re;
    }

    /**
     * 时间处理方法2
     * @param zon	任务余剩时间
     * @param hTeStart	任务最初始开始时间
     * @param tasks	任务集合
     * @param grpB	组别
     * @param dep	部门
     * @param id_O	订单编号
     * @param index	订单下标
     * @param task	当前处理任务
     * @param isT	保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
     * @param random	当前唯一编号
     * @param isK	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param teDate	当前处理的任务的所在日期对象
     * @param teDaF	当前任务所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isYx	保存是否出现任务为空异常:isYx = 0 正常操作未出现异常、isYx = 1 出现拿出任务为空异常镜像|数据库
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/19 0:03
     */
    private JSONObject chkInJi2(Long zon,Long hTeStart,List<Task> tasks,String grpB,String dep
            ,String id_O,Integer index,Task task,Integer isT,String random,Integer isK
            ,JSONObject teDate,JSONObject teDaF,Integer isC,JSONObject sho,int isYx,int csSta){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer zOk = sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk");
        // 判断任务余剩时间不等于0
        if (zon != 0) {
            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
            int isR = 0;
            // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
            int isP;
            // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
            int isPd = 0;
            // 判断任务最初始开始时间为0
            if (hTeStart == 0) {
//                System.out.println("进入hTeStart == 0---");
//                System.out.println(JSON.toJSONString(task));
//                System.out.println(JSON.toJSONString(tasks));
                // 遍历任务集合
                for (int i = 0; i < tasks.size(); i++) {
                    // 根据下标获取对比任务信息1
                    Task task1 = tasks.get(i);
                    // 判断下标加1小于任务集合长度
                    if ((i + 1) < tasks.size()) {
                        // 根据下标获取对比任务信息2
                        Task task2 = tasks.get(i + 1);
//                        System.out.println("taskzongk:");
//                        System.out.println(JSON.toJSONString(task1));
//                        System.out.println(JSON.toJSONString(task2));
                        // 调用计算空插时间方法
                        JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate,dep,grpB,zOk);
                        // 获取存储问题状态参数
                        isP = ji.getInteger("isP");
                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                        if (isP == 2) {
                            // 获取控制是否跳天参数
                            isR = ji.getInteger("isR");
                            // 获取任务余剩时间
                            zon = ji.getLong("zon");
                            break;
                        } else if (isP == 4) {
                            // 获取任务余剩时间
                            zon = ji.getLong("zon");
                        }
                    }
                }
            } else {
                // 存储任务冲突信息参数：isL == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
                int isL = 0;
//                System.out.println("进入hTeStart != 0--- isL:"+isL);
                // 遍历任务集合
                for (int i = 0; i < tasks.size(); i++) {
                    // 根据下标获取对比任务信息1
                    Task task1 = tasks.get(i);
                    // 判断下标加1小于任务集合长度
                    if ((i + 1) < tasks.size()) {
                        // 根据下标获取对比任务信息2
                        Task task2 = tasks.get(i + 1);
//                        System.out.println("task-zon:");
//                        System.out.println(JSON.toJSONString(task1));
//                        System.out.println(JSON.toJSONString(task2));
                        // 判断进入条件
                        // isT : 保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
                        // isL : 存储任务冲突信息参数：isL == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
                        // isK : 控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
                        if (isT == 0 && isL == 0 || isK == 1) {
//                            System.out.println("进入if");
                            isK = 0;
                            // 判断当前任务的开始时间大于等于对比任务信息1的结束时间
                            if (task.getTePStart() >= task1.getTePFinish()) {
                                // 定义存储调用[计算空插时间方法]的结果
                                JSONObject ji;
                                // 判断当前任务的结束时间小于对比任务信息2的开始时间
                                if (task.getTePFinish() < task2.getTePStart()) {
//                                    System.out.println("进入这里-1-1");
                                    // 调用计算空插时间方法并赋值
                                    ji = ji(task, task1, task2, tasks, i, zon, 1,random,teDate,dep,grpB,zOk);
                                } else if (task.getTePStart() <= task2.getTePStart() && task.getPriority() >= task2.getPriority()) {
//                                    System.out.println("进入这里-1-2");
                                    // 调用计算空插时间方法并赋值
                                    ji = ji(task, task1, task2, tasks, i, zon, 2,random,teDate,dep,grpB,zOk);
                                } else if (task.getTePStart() < task1.getTePFinish() && task1.getPriority() != -1) {
//                                    System.out.println("进入这里-1-3");
                                    isL = 3;
                                    break;
                                } else {
                                    // 判断当前任务的优先级小于对比任务信息2的优先级
                                    if (task.getPriority() < task2.getPriority()) {
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                            isL = 3;
                                            System.out.println("进入-冲突-最开始-这里-1");
                                            // 调用处理时间冲突方法
                                            JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 0, sho,csSta);
                                            // 获取任务余剩时间
                                            zon = ct1.getLong("zon");
                                            // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isPd = ct1.getInteger("isPd");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isR = 2;
//                                            System.out.println("进入-冲突-最开始-这里-1");
                                            break;
                                        } else
                                        if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-2");
                                            jiLJ.add("进入-冲突-最开始-这里-2");
                                            isL = 4;
                                            break;
                                        } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-3");
                                            jiLJ.add("进入-冲突-最开始-这里-3");
                                            isL = 5;
                                            break;
                                        }
                                    } else if (task.getPriority() < task1.getPriority()){
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
//                                            System.out.println("进入-冲突-最开始-这里-1--1");
                                            isL = 33;
                                            // 调用处理时间冲突方法
                                            JSONObject ct1 = getCt1(task,task1,task2,zon,tasks,i,random,grpB,dep,teDate,teDaF,isC,0,sho,csSta);
                                            // 获取任务余剩时间
                                            zon = ct1.getLong("zon");
                                            // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isPd = ct1.getInteger("isPd");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isR = 2;
                                            System.out.println("进入-冲突-最开始-这里-1--1");
                                            break;
                                        } else
                                        if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-2--1");
                                            jiLJ.add("进入-冲突-最开始-这里-2--1");
                                            isL = 44;
                                            break;
                                        } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-3--1");
                                            jiLJ.add("进入-冲突-最开始-这里-3--1");
                                            isL = 55;
                                            break;
                                        }
                                    }
//                                    System.out.println("弹回---");
                                    isK = 1;
                                    continue;
                                }
                                // 获取控制是否结束循环参数
                                isP = ji.getInteger("isP");
                                // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                if (isP == 2) {
                                    // 获取控制是否跳天参数
                                    isR = ji.getInteger("isR");
                                    // 获取任务余剩时间
                                    zon = ji.getLong("zon");
                                    break;
                                } else if (isP == 4) {
                                    // 获取任务余剩时间
                                    zon = ji.getLong("zon");
                                    isL = 1;
                                }
                                else {
                                    isL = 1;
                                }
                            } else {
//                                System.out.println("进入这里-2-1");
                                // 判断当前任务优先级小于对比任务信息1的优先级
                                if (task.getPriority() < task1.getPriority()) {
                                    // 判断当前任务的开始时间大于等于对比任务信息1的开始时间，并且当前任务的开始时间小于对比任务信息1的结束时间
                                    if (task.getTePStart() >= task1.getTePStart() && task.getTePStart() < task1.getTePFinish()) {
                                        isL = 3;
                                        System.out.println("进入-冲突-中间-这里-1");
                                        jiLJ.add("进入-冲突-中间-这里-1");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isR = 2;
                                        break;
                                    } else
                                    if (task1.getTePStart() >= task.getTePStart() && task1.getTePFinish() <= task.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-2");
                                        jiLJ.add("进入-冲突-中间-这里-2");
                                        isL = 4;
                                        break;
                                    } else if (task.getTePFinish() > task1.getTePStart() && task.getTePFinish() < task1.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-3");
                                        jiLJ.add("进入-冲突-中间-这里-3");
                                        isL = 5;
                                        break;
                                    } else {
                                        System.out.println("进入输出ji---1:");
                                        // 调用计算空插时间方法
                                        JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate,dep,grpB,zOk);
                                        // 获取控制是否结束循环参数
                                        isP = ji.getInteger("isP");
                                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                        if (isP == 2) {
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isR = ji.getInteger("isR");
                                            // 获取任务余剩时间
                                            zon = ji.getLong("zon");
                                            break;
                                        } else if (isP == 4) {
                                            // 获取任务余剩时间
                                            zon = ji.getLong("zon");
                                        }
                                    }
                                } else if (task.getPriority() < task2.getPriority()) {
                                    // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                    if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                        isL = 3;
                                        System.out.println("进入-冲突-最后面-这里-1");
                                        jiLJ.add("进入-冲突-最后面-这里-1");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isR = 2;
                                        break;
                                    } else
                                    if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                        isL = 4;
//                                        System.out.println("进入-冲突-最后面-这里-2");
                                        // 调用处理时间冲突方法
                                        JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 0, sho,csSta);
                                        // 获取任务余剩时间
                                        zon = ct1.getLong("zon");
                                        // 获取存储问题状态参数
                                        isPd = ct1.getInteger("isPd");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isR = 2;
                                        System.out.println("进入-冲突-最后面-这里-2");
                                        break;
                                    } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                        System.out.println("进入-冲突-最后面-这里-3");
                                        jiLJ.add("进入-冲突-最后面-这里-3");
                                        isL = 5;
                                        break;
                                    } else {
                                        System.out.println("进入输出ji---1-2:");
                                        // 调用计算空插时间方法
                                        JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate,dep,grpB,zOk);
                                        // 获取控制是否结束循环参数
                                        isP = ji.getInteger("isP");
                                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                        if (isP == 2) {
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isR = ji.getInteger("isR");
                                            // 获取任务余剩时间
                                            zon = ji.getLong("zon");
                                            break;
                                        } else if (isP == 4) {
                                            // 获取任务余剩时间
                                            zon = ji.getLong("zon");
                                        }
                                    }
                                } else {
//                                    System.out.println("--优先级跳过--");
                                    // 获取余剩时间（对比任务2的开始时间-对比任务1的结束时间）
                                    long s = task2.getTePStart() - task1.getTePFinish();
                                    // 判断余剩时间大于0
                                    if (s > 0) {
                                        // 获取时间差（余剩时间-当前任务总时间）
                                        long cha = s - task.getTeDurTotal();
                                        // 判断时间差大于等于0
                                        if (cha >= 0) {
//                                            System.out.println("--优先级跳过--1");
                                            Task task3 = Obj.getTaskX(task1.getTePFinish(),(task1.getTePFinish()+task.getTeDurTotal())
                                                    ,task.getTeDurTotal(),task);
                                            // 指定任务集合下标为任务下标+1添加任务信息
                                            tasks.add(i+1,task3);
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random,grpB,dep)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                                                    ,zOk,random);
                                            re.put("xFin",task3.getTePFinish());
                                            // 任务余剩时间累减
                                            zon -= task.getTeDurTotal();
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isR = 1;
                                            break;
                                        } else {
//                                            System.out.println("--优先级跳过--2");
                                            // 获取时间差2（当前任务总时间-余剩时间）
                                            long cha2 = task.getTeDurTotal() - s;
                                            Task task3 = Obj.getTaskX(task1.getTePFinish(),(task1.getTePFinish()+s),s,task);
//                                            System.out.println(JSON.toJSONString(task3));
                                            // 指定任务集合下标为任务下标+1添加任务信息
                                            tasks.add(i+1,task3);
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random,grpB,dep)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                                                    ,zOk,random);
                                            // 更新任务总时间
                                            task.setTeDurTotal(cha2);
                                            // 更新任务开始时间
                                            task.setTePStart(task1.getTePStart());
                                            // 更新任务结束时间
                                            task.setTePFinish(task1.getTePFinish());
                                            // 任务余剩时间累减
                                            zon -= s;
                                        }
                                    }
                                }
                            }
                        } else {
//                            System.out.println("进入else");
                            // 判断当前任务优先级小于对比任务2的优先级
                            if (task.getPriority() < task2.getPriority()) {
                                // 判断当前任务开始时间小于对比任务2的开始时间，并且当前任务的开始时间小于对比任务2的结束时间
                                if (task.getTePStart() < task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                    isL = 3;
//                                    System.out.println("进入-冲突-新最开始-这里-1");
                                    // 调用处理时间冲突方法
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 1, sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1.getLong("zon");
                                    // 获取存储问题状态参数
                                    isPd = ct1.getInteger("isPd");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    System.out.println("进入-冲突-新最开始-这里-1");
                                    break;
                                } else {
//                                    System.out.println("进入else-1");
                                    // 调用计算空插时间方法
                                    JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate,dep,grpB,zOk);
                                    // 获取控制是否结束循环参数
                                    isP = ji.getInteger("isP");
                                    // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                    if (isP == 2) {
                                        // 获取控制是否跳天参数
                                        isR = ji.getInteger("isR");
                                        // 获取任务余剩时间
                                        zon = ji.getLong("zon");
                                        break;
                                    } else if (isP == 4) {
                                        // 获取任务余剩时间
                                        zon = ji.getLong("zon");
                                    }
                                }
                            } else if (task.getPriority() < task1.getPriority()){
                                System.out.println("进入-冲突-新最开始-这里-1--1");
                                jiLJ.add("进入-冲突-新最开始-这里-1--1");
                            } else {
//                                System.out.println("进入else-2");
                                // 调用计算空插时间方法
                                JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate,dep,grpB,zOk);
                                // 获取控制是否结束循环参数
                                isP = ji.getInteger("isP");
                                // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                if (isP == 2) {
                                    // 获取控制是否跳天参数
                                    isR = ji.getInteger("isR");
                                    // 获取任务余剩时间
                                    zon = ji.getLong("zon");
                                    break;
                                } else if (isP == 4) {
                                    // 获取任务余剩时间
                                    zon = ji.getLong("zon");
                                }
                            }
                        }
                    }
                }
                // 判断冲突信息
                if (isL >= 3) {
                    System.out.println("出现时间冲突!isL:"+isL);
                }
            }
            // 调用处理[时间处理方法2]结束操作的方法
            hTeStart = getIsT(isR, isC, tasks, grpB, dep, random, zon, isK, hTeStart, task, id_O, index, teDate, teDaF,sho,isPd,isYx,csSta);
//            System.out.println("返回后:"+hTeStart);
        } else {
            // 存储任务冲突信息参数：isL == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
            int isL = 0;
            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
            int isR = 0;
            // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
            int isPd = 0;
            // 遍历任务集合
            for (int i = 0; i < tasks.size(); i++) {
                // 根据下标获取对比任务信息1
                Task task1 = tasks.get(i);
                // 判断下标加1小于任务集合长度
                if ((i + 1) < tasks.size()) {
                    // 根据下标获取对比任务信息2
                    Task task2 = tasks.get(i + 1);
//                    System.out.println("task-x-zon:");
//                    System.out.println(JSON.toJSONString(task));
//                    System.out.println(JSON.toJSONString(task1));
//                    System.out.println(JSON.toJSONString(task2));
                    // 判断进入条件
                    // isT : 保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
                    // isK : 控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
                    if (isT == 0 || isK == 1) {
                        isK = 0;
                        // 判断当前任务的开始时间大于等于对比任务1的结束时间
                        if (task.getTePStart() >= task1.getTePFinish()) {
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < task2.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                    isL = 3;
//                                    System.out.println("进入-x-冲突-最开始-这里-1");
                                    // 调用处理时间冲突方法
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 0, sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1.getLong("zon");
                                    // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isPd = ct1.getInteger("isPd");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    System.out.println("进入-x-冲突-最开始-这里-1");
                                    break;
                                } else
                                if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-2");
                                    jiLJ.add("进入-x-冲突-最开始-这里-2");
                                    isL = 4;
                                    break;
                                } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3");
                                    jiLJ.add("进入-x-冲突-最开始-这里-3");
                                    isL = 5;
                                    break;
                                }
                            } else if (task.getPriority() < task1.getPriority()){
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
//                                    System.out.println("进入-x-冲突-最开始-这里-1--1");
                                    isL = 33;
                                    // 调用处理时间冲突方法
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 0, sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1.getLong("zon");
                                    // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isPd = ct1.getInteger("isPd");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    System.out.println("进入-x-冲突-最开始-这里-1--1");
                                    break;
                                } else
                                if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-2--1");
                                    jiLJ.add("进入-x-冲突-最开始-这里-2--1");
                                    isL = 44;
                                    break;
                                } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3--1");
                                    jiLJ.add("进入-x-冲突-最开始-这里-3--1");
                                    isL = 55;
                                    break;
                                }
                            }
//                            System.out.println("弹回---");
                            isK = 1;
                        } else {
//                            System.out.println("进入这里-2-2");
                            // 判断当前任务优先级小于对比任务1的优先级
                            if (task.getPriority() < task1.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务信息1的开始时间，并且当前任务的开始时间小于对比任务信息1的结束时间
                                if (task.getTePStart() >= task1.getTePStart() && task.getTePStart() < task1.getTePFinish()) {
                                    isL = 3;
                                    System.out.println("进入-x-冲突-中间-这里-1");
                                    jiLJ.add("进入-x-冲突-中间-这里-1");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    break;
                                } else
                                if (task1.getTePStart() >= task.getTePStart() && task1.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-2");
                                    jiLJ.add("进入-x-冲突-中间-这里-2");
                                    isL = 4;
                                    break;
                                } else if (task.getTePFinish() > task1.getTePStart() && task.getTePFinish() < task1.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-3");
                                    jiLJ.add("进入-x-冲突-中间-这里-3");
                                    isL = 5;
                                    break;
                                }
                            } else if (task.getPriority() < task2.getPriority()) {
                                if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                    isL = 3;
                                    System.out.println("进入-x-冲突-最后面-这里-1");
                                    jiLJ.add("进入-x-冲突-最后面-这里-1");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    break;
                                } else
                                if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                    isL = 4;
//                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    // 调用处理时间冲突方法
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 0, sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1.getLong("zon");
                                    // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isPd = ct1.getInteger("isPd");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    break;
                                } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最后面-这里-3");
                                    jiLJ.add("进入-x-冲突-最后面-这里-3");
                                    isL = 5;
                                    break;
                                }
                            } else {
                                System.out.println("--x-优先级跳过--");
                            }
                        }
                    } else {
                        // 判断当前任务优先级小于对比任务2的优先级
                        if (task.getPriority() < task2.getPriority()) {
                            // 判断当前任务开始时间小于对比任务2的开始时间，并且当前任务的开始时间小于对比任务2的结束时间
                            if (task.getTePStart() < task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                isL = 3;
//                                System.out.println("进入-x-冲突-新最开始-这里-1");
                                // 调用处理时间冲突方法
                                JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB, dep, teDate, teDaF, isC, 1, sho,csSta);
                                // 获取任务余剩时间
                                zon = ct1.getLong("zon");
                                // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                isPd = ct1.getInteger("isPd");
                                // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                isR = 2;
                                System.out.println("进入-x-冲突-新最开始-这里-1");
                                break;
                            }
                        } else if (task.getPriority() < task1.getPriority()){
                            System.out.println("进入-x-冲突-新最开始-这里-1--1");
                            jiLJ.add("进入-x-冲突-新最开始-这里-1--1");
                        }
                    }
                }
            }
            if (isL >= 3) {
                System.out.println("-x-出现时间冲突!isL:"+isL);
            }
            // 调用处理[时间处理方法2]结束操作的方法
            hTeStart = getIsT(isR, isC, tasks, grpB, dep, random, zon, isK, hTeStart, task, id_O, index, teDate, teDaF,sho,isPd,isYx,csSta);
        }
        re.put("hTeStart",hTeStart);
        return re;
    }

    /**
     * 处理[时间处理方法2]结束操作的方法
     * @param isR	控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param tasks	任务集合
     * @param grpB	组别
     * @param dep	部门
     * @param random	当前唯一编号
     * @param zon	任务余剩时间
     * @param isK	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param hTeStart	任务最初始开始时间
     * @param task	当前处理任务信息
     * @param id_O	订单编号
     * @param index	订单下标
     * @param teDate	当前处理的任务的所在日期对象
     * @param teDaF	当前任务所在日期对象
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isPd	存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
     * @param isYx	保存是否出现任务为空异常:isYx = 0 正常操作未出现异常、isYx = 1 出现拿出任务为空异常镜像|数据库
     * @return long  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 22:21
     */
    private long getIsT(int isR,int isC,List<Task> tasks,String grpB,String dep,String random,Long zon,int isK,Long hTeStart
            ,Task task,String id_O,int index,JSONObject teDate,JSONObject teDaF
            ,JSONObject sho,int isPd,int isYx,int csSta){
        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
        if (isR == 0) {
            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 0) {
                setTasksAndZon(tasks,grpB,dep,getTeS(random,grpB,dep),task.getId_C(),false,zon,coupaUtil);
            } else {
                // 调用写入镜像任务集合方法
                setTasksF(tasks,grpB,dep,getTeS(random,grpB,dep),random);
                // 调用写入镜像任务余剩时间方法
                setZonF(zon,grpB,dep,getTeS(random,grpB,dep),random);
            }
            // 根据当前唯一编号获取最新的（最后一个）当前时间戳
            JSONObject rand = onlyStartMapAndDep.getJSONObject(random);
            // 根据部门获取最新的（最后一个）当前时间戳
            JSONObject onlyDep = rand.getJSONObject(dep);
            // 根据组别获取最新的（最后一个）当前时间戳
            Long aLong = onlyDep.getLong(grpB);
            // 根据部门添加最新的（最后一个）当前时间戳
            onlyDep.put(grpB,(aLong+86400L));
            // 根据部门添加最新的（最后一个）当前时间戳
            rand.put(dep,onlyDep);
            // 根据当前唯一编号添加最新的（最后一个）当前时间戳
            onlyStartMapAndDep.put(random,rand);
//            System.out.println("这里跳天调用-1:"+isK);
            // 判断跳天强制停止参数等于55
            if (yiShu == 55) {
                System.out.println("进入强制停止-----1");
                // 赋值强制停止出现后的记录参数
                isQzTz = 1;
                return hTeStart;
            }
            // 调用时间处理方法
            JSONObject jsonObject = chkInJi(task, hTeStart, grpB, dep, id_O
                    , index, 1, random, isK, teDate, teDaF, isC, sho,isYx,csSta,false);
            // 获取任务最初始开始时间
            hTeStart = jsonObject.getLong("hTeStart");
        } else {
            // 获取任务最初始开始时间
            hTeStart = task.getTePFinish();
//            System.out.println("最后返回:"+hTeStart+"-isPd:"+isPd);
            // isC – isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 0) {
                setTasksAndZon(tasks,grpB,dep,getTeS(random,grpB,dep),task.getId_C(),false,zon,coupaUtil);
            } else {
                // isPd – 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                if (isPd != 2) {
                    // 调用写入镜像任务集合方法
                    setTasksF(tasks,grpB,dep,getTeS(random,grpB,dep),random);
                    // 调用写入镜像任务余剩时间方法
                    setZonF(zon,grpB,dep,getTeS(random,grpB,dep),random);
                }
            }
        }
        return hTeStart;
    }

    /**
     * 冲突处理核心方法
     * @param task	当前任务信息
     * @param task1	对比任务信息1
     * @param task2	对比任务信息2
     * @param zon	任务余剩时间
     * @param tasks	任务集合
     * @param i	任务下标
     * @param conflict	冲突任务集合
     * @param teSB 当前时间戳
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param teDate	当前处理的任务的所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param ts    ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho 用于存储判断镜像是否是第一个被冲突的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/21 1:39
     */
    private JSONObject conflictHandle(Task task,Task task1,Task task2,Long zon,List<Task> tasks,int i
            ,List<Task> conflict,long teSB,String random,String grpB,String dep
            ,JSONObject teDate,Integer isC,Integer ts,JSONObject sho,int csSta){
//        System.out.println("进入conflictHandle...");
//        System.out.println(JSON.toJSONString(task));
//        System.out.println(JSON.toJSONString(task1));
//        System.out.println(JSON.toJSONString(task2));
        // 创建创建返回结果对象
        JSONObject re = new JSONObject();
        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
        re.put("jie",0);
        // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
        int isD2 = onlyIsDs.getInteger(random);
        // 存储任务是否被处理完状态参数：isJ == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        int isJ = 0;
        // 获取余剩时间（对比任务2的开始时间-对比任务1的结束时间）
        long s = task2.getTePStart() - task1.getTePFinish();
        /*
         * 存储冲突处理模式参数：
         * 1、isP == 0 正常处理（使用所有冲突处理方法）不携带任务集合当前下标往后的循环处理
         * 2、isP == 1 携带任务集合当前下标往后的循环处理 -
         * 3、isP == 2 对比任务2开始时间加上当前任务总时间大于对比任务3的开始时间快速处理冲突模式
         * 4、isP == 3 对比任务3不属于系统任务快速处理冲突模式
         * 5、isP == 4 正常处理（使用所有冲突处理方法）携带任务集合当前下标往后的循环处理
         */
        int isP = 0;
        // 控制任务下标累加数参数：iZ == 1 下标加1、iZ == 2 下标加2
        int iZ = 1;
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer zOk = sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk");
        // ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
        if (ts == 1) {
            isP = 4;
            // 获取对比任务3
            Task task3X = tasks.get(i+2);
            // 判断对比任务是系统任务
            if (task3X.getPriority() == -1) {
                // 获取开始时间（对比任务2的开始时间+当前任务的总时间）
                long kai = task2.getTePStart() + task.getTeDurTotal();
                // 判断开始时间大于对比任务3的开始时间
                if (kai > task3X.getTePStart()) {
                    iZ = 2;
                    // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                    if (isD2 == 0) {
                        // 获取时间差（对比任务3的开始时间-当前任务的开始时间）
                        long cha = task3X.getTePStart() - task.getTePStart();
                        // 获取余剩总时间（当前任务总时间-时间差）
                        s = task.getTeDurTotal() - cha;
                        // 任务余剩时间累加
                        zon += task2.getTeDurTotal();
                        // 冲突任务集合添加对比任务2的任务信息
                        conflict.add(Obj.getTaskX(task.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                        // 调用添加或更新产品状态方法
                        addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                        // 任务余剩时间累减
                        zon -= cha;
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.set(i+1,Obj.getTaskX(task2.getTePStart(), task3X.getTePStart(),cha,task));
                        // 更新当前任务的开始时间
                        task.setTePStart(task.getTePStart());
                    } else {
                        // 获取时间差（对比任务3的开始时间-对比任务2的开始时间）
                        long cha = task3X.getTePStart() - task2.getTePStart();
                        // 获取余剩总时间（当前任务总时间-时间差）
                        s = task.getTeDurTotal() - cha;
                        // 任务余剩时间累加
                        zon += task2.getTeDurTotal();
                        // 冲突任务集合添加对比任务2的任务信息
                        conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                        // 调用添加或更新产品状态方法
                        addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                        // 任务余剩时间累减
                        zon -= cha;
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.set(i+1,Obj.getTaskX(task2.getTePStart(), task3X.getTePStart(),cha,task));
                        // 更新当前任务的开始时间
                        task.setTePStart(task2.getTePStart());
                    }
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(getTeS(random,grpB,dep)+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
                    // 更新当前任务的总时间
                    task.setTeDurTotal(s);
                    // 更新任务的结束时间
                    task.setTePFinish(task3X.getTePStart());
//                    System.out.println("备用--");
                } else {
                    isP = 2;
                }
            } else {
                isP = 3;
            }

            if (isP == 2 || isP == 3) {
                // 获取开始时间（对比任务2的开始时间+当前任务的总时间）
                long kai = task2.getTePStart()+task.getTeDurTotal();
                // 判断开始时间大于对比任务3的开始时间
                if (kai > task3X.getTePFinish()) {
//                    System.out.println("进入isP-2|3-大于:");
                    // 获取时间差2（当前任务的总时间-对比任务2的总时间）
                    long cha2 = task.getTeDurTotal() - task2.getTeDurTotal();
                    // 更新当前任务的总时间
                    task.setTeDurTotal(cha2);
                    // 任务余剩时间累加
                    zon += task2.getTeDurTotal();
                    // 冲突任务集合添加对比任务2的任务信息
                    conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                    // 任务余剩时间累减
                    zon -= task2.getTeDurTotal();
                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                    tasks.set(i+1,Obj.getTaskX(task2.getTePStart(),(task2.getTePStart()+task2.getTeDurTotal())
                            ,task2.getTeDurTotal(),task));
                    isP = 1;
                } else {
//                    System.out.println("进入isP-2|3-小于等于:");
                    // 任务余剩时间累加
                    zon += task2.getTeDurTotal();
                    // 冲突任务集合添加对比任务2的任务信息
                    conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                    // 任务余剩时间累减
                    zon -= task.getTeDurTotal();
                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                    tasks.set(i+1,Obj.getTaskX(task2.getTePStart(),(task2.getTePStart()+task.getTeDurTotal())
                            ,task.getTeDurTotal(),task));
                }
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(getTeS(random,grpB,dep)+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep)
                        ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
            }
        }
        if (isP == 0) {
            // 判断余剩总时间大于0
            if (s > 0) {
                // 获取时间差（余剩总时间-当前任务的开始时间）
                long cha = s - task.getTeDurTotal();
                // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                if (isD2 == 0) {
                    // 判断时间差大于等于0
                    if (cha >= 0) {
                        // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                        tasks.add(i+1,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal())
                                ,task.getTeDurTotal(),task));
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(getTeS(random,grpB,dep)+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                        // 任务余剩时间累减
                        zon -= task.getTeDurTotal();
                        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                        re.put("jie",2);
                        re.put("zon",zon);
                        re.put("hTeStart",(task.getTePStart()+task.getTeDurTotal()));
                        re.put("teSB",teSB);
                        isJ = 1;
                        re.put("isJ",isJ);
//                        System.out.println("进入这里--=1");
                        return re;
                    } else {
                        // 获取时间差2（当前任务的总时间-余剩总时间）
                        long cha2 = task.getTeDurTotal() - s;
                        // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                        tasks.add(i+1,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+s),s,task));
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(getTeS(random,grpB,dep)+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                        // 更新当前任务的总时间
                        task.setTeDurTotal(cha2);
                        // 更新当前任务的开始时间
                        task.setTePStart((task.getTePStart()+s));
                        // 更新任务的结束时间
                        task.setTePFinish((task.getTePStart()+s)+task.getTeDurTotal());
//                        System.out.println("进入这里++=1");
                        isD2 = 1;
                        // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                        onlyIsDs.put(random,1);
                        // 任务余剩时间累减
                        zon -= s;
                    }
                } else {
                    // 判断时间差大于等于0
                    if (cha >= 0) {
                        // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                        tasks.add(i+1,Obj.getTaskX(task1.getTePFinish(),(task1.getTePFinish()+task.getTeDurTotal())
                                ,task.getTeDurTotal(),task));
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(getTeS(random,grpB,dep)+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                        // 任务余剩时间累减
                        zon -= task.getTeDurTotal();
                        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                        re.put("jie",2);
                        re.put("zon",zon);
                        re.put("hTeStart",(task1.getTePFinish()+task.getTeDurTotal()));
                        re.put("teSB",teSB);
                        isJ = 1;
                        re.put("isJ",isJ);
//                        System.out.println("进入这里--=2");
                        return re;
                    } else {
                        // 获取时间差2（当前任务的总时间-余剩总时间）
                        long cha2 = task.getTeDurTotal() - s;
                        // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                        tasks.add(i+1,Obj.getTaskX(task1.getTePFinish(),(task1.getTePFinish()+s),s,task));
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(getTeS(random,grpB,dep)+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                        // 更新当前任务的总时间
                        task.setTeDurTotal(cha2);
                        // 更新当前任务的开始时间
                        task.setTePStart((task1.getTePFinish()+s));
                        // 更新任务的结束时间
                        task.setTePFinish((task1.getTePFinish()+s)+task.getTeDurTotal());
//                        System.out.println("进入这里++=2");
                        // 任务余剩时间累减
                        zon -= s;
                    }
                }
            }
            // 判断当前任务的结束时间小于等于对比任务2的开始时间
            if (task.getTePFinish() <= task2.getTePStart()) {
//                System.out.println("进入这个地方-1");
                // 判断当前任务的优先级小于对比任务1的优先级
                if (task.getPriority() < task1.getPriority()) {
                    // 任务余剩时间累加
                    zon += task1.getTeDurTotal();
                    // 冲突任务集合添加对比任务1的任务信息
                    conflict.add(Obj.getTaskX(task1.getTePStart(), task1.getTePFinish(),task1.getTeDurTotal(),task1));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), task1.getId_O(),task1.getIndex().toString(),0);
                    // 任务余剩时间累减
                    zon -= task.getTeDurTotal();
                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                    tasks.set(i,Obj.getTaskX(task1.getTePStart(),(task1.getTePStart()+task.getTeDurTotal())
                            ,task.getTeDurTotal(),task));
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(getTeS(random,grpB,dep)+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
                    // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                    re.put("jie",2);
//                    System.out.println("进入这里--=3");
                    isJ = 1;
                } else {
                    // 判断当前任务的优先级小于对比任务2的优先级
                    if (task.getPriority() < task2.getPriority()) {
                        // 任务余剩时间累加
                        zon += task2.getTeDurTotal();
                        // 冲突任务集合添加对比任务2的任务信息
                        conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                        // 调用添加或更新产品状态方法
                        addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                        // 任务余剩时间累减
                        zon -= task.getTeDurTotal();
                        // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                        tasks.set(i+1,Obj.getTaskX(task2.getTePStart(),(task2.getTePStart()+task.getTeDurTotal())
                                ,task.getTeDurTotal(),task));
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(getTeS(random,grpB,dep)+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
                        isJ = 1;
//                        System.out.println("进入这里--=4");
                    }
//                    else {
//                        isJ = 0;
//                    }
                }
                re.put("zon",zon);
                re.put("hTeStart",task.getTePFinish());
                re.put("teSB",teSB);
                re.put("isJ",isJ);
                return re;
            } else if (task.getTePFinish() <= task2.getTePFinish()) {
//                System.out.println("进入这个地方-2");
                // 判断当前任务的优先级小于对比任务1的优先级
                if (task.getPriority() < task1.getPriority()) {
                    // 任务余剩时间累加
                    zon += task1.getTeDurTotal();
                    // 冲突任务集合添加对比任务1的任务信息
                    conflict.add(Obj.getTaskX(task1.getTePStart(),task1.getTePFinish(),task1.getTeDurTotal(),task1));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), task1.getId_O(),task1.getIndex().toString(),0);
                    // 任务余剩时间累减
                    zon -= task.getTeDurTotal();
                    // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                    tasks.set(i,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal())
                            ,task.getTeDurTotal(),task));
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(getTeS(random,grpB,dep)+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
//                    System.out.println("进入这里--=5");
                    // 判断当前任务的优先级小于对比任务2的优先级
                    if (task.getPriority() < task2.getPriority()) {
                        // 判断当前任务的开始时间大于等于对比任务2的开始时间，并且当前任务的开始时间小于等于对比任务2的结束时间
                        if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() <= task2.getTePFinish()) {
                            // 任务余剩时间累加
                            zon += task2.getTeDurTotal();
                            // 冲突任务集合添加对比任务2的任务信息
                            conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                            // 任务集合删除指定下标(i+1)任务
                            tasks.remove((i+1));
                        }
                    }
                } else {
                    // 判断当前任务的优先级小于对比任务2的优先级
                    if (task.getPriority() < task2.getPriority()) {
                        // 判断当前任务的开始时间大于等于对比任务2的开始时间，并且当前任务的开始时间小于等于对比任务2的结束时间
                        if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() <= task2.getTePFinish()) {
                            // 任务余剩时间累加
                            zon += task2.getTeDurTotal();
                            // 冲突任务集合添加对比任务2的任务信息
                            conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i+1,Obj.getTaskX(-1L,(task.getTePStart()+task.getTeDurTotal()),-1L,task));
                            // 添加当前处理的任务的所在日期对象状态
                            teDate.put(getTeS(random,grpB,dep)+"",0);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
//                            System.out.println("进入这里--=6");
                        }
                    }
                }
                // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                re.put("jie",2);
                re.put("zon",zon);
                re.put("hTeStart",task.getTePFinish());
                re.put("teSB",teSB);
                isJ = 1;
                re.put("isJ",isJ);
                return re;
            } else {
                isP = 1;
//                System.out.println("进入这个地方-3");
                // 判断当前任务的优先级小于对比任务1的优先级
                if (task.getPriority() < task1.getPriority()) {
//                    System.out.println("进入这个地方-3-1");
                    // 任务余剩时间累加
                    zon += task1.getTeDurTotal();
                    // 冲突任务集合添加对比任务1的任务信息
                    conflict.add(Obj.getTaskX(task1.getTePStart(),task1.getTePFinish(),task1.getTeDurTotal(),task1));
                    // 调用添加或更新产品状态方法
                    addSho(sho, task.getId_O(),task.getIndex().toString(), task1.getId_O(),task1.getIndex().toString(),0);
                    // 判断当前任务的优先级小于对比任务2的优先级
                    if (task.getPriority() < task2.getPriority()) {
                        // 判断对比任务2的优先级等于系统
                        if (task2.getPriority() == -1) {
                            // 获取余剩总时间（对比任务2的开始时间-当前任务的开始时间）
                            s = task2.getTePStart() - task.getTePStart();
                            // 获取时间差（当前任务总时间-余剩总时间）
                            long cha = task.getTeDurTotal() - s;
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+s),s,task));
                            // 更新当前任务的总时间
                            task.setTeDurTotal(cha);
                            // 更新当前任务的开始时间
                            task.setTePStart(task2.getTePFinish());
                            // 更新任务的结束时间
                            task.setTePFinish(task2.getTePFinish());
//                            System.out.println("进入这里++=3");
                            // 任务余剩时间累减
                            zon -= s;
                        } else {
                            // 任务余剩时间累加
                            zon += task2.getTeDurTotal();
                            // 冲突任务集合添加对比任务2的任务信息
                            conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                            // 任务集合删除指定下标(i+1)任务
                            tasks.remove(i+1);
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i,Obj.getTaskX(task2.getTePStart(),(task2.getTePStart()+task.getTeDurTotal())
                                    ,task.getTeDurTotal(),task));
//                            System.out.println("进入这里--=7");
                        }
                    } else {
//                        System.out.println("进入新开辟的-1");
                        // 判断对比任务1的总时间大于当前任务的总时间
                        if (task1.getTeDurTotal() > task.getTeDurTotal()) {
//                            System.out.println("大于:");
                            isP = 3;
                            // 任务余剩时间累减
                            zon -= task.getTeDurTotal();
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i,Obj.getTaskX(task1.getTePStart(),(task1.getTePStart()+task.getTeDurTotal())
                                    ,task.getTeDurTotal(),task));
                        } else {
//                            System.out.println("小于等于:");
                            // 获取余剩总时间（当前任务的总时间-对比任务1的总时间）
                            s = task.getTeDurTotal() - task1.getTeDurTotal();
                            // 任务余剩时间累减
                            zon -= task1.getTeDurTotal();
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i,Obj.getTaskX(task1.getTePStart(),task1.getTePFinish(),task1.getTeDurTotal(),task));
                            // 更新当前任务的总时间
                            task.setTeDurTotal(s);
                        }
                    }
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(getTeS(random,grpB,dep)+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
                } else {
//                    System.out.println("进入这个地方-3-2");
                    // 判断对比任务2的优先级等于系统
                    if (task2.getPriority() == -1) {
                        // 获取余剩总时间（对比任务2的开始时间-当前任务的开始时间）
                        s = task2.getTePStart() - task.getTePStart();
                        // 获取时间差（当前任务总时间-余剩总时间）
                        long cha = task.getTeDurTotal() - s;
                        // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                        tasks.set(i,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+s),s,task));
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(getTeS(random,grpB,dep)+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                        // 更新当前任务的总时间
                        task.setTeDurTotal(cha);
                        // 更新当前任务的开始时间
                        task.setTePStart(task2.getTePFinish());
                        // 更新任务的结束时间
                        task.setTePFinish(task2.getTePFinish());
//                        System.out.println("进入这里++=4");
                        // 任务余剩时间累减
                        zon -= s;
                    } else {
                        // 判断当前任务的优先级小于对比任务2的优先级
                        if (task.getPriority() < task2.getPriority()) {
                            // 任务余剩时间累加
                            zon += task2.getTeDurTotal();
                            // 冲突任务集合添加对比任务2的任务信息
                            conflict.add(Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task2));
                            // 调用添加或更新产品状态方法
                            addSho(sho, task.getId_O(),task.getIndex().toString(), task2.getId_O(),task2.getIndex().toString(),0);
                            // 获取对比任务3
                            Task task3 = tasks.get(i + 2);
                            // 判断对比任务3的优先级等于系统
                            if (task3.getPriority() == -1) {
//                                System.out.println(JSON.toJSONString(tasks.get(i+1)));
                                // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                if (isD2 == 0) {
                                    // 获取时间差（对比任务3的开始时间-当前任务的开始时间）
                                    long cha = task3.getTePStart() - task.getTePStart();
                                    // 获取余剩总时间（当前任务总时间-时间差）
                                    s = task.getTeDurTotal() - cha;
                                    // 更新当前任务总时间
                                    task.setTeDurTotal(s);
                                    // 任务余剩时间累减
                                    zon -= cha;
                                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                    tasks.set(i+1,Obj.getTaskX(task.getTePStart(),task3.getTePStart(),cha,task));
                                    // 更新当前任务的开始时间
                                    task.setTePStart(task.getTePStart());
//                                    System.out.println("进入这里--=8");
                                } else {
                                    // 获取时间差（对比任务3的开始时间-对比任务2的开始时间）
                                    long cha = task3.getTePStart() - task2.getTePStart();
                                    // 获取余剩总时间（当前任务总时间-时间差）
                                    s = task.getTeDurTotal() - cha;
                                    // 更新当前任务总时间
                                    task.setTeDurTotal(s);
                                    // 任务余剩时间累减
                                    zon -= cha;
                                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                    tasks.set(i+1,Obj.getTaskX(task2.getTePStart(),task3.getTePStart(),cha,task));
                                    // 更新当前任务的开始时间
                                    task.setTePStart(task2.getTePStart());
//                                    System.out.println("进入这里--=9");
                                }
                                // 更新任务的结束时间
                                task.setTePFinish(task3.getTePStart());
                            } else {
                                // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                if (isD2 == 0) {
                                    // 获取时间差（对比任务2的结束时间-当前任务的开始时间）
                                    long cha = task2.getTePFinish() - task.getTePStart();
                                    // 获取余剩总时间（当前任务总时间-时间差）
                                    s = task.getTeDurTotal() - cha;
                                    // 任务余剩时间累减
                                    zon -= cha;
                                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                    tasks.set(i+1,Obj.getTaskX(task.getTePStart(),task2.getTePFinish(),cha,task));
//                                    System.out.println("进入这里--=22");
                                    // 更新当前任务的开始时间
                                    task.setTePStart(task.getTePStart());
                                } else {
                                    // 获取余剩总时间（当前任务总时间-对比任务2的总时间）
                                    s = task.getTeDurTotal() - task2.getTeDurTotal();
                                    // 任务余剩时间累减
                                    zon -= task2.getTeDurTotal();
                                    // 更新任务集合指定下标（i（任务下标）+1）的任务信息为当前任务信息
                                    tasks.set(i+1,Obj.getTaskX(task2.getTePStart(),task2.getTePFinish(),task2.getTeDurTotal(),task));
//                                    System.out.println("进入这里--=23");
                                }
                                // 更新当前任务总时间
                                task.setTeDurTotal(s);
                            }
                            // 添加当前处理的任务的所在日期对象状态
                            teDate.put(getTeS(random,grpB,dep)+"",0);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep)
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk"),random);
                            iZ = 2;
                            isD2 = 1;
                            // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                            onlyIsDs.put(random,1);
                        }
                    }
//                    System.out.println("备用--");
                }
            }
        }
        if (isP == 1 || isP == 4) {
            // 判断（i（当前任务下标）+iZ（控制任务下标累加数参数））小于任务集合总长度
            if ((i + iZ) < tasks.size()) {
//                System.out.println(JSON.toJSONString(task));
                // 遍历任务集合
                for (int i1 = i+iZ; i1 < tasks.size(); i1++) {
                    // 获取对比任务1
                    Task task2X = tasks.get(i1);
                    // 判断当前任务下标+1小于任务集合总长度
                    if ((i1+1) < tasks.size()) {
                        // 获取对比任务2
                        Task task3 = tasks.get(i1+1);
//                        System.out.println("进入最里面:");
                        // 判断对比任务1的优先级等于系统，并且对比任务2的优先级等于系统
                        if (task2X.getPriority() == -1 && task3.getPriority() == -1) {
                            // 获取余剩总时间（对比任务2的开始时间-对比任务1的结束时间）
                            s = task3.getTePStart() - task2X.getTePFinish();
                            // 判断余剩总时间大于0
                            if (s > 0) {
                                // 获取时间差（余剩总时间-当前任务总时间）
                                long cha = s - task.getTeDurTotal();
                                // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                                if (isD2 == 0) {
                                    // 判断时间差大于等于0
                                    if (cha >= 0) {
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(i1+1,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal())
                                                ,task.getTeDurTotal(),task));
                                        // 添加当前处理的任务的所在日期对象状态
                                        teDate.put(getTeS(random,grpB,dep)+"",0);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                        // 任务余剩时间累减
                                        zon -= task.getTeDurTotal();
                                        re.put("zon",zon);
                                        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                        re.put("jie",2);
                                        re.put("hTeStart",(task.getTePStart()+task.getTeDurTotal()));
                                        isJ = 1;
                                        // 更新当前任务的开始时间
                                        task.setTePStart(task.getTePStart());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish((task.getTePStart()+task.getTeDurTotal()));
//                                        System.out.println("进入这里++=5-2");
                                        break;
                                    } else {
                                        // 获取时间差2（当前任务总时间-余剩总时间）
                                        long cha2 = task.getTeDurTotal() - s;
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(i1+1,Obj.getTaskX(task.getTePFinish(),(task.getTePFinish()+s),s,task));
                                        // 添加当前处理的任务的所在日期对象状态
                                        teDate.put(getTeS(random,grpB,dep)+"",0);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                        // 更新当前任务的总时间
                                        task.setTeDurTotal(cha2);
                                        // 更新当前任务的开始时间
                                        task.setTePStart(task1.getTePStart());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish(task1.getTePFinish());
//                                        System.out.println("进入这里++=5");
                                        // 任务余剩时间累减
                                        zon -= s;
                                    }
                                } else {
                                    // 判断时间差大于等于0
                                    if (cha >= 0) {
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(i1+1,Obj.getTaskX(task2X.getTePFinish(),(task2X.getTePFinish()+task.getTeDurTotal())
                                                ,task.getTeDurTotal(),task));
                                        // 添加当前处理的任务的所在日期对象状态
                                        teDate.put(getTeS(random,grpB,dep)+"",0);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                        // 任务余剩时间累减
                                        zon -= task.getTeDurTotal();
                                        re.put("zon",zon);
                                        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                        re.put("jie",2);
                                        re.put("hTeStart",(task.getTePStart()+task.getTeDurTotal()));
                                        isJ = 1;
                                        // 更新当前任务的开始时间
                                        task.setTePStart(task2X.getTePFinish());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish((task2X.getTePFinish()+task.getTeDurTotal()));
//                                        System.out.println("进入这里--=10");
                                        break;
                                    } else {
                                        // 获取时间差2（当前任务总时间-余剩总时间）
                                        long cha2 = task.getTeDurTotal() - s;
                                        // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                        tasks.add(i1+1,Obj.getTaskX(task2X.getTePFinish(),(task2X.getTePFinish()+s),s,task));
                                        // 添加当前处理的任务的所在日期对象状态
                                        teDate.put(getTeS(random,grpB,dep)+"",0);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                        // 更新当前任务的总时间
                                        task.setTeDurTotal(cha2);
                                        // 更新当前任务的开始时间
                                        task.setTePStart(task1.getTePStart());
                                        // 更新当前任务的结束时间
                                        task.setTePFinish(task1.getTePFinish());
//                                        System.out.println("进入这里++=6");
                                        // 任务余剩时间累减
                                        zon -= s;
                                    }
                                }
                            }
                        } else {
                            // 判断当前任务优先级小于对比任务3的优先级
                            if (task.getPriority() < task3.getPriority()) {
//                                System.out.println("进入这里X-X-1");
                                // 判断当前任务的开始时间小于对比任务3的开始时间，并且当前任务的结束时间小于对比任务3的结束时间
                                if (task.getTePStart() < task3.getTePStart() && task.getTePFinish() < task3.getTePFinish()) {
//                                    System.out.println("进入这里X-X-1-1");
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject ct1F = getCt1F(task, task2X, task3, zon, tasks, i, i1, conflict, teSB
                                            , random, grpB, dep, teDate,isC,1,sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1F.getLong("zon");
                                    // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (ct1F.getInteger("jie") == 2) {
                                        isJ = 1;
                                        break;
                                    }
                                } else {
//                                    System.out.println("进入这里X-X-1-2");
                                    // 获取余剩总时间（对比任务3的开始时间-对比任务1的结束时间）
                                    s = task3.getTePStart() - task2X.getTePFinish();
                                    // 判断余剩总时间大于0
                                    if (s > 0) {
                                        // 获取时间差（余剩总时间-当前任务总时间）
                                        long cha = s - task.getTeDurTotal();
                                        // 判断时间差大于等于0
                                        if (cha >= 0) {
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(i1 + 1, Obj.getTaskX(task.getTePStart(),(task.getTePStart() + task.getTeDurTotal())
                                                    ,task.getTeDurTotal(), task));
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random, grpB, dep) + "", 0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                            // 任务余剩时间累减
                                            zon -= task.getTeDurTotal();
                                            re.put("zon", zon);
                                            // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                            re.put("jie", 2);
                                            re.put("hTeStart", (task.getTePStart() + task.getTeDurTotal()));
//                                            System.out.println("进入这里再出-1");
                                            isJ = 1;
                                            break;
                                        } else {
                                            // 获取时间差2（当前任务总时间-余剩总时间）
                                            long cha2 = task.getTeDurTotal() - s;
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(i1 + 1, Obj.getTaskX(task.getTePFinish(), (task.getTePFinish() + s), s, task));
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random, grpB, dep) + "", 0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                            // 更新当前任务的总时间
                                            task.setTeDurTotal(cha2);
                                            // 更新当前任务的开始时间
                                            task.setTePStart((task.getTePFinish() + s));
                                            // 更新当前任务的结束时间
                                            task.setTePFinish((task.getTePFinish() + s) + task.getTeDurTotal());
//                                            System.out.println("进入这里++=7");
                                            // 任务余剩时间累减
                                            zon -= s;
                                        }
                                    } else {
//                                        System.out.println("进入新的啊-");
                                        // 调用处理时间冲突方法复刻方法
                                        JSONObject ct1F = getCt1F(task, task2X, task3, zon, tasks, i, i1, conflict, teSB
                                                , random, grpB, dep, teDate,isC,1,sho,csSta);
                                        // 获取任务余剩时间
                                        zon = ct1F.getLong("zon");
                                        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                        if (ct1F.getInteger("jie") == 2) {
                                            isJ = 1;
                                            break;
                                        }
                                    }
                                }
                            } else {
//                                System.out.println("进入这里X-X-2");
                                // 判断当前任务的开始时间大于等于对比任务1的开始时间，并且当前任务的开始时间小于对比任务1的结束时间
                                if (task.getTePStart() >= task2X.getTePStart() && task.getTePStart() < task2X.getTePFinish()) {
//                                    System.out.println("进入这里-X-1");
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject ct1F = getCt1F(task, task2X, task3, zon, tasks, i, i1, conflict, teSB
                                            , random, grpB, dep, teDate,isC,0,sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1F.getLong("zon");
                                    // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (ct1F.getInteger("jie") == 2) {
                                        isJ = 1;
                                        break;
                                    }
                                } else if (task2X.getTePStart() >= task.getTePStart() && task2X.getTePFinish() <= task.getTePFinish()) {
//                                    System.out.println("进入这里-X-2");
                                    // 调用处理时间冲突方法复刻方法
                                    JSONObject ct1F = getCt1F(task, task2X, task3, zon, tasks, i, i1, conflict, teSB
                                            , random, grpB, dep, teDate,isC,0,sho,csSta);
                                    // 获取任务余剩时间
                                    zon = ct1F.getLong("zon");
                                    // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                    if (ct1F.getInteger("jie") == 2) {
                                        isJ = 1;
                                        break;
                                    }
                                } else if (task.getTePFinish() > task2X.getTePStart() && task.getTePFinish() < task2X.getTePFinish()) {
                                    System.out.println("进入这里-X-3");
                                } else {
                                    // 获取余剩总时间（对比任务3的开始时间-对比任务1的结束时间）
                                    s = task3.getTePStart() - task2X.getTePFinish();
                                    // 判断余剩总时间大于0
                                    if (s > 0) {
                                        // 获取时间差（余剩总时间-当前任务总时间）
                                        long cha = s - task.getTeDurTotal();
                                        // 判断时间差大于等于0
                                        if (cha >= 0) {
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(i1 + 1, Obj.getTaskX(task.getTePStart(), (task.getTePStart() + task.getTeDurTotal())
                                                    , task.getTeDurTotal(), task));
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random, grpB, dep) + "", 0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                            // 任务余剩时间累减
                                            zon -= task.getTeDurTotal();
                                            re.put("zon", zon);
                                            // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
                                            re.put("jie", 2);
                                            re.put("hTeStart", (task.getTePStart() + task.getTeDurTotal()));
//                                            System.out.println("进入这里再出-1");
                                            isJ = 1;
                                            break;
                                        } else {
                                            // 获取时间差2（当前任务总时间-余剩总时间）
                                            long cha2 = task.getTeDurTotal() - s;
                                            // 任务集合按照指定下标（i1（任务下标）+1）添加任务信息
                                            tasks.add(i1 + 1, Obj.getTaskX(task.getTePFinish(), (task.getTePFinish() + s), s, task));
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random, grpB, dep) + "", 0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep),zOk,random);
                                            // 更新当前任务的总时间
                                            task.setTeDurTotal(cha2);
                                            // 更新当前任务的开始时间
                                            task.setTePStart((task.getTePFinish() + s));
                                            // 更新当前任务的结束时间
                                            task.setTePFinish((task.getTePFinish() + s) + task.getTeDurTotal());
                                            // 任务余剩时间累减
                                            zon -= s;
//                                            System.out.println("进入这里再出-2");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isP >= 2) {
            isJ = 1;
//            System.out.println("进入这里--=x4:"+isP);
            re.put("zon",zon);
            re.put("hTeStart",task.getTePFinish());
            re.put("teSB",teSB);
            re.put("isJ",isJ);
            re.put("jie",2);
            return re;
        }
        re.put("zon",zon);
        re.put("teSB",teSB);
        re.put("isJ",isJ);
        return re;
    }

    /**
     * 添加或更新产品状态方法
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param taId_O	被获取产品状态的订单编号
     * @param taIndex	被获取产品状态的订单下标
     * @param ta2Id_O	添加产品状态的订单编号
     * @param ta2Index	添加产品状态的订单下标
     * @param teSq	teSq == 0 设置状态为不是被第一个处理时间的产品、teSq == 1 设置状态为被获取产品状态的状态
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 14:19
     */
    private void addSho(JSONObject sho,String taId_O,String taIndex,String ta2Id_O,String ta2Index,Integer teSq){
        JSONObject jsonObjectT = sho.getJSONObject(taId_O);
        JSONObject jsonObjectT2 = jsonObjectT.getJSONObject(taIndex);
        // 获取产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer zOk = jsonObjectT2.getInteger("zOk");
        // 判断产品状态为当前递归产品
        if (zOk == -1) {
            // 创建订单编号存储产品状态对象
            JSONObject js1 = new JSONObject();
            // 创建订单下标存储产品状态对象
            JSONObject js2 = new JSONObject();
            // 添加产品状态为第一个产品
            js2.put("zOk",1);
            // 添加当前订单编号和订单下标记录
            js2.put("z",ta2Id_O+"+"+ta2Index);
            // 添加订单下标存储产品状态对象
            js1.put(ta2Index,js2);
            // 添加订单编号存储产品状态对象
            sho.put(ta2Id_O,js1);
        } else {
            // 获取被获取产品状态的产品订单编号和订单下标记录
            String z = jsonObjectT2.getString("z");
            // 获取添加产品状态的订单编号产品状态信息
            JSONObject jsonObject = sho.getJSONObject(ta2Id_O);
            // teSq == 1 设置状态为被获取产品状态的状态
            if (teSq == 1) {
                // 判断添加产品状态的订单编号状态信息为空
                if (null == jsonObject) {
                    // 创建订单编号存储产品状态对象
                    JSONObject js1 = new JSONObject();
                    // 创建订单下标存储产品状态对象
                    JSONObject js2 = new JSONObject();
                    // 添加产品状态为被获取产品状态的状态
                    js2.put("zOk",zOk);
                    // 添加被获取产品状态的订单编号和订单下标记录
                    js2.put("z",z);
                    // 添加订单下标存储产品状态对象
                    js1.put(ta2Index,js2);
                    // 添加订单编号存储产品状态对象
                    sho.put(ta2Id_O,js1);
                } else {
                    // 获取添加产品状态的订单下标产品状态信息
                    JSONObject jsonObject2 = jsonObject.getJSONObject(ta2Index);
                    // 判断添加产品状态的订单下标状态信息为空
                    if (null == jsonObject2) {
                        // 创建订单下标存储产品状态对象
                        JSONObject js2 = new JSONObject();
                        // 添加产品状态为被获取产品状态的状态
                        js2.put("zOk",zOk);
                        // 添加被获取产品状态的订单编号和订单下标记录
                        js2.put("z",z);
                        // 添加订单下标存储产品状态对象
                        jsonObject.put(ta2Index,js2);
                        // 添加订单编号存储产品状态对象
                        sho.put(ta2Id_O,jsonObject);
                    } else {
                        // 判断添加产品状态的订单编号和订单下标记录不等于被获取产品状态的订单编号和订单下标记录
                        if (!jsonObject2.getString("z").equals(z)) {
                            // 创建订单编号存储产品状态对象
                            JSONObject js1 = new JSONObject();
                            // 创建订单下标存储产品状态对象
                            JSONObject js2 = new JSONObject();
                            // 添加产品状态为被获取产品状态的状态
                            js2.put("zOk",zOk);
                            // 添加被获取产品状态的订单编号和订单下标记录
                            js2.put("z",z);
                            // 添加订单下标存储产品状态对象
                            js1.put(ta2Index,js2);
                            // 添加订单编号存储产品状态对象
                            sho.put(ta2Id_O,js1);
                        }
                    }
                }
            } else {
                // teSq == 0 设置状态为不是被第一个处理时间的产品

                // 判断添加产品状态的订单编号状态信息为空
                if (null == jsonObject) {
                    // 创建订单编号存储产品状态对象
                    JSONObject js1 = new JSONObject();
                    // 创建订单下标存储产品状态对象
                    JSONObject js2 = new JSONObject();
                    // 添加产品状态为不是被第一个处理时间的产品
                    js2.put("zOk",2);
                    // 添加被获取产品状态的订单编号和订单下标记录
                    js2.put("z",z);
                    // 添加订单下标存储产品状态对象
                    js1.put(ta2Index,js2);
                    // 添加订单编号存储产品状态对象
                    sho.put(ta2Id_O,js1);
                } else {
                    // 获取添加产品状态的订单下标产品状态信息
                    JSONObject jsonObject2 = jsonObject.getJSONObject(ta2Index);
                    // 判断添加产品状态的订单下标状态信息为空
                    if (null == jsonObject2) {
                        // 创建订单下标存储产品状态对象
                        JSONObject js2 = new JSONObject();
                        // 添加产品状态为不是被第一个处理时间的产品
                        js2.put("zOk",2);
                        // 添加被获取产品状态的订单编号和订单下标记录
                        js2.put("z",z);
                        // 添加订单下标存储产品状态对象
                        jsonObject.put(ta2Index,js2);
                        // 添加订单编号存储产品状态对象
                        sho.put(ta2Id_O,jsonObject);
                    } else {
                        // 判断添加产品状态的订单编号和订单下标记录不等于被获取产品状态的订单编号和订单下标记录
                        if (!jsonObject2.getString("z").equals(z)) {
                            // 创建订单编号存储产品状态对象
                            JSONObject js1 = new JSONObject();
                            // 创建订单下标存储产品状态对象
                            JSONObject js2 = new JSONObject();
                            // 添加产品状态为不是被第一个处理时间的产品
                            js2.put("zOk",2);
                            // 添加被获取产品状态的订单编号和订单下标记录
                            js2.put("z",z);
                            // 添加订单下标存储产品状态对象
                            js1.put(ta2Index,js2);
                            // 添加订单编号存储产品状态对象
                            sho.put(ta2Id_O,js1);
                        }
                    }
                }
            }
        }
    }

    /**
     * 空插冲突处理方法
     * @param taskX	当前处理任务信息
     * @param task1X	对比任务信息-1
     * @param task2X	对比任务信息-2
     * @param tasks	任务集合
     * @param i1	任务下标
     * @param conflictInd	被冲突任务下标
     * @param zon	任务余剩时间
     * @param conflict	被冲突任务集合
     * @param teDate	当前处理的任务的所在日期对象
     * @param random	当前唯一编号
     * @param dep	部门
     * @param grpB	组别
     * @param objAction	所有递归信息
     * @param actZ	递归信息
     * @param yTeD	任务所在时间键的第一个键的值（时间戳）
     * @param teDaF	当前任务所在日期对象
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 16:12
     */
    private JSONObject konChaConflict(Task taskX,Task task1X,Task task2X,List<Task> tasks,int i1
            ,int conflictInd,long zon,List<Task> conflict,JSONObject teDate,String random,String dep,String grpB
            ,JSONArray objAction,JSONObject actZ,Long yTeD,JSONObject teDaF,Integer isC,JSONObject sho,int csSta){
        // 创建返回结果
        JSONObject re = new JSONObject();
        // 添加正常时间够用停止状态
        re.put("isT2",0);
//        System.out.println("进入补被冲突的:"+conflictInd);
//        System.out.println(JSON.toJSONString(taskX));
//        System.out.println(JSON.toJSONString(task1X));
//        System.out.println(JSON.toJSONString(task2X));
//        System.out.println("conflict-前:");
//        System.out.println(JSON.toJSONString(conflict));
        // 获取产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer zOk = sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk");
        // 判断对比任务1的优先级等于系统并且对比任务2的优先级不等于系统
        if (task1X.getPriority() == -1 && task2X.getPriority() != -1) {
//            System.out.println("进入-1和!=-1:");
            // 获取开始时间（对比任务1的结束时间+当前任务的任务总时间）
            long kai = task1X.getTePFinish()+taskX.getTeDurTotal();
            // 判断开始时间大于对比任务2的结束时间
            if (kai > task2X.getTePFinish()) {
                // 判断当前任务优先级小于对比任务2优先级
                if (taskX.getPriority() < task2X.getPriority()){
                    // 任务余剩时间累加（+对比任务2的总时间）
                    zon += task2X.getTeDurTotal();
                    // 任务余剩时间累减（-当前任务总时间）
                    zon -= taskX.getTeDurTotal();
                    // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                    tasks.set(i1+1,Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                            ,taskX.getTeDurTotal(),taskX));
//                    System.out.println("进入这里++=33");
                    // 冲突任务集合添加对比任务2信息
                    conflict.add(Obj.getTaskX(task2X.getTePStart(),task2X.getTePFinish(),task2X.getTeDurTotal(),task2X));
                    // 调用添加或更新产品状态方法
                    addSho(sho, taskX.getId_O(),taskX.getIndex().toString(),task2X.getId_O(),task2X.getIndex().toString(),0);
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(yTeD+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                            ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk"),random);
                }
//                System.out.println("进入前赋值-conflictInd-2-1:"+conflictInd);
                // 调用处理冲突核心方法2
                JSONObject bqCt2 = getBqCt2(conflictInd, tasks, conflict, taskX, zon, teDate, random
                        , objAction, actZ, i1,dep,grpB,yTeD,teDaF,isC,sho,csSta);
                // 更新冲突集合指定的冲突下标的任务信息
                conflict.set(conflictInd,Obj.getTaskX(taskX.getTePStart(),taskX.getTePFinish(),taskX.getTeDurTotal(),taskX));
                // 获取冲突下标
                conflictInd = bqCt2.getInteger("conflictInd");
                // 获取任务余剩时间
                zon = bqCt2.getLong("zon");
                // 获取任务所在时间键的第一个键的值（时间戳）
                yTeD = bqCt2.getLong("yTeD");
            } else {
                // 判断当前任务优先级小于对比任务2的优先级
                if (taskX.getPriority() < task2X.getPriority()) {
                    // 判断开始时间小于等于对比时间2的开始时间
                    if (kai <= task2X.getTePStart()) {
                        // 任务集合添加任务下标加1（i1+1）添加任务信息
                        tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                                ,taskX.getTeDurTotal(),taskX));
                        // 任务余剩时间累减
                        zon -= taskX.getTeDurTotal();
                        // 冲突任务下标累加
                        conflictInd++;
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(yTeD+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
                    } else if (kai > task2X.getTePStart() && kai <= task2X.getTePFinish()) {
                        // 任务余剩时间累加（+对比任务2的总时间）
                        zon += task2X.getTeDurTotal();
                        // 任务余剩时间累减（-当前任务总时间）
                        zon -= taskX.getTeDurTotal();
                        // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                        tasks.set(i1+1,Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                                ,taskX.getTeDurTotal(),taskX));
                        // 冲突任务集合添加对比任务2信息
                        conflict.add(Obj.getTaskX(task2X.getTePStart(),task2X.getTePFinish(),task2X.getTeDurTotal(),task2X));
                        // 调用添加或更新产品状态方法
                        addSho(sho, taskX.getId_O(),taskX.getIndex().toString(),task2X.getId_O(), task2X.getIndex().toString(),0);
//                        System.out.println("进入这里++=34");
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(yTeD+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                                ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk"),random);
                    }
                }
            }
        } else if (task1X.getPriority() == -1 && task2X.getPriority() == -1) {
//            System.out.println("进入-1和-1:");
            // 获取开始时间（对比任务1的结束时间+当前任务的任务总时间）
            long kai = task1X.getTePFinish()+taskX.getTeDurTotal();
            // 判断开始时间大于对比任务2的开始时间
            if (kai > task2X.getTePStart()) {
                // 获取余剩时间（对比时间2的开始时间-对比时间1的结束时间）
                long s = task2X.getTePStart() - task1X.getTePFinish();
                // 判断余剩时间大于0
                if (s > 0) {
                    // 获取时间差（当前任务总时间-余剩时间）
                    long cha = taskX.getTeDurTotal() - s;
                    // 任务集合添加任务下标加1（i1+1）添加任务信息
                    tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+s),s,taskX));
                    // 任务余剩时间累减
                    zon -= s;
                    // 设置当前任务总共时间
                    taskX.setTeDurTotal(cha);
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(yTeD+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
                } else {
                    re.put("isT2",1);
                }
            } else {
                // 任务集合添加任务下标加1（i1+1）添加任务信息
                tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                        ,taskX.getTeDurTotal(),taskX));
                // 任务余剩时间累减
                zon -= taskX.getTeDurTotal();
                // 冲突任务下标累加
                conflictInd++;
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(yTeD+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
            }
        } else if (task1X.getPriority() != -1 && task2X.getPriority() == -1) {
//            System.out.println("进入!=-1和-1:");
            // 判断当前任务优先级小于对比任务1的优先级
            if (taskX.getPriority() < task1X.getPriority()) {
                // 任务余剩时间累加（+对比任务1的总时间）
                zon += task1X.getTeDurTotal();
                // 任务余剩时间累减（-当前任务总时间）
                zon -= taskX.getTeDurTotal();
                // 更新任务集合对应的下标（i1）的任务信息为当前任务
                tasks.set(i1,Obj.getTaskX(task1X.getTePStart(),(task1X.getTePStart()+taskX.getTeDurTotal())
                        ,taskX.getTeDurTotal(),taskX));
                // 冲突任务集合添加对比任务1信息
                conflict.add(Obj.getTaskX(task1X.getTePStart(),task1X.getTePFinish(),task1X.getTeDurTotal(),task1X));
                // 调用添加或更新产品状态方法
                addSho(sho, taskX.getId_O(),taskX.getIndex().toString(), task1X.getId_O(),task1X.getIndex().toString(),0);
//                System.out.println("进入这里++=35");
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(yTeD+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                        ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk"),random);
            } else {
                // 获取开始时间（对比任务1的结束时间+当前任务的任务总时间）
                long kai = task1X.getTePFinish()+taskX.getTeDurTotal();
                // 判断开始时间大于对比任务2的开始时间
                if (kai > task2X.getTePStart()) {
//                    System.out.println("进入!=-1和-1 -- 1:");
                    // 获取余剩时间（对比时间2的开始时间-对比时间1的结束时间）
                    long s = task2X.getTePStart() - task1X.getTePFinish();
                    // 判断余剩时间大于0
                    if (s > 0) {
                        // 获取时间差（当前任务总时间-余剩时间）
                        long cha = taskX.getTeDurTotal() - s;
                        // 任务集合添加任务下标加1（i1+1）添加任务信息
                        tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+s),s,taskX));
                        // 任务余剩时间累减
                        zon -= s;
                        // 设置当前任务总共时间
                        taskX.setTeDurTotal(cha);
                        // 添加当前处理的任务的所在日期对象状态
                        teDate.put(yTeD+"",0);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
                    } else {
                        re.put("isT2",1);
                    }
                } else {
//                    System.out.println("进入!=-1和-1 -- 2:");
                    // 任务集合添加任务下标加1（i1+1）添加任务信息
                    tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                            ,taskX.getTeDurTotal(),taskX));
                    // 任务余剩时间累减
                    zon -= taskX.getTeDurTotal();
                    // 冲突任务下标累加
                    conflictInd++;
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(yTeD+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
                }
            }
        } else {
//            System.out.println("进入!=-1和!=-1:");
            // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
            int isJin = 0;
            // 判断当前任务优先级小于对比任务1的优先级
            if (taskX.getPriority() < task1X.getPriority()) {
                // 任务余剩时间累加
                zon += task1X.getTeDurTotal();
                // 任务余剩时间累减
                zon -= taskX.getTeDurTotal();
                // 更新任务集合对应的下标（i1）的任务信息为当前任务
                tasks.set(i1,Obj.getTaskX(task1X.getTePStart(),(task1X.getTePStart()+taskX.getTeDurTotal()),taskX.getTeDurTotal(),taskX));
                // 冲突任务集合添加对比任务1信息
                conflict.add(Obj.getTaskX(task1X.getTePStart(),task1X.getTePFinish(),task1X.getTeDurTotal(),task1X));
                // 调用添加或更新产品状态方法
                addSho(sho, taskX.getId_O(),taskX.getIndex().toString(),task1X.getId_O(), task1X.getIndex().toString(),0);
                // 冲突任务下标累加
                conflictInd++;
                // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                isJin = 1;
//                System.out.println("进入这里++=36");
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(yTeD+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                        ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk"),random);
            }
            // 获取余剩时间（对比时间2的开始时间-对比时间1的结束时间）
            long s = task2X.getTePStart() - task1X.getTePFinish();
            // 判断余剩时间大于0
            if (s > 0) {
                // 获取时间差（当前任务总时间-余剩时间）
                long cha = taskX.getTeDurTotal() - s;
                // 任务集合添加任务下标加1（i1+1）添加任务信息
                tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+s),s,taskX));
                // 任务余剩时间累减
                zon -= s;
                // 判断时间差不为0
                if (cha != 0) {
                    // 设置当前任务总共时间
                    taskX.setTeDurTotal(cha);
                }
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(yTeD+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
            } else {
                // 存储时间差 - 判断对比任务2优先级等于系统，如果等于就赋值为0，否则xin等于（对比任务2的开始时间-对比任务1的结束时间）
                long xin = task2X.getPriority()==-1?0:task2X.getTePStart() - task1X.getTePFinish();
                // 获取开始时间（对比任务1+存储时间差）
                long kai = task1X.getTePFinish() + xin;
                // 开始时间累加当前任务总时间
                kai += taskX.getTeDurTotal();
                // 判断开始时间大于对比任务2的结束时间并且，当前任务优先级小于对比任务2的优先级
                if (kai > task2X.getTePFinish() && taskX.getPriority() < task2X.getPriority()) {
                    // 任务余剩时间累加
                    zon += task2X.getTeDurTotal();
                    // 判断存储时间差大于0
                    if (xin > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        taskX.setTeDurTotal(taskX.getTeDurTotal()-xin);
                    }
                    // 任务余剩时间累减
                    zon -= taskX.getTeDurTotal();
                    // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                    tasks.set(i1+1,Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                            ,taskX.getTeDurTotal(),taskX));
                    // 冲突任务集合添加对比任务2信息
                    conflict.add(Obj.getTaskX(task2X.getTePStart(),task2X.getTePFinish(),task2X.getTeDurTotal(),task2X));
                    // 调用添加或更新产品状态方法
                    addSho(sho, taskX.getId_O(),taskX.getIndex().toString(), task2X.getId_O(),task2X.getIndex().toString(),0);
                    // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                    if (isJin == 0) {
                        // 冲突任务下标累加
                        conflictInd++;
                    }
//                    System.out.println("进入前赋值-conflictInd-2-2:"+conflictInd);
                    // 调用处理冲突核心方法2
                    JSONObject bqCt2 = getBqCt2(conflictInd, tasks, conflict, taskX, zon, teDate, random
                            , objAction, actZ, i1,dep,grpB,yTeD,teDaF,isC,sho,csSta);
                    // 更新冲突集合指定的冲突下标的任务信息
                    conflict.set(conflictInd,Obj.getTaskX(taskX.getTePStart(),taskX.getTePFinish(),taskX.getTeDurTotal(),taskX));
                    // 获取冲突任务下标
                    conflictInd = bqCt2.getInteger("conflictInd");
                    // 获取任务余剩时间
                    zon = bqCt2.getLong("zon");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    yTeD = bqCt2.getLong("yTeD");
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(yTeD+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                            ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk"),random);
                } else if (kai > task2X.getTePStart() && kai <= task2X.getTePFinish() && taskX.getPriority() < task2X.getPriority()) {
                    // 任务余剩时间累加
                    zon += task2X.getTeDurTotal();
                    // 判断存储时间差大于0
                    if (xin > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        taskX.setTeDurTotal(taskX.getTeDurTotal()-xin);
                    }
                    // 任务余剩时间累减
                    zon -= taskX.getTeDurTotal();
                    // 更新任务集合对应的下标（i1+1）的任务信息为当前任务
                    tasks.set(i1+1,Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                            ,taskX.getTeDurTotal(),taskX));
                    // 冲突任务集合添加对比任务2信息
                    conflict.add(Obj.getTaskX(task2X.getTePStart(),task2X.getTePFinish(),task2X.getTeDurTotal(),task2X));
                    // 调用添加或更新产品状态方法
                    addSho(sho, taskX.getId_O(),taskX.getIndex().toString(),task2X.getId_O(), task2X.getIndex().toString(),0);
//                    System.out.println("进入这里++=37");
                    // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                    if (isJin == 0) {
                        // 冲突任务下标累加
                        conflictInd++;
                    }
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(yTeD+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                            ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk"),random);
                } else if (kai <= task2X.getTePStart()) {
//                    System.out.println("进入这个奇怪的地方");
                    // 判断存储时间差大于0
                    if (xin > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        taskX.setTeDurTotal(taskX.getTeDurTotal()-xin);
                    }
                    // 任务集合添加任务下标加1（i1+1）添加任务信息
                    tasks.add((i1+1),Obj.getTaskX(task1X.getTePFinish(),(task1X.getTePFinish()+taskX.getTeDurTotal())
                            ,taskX.getTeDurTotal(),taskX));
                    // 任务余剩时间累减
                    zon -= taskX.getTeDurTotal();
                    // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                    if (isJin == 0) {
                        // 冲突任务下标累加
                        conflictInd++;
                    }
                    // 添加当前处理的任务的所在日期对象状态
                    teDate.put(yTeD+"",0);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,random);
                }
            }
        }
        // 添加返回结果信息
        re.put("zon",zon);
        re.put("conflictInd",conflictInd);
        re.put("yTeD",yTeD);
        return re;
    }

    /**
     * 计算空插时间方法
     * @param task	当前任务信息
     * @param task1	对比任务信息1
     * @param task2	对比任务信息2
     * @param tasks	任务集合
     * @param i	任务下标
     * @param zon	任务余剩时间
     * @param isKC	控制计算模式参数，isKC == 0 需要重写任务的开始时间和结束时间、isKC == 1 不需要重写任务的开始时间和结束时间、isKC == 2 使用新模式进行计算时间
     * @param random	当前唯一编号
     * @param teDate	存储当前处理的任务的所在日期对象状态
     * @param dep	部门
     * @param grpB	组别
     * @param zOk	产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/2/18 21:53
     */
    private JSONObject ji(Task task, Task task1, Task task2, List<Task> tasks, Integer i, Long zon
            , Integer isKC,String random,JSONObject teDate,String dep,String grpB,int zOk){
        // 创建返回结果
        JSONObject re = new JSONObject();
        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
        re.put("isR", 0);
        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
        re.put("isP",0);
        // 获取余剩时间（对比任务信息2的开始时间-对比任务信息1的结束时间）
        long s = task2.getTePStart() - task1.getTePFinish();
        // 判断余剩时间大于0
        if (s > 0) {
            // 控制计算模式参数，isKC == 0 需要重写任务的开始时间和结束时间、isKC == 1 不需要重写任务的开始时间和结束时间、isKC == 2 使用新模式进行计算时间
            if (isKC == 2) {
//                System.out.println("ji-1:");
                // 添加控制是否结束跳天
                re.put("isR", 1);
                // 获取时间差（当前任务的开始时间-对比任务信息1的结束时间）
                long sta = task.getTePStart() - task1.getTePFinish();
                // 获取时间差2（余剩时间-时间差）
                long staJ = s - sta;
                // 设置任务总共时间（任务总共时间-时间差2）
                task.setTeDurTotal(task.getTeDurTotal()-staJ);
                // 设置任务开始时间
                task.setTePStart(task.getTePStart());
                // 设置任务结束时间（对比任务信息2的开始时间）
                task.setTePFinish(task2.getTePStart());
                // 任务集合指定添加任务下标+1位置添加任务信息
                tasks.add(i+1,Obj.getTaskX(task.getTePStart(),task2.getTePStart(),staJ,task));
                // 添加控制是否结束循环参数
                re.put("isP",4);
                // 任务余剩时间累减
                zon -= staJ;
                // 调用获取当前时间戳方法
                Long teS = getTeS(random, grpB, dep);
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(teS+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(), task.getIndex(),teS,zOk,random);
            } else {
                // 添加控制是否跳天参数
                re.put("isR", 1);
                // 获取时间差（余剩时间-当前任务时间）
                long cha = s - task.getTeDurTotal();
                // 判断时间差大于等于0
                if (cha >= 0) {
//                    System.out.println("ji-2:");
                    // 控制计算模式参数，isKC == 0 需要重写任务的开始时间和结束时间、isKC == 1 不需要重写任务的开始时间和结束时间、isKC == 2 使用新模式进行计算时间
                    if (isKC == 0) {
                        // 更新任务开始时间
                        task.setTePStart(task1.getTePFinish());
                        // 更新任务结束时间
                        task.setTePFinish(task1.getTePFinish()+task.getTeDurTotal());
                    }
                    Task task3 = Obj.getTaskX(task.getTePStart(),task.getTePFinish(),task.getTeDurTotal(),task);
                    // 任务集合指定添加任务下标+1位置添加任务信息
                    tasks.add(i+1,task3);
                    // 添加控制是否结束循环参数
                    re.put("isP",2);
                    // 任务余剩时间累减
                    zon -= task.getTeDurTotal();
                } else {
//                    System.out.println("ji-3:");
                    // 获取时间差2（当前任务时间-余剩时间）
                    long cha2 = task.getTeDurTotal() - s;
                    // 更新任务总时间
                    task.setTeDurTotal(cha2);
                    // 更新任务开始时间
                    task.setTePStart(task1.getTePFinish());
                    // 更新任务结束时间
                    task.setTePFinish((task1.getTePFinish()+s));
                    // 任务集合指定添加任务下标+1位置添加任务信息
                    tasks.add(i+1,Obj.getTaskX(task1.getTePFinish(),(task1.getTePFinish()+s),s,task));
                    // 添加控制是否结束循环参数
                    re.put("isP",4);
                    // 任务余剩时间累减
                    zon -= s;
                }
                // 调用获取当前时间戳方法
                Long teS = getTeS(random, grpB, dep);
                // 添加当前处理的任务的所在日期对象状态
                teDate.put(teS+"",0);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(), task.getIndex(),teS,zOk,random);
            }
        } else {
            // 添加控制是否结束循环参数
            re.put("isP",3);
        }
        re.put("zon",zon);
        return re;
    }

}
