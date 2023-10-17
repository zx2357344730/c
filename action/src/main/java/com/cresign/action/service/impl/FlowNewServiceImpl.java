package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.common.ActionEnum;
import com.cresign.action.service.FlowNewService;
import com.cresign.action.utils.DgCheckUtil;
import com.cresign.action.utils.TaskObj;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.es.lSBOrder;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.chkin.Task;
import com.cresign.tools.pojo.po.orderCard.OrderAction;
import com.cresign.tools.pojo.po.orderCard.OrderInfo;
import com.cresign.tools.pojo.po.orderCard.OrderODate;
import com.cresign.tools.pojo.po.orderCard.OrderOItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Future;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName FlowNewServiceImpl
 * @Date 2023/9/15
 * @ver 1.0.0
 */
@Service
@EnableAsync
public class FlowNewServiceImpl implements FlowNewService {

    @Autowired
    private Qt qt;

    @Autowired
    private DbUtils dbu;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private RetResult retResult;

//    @Autowired
//    private TestAsync testAsync;

    @Autowired
    private DgCheckUtil checkUtil;

//    Map<String, Map<String, Prod>> allDgProd = new ConcurrentHashMap<>(16);

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse getDgResult(String id_OParent, String id_U, String myCompId
            , Long teStart, String divideOrder) {
        System.out.println("开始时间:");
        System.out.println(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_OParent, Arrays.asList("oItem", "info", "view", "action"), Order.class);

        // 判断订单是否为空
        if (null == salesOrderData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }

        if (!salesOrderData.getInfo().getId_C().equals(myCompId)) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_SUPPLIER_ID_IS_NULL.getCode(), "必须是自己生产的");
        }
        Map<String, Prod> dgProd = new HashMap<>(16);
        if (salesOrderData.getOItem().getJSONArray("allProdId") == null){
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_SUPPLIER_ID_IS_NULL.getCode(), "需要检查所有零件");
        }

        List<Prod> prods = qt.getMDContentFast(salesOrderData.getOItem().getJSONArray("allProdId")
                , qt.strList("info", "part"), Prod.class);
        prods.forEach(prod -> dgProd.put(prod.getId(),prod));
        System.out.println("中间时间:");
        System.out.println(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // 第一次把action卡创建出来
        if (null == salesOrderData.getAction()) {
            JSONObject newEmptyAction = new JSONObject();
            salesOrderData.setAction(newEmptyAction);
        }
        if (null != salesOrderData.getAction().getString("isDg")) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经被递归了");
        }
        if (salesOrderData.getInfo().getLST() != 7) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_ORDER_NEED_FINAL.getCode(), "需要两方确认");
        }

        // 转换oItem为list
        JSONArray oParent_objItem = salesOrderData.getOItem().getJSONArray("objItem");

        Integer oParent_prior = salesOrderData.getInfo().getPriority();

        // 创建存储递归OItem结果的Map
        Map<String, List<OrderOItem>> objOItemCollection = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

        // 创建存储递归Action结果的Map
        Map<String, List<OrderAction>> objActionCollection = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

        // 定义以公司id为键的存储map            // 定义以订单id为键的存储
        JSONArray casItemData = new JSONArray();

        //orderAction存储map
        Map<String, OrderAction> pidActionCollection = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

        String refOP = salesOrderData.getInfo().getId_C().equals(salesOrderData.getInfo().getId_CB()) ?
                salesOrderData.getInfo().getRefB() : salesOrderData.getInfo().getRef();

        // 创建递归存储的时间处理信息
        List<OrderODate> oDates = new ArrayList<>();
        // 创建递归存储的时间任务信息
        List<Task> oTasks = new ArrayList<>();
        // 创建存储零件编号的合并信息记录合并时的下标
        JSONObject mergeJ = new JSONObject();
        // 遍历订单内所有产品

        for (int item = 0; item < oParent_objItem.size(); item++) {

            if (!oParent_objItem.getJSONObject(item).getString("seq").equals(3)) {

                OrderOItem objOItem = JSONObject.parseObject(JSON.toJSONString(oParent_objItem.getJSONObject(item)), OrderOItem.class);
                objOItem.setPriority(oParent_prior);
                OrderAction objAction = new OrderAction(100, 0, 0, 1, salesOrderData.getId(),
                        refOP, objOItem.getId_P(), id_OParent, item, objOItem.getRKey(), 0, 0,
                        new JSONArray(), new JSONArray(), new JSONArray(), new JSONArray(), salesOrderData.getInfo().getWrdN(), objOItem.getWrdN());

                ////////////////actually dg ///////////////////////
                JSONObject isJsLj = new JSONObject();
                isJsLj.put("1", 0);
                //dgType: 1 = firstLayer (sales Items), 2 = regular/subTask or subProd, 3 = depSplit regular
                // T/P - T/P -T/P.... problem is id_P == ""?

                this.dgProcess(
                        1, myCompId, id_OParent,
                        objOItem, objAction,
                        casItemData,
                        oParent_objItem, item,
                        objOItemCollection, objActionCollection,
                        pidActionCollection,
                        objOItem.getId_P(), oDates, oTasks, mergeJ, 0, null, isJsLj, dgProd,divideOrder);
            }
        }

        // 判断递归结果是否为空
        if (objActionCollection.size() == 0) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_RECURSION_RESULT_IS_NULL.getCode(), "递归结果为空");
        }
        System.out.println(JSON.toJSONString(casItemData));
