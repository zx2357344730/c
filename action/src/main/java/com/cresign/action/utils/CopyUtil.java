package com.cresign.action.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.CosUpload;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.DbUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.uuid.UUID19;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author kevin
 * @ClassName copyUtil
 * @Description
 * @updated 2024/7/9 12:12
 * @return
 * @ver 1.0.0
 **/

@Component
public class CopyUtil {
    @Autowired
    private Qt qt;

    @Autowired
    private CosUpload cos;


    @Autowired
    private DbUtils dbu;


    public String tempa(String id_C, String id, String listType, String grp) {
        if ("lSOrder".equals(listType) || "lBOrder".equals(listType)) {
            Order order = qt.getMDContent(id, "", Order.class);

            JSONObject jsonCopy = copyUtil(id, id_C, grp, listType);
            JSONObject listOrder = jsonCopy.getJSONObject("es");
            String copyId = jsonCopy.getJSONObject("mongo").getString("id");
            if(!order.getTempa().equals(null)) {
                JSONObject mapResult = dbu.tempaCOUPA(order.getTempa().getJSONArray("objData"),
                        order.getTempa().getJSONObject("objVar"), listOrder, listType);

                qt.setMDContent(copyId, mapResult.getJSONObject("mongo"), Order.class);
                mapResult.getJSONObject("es").put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                qt.setES("lSBOrder", qt.setESFilt("id_O", copyId), mapResult.getJSONObject("es"));
            }
            return copyId;
        }

        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.PROD_NOT_FOUND.getCode(), null);
    }
    public JSONObject copyUtil(String id, String id_C, String grp, String listType) {

//        authCheck.getUserUpdateAuth(id_U,id_C,listType,grp,"batch",new JSONArray().fluentAdd("copy"));

//             查询id
        String resetGrp;
        if ("lSOrder".equals(listType)) {
            System.out.println("lSOrder");
            resetGrp = "grp";

            // 获取到要复制的数据
//            Order sourceOrder = mongoTemplate.findOne(query, Order.class);
            Order sourceOrder = qt.getMDContent(id, "", Order.class);
            System.out.println(sourceOrder);
            // 判断为空或者lST < 8
            if (ObjectUtils.isEmpty(sourceOrder) || sourceOrder.getInfo().getLST() >= 7) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.COMP_IS_NULL_LST.getCode(), null);
            }

            sourceOrder.getView().remove("tempa");
            sourceOrder.setTempa(null);

            return copyOrder(sourceOrder, listType, id_C, grp, resetGrp);

        } else if ("lBOrder".equals(listType)) {
            System.out.println("lBOrder");
            resetGrp = "grpB";

            // 获取到要复制的数据
            Order sourceOrder = qt.getMDContent(id, "", Order.class);
            System.out.println(sourceOrder);
            // 判断为空或者lST < 8
            if (ObjectUtils.isEmpty(sourceOrder) || sourceOrder.getInfo().getLST() >= 7) {
                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.COMP_IS_NULL_LST.getCode(), null);
            }
            sourceOrder.getView().remove("tempa");
            sourceOrder.setTempa(null);

            return copyOrder(sourceOrder, listType, id_C, grp, resetGrp);

        }
        return null;
    }

    @Async
    public void copyCOSFile(String id_C, String copyId, JSONArray fileList) {


        for (int i = 0; i < fileList.size(); i++) {

            JSONObject indexData = fileList.getJSONObject(i);

            //是数组 不知道怎样判断行不行
            if (indexData.get("objFile") != null) {

                JSONArray arrFile = indexData.getJSONArray("objFile");

                for (int j = 0; j < arrFile.size(); j++) {

                    JSONObject indexFile =  arrFile.getJSONObject(j);

                    if (indexFile.get("fileSource") != null) {

                        //拷贝文件  1.原路徑 2.目标路径
                        cos.copyCFiles(indexFile.get("fileSource").toString(), id_C+"/"+copyId + "/file00s/" + i + "/" + indexFile.get("fileName"));
                    }
                }
            }
        }
    }

    public JSONObject copyOrder(Order sourceOrder,String listType,String id_C,String grp,String resetGrp){

        // 原数据的id
        String sourceId = sourceOrder.getId();

        // 复制后的数据新的id
        String copyId = qt.GetObjectId();


        if (sourceOrder.getFile00s() != null) {

            /*
            1.先计算文件大小，查询容量卡是否还能上传
            2.能上传则修改容量卡，不能则不走第三步
            3.可以的话拷贝文件(已用腾讯接口，不用阿里接口)
         */
            //计算方法
            long fileSize = 0l;

            for (int i = 0; i < sourceOrder.getFile00s().getJSONArray("objData").size(); i++) {

                JSONObject indexData = sourceOrder.getFile00s().getJSONArray("objData").getJSONObject(i);

                //是数组 不知道怎样判断行不行
                if (indexData.get("objFile") != null) {

                    JSONArray arrFile =  indexData.getJSONArray("objFile");

                    for (int j = 0; j < arrFile.size(); j++) {

                        JSONObject indexFile =  arrFile.getJSONObject(j);


                        if (indexFile.get("fileSource") != null) {

                            //查看文件大小方法
                            long zeffilN = cos.getCresignSize(indexFile.get("fileSource").toString());
                            fileSize += zeffilN;
                        }
                    }
                }
            }

            if (fileSize > 0) {

                qt.checkPowerUp(id_C, fileSize, "capacity");
                 //拷贝方法
                copyCOSFile(id_C, copyId,  sourceOrder.getFile00s().getJSONArray("objData"));

            }
        }

        JSONArray filterArray;
        // 判断如果是lSProd那id_C是自己的公司，如果是lBProd则id_C是id_CB
        if ("lSOrder".equals(listType)) {
            filterArray = qt.setESFilt("id_C", id_C, "id_O", sourceId);
        } else {
            filterArray = qt.setESFilt("id_CB", id_C, "id_O", sourceId);
        }
        String tmd = DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate());
        // 4.查询es中的这个列表的数据
        JSONArray arrayEs = qt.getES("lSBOrder", filterArray);
        JSONObject jsonEs = arrayEs.getJSONObject(0);
        qt.upJson(jsonEs, "id_O", copyId, resetGrp, grp, "tmk", tmd, "tmd", tmd);

        qt.addES("lSBOrder", jsonEs);

        sourceOrder.getInfo().setTmk(tmd);
        sourceOrder.getInfo().setTmd(tmd);
        sourceOrder.setId(copyId);

        if (resetGrp.equals("grpB"))
        {
            sourceOrder.getInfo().setGrpB(grp);
        } else
        {
            sourceOrder.getInfo().setGrp(grp);
        }

        //sourceOrder oItem all change to new id
        //sourceOrder init all action / oStock
        if (sourceOrder.getOItem() != null)
        {
            for (int i = 0; i < sourceOrder.getOItem().getJSONArray("objItem").size(); i++)
            {
                JSONObject orderItem = sourceOrder.getOItem().getJSONArray("objItem").getJSONObject(i);
                orderItem.put("id_O", copyId);
                orderItem.put("rKey", UUID19.uuid8());

                if (sourceOrder.getOItem().getJSONArray("objCard").contains("action")) {
                    dbu.initAction(orderItem, sourceOrder.getAction().getJSONArray("objAction"), i);
//                    JSONObject orderAction = sourceOrder.getAction().getJSONArray("objAction").getJSONObject(i);
//                    orderAction.put("id_O", copyId);
//                    orderAction.put("rKey", orderItem.getString("rKey"));
                }

                if (sourceOrder.getOItem().getJSONArray("objCard").contains("oStock")) {
                    dbu.initOStock(orderItem, sourceOrder.getOStock().getJSONArray("objData"), i);
//                    JSONObject orderOStock = sourceOrder.getOStock().getJSONArray("objData").getJSONObject(i);
//                    orderOStock.put("id_O", copyId);
//                    orderOStock.put("rKey", orderItem.getString("rKey"));
                }

            }
        }
        sourceOrder.setTvs(1);
        // 插入数据到mongo
        qt.addMD(sourceOrder);
        System.out.println(copyId);
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("mongo", sourceOrder);
        jsonResult.put("es", jsonEs);
        return jsonResult;
    }


}
