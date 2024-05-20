package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.TimeZjService;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.assetCard.AssetInfo;
import com.cresign.tools.pojo.po.chkin.Task;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * @Description 作者很懒什么也没写
 * @ClassName TimeServiceImpl
 * @author tang
 * @Date 2021/12/20 11:10
 * @ver 1.0.0
 */
@Service
public class TimeZjServiceImplX extends TimeZj implements TimeZjService {

    /**
     * 是否测试: = true 说明是测试、 = false 说明不是测试
     */
    public static boolean isTest = true;
    /**
     * 是否添加测试订单与任务: = true 说明是、 = false 说明不是
     */
    public static boolean isTestAddOrder = true;

    @Resource
    private RetResult retResult;

    /**
     * 移动资产内aArrange卡片
     * @param id_C	当前公司编号
     * @param moveId_A	移动的资产编号
     * @param coverMoveId_A	被移动的资产编号
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2022/10/30
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse moveAArrange(String id_C, String moveId_A, String coverMoveId_A) {
        // 根据asset编号获取asset的时间处理卡片信息
//        Asset asset = coupaUtil.getAssetById(coverMoveId_A, Arrays.asList(timeCard,"info"));
        Asset asset = qt.getMDContent(coverMoveId_A,qt.strList(timeCard,"info"), Asset.class);
        if (null == asset) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_NULL.getCode(), "被移动的资产为空");
        }
        AssetInfo info = asset.getInfo();
        if (null == info) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_INFO_NULL.getCode(), "资产内Info为空");
        }
        if (!info.getId_C().equals(id_C)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ID_C_NO_MATCHING.getCode(), "公司编号不匹配");
        }
//        JSONObject aArrange = asset.getAArrange2();
        JSONObject aArrange = getAArrangeNew(asset);
        if (null == aArrange) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_ARRANGE_NULL.getCode(), "被移动的资产Arrange为空");
        }
        JSONObject objTask = aArrange.getJSONObject("objTask");
        if (null == objTask) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_ARRANGE_OBJ_TASK_NULL.getCode(), "被移动的资产Arrange内objTask为空");
        }
        // 根据asset编号获取asset的时间处理卡片信息
//        Asset assetMove = coupaUtil.getAssetById(moveId_A, Arrays.asList(timeCard,"info"));
        Asset assetMove = qt.getMDContent(moveId_A,qt.strList(timeCard,"info"), Asset.class);
        if (null == assetMove) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_NULL.getCode(), "移动的资产为空");
        }
        AssetInfo infoNew = assetMove.getInfo();
        if (null == infoNew) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_INFO_NULL.getCode(), "移动的资产内Info为空");
        }
        if (!infoNew.getId_C().equals(id_C)) {
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ID_C_NO_MATCHING.getCode(), "移动的公司编号不匹配");
        }
        // 获取时间处理卡片信息
//        JSONObject aArrangeMove = assetMove.getAArrange2();
        JSONObject aArrangeMove = getAArrangeNew(assetMove);
        if (null == aArrangeMove) {
            aArrangeMove = new JSONObject();
        } else {
            JSONObject objTaskNew = aArrangeMove.getJSONObject("objTask");
            if (null != objTaskNew) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_ASSET_EXISTS_NULL.getCode(), "资产内已存在");
            }
        }
        // 添加信息
        aArrangeMove.put("objTask",objTask);
//        // 创建请求参数存储字典
//        JSONObject mapKey = new JSONObject();
//        // 添加请求参数
//        mapKey.put(timeCard,aArrangeMove);
//        coupaUtil.updateAssetByKeyAndListKeyVal("id",moveId_A,mapKey);
        qt.setMDContent(moveId_A,qt.setJson(timeCard,aArrangeMove), Asset.class);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "移动AArrange成功!");
    }

    /**
     * 获取物料预计开始时间
     * @param id_O	订单编号
     * @param id_C	公司编号
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/17
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse getEstimateStartTime(String id_O, String id_C, Long teStart) {
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O,"casItemx", Order.class);
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getCasItemx()
                || null == salesOrderData.getCasItemx().getJSONObject("java")
                || null == salesOrderData.getCasItemx().getJSONObject("java").getJSONObject("oDateObj")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 获取递归订单列表
        JSONObject oDateObj = salesOrderData.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        mergeDelSumMaxTimeByODateObj(id_O, oDateObj);
//        System.out.println(jsonObject);
//        long maxTeDurTotal = 0;
//        System.out.println("最后输出:"+(teStart+maxTeDurTotal)+" - "+maxTeDurTotal);
//        // (数据类型)(最小值+Math.random()*(最大值-最小值+1))
//        //   (int)  ( 1 + Math.random()* (  6 - 1 + 1))
//        int i = (int) (1 + Math.random() * (6 - 1 + 1));
//        System.out.println("i:"+i+" - maxTeDurTotal:"+maxTeDurTotal);
//        maxTeDurTotal += i * 86400L;
//        System.out.println("maxTeDurTotal:"+maxTeDurTotal);

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    /**
     * 时间处理方法
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9 1:26
     */
    public ApiResponse getAtFirst(String id_O, Long teStart, String id_C, Integer wn0TPrior){
        if (isTest)
            // 调用根据公司编号清空所有任务信息方法
            setTaskAndZonKai(id_C);
        System.out.println();
//        atFirst(wn0TPrior,teStart,id_O,id_C);
        atFirstODateObj(wn0TPrior,teStart,id_O,id_C);
        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
    }

    @Override
    public ApiResponse setAtFirst(String id_O, Long teStart, String id_C, Integer wn0TPrior
            , int dateIndex, String layer, String id_PF) {
        if (isTest)
            // 调用根据公司编号清空所有任务信息方法
            setTaskAndZonKai(id_C);
        System.out.println();
        atFirstODateObjSet(wn0TPrior,teStart,id_O,id_C,dateIndex,layer,id_PF);
        return retResult.ok(CodeEnum.OK.getCode(), "set时间处理成功!");
    }
    /**
     * 时间处理核心方法，指定dateIndex
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @author tang
     * @ver 1.0.0
     */
    public void atFirstODateObjSet(int wn0TPrior,long teStart,String id_O,String id_C
            , int dateIndexMain, String layerMain, String id_PFMain){
        TimeZj.isZ = 6;
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
                || null == salesOrderData.getCasItemx() || null == salesOrderData.getInfo()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject casItemx = salesOrderData.getCasItemx();
        // 获取递归存储的时间处理信息
        JSONObject oDateObj = casItemx.getJSONObject("java").getJSONObject("oDateObj");
        // 存储部门对应组别的职位总人数
        JSONObject grpUNumAll = new JSONObject();
        oDateObj = mergeTaskByPriorODateObj(oDateObj);

//        // 抛出操作成功异常
//        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
        System.out.println("开始oDateObj:");
        System.out.println(JSON.toJSONString(oDateObj));
        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
        List<Integer> layerList = new ArrayList<>();
        List<Integer> layerListThis = new ArrayList<>();
        for (String layer : oDateObj.keySet()) {
            int layerInt = Integer.parseInt(layer);
            int layerMainInt = Integer.parseInt(layerMain);
            layerList.add(Integer.parseInt(layer));
            if (layerInt >= layerMainInt) {
                layerListThis.add(Integer.parseInt(layer));
                JSONObject layerInfo = oDateObj.getJSONObject(layer);
                for (String id_PF : layerInfo.keySet()) {
                    JSONObject pfInfo = layerInfo.getJSONObject(id_PF);
                    pfInfo.put("arrPStart",new JSONObject());
                    layerInfo.put(id_PF,pfInfo);
                    oDateObj.put(layer,layerInfo);
                }
            }
        }
        if (layerListThis.size() > 0) {
            layerList.sort(Comparator.reverseOrder());
            layerListThis.sort(Comparator.reverseOrder());
            boolean isFirst = layerList.get(0).equals(layerListThis.get(0));
            dgTimeSetFirst(id_O,layerListThis.get(0),oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior
                    ,isFirst,teStart,dateIndexMain,id_PFMain);
            qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
        }
    }
    private void dgTimeSetFirst(String id_OPHighest,int layer,JSONObject oDateObj,Order salesOrderData
            ,String id_C,JSONObject grpUNumAll,int wn0TPrior,boolean isFirst,long teStart
            , int dateIndexMain, String id_PFMain){
        if (layer > 0) {
            System.out.println("layer:"+layer);
            JSONObject prodInfo = oDateObj.getJSONObject(layer + "");
            boolean isNext = true;
            for (String prodId : prodInfo.keySet()) {
                JSONObject partInfo = prodInfo.getJSONObject(prodId);
                long maxStaTime = teStart;
                if (!prodId.equals(id_PFMain)) {
                    Long tePFinish = partInfo.getLong("tePFinish");
                    if (null == tePFinish || tePFinish == 0) {
                        isNext = false;
                    }
                    break;
                }
                if (!isFirst) {
                    System.out.println("partInfo:");
                    JSONObject arrPStartThis = partInfo.getJSONObject("arrPStart");
                    System.out.println(JSON.toJSONString(partInfo));
                    if (arrPStartThis.size() > 0) {
                        maxStaTime = 0;
                        teStart = 0;
                        long maxStaTimeInside;
                        for (String pfId : arrPStartThis.keySet()) {
                            maxStaTimeInside = arrPStartThis.getJSONObject(pfId).getLong("lastTePFin");
                            if (maxStaTimeInside > maxStaTime) {
                                maxStaTime = maxStaTimeInside;
                                teStart = arrPStartThis.getJSONObject(pfId).getLong("time");
                            }
                        }
//                        maxStaTime = arrPStartThis.getJSONObject(0).getLong("lastTePFin");
//                        teStart = arrPStartThis.getJSONObject(0).getLong("time");
//                        if (arrPStartThis.size() > 0) {
//                            for (int i = 1; i < arrPStartThis.size(); i++) {
//                                Long time = arrPStartThis.getJSONObject(i).getLong("lastTePFin");
//                                if (time > maxStaTime) {
//                                    maxStaTime = time;
//                                    teStart = arrPStartThis.getJSONObject(i).getLong("time");
//                                }
//                            }
//                        }
                    }
                }

                partInfo.put("tePStart",maxStaTime);
                // 定义，存储进入未操作到的地方记录
                JSONObject recordNoOperation = new JSONObject();
                // 存储任务所在日期
                JSONObject storageTaskWhereTime = new JSONObject();
                // 镜像任务存储
                Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
                // 镜像总时间存储
                JSONObject allImageTotalTime = new JSONObject();
                // 全部任务存储
                JSONObject objTaskAll = new JSONObject();
                JSONObject grpBGroupIdOJ = new JSONObject();
                // 存储casItemx内订单列表的订单oDates数据
                JSONObject actionIdO = new JSONObject();
                // 存储部门对应组别的上班和下班时间
                JSONObject xbAndSbAll = new JSONObject();
                // 统一id_O和index存储记录状态信息
                JSONObject recordId_OIndexState = new JSONObject();
                // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                JSONObject onlyRefState = new JSONObject();
                // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                JSONObject newestLastCurrentTimestamp = new JSONObject();
                // 存储当前唯一编号的第一个当前时间戳
                JSONObject onlyFirstTimeStamp = new JSONObject();
                // 获取唯一下标
                String random = new ObjectId().toString();
                // 获取全局唯一下标
                String randomAll = new ObjectId().toString();

                // 设置问题记录的初始值
                yiShu.put(randomAll,0);
                leiW.put(randomAll,0);
                xin.put(randomAll,0);
                isQzTz.put(randomAll,0);
                recordNoOperation.put(randomAll,new JSONArray());

                // 设置存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.put(random,teStart);
                // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                onlyRefState.put(random,0);
                // 存储最初开始时间
                long initialStartTime = 0L;
                // 存储最后结束时间
                long lastEndTime = 0L;
                // 获取递归订单列表
                JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
                // 存储递归订单列表的订单编号集合
                JSONArray objOrderList = new JSONArray();
                for (int i = 0; i < objOrder.size() - 1; i++) {
                    // 添加订单编号
                    objOrderList.add(objOrder.getJSONObject(i).getString("id_O"));
                }
                JSONObject depAllTime = new JSONObject();
                System.out.println("--- 分割 ---");
                // 遍历订单列表
                Map<String,Asset> assetMap = new HashMap<>();
                // 当前处理信息
                JSONObject thisInfo = new JSONObject();
                setThisInfoLayer(thisInfo,layer);
                setThisInfoLayerProdId(thisInfo,prodId);
//                System.out.println("xbAndSbAll:");
//                System.out.println(JSON.toJSONString(xbAndSbAll));

                OrderInfo info = salesOrderData.getInfo();
                // 用于存储时间冲突的副本
                JSONObject timeConflictCopy = new JSONObject();
                // 用于存储判断镜像是否是第一个被冲突的产品
                JSONObject sho = new JSONObject();
                // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
                boolean canOnlyEnterOnce = true;
                // 定义用来存储最大结束时间
                long maxLastTime = 0;

                // 用于存储每一个时间任务的结束时间
                JSONArray teFinList = new JSONArray();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                JSONObject serialOneFatherLastTime = new JSONObject();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
                JSONObject serialOneFatherStartTime = new JSONObject();
                // 清理状态
                JSONObject clearStatus = new JSONObject();

                setThisInfoRef(thisInfo,"time");
                // 镜像任务所在时间
                JSONObject allImageTeDate = new JSONObject();
                JSONArray oDates = partInfo.getJSONArray("oDates");
                for (int i = 0; i < oDates.size(); i++) {
                    JSONObject oDate = oDates.getJSONObject(i);
                    getGrpB(oDate.getString("grpB"),oDate.getString("dep"), assetMap,id_C
                            ,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,oDate.getString("id_O"),teStart);
                }
                setOrderFatherId(salesOrderData.getId(),thisInfo,actionIdO,objOrder,oDateObj);

                boolean isOne = false;
                setThisInfoFinalPartDateIndex(thisInfo,(oDates.size()-1));
                setThisInfoFinalPartDate(thisInfo,oDates.getJSONObject((oDates.size()-1)).getString("id_O")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("layer")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("id_PF"),id_OPHighest);
                setThisInfoIsConflict(thisInfo,false);
                // 遍历时间处理信息集合
                for (int i = dateIndexMain; i < oDates.size(); i++) {
                    setThisInfoIsFinalPart(thisInfo, i >= oDates.size() - 1);
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    // 获取订单编号
                    String id_OInside = oDate.getString("id_O");
                    // 获取订单下标
                    int indexInside = oDate.getInteger("index");
                    // 获取时间处理的序号
                    Integer priorItem = oDate.getInteger("priorItem");
                    // 获取时间处理的父零件编号
                    String id_PF = oDate.getString("id_PF");
                    // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
                    Integer csSta = oDate.getInteger("csSta");
                    // 获取时间处理的判断是否是空时间信息
                    Boolean empty = oDate.getBoolean("empty");
                    // 判断当前时间处理为空时间信息
                    if (empty) {
                        // 获取时间处理的链接下标
                        Integer linkInd = oDate.getInteger("linkInd");
                        // 根据链接下标获取指定的结束时间
                        Long indexEndTime = teFinList.getLong(linkInd);
                        // 判断父id的预计开始时间为空，并且序号为第一个
                        if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
                            serialOneFatherStartTime.put(id_PF,indexEndTime);
                        }
                        // 根据父零件编号获取序号信息
                        JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                        // 判断序号信息为空
                        if (null == fatherSerialInfo) {
                            // 创建序号信息
                            fatherSerialInfo = new JSONObject();
                            // 添加序号的结束时间，默认为0
                            fatherSerialInfo.put(priorItem.toString(),0);
                        }
                        // 获取序号结束时间
                        Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
                        // 添加链接结束时间到当前空时间处理结束时间列表内
                        teFinList.add(indexEndTime);
                        // 判断链接结束时间大于当前结束时间
                        if (indexEndTime > serialEndTime) {
                            // 修改当前结束时间为链接结束时间
                            fatherSerialInfo.put(priorItem.toString(),indexEndTime);
                            // 根据父零件编号添加序号信息
                            serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
                        }
                        continue;
                    }
                    // 获取当前唯一ID存储时间处理的最初开始时间
                    Long hTeStart = initialStartTime;
                    // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
                    JSONObject firstConflictId_O = new JSONObject();
                    JSONObject firstConflictIndex = new JSONObject();
                    // 设置为-1代表的是递归的零件
                    firstConflictIndex.put("prodState",-1);
                    firstConflictIndex.put("z","-1");
                    firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
                    sho.put(oDate.getString("id_O"),firstConflictId_O);
                    // 获取时间处理的组别
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 获取时间处理的零件产品编号
                    String id_P = oDate.getString("id_P");
                    // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
                    Integer kaiJie = oDate.getInteger("kaiJie");
                    // 获取时间处理的实际准备时间
                    Long wntPrep = oDate.getLong("wntPrep");
                    Long wntDurTotal = oDate.getLong("wntDurTotal");
                    long initialTeStart;
                    // 判断当前唯一ID存储时间处理的最初开始时间为0
                    if (hTeStart == 0) {
                        // 调用获取当前时间戳方法设置开始时间
                        initialTeStart = getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                    } else {
                        // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
                        if (csSta == 1 && kaiJie == 1) {
                            // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
                            hTeStart = lastEndTime;
                        }
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-1:"+hTeStart);
                    }
                    boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);

                    // 序号是不为1层级
                    // 判断执行方法为true
                    if (isExecutionMethod) {
                        // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                        JSONObject serialOneEndTime;
                        System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
                        System.out.println(JSON.toJSONString(serialOneFatherLastTime));
                        // 获取判断自己的id是否等于已存在的父id
                        boolean b = serialOneFatherLastTime.containsKey(id_P);
                        // 判断自己的id是已存在的父id
                        if (b) {
                            // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
                            // 转换键信息
                            List<String> list = new ArrayList<>(serialOneEndTime.keySet());
                            // 获取最后一个时间信息
                            String s = list.get(list.size() - 1);
                            // 赋值为最后一个时间信息
                            hTeStart = serialOneEndTime.getLong(s);
                        } else {
                            // 根据父id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
                            if (null != serialOneEndTime) {
                                // 获取上一个序号的时间信息并赋值
                                hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
                            }
                        }
                        // 设置开始时间
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-3:"+hTeStart);
                    }

                    // 获取任务的最初开始时间备份
                    long teStartBackups = initialTeStart;
                    if (!isFirst && !isOne) {
                        teStartBackups = maxStaTime;
                    }
                    // 获取最初结束时间
                    Long teFin = (teStartBackups+(wntDurTotal+wntPrep));
                    // 获取任务信息，并且转换为任务类
                    Task task = new Task(oDate.getInteger("priority"),id_OInside,indexInside,wntPrep
                            ,wntDurTotal+wntPrep,id_C,i,oDate.getJSONObject("wrdN"));
                    // 设置最初任务信息的时间信息
                    task.setTePStart(teStartBackups);
                    task.setTePFinish(teFin);
                    task.setTeCsStart(teStartBackups);
                    task.setTeCsSonOneStart(0L);
                    task.setWrdNO(info.getWrdN());
                    task.setRefOP(salesOrderData.getId());
                    task.setWn2qtyneed(oDate.getDouble("wn2qtyneed"));
                    task.setId_PF(getThisInfoLayerProdId(thisInfo));
                    task.setLayer(getThisInfoLayer(thisInfo));
                    // 判断优先级不等于-1
                    if (wn0TPrior != -1) {
                        // 设置优先级为传参的优先级
                        task.setPriority(wn0TPrior);
                    }
                    // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
                    if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
                            && kaiJie != 5 && kaiJie != 3
                    ) {
                        // 根据父id添加开始时间
                        serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
                    } else if (kaiJie == 3 || kaiJie == 5) {
                        // 添加子最初开始时间
                        task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
                    }

                    // 创建当前处理的任务的所在日期对象
                    JSONObject teDate = new JSONObject();
                    System.out.println();
                    System.out.println("++ taskTe: ++ "+dep+" - "+grpB);
                    System.out.println();
                    System.out.println(JSON.toJSONString(task));
                    System.out.println(JSON.toJSONString(objTaskAll));
                    System.out.println(JSON.toJSONString(allImageTasks));

                    JSONObject conflictInfo = getThisInfoConflictInfo(thisInfo);
                    if (null == conflictInfo) {
                        conflictInfo = new JSONObject();
                    }
                    JSONObject thisConfInfo = new JSONObject();
                    thisConfInfo.put("dep",dep);
                    thisConfInfo.put("grpB",grpB);
                    thisConfInfo.put("status",0);
                    conflictInfo.put(task.getDateIndex()+"",thisConfInfo);
                    setThisInfoConflictInfo(thisInfo,conflictInfo);
                    int isCanOnly = 1;
                    if (isOne) {
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject upConfInfo = thisInfoConflictInfo.getJSONObject((task.getDateIndex() - 1) + "");
                        for (int upI = (task.getDateIndex()-1); upI >= 0; upI--) {
                            upConfInfo = thisInfoConflictInfo.getJSONObject((upI) + "");
                            if (null != upConfInfo) {
                                break;
                            }
                        }
                        if (null != upConfInfo) {
                            Integer status = upConfInfo.getInteger("status");
                            if (status == 1) {
                                String upDep = upConfInfo.getString("dep");
                                String upGrpB = upConfInfo.getString("grpB");
                                if (!upDep.equals(dep) || !upGrpB.equals(grpB)) {
                                    System.out.println("--+ 进入前置修改全局任务信息 +-- :");
                                    updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
                                    System.out.println("修改后:");
                                    System.out.println(JSON.toJSONString(objTaskAll));
                                    System.out.println(JSON.toJSONString(allImageTasks));
                                }
                            }
                        }
                    } else {
                        isOne = true;
                        if (!isFirst) {
                            isCanOnly = 0;
                            onlyRefState.put(random,1);
                            hTeStart = maxStaTime;
                        }
                    }
                    // 调用时间处理方法
                    JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
                            ,0,random,isCanOnly,teDate,timeConflictCopy,0
                            ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                            ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
                    // 更新任务最初始开始时间
                    hTeStart = timeHandleInfo.getLong("hTeStart");
                    System.out.println("最外层-3-2:"+hTeStart);
                    System.out.println(JSON.toJSONString(timeHandleInfo));
                    System.out.println(JSON.toJSONString(actionIdO));
                    System.out.println(JSON.toJSONString(oDate));
                    // 添加结束时间
                    teFinList.add(hTeStart);

                    System.out.println("id_OInside:"+id_OInside);
                    String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
                    JSONObject actByOPInfo = actionIdO.getJSONObject(id_OP);
                    JSONObject layerInfo = actByOPInfo.getJSONObject(oDate.getString("layer"));
                    JSONObject oPDateInfo = layerInfo.getJSONObject(oDate.getString("id_PF"));
                    JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
                    JSONObject thisODate = oDatesNew.getJSONObject(i);
                    thisODate.put("teDate",teDate);
                    oDatesNew.set(i,thisODate);
                    oPDateInfo.put("oDates",oDatesNew);
                    layerInfo.put(oDate.getString("id_PF"),oPDateInfo);
                    actByOPInfo.put(oDate.getString("layer"),layerInfo);
                    actionIdO.put(id_OP,actByOPInfo);
                    System.out.println("添加后:");
                    System.out.println(JSON.toJSONString(actionIdO));

                    // 定义存储最后结束时间参数
                    long storageLastEndTime;
                    // 判断序号是为1层级
                    if (csSta == 1) {
                        // 获取实际结束时间
                        Long actualEndTime = timeHandleInfo.getLong("xFin");
//                System.out.println("xFinW:"+xFin);
                        // 定义存储判断实际结束时间是否为空
                        boolean isActualEndTime = false;
                        // 判断实际结束时间不等于空
                        if (null != actualEndTime) {
//                    System.out.println("xFin:"+xFin);
                            // 赋值实际结束时间
                            hTeStart = actualEndTime;
                            // 判断当前实际结束时间大于最大结束时间
                            if (actualEndTime > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = actualEndTime;
                            }
                            // 设置不为空
                            isActualEndTime = true;
                        } else {
                            // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
                            if (task.getTePFinish() > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = task.getTePFinish();
                            }
                        }
//                System.out.println("maxSte:"+maxSte);
                        // 判断实际结束时间不为空
                        if (isActualEndTime) {
                            // 赋值结束时间
                            storageLastEndTime = actualEndTime;
                        } else {
                            // 赋值结束时间
                            storageLastEndTime = task.getTePFinish();
                        }
                        // 判断是第一次进入
                        if (canOnlyEnterOnce) {
                            // 添加设置第一层的开始时间
                            lastEndTime=task.getTePStart();
                            // 设置只能进入一次
                            canOnlyEnterOnce = false;
                        }
                    } else {
                        // 直接赋值最后结束时间
                        storageLastEndTime = hTeStart;
                    }
                    // 根据父id获取最后结束时间信息
                    JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                    // 判断最后结束时间信息为空
                    if (null == fatherGetEndTimeInfo) {
                        // 创建并且赋值最后结束时间
                        fatherGetEndTimeInfo = new JSONObject();
                        fatherGetEndTimeInfo.put(priorItem.toString(),0);
                    }
                    // 根据序号获取最后结束时间
                    Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
                    // 判断最后结束时间为空
                    if (null == aLong) {
                        // 为空，则直接添加最后结束时间信息
                        fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                        serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                    } else {
                        // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
                        if (storageLastEndTime > aLong) {
                            // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
                            fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                            serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                        }
                    }
                    initialStartTime=hTeStart;
                    if (getThisInfoIsFinalPart(thisInfo) && getThisInfoIsConflict(thisInfo)) {
                        System.out.println("开始处理冲突任务:");
                        System.out.println(JSON.toJSONString(objTaskAll));
                        System.out.println(JSON.toJSONString(allImageTasks));
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject thisInfoConflictInfoSon = thisInfoConflictInfo.getJSONObject( i+ "");
                        thisInfoConflictInfoSon.put("status",1);
                        thisInfoConflictInfo.put(i+"",thisInfoConflictInfoSon);
                        setThisInfoConflictInfo(thisInfo,thisInfoConflictInfo);

                        JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
                        List<Task> conflict = new ArrayList<>();
                        JSONArray conflictArr = thisInfoQuiltConflictInfo.getJSONArray("conflict");
                        for (int c = 0; c < conflictArr.size(); c++) {
                            conflict.add(JSONObject.parseObject(JSON.toJSONString(conflictArr.getJSONObject(c)), Task.class));
                        }
                        conflict.sort(Comparator.comparing(Task::getPriority));
                        JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
                        JSONObject indexInfoNew = new JSONObject();
                        for (int conf = 0; conf < conflict.size(); conf++) {
                            Task taskConflict = conflict.get(conf);
                            String id_OConf = taskConflict.getId_O();
                            Integer dateIndexConf = taskConflict.getDateIndex();
                            for (String indInfoKey : indexInfo.keySet()) {
                                JSONObject object = indexInfo.getJSONObject(indInfoKey);
                                if (object.getString("id_O").equals(id_OConf)
                                        && Objects.equals(object.getInteger("dateIndex"), dateIndexConf)) {
                                    indexInfoNew.put(conf+"", object);
                                }
                            }
                        }
                        thisInfoQuiltConflictInfo.put("indexInfo",indexInfoNew);
                        thisInfoQuiltConflictInfo.put("conflict",conflict);
                        System.out.println(JSON.toJSONString(thisInfoQuiltConflictInfo));
                        setThisInfoQuiltConflictInfo(thisInfo,thisInfoQuiltConflictInfo);

                        updateAllImageTasksSetObjTaskAll(objTaskAll,allImageTasks,allImageTotalTime);

                        dgHandleFollowUpConflict(thisInfo,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
                                ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                ,clearStatus,allImageTeDate,conflict,grpUNumAll);
                    }
                }