//        // 获取递归结果键
//        Set<String> actionCollection = objActionCollection.keySet();
//        System.out.println(JSON.toJSONString(actionCollection));
//        System.out.println();
//        for (String s : actionCollection) {
//            System.out.println("s:"+s+"  ,  size:"+objActionCollection.get(s).size());
////            System.out.println(JSON.toJSONString(objActionCollection.get(s)));
//        }
//        System.out.println(JSON.toJSONString(cun));
//        System.out.println(cun.size());
//        System.out.println(JSON.toJSONString(cun2));
//        System.out.println(cun2.size());
//        System.out.println(JSON.toJSONString(cun3));
//        System.out.println(cun3.size());
//        System.out.println(JSON.toJSONString(cun4));
//        System.out.println(cun4.size());
//        System.out.println(JSON.toJSONString(objActionCollection));
//        System.out.println(JSON.toJSONString(objOItemCollection));
//        System.out.println(JSON.toJSONString(pidActionCollection));
//        System.out.println(JSON.toJSONString(oParent_objItem));

        System.out.println("save-start");
        /////////// setup Dep.objlBProd + objlSProd for grpP

        //putting the Sales order as the last casItem... I donno why
        JSONObject thisOrderData = new JSONObject();
        thisOrderData.put("id_C", myCompId);
        thisOrderData.put("id_O", id_OParent);
        thisOrderData.put("lST", 4);
        thisOrderData.put("type", 1);
        thisOrderData.put("priority", salesOrderData.getInfo().getPriority());
        thisOrderData.put("size", oParent_objItem.size());
        thisOrderData.put("wrdN", salesOrderData.getInfo().getWrdN());
        casItemData.add(thisOrderData);


        // 获取递归结果键
        Set<String> actionCollection = objActionCollection.keySet();
        // before getting so many id_A, get myComp id_A first for future use
        Asset myDef = qt.getConfig(myCompId, "a-auth", "def");
        List<Order> addOrder = new ArrayList<>();
        // 遍历键，并创建采购单
        for (String thisOrderId : actionCollection) {

            // 获取对应订单id的零件递归信息
            List<OrderAction> unitAction = objActionCollection.get(thisOrderId);
            // 获取对应订单id的零件信息
            List<OrderOItem> unitOItem = objOItemCollection.get(thisOrderId);

            // 创建订单info
            String prodCompId = "";
            JSONObject orderNameCas = new JSONObject();
            String targetCompId;
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
            String grpO = "";
            String grpOB = "1000";
//                String aId;
            Asset asset = null;

            if (!targetCompId.equals(myCompId)) {

                asset = qt.getConfig(targetCompId, "a-auth", "def");

                if (asset.getId().equals("none")) {
                    asset = myDef;
                }
            } else {
//                    aId = myDef.getId();
                asset = myDef;
            }
            System.out.println("otherDeff" + targetCompId);


            // if it is a real Company get grpB setting from objlBProd by ref, else do nothing now, later can do extra
            JSONObject defResultBP = asset.getDef().getJSONObject("objlBP");
            JSONObject defResultBC = asset.getDef().getJSONObject("objlBC");

            for (OrderOItem orderOItem : unitOItem) {
//                System.out.println(orderOItem.getGrpB());
                String grpB = orderOItem.getGrpB();
                if (grpBGroup.getJSONObject(grpB) == null) {
                    grpBGroup.put(grpB, defResultBP.getJSONObject(grpB));
                }
            }
            Asset asset2 = null;
            if (!prodCompId.equals(myCompId)) {
                asset2 = qt.getConfig(prodCompId, "a-auth", "def");
                if (asset2.getId().equals("none")) {

                    asset2 = myDef;
                }

            } else {
                asset2 = myDef;
            }

            JSONObject defResultSP = asset2.getDef().getJSONObject("objlSP");

            JSONObject defResultSC = asset2.getDef().getJSONObject("objlSC");

            for (OrderOItem orderOItem : unitOItem) {
                String grp = orderOItem.getGrp();

                if (grpGroup.getJSONObject(grp) == null) {

                    grpGroup.put(grp, defResultSP.getJSONObject(grp));
                }
            }

            grpO = "1000";
            grpOB = "1000";
            System.out.print("got all ok");

            if (id_OParent.equals(thisOrderId)) {
                // make sales order Action
                this.updateSalesOrder(casItemData, unitAction, unitOItem, salesOrderData, grpBGroup, grpGroup, prodCompId
                        , oDates, oTasks);
            } else {
                // else make Purchase Order
//                // 创建订单
                Order newPO = new Order();

                // 根据键设置订单id
                newPO.setId(thisOrderId);
                System.out.print("got1" + thisOrderId);

                // priority is BY order, get from info and write into ALL oItem
                OrderInfo newPO_Info = new OrderInfo(prodCompId, targetCompId, unitOItem.get(0).getId_CP(), "", id_OParent, "", "",
                        grpO, grpOB, oParent_prior, unitOItem.get(0).getPic(), 4, 0, orderNameCas, null);

                // 设置订单info信息
                newPO.setInfo(newPO_Info);

                // 添加View信息
                JSONArray view = new JSONArray();
                view.add("info");
                view.add("action");
                view.add("oItem");
                view.add("oStock");
                newPO.setView(view);

                JSONArray objCard = new JSONArray();
                objCard.add("action");
                objCard.add("oStock");

                Double wn2qty = 0.0;
                Double wn4price = 0.0;
                JSONArray arrayId_P = new JSONArray();

                for (OrderOItem orderOItem : unitOItem) {
                    wn2qty += orderOItem.getWn2qtyneed();
                    wn4price += orderOItem.getWn4price();
//                    System.out.println("u " + orderOItem);
                    arrayId_P.add(orderOItem.getId_P());
                }

                // 添加OItem信息
                JSONObject newPO_OItem = new JSONObject();
                newPO_OItem.put("objItem", unitOItem);
                newPO_OItem.put("wn2qty", wn2qty);
                newPO_OItem.put("arrP", arrayId_P);
                newPO_OItem.put("wn4price", wn4price);
                newPO_OItem.put("objCard", objCard);
//                if (divideOrder.equals("true")) {
//                    Map<String,List<OrderOItem>> oItemMap = new HashMap<>();
//                    Map<String,List<OrderAction>> actionMap = new HashMap<>();
//                    for (int i = 0; i < unitOItem.size(); i++) {
//                        OrderOItem orderOItem = unitOItem.get(i);
//                        OrderAction orderAction = unitAction.get(i);
//                        String grpB = orderOItem.getGrpB();
//                        List<OrderOItem> orderOItems;
//                        List<OrderAction> orderActions;
//                        if (oItemMap.containsKey(grpB)) {
//                            orderOItems = oItemMap.get(grpB);
//                            orderOItems.add(orderOItem);
//                            orderActions = actionMap.get(grpB);
//                            orderActions.add(orderAction);
//                        } else {
//                            orderOItems = new ArrayList<>();
//                            orderOItems.add(orderOItem);
//                            orderActions = new ArrayList<>();
//                            orderActions.add(orderAction);
//                        }
//                        oItemMap.put(grpB,orderOItems);
//                        actionMap.put(grpB,orderActions);
//                    }
//                    JSONArray sonOrderId = new JSONArray();
//                    for (String s : oItemMap.keySet()) {
//                        List<OrderOItem> orderOItems = oItemMap.get(s);
//                        List<OrderAction> orderActions = actionMap.get(s);
//                        sonOrderId.add(addOrder(prodCompId,targetCompId, orderOItems, newPO.getId(),grpO, grpOB
//                                ,oParent_prior,orderNameCas,orderActions,grpBGroup,grpGroup,s));
//                    }
//                    newPO_OItem.put("sonOrderId", sonOrderId);
//                }
                newPO.setOItem(newPO_OItem);
                System.out.println("sales order");

                // 创建采购单的Action
                JSONObject newPO_Action = new JSONObject();
                newPO_Action.put("objAction", unitAction);
                newPO_Action.put("grpBGroup", grpBGroup);
                newPO_Action.put("grpGroup", grpGroup);
                newPO_Action.put("wn2progress", 0.0);

                //Create oStock
                JSONObject newPO_oStock = dbu.initOStock(qt.list2Arr(unitOItem));
                newPO.setOStock(newPO_oStock);

                newPO.setAction(newPO_Action);

                JSONObject listCol = new JSONObject();

                dbu.summOrder(newPO, listCol);
                // 新增订单
//                qt.addMD(newPO);
                addOrder.add(newPO);
//                    qt.setES(....)
                System.out.println("sales order SAVED " + newPO.getInfo().getWrdN().getString("cn"));

//              // 创建lSBOrder订单
                lSBOrder lsbOrder = new lSBOrder(prodCompId, targetCompId, "", "", id_OParent, thisOrderId, arrayId_P,
                        "", "", null, "1000", unitOItem.get(0).getPic(), 4, 0, orderNameCas, null, null);
                // 新增lsbOrder信息
                qt.addES("lsborder", lsbOrder);
            }
        }
        qt.addAllMD(addOrder);
