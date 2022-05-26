package com.cresign.chat.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.chat.common.ChatEnum;
import com.cresign.chat.service.FlowService;
import com.cresign.chat.service.TimeZjService;
import com.cresign.chat.utils.Obj;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.RedisUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lSBOrder;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.chkin.Task;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.orderCard.OrderODate;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import com.cresign.tools.pojo.po.prodCard.ProdInfo;
import org.apache.poi.ss.formula.functions.T;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class FlowServiceImpl implements FlowService {

    /**
     * 用于存储测试，每个组别对应的部门信息
     */
    private static final JSONObject grpBGroup = new JSONObject();

    /**
     * 根据当前唯一ID存储时间处理的最初开始时间
     */
    private static final JSONObject hTeC = new JSONObject();
    /**
     * 根据当前唯一ID存储时间处理的第一个时间信息的结束时间
     */
    private static final JSONObject csTe = new JSONObject();
    /**
     * 根据当前唯一ID存储时间处理的最初产品编号
     */
    private static final JSONObject csId_P = new JSONObject();

    /**
     * 用于存储递归层级
     */
    private static int isJsLj = 0;

    static {
        // 创建存储递归零件分组信息obj
        JSONObject objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","1xx1");
        objDep.put("id","jD45FAxp");
        grpBGroup.put("1002",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","1xx1");
        objDep.put("id","jD45FAxp");
        grpBGroup.put("1001",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","665591821169");
        objDep.put("id","o-x000");
        grpBGroup.put("1000",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","0000");
        objDep.put("id","o-x");
        grpBGroup.put("673722114758",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","0000");
        objDep.put("id","o-x");
        grpBGroup.put("347152414139",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","1111");
        objDep.put("id","o-x2");
        grpBGroup.put("546679828566",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","1111");
        objDep.put("id","o-x2");
        grpBGroup.put("386677624296",objDep);

//        objDep = new JSONObject();
//        // 添加字段信息
//        objDep.put("dep","0xx1");
//        objDep.put("id","jD45FAxp");
//        grpBGroup.put("1100",objDep);
    }
    static {
        // 创建存储递归零件分组信息obj
        JSONObject objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("774653425478",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("562895845324",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("544993734644",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("729275885261",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("298493828259",objDep);

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("914975375143",objDep);
    }
    static {
        // 创建存储递归零件分组信息obj
        JSONObject objDep;

        objDep = new JSONObject();
        // 添加字段信息
        objDep.put("dep","2xx0");
        objDep.put("id","o-2x");
        grpBGroup.put("735966326474",objDep);
    }

    /**
     checkAllBmdPt - dgCheck - dgCheckUtil

     dgRemove / setAllBmdpt / getPartIsNull

     getDgResult -> dgProcess   / mergePart -> dgMergQtySet -> updateSalesOrder
     **/

    @Autowired
    private CoupaUtil coupaUtil;

    @Resource
    private RestHighLevelClient client;

//    @Autowired
//    private OrderService orderService;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private RetResult retResult;

    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private TimeZjService timeZjService;


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse getDgResult(String id_OParent, String id_U, String myCompId, Long teStart) {

        // 调用方法获取订单信息
        Order salesOrderData = coupaUtil.getOrderByListKey(
                id_OParent, Arrays.asList("oItem", "info", "view", "action"));

        // 判断订单是否为空
        if (null == salesOrderData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }

        for (int i = 0; i < salesOrderData.getOItem().getJSONArray("objItem").size(); i++) {
            String id_P = salesOrderData.getOItem().getJSONArray("objItem").getJSONObject(i).getString("id_P");
            int layer = 0;
            // 创建异常信息存储
            JSONArray isRecurred = new JSONArray();
            // 创建产品信息存储
            JSONArray isEmpty = new JSONArray();
            // 创建零件id集合
            JSONArray pidList = new JSONArray();
            JSONObject nextPart = new JSONObject();

            // ******调用验证方法******
            this.dgCheckUtil(pidList, id_P, myCompId, nextPart, isRecurred, layer, isEmpty);

            if (isRecurred.size() > 0) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_RECURRED.getCode(), id_P);
            }
            if (isEmpty.size() > 0) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_NOT_EXIST.getCode(), id_P);
            }
        }

        // 第一次把action卡创建出来
        if (null == salesOrderData.getAction()) {
            JSONObject newEmptyAction = new JSONObject();
            salesOrderData.setAction(newEmptyAction);
        }
        if (null != salesOrderData.getAction().getString("isDg")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经被递归了");
        }
        if (salesOrderData.getInfo().getLST() != 7) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_ORDER_NEED_FINAL.getCode(), "需要两方确认");
        }

        // 转换oItem为list
        JSONArray oParent_objItem = salesOrderData.getOItem().getJSONArray("objItem");

        // 获取唯一下标
        String random = MongoUtils.GetObjectId();

        Double oParent_prior = salesOrderData.getInfo().getPriority();

        // 创建存储递归OItem结果的Map
        Map<String, List<OrderOItem>> objOItemCollection = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

        // 创建存储递归Action结果的Map
        Map<String, List<OrderAction>> objActionCollection = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

        // 定义以公司id为键的存储map            // 定义以订单id为键的存储
        JSONArray casItemData = new JSONArray();

        //orderAction存储map
        Map<String, OrderAction> pidActionCollection = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 创建递归存储的时间处理信息
        List<OrderODate> oDates = new ArrayList<>();
        // 创建递归存储的时间任务信息
        List<Task> oTasks = new ArrayList<>();
        // 创建存储零件编号的合并信息记录合并时的下标
        JSONObject mergeJ = new JSONObject();
        // 遍历订单内所有产品
        for (int item = 0; item < oParent_objItem.size(); item++) {

            OrderOItem objOItem = JSONObject.parseObject(JSON.toJSONString(oParent_objItem.getJSONObject(item)), OrderOItem.class);

            OrderAction objAction = new OrderAction(100, 0, 0, 1, salesOrderData.getId(),
                    salesOrderData.getInfo().getRef(), objOItem.getId_P(), id_OParent, item, objOItem.getWn0prior(), 0, 0,
                    null, null, null, null, salesOrderData.getInfo().getWrdN(), objOItem.getWrdN());
            ////////////////actually dg ///////////////////////

            //dgType: 1 = firstLayer (sales Items), 2 = regular/subTask or subProd, 3 = depSplit regular
            // T/P - T/P -T/P.... problem is id_P == ""?
//            Map<Integer,Integer> priorInd = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
//            JSONArray priorArrData = new JSONArray();
            this.dgProcess(1, myCompId, id_OParent, objOItem, objAction, casItemData, oParent_objItem
                    , item, objOItemCollection, objActionCollection, pidActionCollection,random
                    ,objOItem.getId_P(), oDates,oTasks,mergeJ,0);
        }

        // 判断递归结果是否为空
        if (objActionCollection.size() <= 0) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RECURSION_RESULT_IS_NULL.getCode(), "递归结果为空");
        }

        ////////////////////////////////////// Get Dep.objlBProd for grpP

        /////////// setup Dep.objlBProd + objlSProd for grpP

        //putting the Sales order as the last casItem... I donno why
        JSONObject thisOrderData = new JSONObject();
        thisOrderData.put("id_C", myCompId);
        thisOrderData.put("id_O", id_OParent);
        thisOrderData.put("lST", 4);
        thisOrderData.put("type", 1);
        thisOrderData.put("size", oParent_objItem.size());
        thisOrderData.put("wrdN", salesOrderData.getInfo().getWrdN());
        casItemData.add(thisOrderData);

        System.out.println("oDates:");
        System.out.println(JSON.toJSONString(oDates));
        System.out.println("oTasks:");
        System.out.println(JSON.toJSONString(oTasks));

//        // 调用任务最后处理方法
//        timeZjService.setZui(teDaF,random,myCompId);

        // 获取递归结果键
        Set<String> actionCollection = objActionCollection.keySet();
        int i = 1;
        // 遍历键，并创建采购单
        for (String thisOrderId : actionCollection) {

            // 获取对应订单id的零件递归信息
            List<OrderAction> unitAction = objActionCollection.get(thisOrderId);
            // 获取对应订单id的零件信息
            List<OrderOItem> unitOItem = objOItemCollection.get(thisOrderId);

            // 创建订单info
            String prodCompId = "";
            JSONObject orderNameCas = new JSONObject();
            String targetCompId = "";
            if (id_OParent.equals(thisOrderId)) {
                targetCompId = salesOrderData.getInfo().getId_CB();
            } else {
                targetCompId = myCompId;
            }
            for (int j = 0; j < casItemData.size(); j++) {
                if (casItemData.getJSONObject(j).getString("id_O").equals(thisOrderId)) {
                    prodCompId = casItemData.getJSONObject(j).getString("id_C");
                    orderNameCas = casItemData.getJSONObject(j).getJSONObject("wrdN");
                    break;
                }
            }

            JSONObject grpBGroup = new JSONObject();
            JSONObject grpGroup = new JSONObject();

            String aId = redisUtils.getId_A(targetCompId, "a-auth");

            // if it is a real Company get grpB setting from objlBProd by ref, else do nothing now, later can do extra
            if (!aId.equals("none")) {

                Asset asset = redisUtils.getAssetById(aId, Collections.singletonList("def"));
                JSONArray defResultBP = asset.getDef().getJSONArray("objlBProd");
                JSONArray defResultBC = asset.getDef().getJSONArray("objlBComp");

                for (int i1 = 0; i1 < unitOItem.size(); i1++) {
                    if (grpBGroup.getJSONObject(unitOItem.get(i1).getGrpB()) == null) {
                        for (int i2 = 0; i2 < defResultBP.size(); i2++) {
                            if (unitOItem.get(i1).getGrpB().equals(defResultBP.getJSONObject(i2).getString("ref")))
                                grpBGroup.put(unitOItem.get(i1).getGrpB(), defResultBP.getJSONObject(i2));
                        }
                    }
                }
//                    for (int i2 = 0; i2 < assetGrpChecker.getDef().getJSONArray("objlBComp").size(); i++) {
//                        String grpRef = assetGrpChecker.getDef().getJSONArray("objlBComp").getJSONObject(i).getString("ref");
//
//                        if (grpRef.equals(oItemData.getString(grpType)))
//                        {
//                            grpA =  assetGrpChecker.getDef().getJSONArray(objListType).getJSONObject(i).getString("grpO");
//                            break;
//                        }
//                    }
            }


            String aId2 = redisUtils.getId_A(prodCompId, "a-auth");

            // if it is a real Company get grpB setting from objlBProd by ref, else do nothing now, later can do extra
            if (!aId2.equals("none")) {

                Asset asset = redisUtils.getAssetById(aId2, Collections.singletonList("def"));
                JSONArray defResultSP = asset.getDef().getJSONArray("objlSProd");

                for (int i1 = 0; i1 < unitOItem.size(); i1++) {
                    if (grpGroup.getJSONObject(unitOItem.get(i1).getGrp()) == null) {
                        for (int i2 = 0; i2 < defResultSP.size(); i2++) {
                            if (unitOItem.get(i1).getGrp().equals(defResultSP.getJSONObject(i2).getString("ref")))
                                grpGroup.put(unitOItem.get(i1).getGrp(), defResultSP.getJSONObject(i2));
                        }
                    }
                }
            }

//            System.out.print("got all ok");

            if (id_OParent.equals(thisOrderId)) {
                System.out.println("修改订单，订单号:"+thisOrderId);
                // make sales order Action
                this.updateSalesOrder(casItemData, unitAction, unitOItem, salesOrderData, grpBGroup, grpGroup, prodCompId,oDates,oTasks);

            } else {
                System.out.println("新增订单，订单号:"+thisOrderId);
                // else make Purchase Order
//                // 创建订单
                Order newPO = new Order();

                // 根据键设置订单id
                newPO.setId(thisOrderId);
//                System.out.print("got1" + thisOrderId);

                // priority is BY order, get from info and write into ALL oItem
                OrderInfo newPO_Info = new OrderInfo(prodCompId, targetCompId, unitOItem.get(0).getId_CP(), "", id_OParent, "", "", "1000", "1000", oParent_prior, "", 4, 0, orderNameCas, null);

                // 设置订单info信息
                newPO.setInfo(newPO_Info);

                // 添加View信息
                JSONArray view = new JSONArray();
                view.add("info");
                view.add("action");
                view.add("oItem");
                view.add("oStock");
                newPO.setView(view);

//                System.out.print("got2 + ");

                Double wn2qty = 0.0;
                Double wn4price = 0.0;


                for (int k = 0; k < unitOItem.size(); k++) {
                    wn2qty += unitOItem.get(k).getWn2qtyneed();
                    wn4price += unitOItem.get(k).getWn4price();
//                    System.out.println("u " + unitOItem.get(k));
                }

                // 添加OItem信息
                JSONObject newPO_OItem = new JSONObject();
                newPO_OItem.put("objItem", unitOItem);
                newPO_OItem.put("wn2qty", wn2qty);
                newPO_OItem.put("wn4price", wn4price);
                newPO.setOItem(newPO_OItem);

                // 创建采购单的Action
                JSONObject newPO_Action = new JSONObject();
                newPO_Action.put("objAction", unitAction);
                newPO_Action.put("grpBGroup", grpBGroup);
                newPO_Action.put("grpGroup", grpGroup);
                newPO_Action.put("wn2progress", 0.0);

                //Create oStock
                JSONObject newPO_oStock = new JSONObject();
                newPO_oStock.put("objData", new JSONArray());
//                System.out.print("got3" + thisOrderId);

                for (int k = 0; k < unitOItem.size(); k++) {
                    JSONObject initStock = new JSONObject();
                    initStock.put("wn2qtynow", 0);
                    initStock.put("wn2qtymade", 0);
                    initStock.put("id_P", unitOItem.get(k).getId_P());
                    newPO_oStock.getJSONArray("objData").add(initStock);
                }
                newPO.setOStock(newPO_oStock);
//                System.out.print("got4" + thisOrderId);

                newPO.setAction(newPO_Action);
                // 新增订单
                coupaUtil.saveOrder(newPO);
//                System.out.print("got all saved" + newPO.getInfo().getWrdN());


//              // 创建lSBOrder订单
                lSBOrder lsbOrder = new lSBOrder(prodCompId, targetCompId, "", "", id_OParent, thisOrderId,
                        "", "", null, null, "", 4, 0, orderNameCas, null, null);
                // 新增lsbOrder信息
                coupaUtil.updateES_lSBOrder(lsbOrder);
            }
        }
        System.out.println("all finished...");
        // END FOR

        // 创建返回结果存储集合
        JSONObject result = new JSONObject();

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 添加订单方法 - 注释完成
     * ##param keyOfResult	递归结果键
     *
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 8:40
     */
    public void updateSalesOrder(JSONArray casItemData, List<OrderAction> salesAction, List<OrderOItem> salesOItem,
                                 Order orderParentData, JSONObject grpBGroup, JSONObject grpGroup, String myCompId,
                                 List<OrderODate> oDates,List<Task> oTasks) {

        // 添加订单基础信息存储
        JSONObject casItemx = new JSONObject();
        JSONObject nowData = new JSONObject();
        nowData.put("objOrder", casItemData);
        casItemx.put(myCompId, nowData);

        // 创建产品零件递归信息
        JSONObject salesOrder_Action = new JSONObject();

        // 添加对应的产品零件递归信息
        salesOrder_Action.put("objAction", salesAction);
        salesOrder_Action.put("isDg", "true");
        salesOrder_Action.put("grpBGroup", grpBGroup);
        salesOrder_Action.put("grpGroup", grpGroup);
        salesOrder_Action.put("wn2progress", 0.0);
        salesOrder_Action.put("oDates",oDates);
        salesOrder_Action.put("oTasks",oTasks);


        // 设置action信息
        orderParentData.setAction(salesOrder_Action);
        // 设置订单的递归结果map
        orderParentData.setCasItemx(casItemx);

        JSONArray view = orderParentData.getView();
        System.out.print("got all ok Sales");

        //Create oStock
        JSONObject newPO_oStock = new JSONObject();
        newPO_oStock.put("objData", new JSONArray());
        for (int k = 0; k < salesOItem.size(); k++) {
            JSONObject initStock = new JSONObject();
            initStock.put("wn2qtynow", 0);
            initStock.put("wn2qtymade", 0);
            initStock.put("id_P", salesOItem.get(k).getId_P());
            newPO_oStock.getJSONArray("objData").add(initStock);
        }
        orderParentData.setOStock(newPO_oStock);

        if (!view.contains("action")) {
            view.add("action");
        }
        if (!view.contains("casItemx")) {
            view.add("casItemx");
        }
        if (!view.contains("oStock")) {
            view.add("oStock");
        }
        // 设置view值
        orderParentData.setView(view);

        // 新增订单
        coupaUtil.saveOrder(orderParentData);
    }


    /**
     * 递归方法 - 注释完成
     * //     * ##param id_P	产品id
     * //     * ##param newObjectId    随机id
     * //     * ##param priority  序号
     * //     * ##param objOItemCollection   oitem递归数据集合
     * //     * ##param objActionCollection  action递归数据集合
     * //     * ##param orderIds  订单id集合
     * //     * ##param compIds   公司id集合
     * //     * ##param id_O   订单id
     * //     * ##param pidActionCollection    action递归数据重复集合
     * //     * ##param orderCompPosition 存储订单对应公司的职位集合
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/5/29 14:20
     */
    // get id_P product info,
    // make objAction
    // get ES info for objOItem
    // make grpBGroup
    // make new id_O if needed
    // set OUpper, ONext and with
    // write action/oItem collection
    // write pidActionCollection
    // write subParts
    // check repeat => mergeparts
    // check bmdpt == 2 then dg again
    // else stop


    //KEV subParts + 1 self-Task
    // Sales order subPart has DG44-4
    // sumChild = 4? for those with NO subTask, it turns into 4
    private void dgProcess(Integer dgType, String myCompId, String id_OParent, OrderOItem upperOItem
            , OrderAction upperAction, JSONArray casItemData, JSONArray partArray, Integer partIndex,
            Map<String, List<OrderOItem>> objOItemCollection, Map<String, List<OrderAction>> objActionCollection,
            Map<String, OrderAction> pidActionCollection,String random,String id_PF
            ,List<OrderODate> oDates,List<Task> oTasks,JSONObject mergeJ,int csSta) {
        isJsLj++;
        int dq = isJsLj;
        // 存储序号是否为1层级
        int csStaN = 0;
        // 获取父id是否是当前唯一ID存储时间处理的最初产品编号存储
        boolean isPf = id_PF.equals(csId_P.getString(random));
        // 判断上一个序号是否为1层级
        if (csSta == 1) {
            // 判断是，则将自己也设置为是
            csStaN = 1;
        }
        String id_P = partArray.getJSONObject(partIndex).getString("id_P");
//        System.out.println("id_P:"+id_P);
        JSONObject partInfo = partArray.getJSONObject(partIndex);
        String prodCompId = partInfo.getString("id_C");

        // 获取, 设新 当前订单id
        // 定义递归订单id
        String newOrderId = "";
        Integer newOrderIndex = 0;

        // 如果是第一层就直接赋值，说明是订单，不是生产单
        if (dgType.equals(1)) {
            newOrderId = id_OParent;
            newOrderIndex = partIndex;
        }
        // if this is a task, and it's not Sales layer
        else if (id_P.equals("") && dgType.equals(2)) {
            if (partIndex.equals(0)) {
                //if I am the first one, make new Order
                newOrderId = (MongoUtils.GetObjectId());

                //same detail into casItemx
                JSONObject thisOrderData = new JSONObject();
                thisOrderData.put("id_C", myCompId);
                thisOrderData.put("id_O", newOrderId);
                thisOrderData.put("lST", 4);
                thisOrderData.put("size", 1);
                thisOrderData.put("type", 4); //task - subTask
                JSONObject casWrdN = new JSONObject();
                casWrdN.put("cn", upperOItem.getWrdN().getString("cn") + "的子任务单");
                thisOrderData.put("wrdN", casWrdN);
                casItemData.add(thisOrderData);
            } else {
                // not the first one, get Id_O from casItemData
                int casIndex = casItemData.size();
                newOrderId = casItemData.getJSONObject(casIndex - 1).getString("id_O");
                newOrderIndex = partIndex;
                casItemData.getJSONObject(casIndex - 1).put("size", partIndex + 1);
            }
        } else if (null != pidActionCollection.get(id_P)) {   //Now, it is prod not task， Must Merge if id_P is in pidActionCollection
            // as long as you have it in pidArray, you MUST merge
            System.out.println("I merged2////");
//            System.out.println(id_P+" - "+mergeJ.getInteger(id_P));
//            System.out.println(pidActionCollection.get(id_P).getId_O());
//            System.out.println(pidActionCollection.get(id_P).getIndex());
            String fin_O = pidActionCollection.get(id_P).getId_O();
            Integer fin_Ind = pidActionCollection.get(id_P).getIndex();
            partInfo.put("fin_O", fin_O);
            partInfo.put("fin_Ind", fin_Ind);
            // 获取存储零件编号的合并信息记录合并时的下标
            Integer ind = mergeJ.getInteger(id_P);
            // 创建时间操作处理信息存储类
            OrderODate orderODate = new OrderODate();
            // 添加时间处理信息
            orderODate.setEmpty(true);
            orderODate.setLinkInd(ind);
            orderODate.setPriorItem(objOItemCollection.get(fin_O).get(fin_Ind).getWn0prior());
            orderODate.setId_PF(id_PF);
            orderODate.setId_C(myCompId);
            // 判断父编号是当前唯一ID存储时间处理的最初产品编号并且序号为1
            if (isPf&&orderODate.getPriorItem() == 1) {
                // 设置序号是为1层级
                csStaN = 1;
            }
            // 添加信息
            orderODate.setCsSta(csStaN);
            oDates.add(orderODate);
            // 创建一个任务类
            Task task = new Task();
            // 随便添加一个信息
            task.setPriority(-2);
            // 使用一个空任务对象来与时间处理信息对齐
            oTasks.add(task);
            this.mergePart(id_P, partArray, partIndex, upperAction, upperOItem,
                    upperOItem.getWn2qtyneed(),
                    pidActionCollection,
                    objActionCollection, objOItemCollection,oDates,mergeJ
            );
            return;
        } else {
            // Now need to check if prodCompId is in casItemData, if so, get and append 1 more prod in that order
            // 根据公司获取递归订单id
            boolean isNew = true;

            for (int k = 0; k < casItemData.size(); k++) {
//                System.out.println("casItem" + casItemData.getJSONObject(k));
                // 2 cases: prod is mine, check only if casItemx Type =2 and not 4
                if (prodCompId.equals(myCompId) &&
                        prodCompId.equals(casItemData.getJSONObject(k).getString("id_C")) &&
                        casItemData.getJSONObject(k).getString("type").equals(2) ||
                        prodCompId.equals(casItemData.getJSONObject(k).getString("id_C"))) {

                    newOrderId = casItemData.getJSONObject(k).getString("id_O");
                    newOrderIndex = casItemData.getJSONObject(k).getInteger("size");
                    casItemData.getJSONObject(k).put("size", newOrderIndex + 1);
                    isNew = false;
                }
            }
            if (isNew) {

                // 赋值随机id
                newOrderId = (MongoUtils.GetObjectId());
                newOrderIndex = 0;

                // 递归订单map添加随机id
                JSONObject orderName = new JSONObject();
                if (prodCompId.equals(myCompId)) {
                    orderName.put("cn", upperAction.getRefOP() + " 派工单-00" + casItemData.size());

                } else {
                    orderName.put("cn", upperAction.getRefOP() + " 采购单-00" + casItemData.size());
                }

                JSONObject thisOrderData = new JSONObject();
                thisOrderData.put("id_C", prodCompId);
                thisOrderData.put("id_O", newOrderId);
                thisOrderData.put("lST", 4);
                thisOrderData.put("size", 1);
                thisOrderData.put("type", 2); //1=Sales 2=regular 3=dep task 4=subTask
                thisOrderData.put("wrdN", orderName);
                casItemData.add(thisOrderData);
            }
        }

        //看看有没有， 没有就创建新的 oItem / action
        List<OrderOItem> orderOItemList;
        if (null != objOItemCollection.get(newOrderId)) {
            orderOItemList = objOItemCollection.get(newOrderId);
        } else {
            orderOItemList = new ArrayList<>();
        }
        List<OrderAction> orderActionList;
        if (null != objActionCollection.get(newOrderId)) {
            // 如果不为空则获取
            orderActionList = objActionCollection.get(newOrderId);
        } else {
            orderActionList = new ArrayList<>();
        }

        OrderOItem objOItem;
        OrderAction objAction;
        // 设置订单零件值
        if (dgType.equals(1)) {
//            objOItem.setId_CB(partInfo.getString("id_CB"));
//            objOItem.setWn2qtyneed(upperOItem.getWn2qtyneed());
            objOItem = upperOItem;
            objAction = upperAction;
        } else if (id_P.equals("")) {
            System.out.println("Upp" + newOrderId);
            System.out.println("upp" + partArray.size() + partArray.getJSONObject(0).getJSONObject("wrdN"));
            System.out.println("upp" + partIndex);
            JSONObject unitTask = partArray.getJSONObject(partIndex);

            objOItem = new OrderOItem("", upperOItem.getId_O(), myCompId, myCompId, myCompId, newOrderId, partIndex, 1, "", "",
                    "1009", upperOItem.getGrpB(), unitTask.getInteger("wn0prior"), "", 0, 0, 1.0, 0.0,
                    upperOItem.getWrdN(), unitTask.getJSONObject("wrdN"), unitTask.getJSONObject("wrddesc"), unitTask.getJSONObject("wrdprep"));

            objOItem.setSeq(unitTask.getString("seq"));
            objAction = new OrderAction(100, 0, 1, 1, id_OParent, upperAction.getRefOP(), "", newOrderId, partIndex,
                    unitTask.getInteger("wn0prior"), 0, 0, null, null, null, null,
                    upperOItem.getWrdN(), unitTask.getJSONObject("wrdN"));

            System.out.println("oItem" + objOItem);


            JSONObject upPrntsData = new JSONObject();
            upPrntsData.put("id_O", upperOItem.getId_O());
            upPrntsData.put("index", upperOItem.getIndex());
            upPrntsData.put("wrdN", upperOItem.getWrdN());
            upPrntsData.put("wn2qtyneed", 1);
            upPrntsData.put("wn2qtyall", upperOItem.getWn2qtyneed());
            objAction.getUpPrnts().add(upPrntsData);

        } else {
            objOItem = new OrderOItem(id_P, upperOItem.getId_OP(),
                    partInfo.getString("id_CP") == null ? prodCompId : partInfo.getString("id_CP"),
                    prodCompId, myCompId,
                    newOrderId, newOrderIndex,
                    partInfo.getInteger("bmdpt"), partInfo.getString("ref"), partInfo.getString("refB"),
                    partInfo.getString("grp"), partInfo.getString("grpB"), partInfo.getInteger("wn0prior"), partInfo.getString("pic"),
                    partInfo.getInteger("lUT"), partInfo.getInteger("lCR"),
                    0.0,
                    partInfo.getDouble("wn4price"), upperOItem.getWrdN(), partInfo.getJSONObject("wrdN"),
                    partInfo.getJSONObject("wrddesc"), upperOItem.getWrddesc());

            objOItem.setTmd(dateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));
            objOItem.setSeq("3"); // set DGAction specific seq = 3
            objOItem.setWn2qtyneed(upperOItem.getWn2qtyneed() * partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));

            objAction = new OrderAction(100, 0, 1, partInfo.getInteger("bmdpt"),
                    id_OParent, upperAction.getRefOP(), partInfo.getString("id_P"), newOrderId, newOrderIndex, partInfo.getInteger("wn0prior"), 0
                    , 0, null, null, null, null, upperOItem.getWrdN(), partInfo.getJSONObject("wrdN"));

            objAction.setPriority(0);
            JSONObject upPrntsData = new JSONObject();
            upPrntsData.put("id_O", upperOItem.getId_O());
            upPrntsData.put("index", upperOItem.getIndex());
            upPrntsData.put("wn2qtyall", upperOItem.getWn2qtyneed());
            upPrntsData.put("wn2qtyneed", objOItem.getWn2qtyneed());
            upPrntsData.put("wrdN", upperOItem.getWrdN());
            objAction.getUpPrnts().add(upPrntsData);

        }

        if (partIndex == 0 && objAction.getBmdpt() == 1) {
            upperAction.setBmdpt(4);
        }

        // 添加产品信息
        orderActionList.add(objAction);
//        objActInd = orderActionList.size()-1;
        orderOItemList.add(objOItem);

        objActionCollection.put(newOrderId, orderActionList);
        partInfo.put("fin_O", newOrderId);
        partInfo.put("fin_Ind", newOrderIndex);

        objOItemCollection.put(newOrderId, orderOItemList);

        // 判断递归bmdpt等于物料或者等于部件, 是就放pidActionCollection，  1/4不放
        if (objAction.getBmdpt() == 3 || objAction.getBmdpt() == 2) {
            pidActionCollection.put(objOItem.getId_P(), objAction);
        }

        // 循环遍历减一的序号集合
        // NOW I am the last Item in the partArray / subTasks, I can then update ALL the previous Items' wn0prior
        if (partIndex == partArray.size() - 1) {
            Boolean keepGoing;

            for (int i = 0; i < partArray.size(); i++) {
                String finO = partArray.getJSONObject(i).getString("fin_O");
                Integer fin_Ind = partArray.getJSONObject(i).getInteger("fin_Ind");
                // this is myself
                OrderAction unitAction = objActionCollection.get(finO).get(fin_Ind);

                Integer myPrior = partArray.getJSONObject(i).getInteger("wn0prior");
                keepGoing = true;
                Integer checkPrev = i - 1;

                do {
                    // if it's already the first item or prior 2 steps away, stop
                    if (checkPrev < 0 || (myPrior - 2) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                        keepGoing = false;
                    } else if (myPrior == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                        // if prior Same, skip

                    } else if ((myPrior - 1) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                        // else prior need add PrtPrev
                        JSONObject idAndIndex = new JSONObject();
                        idAndIndex.put("id_O", partArray.getJSONObject(checkPrev).getString("fin_O"));
                        idAndIndex.put("index", partArray.getJSONObject(checkPrev).getInteger("fin_Ind"));
                        // Here, I put the checking IdIndex into my own list of prtPrev
                        unitAction.getPrtPrev().add(idAndIndex);
                    }
                    checkPrev--; // move 1 step previous
                } while (keepGoing);
                unitAction.setSumPrev(unitAction.getPrtPrev().size());

                keepGoing = true;
                Integer checkNext = i + 1;

                do {
                    if (checkNext == partArray.size()) {
                        keepGoing = false;
                    } else if ((myPrior + 2) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                        keepGoing = false;
                    } else if (myPrior == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                    } else if ((myPrior + 1) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                        JSONObject idAndIndex = new JSONObject();
                        idAndIndex.put("id_O", partArray.getJSONObject(checkNext).getString("fin_O"));
                        idAndIndex.put("index", partArray.getJSONObject(checkNext).getInteger("fin_Ind"));
                        // Here, I put the checking IdIndex into my own list of prtPrev
                        unitAction.getPrtNext().add(idAndIndex);
                    }
                    checkNext++;
                } while (keepGoing);
            }
        }

        if (isPf&&objOItem.getWn0prior() == 1) {
            csStaN = 1;
        }
        if (!dgType.equals(1)) {
            // 创建JSONObject存储子零件信息
            JSONObject subPartData = new JSONObject();
            // 设置子零件信息
            subPartData.put("id_O", objAction.getId_O());
            subPartData.put("index", objAction.getIndex());
            subPartData.put("id_P", id_P);
            subPartData.put("wrdN", objOItem.getWrdN());
            subPartData.put("prior", objAction.getPriority());
            subPartData.put("upIndex", objAction.getUpPrnts().size() - 1);
            subPartData.put("qtyEach", partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));

            // 递归零件信息设置子零件集合
            if (upperAction.getSubParts() == null) {
                JSONArray subPart = new JSONArray();
                subPart.add(subPartData);
                upperAction.setSubParts(subPart);
            } else {
                upperAction.getSubParts().add(subPartData);
            }
            upperAction.setSumChild(partArray.size());
        }
        if (dq == 1) {
            csId_P.put(random,objOItem.getId_P());
        }

        //////////////// prtPrev sumPrev, prtNext, subParts SumChild 3 things set here, then prnt set above

        //////////////////// if this Item has part, go into dgProc
        if (!id_P.equals("")) {
            Prod thisProd = coupaUtil.getProdByListKey(id_P, Arrays.asList("_id", "part", "info"));

            if (thisProd != null && thisProd.getPart() != null && thisProd.getPart().getJSONArray("objItem").size() > 0 &&
                    thisProd.getInfo().getId_C().equals(myCompId)) {
                JSONArray partArrayNext = thisProd.getPart().getJSONArray("objItem");
                objAction.setBmdpt(2);
                // the first item's bmdpt != 6, if ==6 then ALL parts in that part will == 6, then need pick by igura
                if (partArrayNext.getJSONObject(0).getInteger("bmdpt") != 6) {
                    if (partArrayNext != null) {
                        // 进下一层处理part递归
                        for (int item = 0; item < partArrayNext.size(); item++) {
                            this.dgProcess(2, myCompId, id_OParent, objOItem, objAction, casItemData,
                                    partArrayNext, item, objOItemCollection, objActionCollection,
                                    pidActionCollection,random, thisProd.getId(),oDates,oTasks,mergeJ,csStaN);
                        }
                    }
                }
            }
        } else {
            if (objOItem.getSubTask() != null && objOItem.getSubTask().size() > 0) {
                for (int item = 0; item < objOItem.getSubTask().size(); item++) {
                    this.dgProcess(2, myCompId, newOrderId, objOItem, objAction, casItemData
                            , objOItem.getSubTask(), item, objOItemCollection, objActionCollection,
                            pidActionCollection,random,id_PF,oDates,oTasks,mergeJ,csStaN);
                }
            }
        }

        if (dq != 1) {
//            System.out.println("bmdpt:"+objOItem.getBmdpt());
            // 创建订单时间操作处理专用类
            OrderODate orderODate = new OrderODate();
            // 添加时间操作处理信息
            orderODate.setId_PF(id_PF);
            orderODate.setId_P(objOItem.getId_P());
            orderODate.setPriorItem(objOItem.getWn0prior());
            orderODate.setTeStart(0L);
            orderODate.setTaFin(0L);
            // 判断层级为第一层并且序号为1
            if (csStaN == 1 && objOItem.getWn0prior() == 1) {
                // 添加信息
                orderODate.setKaiJie(1);
            } else {
                // 判断序号为1 - 如果是第一层并且是部件
                if (objOItem.getWn0prior() == 1) {
                    // 添加信息
                    orderODate.setKaiJie(4);
                } else {
                    // 添加信息
                    orderODate.setKaiJie(2);
                }
            }
            // 添加信息
            orderODate.setCsSta(csStaN);
            // 设置订单时间操作信息
            orderODate.setTeDur(partInfo.getLong("teDur")==null?120:partInfo.getLong("teDur"));
            orderODate.setTePrep(partInfo.getLong("tePrep")==null?60:partInfo.getLong("tePrep"));
            // action里面的
            orderODate.setPriority(0);
            orderODate.setTeDurTotal(0L);
            orderODate.setWn2qtyneed(objOItem.getWn2qtyneed());
            // 判断bmdpt等于部件
            if (objAction.getBmdpt() == 2) {
                // 设置订单时间操作信息
//                orderODate.setTeDurTotal(0L);
                orderODate.setTePrep(180L);
                orderODate.setTeDur(0L);
                // 判断层级为第一层
                if (csStaN == 1) {
                    orderODate.setKaiJie(3);
                } else {
                    // 添加信息
                    orderODate.setKaiJie(5);
                }
            }
//            else {
////                System.out.println("teDur:"+orderODate.getTeDur()+",qty:"+objOItem.getWn2qtyneed()+",num:"+orderODate.getGrpUNum());
//                // 设置订单时间操作信息
//                orderODate.setTeDurTotal((long) ((orderODate.getTeDur()*objOItem.getWn2qtyneed())/orderODate.getGrpUNum()));
//            }
//            System.out.println("wn2qtyneed:"+objOItem.getWn2qtyneed()+" - teDurTotal:"
//                    +orderODate.getTeDurTotal()+" - tePrep:"+orderODate.getTePrep()+" - prior:"+objOItem.getWn0prior());
//            System.out.println("csTeJ:"+" - id_P:"+objOItem.getId_P()+" - id_PF:"+id_PF);
            // 添加信息
            orderODate.setId_O(objAction.getId_O());
            orderODate.setId_C(myCompId);
            orderODate.setIndex(objAction.getIndex());
            orderODate.setGrpB(objOItem.getGrpB());
            oDates.add(orderODate);
            // 判断存储零件编号的合并信息记录合并时的下标为空
            if (null == mergeJ.getInteger(objOItem.getId_P())) {
//                System.out.println("合并记录，为空");
                // 添加下标信息
                mergeJ.put(objOItem.getId_P(),oDates.size()-1);
            }
            // 获取零件名称
            String itemWrdN = objOItem.getWrdN().getString("cn");
            // 调用生成任务信息
            Task task = Obj.getTask(orderODate.getTeStart(), orderODate.getTeFin(), orderODate.getId_O()
                    , orderODate.getIndex(), 0L
                    , orderODate.getPriority(), itemWrdN, orderODate.getTePrep(),orderODate.getTeDelayDate()
                    ,myCompId,0L,0L);
            // 设置任务公司编号
            task.setId_C(myCompId);
            System.out.println("task:");
            System.out.println(JSON.toJSONString(task));
            oTasks.add(task);
        }
    }

    private void mergePart(String id_P, JSONArray partArray, Integer partIndex,
                           OrderAction upperAction, OrderOItem upperOItem, Double qtyNeed, Map<String, OrderAction> pidActionCollection,
                           Map<String, List<OrderAction>> objActionCollection, Map<String
            , List<OrderOItem>> objOItemCollection,List<OrderODate> oDates,JSONObject mergeJ) {

        Integer checkPrev = partIndex - 1;
        Integer checkNext = partIndex + 1;
        Boolean keepGoing = true;
        String finO = pidActionCollection.get(id_P).getId_O();
        Integer fin_Ind = pidActionCollection.get(id_P).getIndex();
        Integer myPrior = partArray.getJSONObject(partIndex).getInteger("wn0prior");

        // this is my Action::
        OrderOItem unitOItem = objOItemCollection.get(finO).get(fin_Ind);

        OrderAction unitAction = objActionCollection.get(finO).get(fin_Ind);
        do {
            if (checkPrev < 0 || (myPrior - 2) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                keepGoing = false;
            } else if (myPrior == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
            } else if ((myPrior - 1) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                JSONObject idAndIndex = new JSONObject();
                idAndIndex.put("id_O", finO);
                idAndIndex.put("index", fin_Ind);
                // Here, I put the checking IdIndex into my own list of prtPrev
                unitAction.getPrtPrev().add(idAndIndex);
            }
            checkPrev--;
        } while (keepGoing);

        unitAction.setSumPrev(unitAction.getPrtPrev().size());
        keepGoing = true;

        do {
            if (checkNext == partArray.size()) {
                keepGoing = false;
            } else if ((myPrior + 2) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                keepGoing = false;
            } else if (myPrior == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                System.out.println("Same Prior, do nothing here");
            }
            checkNext++;
        } while (keepGoing);


        JSONObject upPrntsData = new JSONObject();
        upPrntsData.put("id_O", upperOItem.getId_O());
        upPrntsData.put("index", upperOItem.getIndex());
        upPrntsData.put("wn2qtyall", upperOItem.getWn2qtyneed());
        upPrntsData.put("wn2qtyneed", unitOItem.getWn2qtyneed());
        upPrntsData.put("wrdN", upperOItem.getWrdN());

        unitAction.getUpPrnts().add(upPrntsData);

        // 创建JSONObject存储子零件信息
        JSONObject subPartData = new JSONObject();
        // 设置子零件信息
        subPartData.put("id_O", finO);
        subPartData.put("index", fin_Ind);
        subPartData.put("id_P", id_P);
        subPartData.put("upIndex", unitAction.getUpPrnts().size() - 1);
        subPartData.put("wrdN", partArray.getJSONObject(partIndex).getJSONObject("wrdN"));
        subPartData.put("prior", partArray.getJSONObject(partIndex).getInteger("wn0prior"));
        subPartData.put("qtyEach", partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));

        // 递归零件信息设置子零件集合
        if (upperAction.getSubParts() == null) {
            JSONArray subPart = new JSONArray();
            subPart.add(subPartData);
            upperAction.setSubParts(subPart);
        } else {
            upperAction.getSubParts().add(subPartData);
        }
        upperAction.setSumChild(partArray.size());

//        System.out.println("qtyNeed:"+qtyNeed);
//        System.out.println("wn4:"+partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));
        // Loop into all subParts to make sure all qty is added correctly
        this.dgMergeQtySet(qtyNeed, partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"),
                finO, fin_Ind, objActionCollection, objOItemCollection,oDates,mergeJ);

    }

    private void dgMergeQtySet(Double qtyNeed, Double qtySubNeed, String finO, Integer fin_Ind
            , Map<String, List<OrderAction>> objActionCollection, Map<String, List<OrderOItem>> objOItemCollection
            ,List<OrderODate> oDates,JSONObject mergeJ
    ) {
        OrderOItem unitOItem = objOItemCollection.get(finO).get(fin_Ind);
        OrderAction unitAction = objActionCollection.get(finO).get(fin_Ind);
        Integer ind = mergeJ.getInteger(unitOItem.getId_P());
        OrderODate oDa = oDates.get(ind);

        Double currentQty = unitOItem.getWn2qtyneed();
//        System.out.println("currentQty:"+currentQty);
//        System.out.println("qtyNeed:"+qtyNeed);
//        System.out.println("qtySubNeed:"+qtySubNeed);
//        System.out.println("idp:"+unitOItem.getId_P());

//        unitOItem.setWn2qtyneed(currentQty + qtyNeed * qtySubNeed);
//        System.out.println("unitOItem.getWn2qtyneed():"+unitOItem.getWn2qtyneed());
//        System.out.println("oDa.wn2:"+oDa.getWn2qtyneed());
        oDa.setWn2qtyneed(oDa.getWn2qtyneed()+currentQty);
//        System.out.println("oDa.wn2:"+oDa.getWn2qtyneed());
//        System.out.println("unit:"+unitOItem.getWn2qtyneed());
//        System.out.println("teDurTotal-q:"+oDa.getTeDurTotal());
//        oDa.setTeDurTotal((long)((oDa.getTeDur()*unitOItem.getWn2qtyneed())/oDa.getGrpUNum()));
//        System.out.println("teDurTotal:"+oDa.getTeDurTotal());
        oDates.set(ind,oDa);

        for (int i = 0; i < unitAction.getSubParts().size(); i++) {
//            System.out.println("进入循环:");
            JSONObject unit = unitAction.getSubParts().getJSONObject(i);
            this.dgMergeQtySet(qtyNeed * qtySubNeed, unit.getDouble("qtyEach"),
                    unit.getString("id_O"), unit.getInteger("index"), objActionCollection
                    , objOItemCollection,oDates,mergeJ);

        }
    }

    /**
     * 递归验证 - 注释完成
     * ##param id_P 产品编号
     *
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 9:03
     * DONE Check Kev
     */
    @Override
    public ApiResponse dgCheck(String id_P, String id_C) {

        // 创建层级存储
        int layer = 0;
        // 创建异常信息存储
        JSONArray isRecurred = new JSONArray();
        // 创建产品信息存储
        JSONArray isEmpty = new JSONArray();
        // 创建返回结果
        JSONObject result = new JSONObject();
        // 创建零件id集合
        JSONArray pidList = new JSONArray();
        JSONObject nextPart = new JSONObject();

        // ******调用验证方法******
        this.dgCheckUtil(pidList, id_P, id_C, nextPart, isRecurred, layer, isEmpty);

        // 添加到返回结果
        result.put("recurred", isRecurred);
        result.put("isNull", isEmpty);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    /**
     * 递归验证工具 - 注释完成
     * ##param pidList	产品编号集合
     * ##param id_P	产品编号
     * ##param objectMap	零件信息
     * ##param isRecurred	产品状态存储map
     * ##param layer	层级
     * ##param isEmpty	异常信息存储集合
     *
     * @return void  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/11/16 9:25
     * DONE Check Kev
     */
    private void dgCheckUtil(JSONArray pidList, String id_P, String id_C, JSONObject objectMap
            , JSONArray isRecurred, Integer layer, JSONArray isEmpty) {

        // 根据父编号获取父产品信息
        Prod thisItem = coupaUtil.getProdByListKey(id_P, Arrays.asList("part", "info"));
        // 层级加一
        layer++;
        boolean isConflict = false;
        JSONArray checkList = new JSONArray();

        // 判断父产品不为空，部件父产品零件不为空
        if (null != thisItem) {

            for (int i = 0; i < pidList.size(); i++) {
//                System.out.println("冲突Check" + id_P);
                // 判断编号与当前的有冲突
                if (pidList.getString(i).equals(id_P)) {
                    // 创建零件信息
                    JSONObject conflictProd = new JSONObject();
                    // 添加零件信息
                    conflictProd.put("id_P", id_P);
                    conflictProd.put("layer", (layer + 1));
                    conflictProd.put("index", i);
                    // 添加到结果存储
                    isRecurred.add(conflictProd);
                    // 设置为有冲突
                    isConflict = true;
                    // 结束
                    break;
                }
            }

            if (!isConflict) {
                checkList = (JSONArray) pidList.clone();
                checkList.add(id_P);
            }

            // 获取prod的part信息
            if (!isConflict &&
                    null != thisItem.getPart() &&
                    thisItem.getInfo().getId_C().equals(id_C) &&
                    null != thisItem.getPart().get("objItem")) {
                JSONArray nextItem = thisItem.getPart().getJSONArray("objItem");
                // 遍历零件信息1
                for (int j = 0; j < nextItem.size(); j++) {
                    // 判断零件不为空并且零件编号不为空
                    if (null != nextItem.get(j) && null != nextItem.getJSONObject(j).get("id_P")) {

                        // 继续调用验证方法
//                        System.out.println("判断无冲突" + isConflict);
                        this.dgCheckUtil(checkList, nextItem.getJSONObject(j).getString("id_P"), id_C, nextItem.getJSONObject(j)
                                , isRecurred, layer, isEmpty);
                    } else {
                        if (null != objectMap) {
                            objectMap.put("errDesc", "产品编号为空！");
                            isEmpty.add(objectMap);
                        }
                    }
                }
            }
        } else {
            objectMap.put("errDesc", "产品不存在！");
            isEmpty.add(objectMap);
        }
    }

    /**
     * 根据请求参数，获取更新后的订单oitem
     * ##param id_P 产品编号
     * ##param id_C 公司编号
     *
     * @return java.lang.String  返回结果: 日志结果
     * @author tang
     * @version 1.0.0
     * @date 2020/8/6 9:08
     * KEV Checked
     */
    @Override
    public ApiResponse setDgAllBmdpt(String id_P, String id_C) {
        Prod prod = coupaUtil.getProdByListKey(
                id_P, Arrays.asList("info", "part"));
        if (null == prod) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_NOT_EXIST.getCode(), "产品不存在");
        }

        JSONObject part = prod.getPart();
        if (null == part) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_NO_PART.getCode(), "产品无零件");
        }

        // 获取prod的part信息
        JSONArray partItem = prod.getPart().getJSONArray("objItem");
        if (null == partItem || partItem.size() == 0) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_NO_PART.getCode(), "产品无零件");
        }

        this.checkAllBmdPt(id_C, partItem, id_P);
        return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
    }

    /**
     * 根据id_C，判断prod是什么类型
     * ##param id_C	公司编号
     *
     * @return int  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2020/9/17 16:52
     * DONE Check Kev
     */
    private void checkAllBmdPt(String id_C, JSONArray partItem, String id_P) {
        for (int item = 0; item < partItem.size(); item++) {
            JSONObject thisItem = partItem.getJSONObject(item);

            JSONArray partDataES = coupaUtil.getListData("lBProd", "id_P", thisItem.getString("id_P"), "id_CB", id_C);

            Prod thisProd = coupaUtil.getProdByListKey(
                    thisItem.getString("id_P"), Arrays.asList("part", "info"));

            int bmdptValue;
            if (null == thisProd) {
                bmdptValue = 1;
            } else {
                // 获取id的信息
                ProdInfo prodInfo = thisProd.getInfo();
                JSONObject part = thisProd.getPart();
                Comp compById = coupaUtil.getCompByListKey(id_C, Arrays.asList("info"));

                if (null == compById || null == compById.getInfo() || !prodInfo.getId_C().equals(id_C)) {
                    //没有公司 = 物料
                    bmdptValue = 3;
                } else {
                    // 是自己的part 才可以进去查改bmdpt
                    if (null == part) {
                        bmdptValue = 1;
                    } else {
                        JSONArray nextPart = part.getJSONArray("objItem");
                        if (null == nextPart || nextPart.size() == 0) {
                            bmdptValue = 1;
                        } else {
                            this.checkAllBmdPt(id_C, nextPart, thisItem.get("id_P").toString());
                            bmdptValue = 2;
                        }
                    }
                }
            }
            // saving updated Key info into part

            thisItem.put("grp", partDataES.getJSONObject(0).getString("grp"));
            thisItem.put("grpB", partDataES.getJSONObject(0).getString("grpB"));

            if (partDataES.getJSONObject(0).getInteger("lCR") != null) {
                thisItem.put("lCR", partDataES.getJSONObject(0).getInteger("lCR"));
            }
            if (partDataES.getJSONObject(0).getInteger("lUT") != null) {
                thisItem.put("lUT", partDataES.getJSONObject(0).getInteger("lUT"));
            }
//                thisItem.put("seq", partDataES.getJSONObject(0).getString("seq"));
            thisItem.put("tdur", partDataES.getJSONObject(0).getInteger("tdur"));
            if (partDataES.getJSONObject(0).getDouble("wn4price") != null) {
                thisItem.put("wn4price", partDataES.getJSONObject(0).getDouble("wn4price"));
            }
//                thisItem.put("tmd", partDataES.getJSONObject(0).getString("tmd"));
            thisItem.put("pic", partDataES.getJSONObject(0).getString("pic"));
            thisItem.put("wrdN", partDataES.getJSONObject(0).getJSONObject("wrdN"));
            thisItem.put("wrddesc", partDataES.getJSONObject(0).getJSONObject("wrddesc"));
            thisItem.put("ref", partDataES.getJSONObject(0).getString("refB"));
            thisItem.put("grpB", partDataES.getJSONObject(0).getString("grpB"));
            thisItem.put("bmdpt", bmdptValue);
            System.out.println(thisItem.get("id_P") + "--" + thisItem);
            partItem.set(item, thisItem);
//                partItem.set(item,partDataES.getJSONObject(0));
//                partItem.getJSONObject(item).put("bmdpt",bmdptValue);
            System.out.println("thisItem" + JSON.toJSONString(partItem.getJSONObject(item)));

        }
        JSONObject partData = new JSONObject();
        partData.put("part.objItem", partItem);
        coupaUtil.updateProdByListKeyVal(id_P, partData);
    }

    /**
     * 检查part是否为空 - 注释完成
     * ##param id_P 产品编号
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/1/19 10:05
     * @Checked kev 2021/11/15
     * DONE Check Kev
     */
    @Override
    public ApiResponse getPartIsNull(String id_P) {

        // 根据id获取产品信息
        Prod prod = coupaUtil.getProdByListKey(
                id_P, Arrays.asList("info", "part"));
        // 判断产品为空
        if (null == prod) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PROD_NOT_EXIST.getCode(), "产品不存在");
        }
        // 获取产品零件信息
        JSONObject part = prod.getPart();
        // 判断产品零件信息为空
        if (null == part) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PART_IS_NULL.getCode(), "0");
        }
        // 获取prod的part信息
        JSONArray partItem = prod.getPart().getJSONArray("objItem");
        // 判断产品零件信息为空
        if (null == partItem || partItem.size() == 0) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PART_IS_NULL.getCode(), null);
        }
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "1");
    }

    /**
     * 时间处理方法
     * @param id_O	订单编号
     * @param id_U	用户编号
     * @param id_C	公司编号
     * @param teStart	开始时间
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/5/19 10:30
     */
    @Override
    public ApiResponse timeHandle(String id_O, String id_U, String id_C, Long teStart,Integer wn0TPrior) {
        System.out.println("初始-teStart:"+teStart);

        // 调用添加测试数据方法
        Obj.addTasks(teStart,"1001","1xx1",id_C,coupaUtil);
        Obj.addTasksAndOrder(teStart,id_C,coupaUtil);
        Obj.addTasksAndOrder3(teStart,id_C,coupaUtil);
        Obj.addOrder(teStart,coupaUtil);
        Obj.addOrder2(teStart,coupaUtil);
        Obj.addOrder3(teStart,coupaUtil);

        // 调用方法获取订单信息
        Order salesOrderData = coupaUtil.getOrderByListKey(
                id_O, Arrays.asList("oItem", "info", "view", "action", "casItemx"));

        // 判断订单是否为空
        if (null == salesOrderData || null == salesOrderData.getAction() || null == salesOrderData.getOItem()
                || null == salesOrderData.getCasItemx()) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        // 获取唯一下标
        String random = MongoUtils.GetObjectId();
        // 设置存储当前唯一编号的第一个当前时间戳
        TimeZjServiceImpl.onlyStartMap.put(random,teStart);
        // 设置存储当前唯一编号状态，== 0 未被第一次操作、 == 1 被第一次操作
        TimeZjServiceImpl.onlyIsDs.put(random,0);
        // 创建任务最初始开始时间存储
        hTeC.put(random,0L);

        // 获取递归订单列表
        JSONArray objOrder = salesOrderData.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
        // 创建订单信息存储json
        JSONObject ozMap = new JSONObject();
        // 遍历订单列表
        for (int i = 0; i < objOrder.size(); i++) {
            // 获取订单列表的订单编号
            String id_Oz = objOrder.getJSONObject(i).getString("id_O");
            // 根据订单编号查询action卡片信息
            Order ozAction = coupaUtil.getOrderByListKey(id_Oz, Collections.singletonList("action"));
            // 获取递归信息
            JSONArray objAction = ozAction.getAction().getJSONArray("objAction");
            // 根据订单编号添加订单信息存储
            ozMap.put(id_Oz,objAction);
        }
        // 添加当前时间处理的订单信息
        TimeZjServiceImpl.randomAction.put(random,ozMap);

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
            Long hTeStart = hTeC.getLong(random);
            // 根据当前递归信息创建添加存储判断镜像是否是第一个被冲突的产品信息
            JSONObject js1 = new JSONObject();
            JSONObject js2 = new JSONObject();
            // 设置为-1代表的是递归的零件
            js2.put("zOk",-1);
            js2.put("z","-1");
            js1.put(oDa.getString("index"),js2);
            sho.put(oDa.getString("id_O"),js1);
            // 设置统一随机数存储对象值
            TimeZjServiceImpl.randoms.put(random,random);
            // 设置订单id和订单index统一存储记录状态-根据random存储值
            TimeZjServiceImpl.storageReset.put(random,new HashMap<>());
            // 获取时间处理的组别
            String grpB = oDa.getString("grpB");
            // 根据组别获取部门
            String dep = grpBGroup.getJSONObject(grpB).getString("dep");
//            // 获取时间处理的部门
//            String dep = oDa.getString("dep");
            // 调用根据grpB，dep获取数据库职位总人数方法并且添加信息
//            orderODate.setGrpUNum(timeZjService.getObjGrpUNum(objOItem.getGrpB(),dep,myCompId));
//            .setDep(dep);
            oDa.put("dep",dep);
            oDa.put("grpUNum",timeZjService.getObjGrpUNum(grpB,dep,oDa.getString("id_C")));
            // 获取时间处理的零件产品编号
            String id_P = oDa.getString("id_P");
            // 获取时间处理的记录，存储是递归第一层的，序号为1和序号为最后一个状态
            Integer kaiJie = oDa.getInteger("kaiJie");
            // 获取时间处理的实际准备时间
            Long tePrep = oDa.getLong("tePrep");
//            // 获取时间处理的总任务时间
//            Long teDurTotal = oDa.getLong("teDurTotal");
            Long teDur = oDa.getLong("teDur");
            Double wn2qtyneed = oDa.getDouble("wn2qtyneed");
//            System.out.println(JSON.toJSONString(oDa));
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
                oDa.put("teStart",timeZjService.getTeS(random,grpB,dep));
            } else {
                // 判断序号是为1层级并且记录，存储是递归第一层的，序号为1和序号为最后一个状态为第一层
                if (csSta == 1 && kaiJie == 1) {
                    // 获取当前唯一ID存储时间处理的第一个时间信息的结束时间
                    hTeStart = csTe.getLong(random);
                }
                oDa.put("teStart",hTeStart);
            }
            // 存储判断执行方法
            boolean isD1 = false;
            // 序号是不为1层级
            if (csSta == 0 || (kaiJie != 1 && csSta == 1)) {
                isD1 = true;
            }