                // 调用任务最后处理方法
                timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,salesOrderData.getId()
                        ,objOrderList,actionIdO,allImageTeDate,depAllTime,thisInfo,oDateObj);

                // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.remove(random);
                // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                newestLastCurrentTimestamp.remove(random);
                // 根据当前唯一标识删除信息
                onlyRefState.remove(random);
                System.out.println("thisInfo:");
                System.out.println(JSON.toJSONString(thisInfo));
            }
            if (isNext) {
                dgTimeSet(id_OPHighest,layer-1,oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior,teStart);
            }
        }
    }
    private void dgTimeSet(String id_OPHighest,int layer,JSONObject oDateObj,Order salesOrderData
            ,String id_C,JSONObject grpUNumAll,int wn0TPrior,long teStart){
        if (layer > 0) {
            System.out.println("layer:"+layer);
            JSONObject prodInfo = oDateObj.getJSONObject(layer + "");
            for (String prodId : prodInfo.keySet()) {
                JSONObject partInfo = prodInfo.getJSONObject(prodId);
                long maxStaTime = teStart;
                System.out.println("partInfo:");
                JSONObject arrPStartThis = partInfo.getJSONObject("arrPStart");
                System.out.println(JSON.toJSONString(partInfo));
                if (arrPStartThis.size() > 0) {
                    maxStaTime = 0;
                    teStart = 0;
                    long maxStaTimeInside;
                    for (String pfId : arrPStartThis.keySet()) {
                        maxStaTimeInside = arrPStartThis.getJSONObject(pfId).getLong("lastTePFin");
                        if (maxStaTimeInside > maxStaTime) {
                            maxStaTime = maxStaTimeInside;
                            teStart = arrPStartThis.getJSONObject(pfId).getLong("time");
                        }
                    }
//                        maxStaTime = arrPStartThis.getJSONObject(0).getLong("lastTePFin");
//                        teStart = arrPStartThis.getJSONObject(0).getLong("time");
//                        if (arrPStartThis.size() > 0) {
//                            for (int i = 1; i < arrPStartThis.size(); i++) {
//                                Long time = arrPStartThis.getJSONObject(i).getLong("lastTePFin");
//                                if (time > maxStaTime) {
//                                    maxStaTime = time;
//                                    teStart = arrPStartThis.getJSONObject(i).getLong("time");
//                                }
//                            }
//                        }
                }
                partInfo.put("tePStart",maxStaTime);
                // 定义，存储进入未操作到的地方记录
                JSONObject recordNoOperation = new JSONObject();
                // 存储任务所在日期
                JSONObject storageTaskWhereTime = new JSONObject();
                // 镜像任务存储
                Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
                // 镜像总时间存储
                JSONObject allImageTotalTime = new JSONObject();
                // 全部任务存储
                JSONObject objTaskAll = new JSONObject();
                JSONObject grpBGroupIdOJ = new JSONObject();
                // 存储casItemx内订单列表的订单oDates数据
                JSONObject actionIdO = new JSONObject();
                // 存储部门对应组别的上班和下班时间
                JSONObject xbAndSbAll = new JSONObject();
                // 统一id_O和index存储记录状态信息
                JSONObject recordId_OIndexState = new JSONObject();
                // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                JSONObject onlyRefState = new JSONObject();
                // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                JSONObject newestLastCurrentTimestamp = new JSONObject();
                // 存储当前唯一编号的第一个当前时间戳
                JSONObject onlyFirstTimeStamp = new JSONObject();
                // 获取唯一下标
                String random = new ObjectId().toString();
                // 获取全局唯一下标
                String randomAll = new ObjectId().toString();

                // 设置问题记录的初始值
                yiShu.put(randomAll,0);
                leiW.put(randomAll,0);
                xin.put(randomAll,0);
                isQzTz.put(randomAll,0);
                recordNoOperation.put(randomAll,new JSONArray());

                // 设置存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.put(random,teStart);
                // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                onlyRefState.put(random,0);
                // 存储最初开始时间
                long initialStartTime = 0L;
                // 存储最后结束时间
                long lastEndTime = 0L;
                // 获取递归订单列表
                JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
                // 存储递归订单列表的订单编号集合
                JSONArray objOrderList = new JSONArray();
                for (int i = 0; i < objOrder.size() - 1; i++) {
                    // 添加订单编号
                    objOrderList.add(objOrder.getJSONObject(i).getString("id_O"));
                }
                JSONObject depAllTime = new JSONObject();
                System.out.println("--- 分割 ---");
                // 遍历订单列表
                Map<String,Asset> assetMap = new HashMap<>();
                // 当前处理信息
                JSONObject thisInfo = new JSONObject();
                setThisInfoLayer(thisInfo,layer);
                setThisInfoLayerProdId(thisInfo,prodId);
//                System.out.println("xbAndSbAll:");
//                System.out.println(JSON.toJSONString(xbAndSbAll));

                OrderInfo info = salesOrderData.getInfo();
                // 用于存储时间冲突的副本
                JSONObject timeConflictCopy = new JSONObject();
                // 用于存储判断镜像是否是第一个被冲突的产品
                JSONObject sho = new JSONObject();
                // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
                boolean canOnlyEnterOnce = true;
                // 定义用来存储最大结束时间
                long maxLastTime = 0;

                // 用于存储每一个时间任务的结束时间
                JSONArray teFinList = new JSONArray();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                JSONObject serialOneFatherLastTime = new JSONObject();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
                JSONObject serialOneFatherStartTime = new JSONObject();
                // 清理状态
                JSONObject clearStatus = new JSONObject();

                setThisInfoRef(thisInfo,"time");
                // 镜像任务所在时间
                JSONObject allImageTeDate = new JSONObject();
                JSONArray oDates = partInfo.getJSONArray("oDates");
                for (int i = 0; i < oDates.size(); i++) {
                    JSONObject oDate = oDates.getJSONObject(i);
                    getGrpB(oDate.getString("grpB"),oDate.getString("dep"), assetMap,id_C
                            ,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,oDate.getString("id_O"),teStart);
                }
                setOrderFatherId(salesOrderData.getId(),thisInfo,actionIdO,objOrder,oDateObj);

                boolean isOne = false;
                setThisInfoFinalPartDateIndex(thisInfo,(oDates.size()-1));
                setThisInfoFinalPartDate(thisInfo,oDates.getJSONObject((oDates.size()-1)).getString("id_O")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("layer")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("id_PF"),id_OPHighest);
                setThisInfoIsConflict(thisInfo,false);
                // 遍历时间处理信息集合
                for (int i = 0; i < oDates.size(); i++) {
                    setThisInfoIsFinalPart(thisInfo, i >= oDates.size() - 1);
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    // 获取订单编号
                    String id_OInside = oDate.getString("id_O");
                    // 获取订单下标
                    int indexInside = oDate.getInteger("index");
                    // 获取时间处理的序号
                    Integer priorItem = oDate.getInteger("priorItem");
                    // 获取时间处理的父零件编号
                    String id_PF = oDate.getString("id_PF");
                    // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
                    Integer csSta = oDate.getInteger("csSta");
                    // 获取时间处理的判断是否是空时间信息
                    Boolean empty = oDate.getBoolean("empty");
                    // 判断当前时间处理为空时间信息
                    if (empty) {
                        // 获取时间处理的链接下标
                        Integer linkInd = oDate.getInteger("linkInd");
                        // 根据链接下标获取指定的结束时间
                        Long indexEndTime = teFinList.getLong(linkInd);
                        // 判断父id的预计开始时间为空，并且序号为第一个
                        if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
                            serialOneFatherStartTime.put(id_PF,indexEndTime);
                        }
                        // 根据父零件编号获取序号信息
                        JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                        // 判断序号信息为空
                        if (null == fatherSerialInfo) {
                            // 创建序号信息
                            fatherSerialInfo = new JSONObject();
                            // 添加序号的结束时间，默认为0
                            fatherSerialInfo.put(priorItem.toString(),0);
                        }
                        // 获取序号结束时间
                        Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
                        // 添加链接结束时间到当前空时间处理结束时间列表内
                        teFinList.add(indexEndTime);
                        // 判断链接结束时间大于当前结束时间
                        if (indexEndTime > serialEndTime) {
                            // 修改当前结束时间为链接结束时间
                            fatherSerialInfo.put(priorItem.toString(),indexEndTime);
                            // 根据父零件编号添加序号信息
                            serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
                        }
                        continue;
                    }
                    // 获取当前唯一ID存储时间处理的最初开始时间
                    Long hTeStart = initialStartTime;
                    // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
                    JSONObject firstConflictId_O = new JSONObject();
                    JSONObject firstConflictIndex = new JSONObject();
                    // 设置为-1代表的是递归的零件
                    firstConflictIndex.put("prodState",-1);
                    firstConflictIndex.put("z","-1");
                    firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
                    sho.put(oDate.getString("id_O"),firstConflictId_O);
                    // 获取时间处理的组别
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 获取时间处理的零件产品编号
                    String id_P = oDate.getString("id_P");
                    // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
                    Integer kaiJie = oDate.getInteger("kaiJie");
                    // 获取时间处理的实际准备时间
                    Long wntPrep = oDate.getLong("wntPrep");
                    Long wntDurTotal = oDate.getLong("wntDurTotal");
                    long initialTeStart;
                    // 判断当前唯一ID存储时间处理的最初开始时间为0
                    if (hTeStart == 0) {
                        // 调用获取当前时间戳方法设置开始时间
                        initialTeStart = getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                    } else {
                        // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
                        if (csSta == 1 && kaiJie == 1) {
                            // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
                            hTeStart = lastEndTime;
                        }
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-1:"+hTeStart);
                    }
                    boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);

                    // 序号是不为1层级
                    // 判断执行方法为true
                    if (isExecutionMethod) {
                        // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                        JSONObject serialOneEndTime;
                        System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
                        System.out.println(JSON.toJSONString(serialOneFatherLastTime));
                        // 获取判断自己的id是否等于已存在的父id
                        boolean b = serialOneFatherLastTime.containsKey(id_P);
                        // 判断自己的id是已存在的父id
                        if (b) {
                            // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
                            // 转换键信息
                            List<String> list = new ArrayList<>(serialOneEndTime.keySet());
                            // 获取最后一个时间信息
                            String s = list.get(list.size() - 1);
                            // 赋值为最后一个时间信息
                            hTeStart = serialOneEndTime.getLong(s);
                        } else {
                            // 根据父id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
                            if (null != serialOneEndTime) {
                                // 获取上一个序号的时间信息并赋值
                                hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
                            }
                        }
                        // 设置开始时间
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-3:"+hTeStart);
                    }

                    // 获取任务的最初开始时间备份
                    long teStartBackups = initialTeStart;
                    if (!isOne) {
                        teStartBackups = maxStaTime;
                    }
                    // 获取最初结束时间
                    Long teFin = (teStartBackups+(wntDurTotal+wntPrep));
                    // 获取任务信息，并且转换为任务类
                    Task task = new Task(oDate.getInteger("priority"),id_OInside,indexInside,wntPrep
                            ,wntDurTotal+wntPrep,id_C,i,oDate.getJSONObject("wrdN"));
                    // 设置最初任务信息的时间信息
                    task.setTePStart(teStartBackups);
                    task.setTePFinish(teFin);
                    task.setTeCsStart(teStartBackups);
                    task.setTeCsSonOneStart(0L);
                    task.setWrdNO(info.getWrdN());
                    task.setRefOP(salesOrderData.getId());
                    task.setWn2qtyneed(oDate.getDouble("wn2qtyneed"));
                    task.setId_PF(getThisInfoLayerProdId(thisInfo));
                    task.setLayer(getThisInfoLayer(thisInfo));
                    // 判断优先级不等于-1
                    if (wn0TPrior != -1) {
                        // 设置优先级为传参的优先级
                        task.setPriority(wn0TPrior);
                    }
                    // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
                    if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
                            && kaiJie != 5 && kaiJie != 3
                    ) {
                        // 根据父id添加开始时间
                        serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
                    } else if (kaiJie == 3 || kaiJie == 5) {
                        // 添加子最初开始时间
                        task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
                    }

                    // 创建当前处理的任务的所在日期对象
                    JSONObject teDate = new JSONObject();
                    System.out.println();
                    System.out.println("++ taskTe: ++ "+dep+" - "+grpB);
                    System.out.println();
                    System.out.println(JSON.toJSONString(task));
                    System.out.println(JSON.toJSONString(objTaskAll));
                    System.out.println(JSON.toJSONString(allImageTasks));

                    JSONObject conflictInfo = getThisInfoConflictInfo(thisInfo);
                    if (null == conflictInfo) {
                        conflictInfo = new JSONObject();
                    }
                    JSONObject thisConfInfo = new JSONObject();
                    thisConfInfo.put("dep",dep);
                    thisConfInfo.put("grpB",grpB);
                    thisConfInfo.put("status",0);
                    conflictInfo.put(task.getDateIndex()+"",thisConfInfo);
                    setThisInfoConflictInfo(thisInfo,conflictInfo);
                    int isCanOnly = 1;
                    if (isOne) {
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject upConfInfo = thisInfoConflictInfo.getJSONObject((task.getDateIndex() - 1) + "");
                        for (int upI = (task.getDateIndex()-1); upI >= 0; upI--) {
                            upConfInfo = thisInfoConflictInfo.getJSONObject((upI) + "");
                            if (null != upConfInfo) {
                                break;
                            }
                        }
                        if (null != upConfInfo) {
                            Integer status = upConfInfo.getInteger("status");
                            if (status == 1) {
                                String upDep = upConfInfo.getString("dep");
                                String upGrpB = upConfInfo.getString("grpB");
                                if (!upDep.equals(dep) || !upGrpB.equals(grpB)) {
                                    System.out.println("--+ 进入前置修改全局任务信息 +-- :");
                                    updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
                                    System.out.println("修改后:");
                                    System.out.println(JSON.toJSONString(objTaskAll));
                                    System.out.println(JSON.toJSONString(allImageTasks));
                                }
                            }
                        }
                    } else {
                        isOne = true;
                        isCanOnly = 0;
                        onlyRefState.put(random,1);
                        hTeStart = maxStaTime;
                    }
                    // 调用时间处理方法
                    JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
                            ,0,random,isCanOnly,teDate,timeConflictCopy,0
                            ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                            ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
                    // 更新任务最初始开始时间
                    hTeStart = timeHandleInfo.getLong("hTeStart");
                    System.out.println("最外层-3-2:"+hTeStart);
                    System.out.println(JSON.toJSONString(timeHandleInfo));
                    System.out.println(JSON.toJSONString(actionIdO));
                    System.out.println(JSON.toJSONString(oDate));
                    // 添加结束时间
                    teFinList.add(hTeStart);

                    System.out.println("id_OInside:"+id_OInside);
                    String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
                    JSONObject actByOPInfo = actionIdO.getJSONObject(id_OP);
                    JSONObject layerInfo = actByOPInfo.getJSONObject(oDate.getString("layer"));
                    JSONObject oPDateInfo = layerInfo.getJSONObject(oDate.getString("id_PF"));
                    JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
                    JSONObject thisODate = oDatesNew.getJSONObject(i);
                    thisODate.put("teDate",teDate);
                    oDatesNew.set(i,thisODate);
                    oPDateInfo.put("oDates",oDatesNew);
                    layerInfo.put(oDate.getString("id_PF"),oPDateInfo);
                    actByOPInfo.put(oDate.getString("layer"),layerInfo);
                    actionIdO.put(id_OP,actByOPInfo);
                    System.out.println("添加后:");
                    System.out.println(JSON.toJSONString(actionIdO));

                    // 定义存储最后结束时间参数
                    long storageLastEndTime;
                    // 判断序号是为1层级
                    if (csSta == 1) {
                        // 获取实际结束时间
                        Long actualEndTime = timeHandleInfo.getLong("xFin");
//                System.out.println("xFinW:"+xFin);
                        // 定义存储判断实际结束时间是否为空
                        boolean isActualEndTime = false;
                        // 判断实际结束时间不等于空
                        if (null != actualEndTime) {
//                    System.out.println("xFin:"+xFin);
                            // 赋值实际结束时间
                            hTeStart = actualEndTime;
                            // 判断当前实际结束时间大于最大结束时间
                            if (actualEndTime > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = actualEndTime;
                            }
                            // 设置不为空
                            isActualEndTime = true;
                        } else {
                            // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
                            if (task.getTePFinish() > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = task.getTePFinish();
                            }
                        }
//                System.out.println("maxSte:"+maxSte);
                        // 判断实际结束时间不为空
                        if (isActualEndTime) {
                            // 赋值结束时间
                            storageLastEndTime = actualEndTime;
                        } else {
                            // 赋值结束时间
                            storageLastEndTime = task.getTePFinish();
                        }
                        // 判断是第一次进入
                        if (canOnlyEnterOnce) {
                            // 添加设置第一层的开始时间
                            lastEndTime=task.getTePStart();
                            // 设置只能进入一次
                            canOnlyEnterOnce = false;
                        }
                    } else {
                        // 直接赋值最后结束时间
                        storageLastEndTime = hTeStart;
                    }
                    // 根据父id获取最后结束时间信息
                    JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                    // 判断最后结束时间信息为空
                    if (null == fatherGetEndTimeInfo) {
                        // 创建并且赋值最后结束时间
                        fatherGetEndTimeInfo = new JSONObject();
                        fatherGetEndTimeInfo.put(priorItem.toString(),0);
                    }
                    // 根据序号获取最后结束时间
                    Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
                    // 判断最后结束时间为空
                    if (null == aLong) {
                        // 为空，则直接添加最后结束时间信息
                        fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                        serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                    } else {
                        // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
                        if (storageLastEndTime > aLong) {
                            // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
                            fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                            serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                        }
                    }
                    initialStartTime=hTeStart;
                    if (getThisInfoIsFinalPart(thisInfo) && getThisInfoIsConflict(thisInfo)) {
                        System.out.println("开始处理冲突任务:");
                        System.out.println(JSON.toJSONString(objTaskAll));
                        System.out.println(JSON.toJSONString(allImageTasks));
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject thisInfoConflictInfoSon = thisInfoConflictInfo.getJSONObject( i+ "");
                        thisInfoConflictInfoSon.put("status",1);
                        thisInfoConflictInfo.put(i+"",thisInfoConflictInfoSon);
                        setThisInfoConflictInfo(thisInfo,thisInfoConflictInfo);

                        JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
                        List<Task> conflict = new ArrayList<>();
                        JSONArray conflictArr = thisInfoQuiltConflictInfo.getJSONArray("conflict");
                        for (int c = 0; c < conflictArr.size(); c++) {
                            conflict.add(JSONObject.parseObject(JSON.toJSONString(conflictArr.getJSONObject(c)), Task.class));
                        }
                        conflict.sort(Comparator.comparing(Task::getPriority));
                        JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
                        JSONObject indexInfoNew = new JSONObject();
                        for (int conf = 0; conf < conflict.size(); conf++) {
                            Task taskConflict = conflict.get(conf);
                            String id_OConf = taskConflict.getId_O();
                            Integer dateIndexConf = taskConflict.getDateIndex();
                            for (String indInfoKey : indexInfo.keySet()) {
                                JSONObject object = indexInfo.getJSONObject(indInfoKey);
                                if (object.getString("id_O").equals(id_OConf)
                                        && Objects.equals(object.getInteger("dateIndex"), dateIndexConf)) {
                                    indexInfoNew.put(conf+"", object);
                                }
                            }
                        }
                        thisInfoQuiltConflictInfo.put("indexInfo",indexInfoNew);
                        thisInfoQuiltConflictInfo.put("conflict",conflict);
                        System.out.println(JSON.toJSONString(thisInfoQuiltConflictInfo));
                        setThisInfoQuiltConflictInfo(thisInfo,thisInfoQuiltConflictInfo);

                        updateAllImageTasksSetObjTaskAll(objTaskAll,allImageTasks,allImageTotalTime);

                        dgHandleFollowUpConflict(thisInfo,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
                                ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                ,clearStatus,allImageTeDate,conflict,grpUNumAll);
                    }
                }

                // 调用任务最后处理方法
                timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,salesOrderData.getId()
                        ,objOrderList,actionIdO,allImageTeDate,depAllTime,thisInfo,oDateObj);

                // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.remove(random);
                // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                newestLastCurrentTimestamp.remove(random);
                // 根据当前唯一标识删除信息
                onlyRefState.remove(random);
                System.out.println("thisInfo:");
                System.out.println(JSON.toJSONString(thisInfo));
            }
            dgTimeSet(id_OPHighest,layer-1,oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior,teStart);
        }
    }

    /**
     * 多订单时间处理方法
     * @param id_C  公司编号
     * @param orderList 订单信息
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     */
    @Override
    public ApiResponse getAtFirstList(String id_C, JSONArray orderList) {
        if (isTest)
            // 调用根据公司编号清空所有任务信息方法
            setTaskAndZonKai(id_C);
        for (int o = 0; o < orderList.size(); o++) {
            JSONObject orderInfo = orderList.getJSONObject(o);
            String id_O = orderInfo.getString("id_O");
            Integer wn0TPrior = orderInfo.getInteger("wn0TPrior");
            Long teStart = orderInfo.getLong("teStart");
            System.out.println(o+"- 订单开始:"+id_O);
//            atFirst(wn0TPrior,teStart,id_O,id_C);
            atFirstODateObj(wn0TPrior,teStart,id_O,id_C);
            System.out.println("--- 分割 ---");
        }
        return retResult.ok(CodeEnum.OK.getCode(), "多个时间处理成功!");
    }

    /**
     * 时间处理核心方法
     * @param id_O  主订单编号
     * @param teStart   开始时间
     * @param id_C  公司编号
     * @param wn0TPrior 优先级
     * @author tang
     * @ver 1.0.0
     */
    public void atFirstODateObj(int wn0TPrior,long teStart,String id_O,String id_C){
        TimeZj.isZ = 6;
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
                || null == salesOrderData.getCasItemx() || null == salesOrderData.getInfo()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject casItemx = salesOrderData.getCasItemx();
        // 获取递归存储的时间处理信息
        JSONObject oDateObj = casItemx.getJSONObject("java").getJSONObject("oDateObj");
        // 存储部门对应组别的职位总人数
        JSONObject grpUNumAll = new JSONObject();
        oDateObj = mergeTaskByPriorODateObj(oDateObj);

//        // 抛出操作成功异常
//        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
        System.out.println("开始oDateObj:");
        System.out.println(JSON.toJSONString(oDateObj));
        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
        List<Integer> layerList = new ArrayList<>();
        for (String layer : oDateObj.keySet()) {
            layerList.add(Integer.parseInt(layer));

            JSONObject layerInfo = oDateObj.getJSONObject(layer);
            for (String id_PF : layerInfo.keySet()) {
                JSONObject pfInfo = layerInfo.getJSONObject(id_PF);
                pfInfo.put("arrPStart",new JSONObject());
                layerInfo.put(id_PF,pfInfo);
                oDateObj.put(layer,layerInfo);
            }
        }
        layerList.sort(Comparator.reverseOrder());
        dgTime(id_O,layerList.get(0),oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior,true,teStart);
        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
    }
    private void dgTime(String id_OPHighest,int layer,JSONObject oDateObj,Order salesOrderData
            ,String id_C,JSONObject grpUNumAll,int wn0TPrior,boolean isFirst,long teStart){
        if (layer > 0) {
            System.out.println("layer:"+layer);
            JSONObject prodInfo = oDateObj.getJSONObject(layer + "");
            for (String prodId : prodInfo.keySet()) {
                JSONObject partInfo = prodInfo.getJSONObject(prodId);
                long maxStaTime = teStart;
                if (!isFirst) {
                    System.out.println("partInfo:");
                    JSONObject arrPStartThis = partInfo.getJSONObject("arrPStart");
                    System.out.println(JSON.toJSONString(partInfo));
                    if (arrPStartThis.size() > 0) {
                        maxStaTime = 0;
                        teStart = 0;
                        long maxStaTimeInside;
                        for (String pfId : arrPStartThis.keySet()) {
                            maxStaTimeInside = arrPStartThis.getJSONObject(pfId).getLong("lastTePFin");
                            if (maxStaTimeInside > maxStaTime) {
                                maxStaTime = maxStaTimeInside;
                                teStart = arrPStartThis.getJSONObject(pfId).getLong("time");
                            }
                        }
//                        maxStaTime = arrPStartThis.getJSONObject(0).getLong("lastTePFin");
//                        teStart = arrPStartThis.getJSONObject(0).getLong("time");
//                        if (arrPStartThis.size() > 0) {
//                            for (int i = 1; i < arrPStartThis.size(); i++) {
//                                Long time = arrPStartThis.getJSONObject(i).getLong("lastTePFin");
//                                if (time > maxStaTime) {
//                                    maxStaTime = time;
//                                    teStart = arrPStartThis.getJSONObject(i).getLong("time");
//                                }
//                            }
//                        }
                    }
                }
                partInfo.put("tePStart",maxStaTime);
                // 定义，存储进入未操作到的地方记录
                JSONObject recordNoOperation = new JSONObject();
                // 存储任务所在日期
                JSONObject storageTaskWhereTime = new JSONObject();
                // 镜像任务存储
                Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks = new HashMap<>(16);
                // 镜像总时间存储
                JSONObject allImageTotalTime = new JSONObject();
                // 全部任务存储
                JSONObject objTaskAll = new JSONObject();
                JSONObject grpBGroupIdOJ = new JSONObject();
                // 存储casItemx内订单列表的订单oDates数据
                JSONObject actionIdO = new JSONObject();
                // 存储部门对应组别的上班和下班时间
                JSONObject xbAndSbAll = new JSONObject();
                // 统一id_O和index存储记录状态信息
                JSONObject recordId_OIndexState = new JSONObject();
                // 存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                JSONObject onlyRefState = new JSONObject();
                // 根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                JSONObject newestLastCurrentTimestamp = new JSONObject();
                // 存储当前唯一编号的第一个当前时间戳
                JSONObject onlyFirstTimeStamp = new JSONObject();
                // 获取唯一下标
                String random = new ObjectId().toString();
                // 获取全局唯一下标
                String randomAll = new ObjectId().toString();

                // 设置问题记录的初始值
                yiShu.put(randomAll,0);
                leiW.put(randomAll,0);
                xin.put(randomAll,0);
                isQzTz.put(randomAll,0);
                recordNoOperation.put(randomAll,new JSONArray());

                // 设置存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.put(random,teStart);
                // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
                onlyRefState.put(random,0);
                // 存储最初开始时间
                long initialStartTime = 0L;
                // 存储最后结束时间
                long lastEndTime = 0L;
                // 获取递归订单列表
                JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
                // 存储递归订单列表的订单编号集合
                JSONArray objOrderList = new JSONArray();
                for (int i = 0; i < objOrder.size() - 1; i++) {
                    // 添加订单编号
                    objOrderList.add(objOrder.getJSONObject(i).getString("id_O"));
                }
                JSONObject depAllTime = new JSONObject();
                System.out.println("--- 分割 ---");
                // 遍历订单列表
                Map<String,Asset> assetMap = new HashMap<>();
                // 当前处理信息
                JSONObject thisInfo = new JSONObject();
                setThisInfoLayer(thisInfo,layer);
                setThisInfoLayerProdId(thisInfo,prodId);
//                System.out.println("xbAndSbAll:");
//                System.out.println(JSON.toJSONString(xbAndSbAll));

                OrderInfo info = salesOrderData.getInfo();
                // 用于存储时间冲突的副本
                JSONObject timeConflictCopy = new JSONObject();
                // 用于存储判断镜像是否是第一个被冲突的产品
                JSONObject sho = new JSONObject();
                // 用于存储控制只进入一次的判断，用于记录第一个数据处理的结束时间
                boolean canOnlyEnterOnce = true;
                // 定义用来存储最大结束时间
                long maxLastTime = 0;

                // 用于存储每一个时间任务的结束时间
                JSONArray teFinList = new JSONArray();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                JSONObject serialOneFatherLastTime = new JSONObject();
                // 用于存储，产品序号为1处理的，按照父零件编号存储每个序号的预计开始时间
                JSONObject serialOneFatherStartTime = new JSONObject();
                // 清理状态
                JSONObject clearStatus = new JSONObject();

                setThisInfoRef(thisInfo,"time");
                // 镜像任务所在时间
                JSONObject allImageTeDate = new JSONObject();
                JSONArray oDates = partInfo.getJSONArray("oDates");
                for (int i = 0; i < oDates.size(); i++) {
                    JSONObject oDate = oDates.getJSONObject(i);
                    getGrpB(oDate.getString("grpB"),oDate.getString("dep"), assetMap,id_C
                            ,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,oDate.getString("id_O"),teStart);
                }
                setOrderFatherId(salesOrderData.getId(),thisInfo,actionIdO,objOrder,oDateObj);

                boolean isOne = false;
                setThisInfoFinalPartDateIndex(thisInfo,(oDates.size()-1));
                setThisInfoFinalPartDate(thisInfo,oDates.getJSONObject((oDates.size()-1)).getString("id_O")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("layer")
                        ,oDates.getJSONObject((oDates.size()-1)).getString("id_PF"),id_OPHighest);
                setThisInfoIsConflict(thisInfo,false);
                // 遍历时间处理信息集合
                for (int i = 0; i < oDates.size(); i++) {
                    setThisInfoIsFinalPart(thisInfo, i >= oDates.size() - 1);
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    // 获取订单编号
                    String id_OInside = oDate.getString("id_O");
                    // 获取订单下标
                    int indexInside = oDate.getInteger("index");
                    // 获取时间处理的序号
                    Integer priorItem = oDate.getInteger("priorItem");
                    // 获取时间处理的父零件编号
                    String id_PF = oDate.getString("id_PF");
                    // 获取时间处理的序号是否为1层级 csSta - timeHandleSerialNoIsOne
                    Integer csSta = oDate.getInteger("csSta");
                    // 获取时间处理的判断是否是空时间信息
                    Boolean empty = oDate.getBoolean("empty");
                    // 判断当前时间处理为空时间信息
                    if (empty) {
                        // 获取时间处理的链接下标
                        Integer linkInd = oDate.getInteger("linkInd");
                        // 根据链接下标获取指定的结束时间
                        Long indexEndTime = teFinList.getLong(linkInd);
                        // 判断父id的预计开始时间为空，并且序号为第一个
                        if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1) {
                            serialOneFatherStartTime.put(id_PF,indexEndTime);
                        }
                        // 根据父零件编号获取序号信息
                        JSONObject fatherSerialInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                        // 判断序号信息为空
                        if (null == fatherSerialInfo) {
                            // 创建序号信息
                            fatherSerialInfo = new JSONObject();
                            // 添加序号的结束时间，默认为0
                            fatherSerialInfo.put(priorItem.toString(),0);
                        }
                        // 获取序号结束时间
                        Long serialEndTime = fatherSerialInfo.getLong(priorItem.toString());
                        // 添加链接结束时间到当前空时间处理结束时间列表内
                        teFinList.add(indexEndTime);
                        // 判断链接结束时间大于当前结束时间
                        if (indexEndTime > serialEndTime) {
                            // 修改当前结束时间为链接结束时间
                            fatherSerialInfo.put(priorItem.toString(),indexEndTime);
                            // 根据父零件编号添加序号信息
                            serialOneFatherLastTime.put(id_PF,fatherSerialInfo);
                        }
                        continue;
                    }
                    // 获取当前唯一ID存储时间处理的最初开始时间
                    Long hTeStart = initialStartTime;
                    // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
                    JSONObject firstConflictId_O = new JSONObject();
                    JSONObject firstConflictIndex = new JSONObject();
                    // 设置为-1代表的是递归的零件
                    firstConflictIndex.put("prodState",-1);
                    firstConflictIndex.put("z","-1");
                    firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
                    sho.put(oDate.getString("id_O"),firstConflictId_O);
                    // 获取时间处理的组别
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 获取时间处理的零件产品编号
                    String id_P = oDate.getString("id_P");
                    // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
                    Integer kaiJie = oDate.getInteger("kaiJie");
                    // 获取时间处理的实际准备时间
                    Long wntPrep = oDate.getLong("wntPrep");
                    Long wntDurTotal = oDate.getLong("wntDurTotal");
                    long initialTeStart;
                    // 判断当前唯一ID存储时间处理的最初开始时间为0
                    if (hTeStart == 0) {
                        // 调用获取当前时间戳方法设置开始时间
                        initialTeStart = getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp);
                    } else {
                        // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
                        if (csSta == 1 && kaiJie == 1) {
                            // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
                            hTeStart = lastEndTime;
                        }
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-1:"+hTeStart);
                    }
                    boolean isExecutionMethod = (csSta == 0 && priorItem != 1) || (kaiJie != 1 && csSta == 1);

                    // 序号是不为1层级
                    // 判断执行方法为true
                    if (isExecutionMethod) {
                        // 定义获取存储，产品序号为1处理的，按照父零件编号存储每个序号的最后结束时间
                        JSONObject serialOneEndTime;
                        System.out.println("serialOneFatherLastTime:id_P:"+id_P+" - id_PF:"+id_PF);
                        System.out.println(JSON.toJSONString(serialOneFatherLastTime));
                        // 获取判断自己的id是否等于已存在的父id
                        boolean b = serialOneFatherLastTime.containsKey(id_P);
                        // 判断自己的id是已存在的父id
                        if (b) {
                            // 根据自己的id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_P);
                            // 转换键信息
                            List<String> list = new ArrayList<>(serialOneEndTime.keySet());
                            // 获取最后一个时间信息
                            String s = list.get(list.size() - 1);
                            // 赋值为最后一个时间信息
                            hTeStart = serialOneEndTime.getLong(s);
                        } else {
                            // 根据父id获取按照父零件编号存储每个序号的最后结束时间
                            serialOneEndTime = serialOneFatherLastTime.getJSONObject(id_PF);
                            if (null != serialOneEndTime) {
                                // 获取上一个序号的时间信息并赋值
                                hTeStart = serialOneEndTime.getLong(((priorItem - 1) + ""));
                            }
                        }
                        // 设置开始时间
                        initialTeStart = hTeStart;
                        System.out.println("这里开始时间-3:"+hTeStart);
                    }

                    // 获取任务的最初开始时间备份
                    long teStartBackups = initialTeStart;
                    if (!isFirst && !isOne) {
                        teStartBackups = maxStaTime;
                    }
                    // 获取最初结束时间
                    Long teFin = (teStartBackups+(wntDurTotal+wntPrep));
                    // 获取任务信息，并且转换为任务类
                    Task task = new Task(oDate.getInteger("priority"),id_OInside,indexInside,wntPrep
                            ,wntDurTotal+wntPrep,id_C,i,oDate.getJSONObject("wrdN"));
                    // 设置最初任务信息的时间信息
                    task.setTePStart(teStartBackups);
                    task.setTePFinish(teFin);
                    task.setTeCsStart(teStartBackups);
                    task.setTeCsSonOneStart(0L);
                    task.setWrdNO(info.getWrdN());
                    task.setRefOP(salesOrderData.getId());
                    task.setWn2qtyneed(oDate.getDouble("wn2qtyneed"));
                    task.setId_PF(getThisInfoLayerProdId(thisInfo));
                    task.setLayer(getThisInfoLayer(thisInfo));
                    // 判断优先级不等于-1
                    if (wn0TPrior != -1) {
                        // 设置优先级为传参的优先级
                        task.setPriority(wn0TPrior);
                    }
                    // 判断父id的预计开始时间为空并且，序号为1，并且不是部件并且不是递归的最后一个
                    if (null == serialOneFatherStartTime.getLong(id_PF) && priorItem == 1
                            && kaiJie != 5 && kaiJie != 3
                    ) {
                        // 根据父id添加开始时间
                        serialOneFatherStartTime.put(id_PF,task.getTeCsStart());
                    } else if (kaiJie == 3 || kaiJie == 5) {
                        // 添加子最初开始时间
                        task.setTeCsSonOneStart(serialOneFatherStartTime.getLong(id_P));
                    }

                    // 创建当前处理的任务的所在日期对象
                    JSONObject teDate = new JSONObject();
                    System.out.println();
                    System.out.println("++ taskTe: ++ "+dep+" - "+grpB);
                    System.out.println();
                    System.out.println(JSON.toJSONString(task));
                    System.out.println(JSON.toJSONString(objTaskAll));
                    System.out.println(JSON.toJSONString(allImageTasks));

                    JSONObject conflictInfo = getThisInfoConflictInfo(thisInfo);
                    if (null == conflictInfo) {
                        conflictInfo = new JSONObject();
                    }
                    JSONObject thisConfInfo = new JSONObject();
                    thisConfInfo.put("dep",dep);
                    thisConfInfo.put("grpB",grpB);
                    thisConfInfo.put("status",0);
                    conflictInfo.put(task.getDateIndex()+"",thisConfInfo);
                    setThisInfoConflictInfo(thisInfo,conflictInfo);
                    int isCanOnly = 1;
                    if (isOne) {
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject upConfInfo = thisInfoConflictInfo.getJSONObject((task.getDateIndex() - 1) + "");
                        for (int upI = (task.getDateIndex()-1); upI >= 0; upI--) {
                            upConfInfo = thisInfoConflictInfo.getJSONObject((upI) + "");
                            if (null != upConfInfo) {
                                break;
                            }
                        }
                        if (null != upConfInfo) {
                            Integer status = upConfInfo.getInteger("status");
                            if (status == 1) {
                                String upDep = upConfInfo.getString("dep");
                                String upGrpB = upConfInfo.getString("grpB");
                                if (!upDep.equals(dep) || !upGrpB.equals(grpB)) {
                                    System.out.println("--+ 进入前置修改全局任务信息 +-- :");
                                    updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
                                    System.out.println("修改后:");
                                    System.out.println(JSON.toJSONString(objTaskAll));
                                    System.out.println(JSON.toJSONString(allImageTasks));
                                }
                            }
                        }
                    } else {
                        isOne = true;
                        if (!isFirst) {
                            isCanOnly = 0;
                            onlyRefState.put(random,1);
                            hTeStart = maxStaTime;
                        }
                    }
                    // 调用时间处理方法
                    JSONObject timeHandleInfo = timeHandle(task,hTeStart,grpB,dep,id_OInside,indexInside
                            ,0,random,isCanOnly,teDate,timeConflictCopy,0
                            ,sho,0,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                            ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                            ,clearStatus,thisInfo,allImageTeDate,false,depAllTime);
                    // 更新任务最初始开始时间
                    hTeStart = timeHandleInfo.getLong("hTeStart");
                    System.out.println("最外层-3-2:"+hTeStart);
                    System.out.println(JSON.toJSONString(timeHandleInfo));
                    System.out.println(JSON.toJSONString(actionIdO));
                    System.out.println(JSON.toJSONString(oDate));
                    // 添加结束时间
                    teFinList.add(hTeStart);

                    System.out.println("id_OInside:"+id_OInside);
                    String id_OP = sonGetOrderFatherId(id_OInside, id_C, thisInfo, actionIdO, grpUNumAll);
                    JSONObject actByOPInfo = actionIdO.getJSONObject(id_OP);
                    JSONObject layerInfo = actByOPInfo.getJSONObject(oDate.getString("layer"));
                    JSONObject oPDateInfo = layerInfo.getJSONObject(oDate.getString("id_PF"));
                    JSONArray oDatesNew = oPDateInfo.getJSONArray("oDates");
                    JSONObject thisODate = oDatesNew.getJSONObject(i);
                    thisODate.put("teDate",teDate);
                    oDatesNew.set(i,thisODate);
                    oPDateInfo.put("oDates",oDatesNew);
                    layerInfo.put(oDate.getString("id_PF"),oPDateInfo);
                    actByOPInfo.put(oDate.getString("layer"),layerInfo);
                    actionIdO.put(id_OP,actByOPInfo);
                    System.out.println("添加后:");
                    System.out.println(JSON.toJSONString(actionIdO));

                    // 定义存储最后结束时间参数
                    long storageLastEndTime;
                    // 判断序号是为1层级
                    if (csSta == 1) {
                        // 获取实际结束时间
                        Long actualEndTime = timeHandleInfo.getLong("xFin");
//                System.out.println("xFinW:"+xFin);
                        // 定义存储判断实际结束时间是否为空
                        boolean isActualEndTime = false;
                        // 判断实际结束时间不等于空
                        if (null != actualEndTime) {
//                    System.out.println("xFin:"+xFin);
                            // 赋值实际结束时间
                            hTeStart = actualEndTime;
                            // 判断当前实际结束时间大于最大结束时间
                            if (actualEndTime > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = actualEndTime;
                            }
                            // 设置不为空
                            isActualEndTime = true;
                        } else {
                            // 判断当前实际结束时间大于最大结束时间：注 ： xFin 和 task.getTePFinish() 有时候是不一样的，不能随便改
                            if (task.getTePFinish() > maxLastTime) {
                                // 判断大于则更新最大结束时间为当前结束时间
                                maxLastTime = task.getTePFinish();
                            }
                        }
//                System.out.println("maxSte:"+maxSte);
                        // 判断实际结束时间不为空
                        if (isActualEndTime) {
                            // 赋值结束时间
                            storageLastEndTime = actualEndTime;
                        } else {
                            // 赋值结束时间
                            storageLastEndTime = task.getTePFinish();
                        }
                        // 判断是第一次进入
                        if (canOnlyEnterOnce) {
                            // 添加设置第一层的开始时间
                            lastEndTime=task.getTePStart();
                            // 设置只能进入一次
                            canOnlyEnterOnce = false;
                        }
                    } else {
                        // 直接赋值最后结束时间
                        storageLastEndTime = hTeStart;
                    }
                    // 根据父id获取最后结束时间信息
                    JSONObject fatherGetEndTimeInfo = serialOneFatherLastTime.getJSONObject(id_PF);
                    // 判断最后结束时间信息为空
                    if (null == fatherGetEndTimeInfo) {
                        // 创建并且赋值最后结束时间
                        fatherGetEndTimeInfo = new JSONObject();
                        fatherGetEndTimeInfo.put(priorItem.toString(),0);
                    }
                    // 根据序号获取最后结束时间
                    Long aLong = fatherGetEndTimeInfo.getLong(priorItem.toString());
                    // 判断最后结束时间为空
                    if (null == aLong) {
                        // 为空，则直接添加最后结束时间信息
                        fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                        serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                    } else {
                        // 不为空，则判断当前最后结束时间大于已存在的最后结束时间
                        if (storageLastEndTime > aLong) {
                            // 判断当前最后结束时间大于，则更新最后结束时间为当前结束时间
                            fatherGetEndTimeInfo.put(priorItem.toString(),storageLastEndTime);
                            serialOneFatherLastTime.put(id_PF,fatherGetEndTimeInfo);
                        }
                    }
                    initialStartTime=hTeStart;
                    if (getThisInfoIsFinalPart(thisInfo) && getThisInfoIsConflict(thisInfo)) {
                        System.out.println("开始处理冲突任务:");
                        System.out.println(JSON.toJSONString(objTaskAll));
                        System.out.println(JSON.toJSONString(allImageTasks));
                        JSONObject thisInfoConflictInfo = getThisInfoConflictInfo(thisInfo);
                        JSONObject thisInfoConflictInfoSon = thisInfoConflictInfo.getJSONObject( i+ "");
                        thisInfoConflictInfoSon.put("status",1);
                        thisInfoConflictInfo.put(i+"",thisInfoConflictInfoSon);
                        setThisInfoConflictInfo(thisInfo,thisInfoConflictInfo);

                        JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
                        List<Task> conflict = new ArrayList<>();
                        JSONArray conflictArr = thisInfoQuiltConflictInfo.getJSONArray("conflict");
                        for (int c = 0; c < conflictArr.size(); c++) {
                            conflict.add(JSONObject.parseObject(JSON.toJSONString(conflictArr.getJSONObject(c)), Task.class));
                        }
                        conflict.sort(Comparator.comparing(Task::getPriority));
                        JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
                        JSONObject indexInfoNew = new JSONObject();
                        for (int conf = 0; conf < conflict.size(); conf++) {
                            Task taskConflict = conflict.get(conf);
                            String id_OConf = taskConflict.getId_O();
                            Integer dateIndexConf = taskConflict.getDateIndex();
                            for (String indInfoKey : indexInfo.keySet()) {
                                JSONObject object = indexInfo.getJSONObject(indInfoKey);
                                if (object.getString("id_O").equals(id_OConf)
                                        && Objects.equals(object.getInteger("dateIndex"), dateIndexConf)) {
                                    indexInfoNew.put(conf+"", object);
                                }
                            }
                        }
                        thisInfoQuiltConflictInfo.put("indexInfo",indexInfoNew);
                        thisInfoQuiltConflictInfo.put("conflict",conflict);
                        System.out.println(JSON.toJSONString(thisInfoQuiltConflictInfo));
                        setThisInfoQuiltConflictInfo(thisInfo,thisInfoQuiltConflictInfo);

                        updateAllImageTasksSetObjTaskAll(objTaskAll,allImageTasks,allImageTotalTime);

                        dgHandleFollowUpConflict(thisInfo,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
                                ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                ,clearStatus,allImageTeDate,conflict,grpUNumAll);
                    }
                }

                // 调用任务最后处理方法
                timeZjServiceComprehensive.taskLastHandle(timeConflictCopy,id_C,randomAll,objTaskAll
                        ,storageTaskWhereTime,allImageTotalTime,allImageTasks,recordNoOperation,salesOrderData.getId()
                        ,objOrderList,actionIdO,allImageTeDate,depAllTime,thisInfo,oDateObj);

                // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
                onlyFirstTimeStamp.remove(random);
                // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
                newestLastCurrentTimestamp.remove(random);
                // 根据当前唯一标识删除信息
                onlyRefState.remove(random);
//                JSONObject thisInfoLastTeInfo = getThisInfoLastTeInfo(thisInfo);
                System.out.println("thisInfo:");
                System.out.println(JSON.toJSONString(thisInfo));
//                System.out.println("thisInfoLastTeInfo:");
//                System.out.println(JSON.toJSONString(thisInfoLastTeInfo));
//                Long lastTePFin = thisInfoLastTeInfo.getLong("lastTePFin");
//                partInfo.put("tePFinish",lastTePFin);
//                prodInfo.put(prodId,partInfo);
//                oDateObj.put(layer + "",prodInfo);
//                int layerUp = layer-1;
//                if (layerUp > 0) {
//                    JSONObject thisInfoFinalPartDate = getThisInfoFinalPartDate(thisInfo);
//                    String id_PF = partInfo.getString("id_PF");
//                    JSONObject prodInfoUp = oDateObj.getJSONObject(layerUp + "");
//                    JSONObject partInfoUp = prodInfoUp.getJSONObject(id_PF);
//                    JSONArray arrPStart = partInfoUp.getJSONArray("arrPStart");
//                    arrPStart.add(thisInfoFinalPartDate.getJSONObject("lastTime"));
//                    partInfoUp.put("arrPStart",arrPStart);
//                    prodInfoUp.put(id_PF,partInfoUp);
//                    oDateObj.put(layerUp + "",prodInfoUp);
//                }
            }
            dgTime(id_OPHighest,layer-1,oDateObj,salesOrderData,id_C,grpUNumAll,wn0TPrior,false,teStart);
        }
    }

    /**
     * 递归处理后续冲突
     */
    @SuppressWarnings("unchecked")
    private void dgHandleFollowUpConflict(JSONObject thisInfo,String id_C,JSONObject objTaskAll
            ,JSONObject depAllTime,String random,JSONObject timeConflictCopy,JSONObject sho,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime
            ,JSONObject allImageTotalTime,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState
            ,JSONObject recordNoOperation,JSONObject clearStatus,JSONObject allImageTeDate,List<Task> conflict
            ,JSONObject grpUNumAll){
        JSONObject thisInfoQuiltConflictInfo = getThisInfoQuiltConflictInfo(thisInfo);
        Integer operateIndex = getQuiltConflictInfoOperateIndex(thisInfo);
        JSONObject indexInfo = thisInfoQuiltConflictInfo.getJSONObject("indexInfo");
        JSONObject indexInfoSon = indexInfo.getJSONObject(operateIndex + "");
        if (null != indexInfoSon) {
            Integer thisI = indexInfoSon.getInteger("thisI");
            Long thisTime = indexInfoSon.getLong("thisTime");
            JSONObject id_oAndIndexTaskInfo = indexInfoSon.getJSONObject("id_OAndIndexTaskInfo");
            Task task = conflict.get(operateIndex);
//            String id_OP = sonGetOrderFatherId(task.getId_O(), task.getId_C(), thisInfo, actionIdO, new JSONObject());
            JSONArray oDates = actionIdO.getJSONObject(task.getRefOP()).getJSONObject(task.getLayer()+"")
                    .getJSONObject(task.getId_PF()).getJSONArray("oDates");
            JSONObject dgInfo = oDates.getJSONObject(task.getDateIndex());
            String dep = dgInfo.getString("dep");
            String grpB = dgInfo.getString("grpB");
            // 获取指定的全局任务信息方法
            JSONObject tasksAndZon = getTasksAndZon(thisTime, grpB, dep, id_C,objTaskAll,depAllTime);
            // 获取任务信息
            Object[] tasksIs = isTasksNull(tasksAndZon,depAllTime.getJSONObject(dep)
                    .getJSONObject(grpB).getLong(thisTime+""));
            List<Task> tasks = (List<Task>) tasksIs[0];
            long zon = (long) tasksIs[1];
            System.out.println(JSON.toJSONString(objTaskAll));
            System.out.println(JSON.toJSONString(allImageTasks));
            System.out.println("最后循序的输出: zon:"+zon+" - thisI:"+thisI+" - thisTime:"+thisTime);
            System.out.println(JSON.toJSONString(id_oAndIndexTaskInfo));
            System.out.println(JSON.toJSONString(conflict));
            System.out.println(JSON.toJSONString(tasks));
            JSONObject conflictEndNew = timeZjServiceComprehensive.handleTimeConflictEndNew(thisI, tasks, conflict, zon, random
                    , timeConflictCopy, 0, 1, sho, csSta, randomAll, xbAndSbAll, actionIdO, objTaskAll
                    , recordId_OIndexState, storageTaskWhereTime, allImageTotalTime, allImageTasks, onlyFirstTimeStamp
                    , newestLastCurrentTimestamp, onlyRefState, recordNoOperation, clearStatus, thisInfo, allImageTeDate
                    , depAllTime, id_oAndIndexTaskInfo, operateIndex,false,0);
            updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
            if (task.getLayer() > 1) {
                JSONObject thisInfoSonLayerInfo = getThisInfoSonLayerInfo(thisInfo);
                System.out.println("thisInfoSonLayerInfo:");
                System.out.println(JSON.toJSONString(thisInfoSonLayerInfo));
                JSONObject opInfo = thisInfoSonLayerInfo.getJSONObject(task.getRefOP());
                JSONObject layerInfo = opInfo.getJSONObject(task.getLayer() + "");
                long lastTePFin = thisTime;
                long endTime = thisTime;
                if (conflictEndNew.containsKey("newLastEnd")) {
                    System.out.println("conflictEndNew:");
                    System.out.println(JSON.toJSONString(conflictEndNew));
                    Long lastFin = conflictEndNew.getLong("lastFin");
                    Long endTimeNew = conflictEndNew.getLong("endTime");
                    JSONObject layerMaxData = layerInfo.getJSONObject("layerMaxData");
                    System.out.println("q-layerMaxData:");
                    System.out.println(JSON.toJSONString(layerMaxData));
                    if (layerMaxData.size() == 0) {
                        layerMaxData.put("time",endTimeNew);
                        layerMaxData.put("lastTePFin",lastFin);
                        layerInfo.put("layerMaxData",layerMaxData);
                    } else {
                        Long layerMaxTime = layerMaxData.getLong("time");
                        if (endTimeNew > layerMaxTime) {
                            layerMaxData.put("time",endTimeNew);
                            layerMaxData.put("lastTePFin",lastFin);
                            layerInfo.put("layerMaxData",layerMaxData);
                        } else {
                            Long layerMaxLastTePFin = layerMaxData.getLong("lastTePFin");
                            if (lastFin > layerMaxLastTePFin) {
                                layerMaxData.put("lastTePFin",lastFin);
                                layerInfo.put("layerMaxData",layerMaxData);
                            }
                        }
                    }
                    System.out.println("h-layerMaxData:");
                    System.out.println(JSON.toJSONString(layerMaxData));
                    lastTePFin = layerMaxData.getLong("lastTePFin");
                    endTime = layerMaxData.getLong("time");
                }
                int layerCount = layerInfo.getInteger("layerCount");
                layerCount-=1;
                layerInfo.put("layerCount",layerCount);
                opInfo.put(task.getLayer() + "",layerInfo);
                thisInfoSonLayerInfo.put(task.getRefOP(),opInfo);
                setThisInfoSonLayerInfo(thisInfo,thisInfoSonLayerInfo);
                if (layerCount == 0) {
                    getUpConf(thisInfo,task.getRefOP(),task.getLayer()-1,id_C,actionIdO,endTime,objTaskAll,depAllTime
                            ,random,timeConflictCopy,sho,csSta,randomAll,xbAndSbAll,recordId_OIndexState,storageTaskWhereTime
                            ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState
                            ,recordNoOperation,clearStatus,allImageTeDate,grpUNumAll,lastTePFin);
                }
            }
            dgHandleFollowUpConflict(thisInfo,id_C,objTaskAll,depAllTime,random,timeConflictCopy,sho,csSta
                    ,randomAll,xbAndSbAll,actionIdO,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                    ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                    ,clearStatus,allImageTeDate,conflict,grpUNumAll);
        }
    }

    @SuppressWarnings("unchecked")
    private void getUpConf(JSONObject thisInfo,String id_OP,int layer,String id_C
            ,JSONObject actionIdO,long endTime,JSONObject objTaskAll,JSONObject depAllTime,String random
            ,JSONObject timeConflictCopy,JSONObject sho,int csSta,String randomAll,JSONObject xbAndSbAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime,JSONObject allImageTotalTime
            ,Map<String, Map<String, Map<Long, List<Task>>>> allImageTasks,JSONObject onlyFirstTimeStamp
            ,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState,JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject allImageTeDate,JSONObject grpUNumAll,long lastTePFin){
        JSONObject thisInfoClearOPLayer = getThisInfoClearOPLayer(thisInfo);
        System.out.println("thisInfoClearOPLayer:");
        System.out.println(JSON.toJSONString(thisInfoClearOPLayer));
        JSONObject clearOpInfo = thisInfoClearOPLayer.getJSONObject(id_OP);
        JSONObject clearLayerInfo = clearOpInfo.getJSONObject(layer+"");
        for (String pf : clearLayerInfo.keySet()) {
            JSONObject clearPfInfo = clearLayerInfo.getJSONObject(pf);
            JSONArray oDates = actionIdO.getJSONObject(id_OP).getJSONObject(layer+"")
                    .getJSONObject(pf).getJSONArray("oDates");
            JSONObject dgInfo = oDates.getJSONObject(0);
            String dep = dgInfo.getString("dep");
            String grpB = dgInfo.getString("grpB");
            // 遍历订单列表
            Map<String,Asset> assetMap = new HashMap<>();
            JSONObject grpBGroupIdOJ = new JSONObject();
            System.out.println("up-ShuChu:"+id_OP+"_"+endTime);
//            System.out.println(JSON.toJSONString(sho));
            System.out.println(JSON.toJSONString(clearPfInfo));
            for (int i = 0; i < oDates.size(); i++) {
                JSONObject oDate = oDates.getJSONObject(i);
                // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
                JSONObject firstConflictId_O = new JSONObject();
                JSONObject firstConflictIndex = new JSONObject();
                // 设置为-1代表的是递归的零件
                firstConflictIndex.put("prodState",1);
                firstConflictIndex.put("z",oDate.getString("id_O")+"+"+oDate.getString("index"));
                firstConflictId_O.put(oDate.getString("index"),firstConflictIndex);
                sho.put(oDate.getString("id_O"),firstConflictId_O);
                getGrpB(oDate.getString("grpB"),oDate.getString("dep"), assetMap,id_C
                        ,depAllTime,grpUNumAll,xbAndSbAll,grpBGroupIdOJ,oDate.getString("id_O"),endTime);
            }
            // 获取指定的全局任务信息方法
            JSONObject tasksAndZon = getTasksAndZon(endTime, grpB, dep, id_C,objTaskAll,depAllTime);
            System.out.println(JSON.toJSONString(tasksAndZon));
            // 获取任务信息
            Object[] tasksIs = isTasksNull(tasksAndZon,depAllTime.getJSONObject(dep)
                    .getJSONObject(grpB).getLong(endTime+""));
            List<Task> tasks = (List<Task>) tasksIs[0];
            long zon = (long) tasksIs[1];
            List<Task> conflict = new ArrayList<>();
            Task task = qt.cloneThis(clearPfInfo.getJSONObject(id_OP).getJSONObject(0 + ""), Task.class);
            task.setTePStart(lastTePFin);
            task.setTePFinish(lastTePFin+task.getWntDurTotal());
            conflict.add(task);
            timeZjServiceComprehensive.handleTimeConflictEndNew(0,tasks,conflict,zon,random,timeConflictCopy
                    ,0,1,sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                    ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                    ,depAllTime,clearPfInfo,0,true,endTime);
            updateObjTaskAllSetAllImageTasks(objTaskAll,allImageTasks,allImageTotalTime);
        }
        if (layer - 1 > 0) {
            getUpConf(thisInfo,id_OP,layer-1,id_C,actionIdO,endTime,objTaskAll,depAllTime,random,timeConflictCopy,sho
                    ,csSta,randomAll,xbAndSbAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                    ,allImageTeDate,grpUNumAll,lastTePFin);
        }
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
    @Override
    public ApiResponse removeTime(String id_O, String id_C) {
        return timeZjServiceEmptyInsert.removeTime(id_O,id_C);
    }

    /**
     * 任务实际结束时间处理接口
     * @param dep	部门
     * @param grpB	组别
     * @param currentTime	处理时间
     * @param index	任务列表对应下标
     * @param id_C	公司编号
     * @param taPFinish	任务实际结束时间
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse timeSortFromNew(String dep, String grpB, long currentTime, int index, String id_C, long taPFinish) {
        return timeZjServiceNew.timeSortFromNew(dep,grpB,currentTime,index,id_C,taPFinish);
    }

    /**
     * 删除或者新增aArrange卡片信息
     * @param id_C	公司编号
     * @param object	操作信息
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2023/2/10
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse delOrAddAArrange(String id_C,String dep, JSONObject object) {
        return timeZjServiceNew.delOrAddAArrange(id_C,dep,object);
    }

    /**
     * 根据订单编号与下标获取剩余数量的预计完成时间
     * @param id_O	订单编号
     * @param index	下标
     * @param number	人数
     * @return 返回结果: {@link ApiResponse}
     * @author tang
     * @date 创建时间: 2022/11/6
     * @ver 版本号: 1.0.0
     */
    @Override
    public ApiResponse timeCalculation(String id_O, int index, int number) {
        return timeZjServiceNew.timeCalculation(id_O,index,number);
    }

    /**
     * 获取计算物料时间后的开始时间
     * @param id_O  订单编号
     * @param teStart   开始时间
     * @return  开始时间
     */
    @Override
    public ApiResponse getTeStart(String id_O, Long teStart) {
//        long maxTime = teStart(id_O);
//        // qtySafex
//        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
//        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
//            // 返回为空错误信息
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//        }
//        JSONObject java = order.getCasItemx().getJSONObject("java");
//        JSONArray oDates = java.getJSONArray("oDates");
//        Map<String, HashSet<String>> compMap = new HashMap<>();
//        for (int i = 0; i < oDates.size(); i++) {
//            JSONObject oDate = oDates.getJSONObject(i);
//            int bmdpt = oDate.getInteger("bmdpt");
//            if (bmdpt == 3) {
//                String id_P = oDate.getString("id_P");
//                String id_C = oDate.getString("id_C");
//                HashSet<String> prodSet;
//                if (compMap.containsKey(id_C)) {
//                    prodSet = compMap.get(id_C);
//                } else {
//                    prodSet = new HashSet<>();
//                }
//                prodSet.add(id_P);
//                compMap.put(id_C,prodSet);
//            } else {
//                Long wntDur = oDate.getLong("wntDur");
//                if (bmdpt != 2 && wntDur <= 0) {
//                    // 返回为空错误信息
//                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
//                }
//            }
//        }
//        long maxTime = 0;
//        Map<String,Prod> prodMap = new HashMap<>();
//        for (String id_C : compMap.keySet()) {
//            HashSet<String> prodArr = compMap.get(id_C);
//            for (String id_P : prodArr) {
//                Prod prod;
//                if (prodMap.containsKey(id_P)) {
//                    prod = prodMap.get(id_P);
//                } else {
//                    prod = qt.getMDContent(id_P, "qtySafex", Prod.class);
//                    if (null != prod && null != prod.getQtySafex() && null != prod.getQtySafex().getDouble(id_C)) {
//                        prodMap.put(id_P,prod);
//                    } else {
////                        prod = null;
//                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
//                    }
//                }
//                if (null != prod) {
//                    double hour = prod.getQtySafex().getDouble(id_C);
//                    long time = (long) ((hour*60)*60);
//                    if (time > maxTime) {
//                        maxTime = time;
//                    }
//                }
//            }
//        }
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), teStart+teStart(id_O));
    }

    /**
     * 时间处理方法
     * @param task	当前处理任务信息
     * @param hTeStart	任务最初始开始时间
     * @param grpB	组别
     * @param dep	部门
     * @param id_O	订单编号
     * @param index	订单下标
     * @param isJumpDays	保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
     * @param random	当前唯一编号
     * @param isCanOnlyOnceTimeEmptyInsert	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param teDate	当前处理的任务的所在日期对象
     * @param timeConflictCopy	当前任务所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isSaveAppearEmpty	保存是否出现任务为空异常: = 0 正常操作未出现异常、 = 1 出现拿出任务为空异常镜像|数据库
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param recordId_OIndexState	统一id_O和index存储记录状态信息
     * @param storageTaskWhereTime	存储任务所在日期
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyRefState	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param recordNoOperation	定义存储进入未操作到的地方记录
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    @SuppressWarnings("unchecked")
    public JSONObject timeHandle(Task task,Long hTeStart,String grpB,String dep,String id_O,Integer index
            ,Integer isJumpDays,String random,Integer isCanOnlyOnceTimeEmptyInsert,JSONObject teDate
            ,JSONObject timeConflictCopy,Integer isGetTaskPattern,JSONObject sho,int isSaveAppearEmpty,int csSta
            ,String randomAll,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime,JSONObject allImageTotalTime
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks,JSONObject onlyFirstTimeStamp
            ,JSONObject newestLastCurrentTimestamp,JSONObject onlyRefState,JSONObject recordNoOperation
            ,JSONObject clearStatus,JSONObject thisInfo,JSONObject allImageTeDate,boolean isComprehensiveHandle
            ,JSONObject depAllTime){
        // 创建返回结果对象
        JSONObject result = new JSONObject();
        // 跳天强制停止参数累加
        yiShu.put(randomAll,(yiShu.getInteger(randomAll)+1));
//        System.out.println("newest-外:");
//        System.out.println(JSON.toJSONString(newestLastCurrentTimestamp));

//        System.out.println("获取前:");
//        System.out.println(JSON.toJSONString(allImageTotalTime));
//        System.out.println();
//        System.out.println(JSON.toJSONString(allImageTasks));
//        System.out.println();
//        System.out.println(JSON.toJSONString(objTaskAll));
        // 调用获取任务综合信息方法
        Map<String, Object> jumpDay = getJumpDay(random, grpB, dep,0,0L,isGetTaskPattern
                ,task.getId_C(),xbAndSbAll,objTaskAll,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                ,newestLastCurrentTimestamp,depAllTime);

//        System.out.println(JSON.toJSONString(jumpDay));

        // 获取任务集合
        List<Task> tasks = (List<Task>) jumpDay.get("tasks");
        // 获取任务余剩时间
        long zon = (long) jumpDay.get("zon");
//        System.out.println("查询后的zon:"+zon);

//        System.out.println("输出外sho-q:");
//        System.out.println(sho);
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        JSONObject shoObj = sho.getJSONObject(task.getId_O());
        if (null != shoObj) {
            JSONObject shoObjIndex = shoObj.getJSONObject(task.getIndex().toString());
            if (null != shoObjIndex) {
                Integer prodState = shoObjIndex.getInteger("prodState");
//        System.out.println("输出外sho-h:");
//        System.out.println(sho);
                if (null!=prodState && prodState != -1) {
                    // 调用获取获取统一id_O和index存储记录状态信息方法
                    Integer storage = getStorage(task.getId_O(), task.getIndex(),recordId_OIndexState);
                    // storage == 0 正常状态存储、storage == 1 冲突状态存储、storage == 2 调用时间处理状态存储
                    if (storage == 0) {
                        // 调用写入存储记录状态方法
                        setStorage(2, task.getId_O(), task.getIndex(),recordId_OIndexState);
                    }
                }
            }
//        System.out.println("输出外sho-h2:");
//        System.out.println(sho);
        }

        // 调用时间处理方法2
        JSONObject jsonObject = timeHandleCore(zon, hTeStart, tasks, grpB, dep, id_O, index, task
                , isJumpDays, random, isCanOnlyOnceTimeEmptyInsert, teDate, timeConflictCopy
                , isGetTaskPattern, sho,isSaveAppearEmpty,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                ,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
        // 获取任务最初始开始时间
        hTeStart = jsonObject.getLong("hTeStart");
        result.put("hTeStart",hTeStart);
        result.put("endTime",jsonObject.getLong("endTime"));
        result.put("xFin",jsonObject.getLong("xFin"));
//        System.out.println(JSON.toJSONString(tasks));
        JSONArray tasksArr = new JSONArray();
        for (Task task1 : tasks) {
            tasksArr.add(JSONObject.parseObject(JSON.toJSONString(task1)));
        }
        result.put("tasks",tasksArr);
        return result;
    }

    /**
     * 时间处理核心方法
     * @param zon	任务余剩时间
     * @param hTeStart	任务最初始开始时间
     * @param tasks	任务集合
     * @param grpB	组别
     * @param dep	部门
     * @param id_O	订单编号
     * @param index	订单下标
     * @param task	当前处理任务
     * @param isJumpDays	保存是否是时间处理方法调用的跳天操作：isJumpDays == 0 不是、isJumpDays == 1 是
     * @param random	当前唯一编号
     * @param isCanOnlyOnceTimeEmptyInsert	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
     * @param teDate	当前处理的任务的所在日期对象
     * @param timeConflictCopy	当前任务所在日期对象
     * @param isGetTaskPattern	 = 0 获取数据库任务信息、 = 1 获取镜像任务信息
     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
     * @param isSaveAppearEmpty	保存是否出现任务为空异常: = 0 正常操作未出现异常、 = 1 出现拿出任务为空异常镜像|数据库
     * @param csSta	时间处理的序号是否为1层级
     * @param randomAll	全局唯一编号
     * @param xbAndSbAll	全局上班下班信息
     * @param actionIdO	存储casItemx内订单列表的订单action数据
     * @param objTaskAll	全局任务信息
     * @param recordId_OIndexState	统一id_O和index存储记录状态信息
     * @param storageTaskWhereTime	存储任务所在日期
     * @param allImageTotalTime	全局镜像任务余剩总时间信息
     * @param allImageTasks	全局镜像任务列表信息
     * @param onlyFirstTimeStamp	存储当前唯一编号的第一个当前时间戳
     * @param newestLastCurrentTimestamp	根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
     * @param onlyRefState	存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
     * @param recordNoOperation	定义存储进入未操作到的地方记录
     * @param clearStatus 清理状态信息
     * @param thisInfo 当前处理通用信息存储
     * @param allImageTeDate 镜像任务所在日期
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/6/9
     */
    private JSONObject timeHandleCore(Long zon,Long hTeStart,List<Task> tasks,String grpB,String dep
            ,String id_O,Integer index,Task task,Integer isJumpDays,String random
            ,Integer isCanOnlyOnceTimeEmptyInsert,JSONObject teDate,JSONObject timeConflictCopy
            ,Integer isGetTaskPattern,JSONObject sho,int isSaveAppearEmpty,int csSta,String randomAll
            ,JSONObject xbAndSbAll,JSONObject actionIdO,JSONObject objTaskAll
            ,JSONObject recordId_OIndexState,JSONObject storageTaskWhereTime,JSONObject allImageTotalTime
            ,Map<String,Map<String,Map<Long,List<Task>>>> allImageTasks
            ,JSONObject onlyFirstTimeStamp,JSONObject newestLastCurrentTimestamp
            ,JSONObject onlyRefState,JSONObject recordNoOperation,JSONObject clearStatus,JSONObject thisInfo
            ,JSONObject allImageTeDate,boolean isComprehensiveHandle,JSONObject depAllTime){
//        System.out.println("时间处理核心方法-1:");
//        System.out.println(JSON.toJSONString(tasks));
        // 创建返回结果对象
        JSONObject result = new JSONObject();
//        System.out.println("sho:");
//        System.out.println(JSON.toJSONString(sho));
        // 产品状态，== -1 当前递归产品、== 1 第一个被处理时间的产品、== 2 不是被第一个处理时间的产品
        Integer prodState = sho.getJSONObject(task.getId_O())
                .getJSONObject(task.getIndex().toString()).getInteger("prodState");
        long endTime = 0;
        // 判断任务余剩时间不等于0
        if (zon != 0) {
            // 控制是否跳天参数：isJumpDay == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
            int isJumpDay = 0;
            // 控制是否结束循环参数：isEndLoop == 2 结束循环、isP == 0 | 3 | 4 继续循环
            int isEndLoop;
            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
            int isProblemState = 0;
            boolean isSetEnd = true;
            boolean isSetTimeHandleEnd = true;
            // 判断任务最初始开始时间为0
            if (hTeStart == 0) {
//                System.out.println("进入hTeStart == 0---");
//                System.out.println(JSON.toJSONString(task));
//                System.out.println(JSON.toJSONString(tasks));
                // 遍历任务集合
                for (int i = 0; i < tasks.size(); i++) {
                    // 根据下标获取对比任务信息1
                    Task contrastTaskOne = tasks.get(i);
                    // 判断下标加1小于任务集合长度
                    if ((i + 1) < tasks.size()) {
                        // 根据下标获取对比任务信息2
                        Task contrastTaskTwo = tasks.get(i + 1);
//                        System.out.println("taskzongk:");
//                        System.out.println(JSON.toJSONString(task1));
//                        System.out.println(JSON.toJSONString(task2));
                        System.out.println("emptyInsertTimeInfo-q:");
                        System.out.println(JSON.toJSONString(tasks));
                        // 调用计算空插时间方法
                        JSONObject emptyInsertTimeInfo =
                                timeZjServiceEmptyInsert.calculationEmptyInsertTime( task, contrastTaskOne
                                , contrastTaskTwo, tasks, i, zon,0,random,teDate,dep,grpB
                                ,prodState,storageTaskWhereTime,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                        ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                        System.out.println("emptyInsertTimeInfo-h:");
                        System.out.println(JSON.toJSONString(tasks));
                        // 获取存储问题状态参数
                        isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                        if (isEndLoop == 2) {
                            // 获取控制是否跳天参数
                            isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                            // 获取任务余剩时间
                            zon = emptyInsertTimeInfo.getLong("zon");
                            hTeStart = emptyInsertTimeInfo.getLong("tePFinish");
                            endTime = emptyInsertTimeInfo.getLong("endTime");
                            break;
                        } else if (isEndLoop == 4) {
                            // 获取任务余剩时间
                            zon = emptyInsertTimeInfo.getLong("zon");
                        }
                    }
                }
            }
            else {
                // 存储任务冲突信息参数：isTaskConflictInfo == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
                int isTaskConflictInfo = 0;
//                System.out.println("进入hTeStart != 0--- isTaskConflictInfo:"+isTaskConflictInfo);
                // 遍历任务集合
                for (int i = 0; i < tasks.size(); i++) {
                    // 根据下标获取对比任务信息1
                    Task contrastTaskOne = tasks.get(i);
                    // 判断下标加1小于任务集合长度
                    if ((i + 1) < tasks.size()) {
                        // 根据下标获取对比任务信息2
                        Task contrastTaskTwo = tasks.get(i + 1);
//                        System.out.println("task-zon:");
//                        System.out.println(JSON.toJSONString(task1));
//                        System.out.println(JSON.toJSONString(task2));
                        // 判断进入条件
                        // isJumpDays : 保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
                        // isTaskConflictInfo : 存储任务冲突信息参数：isTaskConflictInfo == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
                        // isCanOnlyOnceTimeEmptyInsert : 控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
                        if (isJumpDays == 0 && isTaskConflictInfo == 0 || isCanOnlyOnceTimeEmptyInsert == 1) {
//                            System.out.println("进入if");
                            isCanOnlyOnceTimeEmptyInsert = 0;
                            // 判断当前任务的开始时间大于等于对比任务信息1的结束时间
                            if (task.getTePStart() >= contrastTaskOne.getTePFinish()) {
                                // 定义存储调用[计算空插时间方法]的结果
                                JSONObject emptyInsertTimeInfo;
                                // 判断当前任务的结束时间小于对比任务信息2的开始时间
                                if (task.getTePFinish() < contrastTaskTwo.getTePStart()) {
                                    System.out.println(JSON.toJSONString(task));
                                    System.out.println(JSON.toJSONString(contrastTaskOne));
                                    System.out.println(JSON.toJSONString(contrastTaskTwo));
                                    System.out.println("进入这里-1-1-q:");
                                    // 调用计算空插时间方法并赋值
                                    emptyInsertTimeInfo = timeZjServiceEmptyInsert.calculationEmptyInsertTime(
                                            task, contrastTaskOne, contrastTaskTwo, tasks, i, zon
                                            , 1,random,teDate
                                            ,dep,grpB,prodState,storageTaskWhereTime,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                                    hTeStart = emptyInsertTimeInfo.getLong("tePFinish");
                                    endTime = emptyInsertTimeInfo.getLong("endTime");
                                    System.out.println("进入这里-1-1-h:"+hTeStart);
                                    System.out.println(JSON.toJSONString(emptyInsertTimeInfo));
                                } else if (task.getTePStart() <= contrastTaskTwo.getTePStart()
                                        && task.getPriority() >= contrastTaskTwo.getPriority()) {
                                    System.out.println("进入这里-1-2-q:");
//                                    System.out.println(JSON.toJSONString(tasks));
                                    // 调用计算空插时间方法并赋值
                                    emptyInsertTimeInfo = timeZjServiceEmptyInsert.calculationEmptyInsertTime(
                                            task, contrastTaskOne, contrastTaskTwo, tasks, i, zon
                                            , 2,random,teDate
                                            ,dep,grpB,prodState,storageTaskWhereTime,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                                    endTime = emptyInsertTimeInfo.getLong("endTime");
//                                    System.out.println("进入这里-1-2-h:");
//                                    System.out.println(JSON.toJSONString(tasks));
//                                    System.out.println(JSON.toJSONString(emptyInsertTimeInfo));
                                } else if (task.getTePStart() < contrastTaskOne.getTePFinish()
                                        && contrastTaskOne.getPriority() != -1) {
//                                    System.out.println("进入这里-1-3");
                                    isTaskConflictInfo = 3;
                                    break;
                                } else
                                {
                                    // 判断当前任务的优先级小于对比任务信息2的优先级
                                    if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                                && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                            isTaskConflictInfo = 3;
                                            System.out.println("进入-冲突-最开始-这里-1");
//                                            System.out.println("进入-冲突-最开始-sho");
//                                            System.out.println(JSON.toJSONString(sho));
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    , contrastTaskOne, contrastTaskTwo, zon, tasks, i
                                                    , random, grpB, dep, teDate, timeConflictCopy
                                                    , isGetTaskPattern, 0
                                                    , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                                    ,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
//                                            System.out.println("处理完成:");
                                            isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
//                                            isSetEnd = true;
//                                            System.out.println(JSON.toJSONString(allImageTasks));
                                            hTeStart = timeConflictInfo.getLong("tePFinish");
                                            System.out.println("进入-冲突-最开始-这里-1-h:"+isSetEnd+" - hTeStart:"+hTeStart);
                                            endTime = timeConflictInfo.getLong("endTime");
//                                            System.out.println("进入-冲突-最开始-这里-1");
                                            break;
                                        } else
                                        if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                                && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-2");
//                                            System.out.println(JSON.toJSONString(task));
//                                            System.out.println(JSON.toJSONString(contrastTaskTwo));
//                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-2",recordNoOperation);
                                            isTaskConflictInfo = 4;
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    , contrastTaskOne, contrastTaskTwo, zon, tasks, i
                                                    , random, grpB, dep, teDate, timeConflictCopy
                                                    , isGetTaskPattern, 0
                                                    , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                                    ,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
//                                            System.out.println("处理完成:");
                                            isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
//                                            isSetEnd = true;
//                                            System.out.println(JSON.toJSONString(allImageTasks));
                                            hTeStart = timeConflictInfo.getLong("tePFinish");
                                            System.out.println("进入-冲突-最开始-这里-2-h:"+isSetEnd+" - hTeStart:"+hTeStart);
                                            endTime = timeConflictInfo.getLong("endTime");
                                            break;
                                        } else if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                                && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
//                                            System.out.println(JSON.toJSONString(task));
//                                            System.out.println(JSON.toJSONString(contrastTaskOne));
//                                            System.out.println(JSON.toJSONString(contrastTaskTwo));
                                            System.out.println("进入-冲突-最开始-这里-3");
//                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-3",recordNoOperation);
                                            isTaskConflictInfo = 5;
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    , contrastTaskOne, contrastTaskTwo, zon, tasks, i
                                                    , random, grpB, dep, teDate, timeConflictCopy
                                                    , isGetTaskPattern, 0
                                                    , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                                    ,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                    ,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
//                                            System.out.println("处理完成:");
                                            isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
//                                            isSetEnd = true;
//                                            System.out.println(JSON.toJSONString(allImageTasks));
                                            hTeStart = timeConflictInfo.getLong("tePFinish");
                                            System.out.println("进入-冲突-最开始-这里-2-h:"+isSetEnd+" - hTeStart:"+hTeStart);
                                            endTime = timeConflictInfo.getLong("endTime");
                                            break;
                                        }
                                    } else if (task.getPriority() < contrastTaskOne.getPriority()){
                                        // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                        if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                                && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-1--1");
                                            isTaskConflictInfo = 33;
                                            // 调用处理时间冲突方法
                                            JSONObject timeConflictInfo
                                                    = timeZjServiceTimeConflict.handleTimeConflict(task
                                                    ,contrastTaskOne,contrastTaskTwo,zon,tasks,i,random
                                                    ,grpB,dep,teDate,timeConflictCopy,isGetTaskPattern
                                                    ,0,sho,csSta,randomAll,xbAndSbAll
                                                    ,actionIdO
                                                    ,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                    ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                    ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                    ,depAllTime);
                                            // 获取任务余剩时间
                                            zon = timeConflictInfo.getLong("zon");
                                            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                            isProblemState = timeConflictInfo.getInteger("isProblemState");
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 2;
                                            endTime = timeConflictInfo.getLong("endTime");
                                            System.out.println("进入-冲突-最开始-这里-1--1-h:");
                                            break;
                                        } else
                                        if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                                && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-2--1");
                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-2--1",recordNoOperation);
                                            isTaskConflictInfo = 44;
                                            break;
                                        } else if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                                && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                            System.out.println("进入-冲突-最开始-这里-3--1");
                                            addRecordGetIntoOperation(randomAll,"进入-冲突-最开始-这里-3--1",recordNoOperation);
                                            isTaskConflictInfo = 55;
                                            break;
                                        }
                                    }
//                                    System.out.println("弹回---");
                                    isCanOnlyOnceTimeEmptyInsert = 1;
                                    continue;
                                }
                                // 获取控制是否结束循环参数
                                isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                if (isEndLoop == 2) {
                                    // 获取控制是否跳天参数
                                    isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                    break;
                                } else if (isEndLoop == 4) {
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                    isTaskConflictInfo = 1;
                                }
                                else {
                                    isTaskConflictInfo = 1;
                                }
                            } else
                            {
//                                System.out.println("进入这里-2-1");
                                // 判断当前任务优先级小于对比任务信息1的优先级
                                if (task.getPriority() < contrastTaskOne.getPriority()) {
                                    // 判断当前任务的开始时间大于等于对比任务信息1的开始时间，并且当前任务的开始时间小于对比任务信息1的结束时间
                                    if (task.getTePStart() >= contrastTaskOne.getTePStart()
                                            && task.getTePStart() < contrastTaskOne.getTePFinish()) {
                                        isTaskConflictInfo = 3;
                                        System.out.println("进入-冲突-中间-这里-1");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-中间-这里-1",recordNoOperation);
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        break;
                                    } else
                                    if (contrastTaskOne.getTePStart() >= task.getTePStart()
                                            && contrastTaskOne.getTePFinish() <= task.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-2");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-中间-这里-2",recordNoOperation);
                                        isTaskConflictInfo = 4;
                                        break;
                                    } else if (task.getTePFinish() > contrastTaskOne.getTePStart()
                                            && task.getTePFinish() < contrastTaskOne.getTePFinish()) {
                                        System.out.println("进入-冲突-中间-这里-3");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-中间-这里-3",recordNoOperation);
                                        isTaskConflictInfo = 5;
                                        break;
                                    } else {
                                        System.out.println("进入输出ji---1-q:");

                                        // 调用计算空插时间方法
                                        JSONObject emptyInsertTimeInfo
                                                = timeZjServiceEmptyInsert.calculationEmptyInsertTime( task
                                                , contrastTaskOne, contrastTaskTwo
                                                , tasks, i, zon,0,random,teDate,dep,grpB
                                                ,prodState,storageTaskWhereTime,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
                                        System.out.println("进入输出ji---1-h:");
                                        // 获取控制是否结束循环参数
                                        isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                        if (isEndLoop == 2) {
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                            endTime = emptyInsertTimeInfo.getLong("endTime");
                                            break;
                                        } else if (isEndLoop == 4) {
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                        }
                                    }
                                } else if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                    // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                    if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                            && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                        isTaskConflictInfo = 3;
                                        System.out.println("进入-冲突-最后面-这里-1");
                                        addRecordGetIntoOperation(randomAll,"进入-冲突-最后面-这里-1",recordNoOperation);
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        break;
                                    } else
                                    if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                            && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                        isTaskConflictInfo = 4;
                                        System.out.println("进入-冲突-最后面-这里-2-q:");
                                        // 调用处理时间冲突方法
                                        JSONObject timeConflictInfo
                                                = timeZjServiceTimeConflict.handleTimeConflict(task
                                                , contrastTaskOne
                                                , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                                , timeConflictCopy, isGetTaskPattern, 0
                                                , sho,csSta,randomAll,xbAndSbAll,actionIdO
                                                ,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                ,depAllTime);
                                        // 获取任务余剩时间
                                        zon = timeConflictInfo.getLong("zon");
                                        // 获取存储问题状态参数
                                        isProblemState = timeConflictInfo.getInteger("isProblemState");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        endTime = timeConflictInfo.getLong("endTime");
                                        System.out.println("进入-冲突-最后面-这里-2-h:");
                                        break;
                                    } else
                                    if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                            && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                        System.out.println("进入-冲突-最后面-这里-3-q:");
//                                        addRecordGetIntoOperation(randomAll,"进入-冲突-最后面-这里-3",recordNoOperation);
                                        isTaskConflictInfo = 5;
                                        // 调用处理时间冲突方法
                                        JSONObject timeConflictInfo
                                                = timeZjServiceTimeConflict.handleTimeConflict(task
                                                , contrastTaskOne
                                                , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                                , timeConflictCopy, isGetTaskPattern, 0
                                                , sho,csSta,randomAll,xbAndSbAll,actionIdO
                                                ,objTaskAll,recordId_OIndexState,storageTaskWhereTime
                                                ,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                                ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                                ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle
                                                ,depAllTime);
                                        // 获取任务余剩时间
                                        zon = timeConflictInfo.getLong("zon");
                                        // 获取存储问题状态参数
                                        isProblemState = timeConflictInfo.getInteger("isProblemState");
                                        // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                        isJumpDay = 2;
                                        endTime = timeConflictInfo.getLong("endTime");
                                        System.out.println("进入-冲突-最后面-这里-3-h:");
                                        break;
                                    } else {
                                        System.out.println(JSON.toJSONString(task));
                                        System.out.println(JSON.toJSONString(contrastTaskOne));
                                        System.out.println(JSON.toJSONString(contrastTaskTwo));
                                        System.out.println("进入输出ji---1-2-q:");

                                        // 调用计算空插时间方法
                                        JSONObject emptyInsertTimeInfo
                                                = timeZjServiceEmptyInsert.calculationEmptyInsertTime(task
                                                , contrastTaskOne
                                                , contrastTaskTwo, tasks, i, zon,0
                                                ,random,teDate,dep,grpB,prodState,storageTaskWhereTime
                                                ,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                                ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);

                                        System.out.println("进入输出ji---1-2-h:");
                                        // 获取控制是否结束循环参数
                                        isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                        // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                        if (isEndLoop == 2) {
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                            endTime = emptyInsertTimeInfo.getLong("endTime");
                                            break;
                                        } else if (isEndLoop == 4) {
                                            // 获取任务余剩时间
                                            zon = emptyInsertTimeInfo.getLong("zon");
                                        }
                                    }
                                } else {
//                                    System.out.println("--优先级跳过--");
                                    // 获取余剩时间（对比任务2的开始时间-对比任务1的结束时间）
                                    long remainingTime = contrastTaskTwo.getTePStart()
                                            - contrastTaskOne.getTePFinish();
                                    // 判断余剩时间大于0
                                    if (remainingTime > 0) {
                                        // 获取时间差（余剩时间-当前任务总时间）
                                        long timeDifference = remainingTime - task.getWntDurTotal();
//                                        System.out.println("优先级:");
//                                        System.out.println(JSON.toJSONString(task));
//                                        System.out.println(JSON.toJSONString(contrastTaskOne));
//                                        System.out.println(JSON.toJSONString(contrastTaskTwo));
                                        // 判断时间差大于等于0
                                        if (timeDifference >= 0) {
                                            System.out.println("--优先级跳过--1");
                                            Task contrastTaskThree = TaskObj.getTaskX(
                                                    contrastTaskOne.getTePFinish()
                                                    ,(contrastTaskOne.getTePFinish()+task.getWntDurTotal())
                                                    ,task.getWntDurTotal(),task);
//                                            System.out.println(JSON.toJSONString(contrastTaskThree));
                                            // 指定任务集合下标为任务下标+1添加任务信息
                                            tasks.add(i+1,contrastTaskThree);
                                            endTime = tasks.get(0).getTePStart();
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getDateIndex(),getTeS(random,grpB,dep
                                                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    ,prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , task.getWntDurTotal(),allImageTeDate,isGetTaskPattern,endTime);
                                            result.put("xFin",contrastTaskThree.getTePFinish());
                                            // 任务余剩时间累减
                                            zon -= task.getWntDurTotal();
                                            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                            isJumpDay = 1;
                                            hTeStart = contrastTaskThree.getTePFinish();
                                            break;
                                        } else {
                                            System.out.println("--优先级跳过--2");
                                            // 获取时间差2（当前任务总时间-余剩时间）
                                            long timeDifferenceNew = task.getWntDurTotal() - remainingTime;
                                            Task contrastTaskThree = TaskObj.getTaskX(contrastTaskOne.getTePFinish()
                                                    ,(contrastTaskOne.getTePFinish()+remainingTime),remainingTime,task);
//                                            System.out.println(JSON.toJSONString(contrastTaskThree));
                                            // 指定任务集合下标为任务下标+1添加任务信息
                                            tasks.add(i+1,contrastTaskThree);
                                            endTime = tasks.get(0).getTePStart();
                                            // 添加当前处理的任务的所在日期对象状态
                                            teDate.put(getTeS(random,grpB,dep,onlyFirstTimeStamp
                                                    ,newestLastCurrentTimestamp)+"",0);
                                            // 调用判断产品状态再调用写入任务所在日期方法的方法
                                            putTeDate(task.getId_O(), task.getDateIndex(),getTeS(random,grpB,dep
                                                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    ,prodState,storageTaskWhereTime);
                                            setAllImageTeDateAndDate(task.getId_O(), task.getDateIndex()
                                                    ,getTeS(random,grpB,dep,onlyFirstTimeStamp,newestLastCurrentTimestamp)
                                                    , remainingTime,allImageTeDate,isGetTaskPattern,endTime);
                                            // 更新任务总时间
                                            task.setWntDurTotal(timeDifferenceNew);
                                            // 更新任务开始时间
                                            task.setTePStart(contrastTaskOne.getTePStart());
                                            // 更新任务结束时间
                                            task.setTePFinish(contrastTaskOne.getTePFinish());
                                            // 任务余剩时间累减
                                            zon -= remainingTime;
                                            hTeStart = contrastTaskThree.getTePFinish();
                                        }
                                    }
                                }
                            }
                        } else
                        {
//                            System.out.println("进入else");
                            // 判断当前任务优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断当前任务开始时间小于对比任务2的开始时间，并且当前任务的开始时间小于对比任务2的结束时间
                                if (task.getTePStart() < contrastTaskTwo.getTePStart()
                                        && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    isTaskConflictInfo = 3;
//                                    System.out.println("进入-冲突-新最开始-这里-1");
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo
                                            = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne
                                            , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                            , timeConflictCopy, isGetTaskPattern, 1, sho
                                            ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                            ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                            ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                                            ,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 获取存储问题状态参数
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    hTeStart = timeConflictInfo.getLong("tePFinish");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
//                                    System.out.println("进入-冲突-新最开始-这里-1-h:"+hTeStart);
                                    isSetTimeHandleEnd = false;
                                    break;
                                } else {
                                    System.out.println("进入else-1-q:");

                                    // 调用计算空插时间方法
                                    JSONObject emptyInsertTimeInfo
                                            = timeZjServiceEmptyInsert.calculationEmptyInsertTime( task, contrastTaskOne
                                            , contrastTaskTwo, tasks, i, zon,0,random,teDate,dep,grpB
                                            ,prodState,storageTaskWhereTime,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                            ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
//                                    System.out.println("进入else-1-h:");
                                    // 获取控制是否结束循环参数
                                    isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                    // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                    if (isEndLoop == 2) {
                                        // 获取控制是否跳天参数
                                        isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                        // 获取任务余剩时间
                                        zon = emptyInsertTimeInfo.getLong("zon");
                                        endTime = emptyInsertTimeInfo.getLong("endTime");
                                        break;
                                    } else if (isEndLoop == 4) {
                                        // 获取任务余剩时间
                                        zon = emptyInsertTimeInfo.getLong("zon");
                                    }
                                }
                            } else if (task.getPriority() < contrastTaskOne.getPriority()){
//                                System.out.println(JSON.toJSONString(task));
//                                System.out.println(JSON.toJSONString(contrastTaskOne));
//                                System.out.println(JSON.toJSONString(contrastTaskTwo));
                                addRecordGetIntoOperation(randomAll,"进入-冲突-新最开始-这里-1--1",recordNoOperation);
                                System.out.println("进入-冲突-新最开始-这里-1--1");
                                isTaskConflictInfo = 4;
//                                    System.out.println("进入-冲突-新最开始-这里-1");
                                // 调用处理时间冲突方法
                                JSONObject timeConflictInfo
                                        = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne
                                        , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                        , timeConflictCopy, isGetTaskPattern, 1, sho
                                        ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                        ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                        ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                                        ,isComprehensiveHandle,depAllTime);
                                // 获取任务余剩时间
                                zon = timeConflictInfo.getLong("zon");
                                // 获取存储问题状态参数
                                isProblemState = timeConflictInfo.getInteger("isProblemState");
                                hTeStart = timeConflictInfo.getLong("tePFinish");
                                // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                isJumpDay = 2;
                                endTime = timeConflictInfo.getLong("endTime");
