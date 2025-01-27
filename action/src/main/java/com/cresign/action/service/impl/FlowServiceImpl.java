package com.cresign.action.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.common.ActionEnum;
import com.cresign.action.service.FlowService;
import com.cresign.action.utils.Obj;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;

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
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class FlowServiceImpl implements FlowService {

//    private static final Map<Long, List<Task>> taskM = new HashMap<>();
//    private static final Map<Long,Long> zonM = new HashMap<>();

    @Autowired
    private CoupaUtil coupaUtil;

    @Autowired
    private DbUtils dbUtils;

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private RetResult retResult;

    @Autowired
    private Qt qt;

    @Autowired
    private TimeZjServiceImpl timeZjService;


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse getDgResult(String id_OParent,String id_U,String myCompId,Long teStart) {

            // 调用方法获取订单信息
            Order salesOrderData = coupaUtil.getOrderByListKey(
                    id_OParent, Arrays.asList("oItem", "info","view", "action"));

            // 判断订单是否为空
            if (null == salesOrderData) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
            }


        if (!salesOrderData.getInfo().getId_C().equals(myCompId))
            {
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_SUPPLIER_ID_IS_NULL.getCode(), "必须是自己生产的");
            }

            for (int i = 0; i < salesOrderData.getOItem().getJSONArray("objItem").size(); i++)
            {
                String id_P = salesOrderData.getOItem().getJSONArray("objItem").getJSONObject(i).getString("id_P");
                // 创建异常信息存储
                JSONArray isRecurred = new JSONArray();
                // 创建产品信息存储
                JSONArray isEmpty = new JSONArray();
                // 创建零件id集合
                JSONArray pidList = new JSONArray();
                JSONObject nextPart = new JSONObject();

                JSONObject stat = new JSONObject();
                stat.put("layer", 0);
                stat.put("count", 0);


                // ******调用验证方法******
                this.dgCheckUtil(pidList,id_P, myCompId, nextPart,isRecurred,isEmpty, stat);

                if (isRecurred.size() > 0)
                {
                    throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_RECURRED.getCode(), id_P);
                }
                if (isEmpty.size() > 0)
                {
                    throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NOT_EXIST.getCode(), id_P);
                }
            }



        // 第一次把action卡创建出来
            if (null == salesOrderData.getAction()) {
                JSONObject newEmptyAction = new JSONObject();
                salesOrderData.setAction(newEmptyAction);
            }
            if (null != salesOrderData.getAction().getString("isDg")) {
                // 返回为空错误信息
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_OPERATION_IS_PROCESSED.getCode(), "已经被递归了");
            }
            if (salesOrderData.getInfo().getLST() != 7)
            {
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_ORDER_NEED_FINAL.getCode(), "需要两方确认");
            }

            // 转换oItem为list
            JSONArray oParent_objItem = salesOrderData.getOItem().getJSONArray("objItem");

            Double oParent_prior = salesOrderData.getInfo().getPriority();

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

            OrderOItem objOItem = JSONObject.parseObject(JSON.toJSONString(oParent_objItem.getJSONObject(item)),OrderOItem.class);

            OrderAction objAction = new OrderAction(100,0,0,1,salesOrderData.getId(),
                    refOP, objOItem.getId_P(),id_OParent,item,objOItem.getWn0prior(),0,0,
                    new JSONArray(),new JSONArray(), new JSONArray(), new JSONArray(), salesOrderData.getInfo().getWrdN(), objOItem.getWrdN());

                ////////////////actually dg ///////////////////////
            JSONObject isJsLj = new JSONObject();
            isJsLj.put("1",0);
            //dgType: 1 = firstLayer (sales Items), 2 = regular/subTask or subProd, 3 = depSplit regular
            // T/P - T/P -T/P.... problem is id_P == ""?


                this.dgProcess(
                        1, myCompId, id_OParent,
                        objOItem, objAction,
                        casItemData,
                        oParent_objItem, item,
                        objOItemCollection, objActionCollection,
                        pidActionCollection,
                        objOItem.getId_P(), oDates,oTasks,mergeJ,0,null,isJsLj);
        }

        // 判断递归结果是否为空
        if (objActionCollection.size() <= 0){
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_RECURSION_RESULT_IS_NULL.getCode(), "递归结果为空");
        }


            /////////// setup Dep.objlBProd + objlSProd for grpP

        //putting the Sales order as the last casItem... I donno why
        JSONObject thisOrderData = new JSONObject();
        thisOrderData.put("id_C", myCompId);
        thisOrderData.put("id_O", id_OParent);
        thisOrderData.put("lST", 4);
        thisOrderData.put("type",1);
        thisOrderData.put("size",oParent_objItem.size());
        thisOrderData.put("wrdN", salesOrderData.getInfo().getWrdN());
        casItemData.add(thisOrderData);


        // 获取递归结果键
        Set<String> actionCollection = objActionCollection.keySet();
//        //before getting so many id_A, get myComp id_A first for future use
//        String myId_A = dbUtils.getId_A(myCompId, "a-auth");
//        Asset myDef = dbUtils.getAssetById(myId_A, Collections.singletonList("def"));
        Asset myDef = qt.getConfig(myCompId, "a-auth", "def");

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
                for (int j = 0; j < casItemData.size(); j++)
                {
                    if (casItemData.getJSONObject(j).getString("id_O").equals(thisOrderId))
                    {
                        prodCompId = casItemData.getJSONObject(j).getString("id_C");
                        orderNameCas = casItemData.getJSONObject(j).getJSONObject("wrdN");
                        break;
                    }
                }

                JSONObject grpBGroup = new JSONObject();
                JSONObject grpGroup = new JSONObject();
                JSONObject listData = new JSONObject();
                JSONArray lsbArray = dbUtils.getEsKey("id_C", prodCompId, "id_CB", targetCompId, "lsbcomp");
                if (lsbArray.size() > 0) {
                    listData = lsbArray.getJSONObject(0);
                }
                String grpO = "";
                String grpOB = "1000";
//                String aId;
                Asset asset = null;

                if (!targetCompId.equals(myCompId))
                {
//                    aId = dbUtils.getId_A(targetCompId, "a-auth");

                    asset = qt.getConfig(targetCompId,"a-auth","def");


                    if  (asset.getId().equals("none")) {
                        asset = myDef;
                    }
                } else {
//                    aId = myDef.getId();
                    asset = myDef;
                }
                System.out.println("otherDeff"+targetCompId);


                // if it is a real Company get grpB setting from objlBProd by ref, else do nothing now, later can do extra
//                if (!aId.equals("none")) {
//                    Asset asset;
//                    if (!targetCompId.equals(myCompId))
//                    {
//                        asset = dbUtils.getAssetById(aId, Collections.singletonList("def"));
//
//                    } else {
//                        asset = myDef;
//                    }
                    JSONObject defResultBP = asset.getDef().getJSONObject("objlBP");
                    JSONObject defResultBC = asset.getDef().getJSONObject("objlBC");

                    for (OrderOItem orderOItem : unitOItem) {
                        System.out.println(orderOItem.getGrpB());
                        String grpB = orderOItem.getGrpB();
                        if (grpBGroup.getJSONObject(grpB) == null) {
                            grpBGroup.put(grpB, defResultBP.getJSONObject(grpB));
                        }
                    }
                    // get GrpB and grpS of id_CB
