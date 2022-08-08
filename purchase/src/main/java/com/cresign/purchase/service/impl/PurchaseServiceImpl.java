package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.enumeration.PurchaseEnum;
import com.cresign.purchase.service.PurchaseService;
import com.cresign.purchase.utils.HttpClientUtils;
import com.cresign.purchase.utils.QRCodeUtil;
import com.cresign.purchase.utils.SMSTencent;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.common.Constants;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Ut;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.SMSTemplateEnum;
import com.cresign.tools.enumeration.SMSTypeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.InitJava;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.User;
import com.github.wxpay.sdk.WXPayUtil;
import com.mongodb.bulk.BulkWriteResult;
import com.stripe.Stripe;
import com.stripe.model.Price;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private Ut ut;

    @Autowired
    private DateUtils dateUtils;

    @Resource
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private StringRedisTemplate redisTemplate1;


    @Autowired
    private CoupaUtil coupaUtil;

    @Autowired
    private RetResult retResult;

    private static final String QD_Key = "qdKey";

    /**
     * 新增stripe的产品和价格
     *
     * ##Params: orderId 订单id
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/25 13:58
     */
    @Override
    public ApiResponse addStripeProductAndPrice(String orderId) {
        // 根据订单id获取订单信息
        JSONObject orderById = this.getOrderById(orderId);

        if (null == orderById) {
            // 返回处理结果
//            return RetResult.jsonResultEncrypt(HttpStatus.OK, PurchaseEnum.ERR_ORDER_BE_OVERDUE.getCode(), "订单已过期，请重新购买");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_ORDER_BE_OVERDUE.getCode(), "订单已过期，请重新购买");
        }
        String priceId;
        try {
            Stripe.apiKey = "sk_test_hAxmJ93SFAxiIOUoSx8DQIQ300SC7Adg2A";

//            Map<String, Object> paramsProd = new HashMap<>();
////            paramsProd.put("name", orderById.get("modeName"));
//            paramsProd.put("name", orderById.get("modeName"));
//
//            Product productC = Product.create(paramsProd);

            JSONObject paramsPrice = new JSONObject();
//            paramsPrice.put("unit_amount", (int) (commUtils.getDouble(orderById.get("wn2PaidPrice").toString()) * 100));
            paramsPrice.put("currency", orderById.get("lCR"));
            paramsPrice.put("product", "prod_HZnaJXBabphc5L");

            Price prodPrice = Price.create(paramsPrice);



//            Product product = Product.retrieve("prod_HZnaJXBabphc5L");
//
//            product.delete();

            priceId = prodPrice.getId();

        } catch (Exception e) {

            e.printStackTrace();
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "失败:操作失败！！！,请联系管理员！");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "失败:操作失败！！！,请联系管理员！");
        }
//        String s = RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), priceId);

        // 抛出操作成功异常
        return retResult.ok(CodeEnum.OK.getCode(), RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), priceId));
    }

    /**
     * 根据orderId查询微信支付订单结果
     *
     * ##Params: orderId 订单编号confirmOrder
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 8:19
     */
    @Override
    public ApiResponse wxGetOrder(String orderId) {


        // 创建请求结果存储
        Map<String, String> reDataMap = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 公众账号ID
        reDataMap.put("appid", Constants.WX_APP_ID);
        // mch_id = 商户号
        reDataMap.put("mch_id", Constants.WX_MCH_ID);
        // 查询订单id
        reDataMap.put("out_trade_no", orderId);
        // 随机字符串
        reDataMap.put("nonce_str", MongoUtils.GetObjectId());

        // 创建签名
        String sign = null;
        try {
            // 获取签名
            sign = WXPayUtil.generateSignature(reDataMap, Constants.WX_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断签名为空
        if (null == sign) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败");
        }
        // 签名
        reDataMap.put("sign", sign);

        // 创建请求xml
        String xml = null;
        try {
            // 将Map转换为xml
            xml = WXPayUtil.mapToXml(reDataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断请求xml为空
        if (null == xml) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败");
        }

        // 调用api发送请求并返回结果
        String byXml = HttpClientUtils.doPostByXml(Constants.WX_QUERY_ORDER_URL, xml);

        // 创建存储map
        Map<String, String> stringStringMap = null;
        try {
            // 将请求成功返回的xml转换成map
            stringStringMap = WXPayUtil.xmlToMap(byXml);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 判断存储map为空
        if (null == stringStringMap) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败");
        }



        // 判断请求是否成功
        if ("SUCCESS".equals(stringStringMap.get("return_code"))) {
            if ("SUCCESS".equals(stringStringMap.get("result_code"))) {
                if ("SUCCESS".equals(stringStringMap.get("trade_state"))) {
                    // 返回处理结果
                    // 抛出操作成功异常
//                    return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), "1");
                    return retResult.ok(CodeEnum.OK.getCode(), "1");
                }
            }
        }
//        return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), "0");
        return retResult.ok(CodeEnum.OK.getCode(), "0");
    }

    /**
     * 根据orderId和id_C生成微信支付二维码
     *
     * ##Params: orderId 订单编号
     * ##Params: id_C    公司编号
     * ##return: java.lang.String  返回结果: 支付二维码
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 8:19
     */

    private String get32Random(){
        String s = UUID.randomUUID().toString();
        //public String replaceAll(String regex, String replacement)
        //replaceAll() 方法使用给定的参数 replacement 替换字符串所有匹配给定的正则表达式的子字符串
        return s.replaceAll("-", "");
    }

    @Override
    public ApiResponse wxCharge(String orderId, String id_C) {


        // 根据订单id获取订单信息
        Map<String, Object> orderById = this.getOrderById(orderId);

        // 判断订单信息为空
        if (null == orderById) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_ORDER_BE_OVERDUE.getCode(), "订单已过期，请重新购买！！！");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_ORDER_BE_OVERDUE.getCode(), null);
        }

        // 创建32位随机码
        String rechargeNo = this.get32Random();

        // 创建请求map存储
        Map<String, String> reDataMap = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 公众账号ID
        reDataMap.put("appid", Constants.WX_APP_ID);
        // mch_id = 商户号
        reDataMap.put("mch_id", Constants.WX_MCH_ID);
        // 随机字符串
        reDataMap.put("nonce_str", WXPayUtil.generateNonceStr());
        // 获取支付订单商品名称
        reDataMap.put("body", orderById.get("wcnN").toString());
        // 商户订单号
        reDataMap.put("out_trade_no", rechargeNo);
        int price = (int) ((ut.getDouble(orderById.get("wn2PaidPrice").toString())) * 100);
        // 获取支付订单价格
        reDataMap.put("total_fee", price + "");

        // 预支付信息保存
        Map<String, Object> data = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 添加商户订单号
        data.put("rechargeNo", rechargeNo);
        // 添加支付方式
        data.put("paymentType", "wx");
        // 添加到订单
        orderById.put("data", data);

        // 定义ip存储
        InetAddress loHost = null;
        try {
            // 获取支付ip
            loHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        // 判断ip为空
        if (null == loHost) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:loHost为空");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:loHost为空");
        }
        // 终端IP
        reDataMap.put("spbill_create_ip", loHost.getHostAddress());
        // 支付成功回调地址
        reDataMap.put("notify_url", "http://127.0.0.1:10010/c/purchaseF/getRenewalCalculation");
        // 交易类型
        reDataMap.put("trade_type", "NATIVE");
        // 商品id
        reDataMap.put("product_id", orderById.get("ref").toString());

        // 定义存储签名
        String sign = null;
        try {
            // 获取签名
            sign = WXPayUtil.generateSignature(reDataMap, Constants.WX_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断签名为空
        if (null == sign) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:sign为空");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:sign为空");
        }
        // 签名
        reDataMap.put("sign", sign);

        // 存储请求xml
        String xml = null;
        try {
            // 将请求Map转换为xml
            xml = WXPayUtil.mapToXml(reDataMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断xml为空
        if (null == xml) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:xml为空");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:xml为空");
        }

        // 调用api发送请求并返回结果
        String byXml = HttpClientUtils.doPostByXml(Constants.WX_UNIFIED_ORDER_URL, xml);

        // 创建存储map
        Map<String, String> stringStringMap = null;
        try {
            // 将请求xml结果转换成map
            stringStringMap = WXPayUtil.xmlToMap(byXml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 判断map为空
        if (null == stringStringMap) {
            // 返回处理结果
//            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:stringStringMap为空");
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:stringStringMap为空");
        }



        // 判断二维码是否生成成功
        if ("SUCCESS".equals(stringStringMap.get("return_code"))) {
            if ("SUCCESS".equals(stringStringMap.get("result_code"))) {

                // 获取二维码路径
                String s = QRCodeUtil.zxingCodeCreateTang(stringStringMap.get("code_url"), "QR/" + id_C, 250, null, rechargeNo);



                addOrderById(orderById);

                // 创建返回结果
                Map<String, String> result = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);

                // 二维码路径
                result.put("img", s);
                // 商户订单号
                result.put("wxOrderId", rechargeNo);
                // 订单id
                result.put("orderId", orderId);

                // 抛出操作成功异常
//                return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), result);
                return retResult.ok(CodeEnum.OK.getCode(), result);
            } else {
                // 返回处理结果
//                return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), "失败:result_code错误");
                throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:result_code错误");
            }
        }
        // 返回处理结果
//        return RetResult.errorJsonResult(HttpStatus.OK, CodeEnum.OK.getCode(), "失败:return_code错误");
        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "操作失败:return_code错误");
    }

    /**
     * stripe支付成功回调方法
     *
     * ##Params: orderId 订单编号
     * ##Params: id_C    公司编号
     * ##Params: uId     用户编号
     * ##return: java.lang.String  返回结果: 添加结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 8:20
     */
    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse stripeChargeNew(String orderId, String id_C, String uId) {

        //KEV AddModule Method is wrong


        // 根据订单id获取订单信息
        Map<String, Object> orderById = this.getOrderById(orderId);

        if (null == orderById) {
            // 返回处理结果
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_ORDER_BE_OVERDUE.getCode(), "失败:订单已过期，请重新购买！！！");
        }

        String aId_Auth = null;
        String aId_BuyTemp = null;
        String view = null;
        String id_O = null;
        int optionType = ut.objToInteger(orderById.get("optionType"));
        if (optionType == 0) {

        } else if (optionType == 1 || optionType == 2) {
//            String aId = (SetLogEnum.A_MODULE.getType() + id_C);
            String assetId = coupaUtil.getAssetId(id_C, "a-module");

            if (null != assetId) {
                Asset asset = coupaUtil.getAssetById(assetId, Collections.singletonList("modList"));
                if (null == asset) {
                    // 返回处理结果
//                    return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_GET_NULL.getCode(), "失败:购买信息为空！！！");
                    throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_PURCHASE_INFO_IS_NULL.getCode(), "失败:购买信息为空！！！");
                }
//                Map<String, Object> modList = asset.getModList();
//                List<Map<String, Object>> o = (List<Map<String, Object>>) modList.get("objMod");

                Map<String, Object> mod = null;
//                for (Map<String, Object> map : o) {
//                    if (map.get("ref").equals(orderById.get("ref"))) {
//                        mod = map;
//                    }
//                }
//                if (null == mod) {
//                    // 返回处理结果
////                    return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_GET_NULL.getCode(), "失败:购买信息为空！！！");
//                    throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_PURCHASE_INFO_IS_NULL.getCode(), "失败:购买信息为空！！！");
//                }

                if (optionType == 1) {
                    orderById.put("tdurDay", (ut.objToInteger(orderById.get("tdurDay")) + ut.objToInteger(mod.get("tdurDay"))));
                }
                orderById.put("tdurMonth", (ut.objToInteger(orderById.get("tdurMonth")) + ut.objToInteger(mod.get("tdurMonth"))));
                orderById.put("wn2PaidPrice", (ut.getDouble(orderById.get("wn2PaidPrice").toString()) + ut.getDouble(mod.get("wn2PaidPrice").toString())));
                orderById.put("wn2EstPrice", (ut.getDouble(orderById.get("wn2EstPrice").toString()) + ut.getDouble(mod.get("wn2EstPrice").toString())));
            } else {
                // 返回处理结果
//                return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_GET_NULL.getCode(), "失败:购买信息为空！！！");
                throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_PURCHASE_INFO_IS_NULL.getCode(), "失败:购买信息为空！！！");
            }
        }


        // 预支付信息保存
        Map<String, Object> data = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        // 添加支付方式
        data.put("paymentType", "stripe");
        // 添加到订单
        orderById.put("data", data);
        addOrderById(orderById);