//                                    System.out.println("进入-冲突-新最开始-这里-1-h:"+hTeStart);
                                isSetTimeHandleEnd = false;
                                break;
                            } else {
//                                System.out.println("进入else-2-q:");
//                                System.out.println(JSON.toJSONString(tasks));
                                // 调用计算空插时间方法
                                JSONObject emptyInsertTimeInfo
                                        = timeZjServiceEmptyInsert.calculationEmptyInsertTime( task, contrastTaskOne, contrastTaskTwo
                                        , tasks, i, zon,0,random,teDate,dep,grpB,prodState
                                        ,storageTaskWhereTime,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                        ,isGetTaskPattern,allImageTeDate,onlyRefState,thisInfo);
//                                System.out.println("进入else-2-h:");
//                                System.out.println(JSON.toJSONString(tasks));
                                // 获取控制是否结束循环参数
                                isEndLoop = emptyInsertTimeInfo.getInteger("isEndLoop");
                                // 控制是否结束循环参数：isP == 2 结束循环、isP == 0 | 3 | 4 继续循环
                                if (isEndLoop == 2) {
                                    // 获取控制是否跳天参数
                                    isJumpDay = emptyInsertTimeInfo.getInteger("isJumpDay");
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                    hTeStart = emptyInsertTimeInfo.getLong("tePFinish");
                                    endTime = emptyInsertTimeInfo.getLong("endTime");
                                    break;
                                } else if (isEndLoop == 4) {
                                    // 获取任务余剩时间
                                    zon = emptyInsertTimeInfo.getLong("zon");
                                }
                            }
                        }
                    }
                }
                // 判断冲突信息
                if (isTaskConflictInfo >= 3) {
                    System.out.println("出现时间冲突!isTaskConflictInfo:"+isTaskConflictInfo);
                    System.out.println(JSON.toJSONString(allImageTasks));
                }
            }