//                    if (prodCompId.equals(targetCompId))
//                    {   // I am both
//                        grpOB = "1010";
//                    } else {
//                        String compGrpB = listData.getString("grpB");
//
//                        grpOB = defResultBC.getJSONObject(compGrpB).getString("grpO");
////                        for (int k = 0; k < defResultBC.size(); k++)
////                        {
////                            String grpRef = defResultBC.getJSONObject(k).getString("ref");
////                            if (compGrpB.equals(grpRef))
////                            {
////                                grpOB =  defResultBC.getJSONObject(k).getString("grpO");
////                                break;
////                            }
////                        }
////                    }
//                }

//                String aId2;

                Asset asset2 = null;

                if (!prodCompId.equals(myCompId))
                {
                    asset2 = qt.getConfig(prodCompId,"a-auth","def");
                    if  (asset2.getId().equals("none")) {

                        asset2 = myDef;
                    }

                } else {
                    asset2 = myDef;
                }

//                if (!prodCompId.equals(myCompId))
//                {
//                    aId2 = dbUtils.getId_A(prodCompId, "a-auth");
//                } else if (myCompId.equals(targetCompId)) {
//                    aId2 = "none";
//                } else {
//                    aId2 = myDef.getId();
//                }

                // if it is a real Company get grpB setting from objlBP by ref, else do nothing now, later can do extra
//                if (!aId2.equals("none")) {
//                    Asset asset;
//                    if (!prodCompId.equals(myCompId))
//                    {
//                        asset = dbUtils.getAssetById(aId2, Collections.singletonList("def"));
//
//                    } else {
//                        asset = myDef;
//                    }


                JSONObject defResultSP = asset2.getDef().getJSONObject("objlSP");

                JSONObject defResultSC = asset2.getDef().getJSONObject("objlSC");

                    for (OrderOItem orderOItem : unitOItem) {
                        String grp = orderOItem.getGrp();

                        if (grpGroup.getJSONObject(grp) == null) {

                            grpGroup.put(grp, defResultSP.getJSONObject(grp));
                        }
                    }

//                 get GrpB and grpS of id_CB
//                    if (prodCompId.equals(targetCompId))
//                    {   // I am both
//                        grpO = "1010";
//                    } else
//                    {
//                        String compGrpB = listData.getString("grp");
//                        grpOB = defResultSC.getJSONObject(compGrpB).getString("grpO");
//
//                    for (int k = 0; k < defResultSC.size(); k++)
//                        {
//                            String grpRef = defResultSC.getJSONObject(k).getString("ref");
//                            if (compGrpB.equals(grpRef))
//                            {
//                                grpOB =  defResultSC.getJSONObject(k).getString("grpO");
//                                break;
//                            }
//                        }
//                    }

//                  }
                grpO = "1000";
                grpOB = "1000";

                System.out.print("got all ok");

                if (id_OParent.equals(thisOrderId)) {
                    // make sales order Action
                    System.out.print("sales order");
                    System.out.print(unitAction);

                    System.out.print(oDates);

                    this.updateSalesOrder(casItemData,unitAction,unitOItem,salesOrderData,grpBGroup, grpGroup, prodCompId
                    ,oDates,oTasks);

                } else
                {
                // else make Purchase Order
//                // 创建订单
                Order newPO = new Order();
                    System.out.println("sales order");


                    // 根据键设置订单id
                newPO.setId(thisOrderId);
                    System.out.print("got1"+thisOrderId);

                // priority is BY order, get from info and write into ALL oItem
                OrderInfo newPO_Info = new OrderInfo(prodCompId,targetCompId,unitOItem.get(0).getId_CP(),"", id_OParent,"","",grpO,grpOB,oParent_prior,unitOItem.get(0).getPic(),4,0,orderNameCas,null);
                    System.out.println("sales order");

                // 设置订单info信息
                newPO.setInfo(newPO_Info);

                // 添加View信息
                JSONArray view = new JSONArray();
                view.add("info");
                view.add("action");
                view.add("oItem");
                view.add("oStock");
                newPO.setView(view);
                    System.out.println("sales order");

                JSONArray objCard = new JSONArray();
                objCard.add("action");
                objCard.add("oStock");

                Double wn2qty = 0.0;
                Double wn4price = 0.0;
                JSONArray arrayId_P = new JSONArray();
                    System.out.println("sales order");

                for (OrderOItem orderOItem : unitOItem) {
                    wn2qty += orderOItem.getWn2qtyneed();
                    wn4price += orderOItem.getWn4price();
                    System.out.println("u " + orderOItem);
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

                // 创建采购单的Action
                JSONObject newPO_Action = new JSONObject();
                newPO_Action.put("objAction", unitAction);
                newPO_Action.put("grpBGroup", grpBGroup);
                newPO_Action.put("grpGroup", grpGroup);
                newPO_Action.put("wn2progress", 0.0);

                //Create oStock
                JSONObject newPO_oStock = new JSONObject();
                newPO_oStock.put("objData", new JSONArray());

                    for (OrderOItem orderOItem : unitOItem) {
                        JSONObject initStock = new JSONObject();
                        initStock.put("wn2qtynow", 0);
                        initStock.put("wn2qtymade", 0);
                        initStock.put("id_P", orderOItem.getId_P());

                        newPO_oStock.getJSONArray("objData").add(initStock);
                    }


                    newPO.setOStock(newPO_oStock);

                newPO.setAction(newPO_Action);
                // 新增订单
//                coupaUtil.saveOrder(newPO);
System.out.println("saving now");
                    qt.addMD(newPO);
                    System.out.println("sales order SAVED "+ newPO.getInfo().getWrdN().getString("cn"));


//              // 创建lSBOrder订单
                lSBOrder lsbOrder = new lSBOrder(prodCompId,targetCompId,"","",id_OParent,thisOrderId, arrayId_P,
                            "","",null,"1000",unitOItem.get(0).getPic(),4,0,orderNameCas,null,null);
                    // 新增lsbOrder信息

                    qt.addES("lsborder", lsbOrder);
//                coupaUtil.updateES_lSBOrder(lsbOrder);

                }
        }
            System.out.println("all finished...");
            // END FOR