//        logService.addListLog1(id_C,null,uId,SetLogEnum.A_MODULE + id_C
//                ,Collections.singletonList("endDate"),orderById,"用户购买", GetUserIpUtil.getIpAddress(request));
        List<Object> filter = new ArrayList<>();
        Map<String, Object> map = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        map.put("bcdLevel", orderById.get("bcdLevel"));
        map.put("ref", orderById.get("ref"));
        filter.add(map);

        Map<String, Object> reqMap = new HashMap<>(Constants.HASH_MAP_DEFAULT_LENGTH);
        reqMap.put("uid", uId);
        reqMap.put("id_C", id_C);
        reqMap.put("lNG", orderById.get("lNG"));
        reqMap.put("filter", filter);
        reqMap.put("logType", Collections.singletonList("endDate"));
        JSONObject json = new JSONObject();
//        try {
////            json = coupaUtil.addModuleMethod(reqMap);
//        } catch (IOException e) {
//            e.printStackTrace();
//            // 返回处理结果
////            return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_GET_NULL.getCode(), "失败:购买失败！！！");
//            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_PURCHASE_FAIL.getCode(), "失败:购买失败！！！");
//        }
        if (json.getBoolean("boolean")) {

//            aId_Auth = "a-auth-" + id_C;
//            String assetId = assetService.getAssetId(id_C, "a-auth");
//            if (null == assetId) {
//                // 返回处理结果
//                return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_GET_NULL.getCode(), "失败:购买信息为空！！！");
//            }
//            aId_Auth = assetId;
//            view = orderById.get("ref").toString();
//            int modeName = assetService.setAssetAuth(aId_Auth, view);
//            if (modeName == 0) {
//                del(aId_Auth, null, view, null);
//                ("auth失败");
//            }
//            aId_BuyTemp = SetLogEnum.A_MODULE.getType() + id_C;
//            assetId = assetService.getAssetId(id_C, "a-module");
//            if (null == assetId) {
//                // 返回处理结果
//                return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_GET_NULL.getCode(), "失败:购买信息为空！！！");
//            }
//            aId_BuyTemp = assetId;
//            id_O = orderById.get("id_O").toString();
//            int i = assetService.setAssetBuyTemp(aId_BuyTemp, orderById);
//            if (i == 0) {
//                del(aId_Auth, aId_BuyTemp, view, id_O);
//                ("buyTemp失败");
//            }

//            delAndIs(this.addOrderAndLsb(orderById, uId, id_C));
//
//            del(aId_Auth, aId_BuyTemp, view, id_O);


//            try {
//
//
//
//            } catch (Exception e) {
//                ("执行失败程序!");
//                del(aId_Auth, aId_BuyTemp, view, id_O);
//                // 返回处理结果
//                return LogAndExService.exBZ3(Constants.STRING_NULL, Constants.STRING_NULL, Constants.STRING_NULL, Collections.singletonList(LogEnum.LOG_SUMMARY.getType()),
//                        SetLogEnum.A_ALLCHECK + Constants.STRING_NULL, Constants.MAP_S_O_NULL, MyError.FAILF, 0,
//                        logService, LogEnum.LOG_IS_SET_NO, "操作失败！！！,请联系管理员！");
//            }
            // 抛出操作成功异常
//            return RetResult.jsonResultEncrypt(HttpStatus.OK, PurchaseEnum.PR_SUCCESS.getCode(), 1);
            return retResult.ok(CodeEnum.OK.getCode(), 1);
        }
        // 返回处理结果
