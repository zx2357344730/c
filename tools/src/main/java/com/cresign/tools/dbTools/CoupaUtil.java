package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoupaUtil {

    @Autowired
    private Qt qt;

    public void updateAssetByKeyAndListKeyVal(String key, Object val, JSONObject keyVal) {
        // 创建查询条件，并且添加查询条件

        qt.setMDContent(val.toString(),keyVal,Asset.class);
//        Query query = new Query(new Criteria(key).is(val));
//
//        // 创建修改对象
//        Update update = new Update();
//
//        // 循环添加修改的键和值
//        keyVal.keySet().forEach(k -> update.set(k,keyVal.get(k)));
//
//        // 调用数据库进行修改
//        mongoTemplate.updateFirst(query,update, Asset.class);
    }

    /**
     * 根据id_C和ref获取Asset的Id
     * @param id_C	公司id
     * @param ref	模块名称
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/25 15:20
     */

    //Need change to getConfig
    public String getAssetId(String id_C, String ref) {

//        System.out.println("what?"+id_C+ref);
//
//        Query getAsset = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is(ref));
//
////        String id_A = qt.getId_A(id_C, ref);
////        System.out.println(id_A+"......");
////        Query getAsset = new Query(new Criteria("_id").is(id_A));
//        getAsset.fields().include("_id");
//        Asset one = mongoTemplate.findOne(getAsset, Asset.class);
//        System.out.print("one"+one);
//        if (null != one) {
//            return one.getId();
//        } else {
//            return null;
//        }

        String result = qt.getId_A(id_C, ref);
        if (result.equals(""))
            return null;
        else
            return result;
    }

    /**
     * 根据aId获取listKey需要的信息
     * @param listKey	需要的数据集合
     * @return com.cresign.chat.pojo.po.Asset  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:29
     */
    //FIXED
    public Asset getAssetById(String id_A, List<String> listKey) {
//        Query query = new Query(new Criteria("_id").is(aId));
//        Field fields = query.fields();
//        listKey.forEach(fields::include);
        return qt.getMDContent(id_A, listKey, Asset.class);
//        return mongoTemplate.findOne(query, Asset.class);
    }

    /**
     * 根据keyVal修改oId的对应数据
     * @param oId	订单id
     * @param keyVal	需要修改的键和值
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 14:55
     */
    public void updateOrderByListKeyVal(String oId, JSONObject keyVal) {
//        ("进入修改订单方法...");
//        ("修改值:");
//        (JSON.toJSONString(keyVal));
//        ("oId:"+oId);
        // 创建查询条件，并且添加查询条件
//        Query query = new Query(new Criteria("id").is(oId));
//
//        // 创建修改对象
//        Update update = new Update();
//
//        // 循环添加修改的键和值
//        keyVal.keySet().forEach(k -> update.set(k,keyVal.get(k)));
//
//        // 调用数据库进行修改
//        mongoTemplate.updateFirst(query,update,Order.class);
        qt.setMDContent(oId, keyVal, Order.class);
    }

    public Order getOrderByListKey(String id_O, List<String> listKey) {
        // 创建查询对象，并且添加查询条件
//        Query query = new Query(new Criteria("_id").is(oId));
//
//        // 过滤对象存储
//        Field fields = query.fields();
//
//        // 添加过滤条件
//        listKey.forEach(fields::include);

        return qt.getMDContent(id_O, listKey, Order.class);

        // 返回查询结果
//        return mongoTemplate.findOne(query, Order.class);
    }


}