//            // 创建返回结果存储集合
//            JSONObject result = new JSONObject();
//
//            // 添加返回信息
//           // result.put("compIds",compIds);
//           // result.put("orderIds",orderIds);
////            result.put("action",objActionCollection);
////            result.put("oitem",objOItemCollection);
////            result.put("grpBGroup",grpBGroup);
////            result.put("grpGroup",grpGroup);

        // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), "");
        }

        /**
         * 添加订单方法 - 注释完成
         * @author tang
         * @ver 1.0.0
         * @date 2020/8/6 8:40
         */
        public void updateSalesOrder(JSONArray casItemData,List<OrderAction> salesAction, List<OrderOItem> salesOItem,
                                     Order orderParentData,JSONObject grpBGroup,JSONObject grpGroup, String myCompId,
                                     List<OrderODate> oDates,List<Task> oTasks)
        {

            // 添加订单基础信息存储
            JSONObject casItemx = new JSONObject();
            JSONObject nowData = new JSONObject();
            nowData.put("objOrder", casItemData);
            casItemx.put(myCompId,nowData);

            // 创建产品零件递归信息
            JSONObject salesOrder_Action = new JSONObject();

            // 添加对应的产品零件递归信息
            salesOrder_Action.put("objAction",salesAction);
            salesOrder_Action.put("isDg", "true");
            salesOrder_Action.put("grpBGroup",grpBGroup);
            salesOrder_Action.put("grpGroup", grpGroup);
            salesOrder_Action.put("wn2progress", 0.0);
            //ZJ
            salesOrder_Action.put("oDates",oDates);
            salesOrder_Action.put("oTasks",oTasks);
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
            JSONObject newPO_oStock = new JSONObject();
            newPO_oStock.put("objData", new JSONArray());
            for (OrderOItem orderOItem : salesOItem) {
                JSONObject initStock = new JSONObject();
                initStock.put("wn2qtynow", 0);
                initStock.put("wn2qtymade", 0);
                initStock.put("id_P", orderOItem.getId_P());
                newPO_oStock.getJSONArray("objData").add(initStock);
            }
            orderParentData.setOStock(newPO_oStock);

            if(!view.contains("action")) {
                view.add("action");
            }
            if(!view.contains("casItemx")) {
                view.add("casItemx");
            }
            if(!view.contains("oStock")) {
                view.add("oStock");
            }
            // 设置view值

            orderParentData.setView(view);

            // 新增订单
//            coupaUtil.saveOrder(orderParentData);
            System.out.println("saving Sales"+orderParentData);


            qt.saveMD(orderParentData);
            System.out.println("sales order SAVED Parent "+ orderParentData.getInfo().getWrdN().getString("cn"));

        }





    /**
     * 递归方法 - 注释完成
//     * @param id_P	产品id
//     * @param newObjectId    随机id
//     * @param priority  序号
//     * @param objOItemCollection   oitem递归数据集合
//     * @param objActionCollection  action递归数据集合
//     * @param orderIds  订单id集合
//     * @param compIds   公司id集合
//     * @param id_O   订单id
//     * @param pidActionCollection    action递归数据重复集合
    //     * @param orderCompPosition 存储订单对应公司的职位集合
     * @author tang
     * @ver 1.0.0
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


    private void dgProcess(
            Integer dgType, String myCompId, String id_OParent,
            OrderOItem upperOItem, OrderAction upperAction,
            JSONArray casItemData,
            JSONArray partArray, Integer partIndex,
             Map<String, List<OrderOItem>> objOItemCollection,
             Map<String, List<OrderAction>> objActionCollection,
             Map<String, OrderAction> pidActionCollection,

            String id_PF,List<OrderODate> oDates,List<Task> oTasks,JSONObject mergeJ,int csSta,String csId_P,JSONObject isJsLj)
    {
        //ZJ
        isJsLj.put("1",(isJsLj.getInteger("1")+1));
        int dq = isJsLj.getInteger("1");
        // 存储序号是否为1层级
        int csStaN = 0;
        // 获取父id是否是当前唯一ID存储时间处理的最初产品编号存储
        boolean isPf = id_PF.equals(csId_P);
        // 判断上一个序号是否为1层级
        if (csSta == 1) {
            // 判断是，则将自己也设置为是
            csStaN = 1;
        }
        //ZJ



        String id_P = partArray.getJSONObject(partIndex).getString("id_P");
        JSONObject partInfo = partArray.getJSONObject(partIndex);
        String prodCompId = partInfo.getString("id_C");

        // 获取, 设新 当前订单id
        // 定义递归订单id
        String newOrderId ="";
        Integer newOrderIndex = 0;

//        if (dgType.equals(1)) {
//            newOrderId = id_OParent;
//            newOrderIndex = partIndex;
//
////            // Here we check the product list if it is repeated
////            // if it is repeat
////                // can I use the mergePart to just increase the qty of the subs
////            System.out.println("I merged2////");
////            partInfo.put("fin_O", pidActionCollection.get(id_P).getId_O());
////            partInfo.put("fin_Ind", pidActionCollection.get(id_P).getIndex());
////
////            this.mergePart(id_P, partArray,partIndex,upperAction, upperOItem,
////                    // upperOItem.getWn2qtyneed(),
////                    pidActionCollection,
////                    objActionCollection, objOItemCollection
////            );
////            // else, put into the checker list Now here we put the id_P into checker list
//
//        }
        // if this is a task, and it's not Sales layer
        //else
        if (id_P.equals("") && dgType.equals(2))
        {
            if (partIndex.equals(0))
            {
                //if I am the first one, make new Order
                newOrderId = qt.GetObjectId();

                //same detail into casItemx
                JSONObject thisOrderData = new JSONObject();
                thisOrderData.put("id_C", myCompId);
                thisOrderData.put("id_O", newOrderId);
                thisOrderData.put("lST", 4);
                thisOrderData.put("size",1);
                thisOrderData.put("type", 4); //task - subTask
                JSONObject casWrdN = new JSONObject();
                casWrdN.put("cn", upperOItem.getWrdN().getString("cn")+"的子任务单");
                thisOrderData.put("wrdN", casWrdN);
                casItemData.add(thisOrderData);
            } else {
                // not the first one, get Id_O from casItemData
                int casIndex = casItemData.size();
                newOrderId = casItemData.getJSONObject(casIndex - 1).getString("id_O");
                newOrderIndex = partIndex;
                casItemData.getJSONObject(casIndex-1).put("size", partIndex + 1);
            }
        }
        else if (null != pidActionCollection.get(id_P))
        {   //Now, it is prod not task， Must Merge if id_P is in pidActionCollection
            // as long as you have it in pidArray, you MUST merge
            System.out.println("I merged2////"+ upperAction);


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
            //ZJ


//            partInfo.put("fin_O", pidActionCollection.get(id_P).getId_O());
//            partInfo.put("fin_Ind", pidActionCollection.get(id_P).getIndex());

            this.mergePart(id_P, partArray,partIndex,upperAction, upperOItem, dgType,
                   // upperOItem.getWn2qtyneed(),
                    pidActionCollection,
                    objActionCollection, objOItemCollection
                    ,oDates,mergeJ
            );


            System.out.println("salesOrder@now");
            System.out.println(objActionCollection.get(fin_O));
            System.out.println(objActionCollection.get(id_OParent));
            return;
        }
        else if (dgType.equals(1)) {
            newOrderId = id_OParent;
            newOrderIndex = partIndex;
        }
        else {
            // Now need to check if prodCompId is in casItemData, if so, get and append 1 more prod in that order
            // 根据公司获取递归订单id
            boolean isNew = true;

            for (int k = 0; k < casItemData.size(); k++) {
                System.out.println("casItem"+casItemData.getJSONObject(k));
                // 2 cases: prod is mine, check only if casItemx Type =2 and not 4
                if(prodCompId.equals(myCompId)&&
                        prodCompId.equals(casItemData.getJSONObject(k).getString("id_C")) &&
                        casItemData.getJSONObject(k).getString("type").equals("2") ||
                        prodCompId.equals(casItemData.getJSONObject(k).getString("id_C"))) {

                    newOrderId = casItemData.getJSONObject(k).getString("id_O");
                    newOrderIndex = casItemData.getJSONObject(k).getInteger("size");
                    casItemData.getJSONObject(k).put("size", newOrderIndex + 1);
                    isNew = false;
                }
            }
            if (isNew) {

                // 赋值随机id
                newOrderId = qt.GetObjectId();
                newOrderIndex = 0;

                // 递归订单map添加随机id
                JSONObject orderName = new JSONObject();
                if (prodCompId.equals(myCompId))
                {
                    orderName.put("cn", upperAction.getRefOP() + " 派工单-00" + casItemData.size());

                } else {
                    orderName.put("cn", upperAction.getRefOP() + " 采购单-00" + casItemData.size());
                }

                JSONObject thisOrderData = new JSONObject();
                thisOrderData.put("id_C", prodCompId);
                thisOrderData.put("id_O", newOrderId);
                thisOrderData.put("lST", 4);
                thisOrderData.put("size",1);
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
        if (dgType.equals(1))
        {
//            objOItem.setId_CB(partInfo.getString("id_CB"));
//            objOItem.setWn2qtyneed(upperOItem.getWn2qtyneed());
            objOItem = upperOItem;
            objAction = upperAction;
        }
        else if(id_P.equals("")) //IF i am process
        {
            System.out.println("Upp"+newOrderId);
            System.out.println("upp"+partArray.size()+partArray.getJSONObject(0).getJSONObject("wrdN"));
            System.out.println("upp"+partIndex);
            JSONObject unitTask = partArray.getJSONObject(partIndex);

            objOItem = new OrderOItem("",upperOItem.getId_O(),myCompId, myCompId,myCompId,newOrderId,partIndex,1,"","",
                            "1009", upperOItem.getGrpB(), unitTask.getInteger("wn0prior"),"",0,0,1.0,0.0,
                            upperOItem.getWrdN(), unitTask.getJSONObject("wrdN"),unitTask.getJSONObject("wrddesc"),unitTask.getJSONObject("wrdprep"));

            objOItem.setSeq(unitTask.getString("seq"));

            objAction = new OrderAction(100,0,1,1,id_OParent,upperAction.getRefOP(),"",newOrderId,partIndex,
                    unitTask.getInteger("wn0prior"),0,0,null, null,null, null,
                    upperOItem.getWrdN(),unitTask.getJSONObject("wrdN"));

            System.out.println("oItem"+objOItem);


//            JSONObject upPrntsData = new JSONObject();
//            upPrntsData.put("id_O", upperOItem.getId_O());
//            upPrntsData.put("index", upperOItem.getIndex());
//            upPrntsData.put("wrdN", upperOItem.getWrdN());
//            upPrntsData.put("wn2qtyneed",1);

            JSONObject upPrntsData = objAction.upPrnt(upperOItem.getId_O(), upperOItem.getIndex(),  upperOItem.getWrdN(),1.0);
//            upPrntsData.put("wn2qtyall",upperOItem.getWn2qtyneed());
            objAction.getUpPrnts().add(upPrntsData);

        } else
        {
            objOItem = new OrderOItem(id_P,upperOItem.getId_OP(),
                    partInfo.getString("id_CP")==null? prodCompId:partInfo.getString("id_CP"),
                    prodCompId,myCompId,
                    newOrderId,newOrderIndex,
                    partInfo.getInteger("bmdpt"),partInfo.getString("ref"),partInfo.getString("refB"),
                    partInfo.getString("grp"),partInfo.getString("grpB"),0,partInfo.getString("pic"),
                    partInfo.getInteger("lUT"),partInfo.getInteger("lCR"),
                    0.0,
                    partInfo.getDouble("wn4price"),upperOItem.getWrdN(),partInfo.getJSONObject("wrdN"),
                    partInfo.getJSONObject("wrddesc"),partInfo.getJSONObject("wrdprep"));

            objOItem.setTmd(dateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
            objOItem.setSeq("3"); // set DGAction specific seq = 3
            objOItem.setWn2qtyneed(upperOItem.getWn2qtyneed() * partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));

            objAction = new OrderAction(100,0,1,partInfo.getInteger("bmdpt"),
                    id_OParent, upperAction.getRefOP(),partInfo.getString("id_P"),newOrderId,newOrderIndex,partInfo.getInteger("wn0prior"),0
                    ,0,null,null,null,null,upperOItem.getWrdN(),partInfo.getJSONObject("wrdN"));

            //ZJ
            objAction.setPriority(0);
            //ZJ

            if (!dgType.equals(1)) {
                JSONObject upPrntsData = new JSONObject();

                upPrntsData.put("id_O", upperOItem.getId_O());
                upPrntsData.put("index", upperOItem.getIndex());
//                upPrntsData.put("I am first", upperOItem.getIndex());
//                upPrntsData.put("wn2qty1", upperOItem.getWn2qtyneed());
//                upPrntsData.put("wn2qty2", objOItem.getWn2qtyneed());
//                upPrntsData.put("wn2qty3", partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));
//                upPrntsData.put("wn2qtyall", upperOItem.getWn2qtyneed());
                upPrntsData.put("wn2qtyneed", objOItem.getWn2qtyneed());
//                upPrntsData.put("qtyEach", partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));


                upPrntsData.put("wrdN", upperOItem.getWrdN());
                objAction.getUpPrnts().add(upPrntsData);

                // this is a new upPrnts
            }
        }

        // Set bmdpt = 4 when the subProd/subTask start with 1(process)
        if (partIndex == 0 && objAction.getBmdpt() == 1)
        {
            upperAction.setBmdpt(4);
        }


        // 添加产品信息
        orderActionList.add(objAction);
        orderOItemList.add(objOItem);


            objActionCollection.put(newOrderId,orderActionList);
            partInfo.put("fin_O", newOrderId);
            partInfo.put("fin_Ind", newOrderIndex);

        objOItemCollection.put(newOrderId,orderOItemList);

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
                    } else if (myPrior != partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                        if ((myPrior - 1) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                            // else prior need add PrtPrev
                            JSONObject idAndIndex = new JSONObject();
                            idAndIndex.put("id_O", partArray.getJSONObject(checkPrev).getString("fin_O"));
                            idAndIndex.put("index", partArray.getJSONObject(checkPrev).getInteger("fin_Ind"));
                            // Here, I put the checking IdIndex into my own list of prtPrev
                            unitAction.getPrtPrev().add(idAndIndex);
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
                    }
                    else if ((myPrior + 2) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
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
        //ZJ
        if (isPf&&objOItem.getWn0prior() == 1) {
            csStaN = 1;
        }
        //ZJ

        if(!dgType.equals(1)) {
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

        //////////////// prtPrev sumPrev, prtNext, subParts SumChild 3 things set here, then prnt set above

        //////////////////// if this Item has part, go into dgProc
        if (!id_P.equals(""))
        {
//        Prod thisProd = coupaUtil.getProdByListKey(id_P, Arrays.asList("_id","part","info"));
        Prod thisProd = qt.getMDContent(id_P, Arrays.asList("_id","part","info"), Prod.class);

        System.out.println("thisProd"+thisProd);

        if (thisProd != null && thisProd.getPart() != null && thisProd.getPart().getJSONArray("objItem").size() > 0 &&
                thisProd.getInfo().getId_C().equals(myCompId))
            {
                JSONArray partArrayNext = thisProd.getPart().getJSONArray("objItem");
                objAction.setBmdpt(2);
                // the first item's bmdpt != 6, if ==6 then ALL parts in that part will == 6, then need pick by igura
                if (partArrayNext.getJSONObject(0).getInteger("bmdpt") != 6) {
//                        partArrayNext.sort((o1, o2) -> {
//                            // 获取第一个的wn0prior
//                            JSONObject o1O = JSON.parseObject(JSON.toJSONString(o1));
//                            Integer o1I = o1O.getInteger("wn0prior");
//
//                            // 获取第一个的wn0prior
//                            JSONObject o2O = JSON.parseObject(JSON.toJSONString(o2));
//                            Integer o2I = o2O.getInteger("wn0prior");
//
//                            // 进行升序排序
//                            return o1I.compareTo(o2I);
//                        });

                        // 进下一层处理part递归
                        for (int item = 0; item < partArrayNext.size(); item++) {
                            this.dgProcess(
                                    2, myCompId, id_OParent,
                                    objOItem, objAction,
                                    casItemData,
                                    partArrayNext, item,
                                    objOItemCollection, objActionCollection,
                                    pidActionCollection,
                                    thisProd.getId(),oDates,oTasks,mergeJ,csStaN,csId_P,isJsLj);
                        }

                }
            }
        } else {
            if (objOItem.getSubTask() != null && objOItem.getSubTask().size() > 0) {
                for (int item = 0; item < objOItem.getSubTask().size(); item++)
                {

                    this.dgProcess(
                            2, myCompId,  newOrderId,
                            objOItem, objAction,
                            casItemData,
                            objOItem.getSubTask(), item,
                            objOItemCollection, objActionCollection,
                            pidActionCollection
                            ,id_PF,oDates,oTasks,mergeJ,csStaN,csId_P,isJsLj);
                }
            }
        }

        //ZJ
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
            System.out.println("wn2qtyneed:"+objOItem.getWn2qtyneed()+" - teDurTotal:"
                    +orderODate.getTeDurTotal()+" - tePrep:"+orderODate.getTePrep()+" - prior:"+objOItem.getWn0prior());
            System.out.println("csTeJ:"+" - id_P:"+objOItem.getId_P()+" - id_PF:"+id_PF+" - dq:"+dq);
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
        //ZJ

 }

    /**
     *
     * @param id_P the P that is merging
     * @param partArray P.part P's part
     * @param partIndex P.index
     * @param upperAction upper layer's Action
     * @param upperOItem upper layer's OItem
     * @param dgType If I am the first Layer (I am in Sales' order
     * @param pidActionCollection Check if id_P duplicated, dub = need merge
     * @param objActionCollection List of Action for update
     * @param objOItemCollection List of OItem for update
     * mergePart setup
     */
    private void mergePart(String id_P, JSONArray partArray, Integer partIndex,
                             OrderAction upperAction, OrderOItem upperOItem, Integer dgType,
                           Map<String, OrderAction> pidActionCollection,
            Map<String, List<OrderAction>> objActionCollection,Map<String, List<OrderOItem>> objOItemCollection,
                           List<OrderODate> oDates,JSONObject mergeJ               )
    {
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
        System.out.println("DGType");

        // this is my Action::
        OrderOItem unitOItem = JSONObject.parseObject(JSON.toJSONString(objOItemCollection.get(finO).get(fin_Ind)),OrderOItem.class);

        OrderAction unitAction = JSONObject.parseObject(JSON.toJSONString(objActionCollection.get(finO).get(fin_Ind)),OrderAction.class);

        try {
//            System.out.println("DGType" + partArray.getJSONObject(3).getString("dsafsvncksfsd"));


            do {
                //***** part getIntwn0prior Null pointer
                if (checkPrev < 0 || (myPrior - 2) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                    keepGoing = false;
                } else {
                    if (!myPrior.equals(partArray.getJSONObject(checkPrev).getInteger("wn0prior"))) {
                        if ((myPrior - 1) == partArray.getJSONObject(checkPrev).getInteger("wn0prior")) {
                            JSONObject idAndIndex = new JSONObject();
                            idAndIndex.put("id_O", finO);
                            idAndIndex.put("index", fin_Ind);
                            // Here, I put the checking IdIndex into my own list of prtPrev
                            unitAction.getPrtPrev().add(idAndIndex);
                        }
                    }
                }
                checkPrev--;
            } while (keepGoing);

        } catch (Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }

                unitAction.setSumPrev(unitAction.getPrtPrev().size());
//                keepGoing = true;
//
//                do {
//                    //
//                        if (checkNext == partArray.size()) {
//                        keepGoing = false;
//                    } else if ((myPrior + 2) == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
//                        keepGoing = false;
//                    } else if (myPrior == partArray.getJSONObject(checkNext).getInteger("wn0prior")) {
//                        System.out.println("Same Prior, do nothing here");
//                    }
//                    checkNext++;
//                } while (keepGoing);

                //if firstLayer merge, then DO NOT setup upPrnts, because there should be No upPrnts
                //but then, set all subparts' upPrnts to include my qty

                if (dgType.equals(1)) {
                    for (int i = 0; i < unitAction.getSubParts().size(); i++)
                    {
                        String subId_O = unitAction.getSubParts().getJSONObject(i).getString("id_O");
                        Integer subIndex = unitAction.getSubParts().getJSONObject(i).getInteger("index");
                        OrderAction subAction = objActionCollection.get(subId_O).get(subIndex);
                        JSONObject upPrntsData = new JSONObject();
                        upPrntsData.put("id_O", upperOItem.getId_O());
                        upPrntsData.put("index", upperOItem.getIndex());
                        upPrntsData.put("wrdN", upperOItem.getWrdN());
                        upPrntsData.put("wn2qtyneed",  upperOItem.getWn2qtyneed() * unitAction.getSubParts().getJSONObject(i).getDouble("qtyEach"));
                        upPrntsData.put("Subi",  i);

                        subAction.getUpPrnts().add(upPrntsData);
                    }
                } else {
                    System.out.println("DGType");
                    JSONObject upPrntsData = new JSONObject();
                    upPrntsData.put("id_O", upperOItem.getId_O());
                    upPrntsData.put("index", upperOItem.getIndex());
                    upPrntsData.put("wn2qtyneed", upperOItem.getWn2qtyneed() * partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"));
                    upPrntsData.put("wrdN", upperOItem.getWrdN());



                    unitAction.getUpPrnts().add(upPrntsData);
                    System.out.println(unitAction);
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
                subPartData.put("qtyEach",partArray.getJSONObject(partIndex).getDouble("wn4qtyneed") );


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
        System.out.println("DGType");

                // Loop into all subParts to make sure all qty is added correctly
                this.dgMergeQtySet(finO, qtyNeed, partArray.getJSONObject(partIndex).getDouble("wn4qtyneed"),
                        finO,fin_Ind,dgType, objActionCollection, objOItemCollection,oDates,mergeJ);

                if (dgType.equals(1))
                {
                    System.out.println("partIndex@1"+ partIndex);
                    // fix the action of the next+ item using the unitAction of the 1st one
                    unitAction.setIndex(partIndex);
                    for (int i = 0; i< unitAction.getSubParts().size(); i++)
                    {
                        unitAction.getSubParts().getJSONObject(i).put("upIndex", partIndex);
//                        System.out.println("now all sub shit");
//                        System.out.println(qtyNeed);
//
//                        OrderAction subAction = objActionCollection.get(unitAction.getSubParts().getJSONObject(i).getString("id_O")).
//                                get(unitAction.getSubParts().getJSONObject(i).getInteger("index"));
//                        subAction.getUpPrnts().getJSONObject(partIndex).put("afterdgmwn2qtyneed", qtyNeed);
//                        subAction.getUpPrnts().getJSONObject(partIndex).put("wn2qtyneed", qtyNeed);

                    }


                    objActionCollection.get(finO).add(unitAction);
                    objOItemCollection.get(finO).add(unitOItem);
                }
    }

    private void dgMergeQtySet(String id_OP, Double qtyNeed, Double qtySubNeed, String finO, Integer fin_Ind, Integer dgType,
                           Map<String, List<OrderAction>> objActionCollection,Map<String, List<OrderOItem>> objOItemCollection
            ,List<OrderODate> oDates,JSONObject mergeJ)
    {
        OrderOItem unitOItem = objOItemCollection.get(finO).get(fin_Ind);
        OrderAction unitAction = objActionCollection.get(finO).get(fin_Ind);

        if (qtySubNeed == null)
        {
            qtySubNeed = 1.0;
        }



        Double currentQty = unitOItem.getWn2qtyneed();
//
//        System.out.println("current"+currentQty);
//        System.out.println("qtyNeed"+qtyNeed);
//        System.out.println("qtySub"+qtySubNeed);
        System.out.println("I am checking here");
        System.out.println(currentQty);
        System.out.println(unitOItem.getWrdN().getString("cn"));
        System.out.println(qtyNeed);

        unitOItem.setWn2qtyneed(currentQty + qtyNeed * qtySubNeed);

        System.out.println(unitOItem.getWn2qtyneed());



        // summ qtyall and qtyneed if I am not the main Parent
        if (!id_OP.equals(finO)) {


            Integer ind = mergeJ.getInteger(unitOItem.getId_P());
            OrderODate oDa = oDates.get(ind);
            oDa.setWn2qtyneed(oDa.getWn2qtyneed()+currentQty);
            oDates.set(ind,oDa);

            System.out.println(id_OP+finO);
            if (!dgType.equals(1))
            {
//                System.out.println("I am checking 2");
//                System.out.println(currentQty);
//                System.out.println(unitOItem.getWrdN().getString("cn"));
//                System.out.println(unitOItem.getWn2qtyneed());
//
                JSONObject upPrntsData = unitAction.getUpPrnts().getJSONObject(0); //******
                     upPrntsData.put("CALcul", unitOItem.getWn2qtyneed());
                upPrntsData.put("wn2qtyneed", unitOItem.getWn2qtyneed());
//
//                upPrntsData.put("CALculatetyneed3", qtySubNeed);
//                upPrntsData.put("CALculatetyneed4", qtyNeed);
//
//                System.out.println("*** upPrntsData" + upPrntsData);
            } else {
                dgType = 2;
            }

        }

        for (int i = 0; i < unitAction.getSubParts().size(); i++)
        {
            //Here I dig into each subParts and sum qtyall / qtyneed
            JSONObject unit = unitAction.getSubParts().getJSONObject(i);

            this.dgMergeQtySet(id_OP,qtyNeed * qtySubNeed, unit.getDouble("qtyEach"),
                  unit.getString("id_O") ,unit.getInteger("index"), dgType, objActionCollection,objOItemCollection
            ,oDates,mergeJ);
        }
    }


    @Override
    public ApiResponse prodPart(String id_P) {
            JSONObject jsonPart = new JSONObject();
            JSONObject recurCheckList = new JSONObject();
            recurCheckList.put(id_P, "");
            Prod prod = (Prod) dbUtils.getMongoOneFields(id_P, Arrays.asList("info", "part"), Prod.class);
            JSONArray arrayObjItem = prod.getPart().getJSONArray("objItem");

            JSONObject stat = new JSONObject();
            stat.put("count", 1);
            stat.put("allCount", prod.getPart().getInteger("wn0Count") == null ?
                    300 : prod.getPart().getInteger("wn0Count"));

            System.out.println("stat" + stat);
            System.out.println("recur"+recurCheckList);
            System.out.println(jsonPart);

            jsonPart.put("name", prod.getInfo().getWrdN().getString("cn"));
            jsonPart.putAll(JSON.parseObject(JSON.toJSONString(prod.getInfo())));

            jsonPart.put("children", recursionProdPart(arrayObjItem, recurCheckList, stat));

            return retResult.ok(CodeEnum.OK.getCode(), jsonPart);

    }

    public Object recursionProdPart(JSONArray arrayObjItem, JSONObject recurCheckList, JSONObject stat) {

            JSONArray arrayChildren = new JSONArray();
            HashSet<String> setId_P = new HashSet();

            if (stat.getInteger("count") > stat.getInteger("allCount")) {
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_RECURRED.getCode(), "产品需要重新检查");
            }
            stat.put("count", stat.getInteger("count") + 1);
            System.out.println("stat" + stat);


            // Here if it is already loop too much by over the checked wn0Count by our dgCheck
            // throw error, and ask for redg

            for (int i = 0; i < arrayObjItem.size(); i++) {
                String id_P = arrayObjItem.getJSONObject(i).getString("id_P");

                System.out.println(id_P);
                System.out.println(recurCheckList);
                if (recurCheckList.getString(id_P) != null) {
                    throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_NO_RECURSION_PART.getCode(), "");
                }
                if (arrayObjItem.getJSONObject(i).getInteger("bmdpt").equals(2)) {
                    recurCheckList.put(id_P, "");
                }
                setId_P.add(id_P);
//            setId_P = setId_P + "," + id_P;
            }
            List<Prod> prods = (List<Prod>) qt.getMDContentMany(setId_P, "part", Prod.class);
            JSONObject jsonProdPart = new JSONObject();
            for (Prod prod : prods) {
                System.out.println("prod in prods" + prod);
                jsonProdPart.put(prod.getId(), prod.getPart());
            }
            for (int i = 0; i < arrayObjItem.size(); i++) {
                JSONObject jsonObjItem = arrayObjItem.getJSONObject(i);
                JSONObject jsonChildren = new JSONObject();
                jsonChildren.put("name", jsonObjItem.getJSONObject("wrdN").getString("cn"));
                jsonChildren.putAll(jsonObjItem);
                //有下一层
                if (jsonProdPart.getJSONObject(jsonObjItem.getString("id_P")) != null) {
                    JSONArray arrayPartObjItem = jsonProdPart.getJSONObject(jsonObjItem.getString("id_P")).getJSONArray("objItem");
                    jsonChildren.put("children", recursionProdPart(arrayPartObjItem, recurCheckList, stat));
                }
                arrayChildren.add(jsonChildren);
            }

            return arrayChildren;

    }

    /**
     * 递归验证 - 注释完成
     * @param id_P 产品编号
     * @return java.lang.String  返回结果: 递归结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 9:03
     * DONE Check Kev
     */
    @Override
    public ApiResponse dgCheck(String id_P, String id_C){


            // 创建异常信息存储
            JSONArray isRecurred = new JSONArray();
            // 创建产品信息存储
            JSONArray isEmpty = new JSONArray();
            // 创建返回结果
            JSONObject result = new JSONObject();
            // 创建零件id集合
            JSONArray pidList = new JSONArray();
            JSONObject nextPart = new JSONObject();

            JSONObject stat = new JSONObject();
            stat.put("layer", 0);
            stat.put("count", 0);


            // ******调用验证方法******
            this.dgCheckUtil(pidList,id_P, id_C, nextPart,isRecurred,isEmpty, stat);

            System.out.println("layer "+ stat.get("layer"));
            System.out.println("count "+ stat.get("count"));


        // 添加到返回结果
            result.put("recurred",isRecurred);
            result.put("isNull",isEmpty);
            result.put("wn0Count", stat.getInteger("count"));

            // save the "allCount" into id.P .part.wn0Count if this is a correct product
            if (isRecurred.size() == 0 && isEmpty.size() == 0) {
                JSONObject probKey = new JSONObject();
                probKey.put("part.wn0Count", stat.getInteger("count"));
//                coupaUtil.updateProdByListKeyVal(id_P, probKey);
                qt.setMDContent(id_P,probKey,Prod.class);
            }

            // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), result);
    }

        /**
         * 递归验证工具 - 注释完成
         * @param pidList	产品编号集合
         * @param id_P	产品编号
         * @param objectMap	零件信息
         * @param isRecurred	产品状态存储map
         * @param isEmpty	异常信息存储集合
         * @return void  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2020/11/16 9:25
         * DONE Check Kev

         */
        private void dgCheckUtil(JSONArray pidList,String id_P,String id_C, JSONObject objectMap
                ,JSONArray isRecurred,JSONArray isEmpty, JSONObject stat){

            // 根据父编号获取父产品信息
//            Prod thisItem = coupaUtil.getProdByListKey(id_P, Arrays.asList("part","info"));
            Prod thisItem = qt.getMDContent(id_P, qt.strList("info", "part"), Prod.class);
            System.out.println("thiItem"+thisItem);

            // 层级加一
            stat.put("layer", stat.getInteger("layer") + 1 );

            boolean isConflict = false;
            JSONArray checkList = new JSONArray();

            // 判断父产品不为空，部件父产品零件不为空
            if (null != thisItem) {

                for (int i = 0; i < pidList.size(); i++) {
                    System.out.println("冲突Check"+id_P);
                    // 判断编号与当前的有冲突
                    if (pidList.getString(i).equals(id_P)) {
                        // 创建零件信息
                        JSONObject conflictProd = new JSONObject();
                        // 添加零件信息
                        conflictProd.put("id_P",id_P);
                        conflictProd.put("layer",(stat.getInteger("layer")+1));
                        conflictProd.put("index",i);
                        // 添加到结果存储
                        isRecurred.add(conflictProd);
                        // 设置为有冲突
                        isConflict = true;
                        // 结束
                        break;
                    }
                }

                if (!isConflict)
                {
                    checkList = (JSONArray) pidList.clone();
                    checkList.add(id_P);
                }

                // 获取prod的part信息
                if(!isConflict &&
                        null != thisItem.getPart() &&
                        thisItem.getInfo().getId_C().equals(id_C) &&
                        null != thisItem.getPart().get("objItem")) {
                    JSONArray nextItem = thisItem.getPart().getJSONArray("objItem");
                    // 遍历零件信息1
                    for (int j = 0; j < nextItem.size(); j++) {
                        // 判断零件不为空并且零件编号不为空
                        stat.put("count", stat.getInteger("count") + 1 );
                        System.out.println("count "+ stat.getInteger("count"));
                        if (null != nextItem.get(j) && null != nextItem.getJSONObject(j).get("id_P")) {

                                // 继续调用验证方法
                                System.out.println("判断无冲突"+isConflict);
                                if (nextItem.getJSONObject(j).getDouble("wn4qtyneed") == null ||
                                        nextItem.getJSONObject(j).getDouble("wn2qty") == null ||
                                        nextItem.getJSONObject(j).getDouble("wn2port") == null)
                                {
                                    objectMap.put("errDesc", "数量为空！");
                                    isEmpty.add(objectMap);
                                } else {
                                    this.dgCheckUtil(checkList, nextItem.getJSONObject(j).getString("id_P"), id_C, nextItem.getJSONObject(j)
                                            , isRecurred, isEmpty, stat);
                                }
                        } else {
                            if (null != objectMap) {
                                objectMap.put("errDesc", "产品不存在！");
                                isEmpty.add(objectMap);
                            }
                        }
                    }
                }
            } else
            {
                    objectMap.put("errDesc","产品不存在！");
                    isEmpty.add(objectMap);
            }
        }

    /**
     * 根据请求参数，获取更新后的订单oitem
     * @param id_P 产品编号
     * @param id_C 公司编号
     * @return java.lang.String  返回结果: 日志结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/8/6 9:08
     * KEV Checked
     */
    @Override
    public ApiResponse dgUpdatePartInfo(String id_P,String id_C) {
//        Prod prod = coupaUtil.getProdByListKey(
//                id_P,Arrays.asList("info","part"));

        Prod prod = qt.getMDContent(id_P, qt.strList("info","part"), Prod.class);
        if (null == prod) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NOT_EXIST.getCode(), "产品不存在");
        }

        JSONObject part = prod.getPart();
        if (null == part) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NO_PART.getCode(), "产品无零件");
        }
        
        // 获取prod的part信息
        JSONArray partItem = prod.getPart().getJSONArray("objItem");
        if (null == partItem || partItem.size() == 0) {
            // 返回错误信息
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NO_PART.getCode(), "产品无零件");
        }
        JSONObject stat = new JSONObject();
        stat.put("count", 1);
        stat.put("allCount", prod.getPart().getInteger("wn0Count") == null ?
                300 : prod.getPart().getInteger("wn0Count"));

        this.updatePartInfo(id_C,partItem,id_P, stat);

        if (stat.getInteger("count") < stat.getInteger("allCount"))
        {
            System.out.println("it's less than that"+ stat.getInteger("count"));
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_NO_RECURSION_PART.getCode(), "产品需要更新");
        }

        return retResult.ok(CodeEnum.OK.getCode(), partItem);
    }

    /**
     * 根据id_C，判断prod是什么类型
     * @param id_C	公司编号
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2020/9/17 16:52
     * DONE Check Kev
     */
    private void updatePartInfo(String id_C,JSONArray partItem,String id_P, JSONObject stat){
        for (int item = 0; item < partItem.size(); item++)
        {
                JSONObject thisItem = partItem.getJSONObject(item);
                if (stat.getInteger("count") > stat.getInteger("allCount")) {

                    throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_NO_RECURSION_PART.getCode(), "产品需要更新");
                }
                stat.put("count", stat.getInteger("count") + 1);


//                JSONArray partDataES = dbUtils.getEsKey("id_P", thisItem.getString("id_P"), "id_CB", id_C, "lBProd");

                JSONArray partDataES = qt.getES("lBProd", qt.setESFilt("id_P", thisItem.getString("id_P"), "id_CB", id_C));

                System.out.println(partDataES);
//            Prod thisProd = coupaUtil.getProdByListKey(
//                    thisItem.getString("id_P"), Arrays.asList("part","info"));
                Prod thisProd = qt.getMDContent(thisItem.getString("id_P"), qt.strList("info", "part"), Prod.class);

                int bmdptValue =0;

                if (null == thisProd) {
                    bmdptValue = 1;
                } else {
                    // 获取id的信息
                    ProdInfo prodInfo = thisProd.getInfo();
                    System.out.println("prodInfo");
                    System.out.println(prodInfo);
                    JSONObject part = thisProd.getPart();
//                Comp compById = coupaUtil.getCompByListKey(id_C, Arrays.asList("info"));
                    Comp compById = qt.getMDContent(id_C, "info", Comp.class);

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
                                this.updatePartInfo(id_C, nextPart, thisItem.get("id_P").toString(), stat);
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
                System.out.println("thisItem"+JSON.toJSONString(partItem.getJSONObject(item)));

        }
//        JSONObject partData = new JSONObject();
//        partData.put("part.objItem",partItem);
        qt.setMDContent(id_P, qt.setJson("part.objItem", partItem),Prod.class);
//        coupaUtil.updateProdByListKeyVal(id_P,partData);
    }

        /**
         * 检查part是否为空 - 注释完成
         * @param id_P 产品编号
         * @return java.lang.String  返回结果: 结果
         * @author tang
         * @ver 1.0.0
         * @date 2021/1/19 10:05
         * @Checked kev 2021/11/15
         * DONE Check Kev

         */
        @Override
        public ApiResponse getPartIsNull(String id_P) {

            // 根据id获取产品信息
//            Prod prod = coupaUtil.getProdByListKey(
//                    id_P,Arrays.asList("info","part"));
            Prod prod = qt.getMDContent(id_P, Arrays.asList("_id","part","info"), Prod.class);

            // 判断产品为空
            if (null == prod) {
                // 返回错误信息
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PROD_NOT_EXIST.getCode(), "产品不存在");
            }
            // 获取产品零件信息
            JSONObject part = prod.getPart();
            // 判断产品零件信息为空
            if (null == part) {
                // 返回错误信息
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PART_IS_NULL.getCode(), "0");
            }
            // 获取prod的part信息
            JSONArray partItem = prod.getPart().getJSONArray("objItem");
            // 判断产品零件信息为空
            if (null == partItem || partItem.size() == 0) {
                // 返回错误信息
                throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ERR_PART_IS_NULL.getCode(), null);
            }
            // 抛出操作成功异常
            return retResult.ok(CodeEnum.OK.getCode(), "1");
        }

    /**
     * 删除指定list的redis键 - 注释完成
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2021/6/16 14:32
     */
    @Override
    public ApiResponse dgRemove(String id_O,String id_C,String id_U) {

//        Order orderParent = coupaUtil.getOrderByListKey(id_O, Arrays.asList());
        Order orderParent = qt.getMDContent(id_O, Arrays.asList("action", "oItem", "casItemx", "view"),Order.class);

        if (orderParent == null)
        {
            throw new ErrorResponseException(HttpStatus.OK, ActionEnum.ORDER_NOT_EXIST.getCode(), "订单不存在");
        }
        JSONArray casList;

        if (orderParent.getCasItemx().getJSONObject(id_C) == null)
        {
            casList = orderParent.getCasItemx().getJSONArray("objOrder");
        } else {
            casList = orderParent.getCasItemx().getJSONObject(id_C).getJSONArray("objOrder");
        }

        JSONObject myOrder = new JSONObject();
        myOrder.put("id_O", id_O);
        casList.add(myOrder);

        // loop casItemx orders
        for (int i = 0; i < casList.size(); i++)
        {
            // check if orders are final, if need request Cancel, send request
            // if ok, open order

            String subOrderId = casList.getJSONObject(i).getString("id_O");

                try {
//                    // 创建es删除请求
//                    DeleteByQueryRequest requestAct = new DeleteByQueryRequest("action");
//                    // 设置删除信息
//                    requestAct.setQuery(new TermQueryBuilder("data.id_O.keyword", subOrderId));
//                    // 请求方法
//                    client.deleteByQuery(requestAct, RequestOptions.DEFAULT);
                    qt.delES("action", qt.setESFilt("data.id_O.keyword", subOrderId));
                    qt.delES("assetflow", qt.setESFilt("data.id_O.keyword", subOrderId));

                } catch (Exception e) {
                    System.out.println("删除es出现错误:" + e.getMessage());
                }
                // delete that order        // 删除订单
            if (!subOrderId.equals(id_O)) {
                // 创建es删除请求
                try {
//                    DeleteByQueryRequest requestLB = new DeleteByQueryRequest("lsborder");
//                // 设置删除信息
//                    requestLB.setQuery(new TermQueryBuilder("id_O", subOrderId));
//                    // 请求方法
//                    client.deleteByQuery(requestLB, RequestOptions.DEFAULT);
                    qt.delES("lsborder", qt.setESFilt("id_O", "exact",subOrderId));

                } catch (Exception e) {
                    System.out.println("删除es出现错误:" + e.getMessage());
                }

                qt.delMD(subOrderId, Order.class);
            }
        }

        JSONObject orderData = new JSONObject();
        orderData.put("casItemx", null);
        orderData.put("action", null);
        JSONArray view = orderParent.getView();

        JSONObject oItem = orderParent.getOItem();

        oItem.getJSONArray("objCard").remove("action");
        orderData.put("oItem", oItem);

        view.remove("casItemx");
        view.remove("action");
        orderData.put("view", view);

//        coupaUtil.updateOrderByListKeyVal(id_O,orderData);
        qt.setMDContent(id_O,orderData, Order.class);

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "删除成功！！!!!");
    }

    /**
     * 时间处理方法
     * @param id_O	订单编号
     * @param id_U	用户编号
     * @param id_C	公司编号
     * @param teStart	开始时间
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/5/19 10:30
     */
    @Override
    public ApiResponse timeHandle(String id_O, String id_U, String id_C, Long teStart,Integer wn0TPrior) {
        System.out.println("初始-teStart:"+teStart+" , id_U:"+id_U);

        timeZjService.getAtFirst(id_O,teStart,id_C,wn0TPrior);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "时间处理成功!");
    }

    @Override
    public ApiResponse removeTime(String id_O, String id_C) {
        timeZjService.removeTime(id_O,id_C);
        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), "时间删除处理成功!");
    }

}
