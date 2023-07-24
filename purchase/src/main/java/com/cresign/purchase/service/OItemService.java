//package com.cresign.purchase.service;
//
//import com.alibaba.fastjson.JSONArray;
//import com.cresign.tools.apires.ApiResponse;
//
//import java.io.IOException;
//
//public interface OItemService {
//
//    ApiResponse mergeOrder(String id_O, Integer index, String mergeId_O, Integer mergeIndex);
//
//    ApiResponse splitOrder(String id_O, Integer index, JSONArray arrayWn2qty);
//
////    ApiResponse moveOItem(String id_O, Integer index, String moveId_O, Integer moveIndex);
//
//    ApiResponse delOItem(String id_O, Integer index) throws IOException;
//
//
//    ApiResponse mergeOrders(String id_O, Integer index, JSONArray arrayMergeId_O, JSONArray arrayMergeIndex);
//
//    ApiResponse moveOItems(String id_O, Integer index, String moveId_O, JSONArray arrayMoveIndex);
//
//
//    ApiResponse movePosition(String id_O, Integer index, Integer moveIndex);
//
//    ApiResponse replaceProd(JSONArray arrId_P, Boolean isAdd) throws IOException;
//
//    ApiResponse replaceComp(String id_O, JSONArray arrayReplace) throws CloneNotSupportedException, IOException;
//    /**
//     * text00s卡内容转oItem
//     * @author Rachel
//     * @Date 2022/04/10
//     * @Param id_C 公司id
//     * @Param id_O 订单id
//     * @Param id text00s卡所在id
//     * @Param cardIndex text00s卡index
//     * @Param textIndex text00s卡里的index
//     * @Param table text00s卡所在表 Comp / Order / User / Prod / Asset
//     * @Return java.lang.Object
//     * @Card
//     **/
//    ApiResponse textToOItem(String id_C, String id_O, String id, Integer cardIndex, Integer textIndex, String table);
//
//
//}