//            System.out.println("进入收尾:endTime-q:"+endTime);
//            System.out.println();
            if (isSetTimeHandleEnd) {
                // 调用处理[时间处理方法2]结束操作的方法
                JSONObject timeHandleEndInfo = timeZjServiceComprehensive.timeHandleEnd(isJumpDay, isGetTaskPattern, tasks, grpB, dep, random, zon
                        , isCanOnlyOnceTimeEmptyInsert, hTeStart, task, id_O, index, teDate, timeConflictCopy
                        ,sho,isProblemState,isSaveAppearEmpty,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                        ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus,thisInfo
                        ,allImageTeDate,isSetEnd,endTime,depAllTime);
                hTeStart = timeHandleEndInfo.getLong("hTeStart");
                endTime = timeHandleEndInfo.getLong("endTime");
            } else {
                System.out.println("新的-最后操作跳过-:");
            }
//            System.out.println("返回后:"+hTeStart);
//            System.out.println("进入收尾:endTime-h:"+endTime);
        }
        else {
            // 存储任务冲突信息参数：isTaskConflictInfo == 0 控制只进入一次时间空插全流程处理-可以进入、isL > 0 记录冲突信息，不可进入-控制只进入一次时间空插全流程处理
            int isTaskConflictInfo = 0;
            // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
            int isJumpDay = 0;
            // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
            int isProblemState = 0;
            boolean isSetEnd = true;
            // 遍历任务集合
            for (int i = 0; i < tasks.size(); i++) {
                // 根据下标获取对比任务信息1
                Task contrastTaskOne = tasks.get(i);
                // 判断下标加1小于任务集合长度
                if ((i + 1) < tasks.size()) {
                    // 根据下标获取对比任务信息2
                    Task contrastTaskTwo = tasks.get(i + 1);
//                    System.out.println("task-x-zon:");
//                    System.out.println(JSON.toJSONString(task));
//                    System.out.println(JSON.toJSONString(task1));
//                    System.out.println(JSON.toJSONString(task2));
                    // 判断进入条件
                    // isT : 保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
                    // isK : 控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
                    if (isJumpDays == 0 || isCanOnlyOnceTimeEmptyInsert == 1) {
                        isCanOnlyOnceTimeEmptyInsert = 0;
                        // 判断当前任务的开始时间大于等于对比任务1的结束时间
                        if (task.getTePStart() >= contrastTaskOne.getTePFinish()) {
                            // 判断当前任务的优先级小于对比任务2的优先级
                            if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart()
                                        && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    isTaskConflictInfo = 3;
                                    System.out.println("进入-x-冲突-最开始-这里-1-q");
//                                    System.out.println(JSON.toJSONString(onlyRefState));
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo
                                            = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne
                                            , contrastTaskTwo, zon, tasks, i, random, grpB, dep, teDate
                                            , timeConflictCopy, isGetTaskPattern, 0, sho
                                            ,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                                            ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                            ,allImageTasks,onlyFirstTimeStamp,newestLastCurrentTimestamp
                                            ,onlyRefState,recordNoOperation,clearStatus,thisInfo,allImageTeDate
                                            ,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
                                    long hTeStartNew = timeConflictInfo.getLong("tePFinish");
                                    System.out.println("进入-x-冲突-最开始-这里-1-h:旧:"+hTeStart+" - 新:"+hTeStartNew);
                                    hTeStart = hTeStartNew;
//                                    System.out.println(JSON.toJSONString(timeConflictInfo));
                                    break;
                                } else
                                if (contrastTaskTwo.getTePStart() >= task.getTePStart()
                                        && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-2");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-2",recordNoOperation);
                                    isTaskConflictInfo = 4;
                                    break;
                                } else if (task.getTePFinish() > contrastTaskTwo.getTePStart()
                                        && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-3",recordNoOperation);
                                    isTaskConflictInfo = 5;
                                    break;
                                }
                            } else if (task.getPriority() < contrastTaskOne.getPriority()){
                                // 判断当前任务的开始时间大于等于对比任务信息2的开始时间，并且当前任务的开始时间小于对比任务信息2的结束时间
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart() && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-1--1");
                                    isTaskConflictInfo = 33;
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo = timeZjServiceTimeConflict.handleTimeConflict(task
                                            , contrastTaskOne, contrastTaskTwo, zon, tasks, i, random, grpB
                                            , dep, teDate, timeConflictCopy, isGetTaskPattern, 0
                                            , sho,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll,recordId_OIndexState
                                            ,storageTaskWhereTime,allImageTotalTime,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                                            ,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
                                    System.out.println("进入-x-冲突-最开始-这里-1--1-1");
                                    break;
                                } else
                                if (contrastTaskTwo.getTePStart() >= task.getTePStart() && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-2--1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-2--1",recordNoOperation);
                                    isTaskConflictInfo = 44;
                                    break;
                                } else if (task.getTePFinish() > contrastTaskTwo.getTePStart() && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最开始-这里-3--1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最开始-这里-3--1",recordNoOperation);
                                    isTaskConflictInfo = 55;
                                    break;
                                }
                            }
//                            System.out.println("弹回---");
                            isCanOnlyOnceTimeEmptyInsert = 1;
                        } else {
//                            System.out.println("进入这里-2-2");
                            // 判断当前任务优先级小于对比任务1的优先级
                            if (task.getPriority() < contrastTaskOne.getPriority()) {
                                // 判断当前任务的开始时间大于等于对比任务信息1的开始时间，并且当前任务的开始时间小于对比任务信息1的结束时间
                                if (task.getTePStart() >= contrastTaskOne.getTePStart() && task.getTePStart() < contrastTaskOne.getTePFinish()) {
                                    isTaskConflictInfo = 3;
                                    System.out.println("进入-x-冲突-中间-这里-1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-中间-这里-1",recordNoOperation);
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    break;
                                } else
                                if (contrastTaskOne.getTePStart() >= task.getTePStart() && contrastTaskOne.getTePFinish() <= task.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-2");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-中间-这里-2",recordNoOperation);
                                    isTaskConflictInfo = 4;
                                    break;
                                } else if (task.getTePFinish() > contrastTaskOne.getTePStart() && task.getTePFinish() < contrastTaskOne.getTePFinish()) {
                                    System.out.println("进入-x-冲突-中间-这里-3");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-中间-这里-3",recordNoOperation);
                                    isTaskConflictInfo = 5;
                                    break;
                                }
                            } else if (task.getPriority() < contrastTaskTwo.getPriority()) {
                                if (task.getTePStart() >= contrastTaskTwo.getTePStart() && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                    isTaskConflictInfo = 3;
                                    System.out.println("进入-x-冲突-最后面-这里-1");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最后面-这里-1",recordNoOperation);
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    break;
                                } else
                                if (contrastTaskTwo.getTePStart() >= task.getTePStart() && contrastTaskTwo.getTePFinish() <= task.getTePFinish()) {
                                    isTaskConflictInfo = 4;
                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    // 调用处理时间冲突方法
                                    JSONObject timeConflictInfo = timeZjServiceTimeConflict.handleTimeConflict(task
                                            , contrastTaskOne, contrastTaskTwo, zon, tasks, i, random, grpB
                                            , dep, teDate, timeConflictCopy, isGetTaskPattern, 0
                                            , sho,csSta,randomAll,xbAndSbAll,actionIdO
                                            ,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime
                                            ,allImageTasks,onlyFirstTimeStamp
                                            ,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                                            ,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                    // 获取任务余剩时间
                                    zon = timeConflictInfo.getLong("zon");
                                    // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                    isProblemState = timeConflictInfo.getInteger("isProblemState");
                                    // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                    isJumpDay = 2;
                                    endTime = timeConflictInfo.getLong("endTime");
                                    System.out.println("进入-x-冲突-最后面-这里-2");
                                    break;
                                } else if (task.getTePFinish() > contrastTaskTwo.getTePStart() && task.getTePFinish() < contrastTaskTwo.getTePFinish()) {
                                    System.out.println("进入-x-冲突-最后面-这里-3");
                                    addRecordGetIntoOperation(randomAll,"进入-x-冲突-最后面-这里-3",recordNoOperation);
                                    isTaskConflictInfo = 5;
                                    break;
                                }
                            } else {
                                System.out.println("--x-优先级跳过--");
                            }
                        }
                    } else {
                        // 判断当前任务优先级小于对比任务2的优先级
                        if (task.getPriority() < contrastTaskTwo.getPriority()) {
                            // 判断当前任务开始时间小于对比任务2的开始时间，并且当前任务的开始时间小于对比任务2的结束时间
                            if (task.getTePStart() < contrastTaskTwo.getTePStart() && task.getTePStart() < contrastTaskTwo.getTePFinish()) {
                                isTaskConflictInfo = 3;
                                System.out.println("进入-x-冲突-新最开始-这里-1");
                                // 调用处理时间冲突方法
                                JSONObject timeConflictInfo = timeZjServiceTimeConflict.handleTimeConflict(task, contrastTaskOne, contrastTaskTwo, zon, tasks, i, random, grpB
                                        , dep, teDate, timeConflictCopy, isGetTaskPattern, 1, sho,csSta,randomAll,xbAndSbAll,actionIdO
                                        ,objTaskAll,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                                        ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation
                                        ,clearStatus,thisInfo,allImageTeDate,isComprehensiveHandle,depAllTime);
                                // 获取任务余剩时间
                                zon = timeConflictInfo.getLong("zon");
                                // 存储问题状态参数: isProblemState = 0 正常、isPd = 1 订单编号为空、isPd = 2 主生产部件
                                isProblemState = timeConflictInfo.getInteger("isProblemState");
                                // 控制是否跳天参数：isR == 0 继续跳天操作、isR == 1 | 2 停止跳天操作
                                isJumpDay = 2;
                                isSetEnd = timeConflictInfo.getBoolean("isSetEnd");
                                endTime = timeConflictInfo.getLong("endTime");
                                System.out.println("进入-x-冲突-新最开始-这里-1");
                                break;
                            }
                        } else if (task.getPriority() < contrastTaskOne.getPriority()){
                            System.out.println("进入-x-冲突-新最开始-这里-1--1");
                            addRecordGetIntoOperation(randomAll,"进入-x-冲突-新最开始-这里-1--1",recordNoOperation);
                        }
                    }
                }
            }