//        System.out.println("save-end");
//        System.out.println("all finished...");
//        System.out.println("结束-时间:");
//        System.out.println(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // END FOR
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    @Override
    public ApiResponse dgCheckOrder(String myCompId,String id_O) {
        System.out.println("开始时间:");
        System.out.println(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // 调用方法获取订单信息
        Order salesOrderData = qt.getMDContent(id_O, Arrays.asList("oItem", "info", "view", "action"), Order.class);

        // 判断订单是否为空
        if (null == salesOrderData) {
            // 返回为空错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }

        if (!salesOrderData.getInfo().getId_C().equals(myCompId)) {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_SUPPLIER_ID_IS_NULL.getCode(), "必须是自己生产的");
        }
        HashSet<String> id_Ps = getCheckOrderAllId_P2(salesOrderData.getOItem().getJSONArray("objItem"), myCompId);
//        System.out.println(JSON.toJSONString(id_Ps));
//        System.out.println("当前递归:"+id_Ps.size());
        qt.setMDContent(salesOrderData.getId(),qt.setJson("oItem.allProdId"
                ,id_Ps), Order.class);
//        System.out.println("结束时间:");
//        System.out.println(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "");
    }

    private HashSet<String> getCheckOrderAllId_P(JSONArray objItem,String myCompId){
        HashSet<String> id_Ps = new HashSet<>();
        int forZon = objItem.size() / 7;
        JSONArray item6 = new JSONArray();
        JSONArray item7 = new JSONArray();
        if (forZon > 0) {
            JSONArray item1 = new JSONArray();
            JSONArray item2 = new JSONArray();
            JSONArray item3 = new JSONArray();
            JSONArray item4 = new JSONArray();
            JSONArray item5 = new JSONArray();
            int lei = 0;
            for (int i = 0; i < forZon; i++) {
                item1.add(objItem.getJSONObject(lei));
                item2.add(objItem.getJSONObject(lei+1));
                item3.add(objItem.getJSONObject(lei+2));
                item4.add(objItem.getJSONObject(lei+3));
                item5.add(objItem.getJSONObject(lei+4));
                item6.add(objItem.getJSONObject(lei+5));
                item7.add(objItem.getJSONObject(lei+6));
                lei+=7;
            }
            int jie = objItem.size()-(forZon*7);
            if (jie > 0) {
                if (jie == 1) {
                    item1.add(objItem.getJSONObject((forZon*7)));
                } else if (jie == 2) {
                    item1.add(objItem.getJSONObject((forZon*7)));
                    item2.add(objItem.getJSONObject((forZon*7)+1));
                } else if (jie == 3) {
                    item1.add(objItem.getJSONObject((forZon*7)));
                    item2.add(objItem.getJSONObject((forZon*7)+1));
                    item3.add(objItem.getJSONObject((forZon*7)+2));
                } else if (jie == 4) {
                    item1.add(objItem.getJSONObject((forZon*7)));
                    item2.add(objItem.getJSONObject((forZon*7)+1));
                    item3.add(objItem.getJSONObject((forZon*7)+2));
                    item4.add(objItem.getJSONObject((forZon*7)+3));
                } else if (jie == 5) {
                    item1.add(objItem.getJSONObject((forZon*7)));
                    item2.add(objItem.getJSONObject((forZon*7)+1));
                    item3.add(objItem.getJSONObject((forZon*7)+2));
                    item4.add(objItem.getJSONObject((forZon*7)+3));
                    item5.add(objItem.getJSONObject((forZon*7)+4));
                } else {
                    item1.add(objItem.getJSONObject((forZon*7)));
                    item2.add(objItem.getJSONObject((forZon*7)+1));
                    item3.add(objItem.getJSONObject((forZon*7)+2));
                    item4.add(objItem.getJSONObject((forZon*7)+3));
                    item5.add(objItem.getJSONObject((forZon*7)+4));
                    item6.add(objItem.getJSONObject((forZon*7)+5));
                }
            }
            Future<String> future1 = checkUtil.testResult(id_Ps, item1, myCompId);
            Future<String> future2 = checkUtil.testResult(id_Ps, item2, myCompId);
            Future<String> future3 = checkUtil.testResult(id_Ps, item3, myCompId);
            Future<String> future4 = checkUtil.testResult(id_Ps, item4, myCompId);
            Future<String> future5 = checkUtil.testResult(id_Ps, item5, myCompId);
            Future<String> future6 = checkUtil.testResult(id_Ps, item6, myCompId);
            checkUtil.checkUtil(id_Ps,item7,myCompId);
            while (true) {
                if (future1.isDone()
                        && future2.isDone() && future3.isDone() && future4.isDone()
                        && future5.isDone() && future6.isDone()
                ) {
                    break;
                }
            }
        } else {
            if (objItem.size() > 3) {
                boolean isAdd = true;
                for (int i = 0; i < objItem.size(); i++) {
                    if (isAdd) {
                        isAdd = false;
                        item7.add(objItem.getJSONObject(i));
                    } else {
                        isAdd = true;
                        item6.add(objItem.getJSONObject(i));
                    }
                }
                Future<String> future6 = checkUtil.testResult(id_Ps, item6, myCompId);
                checkUtil.checkUtil(id_Ps,item7,myCompId);
                while (true) {
                    if ( future6.isDone()) {
                        break;
                    }
                }
            } else {
                checkUtil.checkUtil(id_Ps,item7,myCompId);
            }
        }
        return id_Ps;
    }
    private HashSet<String> getCheckOrderAllId_P2(JSONArray objItem,String myCompId){
        HashSet<String> id_Ps = new HashSet<>();
        int itemSize = objItem.size();
        if (itemSize <= 6) {
            checkUtil.checkUtil(id_Ps,objItem,myCompId);
        }
        int deng;
        if (itemSize <= 12) {
            deng = 2;
        } else if (itemSize <= 18) {
            deng = 3;
        } else if (itemSize <= 24) {
            deng = 4;
        } else if (itemSize <= 30) {
            deng = 5;
        } else if (itemSize <= 36) {
            deng = 6;
        } else {
            deng = 7;
        }
        List<List<JSONObject>> subList = qt.getSubList(deng, objItem, true, JSONObject.class);
        Future<String> future1 = checkUtil.testResult(id_Ps, subList.get(1), myCompId);
        if (deng == 2) {
            getReturn(deng,future1,null,null,null,null,null,id_Ps,subList.get(0),myCompId);
            return id_Ps;
        }
        Future<String> future2 = checkUtil.testResult(id_Ps, subList.get(2), myCompId);
        if (deng == 3) {
            getReturn(deng,future1,future2,null,null,null,null,id_Ps,subList.get(0),myCompId);
            return id_Ps;
        }
        Future<String> future3 = checkUtil.testResult(id_Ps, subList.get(3), myCompId);
        if (deng == 4) {
            getReturn(deng,future1,future2,future3,null,null,null,id_Ps,subList.get(0),myCompId);
            return id_Ps;
        }
        Future<String> future4 = checkUtil.testResult(id_Ps, subList.get(4), myCompId);
        if (deng == 5) {
            getReturn(deng,future1,future2,future3,future4,null,null,id_Ps,subList.get(0),myCompId);
            return id_Ps;
        }
        Future<String> future5 = checkUtil.testResult(id_Ps, subList.get(5), myCompId);
        if (deng == 6) {
            getReturn(deng,future1,future2,future3,future4,future5,null,id_Ps,subList.get(0),myCompId);
            return id_Ps;
        }
        Future<String> future6 = checkUtil.testResult(id_Ps, subList.get(6), myCompId);
        getReturn(deng,future1,future2,future3,future4,future5,future6,id_Ps,subList.get(0),myCompId);
        return id_Ps;
    }

    public void getReturn(int deng,Future<String> future1
            ,Future<String> future2,Future<String> future3
            ,Future<String> future4,Future<String> future5,Future<String> future6
            ,HashSet<String> id_Ps,List<JSONObject> subListSon, String myCompId){
//        System.out.println("主:"+qtAsNew.getThreadId());
//        System.out.println("?");
        checkUtil.checkUtil(id_Ps,subListSon,myCompId);
        while (true) {
            if (deng == 2 && future1.isDone()) {
                break;
            } else if (deng == 3 && future1.isDone() && future2.isDone()) {
                break;
            } else if (deng == 4 && future1.isDone() && future2.isDone() && future3.isDone()) {
                break;
            } else if (deng == 5 && future1.isDone() && future2.isDone()
                    && future3.isDone() && future4.isDone()) {
                break;
            } else if (deng == 6 && future1.isDone() && future2.isDone() && future5.isDone()
                    && future3.isDone() && future4.isDone()) {
                break;
            } else {
                if (future1.isDone() && future2.isDone() && future5.isDone()
                        && future3.isDone() && future4.isDone() && future6.isDone()) {
                    break;
                }
            }
        }
    }

    private String addOrder(String prodCompId, String targetCompId, List<OrderOItem> unitOItem
            , String id_OParent, String grpO, String grpOB, Integer oParent_prior, JSONObject orderNameCas
            , List<OrderAction> unitAction, JSONObject grpBGroup, JSONObject grpGroup, String thisGrpB) {
        String thisOrderId = qt.GetObjectId();
        Order newPO = new Order();

        // 根据键设置订单id
        newPO.setId(thisOrderId);
        System.out.print("got1" + newPO.getId());
        String cn = orderNameCas.getString("cn");
        orderNameCas.put("cn", cn + "-" + thisGrpB);

        // priority is BY order, get from info and write into ALL oItem
        OrderInfo newPO_Info = new OrderInfo(prodCompId, targetCompId, unitOItem.get(0).getId_CP(), "", id_OParent, "", "",
                grpO, grpOB, oParent_prior, unitOItem.get(0).getPic(), 4, 0, orderNameCas, null);

        // 设置订单info信息
        newPO.setInfo(newPO_Info);

        // 添加View信息
        JSONArray view = new JSONArray();
        view.add("info");
        view.add("action");
        view.add("oItem");
        view.add("oStock");
        newPO.setView(view);

        JSONArray objCard = new JSONArray();
        objCard.add("action");
        objCard.add("oStock");

        Double wn2qty = 0.0;
        Double wn4price = 0.0;
        JSONArray arrayId_P = new JSONArray();

        for (OrderOItem orderOItem : unitOItem) {
            wn2qty += orderOItem.getWn2qtyneed();
            wn4price += orderOItem.getWn4price();
//            System.out.println("u " + orderOItem);
            arrayId_P.add(orderOItem.getId_P());
        }

        // 添加OItem信息
        JSONObject newPO_OItem = new JSONObject();
        newPO_OItem.put("objItem", unitOItem);
        newPO_OItem.put("wn2qty", wn2qty);
        newPO_OItem.put("arrP", arrayId_P);
        newPO_OItem.put("wn4price", wn4price);
        newPO_OItem.put("objCard", objCard);
        newPO.setOItem(newPO_OItem);
        System.out.println("sales order");
        JSONObject grpBGroupNew = new JSONObject();
        grpBGroupNew.put(thisGrpB, grpBGroup.getJSONObject(thisGrpB));
        // 创建采购单的Action
        JSONObject newPO_Action = new JSONObject();
        newPO_Action.put("objAction", unitAction);
        newPO_Action.put("grpBGroup", grpBGroupNew);
        newPO_Action.put("grpGroup", grpGroup);
        newPO_Action.put("wn2progress", 0.0);

        //Create oStock
        JSONObject newPO_oStock = dbu.initOStock(qt.list2Arr(unitOItem));
        newPO.setOStock(newPO_oStock);

        newPO.setAction(newPO_Action);

        JSONObject listCol = new JSONObject();

//        dbu.summOrder(newPO, listCol);
        // 新增订单
//        qt.addMD(newPO);
//                    qt.setES(....)
        System.out.println("sales order SAVED " + newPO.getInfo().getWrdN().getString("cn"));

//              // 创建lSBOrder订单
        lSBOrder lsbOrder = new lSBOrder(prodCompId, targetCompId, "", "", id_OParent, newPO.getId(), arrayId_P,
                "", "", null, "1000", unitOItem.get(0).getPic(), 4, 0, orderNameCas, null, null);
        // 新增lsbOrder信息
//        qt.addES("lsborder", lsbOrder);
        return thisOrderId;
    }

    private void dgProcess(
            Integer dgType, String myCompId, String id_OParent,
            OrderOItem upperOItem, OrderAction upperAction,
            JSONArray casItemData,
            JSONArray partArray, Integer partIndex,
            Map<String, List<OrderOItem>> objOItemCollection,
            Map<String, List<OrderAction>> objActionCollection,
            Map<String, OrderAction> pidActionCollection,
            String id_PF, List<OrderODate> oDates, List<Task> oTasks, JSONObject mergeJ
            , int csSta, String csId_P, JSONObject isJsLj, Map<String, Prod> dgProd,String divideOrder) {
        //ZJ

        isJsLj.put("1", (isJsLj.getInteger("1") + 1));
        int dq = isJsLj.getInteger("1");
        // 存储序号是否为1层级
        int timeHandleSerialNoIsOneInside = 0;
        // 获取父id是否是当前唯一ID存储时间处理的最初产品编号存储
        boolean isPf = id_PF.equals(csId_P);
        // 判断上一个序号是否为1层级
        if (csSta == 1) {
            // 判断是，则将自己也设置为是
            timeHandleSerialNoIsOneInside = 1;
        }
        //ZJ

        String id_P = partArray.getJSONObject(partIndex).getString("id_P");
        JSONObject partInfo = partArray.getJSONObject(partIndex);
        String prodCompId = partInfo.getString("id_C");
        String grpB = partInfo.getString("grpB");

        // 获取, 设新 当前订单id
        // 定义递归订单id
        String newOrderId = "";
        Integer newOrderIndex = 0;

        // if this is a task, and it's not Sales layer
        //else
        if (id_P.equals("") && dgType > 1) {
//            if (prodCompId.equals(myCompId)){
//                cun.add(grpB);
//            }
            if (partIndex.equals(0)) {
                //if I am the first one, make new Order
                newOrderId = qt.GetObjectId();

                //same detail into casItemx
                JSONObject thisOrderData = new JSONObject();
                thisOrderData.put("id_C", myCompId);
                thisOrderData.put("id_O", newOrderId);
                thisOrderData.put("lST", 4);
                thisOrderData.put("size", 1);
                thisOrderData.put("type", 4); //task - subTask
                thisOrderData.put("grpB",grpB);
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
        } else if (null != pidActionCollection.get(id_P)) {
            //Now, it is prod not task， Must Merge if id_P is in pidActionCollection
            // as long as you have it in pidArray, you MUST merge
//            System.out.println("I merged2////" + upperAction);
//            if (prodCompId.equals(myCompId)){
//                cun2.add(grpB);
//            }
            //ZJ
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
            orderODate.setBmdpt(objActionCollection.get(fin_O).get(fin_Ind).getBmdpt());

            // 判断父编号是当前唯一ID存储时间处理的最初产品编号并且序号为1
            if (isPf && orderODate.getPriorItem() == 1) {
                // 设置序号是为1层级
                timeHandleSerialNoIsOneInside = 1;
            }
            // 添加信息
            orderODate.setCsSta(timeHandleSerialNoIsOneInside);
            oDates.add(orderODate);
            // 创建一个任务类
            Task task = new Task();
            // 随便添加一个信息
            task.setPriority(-2);
            // 使用一个空任务对象来与时间处理信息对齐
            oTasks.add(task);
            //ZJ

            this.mergePart(id_P, partArray, partIndex, upperAction, upperOItem, dgType,
                    // upperOItem.getWn2qtyneed(),
                    pidActionCollection,
                    objActionCollection, objOItemCollection
                    , oDates, mergeJ
            );


//            System.out.println("salesOrder@now");
//            System.out.println(objActionCollection.get(fin_O));
//            System.out.println(objActionCollection.get(id_OParent));
            return;
        } else if (dgType.equals(1)) {
            newOrderId = id_OParent;
            newOrderIndex = partIndex;
        } else {
            // Now need to check if prodCompId is in casItemData, if so, get and append 1 more prod in that order
            // 根据公司获取递归订单id
            boolean isNew = true;
            boolean div = divideOrder.equals("true");
            String grpBNew;
            for (int k = 0; k < casItemData.size(); k++) {
//                System.out.println("casItem" + casItemData.getJSONObject(k));
                // 2 cases: prod is mine, check only if casItemx Type =2 and not 4
                if (prodCompId.equals(myCompId) &&
                        prodCompId.equals(casItemData.getJSONObject(k).getString("id_C")) &&
                        casItemData.getJSONObject(k).getString("type").equals("2")
                        || prodCompId.equals(casItemData.getJSONObject(k).getString("id_C"))
                ) {
                    if (div && prodCompId.equals(myCompId)) {
                        grpBNew = casItemData.getJSONObject(k).getString("grpB");
                        if (null != grpBNew && grpBNew.equals(grpB)) {
                            newOrderId = casItemData.getJSONObject(k).getString("id_O");
                            newOrderIndex = casItemData.getJSONObject(k).getInteger("size");
                            casItemData.getJSONObject(k).put("size", newOrderIndex + 1);
                            isNew = false;
                        }
                    } else {
                        newOrderId = casItemData.getJSONObject(k).getString("id_O");
                        newOrderIndex = casItemData.getJSONObject(k).getInteger("size");
                        casItemData.getJSONObject(k).put("size", newOrderIndex + 1);
                        isNew = false;
                    }
                }
            }
            if (isNew) {

                // 赋值随机id
                newOrderId = qt.GetObjectId();
                newOrderIndex = 0;

                // 递归订单map添加随机id
                JSONObject orderName = new JSONObject();



                if (prodCompId.equals(myCompId)) {
                    if (div) {
                        Asset as1 = qt.getConfig(prodCompId, "a-auth", "def.objlBP."+grpB+".wrdN");
                        String grpName = as1.getDef().getJSONObject("objlBP").getJSONObject(grpB).getJSONObject("wrdN").getString("cn");

                        orderName.put("cn", upperAction.getRefOP() + " 派工单-00" + casItemData.size()+":" +grpName);
                    } else {
                        orderName.put("cn", upperAction.getRefOP() + " 派工单-00" + casItemData.size());
                    }
                } else {
                    orderName.put("cn", upperAction.getRefOP() + " 采购单-00" + casItemData.size());
                }

                JSONObject thisOrderData = new JSONObject();
                thisOrderData.put("id_C", prodCompId);
                thisOrderData.put("id_O", newOrderId);
                thisOrderData.put("grpB",grpB);
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
            objOItem = upperOItem;
            objAction = upperAction;
        } else {
            objOItem = new OrderOItem(id_P, upperOItem.getId_OP(),
                    partInfo.getString("id_CP") == null ? prodCompId : partInfo.getString("id_CP"),
                    prodCompId, myCompId,
                    newOrderId, newOrderIndex,
                    partInfo.getString("ref"), partInfo.getString("refB"),
                    partInfo.getString("grp"), partInfo.getString("grpB"), 0, partInfo.getString("pic"),
                    partInfo.getInteger("lUT"), partInfo.getInteger("lCR"),
                    0.0,
                    partInfo.getDouble("wn4price"), upperOItem.getWrdN(), partInfo.getJSONObject("wrdN"),
                    partInfo.getJSONObject("wrddesc"), partInfo.getJSONObject("wrdprep"));

            objOItem.setTmd(dateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            objOItem.setSeq("3"); // set DGAction specific seq = 3

            // if C=CB and bmdpt =1 means it's my own process, I cannot set grp
            if (prodCompId.equals(myCompId) && partInfo.getInteger("bmdpt").equals(1)) {
                objOItem.setGrp("");
            }

            objOItem.setWn2qtyneed(upperOItem.getWn2qtyneed() * partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));

            objAction = new OrderAction(100, 0, 1, partInfo.getInteger("bmdpt"),
                    id_OParent, upperAction.getRefOP(), partInfo.getString("id_P"), newOrderId, newOrderIndex, objOItem.getRKey(), 0
                    , 0, null, null, null, null, upperOItem.getWrdN(), partInfo.getJSONObject("wrdN"));

            //ZJ
            objAction.setPriority(0);
            //ZJ

            if (!dgType.equals(1)) {
                JSONObject upPrntsData = new JSONObject();

                upPrntsData.put("id_O", upperOItem.getId_O());
                upPrntsData.put("index", upperOItem.getIndex());

                upPrntsData.put("wn2qtyneed", objOItem.getWn2qtyneed());


                upPrntsData.put("wrdN", upperOItem.getWrdN());
                objAction.getUpPrnts().add(upPrntsData);

                // this is a new upPrnts
            }
        }

        // Set bmdpt = 4 when the subProd/subTask start with 1(process)
        if (partIndex == 0 && objAction.getBmdpt() == 1) {
            upperAction.setBmdpt(4);
        }

        // 添加产品信息
        if (partIndex > 0 && objActionCollection.isEmpty()) {
            for (int i = 0; i < partIndex; i++) {
                orderActionList.add(new OrderAction());
                orderOItemList.add(new OrderOItem());
            }
        }
        orderActionList.add(objAction);
        orderOItemList.add(objOItem);

        objActionCollection.put(newOrderId, orderActionList);
        partInfo.put("fin_O", newOrderId);
        partInfo.put("fin_Ind", newOrderIndex);

        objOItemCollection.put(newOrderId, orderOItemList);

        // 判断递归bmdpt等于物料或者等于部件, 是就放pidActionCollection，  1/4不放
        if (objAction.getBmdpt() != 1) // && !dgType.equals(1))
        {
            pidActionCollection.put(objOItem.getId_P(), objAction);
        }

        // 循环遍历减一的序号集合
        // NOW I am the last Item in the partArray / subTasks, I can then update ALL the previous Items' wn0prior
        if (partIndex == partArray.size() - 1) {
            boolean keepGoing;

            for (int i = 0; i < partArray.size(); i++) {
                if (partArray.getJSONObject(i).getInteger("fin_Ind") != null) {
//                    qt.errPrint("fin_O?", null, partArray, i, objActionCollection);

                    String finO = partArray.getJSONObject(i).getString("fin_O");
                    int fin_Ind = partArray.getJSONObject(i).getInteger("fin_Ind");
                    // this is myself
                    OrderAction unitAction = objActionCollection.get(finO).get(fin_Ind);

                    int myPrior = partArray.getJSONObject(i).getInteger("wn0prior");
                    keepGoing = true;
                    int checkPrev = i - 1;

                    do {
                        // if it's already the first item or prior 2 steps away, stop
                        if (checkPrev < 0 || (myPrior - 2) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                            keepGoing = false;
                        } else {
                            JSONObject idAndIndex = new JSONObject();
                            idAndIndex.put("id_O", partArray.getJSONObject(checkPrev).getString("fin_O"));
                            idAndIndex.put("index", partArray.getJSONObject(checkPrev).getInteger("fin_Ind"));
                            if (unitAction.getPrtPrev().contains(idAndIndex)) {
//                                System.out.println("repeated");

                            } else if (myPrior != partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                                if ((myPrior - 1) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {

                                    // Here, I put the checking IdIndex into my own list of prtPrev
                                    unitAction.getPrtPrev().add(idAndIndex);
                                }
                            }
                        }
                        checkPrev--; // move 1 step previous
                    } while (keepGoing);
                    unitAction.setSumPrev(unitAction.getPrtPrev().size());

                    keepGoing = true;
                    int checkNext = i + 1;

                    do {
                        if (checkNext == partArray.size()) {
                            keepGoing = false;
                        } else if ((myPrior + 2) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                            keepGoing = false;
                        } else {
                            if (myPrior != partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                                if ((myPrior + 1) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
                                    JSONObject idAndIndex = new JSONObject();
                                    idAndIndex.put("id_O", partArray.getJSONObject(checkNext).getString("fin_O"));
                                    idAndIndex.put("index", partArray.getJSONObject(checkNext).getInteger("fin_Ind"));
                                    // Here, I put the checking IdIndex into my own list of prtPrev
                                    unitAction.getPrtNext().add(idAndIndex);
                                }
                            }
                        }
                        checkNext++;
                    } while (keepGoing);
                }
            }
        }
        //ZJ
        if (isPf && objOItem.getWn0prior() == 1) {
            timeHandleSerialNoIsOneInside = 1;
        }
        //ZJ

        if (!dgType.equals(1)) {
            // 创建JSONObject存储子零件信息
            JSONObject subPartData = new JSONObject();
            // 设置子零件信息
            subPartData.put("id_O", objAction.getId_O());
            subPartData.put("index", objAction.getIndex());
            subPartData.put("id_P", id_P);
            subPartData.put("wrdN", objOItem.getWrdN());
            subPartData.put("prior", objOItem.getWn0prior());
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

        //////////////// prtPrev sumPrev, prtNext, subParts SumChild 3 things set here, then prnt set above

        //////////////////// if this Item has part, go into dgProc
        if (!id_P.equals("")) {
//            Prod thisProd = qt.getMDContent(id_P, Arrays.asList("_id", "part", "info"), Prod.class);
            Prod thisProd = dgProd.get(id_P);
//            System.out.println("thisProd" + thisProd);

//            if (thisProd != null && thisProd.getPart() != null && thisProd.getPart().getJSONArray("objItem").size() > 0 &&
//                    thisProd.getInfo().getId_C().equals(myCompId)) {
            if (thisProd != null && thisProd.getPart() != null && thisProd.getPart().getJSONArray("objItem").size() > 0 &&
                    thisProd.getInfo().getId_C().equals(myCompId)) {
                JSONArray partArrayNext = thisProd.getPart().getJSONArray("objItem");
                objAction.setBmdpt(2);
                // the first item's bmdpt != 6, if ==6 then ALL parts in that part will == 6, then need pick by igura
                if (partArrayNext.getJSONObject(0).getInteger("bmdpt") != 6) {

                    // 进下一层处理part递归
                    for (int item = 0; item < partArrayNext.size(); item++) {

                        this.dgProcess(
                                dgType + 1, myCompId, id_OParent,
                                objOItem, objAction,
                                casItemData,
                                partArrayNext, item,
                                objOItemCollection, objActionCollection,
                                pidActionCollection,
                                thisProd.getId(), oDates, oTasks, mergeJ, timeHandleSerialNoIsOneInside
                                , csId_P, isJsLj, dgProd,divideOrder);
                        //changed dgType
                        // now check the last time and put into objAction a key
                        if (dgType == 2 && item + 1 == partArrayNext.size()) {
                            //TODO KEV put an isPartNext = true to the last item
//                            qt.errPrint("last item", null, objAction, objActionCollection);
                        }
                    }

                }
            }
        }
        //ZJ
        if (dq != 1) {
            // 创建订单时间操作处理专用类
            OrderODate orderODate = new OrderODate();
            // 添加时间操作处理信息
            orderODate.setId_PF(id_PF);
            orderODate.setId_P(objOItem.getId_P());
            orderODate.setPriorItem(partInfo.getInteger("wn0prior"));
            orderODate.setTeStart(0L);
            orderODate.setTaFin(0L);
            // 判断层级为第一层并且序号为1
            if (timeHandleSerialNoIsOneInside == 1 && objOItem.getWn0prior() == 1) {
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
            orderODate.setCsSta(timeHandleSerialNoIsOneInside);
            // 设置订单时间操作信息
            orderODate.setTeDur(partInfo.getLong("teDur") == null ? 120 : partInfo.getLong("teDur"));
            orderODate.setTePrep(partInfo.getLong("tePrep") == null ? 60 : partInfo.getLong("tePrep"));
            // action里面的
            //++ZJ
            orderODate.setPriority(0);
            //ZJ
            orderODate.setTeDurTotal(0L);
            orderODate.setWn2qtyneed(objOItem.getWn2qtyneed());
            // 判断bmdpt等于部件
            if (objAction.getBmdpt() == 2) {
                // 设置订单时间操作信息
                orderODate.setTePrep(180L);
                orderODate.setTeDur(0L);
                // 判断层级为第一层
                if (timeHandleSerialNoIsOneInside == 1) {
                    orderODate.setKaiJie(3);
                } else {
                    // 添加信息
                    orderODate.setKaiJie(5);
                }
            }
//            System.out.println("wn2qtyneed:" + objOItem.getWn2qtyneed() + " - teDurTotal:"
//                    + orderODate.getTeDurTotal() + " - tePrep:" + orderODate.getTePrep() + " - prior:" + objOItem.getWn0prior());
//            System.out.println("csTeJ:" + " - id_P:" + objOItem.getId_P() + " - id_PF:" + id_PF + " - dq:" + dq);
            // 添加信息
            orderODate.setId_O(objAction.getId_O());
            orderODate.setId_C(myCompId);
            orderODate.setIndex(objAction.getIndex());
            orderODate.setGrpB(objOItem.getGrpB());
            oDates.add(orderODate);
            // 判断存储零件编号的合并信息记录合并时的下标为空
            if (null == mergeJ.getInteger(objOItem.getId_P())) {
                // 添加下标信息
                mergeJ.put(objOItem.getId_P(), oDates.size() - 1);
            }
            // 获取零件名称
            String itemWrdN = objOItem.getWrdN().getString("cn");
            boolean isNextPart = null != objAction.getPrtNext() && objAction.getPrtNext().size() == 0 && dgType == 2;

            // 调用生成任务信息
            Task task = TaskObj.getTask(orderODate.getTeStart(), orderODate.getTeFin(), orderODate.getId_O()
                    , orderODate.getIndex(), 0L
                    , orderODate.getPriority(), itemWrdN, orderODate.getTePrep(), orderODate.getTeDelayDate()
                    , myCompId, 0L, 0L, -1, isNextPart);
            // 设置任务公司编号
            task.setId_C(myCompId);
//            System.out.println("task:");
//            System.out.println(JSON.toJSONString(task));
            oTasks.add(task);
        }
        //ZJ
    }

    /**
     * 添加订单方法 - 注释完成
     *
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 8:40
     */
    public void updateSalesOrder(JSONArray casItemData, List<OrderAction> salesAction
            , List<OrderOItem> salesOItem, Order orderParentData
            , JSONObject grpBGroup, JSONObject grpGroup, String myCompId
            , List<OrderODate> oDates, List<Task> oTasks) {
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
        //ZJ
//        salesOrder_Action.put("oDates", oDates);
//        salesOrder_Action.put("oTasks", oTasks);
        //ZJ

        JSONObject salesOrder_OItem;
        JSONArray objCard = new JSONArray();
        objCard.add("action");
        objCard.add("oStock");
        salesOrder_OItem = orderParentData.getOItem();
        salesOrder_OItem.put("objCard", objCard);


        // 设置action信息
        orderParentData.setAction(salesOrder_Action);
        // 设置订单的递归结果map
        orderParentData.setCasItemx(casItemx);
        // 设置oItem.objCard信息
        orderParentData.setOItem(salesOrder_OItem);

        JSONArray view = orderParentData.getView();
        System.out.println("got all ok Sales");

        //Create oStock
        JSONObject newPO_oStock = dbu.initOStock(qt.list2Arr(salesOItem));
        orderParentData.setOStock(newPO_oStock);

        if (!view.contains("action") && !view.contains("Vaction")) {
            view.add("action");
        }
        if (!view.contains("casItemx") && !view.contains("VcasItemx")) {
            view.add("casItemx");
        }
        if (!view.contains("oStock") && !view.contains("VoStock")) {
            view.add("oStock");
        }
        // 设置view值

        orderParentData.setView(view);

        // 新增订单

        qt.saveMD(orderParentData);
//        saveOrder.add(orderParentData);
        System.out.println("sales order SAVED Parent " + orderParentData.getInfo().getWrdN().getString("cn"));
    }


    private synchronized void dgCheckUtil(JSONArray pidList, String id_P, String id_C
            , JSONObject objectMap, JSONArray isRecurred
            , JSONArray isEmpty, JSONObject stat, HashSet<String> id_Ps) {
        try {

//            System.out.println(id_P);
            // 根据父编号获取父产品信息
            Prod thisItem = qt.getMDContent(id_P, qt.strList("info", "part"), Prod.class);
//        System.out.println("thiItem" + thisItem);

            // 层级加一
            stat.put("layer", stat.getInteger("layer") + 1);

            boolean isConflict = false;
            JSONArray checkList = new JSONArray();

            // 判断父产品不为空，部件父产品零件不为空
            if (thisItem != null) {
                for (int i = 0; i < pidList.size(); i++) {
//                System.out.println("冲突Check" + id_P);
                    // 判断编号与当前的有冲突
                    if (pidList.getString(i).equals(id_P)) {
                        // 创建零件信息
                        JSONObject conflictProd = new JSONObject();
                        // 添加零件信息
                        conflictProd.put("id_P", id_P);
                        conflictProd.put("layer", (stat.getInteger("layer") + 1));
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
                        stat.put("count", stat.getInteger("count") + 1);
//                    System.out.println("count " + stat.getInteger("count"));
                        if (null != nextItem.get(j) && null != nextItem.getJSONObject(j).get("id_P")) {

                            // 继续调用验证方法
//                        System.out.println("判断无冲突" + isConflict);
                            if (nextItem.getJSONObject(j).getDouble("wn4qtyneed") == null ||
                                    nextItem.getJSONObject(j).getDouble("wn2qty") == null ||
                                    nextItem.getJSONObject(j).getDouble("wn2port") == null) {
                                objectMap.put("errDesc", "数量为空！");
                                isEmpty.add(objectMap);
                            } else {
                                id_Ps.add(nextItem.getJSONObject(j).getString("id_P"));
                                this.dgCheckUtil(checkList, nextItem.getJSONObject(j).getString("id_P"), id_C, nextItem.getJSONObject(j)
                                        , isRecurred, isEmpty, stat, id_Ps);
                            }
                        } else {
                            if (null != objectMap) {
                                objectMap.put("errDesc", "产品不存在！");
                                isEmpty.add(objectMap);
                            }
                        }
                    }
                }
            } else if (!id_P.equals("")) {
                objectMap.put("errDesc", "产品不存在！");
                isEmpty.add(objectMap);
            }
        } catch (Exception ex) {
            System.out.println("出现异常:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * @param id_P                the P that is merging
     * @param partArray           P.part P's part
     * @param partIndex           P.index
     * @param upperAction         upper layer's Action
     * @param upperOItem          upper layer's OItem
     * @param dgType              If I am the first Layer (I am in Sales' order
     * @param pidActionCollection Check if id_P duplicated, dub = need merge
     * @param objActionCollection List of Action for update
     * @param objOItemCollection  List of OItem for update
     *                            mergePart setup
     */
    private void mergePart(String id_P, JSONArray partArray, Integer partIndex
            , OrderAction upperAction, OrderOItem upperOItem
            , Integer dgType, Map<String, OrderAction> pidActionCollection
            , Map<String, List<OrderAction>> objActionCollection
            , Map<String, List<OrderOItem>> objOItemCollection
            , List<OrderODate> oDates, JSONObject mergeJ) {
        // upperAction and upperOItem @ dgLayer = 1 is here with just regular shit
        // qtyNeed = upperOItem.qtyNeed
        Double qtyNeed = upperOItem.getWn2qtyneed();

        int checkPrev = partIndex - 1;
        Integer checkNext = partIndex + 1;
        boolean keepGoing = true;

        // this finO + fin_Ind points to the "repeated" oItem, so you can update
        String finO = pidActionCollection.get(id_P).getId_O();
        Integer fin_Ind = pidActionCollection.get(id_P).getIndex();
        Integer myPrior = partArray.getJSONObject(partIndex).getInteger("wn0prior");

        partArray.getJSONObject(partIndex).put("fin_O", finO);
        partArray.getJSONObject(partIndex).put("fin_Ind", fin_Ind);

        // this is my Action::
        OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(objOItemCollection.get(finO).get(fin_Ind)), OrderOItem.class);

        OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(objActionCollection.get(finO).get(fin_Ind)), OrderAction.class);

        try {
            do {
                //***** part getIntwn0prior Null pointer
                if (checkPrev < 0 || (myPrior - 2) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                    keepGoing = false;
                } else {
                    JSONObject idAndIndex = new JSONObject();
                    idAndIndex.put("id_O", finO);
                    idAndIndex.put("index", fin_Ind);

                    if (unitAction.getPrtPrev().contains(idAndIndex) ||
                            (finO.equals(unitOItem.getId_O()) && fin_Ind.equals(unitOItem.getIndex()))) {
//                        System.out.println("repeated");

                    } else {
                        if (!myPrior.equals(partArray.getJSONObject(checkPrev).getInteger("wn0prior"))) {
                            if ((myPrior - 1) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                                unitAction.getPrtPrev().add(idAndIndex);
                            }
                        }
                    }
                }
                checkPrev--;
            } while (keepGoing);
        } catch (Exception e) {
            System.out.println("合并异常:" + e.getMessage());
            e.printStackTrace();
        }

        unitAction.setSumPrev(unitAction.getPrtPrev().size());
        //if firstLayer merge, then DO NOT setup upPrnts, because there should be No upPrnts
        //but then, set all subparts' upPrnts to include my qty

        if (dgType.equals(1)) {
            for (int i = 0; i < unitAction.getSubParts().size(); i++) {
                String subId_O = unitAction.getSubParts().getJSONObject(i).getString("id_O");
                Integer subIndex = unitAction.getSubParts().getJSONObject(i).getInteger("index");
                OrderAction subAction = objActionCollection.get(subId_O).get(subIndex);
                JSONObject upPrntsData = new JSONObject();
                upPrntsData.put("id_O", upperOItem.getId_O());
                upPrntsData.put("index", upperOItem.getIndex());
                upPrntsData.put("wrdN", upperOItem.getWrdN());
                upPrntsData.put("wn2qtyneed", upperOItem.getWn2qtyneed() * unitAction.getSubParts().getJSONObject(i).getDouble("qtyEach"));
                upPrntsData.put("Subi", i);

                subAction.getUpPrnts().add(upPrntsData);
            }
        } else {
            JSONObject upPrntsData = new JSONObject();
            upPrntsData.put("id_O", upperOItem.getId_O());
            upPrntsData.put("index", upperOItem.getIndex());
            upPrntsData.put("wn2qtyneed", upperOItem.getWn2qtyneed() * partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));
            upPrntsData.put("wrdN", upperOItem.getWrdN());


            unitAction.getUpPrnts().add(upPrntsData);
//            System.out.println(unitAction);
            objActionCollection.get(finO).set(fin_Ind, unitAction);
        }

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


        // 加时间 oDate 时间

        // Loop into all subParts to make sure all qty is added correctly
        this.dgMergeQtySet(finO, qtyNeed, partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"),
                finO, fin_Ind, dgType, objActionCollection, objOItemCollection, oDates, mergeJ);

        if (dgType.equals(1)) {
//            System.out.println("partIndex@1" + partIndex);
            // fix the action of the next+ item using the unitAction of the 1st one
            unitAction.setIndex(partIndex);
            for (int i = 0; i < unitAction.getSubParts().size(); i++) {
                unitAction.getSubParts().getJSONObject(i).put("upIndex", partIndex);
            }

            objActionCollection.get(finO).add(unitAction);
            objOItemCollection.get(finO).add(unitOItem);
        }
    }

    private void dgMergeQtySet(String id_OP, Double qtyNeed, Double qtySubNeed
            , String finO, Integer fin_Ind, Integer dgType
            , Map<String, List<OrderAction>> objActionCollection
            , Map<String, List<OrderOItem>> objOItemCollection
            , List<OrderODate> oDates, JSONObject mergeJ) {
        OrderOItem unitOItem = objOItemCollection.get(finO).get(fin_Ind);
        OrderAction unitAction = objActionCollection.get(finO).get(fin_Ind);

        if (qtySubNeed == null) {
            qtySubNeed = 1.0;
        }


        Double currentQty = unitOItem.getWn2qtyneed();
//        System.out.println("I am checking here");
//        System.out.println(currentQty);
//        System.out.println(unitOItem.getWrdN().getString("cn"));
//        System.out.println(qtyNeed);

        unitOItem.setWn2qtyneed(currentQty + qtyNeed * qtySubNeed);

//        System.out.println(unitOItem.getWn2qtyneed());


        // summ qtyall and qtyneed if I am not the main Parent
        if (!id_OP.equals(finO)) {


            Integer ind = mergeJ.getInteger(unitOItem.getId_P());
            OrderODate oDa = oDates.get(ind);
            oDa.setWn2qtyneed(oDa.getWn2qtyneed() + currentQty);
            oDates.set(ind, oDa);

//            System.out.println(id_OP + finO);
            if (!dgType.equals(1)) {
                JSONObject upPrntsData = unitAction.getUpPrnts().getJSONObject(0);
                upPrntsData.put("wn2qtyneed", unitOItem.getWn2qtyneed());
            } else {
                dgType = 2;
            }

        }

        for (int i = 0; i < unitAction.getSubParts().size(); i++) {
            //Here I dig into each subParts and sum qtyall / qtyneed
            JSONObject unit = unitAction.getSubParts().getJSONObject(i);

            this.dgMergeQtySet(id_OP, qtyNeed * qtySubNeed, unit.getDouble("qtyEach"),
                    unit.getString("id_O"), unit.getInteger("index"), dgType, objActionCollection, objOItemCollection
                    , oDates, mergeJ);
        }
    }
}
