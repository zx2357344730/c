package com.cresign.details.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;

import java.io.IOException;

public interface StorageService {

//    /**
//     * 从仓库领取物料到订单，保存在当前订单的oStock里
//     * ##author: Jevon
//     * ##Params: id_C      公司id
//     * ##Params: id_U      用户id
//     * ##Params: grp       组别
//     * ##Params: listType  列表类型
//     * ##Params: id_O      订单id
//     * ##Params: assetObject 领取对象（id_A:领取的物料id, wn2qty:数量 ）
//     * ##version: 1.0
//     * ##updated: 2020/10/20 15:39
//     * ##Return: java.lang.String
//     */
//    ApiResponse goodsAllocation(String id_C, String id_U, String grp, String listType, String id_O, JSONArray assetObject) throws IOException;
//
//    /**
//     * 从当前订单的oStock卡里的物料入库到仓库
//     * ##author: Jevon
//     * ##Params: id_C      公司id
//     * ##Params: id_U      用户id
//     * ##Params: grp       组别
//     * ##Params: listType  列表id
//     * ##Params: id_O      订单id
//     * ##Params: id_P      oStock卡里有多个产品，领取入库那个产品就放那个id_P
//     * ##Params: id_A      领取的产品放仓库那里，仓库里面有的话就放id_A,没有的话留空， ""
//     * ##Params: wn2qty    数量
//     * ##version: 1.0
//     * ##updated: 2020/10/23 15:01
//     * ##Return: java.lang.String
//     */
//    ApiResponse goodsWarehousing(String id_C,String id_U,String grp,String listType,String id_O,String id_P,String id_A,String wn2qty) throws IOException;
//
//
//    /**
//     * 在仓库合并产品（已调用file服务API）
//     * ##author: Jevon
//     * ##Params: id_U
//     * ##Params: id_C
//     * ##Params: listType
//     * ##Params: id_to
//     * ##Params: id_from
//     * ##version: 1.0
//     * ##updated: 2020/10/22 8:42
//     * ##Return: java.lang.String
//     */
//    ApiResponse prodMerge (String id_U, String id_C,String grp, String listType,String id_to,List<String> id_from) throws IOException;
//    /**
//     * 在仓库拆分产品
//     * ##author: Jevon
//     * ##Params:
//     * ##version: 1.0
//     * ##updated: 2020/10/22 14:01
//     * ##Return: java.lang.String
//     */
//    ApiResponse  prodSplit(String id_U, String id_C,String grp, String listType,String id_to,Double wn2qty) throws IOException;
//
//    /**
//     * 检查所有库存预警
//     * ##author: Jevon early warning
//     * ##Params: id_C
//     * ##Params: id_U
//     * ##Params: listType
//     * ##version: 1.0
//     * ##updated: 2020/11/18 15:34
//     * ##Return: java.lang.String
//     */
//    ApiResponse batchQtySafe(String id_C, String id_U, String listType) throws IOException;
//
//
//
//    /**
//     * 预警生成订单
//     * ##author: Jevon
//     * ##Params: id_U
//     * ##Params: id_C
//     * ##Params: reqJson
//     * ##Params: listType
//     * ##Params: data
//     * ##version: 1.0
//     * ##updated: 2020/11/19 15:34
//     * ##Return: java.lang.String
//     */
//    ApiResponse addOrder (String id_U, String id_C, String grp, HashMap<String, Object> reqJson, String listType, String data) throws IOException;

    /**
     * 根据公司id获取仓库
     * @Author Rachel
     * @Date 2021/08/14
     * ##param id_C 公司id
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse getWarehouse(String id_C);

    /**
     * 根据公司id和仓库ref获取区域
     * @Author Rachel
     * @Date 2021/08/14
     * ##param id_C 公司id
     * ##param ref 仓库
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse getArea(String id_C, String ref);

    /**
     * 根据公司id,仓库ref和区域refArea获取货架
     * @Author Rachel
     * @Date 2021/08/14
     * ##param id_C 公司id
     * ##param ref 仓库
     * ##param refArea 区域
     * @Return com.cresign.tools.apires.ApiResponse
     **/
//    ApiResponse getRack(String id_C, String ref, String refArea);