//        return RetResult.errorJsonResult(HttpStatus.OK, PurchaseEnum.PR_FAILF.getCode(), "失败:操作失败！！！,请联系管理员！");
        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, PurchaseEnum.ERR_OPERATION_FAILED.getCode(), "失败:操作失败！！！,请联系管理员！");
    }

    @Override

    public ApiResponse inspectUserDef(String id_U, String id_C) {

        int resultNum = selectCompDefOFUser(id_U, id_C);

        if (resultNum == 0) {

            return retResult.ok(CodeEnum.OK.getCode(),null);

        } else if (resultNum == 1) {


            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.NO_CHARGE_USER.getCode(), null);

        }


        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.PR_COMP_DATA_NO_FOUND.getCode(), null);

    }

    @Override
    public ApiResponse confirmOrder(String id_U, Integer optionType, JSONObject data) throws ParseException {


        // objectid
        String id = new ObjectId().toString();
        int resultNum = selectCompDefOFUser(id_U, data.getString("id_C"));
        if (resultNum == 0) {
            if (optionType.equals(0)) {
                return purchaseModule(id_U, data,  id, optionType);
            }
            else if (optionType.equals(1)) {
                return renewModule(id_U, data, id, optionType);
            }
//            else if (optionType.equals(2)) {
//                upgradeModule(id_U, data, logType, id, optionType);
//
//            }

        } else if (resultNum == 1) {

            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.NO_CHARGE_USER.getCode(), null);

        }
        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.PR_COMP_DATA_NO_FOUND.getCode(), null);



    }



    //续费
    private ApiResponse renewModule(String id_U, JSONObject data,  String id, Integer optionType) throws ParseException {

        JSONObject calculation = renewCalculation(data);

        if (calculation != null) {
            try {

//                //应付款
//                data.put("accountsPayable", calculation.get("accountsPayable"));
//                // 折扣
//                data.put("discount", calculation.get("discount"));
//                // 实付款
//                data.put("actualPayment", calculation.get("actualPayment"));
//                //货币方式
//                data.put("monetary", calculation.get("monetary"));
//                //购买人数
//                data.put("uNum", calculation.get("uNum"));

                //订单ID
                calculation.put("id_O", id);

                //用户id
                calculation.put("amk", id_U);


                //0：购买，1：续费，2：升级
                //calculation.put("optionType",optionType);
//                //开始时间
//                data.put("tmk", calculation.get("tmk"));
//                //结束时间
//                data.put("tfin", calculation.get("tfin"));
//                int dateNum = commUtils.nDaysBetweenTwoDate((String) calculation.get("tmk"), (String) calculation.get("tfin"));
//                //购买天数
//                data.put("dateNum", dateNum);

                redisTemplate0.opsForValue().set(id, JSON.toJSONString(calculation), 10, TimeUnit.MINUTES);

            } catch (Exception e) {

                throw new ErrorResponseException(HttpStatus.OK, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);

            }

            return retResult.ok(CodeEnum.OK.getCode(), id);




        } else {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);

        }


    }

    //购买
    private ApiResponse purchaseModule(String id_U, Map<String, Object> data,  String id, Integer optionType) {
        JSONObject calculation = basicCalculation((JSONObject) JSONObject.toJSON(data));
        if (calculation != null) {
            try {
//                ("calculation = " + calculation);
//
//                //模块名称
//                data.put("wcnN", calculation.get("wcnN"));
//                //应付款
//                data.put("wn2EstPrice", calculation.get("wn2EstPrice"));
//                // 折扣
//                data.put("discount", calculation.get("discount"));
//                // 实付款
//                data.put("wn2PaidPrice", calculation.get("wn2PaidPrice"));
//                //货币方式
//                data.put("lCR", calculation.get("lCR"));
//                //购买人数
//                data.put("wn0maxUser", calculation.get("wn0maxUser"));

                //订单ID
                calculation.put("id_O", id);

                //用户id
                calculation.put("amk", id_U);

                //开始时间
                calculation.put("tmk", DateUtils.getDateByT(DateEnum.DATE_ONE.getDate()));
                //结束时间
                calculation.put("tfin", dateUtils.getEndTime(DateUtils.getDateByT(DateEnum.DATE_ONE.getDate()), (Integer) data.get("tdurMonth")));

//                int tdurDay = DateUtils.nDaysBetweenTwoDate(DateUtils.getDateByT(DateEnum.DATE_ONE.getDate()),
//                        commUtils.getEndTime(DateUtils.getDateByT(DateEnum.DATE_ONE.getDate()), (Integer) data.get("tdurMonth")));
//                //购买天数
//                data.put("tdurDay", tdurDay);

                //0：购买，1：续费，2：升级
                calculation.put("optionType", optionType);

                redisTemplate0.opsForValue().set(id, JSON.toJSONString(calculation), 10, TimeUnit.MINUTES);

            } catch (Exception e) {


                throw new ErrorResponseException(HttpStatus.OK, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);


            }


            return retResult.ok(CodeEnum.OK.getCode(), id);


        } else {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);


        }
    }

    /**
     * 根据orderId获取redis的订单
     *
     * ##Params: orderId redis订单id
     * ##return: java.util.Map<java.lang.String, java.lang.Object>  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 9:43
     */
    private JSONObject getOrderById(String orderId) {
        Object o = redisTemplate0.opsForValue().get(orderId);
        if (null != o) {
            return JSON.parseObject(o.toString());
        }
        return null;
    }

    /**
     * 把orderById信息新增到redis数据库
     *
     * ##Params: orderById 订单信息
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 9:43
     */
    private void addOrderById(Map<String, Object> orderById) {
        redisTemplate0.opsForValue().set(orderById.get("id_O").toString(), JSON.toJSONString(orderById));
    }

    /**
     * 根据aId_Auth删除view指定数据，根据aId_BuyTemp删除id_O指定数据
     *
     * ##Params: aId_Auth    assetId
     * ##Params: aId_BuyTemp assetId
     * ##Params: view        数据
     * ##Params: id_O        数据
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 9:44
     */
    private void del(String aId_Auth, String aId_BuyTemp, String view, String id_O) {
        if (null != aId_Auth) {
            coupaUtil.delAssetAuth(aId_Auth, view);
        }
        if (null != aId_BuyTemp) {
            coupaUtil.delAssetBuyTemp(aId_BuyTemp, id_O);
        }
    }

    /**
     * 根据map删除订单，和lsb订单
     *
     * ##Params: map 数据
     * ##return: void  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/8 9:45
     */
