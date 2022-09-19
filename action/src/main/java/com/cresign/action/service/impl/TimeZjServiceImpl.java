package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.common.ActionEnum;
import com.cresign.action.service.TimeZjService;
import com.cresign.action.utils.Obj;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.chkin.Task;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName TimeServiceImpl
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2021/12/20 11:10
 * @ver 1.0.0
 */
@Service
public class TimeZjServiceImpl implements TimeZjService {

    @Resource
    private CoupaUtil coupaUtil;

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
    /**
     * 是否测试: = true 说明是测试、 = false 说明不是测试
     */
    public static boolean isTest = true;

    /**
     * 初始化时间处理方法
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 1:26
     */
    public void getAtFirst(String id_O,Long teStart,String id_C,Integer wn0TPrior){
        // 调用方法获取订单信息
        Order salesOrderData = coupaUtil.getOrderByListKey(
                id_O, Arrays.asList("oItem", "info", "view", "action", "casItemx"));

        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
                || null == salesOrderData.getCasItemx()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 定义，存储进入未操作到的地方记录
        JSONObject jiLJN = new JSONObject();
        // 存储当前唯一编号的第一个当前时间戳
        JSONObject onlyStartMapJ = new JSONObject();
        // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
        JSONObject onlyStartMapAndDepJ = new JSONObject();
        // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
        JSONObject onlyIsDsJ = new JSONObject();
        // 统一id_O和index存储记录状态信息
        JSONObject storageResetJ = new JSONObject();
        // 存储任务所在日期
        JSONObject teDateFAllJ = new JSONObject();
        // 镜像任务存储
        Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ = new HashMap<>(16);
        // 镜像总时间存储
        JSONObject zonFAllJ = new JSONObject();
        // 存储部门对应组别的上班和下班时间
        JSONObject xbAndSbAll = new JSONObject();
        // 存储部门对应组别的职位总人数
        JSONObject grpUNumAll = new JSONObject();
        // 存储casItemx内订单列表的订单action数据
        JSONObject actionIdO = new JSONObject();
        // 根据组别存储部门信息
        JSONObject grpBGroupIdOJ = new JSONObject();
        // 全部任务存储
        JSONObject objTaskAll = new JSONObject();
        // 判断是测试
        if (isTest) {
            // 调用根据公司编号清空所有任务信息方法
            setTaskAndZonKai(id_C);
            // 调用添加测试数据方法
            Obj.addOrder(teStart,coupaUtil);
            Obj.addOrder2(teStart,coupaUtil);
            Obj.addOrder3(teStart,coupaUtil);
            // 调用添加测试数据方法
            Obj.addTasks(teStart,"1001","1xx1",id_C,objTaskAll);
            Obj.addTasksAndOrder(teStart,id_C,objTaskAll);
            Obj.addTasksAndOrder3(teStart,id_C,objTaskAll);
        }
        // 获取唯一下标
        String random = MongoUtils.GetObjectId();
        // 获取全局唯一下标
        String randomAll = MongoUtils.GetObjectId();

        // 设置问题记录的初始值
        yiShu.put(randomAll,0);
        leiW.put(randomAll,0);
        xin.put(randomAll,0);
        isQzTz.put(randomAll,0);
        jiLJN.put(randomAll,new JSONArray());

        // 设置存储当前唯一编号的第一个当前时间戳
        onlyStartMapJ.put(random,teStart);
        // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
        onlyIsDsJ.put(random,0);
        // 存储最初开始时间
        long hTeC = 0L;
        // 存储最后结束时间
        long csTe = 0L;
        // 获取递归订单列表
        JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
        // 存储递归订单列表的订单编号集合
        JSONArray objOrderList = new JSONArray();
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的打卡卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("chkin00s"));
        // 获取打卡卡片信息
        JSONObject chkin00s = asset.getChkin00s();
        // 获取职位人数信息
        JSONObject objZw = chkin00s.getJSONObject("objZw");
        // 存储判断职位人数信息是否为空
        boolean objZwP = true;
        // 判断职位人数信息为空
        if (null == objZw) {
            // 设置值为空
            objZwP = false;
        }
        // 获取打卡信息
        JSONArray objData = chkin00s.getJSONArray("objData");
        // 存储判断打卡信息是否为空
        boolean objXbAndSb = true;
        // 存储判断是否是否有时间处理打卡信息
        boolean timeP = false;
        // 定义存储时间处理打卡时间字典
        JSONObject xbAndSb = null;
        // 定义存储时间处理打卡信息下标
        int wtInd = -1;
        // 判断打卡信息为空
        if (null == objData) {
            objXbAndSb = false;
        } else {
            // 遍历打卡信息
            for (int j = 0; j < objData.size(); j++) {
                // 根据j获取对应的打卡信息
                JSONObject dataZ = objData.getJSONObject(j);
                // 判断是否是时间处理打卡信息
                if (null != dataZ.getInteger("timeP")) {
                    // 设置为是
                    timeP = true;
                    // 获取下标位置
                    wtInd = j;
                    // 创建时间处理打卡信息存储
                    xbAndSb = new JSONObject();
                    // 获取时间处理打卡的上班和下班信息
                    JSONObject objDataZ = objData.getJSONObject(j);
                    // 添加信息
                    xbAndSb.put("xb",objDataZ.getJSONArray("objXb"));
                    xbAndSb.put("sb",objDataZ.getJSONArray("objSb"));
                    break;
                }
            }
        }
        // 获取部门组别对应时间处理打卡信息下标字典
        JSONObject objWorkTime = chkin00s.getJSONObject("objWorkTime");
        // 判断为空
        if (null == objWorkTime) {
            objXbAndSb = false;
        }
        // 遍历订单列表
        for (int i = 0; i < objOrder.size(); i++) {
            // 获取订单列表的订单编号
            String id_Oz = objOrder.getJSONObject(i).getString("id_O");
            // 判断订单等于主订单，则通过循环
            if (id_Oz.equals(id_O)) {
                continue;
            }
            // 添加订单编号
            objOrderList.add(id_Oz);
            // 根据订单编号查询action卡片信息
            Order ozAction = coupaUtil.getOrderByListKey(id_Oz, Collections.singletonList("action"));
            // 获取递归信息
            JSONArray objAction = ozAction.getAction().getJSONArray("objAction");
            // 获取组别对应部门信息
            JSONObject grpBGroup = ozAction.getAction().getJSONObject("grpBGroup");
            // 遍历组别对应部门信息
            for (String grpB : grpBGroup.keySet()) {
                // 创建存储部门字典
                JSONObject js = new JSONObject();
                // 根据组别获取组别信息
                JSONObject grpBGroupZ = grpBGroup.getJSONObject(grpB);
                // 获取组别的部门
                String dep = grpBGroupZ.getString("dep");
                // 判断职位人数不为空
                if (objZwP) {
                    // 根据部门获取职位人数部门信息
                    JSONObject depM = objZw.getJSONObject(dep);
                    // 判断不为空
                    if (null != depM) {
                        // 根据组别，获取职位人数部门对应的组别信息
                        Integer grpBM = objZw.getInteger(grpB);
                        if (null != grpBM) {
                            // 根据部门，获取部门对应的全局职位人数信息
                            JSONObject depMAll = grpUNumAll.getJSONObject(dep);
                            // 判断部门全局职位人数信息为空
                            if (null == depMAll) {
                                // 创建部门全局职位人数信息
                                depMAll = new JSONObject();
                                // 根据组别添加职位人数
                                depMAll.put(grpB,grpBM);
                                grpUNumAll.put(dep,depMAll);
                            } else {
                                // 直接根据组别获取全局职位人数
                                Integer grpBMAll = depMAll.getInteger(grpB);
                                // 判断为空
                                if (null == grpBMAll) {
                                    // 添加全局职位人数信息
                                    depMAll.put(grpB,grpBM);
                                    grpUNumAll.put(dep,depMAll);
                                }
                            }
                        }
                    }
                }
                // 判断上班下班时间不为空，并且有时间处理打卡信息
                if (objXbAndSb && timeP) {
                    // 根据部门获取上班下班信息
                    JSONObject depW = objWorkTime.getJSONObject(dep);
                    if (null != depW) {
                        // 根据组别获取上班下班信息
                        Integer grpBW = depW.getInteger(grpB);
                        // 判断上班下班信息不为空，并且，下标位置等于时间处理打卡信息的下标
                        if (null != grpBW && grpBW == wtInd) {
                            // 根据部门获取全局上班下班信息
                            JSONObject depWAll = xbAndSbAll.getJSONObject(dep);
                            // 判断为空
                            if (null == depWAll) {
                                // 创建
                                depWAll = new JSONObject();
                                // 添加全局上班下班信息
                                depWAll.put(grpB,xbAndSb);
                                xbAndSbAll.put(dep,depWAll);
                            } else {
                                // 根据组别获取全局上班下班信息
                                JSONObject grpBWAll = depWAll.getJSONObject(grpB);
                                if (null == grpBWAll) {
                                    // 添加全局上班下班信息
                                    depWAll.put(grpB,xbAndSb);
                                    xbAndSbAll.put(dep,depWAll);
                                }
                            }
                        }
                    }
                }
                // 添加部门信息
                js.put("dep",dep);
                js.put("id_O",id_Oz);
                // 添加信息
                grpBGroupIdOJ.put(grpB,js);
            }
            // 根据订单编号添加订单信息存储
            actionIdO.put(id_Oz,objAction);
        }

        // 获取递归存储的时间处理信息
        JSONArray oDates = salesOrderData.getAction().getJSONArray("oDates");
        // 获取递归存储的时间任务信息
        JSONArray oTasks = salesOrderData.getAction().getJSONArray("oTasks");
        // 用于存储时间冲突的副本
        JSONObject teDaF = new JSONObject();
        // 用于存储判断镜像是否是第一个被冲突的产品
        JSONObject sho = new JSONObject();
        // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
        boolean isPn = true;
        // 定义用来存储最大结束时间
        long maxSte = 0;
        // 用于存储每一个时间任务的结束时间
        JSONArray teFinList = new JSONArray();
        // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
        JSONObject pfTe = new JSONObject();
        // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
        JSONObject pfTeSta = new JSONObject();
        // 遍历时间处理信息集合
        for (int i = 0; i < oDates.size(); i++) {
            // 获取i对应的时间处理信息
            JSONObject oDa = oDates.getJSONObject(i);
            // 获取时间处理的序号
            Integer priorItem = oDa.getInteger("priorItem");
            // 获取时间处理的父零件编号
            String id_PF = oDa.getString("id_PF");
            // 获取时间处理的序号是否为1层级
            Integer csSta = oDa.getInteger("csSta");
            // 获取时间处理的判断是否是空时间信息
            Boolean empty = oDa.getBoolean("empty");
            // 判断当前时间处理为空时间信息
            if (empty) {
                // 获取时间处理的链接下标
                Integer linkInd = oDa.getInteger("linkInd");
                // 根据链接下标获取指定的结束时间
                Long aLongL = teFinList.getLong(linkInd);
                // 判断父id的预计开始时间为空，并且序号为第一个
                if (null == pfTeSta.getLong(id_PF) && priorItem == 1) {
                    pfTeSta.put(id_PF,aLongL);
                }
                // 根据父零件编号获取序号信息
                JSONObject pfTeZ = pfTe.getJSONObject(id_PF);
                // 判断序号信息为空
                if (null == pfTeZ) {
                    // 创建序号信息
                    pfTeZ = new JSONObject();
                    // 添加序号的结束时间，默认为0
                    pfTeZ.put(priorItem.toString(),0);
                }
                // 获取序号结束时间
                Long aLong = pfTeZ.getLong(priorItem.toString());
                // 添加链接结束时间到当前空时间处理结束时间列表内
                teFinList.add(aLongL);
                // 判断链接结束时间大于当前结束时间
                if (aLongL > aLong) {
                    // 修改当前结束时间为链接结束时间
                    pfTeZ.put(priorItem.toString(),aLongL);
                    // 根据父零件编号添加序号信息
                    pfTe.put(id_PF,pfTeZ);
                }
                continue;
            }

            // 获取当前唯一ID存储时间处理的最初开始时间
            Long hTeStart = hTeC;
            // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
            JSONObject js1 = new JSONObject();
            JSONObject js2 = new JSONObject();
            // 设置为-1代表的是递归的零件
            js2.put("zOk",-1);
            js2.put("z","-1");
            js1.put(oDa.getString("index"),js2);
            sho.put(oDa.getString("id_O"),js1);
            // 获取时间处理的组别
            String grpB = oDa.getString("grpB");
            // 根据组别获取部门
            String dep = grpBGroupIdOJ.getJSONObject(grpB).getString("dep");
            oDa.put("dep",dep);
            oDa.put("grpUNum",getObjGrpUNum(grpB,dep,oDa.getString("id_C"),grpUNumAll));
            // 获取时间处理的零件产品编号
            String id_P = oDa.getString("id_P");
            // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
            Integer kaiJie = oDa.getInteger("kaiJie");
            // 获取时间处理的实际准备时间
            Long tePrep = oDa.getLong("tePrep");
            Long teDur = oDa.getLong("teDur");
            Double wn2qtyneed = oDa.getDouble("wn2qtyneed");
//            System.out.println(JSON.toJSONString(oDa));
            // 存储任务总时间
            long l = (long)(teDur * wn2qtyneed);
//            System.out.println("teDur:"+teDur+",wn2qty:"+wn2qtyneed+",l:"+l);
            // 获取时间处理的总任务时间
            Long teDurTotal = (l/ oDa.getInteger("grpUNum"));
            System.out.println("teDurTotalTe:" +teDurTotal+" - tePrep:"
                    +tePrep+" - prior:"+priorItem);
            System.out.println("csTeJTe:"+" - id_PF:"+id_PF);
            // 判断当前唯一ID存储时间处理的最初开始时间为0
            if (hTeStart == 0) {
                // 调用获取当前时间戳方法设置开始时间
                oDa.put("teStart",getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ));
            } else {
                // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
                if (csSta == 1 && kaiJie == 1) {
                    // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
                    hTeStart = csTe;
                }
                oDa.put("teStart",hTeStart);
            }
            // 存储判断执行方法
            boolean isD1 = false;
            // 序号是不为1层级
            if (csSta == 0 || (kaiJie != 1 && csSta == 1)) {
                isD1 = true;
            }
            // 判断执行方法为true
            if (isD1) {
                // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                JSONObject jsonObject;
                // 获取判断自己的id是否等于已存在的父id
                boolean b = pfTe.containsKey(id_P);
                // 判断自己的id是已存在的父id
                if (b) {
                    // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
                    jsonObject = pfTe.getJSONObject(id_P);
                    // 转换键信息
                    List<String> list = new ArrayList<>(jsonObject.keySet());
                    // 获取最后一个时间信息
                    String s = list.get(list.size() - 1);
                    // 赋值为最后一个时间信息
                    hTeStart = jsonObject.getLong(s);
                } else {
                    // 根据父id获取按照父零件编号存储每个序号的最后结束时间
                    jsonObject = pfTe.getJSONObject(id_PF);
                    // 获取上一个序号的时间信息并赋值
                    hTeStart = jsonObject.getLong(((priorItem - 1) + ""));
                }
                // 设置开始时间
                oDa.put("teStart",hTeStart);
            }