    /**
         * 获取货架上的非空格子
     * @Author Rachel
     * @Date 2021/08/16
     * ##param id_C 公司id
     * ##param locAddr 仓库位置##区域##货架前缀##货架编号
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse getLocByRef(String id_C, String locAddr) throws IOException;

    /**
     * 获取货架上的空格子
     * @Author Rachel
     * @Date 2021/08/16
     * ##param id_C 公司id
     * ##param locAddr 仓库位置##区域##货架前缀##货架编号
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse getLocByRefEmpty(String id_C, String locAddr) throws IOException;

    /**
     * 获取公司仓库名
     * @Author Rachel
     * @Date 2022/03/14
     * @Param id_C
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse getLocName(String id_C);

//    public JSONObject getLocSpace(String id_C, String locAddr, JSONArray locSpace) throws IOException;

    /**
     * 移动货架格子的产品(每次只能移动一种产品)
     * @Author Rachel
     * @Date 2021/08/20
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param fromLocAddr 移动之前的货架：仓库位置##区域##货架前缀##货架编号
     * ##param toLocAddr 移动之后的货架：仓库位置##区域##货架前缀##货架编号
     * ##param fromIndex 移动之前的格子
     * ##param tindex 移动之后的格子
     * ##param fromQty 每个格子拿的数量
     * ##param toQty 每个格子放的数量
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse moveAsset(String id_C, String fromLocAddr, String toLocAddr, JSONArray fromSpace, JSONArray toSpace, JSONArray fromQty, JSONArray toQty) throws IOException;

    /**
     * 从订单oStock移动产品到仓库
     * @Author Rachel
     * @Date 2021/08/26
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_O 订单id
     * ##param index 订单下标
     * ##param locAddr 仓库位置##区域##货架前缀##货架编号
     * ##param locSpace 格子
     * ##param spaceQty 数量
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse pushAsset(JSONObject tokData, String id_O, Integer index, String locAddr, JSONArray locSpace, JSONArray wn2qty) throws IOException;

    /**
     * 根据产品的货架位置从仓库移动产品到订单oStock
     * @Author Rachel
     * @Date 2021/08/26
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_O 订单id
     * ##param index 订单下标
     * ##param locAddr 仓库位置##区域##货架前缀##货架编号
     * ##param locSpace 格子
     * ##param spaceQty 数量
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse popAssetByLocation(JSONObject tokData, String id_P, String id_O, Integer index, String locAddr, JSONArray locSpace, JSONArray wn2qty) throws IOException;

    /**
     * 根据资产id从仓库移动产品到订单oStock
     * @Author Rachel
     * @Date 2021/09/17
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_O 订单id
     * ##param index 订单oStock下标
     * ##param id_A 资产id
     * ##param locSpace 格子
     * ##param spaceQty 数量
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse popAssetById_A(JSONObject tokData, String id_P, String id_O, Integer index,
                               String id_A, JSONArray locSpace, JSONArray wn2qty, Boolean isResv) throws IOException;

    /**
     * 修改仓库描述卡
     * @Author Rachel
     * @Date 2021/09/14
     * ##param id_C 公司id
     * ##param id_A 资产id
     * ##param locSetup 仓库卡内容
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card Asset.locSetup
     **/
//    ApiResponse up_locSetup(String id_U, String id_C, String id_A, JSONObject locSetup, Integer tvs);

    ApiResponse shipNow(JSONObject tokData, String id_O, Integer index, Boolean isLink, Double qtyShip, Integer prntIndex);

    /**
     * 修改订单oStock数量
     * @Author Rachel
     * @Date 2021/11/29
     * ##param id_O
     * ##param index
     * ##param wn2qtynow
     * @Return com.cresign.tools.apires.ApiResponse
     * @Card
     **/
    ApiResponse updateOStock(String id_O, Integer index, Double wn2qtynow, JSONObject tokData, JSONArray arrTime);


    ApiResponse deductQty(String id_O, Integer index, Double wn2qtynow, String id_UW, JSONObject tokData);

    /**
     * 获取减去现有零件能增加的最大产品数
     * @Author Rachel
     * @Data 2021/09/03
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组
     * ##param id_O 订单id
     * ##param index 数组下标
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse producedMax(String id_U, String id_C, String id_O, Integer index);

    /**
     * 增加产品数量，减去相应的零件数量
     * @Author Rachel
     * @Data 2021/09/03
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组
     * ##param id_O 订单id
     * ##param index 数组下标
     * ##param wn2qtynow 要增加的产品数量
     * @Return com.cresign.tools.apires.ApiResponse
     **/
    ApiResponse producedNow(JSONObject tokData, String id_O, Integer index, Double wn2qtynow) throws IOException;

    /**
     * 丢弃指定产品/零件的所有子零件
     * @Author Rachel
     * @Date 2021/12/10
     * ##param id_U 用户id
     * ##param id_C 公司id
     * ##param listType 列表类型
     * ##param grp 组别
     * ##param id_O 订单id
     * ##param index 下标
     * @Return java.lang.Object
     * @Card
     **/
//    ApiResponse producedClear(String id_U, String id_C, String listType, String grp, String id_O, Integer index);

    /**
     * 盘点仓库
     * @Author Rachel
     * @Date 2022/03/14
     * @Param id_C 公司id
     * @Param locAddr 货架
     * @Param arrayLoc
     * @Return java.lang.Object
     * @Card
     **/
    Object inventory(String id_C, String locAddr, JSONArray arrayLoc) throws IOException;

    ApiResponse getFromStock(String id_C, String id_O) throws IOException;

    Integer updateOStockPi(String id_C, String id_O, Integer index, Double wn2qtynow, String dep, String grpU, String id_u, JSONObject wrdNU, JSONArray arrTime);
}