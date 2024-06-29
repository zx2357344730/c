package com.cresign.action.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName GsThisInfo
 * @Date 2024/6/19
 * @ver 1.0.0
 */
public class GsThisInfo {
    /**
     * 写入清理状态方法
     * @param id_O	订单编号
     * @param dateIndex	数据下标
     * @param clearStatus	清理状态信息
     * @param status	更新状态
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    public static void setClearStatus(String id_O, int dateIndex, JSONObject clearStatus, int status){
        // 获取订单编号清理信息
        JSONObject clearId_O = clearStatus.getJSONObject(id_O);
        // 定义存储数据下标清理信息
        JSONObject clearIndex;
        // 判断订单编号清理信息为空
        if (null == clearId_O) {
            // 创建信息
            clearId_O = new JSONObject();
            clearIndex = new JSONObject();
        } else {
            // 获取数据下标清理信息
            clearIndex = clearId_O.getJSONObject(dateIndex + "");
            // 判断数据下标清理信息为空
            if (null == clearIndex) {
                // 创建信息
                clearIndex = new JSONObject();
            }
        }
        // 更新状态
//        clearIndex.put(dateIndex+"",status);
        clearIndex.put("status",status);
//        clearId_O.put(id_O,clearIndex);
        clearId_O.put(dateIndex+"", clearIndex);
        clearStatus.put(id_O,clearId_O);
    }

    /**
     * 更新当前信息ref方法
     * @param thisInfo	当前处理通用信息存储
     * @param ref	信息名称
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoRef(JSONObject thisInfo,String ref){
        // 更新ref
        thisInfo.put("thisRef",ref);
    }
    /**
     * 获取当前信息ref方法
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @date 创建时间: 2023/2/8
     * @ver 版本号: 1.0.0
     */
    public static String getThisInfoRef(JSONObject thisInfo){
        return thisInfo.getString("thisRef");
    }
    /**
     * 更新当前信息的冲突信息方法
     * @param thisInfo	当前处理通用信息存储
     * @param conflictInfo	冲突信息
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoConflictInfo(JSONObject thisInfo,JSONObject conflictInfo){
        // 更新ref
        thisInfo.put("conflictInfo",conflictInfo);
    }
    /**
     * 获取当前信息的冲突状态
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoConflictInfo(JSONObject thisInfo){
        return thisInfo.getJSONObject("conflictInfo");
    }
    /**
     * 更新当前信息的冲突最后一个ODate信息
     * @param thisInfo	当前处理通用信息存储
     * @param conflictLastODate	冲突最后一个ODate信息
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoConflictLastODate(JSONObject thisInfo,JSONObject conflictLastODate){
        // 更新ref
        thisInfo.put("conflictLastODate",conflictLastODate);
    }
    /**
     * 获取当前信息的冲突最后一个ODate信息
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoConflictLastODate(JSONObject thisInfo){
        return thisInfo.getJSONObject("conflictLastODate");
    }
    /**
     * 更新当前信息的父id信息方法
     * @param thisInfo	当前处理通用信息存储
     * @param orderFatherId	父id信息
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoOrderFatherId(JSONObject thisInfo,JSONObject orderFatherId){
        // 更新ref
        thisInfo.put("orderFatherId",orderFatherId);
    }
    /**
     * 获取当前信息的父id信息
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoOrderFatherId(JSONObject thisInfo){
        return thisInfo.getJSONObject("orderFatherId");
    }
    /**
     * 写入最后一个零件时间下标
     * @param thisInfo	当前处理通用信息存储
     * @param finalPartDateIndex	最后一个零件时间下标
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoFinalPartDateIndex(JSONObject thisInfo,int finalPartDateIndex){
        // 更新finalPartDateIndex
        thisInfo.put("finalPartDateIndex",finalPartDateIndex);
    }
    /**
     * 获取最后一个零件时间下标
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static int getThisInfoFinalPartDateIndex(JSONObject thisInfo){
        return thisInfo.getInteger("finalPartDateIndex");
    }
    /**
     * 写入最后一个零件时间信息
     * @param thisInfo	当前处理通用信息存储
     * @param id_O	最后一个零件时间订单编号
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoFinalPartDate(JSONObject thisInfo,String id_O,String layer,String id_PF,String id_OP){
        JSONObject set = new JSONObject();
        set.put("id_O",id_O);
        set.put("layer",layer);
        set.put("id_PF",id_PF);
        set.put("id_OP",id_OP);
        thisInfo.put("finalPartDate",set);
    }
//    public void setThisInfoFinalPartDate(JSONObject thisInfo,JSONObject lastTime){
//        JSONObject thisInfoFinalPartDate = getThisInfoFinalPartDate(thisInfo);
//        thisInfoFinalPartDate.put("lastTime",lastTime);
//        thisInfo.put("finalPartDate",thisInfoFinalPartDate);
//    }
    /**
     * 获取最后一个零件时间订单编号
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoFinalPartDate(JSONObject thisInfo){
        return thisInfo.getJSONObject("finalPartDate");
    }
    /**
     * 写入是否是最后一个零件
     * @param thisInfo	当前处理通用信息存储
     * @param isFinalPart	是否是最后一个零件
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoIsFinalPart(JSONObject thisInfo,boolean isFinalPart){
        // 更新finalPartDateIndex
        thisInfo.put("isFinalPart",isFinalPart);
    }
    /**
     * 获取是否是最后一个零件
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static boolean getThisInfoIsFinalPart(JSONObject thisInfo){
        return thisInfo.getBoolean("isFinalPart");
    }
    /**
     * 写入是否有冲突
     * @param thisInfo	当前处理通用信息存储
     * @param isConflict	是否有冲突
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoIsConflict(JSONObject thisInfo,boolean isConflict){
        thisInfo.put("isConflict",isConflict);
    }
    /**
     * 获取是否有冲突
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static boolean getThisInfoIsConflict(JSONObject thisInfo){
        return thisInfo.getBoolean("isConflict");
    }
    /**
     * 写入被冲突的信息
     * @param thisInfo	当前处理通用信息存储
     * @param quiltConflictInfo	被冲突的信息
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoQuiltConflictInfo(JSONObject thisInfo,JSONObject quiltConflictInfo){
        // 更新finalPartDateIndex
        thisInfo.put("quiltConflictInfo",quiltConflictInfo);
    }
    /**
     * 获取被冲突的信息
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoQuiltConflictInfo(JSONObject thisInfo){
        return thisInfo.getJSONObject("quiltConflictInfo");
    }
    /**
     * 写入时间处理操作次数
     * @param thisInfo	当前处理通用信息存储
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoTimeCount(JSONObject thisInfo){
        Integer thisInfoTimeCount = getThisInfoTimeCount(thisInfo);
        if (null == thisInfoTimeCount) {
            thisInfoTimeCount = 0;
        }
        thisInfoTimeCount++;
        // 更新finalPartDateIndex
        thisInfo.put("timeCount",thisInfoTimeCount);
    }
    /**
     * 获取时间处理操作次数
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static Integer getThisInfoTimeCount(JSONObject thisInfo){
        return thisInfo.getInteger("timeCount");
    }
    /**
     * 写入最后一个任务的结束时间
     * @param thisInfo	当前处理通用信息存储
     * @param lastTePFin 最后一个任务的结束时间
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoLastTeInfo(JSONObject thisInfo,JSONObject lastTePFin){
        // 更新lastTePFin
        thisInfo.put("lastTeInfo",lastTePFin);
    }
    /**
     * 获取最后一个任务的结束时间
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoLastTeInfo(JSONObject thisInfo){
        return thisInfo.getJSONObject("lastTeInfo");
    }
    /**
     * 写入当前层级
     * @param thisInfo	当前处理通用信息存储
     * @param layer 当前层级
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoLayer(JSONObject thisInfo,int layer){
        thisInfo.put("layer",layer);
    }
    /**
     * 获取当前层级
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static int getThisInfoLayer(JSONObject thisInfo){
        return thisInfo.getInteger("layer");
    }
    /**
     * 写入当前层级父零件编号
     * @param thisInfo	当前处理通用信息存储
     * @param prodId 父零件编号
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoLayerProdId(JSONObject thisInfo,String prodId){
        thisInfo.put("prodId",prodId);
    }
    /**
     * 获取当前层级父零件编号
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static String getThisInfoLayerProdId(JSONObject thisInfo){
        return thisInfo.getString("prodId");
    }
    /**
     * 写入当前清理的父与层级信息
     * @param thisInfo	当前处理通用信息存储
     * @param clearOPLayer 父零件编号
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoClearOPLayer(JSONObject thisInfo,JSONObject clearOPLayer){
        thisInfo.put("clearOPLayer",clearOPLayer);
    }
    /**
     * 获取当前清理的父与层级信息
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoClearOPLayer(JSONObject thisInfo){
        return thisInfo.getJSONObject("clearOPLayer");
    }

    /**
     * 写入当前层级的信息
     * @param thisInfo	当前处理通用信息存储
     * @param sonLayerInfo 当前层级信息
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static void setThisInfoSonLayerInfo(JSONObject thisInfo,JSONObject sonLayerInfo){
        thisInfo.put("sonLayerInfo",sonLayerInfo);
    }
    /**
     * 获取当前层级的信息
     * @param thisInfo	当前处理通用信息存储
     * @return 返回结果: {@link String}
     * @author tang
     * @ver 版本号: 1.0.0
     */
    public static JSONObject getThisInfoSonLayerInfo(JSONObject thisInfo){
        return thisInfo.getJSONObject("sonLayerInfo");
    }

    public static void setEasyDepTeSta(JSONObject thisEasy,JSONObject depTeSta){
        thisEasy.put("depTeSta",depTeSta);
    }
    public static JSONObject getEasyDepTeSta(JSONObject thisEasy){
        return thisEasy.getJSONObject("depTeSta");
    }
    public static void setEasyAssetId(JSONObject thisEasy,JSONObject assetId){
        thisEasy.put("assetId",assetId);
    }
    public static JSONObject getEasyAssetId(JSONObject thisEasy){
        return thisEasy.getJSONObject("assetId");
    }

}