//            // 记录，存储是递归第一层的，序号为1和序号为最后一个状态,不为第一层
//            if (kaiJie != 1 && csSta == 1) {
//                isD1 = true;
//            }
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

            // TODO zj : teDate int写成当天的消费时间
            // 创建当前处理的任务的所在日期对象
            JSONObject teDate = new JSONObject();
            System.out.println("taskTe:");
            System.out.println(JSON.toJSONString(task));
            // 获取订单编号
            String id_On = oDa.getString("id_O");
            // 获取订单下标
            Integer index = oDa.getInteger("index");
            // 调用时间处理方法
            JSONObject jo = timeZjService.chkInJi(task,hTeStart,grpB,dep,id_On,index,0,random
                    ,1,teDate,teDaF,0,sho,0,csSta,true);
            // 更新任务最初始开始时间
            hTeStart = jo.getLong("hTeStart");
            System.out.println("最外层:"+hTeStart);
            // 添加结束时间
            teFinList.add(hTeStart);

            // 根据唯一id获取当前时间处理的订单信息
            JSONObject randAct = TimeZjServiceImpl.randomAction.getJSONObject(random);
            // 根据订单编号获取递归集合
            JSONArray jsonArray = randAct.getJSONArray(id_On);
            // 根据订单下标获取递归信息并且转换为递归类
            OrderAction orderAction = JSON.parseObject(JSON.toJSONString(jsonArray.getJSONObject(index)),OrderAction.class);
            // 更新递归信息
            orderAction.setDep(dep);
            orderAction.setGrpB(grpB);
            orderAction.setTeDate(teDate);
            // 将更新的递归信息写入回去
            jsonArray.set(index,orderAction);
            randAct.put(id_On,jsonArray);
            TimeZjServiceImpl.randomAction.put(random,randAct);

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
                    csTe.put(random,task.getTePStart());
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
//            if (csSta == 0) {
//                JSONObject pfTeZ = pfTe.getJSONObject(id_PF);
//                if (null == pfTeZ) {
//                    pfTeZ = new JSONObject();
//                    pfTeZ.put(priorItem.toString(),0);
//                }
//                Long aLong = pfTeZ.getLong(priorItem.toString());
//                if (null == aLong) {
//                    pfTeZ.put(priorItem.toString(),hTeStart);
//                    pfTe.put(id_PF,pfTeZ);
//                } else {
//                    if (hTeStart > aLong) {
//                        pfTeZ.put(priorItem.toString(),hTeStart);
//                        pfTe.put(id_PF,pfTeZ);
//                    }
//                }
//                System.out.println();
//            } else {
//                long pfT;
//                if (csSta == 1) {
//                    Long xFin = jo.getLong("xFin");
////                    System.out.println("xFinW:"+xFin);
//                    boolean isXf = false;
////                    long pfT;
//                    if (null != xFin) {
//                        System.out.println("xFin:"+xFin);
//                        hTeStart = xFin;
//                        if (xFin > maxSte) {
//                            maxSte = xFin;
//                        }
//                        isXf = true;
//                    } else {
//                        if (task.getTePFinish() > maxSte) {
//                            maxSte = task.getTePFinish();
//                        }
//                    }
////                    System.out.println("maxSte:"+maxSte);
//                    System.out.println();
//
//                    if (isXf) {
//                        pfT = xFin;
//                    } else {
//                        pfT = task.getTePFinish();
//                    }
//                } else {
//                    pfT = hTeStart;
//                }
//
//                JSONObject pfTeZ = pfTe.getJSONObject(id_PF);
//                if (null == pfTeZ) {
//                    pfTeZ = new JSONObject();
//                    pfTeZ.put(priorItem.toString(),0);
//                }
//                Long aLong = pfTeZ.getLong(priorItem.toString());
//                if (null == aLong) {
//                    pfTeZ.put(priorItem.toString(),pfT);
//                    pfTe.put(id_PF,pfTeZ);
//                } else {
//                    if (pfT > aLong) {
//                        pfTeZ.put(priorItem.toString(),pfT);
//                        pfTe.put(id_PF,pfTeZ);
//                    }
//                }
//
//                if (isPn) {
//                    csTe.put(random,task.getTePStart());
//                    isPn = false;
//                }
//            }
            // 更新当前唯一ID存储时间处理的最初开始时间
            hTeC.put(random,hTeStart);
            System.out.println();
        }

        // 遍历递归订单列表
        for (int i = 0; i < objOrder.size(); i++) {
            // 获取递归订单编号
            String id_Oz = objOrder.getJSONObject(i).getString("id_O");
            // 根据唯一id获取当前时间处理的订单信息
            JSONObject randAct = TimeZjServiceImpl.randomAction.getJSONObject(random);
            // 根据订单编号获取递归列表信息
            JSONArray objAction = randAct.getJSONArray(id_Oz);
            // 创建请求更改参数
            JSONObject mapKey = new JSONObject();
            // 添加请求更改参数信息
            mapKey.put("action.objAction",objAction);
            // 调用接口发起数据库更改信息请求
            coupaUtil.updateOrderByListKeyVal(id_Oz,mapKey);
        }

        // 调用任务最后处理方法
        timeZjService.setZui(teDaF,random,id_C);

        // 递归完成了，删除存储当前唯一编号的第一个当前时间戳
        TimeZjServiceImpl.onlyStartMap.remove(random);
        // 递归完成了，删除根据random（当前唯一编号）,grpB（组别）,dep（部门）存储最新的（最后一个）当前时间戳
        TimeZjServiceImpl.onlyStartMapAndDep.remove(random);
        // 根据唯一id删除当前时间
        hTeC.remove(random);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
    }

    /**
     * 删除指定list的redis键 - 注释完成
     *
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2021/6/16 14:32
     */
    @Override
    public ApiResponse dgRemove(String id_O, String id_C, String id_U) {


        // KEV here it can only remove "action" flows
        // need remake


        // **** check if orders are final, if need request Cancel, send request
        // if ok, open order
        // **** check isPush, if true send a Cancel msg statusChg (bcdStatus != 100)
        // send log by action's grpGroup/grpBGroup @ 1 log 2 id_FC?
        // delete that order
        // **** do not delete action
        // batch delete mdb, es
        // delete that order's lsborder
        // delete main orders' casItemx + action

        // **** later go to prob, check all id+ind and send cancel to each of those
        // set oItem's bcdStatus to ? (id_OProb is cancelled) cascade cancel...........

//        System.out.println("输出:");
//        System.out.println(id_O);
//        System.out.println(id_C);
//        id_C = "6076a1c7f3861e40c87fd294";
        Order orderParent = coupaUtil.getOrderByListKey(id_O, Arrays.asList());

        if (orderParent == null) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONArray casList;

        if (orderParent.getCasItemx().getJSONObject(id_C) == null) {
            casList = orderParent.getCasItemx().getJSONArray("objOrder");
        } else {
            casList = orderParent.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
        }

        JSONObject myOrder = new JSONObject();
        myOrder.put("id_O", id_O);
        casList.add(myOrder);

        // loop casItemx orders
        for (int i = 0; i < casList.size(); i++) {
            // check if orders are final, if need request Cancel, send request
            // if ok, open order

            String subOrderId = casList.getJSONObject(i).getString("id_O");

            try {
                // 创建es删除请求
                DeleteByQueryRequest requestAct = new DeleteByQueryRequest("action");
                // 设置删除信息
                requestAct.setQuery(new TermQueryBuilder("data.id_O.keyword", subOrderId));
                // 请求方法
                client.deleteByQuery(requestAct, RequestOptions.DEFAULT);
            } catch (Exception e) {
                System.out.println("删除es出现错误:" + e.getMessage());
            }
            // delete that order        // 删除订单
            if (!subOrderId.equals(id_O)) {
                // 创建es删除请求
                try {
                    DeleteByQueryRequest requestLB = new DeleteByQueryRequest("lsborder");
                    // 设置删除信息
                    requestLB.setQuery(new TermQueryBuilder("id_O", subOrderId));
                    // 请求方法
                    client.deleteByQuery(requestLB, RequestOptions.DEFAULT);
                } catch (Exception e) {
                    System.out.println("删除es出现错误:" + e.getMessage());
                }

                coupaUtil.removeOrderById(subOrderId);
            }
        }

        JSONObject orderData = new JSONObject();
        orderData.put("casItemx", null);
        orderData.put("action", null);
        JSONArray view = orderParent.getView();

        view.remove("casItemx");
        view.remove("action");
        orderData.put("view", view);

        coupaUtil.updateOrderByListKeyVal(id_O, orderData);

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "删除成功！！!!!");
    }

}