//            if (isTaskConflictInfo >= 3) {
//                System.out.println("-x-出现时间冲突!isTaskConflictInfo:"+isTaskConflictInfo);
//            }
            // 调用处理[时间处理方法2]结束操作的方法
            JSONObject timeHandleEndInfo = timeZjServiceComprehensive.timeHandleEnd(isJumpDay, isGetTaskPattern
                    , tasks, grpB, dep, random, zon
                    , isCanOnlyOnceTimeEmptyInsert, hTeStart, task, id_O, index, teDate, timeConflictCopy
                    ,sho,isProblemState,isSaveAppearEmpty,csSta,randomAll,xbAndSbAll,actionIdO,objTaskAll
                    ,recordId_OIndexState,storageTaskWhereTime,allImageTotalTime,allImageTasks
                    ,onlyFirstTimeStamp,newestLastCurrentTimestamp,onlyRefState,recordNoOperation,clearStatus
                    ,thisInfo,allImageTeDate,isSetEnd,endTime,depAllTime);
            hTeStart = timeHandleEndInfo.getLong("hTeStart");
            endTime = timeHandleEndInfo.getLong("endTime");
        }

        result.put("hTeStart",hTeStart);
        result.put("endTime",endTime);
        return result;
    }

    public void getGrpB(String grpB,String dep, Map<String,Asset> assetMap, String id_C, JSONObject depAllTime
            , JSONObject grpUNumAll, JSONObject xbAndSbAll, JSONObject grpBGroupIdOJ, String id_OInside
            ,long teS){
        // 创建存储部门字典
        JSONObject depMap = new JSONObject();
//        // 根据组别获取组别信息
//        JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
//        // 获取组别的部门
//        String dep = grpBGroupInfo.getString("dep");
        Asset assetDep;
        if (assetMap.containsKey(dep)) {
            assetDep = assetMap.get(dep);
        } else {
            assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
            assetMap.put(dep,assetDep);
        }
        JSONObject chkGrpB;
        if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin") || null == assetDep.getChkin().getJSONObject("objChkin").getJSONObject(grpB)) {
            chkGrpB = TaskObj.getChkinJava();
        } else {
            JSONObject chkin = assetDep.getChkin();
            JSONObject objChkin = chkin.getJSONObject("objChkin");
            chkGrpB = objChkin.getJSONObject(grpB);
        }
        JSONArray arrTime = null;
        Integer teDur = 0;
        if (chkGrpB.containsKey("whWeek")) {
            System.out.println("teS-2:"+teS);
            boolean isWeek = true;
            if (chkGrpB.containsKey("whDate")) {
                JSONObject whDate = chkGrpB.getJSONObject("whDate");
                if (whDate.containsKey(teS + "")) {
                    isWeek = false;
                    JSONObject wData = whDate.getJSONObject(teS + "");
                    boolean isWntWork = wData.containsKey("wntWork");
                    boolean isWntOver = wData.containsKey("wntOver");
                    if (wData.getInteger("workType") == 1) {
                        if (wData.containsKey("arrTime") && wData.getJSONArray("arrTime").size() > 0) {
                            arrTime = wData.getJSONArray("arrTime");
                        } else {
                            arrTime = chkGrpB.getJSONArray("arrTime");
                        }
                        if (isWntWork) {
                            teDur = wData.getInteger("wntWork");
                        } else {
                            teDur = chkGrpB.getInteger("teDur");
                        }
                    } else if (wData.getInteger("workType") == 2) {
                        if (wData.containsKey("arrTime") && wData.getJSONArray("arrTime").size() > 0) {
                            arrTime = wData.getJSONArray("arrTime");
                        } else {
                            arrTime = chkGrpB.getJSONArray("arrTime");
                        }
                        if (isWntWork) {
                            teDur = wData.getInteger("wntWork");
                        } else {
                            teDur = chkGrpB.getInteger("teDur");
                        }
                        if (isWntOver) {
                            teDur+=wData.getInteger("wntOver");
                        } else {
                            teDur+=chkGrpB.getInteger("wntOver")==null?0:chkGrpB.getInteger("wntOver");
                        }
                    }
                }
            }
            if (isWeek) {
                JSONObject whWeek = chkGrpB.getJSONObject("whWeek");
                JSONObject weekData = whWeek.getJSONObject(dateToWeek(teS) + "");
                boolean isWntWork = weekData.containsKey("wntWork");
                boolean isWntOver = weekData.containsKey("wntOver");
                if (weekData.getInteger("workType") == 1) {
                    if (weekData.containsKey("arrTime") && weekData.getJSONArray("arrTime").size() > 0) {
                        arrTime = weekData.getJSONArray("arrTime");
                    } else {
                        arrTime = chkGrpB.getJSONArray("arrTime");
                    }
                    if (isWntWork) {
                        teDur = weekData.getInteger("wntWork");
                    } else {
                        teDur = chkGrpB.getInteger("teDur");
                    }
                } else if (weekData.getInteger("workType") == 2) {
                    if (weekData.containsKey("arrTime") && weekData.getJSONArray("arrTime").size() > 0) {
                        arrTime = weekData.getJSONArray("arrTime");
                    } else {
                        arrTime = chkGrpB.getJSONArray("arrTime");
                    }
                    if (isWntWork) {
                        teDur = weekData.getInteger("wntWork");
                    } else {
                        teDur = chkGrpB.getInteger("teDur");
                    }
                    if (isWntOver) {
                        teDur+=weekData.getInteger("wntOver");
                    } else {
                        teDur+=chkGrpB.getInteger("wntOver")==null?0:chkGrpB.getInteger("wntOver");
                    }
                }
            }
        } else {
            arrTime = chkGrpB.getJSONArray("arrTime");
            teDur = chkGrpB.getInteger("teDur");
        }
