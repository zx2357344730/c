//package com.cresign.tools.dbTools;
//
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.tools.pojo.po.Asset;
//import com.cresign.tools.pojo.po.Order;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class CoupaUtil {
//
//    @Autowired
//    private Qt qt;
//
//    //Need change to getConfig
//    public String getAssetId(String id_C, String ref) {
//
////        System.out.println("what?"+id_C+ref);
////
////        Query getAsset = new Query(new Criteria("info.id_C").is(id_C).and("info.ref").is(ref));
////
//////        String id_A = qt.getId_A(id_C, ref);
//////        System.out.println(id_A+"......");
//////        Query getAsset = new Query(new Criteria("_id").is(id_A));
////        getAsset.fields().include("_id");
////        Asset one = mongoTemplate.findOne(getAsset, Asset.class);
////        System.out.print("one"+one);
////        if (null != one) {
////            return one.getId();
////        } else {
////            return null;
////        }
//
//        String result = qt.getId_A(id_C, ref);
//        if (result.equals(""))
//            return null;
//        else
//            return result;
//    }
//
//
//}
