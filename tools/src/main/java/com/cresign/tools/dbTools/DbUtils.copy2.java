//package com.cresign.tools.dbTools;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.tools.enumeration.CodeEnum;
//import com.cresign.tools.enumeration.DateEnum;
//import com.cresign.tools.enumeration.ErrEnum;
//import com.cresign.tools.exception.ErrorResponseException;
//import com.cresign.tools.pojo.es.lBAsset;
//import com.cresign.tools.pojo.es.lBProd;
//import com.cresign.tools.pojo.es.lSAsset;
//import com.cresign.tools.pojo.es.lSProd;
//import com.cresign.tools.pojo.po.*;
//import com.cresign.tools.pojo.po.assetCard.AssetAStock;
//import com.cresign.tools.pojo.po.assetCard.AssetInfo;
//import com.cresign.tools.reflectTools.ApplicationContextTools;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.action.update.UpdateRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.index.query.QueryBuilder;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//import javax.script.*;
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Service
//public class DbUtils {
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Autowired
//    private RestHighLevelClient client;
//
//    @Autowired
//    private Qt qt;
//
//    @Autowired
//    private Ws ws;
//
//
//
//    /**
//     * 获取原有日志修改后发日志
//     * @author Rachel
//     * @Date 2022/05/18
//     * @Param queryBuilder es查询语句
//     * @Param sortKey 排序字段
//     * @Param jsonLog 日志修改 / 新增字段
//     * @Param jsonData 日志data修改 / 新增字段
//     * @Param logType 日志类型
//     * @Return org.elasticsearch.action.index.IndexResponse
//     * @Card
//     **/
//
//    public LogFlow getRecentLog(String id_O, Integer index, JSONObject tokData, String logType) {
//        JSONArray hits = qt.getES(logType, qt.setESFilt("id_C", tokData.getString("id_C"),"id_O", id_O, "index", index),1,1,"tmd","desc");
//        if (hits.size() == 0)
//        {
//            Order thisOrder = qt.getMDContent(id_O, Arrays.asList("oItem", "info", "action"), Order.class);
//            LogFlow newLog = new LogFlow(tokData, thisOrder.getOItem().getJSONArray("objItem").getJSONObject(index), thisOrder.getAction(),
//                    thisOrder.getInfo().getId_CB(), id_O, index, logType,
//                    "", "", 3);
//            return newLog;
//        }
//        LogFlow newestLog = qt.jsonTo(hits.getJSONObject(0), LogFlow.class);
////        newestLog.putAll(jsonLog);
//        //Setup who and when
//        newestLog.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//        newestLog.setId_U(tokData.getString("id_U"));
//        newestLog.setDep(tokData.getString("dep"));
//        newestLog.setGrpU(tokData.getString("grpU"));
//        newestLog.setWrdNU(tokData.getJSONObject("wrdNU"));
//
//        return newestLog;
//    }
//
//    public void updateListCol(QueryBuilder query, String listType, JSONObject listCol) throws IOException {
//
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        searchSourceBuilder.query(query).size(5000);
//        SearchRequest srb = new SearchRequest(listType);
//
//        srb.source(searchSourceBuilder);
//
//        SearchResponse search = client.search(srb, RequestOptions.DEFAULT);
//
//        for (SearchHit hit : search.getHits().getHits()) {
//            UpdateRequest updateRequest = new UpdateRequest();
//
//            hit.getSourceAsMap().put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//            hit.getSourceAsMap().putAll(listCol);
//
//            updateRequest.index(listType);
//            updateRequest.id(hit.getId());
//            updateRequest.doc(hit.getSourceAsMap());
//            client.update(updateRequest, RequestOptions.DEFAULT);
//
//        }
//
//    }
//
//    public void setPowerup(String id_C, JSONObject jsonUpdate) {
//        Asset asset = qt.getConfig(id_C, "a-core", Arrays.asList("powerup", "view"));
//        System.out.println("id_C=" + id_C);
//        System.out.println("asset=" + asset);
//        if (!asset.getId().equals("none")) {
//            JSONObject jsonPowerup = asset.getPowerup();
//            if (asset.getPowerup() == null) {
//                JSONObject cap = qt.setJson("total", 1024000000, "used", 0, "status", true);
//                JSONArray newView = asset.getView();
//                newView.add("powerup");
//                qt.setMDContent(asset.getId(), qt.setJson("powerup", qt.setJson("capacity", cap), "view", newView), Asset.class);
//                asset.setPowerup(qt.setJson("powerup", qt.setJson("capacity", cap)));
//
//            } else {
//                jsonPowerup = asset.getPowerup();
//                jsonPowerup.putAll(jsonUpdate);
//            }
//            qt.setMDContent(asset.getId(), qt.setJson("powerup", jsonPowerup), Asset.class);
//        }
//
//    }
//
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//    public void updateRefOP(String id_CB, String id_CS, String id_FC, String id_FS,
//                         String id_OP, String refOP, JSONObject wrdN, String id_O, Integer index, Boolean isStart)
//{
//    if (id_CB == null)
//        id_CB = "";
//    if (id_CS == null)
//        id_CS = "";
//    if (id_FC == null)
//        id_FC = "";
//    if (id_FS == null)
//        id_FS = "";
//    if (id_CB == id_CS)
//        id_CS = "";
//
//    Asset assetB = qt.getConfig(id_CB, "a-auth", "flowControl");
//    Asset assetS = qt.getConfig(id_CS, "a-auth", "flowControl");
//
//    if (id_CB == id_CS)
//        assetS = qt.cloneThis(assetB, Asset.class);
//
//    String refInside = id_O + "_" + index;
//
//    JSONObject refOPInfo = qt.setJson("refOP", refOP, "wrdN", wrdN, "index", new JSONArray(), "id_OP", id_OP);
//
//    // check and update id_FC
//    if (!id_FC.equals("") && !assetB.getId().equals("none"))
//    {
//
//        for (int i = 0; i < assetB.getFlowControl().getJSONArray("objData").size(); i++)
//        {
//            JSONObject flowInfo = assetB.getFlowControl().getJSONArray("objData").getJSONObject(i);
//            //adding 1 index
//            if (flowInfo.getString("id").equals(id_FC) && isStart && refOP != null) {
//                // case 1: id_OP not exists in refOP, init
//                if (flowInfo.getJSONObject("refOP") == null)
//                {
//                    throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
////
////                    flowInfo.put("refOP", new JSONObject());
////                    qt.errPrint("in refOPNull;");
//                }
//                // case 2: refOP not exists
//                else if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
//                {
//                    //id_OP getES, then put here wrddesc, pic, grp, id_CB, priority, etc
//                    JSONObject extraInfo = qt.getES("lSBOrder", qt.setESFilt("id_O", id_OP)).getJSONObject(0);
//                    qt.upJson(refOPInfo, "wrddesc", extraInfo.getJSONObject("wrddesc"), "pic", extraInfo.getString("pic"),"tmd", extraInfo.getString("tmd"),
//                            "grp", extraInfo.getString("grp"), "priority", extraInfo.getInteger("priority"), "id_CB", extraInfo.getString("id_CB"));
////                    qt.pushMDContent(assetS.getId(), "flowControl.objData." + i + ".refOP." + refOP, refOPInfo, Asset.class);
//
//                    flowInfo.getJSONObject("refOP").put(refOP, refOPInfo);
//                } else {
//
//                    // any case, add this index to index[]
//                    if (!flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").contains(refInside)) {
//                    flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").add(refInside);
////                        qt.pushMDContent(assetS.getId(), "flowControl.objData." + i + ".refOP." + refOP + ".index", refInside, Asset.class);
//                    }
//                }
////                qt.errPrint("idFC addIndex", flowInfo, id_FC, refOP, id_OP, refOPInfo, wrdN);
//                qt.setMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i+ ".refOP." + refOP, flowInfo.getJSONObject("refOP").getJSONObject(refOP)), Asset.class);
//                break;
//
//            } // removing now here:
//            else if (flowInfo.getString("id").equals(id_FC) && !isStart)
//            {
//
//                // case 1: if index size == 1, remove the whole id_OP
//                try {
//                    if (flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").size() == 1)
//                    {
////                        flowInfo.getJSONObject("refOP").remove(refOP);
//                        qt.pullMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP", refOP), Asset.class);
//                    } else {
//                        flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").remove(refInside);
////                        qt.pullMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP." + refOP + ".index", refInside), Asset.class);
//                        qt.setMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP." + refOP, flowInfo.getJSONObject("refOP").getJSONObject(refOP)), Asset.class);
//                    }
//                } catch (Exception e) {
//                    // here it must have an error flowcontrol
//                }
//                break;
//            }
//        }
//    }
//    if (!id_FS.equals("") && !assetS.getId().equals("none"))
//    {
//        for (int i = 0; i < assetS.getFlowControl().getJSONArray("objData").size(); i++)
//        {
//            JSONObject flowInfo = assetS.getFlowControl().getJSONArray("objData").getJSONObject(i);
//            //adding 1 index
//            if (flowInfo.getString("id").equals(id_FS) && isStart && refOP != null) {
//                // case 1: id_OP not exists in refOP, init
//                if (flowInfo.getJSONObject("refOP") == null)
//                {
//                    throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
////                    flowInfo.put("refOP", new JSONObject());
//                }
//                // case 2: refOP not exists
//                else if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
//                {
//                    //id_OP getES, then put here wrddesc, pic, grp, id_CB, priority, etc
//                    JSONObject extraInfo = qt.getES("lSBOrder", qt.setESFilt("id_O", id_OP)).getJSONObject(0);
//                    qt.upJson(refOPInfo, "wrddesc", extraInfo.getJSONObject("wrddesc"), "pic", extraInfo.getString("pic"),
//                            "grp", extraInfo.getString("grp"), "priority", extraInfo.getInteger("priority"), "id_CB", extraInfo.getString("id_CB"));
////                    flowInfo.getJSONObject("refOP").put(refOP, refOPInfo);
//                    qt.pushMDContent(assetS.getId(), "flowControl.objData." + i + ".refOP." + refOP, refOPInfo, Asset.class);
//                } else {
//                    // any case, add this index to index[]
//                    if (!flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").contains(refInside)) {
////                        flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").add(refInside);
//                        qt.pushMDContent(assetS.getId(), "flowControl.objData." + i + ".refOP." + refOP + ".index", refInside, Asset.class);
//                    }
////                    qt.setMDContent(assetS.getId(), qt.setJson("flowControl.objData." + i + ".refOP." + refOP, flowInfo.getJSONObject("refOP").getJSONObject(refOP)), Asset.class);
//                }
//                break;
//
//            } // removing now here:
//            else if (flowInfo.getString("id").equals(id_FS) && !isStart) {
//                // case 1: if index size == 1, remove the whole id_OP
//                try {
//                    if (flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").size() == 1)
//                    {
////                        flowInfo.getJSONObject("refOP").remove(refOP);
//                        qt.pullMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP", refOP), Asset.class);
//
//                    } else {
//                        flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").remove(refInside);
//                        qt.pullMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i + ".refOP." + refOP + ".index", refInside), Asset.class);
////                        qt.setMDContent(assetS.getId(), qt.setJson("flowControl.objData." + i + ".refOP." + refOP, flowInfo.getJSONObject("refOP").getJSONObject(refOP)), Asset.class);
//                    }
//                } catch (Exception e) {
//                    // here it must have an error flowcontrol
//                }
//                break;
//            }
//        }
//    }
//}
//
////use this on activateThis, StatusChg, chgDepAndFlow, make sure assetB/assetS is from Controller
////  qt.setMDContent(assetB.getId(), qt.setJson("flowControl", assetB.getJSONObject(id_CB)), Asset.class);
//    //before that I need to getFlowcontrol and save to assetB.id_C / assetS.id_C
//    public void updateRefOP2(JSONObject assetCollection, String id_CB, String id_CS, String id_FC, String id_FS,
//                            String id_OP, String refOP, JSONObject wrdN, String id_O, Integer index, Boolean isStart)
//    {
//        if(id_OP.equals("") || id_OP.equals(null))
//        {
//            return;
//        }
//
//        //use the following to get flowControl into assetB.id_CB
//        if (id_CB == null)
//            id_CB = "";
//        if (id_CS == null)
//            id_CS = "";
//        if (id_FC == null)
//            id_FC = "";
//        if (id_FS == null)
//            id_FS = "";
//        if (id_CB == id_CS)
//            id_CS = "";
//
//        if (id_CB != "" && assetCollection.getJSONObject(id_CB) == null)
//        {
//            assetCollection.put(id_CB, qt.toJson(qt.getConfig(id_CB, "a-auth", "flowControl")));
//        }
//        if (!assetCollection.getJSONObject(id_CB).getString("id").equals("none") && id_CB != "" && assetCollection.getJSONObject(id_CB).getJSONArray("indexList") == null) {
//            assetCollection.getJSONObject(id_CB).put("indexList", new JSONArray());
//        }
//
//        if (id_CS != "" && assetCollection.getJSONObject(id_CS) == null)
//        {
//            assetCollection.put(id_CS, qt.toJson(qt.getConfig(id_CS, "a-auth", "flowControl")));
//        }
//        if (!assetCollection.getJSONObject(id_CB).getString("id").equals("none") && id_CS != "" && assetCollection.getJSONObject(id_CS).getJSONArray("indexList") == null) {
//            assetCollection.getJSONObject(id_CS).put("indexList", new JSONArray());
//        }
//
//        String refInside = id_O + "_" + index;
//
//        JSONObject refOPInfo = qt.setJson("refOP", refOP, "wrdN", wrdN, "index", new JSONArray(), "id_OP", id_OP);
//
//        // check and update id_FC
//        if (!id_FC.equals("") && !assetCollection.getJSONObject(id_CB).getString("id").equals("none"))
//        {
//
//            for (int i = 0; i < assetCollection.getJSONObject(id_CB).getJSONObject("flowControl").getJSONArray("objData").size(); i++)
//            {
//                JSONObject flowInfo = assetCollection.getJSONObject(id_CB).getJSONObject("flowControl").getJSONArray("objData").getJSONObject(i);
//                //adding 1 index
//                if (flowInfo.getString("id").equals(id_FC) && isStart && refOP != null) {
//                    // case 1: id_OP not exists in refOP, init
//                    if (flowInfo.getJSONObject("refOP") == null)
//                    {
//                        flowInfo.put("refOP", new JSONObject());
//                    }
//                    // case 2: refOP not exists
//                    if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
//                    {
//                        //id_OP getES, then put here wrddesc, pic, grp, id_CB, priority, etc
//                        JSONObject extraInfo = qt.getES("lSBOrder", qt.setESFilt("id_O", id_OP)).getJSONObject(0);
//                        qt.upJson(refOPInfo, "wrddesc", extraInfo.getJSONObject("wrddesc"), "pic", extraInfo.getString("pic"),"tmd", extraInfo.getString("tmd"),
//                                "grp", extraInfo.getString("grp"), "priority", extraInfo.getInteger("priority"), "id_CB", extraInfo.getString("id_CB"));
//
//                        flowInfo.getJSONObject("refOP").put(refOP, refOPInfo);
//                    } else {
//
//                        // any case, add this index to index[]
//                        if (!flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").contains(refInside)) {
//                            flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").add(refInside);
//                        }
//                    }
//                    if (!assetCollection.getJSONObject(id_CB).getJSONArray("indexList").contains(i)) {
//                        assetCollection.getJSONObject(id_CB).getJSONArray("indexList").add(i);
//                    }
//                    break;
//
//                } // removing now here:
//                else if (flowInfo.getString("id").equals(id_FC) && !isStart)
//                {
//
//                    // case 1: if index size == 1, remove the whole id_OP
//                        if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
//                        {
//                            break;
//                        }
//                        else if (flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").size() == 1)
//                        {
//                            flowInfo.getJSONObject("refOP").remove(refOP);
//                        } else {
//                            flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").remove(refInside);
//                        }
//                    if (!assetCollection.getJSONObject(id_CB).getJSONArray("indexList").contains(i)) {
//                        assetCollection.getJSONObject(id_CB).getJSONArray("indexList").add(i);
//                    }
//                    break;
//                }
//            }
//        }
//        if (!id_FS.equals("") && !assetCollection.getJSONObject(id_CS).getString("id").equals("none"))
//        {
//            for (int i = 0; i < assetCollection.getJSONObject(id_CS).getJSONObject("flowControl").getJSONArray("objData").size(); i++)
//            {
//                JSONObject flowInfo = assetCollection.getJSONObject(id_CS).getJSONObject("flowControl").getJSONArray("objData").getJSONObject(i);
//                //adding 1 index
//                if (flowInfo.getString("id").equals(id_FS) && isStart && refOP != null) {
//                    // case 1: id_OP not exists in refOP, init
//                    if (flowInfo.getJSONObject("refOP") == null)
//                    {
//                        throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
////                    flowInfo.put("refOP", new JSONObject());
//                    }
//                    // case 2: refOP not exists
//                    else if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
//                    {
//                        //id_OP getES, then put here wrddesc, pic, grp, id_CB, priority, etc
//                        JSONObject extraInfo = qt.getES("lSBOrder", qt.setESFilt("id_O", id_OP)).getJSONObject(0);
//                        qt.upJson(refOPInfo, "wrddesc", extraInfo.getJSONObject("wrddesc"), "pic", extraInfo.getString("pic"),
//                                "grp", extraInfo.getString("grp"), "priority", extraInfo.getInteger("priority"), "id_CB", extraInfo.getString("id_CB"));
//                        flowInfo.getJSONObject("refOP").put(refOP, refOPInfo);
//                    } else {
//                        // any case, add this index to index[]
//                        if (!flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").contains(refInside)) {
//                        flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").add(refInside);
//                        }
//                    }
//                    if (!assetCollection.getJSONObject(id_CS).getJSONArray("indexList").contains(i)) {
//                        assetCollection.getJSONObject(id_CS).getJSONArray("indexList").add(i);
//                    }
//                    break;
//
//                } // removing now here:
//                else if (flowInfo.getString("id").equals(id_FS) && !isStart) {
//                    // case 1: if index size == 1, remove the whole id_OP
//                        if (flowInfo.getJSONObject("refOP").getJSONObject(refOP) == null)
//                        {
//                            break;
//                        }
//                        else if (flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").size() == 1)
//                        {
//                            flowInfo.getJSONObject("refOP").remove(refOP);
//                        } else {
//                            flowInfo.getJSONObject("refOP").getJSONObject(refOP).getJSONArray("index").remove(refInside);
//                        }
//                        if (!assetCollection.getJSONObject(id_CS).getJSONArray("indexList").contains(i)) {
//                            assetCollection.getJSONObject(id_CS).getJSONArray("indexList").add(i);
//                        }
//                    break;
//                }
//            }
//        }
//    }
//
//    public void setMDRefOP(JSONObject assetCollection)
//    {
//        List<JSONObject> listBulk = new ArrayList<>();
//
//        for (String compId : assetCollection.keySet() )
//        {
//            if (!assetCollection.getJSONObject(compId).getString("id").equals("none")) {
//                for (int i = 0; i < assetCollection.getJSONObject(compId).getJSONArray("indexList").size(); i++)
//                {
////                    String refOP = assetCollection.getJSONObject(compId).getJSONArray("refList").getString(i);
//                    Integer index = assetCollection.getJSONObject(compId).getJSONArray("indexList").getInteger(i);
//
//                    JSONObject update = new JSONObject();
//                    update.put("flowControl.objData." + index + ".refOP",
//                    assetCollection.getJSONObject(compId).getJSONObject("flowControl").getJSONArray("objData")
//                    .getJSONObject(index).getJSONObject("refOP"));
//
//                    listBulk.add(qt.setJson("type", "update", "id", assetCollection.getJSONObject(compId).getString("id"), "update", update));
//                }
////                qt.setMDContent(assetB.getId(), qt.setJson("flowControl.objData." + i+ ".refOP." + refOP, flowInfo.getJSONObject("refOP").getJSONObject(refOP)), Asset.class);
////                qt.setMDContent(assetCollection.getJSONObject(compId).getString("id"), qt.setJson("flowControl",
////                        assetCollection.getJSONObject(compId).getJSONObject("flowControl")), Asset.class);
//            }
//        }
//        if (listBulk.size() > 0) {
//            qt.setMDContentMany(listBulk, Asset.class);
//        }
//
//
//    }
//
////Order init oItem, action, oStock if needed
//
//    public JSONObject checkCard(Order order)
//    {
//        JSONObject jsonCard = new JSONObject();
//        JSONArray arrayCard = null;
//        JSONArray arrayOItem = null;
//        JSONArray arrayArrP = null;
//        JSONArray arrayData = null;
//        System.out.println("checking Card");
//
//            arrayOItem = order.getOItem().getJSONArray("objItem");
//            if (order.getOItem().getString("bmdHeight") == null)
//            {    order.getOItem().put("bmdHeight", "unset"); }
//            if ( order.getOItem().getJSONArray("objCard") == null)
//            {
//                order.getOItem().put("objCard", new JSONArray());
//                order.getOItem().getJSONArray("objCard").add("oItem");
//
//                if (order.getOStock() != null)
//                {
//                    order.getOItem().getJSONArray("objCard").add("oStock");
//                }
//                if (order.getAction() != null) {
//                    order.getOItem().getJSONArray("objCard").add("action");
//                }
//                if (order.getOQc() != null) {
//                    order.getOItem().getJSONArray("objCard").add("oQc");
//                }
//            }
//            if (order.getOItem().getJSONArray("arrP") == null)
//            {
//                order.getOItem().put("arrP", new JSONArray());
//                for (int i = 0; i < arrayOItem.size(); i++)
//                {
//                    order.getOItem().getJSONArray("arrP").add(arrayOItem.getJSONObject(i).getString("id_P"));
//                }
//            }
//
//            if (order.getOItem().getJSONArray("objItemCol") == null)
//             {
//                 order.getOItem().put("objItemCol", qt.getInitData("cn").getList().getJSONArray("objItemCol"));
//    //             System.out.println(qt.getInitData("cn").getList());
//             }
//
//            arrayCard = order.getOItem().getJSONArray("objCard");
//            arrayArrP = order.getOItem().getJSONArray("arrP");
//
//        if (arrayCard == null || arrayOItem == null || arrayArrP == null || arrayOItem.size() != arrayArrP.size()) {
//            System.out.println("checkCard, null @ 553");
//
//            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
//        }
//
//        // for each "card" in objCard: check if "json data" actually has data
//        for (int n = 0; n < arrayCard.size(); n++)
//        {
//                switch (arrayCard.getString(n)) {
//                    case "oStock":
//                        if (order.getOStock() == null)
//                        {
//                            //whole card Init
//                            order.setOStock(this.initOStock(order.getOItem().getJSONArray("objItem")));
//                            order.getView().add("oStock");
//                        }
//                        // Check if all oItem has a oStock, if not, init it
//                        for (int i = 0; i < arrayOItem.size(); i++)
//                        {
//                            if (order.getOStock().getJSONArray("objData").getJSONObject(i) == null)
//                            {
//                                // just init 1 oitem
//                                order.getOStock().put("objData", this.initOStock(arrayOItem.getJSONObject(i), order.getOStock().getJSONArray("objData"),i));
//                            }
//                            if (order.getOStock().getJSONArray("objData").getJSONObject(i).getDouble("wn2qtynowS") == null)
//                            {
//                                order.getOStock().getJSONArray("objData").getJSONObject(i).put("wn2qtynowS", 0.0);
//                                order.getOStock().getJSONArray("objData").getJSONObject(i).put("wn2qtyship", 0.0);
//                                order.getOStock().getJSONArray("objData").getJSONObject(i).put("wn2qtyshipnow", 0.0);
//
//                            }
//                        }
//                        arrayData = order.getOStock().getJSONArray("objData");
//
//                        break;
//                    case "action":
//                        if (order.getAction() == null)
//                        {
//                            order.setAction(this.initAction(order.getOItem().getJSONArray("objItem")));
//                            order.getView().add("action");
//                        }
//                        // Check if all oItem has a action, if not, init it
//                        for (int i = 0; i < arrayOItem.size(); i++)
//                        {
//                            this.initAction(arrayOItem.getJSONObject(i), order.getAction().getJSONArray("objAction"),i);
//                        }
//                        arrayData = order.getAction().getJSONArray("objAction");
//                        break;
//                    case "oQc":
//                        if (null == order.getOQc()) {
//                            order.setOQc(this.initOQc(order.getOItem().getJSONArray("objItem")));
//                            order.getView().add("oQc");
//                        }
//                        for (int i = 0; i < arrayOItem.size(); i++) {
//                            if (null == order.getOQc().getJSONArray("objQc").getJSONObject(i)) {
//                                this.initOQc(arrayOItem.getJSONObject(i),order.getOQc().getJSONArray("objQc"),i);
//                            }
//                        }
//                        break;
//                }
//                jsonCard.put(arrayCard.getString(n), arrayData);
//        }
//        // this will return an JSONObject with "oStock":{...}, "action":{...}
//        return jsonCard;
//    }
//
//    public JSONObject summOrder(Order order, JSONObject listCol) {
//        return this.summOrder(order, listCol, qt.setArray("oStock", "action", "oQc"), null);
//    }
//
//
//    public void updateOrder(Order order)
//    {
//        try {
//        JSONObject listCol = new JSONObject();
//        this.summOrder(order, listCol);
//        qt.setMDContent(order.getId(), qt.setJson("view", order.getView(), "info", order.getInfo(), "oItem", order.getOItem(),
//                "oStock", order.getOStock(), "action", order.getAction()), Order.class);
//        qt.setES("lSBOrder", qt.setESFilt("id_O", order.getId()), listCol);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//    public void addOrder(Order order, JSONObject listCol)
//    {
//        try {
//            this.summOrder(order, listCol);
//            qt.addMD(order);
//            qt.addES("lSBOrder", listCol);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public JSONObject summOrder(Order order, JSONObject listCol, JSONArray cardList)
//    {
//        return this.summOrder(order, listCol, cardList, null);
//    }
//
//
//    public JSONObject summOrder(Order order, JSONObject listCol, JSONArray cardList, JSONArray arrayIndex) {
//
//        if (order.getOItem() == null)
//            this.initOItem(order);
////            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
//
//        JSONArray oItem = order.getOItem().getJSONArray("objItem");
//        JSONArray oStock = null;
//        JSONArray action = null;
//        JSONArray oQc = null;
//
//        Double wn2fin = 0.0;
//        Double wn2made = 0.0;
//        Double wn2progress = 0.0;
//        Double wn2qtyship = 0.0;
//        Integer count = 0;
//        Double wn2qty = 0.0;
//        Double wn4price = 0.0;
//        JSONArray arrP = new JSONArray();
//
//        if (order.getOItem().getJSONArray("objCard").contains("oStock") && order.getOStock() != null)
//        {
//            oStock = order.getOStock().getJSONArray("objData");
//        }
//        if (order.getOItem().getJSONArray("objCard").contains("action") && order.getAction() != null)
//        {
//            action = order.getAction().getJSONArray("objAction");
//        }
//        if (order.getOItem().getJSONArray("objCard").contains("oQc") && null != order.getOQc())
//        {
//            oQc = order.getOQc().getJSONArray("objQc");
//        }
//
//        for (int i = 0; i < oItem.size(); i++)
//        {
//            wn2qty = DoubleUtils.add(oItem.getJSONObject(i).getDouble("wn2qtyneed"), wn2qty);
//
//            if (oItem.getJSONObject(i).getDouble("wn4price") == null)
//                oItem.getJSONObject(i).put("wn4price", 0.0);
//
//            wn4price = DoubleUtils.add(wn4price, DoubleUtils.multiply(oItem.getJSONObject(i).getDouble("wn2qtyneed"),oItem.getJSONObject(i).getDouble("wn4price")));
//            arrP.add(oItem.getJSONObject(i).getString("id_P"));
//
//            // setup wn0prior by using seq***
//            // if seq == 0, wn0prior = 0, seq == 1, wn0prior = prevPrior, seq == 2, wn0prior = prevPrior + 1, seq == 3, no set
//            if (i == 0 || oItem.getJSONObject(i).getString("seq").equals("0"))
//            {
//                oItem.getJSONObject(i).put("wn0prior", 0);
//            } else if (oItem.getJSONObject(i).getString("seq").equals("1")) {
//                oItem.getJSONObject(i).put("wn0prior", oItem.getJSONObject(i - 1).getInteger("wn0prior"));
//            } else if (oItem.getJSONObject(i).getString("seq").equals("2")) {
//                oItem.getJSONObject(i).put("wn0prior", oItem.getJSONObject(i - 1).getInteger("wn0prior") + 1);
//            }
//            oItem.getJSONObject(i).put("index", i);
//
//            if (oStock != null && cardList.contains("oStock"))
//            {
//                if (oStock.size() <= i || oStock.getJSONObject(i) == null)
//                {
//                    oStock = this.initOStock(oItem.getJSONObject(i),oStock, i);
//                }
//
//                Double madePercent = DoubleUtils.divide(oStock.getJSONObject(i).getDouble("wn2qtymade"),oItem.getJSONObject(i).getDouble("wn2qtyneed"));
//                wn2made = DoubleUtils.add(wn2made, madePercent);
//                if (oStock.getJSONObject(i).getDouble("wn2qtyship") != null)
//                {
//                    wn2qtyship = DoubleUtils.add(wn2made, oStock.getJSONObject(i).getDouble("wn2qtyship"));
//                }
//                oStock.getJSONObject(i).put("index", i);
////                "rKey" 《=Oitem
//
//                if (action != null)
//                {
//                    for (int j = 0; j < action.getJSONObject(i).getJSONArray("upPrnts").size(); j++)
//                    {
//                        if (oStock.getJSONObject(i).getJSONArray("objShip").size() - 1 < j)
//                        {
//                            // init it if it is not init yet, if bmdpt == 1 it is process, process only need 1 objShip[0]
//                            if (j == 0 || !action.getJSONObject(i).getInteger("bmdpt").equals(1)) {
//                                JSONObject newObjShip = qt.setJson(
//                                        "wn2qtynow", 0.0, "wn2qtymade", 0.0,
//                                        "wn2qtyneed", oItem.getJSONObject(i).getDouble("wn2qtyneed"));
//                                oStock.getJSONObject(i).getJSONArray("objShip").add(newObjShip);
//                            }
//                        }
//                    }
//                }
//            }
//            if (action != null && cardList.contains("action"))
//            {
//                if (action.size() <= i || action.getJSONObject(i) == null)
//                {
//                    this.initAction(oItem.getJSONObject(i),action, i);
//                }
//                count = count + action.getJSONObject(i).getInteger("bcdStatus") == 2 ? 1: 0;
//                action.getJSONObject(i).put("index", i);
//
//                String grp = oItem.getJSONObject(i).getString("grp");
//                String grpB = oItem.getJSONObject(i).getString("grpB");
//
//                // if grp not exists, need to init grpGroup
//                if (grp != null && !grp.equals("") && order.getAction().getJSONObject("grpGroup").getJSONObject(grp) == null)
//                {
//                    Asset asset = qt.getConfig(oItem.getJSONObject(i).getString("id_C"),"a-auth","def.objlSP."+grp);
//                    //sales side getlSProd, and set default values
//                    System.out.println("getGrp"+asset.getId());
//
//                    if (!asset.getId().equals("none")) {
//                        JSONObject grpData = asset.getDef().getJSONObject("objlSP").getJSONObject(grp) == null? new JSONObject() : asset.getDef().getJSONObject("objlSP").getJSONObject(grp);
//                        order.getAction().getJSONObject("grpGroup").put(grp, grpData);
//                    }
//                }
//                if (grpB != null && !grpB.equals("") && order.getAction().getJSONObject("grpBGroup").getJSONObject(grpB) == null)
//                {
//
//                    Asset asset = qt.getConfig(order.getInfo().getId_CB(),"a-auth","def.objlBP."+grpB);
//                    System.out.println("getGrpB"+asset.getId());
//
//                    //sales side getlSProd, and set default values
//                    if (!asset.getId().equals("none")) {
//                        JSONObject grpData = asset.getDef().getJSONObject("objlBP").getJSONObject(grpB) == null ? new JSONObject() : asset.getDef().getJSONObject("objlBP").getJSONObject(grpB);
//                        order.getAction().getJSONObject("grpBGroup").put(grpB, grpData);
//                    }
//                }
//            }
//            if (null != oQc && cardList.contains("oQc")){
//                System.out.println("oQc");
//            }
//        }
//        wn2fin = wn2made; //DoubleUtils.divide(wn2made, oItem.size());
//        wn2progress = DoubleUtils.divide(count, oItem.size());
//        qt.upJson(listCol, "wn2fin", wn2fin, "wn2progress", wn2progress, "wn2qty", wn2qty, "wn4price", wn4price, "arrP", arrP, "wn2qtyship", wn2qtyship);
//
//        order.getOItem().put("wn2qty", wn2qty);
//        order.getOItem().put("wn4price", wn4price);
//        order.getOItem().put("arrP", arrP);
//
//        JSONObject result = new JSONObject();
//        result.put("oItem", order.getOItem());
//        result.put("view", order.getView());
//        if (oStock != null && cardList.contains("oStock")) {
//            order.getOStock().put("wn2fin", wn2fin);
//            order.getOStock().put("wn2qtyship", wn2qtyship);
//
//            result.put("oStock", order.getOStock());
//        }
//        if (action != null && cardList.contains("action")) {
//            result.put("action", order.getAction());
//            order.getAction().put("wn2progress", wn2progress);
//        }
//        if (null != oQc && cardList.contains("oQc")) {
//            result.put("oQc", order.getOQc());
//        }
//
////        if (order.getOTrigger() != null && order.getOTrigger().getJSONArray("objData") != null) {
////            JSONArray arrayTrigger = order.getOTrigger().getJSONArray("objData");
////            for (int i = 0; i < arrayTrigger.size(); i++) {
////                JSONObject jsonTrigger = arrayTrigger.getJSONObject(i);
////                JSONArray arrayIf = jsonTrigger.getJSONArray("objIf");
////                JSONObject jsonExecs = jsonTrigger.getJSONObject("objExec");
////                JSONObject jsonVars = jsonTrigger.getJSONObject("objVar");
////                String[] refSplit = jsonTrigger.getString("ref").split("##");
////                //es字段
////                if ("es".equals(refSplit[0])) {
////                    jsonVars.put(refSplit[1], listCol.get(refSplit[1]));
////                }
////                else if (cardList.contains(refSplit[0])) {
////                    JSONArray array = null;
////                    if ("oItem".equals(refSplit[0])) {
////                        array = order.getOItem().getJSONArray("objItem");
////                    } else if ("action".equals(refSplit[0])) {
////                        array = order.getAction().getJSONArray("objAction");
////                    } else if ("oStock".equals(refSplit[0])) {
////                        array = order.getOStock().getJSONArray("objData");
////                    }
////                    //有标注修改的oItem
////                    if (arrayIndex != null) {
////                        for (int j = 0; j < arrayIndex.size(); j++) {
////                            Integer index = arrayIndex.getInteger(j);
////                            jsonVars.put(refSplit[1], array.getJSONObject(index).get(refSplit[1]));
////                            for (int k = 0; k < arrayIf.size(); k++) {
////                                JSONObject jsonIf = arrayIf.getJSONObject(k);
////                                //修改的oItem需要trigger
////                                if (jsonIf.getJSONArray("index").contains(index)) {
////                                    scriptEngineIf(jsonIf, jsonVars, jsonExecs);
////                                }
////                            }
////                        }
////                    }
////                    else {
////                        for (int j = 0; j < array.size(); j++) {
////                            jsonVars.put(refSplit[1], array.getJSONObject(j).get(refSplit[1]));
////                            for (int k = 0; k < arrayIf.size(); k++) {
////                                JSONObject jsonIf = arrayIf.getJSONObject(k);
////                                //修改的oItem需要trigger
////                                if (jsonIf.getJSONArray("index").contains(j)) {
////                                    scriptEngineIf(jsonIf, jsonVars, jsonExecs);
////                                }
////                            }
////                        }
////                    }
////                }
////            }
////        }
//
//        if (order.getOTrigger() != null) {
//            JSONObject jsonTrigger = order.getOTrigger();
//            JSONArray arrayIf = jsonTrigger.getJSONArray("objIf");
//            JSONObject jsonExecs = jsonTrigger.getJSONObject("objExec");
//            JSONObject jsonVars = jsonTrigger.getJSONObject("objVar");
//            JSONObject jsonOrder = new JSONObject();
//            if (cardList.contains("oItem")) {
//                jsonOrder.put("oItem", order.getOItem().getJSONArray("objItem"));
//            }
//            if (cardList.contains("action")) {
//                jsonOrder.put("action", order.getAction().getJSONArray("objAction"));
//            }
//            if (cardList.contains("oStock")) {
//                jsonOrder.put("oStock", order.getOStock().getJSONArray("objData"));
//            }
//            jsonVars.forEach((k, v) ->{
//                String var = jsonVars.getString(k);
//                if (var.startsWith("##OI")) {
//                    String substring = var.substring(5);
//                    String[] split = substring.split("##");
//                    if ("es".equals(split[0])) {
//                        jsonVars.put(k, listCol.get(split[1]));
//                    }
//                }
//            });
//            //有标注修改的oItem
//            if (arrayIndex != null) {
//                for (int j = 0; j < arrayIndex.size(); j++) {
//                    Integer index = arrayIndex.getInteger(j);
//                    jsonVars.forEach((k, v) ->{
//                        String var = jsonVars.getString(k);
//                        if (var.startsWith("##OI")) {
//                            String substring = var.substring(5);
//                            String[] split = substring.split("##");
//                            if (jsonOrder.getJSONArray(split[0]) != null) {
//                                jsonVars.put(k, jsonOrder.getJSONArray(split[0]).getJSONObject(index).get(split[1]));
//                            }
//                        }
//                    });
//                    for (int k = 0; k < arrayIf.size(); k++) {
//                        JSONObject jsonIf = arrayIf.getJSONObject(k);
//                        //修改的oItem需要trigger
//                        JSONArray arrayActivate = jsonIf.getJSONArray("activate");
//                        for (int i = 0; i < arrayActivate.size(); i++) {
//                            //需要检查该index
//                            if (index == jsonIf.getJSONArray("index").getInteger(i) && arrayActivate.getBoolean(i)) {
//                                jsonIf.put("id_O", order.getId());
//                                jsonIf.put("ifIndex", k);
//                                jsonIf.put("oIndex", index);
//                                oTriggerIf(jsonIf, jsonVars, jsonExecs);
//                            }
//                        }
//                    }
//                }
//            }
//            else {
//                JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
//                for (int j = 0; j < arrayOItem.size(); j++) {
//                    Integer index = j;
//                    jsonVars.forEach((k, v) ->{
//                        String var = jsonVars.getString(k);
//                        if (var.startsWith("##OI")) {
//                            String substring = var.substring(5);
//                            String[] split = substring.split("##");
//                            if (jsonOrder.getJSONArray(split[0]) != null) {
//                                jsonVars.put(k, jsonOrder.getJSONArray(split[0]).getJSONObject(index).get(split[1]));
//                            }
//                        }
//                    });
//                    for (int k = 0; k < arrayIf.size(); k++) {
//                        JSONObject jsonIf = arrayIf.getJSONObject(k);
//                        //修改的oItem需要trigger
//                        JSONArray arrayActivate = jsonIf.getJSONArray("activate");
//                        for (int i = 0; i < arrayActivate.size(); i++) {
//                            //需要检查该index
//                            if (index == jsonIf.getJSONArray("index").getInteger(i) && arrayActivate.getBoolean(i)) {
//                                oTriggerIf(jsonIf, jsonVars, jsonExecs);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return result;
//
//    }
//
//
//    /////////////////////////////////////////////////////////////////////////////////////
//
//
////     SE if / info / exec (oTrig, cTrig, Summ00s, tempa
//    public void oTriggerIf(JSONObject jsonObjIf, JSONObject jsonObjVar, JSONObject jsonObjExec) {
//        try {
//            String script = jsonObjIf.getString("script");
//            String id_O = jsonObjIf.getString("id_O");
//            Integer ifIndex = jsonObjIf.getInteger("ifIndex");
//            Integer oIndex = jsonObjIf.getInteger("oIndex");
//            if (script.startsWith("##")) {
////                ##op14##function fun(op14) {
////                    if(op14.wn2qtynow > 12000) {
////                        return "exec1";
////                    }
////                }
////                fun(op14)
//                String[] scriptSplit = script.split("##");
//                System.out.println("\n\n\nscriptSplit=");
//                for (int i = 0; i < scriptSplit.length; i++) {
//                    System.out.println(i + ":" + scriptSplit[i]);
//                }
//                String valueFor = scriptSplit[1];
//                String scriptSubString = scriptSplit[2];
//                System.out.println("valueFor" + valueFor + ",scriptSubString=" + scriptSubString);
//                if (!scriptSubString.contains("for")) {
//                    Object varFor = scriptEngineVar(jsonObjVar.getString(valueFor), jsonObjVar);
//                    JSONArray arrayValue = (JSONArray) JSON.toJSON(varFor);
//                    for (int i = 0; i < arrayValue.size(); i++) {
//                        JSONObject value = arrayValue.getJSONObject(i);
//                        System.out.println(value);
//                        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
//                        Compilable compilable = (Compilable) scriptEngine;
//                        Bindings bindings = scriptEngine.createBindings();
//                        CompiledScript compiledScript = compilable.compile(scriptSubString);
//                        //从script获取参数列表
//                        String[] scriptValue = script.split("\\(");
//                        System.out.println(scriptValue[0] + "," + scriptValue[1]);
//                        if (scriptValue[1].split("\\)").length > 0) {
//                            String scriptVar = scriptValue[1].split("\\)")[0];
//                            System.out.println("scriptVar=" + scriptVar);
//                            if (scriptVar != null && !scriptVar.equals("")) {
//                                String[] arrayKey = scriptVar.split(",");
//                                //传参
//                                for (int k = 0; k < arrayKey.length; k++) {
//                                    String key = arrayKey[k];
//                                    System.out.println("key=" + key);
//                                    if (valueFor.equals(key)) {
//                                        bindings.put(key, value);
//                                    } else {
//                                        Object var = scriptEngineVar(jsonObjVar.getString(key), jsonObjVar);
//                                        bindings.put(key, var);
//                                    }
//                                }
//                            }
//                        }
//
//                        String scriptResult = String.valueOf(compiledScript.eval(bindings));
//                        System.out.println("Result=" + scriptResult);
//                        JSONArray arrayObjExec = jsonObjExec.getJSONArray(scriptResult);
//                        //KEV WHY? in If you call Exec directly?
//                        //Need to redefine how "Array of lBUser works"
//                        if (arrayObjExec != null) {
//                            JSONObject jsonObjVarClone = qt.cloneObj(jsonObjVar);
//                            jsonObjVarClone.put(valueFor, value);
//                            System.out.println("===");
//                            System.out.println(value);
//                            System.out.println("===");
//                            this.scriptEngineExec(arrayObjExec, jsonObjVarClone);
//                            JSONObject jsonUpdate = qt.setJson("oTrigger.objIf." + ifIndex + ".activate." + oIndex, false);
//                            qt.setMDContent(id_O, jsonUpdate, Order.class);
//                        }
//                    }
//                }
//            } else {
//                // Stop any for or other script illegal text, I can use a cn_java to do a map
//                if (!script.contains("for")) {
//                    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
//                    Compilable compilable = (Compilable) scriptEngine;
//                    Bindings bindings = scriptEngine.createBindings();
//                    CompiledScript compiledScript = compilable.compile(script);
//                    //从script获取参数列表
//                    String[] scriptSplit = script.split("\\(");
//                    System.out.println(scriptSplit[0] + "," + scriptSplit[1]);
//                    if (scriptSplit[1].split("\\)").length > 0) {
//                        String scriptVar = scriptSplit[1].split("\\)")[0];
//                        System.out.println("scriptVar=" + scriptVar);
//                        if (scriptVar != null && !scriptVar.equals("")) {
//                            String[] arrayKey = scriptVar.split(",");
//                            //传参
//                            for (int k = 0; k < arrayKey.length; k++) {
//                                String key = arrayKey[k];
//                                System.out.println("key=" + key);
//                                Object var = this.scriptEngineVar(jsonObjVar.getString(key), jsonObjVar);
//                                bindings.put(key, var);
//                            }
//                        }
//                    }
//                    //This if statement will always return a String, and never bool...
//                    String scriptResult = String.valueOf(compiledScript.eval(bindings));
//                    System.out.println("Result=" + scriptResult);
//                    JSONArray arrayObjExec = jsonObjExec.getJSONArray(scriptResult);
//                    if (arrayObjExec != null) {
//                        this.scriptEngineExec(arrayObjExec, jsonObjVar);
//                        JSONObject jsonUpdate = qt.setJson("oTrigger.objIf." + ifIndex + ".activate." + oIndex, false);
//                        qt.setMDContent(id_O, jsonUpdate, Order.class);
//                    }
//                }
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), null);
//        }
//    }
//
//    //KEY method
//    public void scriptEngineExec(JSONArray arrayExec, JSONObject jsonVars) {
//        try {
//            for (int i = 0; i < arrayExec.size(); i++) {
//                JSONObject jsonExec = arrayExec.getJSONObject(i);
//                String method = jsonExec.getString("method");
//                System.out.println("method=" + method);
//                JSONObject jsonParams = new JSONObject();
//                JSONObject jsonStruct = jsonExec.getJSONObject("pStruct");
//                String id_I = jsonStruct.getString("id_I");
//                String structKey = jsonStruct.getString("key");
//                String structVal = jsonStruct.getString("val");
//
//                //Let id_I to fillup all variables and then change by the ExecVars
//                if (!id_I.isEmpty()) {
//                    Info info = qt.getMDContent(id_I, "jsonInfo", Info.class);
//                    JSONObject jsonInfo = info.getJsonInfo().getJSONObject("objData");
//                    String[] valSplit = structKey.split("\\.");
//                    if (structVal.equals("")) {
//                        if (structKey.equals("")) {
//                            jsonParams = jsonInfo;
//                        } else if (valSplit.length == 1) {
//                            jsonParams = jsonInfo.getJSONObject(valSplit[0]);
//                        } else if (valSplit.length == 2) {
//                            jsonParams = jsonInfo.getJSONObject(valSplit[0]).getJSONObject(valSplit[1]);
//                        }
//                    } else {
//                        if (structKey.equals("")) {
//                            jsonParams.put(structVal, jsonInfo);
//                        } else if (valSplit.length == 1) {
//                            jsonParams.put(structVal, jsonInfo.getJSONObject(valSplit[0]));
//                        } else if (valSplit.length == 2) {
//                            jsonParams.put(structVal, jsonInfo.getJSONObject(valSplit[0]).getJSONObject(valSplit[1]));
//                        }
//                    }
//                }
//                //just break down params
//                JSONArray arrayParams = qt.cloneArr(jsonExec.getJSONArray("params"));
//                for (int j = 0; j < arrayParams.size(); j++) {
//                    JSONObject jsonParam = arrayParams.getJSONObject(j);
//                    String key = jsonParam.getString("key");
//                    String dataType = jsonParam.get("val").getClass().getSimpleName();
//                    if (dataType.equals("String")) {
//                        String val = jsonParam.getString("val");
//                        if (val.startsWith("##OP")) {
//                            String[] valSplit = val.split("\\.");
//                            System.out.println("paramSplit.length=" + valSplit.length);
//                            for (int k = 0; k < valSplit.length; k++) {
//                                System.out.print(k + ":" + valSplit[k] + ",");
//                            }
//                            JSONObject jsonVar = jsonVars.getJSONObject(valSplit[1]);
//                            System.out.println("\njsonVar=" + jsonVar);
//                            if (valSplit.length == 2) {
//                                jsonParam.put("val", jsonVar.get("val"));
//                            } else if (valSplit.length == 3) {
//                                jsonParam.put("val", jsonVar.getJSONObject("val").get(valSplit[2]));
//                            }
////                            String retType = jsonVar.getString("retType");
////                            if (retType.equals("String")) {
////                                String var = jsonVar.getString("val");
////                                System.out.println("var=" + var);
////                                jsonParam.put("val", var);
////                            } else if (retType.equals("json")) {
////                                JSONObject var = jsonVar.getJSONObject("val");
////                                System.out.println("var=" + var);
////                                if (valSplit.length == 2) {
////                                    jsonParam.put("val", var);
////                                } else if (valSplit.length > 2) {
////                                    jsonParams.put("val", var.getJSONObject(valSplit[2]));
////                                }
////                            }
//                        }
//                        else if (val.startsWith("##")) {
//                            this.scriptEngineVar(val, jsonVars);
//                        }
//                    }
//                    String[] keySplit = key.split("\\.");
//                    System.out.println("keySplit.length=" + keySplit.length);
//                    System.out.println("jsonParam=" + jsonParam);
//                    Object val = jsonParam.get("val");
//                    if (key.equals("")) {
//                        jsonParams.putAll(jsonParam.getJSONObject("val"));
//                    } else if (keySplit.length == 1) {
//                        jsonParams.put(key, val);
//                    } else if (keySplit.length == 2) {
//                        if (jsonParams.getJSONObject(keySplit[0]) != null) {
//                            jsonParams.getJSONObject(keySplit[0]).put(keySplit[1], val);
//                        } else {
//                            JSONObject json = qt.setJson(keySplit[1], val);
//                            jsonParams.put(keySplit[0], json);
//                        }
//                    }
//                }
//
//                //Finished calculating ALL params
//                System.out.println("jsonParams=" + jsonParams);
//
//                //ALL method must be in com.cresign.timer, else just send out a log etc, addES..
//                if (method.startsWith("com.cresign")) {
//                    //调用方法
//                    String[] methodSplit = method.split("##");
//                    Class<?> clazz = Class.forName(methodSplit[0]);
//                    Object bean = ApplicationContextTools.getBean(clazz);
//                    Method method1 = clazz.getMethod(methodSplit[1], new Class[]{JSONObject.class});
//
//                    //Key!! invoke here with bean + params
//                    JSONObject jsonOutput = jsonExec.getJSONObject("output");
//                    if (jsonOutput != null) {
//                        Object invoke = method1.invoke(bean, jsonParams);
//                        String key = jsonOutput.getString("key");
//                        String type = jsonExec.getString("type");
//                        String valType = jsonOutput.getString("valType");
//                        JSONObject jsonVar = qt.setJson("type", type, "valType", valType, "val", invoke);
//                        jsonVars.put(key, jsonVar);
//                        System.out.println("jsonVars99=" + jsonVars);
//                    } else {
//                        method1.invoke(bean, jsonParams);
//                    }
//                } else {
//                    //发日志
//                    //Else, send log in "method" logFlow
//                    jsonParams.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//                    qt.addES(method, jsonParams);
////                logUtil.sendLogByFilebeat(method, jsonParams);
////                LogFlow log = JSONObject.parseObject(JSON.toJSONString(jsonParams),LogFlow.class);
////                wsClient.sendLogWS(log);
//                }
//            }
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    public Object scriptEngineVar(String var, JSONObject jsonVars) {
//        try {
//            System.out.println("");
//            System.out.println("var=" + var);
//            System.out.println("jsonVars=" + jsonVars);
//            if (var.startsWith("##")) {
//                //Get from card
//                if (var.startsWith("##C")) {
//                    if (var.startsWith("##CC")) {
//                        String varSubstring = var.substring(5);
//                        String[] varSplits = varSubstring.split("\\$\\$");
//                        StringBuffer sb = new StringBuffer();
//                        for (int i = 0; i < varSplits.length; i++) {
//                            String varSplit = varSplits[i];
//                            if (varSplit.startsWith("##")) {
//                                String key = varSplit.substring(2);
//                                qt.errPrint("sev", jsonVars.getString(key), jsonVars, varSplit);
//                                Object result = this.scriptEngineVar(jsonVars.getString(key), jsonVars);
//                                System.out.println("##CC=" + key + ":" + result);
//                                if (qt.toJson(result) != null && qt.toJson(result).getString("cn") != null)
//                                {
//                                    result = qt.toJson(result).getString("cn");
//                                }
//                                sb.append(result);
//                            } else {
//                                sb.append(varSplit);
//                            }
//                        }
//                        return sb.toString();
//                    }
//
//                    String[] scriptSplit = var.split("\\.");
//                    Query query = new Query(new Criteria("_id").is(scriptSplit[2]));
//                    List list = new ArrayList();
//                    //See which COUPA
//                    switch (scriptSplit[1]) {
//                        case "Comp":
//                            list = mongoTemplate.find(query, Comp.class);
//                            break;
//                        case "Order":
//                            list = mongoTemplate.find(query, Order.class);
//                            break;
//                        case "User":
//                            list = mongoTemplate.find(query, User.class);
//                            break;
//                        case "Prod":
//                            list = mongoTemplate.find(query, Prod.class);
//                            break;
//                        case "Asset":
//                            list = mongoTemplate.find(query, Asset.class);
//                            break;
//                    }
//                    if (list.get(0) == null) {
//                        return null;
//                    }
//                    JSONObject jsonList = (JSONObject) JSON.toJSON(list.get(0));
//                    if (jsonList.getJSONObject(scriptSplit[3]) == null) {
//                        return null;
//                    }
//                    JSONObject jsonVar = jsonList.getJSONObject(scriptSplit[3]);
//                    for (int k = 4; k < scriptSplit.length - 1; k++) {
//                        //根据[拆分
//                        //break up array
//                        String[] ifArray = scriptSplit[k].split("\\[");
//                        System.out.println("length=" + ifArray.length);
//                        //拆分成一份类型为jsonObject
//                        if (ifArray.length == 1) {
//                            if (jsonVar.getJSONObject(ifArray[0]) == null) {
//                                return null;
//                            }
//                            jsonVar = jsonVar.getJSONObject(ifArray[0]);
//                        }
//                        //拆分成两份类型为jsonArray
//                        if (ifArray.length == 2) {
//                            String[] index = ifArray[1].split("]");
//                            if (jsonVar.getJSONArray(ifArray[0]).getJSONObject(Integer.parseInt(index[0])) == null) {
//                                return null;
//                            }
//                            jsonVar = jsonVar.getJSONArray(ifArray[0]).getJSONObject(Integer.parseInt(index[0]));
//                        }
//                    }
//                    System.out.println("jsonVar=" + jsonVar);
//                    if (jsonVar.get(scriptSplit[scriptSplit.length - 1]) == null) {
//                        return null;
//                    }
//                    if (var.startsWith("##CB")) {
//                        Boolean scriptResult = jsonVar.getBoolean(scriptSplit[scriptSplit.length - 1]);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##CS")) {
//                        String scriptResult = jsonVar.getString(scriptSplit[scriptSplit.length - 1]);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##CI")) {
//                        Integer scriptResult = jsonVar.getInteger(scriptSplit[scriptSplit.length - 1]);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##CN")) {
//                        Double scriptResult = jsonVar.getDouble(scriptSplit[scriptSplit.length - 1]);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##CA")) {
//                        JSONArray scriptResult = jsonVar.getJSONArray(scriptSplit[scriptSplit.length - 1]);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##CO")) {
//                        System.out.println("test=" + jsonVar.getJSONObject(scriptSplit[scriptSplit.length - 1]));
//
//                        return jsonVar.getJSONObject(scriptSplit[scriptSplit.length - 1]);
//                    }
//                }
//                //T means it is a counter
//                else if (var.startsWith("##T")) {
//                    String[] scriptSplit = var.split("\\.");
//                    Asset asset = qt.getConfig(scriptSplit[1], "a-core", "refAuto");
//                    if (asset == null || asset.getRefAuto() == null || asset.getRefAuto().getJSONObject("objCounter") == null ||
//                            asset.getRefAuto().getJSONObject("objCounter").getJSONObject(scriptSplit[2]) == null) {
//                        return null;
//                    }
//                    JSONObject jsonCounter = asset.getRefAuto().getJSONObject("objCounter").getJSONObject(scriptSplit[2]);
//                    Integer count = jsonCounter.getInteger("count");
//                    Integer max = jsonCounter.getInteger("max");
//                    Integer digit = jsonCounter.getInteger("digit");
//                    int length = digit - String.valueOf(count).length();
//                    System.out.println("length=" + length);
//                    StringBuffer sb = new StringBuffer();
//                    for (int i = 0; i < length; i++) {
//                        sb.append("0");
//                    }
//                    String strCount = String.valueOf(sb);
//                    strCount += count;
//                    System.out.println("strCount=" + strCount);
//                    if (count == max) {
//                        count = 1;
//                    } else {
//                        count++;
//                    }
//                    System.out.println("count=" + count);
//                    JSONObject jsonUpdate = qt.setJson("refAuto.objCounter." + scriptSplit[2] + ".count", count);
//                    qt.setMDContent(asset.getId(), jsonUpdate, Asset.class);
//                    return strCount;
//                }
//                else if (var.startsWith("##G")) {
//                    String[] scriptSplit = var.split("\\.");
//                    Asset asset = qt.getConfig(scriptSplit[1], "a-core", "refAuto");
//                    if (asset == null || asset.getRefAuto() == null || asset.getRefAuto().getJSONObject("global") == null) {
//                        return null;
//                    }
//                    return asset.getRefAuto().getJSONObject("global").get(scriptSplit[2]);
//                }
//                //##F.com.cresign.timer.controller.StatController##getStatisticByEs1##op0
//                //F = it is a function, then break the string and recall myself to calculate
//                else if (var.startsWith("##F")) {
//                    String varSubstring = var.substring(4);
//                    String[] varSplit = varSubstring.split("##");
//                    System.out.println("##F=" + varSplit[0] + "," + varSplit[1] + "," + varSplit[2]);
//                    JSONObject jsonResult;
//                    if (jsonVars.getJSONObject(varSplit[2]).get("val") instanceof String) {
//                        String result = (String) this.scriptEngineVar(jsonVars.getJSONObject(varSplit[2]).getString("val"), jsonVars);
//                        System.out.println("result=" + result);
//                        jsonResult = JSONObject.parseObject(result);
//                    } else {
//                        jsonResult = jsonVars.getJSONObject(varSplit[2]).getJSONObject("val");
//                    }
//                    System.out.println("jsonResult=" + jsonResult);
//                    Class<?> clazz = Class.forName(varSplit[0]);
//                    Object bean = ApplicationContextTools.getBean(clazz);
//                    Method method1 = clazz.getMethod(varSplit[1], new Class[]{JSONObject.class});
//                    System.out.println("varSplit[1]=" + varSplit[1]);
//                    System.out.println("method1=" + method1);
//                    //invoke....
//                    Object invoke = method1.invoke(bean, jsonResult);
//                    System.out.println("invoke=" + invoke);
//                    return invoke;
//                }
//                //D = date formates
//                else if (var.startsWith("##D")) {
//                    if (var.startsWith("##DT")) {
//                        String varSubstring = var.substring(5);
//                        System.out.println("varSubstring=" + varSubstring);
//                        SimpleDateFormat sdf = new SimpleDateFormat(varSubstring);
//                        String date = sdf.format(new Date());
//                        System.out.println("##DT=" + date);
//                        return date;
//                    }
//                    else {
//                        String varSubstring = var.substring(4);
//                        System.out.println("varSubstring=" + varSubstring);
//                        String[] varSplit = varSubstring.split("##");
//                        SimpleDateFormat sdf = null;
//                        Calendar calendar = Calendar.getInstance();
//                        for (int i = varSplit.length - 1; i >= 0; i--) {
//                            String partTime = varSplit[i];
//                            if (partTime.equals("*")) {
//                                switch (i) {
//                                    case 0:
//                                        sdf = new SimpleDateFormat("yyyy");
//                                        varSplit[0] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 1:
//                                        sdf = new SimpleDateFormat("MM");
//                                        varSplit[1] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 2:
//                                        sdf = new SimpleDateFormat("dd");
//                                        varSplit[2] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 3:
//                                        sdf = new SimpleDateFormat("HH");
//                                        varSplit[3] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 4:
//                                        sdf = new SimpleDateFormat("mm");
//                                        varSplit[4] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 5:
//                                        sdf = new SimpleDateFormat("ss");
//                                        varSplit[5] = sdf.format(calendar.getTime());
//                                        break;
//                                }
//                            } else if (partTime.startsWith("+") || partTime.startsWith("-")) {
//                                int part = Integer.parseInt(partTime);
//                                System.out.println("part=" + part);
//                                switch (i) {
//                                    case 0:
//                                        calendar.add(Calendar.YEAR, part);
//                                        sdf = new SimpleDateFormat("yyyy");
//                                        varSplit[0] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 1:
//                                        calendar.add(Calendar.MONTH, part);
//                                        sdf = new SimpleDateFormat("MM");
//                                        varSplit[1] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 2:
//                                        calendar.add(Calendar.DATE, part);
//                                        sdf = new SimpleDateFormat("dd");
//                                        varSplit[2] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 3:
//                                        calendar.add(Calendar.HOUR_OF_DAY, part);
//                                        sdf = new SimpleDateFormat("HH");
//                                        varSplit[3] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 4:
//                                        calendar.add(Calendar.MINUTE, part);
//                                        sdf = new SimpleDateFormat("mm");
//                                        varSplit[4] = sdf.format(calendar.getTime());
//                                        break;
//                                    case 5:
//                                        calendar.add(Calendar.SECOND, part);
//                                        sdf = new SimpleDateFormat("ss");
//                                        varSplit[5] = sdf.format(calendar.getTime());
//                                        break;
//                                }
//                            }
//                            System.out.println("i=" + i + ",varSplit=" + varSplit[i]);
//                        }
//                        StringBuffer stringBuffer = new StringBuffer();
//                        stringBuffer = stringBuffer.append(varSplit[0]).append("/").append(varSplit[1]).append("/").append(varSplit[2])
//                                .append(" ").append(varSplit[3]).append(":").append(varSplit[4]).append(":").append(varSplit[5]);
//                        System.out.println("##D=" + stringBuffer);
//                        return stringBuffer;
//                    }
//                }
//                else if (var.startsWith("##PU")) {
//                    String[] varSplit = var.split("\\.");
//                    String[] numSplit = varSplit[2].split(",");
//                    if (varSplit[0].equals("Integer")) {
//                        Integer num1;
//                        Integer num2;
//                        if (numSplit[0].startsWith("##")) {
//                            num1 = jsonVars.getJSONObject(numSplit[0]).getInteger("val");
//                        } else {
//                            num1 = Integer.parseInt(numSplit[0]);
//                        }
//                        if (numSplit[1].startsWith("##")) {
//                            num2 = jsonVars.getJSONObject(numSplit[1]).getInteger("val");
//                        } else {
//                            num2 = Integer.parseInt(numSplit[1]);
//                        }
//                        if (varSplit[2].equals("+")) {
//                            return num1 + num2;
//                        } else if (varSplit[2].equals("-")) {
//                            return num1 - num2;
//                        } else if (varSplit[2].equals("*")) {
//                            return num1 * num2;
//                        } else if (varSplit[2].equals("/")) {
//                            return num1 / num2;
//                        }
//                    }
//                    else if (varSplit[0].equals("Double")) {
//                        Double num1;
//                        Double num2;
//                        if (numSplit[0].startsWith("##")) {
//                            num1 = jsonVars.getJSONObject(numSplit[0]).getDouble("val");
//                        } else {
//                            num1 = Double.parseDouble(numSplit[0]);
//                        }
//                        if (numSplit[1].startsWith("##")) {
//                            num2 = jsonVars.getJSONObject(numSplit[1]).getDouble("val");
//                        } else {
//                            num2 = Double.parseDouble(numSplit[1]);
//                        }
//                        if (varSplit[2].equals("+")) {
//                            return DoubleUtils.add(num1, num2);
//                        } else if (varSplit[2].equals("-")) {
//                            return DoubleUtils.subtract(num1, num2);
//                        } else if (varSplit[2].equals("*")) {
//                            return DoubleUtils.multiply(num1, num2);
//                        } else if (varSplit[2].equals("/")) {
//                            return DoubleUtils.divide(num1, num2);
//                        }
//                    }
//                    else if (varSplit[0].equals("String")) {
//                        String str1;
//                        String str2;
//                        if (numSplit[0].startsWith("##")) {
//                            str1 = jsonVars.getJSONObject(numSplit[0]).getString("val");
//                        } else {
//                            str1 = numSplit[0];
//                        }
//                        if (numSplit[1].startsWith("##")) {
//                            str2 = jsonVars.getJSONObject(numSplit[1]).getString("val");
//                        } else {
//                            str2 = numSplit[1];
//                        }
//                        return str1.equals(str2);
//                    }
//                }
//                else if (var.startsWith("##PA")) {
//                    String[] varSplit = var.split("\\.");
//                    String[] numSplit = varSplit[2].split(",");
//                    if (varSplit[0].equals("Integer")) {
//                        Integer num1;
//                        Integer num2;
//                        if (numSplit[0].startsWith("##")) {
//                            num1 = jsonVars.getJSONObject(numSplit[0]).getInteger("val");
//                        } else {
//                            num1 = Integer.parseInt(numSplit[0]);
//                        }
//                        if (numSplit[1].startsWith("##")) {
//                            num2 = jsonVars.getJSONObject(numSplit[1]).getInteger("val");
//                        } else {
//                            num2 = Integer.parseInt(numSplit[1]);
//                        }
//                        if (varSplit[2].equals("=")) {
//                            return num1 == num2;
//                        } else if (varSplit[2].equals(">")) {
//                            return num1 > num2;
//                        } else if (varSplit[2].equals(">=")) {
//                            return num1 >= num2;
//                        }
//                    }
//                    else if (varSplit[0].equals("Double")) {
//                        Double num1;
//                        Double num2;
//                        if (numSplit[0].startsWith("##")) {
//                            num1 = jsonVars.getJSONObject(numSplit[0]).getDouble("val");
//                        } else {
//                            num1 = Double.parseDouble(numSplit[0]);
//                        }
//                        if (numSplit[1].startsWith("##")) {
//                            num2 = jsonVars.getJSONObject(numSplit[1]).getDouble("val");
//                        } else {
//                            num2 = Double.parseDouble(numSplit[1]);
//                        }
//                        if (varSplit[2].equals("=")) {
//                            return DoubleUtils.doubleEquals(num1, num2);
//                        } else if (varSplit[2].equals(">")) {
//                            return DoubleUtils.doubleGt(num1, num2);
//                        } else if (varSplit[2].equals(">=")) {
//                            return DoubleUtils.doubleGte(num1, num2);
//                        }
//                    }
//                    else if (varSplit[0].equals("String")) {
//                        String str1;
//                        String str2;
//                        if (numSplit[0].startsWith("##")) {
//                            str1 = jsonVars.getJSONObject(numSplit[0]).getString("val");
//                        } else {
//                            str1 = numSplit[0];
//                        }
//                        if (numSplit[1].startsWith("##")) {
//                            str2 = jsonVars.getJSONObject(numSplit[1]).getString("val");
//                        } else {
//                            str2 = numSplit[1];
//                        }
//                        return str1.equals(str2);
//                    }
//                }
//                // else this is not a ##, use script engine to get result
//                else {
//                    String varSubString = var.substring(4);
//                    if (varSubString.contains("for")) {
//                        return null;
//                    }
//                    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
//                    Compilable compilable = (Compilable) scriptEngine;
//                    Bindings bindings = scriptEngine.createBindings();
//                    CompiledScript compiledScript = compilable.compile(varSubString);
//                    String[] varSplit = var.split("\\(");
//                    System.out.println(varSplit[0] + "," + varSplit[1]);
//                    if (varSplit[1].split("\\)\\{").length > 0) {
//                        String scriptVar = varSplit[1].split("\\)\\{")[0];
//                        System.out.println("scriptVar=" + scriptVar);
//                        if (scriptVar != null && !scriptVar.equals("")) {
//                            System.out.println("true");
//                            String[] arrayKey = scriptVar.split(",");
//                            System.out.println("arrayKey.size=" + arrayKey.length);
//                            for (int i = 0; i < arrayKey.length; i++) {
//                                String key = arrayKey[i];
//                                System.out.println("key=" + key);
//                                // I am calling myself again here
//                                Object result;
//                                if (jsonVars.getJSONObject(key).get("val") instanceof String) {
//                                    result = this.scriptEngineVar(jsonVars.getJSONObject(key).getString("val"), jsonVars);
//                                } else {
//                                    result = jsonVars.getJSONObject(key).get("val");
//                                }
//                                System.out.println(i + ":" + result);
//                                bindings.put(key, result);
//                            }
//                        }
//                    }
//
//                    //after created all compile data, here is actual eval
//                    if (var.startsWith("##B")) {
//                        Boolean scriptResult = (Boolean) compiledScript.eval(bindings);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##S")) {
//                        String scriptResult = compiledScript.eval(bindings).toString();
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##I")) {
//                        Integer scriptResult = (Integer) compiledScript.eval(bindings);
//                        System.out.println("##I=" + scriptResult);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##N")) {
//                        Double scriptResult = (Double) compiledScript.eval(bindings);
//                        System.out.println("##N=" + scriptResult);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##A")) {
//                        String result = JSON.toJSONString((compiledScript.eval(bindings)));
//                        System.out.println("##A=" + result);
//                        result = result.replace("\\", "");
//                        System.out.println(result);
//                        result = result.substring(1, result.length() - 1);
//                        System.out.println(result);
//                        JSONArray scriptResult = JSON.parseArray(result);
//                        System.out.println(scriptResult);
//                        return scriptResult;
//                    }
//                    if (var.startsWith("##O")) {
//                        String result = JSON.toJSONString(compiledScript.eval(bindings));
//                        System.out.println("##O=" + result);
//                        result = result.replace("\\", "");
//                        System.out.println(result);
//                        result = result.substring(1, result.length() - 1);
//                        System.out.println(result);
//                        JSONObject scriptResult = JSON.parseObject(result);
//                        System.out.println(scriptResult);
//                        return scriptResult;
//                    }
//                }
//            }
//            // if they are not B/S/N/A/O, it's NO ##, so it's either number or text
//            //正则判断是否数字
//            Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
//            Matcher matcher = pattern.matcher(var);
//            //判断字符串是否是数字
//            if (matcher.matches()) {
//                return Double.parseDouble(var);
//            }
//            return var;
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.DB_ERROR.getCode(), e.toString());
//        }
//    }
//
//    public JSONObject copyCOUPA(String id_C, JSONObject jsonMongo, JSONObject jsonEs) {
//        String newId = qt.GetObjectId();
//        jsonMongo.put("id", newId);
//        JSONObject jsonInfo = jsonMongo.getJSONObject("info");
//        jsonInfo.put("id_C", id_C);
//        return null;
//    }
////    public JSONObject copyUtil(String id_U, String id, String id_C, String grp, String listType) {
////
////        authCheck.getUserUpdateAuth(id_U,id_C,listType,grp,"batch",new JSONArray().fluentAdd("copy"));
////
//////             查询id
////        String resetGrp = "";
////        if ("lSProd".equals(listType)) {
////            System.out.println("lSProd");
////            resetGrp = "grp";
////
////            // 获取到要复制的数据
////            Prod sourceProd = qt.getMDContent(id, "", Prod.class);
////            System.out.println(sourceProd);
////            // 判断为空
////            if (ObjectUtils.isEmpty(sourceProd)) {
////                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_FOUND.getCode(), null);
////            }
////            if (!sourceProd.getInfo().getId_C().equals(id_C))
////            {
////                //check if id_C is real?
////            }
////            sourceProd.getView().remove("tempa");
////            sourceProd.setTempa(null);
////
////            return copyProd(sourceProd, listType, id_C, grp, resetGrp);
////
////        } else if ("lBProd".equals(listType)) {
////            System.out.println("lBProd");
////            resetGrp = "grpB";
////
////            // 获取到要复制的数据
////            Prod sourceProd = qt.getMDContent(id, "", Prod.class);
////
////            // 判断为空或者bcdNet = 1（别人真公司的  不能拷贝）
////            if (ObjectUtils.isEmpty(sourceProd) || qt.judgeComp(id_C, sourceProd.getInfo().getId_C()) == 1) {
////                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.BCDNET_ERROR.getCode(), null);
////            }
////            sourceProd.getView().remove("tempa");
////            sourceProd.setTempa(null);
////
////            return copyProd(sourceProd, listType, id_C, grp, resetGrp);
////
////        } else if ("lSInfo".equals(listType)) {
////            resetGrp = "grp";
////
////            // 获取到要复制的数据
//////            Prod sourceProd = mongoTemplate.findOne(query, Prod.class);
////            Info sourceInfo = qt.getMDContent(id, "", Info.class);
////            System.out.println(sourceInfo);
////            // 判断为空
////            if (ObjectUtils.isEmpty(sourceInfo)) {
////                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_FOUND.getCode(), null);
////            }
////            sourceInfo.getView().remove("tempa");
////            sourceInfo.setTempa(null);
////
////            return copyInfo(sourceInfo, listType, id_C, grp, resetGrp);
////
////        } else if ("lBInfo".equals(listType)) {
////            resetGrp = "grpB";
////
////            // 获取到要复制的数据
////            Info sourceInfo = qt.getMDContent(id, "", Info.class);
////
////            // 判断为空或者bcdNet = 1（别人真公司的  不能拷贝）
////            if (ObjectUtils.isEmpty(sourceInfo) || qt.judgeComp(id_C, sourceInfo.getInfo().getId_C()) == 1) {
////                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.BCDNET_ERROR.getCode(), null);
////            }
////            sourceInfo.getView().remove("tempa");
////            sourceInfo.setTempa(null);
////
////            return copyInfo(sourceInfo, listType, id_C, grp, resetGrp);
////
////        } else if ("lSOrder".equals(listType)) {
////            System.out.println("lSOrder");
////            resetGrp = "grp";
////
////            // 获取到要复制的数据
//////            Order sourceOrder = mongoTemplate.findOne(query, Order.class);
////            Order sourceOrder = qt.getMDContent(id, "", Order.class);
////            System.out.println(sourceOrder);
////            // 判断为空或者lST < 8
////            if (ObjectUtils.isEmpty(sourceOrder) || sourceOrder.getInfo().getLST() >= 7) {
////                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.COMP_IS_NULL_LST.getCode(), null);
////            }
////            sourceOrder.getView().remove("tempa");
////            sourceOrder.setTempa(null);
////
////            return copyOrder(sourceOrder, listType, id_C, grp, resetGrp);
////
////        } else if ("lBOrder".equals(listType)) {
////            System.out.println("lBOrder");
////            resetGrp = "grpB";
////
////            // 获取到要复制的数据
////            Order sourceOrder = qt.getMDContent(id, "", Order.class);
////            System.out.println(sourceOrder);
////            // 判断为空或者lST < 8
////            if (ObjectUtils.isEmpty(sourceOrder) || sourceOrder.getInfo().getLST() >= 7) {
////                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.COMP_IS_NULL_LST.getCode(), null);
////            }
////            sourceOrder.getView().remove("tempa");
////            sourceOrder.setTempa(null);
////
////            return copyOrder(sourceOrder, listType, id_C, grp, resetGrp);
////
////        }
////        return null;
////    }
//
//    //case1: info.wrdN if card has no index, and it is wrdXXX
//    //case4: info.ref
//    //case2: part.objItem.$.wrdN
//    //case3: part.objItem.$.grp
//    //case5: text00s.objData.3.wn2qty
//    //case5: text00s.objData.3.wrdFN
//    //case6: text00s.objData.3.objText.$.wrddesc / index [2]
//    //case7: text00s.objData.3.objText.$.ref / index[2,4]
//
//    public JSONObject tempaCOUPA(JSONArray arrayData, JSONObject jsonVar, JSONObject jsonEs, String listType) {
//        jsonEs.remove("grpT");
//        Init init = qt.getInitData("cn");
//
//        JSONObject jsonCol = init.getCol().getJSONObject(listType);
//        JSONObject upKeyVal = new JSONObject();
//        for (int i = 0; i < arrayData.size(); i++) {
//            JSONObject jsonData = arrayData.getJSONObject(i);
//            String key = jsonData.getString("key");
//            JSONArray arrayIndex = jsonData.getJSONArray("index");
//
//            /////////////*************////////////
//            Object value = this.scriptEngineVar(jsonData.getString("value"), jsonVar);
//            jsonData.put("value", value);
//
//            //for lSBOrder etc, checking grp here is useless, need to check before this
////            if (key.equals("info.grp")) {
////                if (!arrayGrpTarget.contains(key)) {
////                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_FOUND.getCode(), null); //GRP_NOT_MATCH
////                }
////            }
//            String[] keySplit = key.split("\\.");
//
//            if (arrayIndex == null)
//            {
//                String colKey = keySplit[keySplit.length - 1];
//
//                //if card has no index, and it is wrdXXX
//                if (colKey.startsWith("wrd"))
//                {
//                    key = key + ".cn";
//                    qt.upJson(upKeyVal, key, value);
//                    if (jsonCol.getJSONObject(colKey) != null) {
//                        jsonEs.put(colKey, qt.setJson("cn", value));
//                    }
//
//                } else
//                {
//                    qt.upJson(upKeyVal, key, value);
//                    if (jsonCol.getJSONObject(colKey) != null) {
//                        jsonEs.put(colKey, value);
//                    }
//                }
//            }
//            else {
//                // objXXX.$.wrdN.cn stuff like this. for file00s etc
//                int keyIndexOf = key.indexOf("$"); // text00s objData $ wrddesc cn
//                String keyPrefix = key.substring(0, keyIndexOf);
//                String keySuffix = key.substring(keyIndexOf + 1, key.length());
//
//                if (keySuffix.startsWith(".wrd"))
//                {
//                    keySuffix = keySuffix + ".cn";
//                }
//                for (int j = 0; j < arrayIndex.size(); j++) {
//                    Integer index = arrayIndex.getInteger(j);
//                    qt.upJson(upKeyVal, keyPrefix + index + keySuffix, value);
//                }
//            }
//        }
//        JSONObject mapResult = new JSONObject();
//        mapResult.put("mongo", upKeyVal);
//        mapResult.put("es", jsonEs);
//        return mapResult;
//    }
//
////
////    public Object scriptEngineVar2(Object obj, JSONObject jsonVars) {
////        try {
////            if (obj instanceof String) {
////                String var = obj.toString();
////                System.out.println("");
////                System.out.println("var=" + var);
////                System.out.println("jsonVars=" + jsonVars);
////                if (var.startsWith("##")) {
////                    //Get from card
////                    if (var.startsWith("##C")) {
////                        if (var.startsWith("##CC")) {
////                            String varSubstring = var.substring(5);
////                            String[] varSplits = varSubstring.split("\\$\\$");
////                            StringBuffer sb = new StringBuffer();
////                            for (int i = 0; i < varSplits.length; i++) {
////                                String varSplit = varSplits[i];
////                                if (varSplit.startsWith("##")) {
////                                    String key = varSplit.substring(2);
////                                    qt.errPrint("sev", jsonVars.getString(key), jsonVars, varSplit);
////                                    Object result = this.scriptEngineVar(jsonVars.getString(key), jsonVars);
////                                    System.out.println("##CC=" + key + ":" + result);
////                                    sb.append(result);
////                                } else {
////                                    sb.append(varSplit);
////                                }
////                            }
////                            return sb.toString();
////                        }
////
////                        String[] scriptSplit = var.split("\\.");
////                        Query query = new Query(new Criteria("_id").is(scriptSplit[2]));
////                        List list = new ArrayList();
////                        //See which COUPA
////                        switch (scriptSplit[1]) {
////                            case "Comp":
////                                list = mongoTemplate.find(query, Comp.class);
////                                break;
////                            case "Order":
////                                list = mongoTemplate.find(query, Order.class);
////                                break;
////                            case "User":
////                                list = mongoTemplate.find(query, User.class);
////                                break;
////                            case "Prod":
////                                list = mongoTemplate.find(query, Prod.class);
////                                break;
////                            case "Asset":
////                                list = mongoTemplate.find(query, Asset.class);
////                                break;
////                        }
////                        if (list.get(0) == null) {
////                            return null;
////                        }
////                        JSONObject jsonList = (JSONObject) JSON.toJSON(list.get(0));
////                        if (jsonList.getJSONObject(scriptSplit[3]) == null) {
////                            return null;
////                        }
////                        JSONObject jsonVar = jsonList.getJSONObject(scriptSplit[3]);
////                        for (int k = 4; k < scriptSplit.length - 1; k++) {
////                            //根据[拆分
////                            //break up array
////                            String[] ifArray = scriptSplit[k].split("\\[");
////                            System.out.println("length=" + ifArray.length);
////                            //拆分成一份类型为jsonObject
////                            if (ifArray.length == 1) {
////                                if (jsonVar.getJSONObject(ifArray[0]) == null) {
////                                    return null;
////                                }
////                                jsonVar = jsonVar.getJSONObject(ifArray[0]);
////                            }
////                            //拆分成两份类型为jsonArray
////                            if (ifArray.length == 2) {
////                                String[] index = ifArray[1].split("]");
////                                if (jsonVar.getJSONArray(ifArray[0]).getJSONObject(Integer.parseInt(index[0])) == null) {
////                                    return null;
////                                }
////                                jsonVar = jsonVar.getJSONArray(ifArray[0]).getJSONObject(Integer.parseInt(index[0]));
////                            }
////                        }
////                        System.out.println("jsonVar=" + jsonVar);
////                        if (jsonVar.get(scriptSplit[scriptSplit.length - 1]) == null) {
////                            return null;
////                        }
////                        if (var.startsWith("##CB")) {
////                            Boolean scriptResult = jsonVar.getBoolean(scriptSplit[scriptSplit.length - 1]);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##CS")) {
////                            String scriptResult = jsonVar.getString(scriptSplit[scriptSplit.length - 1]);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##CI")) {
////                            Integer scriptResult = jsonVar.getInteger(scriptSplit[scriptSplit.length - 1]);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##CN")) {
////                            Double scriptResult = jsonVar.getDouble(scriptSplit[scriptSplit.length - 1]);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##CA")) {
////                            JSONArray scriptResult = jsonVar.getJSONArray(scriptSplit[scriptSplit.length - 1]);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##CO")) {
////                            System.out.println("test=" + jsonVar.getJSONObject(scriptSplit[scriptSplit.length - 1]));
////
////                            return jsonVar.getJSONObject(scriptSplit[scriptSplit.length - 1]);
////                        }
////                    }
////                    //T means it is a counter
////                    else if (var.startsWith("##T")) {
////                        String[] scriptSplit = var.split("\\.");
////                        Asset asset = qt.getConfig(scriptSplit[1], "a-core", "refAuto");
////                        if (asset == null || asset.getRefAuto() == null || asset.getRefAuto().getJSONObject("objCounter") == null ||
////                                asset.getRefAuto().getJSONObject("objCounter").getJSONObject(scriptSplit[2]) == null) {
////                            return null;
////                        }
////                        JSONObject jsonCounter = asset.getRefAuto().getJSONObject("objCounter").getJSONObject(scriptSplit[2]);
////                        Integer count = jsonCounter.getInteger("count");
////                        Integer max = jsonCounter.getInteger("max");
////                        Integer digit = jsonCounter.getInteger("digit");
////                        int length = digit - String.valueOf(count).length();
////                        System.out.println("length=" + length);
////                        StringBuffer sb = new StringBuffer();
////                        for (int i = 0; i < length; i++) {
////                            sb.append("0");
////                        }
////                        String strCount = String.valueOf(sb);
////                        strCount += count;
////                        System.out.println("strCount=" + strCount);
////                        if (count == max) {
////                            count = 1;
////                        } else {
////                            count++;
////                        }
////                        System.out.println("count=" + count);
////                        JSONObject jsonUpdate = qt.setJson("refAuto.objCounter." + scriptSplit[2] + ".count", count);
////                        qt.setMDContent(asset.getId(), jsonUpdate, Asset.class);
////                        return strCount;
////                    }
////                    //##F.com.cresign.timer.controller.StatController##getStatisticByEs1##op0
////                    //F = it is a function, then break the string and recall myself to calculate
////                    else if (var.startsWith("##F")) {
////                        String varSubstring = var.substring(4);
////                        String[] varSplit = varSubstring.split("##");
////                        System.out.println("##F=" + varSplit[0] + "," + varSplit[1] + "," + varSplit[2]);
////                        String result = (String) this.scriptEngineVar(jsonVars.getJSONObject(varSplit[2]).get("val"), jsonVars);
////                        System.out.println("result=" + result);
////                        JSONObject jsonResult = JSONObject.parseObject(result);
////                        System.out.println("jsonResult=" + jsonResult);
////                        Class<?> clazz = Class.forName(varSplit[0]);
////                        Object bean = ApplicationContextTools.getBean(clazz);
////                        Method method1 = clazz.getMethod(varSplit[1], new Class[]{JSONObject.class});
////                        System.out.println("varSplit[1]=" + varSplit[1]);
////                        System.out.println("method1=" + method1);
////                        //invoke....
////                        Object invoke = method1.invoke(bean, jsonResult);
////                        System.out.println("invoke=" + invoke);
////                        return invoke;
////                    }
////                    //D = date formates
////                    else if (var.startsWith("##D")) {
////                        if (var.startsWith("##DT")) {
////                            String varSubstring = var.substring(5);
////                            System.out.println("varSubstring=" + varSubstring);
////                            SimpleDateFormat sdf = new SimpleDateFormat(varSubstring);
////                            String date = sdf.format(new Date());
////                            System.out.println("##DT=" + date);
////                            return date;
////                        }
////                        else {
////                            String varSubstring = var.substring(4);
////                            System.out.println("varSubstring=" + varSubstring);
////                            String[] varSplit = varSubstring.split("##");
////                            SimpleDateFormat sdf = null;
////                            Calendar calendar = Calendar.getInstance();
////                            for (int i = varSplit.length - 1; i >= 0; i--) {
////                                String partTime = varSplit[i];
////                                if (partTime.equals("*")) {
////                                    switch (i) {
////                                        case 0:
////                                            sdf = new SimpleDateFormat("yyyy");
////                                            varSplit[0] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 1:
////                                            sdf = new SimpleDateFormat("MM");
////                                            varSplit[1] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 2:
////                                            sdf = new SimpleDateFormat("dd");
////                                            varSplit[2] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 3:
////                                            sdf = new SimpleDateFormat("HH");
////                                            varSplit[3] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 4:
////                                            sdf = new SimpleDateFormat("mm");
////                                            varSplit[4] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 5:
////                                            sdf = new SimpleDateFormat("ss");
////                                            varSplit[5] = sdf.format(calendar.getTime());
////                                            break;
////                                    }
////                                } else if (partTime.startsWith("+") || partTime.startsWith("-")) {
////                                    int part = Integer.parseInt(partTime);
////                                    System.out.println("part=" + part);
////                                    switch (i) {
////                                        case 0:
////                                            calendar.add(Calendar.YEAR, part);
////                                            sdf = new SimpleDateFormat("yyyy");
////                                            varSplit[0] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 1:
////                                            calendar.add(Calendar.MONTH, part);
////                                            sdf = new SimpleDateFormat("MM");
////                                            varSplit[1] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 2:
////                                            calendar.add(Calendar.DATE, part);
////                                            sdf = new SimpleDateFormat("dd");
////                                            varSplit[2] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 3:
////                                            calendar.add(Calendar.HOUR_OF_DAY, part);
////                                            sdf = new SimpleDateFormat("HH");
////                                            varSplit[3] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 4:
////                                            calendar.add(Calendar.MINUTE, part);
////                                            sdf = new SimpleDateFormat("mm");
////                                            varSplit[4] = sdf.format(calendar.getTime());
////                                            break;
////                                        case 5:
////                                            calendar.add(Calendar.SECOND, part);
////                                            sdf = new SimpleDateFormat("ss");
////                                            varSplit[5] = sdf.format(calendar.getTime());
////                                            break;
////                                    }
////                                }
////                                System.out.println("i=" + i + ",varSplit=" + varSplit[i]);
////                            }
////                            StringBuffer stringBuffer = new StringBuffer();
////                            stringBuffer = stringBuffer.append(varSplit[0]).append("/").append(varSplit[1]).append("/").append(varSplit[2])
////                                    .append(" ").append(varSplit[3]).append(":").append(varSplit[4]).append(":").append(varSplit[5]);
////                            System.out.println("##D=" + stringBuffer);
////                            return stringBuffer;
////                        }
////                    }
////                    // else this is not a ##, use script engine to get result
////                    else {
////                        String varSubString = var.substring(4);
////                        if (varSubString.contains("for")) {
////                            return null;
////                        }
////                        ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
////                        Compilable compilable = (Compilable) scriptEngine;
////                        Bindings bindings = scriptEngine.createBindings();
////                        CompiledScript compiledScript = compilable.compile(varSubString);
////                        String[] varSplit = var.split("\\(");
////                        System.out.println(varSplit[0] + "," + varSplit[1]);
////                        if (varSplit[1].split("\\)\\{").length > 0) {
////                            String scriptVar = varSplit[1].split("\\)\\{")[0];
////                            System.out.println("scriptVar=" + scriptVar);
////                            if (scriptVar != null && !scriptVar.equals("")) {
////                                System.out.println("true");
////                                String[] arrayKey = scriptVar.split(",");
////                                System.out.println("arrayKey.size=" + arrayKey.length);
////                                for (int i = 0; i < arrayKey.length; i++) {
////                                    String key = arrayKey[i];
////                                    System.out.println("key=" + key);
////                                    // I am calling myself again here
////                                    Object result = this.scriptEngineVar(jsonVars.getJSONObject(key).get("val"), jsonVars);
////                                    System.out.println(i + ":" + result);
////                                    bindings.put(key, result);
////                                }
////                            }
////                        }
////
////                        //after created all compile data, here is actual eval
////                        if (var.startsWith("##B")) {
////                            Boolean scriptResult = (Boolean) compiledScript.eval(bindings);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##S")) {
////                            String scriptResult = compiledScript.eval(bindings).toString();
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##I")) {
////                            Integer scriptResult = (Integer) compiledScript.eval(bindings);
////                            System.out.println("##I=" + scriptResult);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##N")) {
////                            Double scriptResult = (Double) compiledScript.eval(bindings);
////                            System.out.println("##N=" + scriptResult);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##A")) {
////                            String result = JSON.toJSONString((compiledScript.eval(bindings)));
////                            System.out.println("##A=" + result);
////                            result = result.replace("\\", "");
////                            System.out.println(result);
////                            result = result.substring(1, result.length() - 1);
////                            System.out.println(result);
////                            JSONArray scriptResult = JSON.parseArray(result);
////                            System.out.println(scriptResult);
////                            return scriptResult;
////                        }
////                        if (var.startsWith("##O")) {
////                            String result = JSON.toJSONString(compiledScript.eval(bindings));
////                            System.out.println("##O=" + result);
////                            result = result.replace("\\", "");
////                            System.out.println(result);
////                            result = result.substring(1, result.length() - 1);
////                            System.out.println(result);
////                            JSONObject scriptResult = JSON.parseObject(result);
////                            System.out.println(scriptResult);
////                            return scriptResult;
////                        }
////                    }
////                }
////                // if they are not B/S/N/A/O, it's NO ##, so it's either number or text
////                //正则判断是否数字
////                Pattern pattern = Pattern.compile("-?[0-9]+(\\\\.[0-9]+)?");
////                Matcher matcher = pattern.matcher(var);
////                //判断字符串是否是数字
////                if (matcher.matches()) {
////                    return Double.parseDouble(var);
////                }
////            }
////            return obj;
////        } catch (Exception e)
////        {
////            e.printStackTrace();
////            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.DB_ERROR.getCode(), e.toString());
////        }
////    }
//
//    public void initAction(JSONObject orderOItem, JSONArray action, Integer index)
//    {
//
//        if (action.size() <= index || action.getJSONObject(index) == null) {
//            JSONObject actionData = qt.setJson(
//                    "bcdStatus", 100, "bisPush", 0,
//                    "bisactivate", 0,
//                    "id_O", orderOItem.getString("id_O"),
//                    "id_Us", new JSONArray(),
//                    "id_OP", orderOItem.getString("id_OP"),
//                    "id_P", orderOItem.getString("id_P"),
//                    "index", orderOItem.getString("index"),
//                    "refOP", "",
//                    "prob", new JSONArray(),
//                    "sumChild", 0, "sumPrev", 0,
//                    "layer", 0,
//                    "prtNext", new JSONArray(),
//                    "prtPrev", new JSONArray(),
//                    "subParts", new JSONArray(),
//                    "upPrnts", new JSONArray(),
//                    "sumChild", 0,
//                    "rKey", orderOItem.getString("rKey"),
//                    "wrdNP", orderOItem.getJSONObject("wrdNP"),
//                    "wrdN", orderOItem.getJSONObject("wrdN"),
//                    "bmdpt", 1, "priority", 2);
//
//            action.set(index, actionData);
//        }
//
//        //These data must renew
//        action.getJSONObject(index).put("id_O", orderOItem.getString("id_O"));
//        action.getJSONObject(index).put("index", orderOItem.getString("index"));
//        action.getJSONObject(index).put("rKey", orderOItem.getString("rKey"));
//
//
//        if (action.getJSONObject(index).getJSONArray("upPrnts") == null)
//        {
//            action.getJSONObject(index).put("upPrnts", new JSONArray());
//        }
//        if (action.getJSONObject(index).getJSONArray("subParts") == null)
//        {
//            action.getJSONObject(index).put("subParts", new JSONArray());
//        }
//        if (action.getJSONObject(index).getJSONArray("prtPrev") == null)
//        {
//            action.getJSONObject(index).put("prtPrev", new JSONArray());
//        }
//        if (action.getJSONObject(index).getJSONArray("prtNext") == null)
//        {
//            action.getJSONObject(index).put("prtNext", new JSONArray());
//        }
//    }
//
//    public void initOItem(Order order)
//    {
//        JSONObject newOItem = qt.setJson("objItem", new JSONArray(), "arrP", new JSONArray(),
//                "bmdHeight", "unset", "objItemCol", new JSONArray(), "wn2qty", 0.0, "wn4price", 0.0,
//                "objCard", qt.setArray("oItem"));
//        order.setOItem(newOItem);
//    }
//
//    public void initOQc(JSONObject orderOItem, JSONArray oQc, Integer index)
//    {
//        if (oQc.size() <= index || oQc.getJSONObject(index) == null) {
//            JSONObject oQcData = qt.setJson(
//                    "score", 0, "foCount", 5);
//
//            oQc.set(index, oQcData);
//        }
////        if (oQc.getJSONObject(index).getJSONArray("score") == null)
////        {
////            oQc.getJSONObject(index).put("score", 0);
////        }
////        if (oQc.getJSONObject(index).getJSONArray("foCount") == null)
////        {
////            oQc.getJSONObject(index).put("foCount", 5);
////        }
//    }
//
//    /***
//     * init entire oStock from nothing
//     * @param oItem need to send the entire oItem.objItem for data to init
//     * @return
//     */
//    public JSONObject initAction(JSONArray oItem)
//    {
//        JSONArray actionArray = new JSONArray();
//        for (int i = 0; i < oItem.size(); i++)
//        {
//            this.initAction(oItem.getJSONObject(i), actionArray, i);
//        }
//        JSONObject action = new JSONObject();
//        action.put("objAction", actionArray);
//        action.put("wn2progress", 0.0);
//        action.put("grpGroup", new JSONObject());
//        action.put("grpBGroup", new JSONObject());
//        return action;
//    }
//
//    public JSONObject initOQc(JSONArray oItem)
//    {
//        JSONArray actionArray = new JSONArray();
//        for (int i = 0; i < oItem.size(); i++)
//        {
//            this.initOQc(oItem.getJSONObject(i), actionArray, i);
//        }
//        JSONObject oQc = new JSONObject();
//        oQc.put("objQc", actionArray);
//        return oQc;
//    }
//
//    public JSONArray initOStock(JSONObject orderOItem, JSONArray oStock, Integer index)
//    {
//
//            JSONObject oStockData = qt.setJson("wn2qtynow", 0.0, "wn2qtymade", 0.0,
//                    "id_P", orderOItem.getString("id_P"),
//                    "resvQty", new JSONObject(),
//                    "rKey", orderOItem.getString("rKey"));
//
//            oStockData.put("objShip", qt.setArray(
//                    qt.setJson("wn2qtyship", 0.0, "wn2qtyshipnow", 0.0,
//                            "wn2qtynowS", 0.0, "wn2qtynow", 0.0, "wn2qtymade", 0.0,
//                            "wn2qtyneed", orderOItem.getDouble("wn2qtyneed"))));
//
//
//
//            oStock.set(index, oStockData);
//
//            return oStock;
//    }
//
//    /***
//     * init entire oStock from nothing
//     * @param oItem need to send the entire oItem.objItem for data to init
//     * @return
//     */
//    public JSONObject initOStock(JSONArray oItem)
//    {
//        JSONArray stockArray = new JSONArray();
//        for (int i = 0; i < oItem.size(); i++)
//        {
//            this.initOStock(oItem.getJSONObject(i), stockArray, i);
//        }
//        JSONObject oStock = new JSONObject();
//        oStock.put("objData", stockArray);
//        oStock.put("wn2fin", 0.0);
//        oStock.put("wn2qtyship", 0.0);
//
//        return oStock;
//    }
//
//    public JSONObject initOMoney() {
//        JSONObject oMoney = qt.setJson("grp", "1000", "grpB", "1000", "mnymade", 0.0, "mnynow", 0.0, "mnypaid", 0.0);
//        return oMoney;
//    }
//
//    public void setStock(JSONArray arrayLsbasset, JSONObject tokData, String id_CB, String id_P, String id_A, Double wn2qty, Integer index, String locAddr,
//                         JSONArray locSpace, JSONArray spaceQty, JSONArray arrPP, JSONArray procQty, Boolean isProc, Integer lAT, String zcndesc, Integer imp) {
//        if (id_A == null) {
//            JSONArray filterArray = qt.setESFilt("id_C", tokData.getString("id_C"), "id_CB", id_CB, "locAddr", locAddr);
//            qt.setESFilt(filterArray, "locSpace", "contain", locSpace);
//            if (arrPP != null) {
//                qt.setESFilt(filterArray, "arrPP", "notnull", null);
//            }
//            //货架指定的格子内容
//            JSONArray arrayEs = qt.getES("lSAsset", filterArray);
//            //货架格子有多种产品或产品不同
//            if (arrayEs.size() > 1) {
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.SPACE_OCCUPIED.getCode(), null);
//            }
//            //货架指定的格子没有该产品
//            if (arrayEs.size() == 0) {
//                filterArray = qt.setESFilt("id_C", tokData.getString("id_C"), "id_CB", id_CB, "id_P", id_P, "locAddr", locAddr);
//                if (arrPP != null) {
//                    qt.setESFilt(filterArray, "arrPP", "notnull", null);
//                }
//                JSONArray arrayEs1 = qt.getES("lSAsset", filterArray);
//                //货架没有该产品，新建
//                if (arrayEs1.size() == 0) {
//                    id_A = "";
//                } else if (arrPP != null && !arrPP.equals(arrayEs1.getJSONObject(0).getJSONArray("arrPP"))) {
//                    id_A = "";
//                } else {
//                    id_A = arrayEs1.getJSONObject(0).getString("id_A");
//                }
//            } else {
//                if (id_P != null && !id_P.isEmpty() && !id_P.equals(arrayEs.getJSONObject(0).getString("id_P"))) {
//                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.SPACE_OCCUPIED.getCode(), null);
//                }
//                if (arrPP != null && !arrPP.equals(arrayEs.getJSONObject(0).getJSONArray("arrPP"))) {
//                    id_A = "";
//                } else {
//                    id_A = arrayEs.getJSONObject(0).getString("id_A");
//                }
//            }
//        }
//        JSONObject jsonLog = qt.setJson(
//                "zcndesc", zcndesc,
//                "imp", imp);
//        JSONObject json = qt.setJson("tokData", tokData,
//                "id_CB", id_CB,
//                "id_P", id_P,
//                "id_A", id_A,
//                "wn2qty", wn2qty,
//                "index", index,
//                "locAddr", locAddr,
//                "locSpace", qt.cloneArr(locSpace),
//                "spaceQty", qt.cloneArr(spaceQty),
//                "lAT", lAT,
//                "isProc", isProc,
//                "log", jsonLog);
//        if (arrPP != null && procQty != null) {
//            json.put("arrPP", arrPP);
//            json.put("procQty", procQty);
//        }
//        arrayLsbasset.add(json);
//    }
//
//    public void setStock(JSONArray arrayLsbasset, JSONObject tokData, String id_CB, String id_P, String id_A, Double wn2qty, Integer index,
//                          String locAddr, JSONArray locSpace, JSONArray spaceQty, Integer lAT, String zcndesc, Integer imp) {
//        this.setStock(arrayLsbasset, tokData, id_CB, id_P, id_A, wn2qty, index, locAddr, locSpace, spaceQty, null, null, null, lAT, zcndesc, imp);
//    }
//
//    public void setMoney(JSONArray arrayLsbasset, JSONObject tokData, String id_CB, String id_P, String id_A, Double wn2qty, Integer lAT, JSONObject action,
//                               JSONObject oMoney, Integer index, String zcndesc, Integer imp, String listType) {
//        String id_C = tokData.getString("id_C");
//        Asset auth = qt.getConfig(id_C, "a-auth", "def");
//        System.out.println("auth=" + auth);
//        if (auth.getDef().getJSONObject("objlType").getJSONObject("bisList").getBoolean("lBAsset")) {
//            if (id_A == null) {
//                JSONArray filterArray = qt.setESFilt("id_C", id_C, "id_CB", id_CB, "id_P", id_P);
//                JSONArray arrayEs = qt.getES(listType, filterArray);
//                if (arrayEs.size() > 0) {
//                    id_A = arrayEs.getJSONObject(0).getString("id_A");
//                }
//            }
//            String id_OP = "";
//            if (index != null) {
//                id_OP = action.getJSONArray("objAction").getJSONObject(index).getString("id_OP");
//            }
////        String id = action.getJSONObject("grpBGroup").getJSONObject(oMoney.getString("grpB")).getString("id_Money");
////        String id_FS = action.getJSONObject("grpGroup").getJSONObject(oMoney.getString("grp")).getString("id_Money");
//            String id = "1000";
//            String id_FS = "1000";
//            JSONObject jsonLog = qt.setJson("id", id,
//                    "id_FS", id_FS,
//                    "grpB", oMoney.getString("grpB"),
//                    "grp", oMoney.getString("grp"),
//                    "id_OP", id_OP,
//                    "index", index,
//                    "id_CS", id_C,
//                    "zcndesc", zcndesc,
//                    "imp", imp);
//            JSONObject json = qt.setJson("tokData", tokData,
//                    "id_CB", id_CB,
//                    "id_P", id_P,
//                    "id_A", id_A,
//                    "wn2qty", wn2qty,
//                    "lAT", lAT,
//                    "log", jsonLog);
//            arrayLsbasset.add(json);
//        }
//    }
//
//    //type：resv:预约 proc:工序 空字符串:普通
//    public void updateAsset(Order order, JSONArray arrayLsasset, JSONArray arrayLbasset, String type) {
//        HashSet setId_A = new HashSet();
//        HashSet setId_P = new HashSet();
//        HashSet setAuthId = new HashSet();
//        JSONArray arrayId_A = new JSONArray();
//        JSONArray arrayId_AB = new JSONArray();
//        for (int i = 0; i < arrayLsasset.size(); i++) {
//            JSONObject jsonLsasset = arrayLsasset.getJSONObject(i);
//            String id_A = jsonLsasset.getString("id_A");
//            if (id_A != null && !id_A.equals("")) {
//                arrayId_A.add(id_A);
//                setId_A.add(id_A);
//            }
//            setId_P.add(jsonLsasset.getString("id_P"));
//
//            String id_C = jsonLsasset.getJSONObject("tokData").getString("id_C");
//            setAuthId.add(qt.getId_A(id_C, "a-auth"));
//        }
//        for (int i = 0; i < arrayLbasset.size(); i++) {
//            JSONObject jsonLbasset = arrayLbasset.getJSONObject(i);
//            String id_A = jsonLbasset.getString("id_A");
//            if (id_A != null && !id_A.equals("")) {
//                arrayId_AB.add(id_A);
//                if (!jsonLbasset.getJSONObject("tokData").getString("id_C").equals(jsonLbasset.getString("id_C"))) {
//                    arrayId_A.add(id_A);
//                }
//                setId_A.add(id_A);
//            }
//            setId_P.add(jsonLbasset.getString("id_P"));
//            String id_C = jsonLbasset.getJSONObject("tokData").getString("id_C");
//            setAuthId.add(qt.getId_A(id_C, "a-auth"));
//        }
//        JSONArray filterArray = qt.setESFilt("id_A", "contain", arrayId_A);
//        JSONArray filterArrayB = qt.setESFilt("id_A", "contain", arrayId_AB);
//        JSONArray arrayLsa = qt.getES("lSAsset", filterArray);
//        JSONArray arrayLba = qt.getES("lBAsset", filterArrayB);
//        JSONObject jsonLsas = qt.arr2Obj(arrayLsa, "id_A");
//        JSONObject jsonLbas = qt.arr2Obj(arrayLba, "id_A");
//
//        List<?> assets = qt.getMDContentMany(setId_A, Arrays.asList("info", "aStock"), Asset.class);
//        JSONObject allAssetObj = qt.list2Obj(assets, "id");
//
//        List<?> prods = qt.getMDContentMany(setId_P, Arrays.asList("info", "qtySafex"), Prod.class);
//        JSONObject allProdObj = qt.list2Obj(prods, "id");
//
//        List<Asset> auths = qt.getMDContentMany(setAuthId, Arrays.asList("info", "def"), Asset.class);
//        JSONObject allAuthObj = new JSONObject();
//        for (Asset auth : auths) {
//            allAuthObj.put(auth.getInfo().getId_C(), auth);
//        }
//
//        for (int i = 0; i < arrayLsasset.size(); i++) {
//            JSONObject jsonLsasset = arrayLsasset.getJSONObject(i);
//            String id_A = jsonLsasset.getString("id_A");
//            String id_P = jsonLsasset.getString("id_P");
//            String id_C = jsonLsasset.getJSONObject("tokData").getString("id_C");
//
//            if (jsonLsas.getJSONObject(id_A) != null) {
//                jsonLsasset.put("jsonLsa", jsonLsas.getJSONObject(id_A));
//                jsonLsasset.put("jsonAsset", allAssetObj.getJSONObject(id_A));
//            }
//            JSONObject jsonProd = allProdObj.getJSONObject(id_P);
//            jsonLsasset.put("jsonProd", jsonProd);
//
//            String defKey = null;
//
//            if (jsonProd == null) {
//                jsonLsasset.put("grp", "1000");
//                jsonLsasset.put("lUT", 0);
//                jsonLsasset.put("lCR", 0);
//            } else {
//                JSONObject info = jsonProd.getJSONObject("info");
//
//                String grpP =  info.getString("grp");
//
//                if (jsonLsasset.getInteger("lAT").equals(3))
//                {
//                    qt.errPrint("ia m in updateAsset.... ");
//                    grpP = qt.getESItem("lBProd", id_P, id_C, lBProd.class).getGrpB();
//                    defKey = "objlBP";
//                } else if (jsonLsasset.getInteger("lAT").equals(2))
//                {
//                    grpP = qt.getESItem("lSProd", id_P, id_C, lSProd.class).getGrp();
//                    defKey = "objlSP";
//                }
//
//                Integer lUT = info.getInteger("lUT");
//                Integer lCR = info.getInteger("lCR");
//                if (allAuthObj.getJSONObject(id_C) != null) {
//                    JSONObject def = allAuthObj.getJSONObject(id_C).getJSONObject("def");
//                    if (grpP == null || grpP.isEmpty() || def.getJSONObject(defKey).getJSONObject(grpP) == null) {
//                        jsonLsasset.put("grp", "1000");
//                    } else {
//                        jsonLsasset.put("grp", def.getJSONObject(defKey).getJSONObject(grpP).getString("grpA"));
//                    }
//                } else {
//                    jsonLsasset.put("grp", "1000");
//                }
//
//                if (lUT == null) {
//                    jsonLsasset.put("lUT", 0);
//                } else {
//                    jsonLsasset.put("lUT", lUT);
//                }
//                if (lCR == null) {
//                    jsonLsasset.put("lCR", 0);
//                } else {
//                    jsonLsasset.put("lCR", lCR);
//                }
//            }
//        }
//        for (int i = 0; i < arrayLbasset.size(); i++) {
//            JSONObject jsonLbasset = arrayLbasset.getJSONObject(i);
//            String id_A = jsonLbasset.getString("id_A");
//            String id_P = jsonLbasset.getString("id_P");
//            String id_C = jsonLbasset.getJSONObject("tokData").getString("id_C");
//
//            if (jsonLbas.getJSONObject(id_A) != null) {
//                jsonLbasset.put("jsonLsa", jsonLbas.getJSONObject(id_A));
//                jsonLbasset.put("jsonAsset", allAssetObj.getJSONObject(id_A));
//                if (!jsonLbasset.getJSONObject("tokData").getString("id_C").equals(jsonLbasset.getString("id_C"))) {
//                    jsonLbasset.put("jsonLba", jsonLsas.getJSONObject(id_A));
//                }
//            }
//            JSONObject jsonProd = allProdObj.getJSONObject(id_P);
//            jsonLbasset.put("jsonProd", jsonProd);
//
//            String defKey = null;
//            //id_C相同id_CB不同：lSProd
//            if (id_C.equals(order.getInfo().getId_C()) && !id_C.equals(order.getInfo().getId_CB())) {
//                defKey = "objlSP";
//            } else {
//                defKey = "objlBP";
//            }
//            if (allProdObj.getJSONObject(id_P) == null || allAssetObj.getJSONObject(id_C) == null) {
//                jsonLbasset.put("grp", "");
//            } else {
//                JSONObject info = jsonProd.getJSONObject("info");
//                String grpP = info.getString("grp");
//                Integer lUT = info.getInteger("lUT");
//                Integer lCR = info.getInteger("lCR");
//                JSONObject def = allAssetObj.getJSONObject(id_C).getJSONObject("def");
//                if (grpP == null || grpP.isEmpty() || def.getJSONObject(defKey).getJSONObject(grpP) == null) {
//                    jsonLbasset.put("grp", "1000");
//                } else {
//                    jsonLbasset.put("grp", def.getJSONObject(defKey).getJSONObject(grpP).getString("grpA"));
//                }
//                if (lUT == null || def.getJSONArray("lUT").getInteger(lUT) == null) {
//                    jsonLbasset.put("lUT", 0);
//                } else {
//                    jsonLbasset.put("lUT", lUT);
//                }
//                if (lCR == null || def.getJSONArray("lCR").getInteger(lCR) == null) {
//                    jsonLbasset.put("lCR", 0);
//                } else {
//                    jsonLbasset.put("lCR", lCR);
//                }
//            }
//        }
//        System.out.println("arraylsasset=" + arrayLsasset);
//        System.out.println("arrayLbasset=" + arrayLbasset);
//
//        List<JSONObject> listBulkAsset = new ArrayList<>();
//        List<JSONObject> listBulkLsasset = new ArrayList<>();
//        List<JSONObject> listBulkLbasset = new ArrayList<>();
//        //处理arrayLsasset
//        this.assetValueChange(order, arrayLsasset, listBulkAsset, listBulkLsasset, null, type, true);
//        //处理arrayLbasset
//        this.assetValueChange(order, arrayLbasset, listBulkAsset, listBulkLbasset, listBulkLsasset, type, false);
//        qt.errPrint(" grp", null, arrayLsasset);
//
//        qt.setMDContentMany(listBulkAsset, Asset.class);
//        qt.setESMany("lSAsset", listBulkLsasset);
//        qt.setESMany("lBAsset", listBulkLbasset);
//    }
//
//
//    public void assetValueChange(Order order, JSONArray arrayAssetChg, List<JSONObject> listBulkAsset, List<JSONObject> listBulkLsasset,
//                          List<JSONObject> listBulkLbasset, String type, Boolean isLsa) {
//
//        String id_O = order.getId();
//        JSONArray arrayOItem = order.getOItem().getJSONArray("objItem");
//        for (int i = 0; i < arrayAssetChg.size(); i++) {
//            JSONObject assetChgObj = arrayAssetChg.getJSONObject(i);
//            JSONObject tokData = assetChgObj.getJSONObject("tokData");
//            String id_C = tokData.getString("id_C");
//            String id_U = tokData.getString("id_U");
//            String grpU = tokData.getString("grpU");
//            String id_CB = assetChgObj.getString("id_CB");
//            JSONObject jsonLog = assetChgObj.getJSONObject("log");
//            Integer index = assetChgObj.getInteger("index");
//            String id_P = assetChgObj.getString("id_P");
//            Double wn2qty = assetChgObj.getDouble("wn2qty");
//            Integer lUT = assetChgObj.getInteger("lUT");
//            Integer lAT = assetChgObj.getInteger("lAT");
//            String grp = assetChgObj.getString("grp");
//            JSONObject jsonBulkAsset = null;
//            JSONObject jsonBulkLsasset = null;
//            String id_A = null;
//            //index不为空是产品，反之是金钱
//            // basically depends on what arrayAssetChg is, it will update asset accordingly, so this array is very important instruction
//            // type 1 = deduct/add qty by stocks
//            // type 2 = init stocks + qty add into it
//            // ??? what if deduct but
//            // type 3 = deduct/add money
//
//
//
//            //K - should use lAT to check if this is Stock
//            JSONObject prod = assetChgObj.getJSONObject("jsonProd");
//            qt.errPrint("prodInfo", prod, assetChgObj);
//            if (prod == null)
//            {
//                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_FOUND.getCode(), null);
//            }
//            JSONObject prodInfo = prod.getJSONObject("info");
//            Integer lCR = prodInfo.getInteger("lCR");
//            if (assetChgObj.getString("locAddr") != null) {
//                JSONObject jsonOItem = arrayOItem.getJSONObject(index);
//                Double wn4price = jsonOItem.getDouble("wn4price");
//                Double wn4value = DoubleUtils.multiply(wn2qty, wn4price);
//                String locAddr = assetChgObj.getString("locAddr");
//                JSONArray arrayUpdateLocSpace = assetChgObj.getJSONArray("locSpace");
//                JSONArray arrayUpdateSpaceQty = assetChgObj.getJSONArray("spaceQty");
//                Boolean isProc = assetChgObj.getBoolean("isProc");
//                JSONObject wrdN = jsonOItem.getJSONObject("wrdN");
//                //Type 1: 存在资产
//                if (isProc != null) {
//                    if (isProc) {
//                        wrdN.put("cn", "【已加工】" + wrdN.getString("cn"));
//                    } else {
//                        wrdN.put("cn", "【未加工】" + wrdN.getString("cn"));
//                    }
//                }
//                if (assetChgObj.getJSONObject("jsonLsa") != null)
//                {
//                    JSONObject jsonLsa = assetChgObj.getJSONObject("jsonLsa");
//                    id_A = jsonLsa.getString("id_A");
//                    JSONObject jsonAsset = assetChgObj.getJSONObject("jsonAsset");
//                    JSONObject aStock = jsonAsset.getJSONObject("aStock");
//                    JSONArray arrayLocSpace = aStock.getJSONArray("locSpace");
//                    JSONArray arraySpaceQty = aStock.getJSONArray("spaceQty");
//
//                    System.out.println("arrayLocSpace=" + arrayLocSpace);
//                    System.out.println("arraySpaceQty=" + arraySpaceQty);
//                    System.out.println("arrayUpdateLocSpace=" + arrayUpdateLocSpace);
//                    System.out.println("arrayUpdateSpaceQty=" + arrayUpdateSpaceQty);
//                    JSONArray arrayResultLocSpace = new JSONArray();
//                    JSONArray arrayResultSpaceQty = new JSONArray();
//                    for (int j = 0; j < arrayLocSpace.size(); j++) {
//                        for (int k = 0; k < arrayUpdateLocSpace.size(); k++) {
//                            Integer locSpace = arrayLocSpace.getInteger(j);
//                            Double spaceQty = arraySpaceQty.getDouble(j);
//                            Integer updateLocSpace = arrayUpdateLocSpace.getInteger(k);
//                            //移动数量，移入正数，移出负数
//                            Double updateSpaceQty = arrayUpdateSpaceQty.getDouble(k);
//                            //格子相等
//                            if (locSpace == updateLocSpace) {
//                                System.out.println("=,locSpace=" + locSpace + ",updateLocSpace=" + updateLocSpace);
//                                Double qty = DoubleUtils.add(spaceQty, updateSpaceQty);
//                                System.out.println("spaceQty=" + spaceQty);
//                                System.out.println("updateSpaceQty=" + updateSpaceQty);
//                                //货架数量小于移动数量
//                                if (DoubleUtils.doubleGt(0, qty)) {
//                                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_ENOUGH.getCode(), null);
//                                }
//                                //大于
//                                if (DoubleUtils.doubleGt(qty, 0)) {
//                                    arrayResultLocSpace.add(updateLocSpace);
//                                    arrayResultSpaceQty.add(qty);
//                                }
//                                arrayLocSpace.remove(0);
//                                arraySpaceQty.remove(0);
//                                arrayUpdateLocSpace.remove(0);
//                                arrayUpdateSpaceQty.remove(0);
//                                j --;
//                                k --;
//                                break;
//                            }
//                            //移动格子小
//                            else if (locSpace > updateLocSpace) {
//                                if (DoubleUtils.doubleGt(0, updateSpaceQty)) {
//                                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_ENOUGH.getCode(), null);
//                                }
//                                System.out.println(">,locSpace=" + locSpace + ",updateLocSpace=" + updateLocSpace);
//                                arrayResultLocSpace.add(updateLocSpace);
//                                arrayResultSpaceQty.add(updateSpaceQty);
//                                arrayUpdateLocSpace.remove(0);
//                                arrayUpdateSpaceQty.remove(0);
//                                k --;
//                            }
//                            //移动格子大
//                            else if (locSpace < updateLocSpace) {
//                                System.out.println("<,locSpace=" + locSpace + ",updateLocSpace=" + updateLocSpace);
//                                arrayResultLocSpace.add(locSpace);
//                                arrayResultSpaceQty.add(spaceQty);
//                                arrayLocSpace.remove(0);
//                                arraySpaceQty.remove(0);
//                                j --;
//                                break;
//                            }
//                        }
//                    }
//                    System.out.println("arrayResultLocSpace=" + arrayResultLocSpace);
//                    System.out.println("arrayResultSpaceQty=" + arrayResultSpaceQty);
//                    if (arrayLocSpace.size() != 0) {
//                        arrayResultLocSpace.addAll(arrayLocSpace);
//                        arrayResultSpaceQty.addAll(arraySpaceQty);
//                    }
//                    if (arrayUpdateLocSpace.size() != 0) {
//                        for (int j = 0; j < arrayUpdateSpaceQty.size(); j++) {
//                            if (DoubleUtils.doubleGt(0, arrayUpdateSpaceQty.getDouble(j))) {
//                                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_ENOUGH.getCode(), null);
//                            }
//                        }
//                        arrayResultLocSpace.addAll(arrayUpdateLocSpace);
//                        arrayResultSpaceQty.addAll(arrayUpdateSpaceQty);
//                    }
//                    System.out.println("arrayResultLocSpace=" + arrayResultLocSpace);
//                    System.out.println("arrayResultSpaceQty=" + arrayResultSpaceQty);
//
//
//                    // what is else
//                    if ("resv".equals(type) && aStock.getJSONObject("resvQty") != null &&
//                            aStock.getJSONObject("resvQty").getDouble(id_O + "-" + index) != null) {
//                        ///////************SET - aStock resvAsset qty **************//////////
//                        Double remain = DoubleUtils.add(aStock.getDouble("wn2qtyResv"), wn2qty);
//
//                        if (aStock.getDouble("wn2qty") == 0 && remain == 0) {
//                            jsonBulkAsset = qt.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = qt.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        }
//                        else {
//                            //check if fromSum == resvQty.wn2qty, if so remove that object, else deduct
//                            JSONObject jsonResvQty = aStock.getJSONObject("resvQty");
//                            if (DoubleUtils.doubleEquals(jsonResvQty.getDouble(id_O + "-" + index), wn2qty)) {
//                                jsonResvQty.remove(id_O + "-" + index);
//                            } else {
//                                jsonResvQty.put(id_O + "-" + index, DoubleUtils.add(jsonResvQty.getDouble(id_O + "-" + index), wn2qty));
//                            }
//
//                            AssetAStock assetAStock = new AssetAStock(
//                                    wn4price, locAddr, arrayResultLocSpace, arrayResultSpaceQty, remain, jsonResvQty);
//                            assetAStock.setLUT(lUT);
//                            assetAStock.setLCR(lCR);
//                            JSONObject jsonUpdate = qt.setJson("aStock", assetAStock);
//                            jsonBulkAsset = qt.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            qt.upJson(jsonLsa,
//                                    //"wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayResultLocSpace,
//                                    "spaceQty", arrayResultSpaceQty,
//                                    "wn2qtyResv", remain,
//                                    "lUT", lUT,
//                                    "lCR", lCR);
//                            jsonBulkLsasset = qt.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                    else if ("proc".equals(type)) {
//                        if (DoubleUtils.doubleEquals(aStock.getDouble("wn2qty"), -wn2qty)) {
//                            jsonBulkAsset = qt.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = qt.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        } else {
//                            JSONArray arrayProcQty = assetChgObj.getJSONArray("procQty");
//                            JSONArray arrayArrPPA = aStock.getJSONArray("arrPP");
//                            JSONArray arrayProcQtyA = aStock.getJSONArray("procQty");
//                            System.out.println("arrayProcQty=" + arrayProcQty);
//                            System.out.println("arrayProcQtyA1=" + arrayProcQtyA);
//                            for (int j = 0; j < arrayProcQty.size(); j++) {
//                                arrayProcQtyA.set(j, DoubleUtils.add(arrayProcQtyA.getDouble(j), arrayProcQty.getDouble(j)));
//                            }
//                            System.out.println("arrayProcQtyA2=" + arrayProcQtyA);
//                            AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayResultLocSpace, arrayResultSpaceQty, arrayArrPPA, arrayProcQtyA);
//                            assetAStock.setLUT(lUT);
//                            assetAStock.setLCR(lCR);
//                            JSONObject jsonUpdate = qt.setJson("aStock", assetAStock);
//                            jsonBulkAsset = qt.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            qt.upJson(jsonLsa, "wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayResultLocSpace,
//                                    "spaceQty", arrayResultSpaceQty,
//                                    "arrPP", arrayArrPPA,
//                                    "procQty", arrayProcQtyA,
//                                    "lUT", lUT,
//                                    "lCR", lCR);
//                            jsonBulkLsasset = qt.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                    else if ("part".equals(type)) {
//                        if (DoubleUtils.doubleEquals(aStock.getDouble("wn4price"), -wn4price)) {
//                            jsonBulkAsset = qt.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = qt.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        } else {
//                            JSONArray arrayWn2qty = assetChgObj.getJSONArray("procQty");
//                            JSONArray arrayArrPPA = aStock.getJSONArray("arrPP");
//                            JSONArray arrayWn2qtyA = aStock.getJSONArray("arrayWn2qty");
//                            System.out.println("arrayProcQty=" + arrayWn2qty);
//                            System.out.println("arrayWn2qtyA1=" + arrayWn2qtyA);
//                            for (int j = 0; j < arrayWn2qty.size(); j++) {
//                                arrayWn2qtyA.set(j, DoubleUtils.add(arrayWn2qtyA.getDouble(j), arrayWn2qty.getDouble(j)));
//                            }
//                            System.out.println("arrayWn2qtyA2=" + arrayWn2qtyA);
//                            AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayResultLocSpace, arrayResultSpaceQty, arrayArrPPA, arrayWn2qtyA);
//                            assetAStock.setLUT(lUT);
//                            assetAStock.setLCR(lCR);
//                            JSONObject jsonUpdate = qt.setJson("aStock", assetAStock);
//                            jsonBulkAsset = qt.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            qt.upJson(jsonLsa, "wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayResultLocSpace,
//                                    "spaceQty", arrayResultSpaceQty,
//                                    "arrPP", arrayArrPPA,
//                                    "procQty", arrayWn2qtyA,
//                                    "lUT", lUT,
//                                    "lCR", lCR);
//                            jsonBulkLsasset = qt.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                    else if ("safex".equals(type)) {
//                        JSONObject jsonId_C = prod.getJSONObject("qtySafex").getJSONObject(id_C);
//                        JSONArray arraySafex = jsonId_C.getJSONArray("objSafex");
//                        JSONObject jsonResv = new JSONObject();
//                        Double wn2sum = wn2qty;
//                        for (int j = 0; j < arraySafex.size(); j++) {
//                            JSONObject jsonSafex = arraySafex.getJSONObject(j);
//                            Double wn2safex = jsonSafex.getDouble("wn2qty");
//                            if (DoubleUtils.doubleGte(wn2sum, wn2safex)) {
//                                wn2sum = DoubleUtils.subtract(wn2sum, wn2safex);
//                                jsonResv.put(jsonSafex.getString("id_O") + "-" + jsonSafex.getInteger("index"), wn2safex);
//                                arraySafex.remove(j);
//                                j --;
//                            } else if (DoubleUtils.doubleGte(wn2sum, 0.0)) {
//                                jsonResv.put(jsonSafex.getString("id_O") + "-" + jsonSafex.getInteger("index"), wn2sum);
//                                jsonSafex.put("wn2qty", wn2safex - wn2sum);
//                                break;
//                            }
//                        }
//                        JSONObject jsonUpdate = qt.setJson("qtySafex." + id_C, jsonId_C);
//                        qt.setMDContent(id_P, jsonUpdate, Prod.class);
//
//                        if (DoubleUtils.doubleEquals(aStock.getDouble("wn2qty"), -wn2qty)) {
//                            jsonBulkAsset = qt.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = qt.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        } else {
//                            AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayResultLocSpace, arrayResultSpaceQty, DoubleUtils.subtract(wn2qty, wn2sum), jsonResv);
//                            assetAStock.setLUT(lUT);
//                            assetAStock.setLCR(lCR);
//                            jsonUpdate = qt.setJson("aStock", assetAStock);
//                            jsonBulkAsset = qt.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            qt.upJson(jsonLsa, "wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayResultLocSpace,
//                                    "spaceQty", arrayResultSpaceQty,
//                                    "lUT", lUT,
//                                    "lCR", lCR);
//                            jsonBulkLsasset = qt.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                    else {
//                        if (DoubleUtils.doubleEquals(aStock.getDouble("wn2qty"), -wn2qty)) {
//                            jsonBulkAsset = qt.setJson("type", "delete",
//                                    "id", id_A);
//                            jsonBulkLsasset = qt.setJson("type", "delete",
//                                    "id", jsonLsa.getString("id_ES"));
//                        } else {
//                            AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayResultLocSpace, arrayResultSpaceQty);
//                            assetAStock.setLUT(lUT);
//                            assetAStock.setLCR(lCR);
//                            JSONObject jsonUpdate = qt.setJson("aStock", assetAStock);
//                            jsonBulkAsset = qt.setJson("type", "update",
//                                    "id", id_A,
//                                    "update", jsonUpdate);
//
//                            qt.upJson(jsonLsa, "wn2qty", DoubleUtils.add(aStock.getDouble("wn2qty"), wn2qty),
//                                    "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn4value),
//                                    "locSpace", arrayResultLocSpace,
//                                    "spaceQty", arrayResultSpaceQty,
//                                    "lUT", lUT,
//                                    "lCR", lCR);
//                            jsonBulkLsasset = qt.setJson("type", "update",
//                                    "id", jsonLsa.getString("id_ES"),
//                                    "update", jsonLsa);
//                        }
//                    }
//                }
//                //不存在资产，新增资产
//                else { //Type 2: add new
//                    Asset asset = new Asset();
//                    id_A = qt.GetObjectId();
//                    asset.setId(id_A);
////                    String grp = "";
////                    if (lAT == 2) {
////                        grp = "1001";
////                    } else if (lAT == 3) {
////                        grp = "1005";
////                    }
//                    AssetInfo assetInfo = new AssetInfo(id_C, id_C, id_P, wrdN,
//                            jsonOItem.getJSONObject("wrddesc"), grp, jsonOItem.getString("ref"),
//                            jsonOItem.getString("pic"), lAT);
//                    asset.setInfo(assetInfo);
//
//                    lSAsset lsasset = new lSAsset(id_A, id_C, id_C, id_C, id_P, wrdN,
//                            jsonOItem.getJSONObject("wrddesc"), grp, jsonOItem.getString("pic"),
//                            jsonOItem.getString("ref"), lAT, wn2qty, wn4price);
//                    lsasset.setLocAddr(locAddr);
//                    lsasset.setLocSpace(arrayUpdateLocSpace);
//                    lsasset.setSpaceQty(arrayUpdateSpaceQty);
//                    lsasset.setLUT(lUT);
//                    lsasset.setLCR(lCR);
//                    if ("proc".equals(type)) {
//                        JSONArray arrayArrPP = assetChgObj.getJSONArray("arrPP");
//                        JSONArray arrayProcQty = assetChgObj.getJSONArray("procQty");
//                        System.out.println("arrayProcQty=" + arrayProcQty);
//                        AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayUpdateLocSpace, arrayUpdateSpaceQty, arrayArrPP, arrayProcQty);
//                        assetAStock.setLUT(lUT);
//                        assetAStock.setLCR(lCR);
//                        asset.setAStock(qt.toJson(assetAStock));
//                        asset.setView(qt.setArray("info", "aStock"));
//                        jsonBulkAsset = qt.setJson("type", "insert",
//                                "insert", asset);
//
//                        lsasset.setArrPP(arrayArrPP);
//                        lsasset.setProcQty(arrayProcQty);
//                    } else if ("part".equals(type)) {
//                        JSONArray arrayArrPP = assetChgObj.getJSONArray("arrPP");
//                        JSONArray arrayProcQty = assetChgObj.getJSONArray("procQty");
//                        System.out.println("arrayProcQty=" + arrayProcQty);
//                        AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayUpdateLocSpace, arrayUpdateSpaceQty, arrayArrPP, arrayProcQty);
//                        assetAStock.setLUT(lUT);
//                        assetAStock.setLCR(lCR);
//                        asset.setAStock(qt.toJson(assetAStock));
//                        asset.setView(qt.setArray("info", "aStock"));
//                        jsonBulkAsset = qt.setJson("type", "insert",
//                                "insert", asset);
//
//                        lsasset.setArrPP(arrayArrPP);
//                        lsasset.setProcQty(arrayProcQty);
//                    } else {
//                        AssetAStock assetAStock = new AssetAStock(wn4price, locAddr, arrayUpdateLocSpace, arrayUpdateSpaceQty);
//                        assetAStock.setLUT(lUT);
//                        assetAStock.setLCR(lCR);
//                        asset.setAStock(qt.toJson(assetAStock));
//                        asset.setView(qt.setArray("info", "aStock"));
//                        jsonBulkAsset = qt.setJson("type", "insert",
//                                "insert", asset);
//                    }
//
//                    jsonBulkLsasset = qt.setJson("type", "insert",
//                            "insert", lsasset);
//
//                }
//                listBulkAsset.add(jsonBulkAsset); // insert asset(MD)
//                listBulkLsasset.add(jsonBulkLsasset); //insert lSAsset(ES)
//
//                LogFlow log = new LogFlow(tokData, jsonOItem, order.getAction(),
//                        order.getInfo().getId_CB(), id_O, index, "assetflow", "stoChg",
//                        wrdN.getString("cn") + jsonLog.getString("zcndesc"),
//                        jsonLog.getInteger("imp"));
//                log.setLogData_assetflow(wn2qty, wn4price, id_A, grp);
//                System.out.println("assetflow=" + JSON.toJSON(log));
//               ws.sendWS(log);
//            }
//            else {
//                //存在金钱
//                if (assetChgObj.getJSONObject("jsonLsa") != null) {
//                    JSONObject jsonLsa = assetChgObj.getJSONObject("jsonLsa");
//                    id_A = jsonLsa.getString("id_A");
//                    JSONObject jsonAsset = assetChgObj.getJSONObject("jsonAsset");
//                    JSONObject aStock = jsonAsset.getJSONObject("aStock");
//                    AssetAStock assetAStock = new AssetAStock(DoubleUtils.add(aStock.getDouble("wn4price"), wn2qty));
//                    assetAStock.setLUT(lUT);
//                    assetAStock.setLCR(lCR);
//                    JSONObject jsonUpdate = qt.setJson("aStock", assetAStock);
//                    jsonBulkAsset = qt.setJson("type", "update",
//                            "id", id_A,
//                            "update", jsonUpdate);
//
//                    if (!isLsa && !id_C.equals(id_CB)) {
//                        JSONObject jsonLba = assetChgObj.getJSONObject("jsonLba");
//                        System.out.println("jsonLba=" + jsonLba);
//                        System.out.println("wn4price=" + aStock.getDouble("wn4price"));
//                        System.out.println("wn2qty=" + wn2qty);
//                        System.out.println("wn4value=" + aStock.getDouble("wn4value"));
//                        System.out.println("lUT=" + lUT);
//                        System.out.println("lCR=" + lCR);
//                        qt.upJson(jsonLba, "wn4price", DoubleUtils.add(aStock.getDouble("wn4price"), wn2qty),
//                                "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn2qty),
//                                "lUT", lUT,
//                                "lCR", lCR);
//                        JSONObject jsonBulkLbasset = qt.setJson("type", "update",
//                                "id", jsonLba.getString("id_ES"),
//                                "update", jsonLba);
//                        listBulkLbasset.add(jsonBulkLbasset);
//                    }
//
//                    qt.upJson(jsonLsa, "wn4price", DoubleUtils.add(aStock.getDouble("wn4price"), wn2qty),
//                            "wn4value", DoubleUtils.add(aStock.getDouble("wn4value"), wn2qty),
//                            "lUT", lUT,
//                            "lCR", lCR);
//                    jsonBulkLsasset = qt.setJson("type", "update",
//                            "id", jsonLsa.getString("id_ES"),
//                            "update", jsonLsa);
//                }
//                //不存在金钱，新增
//                else {
//                    Asset asset = new Asset();
//                    id_A = qt.GetObjectId();
//                    asset.setId(id_A);
////                    JSONObject prodInfo = allProdObj.getJSONObject(id_P).getJSONObject("info");
//                    AssetInfo assetInfo = new AssetInfo(id_C, id_C, id_P, prodInfo.getJSONObject("wrdN"),
//                            prodInfo.getJSONObject("wrddesc"), grp, prodInfo.getString("ref"),
//                            prodInfo.getString("pic"), lAT);
//                    asset.setInfo(assetInfo);
//                    AssetAStock assetAStock = new AssetAStock(wn2qty);
//                    assetAStock.setLUT(lUT);
//                    assetAStock.setLCR(lCR);
//                    asset.setAStock((JSONObject) JSON.toJSON(assetAStock));
//                    JSONArray view = qt.setArray("info", "aStock");
//                    asset.setView(view);
//                    jsonBulkAsset = qt.setJson("type", "insert",
//                            "insert", asset);
//
//                    if (isLsa) {
//                        lSAsset lsasset = new lSAsset(id_A, id_C, id_C, id_CB, id_P, prodInfo.getJSONObject("wrdN"),
//                                prodInfo.getJSONObject("wrddesc"), grp, prodInfo.getString("ref"),
//                                prodInfo.getString("pic"), lAT, 1.0, wn2qty);
//                        lsasset.setLUT(lUT);
//                        lsasset.setLCR(lCR);
//                        jsonBulkLsasset = qt.setJson("type", "insert",
//                                "insert", lsasset);
//                    } else {
//                        if (!id_C.equals(id_CB)) {
//                            lSAsset lsasset = new lSAsset(id_A, id_CB, id_CB, id_C, id_P, prodInfo.getJSONObject("wrdN"),
//                                    prodInfo.getJSONObject("wrddesc"), grp, prodInfo.getString("pic"),
//                                    prodInfo.getString("ref"), lAT, 1.0, wn2qty);
//                            lsasset.setLUT(lUT);
//                            lsasset.setLCR(lCR);
//                            JSONObject jsonBulkLbasset = qt.setJson("type", "insert",
//                                    "insert", lsasset);
//                            listBulkLbasset.add(jsonBulkLbasset);
//                        }
//                        lBAsset lbasset = new lBAsset(id_A, id_C, id_C, id_CB, id_P, prodInfo.getJSONObject("wrdN"),
//                                prodInfo.getJSONObject("wrddesc"), grp, prodInfo.getString("pic"),
//                                prodInfo.getString("ref"), lAT, 1.0, wn2qty);
//                        lbasset.setLUT(lUT);
//                        lbasset.setLCR(lCR);
//                        jsonBulkLsasset = qt.setJson("type", "insert",
//                                "insert", lbasset);
//                    }
//                }
//
//                listBulkAsset.add(jsonBulkAsset);
//                listBulkLsasset.add(jsonBulkLsasset);
//
//                LogFlow log = new LogFlow("moneyflow", jsonLog.getString("id"), jsonLog.getString("id_FS"),
//                        "stoChg", id_U, grpU, id_P, jsonLog.getString("grpB"), jsonLog.getString("grp"),
//                        jsonLog.getString("id_OP"), id_O, jsonLog.getInteger("index"), id_C,
//                        jsonLog.getString("id_CS"), prodInfo.getString("pic"), tokData.getString("dep"),
//                        prodInfo.getJSONObject("wrdN").getString("cn") + jsonLog.getString("zcndesc"),
//                        jsonLog.getInteger("imp"), prodInfo.getJSONObject("wrdN"), tokData.getJSONObject("wrdNU"));
//                log.setLogData_money(id_A, "", wn2qty);
//                System.out.println("moneyflow=" + JSON.toJSON(log));
//
//                ws.sendWS(log);
//            }
//        }
//    }
//
//    public void setBulkInsert(List<JSONObject> listBulk, Object obj) {
//        JSONObject jsonBulk = qt.setJson("type", "insert",
//                "insert", obj);
//        listBulk.add(jsonBulk);
//    }
//    public void setBulkDelete(List<JSONObject> listBulk, String id) {
//        JSONObject jsonBulk = qt.setJson("type", "delete",
//                "id", id);
//        listBulk.add(jsonBulk);
//    }
//    public void setBulkUpdate(List<JSONObject> listBulk, String id, JSONObject json) {
//        JSONObject jsonBulk = qt.setJson("type", "update",
//                "id", id,
//                "update", json);
//        listBulk.add(jsonBulk);
//    }
//
//}
