//package com.cresign.purchase.service.impl;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.purchase.enumeration.DetailsEnum;
//import com.cresign.purchase.service.OItemService;
//import com.cresign.tools.advice.RetResult;
//import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.dbTools.DateUtils;
//import com.cresign.tools.dbTools.DbUtils;
//import com.cresign.tools.dbTools.DoubleUtils;
//import com.cresign.tools.dbTools.Qt;
//import com.cresign.tools.enumeration.CodeEnum;
//import com.cresign.tools.enumeration.DateEnum;
//import com.cresign.tools.exception.ErrorResponseException;
//import com.cresign.tools.pojo.es.lBProd;
//import com.cresign.tools.pojo.po.*;
//import com.cresign.tools.pojo.po.orderCard.OrderInfo;
//import com.cresign.tools.pojo.po.orderCard.OrderOItem;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.*;
//
//@Service
//public class OItemServiceImpl implements OItemService {
//
//    @Autowired
//    private DbUtils db;
//
//    @Autowired
//    private RetResult retResult;
//
//    @Autowired
//    private DoubleUtils dbl;
//
//    @Autowired
//    private Qt qt;
//
//    /**
//     * 0. checkOrder @ both
//     * 1. check id_P same
//     * 2. add oItem's qtyneed
//     * 3. add oStock's qtymade + qtynow
//     * 4. put actions' upSubNextPrev into merge's
//     * 5. delete mainOItem
//     * 6. summOrder
//     * @param id_O Main Order
//     * @param index main index
//     * @param mergeId_O Slave Order
//     * @param mergeIndex slave index
//     * @return ok or db error then use Transactional
//     */
//    @Override
//    @Transactional
//    public ApiResponse mergeOrder(String id_O, Integer index, String mergeId_O, Integer mergeIndex) {
//
//        Order mainOrder = qt.getMDContent(id_O, Arrays.asList("info", "oItem", "oStock", "action", "view"), Order.class);
//        Order slaveOrder = qt.getMDContent(mergeId_O, Arrays.asList("info", "oItem", "oStock", "action", "view"), Order.class);
//        //id_C和id_CB都要相同
//        if (!mainOrder.getInfo().getId_C().equals(slaveOrder.getInfo().getId_C()) || !mainOrder.getInfo().getId_CB().equals(slaveOrder.getInfo().getId_CB())) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MERGEORDER_COMP_DIFFERENT.getCode(), id_O);
//        }
//
//        // if target mainOrder's support cards not exists, add it to them, db.checkCard will init
//        for (int i = 0; i < mainOrder.getOItem().getJSONArray("objCard").size(); i++)
//        {
//            String cardName = mainOrder.getOItem().getJSONArray("objCard").getString(i);
//            if (!slaveOrder.getOItem().getJSONArray("objCard").contains(cardName))
//            {
//                slaveOrder.getOItem().getJSONArray("objCard").add(cardName);
//            }
//        }
//
//        //获取订单需要修改的卡
//        db.checkCard(mainOrder);
//        db.checkCard(slaveOrder);
//
//        //id_P要相同
//        if (!mainOrder.getOItem().getJSONArray("objItem").getJSONObject(index).getString("id_P").equals(slaveOrder.getOItem().getJSONArray("objItem").getJSONObject(mergeIndex).getString("id_P"))) {
//            // if not the same, it is just, move one id_O/index -> append to mId_O mindex;
//            this.moveOItem(mergeId_O, mergeIndex , id_O, index);
//        }
//
//        //oItem
////        JSONObject cloneOItem = qt.cloneObj(mainOrder.getOItem().getJSONArray("objItem").getJSONObject(index));
//        JSONObject mainOItem = mainOrder.getOItem().getJSONArray("objItem").getJSONObject(index);
//        JSONObject mergeOItem = slaveOrder.getOItem().getJSONArray("objItem").getJSONObject(mergeIndex);
//
//        Double finalTotal = dbl.add(
//                dbl.multiply(mergeOItem.getDouble("wn2qtyneed"), mergeOItem.getDouble("wn4price")),
//                dbl.multiply(mainOItem.getDouble("wn2qtyneed"), mainOItem.getDouble("wn4price")));
//        Double finalPrice = dbl.divide(finalTotal, mainOItem.getDouble("wn2qtyneed"));
//
//        mainOItem.put("wn2qtyneed", dbl.add(mainOItem.getDouble("wn2qtyneed"), mergeOItem.getDouble("wn2qtyneed")));
//
//
//        mainOItem.put("wn4price", finalPrice);
//
//        if (mainOrder.getOStock() != null) {
//            JSONObject mainOStock = mainOrder.getOStock().getJSONArray("objData").getJSONObject(index);
//            JSONObject mergeOStock = slaveOrder.getOStock().getJSONArray("objData").getJSONObject(mergeIndex);
//            mainOStock.put("wn2qtymade", dbl.add(mainOStock.getDouble("wn2qtymade"), mergeOStock.getDouble("wn2qtymade")));
//            mainOStock.put("wm2qtynow", dbl.add(mainOStock.getDouble("wn2qtynow"), mergeOStock.getDouble("wn2qtynow")));
//
//        }
//
//
//        //action
//        JSONArray arrayMainAction = qt.cloneArr(mainOrder.getAction().getJSONArray("objAction"));
//        JSONArray arrayMergeAction = qt.cloneArr(slaveOrder.getAction().getJSONArray("objAction"));
//        List<JSONObject> bulkActionUpdate = new ArrayList<>();
//
//        if (arrayMainAction != null && arrayMergeAction != null) {
//            JSONObject allOrders = new JSONObject();
//
//            JSONArray arrayMainUpPrnt = arrayMainAction.getJSONObject(index).getJSONArray("upPrnts");
//            JSONArray arrayMainSubPart = arrayMainAction.getJSONObject(index).getJSONArray("subParts");
//
//            this.getOrdersFour(allOrders, arrayMainAction, index, index + 1);
//
//            JSONArray arrayMergeUpPrnt = arrayMergeAction.getJSONObject(mergeIndex).getJSONArray("upPrnts");
//            JSONArray arrayMergeSubPart = arrayMergeAction.getJSONObject(mergeIndex).getJSONArray("subParts");
//            JSONArray arrayMergePrtPrev = arrayMergeAction.getJSONObject(mergeIndex).getJSONArray("prtPrev");
//            JSONArray arrayMergePrtNext = arrayMergeAction.getJSONObject(mergeIndex).getJSONArray("prtNext");
//            this.getOrdersFour(allOrders, arrayMergeAction, mergeIndex, mergeIndex + 1);
//
//            allOrders.put(id_O, mainOrder);
//            allOrders.put(mergeId_O, slaveOrder);
//
//                //修改主订单的父订单数组
//                for (int i = 0; i < arrayMergeUpPrnt.size(); i++) {
//                    JSONObject jsonMergeUpPrnt = arrayMergeUpPrnt.getJSONObject(i);
//
//                    Integer upPrntIndex = getIndex(arrayMainUpPrnt, jsonMergeUpPrnt.getString("id_O"), jsonMergeUpPrnt.getInteger("index"));
//                    //没有相同关联订单，增加关联对象
//                    if (upPrntIndex == -1) {
//                        arrayMainUpPrnt.add(jsonMergeUpPrnt);
//                    }
//                    //关联同一个订单，增加数量
//                    else {
//                        double upPrntWn2qtyneed = dbl.add(arrayMainUpPrnt.getJSONObject(upPrntIndex).getDouble("wn2qtyneed"),
//                                jsonMergeUpPrnt.getDouble("wn2qtyneed"));
//                        arrayMainUpPrnt.getJSONObject(upPrntIndex).put("wn2qtyneed", upPrntWn2qtyneed);
//                        jsonMergeUpPrnt.put("need", upPrntWn2qtyneed);
//                    }
//                }
//
//
//                //修改merge订单的父订单的子订单数组
//
//                bulkActionUpdate = updateRelation(id_O, index, mergeId_O, mergeIndex, arrayMergeUpPrnt, "subParts", "merge", bulkActionUpdate, allOrders);
//
//                //修改主订单的子订单数组
//                    for (int j = 0; j <  arrayMainSubPart.size(); j++) {
//                        JSONObject jsonMainSubPart = arrayMainSubPart.getJSONObject(j);
//                        JSONObject jsonMergeSubPart = arrayMergeSubPart.getJSONObject(j);
//                       //关联同一种产品 - 肯定的。。。。
//                        if (jsonMergeSubPart.getString("id_P").equals(jsonMainSubPart.getString("id_P"))) {
//                            qt.errPrint("why here merging", null, jsonMainSubPart, jsonMergeSubPart );
//                            this.mergeOrder(jsonMainSubPart.getString("id_O"), jsonMainSubPart.getInteger("index"),
//                                    jsonMergeSubPart.getString("id_O"), jsonMergeSubPart.getInteger("index"));
//                        }
//                    }
//
//                bulkActionUpdate = updateRelation(id_O, index, mergeId_O, mergeIndex, arrayMergePrtPrev, "prtNext", "", bulkActionUpdate, allOrders);
//
//                bulkActionUpdate = updateRelation(id_O, index, mergeId_O, mergeIndex, arrayMergePrtNext, "prtPrev", "", bulkActionUpdate, allOrders);
//
//            if (bulkActionUpdate.size() > 0) {
//                qt.setMDContentMany(bulkActionUpdate, Order.class);
//            }
//
//            Order ordUp = qt.getMDContent(id_O, "action.objAction", Order.class);
//            mainOrder.getAction().put("objAction", ordUp.getAction().getJSONArray("objAction"));
//            mainOrder.getAction().getJSONArray("objAction").getJSONObject(index).put("upPrnts", arrayMainUpPrnt);
//            mainOrder.getAction().getJSONArray("objAction").getJSONObject(index).put("subParts", arrayMainSubPart);
//
//
//        }
//
//        System.out.println("bulkActionUpdate=" + bulkActionUpdate);
//
//            //修改lsborder
//        JSONObject listCol = new JSONObject();
//
//        db.summOrder(mainOrder, listCol);
//
//        qt.saveMD(mainOrder);
//
//        qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
//
//        qt.errPrint("deletingOITEM", null, mergeId_O, mergeIndex);
//        this.delOItem(mergeId_O, mergeIndex);
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
////    @Override
//    @Transactional
//    // id_O is target, fromId_O is original
//    public ApiResponse moveOItem(String targetId_O, Integer targetIndex, String fromId_O, Integer fromIndex) {
//
//        Order targetOrder = qt.getMDContent(targetId_O,Arrays.asList("info", "oItem", "oStock", "action", "view"), Order.class);
//        Order fromOrder = qt.getMDContent(fromId_O, Arrays.asList("info", "oItem", "oStock", "action", "view"), Order.class);
//
//        JSONObject fromOItem = fromOrder.getOItem().getJSONArray("objItem").getJSONObject(fromIndex);
//
//        //id_C和id_CB都要相同
//        if (!targetOrder.getInfo().getId_C().equals(fromOrder.getInfo().getId_C()) || !targetOrder.getInfo().getId_CB().equals(fromOrder.getInfo().getId_CB())) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MERGEORDER_COMP_DIFFERENT.getCode(), "in moveOItem");
//        }
//        //获取订单需要修改的卡
//        for (int i = 0; i < fromOrder.getOItem().getJSONArray("objCard").size(); i++)
//        {
//            String cardName = fromOrder.getOItem().getJSONArray("objCard").getString(i);
//            // if target order's support cards not exists, add it to them, db.checkCard will init
//            if (!targetOrder.getOItem().getJSONArray("objCard").contains(cardName))
//            {
//                targetOrder.getOItem().getJSONArray("objCard").add(cardName);
//            }
//        }
//
//        db.checkCard(fromOrder);
//        db.checkCard(targetOrder);
//
//        List<JSONObject> listBulk = new ArrayList<>();
//
//        //在末尾添加oItem
//        if (targetIndex == -1) {
//            targetIndex = targetOrder.getOItem().getJSONArray("objItem").size();
//        }
//
//        //oStock
//        if (fromOrder.getOItem().getJSONArray("objCard").contains("oStock")) {
//            JSONObject fromStock = qt.cloneObj(fromOrder.getOStock().getJSONArray("objData").getJSONObject(fromIndex));
//
//            targetOrder.getOStock().getJSONArray("objData").add(targetIndex, fromStock);
//
//        }
//
////        JSONObject fromActionItem = qt.cloneObj(fromOrder.getAction().getJSONArray("objAction").getJSONObject(fromIndex));
//
//        if (fromOrder.getOItem().getJSONArray("objCard").contains("action")) {
//            //action
//            JSONArray arrayAction = qt.cloneArr(targetOrder.getAction().getJSONArray("objAction"));
//            JSONArray arrayFromAction = qt.cloneArr(fromOrder.getAction().getJSONArray("objAction"));
//
//
//            JSONObject fromActionItem = arrayFromAction.getJSONObject(fromIndex);
////
//
//
//            JSONObject prtOrders = new JSONObject();
//            prtOrders.put(targetId_O, targetOrder);
//            prtOrders.put(fromId_O, fromOrder);
//
//            fromActionItem.put("id_O", targetId_O);
//            fromActionItem.put("index", targetIndex);
//
//
//            // if my original oItem is a nonP unit (id_P==''), I move by connecting to the new Order's prev/next, disconnect from the old
//            if (fromOItem.getString("id_P").equals(""))
//            {
//                fromActionItem.put("upPrnt", new JSONArray());
//                fromActionItem.put("subParts", new JSONArray());
//                fromActionItem.put("prtPrev", new JSONArray());
//                fromActionItem.put("prtNext", new JSONArray());
//                fromActionItem.put("sumChild", 0);
//                fromActionItem.put("sumPrev", 0);
//
//                //my prev = +1's prev ; my next = -1's next ; set my +1's up to myIndex
//
//                if (targetIndex == 0)
//                {
//                    fromActionItem.getJSONArray("prtNext").add(qt.setJson("id_O", targetId_O, "index", 1));
//                } else if (targetIndex != targetOrder.getOItem().getJSONArray("objItem").size())
//                {
//                    fromActionItem.put("prtPrev", arrayAction.getJSONObject(targetIndex).getJSONArray("prtPrev"));
//                    fromActionItem.put("sumPrev", arrayAction.getJSONObject(targetIndex).getJSONArray("prtPrev").size());
//                    fromActionItem.put("prtNext", arrayAction.getJSONObject(targetIndex - 1).getJSONArray("prtNext"));
//                    for (int i = 0; i < fromActionItem.getJSONArray("prtNext").size(); i++)
//                    {
//                        fromActionItem.getJSONArray("prtNext").getJSONObject(i).put("index",
//                                fromActionItem.getJSONArray("prtNext").getJSONObject(i).getInteger("index") + 1);
//                    }
//                } else {
//                    fromActionItem.getJSONArray("prtPrev").add(qt.setJson("id_O", targetId_O, "index", targetIndex));
//                    fromActionItem.put("sumPrev", 1);
//                }
//
//
//
//
//                JSONObject subInfo = qt.setJson("id_O", targetId_O, "index", targetIndex, "id_P", "", "prior", 0, "qtyEach", 1, "upIndex", 0, "wrdN", fromOItem.getJSONObject("wrdN"));
////                JSONObject upInfo = qt.setJson("id_O", targetId_O, "index", targetIndex, "qtyNeed", 1, "wrdN", fromOItem.getJSONObject("wrdN"));
//
//                if (targetIndex != 0 && targetOrder.getOItem().getJSONArray("objItem").getJSONObject(targetIndex - 1).getInteger("objSub") > 0)
//                {
//                    //add subPart (myself 1 more time) + my upPrnts my next's upPrnts
//                    arrayAction.getJSONObject(targetIndex - 1).getJSONArray("subParts").add(subInfo);
//                    fromActionItem.put("upPrnts", arrayAction.getJSONObject(targetIndex + 1).getJSONArray("upPrnts"));
//
//                } else if (targetIndex != 0 && arrayAction.getJSONObject(targetIndex - 1).getJSONArray("upPrnts").size() > 0)
//                {
//                    //add one more subPart
//                    Integer prntIndex = arrayAction.getJSONObject(targetIndex - 1).getJSONArray("upPrnts").getJSONObject(0).getInteger("index");
//                    arrayAction.getJSONObject(prntIndex).getJSONArray("subParts").add(subInfo);
//
//                    // copy that upPrnts
//                    fromActionItem.put("upPrnts", arrayAction.getJSONObject(targetIndex - 1).getJSONArray("upPrnts"));
//
//                }
//
//            } else {
//
//                // this is cascade style linking, need to upRelation
//                //把up/sub/prev/next 的Order 全部getMD 拿回来修改action.prtXXXX
//                this.getOrdersFour(prtOrders, arrayFromAction, fromIndex, fromIndex + 1);
//
//                //修改from订单的父订单的子订单数组
//                listBulk = updateRelation(targetId_O, targetIndex, fromId_O, fromIndex, fromActionItem.getJSONArray("upPrnts"), "subParts", "", listBulk, prtOrders);
//
//                //修改from订单的子订单的父订单数组
//                listBulk = updateRelation(targetId_O, targetIndex, fromId_O, fromIndex, fromActionItem.getJSONArray("subParts"), "upPrnts", "", listBulk, prtOrders);
//
//                //修改from订单的前订单的后订单数组
//                listBulk = updateRelation(targetId_O, targetIndex, fromId_O, fromIndex, fromActionItem.getJSONArray("prtPrev"), "prtNext", "", listBulk, prtOrders);
//
//                //修改from订单的后订单的前订单数组
//                listBulk = updateRelation(targetId_O, targetIndex, fromId_O, fromIndex, fromActionItem.getJSONArray("prtNext"), "prtPrev", "", listBulk, prtOrders);
//
//
//            }
//            listBulk = updateAllTheRest(targetId_O, arrayAction, targetIndex, listBulk, fromOItem.getString("id_P").equals("") ? 4 : 3);
//
//            if (listBulk.size() > 0) {
//                qt.setMDContentMany(listBulk, Order.class);
//                Order ordUp = qt.getMDContent(targetId_O, "action.objAction", Order.class);
//                targetOrder.getAction().put("objAction", ordUp.getAction().getJSONArray("objAction"));
//            }
//            targetOrder.getAction().getJSONArray("objAction").add(targetIndex, fromActionItem);
//        }
//
//        //oItem
//        targetOrder.getOItem().getJSONArray("objItem").add(targetIndex, fromOItem);
//        ///// if targetIndex == 0, reset seq/wn0prior
//
//
//        JSONObject listCol = new JSONObject();
//        db.summOrder(targetOrder, listCol);
//
//        qt.errPrint("target,", null, targetOrder, fromOrder);
//        qt.saveMD(targetOrder);
//
//        qt.setES("lSBOrder", qt.setESFilt("id_O", targetId_O), listCol);
//
//        this.delOItem(fromId_O, fromIndex);
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    /**
//     * to set arrPS so prod can be replaceable
//     * @param arrayId_P - arrPS list of all similar Prod
//     * @param isAdd - if false, it will clear ALL previous arrPS (delete arrPS data,== clearAll)
//     * @return
//     * checked looks OK
//     */
//    @Override
//    public ApiResponse replaceProd(JSONArray arrayId_P, Boolean isAdd) {
//        JSONArray selectProds = qt.getES("lBProd", qt.setESFilt("id_P", "contain", arrayId_P));
//        List<JSONObject> esBulk = new ArrayList<>();
//        if (!isAdd)
//        {
//            arrayId_P = new JSONArray();
//        }
//        for (int i = 0; i < selectProds.size(); i++)
//        {
//            esBulk.add(
//                qt.setJson(
//                        "type", "update",
//                        "id", selectProds.getJSONObject(i).getString("id_ES"),
//                        "update", qt.setJson("arrPS", arrayId_P)
//                )
//            );
//        }
//        qt.setESMany("lBProd", esBulk);
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//
//    /**
//     * wn2qty is an array because my upPrnts is array, so if I split, I need to declare
//     * how many is taken from each upPrnts
//     * 1. copy into new order, get all fields
//     * 2.
//     * @param id_O original order ID
//     * @param index which is splitting
//     * @param arrayWn2qty there're qty for each "upPrnts", you need to pick qty using Array
//     * @return
//     */
//    @Override
//    @Transactional
//    public ApiResponse splitOrder(String id_O, Integer index, JSONArray arrayWn2qty) {
//
//        Order order = qt.getMDContent(id_O, "", Order.class);
//
//        if (order.getInfo().getLST() > 6) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.CONFIRMED_ORDER_CANT_UPDATE.getCode(), null);
//        }
//        JSONObject jsonCard = db.checkCard(order);
//
//        JSONObject jsonOItem = order.getOItem().getJSONArray("objItem").getJSONObject(index);
//        List<JSONObject> listBulk = new ArrayList<>();
//
//        // this is total of all qty split away
//        Double wn2qtySum = arrayWn2qty.stream().mapToDouble(w -> Double.parseDouble(w.toString())).sum();
//
//        //数量不足 (== not ok, you are "moving " not really splitting)
//        if (Double.doubleToLongBits(wn2qtySum) >= Double.doubleToLongBits(
//                order.getOItem().getJSONArray("objItem").getJSONObject(index).getDouble("wn2qtyneed")))
//        {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.PROD_NOT_ENOUGH.getCode(), null);
//        }
//
//        //Create new order
//        Order newOrder = qt.cloneThis(order, Order.class);
//        String newId_O = qt.GetObjectId();
//        newOrder.setId(newId_O);
//        order.getOItem().getJSONArray("objItem").getJSONObject(index).put("wn2qtyneed", jsonOItem.getDouble("wn2qtyneed") - wn2qtySum);
//       //新订单 add qty
//        newOrder.getOItem().put("objItem", new JSONArray());
//        JSONObject jsonNewOItem = qt.cloneObj(jsonOItem);
//        jsonNewOItem.put("wn2qtyneed", wn2qtySum);
//        jsonNewOItem.put("id_O", newId_O);
//        newOrder.getOItem().getJSONArray("objItem").add(jsonNewOItem);
//        //oStock
//        if (order.getOStock() != null) {
//
//            newOrder.getOStock().put("objData", new JSONArray());
//
//            db.initOStock(newOrder.getOItem().getJSONArray("objItem").getJSONObject(0),
//                    newOrder.getOStock().getJSONArray("objData"), 0);
//
//        }
//
//        //action
//        JSONArray arrayAction = jsonCard.getJSONArray("action");
//        if (arrayAction != null) {
//            JSONObject jsonAction = arrayAction.getJSONObject(index);
//
//            newOrder.getAction().put("objAction", new JSONArray());
//            db.initAction(newOrder.getOItem().getJSONArray("objItem").getJSONObject(0),
//                    newOrder.getAction().getJSONArray("objAction"), 0);
//
//
//            JSONObject prtOrders = new JSONObject();
//            prtOrders.put(id_O, order);
//
//            this.getOrdersFour(prtOrders, arrayAction, index, index + 1);
//
//            // here newOrder has already init action with up /sub/next/prev are all []
//            // now init newOrder with all of them
//            JSONObject newOrderAction = newOrder.getAction().getJSONArray("objAction").getJSONObject(0);
//
//
//            for (int i = 0; i < arrayWn2qty.size(); i++)
//            {
//                if (arrayWn2qty.getDouble(i) > 0)
//                {
//                    JSONObject upPrntObj = qt.cloneObj(order.getAction().getJSONArray("objAction").getJSONObject(index).getJSONArray("upPrnts").getJSONObject(i));
//                    upPrntObj.put("wn2qtyneed", arrayWn2qty.getDouble(i));
//                    newOrderAction.getJSONArray("upPrnts").add(upPrntObj);
//                }
//            }
////            newOrderAction.put("upPrnts", qt.cloneArr(jsonAction.getJSONArray("upPrnts")));
//            newOrderAction.put("subParts", qt.cloneArr(jsonAction.getJSONArray("subParts")));
//            for (int i = 0; i < newOrderAction.getJSONArray("subParts").size(); i++)
//            {
//                newOrderAction.getJSONArray("subParts").getJSONObject(i).put("upIndex", 0);
//            }
//            newOrderAction.put("prtPrev", qt.cloneArr(jsonAction.getJSONArray("prtPrev")));
//            newOrderAction.put("prtNext", qt.cloneArr(jsonAction.getJSONArray("prtNext")));
//
//
//            qt.errPrint("upPrnts", null, newOrder);
//
//            prtOrders.put(newId_O, newOrder);
//
//            //now set the related sides
//            listBulk = updateRelation(newId_O, 0, id_O, index, newOrderAction.getJSONArray("upPrnts"), "subParts", "split", listBulk, prtOrders);
//
//            listBulk = updateRelation(newId_O, 0, id_O, index, newOrderAction.getJSONArray("subParts"), "upPrnts", "split", listBulk, prtOrders);
//
//            listBulk = updateRelation(newId_O, 0, id_O, index, newOrderAction.getJSONArray("prtPrev"), "prtNext", "split", listBulk, prtOrders);
//
//            listBulk = updateRelation(newId_O, 0, id_O, index, newOrderAction.getJSONArray("prtNext"), "prtPrev", "split", listBulk, prtOrders);
//
//        }
//        JSONObject listCol = new JSONObject();
//        JSONObject listColNew = qt.getES("lSBOrder", qt.setESFilt("id_O", id_O)).getJSONObject(0);
//        db.summOrder(newOrder, listColNew);
//        listColNew.put("id_O", newId_O);
//        db.summOrder(order, listCol);
//
//        //update all old orders,
//
//        qt.setMDContentMany(listBulk, Order.class);
//        System.out.println("listBulk=" + listBulk);
//
//        qt.saveMD(order);
//        qt.addMD(newOrder);
//        qt.setES("lsborder", qt.setESFilt("id_O", id_O), listCol);
//        qt.addES("lsborder",listColNew);
//
//            //修改lsborder
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
////
////    public ApiResponse delOItem2(String id_O, Integer index) {
////        Order order = qt.getMDContent(id_O, Arrays.asList("oItem", "action", "oStock", "info", "view"), Order.class);
//////        System.out.println("order=" + order);
////        JSONObject jsonCard = db.checkCard(order);
//////        JSONObject oItem = order.getOItem();
//////        JSONArray arrayArrP = oItem.getJSONArray("arrP");
////        JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
//////        JSONObject jsonOItem = arrayOItem.getJSONObject(index);
////        List<JSONObject> listBulk = new ArrayList<>();
//////        Update update = new Update();
////        //只有一个产品，删除整个订单
////        if (index == 0 && arrayOItem.size() == 1) {
////            qt.delMD(id_O, Order.class);
////            qt.delES("lSBOrder", qt.setESFilt("id_O", id_O));
////
////            return retResult.ok(CodeEnum.OK.getCode(), null);
////        }
////
////
////        //oItem
////
////        arrayOItem.remove(index.intValue());
////
////        //oStock
////        JSONArray arrayOStock = jsonCard.getJSONArray("oStock");
////        if (arrayOStock != null) {
////            arrayOStock.remove(index.intValue());
//////            qt.upJson(jsonUpdate, "oStock.objData", arrayOStock);
////        }
////        //action
////        JSONArray arrayAction = jsonCard.getJSONArray("action");
////        if (arrayAction != null) {
////            arrayAction.remove(index.intValue());
////
////            //修改该订单后面被影响的action
////            listBulk = updateAffectedAction(id_O, arrayAction, index, listBulk, 1);
////        }
////        JSONObject listCol = new JSONObject();
////        JSONObject jsonUpdate = db.summOrder(order, listCol);
////
////        JSONObject jsonBulk = qt.setJson("type", "update",
////                "id", id_O,
////                "update", jsonUpdate);
////        listBulk.add(jsonBulk);
////
////        qt.setMDContentMany(listBulk, Order.class);
////        qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), listCol);
////
////        return retResult.ok(CodeEnum.OK.getCode(), null);
////    }
//
//    @Override
//    @Transactional
//    public ApiResponse delOItem(String id_O, Integer index) {
//        Order order = qt.getMDContent(id_O, Arrays.asList("oItem", "action", "oStock", "info", "view"), Order.class);
////        System.out.println("order=" + order);
//        if (order != null) {
//            db.checkCard(order);
//
//            List<JSONObject> listBulk = new ArrayList<>();
//            //只有一个产品，删除整个订单
//            if (index == 0 && order.getOItem().getJSONArray("objItem").size() == 1) {
//                qt.delMD(id_O, Order.class);
//                qt.delES("lSBOrder", qt.setESFilt("id_O", id_O));
//                return retResult.ok(CodeEnum.OK.getCode(), null);
//            }
//
//            //action
//            if (order.getAction() != null) {
//                JSONArray arrayAction = qt.cloneArr(order.getAction().getJSONArray("objAction"));
//                String id_P = order.getOItem().getJSONArray("objItem").getJSONObject(index).getString("id_P");
//                //修改该订单后面被影响的action
////                listBulk = updateAffectedAction(id_O, arrayAction, index, listBulk, 1);
//
//                listBulk = updateAllTheRest(id_O, arrayAction, index, listBulk, id_P.equals("") ? 2 : 1);
//                if (listBulk.size() > 0) {
//                    qt.setMDContentMany(listBulk, Order.class);
//                    Order ordUp = qt.getMDContent(id_O, "action.objAction", Order.class);
//                    order.getAction().put("objAction", ordUp.getAction().getJSONArray("objAction"));
//                }
//
//                order.getAction().getJSONArray("objAction").remove(index.intValue());
//            }
//
//            order.getOItem().getJSONArray("objItem").remove(index.intValue());
//
//            //oStock
////            JSONArray arrayOStock = order.getOStock().getJSONArray("objData");
//            if (order.getOStock() != null) {
//                order.getOStock().getJSONArray("objData").remove(index.intValue());
//            }
//
//
//            JSONObject listCol = new JSONObject();
//            db.summOrder(order, listCol);
//            qt.errPrint("target,", null, order, listCol);
//
//            qt.saveMD(order);
//            qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), listCol);
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    @Override
//    public ApiResponse mergeOrders(String id_O, Integer index, JSONArray arrayMergeId_O, JSONArray arrayMergeIndex) {
//
//        // idO / index index大小倒序
//
//        for (int i = 0; i < arrayMergeId_O.size(); i++) {
//            String mergeId_O = arrayMergeId_O.getString(i);
//            Integer mergeIndex = arrayMergeIndex.getInteger(i);
//            mergeOrder(id_O, index, mergeId_O, mergeIndex);
//        }
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    @Override
//    public ApiResponse moveOItems(String id_O, Integer index, String fromId_O, JSONArray arrayfromIndex) {
//        //index大小倒序
//        for (int i = 0; i < arrayfromIndex.size() - 1; i++) {
//            for (int j = 0; j < arrayfromIndex.size() - 1 - i; j++) {
//                if (arrayfromIndex.getInteger(j) < arrayfromIndex.getInteger(j + 1)) {
//                    Integer fromIndex = arrayfromIndex.getInteger(j);
//                    arrayfromIndex.set(j, arrayfromIndex.getInteger(j + 1));
//                    arrayfromIndex.set(j + 1, fromIndex);
//                }
//            }
//        }
//        for (int i = 0; i < arrayfromIndex.size(); i++) {
//            Integer fromIndex = arrayfromIndex.getInteger(i);
//            moveOItem(id_O, index, fromId_O, fromIndex);
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//
//    @Override
//    //Batch move id_P[] into another order by another company
//    // arrayReplace : [{id_P, index}]
//
//    /**
//     * replacing original P with a new P using a new order or not
//     * arrayReplace = [{id_P, index}]
//     */
//    public ApiResponse replaceComp(String id_O, JSONArray arrayReplace) {
//
//        //index大小倒序 reverse the array
//        for (int i = 0; i < arrayReplace.size() - 1; i++) {
//            for (int j = 0; j < arrayReplace.size() - 1 - i; j++) {
//                if (arrayReplace.getJSONObject(j).getInteger("index") < arrayReplace.getJSONObject(j + 1).getInteger("index")) {
//                    JSONObject jsonReplace = arrayReplace.getJSONObject(j);
//                    arrayReplace.set(j, arrayReplace.getJSONObject(j + 1));
//                    arrayReplace.set(j + 1, jsonReplace);
//                }
//            }
//        }
//
//        //arrayReplace = [id_P = new id_P / index = old index position]
//        Order order = qt.getMDContent(id_O, Arrays.asList("info", "oItem", "oStock", "action","view"), Order.class);
//
//        if (order.getInfo().getLST() > 6) {
//            throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.CONFIRMED_ORDER_CANT_UPDATE.getCode(), null);
//        }
//
//        JSONObject jsonCard = db.checkCard(order);
//
//        JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
//        JSONArray setId_P = new JSONArray();
//
//        for (int i = 0; i < arrayReplace.size(); i++) {
//            String id_P = arrayReplace.getJSONObject(i).getString("id_P");
//            setId_P.add(id_P);
//        }
//
//        JSONArray filt = qt.setESFilt("id_P", "contain", setId_P);
//        qt.setESFilt(filt, "id_CB", "exact", order.getInfo().getId_CB());
//        JSONArray prodInfoList = qt.getES("lBProd", filt);
//        JSONObject mapProdObj = qt.arr2Obj(prodInfoList, "id_P");
//
//        Map<String, Order> mapId_O = new HashMap<>();
//        JSONObject newId_C_Id_O = new JSONObject();
//        List<JSONObject> listBulk = new ArrayList<>();
//
//        //修改lsborder + order @ id_O
//        JSONArray arrayEs = qt.getES("lsborder", qt.setESFilt("id_O", id_O));
//        JSONObject jsonEs = arrayEs.getJSONObject(0);
//        JSONObject listCol = qt.cloneObj(jsonEs);
//
//        JSONArray indexArray = new JSONArray();
//
//        for (int i = 0; i < arrayReplace.size(); i++) {
//            JSONObject jsonReplace = arrayReplace.getJSONObject(i);
//            String replaceId_P = jsonReplace.getString("id_P");
//            Integer index = jsonReplace.getInteger("index");
//
//            lBProd replaceProdInfo = qt.jsonTo(mapProdObj.getJSONObject(replaceId_P), lBProd.class);
//            // prod changed, the new supplier is replaceId_C
//            String replaceId_C = replaceProdInfo.getId_C();
//            JSONObject jsonOItem = arrayOItem.getJSONObject(index);
//            qt.errPrint("jsonOItem", null, jsonOItem);
//
//            //更换前后供应商相同，修改原订单 -- OK
//
//            if (replaceId_C.equals(order.getInfo().getId_C())) {
//
//                //oItem
//                JSONObject jsonUpdate = qt.setJson("oItem.arrP." + index, replaceId_P,
//                        "oItem.objItem." + index + ".id_P", replaceId_P);
//                //oStock
//                JSONArray arrayOStock = jsonCard.getJSONArray("oStock");
//                if (arrayOStock != null) {
//                    jsonUpdate.put("oStock.objData." + index + ".id_P", replaceId_P);
//                }
//                //action
//                JSONArray arrayAction = jsonCard.getJSONArray("action");
//                if (arrayAction != null) {
//                    jsonUpdate.put("action.objAction." + index + ".id_P", replaceId_P);
//                }
//                qt.setMDContent(id_O, jsonUpdate, Order.class);
//                //修改lsborder
//                JSONObject jsonHit = qt.getES("lSBOrder", qt.setESFilt("id_O",id_O)).getJSONObject(0);
//
//                JSONArray arrayEsArrP = jsonHit.getJSONArray("arrP");
//                arrayEsArrP.set(index, replaceId_P);
//
//                qt.setES("lSBOrder", qt.setESFilt("id_O",id_O), qt.setJson("arrP", arrayEsArrP));
//            }
//
//            //换供应商的产品和刚刚新增的订单组内其中一个的供应商相同，放入同一个订单
//
//            else if (newId_C_Id_O.getString(replaceId_C) != null && mapId_O.get(newId_C_Id_O.getString(replaceId_C)) != null) {
//                String replaceId_O = newId_C_Id_O.getString(replaceId_C);
//                Order replaceOrder = mapId_O.get(replaceId_O);
//
//                JSONArray arrayReplaceOItem =  replaceOrder.getOItem().getJSONArray("objItem");
//
//                int size = arrayReplaceOItem.size();
//                //oItem
//                OrderOItem newOItem = new OrderOItem(replaceId_P,replaceOrder.getInfo().getId_OP(), replaceOrder.getInfo().getId_CP(), replaceId_C,
//                        replaceOrder.getInfo().getId_CB(),replaceId_O, size,replaceProdInfo.getRef(), replaceProdInfo.getRefB(),
//                        replaceProdInfo.getGrp(),replaceProdInfo.getGrpB(), 0, replaceProdInfo.getPic(),replaceProdInfo.getLUT(),
//                        1, jsonOItem.getDouble("wn2qtyneed"), jsonOItem.getDouble("wn4price"), null, replaceProdInfo.getWrdN(),
//                        replaceProdInfo.getWrddesc(),jsonOItem.getJSONObject("wrdprep"));
//
//                arrayReplaceOItem.add(newOItem);
//
//                //oStock
//                JSONArray arrayOStock = jsonCard.getJSONArray("oStock");
//                if (arrayOStock != null) {
//                    JSONArray arrayReplaceOStock = replaceOrder.getOStock().getJSONArray("objData");
//                    JSONObject jsonReplaceOStock = qt.cloneObj(arrayOStock.getJSONObject(index));
//                    jsonReplaceOStock.put("id_P", replaceId_P);
//                    arrayReplaceOStock.add(jsonReplaceOStock);
//                }
//                //action
//                JSONArray arrayAction = jsonCard.getJSONArray("action");
//                if (arrayAction != null) {
//
//                    JSONArray arrayReplaceAction = replaceOrder.getAction().getJSONArray("objAction");
//
//                    // set my own action here
//                    JSONObject jsonReplaceAction = qt.cloneObj(arrayAction.getJSONObject(index));
//                    jsonReplaceAction.put("id_O", replaceId_O);
//                    jsonReplaceAction.put("id_P", replaceId_P);
//                    jsonReplaceAction.put("wrdN", replaceProdInfo.getWrdN());
//                    jsonReplaceAction.put("index", size);
//                    arrayReplaceAction.add(jsonReplaceAction);
//                    replaceOrder.getAction().put("objAction", arrayReplaceAction);
//
//                    // now go fix my linked orders
//                    //关联订单
//                    JSONArray arrayUpPrnt = jsonReplaceAction.getJSONArray("upPrnts");
//                    JSONArray arrayPrtNext = jsonReplaceAction.getJSONArray("prtNext");
//                    JSONArray arrayPrtPrev = jsonReplaceAction.getJSONArray("prtPrev");
//
//                    JSONObject mapOrder = new JSONObject();
//                    this.getOrdersFour(mapOrder, arrayAction, index, index+1);
//                    mapOrder.put(id_O, order);
//                    mapOrder.putAll(mapId_O);
//                    qt.errPrint("mapId)O", null, mapId_O, mapOrder);
//
//                        listBulk = updateRelation(replaceId_O, size, id_O, index, arrayUpPrnt, "subParts", "", listBulk, mapOrder);
//
//                        listBulk = updateRelation(replaceId_O, size, id_O, index, arrayPrtNext, "prtPrev", "", listBulk, mapOrder);
//
//                        listBulk = updateRelation(replaceId_O, size, id_O, index, arrayPrtPrev, "prtNext", "", listBulk, mapOrder);
//
//                }
//                indexArray.add(index);
//            }
//            else //Create new order
//            {
//                Order replaceOrder = new Order();
//                replaceOrder.setView(order.getView());
//                String replaceId_O = Qt.GetObjectId();
//                System.out.println("replaceId_O=" + replaceId_O);
//                replaceOrder.setId(replaceId_O);
//                //info
//                JSONObject info_copy = qt.cloneObj(qt.toJson(order.getInfo()));
//                OrderInfo replaceOrderInfo = qt.jsonTo(info_copy, OrderInfo.class);
//                replaceOrderInfo.setId_C(replaceId_C);
//                replaceOrderInfo.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                replaceOrderInfo.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                replaceOrder.setInfo(replaceOrderInfo);
//
//                //oItem
//
//                JSONObject jsonReplaceOItem = qt.cloneObj(jsonOItem);
//
//
//                qt.upJson(jsonReplaceOItem,
//                        "id_C", replaceId_C,
//                        "id_O", replaceId_O,
//                        "id_P", replaceId_P,
//                        "wrdN", replaceProdInfo.getWrdN(),
//                        "index", 0,
//                        "seq", 0,"wn0prior", 1);
//
//                qt.errPrint("replaceProd", null, jsonOItem, replaceProdInfo, jsonReplaceOItem);
//
//
//                JSONObject replaceOItem = qt.setJson(
//                        "objItem", qt.setArray(jsonReplaceOItem));
//
//                replaceOrder.setOItem(replaceOItem);
//                db.checkCard(replaceOrder);
//
//                //oStock
//                JSONArray arrayOStock = jsonCard.getJSONArray("oStock");
//                if (arrayOStock != null) {
//                    replaceOrder.setOStock(db.initOStock(replaceOItem.getJSONArray("objItem")));
//                }
//
//                //action
//                JSONArray arrayAction = jsonCard.getJSONArray("action");
//
//                if (arrayAction != null) {
//                    replaceOrder.setAction(db.initAction(replaceOItem.getJSONArray("objItem")));
//
//                    //关联订单
//                    JSONArray arrayUpPrnt = arrayAction.getJSONObject(0).getJSONArray("upPrnts");
//                    JSONArray arrayPrtNext = arrayAction.getJSONObject(0).getJSONArray("prtNext");
//                    JSONArray arrayPrtPrev = arrayAction.getJSONObject(0).getJSONArray("prtPrev");
//
//                    JSONObject mapOrder = new JSONObject();
//                    this.getOrdersFour(mapOrder, arrayAction, 0, 1);
//                    mapOrder.put(id_O, order);
//                    mapOrder.put(replaceId_O, replaceOrder);
//                    mapOrder.putAll(mapId_O);
//                    qt.errPrint("mapId)O2", null, mapId_O, mapOrder);
//
//
//                    //父订单的子订单
//                        listBulk = updateRelation(replaceId_O, 0, id_O, index, arrayUpPrnt, "subParts", "", listBulk, mapOrder);
//
//                        listBulk = updateRelation(replaceId_O, 0, id_O, index, arrayPrtNext, "prtPrev", "", listBulk, mapOrder);
//
//                        listBulk = updateRelation(replaceId_O, 0, id_O, index, arrayPrtPrev, "prtNext", "", listBulk, mapOrder);
//                }
//                mapId_O.put(replaceId_O, replaceOrder);
//                newId_C_Id_O.put(replaceId_C, replaceId_O);
//                indexArray.add(index);
//            }
//        }
//
//        //新增订单
//        List<JSONObject> listBulkEs = new ArrayList<>();
//        for (Map.Entry<String, Order> entry : mapId_O.entrySet()) {
//            String id = entry.getKey();
//            Order newOrder = mapId_O.get(id);
//            JSONObject thisList = qt.cloneObj(listCol);
//
//            // make sure all newly created orders' summary is updated and set into ES
//            db.summOrder(newOrder, thisList);
//            qt.errPrint("result", null, newOrder);
//            qt.addMD(newOrder);
//            thisList.put("id_O", id);
//            thisList.put("id_C", newOrder.getInfo().getId_C());
//
//            JSONObject jsonBulk = qt.setJson("type", "insert", "insert", thisList);
//
//            listBulkEs.add(jsonBulk);
//        }
//
//        qt.setESMany("lsborder", listBulkEs);
//        qt.setMDContentMany(listBulk,Order.class);
//
//        for (int i = 0; i < indexArray.size(); i ++)
//        {
//            this.delOItem(id_O, indexArray.getInteger(i));
//        }
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    @Override
//    public ApiResponse movePosition(String id_O, Integer toIndex, Integer fromIndex) {
//        Order order = qt.getMDContent(id_O, Arrays.asList("oItem", "action", "oStock", "info", "view"), Order.class);
//        JSONObject jsonCard = db.checkCard(order);
//
//        JSONArray arrayArrP = order.getOItem().getJSONArray("arrP");
//        String moveArrP = arrayArrP.getString(toIndex);
//        JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
//        JSONObject jsonMoveOItem = arrayOItem.getJSONObject(toIndex);
//
//
//        List<JSONObject> listBulk = new ArrayList<>();
//        JSONObject jsonUpdate = new JSONObject();
//        //往前移
//        int num = -1;
//        int relationNum = -1;
//        int max = toIndex;
//        int min = fromIndex;
//        //往后移
//        if (toIndex < fromIndex) {
//            num = 0;
//            relationNum = 1;
//            max = fromIndex;
//            min = toIndex;
//        }
//        //oItem
//        JSONArray arrayNewArrP = new JSONArray();
//        JSONArray arrayNewOItem = new JSONArray();
//        if (fromIndex + num < 0) {
//            arrayNewArrP.add(moveArrP);
//            jsonMoveOItem.put("index", arrayNewOItem.size());
//            arrayNewOItem.add(jsonMoveOItem);
//        }
//        for (int i = 0; i < arrayOItem.size(); i++) {
//            if (i != toIndex) {
//                arrayNewArrP.add(arrayArrP.getString(i));
//                JSONObject jsonOItem = arrayOItem.getJSONObject(i);
//                jsonOItem.put("index", arrayNewOItem.size());
//                arrayNewOItem.add(jsonOItem);
//                if (i == fromIndex - num) {
//                    arrayNewArrP.add(moveArrP);
//                    jsonMoveOItem.put("index", arrayNewOItem.size());
//                    arrayNewOItem.add(jsonMoveOItem);
//                }
//            }
//        }
//        qt.upJson(jsonUpdate, "oItem.arrP", arrayNewArrP,
//                "oItem.objItem", arrayNewOItem);
//        //oStock
//        JSONArray arrayOStock = jsonCard.getJSONArray("oStock");
//        if (arrayOStock != null) {
//            JSONArray arrayNewOStock = new JSONArray();
//            JSONObject jsonMoveOStock = arrayOStock.getJSONObject(toIndex);
//            if (fromIndex + num < 0) {
//                arrayNewOStock.add(jsonMoveOStock);
//            }
//            for (int i = 0; i < arrayOStock.size(); i++) {
//                if (i != toIndex) {
//                    arrayNewOStock.add(arrayOStock.getJSONObject(i));
//                    if (i == fromIndex - num) {
//                        arrayNewOStock.add(jsonMoveOStock);
//                    }
//                }
//            }
//            qt.upJson(jsonUpdate, "oStock.objData", arrayNewOStock);
//        }
//        //action
//        JSONArray arrayAction = jsonCard.getJSONArray("action");
//        if (arrayAction != null) {
//            JSONArray arrayNewAction = new JSONArray();
//            JSONObject jsonMoveAction = arrayAction.getJSONObject(toIndex);
//            if (fromIndex + num < 0) {
//                jsonMoveAction.put("index", arrayNewAction.size());
//                arrayNewAction.add(jsonMoveAction);
//            }
//            for (int i = 0; i < arrayAction.size(); i++) {
//                if (i != toIndex) {
//                    JSONObject jsonAction = arrayAction.getJSONObject(i);
//                    jsonAction.put("index", arrayNewAction.size());
//                    arrayNewAction.add(jsonAction);
//                    if (i == fromIndex - num) {
//                        jsonMoveAction.put("index", arrayNewAction.size());
//                        arrayNewAction.add(jsonMoveAction);
//                    }
//                }
//            }
//            qt.upJson(jsonUpdate, "action.objAction", arrayNewAction);
////            [4,0,1,2,3] index:4, fromIndex:0
//
//            JSONObject prtOrders = new JSONObject();
//            prtOrders.put(id_O, order);
//            this.getOrdersFour(prtOrders, arrayNewAction, min, max + 1);
//
//
//            for (int i = min; i <= max; i++) {
//                int relationIndex;
//                if (i == fromIndex) {
//                    relationIndex = toIndex;
//                } else {
//                    relationIndex = i + relationNum;
//                }
//                JSONObject jsonAction = arrayNewAction.getJSONObject(i);//1
//                JSONArray arrayUpPrnt = jsonAction.getJSONArray("upPrnts");
//                JSONArray arraySubPart = jsonAction.getJSONArray("subParts");
//                JSONArray arrayPrtPrev = jsonAction.getJSONArray("prtPrev");
//                JSONArray arrayPrtNext = jsonAction.getJSONArray("prtNext");
//                if (arrayUpPrnt != null) {
//                    listBulk = updateRelation(id_O, i, id_O , relationIndex, arrayUpPrnt, "subParts", "", listBulk, prtOrders);
//                }
//                if (arraySubPart != null) {
//                    listBulk = updateRelation(id_O, i, id_O , relationIndex, arraySubPart, "upPrnts", "", listBulk, prtOrders);
//                }
//                if (arrayPrtPrev != null) {
//                    listBulk = updateRelation(id_O, i, id_O , relationIndex, arrayPrtPrev, "prtNext", "", listBulk, prtOrders);
//                }
//                if (arrayPrtNext != null) {
//                    listBulk = updateRelation(id_O, i, id_O , relationIndex, arrayPrtNext, "prtPrev", "", listBulk, prtOrders);
//                }
//            }
//        }
//        JSONObject jsonBulk = qt.setJson("type", "update",
//                "id", id_O,
//                "update", jsonUpdate);
//        listBulk.add(jsonBulk);
//        qt.setMDContentMany(listBulk, Order.class);
//
//        qt.setES("lSBOrder", qt.setESFilt("id_O", id_O), qt.setJson("arrP", arrayNewArrP));
//
//
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    @Override
//    public ApiResponse textToOItem(String id_C, String id_O, String id, Integer cardIndex, Integer textIndex, String table) {
//        JSONObject jsonObjData = new JSONObject();
//
//        switch (table) {
//            case "Comp":
//                Comp comp = qt.getMDContent(id, "text00s", Comp.class);
//                jsonObjData = comp.getText00s().getJSONArray("objData").getJSONObject(cardIndex);
//                break;
//            case "Order":
//                Order order = qt.getMDContent(id, "text00s", Order.class);
//                jsonObjData = order.getText00s().getJSONArray("objData").getJSONObject(cardIndex);
//                break;
//            case "User":
//                User user = qt.getMDContent(id, "text00s", User.class);
//                jsonObjData = user.getText00s().getJSONArray("objData").getJSONObject(cardIndex);
//                break;
//            case "Prod":
//                Prod prod = qt.getMDContent(id, "text00s", Prod.class);
//                jsonObjData = prod.getText00s().getJSONArray("objData").getJSONObject(cardIndex);
//                break;
//            case "Asset":
//                Asset asset = qt.getMDContent(id, "text00s", Asset.class);
//                jsonObjData = asset.getText00s().getJSONArray("objData").getJSONObject(cardIndex);
//                break;
//        }
//        JSONObject jsonObjText = jsonObjData.getJSONArray("objText").getJSONObject(textIndex);
//
//        Order order = qt.getMDContent(id_O, "", Order.class);
//        JSONArray arrayObjItem = order.getOItem().getJSONArray("objItem");
//        JSONObject listCol = new JSONObject();
//        OrderOItem lastItem = qt.jsonTo(arrayObjItem.getJSONObject(arrayObjItem.size() - 1),OrderOItem.class);
//
//        lastItem.setWrdN(jsonObjText.getJSONObject("wrdN"));
//        lastItem.setWrddesc(jsonObjText.getJSONObject("wrddesc"));
//        lastItem.setIndex(arrayObjItem.size());
//        lastItem.setId_C(id_C);
//        lastItem.setSeq("2");
//        lastItem.setWn0prior(lastItem.getWn0prior() + 1);
//        lastItem.setObjSub(0);
//        lastItem.genRKey();
//        lastItem.setId_P("");
//
//        order.getOItem().getJSONArray("objItem").add(lastItem);
//
//
//        db.checkCard(order);
//        db.summOrder(order, listCol);
//        qt.saveMD(order);
//        qt.setES("lBOrder", qt.setESFilt("id_O", id_O), listCol);
//
////        qt.pushMDContent(id_O, "oItem.objItem", lastItem, Order.class);
//        return retResult.ok(CodeEnum.OK.getCode(), null);
//    }
//
//    /**
//     *
//     * @Author Rachel
//     * @Date 2022/09/13
//     * @Param id_O
//     * @Param arrayAction
//     * @Param index 删除：index / 插入：index + 1
//     * @Param listBulk
//     * @Param type 删除：1 / 插入：-1
//     * @Return java.util.List<java.util.Map>
//     * @Card
//     **/
//    public List<JSONObject> updateAffectedAction(String id_O, JSONArray arrayAction, Integer index, List<JSONObject> listBulk, Integer type) {
//        JSONObject jsonOrders = new JSONObject();
//
//        this.getOrdersFour(jsonOrders, arrayAction, index, arrayAction.size());
//
//        //index to the end, or type = -1 ?
//        for (int i = index; i < arrayAction.size(); i++) {
//            JSONObject jsonAction = arrayAction.getJSONObject(i);
//            JSONArray arrayUpPrnt = jsonAction.getJSONArray("upPrnts");
//            JSONArray arraySubPart = jsonAction.getJSONArray("subParts");
//            JSONArray arrayPrtPrev = jsonAction.getJSONArray("prtPrev");
//            JSONArray arrayPrtNext = jsonAction.getJSONArray("prtNext");
//
//            // from and to the same id_O
//            // from before index = i + type(1/-1) to i, use this to update all my up/Sub/Preb/Next
//            if (arrayUpPrnt != null) {
//                listBulk = updateRelation(id_O, i, id_O, i + type, arrayUpPrnt, "subParts", "", listBulk, jsonOrders);
//            }
//            if (arraySubPart != null) {
//                listBulk = updateRelation(id_O, i, id_O, i + type, arraySubPart, "upPrnts", "", listBulk, jsonOrders);
//            }
//            if (arrayPrtPrev != null) {
//                listBulk = updateRelation(id_O, i, id_O, i + type, arrayPrtPrev, "prtNext", "", listBulk, jsonOrders);
//            }
//            if (arrayPrtNext != null) {
//                listBulk = updateRelation(id_O, i, id_O, i + type, arrayPrtNext, "prtPrev", "", listBulk, jsonOrders);
//            }
//        }
//        return listBulk;
//    }
//
//
//    /**
//     * 修改因del/addOItem 在中间时， loop 后面的所有action的 up/sub/prev/nextw
//     * @param id_O
//     * @param arrayAction
//     * @param index
//     * @param listBulk
//     * @param upType
//     * @return
//     */
//    public List<JSONObject> updateAllTheRest(String id_O, JSONArray arrayAction, Integer index, List<JSONObject> listBulk, Integer upType) {
//        JSONObject jsonOrders = new JSONObject();
//
//        this.getOrdersFour(jsonOrders, arrayAction, index, arrayAction.size());
//        // index is me, if add below me, then from index is index + 1, new index is index + 2
//        // index is me, if delete me, then from index is index + 1, new index is index
//        Integer inc = -1;
//
//        switch (upType) {
//            case 1:
//                // del a P
//                inc = -1;
//                index++; // then from index is index + 1,
//                break;
//            case 2:
//                // del a nonP
//                inc = -1;
//                break;
//            case 3:
//                // add a P
//                inc = 1;
//                break;
//            case 4:
//                // add a nonP
//                inc = 1;
//                index--;
//                break;
//        }
//
//
//        //index to the end, or inc = -1 ?
//        for (int i = index; i < arrayAction.size(); i++) {
//            JSONObject jsonAction = arrayAction.getJSONObject(i);
//            JSONArray arrayUpPrnt = jsonAction.getJSONArray("upPrnts");
//            JSONArray arraySubPart = jsonAction.getJSONArray("subParts");
//            JSONArray arrayPrtPrev = jsonAction.getJSONArray("prtPrev");
//            JSONArray arrayPrtNext = jsonAction.getJSONArray("prtNext");
//            qt.errPrint("I am updateAllTheRest", null, id_O,"start from this index",index, "current is", i);
//
//            // from and to the same id_O
//            // from before index = i + inc(1/-1) to i, use this to update all my up/Sub/Preb/Next
//            if (arrayUpPrnt != null) {
//                listBulk = updateRelation(id_O, i + inc, id_O, i, arrayUpPrnt, "subParts", "", listBulk, jsonOrders);
//            }
//            if (arraySubPart != null) {
//                listBulk = updateRelation(id_O, i + inc, id_O, i, arraySubPart, "upPrnts", "", listBulk, jsonOrders);
//            }
//            if (arrayPrtPrev != null && ((upType.equals(2) &&!index.equals(i)) || (upType.equals(4) && index + 1 < i))) {
//                listBulk = updateRelation(id_O, i + inc, id_O, i, arrayPrtPrev, "prtNext", "", listBulk, jsonOrders);
//            }
//            if (arrayPrtNext != null) {
//                listBulk = updateRelation(id_O, i + inc, id_O, i, arrayPrtNext, "prtPrev", "", listBulk, jsonOrders);
//            }
//        }
//        //TODO KEV what about fixing wn2prior because "seq=2 is added"
//        return listBulk;
//    }
//
//
//
//    // oStock : id_P/rKey/ objShip[{made/now/need} - 因多父] / resvQty{} / wn2qtymade / wn2qtynow  objShip对应upPrnts， 加减要改
//
//    /**
//     * Loop thru all items in array of my "relationType", then go check the index of theOtherSide, and update that' id_O/index
//     * @param newId_O targetId_O
//     * @param newIndex
//     * @param oldId_O from id_O
//     * @param oldIndex
//     * @param arrayRelation fromActionItems (upPrnts) I am checking
//     * @param theOtherSide I am fixing "subParts"
//     * @param type "" / split/merge/replace <--- should be the same???
//     * @param listBulk updateData
//     * @param jsonOrders allOrder info
//     * @return
//     */
//    public List<JSONObject> updateRelation(String newId_O, Integer newIndex, String oldId_O, Integer oldIndex, JSONArray arrayRelation,
//                                         String theOtherSide, String type, List<JSONObject> listBulk, JSONObject jsonOrders) {
//
//        for (int i = 0; i < arrayRelation.size(); i++) {
//
//            JSONObject myRelationObj = arrayRelation.getJSONObject(i);
//            JSONObject jsonUpdate = null;
//
//            String relatedId_O = myRelationObj.getString("id_O");
//            Integer relatedIndex = myRelationObj.getInteger("index");
//            Order relatedOrder = qt.jsonTo(jsonOrders.getJSONObject(relatedId_O), Order.class);
//            Order myOrder = qt.jsonTo(jsonOrders.getJSONObject(newId_O), Order.class);
//            qt.errPrint("old Indexes", null, oldId_O, oldIndex, newId_O, newIndex, relatedId_O, relatedIndex, theOtherSide);
//
//            JSONArray relatedSideArray = relatedOrder.getAction().getJSONArray("objAction")
//                    .getJSONObject(relatedIndex).getJSONArray(theOtherSide);
//
//
//            // use old newId_O/newIndex to find out where is the other side
//            Integer theOtherSideIndex = getIndex(relatedSideArray, oldId_O, oldIndex);
//            if (theOtherSideIndex == -1)
//            {
//                //no such linkage, can't get the other side's me... error
////                throw new ErrorResponseException(HttpStatus.OK, DetailsEnum.MERGEORDER_COMP_DIFFERENT.getCode(), "in updateRealtion");
//                qt.errPrint("i am in -1 otherSide", null);
//                return listBulk;
//            }
//            JSONObject relatedJson = relatedSideArray.getJSONObject(theOtherSideIndex);
//
//            Double need = myRelationObj.getDouble("need");
//
//            Double wn2qtyneed = relatedOrder.getOItem().getJSONArray("objItem")
//                    .getJSONObject(relatedIndex).getDouble("wn2qtyneed");
//
//            JSONObject oldOItem = jsonOrders.getJSONObject(oldId_O).getJSONObject("oItem").getJSONArray("objItem").getJSONObject(oldIndex);
//
//
//            if (type.equals("split")) {
//                if (theOtherSide.equals("subParts"))
//                {
//                    Double qtyEach = myRelationObj.getDouble("wn2qtyneed");
//                    //拆分全部
//                    if (dbl.compareTo(qtyEach, relatedJson.getDouble("qtyEach")) == 0) {
//                        qt.upJson(relatedJson, "id_O", newId_O,
//                                "index", newIndex, "upIndex", i);
//                        jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide, relatedSideArray);
//                    } else {
//                        qt.upJson(relatedJson, "qtyEach", dbl.subtract(relatedJson.getDouble("qtyEach"), qtyEach));
//                        JSONObject jsonNewInterRelation = qt.cloneObj(relatedJson);
//                        qt.upJson(jsonNewInterRelation, "id_O", newId_O,
//                                "index", newIndex,
//                                "qtyEach", qtyEach);
//                        relatedSideArray.add(jsonNewInterRelation);
//                        jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide, relatedSideArray);
//                    }
//                }
//                else if (theOtherSide.equals("upPrnts"))
//                {
//                    // this is the total needed I need to write into my subParts
//
//                    qt.upJson(relatedJson, "wn2qtyneed", dbl.subtract(relatedJson.getDouble("wn2qtyneed"), oldOItem.getDouble("wn2qtyneed")));
//                    JSONObject jsonNewInterRelation = qt.cloneObj(relatedJson);
//                    qt.upJson(jsonNewInterRelation, "id_O", newId_O,
//                            "index", newIndex,
//                            "qtyNeed", myRelationObj.getDouble("wn2qtyneed"),
//                            "wrdN", oldOItem.getJSONObject("wrdN"));
//
//                    relatedSideArray.add(jsonNewInterRelation);
//                    jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide, relatedSideArray);
//                }
//                else
//                {
//                    relatedSideArray.add(qt.setJson("id_O", newId_O, "index", newIndex));
//                    jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide, relatedSideArray);
//                }
//            }
//            // very special because this is only for upPrnts && has "need" (linking same order)
////            else if (type.equals("merge") && need != null)
////            {
////                relatedSideArray.remove(theOtherSideIndex);
////                Integer interRelationIndex2 = getIndex(relatedSideArray, newId_O, newIndex);
////                JSONObject jsonInterRelation = relatedSideArray.getJSONObject(interRelationIndex2);
////                jsonInterRelation.put("qtyEach", dbl.subtract(need, wn2qtyneed));
////                jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide, relatedSideArray);
////
////                //for the second upPrnts, you may need to delete the objShip from oStock
//////                if (relatedOrder.getOStock() != null && theOtherSide.equals("subParts") && i >= 1) {
////
////                    // old objShip need to remove WRONG!!
//////                    JSONArray oldObjShip = jsonOrders.getJSONObject(oldId_O).getJSONObject("oStock")
//////                            .getJSONArray("objData").getJSONObject(oldIndex).getJSONArray("objShip");
//////
//////                    if (oldObjShip.size() > 1) {
//////                        oldObjShip.remove(i);
//////                        qt.setMDContent(oldId_O, qt.setJson("oStock.objData." + oldIndex + ".objShip", oldObjShip), Order.class);
//////                    }
//////
//////                    if (!myOrder.getAction().getJSONArray("objAction").getJSONObject(newIndex).getInteger("bmdpt").equals(1)) {
//////                        //new objShip need to add [{made/now/need} - 因多父]
//////                        JSONObject newObjShip = qt.setJson("wn2qtyneed", myOrder.getOItem().getJSONArray("objItem").getJSONObject(newIndex).getDouble("wn2qtyneed"),
//////                                "wn2qtynow", 0.0, "wn2qtymade", 0.0);
//////                        qt.upJson(jsonUpdate, "oStock.objData." + relatedIndex + ".objShip." + theOtherSideIndex, newObjShip);
//////                    }
////                    if (theOtherSide.equals("subParts") ) {
////
////                        qt.upJson(jsonUpdate, "action.objAction." + relatedIndex + ".subParts." + theOtherSideIndex + ".upIndex", i);
////
////                }
////
////                jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide + "." + theOtherSideIndex + ".id_O", newId_O,
////                        "action.objAction." + relatedIndex + "." + theOtherSide + "." + theOtherSideIndex + ".index", newIndex);
//////                 // next and prev update here just change pointers
//////
//////                    relatedSideArray.add(qt.setJson("id_O", newId_O, "index", newIndex));
//////                    jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide, relatedSideArray);
//////
////            }
//            else // moveOItem,
//            {
//
//                // set objAction all Types of the otherSide's id_O / index
//                jsonUpdate = qt.setJson("action.objAction." + relatedIndex + "." + theOtherSide + "." + theOtherSideIndex + ".id_O", newId_O,
//                        "action.objAction." + relatedIndex + "." + theOtherSide + "." + theOtherSideIndex + ".index", newIndex);
//
//                if (theOtherSide.equals("subParts"))
//                {
//                    // subParts: id_P 子P== / prior 子wn0prior== / qtyEach子量== / upIndex 我是第几个 / wrdN 子名==
//                    qt.upJson(jsonUpdate, "action.objAction." + relatedIndex + ".subParts." + theOtherSideIndex + ".upIndex", i);
//
//                } else if (theOtherSide.equals("upPrnts"))
//                {
//                    // upPrnts: qtyNeed 这个父的数和名/ wrdN
//                    qt.upJson(jsonUpdate,
//                            "action.objAction." + relatedIndex + ".upPrnts." + theOtherSideIndex + ".qtyNeed",
//                            oldOItem.getDouble("wn2qtyneed"),
//
//                            "action.objAction." + relatedIndex + ".upPrnts." + theOtherSideIndex + ".wrdN",
//                            oldOItem.getJSONObject("wrdN"));
//                }
//            }
//
//            //Fixing casItemx if subParts
//            if (relatedOrder.getCasItemx() != null && theOtherSide.equals("subParts"))
//            {
//                // Removing old casItemx size--
//                JSONArray relatedObjOrder = relatedOrder.getCasItemx().getJSONObject( relatedOrder.getInfo().getId_C() ).getJSONArray("objOrder");
//
//                if (!type.equals("split")) {
//                    qt.errPrint("well", null, myOrder);
//                    for (int j = 0; j < relatedObjOrder.size(); j++) {
//                        JSONObject casObj = relatedObjOrder.getJSONObject(j);
//                        if (casObj.getString("id_O").equals(oldId_O)) {
//
//                            if (casObj.getInteger("size").equals(1)) {
//                                //if size gets to 0, you need to remove that id_O from objOrder list
//                                relatedObjOrder.remove(j);
//                            } else {
//                                // I found the old Order, size--
//                                casObj.put("size", casObj.getInteger("size") - 1);
//                            }
//                            break;
//                        }
//                    }
//                }
//                // finally add one, with id_O, my order.wrdN, lST, size = 1, type = 2
//                JSONObject newCasObj = qt.setJson("id_O", newId_O, "wrdN", myOrder.getInfo().getWrdN(),
//                        "lST", myOrder.getInfo().getLST(), "size", 1, "type", 2);
//                relatedObjOrder.add(newCasObj);
//
//                qt.upJson(jsonUpdate, "casItemx." + relatedOrder.getInfo().getId_C() + ".objOrder", relatedObjOrder);
//            }
//
//            //Fixing oStock.objShip, if upPrnts
//            if (relatedOrder.getOStock() != null && theOtherSide.equals("upPrnts")) {
//                // old objShip need to remove
//                JSONArray oldObjShip = jsonOrders.getJSONObject(oldId_O).getJSONObject("oStock")
//                        .getJSONArray("objData").getJSONObject(oldIndex).getJSONArray("objShip");
//
//                if (!type.equals("split") && oldObjShip.size() > i) {
//                    oldObjShip.remove(i);
//                    qt.setMDContent(oldId_O, qt.setJson("oStock.objData." + oldIndex + ".objShip", oldObjShip), Order.class);
//                }
//
//                //new objShip need to add [{made/now/need} - 因多父]
//                JSONObject newObjShip = qt.setJson("wn2qtyneed", oldOItem.getDouble("wn2qtyneed"),
//                        "wn2qtynow", 0.0, "wn2qtymade", 0.0);
//                qt.upJson(jsonUpdate, "oStock.objData." + relatedIndex + ".objShip." + theOtherSideIndex, newObjShip);
//
//            }
//
//            db.setBulkUpdate(listBulk, relatedId_O, jsonUpdate);
////            JSONObject jsonBulk = qt.setJson("type", "update", "id", relatedId_O, "update", jsonUpdate);
////            qt.errPrint("listbulk", null, listBulk, jsonUpdate, jsonBulk);
////            listBulk.add(jsonBulk);
//        }
//        return listBulk;
//    }
//
//    private void getOrdersFour(JSONObject resultObjObj, JSONArray arrayMaster, Integer min, Integer max)
//    {
//        JSONArray arrayThis = new JSONArray();
//
//        for (int i1 = min; i1 < max; i1++) {
//            JSONObject jsonAction = arrayMaster.getJSONObject(i1);
//            JSONArray arrayUpPrnt = jsonAction.getJSONArray("upPrnts") != null ? jsonAction.getJSONArray("upPrnts") : new JSONArray() ;
//            JSONArray arraySubPart = jsonAction.getJSONArray("subParts") != null ? jsonAction.getJSONArray("subParts") : new JSONArray() ;
//            JSONArray arrayPrtPrev = jsonAction.getJSONArray("prtPrev") != null ? jsonAction.getJSONArray("prtPrev") : new JSONArray() ;
//            JSONArray arrayPrtNext = jsonAction.getJSONArray("prtNext") != null ? jsonAction.getJSONArray("prtNext") : new JSONArray() ;
//
//            for (int i = 0; i < arrayUpPrnt.size(); i++)
//            {
//                arrayThis.add(arrayUpPrnt.getJSONObject(i));
//            }
//            for (int i = 0; i < arraySubPart.size(); i++)
//            {
//                arrayThis.add(arraySubPart.getJSONObject(i));
//            }
//            for (int i = 0; i < arrayPrtNext.size(); i++)
//            {
//                arrayThis.add(arrayPrtNext.getJSONObject(i));
//            }
//            for (int i = 0; i < arrayPrtPrev.size(); i++)
//            {
//                arrayThis.add(arrayPrtPrev.getJSONObject(i));
//            }
//        }
//        this.getOrders(resultObjObj, arrayThis);
//    }
//
//
//
//
//    private void getOrders(JSONObject resultOrderObj, JSONArray arrayRelation)
//    {
//        HashSet setId_O = new HashSet();
//        for (int j = 0; j < arrayRelation.size(); j++) {
//            JSONObject myRelationObj = arrayRelation.getJSONObject(j);
//            if (resultOrderObj.getJSONObject(myRelationObj.getString("id_O")) == null || !setId_O.contains(myRelationObj.getString("id_O"))) {
//                setId_O.add(myRelationObj.getString("id_O"));
//            }
//        }
//        List<?> orders = qt.getMDContentMany(setId_O, Arrays.asList("info","oItem", "action", "casItemx", "oStock"), Order.class);
//        JSONObject result = qt.list2Obj(orders, "id");
//        resultOrderObj.putAll(result);
//    }
//
//    // in a given array, there's id_O and index, we try to find the match and return the index of that array
//    public Integer getIndex(JSONArray array, String id_O, Integer index) {
//        for (int i = 0; i < array.size(); i++) {
//            JSONObject json = array.getJSONObject(i);
//            if (id_O.equals(json.getString("id_O")) && index.equals(json.getInteger("index"))) {
//                return i;
//            }
//        }
//        return -1;
//    }
//}