//        Integer wn0UserCount = chkGrpB.getInteger("wn0UserCount");
        JSONArray objSb = new JSONArray();
        JSONArray objXb = new JSONArray();
        // 用于保存上一个时间段的结束时间
//            long belowTimeData = 0;
//            int priority = 0;
//            long allTime = 0;
        long allTime = getArrTime(arrTime,objSb,objXb);
//            for (int j = 0; j < arrTime.size(); j+=2) {
//                String timeUpper = arrTime.getString(j);
//                String timeBelow = arrTime.getString(j+1);
//                String[] splitUpper = timeUpper.split(":");
//                String[] splitBelow = timeBelow.split(":");
//                int upper = Integer.parseInt(splitUpper[0]);
//                int upperDivide = Integer.parseInt(splitUpper[1]);
//                int below = Integer.parseInt(splitBelow[0]);
//                int belowDivide = Integer.parseInt(splitBelow[1]);
//                long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
//                long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
//                JSONObject objSbZ = new JSONObject();
//                objSbZ.put("priority",priority);
//                priority++;
//                objSbZ.put("tePStart",upperTime);
//                objSbZ.put("tePFinish",belowTime);
//                objSbZ.put("zon",belowTime-upperTime);
//                allTime+=(belowTime-upperTime);
//                objSb.add(objSbZ);
//                if (j == 0) {
//                    if (upperTime != 0) {
//                        JSONObject objXbZ = new JSONObject();
//                        objXbZ.put("priority",-1);
//                        objXbZ.put("tePStart",0);
//                        objXbZ.put("tePFinish",upperTime);
//                        objXbZ.put("zon",upperTime);
//                        objXb.add(objXbZ);
//                    }
//                } else if ((j + 1) + 1 >= arrTime.size()) {
//                    JSONObject objXbZ = new JSONObject();
//                    objXbZ.put("priority",-1);
//                    objXbZ.put("tePStart",belowTimeData);
//                    objXbZ.put("tePFinish",upperTime);
//                    objXbZ.put("zon",upperTime-belowTimeData);
//                    objXb.add(objXbZ);
//                    if (belowTime != 86400) {
//                        objXbZ = new JSONObject();
//                        objXbZ.put("priority",-1);
//                        objXbZ.put("tePStart",belowTime);
//                        objXbZ.put("tePFinish",86400);
//                        objXbZ.put("zon",86400-belowTime);
//                        objXb.add(objXbZ);
//                    }
//                } else {
//                    JSONObject objXbZ = new JSONObject();
//                    objXbZ.put("priority",-1);
//                    objXbZ.put("tePStart",belowTimeData);
//                    objXbZ.put("tePFinish",upperTime);
//                    objXbZ.put("zon",upperTime-belowTimeData);
//                    objXb.add(objXbZ);
//                }
//                belowTimeData = belowTime;
//            }
        JSONObject depData = depAllTime.getJSONObject(dep);
        if (null == depData) {
            depData = new JSONObject();
        }