            // 获取任务的最初开始时间备份
            Long teStartN = oDa.getLong("teStart");
            // 设置最初结束时间
            oDa.put("teFin",(teStartN+(teDurTotal+tePrep)));
            // 获取最初结束时间
            Long teFin = oDa.getLong("teFin");
            // 获取任务信息，并且转换为任务类
            Task task = JSON.parseObject(JSON.toJSONString(oTasks.get(i)),Task.class);
            // 设置最初任务信息的时间信息
            task.setTeDurTotal((teFin - teStartN));
            task.setTePStart(teStartN);
            task.setTePFinish(teFin);
            task.setTeCsStart(teStartN);
            task.setTeCsSonOneStart(0L);
            // 判断优先级不等于-1
            if (wn0TPrior != -1) {
                // 设置优先级为传参的优先级
                task.setPriority(wn0TPrior);
            }
            // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
            if (null == pfTeSta.getLong(id_PF) && priorItem == 1 && kaiJie != 5 && kaiJie != 3) {
                // 根据父id添加开始时间
                pfTeSta.put(id_PF,task.getTeCsStart());
            } else if (kaiJie == 3 || kaiJie == 5) {
                // 添加子最初开始时间
                task.setTeCsSonOneStart(pfTeSta.getLong(id_P));
            }

            // 创建当前处理的任务的所在日期对象
            JSONObject teDate = new JSONObject();
            System.out.println("taskTe:");
            System.out.println(JSON.toJSONString(task));
            // 获取订单编号
            String id_On = oDa.getString("id_O");
            // 获取订单下标
            Integer index = oDa.getInteger("index");
            // 调用时间处理方法
            JSONObject jo = chkInJi(task,hTeStart,grpB,dep,id_On,index,0,random
                    ,1,teDate,teDaF,0,sho,0,csSta
//                    ,true
                    ,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ
                    ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
            // 更新任务最初始开始时间
            hTeStart = jo.getLong("hTeStart");
            System.out.println("最外层:"+hTeStart);
            // 添加结束时间
            teFinList.add(hTeStart);

            // 根据订单编号获取递归集合
            JSONArray jsonArray = actionIdO.getJSONArray(id_On);
            // 根据订单下标获取递归信息并且转换为递归类
            OrderAction orderAction = JSON.parseObject(JSON.toJSONString(jsonArray.getJSONObject(index)),OrderAction.class);
            // 更新递归信息
            orderAction.setDep(dep);
            orderAction.setGrpB(grpB);
            orderAction.setTeDate(teDate);
            // 将更新的递归信息写入回去
            jsonArray.set(index,orderAction);
            actionIdO.put(id_On,jsonArray);

            // 定义存储最后结束时间参数
            long pfT;
            // 判断序号是为1层级
            if (csSta == 1) {
                // 获取实际结束时间
                Long xFin = jo.getLong("xFin");
//                System.out.println("xFinW:"+xFin);
                // 定义存储判断实际结束时间是否为空
                boolean isXf = false;
                // 判断实际结束时间不等于空
                if (null != xFin) {
//                    System.out.println("xFin:"+xFin);
                    // 赋值实际结束时间
                    hTeStart = xFin;
                    // 判断当前实际结束时间大于最大结束时间
                    if (xFin > maxSte) {
                        // 判断大于则更新最大结束时间为当前结束时间
                        maxSte = xFin;
                    }
                    // 设置不为空
                    isXf = true;
                } else {
                    // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
                    if (task.getTePFinish() > maxSte) {
                        // 判断大于则更新最大结束时间为当前结束时间
                        maxSte = task.getTePFinish();
                    }
                }
//                System.out.println("maxSte:"+maxSte);
                // 判断实际结束时间不为空
                if (isXf) {
                    // 赋值结束时间
                    pfT = xFin;
                } else {
                    // 赋值结束时间
                    pfT = task.getTePFinish();
                }
                // 判断是第一次进入
                if (isPn) {
                    // 添加设置第一层的开始时间
                    csTe=task.getTePStart();
                    // 设置只能进入一次
                    isPn = false;
                }
            } else {
                // 直接赋值最后结束时间
                pfT = hTeStart;
            }
            // 根据父id获取最后结束时间信息
            JSONObject pfTeZ = pfTe.getJSONObject(id_PF);
            // 判断最后结束时间信息为空
            if (null == pfTeZ) {
                // 创建并且赋值最后结束时间
                pfTeZ = new JSONObject();
                pfTeZ.put(priorItem.toString(),0);
            }
            // 根据序号获取最后结束时间
            Long aLong = pfTeZ.getLong(priorItem.toString());
            // 判断最后结束时间为空
            if (null == aLong) {
                // 为空，则直接添加最后结束时间信息
                pfTeZ.put(priorItem.toString(),pfT);
                pfTe.put(id_PF,pfTeZ);
            } else {
                // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
                if (pfT > aLong) {
                    // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
                    pfTeZ.put(priorItem.toString(),pfT);
                    pfTe.put(id_PF,pfTeZ);
                }
            }
            hTeC=hTeStart;
            System.out.println();
        }

        // 遍历递归订单列表
        for (int i = 0; i < objOrderList.size(); i++) {
            // 获取递归订单编号
            String id_Oz = objOrderList.getString(i);
            // 根据订单编号获取递归列表信息
            JSONArray objAction = actionIdO.getJSONArray(id_Oz);
            // 创建请求更改参数
            JSONObject mapKey = new JSONObject();
            // 添加请求更改参数信息
            mapKey.put("action.objAction",objAction);
            // 调用接口发起数据库更改信息请求
            coupaUtil.updateOrderByListKeyVal(id_Oz,mapKey);
        }

        // 调用任务最后处理方法
        setZui(teDaF,id_C,randomAll,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,jiLJN,id_O,objOrderList);

        // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
        onlyStartMapJ.remove(random);
        // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
        onlyStartMapAndDepJ.remove(random);
        // 根据当前唯一标识删除信息
        onlyIsDsJ.remove(random);
    }

    /**
     * 根据主订单和对应公司编号，删除时间处理信息
     * @param id_O	主订单编号
     * @param id_C	公司编号
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public void removeTime(String id_O,String id_C){
        // 调用方法获取订单信息
        Order order = coupaUtil.getOrderByListKey(
                id_O, Arrays.asList("casItemx","action"));
        // 判断订单是否为空
        if (null == order || null == order.getCasItemx() || null == order.getAction()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 获取递归订单列表
        JSONArray objOrder = order.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
        // 获取主订单的时间处理所在日期存储信息
        JSONObject timeRecord = order.getAction().getJSONObject("timeRecord");
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号，获取asset的时间处理卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("aArrange"));
        // 获取时间处理卡片信息
        JSONObject aArrange = asset.getAArrange();
        // 获取时间处理信息
        JSONObject objTask = aArrange.getJSONObject("objTask");
        // 创建时间处理信息镜像
        JSONObject objTaskF = JSONObject.parseObject(objTask.toJSONString());
        // 创建时间处理删除后任务列表存储对象
        JSONObject xObjTask = new JSONObject();
        // 遍历订单列表
        for (int j = 0; j < objOrder.size(); j++) {
            // 获取订单编号
            String id_oN = objOrder.getJSONObject(j).getString("id_O");
            // 判断订单编号等于主订单编号
            if (id_oN.equals(id_O)) {
                continue;
            }
            // 遍历时间处理所在日期存储部门列表
            timeRecord.keySet().forEach(dep -> {
                // 获取时间处理所在日期存储部门列表
                JSONObject grpBs = timeRecord.getJSONObject(dep);
                // 遍历时间处理所在日期存储组别列表
                grpBs.keySet().forEach(grpB -> {
                    // 获取时间处理所在日期存储组别列表
                    JSONObject teSs = grpBs.getJSONObject(grpB);
                    // 遍历时间处理所在日期存储日期列表
                    teSs.keySet().forEach(teS -> {
                        // 根据部门组别日期获取对应的任务信息
                        JSONObject taskZ = objTask.getJSONObject(dep).getJSONObject(grpB).getJSONObject(teS);
                        // 获取任务信息
                        JSONArray tasks = taskZ.getJSONArray("tasks");
                        // 获取删除后的部门任务信息
                        JSONObject depX = xObjTask.getJSONObject(dep);
                        // 判断为空
                        if (null == depX) {
                            // 创建
                            depX = new JSONObject();
                        }
                        // 获取删除后的组别任务信息
                        JSONObject grpBX = depX.getJSONObject(grpB);
                        if (null == grpBX) {
                            grpBX = new JSONObject();
                        }
                        // 获取删除后的时间戳任务信息
                        JSONObject teSsX = grpBX.getJSONObject(teS);
                        if (null == teSsX) {
                            teSsX = new JSONObject();
                        }
                        // 获取删除后余剩总时间
                        Long zon = teSsX.getLong("zon");
                        // 判断为空
                        if (null == zon) {
                            // 获取余剩总时间
                            zon = taskZ.getLong("zon");
//                            System.out.println("zon-null:"+zon);
                        }
                        // 定义存储任务列表需要删除的任务下标
                        List<Integer> removeInd = new ArrayList<>();
                        // 遍历任务列表
                        for (int i = 0; i < tasks.size(); i++) {
                            // 获取任务信息
                            JSONObject task = tasks.getJSONObject(i);
                            // 判断订单编号等于要删除的订单编号
                            if (task.getString("id_O").equals(id_oN)) {
                                // 添加删除的任务下标
                                removeInd.add(i);
//                                System.out.println("zon-q:"+zon);
                                // 累加余剩总时间
                                zon = zon + task.getLong("teDurTotal");
//                                System.out.println("teDurTotal:"+task.getLong("teDurTotal"));
//                                System.out.println("zon-h:"+zon);
                            }
                        }
                        // 判断任务列表需要删除的任务下标不为空
                        if (removeInd.size() > 0) {
                            // 降序排序循环存储下标
                            removeInd.sort(Comparator.reverseOrder());
                            // 遍历并删除列表对应下标的任务
                            for (int ind : removeInd) {
                                tasks.remove(ind);
                            }
                        }
                        // 添加删除后信息
                        teSsX.put("zon",zon);
                        teSsX.put("tasks",tasks);
                        grpBX.put(teS,teSsX);
                        depX.put(grpB,grpBX);
                        xObjTask.put(dep,depX);
                    });
                });
            });
        }
        // 遍历删除后的部门信息
        xObjTask.keySet().forEach(dep -> {
            // 获取删除后的部门信息
            JSONObject grpBs = xObjTask.getJSONObject(dep);
            // 获取镜像的部门信息
            JSONObject grpBsY = objTaskF.getJSONObject(dep);
            // 遍历删除后的组别信息
            grpBs.keySet().forEach(grpB -> {
                // 获取删除后的组别信息
                JSONObject teSs = grpBs.getJSONObject(grpB);
                // 获取镜像的组别信息
                JSONObject teSsY = grpBsY.getJSONObject(grpB);
                // 遍历删除后的时间戳信息
                teSs.keySet().forEach(teS -> {
                    // 直接添加删除后的任务信息到镜像任务信息
                    teSsY.put(teS,teSs.getJSONObject(teS));
                    grpBsY.put(grpB,teSsY);
                    objTaskF.put(dep,grpBsY);
                });
            });
        });
        // 添加镜像任务信息
        aArrange.put("objTask",objTaskF);
        // 创建请求参数存储字典
        JSONObject mapKey = new JSONObject();
        // 添加请求参数
        mapKey.put("aArrange",aArrange);
        // 请求修改卡片信息
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);

        // 创建请求更改参数
        mapKey = new JSONObject();
        // 添加请求更改参数信息
        mapKey.put("action.timeRecord",new JSONObject());
        // 调用接口发起数据库更改信息请求
        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);
    }

    /**
     * 任务最后处理方法
     * @param teDaF	时间冲突的副本
     * @param id_C  公司编号
     * @param randomAll	全局唯一编号
     * @param objTaskAll    全局任务信息
     * @param teDateFAllJ   存储任务所在日期
     * @param zonFAllJ  全局任务余剩总时间信息
     * @param tasksFAllJ    全局任务列表镜像信息
     * @param jiLJN 定义，存储进入未操作到的地方记录
     * @param id_O  订单编号
     * @param objOrderList  订单编号列表
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 2:55
     */
    public void setZui(JSONObject teDaF,String id_C,String randomAll,JSONObject objTaskAll,JSONObject teDateFAllJ
            ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ,JSONObject jiLJN
            ,String id_O,JSONArray objOrderList){
//        System.out.println();
        System.out.println("！！！-最后输出这里-！！！:");
        System.out.println();
//        System.out.println(JSON.toJSONString(teDaF));

        System.out.println("输出--是否有未操作到的情况被处理--");
//        JSONArray jsonArray = jiLJ.getJSONArray(randomAll);
        JSONArray jsonArray = jiLJN.getJSONArray(randomAll);
        System.out.println(jsonArray.size()==0?"无情况":"有这种情况");
        for (int i = 0; i < jsonArray.size(); i++) {
            System.out.println(jsonArray.get(i)+"---"+i);
        }
//        if (jsonArray.size() > 0) {
//            System.out.println();
//        }

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
        // 获取所有键（订单id）
        Set<String> strings = teDateFAllJ.keySet();
        // 遍历键（订单id）
        for (String string : strings) {
            // 根据键（订单id）获取信息
            JSONObject stringMapMap = teDateFAllJ.getJSONObject(string);
            // 获取键（index）
            Set<String> strings2 = stringMapMap.keySet();
            // 遍历键（index）
            for (String s : strings2) {
                // 根据键（index）获取任务所在时间对象
                JSONObject teDateF = stringMapMap.getJSONObject(s);
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

//        System.out.println();

        // 部门，组别，当天
        // 获取统一random对应的random唯一id，再根据唯一id获取任务信息镜像，再获取所有键（部门）
        Set<String> strings2 = tasksFAllJ.keySet();
        // 遍历键（部门）
        strings2.forEach(k -> {
            // 根据键（部门）获取组别对应的任务信息
            Map<String, Map<Long, List<Task>>> stringMapMap = tasksFAllJ.get(k);
            // 根据键（部门）获取组别任务当天余剩时间
            JSONObject stringMapMapZ = zonFAllJ.getJSONObject(k);
            // 遍历键（组别）
            stringMapMap.keySet().forEach(v->{
                // 根据键（组别）获取当前任务信息
                Map<Long, List<Task>> longListMap = stringMapMap.get(v);
                // 根据键（组别）获取任务当天余剩时间
                JSONObject longLongMapZ = stringMapMapZ.getJSONObject(v);
                // 遍历键（当前）
                longListMap.keySet().forEach(z->{
                    // 根据键（当天）获取当天任务余剩时间
                    Long zon = longLongMapZ.getLong(z+"");
                    // 获取当前任务信息
                    List<Task> tasks = longListMap.get(z);
                    // 调用写入任务到全局任务信息方法
                    setTasksAndZon(tasks,v,k,z,zon,objTaskAll);
                });
            });
        });
        // 创建主订单的时间处理所在日期存储信息
        JSONObject timeRecord = new JSONObject();
        // 遍历全局任务信息的部门信息
        objTaskAll.keySet().forEach(dep -> {
            // 获取全局任务信息的部门信息
            JSONObject depQjTask = objTaskAll.getJSONObject(dep);
            // 遍历全局任务信息的组别信息
            depQjTask.keySet().forEach(grpB -> {
                // 获取全局任务信息的组别信息
                JSONObject grpBQjTask = depQjTask.getJSONObject(grpB);
                // 遍历全局任务信息的时间戳信息
                grpBQjTask.keySet().forEach(teS -> {
                    // 获取全局任务信息的时间戳信息
                    JSONObject teSQJTask = grpBQjTask.getJSONObject(teS);
                    // 获取任务余剩总时间
                    long zon = teSQJTask.getLong("zon");
                    // 创建存储任务列表
                    List<Task> tasks = new ArrayList<>();
                    // 创建存储要清理的任务下标信息
                    List<Integer> jiS = new ArrayList<>();
                    // 存储任务列表是否全是系统任务
                    boolean isQf = false;
                    // 存储任务是否属于当前主订单
                    boolean isOf = false;
                    // 获取全局任务信息的任务列表
                    JSONArray tasksJ = teSQJTask.getJSONArray("tasks");
                    // 遍历全局任务列表
                    for (int i = 0; i < tasksJ.size(); i++) {
                        // 获取并且转换任务信息
                        Task task = JSONObject.parseObject(JSON.toJSONString(tasksJ.getJSONObject(i)), Task.class);
                        // 判断任务为空任务
                        if (task.getPriority() != -1 && task.getTeDurTotal() == 0 && task.getTeDelayDate() == 0) {
                            // 添加删除下标
                            jiS.add(i);
                        }
                        // 判断属于系统任务
                        if (task.getPriority() != -1 && !isQf) {
                            // 遍历当前主订单的订单列表
                            for (int i1 = 0; i1 < objOrderList.size(); i1++) {
                                // 判断当前任务属于当前主订单列表
                                if (task.getId_O().equals(objOrderList.getString(i1))){
                                    // 等于说明属于
                                    isOf = true;
                                    break;
                                }
                            }
                            // 设置为不是全部都是系统任务
                            isQf = true;
                        }
                        // 添加任务信息
                        tasks.add(task);
                    }
                    // 判断删除任务下标不为空
                    if (jiS.size() > 0) {
                        // 降序排序循环存储下标
                        jiS.sort(Comparator.reverseOrder());
                        // 遍历删除任务列表对应的任务
                        for (int ji : jiS) {
                            tasks.remove(ji);
                        }
                    }
                    // 调用写入任务到全局任务信息方法
                    setTasksAndZon(tasks,grpB,dep,Long.parseLong(teS),zon,objTaskAll);
                    // 判断不是全部都是系统任务，并且属于当前主订单
                    if (isQf && isOf) {
                        // 获取主订单的时间处理所在日期存储信息的部门信息
                        JSONObject timeDep = timeRecord.getJSONObject(dep);
                        if (null == timeDep) {
                            timeDep = new JSONObject();
                        }
                        // 获取主订单的时间处理所在日期存储信息的组别信息
                        JSONObject timeGrpB = timeDep.getJSONObject(grpB);
                        if (null == timeGrpB) {
                            timeGrpB = new JSONObject();
                        }
                        // 获取主订单的时间处理所在日期存储信息的时间戳信息
                        Long timeTeS = timeGrpB.getLong(teS + "");
                        if (null == timeTeS) {
                            timeGrpB.put(teS+"",1);
                        }
                        // 添加当前时间戳
                        timeDep.put(grpB,timeGrpB);
                        timeRecord.put(dep,timeDep);
                    }
                });
            });
        });
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的时间处理卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("aArrange"));
        // 获取时间处理卡片信息
        JSONObject aArrange = asset.getAArrange();
        if (null == aArrange) {
            aArrange = new JSONObject();
        }
        // 添加信息
        aArrange.put("objTask",objTaskAll);
        // 创建请求参数存储字典
        JSONObject mapKey = new JSONObject();
        // 添加请求参数
        mapKey.put("aArrange",aArrange);
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);

        System.out.println("timeRecord:");
        System.out.println(JSON.toJSONString(timeRecord));
        // 创建请求更改参数
        mapKey = new JSONObject();
        // 添加请求更改参数信息
        mapKey.put("action.timeRecord",timeRecord);
        if (!isTest) {
            mapKey.put("action.oDates",new JSONArray());
            mapKey.put("action.oTasks",new JSONArray());
        }
        // 调用接口发起数据库更改信息请求
        coupaUtil.updateOrderByListKeyVal(id_O,mapKey);

        asset = coupaUtil.getAssetById(assetId, Collections.singletonList("aArrange"));
        aArrange = asset.getAArrange();

        System.out.println();
        System.out.println("排序前-Tasks:");
        System.out.println(JSON.toJSONString(aArrange.getJSONObject("objTask")));
        System.out.println();

        // 判断是否有出息强制停止
        if (isQzTz.getInteger(randomAll) == 1) {
            System.out.println("-----出现强制停止-----");
            System.out.println();
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
        int isGrp;
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
                    isGrp = 3;
                }
            } else {
                // 设置为空状态为2
                isGrp = 2;
            }
        } else {
            // 设置为空状态为1
            isGrp = 1;
        }
        // 根据公司编号获取asset编号
        String assetId = coupaUtil.getAssetId(id_C, "a-chkin");
        // 根据asset编号获取asset的时间处理卡片信息
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("aArrange"));
//        System.out.println("获取任务集合:"+teStart+" - "+grpB+" - "+dep);
        // 获取时间处理卡片信息
        JSONObject aArrange = asset.getAArrange();
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
        JSONObject re = objGrpB.getJSONObject(teStart + "");
        // 判断为空状态为2
        if (isGrp == 2) {
            // 创建
            grpBTask = new JSONObject();
        } else if (isGrp == 1) {
            grpBTask = new JSONObject();
            depTask = new JSONObject();
        }
        // 添加任务信息
        grpBTask.put(teStart+"",re);
        depTask.put(grpB,grpBTask);
        objTaskAll.put(dep, depTask);
        return re;
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
        // 获取部门任务信息
        JSONObject depTask = objTaskAll.getJSONObject(dep);
        // 定义存储组别任务信息
        JSONObject grpBTask;
        // 存储当前要写入的任务信息
        JSONObject re = new JSONObject();
        // 添加当前任务信息
        re.put("tasks",tasks);
        re.put("zon",zon);
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
        grpBTask.put(teStart+"",re);
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
        Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("aArrange"));
        // 获取任务卡片信息
        JSONObject aArrange = asset.getAArrange();
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
        mapKey.put("aArrange",aArrange);
        coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
    }

    /**
     * 根据teStart，grpB，dep获取镜像任务信息方法
     * @param teS	当前时间戳
     * @param grpBNext	组别
     * @param depNext	部门
     * @param tasksFAllJ	全局镜像任务列表信息
     * @return java.util.List<com.cresign.tools.pojo.po.chkin.Task>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private List<Task> getTasksF(Long teS,String grpBNext,String depNext
            ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ){
        // 定义任务集合
        List<Task> tasks;
        // 判断获取部门任务为空
        if (null == tasksFAllJ.get(depNext)) {
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
            tasksFAllJ.put(depNext,stringMapMap);
        } else {
            // 根据部门获取部门任务
            Map<String, Map<Long, List<Task>>> stringMapMap = tasksFAllJ.get(depNext);
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
                tasksFAllJ.put(depNext,stringMapMap);
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
                    tasksFAllJ.put(depNext,stringMapMap);
                } else {
                    // 根据当前时间获取任务集合
                    tasks = longListMap.get(teS);
                    // 返回任务集合
                    return tasks;
                }
            }
        }
        // 返回任务集合
        return tasks;
    }

    /**
     * 根据teStart，grpB，dep获取镜像任务余剩时间信息方法
     * @param teS	当前时间戳
     * @param grpBNext	组别
     * @param depNext	部门
     * @param zonFAllJ	全局镜像任务余剩时间信息
     * @return java.lang.Long  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private Long getZonF(Long teS,String grpBNext,String depNext,JSONObject zonFAllJ){
        Long zon;
        JSONObject jsonObject = zonFAllJ.getJSONObject(depNext);
        if (null == jsonObject) {
            jsonObject = new JSONObject();
            JSONObject longLongMap = new JSONObject();
            zon = 28800L;
            longLongMap.put(teS+"",zon);
            jsonObject.put(grpBNext,longLongMap);
            zonFAllJ.put(depNext,jsonObject);
        } else {
            JSONObject longLongMap = jsonObject.getJSONObject(grpBNext);
            if (null == longLongMap) {
                longLongMap = new JSONObject();
                zon = 28800L;
                longLongMap.put(teS+"",zon);
                jsonObject.put(grpBNext,longLongMap);
                zonFAllJ.put(depNext,jsonObject);
            } else {
                if (null == longLongMap.get(teS+"")) {
                    zon = 28800L;
                    longLongMap.put(teS+"",zon);
                    jsonObject.put(grpBNext,longLongMap);
                    zonFAllJ.put(depNext,jsonObject);
                } else {
                    zon = longLongMap.getLong(teS+"");
                    return zon;
                }
            }
        }
        return zon;
    }

    /**
     * 根据id_O，index获取统一id_O和index存储记录状态信息方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @return java.lang.Integer  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private Integer getStorage(String id_O,Integer index,JSONObject storageResetJ){
        // 创建返回结果并赋值，re == 0 正常状态存储、re == 1 冲突状态存储、re == 2 调用时间处理状态存储
        Integer re = 0;
        JSONObject jsonObject = storageResetJ.getJSONObject(id_O);
        // 判断存储记录状态信息为空
        if (null == jsonObject) {
            // 创建存储记录状态信息
            JSONObject map = new JSONObject();
            // 添加订单下标记录状态
            map.put(index.toString(),re);
            // 添加订单编号记录状态
            storageResetJ.put(id_O,map);
        } else {
            // 根据订单编号获取存储记录状态信息
            Integer integer = jsonObject.getInteger(index.toString());
            // 判断存储记录状态信息为空
            if (null == integer) {
                // 添加订单下标记录状态
                jsonObject.put(index.toString(),re);
                // 添加订单编号记录状态
                storageResetJ.put(id_O,jsonObject);
            } else {
                // 赋值返回结果
                return jsonObject.getInteger(index.toString());
            }
        }
        return re;
    }

    /**
     * 根据id_O，index获取任务所在日期方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param teDateFAllJ	存储任务所在日期
     * @return java.util.List<java.lang.String>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private List<String> getTeDateF(String id_O,Integer index,JSONObject teDateFAllJ){
        List<String> re = new ArrayList<>();
        JSONObject jsonObject = teDateFAllJ.getJSONObject(id_O);
        if (null == jsonObject) {
            JSONObject map = new JSONObject();
            map.put(index.toString(),new JSONObject());
            teDateFAllJ.put(id_O,map);
        } else {
            JSONObject jsonObject1 = jsonObject.getJSONObject(index.toString());
            if (null == jsonObject1) {
                jsonObject.put(index.toString(),new JSONObject());
                teDateFAllJ.put(id_O,jsonObject);
            } else {
                return new ArrayList<>(jsonObject1.keySet());
            }
        }
//        System.out.println("获取镜像所在日期列表:-id_O:"+id_O+",-index:"+index);
        return re;
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
        int isGrp;
        // 判断不为空
        if (null != depGrpUNum) {
            // 根据组别获取职位人数
            Integer zwNum = depGrpUNum.getInteger(grpB);
            // 判断职位人数不为空
            if (null != zwNum) {
                // 直接返回结果
                return zwNum;
            } else {
                isGrp = 2;
            }
        } else {
            isGrp = 1;
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
            if (isGrp == 1) {
                depGrpUNum = new JSONObject();
            }
            depGrpUNum.put(grpB, 1);
            grpUNumAll.put(dep, depGrpUNum);
            return 1;
        } else {
            Integer reI = depObj.getInteger(grpB);
            if (null == reI){
                reI = 1;
                System.out.println("grpUNum-为空-grpB:"+grpB);
                depObj.put(grpB,reI);
                objZw.put(dep,depObj);
                chkin00s.put("objZw",objZw);
                // 创建请求参数存储字典
                JSONObject mapKey = new JSONObject();
                // 添加请求参数
                mapKey.put("chkin00s",chkin00s);
                coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
            }
            if (isGrp == 1) {
                depGrpUNum = new JSONObject();
            }
            depGrpUNum.put(grpB, reI);
            grpUNumAll.put(dep, depGrpUNum);
            return reI;
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
    private JSONObject getXbAndSb(String grpB,String dep,String id_C,JSONObject xbAndSbAll){
//        System.out.println("获取不上班打卡时间:"+teStart+"-"+grpB+"-"+dep+"- id_C:"+id_C);
        // 获取全局上班下班信息的部门信息
        JSONObject depXbSb = xbAndSbAll.getJSONObject(dep);
        // 存储为空状态
        int isGrp;
        // 判断部门信息不为空
        if (null != depXbSb) {
            // 获取全局上班下班信息的组别信息
            JSONObject grpBXbSb = depXbSb.getJSONObject(grpB);
            // 判断组别信息不为空
            if (null != grpBXbSb) {
                return grpBXbSb;
            } else {
                isGrp = 2;
            }
        } else {
            isGrp = 1;
        }
        // 创建存储返回结果
        JSONObject re = new JSONObject();
        // 定义存储上班下班部门信息
        JSONObject depO;
        // 存储时间处理打卡信息下标
        int wtInd = 0;
        // 存储是否存在时间处理打卡信息
        int timeP = 0;
        // 存储打卡信息为空状态
        int objDataP = 0;
        // 存储打卡信息部门组别下标状态
        int objWorkTimeP = 0;
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
            JSONObject dataZ = new JSONObject();
            // 添加信息
            dataZ.put("timeP",1);
            dataZ.put("objXb",Obj.getXbJson());
            dataZ.put("objSb",Obj.getSbJson());
            objData.add(dataZ);
            objDataP = 1;
        } else {
            // 遍历打卡信息
            for (int i = 0; i < objData.size(); i++) {
                JSONObject dataZ = objData.getJSONObject(i);
                // 判断存储时间处理打卡信息
                if (null != dataZ.getInteger("timeP")) {
                    // 存在则赋值
                    wtInd = i;
                    timeP = 1;
                    break;
                }
            }
            // 判断不存在时间处理打卡信息
            if (timeP == 0) {
                // 创建时间处理打卡信息
                JSONObject dataZ = new JSONObject();
                // 添加信息
                dataZ.put("timeP",1);
                dataZ.put("objXb",Obj.getXbJson());
                dataZ.put("objSb",Obj.getSbJson());
                objData.add(dataZ);
                wtInd = objData.size()-1;
                objDataP = 2;
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
            depO = new JSONObject();
            depO.put(grpB,wtInd);
            objWorkTime.put(dep,depO);
            objGrpB = wtInd;
            objWorkTimeP = 1;
        } else {
            JSONObject objDep = objWorkTime.getJSONObject(dep);
            if (null == objDep) {
                objDep = new JSONObject();
                objDep.put(grpB,wtInd);
                objWorkTime.put(dep,objDep);
                objGrpB = wtInd;
                objWorkTimeP = 2;
            } else {
                objGrpB = objDep.getInteger(grpB);
                if (null == objGrpB) {
                    objDep.put(grpB,wtInd);
                    objWorkTime.put(dep,objDep);
                    objGrpB = wtInd;
                    objWorkTimeP = 3;
                } else if (objGrpB != wtInd) {
                    objDep.put(grpB,wtInd);
                    objWorkTime.put(dep,objDep);
                    objGrpB = wtInd;
                    objWorkTimeP = 4;
                }
            }
        }
        // 判断打卡信息为空，或者部门组别存储打卡信息下标信息为空
        if (objDataP > 0 || objWorkTimeP > 0) {
            if (objWorkTimeP > 0) {
                chkin00s.put("objWorkTime",objWorkTime);
            }
            if (objDataP > 0) {
                chkin00s.put("objData",objData);
            }
            // 创建请求参数存储字典
            JSONObject mapKey = new JSONObject();
            // 添加请求参数
            mapKey.put("chkin00s",chkin00s);
            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
        }
        JSONObject jsObj = objData.getJSONObject(objGrpB);
        re.put("xb",jsObj.getJSONArray("objXb"));
        re.put("sb",jsObj.getJSONArray("objSb"));

        System.out.println("获取不上班和上班时间:"+"-"+grpB+"-"+dep+"- id_C:"+id_C);

        if (isGrp == 1) {
            depXbSb = new JSONObject();
        }
        depXbSb.put(grpB, re);
        xbAndSbAll.put(dep, depXbSb);
        return re;
    }

    /**
     * 写入镜像任务集合方法
     * @param tasks	任务集合
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param tasksFAllJ	全局镜像任务列表信息
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void setTasksF(List<Task> tasks,String grpBNext,String depNext,Long teS
            ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ){
        if (null == tasksFAllJ.get(depNext)) {
            Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
            Map<Long, List<Task>> tasksFz2 = new HashMap<>();
            tasksFz2.put(teS,tasks);
            tasksFz.put(grpBNext,tasksFz2);
            tasksFAllJ.put(depNext,tasksFz);
        } else {
            Map<String,Map<Long,List<Task>>> tasksFz = tasksFAllJ.get(depNext);
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
            tasksFAllJ.put(depNext,tasksFz);
        }
    }

    /**
     * 写入镜像任务余剩时间方法
     * @param zon	任务余剩时间
     * @param grpBNext	组别
     * @param depNext	部门
     * @param teS	当前时间戳
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void setZonF(Long zon,String grpBNext,String depNext,Long teS,JSONObject zonFAllJ){
//        System.out.println("写入镜像总上班时间:"+zon);
        JSONObject zonFz = zonFAllJ.getJSONObject(depNext);
        if (null == zonFz) {
            zonFz = new JSONObject();
            JSONObject zonFz2 = new JSONObject();
            zonFz2.put(teS+"",zon);
            zonFz.put(grpBNext,zonFz2);
        } else {
            JSONObject zonFz2 = zonFz.getJSONObject(grpBNext);
            if (null == zonFz2) {
                zonFz2 = new JSONObject();
            }
            zonFz2.put(teS+"",zon);
            zonFz.put(grpBNext,zonFz2);
        }
        zonFAllJ.put(depNext,zonFz);
    }

    /**
     * 写入id_O，index和存储记录状态方法
     * @param num	状态
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void setStorage(Integer num,String id_O,Integer index,JSONObject storageResetJ){
//        System.out.println("写入镜像存储重置:"+num);
        JSONObject jsonObject = storageResetJ.getJSONObject(id_O);
        if (null == jsonObject) {
            JSONObject map = new JSONObject();
            map.put(index.toString(),num);
            storageResetJ.put(id_O,map);
        } else {
            jsonObject.put(index.toString(),num);
            storageResetJ.put(id_O,jsonObject);
        }
    }

    /**
     * 写入任务所在日期方法
     * @param id_O	订单编号
     * @param index	订单编号对应的下标
     * @param teS	当前时间戳
     * @param teDateFAllJ	存储任务所在日期
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void setTeDateF(String id_O, Integer index, Long teS,JSONObject teDateFAllJ){
        JSONObject jsonObject = teDateFAllJ.getJSONObject(id_O);
        if (null == jsonObject) {
            JSONObject map = new JSONObject();
            map.put(index.toString(),new JSONObject());
            teDateFAllJ.put(id_O,map);
        } else {
            JSONObject map1 = jsonObject.getJSONObject(index.toString());
            map1.put(teS.toString(),0);
            jsonObject.put(index.toString(),map1);
            teDateFAllJ.put(id_O,jsonObject);
        }
    }

    /**
     * 获取当前时间戳方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return java.lang.Long  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    public Long getTeS(String random,String grpB,String dep,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ){
        JSONObject rand;
        long getTe;
        if (null == onlyStartMapAndDepJ.getJSONObject(random)) {
            rand = new JSONObject();
            JSONObject onlyDep = new JSONObject();
            onlyDep.put(grpB,onlyStartMapJ.getLong(random));
            getTe = onlyStartMapJ.getLong(random);
            rand.put(dep,onlyDep);
            onlyStartMapAndDepJ.put(random,rand);
        } else {
            rand = onlyStartMapAndDepJ.getJSONObject(random);
            JSONObject onlyDep;
            if (null == rand.getJSONObject(dep)) {
                onlyDep = new JSONObject();
                onlyDep.put(grpB,onlyStartMapJ.getLong(random));
                getTe = onlyStartMapJ.getLong(random);
                rand.put(dep,onlyDep);
                onlyStartMapAndDepJ.put(random,rand);
            } else {
                onlyDep = rand.getJSONObject(dep);
                if (null == onlyDep.getLong(grpB)) {
                    onlyDep.put(grpB,onlyStartMapJ.getLong(random));
                    rand.put(dep,onlyDep);
                    onlyStartMapAndDepJ.put(random,rand);
                    getTe = onlyStartMapJ.getLong(random);
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
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void setTeS(String random,String grpB,String dep,Long teS,JSONObject onlyStartMapAndDepJ){
        JSONObject rand;
        if (null == onlyStartMapAndDepJ.getJSONObject(random)) {
            rand = new JSONObject();
            JSONObject onlyDep = new JSONObject();
            onlyDep.put(grpB,teS);
            rand.put(dep,onlyDep);
            onlyStartMapAndDepJ.put(random,rand);
        } else {
            rand = onlyStartMapAndDepJ.getJSONObject(random);
            JSONObject onlyDep;
            if (null == rand.getJSONObject(dep)) {
                onlyDep = new JSONObject();
                onlyDep.put(grpB,teS);
                rand.put(dep,onlyDep);
                onlyStartMapAndDepJ.put(random,rand);
            } else {
                onlyDep = rand.getJSONObject(dep);
                if (null == onlyDep.getLong(grpB)) {
                    onlyDep.put(grpB,teS);
                    rand.put(dep,onlyDep);
                    onlyStartMapAndDepJ.put(random,rand);
                }
            }
        }
    }

    /**
     * 获取任务综合信息方法
     * @param random	当前唯一编号
     * @param grpB	组别
     * @param dep	部门
     * @param is	is = 0 使用random，grpB，dep获取当前时间戳、is = 1 使用teS为时间戳
     * @param teS	当前时间戳
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @param objTaskAll	全局任务信息
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private Map<String,Object> getJumpDay(String random,String grpB,String dep,int is,Long teS
            ,Integer isC,String id_C,JSONObject xbAndSbAll,JSONObject objTaskAll
            ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ){
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
                JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep,onlyStartMapJ
                        ,onlyStartMapAndDepJ), grpB, dep, id_C,objTaskAll);
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
                    re = getChkInJumpDay1(random,grpB,dep,id_C,xbAndSbAll,onlyStartMapJ,onlyStartMapAndDepJ);
                }
            } else {
//                System.out.println("进入jump-2:");
                // 创建返回对象
                re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                // 调用获取镜像任务信息获取任务集合
                tasks = getTasksF(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),grpB,dep,tasksFAllJ);
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 调用获取镜像任务余剩时间获取任务余剩时间
                    zon = getZonF(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),grpB,dep,zonFAllJ);
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(getTeS(random, grpB, dep,onlyStartMapJ
                            ,onlyStartMapAndDepJ), grpB, dep, id_C,objTaskAll);
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
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
                        zon = getZonF(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),grpB,dep,zonFAllJ);
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
                        tasks.add(Obj.getTask(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                ,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                ,"",-1,0L,-1,"电脑",0L,0L,id_C
                                ,0L,0L));
                        // 遍历不上班时间
                        for (int i = 0; i < offWork.size(); i++) {
                            // 根据下标i获取不上班时间段
                            JSONObject jsonObject = offWork.getJSONObject(i);
                            // 任务集合添加任务信息
                            tasks.add(Obj.getTask((getTeS(random,grpB,dep,onlyStartMapJ
                                    ,onlyStartMapAndDepJ) + jsonObject.getLong("tePStart"))
                                    , (getTeS(random,grpB,dep,onlyStartMapJ
                                            ,onlyStartMapAndDepJ) + jsonObject.getLong("tePFinish"))
                                    , "", -1, jsonObject.getLong("zon")
                                    , jsonObject.getInteger("priority"), "电脑", 0L,0L,id_C
                                    ,(getTeS(random,grpB,dep,onlyStartMapJ
                                            ,onlyStartMapAndDepJ) + jsonObject.getLong("tePStart")),0L));
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
                JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C,objTaskAll);
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
                    re = getChkInJumpDay2(teS, grpB, dep,id_C,xbAndSbAll);
                }
            } else {
//                System.out.println("进入jump-4:");
                // 创建返回对象
                re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
                // 调用获取镜像任务信息获取任务集合
                tasks = getTasksF(teS,grpB,dep,tasksFAllJ);
                // 判断任务集合不为空
                if (tasks.size() != 0) {
                    // 调用获取镜像任务余剩时间获取任务余剩时间
                    zon = getZonF(teS,grpB,dep,zonFAllJ);
                } else {
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpB, dep, id_C,objTaskAll);
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
                        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
                        // 调用获取上班时间
                        JSONArray chkIn = xbAndSb.getJSONArray("sb");
                        // 调用获取不上班时间
                        JSONArray offWork = xbAndSb.getJSONArray("xb");
                        // 调用获取镜像任务余剩时间获取任务余剩时间
                        zon = getZonF(teS,grpB,dep,zonFAllJ);
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
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private Map<String,Object> getChkInJumpDay1(String random,String grpB,String dep,String id_C
            ,JSONObject xbAndSbAll,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ){
        // 创建返回对象
        Map<String,Object> re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 定义任务余剩时间
        long zon;
        // 定义任务集合
        List<Task> tasks;
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
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
        tasks.add(Obj.getTask(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                ,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                ,"",-1,0L,-1,"电脑",0L,0L,id_C,0L,0L));
        // 遍历不上班时间
        for (int i = 0; i < offWork.size(); i++) {
            // 根据下标i获取不上班时间段
            JSONObject jsonObject = offWork.getJSONObject(i);
            // 任务集合添加任务信息
            tasks.add(Obj.getTask((getTeS(random,grpB,dep,onlyStartMapJ
                    ,onlyStartMapAndDepJ) + jsonObject.getLong("tePStart"))
                    , (getTeS(random,grpB,dep,onlyStartMapJ
                            ,onlyStartMapAndDepJ) + jsonObject.getLong("tePFinish"))
                    , "", -1, jsonObject.getLong("zon")
                    , jsonObject.getInteger("priority"), "电脑", 0L,0L,id_C
                    ,(getTeS(random,grpB,dep,onlyStartMapJ
                            ,onlyStartMapAndDepJ) + jsonObject.getLong("tePStart")),0L));
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
     * @param id_C	公司编号
     * @param xbAndSbAll	全局上班下班信息
     * @return java.util.Map<java.lang.String,java.lang.Object>  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private Map<String,Object> getChkInJumpDay2(Long teS,String grpB,String dep,String id_C,JSONObject xbAndSbAll){
        // 创建返回对象
        Map<String,Object> re = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 定义任务余剩时间
        long zon;
        // 定义任务集合
        List<Task> tasks;
        JSONObject xbAndSb = getXbAndSb(grpB, dep, id_C,xbAndSbAll);
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
     * @param teDaF	当前任务所在时间
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param isR	isR = 0 不是空插处理、isR = 1 是空插处理
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param id_C	公司编号
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject getBcCtH(String id_oNext,Integer indONext,JSONObject teDaF,Integer isC,Integer isR
            ,JSONObject sho,String id_C,int csSta,String randomAll,JSONObject xbAndSbAll
            ,JSONObject actionIdO,JSONObject objTaskAll,JSONObject storageResetJ,JSONObject teDateFAllJ
            ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
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
                teDateFAllJ = new JSONObject();
            } else {
                // 设置是空插处理
                isR = 1;
            }
        }
        // 获取订单递归信息
        JSONArray objActionNext = actionIdO.getJSONArray(id_oNext);
        if (null == objActionNext) {
//            System.out.println("为空-获取数据库:"+id_oNext);
            // 根据当前订单编号获取订单信息
            Order orderNext = coupaUtil.getOrderByListKey(id_oNext, Collections.singletonList("action"));
            // 获取订单递归信息
            objActionNext = orderNext.getAction().getJSONArray("objAction");
            actionIdO.put(id_oNext,objActionNext);
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
        List<String> p = getTeDateF(id_oNext,indONext,teDateFAllJ);
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
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C,objTaskAll);
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
                    tasks = getTasksF(teS,grpBNext,depNext,tasksFAllJ);
                    // 判断任务集合不等于空
                    if (tasks.size() != 0) {
                        // 调用获取镜像任务余剩时间方法
                        zon = getZonF(teS,grpBNext,depNext,zonFAllJ);
                    } else {
                        // 创建任务集合
                        tasks = new ArrayList<>();
                        JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C,objTaskAll);
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
                tasks = getTasksF(teS,grpBNext,depNext,tasksFAllJ);
                // 判断任务集合不等于空
                if (tasks.size() != 0) {
                    // 调用获取镜像任务余剩时间方法
                    zon = getZonF(teS,grpBNext,depNext,zonFAllJ);
                } else {
                    // 创建任务集合
                    tasks = new ArrayList<>();
                    // 调用获取数据库任务集合方法
                    List<Task> tasks1 = new ArrayList<>();
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C,objTaskAll);
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
                tasksFAllJ = new HashMap<>();
                // 创建任务部门对象
                Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
                // 创建任务组别对象
                Map<Long, List<Task>> tasksFz2 = new HashMap<>();
                // 添加任务集合
                tasksFz2.put(teS,tasks);
                // 添加任务组别对象
                tasksFz.put(grpBNext,tasksFz2);
                // 添加任务部门对象
                tasksFAllJ.put(depNext,tasksFz);

                // 创建当前唯一编号的镜像任务余剩时间对象
                zonFAllJ = new JSONObject();
                // 创建任务余剩时间部门对象
                JSONObject zonFz = new JSONObject();
                // 创建任务余剩时间组别对象
                JSONObject zonFz2 = new JSONObject();
                // 添加任务余剩时间
                zonFz2.put(teS+"",zon);
                // 添加任务余剩时间组别对象
                zonFz.put(grpBNext,zonFz2);
                // 添加任务余剩时间部门对象
                zonFAllJ.put(depNext,zonFz);

                storageResetJ = new JSONObject();
            } else {
                // 调用写入任务余剩时间镜像方法
                setZonF(zon,grpBNext,depNext,teS,zonFAllJ);
                // 调用写入任务集合镜像方法
                setTasksF(tasks,grpBNext,depNext,teS,tasksFAllJ);
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
                    JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C,objTaskAll);
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
                        tasksFAllJ = new HashMap<>();
                        // 创建任务部门对象
                        Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
                        // 创建任务组别对象
                        Map<Long, List<Task>> tasksFz2 = new HashMap<>();
                        // 添加任务集合
                        tasksFz2.put(teS,tasks);
                        // 添加任务组别对象
                        tasksFz.put(grpBNext,tasksFz2);
                        // 添加任务部门对象
                        tasksFAllJ.put(depNext,tasksFz);

                        // 创建当前唯一编号的镜像任务余剩时间对象
                        zonFAllJ = new JSONObject();
                        // 创建任务余剩时间部门对象
                        JSONObject zonFz = new JSONObject();
                        // 创建任务余剩时间组别对象
                        JSONObject zonFz2 = new JSONObject();
                        // 添加任务余剩时间
                        zonFz2.put(teS+"",zon);
                        // 添加任务余剩时间组别对象
                        zonFz.put(grpBNext,zonFz2);
                        // 添加任务余剩时间部门对象
                        zonFAllJ.put(depNext,zonFz);

                        storageResetJ = new JSONObject();
                    } else {
                        // 调用写入任务余剩时间镜像方法
                        setZonF(zon,grpBNext,depNext,teS,zonFAllJ);
                        // 调用写入任务集合镜像方法
                        setTasksF(tasks,grpBNext,depNext,teS,tasksFAllJ);
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
                        JSONObject tasksAndZon = getTasksAndZon(teS, grpBNext, depNext, id_C,objTaskAll);
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
                        tasksFAllJ = new HashMap<>();
                        // 创建任务部门对象
                        Map<String, Map<Long, List<Task>>> tasksFz = new HashMap<>();
                        // 创建任务组别对象
                        Map<Long, List<Task>> tasksFz2 = new HashMap<>();
                        // 添加任务集合
                        tasksFz2.put(teS,tasks);
                        // 添加任务组别对象
                        tasksFz.put(grpBNext,tasksFz2);
                        // 添加任务部门对象
                        tasksFAllJ.put(depNext,tasksFz);

                        // 创建当前唯一编号的镜像任务余剩时间对象
                        zonFAllJ = new JSONObject();
                        // 创建任务余剩时间部门对象
                        JSONObject zonFz = new JSONObject();
                        // 创建任务余剩时间组别对象
                        JSONObject zonFz2 = new JSONObject();
                        // 添加任务余剩时间
                        zonFz2.put(teS+"",zon);
                        // 添加任务余剩时间组别对象
                        zonFz.put(grpBNext,zonFz2);
                        // 添加任务余剩时间部门对象
                        zonFAllJ.put(depNext,zonFz);

                        storageResetJ = new JSONObject();
                    }
//                    System.out.println("这里输出task-带延迟加所有时间-3:");
//                    System.out.println(JSON.toJSONString(task));
                }
            }
        }
        // 判断订单信息不为空
        if (null != task) {
            // 获取唯一下标
            String random = MongoUtils.GetObjectId();
            // 添加唯一编号状态
            onlyIsDsJ.put(random,1);
            // 根据键获取当前时间戳
            long teS = Long.parseLong(p.get(0));
            // 添加唯一标识的当前时间戳
            onlyStartMapJ.put(random,teS);
            // 调用写入当前时间戳
            setTeS(random,grpBNext,depNext,teS,onlyStartMapAndDepJ);
            // 创建当前递归任务所在日期对象
            JSONObject teDate = new JSONObject();
            // 调用时间处理方法
            chkInJi(task,teS,grpBNext,depNext,id_oNext,indONext,0,random,1,teDate,teDaF,1,sho,isYx,csSta
//                    ,true
                    ,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ
                    ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
            // 根据当前唯一标识删除信息
            onlyIsDsJ.remove(random);
            onlyStartMapAndDepJ.remove(random);
            onlyStartMapJ.remove(random);

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
            JSONObject reX = getBcCtH(id_OY, indOY, teDaF, 0, 1
                    ,sho,task.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ,teDateFAllJ
                    ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
            // 赋值问题存储
            isPd = reX.getInteger("isPd");
        }
        // 添加返回信息
        re.put("isPd",isPd);
        return re;
    }

    /**
     * 处理冲突核心方法
     * @param i	当前任务对应循环下标
     * @param tasks	任务集合
     * @param conflict	用于存储被冲突的任务集合
     * @param zon	当前任务余剩时间
     * @param random	当前唯一编号
     * @param dep	部门
     * @param grpB	组别
     * @param teDaF	当前任务所在日期
     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
     * @param ts	ts = 0 获取当前第一次初始时间戳、ts = 1 获取最新的（最后一个）当前时间戳
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject getBqCt(int i,List<Task> tasks,List<Task> conflict,Long zon
            ,String random,String dep,String grpB,JSONObject teDaF
            ,Integer isC,Integer ts,JSONObject sho,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll,JSONObject storageResetJ
            ,JSONObject teDateFAllJ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
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
            List<String> teDateYKeyZ = getTeDateF(taskX.getId_O(), taskX.getIndex(),teDateFAllJ);
            // 获取递归信息
            JSONArray objAction = actionIdO.getJSONArray(taskX.getId_O());
            if (null == objAction) {
//                System.out.println("为空-获取数据库-2:"+taskX.getId_O());
                // 根据被冲突任务订单编号获取订单信息
                Order order = coupaUtil.getOrderByListKey(taskX.getId_O(), Collections.singletonList("action"));
                // 获取递归信息
                objAction = order.getAction().getJSONArray("objAction");
                actionIdO.put(taskX.getId_O(),objAction);
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
            setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD,onlyStartMapAndDepJ);
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
                Integer storage = getStorage(taskX.getId_O(), taskX.getIndex(),storageResetJ);
                // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
                if (storage == 0) {
                    // 调用写入存储记录状态方法
                    setStorage(1, taskX.getId_O(), taskX.getIndex(),storageResetJ);
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
                            ,random,dep,grpB,objAction,actZ,yTeD,teDaF,isC,sho,csSta,randomAll,xbAndSbAll,actionIdO
                            ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                            ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                            JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF, isC, 0
                                    , sho,taskX.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ
                                    ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                            // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                            isPd = bcCtH.getInteger("isPd");
                            // 创建任务所在时间对象
                            teDateO = new JSONObject();
                            // 判断当前任务订单编号不等于冲突任务订单编号
                            if (!id_OD.equals(taskXH.getId_O())) {
                                // 获取所有递归信息
                                objAction = actionIdO.getJSONArray(taskXH.getId_O());
                                if (null == objAction) {
//                                    System.out.println("为空-获取数据库-3:"+taskXH.getId_O());
                                    // 根据冲突任务订单编号获取订单信息
                                    Order order = coupaUtil.getOrderByListKey(taskXH.getId_O(), Collections.singletonList("action"));
                                    // 获取所有递归信息
                                    objAction = order.getAction().getJSONArray("objAction");
                                    actionIdO.put(taskXH.getId_O(),objAction);
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
                            setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD,onlyStartMapAndDepJ);
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
                        JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF, isC, 0
                                , sho,taskX.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ
                                ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                        // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                        isPd = bcCtH.getInteger("isPd");
                    }
                    // isT2 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
                    isT2 = json.getInteger("isT2");
                    // 空插冲突强制停止参数累加
                    leiW.put(randomAll,(leiW.getInteger(randomAll)+1));
                    // 判断空插强制停止参数等于60
                    if (leiW.getInteger(randomAll) == 560) {
//                        System.out.println("----进入强制停止空差冲突方法-1----");
                        // 强制停止出现后的记录参数赋值等于1
                        isQzTz.put(randomAll,1);
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
                JSONObject rand = onlyStartMapAndDepJ.getJSONObject(random);
                // 根据部门获取获取最新的（最后一个）当前时间戳
                JSONObject onlyDep = rand.getJSONObject(dep);
                // 根据组别获取获取最新的（最后一个）当前时间戳
                teS = onlyDep.getLong(grpB);
            } else {
                // 根据当前唯一编号获取存储当前唯一编号的第一个当前时间戳
                teS = onlyStartMapJ.getLong(random);
            }
            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 1) {
//                System.out.println("进入这里写入:");
                // 调用写入镜像任务集合方法
                setTasksF(tasks,grpB,dep,teS,tasksFAllJ);
                // 调用写入镜像任务余剩时间方法
                setZonF(zon,grpB,dep,teS,zonFAllJ);
            }
            // 调用空插方法，获取存储问题状态
            isPd = kc(tasks, conflictInd, conflict, taskX, zon, grpB, dep, random, isT2, teS, 0, teDateO
                    , objAction, actZ, yTeD, teDaF, isC, sho, isPd,csSta,randomAll,xbAndSbAll
                    ,actionIdO,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                    ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    private int kc(List<Task> tasks,int conflictInd,List<Task> conflict,Task taskX,Long zon,String grpB
            ,String dep,String random,int isT2,Long teS,int is,JSONObject teDate,JSONArray objAction
            ,JSONObject actZ,Long yTeD,JSONObject teDaF,Integer isC,JSONObject sho,int isPd,int csSta
            ,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll,JSONObject storageResetJ
            ,JSONObject teDateFAllJ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
        // isT2 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
        if (isT2 == 1) {
            // 当前时间戳加一天
            teS += 86400L;
            // 任务所在时间键的第一个键的值（时间戳）加一天
            yTeD += 86400L;
            System.out.println("--进入空插冲突跳天--");
            // 调用获取任务综合信息方法
            Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,1,teS,isC,taskX.getId_C()
                    ,xbAndSbAll,objTaskAll,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                    JSONObject json = konChaConflict(taskX, task1X, task2X, tasks2, i1, conflictInd, zon2, conflict
                            ,teDate,random,dep,grpB,objAction,actZ,yTeD,teDaF,isC,sho,csSta,randomAll,xbAndSbAll
                            ,actionIdO,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                            ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                            JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF, isC, 1
                                    , sho,taskX.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ
                                    ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                            // 获取存储问题状态参数
                            isPd2 = bcCtH.getInteger("isPd");
                            // 创建任务所在日期对象
                            teDate = new JSONObject();
                            // 判断当前任务订单编号不等于冲突任务订单编号
                            if (!id_OD.equals(taskXH.getId_O())) {
                                // 获取进度卡片的所有递归信息
                                objAction = actionIdO.getJSONArray(taskXH.getId_O());
                                if (null == objAction) {
//                                    System.out.println("为空-获取数据库-4:"+taskXH.getId_O());
                                    // 根据冲突任务订单编号获取订单信息 - t
                                    Order order = coupaUtil.getOrderByListKey(taskXH.getId_O(), Collections.singletonList("action"));
                                    // 获取进度卡片的所有递归信息
                                    objAction = order.getAction().getJSONArray("objAction");
                                    actionIdO.put(taskXH.getId_O(),objAction);
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
                            setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD,onlyStartMapAndDepJ);
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
                            setTasksF(tasks2,grpB,dep,teS,tasksFAllJ);
                            // 调用写入镜像任务余剩时间
                            setZonF(zon2,grpB,dep,teS,zonFAllJ);
                        }
                        // 调用获取冲突处理方法，原方法
                        JSONObject bcCtH = getBcCtH(actZ.getString("id_ONext"), actZ.getInteger("indONext"), teDaF, isC, 1
                                , sho,taskX.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ
                                ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                        // 获取存储问题状态参数
                        isPd2 = bcCtH.getInteger("isPd");
                    }
//                    System.out.println("taskX-kc-2:");
//                    System.out.println(JSON.toJSONString(taskX));
                    // isT22 == 0 正常时间够用停止状态、isT2 == 1 时间不够用停止状态
                    isT22 = json.getInteger("isT2");
                    // 空插冲突强制停止参数累加
                    leiW.put(randomAll,(leiW.getInteger(randomAll)+1));
                    // 判断空插冲突强制停止参数等于60
                    if (leiW.getInteger(randomAll) == 560) {
                        System.out.println("----进入强制停止空差冲突方法-2----");
                        // 赋值强制停止出现后的记录参数等于1
                        isQzTz.put(randomAll,1);
                        break;
                    }
                }
            }
            // 判断空插冲突强制停止参数小于61
            if (leiW.getInteger(randomAll) < 561) {
                System.out.println("---这里问题---:"+isPd2);
                // 调用空插处理方法
                kc(tasks2,conflictInd,conflict,taskX,zon2,grpB,dep,random,isT22,teS,1,teDate
                        ,objAction,actZ,yTeD,teDaF,isC,sho,isPd2,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                        ,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                return isPd2;
            } else {
                System.out.println("----进入强制停止空差冲突方法-2-1----");
                // 赋值强制停止出现后的记录参数等于1
                isQzTz.put(randomAll,1);
            }
        }
        else {
            // is == 0 正常第一次调用空插处理方法、is == 1 空插处理方法调用空插处理方法
            if (is == 1) {
                // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
                if (isC == 0) {
                    setTasksAndZon(tasks,grpB,dep,teS,zon,objTaskAll);
                } else {
                    // 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                    if (isPd != 2) {
                        // 调用写入镜像任务集合方法
                        setTasksF(tasks,grpB,dep,teS,tasksFAllJ);
                        // 调用写入镜像任务余剩时间方法
                        setZonF(zon,grpB,dep,teS,zonFAllJ);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject getBqCt2(int conflictInd,List<Task> tasks,List<Task> conflict,Task taskX,Long zon
            ,JSONObject teDate,String random,JSONArray objAction,JSONObject actZ
            ,int i1,String dep,String grpB,Long yTeD,JSONObject teDaF,Integer isC,JSONObject sho
            ,int csSta,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO
            ,JSONObject objTaskAll,JSONObject storageResetJ,JSONObject teDateFAllJ
            ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
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
                        ,random, dep,grpB,objAction,actZ,yTeD,teDaF,isC,sho,csSta,randomAll,xbAndSbAll,actionIdO
                        ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                        getBcCtH(actZ.getString("id_ONext"),actZ.getInteger("indONext"),teDaF,isC,0
                                ,sho,taskX.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ
                                ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                        // 创建任务所在日期对象
                        teDate = new JSONObject();
                        // 判断当前任务订单编号不等于冲突任务订单编号
                        if (!id_OD.equals(taskXH.getId_O())) {
                            // 获取进度卡片的所有递归信息
                            objAction = actionIdO.getJSONArray(taskXH.getId_O());
                            if (null == objAction) {
//                                System.out.println("为空-获取数据库-5:"+taskXH.getId_O());
                                // 根据冲突任务订单编号获取订单信息 - t
                                Order order = coupaUtil.getOrderByListKey(taskXH.getId_O(), Collections.singletonList("action"));
                                // 获取进度卡片的所有递归信息
                                objAction = order.getAction().getJSONArray("objAction");
                                actionIdO.put(taskXH.getId_O(),objAction);
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
                        setTeS(random , actZ.getString("grpB"), actZ.getString("dep"),yTeD,onlyStartMapAndDepJ);
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
                    getBcCtH(actZ.getString("id_ONext"),actZ.getInteger("indONext"),teDaF,isC,0
                            ,sho,taskX.getId_C(),csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ
                            ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
     * 新增或者修改任务的所在日期对象状态方法
     * @param teS	当前时间戳
     * @param teDate	当前处理的任务的所在日期对象
     * @param teDurTotal	任务总时间
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void addOrUpdateTeDate(Long teS,JSONObject teDate,Long teDurTotal){
        Long aLong = teDate.getLong(teS + "");
        if (null == aLong) {
            // 添加当前处理的任务的所在日期对象状态
            teDate.put(teS+"",teDurTotal);
        } else {
            // 添加当前处理的任务的所在日期对象状态
            teDate.put(teS+"",(aLong+teDurTotal));
        }
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    private JSONObject getCt1(Task task,Task task1,Task task2,Long zon,List<Task> tasks,int i,String random
            ,String grpB,String dep,JSONObject teDate,JSONObject teDaF
            ,Integer isC,Integer ts,JSONObject sho,int csSta,String randomAll,JSONObject xbAndSbAll
            ,JSONObject actionIdO,JSONObject objTaskAll,JSONObject storageResetJ,JSONObject teDateFAllJ
            ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
//        System.out.println("进入-最开始-这里-1");
        // 创建冲突任务集合
        List<Task> conflict = new ArrayList<>();
        // 调用冲突处理核心方法
        JSONObject conflictHandle = conflictHandle(task, task1, task2, zon, tasks, i, conflict
                ,getTeS(random, grpB, dep,onlyStartMapJ,onlyStartMapAndDepJ)
                ,random,grpB,dep,teDate,isC,ts,sho,csSta,randomAll,xbAndSbAll
                ,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                xin.put(randomAll,(xin.getInteger(randomAll)+1));
                // 当前时间戳累加
                teSB += 86400L;
                // 调用获取任务综合信息方法
                Map<String, Object> jumpDay = getJumpDay(random, grpB, dep, 1, teSB,isC,task.getId_C()
                        ,xbAndSbAll,objTaskAll,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                        // 调用冲突处理核心方法
                        conflictHandle = conflictHandle(task, task1X, task2X, zon2, tasks2, i1, conflict
                                , teSB,random,grpB,dep,teDate,isC,0,sho,csSta,randomAll,xbAndSbAll,objTaskAll
                                ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                                setTasksAndZon(tasks2,grpB,dep,teSB,zon2,objTaskAll);
                            } else {
//                                System.out.println("进入-最开始-这里-1-写入镜像:");
                                // 调用写入镜像任务集合方法
                                setTasksF(tasks,grpB,dep,teSB,tasksFAllJ);
                                // 调用写入镜像任务余剩时间方法
                                setZonF(zon,grpB,dep,teSB,zonFAllJ);
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
                if (xin.getInteger(randomAll) == 510) {
                    System.out.println("进入isJ强制结束!!!");
                    // 赋值强制停止出现后的记录参数
                    isQzTz.put(randomAll,1);
                    break;
                }
            } while (true);
        }

        // 调用处理冲突核心方法
        JSONObject re2 = getBqCt(i,tasks,conflict,zon,random,dep,grpB,teDaF,isC,ts,sho,csSta,randomAll
                ,xbAndSbAll,actionIdO,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ
                ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param objTaskAll	全局任务信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    private JSONObject getCt1F(Task task,Task task2X,Task task3,Long zon,List<Task> tasks,int i,int i1
            ,List<Task> conflict,Long teSB,String random,String grpB,String dep
            ,JSONObject teDate,Integer isC,Integer ts,JSONObject sho,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject objTaskAll,JSONObject teDateFAllJ,JSONObject zonFAllJ
            ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ){
        // 调用冲突处理核心方法
        JSONObject conflictHandle = conflictHandle(task, task2X, task3, zon, tasks, i1, conflict
                ,teSB,random,grpB,dep,teDate,isC,ts,sho,csSta,randomAll,xbAndSbAll,objTaskAll
                ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                Map<String, Object> jumpDay = getJumpDay(random, grpB, dep, 1, teSB,isC,task.getId_C()
                        ,xbAndSbAll,objTaskAll,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                                , teSB2,random,grpB,dep,teDate,isC,0,sho,csSta,randomAll,xbAndSbAll
                                ,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                                setTasksAndZon(tasks,grpB,dep,teSB,zon,objTaskAll);
                            } else {
                                // 调用写入镜像任务集合方法
                                setTasksF(tasks,grpB,dep,teSB,tasksFAllJ);
                                // 调用写入镜像任务余剩时间方法
                                setZonF(zon,grpB,dep,teSB,zonFAllJ);
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
                if (xin.getInteger(randomAll) == 510) {
                    System.out.println("进入isJ强制结束!!!");
                    // 赋值强制停止出现后的记录参数
                    isQzTz.put(randomAll,1);
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
     * @param teDateFAllJ	存储任务所在日期
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private void putTeDate(String id_O,Integer index,Long teS,Integer zOk,JSONObject teDateFAllJ){
        // 判断状态不等于当前递归产品
        if (zOk != -1) {
//            System.out.println("添加记录时间-id_O:"+id_O+",index:"+index+",teS:"+teS);
            // 调用写入任务所在日期方法
            setTeDateF(id_O,index,teS,teDateFAllJ);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    public JSONObject chkInJi(Task task,Long hTeStart,String grpB,String dep,String id_O,Integer index,Integer isT,String random
            ,Integer isK,JSONObject teDate,JSONObject teDaF,Integer isC,JSONObject sho,int isYx,int csSta
//            ,boolean isD
            ,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject storageResetJ,JSONObject teDateFAllJ,JSONObject zonFAllJ
            ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ,JSONObject onlyStartMapJ
            ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
        // 创建返回结果对象
        JSONObject re = new JSONObject();
        // 跳天强制停止参数累加
        yiShu.put(randomAll,(yiShu.getInteger(randomAll)+1));
        // 调用获取任务综合信息方法
        Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,0,0L,isC,task.getId_C()
                ,xbAndSbAll,objTaskAll,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
        // 获取任务集合
        List<Task> tasks = (List<Task>) jumpDay.get("tasks");
        // 获取任务余剩时间
        long zon = (long) jumpDay.get("zon");
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer zOk = sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex().toString()).getInteger("zOk");
        if (zOk != -1) {
            // 调用获取获取统一id_O和index存储记录状态信息方法
            Integer storage = getStorage(task.getId_O(), task.getIndex(),storageResetJ);
            // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
            if (storage == 0) {
                // 调用写入存储记录状态方法
                setStorage(2, task.getId_O(), task.getIndex(),storageResetJ);
            }
        }
        // 调用时间处理方法2
        JSONObject jsonObject = chkInJi2(zon, hTeStart, tasks, grpB, dep, id_O, index, task
                , isT, random, isK, teDate, teDaF, isC, sho,isYx,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                ,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject chkInJi2(Long zon,Long hTeStart,List<Task> tasks,String grpB,String dep
            ,String id_O,Integer index,Task task,Integer isT,String random,Integer isK
            ,JSONObject teDate,JSONObject teDaF,Integer isC,JSONObject sho,int isYx,int csSta
            ,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll,JSONObject storageResetJ
            ,JSONObject teDateFAllJ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
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
                        JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate,dep,grpB
                                ,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                                    ji = ji(task, task1, task2, tasks, i, zon, 1,random,teDate,dep,grpB
                                            ,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
                                } else if (task.getTePStart() <= task2.getTePStart() && task.getPriority() >= task2.getPriority()) {
//                                    System.out.println("进入这里-1-2");
                                    // 调用计算空插时间方法并赋值
                                    ji = ji(task, task1, task2, tasks, i, zon, 2,random,teDate,dep,grpB
                                            ,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                                            JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                                    , dep, teDate, teDaF, isC, 0, sho,csSta,randomAll,xbAndSbAll
                                                    ,actionIdO,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ
                                                    ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                            addJiLJ(randomAll,"进入-冲突-最开始-这里-2",jiLJN);
                                            isL = 4;
                                            break;
                                        } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-3");
                                            addJiLJ(randomAll,"进入-冲突-最开始-这里-3",jiLJN);
                                            isL = 5;
                                            break;
                                        }
                                    } else if (task.getPriority() < task1.getPriority()){
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
//                                            System.out.println("进入-冲突-最开始-这里-1--1");
                                            isL = 33;
                                            // 调用处理时间冲突方法
                                            JSONObject ct1 = getCt1(task,task1,task2,zon,tasks,i,random,grpB,dep
                                                    ,teDate,teDaF,isC,0,sho,csSta,randomAll,xbAndSbAll,actionIdO
                                                    ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                                                    ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                            addJiLJ(randomAll,"进入-冲突-最开始-这里-2--1",jiLJN);
                                            isL = 44;
                                            break;
                                        } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-3--1");
                                            addJiLJ(randomAll,"进入-冲突-最开始-这里-3--1",jiLJN);
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
                                        addJiLJ(randomAll,"进入-冲突-中间-这里-1",jiLJN);
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isR = 2;
                                        break;
                                    } else
                                    if (task1.getTePStart() >= task.getTePStart() && task1.getTePFinish() <= task.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-2");
                                        addJiLJ(randomAll,"进入-冲突-中间-这里-2",jiLJN);
                                        isL = 4;
                                        break;
                                    } else if (task.getTePFinish() > task1.getTePStart() && task.getTePFinish() < task1.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-3");
                                        addJiLJ(randomAll,"进入-冲突-中间-这里-3",jiLJN);
                                        isL = 5;
                                        break;
                                    } else {
                                        System.out.println("进入输出ji---1:");
                                        // 调用计算空插时间方法
                                        JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate
                                                ,dep,grpB,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                                        addJiLJ(randomAll,"进入-冲突-最后面-这里-1",jiLJN);
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isR = 2;
                                        break;
                                    } else
                                    if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                        isL = 4;
//                                        System.out.println("进入-冲突-最后面-这里-2");
                                        // 调用处理时间冲突方法
                                        JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                                , dep, teDate, teDaF, isC, 0, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                                ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                                                ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                        addJiLJ(randomAll,"进入-冲突-最后面-这里-3",jiLJN);
                                        isL = 5;
                                        break;
                                    } else {
                                        System.out.println("进入输出ji---1-2:");
                                        // 调用计算空插时间方法
                                        JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random,teDate
                                                ,dep,grpB,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                                            teDate.put(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep
                                                    ,onlyStartMapJ,onlyStartMapAndDepJ)
                                                    ,zOk,teDateFAllJ);
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
                                            teDate.put(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep
                                                    ,onlyStartMapJ,onlyStartMapAndDepJ)
                                                    ,zOk,teDateFAllJ);
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
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                            , dep, teDate, teDaF, isC, 1, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                            ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                                            ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                    JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random
                                            ,teDate,dep,grpB,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
                                addJiLJ(randomAll,"进入-冲突-新最开始-这里-1--1",jiLJN);
                            } else {
//                                System.out.println("进入else-2");
                                // 调用计算空插时间方法
                                JSONObject ji = ji( task, task1, task2, tasks, i, zon,0,random
                                        ,teDate,dep,grpB,zOk,teDateFAllJ,onlyStartMapJ,onlyStartMapAndDepJ);
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
            hTeStart = getIsT(isR, isC, tasks, grpB, dep, random, zon, isK, hTeStart, task, id_O
                    , index, teDate, teDaF,sho,isPd,isYx,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                    ,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                            , dep, teDate, teDaF, isC, 0, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                            ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                                            ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                    addJiLJ(randomAll,"进入-x-冲突-最开始-这里-2",jiLJN);
                                    isL = 4;
                                    break;
                                } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3");
                                    addJiLJ(randomAll,"进入-x-冲突-最开始-这里-3",jiLJN);
                                    isL = 5;
                                    break;
                                }
                            } else if (task.getPriority() < task1.getPriority()){
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
//                                    System.out.println("进入-x-冲突-最开始-这里-1--1");
                                    isL = 33;
                                    // 调用处理时间冲突方法
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                            , dep, teDate, teDaF, isC, 0, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                            ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                                            ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                    addJiLJ(randomAll,"进入-x-冲突-最开始-这里-2--1",jiLJN);
                                    isL = 44;
                                    break;
                                } else if (task.getTePFinish() > task2.getTePStart() && task.getTePFinish() < task2.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3--1");
                                    addJiLJ(randomAll,"进入-x-冲突-最开始-这里-3--1",jiLJN);
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
                                    addJiLJ(randomAll,"进入-x-冲突-中间-这里-1",jiLJN);
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    break;
                                } else
                                if (task1.getTePStart() >= task.getTePStart() && task1.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-2");
                                    addJiLJ(randomAll,"进入-x-冲突-中间-这里-2",jiLJN);
                                    isL = 4;
                                    break;
                                } else if (task.getTePFinish() > task1.getTePStart() && task.getTePFinish() < task1.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-3");
                                    addJiLJ(randomAll,"进入-x-冲突-中间-这里-3",jiLJN);
                                    isL = 5;
                                    break;
                                }
                            } else if (task.getPriority() < task2.getPriority()) {
                                if (task.getTePStart() >= task2.getTePStart() && task.getTePStart() < task2.getTePFinish()) {
                                    isL = 3;
                                    System.out.println("进入-x-冲突-最后面-这里-1");
                                    addJiLJ(randomAll,"进入-x-冲突-最后面-这里-1",jiLJN);
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isR = 2;
                                    break;
                                } else
                                if (task2.getTePStart() >= task.getTePStart() && task2.getTePFinish() <= task.getTePFinish()) {
                                    isL = 4;
//                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    // 调用处理时间冲突方法
                                    JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                            , dep, teDate, teDaF, isC, 0, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                            ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                                            ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                                    addJiLJ(randomAll,"进入-x-冲突-最后面-这里-3",jiLJN);
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
                                JSONObject ct1 = getCt1(task, task1, task2, zon, tasks, i, random, grpB
                                        , dep, teDate, teDaF, isC, 1, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                        ,objTaskAll,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ
                                        ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                            addJiLJ(randomAll,"进入-x-冲突-新最开始-这里-1--1",jiLJN);
                        }
                    }
                }
            }
            if (isL >= 3) {
                System.out.println("-x-出现时间冲突!isL:"+isL);
            }
            // 调用处理[时间处理方法2]结束操作的方法
            hTeStart = getIsT(isR, isC, tasks, grpB, dep, random, zon, isK, hTeStart, task, id_O
                    , index, teDate, teDaF,sho,isPd,isYx,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                    ,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                    ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return long  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private long getIsT(int isR,int isC,List<Task> tasks,String grpB,String dep,String random
            ,Long zon,int isK,Long hTeStart,Task task,String id_O,int index,JSONObject teDate
            ,JSONObject teDaF,JSONObject sho,int isPd,int isYx,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll,JSONObject storageResetJ
            ,JSONObject teDateFAllJ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
        if (isR == 0) {
            // isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 0) {
                setTasksAndZon(tasks,grpB,dep,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),zon,objTaskAll);
            } else {
                // 调用写入镜像任务集合方法
                setTasksF(tasks,grpB,dep,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),tasksFAllJ);
                // 调用写入镜像任务余剩时间方法
                setZonF(zon,grpB,dep,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),zonFAllJ);
            }
            // 根据当前唯一编号获取最新的（最后一个）当前时间戳
            JSONObject rand = onlyStartMapAndDepJ.getJSONObject(random);
            // 根据部门获取最新的（最后一个）当前时间戳
            JSONObject onlyDep = rand.getJSONObject(dep);
            // 根据组别获取最新的（最后一个）当前时间戳
            Long aLong = onlyDep.getLong(grpB);
            // 根据部门添加最新的（最后一个）当前时间戳
            onlyDep.put(grpB,(aLong+86400L));
            // 根据部门添加最新的（最后一个）当前时间戳
            rand.put(dep,onlyDep);
            // 根据当前唯一编号添加最新的（最后一个）当前时间戳
            onlyStartMapAndDepJ.put(random,rand);
//            System.out.println("这里跳天调用-1:"+isK);
            // 判断跳天强制停止参数等于55
            if (yiShu.getInteger(randomAll) == 555) {
                System.out.println("进入强制停止-----1");
                // 赋值强制停止出现后的记录参数
                isQzTz.put(randomAll,1);
                return hTeStart;
            }
            // 调用时间处理方法
            JSONObject jsonObject = chkInJi(task, hTeStart, grpB, dep, id_O, index, 1, random, isK, teDate, teDaF, isC, sho,isYx,csSta
//                    ,false
                    ,randomAll,xbAndSbAll,actionIdO,objTaskAll,storageResetJ,teDateFAllJ
                    ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
            // 获取任务最初始开始时间
            hTeStart = jsonObject.getLong("hTeStart");
        } else {
            // 获取任务最初始开始时间
            hTeStart = task.getTePFinish();
//            System.out.println("最后返回:"+hTeStart+"-isPd:"+isPd);
            // isC – isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
            if (isC == 0) {
                setTasksAndZon(tasks,grpB,dep,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),zon,objTaskAll);
            } else {
                // isPd – 存储问题状态参数: isPd = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                if (isPd != 2) {
                    // 调用写入镜像任务集合方法
                    setTasksF(tasks,grpB,dep,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),tasksFAllJ);
                    // 调用写入镜像任务余剩时间方法
                    setZonF(zon,grpB,dep,getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),zonFAllJ);
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param objTaskAll	全局任务信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject conflictHandle(Task task,Task task1,Task task2,Long zon,List<Task> tasks,int i
            ,List<Task> conflict,long teSB,String random,String grpB,String dep
            ,JSONObject teDate,Integer isC,Integer ts,JSONObject sho,int csSta
            ,String randomAll,JSONObject xbAndSbAll,JSONObject objTaskAll,JSONObject teDateFAllJ
            ,JSONObject zonFAllJ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ
            ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ){
//        System.out.println("进入conflictHandle...");
//        System.out.println(JSON.toJSONString(task));
//        System.out.println(JSON.toJSONString(task1));
//        System.out.println(JSON.toJSONString(task2));
        // 创建创建返回结果对象
        JSONObject re = new JSONObject();
        // jie：用于外部判断任务是否被处理完参数，jie == 0 没有被处理完、jie == 2 已经被处理完了
        re.put("jie",0);
        // 获取存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
//        int isD2 = onlyIsDs.getInteger(random);
        int isD2 = onlyIsDsJ.getInteger(random);
        // 存储任务是否被处理完状态参数：isJ == 0 任务没有被处理完、isJ == 1 任务已经被处理完了
        int isJ = 0;
        // 获取余剩时间（对比任务2的开始时间-对比任务1的结束时间）
        long s = task2.getTePStart() - task1.getTePFinish();
        long teDurTotal;
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
                        teDurTotal = cha;
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
                        teDurTotal = cha;
                        // 更新当前任务的开始时间
                        task.setTePStart(task2.getTePStart());
                    }
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,teDurTotal);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                    .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
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
                    teDurTotal = task2.getTeDurTotal();
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
                    teDurTotal = task.getTeDurTotal();
                }
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,teDurTotal);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(),task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                        ,sho.getJSONObject(task.getId_O()).getJSONObject(task.getIndex()
                                .toString()).getInteger("zOk"),teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,task.getTeDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                ,zOk,teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
                        // 更新当前任务的总时间
                        task.setTeDurTotal(cha2);
                        // 更新当前任务的开始时间
                        task.setTePStart((task.getTePStart()+s));
                        // 更新任务的结束时间
                        task.setTePFinish((task.getTePStart()+s)+task.getTeDurTotal());
//                        System.out.println("进入这里++=1");
                        isD2 = 1;
                        // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                        onlyIsDsJ.put(random,1);
                        // 任务余剩时间累减
                        zon -= s;
                    }
                } else {
                    // 判断时间差大于等于0
                    if (cha >= 0) {
                        // 任务集合按照指定下标（i（任务下标）+1）添加任务信息
                        tasks.add(i+1,Obj.getTaskX(task1.getTePFinish(),(task1.getTePFinish()+task.getTeDurTotal())
                                ,task.getTeDurTotal(),task));
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                ,teDate,task.getTeDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,task.getTeDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                    .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,task.getTeDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                        .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,task.getTeDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                    .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
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
                            tasks.set(i+1,Obj.getTaskX(task.getTePStart(),(task.getTePStart()+task.getTeDurTotal())
                                    ,task.getTeDurTotal(),task));
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                    ,teDate,task.getTeDurTotal());
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
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
                            teDurTotal = s;
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
                            teDurTotal = task.getTeDurTotal();
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
                            teDurTotal = task.getTeDurTotal();
                        } else {
//                            System.out.println("小于等于:");
                            // 获取余剩总时间（当前任务的总时间-对比任务1的总时间）
                            s = task.getTeDurTotal() - task1.getTeDurTotal();
                            // 任务余剩时间累减
                            zon -= task1.getTeDurTotal();
                            // 更新任务集合指定下标i（任务下标）的任务信息为当前任务信息
                            tasks.set(i,Obj.getTaskX(task1.getTePStart(),task1.getTePFinish(),task1.getTeDurTotal(),task));
                            teDurTotal = task1.getTeDurTotal();
                            // 更新当前任务的总时间
                            task.setTeDurTotal(s);
                        }
                    }
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,teDurTotal);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                            ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                    .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                    teDurTotal = cha;
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
                                    teDurTotal = cha;
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
                                    teDurTotal = cha;
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
                                    teDurTotal = task2.getTeDurTotal();
//                                    System.out.println("进入这里--=23");
                                }
                                // 更新当前任务总时间
                                task.setTeDurTotal(s);
                            }
                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,teDurTotal);
                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                    ,sho.getJSONObject(task.getId_O()).getJSONObject(task
                                            .getIndex().toString()).getInteger("zOk"),teDateFAllJ);
                            iZ = 2;
                            isD2 = 1;
                            // 添加存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                            onlyIsDsJ.put(random,1);
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
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                                ,teDate,task.getTeDurTotal());
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                                ,teDate,task.getTeDurTotal());
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                        addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                                        putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                            , random, grpB, dep, teDate,isC,1,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                                            ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                                    ,teDate,task.getTeDurTotal());
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                    ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                    ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                                , random, grpB, dep, teDate,isC,1,sho,csSta,randomAll,xbAndSbAll
                                                ,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                                                ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                                            , random, grpB, dep, teDate,isC,0,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                                            ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                                            , random, grpB, dep, teDate,isC,0,sho,csSta,randomAll,xbAndSbAll
                                            ,objTaskAll,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ
                                            ,onlyStartMapAndDepJ,onlyIsDsJ);
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
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ)
                                                    ,teDate,task.getTeDurTotal());
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                    ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
                                            // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                                            addOrUpdateTeDate(getTeS(random,grpB,dep,onlyStartMapJ,onlyStartMapAndDepJ),teDate,s);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getIndex(),getTeS(random,grpB,dep,onlyStartMapJ
                                                    ,onlyStartMapAndDepJ),zOk,teDateFAllJ);
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
     * @ver 1.0.0
     * @date 2022/6/9
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
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param storageResetJ	统一id_O和index存储记录状态信息
     * @param teDateFAllJ	存储任务所在日期
     * @param zonFAllJ	全局镜像任务余剩总时间信息
     * @param tasksFAllJ	全局镜像任务列表信息
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyIsDsJ	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param jiLJN	定义存储进入未操作到的地方记录
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject konChaConflict(Task taskX,Task task1X,Task task2X,List<Task> tasks,int i1
            ,int conflictInd,long zon,List<Task> conflict,JSONObject teDate,String random,String dep,String grpB
            ,JSONArray objAction,JSONObject actZ,Long yTeD,JSONObject teDaF,Integer isC,JSONObject sho
            ,int csSta,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject storageResetJ,JSONObject teDateFAllJ,JSONObject zonFAllJ
            ,Map<String,Map<String,Map<Long,List<Task>>>> tasksFAllJ,JSONObject onlyStartMapJ
            ,JSONObject onlyStartMapAndDepJ,JSONObject onlyIsDsJ,JSONObject jiLJN){
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(yTeD,teDate, taskX.getTeDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                            ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk")
                            ,teDateFAllJ);
                }
//                System.out.println("进入前赋值-conflictInd-2-1:"+conflictInd);
                // 调用处理冲突核心方法2
                JSONObject bqCt2 = getBqCt2(conflictInd, tasks, conflict, taskX, zon, teDate, random
                        , objAction, actZ, i1,dep,grpB,yTeD,teDaF,isC,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                        ,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(yTeD,teDate, taskX.getTeDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(yTeD,teDate, taskX.getTeDurTotal());
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                                ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex()
                                        .toString()).getInteger("zOk"),teDateFAllJ);
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(yTeD,teDate,s);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
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
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(yTeD,teDate,taskX.getTeDurTotal());
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
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
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(yTeD,teDate,taskX.getTeDurTotal());
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                        ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk")
                        ,teDateFAllJ);
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
                        // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                        addOrUpdateTeDate(yTeD,teDate,s);
                        // 调用判断产品状态再调用写入任务所在日期方法的方法
                        putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(yTeD,teDate, taskX.getTeDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
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
                tasks.set(i1,Obj.getTaskX(task1X.getTePStart(),(task1X.getTePStart()+taskX.getTeDurTotal())
                        ,taskX.getTeDurTotal(),taskX));
                // 冲突任务集合添加对比任务1信息
                conflict.add(Obj.getTaskX(task1X.getTePStart(),task1X.getTePFinish(),task1X.getTeDurTotal(),task1X));
                // 调用添加或更新产品状态方法
                addSho(sho, taskX.getId_O(),taskX.getIndex().toString(),task1X.getId_O(), task1X.getIndex().toString(),0);
                // 冲突任务下标累加
                conflictInd++;
                // 存储控制冲突下标是否累加，== 0 可以累加、== 1 不能累加
                isJin = 1;
//                System.out.println("进入这里++=36");
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(yTeD,teDate, taskX.getTeDurTotal());
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                        ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk")
                        ,teDateFAllJ);
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
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(yTeD,teDate,s);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
            } else {
                // 存储时间差 - 判断对比任务2优先级等于系统，如果等于就赋值为0，否则xin等于（对比任务2的开始时间-对比任务1的结束时间）
                long xinKc = task2X.getPriority()==-1?0:task2X.getTePStart() - task1X.getTePFinish();
                // 获取开始时间（对比任务1+存储时间差）
                long kai = task1X.getTePFinish() + xinKc;
                // 开始时间累加当前任务总时间
                kai += taskX.getTeDurTotal();
                // 判断开始时间大于对比任务2的结束时间并且，当前任务优先级小于对比任务2的优先级
                if (kai > task2X.getTePFinish() && taskX.getPriority() < task2X.getPriority()) {
                    // 任务余剩时间累加
                    zon += task2X.getTeDurTotal();
                    // 判断存储时间差大于0
                    if (xinKc > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        taskX.setTeDurTotal(taskX.getTeDurTotal()-xinKc);
                    }
                    // 任务余剩时间累减
                    zon -= taskX.getTeDurTotal();
                    long teDurTotal = taskX.getTeDurTotal();
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
                            , objAction, actZ, i1,dep,grpB,yTeD,teDaF,isC,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,storageResetJ,teDateFAllJ,zonFAllJ,tasksFAllJ,onlyStartMapJ,onlyStartMapAndDepJ,onlyIsDsJ,jiLJN);
                    // 更新冲突集合指定的冲突下标的任务信息
                    conflict.set(conflictInd,Obj.getTaskX(taskX.getTePStart(),taskX.getTePFinish(),taskX.getTeDurTotal(),taskX));
                    // 获取冲突任务下标
                    conflictInd = bqCt2.getInteger("conflictInd");
                    // 获取任务余剩时间
                    zon = bqCt2.getLong("zon");
                    // 获取任务所在时间键的第一个键的值（时间戳）
                    yTeD = bqCt2.getLong("yTeD");
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(yTeD,teDate,teDurTotal);
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                            ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk")
                            ,teDateFAllJ);
                } else if (kai > task2X.getTePStart() && kai <= task2X.getTePFinish() && taskX.getPriority() < task2X.getPriority()) {
                    // 任务余剩时间累加
                    zon += task2X.getTeDurTotal();
                    // 判断存储时间差大于0
                    if (xinKc > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        taskX.setTeDurTotal(taskX.getTeDurTotal()-xinKc);
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(yTeD,teDate,taskX.getTeDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD
                            ,sho.getJSONObject(taskX.getId_O()).getJSONObject(taskX.getIndex().toString()).getInteger("zOk")
                            ,teDateFAllJ);
                } else if (kai <= task2X.getTePStart()) {
//                    System.out.println("进入这个奇怪的地方");
                    // 判断存储时间差大于0
                    if (xinKc > 0) {
                        // 设置当前任务总时间（当前任务总时间-存储时间差）
                        taskX.setTeDurTotal(taskX.getTeDurTotal()-xinKc);
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
                    // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                    addOrUpdateTeDate(yTeD,teDate,taskX.getTeDurTotal());
                    // 调用判断产品状态再调用写入任务所在日期方法的方法
                    putTeDate(taskX.getId_O(), taskX.getIndex(),yTeD,zOk,teDateFAllJ);
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
     * @param teDateFAllJ 存储任务所在日期
     * @param onlyStartMapJ	存储当前唯一编号的第一个当前时间戳
     * @param onlyStartMapAndDepJ	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject ji(Task task, Task task1, Task task2, List<Task> tasks, Integer i, Long zon
            , Integer isKC,String random,JSONObject teDate,String dep,String grpB,int zOk
            ,JSONObject teDateFAllJ,JSONObject onlyStartMapJ,JSONObject onlyStartMapAndDepJ){
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
                Long teS = getTeS(random, grpB, dep,onlyStartMapJ,onlyStartMapAndDepJ);
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(teS,teDate,staJ);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(), task.getIndex(),teS,zOk,teDateFAllJ);
            } else {
                // 添加控制是否跳天参数
                re.put("isR", 1);
                // 获取时间差（余剩时间-当前任务时间）
                long cha = s - task.getTeDurTotal();
                long teDurTotal;
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
                    // 任务集合指定添加任务下标+1位置添加任务信息
                    tasks.add(i+1,Obj.getTaskX(task.getTePStart(),task.getTePFinish(),task.getTeDurTotal(),task));
                    teDurTotal = task.getTeDurTotal();
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
                    teDurTotal = s;
                    // 添加控制是否结束循环参数
                    re.put("isP",4);
                    // 任务余剩时间累减
                    zon -= s;
                }
                // 调用获取当前时间戳方法
                Long teS = getTeS(random, grpB, dep,onlyStartMapJ,onlyStartMapAndDepJ);
                // 调用新增或者修改任务的所在日期对象状态方法并且写入当天使用总时间
                addOrUpdateTeDate(teS,teDate,teDurTotal);
                // 调用判断产品状态再调用写入任务所在日期方法的方法
                putTeDate(task.getId_O(), task.getIndex(),teS,zOk,teDateFAllJ);
            }
        } else {
            // 添加控制是否结束循环参数
            re.put("isP",3);
        }
        re.put("zon",zon);
        return re;
    }

    /**
     * 添加记录进入哪个未操作到的地方
     * @param randomAll	全局唯一编号
     * @param text	位置信息
     * @param jiLJN	存储记录字典
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 1:55
     */
    private void addJiLJ(String randomAll,String text,JSONObject jiLJN){
        // 根据全局唯一编号获取操作存储字典
        JSONArray jsonArray = jiLJN.getJSONArray(randomAll);
        // 添加位置信息
        jsonArray.add(text);
        // 更新信息
        jiLJN.put(randomAll,jsonArray);
    }
}