//    private void delAndIs(Map<String, Object> map) {
//        int is = commUtils.objToInteger(map.get("is"));
//        if (is == 0) {
//            return;
//        }
//        String oIdB = map.get("oIdB").toString();
//        String oIdX = map.get("oIdX").toString();
//        if (is == 3) {
//            orderClient.removeOrderById(oIdB);
//            orderClient.removeOrderById(oIdX);
//            lsbOrderService.delLsbOrder(oIdB);
//        } else if (is == 2) {
//            orderClient.removeOrderById(oIdB);
//            orderClient.removeOrderById(oIdX);
//        } else if (is == 1) {
//            orderClient.removeOrderById(oIdB);
//        } else if (is == 4) {
//            orderClient.removeOrderById(oIdB);
//            orderClient.removeOrderById(oIdX);
//            lsbOrderService.delLsbOrder(oIdB);
//            lsbOrderService.delLsbOrder(oIdX);
//        }
////        throw new Exception();
//    }



    @Override
    public ApiResponse getPurchaseModuleMoney(String id_U, JSONObject reqJson) {

        //String id_C = reqJson.getString("id_C");

        JSONObject result = new JSONObject();

        try {
            // 结果
            JSONObject calculation = basicCalculation(reqJson);



            if (null == calculation) {

                throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);

            }

            result.put("wn2EstPrice", calculation.getDouble("wn2EstPrice"));
            result.put("wn2PaidPrice", calculation.getDouble("wn2PaidPrice"));

        } catch (RuntimeException e) {

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);

        }
        return retResult.ok(CodeEnum.OK.getCode(),result);



    }




    private JSONObject basicCalculation(JSONObject reqJson) {



        // 获取模块名称
        String ref = reqJson.getString("ref");

        // 客户货币方式
        String lCR = reqJson.getString("lCR");

        // 等级参数
        Integer bcdLevel = reqJson.getInteger("bcdLevel");

        // 人数
        Integer wn0buyUser = reqJson.getInteger("wn0buyUser");

        // 月数
        Integer tdurMonth = reqJson.getInteger("tdurMonth");

        // 货币
        Double exchangeRate = 1d;

        // 折扣
        Double duration = null;

        // 应付款
        Double wn2EstPrice = null;

        // 实付款
        Double wn2PaidPrice = null;

        // 判断必要参数是否为空
        if (
                StringUtils.isEmpty(ref) ||
                        StringUtils.isEmpty(lCR) ||
                        null == bcdLevel ||
                        null == wn0buyUser ||
                        null == tdurMonth
        ) {


            return null;
        }

        /*
            1.根据前端传入的语言、模块名称、等级参数来去数据库中获取对应的模块下的数据
         */


        JSONObject moduleInit = getModuleInit(ref, bcdLevel,reqJson.getString("id_P"));

        // 初始 module的基础数据
        JSONObject moduleObj = moduleInit.getJSONObject("moduleObj");
        // 初始 exchangeRate 的数据
        JSONObject exchangeRateObj = moduleInit.getJSONObject("exchangeRate");
        // 初始化 moduleInfo的基础信息
        JSONObject moduleInfo = moduleInit.getJSONObject("moduleInfo");


        /*
            2.计算价格
         */

        // 基月费
        Double wn1BasePrice = moduleObj.getDouble("wn1BasePrice");

        // 人头数价格
        Double wn1HeadPrice = moduleObj.getDouble("wn1HeadPrice");

        // 免费人头数
        Integer wn0HeadFree = moduleObj.getInteger("wn0HeadFree");

        //客户货币
        exchangeRate = exchangeRateObj.getDouble(lCR);
        // 汇率
//        if ("cn".equals(lNG)) {
//            lCR = "CNY";
//            exchangeRate = exchangeRateObj.getDouble("CNY");
//        } else if ("hk".equals(lNG)) {
//            lCR = "HKD";
//            exchangeRate = exchangeRateObj.getDouble("HKD");
//        } else if ("aud".equals(lNG)) {
//            lCR = "AUD";
//            exchangeRate = exchangeRateObj.getDouble("AUD");
//        }

        /*
            人数计算
         */

        // 初始化人数计算
        Double uNumTotal = 0.0;

        // 如果免费人数大于或者等于用户的选择人数,那就不算人数价格
        if (wn0HeadFree < wn0buyUser) {

            //初始化人数总价  人头数价格        人数        免费人头数        月数
            uNumTotal = wn1HeadPrice * (wn0buyUser - wn0HeadFree) * tdurMonth;
        }



        // 获取objDiscount
        JSONArray objDiscounts = moduleObj.getJSONArray("objDiscount");

        for (int i = 0; i < objDiscounts.size(); i++) {

            JSONObject discount = (JSONObject) objDiscounts.get(i);

            // 判断用户的月份是否符合折扣的月份
            if (discount.getInteger("duration") <= tdurMonth) {

                // 获取其折扣
                duration = discount.getDouble("number");

            }

        }
        //              基月费          月数        初始化人数总价   客户货币
        wn2EstPrice = ((wn1BasePrice * tdurMonth) + uNumTotal) * exchangeRate;
        // 判断是否有折扣
        if (null == duration) {

            wn2PaidPrice = wn2EstPrice;
        } else {

            wn2PaidPrice = wn2EstPrice * duration;
        }
        //总价*客户货币 / 公司基准货币RMB
        //("总价 = " + wn2PaidPrice / exchangeRateObj.getDouble("CNY"));

        JSONObject resultJson = new JSONObject();

        // 应付款
        resultJson.put("wn2EstPrice", Double.valueOf(String.format("%.2f", wn2EstPrice/ exchangeRateObj.getDouble("CNY"))));
        // 实付款
        resultJson.put("wn2PaidPrice", Double.valueOf(String.format("%.2f", wn2PaidPrice/ exchangeRateObj.getDouble("CNY"))));
        // 折扣
        resultJson.put("discount", duration);
        // 货币
        resultJson.put("lCR", lCR);
        // 等级
        resultJson.put("bcdLevel", bcdLevel);
        // 月份
        resultJson.put("tdurMonth", tdurMonth);
        // 模块名称
        resultJson.put("wcnN", moduleInfo.getString("wcnN"));
        // 人数
        resultJson.put("wn0buyUser", wn0buyUser);
        // ref
        resultJson.put("ref", ref);


        /**
         * 时区计算
         */

//        // 获取该用户的时区
//        TimeZone timeZone = getFormatedDateString(-8);
//
//        //通过SimpleDateFormat来设置时间格式
//        SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
//        df.setTimeZone(timeZone);
//
//        //Calendar getInstance() 使用默认时区和语言环境这种方法获得一个日历。
//        Calendar calendar = Calendar.getInstance();
//        String startDate=df.format(calendar.getTime());
//        //在当前日期下+月份
//        calendar.add(Calendar.MONTH, monthNum);
//
//        //将时间结果获取到
//        String endDate=df.format(calendar.getTime());
//        ("startDate = " + startDate);
//        ("endDate = " + endDate);


        return resultJson;

    }




    private JSONObject getModuleInit(String ref, Integer bcdLevel,String id_P) {

        // 设置初始化
        JSONObject resultJson = new JSONObject();


        /*
            1.根据前端传入的语言、模块名称、等级参数来去数据库中获取对应的模块下的数据
         */
        Query modeQ = new Query(
                new Criteria("_id").is(id_P)
                        .and("info.ref").is(ref)
                        .and("buyInit.objData.bcdLevel").is(bcdLevel));
        modeQ.fields()
                .include("buyInit.objData.$")
                .include("info");

        Prod moduleInit = mongoTemplate.findOne(modeQ, Prod.class);

        /*
            2.折扣获取
         */

        // 设置module的基础数据
        //JSONObject moduleObj = (JSONObject) JSONObject.toJSON(moduleInit.getBuyInit().getJSONArray("objData").get(0));
        JSONObject moduleObj = moduleInit.getBuyInit().getJSONArray("objData").getJSONObject(0);
        resultJson.put("moduleInfo", moduleInit.getInfo());
        resultJson.put("moduleObj", moduleObj);

        //查询init
        Query initQ = new Query(new Criteria("_id").is("cn_java"));
        initQ.fields().include("exchangeRate");

        InitJava init = mongoTemplate.findOne(initQ, InitJava.class);
        //汇率
        resultJson.put("exchangeRate",init.getExchangeRate());

        return resultJson;

    }


    @Override
    public String sendBuySms(String id_U, String id_C, String phoneType, String phone) {

        // 判断是否是该公司下的主要负责人
        int resultNum = selectCompDefOFUser(id_U, id_C);

        if (resultNum == 0) {

            String[] phones = {phoneType + phone};

            SMSTencent.sendSMS(phones, 6, SMSTemplateEnum.PURCHASE.getTemplateId(), SMSTypeEnum.PURCHASE.getSmsType());


        } else if (resultNum == 1) {


            return RetResult.jsonResultEncrypt(HttpStatus.OK, PurchaseEnum.NO_CHARGE_USER.getCode(), null);


        } else {

            return RetResult.jsonResultEncrypt(HttpStatus.OK, PurchaseEnum.PR_COMP_DATA_NO_FOUND.getCode(), null);

        }

        return RetResult.jsonResultEncrypt(HttpStatus.OK, CodeEnum.OK.getCode(), null);


    }

    /**
     * 查询用户是否是该公司下def的负责人
     *
     * ##Params: id_U 用户id
     * ##Params: id_C 公司id
     * ##author: JackSon
     * ##version: 1.0
     * ##updated: 2020/8/24 17:13
     * ##Return: int 0 : 是，1 : 不是，2 : 公司数据找不到
     */
    private int selectCompDefOFUser(String id_U, String id_C) {

        Query query = new Query(
                new Criteria("info.id_C").is(id_C)
                        .and("info.ref").is("a-auth"));
        query.fields().include("def");
        Asset one = mongoTemplate.findOne(query, Asset.class);
        if (one != null) {
            if (one.getDef().get("id_UM").equals(id_U)) {
                return 0;

            } else {
                return 1;
            }
        }

        return 2;

    }


    @Override
    public ApiResponse checkBuySms(String id_U, String id_C, String smsCode, String phone) {

        // 判断是否是该公司下的主要负责人
        int resultNum = selectCompDefOFUser(id_U, id_C);

        if (resultNum == 0) {
            if (redisTemplate1.hasKey(SMSTypeEnum.PURCHASE.getSmsType() + phone)) {

                String smsNum = redisTemplate1.opsForValue().get(SMSTypeEnum.PURCHASE.getSmsType() + phone);

                if (smsNum.equals(smsCode)) {

                    redisTemplate1.delete(SMSTypeEnum.PURCHASE.getSmsType() + phone);

                    return retResult.ok(CodeEnum.OK.getCode(),null);
                }


                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.SMS_NOT_TRUE.getCode(), null);

            }



            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.SMS_NOT_TRUE.getCode(), null);

        } else if (resultNum == 1) {

            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.SMS_NOT_TRUE.getCode(), null);

        } else {

            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.SMS_NOT_TRUE.getCode(), null);

        }


    }


    @Override
    public ApiResponse inspectModuleIsBuy(String id_U, String id_C, String ref, Integer bcdLevel) {

        // 判断参数是否为空
        if (StringUtils.isNotEmpty(id_U) && StringUtils.isNotEmpty(id_C) && StringUtils.isNotEmpty(ref)) {

//            int resultNum = selectCompDefOFUser(id_U, id_C);
//
//            // 判断是否是该公司下的主要负责人
//            if (resultNum == 0) {

//                Query query = new Query(
//                        new Criteria("_id").is("a-module-" + id_C)
//                                .and("modList.data.ref").is(ref));
//                query.fields().include("modList.data.$");

                Query moduleQ = new Query(Criteria.where("info.id_C").is(id_C)
                        .and("info.ref").is("a-module").and("control.objData")
                            .elemMatch(new Criteria("ref").is(ref).and("bcdLevel").is(bcdLevel)));  //To find matching documents

                moduleQ.fields().include("control.objData.$");




                Asset asset = mongoTemplate.findOne(moduleQ, Asset.class);

                if (ObjectUtils.isNotEmpty(asset)) {

                    throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.PR_MODULE_IS_BUY_HAVE.getCode(),"");

                }

            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODUL_NO_HAVE.getCode(),"");