//        if (!depData.containsKey(grpB)) {
//            depData.put(grpB,(long)(teDur*60)*60);
//            depAllTime.put(dep,depData);
//        }
        JSONObject grpBData = depData.getJSONObject(grpB);
        if (null == grpBData) {
            grpBData = new JSONObject();
        }
        grpBData.put(teS+"",teDur==0?0L:(long)(teDur*60)*60);
        depData.put(grpB,grpBData);
        depAllTime.put(dep,depData);

        // 创建时间处理打卡信息存储
        JSONObject xbAndSb = new JSONObject();
        // 添加信息
        xbAndSb.put("xb",objXb);
        xbAndSb.put("sb",objSb);

//        // 根据部门，获取部门对应的全局职位人数信息
//        JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
//        // 判断部门全局职位人数信息为空
//        if (null == depAllInfo) {
//            // 创建部门全局职位人数信息
//            depAllInfo = new JSONObject();
//            // 根据组别添加职位人数
//            depAllInfo.put(grpB,wn0UserCount);
//            grpUNumAll.put(dep,depAllInfo);
//        } else {
//            // 直接根据组别获取全局职位人数
//            Integer grpBAllInfo = depAllInfo.getInteger(grpB);
//            // 判断为空
//            if (null == grpBAllInfo) {
//                // 添加全局职位人数信息
//                depAllInfo.put(grpB,wn0UserCount);
//                grpUNumAll.put(dep,depAllInfo);
//            }
//        }

        // 根据部门获取全局上班下班信息
        JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
        JSONObject grpBAllChKin;
        // 判断为空
        if (null == depAllChKin) {
            // 创建
            depAllChKin = new JSONObject();
            grpBAllChKin = new JSONObject();
            grpBAllChKin.put(teS+"",xbAndSb);
            // 添加全局上班下班信息
            depAllChKin.put(grpB, grpBAllChKin);
            xbAndSbAll.put(dep,depAllChKin);
        } else {
            // 根据组别获取全局上班下班信息
            grpBAllChKin = depAllChKin.getJSONObject(grpB);
            if (null == grpBAllChKin) {
                grpBAllChKin = new JSONObject();
                grpBAllChKin.put(teS+"",xbAndSb);
                // 添加全局上班下班信息
                depAllChKin.put(grpB, grpBAllChKin);
                xbAndSbAll.put(dep,depAllChKin);
            } else {
                if (!grpBAllChKin.containsKey(teS + "")) {
                    grpBAllChKin.put(teS+"",xbAndSb);
                    // 添加全局上班下班信息
                    depAllChKin.put(grpB, grpBAllChKin);
                    xbAndSbAll.put(dep,depAllChKin);
                }
            }
        }
        // 添加部门信息
        depMap.put("dep",dep);
        depMap.put("id_O",id_OInside);
        // 添加信息
        grpBGroupIdOJ.put(grpB,depMap);
    }
//    public void getGrpB(JSONObject grpBGroup, Map<String,Asset> assetMap, String id_C, JSONObject depAllTime
//            , JSONObject grpUNumAll, JSONObject xbAndSbAll, JSONObject grpBGroupIdOJ, String id_OInside){
//        // 遍历组别对应部门信息
//        for (String grpB : grpBGroup.keySet()) {
//            // 创建存储部门字典
//            JSONObject depMap = new JSONObject();
//            // 根据组别获取组别信息
//            JSONObject grpBGroupInfo = grpBGroup.getJSONObject(grpB);
//            // 获取组别的部门
//            String dep = grpBGroupInfo.getString("dep");
//            Asset assetDep;
//            if (assetMap.containsKey(dep)) {
//                assetDep = assetMap.get(dep);
//            } else {
//                assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
//                assetMap.put(dep,assetDep);
//            }
//            JSONObject chkGrpB;
//            if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
//                chkGrpB = TaskObj.getChkinJava();
//            } else {
//                JSONObject chkin = assetDep.getChkin();
//                JSONObject objChkin = chkin.getJSONObject("objChkin");
//                chkGrpB = objChkin.getJSONObject(grpB);
//            }
//            JSONArray arrTime = chkGrpB.getJSONArray("arrTime");
//            Integer wn0UserCount = chkGrpB.getInteger("wn0UserCount");
//            JSONArray objSb = new JSONArray();
//            JSONArray objXb = new JSONArray();
//            // 用于保存上一个时间段的结束时间
////            long belowTimeData = 0;
////            int priority = 0;
////            long allTime = 0;
//            long allTime = getArrTime(arrTime,objSb,objXb);
////            for (int j = 0; j < arrTime.size(); j+=2) {
////                String timeUpper = arrTime.getString(j);
////                String timeBelow = arrTime.getString(j+1);
////                String[] splitUpper = timeUpper.split(":");
////                String[] splitBelow = timeBelow.split(":");
////                int upper = Integer.parseInt(splitUpper[0]);
////                int upperDivide = Integer.parseInt(splitUpper[1]);
////                int below = Integer.parseInt(splitBelow[0]);
////                int belowDivide = Integer.parseInt(splitBelow[1]);
////                long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
////                long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
////                JSONObject objSbZ = new JSONObject();
////                objSbZ.put("priority",priority);
////                priority++;
////                objSbZ.put("tePStart",upperTime);
////                objSbZ.put("tePFinish",belowTime);
////                objSbZ.put("zon",belowTime-upperTime);
////                allTime+=(belowTime-upperTime);
////                objSb.add(objSbZ);
////                if (j == 0) {
////                    if (upperTime != 0) {
////                        JSONObject objXbZ = new JSONObject();
////                        objXbZ.put("priority",-1);
////                        objXbZ.put("tePStart",0);
////                        objXbZ.put("tePFinish",upperTime);
////                        objXbZ.put("zon",upperTime);
////                        objXb.add(objXbZ);
////                    }
////                } else if ((j + 1) + 1 >= arrTime.size()) {
////                    JSONObject objXbZ = new JSONObject();
////                    objXbZ.put("priority",-1);
////                    objXbZ.put("tePStart",belowTimeData);
////                    objXbZ.put("tePFinish",upperTime);
////                    objXbZ.put("zon",upperTime-belowTimeData);
////                    objXb.add(objXbZ);
////                    if (belowTime != 86400) {
////                        objXbZ = new JSONObject();
////                        objXbZ.put("priority",-1);
////                        objXbZ.put("tePStart",belowTime);
////                        objXbZ.put("tePFinish",86400);
////                        objXbZ.put("zon",86400-belowTime);
////                        objXb.add(objXbZ);
////                    }
////                } else {
////                    JSONObject objXbZ = new JSONObject();
////                    objXbZ.put("priority",-1);
////                    objXbZ.put("tePStart",belowTimeData);
////                    objXbZ.put("tePFinish",upperTime);
////                    objXbZ.put("zon",upperTime-belowTimeData);
////                    objXb.add(objXbZ);
////                }
////                belowTimeData = belowTime;
////            }
//            if (!depAllTime.containsKey(dep)) {
//                depAllTime.put(dep,allTime);
//            }
//            // 创建时间处理打卡信息存储
//            JSONObject xbAndSb = new JSONObject();
//            // 添加信息
//            xbAndSb.put("xb",objXb);
//            xbAndSb.put("sb",objSb);
//
//            // 根据部门，获取部门对应的全局职位人数信息
//            JSONObject depAllInfo = grpUNumAll.getJSONObject(dep);
//            // 判断部门全局职位人数信息为空
//            if (null == depAllInfo) {
//                // 创建部门全局职位人数信息
//                depAllInfo = new JSONObject();
//                // 根据组别添加职位人数
//                depAllInfo.put(grpB,wn0UserCount);
//                grpUNumAll.put(dep,depAllInfo);
//            } else {
//                // 直接根据组别获取全局职位人数
//                Integer grpBAllInfo = depAllInfo.getInteger(grpB);
//                // 判断为空
//                if (null == grpBAllInfo) {
//                    // 添加全局职位人数信息
//                    depAllInfo.put(grpB,wn0UserCount);
//                    grpUNumAll.put(dep,depAllInfo);
//                }
//            }
//
//            // 根据部门获取全局上班下班信息
//            JSONObject depAllChKin = xbAndSbAll.getJSONObject(dep);
//            // 判断为空
//            if (null == depAllChKin) {
//                // 创建
//                depAllChKin = new JSONObject();
//                // 添加全局上班下班信息
//                depAllChKin.put(grpB, xbAndSb);
//                xbAndSbAll.put(dep,depAllChKin);
//            } else {
//                // 根据组别获取全局上班下班信息
//                JSONObject grpBAllChKin = depAllChKin.getJSONObject(grpB);
//                if (null == grpBAllChKin) {
//                    // 添加全局上班下班信息
//                    depAllChKin.put(grpB, xbAndSb);
//                    xbAndSbAll.put(dep,depAllChKin);
//                }
//            }
//            // 添加部门信息
//            depMap.put("dep",dep);
//            depMap.put("id_O",id_OInside);
//            // 添加信息
//            grpBGroupIdOJ.put(grpB,depMap);
//        }
//    }

    /**
     * 获取上班时间戳与休息时间戳
     * @param arrTime   上班时间段
     * @param objSb 存储上班信息
     * @param objXb 存储休息信息
     * @return  上班时间戳与休息时间戳
     */
    public long getArrTime(JSONArray arrTime,JSONArray objSb,JSONArray objXb){
        long belowTimeData = 0;
        int priority = 0;
        long allTime = 0;
        if (arrTime == null) {
            JSONObject objSbZ = new JSONObject();
            objSbZ.put("priority",priority);
            objSbZ.put("tePStart",0);
            objSbZ.put("tePFinish",0);
            objSbZ.put("zon",0);
            objSb.add(objSbZ);
            JSONObject objXbZ = new JSONObject();
            objXbZ.put("priority",-1);
            objXbZ.put("tePStart",0);
            objXbZ.put("tePFinish",86400);
            objXbZ.put("zon",86400);
            objXb.add(objXbZ);
            return 0L;
        }
        for (int j = 0; j < arrTime.size(); j+=2) {
            String timeUpper = arrTime.getString(j);
            String timeBelow = arrTime.getString(j+1);
            String[] splitUpper = timeUpper.split(":");
            String[] splitBelow = timeBelow.split(":");
            int upper = Integer.parseInt(splitUpper[0]);
            int upperDivide = Integer.parseInt(splitUpper[1]);
            int below = Integer.parseInt(splitBelow[0]);
            int belowDivide = Integer.parseInt(splitBelow[1]);
            long upperTime = (((long) upper * 60) * 60)+((long) upperDivide * 60);
            long belowTime = (((long) below * 60) * 60)+((long) belowDivide * 60);
            JSONObject objSbZ = new JSONObject();
            objSbZ.put("priority",priority);
            priority++;
            objSbZ.put("tePStart",upperTime);
            objSbZ.put("tePFinish",belowTime);
            objSbZ.put("zon",belowTime-upperTime);
            allTime+=(belowTime-upperTime);
            objSb.add(objSbZ);
            if (j == 0) {
                if (upperTime != 0) {
                    JSONObject objXbZ = new JSONObject();
                    objXbZ.put("priority",-1);
                    objXbZ.put("tePStart",0);
                    objXbZ.put("tePFinish",upperTime);
                    objXbZ.put("zon",upperTime);
                    objXb.add(objXbZ);
                }
            } else if ((j + 1) + 1 >= arrTime.size()) {
                JSONObject objXbZ = new JSONObject();
                objXbZ.put("priority",-1);
                objXbZ.put("tePStart",belowTimeData);
                objXbZ.put("tePFinish",upperTime);
                objXbZ.put("zon",upperTime-belowTimeData);
                objXb.add(objXbZ);
                if (belowTime != 86400) {
                    objXbZ = new JSONObject();
                    objXbZ.put("priority",-1);
                    objXbZ.put("tePStart",belowTime);
                    objXbZ.put("tePFinish",86400);
                    objXbZ.put("zon",86400-belowTime);
                    objXb.add(objXbZ);
                }
            } else {
                JSONObject objXbZ = new JSONObject();
                objXbZ.put("priority",-1);
                objXbZ.put("tePStart",belowTimeData);
                objXbZ.put("tePFinish",upperTime);
                objXbZ.put("zon",upperTime-belowTimeData);
                objXb.add(objXbZ);
            }
            belowTimeData = belowTime;
        }
        return allTime;
    }

    /**
     * 获取多订单一起计算物料时间后的开始时间
     * @param id_OArr  订单编号集合
     * @param teStart   开始时间
     * @return  开始时间
     */
    @Override
    public ApiResponse getTeStartTotal(JSONArray id_OArr, Long teStart) {
        long maxTime = 0;
        for (int o = 0; o < id_OArr.size(); o++) {
            String id_O = id_OArr.getString(o);
            long maxTimeNew = teStart(id_O);
            if (maxTimeNew > maxTime) {
                maxTime = maxTimeNew;
            }
//            Order order = qt.getMDContent(id_O, "casItemx", Order.class);
//            if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
//                // 返回为空错误信息
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
//            }
//            JSONObject java = order.getCasItemx().getJSONObject("java");
//            JSONArray oDates = java.getJSONArray("oDates");
//            Map<String, HashSet<String>> compMap = new HashMap<>();
//            for (int i = 0; i < oDates.size(); i++) {
//                JSONObject oDate = oDates.getJSONObject(i);
//                int bmdpt = oDate.getInteger("bmdpt");
//                if (bmdpt == 3) {
//                    String id_P = oDate.getString("id_P");
//                    String id_C = oDate.getString("id_C");
//                    HashSet<String> prodSet;
//                    if (compMap.containsKey(id_C)) {
//                        prodSet = compMap.get(id_C);
//                    } else {
//                        prodSet = new HashSet<>();
//                    }
//                    prodSet.add(id_P);
//                    compMap.put(id_C,prodSet);
//                } else {
//                    Long wntDur = oDate.getLong("wntDur");
//                    if (bmdpt != 2 && wntDur <= 0) {
//                        // 返回为空错误信息
//                        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
//                    }
//                }
//            }
//            Map<String,Prod> prodMap = new HashMap<>();
//            for (String id_C : compMap.keySet()) {
//                HashSet<String> prodArr = compMap.get(id_C);
//                for (String id_P : prodArr) {
//                    Prod prod;
//                    if (prodMap.containsKey(id_P)) {
//                        prod = prodMap.get(id_P);
//                    } else {
//                        prod = qt.getMDContent(id_P, "qtySafex", Prod.class);
//                        if (null != prod && null != prod.getQtySafex() && null != prod.getQtySafex().getDouble(id_C)) {
//                            prodMap.put(id_P,prod);
//                        } else {
//                            prod = null;
//                        }
//                    }
//                    if (null != prod) {
//                        double hour = prod.getQtySafex().getDouble(id_C);
//                        long time = (long) ((hour*60)*60);
//                        if (time > maxTime) {
//                            maxTime = time;
//                        }
//                    }
//                }
//            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), teStart+maxTime);
    }

    /**
     * 获取多订单分开计算物料时间后的开始时间
     * @param orderInfo  订单信息集合
     * @return  开始时间
     */
    @Override
    public ApiResponse getTeStartList(JSONArray orderInfo) {
        JSONArray resultArr = new JSONArray();
        for (int o = 0; o < orderInfo.size(); o++) {
            JSONObject info = orderInfo.getJSONObject(o);
            resultArr.add(info.getLong("teStart")+teStart(info.getString("id_O")));
        }
        return retResult.ok(CodeEnum.OK.getCode(), resultArr);
    }

    /**
     * 获取订单的最长物料时间
     * @param id_O  订单编号
     * @return  最长物料时间
     */
    public long teStart(String id_O){
        long maxTime = 0;
        Order order = qt.getMDContent(id_O, "casItemx", Order.class);
        if (null == order || null == order.getCasItemx() || null == order.getCasItemx().getJSONObject("java")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject java = order.getCasItemx().getJSONObject("java");
        JSONArray oDates = java.getJSONArray("oDates");
        Map<String, HashSet<String>> compMap = new HashMap<>();
        for (int i = 0; i < oDates.size(); i++) {
            JSONObject oDate = oDates.getJSONObject(i);
            int bmdpt = oDate.getInteger("bmdpt");
            if (bmdpt == 3) {
                String id_P = oDate.getString("id_P");
                String id_C = oDate.getString("id_C");
                HashSet<String> prodSet;
                if (compMap.containsKey(id_C)) {
                    prodSet = compMap.get(id_C);
                } else {
                    prodSet = new HashSet<>();
                }
                prodSet.add(id_P);
                compMap.put(id_C,prodSet);
            } else {
                Long wntDur = oDate.getLong("wntDur");
                if (bmdpt != 2 && wntDur <= 0) {
                    // 返回为空错误信息
                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ERR_TIME_NULL.getCode(), "时间为空");
                }
            }
        }
        Map<String,Prod> prodMap = new HashMap<>();
        for (String id_C : compMap.keySet()) {
            HashSet<String> prodArr = compMap.get(id_C);
            for (String id_P : prodArr) {
                Prod prod;
                if (prodMap.containsKey(id_P)) {
                    prod = prodMap.get(id_P);
                } else {
                    prod = qt.getMDContent(id_P, "qtySafex", Prod.class);
                    if (null != prod && null != prod.getQtySafex() && null != prod.getQtySafex().getDouble(id_C)) {
                        prodMap.put(id_P,prod);
                    } else {
                        prod = null;
                    }
                }
                if (null != prod) {
                    double hour = prod.getQtySafex().getDouble(id_C);
                    long time = (long) ((hour*60)*60);
                    if (time > maxTime) {
                        maxTime = time;
                    }
                }
            }
        }
        return maxTime;
    }

    @Override
    public ApiResponse updateODate() {
        JSONArray ids = qt.setArray("65a8d523ec65d36e9095034b","65b338bcec65d36e909503bb","65b33b86ec65d36e909503bc");
        List<Order> orders = qt.getMDContentFast(ids, qt.strList("casItemx"), Order.class);
        List<JSONObject> list = new ArrayList<>();
        for (Order order : orders) {
            JSONObject java = order.getCasItemx().getJSONObject("java");
            JSONArray oDates = java.getJSONArray("oDates");
//            JSONArray jsonArray = java.getJSONArray("");
            for (int i = 0; i < oDates.size(); i++) {
                JSONObject date = oDates.getJSONObject(i);
                date.put("wntDur",date.getLong("teDur"));
                date.put("wntDurTotal",date.getLong("teDurTotal"));
                date.put("wntPrep",date.getLong("tePrep"));
//                date.remove("teDur");
//                date.remove("teDurTotal");
//                date.remove("tePrep");
                oDates.set(i,date);
            }
//            qt.setMDContent(order.getId(),qt.setJson("casItemx.java.oDates",oDates), Order.class);
            list.add(qt.setJson("id",order.getId(),"updateData",qt.setJson("casItemx.java.oDates",oDates)));
        }
        qt.setMDContentFast(list, Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse delExcessiveODateField() {
        // 更改字段
//        JSONArray ids = qt.setArray("65a8d523ec65d36e9095034b","65b338bcec65d36e909503bb","65b33b86ec65d36e909503bc");
//        List<Order> orders = qt.getMDContentFast(ids, qt.strList("casItemx"), Order.class);
//        List<JSONObject> list = new ArrayList<>();
//        for (Order order : orders) {
//            JSONObject java = order.getCasItemx().getJSONObject("java");
//            JSONArray oDates = java.getJSONArray("oDates");
//            JSONArray oTasks = java.getJSONArray("oTasks");
//            for (int i = 0; i < oDates.size(); i++) {
////                JSONObject date = oDates.getJSONObject(i);
////                date.remove("teDur");
////                date.remove("tePrep");
////                date.remove("teDurTotal");
////                date.remove("teStart");
////                date.remove("taStart");
////                date.remove("teFin");
////                date.remove("taFin");
////                oDates.set(i,date);
//
//                JSONObject date = oDates.getJSONObject(i);
//                JSONObject task = oTasks.getJSONObject(i);
//                date.put("wrdN",task.getJSONObject("wrdN"));
//                oDates.set(i,date);
//            }
//            list.add(qt.setJson("id",order.getId(),"updateData",qt.setJson("casItemx.java.oDates",oDates)));
//        }
//        qt.setMDContentFast(list, Order.class);

        // 添加每日人数
//        JSONArray assetIds = qt.setArray("6598e094e21d992c73e0d59c", "6598e094e21d992c73e0d59e");
//        try {
//            LocalDate date = LocalDate.now();
//            int year = date.getYear();
//            int month = date.getMonthValue();
//            JSONArray thisMonth = getThisMonth(year, month, 0);
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 日期格式化样式
//            Date dateNew;
//            JSONObject userCount = new JSONObject();
//            int count = 1;
//            for (int i = 0; i < thisMonth.size(); i++) {
//                thisMonth.set(i,thisMonth.getString(i)+" 00:00:00");
//                dateNew = sdf.parse(thisMonth.getString(i));
//                if (i > 20) {
//                    count = 2;
//                } else if (i > 10) {
//                    count = 3;
//                }
//                userCount.put((dateNew.getTime()/1000)+"",count);
//            }
//            System.out.println("--");
//            System.out.println(JSON.toJSONString(thisMonth));
//            System.out.println(JSON.toJSONString(userCount));
//            for (int i = 0; i < assetIds.size(); i++) {
//                Asset asset = qt.getMDContent(assetIds.getString(i), "chkin", Asset.class);
//                if (null != asset && null != asset.getChkin() && null != asset.getChkin().getJSONObject("objChkin")) {
//                    JSONObject objChkin = asset.getChkin().getJSONObject("objChkin");
//                    for (String grpB : objChkin.keySet()) {
//                        qt.setMDContent(assetIds.getString(i),qt.setJson("chkin.objChkin."+grpB+".wnUser",userCount), Asset.class);
//                    }
//                }
//            }
//        } catch (Exception e){
//            System.out.println("出现异常:");
//        }

        // 添加星期上班
        JSONArray assetIds = qt.setArray("6598e094e21d992c73e0d59c", "6598e094e21d992c73e0d59e");
        for (int i = 0; i < assetIds.size(); i++) {
            Asset asset = qt.getMDContent(assetIds.getString(i), "chkin", Asset.class);
            if (null != asset && null != asset.getChkin() && null != asset.getChkin().getJSONObject("objChkin")) {
                JSONObject whWeek = new JSONObject();
                JSONObject day = new JSONObject();
                // workType = 是否上班，0 = 不上班，1 = 上班，2 = 加班，3 = 特殊，4 = 特殊加班，5 = 三倍加班
                day.put("workType",0);
                // arrTime = 当前星期独立上班时间
                day.put("arrTime",qt.setArray("08:00:00", "12:00:00", "14:00:00", "18:00:00",
                        "19:00:00", "21:00:00"));
                // ovtDate = 加班时间段，开头
                day.put("ovtDate",qt.setArray(4));
                // wntWork = 当前星期普通上班时间
                day.put("wntWork",8);
                // wntOver = 当前星期加班时间
                day.put("wntOver",2);
                whWeek.put("0",day);
                day.put("workType",2);
                whWeek.put("1",qt.cloneObj(day));
                whWeek.put("2",qt.cloneObj(day));
                whWeek.put("3",qt.cloneObj(day));
                whWeek.put("4",qt.cloneObj(day));
                whWeek.put("5",qt.cloneObj(day));
                whWeek.put("6",qt.cloneObj(day));
                JSONObject objChkin = asset.getChkin().getJSONObject("objChkin");
                for (String grpB : objChkin.keySet()) {
                    qt.setMDContent(assetIds.getString(i),qt.setJson("chkin.objChkin."+grpB+".whWeek",whWeek
                            ,"chkin.objChkin."+grpB+".whDate.1715788800",qt.cloneObj(day)
                            ,"chkin.objChkin."+grpB+".wntOver",2
                            ,"chkin.objChkin."+grpB+".teDur",8), Asset.class);
                }
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    /**
     * 获取指定年和月的一个月所有日期
     * @param year  年份
     * @param month 月份
     * @param type 类型 == 0 年月日，== 1 只拿日期
     * @return  一个月的所有日期
     */
    public static JSONArray getThisMonth(int year, int month,int type){
        LocalDate currentDate = LocalDate.of(year,month,1);
        LocalDate firstDayOfMonth = currentDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDayOfMonth = currentDate.with(TemporalAdjusters.lastDayOfMonth());
        JSONArray dates = new JSONArray();
        for (LocalDate date = firstDayOfMonth; date.isBefore(lastDayOfMonth.plusDays(1)); date = date.plusDays(1)) {
            if (type == 1) {
                dates.add(date.getDayOfMonth());
            } else {
                dates.add(date.getYear()+"/"+date.getMonthValue()+"/"+ addZero(date.getDayOfMonth()));
            }
        }
        return dates;
    }
    /**
     * 将数字补零,只限用于时间
     * @param b	需要补零的数字
     * @return java.lang.String  返回结果: 补零结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:20
     */
    public static String addZero(int b) {
        if (b > 9) {
            return b + "";
        } else {
            return "0" + b;
        }
    }

    /**
     * 清理指定任务方法
     * @param id_O  订单编号
     * @param dateIndex 要清理的起始下标
     * @param id_C  公司编号
     * @param layer 层级
     * @param id_PF 父编号
     * @return  清理结果
     */
    @Override
    public ApiResponse getClearOldTask(String id_O, int dateIndex, String id_C,String layer,String id_PF) {
        clearOldTaskNew(id_O,dateIndex,id_C,layer,id_PF);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse setChKinUserCountByOrder(String id_O,String id_C) {
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O,"casItemx", Order.class);
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getCasItemx()
                || null == salesOrderData.getCasItemx().getJSONObject("java")
                || null == salesOrderData.getCasItemx().getJSONObject("java").getJSONObject("oDateObj")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 获取递归存储的时间处理信息
        JSONObject oDateObj = salesOrderData.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
        Map<String,Asset> assetMap = new HashMap<>();
        for (String layer : oDateObj.keySet()) {
            JSONObject layerInfo = oDateObj.getJSONObject(layer);
            for (String pf : layerInfo.keySet()) {
                JSONObject pfInfo = layerInfo.getJSONObject(pf);
                JSONArray oDates = pfInfo.getJSONArray("oDates");
                for (int i = 0; i < oDates.size(); i++) {
                    JSONObject oDate = oDates.getJSONObject(i);
                    String dep = oDate.getString("dep");
                    String grpB = oDate.getString("grpB");
                    Asset asset;
                    if (assetMap.containsKey(dep)) {
                        asset = assetMap.get(dep);
                    } else {
                        asset = qt.getConfig(id_C,"d-"+dep,"chkin");
                        assetMap.put(dep,asset);
                    }
                    JSONObject chkGrpB;
                    int wn0UserCount;
                    if (null == asset || null == asset.getChkin()
                            || null == asset.getChkin().getJSONObject("objChkin")
                            || null == asset.getChkin().getJSONObject("objChkin").getJSONObject(grpB)) {
                        wn0UserCount = 1;
                    } else {
                        JSONObject chkin = asset.getChkin();
                        JSONObject objChkin = chkin.getJSONObject("objChkin");
                        chkGrpB = objChkin.getJSONObject(grpB);
                        wn0UserCount = chkGrpB.getInteger("wn0UserCount")==null?1:chkGrpB.getInteger("wn0UserCount");
                    }
                    oDate.put("grpUNum",wn0UserCount);
                    oDates.set(i,oDate);
                }
                pfInfo.put("oDates",oDates);
                layerInfo.put(pf,pfInfo);
            }
            oDateObj.put(layer,layerInfo);
        }
        qt.setMDContent(id_O,qt.setJson("casItemx.java.oDateObj",oDateObj), Order.class);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse getAtFirstEasy(String id_O, Long teStart, String id_C) {
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O,qt.strList("oItem", "info", "view", "action", "casItemx"), Order.class);
        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
                || null == salesOrderData.getCasItemx() || null == salesOrderData.getInfo()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject casItemx = salesOrderData.getCasItemx();
        // 获取递归存储的时间处理信息
        JSONObject oDateObj = casItemx.getJSONObject("java").getJSONObject("oDateObj");
        List<Integer> layerList = new ArrayList<>();
        for (String layer : oDateObj.keySet()) {
            layerList.add(Integer.parseInt(layer));
            JSONObject layerInfo = oDateObj.getJSONObject(layer);
            for (String id_PF : layerInfo.keySet()) {
                JSONObject pfInfo = layerInfo.getJSONObject(id_PF);
                pfInfo.put("arrPStart",new JSONObject());
                layerInfo.put(id_PF,pfInfo);
                oDateObj.put(layer,layerInfo);
            }
        }
        layerList.sort(Comparator.reverseOrder());
        Map<String,JSONObject> assetGrpMap = new HashMap<>();
        JSONObject objEasy = new JSONObject();
        JSONObject thisEasy = new JSONObject();
        dgTimeEasy(layerList.get(0),oDateObj,id_C
                ,true,teStart,assetGrpMap,objEasy,thisEasy);
        System.out.println("objEasy:");
        System.out.println(JSON.toJSONString(objEasy));
        System.out.println("DepTeSta:");
        JSONObject easyDepTeSta = getEasyDepTeSta(thisEasy);
        System.out.println(JSON.toJSONString(easyDepTeSta));
        JSONObject easyAssetId = getEasyAssetId(thisEasy);
        easyAssetId.forEach((key,val)-> qt.setMDContent(val.toString(),qt.setJson("aArrange.objEasy",objEasy.getJSONObject(key),"aArrange.objEasyLastTime",easyDepTeSta.getLong(key)), Asset.class));
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse setOrderUserCount(String id_O, String id_C, Long teStart) {
        // 调用方法获取订单信息
        Order order = qt.getMDContent(id_O,qt.strList("casItemx"), Order.class);
        // 判断订单是否为空
        if (null == order || null == order.getCasItemx()
                || null == order.getCasItemx().getJSONObject("java")
                || null == order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONObject oDateObj = order.getCasItemx().getJSONObject("java").getJSONObject("oDateObj");
//        JSONObject depAndGrpB = new JSONObject();
        JSONObject queryId = new JSONObject();
        for (String layer : oDateObj.keySet()) {
            JSONObject layerData = oDateObj.getJSONObject(layer);
            for (String id_PF : layerData.keySet()) {
                JSONObject pfData = layerData.getJSONObject(id_PF);
                JSONArray oDates = pfData.getJSONArray("oDates");
                for (int i = 0; i < oDates.size(); i++) {
                    JSONObject date = oDates.getJSONObject(i);
                    String dep = date.getString("dep");
                    String grpB = date.getString("grpB");
                    JSONObject depIdData = queryId.getJSONObject(dep);
                    if (null != depIdData) {
                        if (depIdData.containsKey(grpB)) {
                            date.put("grpUNum",queryId.getJSONObject(dep).getInteger(grpB));
                        } else {
                            date.put("grpUNum",1);
                            System.out.println("grpUNum信息为空-默认-人数写入:");
                        }
                    }
                    else {
                        depIdData = new JSONObject();
                        Asset as = qt.getConfig(id_C, "d-" + dep, "chkin");
                        if (null == as || null == as.getChkin() || null == as.getChkin().getJSONObject("objChkin")) {
                            date.put("grpUNum",1);
                            depIdData.put(grpB,1);
                            System.out.println("Asset为空-默认-人数写入:");
                        }
                        else {
                            JSONObject objChkin = as.getChkin().getJSONObject("objChkin");
                            for (String grpBChk : objChkin.keySet()) {
                                JSONObject grpBChkData = objChkin.getJSONObject(grpBChk);
                                Integer wn0UserCount = grpBChkData.getInteger("wn0UserCount");
                                JSONObject wnUser = grpBChkData.getJSONObject("wnUser");
                                if (null!= wnUser && wnUser.containsKey(teStart+"")) {
                                    depIdData.put(grpB,wnUser.getInteger(teStart+""));
                                    System.out.println("最新人数写入:");
                                } else {
                                    if (null != wn0UserCount && wn0UserCount > 0) {
                                        depIdData.put(grpB,wn0UserCount);
                                        System.out.println("wn0UserCount-人数写入:");
                                    } else {
                                        depIdData.put(grpB,1);
                                        System.out.println("wn0UserCount为空-默认-人数写入:");
                                    }
                                }
                            }
                            date.put("grpUNum",depIdData.getInteger(grpB));
                        }
                        queryId.put(dep,depIdData);
                    }
                    oDates.set(i,date);
                }
                pfData.put("oDates",oDates);
                layerData.put(id_PF,pfData);
            }
            oDateObj.put(layer,layerData);
        }
        System.out.println("oDateObj:");
        System.out.println(JSON.toJSONString(oDateObj));
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse clearThisDayTaskAndSave(String id_C, String dep, String grpB, long thisDay) {
        System.out.println("进入方法");
        clearThisDayTaskAndSaveFc(id_C, dep, grpB, thisDay);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    @Override
    public ApiResponse clearThisDayEasyTaskAndSave(String id_C, String dep, long thisDay) {
        clearThisDayEasyTaskAndSaveFc(id_C, dep, thisDay);
        return retResult.ok(CodeEnum.OK.getCode(), "成功");
    }

    private void dgTimeEasy(int layer,JSONObject oDateObj
            ,String id_C,boolean isFirst,long teStart,Map<String,JSONObject> assetGrpMap,JSONObject objEasy
            ,JSONObject thisEasy){
        if (layer > 0) {
            System.out.println("layer:"+layer);
            JSONObject prodInfo = oDateObj.getJSONObject(layer + "");
            for (String prodId : prodInfo.keySet()) {
                JSONObject partInfo = prodInfo.getJSONObject(prodId);
                JSONArray oDates = partInfo.getJSONArray("oDates");
                // 遍历时间处理信息集合
                for (int i = 0; i < oDates.size(); i++) {
                    // 获取i对应的时间处理信息
                    JSONObject oDate = oDates.getJSONObject(i);
                    // 获取时间处理的组别
                    String grpB = oDate.getString("grpB");
                    String dep = oDate.getString("dep");
                    // 获取时间处理的零件产品编号
                    String id_P = oDate.getString("id_P");
                    System.out.println("输出oDate:"+id_P);
                    // 获取时间处理的实际准备时间
                    Long wntPrep = oDate.getLong("wntPrep");
                    Long wntDurTotal = oDate.getLong("wntDurTotal");
                    JSONObject depEasy = getDepEasy(objEasy,dep,id_C,thisEasy);
                    JSONObject easyDepTeSta = getEasyDepTeSta(thisEasy);
                    if (null == easyDepTeSta) {
                        easyDepTeSta = new JSONObject();
                        easyDepTeSta.put(dep,teStart);
                        setEasyDepTeSta(thisEasy,easyDepTeSta);
                    } else {
                        if (!easyDepTeSta.containsKey(dep)) {
                            easyDepTeSta.put(dep,teStart);
                            setEasyDepTeSta(thisEasy,easyDepTeSta);
                        } else {
                            teStart = easyDepTeSta.getLong(dep);
                            System.out.println("最后时间:"+teStart);
                        }
                    }
                    long wntLeft;
                    JSONArray easyTasks;
                    if (!isFirst) {
                        System.out.println("不是最后一层");
                    }
                    JSONObject teStaObj;
                    if (null != depEasy) {
                        teStaObj = depEasy.getJSONObject(teStart + "");
                        if (null != teStaObj) {
                            wntLeft = teStaObj.getLong("wntLeft");
                            easyTasks = teStaObj.getJSONArray("easyTasks");
                            System.out.println("获取本地:"+wntLeft);
                        } else {
                            teStaObj = new JSONObject();
                            easyTasks = new JSONArray();
                            wntLeft = getTotalTime(assetGrpMap,grpB,id_C,dep);
                            teStaObj.put("wntTotal",wntLeft);
                        }
                    }
                    else {
                        depEasy = new JSONObject();
                        teStaObj = new JSONObject();
                        easyTasks = new JSONArray();
                        wntLeft = getTotalTime(assetGrpMap,grpB,id_C,dep);
                        teStaObj.put("wntTotal",wntLeft);
                    }
                    long totalDur = wntDurTotal + wntPrep;
                    if (wntLeft == 0) {
                        JSONObject easyDepTeStaNew = getEasyDepTeSta(thisEasy);
                        Long depSta = easyDepTeStaNew.getLong(dep);
                        teStaObj = depEasy.getJSONObject(depSta + "");
                        wntLeft = teStaObj.getLong("wntLeft");
                        if (wntLeft == 0) {
                            dgSkipDay(objEasy,thisEasy,dep,assetGrpMap,grpB,id_C,totalDur,oDate,prodId,layer,i);
                        } else {
                            long totalTimeNew = wntLeft - totalDur;
                            if (totalTimeNew < 0) {
                                long disparityTime = totalDur - wntLeft;
                                teStaObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,wntLeft));
                                wntLeft = 0L;
                                teStaObj.put("wntLeft",wntLeft);
                                depEasy.put(depSta+"",teStaObj);
                                objEasy.put(dep,depEasy);
                                dgSkipDay(objEasy,thisEasy,dep,assetGrpMap,grpB,id_C,disparityTime,oDate,prodId,layer,i);
                            } else {
                                teStaObj.put("wntLeft",totalTimeNew);
                                teStaObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,totalDur));
                                depEasy.put(depSta+"",teStaObj);
                                objEasy.put(dep,depEasy);
                            }
                        }
                    }
                    else {
                        long totalTimeNew = wntLeft - totalDur;
                        if (totalTimeNew < 0) {
                            long disparityTime = totalDur - wntLeft;
                            System.out.println("totalDur:"+totalDur+",wntLeft:"+wntLeft);
                            teStaObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,wntLeft));
                            wntLeft = 0L;
                            teStaObj.put("wntLeft",wntLeft);
                            depEasy.put(teStart+"",teStaObj);
                            objEasy.put(dep,depEasy);
                            dgSkipDay(objEasy,thisEasy,dep,assetGrpMap,grpB,id_C,disparityTime,oDate,prodId,layer,i);
                        } else {
                            teStaObj.put("wntLeft",totalTimeNew);
                            teStaObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,totalDur));
                            depEasy.put(teStart+"",teStaObj);
                            objEasy.put(dep,depEasy);
                        }
                    }
                }
            }
            dgTimeEasy(layer-1,oDateObj,id_C,false
                    ,teStart,assetGrpMap,objEasy,thisEasy);
        }
    }

    private JSONArray setEasyTask(JSONArray easyTasks,JSONObject oDate,String prodId,int layer,int i,long timeTotal){
        if (oDate.getJSONObject("wrdN").getString("cn").equals("打铁铰")) {
            System.out.println("打铁铰:");
        }
        easyTasks.add(qt.setJson("wrdN",oDate.getJSONObject("wrdN"),"index",oDate.getInteger("index")
                ,"id_O",oDate.getString("id_O"),"id_P",oDate.getString("id_P"),"id_PF",prodId
                ,"layer",layer,"dateIndex",i,"timeTotal",timeTotal));
        return easyTasks;
    }

    private JSONObject getDepEasy(JSONObject objEasy,String dep,String id_C,JSONObject thisEasy){
        JSONObject depEasy = objEasy.getJSONObject(dep);
        if (null == depEasy) {
            Asset asset = qt.getConfig(id_C, "d-" + dep, qt.strList("aArrange.objEasy","aArrange.objEasyLastTime"));
            JSONObject easyAssetId = getEasyAssetId(thisEasy);
            if (null == easyAssetId) {
                easyAssetId = new JSONObject();
            }
            if (!easyAssetId.containsKey(dep)) {
                easyAssetId.put(dep,asset.getId());
                setEasyAssetId(thisEasy,easyAssetId);
            }
            if (null == asset || null == asset.getAArrange() || !asset.getAArrange().containsKey("objEasy")) {
                return null;
            }
            JSONObject objEasyAsset = asset.getAArrange().getJSONObject("objEasy");
            objEasy.put(dep,objEasyAsset);
            Long objEasyLastTime = asset.getAArrange().getLong("objEasyLastTime");
            if (null != objEasyLastTime) {
                JSONObject easyDepTeSta = new JSONObject();
                easyDepTeSta.put(dep,objEasyLastTime);
                setEasyDepTeSta(thisEasy,easyDepTeSta);
            }
            return objEasyAsset;
        } else {
            return depEasy;
        }
    }

    private void dgSkipDay(JSONObject objEasy,JSONObject thisEasy,String dep
            ,Map<String, JSONObject> assetGrpMap,String grpB,String id_C,long totalDur,JSONObject oDate
            ,String prodId,int layer,int i){
        System.out.println("进入跳天-totalDur:"+totalDur);
        JSONObject easyDepTeSta = getEasyDepTeSta(thisEasy);
        Long depSta = easyDepTeSta.getLong(dep);
        depSta+=86400;
        easyDepTeSta.put(dep,depSta);
        setEasyDepTeSta(thisEasy,easyDepTeSta);
        JSONObject depObj = objEasy.getJSONObject(dep);
        JSONObject staObj = depObj.getJSONObject(depSta + "");
        JSONArray easyTasks;
        if (null == staObj) {
            staObj = new JSONObject();
            easyTasks = new JSONArray();
            long wntLeft = getTotalTime(assetGrpMap, grpB, id_C, dep);
            staObj.put("wntTotal",wntLeft);
            long totalTimeNew = wntLeft - totalDur;
            if (totalTimeNew < 0) {
                long disparityTime = totalDur - wntLeft;
                staObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,wntLeft));
                wntLeft = 0L;
                staObj.put("wntLeft",wntLeft);
                depObj.put(depSta+"",staObj);
                objEasy.put(dep,depObj);
                dgSkipDay(objEasy,thisEasy,dep,assetGrpMap,grpB,id_C,disparityTime,oDate,prodId,layer,i);
            } else {
                staObj.put("wntLeft",totalTimeNew);
                staObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,totalDur));
                depObj.put(depSta+"",staObj);
                objEasy.put(dep,depObj);
            }
        } else {
            Long wntLeft = staObj.getLong("wntLeft");
            easyTasks = staObj.getJSONArray("easyTasks");
            if (wntLeft == 0) {
                dgSkipDay(objEasy,thisEasy,dep,assetGrpMap,grpB,id_C,totalDur,oDate,prodId,layer,i);
            } else {
                long totalTimeNew = wntLeft - totalDur;
                if (totalTimeNew < 0) {
                    long disparityTime = totalDur - wntLeft;
                    staObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,wntLeft));
                    wntLeft = 0L;
                    staObj.put("wntLeft",wntLeft);
                    depObj.put(depSta+"",staObj);
                    objEasy.put(dep,depObj);
                    dgSkipDay(objEasy,thisEasy,dep,assetGrpMap,grpB,id_C,disparityTime,oDate,prodId,layer,i);
                } else {
                    staObj.put("wntLeft",totalTimeNew);
                    staObj.put("easyTasks",setEasyTask(easyTasks,oDate,prodId,layer,i,totalDur));
                    depObj.put(depSta+"",staObj);
                    objEasy.put(dep,depObj);
                }
            }
        }
    }
    private long getTotalTime(Map<String, JSONObject> assetGrpMap,String grpB,String id_C,String dep){
        JSONObject chkGrpB;
        if (assetGrpMap.containsKey(grpB)) {
            chkGrpB = assetGrpMap.get(grpB);
        } else {
            Asset assetDep = qt.getConfig(id_C,"d-"+dep,"chkin");
            if (null == assetDep || null == assetDep.getChkin() || null == assetDep.getChkin().getJSONObject("objChkin")) {
                chkGrpB = TaskObj.getChkinJava();
                assetGrpMap.put(grpB,chkGrpB);
                System.out.println("获取系统:");
            } else {
                JSONObject chkin = assetDep.getChkin();
                JSONObject objChkin = chkin.getJSONObject("objChkin");
                chkGrpB = objChkin.getJSONObject(grpB);
                assetGrpMap.put(grpB,chkGrpB);
                System.out.println("获取数据库:");
            }
        }
        Integer teDur = chkGrpB.getInteger("teDur");
        return (long)(teDur*60)*60;
    }

    private void setEasyDepTeSta(JSONObject thisEasy,JSONObject depTeSta){
        thisEasy.put("depTeSta",depTeSta);
    }
    private JSONObject getEasyDepTeSta(JSONObject thisEasy){
        return thisEasy.getJSONObject("depTeSta");
    }
    private void setEasyAssetId(JSONObject thisEasy,JSONObject assetId){
        thisEasy.put("assetId",assetId);
    }
    private JSONObject getEasyAssetId(JSONObject thisEasy){
        return thisEasy.getJSONObject("assetId");
    }
}