//            } else if (resultNum == 1) {
//
//                return RetResult.jsonResultEncrypt(HttpStatus.OK, PurchaseEnum.NO_CHARGE_USER.getCode(), null);
//
//            } else {
//
//                return RetResult.jsonResultEncrypt(HttpStatus.OK, PurchaseEnum.PR_COMP_DATA_NO_FOUND.getCode(), null);
//
//
//            }

        }

        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
    }




    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse moduleUpdataPlusUser(String id_A,String id_U, String id_C, String ref, Integer bcdLevel, List users) throws IOException {

        //第一层判断，用户是管理员之一
        //users 人数不得大于200
        if (users.size() <= 200) {

            JSONObject stateObject = plusUserControl(id_A, ref, bcdLevel, users);

            if (stateObject.getString("boolean").equals("true")) {


                HashMap<String, Object> indexObj = (HashMap<String, Object>) stateObject.get("reason");

                JSONObject jsonObject = plusUserRolex(id_A,indexObj, id_C, ref, bcdLevel, users);

                if (jsonObject.getString("boolean").equals("true")){

                    return retResult.ok(CodeEnum.OK.getCode(),jsonObject.getJSONArray("replace"));

                }


            } else if (stateObject.getString("boolean").equals("false")) {



                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODULE_ADDUSSER_ERROR.getCode(), null);


            }


        } else {
            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODULE_ADDUSSER_GOBEYOND.getCode(), null);


        }

        return null;
    }

    private JSONObject plusUserControl(String id_A, String ref, Integer bcdLevel, List users) {

        JSONObject stateObject = new JSONObject();
        Query authQ = new Query(Criteria.where("_id").is(id_A)
                .and("control.objData").elemMatch(new Criteria("ref").is(ref)
                        .and("bcdLevel").is(bcdLevel)));  //To find matching documents

        authQ.fields().include("control.objData.$");
        //查询模块基本资料
        Asset moduleInit = mongoTemplate.findOne(authQ, Asset.class);
        if (moduleInit == null) {
            stateObject.put("reason", "Asset对象为空");
            stateObject.put("boolean", "false");
            return stateObject;
        }

        //拿出control.objData
//        ArrayList objDataList = (ArrayList) moduleInit.getControl().get("objData");
//        HashMap<String, Object> indexObj = (HashMap<String, Object>) objDataList.get(0);
        JSONObject indexObj = moduleInit.getControl().getJSONArray("objData").getJSONObject(0);

        //判断模块的最大人数，一. -1的话人数无限制，二.模块的最大人数大于添加人数
        if (indexObj.get("wn0buyUser").equals(-1) || indexObj.getInteger("wn0buyUser") >= users.size()) {
            //循环编号与版本一致的
            if (indexObj.get("ref").equals(ref) && indexObj.get("bcdLevel").equals(bcdLevel)) {

//                    ArrayList usersArray = (ArrayList) indexObj.get("users");
//                    //把前端给的数组加到里面去
//                    usersArray.addAll(users);


                Set<String> usersSet = new HashSet<>((Collection<? extends String>) indexObj.get("id_U"));

                //利用HashSet特性,这样不会有重复的人
                usersSet.addAll(users);
                //需要把Set再转回list吗
                indexObj.put("id_U", new ArrayList<>(usersSet));


                //修改模块
                Update controlupdate = new Update();
                controlupdate.set("control.objData.$", indexObj);
                if (!ObjectUtils.isEmpty(controlupdate.getUpdateObject())) {
                    mongoTemplate.updateFirst(authQ, controlupdate, Asset.class);
                }
                stateObject.put("reason", indexObj);
                stateObject.put("boolean", "true");
                return stateObject;


            } else {
                stateObject.put("reason", "模块人数超出");
                stateObject.put("boolean", "false");
                return stateObject;

            }



        }
        //}
        return null;
    }

    private JSONObject plusUserRolex (String id_A, HashMap<String, Object> indexObj, String id_C, String ref, Integer bcdLevel, List users){

        JSONObject stateObject = new JSONObject();

        List<Pair<Query, Update>> updateList = new ArrayList<>();
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);

        List<String > replaceList = new LinkedList<>();
        //修改user.rolex的
        for (int k = 0; k < users.size(); k++) {

            Query rolexQ = new Query(
                    new Criteria("_id").is(users.get(k))
                            .and("rolex.objComp.id_C").is(id_C));
            rolexQ.fields()
                    .include("rolex.objComp.$");


            User user = mongoTemplate.findOne(rolexQ, User.class);

            if (user == null) {
                continue;
            }

//            ArrayList rolexList = (ArrayList) user.getRolex().get("objComp");
//
//            HashMap<String, Object> rolexMap = (HashMap<String, Object>) rolexList.get(0);
            JSONObject rolexMap  = user.getRolex().getJSONArray("objComp").getJSONObject(0);

            if (rolexMap.get("id_C").equals(id_C)) {

                JSONArray objModList = rolexMap.getJSONArray("objMod");

                //Map<String, Object> module = new HashMap<>();
                JSONObject module = new JSONObject(4);
                module.put("bcdState", indexObj.get("bcdState"));
                module.put("tfin", indexObj.get("tfin"));
                module.put("bcdLevel", indexObj.get("bcdLevel"));
                module.put("ref", indexObj.get("ref"));

                //有objMod键的  直接加
                if (rolexMap.containsKey("objMod")) {
                    boolean judge = true;


                    for (int j = 0; j < objModList.size(); j++) {
                        JSONObject modulesMap = objModList.getJSONObject(j);

                        //先判断ref，有相同的
                        if (modulesMap.get("ref").equals(ref)){
                            //再判断版本是否一致的，一致的证明里面已经有了 judge = false，如果版本不一致，证明是变更版本，要把所在版本删除
                            if (modulesMap.get("bcdLevel").equals(bcdLevel)){
                                judge = false;
                            }else{
                                //去id_U删除
                                JSONObject jsonObject = reduceUserControl(id_A, modulesMap.get("ref").toString(), (Integer) modulesMap.get("bcdLevel"), Arrays.asList(user.getId()));

                                if (jsonObject.getString("boolean").equals("true")) {
                                    //删除Rolex
                                    objModList.remove(j);
                                    //记录变更人的id
                                    replaceList.add(user.getId());

                                }
                            }
                        }

//                        if (modulesMap.get("ref").equals(ref) && modulesMap.get("bcdLevel").equals(bcdLevel)) {
//                            //循环发现里面已经有了   那就false
//                            judge = false;
//                        }

                    }
                    //里面没有重复的就等于 true
                    if (judge) {
                        objModList.add(module);
                    }

                } else {
                    //第一次没有objMod键的，多加一层
                    //ArrayList modulesArr = new ArrayList();
                    JSONArray modulesArr = new JSONArray();
                    modulesArr.add(module);
                    rolexMap.put("objMod", modulesArr);
                }
                Update update = new Update();
                update.set("rolex.objComp.$", rolexMap);
                //把要修改的数据和查询条件放到updataList中，一起批量修改，性能提升
                Pair<Query, Update> updatePair = Pair.of(rolexQ, update);

                updateList.add(updatePair);


            }
        }


        operations.updateMulti(updateList);

        BulkWriteResult result = operations.execute();
        //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1


        stateObject.put("reason", result);
        stateObject.put("replace", replaceList);
        stateObject.put("boolean", "true");
        return stateObject;
    }


    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse moduleUpdataReduceUser(String id_A,String id_U, String id_C, String ref, Integer bcdLevel, List reduce) throws IOException {

        //第一层判断，用户是管理员之一
        //users 人数不得大于200
        if (reduce.size() <= 200) {
            //删除Control方法
            JSONObject stateObject = reduceUserControl(id_A, ref, bcdLevel, reduce);

            if (stateObject.getString("boolean").equals("true")) {

                //删除Rolex方法
                JSONObject jsonObject = reduceUserRolex( id_C, ref, bcdLevel, reduce);

                if (jsonObject.getString("boolean").equals("true")){

                    return retResult.ok(CodeEnum.OK.getCode(),null);

                }


            } else if (stateObject.getString("boolean").equals("false")) {

                throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODULE_ADDUSSER_ERROR.getCode(), null);


            }


        } else {

            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODULE_ADDUSSER_GOBEYOND.getCode(), null);

        }


        return null;
    }



    private JSONObject reduceUserControl(String id_A, String ref, Integer bcdLevel, List reduce) {

        JSONObject stateObject = new JSONObject();
        Query authQ = new Query(Criteria.where("_id").is(id_A)
                .and("control.objData").elemMatch(new Criteria("ref").is(ref)
                    .and("bcdLevel").is(bcdLevel)));

        authQ.fields().include("control.objData.$");
        //查询模块基本资料
        Asset moduleInit = mongoTemplate.findOne(authQ, Asset.class);
        if (moduleInit == null) {
            stateObject.put("reason", "Asset对象为空");
            stateObject.put("boolean", "false");
            return stateObject;
        }

        JSONObject indexObj =  moduleInit.getControl().getJSONArray("objData").getJSONObject(0);

        //ashMap<String, Object> indexObj = (HashMap<String, Object>) objDataList.get(0);


        //Set<String> usersManager = new HashSet<>((Collection<? extends String>) indexObj.get("id_UM"));

        Set<String> reduceArray = new HashSet<>(reduce);

//        //交集,
//        usersManager.retainAll(reduceArray);
//        //判断reduce数组里面不能有管理员的存在，管理员不能删除管理员
//        if (usersManager.size() == 0) {
            //循环编号与版本一致的
            if (indexObj.get("ref").equals(ref) && indexObj.get("bcdLevel").equals(bcdLevel)) {


                Set<String> usersArray = new HashSet<>((Collection<? extends String>) indexObj.get("id_U"));

                //差集,两个数组相同的去掉 ，
                usersArray.removeAll(reduceArray);
                //需要把Set再转回list吗
                //indexObj.put("id_U", new ArrayList<>(usersArray));
                indexObj.put("id_U", usersArray);
                //修改模块
                Update controlupdate = new Update();
                controlupdate.set("control.objData.$", indexObj);
                if (!ObjectUtils.isEmpty(controlupdate.getUpdateObject())) {
                    mongoTemplate.updateFirst(authQ, controlupdate, Asset.class);
                }
                stateObject.put("reason", "修改成功");
                stateObject.put("boolean", "true");
                return stateObject;


            } else {
                stateObject.put("reason", "版本或编号不一致");
                stateObject.put("boolean", "false");
                return stateObject;

            }
//        } else {
//            stateObject.put("reason", "不可删除同级");
//            stateObject.put("boolean", "false");
//            return stateObject;
//        }

        //return null;
    }

    private JSONObject reduceUserRolex ( String id_C, String ref, Integer bcdLevel, List reduce){

        JSONObject stateObject = new JSONObject();

        List<Pair<Query, Update>> updateList = new ArrayList<>();
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);


        //修改user.rolex的
        for (int k = 0; k < reduce.size(); k++) {

            Query rolexQ = new Query(
                    new Criteria("_id").is(reduce.get(k))
                            .and("rolex.objComp.id_C").is(id_C));
            rolexQ.fields()
                    .include("rolex.objComp.$");//.include("info")


            User user = mongoTemplate.findOne(rolexQ, User.class);

            if (user == null) {
                continue;
            }

            JSONObject rolexMap =  user.getRolex().getJSONArray("objComp").getJSONObject(0);

            //HashMap<String, Object> rolexMap = (HashMap<String, Object>) rolexList.get(0);
            if (rolexMap.get("id_C").equals(id_C)) {

                JSONArray objModList =  rolexMap.getJSONArray("objMod");

                if (objModList.size() == 0) {
                    continue;
                }

                for (int j = 0; j < objModList.size(); j++) {
                    JSONObject modulesListMap =  objModList.getJSONObject(j);
                    if (modulesListMap.get("ref").equals(ref) && modulesListMap.get("bcdLevel").equals(bcdLevel)) {

                        objModList.remove(j);

                    }
                }


                Update update = new Update();
                update.set("rolex.objComp.$", rolexMap);
                //把要修改的数据和查询条件放到updataList中，一起批量修改，性能提升
                Pair<Query, Update> updatePair = Pair.of(rolexQ, update);

                updateList.add(updatePair);


            }
        }


        operations.updateMulti(updateList);

        BulkWriteResult result = operations.execute();
        //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1


        stateObject.put("reason", result);
        stateObject.put("boolean", "true");
        return stateObject;
    }



    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse moduleUpAdministrators(String id_A, String id_U, String id_C, String ref, Integer bcdLevel, List plusManager,List reduceManager) {

        //第一层判断，用户是root

//        Query authQ = new Query(Criteria.where("_id").is(id_A).and("control.ref").is(ref));
//
//        authQ.fields().include("control.$");

        Query authQ = new Query(Criteria.where("_id").is(id_A)
                .and("control.objData").elemMatch(new Criteria("ref").is(ref)
                        .and("bcdLevel").is(bcdLevel)));

        authQ.fields().include("control.objData.$");

        //查询模块基本资料
        Asset moduleInit = mongoTemplate.findOne(authQ, Asset.class);
        if (moduleInit == null) {

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, PurchaseEnum.ASSET_NOT_FOUND.getCode(), null);

        }


        JSONObject indexObj = moduleInit.getControl().getJSONArray("objData").getJSONObject(0);


        //HashMap<String, Object> indexObj = (HashMap<String, Object>) arrayList.get(0);
            //循环编号与版本一致的
            if (indexObj.get("ref").equals(ref) && indexObj.get("bcdLevel").equals(bcdLevel)) {
                //管理员数组
                JSONArray managerArray =  indexObj.getJSONArray("id_UM");
                //模块使用者数组
                JSONArray usersArray =  indexObj.getJSONArray("id_U");
                //加   id_UM
                if (plusManager.size() > 0){
                    //管理员数组+id_UM[] = 不得超过5个
                    if (managerArray.size() + plusManager.size() <= 5) {
                        //新人员 + 存在人员
                        int extend = plusManager.size() + managerArray.size();

                        //判断模块的最大人数，一. -1的话人数无限制，二.模块的最大人数大于 添加人数  wn0buyUser >= (usersArray + extend)
                        if (indexObj.get("wn0buyUser").equals(-1) || Integer.parseInt(indexObj.get("wn0buyUser").toString()) >= (usersArray.size() + extend)) {

                            //判断plusManager数组里面的人有没有在usersArray数组里面，没有的话加进去
                            Set<String> usersSet = new HashSet<>((Collection<? extends String>)indexObj.get("id_U"));

                            //利用HashSet特性
                            usersSet.addAll(plusManager);
                            //需要把Set再转回list吗
                            indexObj.put("id_U", new ArrayList<>(usersSet));

                            Set<String> managerSet = new HashSet<>((Collection<? extends String>)indexObj.get("id_UM"));
                            //把前端给的数组加到里面去,利用Set特性
                            managerSet.addAll(plusManager);
                            indexObj.put("id_UM", new ArrayList<>(managerSet));

                            //用这个加的话，相同的也会加进去
                            //managerArray.addAll(plusManager);


                            //修改模块
                            Update controlupdate = new Update();
                            //controlupdate.set("control.$", moduleInit.getControl().get(0));
                            controlupdate.set("control.objData.$", indexObj);
                            if (!ObjectUtils.isEmpty(controlupdate.getUpdateObject())) {
                                mongoTemplate.updateFirst(authQ, controlupdate, Asset.class);
                            }
                            //修改Rolex
                            plusUserRolexAdministrators(indexObj,id_C,  ref,  bcdLevel, plusManager);



                        }else {

                            throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODULE_ADDUSSER_GOBEYOND.getCode(), null);

                            //return "超过模块规定人数";
                        }
                    } else {
                        throw new ErrorResponseException(HttpStatus.OK, PurchaseEnum.MODULE_ADDUSSER_GOBEYOND.getCode(), null);


                        //return "超过限定5个";
                    }
                }
                //减   id_UM
                if (reduceManager.size() > 0){

                    Set<String> managerList = new HashSet<>((Collection<? extends String>)indexObj.get("id_UM"));

                    //差集,两个数组相同的去掉 ，
                    managerList.removeAll(reduceManager);
                    //需要把Set再转回list吗
                    indexObj.put("id_UM", new ArrayList<>(managerList));

                    //修改模块
                    Update controlupdate = new Update();
                    controlupdate.set("control.objData.$", indexObj);
                    //controlupdate.set("control.$", moduleInit.getControl().get(0));
                    if (!ObjectUtils.isEmpty(controlupdate.getUpdateObject())) {
                        mongoTemplate.updateFirst(authQ, controlupdate, Asset.class);
                    }
                    //修改Rolex
                    reduceUserRolexAdministrators(id_C, ref, bcdLevel, reduceManager);
                }

                return retResult.ok(CodeEnum.OK.getCode(),null);

                //return "成功";
            }


        //}

        return null;
    }


    private void plusUserRolexAdministrators ( JSONObject indexObj, String id_C, String ref, Integer bcdLevel, List plusManager){



        List<Pair<Query, Update>> updateList = new ArrayList<>();
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);


        //修改user.rolex的
        for (int k = 0; k < plusManager.size(); k++) {

            Query rolexQ = new Query(
                    new Criteria("_id").is(plusManager.get(k))
                            .and("rolex.objComp.id_C").is(id_C));
            rolexQ.fields()
                    .include("rolex.objComp.$");//.include("info")


            User user = mongoTemplate.findOne(rolexQ, User.class);

            if (user == null) {
                continue;
            }

            JSONObject rolexMap =  user.getRolex().getJSONArray("objComp").getJSONObject(0);

            //HashMap<String, Object> rolexMap = (HashMap<String, Object>) rolexList.get(0);
            if (rolexMap.get("id_C").equals(id_C)) {

                JSONArray modulesList = rolexMap.getJSONArray("modules");

                //Map<String, Object> module = new HashMap<>();
                JSONObject  module = new JSONObject(5);
                module.put("id_UM", true);
                module.put("status", indexObj.get("status"));
                module.put("tfin", indexObj.get("tfin"));
                module.put("bcdLevel", indexObj.get("bcdLevel"));
                module.put("ref", indexObj.get("ref"));

                //有modules键的  直接加
                if (rolexMap.containsKey("modules")) {
                    boolean judge = true;

                    for (int j = 0; j < modulesList.size(); j++) {
                        JSONObject modulesMap =  modulesList.getJSONObject(j);
                        if (modulesMap.get("ref").equals(ref) && modulesMap.get("bcdLevel").equals(bcdLevel)) {
                            modulesMap.put("id_UM", true);

                            judge = false;
                        }

                    }
                    //里面没有重复的就等于 true
                    if (judge) {
                        modulesList.add(module);
                    }

                } else {
                    //第一次没有modules键的，多加一层
                    //ArrayList modulesArr = new ArrayList();
                    JSONArray modulesArr = new JSONArray();
                    modulesArr.add(module);
                    rolexMap.put("modules", modulesArr);
                }

                Update update = new Update();
                update.set("rolex.objComp.$", rolexMap);
                //把要修改的数据和查询条件放到updataList中，一起批量修改，性能提升
                Pair<Query, Update> updatePair = Pair.of(rolexQ, update);

                updateList.add(updatePair);


            }
        }

        operations.updateMulti(updateList);

        BulkWriteResult result = operations.execute();
        //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1



    }

    private void reduceUserRolexAdministrators ( String id_C, String ref, Integer bcdLevel, List reduceManager){



        List<Pair<Query, Update>> updateList = new ArrayList<>();
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);


        //修改user.rolex的
        for (int k = 0; k < reduceManager.size(); k++) {

            Query rolexQ = new Query(
                    new Criteria("_id").is(reduceManager.get(k))
                            .and("rolex.objComp.id_C").is(id_C));
            rolexQ.fields()
                    .include("rolex.objComp.$");//.include("info")


            User user = mongoTemplate.findOne(rolexQ, User.class);

            if (user == null) {
                continue;
            }

            JSONObject rolexMap =  user.getRolex().getJSONArray("objComp").getJSONObject(0);

            //HashMap<String, Object> rolexMap = (HashMap<String, Object>) rolexList.get(0);
            if (rolexMap.get("id_C").equals(id_C)) {

                JSONArray modulesList =  rolexMap.getJSONArray("modules");

                if (modulesList.size() == 0) {
                    continue;
                }

                for (int j = 0; j < modulesList.size(); j++) {
                    JSONObject modulesListMap =  modulesList.getJSONObject(j);
                    if (modulesListMap.get("ref").equals(ref) && modulesListMap.get("bcdLevel").equals(bcdLevel)) {

                        modulesListMap.put("id_UM",false);

                    }
                }

                Update update = new Update();
                update.set("rolex.objComp.$", rolexMap);
                //把要修改的数据和查询条件放到updataList中，一起批量修改，性能提升
                Pair<Query, Update> updatePair = Pair.of(rolexQ, update);

                updateList.add(updatePair);


            }
        }

        operations.updateMulti(updateList);

        BulkWriteResult result = operations.execute();
        //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1



    }


    @Override
    public ApiResponse changeState(String id_A, String id_U, String id_C, String ref, Integer bcdLevel, Integer bcdState) {

        //判断用户是创始人

        Query authQ = new Query(Criteria.where("_id").is(id_A)
                .and("control.objData").elemMatch(new Criteria("ref").is(ref)
                        .and("bcdLevel").is(bcdLevel)));  //To find matching documents

        authQ.fields().include("control.objData.$");

        //查询模块基本资料
        Asset moduleInit = mongoTemplate.findOne(authQ, Asset.class);
        if (moduleInit == null) {

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, PurchaseEnum.ASSET_NOT_FOUND.getCode(), null);

        }


        JSONObject indexObj =  moduleInit.getControl().getJSONArray("objData").getJSONObject(0);


        //HashMap<String, Object> indexObj = (HashMap<String, Object>) arrayList.get(0);
            //循环编号与版本一致的
            if (indexObj.get("ref").equals(ref) && indexObj.get("bcdLevel").equals(bcdLevel)) {
                //修改状态
                indexObj.put("bcdState",bcdState);

                //模块使用者数组
                JSONArray usersArray =  indexObj.getJSONArray("id_U");

                if (usersArray.size() > 0){
                    //修改模块状态，Rolex
                    upModuleState(id_C, ref, bcdLevel,bcdState ,usersArray);
                }


                //修改模块
                Update controlupdate = new Update();

                controlupdate.set("control.objData.$", indexObj);
                if (!ObjectUtils.isEmpty(controlupdate.getUpdateObject())) {
                    mongoTemplate.updateFirst(authQ, controlupdate, Asset.class);
                }

                return retResult.ok(CodeEnum.OK.getCode(),null);

            }





        return null;
    }



    private void upModuleState ( String id_C, String ref, Integer bcdLevel,Integer bcdState ,List usersArray){



        List<Pair<Query, Update>> updateList = new ArrayList<>();
        BulkOperations operations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);


        //修改user.rolex的
        for (int k = 0; k < usersArray.size(); k++) {

            Query rolexQ = new Query(
                    new Criteria("_id").is(usersArray.get(k))
                            .and("rolex.objComp.id_C").is(id_C));
            rolexQ.fields()
                    .include("rolex.objComp.$");//.include("info")


            User user = mongoTemplate.findOne(rolexQ, User.class);

            if (user == null) {
                continue;
            }

            JSONObject rolexMap =  user.getRolex().getJSONArray("objComp").getJSONObject(0);

            //Map<String, Object> rolexMap = (HashMap<String, Object>) rolexList.get(0);
            if (rolexMap.get("id_C").equals(id_C)) {

                JSONArray objModList =  rolexMap.getJSONArray("objMod");

                if (objModList.size() == 0) {
                    continue;
                }

                for (int j = 0; j < objModList.size(); j++) {
                    JSONObject modulesListMap =  objModList.getJSONObject(j);
                    if (modulesListMap.get("ref").equals(ref) && modulesListMap.get("bcdLevel").equals(bcdLevel)) {

                        modulesListMap.put("bcdState",bcdState);

                    }
                }

                Update update = new Update();
                update.set("rolex.objComp.$", rolexMap);
                //把要修改的数据和查询条件放到updataList中，一起批量修改，性能提升
                Pair<Query, Update> updatePair = Pair.of(rolexQ, update);

                updateList.add(updatePair);


            }
        }

        operations.updateMulti(updateList);

        BulkWriteResult result = operations.execute();
        //插入计数 insertedCount=0,匹配计数 matchedCount=300,删除计数 removedCount=0,被改进的 modifiedCount=1



    }


    @Override
    public ApiResponse renewModuleMoney(String id_U, JSONObject reqJson) throws ParseException {

        JSONObject result = new JSONObject();

        try {
            // 结果
            JSONObject calculation = renewCalculation(reqJson);

            if (null == calculation) {

                throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);

            }

            //到期日
            result.put("tfin",calculation.getString("tfin"));
            //模块人数
            result.put("wn0buyUser",calculation.getIntValue("wn0buyUser"));

            result.put("wn2EstPrice", calculation.getDouble("wn2EstPrice"));
            result.put("wn2PaidPrice", calculation.getDouble("wn2PaidPrice"));
            //续费人数
            result.put("buyUserResult", calculation.getJSONObject("buyUserResult"));
            //续费月数
            result.put("monthlyResult", calculation.getJSONObject("monthlyResult"));


        } catch (RuntimeException e) {

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);


        }

        return retResult.ok(CodeEnum.OK.getCode(),null);



    }






    private JSONObject renewCalculation (JSONObject reqJson) throws ParseException {

        // 获取模块名称
        String ref = reqJson.getString("ref");

        // 客户货币
        String lCR = reqJson.getString("lCR");

        // 等级参数
        Integer bcdLevel = reqJson.getInteger("bcdLevel");

        // 公司id
        String id_C = reqJson.getString("id_C");

        // 续费人数
        Integer renewWn0buyUser = reqJson.getInteger("wn0buyUser");

        // 续费月数
        Integer renewTdurMonth = reqJson.getInteger("tdurMonth");

        // 货币
        Double exchangeRate = 1d;



        // 判断必要参数是否为空
        if (
                StringUtils.isEmpty(ref) ||
                        StringUtils.isEmpty(lCR) ||
                        null == bcdLevel ||
                        null == renewWn0buyUser ||
                        null == renewTdurMonth
        ) {


            return null;
        }

        /*
            1.查找模块的基本参数
         */
        Query controlQ = new Query(
                new Criteria("info.id_C").is(id_C)
                        .and("info.ref").is("a-module")
                        .and("control.objData").elemMatch(new Criteria("ref").is(ref)
                        .and("bcdLevel").is(bcdLevel)));  //To find matching documents

//                        .and("control.objData.ref").is(ref)
//                        .and("control.objData.bcdLevel").is(bcdLevel));

        controlQ.fields().include("control.objData.$");

        Asset oldModule = mongoTemplate.findOne(controlQ, Asset.class);

        if (oldModule == null){
            return null;
        }


        //
        JSONObject dataJson = (JSONObject) JSON.toJSON(oldModule.getControl().getJSONArray("objData").get(0));



        // todo 根据mode再去init拿数据。然后再计算出钱   获取模块下的基本参数
        JSONObject oldModuleInit = getModuleInit(dataJson.getString("ref"), dataJson.getInteger("bcdLevel"),reqJson.getString("id_P"));

        //JSONObject oldModuleObj = oldModuleInit.getJSONObject("moduleObj");

        // 初始 module的基础数据  buyInit
        JSONObject moduleObj = oldModuleInit.getJSONObject("moduleObj");
        // 初始 exchangeRate 的数据
        JSONObject exchangeRateObj = oldModuleInit.getJSONObject("exchangeRate");
        // 初始化 moduleInfo的基础信息
        JSONObject moduleInfo = oldModuleInit.getJSONObject("moduleInfo");

        /*
            2.计算价格
         */

        // 基月费
        Double wn1BasePrice = moduleObj.getDouble("wn1BasePrice");

        // 人头数价格
        Double wn1HeadPrice = moduleObj.getDouble("wn1HeadPrice");

        // 免费人头数
        Integer wn0HeadFree = moduleObj.getInteger("wn0HeadFree");

        exchangeRate = exchangeRateObj.getDouble(lCR);
        // 汇率
//        if ("cn".equals(lNG)) {
//            lCR = "CNY";
//            exchangeRate = exchangeRateObj.getDouble("CNY");
//        } else if ("hk".equals(lNG)) {
//            lCR = "HKD";
//            exchangeRate = exchangeRateObj.getDouble("HKD");
//        } else if ("aud".equals(lNG)) {
//            lCR = "AUD";
//            exchangeRate = exchangeRateObj.getDouble("AUD");
//        }


        JSONObject resultJson = new JSONObject();
        //续费月份结果
        JSONObject monthlyResult = new JSONObject();
        //新增人数结果
        JSONObject buyUserResult = new JSONObject();

        //续费月份
        if (renewTdurMonth != null && renewTdurMonth > 0){


            // 折扣
            Double duration = null;

            // 应付款
            Double wn2EstPrice = null;

            // 实付款
            Double wn2PaidPrice = null;



            //现在模块购买的人数
            Integer wn0maxUser = dataJson.getInteger("wn0buyUser");

            /*
            人数计算
            */

            // 初始化人数计算
            Double uNumTotal = 0.0;

            // 如果免费人数大于或者等于用户的选择人数,那就不算人数价格
            if (wn0HeadFree < wn0maxUser) {

                // uNumTotal  =  人头数价格    * ( 现在人数   -  免费人数)     * 续费月份
                uNumTotal = wn1HeadPrice * (wn0maxUser - wn0HeadFree) * renewTdurMonth;
            }



            // 获取discount
            JSONArray discounts = moduleObj.getJSONArray("objDiscount");

            for (int i = 0; i < discounts.size(); i++) {

                JSONObject discount = (JSONObject) discounts.get(i);

                // 判断用户的月份是否符合折扣的月份
                if (discount.getInteger("duration") <= renewTdurMonth) {

                    // 获取其折扣
                    duration = discount.getDouble("number");

                }

            }
            //应付款  =  ((基月费 * 续费月份) + 人数) * 汇率
            wn2EstPrice = ((wn1BasePrice * renewTdurMonth) + uNumTotal) * exchangeRate;
            monthlyResult.put("wn2EstPrice",wn2EstPrice / exchangeRateObj.getDouble("CNY"));
            // 判断是否有折扣
            if (null == duration) {
                wn2PaidPrice = wn2EstPrice ;
            } else {

                wn2PaidPrice = wn2EstPrice * duration ;
            }

            monthlyResult.put("wn2PaidPrice",wn2PaidPrice / exchangeRateObj.getDouble("CNY"));


        }

        //续费新增人数
        if(renewWn0buyUser != null && renewWn0buyUser > 0){

            // 折扣
            Double duration = null;

            // 应付款
            Double wn2EstPrice = null;

            // 实付款
            Double wn2PaidPrice = null;



            // 获取旧数据的开始时间(开始时间要拿当前日期)
            Date startDate = dateUtils.strTurnDate(DateUtils.getDateByT(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
            // 获取旧数据的结束时间
            Date endDate = dateUtils.strTurnDate(dataJson.getString("tfin"));
            // 计算出两个时间差多少个月
            Integer surplusMonth = DateUtils.getMonthDiff(startDate, endDate);

            //  月数    = 续费月数 +  剩余月数
            Integer tdurMonth =  renewTdurMonth + surplusMonth;



            // 所有人数 = 现在模块购买的人数  + 续费人数
            Integer wn0maxUser = dataJson.getInteger("wn0buyUser") + renewWn0buyUser;

            /*
            人数计算
            */

            // 初始化人数计算
            Double uNumTotal = 0.0;

            // 如果免费人数大于或者等于用户的所有人数,那就不算人数价格
            if (wn0HeadFree < wn0maxUser) {

                // uNumTotal  =  (人头数价格    *  续费人数)      * 续费月份
                uNumTotal =  (wn1HeadPrice * renewWn0buyUser)  * tdurMonth;
            }



            // 获取discount
            JSONArray discounts = moduleObj.getJSONArray("objDiscount");

            for (int i = 0; i < discounts.size(); i++) {

                JSONObject discount = (JSONObject) discounts.get(i);

                // 判断用户的月份是否符合折扣的月份
                if (discount.getInteger("duration") <= tdurMonth) {

                    // 获取其折扣
                    duration = discount.getDouble("number");

                }

            }
            //应付款  =          ((基月费 * 所有月份) + 续费人数) * 汇率
            wn2EstPrice = ((wn1BasePrice * tdurMonth) + uNumTotal) * exchangeRate;
            buyUserResult.put("wn2EstPrice",wn2EstPrice / exchangeRateObj.getDouble("CNY"));
            // 判断是否有折扣
            if (null == duration) {

                wn2PaidPrice = wn2EstPrice ;
            } else {

                wn2PaidPrice = wn2EstPrice * duration ;
            }
            buyUserResult.put("wn2PaidPrice",wn2PaidPrice / exchangeRateObj.getDouble("CNY"));
        }

        resultJson.put("monthlyResult",monthlyResult);
        resultJson.put("buyUserResult",buyUserResult);



        //模块名称
        resultJson.put("wcnN",dataJson.getString("wcnN"));

        //货币
        resultJson.put("lCR",lCR);

        //续费人数
        resultJson.put("renewWn0buyUser",renewWn0buyUser);

        //续费人数
        resultJson.put("renewTdurMonth",renewTdurMonth);

        //到期日
        resultJson.put("tfin",dataJson.getString("tfin"));
        //模块人数
        resultJson.put("wn0buyUser",dataJson.getIntValue("wn0buyUser"));
        // 应付款
        resultJson.put("wn2EstPrice", Double.valueOf(String.format("%.2f", monthlyResult.getDouble("wn2EstPrice") + buyUserResult.getDouble("wn2EstPrice"))));
        // 实付款
        resultJson.put("wn2PaidPrice", Double.valueOf(String.format("%.2f", monthlyResult.getDouble("wn2PaidPrice") + buyUserResult.getDouble("wn2PaidPrice"))));



        return resultJson;
    }

}
