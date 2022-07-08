package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.SearchEnum;
import com.cresign.login.service.RedirectService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.authFilt.AuthCheck;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.po.Comp;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.Prod;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.uuid.UUID19;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2021-05-03 9:55
 * ##version: 1.0
 */
@Service
public class RedirectServiceImpl implements RedirectService {

    public static final String SCANCODE_SHAREPROD = "scancode:shareprod-";
    public static final String HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T = "https://www.cresign.cn/qrCodeTest?qrType=shareprod&t=";
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate1;

    @Autowired
    private RetResult retResult;

    @Autowired
    private AuthCheck authCheck;

    @Override
    @Transactional
    public ApiResponse generateProdCode(String id_C, String id_P, String id_U, String mode, JSONObject data) {

        /**
         * 1. 从mongodb获取到token，到redis中查找是否有该token
         * 2. 将前端传入的参数，存放到redis，用随机生成的uuid作为key, 前端传入参数作为val
         * 3. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query prodQ = new Query(new Criteria("_id").is(id_P).and("info.id_C").is(id_C));
        Prod prod = mongoTemplate.findOne(prodQ, Prod.class);
        if (ObjectUtils.isEmpty(prod)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }
        if (ObjectUtils.isNotEmpty(prod.getQrShareCode())) {
            if (StringUtils.isNotEmpty(prod.getQrShareCode().getString("token"))) {
                JSONObject qrShareCode = prod.getQrShareCode();
                String code_token = qrShareCode.getString("token");
                if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                    throw new ErrorResponseException(HttpStatus.OK, SearchEnum.PROD_CODE_IS_EXIT.getCode(), code_token);
                }
            }
        }
        
        
        // 2.
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id_C", id_C);
        jsonObject.put("id_P", id_P);
        jsonObject.put("grp", prod.getInfo().getGrp());
        jsonObject.put("create_id_U", id_U);
        jsonObject.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        if ("frequency".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("count", data.getString("count"));
            jsonObject.put("used_count", "0");
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
        } else if ("time".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("endTimeSec", data.getString("endTimeSec"));
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
            redisTemplate1.expire(keyName, data.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        Update update = new Update();
        update.set("qrShareCode.token",token);
        update.set("qrShareCode.data", jsonObject);

        mongoTemplate.updateFirst(prodQ, update, Prod.class);

        // 3.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }



    @Override
    @Transactional
    public ApiResponse generateUserCode(String id_C,  String id_U, String mode, JSONObject data) {

        /**
         * 1. 从mongodb获取到token，到redis中查找是否有该token
         * 2. 将前端传入的参数，存放到redis，用随机生成的uuid作为key, 前端传入参数作为val
         * 3. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query userQ = new Query(new Criteria("_id").is(id_U));

        userQ.fields().include("rolex.objComp."+id_C).include("qrShareCode");
        User user = mongoTemplate.findOne(userQ, User.class);
        if (ObjectUtils.isEmpty(user)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }
        if (ObjectUtils.isNotEmpty(user.getQrShareCode())) {
            if (StringUtils.isNotEmpty(user.getQrShareCode().getString("token"))) {
                JSONObject qrShareCode = user.getQrShareCode();
                String code_token = qrShareCode.getString("token");
                if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                    throw new ErrorResponseException(HttpStatus.OK, SearchEnum.
PROD_CODE_IS_EXIT.getCode(), HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + code_token);
                }
            }
        }


        // 2.
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id_C", id_C);
        jsonObject.put("id", id_U);

//        ArrayList rolexList = (ArrayList) user.getRolex().get("objComp");
//        HashMap<String, Object> rolexMap = (HashMap<String, Object>) rolexList.get(0);
//        jsonObject.put("grp",rolexMap.get("grpU"));
        jsonObject.put("grp","1099");
        jsonObject.put("create_id_U", id_U);
        jsonObject.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        if ("frequency".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("count", data.getString("count"));
            jsonObject.put("used_count", "0");
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
        } else if ("time".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("endTimeSec", data.getString("endTimeSec"));
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
            redisTemplate1.expire(keyName, data.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        Update update = new Update();
        update.set("qrShareCode.token",token);
        update.set("qrShareCode.data", jsonObject);

        mongoTemplate.updateFirst(userQ, update, User.class);

        // 3.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }

    @Override
    @Transactional
    public ApiResponse generateCompCode(String id_C,  String id_U, String mode, JSONObject data) {

        /**
         * 1. 从mongodb获取到token，到redis中查找是否有该token
         * 2. 将前端传入的参数，存放到redis，用随机生成的uuid作为key, 前端传入参数作为val
         * 3. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query compQ = new Query(new Criteria("_id").is(id_C));//.and("info.id_C").is(id_C)

        compQ.fields().include("qrShareCode");
        Comp comp = mongoTemplate.findOne(compQ, Comp.class);
        if (ObjectUtils.isEmpty(comp)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }
        if (ObjectUtils.isNotEmpty(comp.getQrShareCode())) {
            if (StringUtils.isNotEmpty(comp.getQrShareCode().getString("token"))) {
                JSONObject qrShareCode = comp.getQrShareCode();
                String code_token = qrShareCode.getString("token");
                if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                    throw new ErrorResponseException(HttpStatus.OK, SearchEnum.
PROD_CODE_IS_EXIT.getCode(), HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + code_token);
                }
            }
        }


        // 2.
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id_C", id_C);
        jsonObject.put("id", id_C);
        jsonObject.put("grp","1099");
        jsonObject.put("create_id_U", id_U);
        jsonObject.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        if ("frequency".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("count", data.getString("count"));
            jsonObject.put("used_count", "0");
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
        } else if ("time".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("endTimeSec", data.getString("endTimeSec"));
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
            redisTemplate1.expire(keyName, data.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        Update update = new Update();
        update.set("qrShareCode.token",token);
        update.set("qrShareCode.data", jsonObject);

        mongoTemplate.updateFirst(compQ, update, Comp.class);

        // 3.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }

    @Override
    @Transactional
    public ApiResponse generateOrderCode(String id_C,  String id_U,String id_O,String listType, String mode, JSONObject data) {

        /**
         * 1. 从mongodb获取到token，到redis中查找是否有该token
         * 2. 将前端传入的参数，存放到redis，用随机生成的uuid作为key, 前端传入参数作为val
         * 3. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query orderQ = new Query(new Criteria("_id").is(id_O));

        orderQ.fields().include("qrShareCode").include("info");
        Order order = mongoTemplate.findOne(orderQ, Order.class);
        if (ObjectUtils.isEmpty(order) && order.getInfo().getLST() == null
                && Integer.parseInt(order.getInfo().getLST().toString()) < 8) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }
        if (ObjectUtils.isNotEmpty(order.getQrShareCode())) {
            if (StringUtils.isNotEmpty(order.getQrShareCode().getString("token"))) {
                JSONObject qrShareCode = order.getQrShareCode();
                String code_token = qrShareCode.getString("token");
                if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                    throw new ErrorResponseException(HttpStatus.OK, SearchEnum.
PROD_CODE_IS_EXIT.getCode(), HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + code_token);
                }
            }
        }


        // 2.
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id_C", id_C);
        jsonObject.put("id", id_O);
        if (listType.equals("lSOrder") && order.getInfo().getGrp() != null){
            jsonObject.put("grp",order.getInfo().getGrp());
        }else if (listType.equals("lBOrder") && order.getInfo().getGrpB() != null){
            jsonObject.put("grp",order.getInfo().getGrpB());
        }else{
            jsonObject.put("grp","1099");
        }


        jsonObject.put("create_id_U", id_U);
        jsonObject.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        if ("frequency".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("count", data.getString("count"));
            jsonObject.put("used_count", "0");
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
        } else if ("time".equals(mode)) {
            jsonObject.put("mode", mode);
            jsonObject.put("endTimeSec", data.getString("endTimeSec"));
            redisTemplate1.opsForHash().putAll(keyName, jsonObject);
            redisTemplate1.expire(keyName, data.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        Update update = new Update();
        update.set("qrShareCode.token",token);
        update.set("qrShareCode.data", jsonObject);

        mongoTemplate.updateFirst(orderQ, update, Order.class);

        // 3.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }


    @Override
    public ApiResponse scanCode(String token,String listType , String id_U) {

        String keyName = SCANCODE_SHAREPROD + token;

        Boolean hasKey = redisTemplate1.hasKey(keyName);
        if (!hasKey) {
            throw new ErrorResponseException(HttpStatus.OK, SearchEnum.
PROD_CODE_OVERDUE.getCode(),null);
        }
        Map<Object, Object> entries = redisTemplate1.opsForHash().entries(keyName);
        JSONObject prodJson = (JSONObject) JSONObject.toJSON(entries);

        /**
         * 如果是 frequency 次数模式,则需要拿出已经使用的次数和当前要限制的次数对比
         * 如果是 time 时间模式
         */
        if ("frequency".equals(entries.get("mode"))) {

            // 限制的次数
            int count = Integer.parseInt(entries.get("count").toString());

            // 已使用次数
            int used_count = Integer.parseInt(entries.get("used_count").toString());

            /**
             * 如果使用的次数 +1 == 限制的次数, 则key，等获取完数据后进行删除key
             * 如果使用次数 > 限制次数, 则提醒用户二维码已过期并且删除key
             * 如果使用次数 < 次数，则累加 used_count
             */
            if (used_count +1 == count) {
                System.out.println("进来 used_count +1 == count");

                ApiResponse apiResponse = null;
                try {
                    if (listType.equals("lNUser")){
                        apiResponse = shareUserCode(id_U, prodJson);
                    }else if (listType.equals("lSProd")){
                        apiResponse = shareProdCode(id_U, prodJson);
                    }else if (listType.equals("lNComp")){
                        apiResponse = shareCompCode(id_U, prodJson);
                    }else if (listType.equals("lSOrder") || listType.equals("lBOrder")){
                        apiResponse = shareOrderCode(id_U, prodJson,listType);
                    }

                    redisTemplate1.delete(keyName);
                } catch (RuntimeException e) {
                    redisTemplate1.opsForHash().putAll(keyName, entries);
                    throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                }
                return apiResponse;

            } else if (used_count > count) {
                System.out.println("进来 used_count > count");

                redisTemplate1.delete(keyName);
                throw new ErrorResponseException(HttpStatus.OK, SearchEnum.
PROD_CODE_OVERDUE.getCode(), null);
            } else {
                System.out.println("进来 used_count < count");
                // 累加
                ApiResponse apiResponse = null;
                try {
                    if (listType.equals("lNUser")){
                        apiResponse = shareUserCode(id_U, prodJson);
                    }else if (listType.equals("lSProd")){
                        apiResponse = shareProdCode(id_U, prodJson);
                    }else if (listType.equals("lNComp")){
                        apiResponse = shareCompCode(id_U, prodJson);
                    }else if (listType.equals("lSOrder") || listType.equals("lBOrder")){
                        apiResponse = shareOrderCode(id_U, prodJson,listType);
                    }
                    //使用次数+1
                    redisTemplate1.opsForHash().increment(keyName, "used_count", 1);
                } catch (RuntimeException e) {
                    redisTemplate1.opsForHash().put(keyName, "used_count", String.valueOf(used_count));
                    throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
                }
                return apiResponse;
            }
        } else if ("time".equals(entries.get("mode"))) {

            if (listType.equals("lNUser")){
                return shareUserCode(id_U, prodJson);
            }else if (listType.equals("lSProd")){
                return shareProdCode(id_U, prodJson);
            }else if (listType.equals("lNComp")){
                return shareCompCode(id_U, prodJson);
            }else if (listType.equals("lSOrder") || listType.equals("lBOrder")){
                return shareOrderCode(id_U, prodJson,listType);
            }

        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);

    }

    @Transactional
    @Override
    public ApiResponse resetProdCode(String id_U, String id_P, String id_C) {

        /**
         * 1. 判断该产品是否存在
         * 2. 将原来的二维码数据提取出来，再重新设置到redis中
         * 3. 判断原来的二维码token在redis中是否还存在，存在则删除
         * 4. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query prodQ = new Query(new Criteria("_id").is(id_P).and("info.id_C").is(id_C));
        Prod prod = mongoTemplate.findOne(prodQ, Prod.class);
        if (ObjectUtils.isEmpty(prod)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }

        // 2.
        JSONObject dataJson = prod.getQrShareCode().getJSONObject("data");
        dataJson.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        String mode = dataJson.getString("mode");

        if ("frequency".equals(mode)) {
            dataJson.put("used_count","0");
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
        } else if ("time".equals(mode)) {
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
            redisTemplate1.expire(keyName, dataJson.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        // 3.
        if (StringUtils.isNotEmpty(prod.getQrShareCode().getString("token"))) {
            String code_token = prod.getQrShareCode().getString("token");
            if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                redisTemplate1.delete(SCANCODE_SHAREPROD + code_token);
            }
        }

        //mongoTemplate.updateFirst(prodQ, Update.update("qrShareCode.token",token), Prod.class);
        prod.getQrShareCode().put("token",token);
        mongoTemplate.updateFirst(prodQ, Update.update("qrShareCode",prod.getQrShareCode()), Prod.class);



        // 4.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }


    @Transactional
    @Override
    public ApiResponse resetUserCode(String id_U,  String id_C) {

        /**
         * 1. 判断该用户是否存在
         * 2. 将原来的二维码数据提取出来，再重新设置到redis中
         * 3. 判断原来的二维码token在redis中是否还存在，存在则删除
         * 4. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query userQ = new Query(new Criteria("_id").is(id_U));//.and("info.id_C").is(id_C)
        User user = mongoTemplate.findOne(userQ, User.class);
        if (ObjectUtils.isEmpty(user)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }

        // 2.
        JSONObject dataJson = user.getQrShareCode().getJSONObject("data");
        dataJson.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        String mode = dataJson.getString("mode");

        if ("frequency".equals(mode)) {
            dataJson.put("used_count","0");
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
        } else if ("time".equals(mode)) {
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
            redisTemplate1.expire(keyName, dataJson.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        // 3.
        if (StringUtils.isNotEmpty(user.getQrShareCode().getString("token"))) {
            String code_token = user.getQrShareCode().getString("token");
            if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                redisTemplate1.delete(SCANCODE_SHAREPROD + code_token);
            }
        }

        user.getQrShareCode().put("token",token);
        mongoTemplate.updateFirst(userQ, Update.update("qrShareCode",user.getQrShareCode()), User.class);



        // 4.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }


    @Transactional
    @Override
    public ApiResponse resetCompCode(String id_U,  String id_C) {

        /**
         * 1. 判断该公司是否存在
         * 2. 将原来的二维码数据提取出来，再重新设置到redis中
         * 3. 判断原来的二维码token在redis中是否还存在，存在则删除
         * 4. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query compQ = new Query(new Criteria("_id").is(id_C));//.and("info.id_C").is(id_C)
        Comp comp = mongoTemplate.findOne(compQ, Comp.class);
        if (ObjectUtils.isEmpty(comp)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }

        // 2.
        JSONObject dataJson = comp.getQrShareCode().getJSONObject("data");
        dataJson.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        String mode = dataJson.getString("mode");

        if ("frequency".equals(mode)) {
            dataJson.put("used_count","0");
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
        } else if ("time".equals(mode)) {
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
            redisTemplate1.expire(keyName, dataJson.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        // 3.
        if (StringUtils.isNotEmpty(comp.getQrShareCode().getString("token"))) {
            String code_token = comp.getQrShareCode().getString("token");
            if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                redisTemplate1.delete(SCANCODE_SHAREPROD + code_token);
            }
        }

        comp.getQrShareCode().put("token",token);
        mongoTemplate.updateFirst(compQ, Update.update("qrShareCode",comp.getQrShareCode()), Comp.class);

        // 4.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }

    @Transactional
    @Override
    public ApiResponse resetOrderCode(String id_U, String id_O, String id_C) {

        /**
         * 1. 判断该订单是否存在
         * 2. 将原来的二维码数据提取出来，再重新设置到redis中
         * 3. 判断原来的二维码token在redis中是否还存在，存在则删除
         * 4. 并返回 url 拼接 key返回给前端
         */

        // 1.
        Query orderQ = new Query(new Criteria("_id").is(id_O));//.and("info.id_C").is(id_C)
        Order order = mongoTemplate.findOne(orderQ, Order.class);
        if (ObjectUtils.isEmpty(order)) {
            throw new ErrorResponseException(HttpStatus.OK, CodeEnum.NOT_FOUND.getCode(), "");
        }

        // 2.
        JSONObject dataJson = order.getQrShareCode().getJSONObject("data");
        dataJson.put("create_time", DateUtils.getDateByT(DateEnum.DATE_TWO.getDate()));

        String token = UUID19.uuid();
        String keyName = SCANCODE_SHAREPROD + token;

        String mode = dataJson.getString("mode");

        if ("frequency".equals(mode)) {
            dataJson.put("used_count","0");
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
        } else if ("time".equals(mode)) {
            redisTemplate1.opsForHash().putAll(keyName, dataJson);
            redisTemplate1.expire(keyName, dataJson.getInteger("endTimeSec"), TimeUnit.SECONDS);
        } else {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }

        // 3.
        if (StringUtils.isNotEmpty(order.getQrShareCode().getString("token"))) {
            String code_token = order.getQrShareCode().getString("token");
            if (redisTemplate1.hasKey(SCANCODE_SHAREPROD + code_token)) {
                redisTemplate1.delete(SCANCODE_SHAREPROD + code_token);
            }
        }

        order.getQrShareCode().put("token",token);
        mongoTemplate.updateFirst(orderQ, Update.update("qrShareCode",order.getQrShareCode()), Order.class);

        // 4.
        String url = HTTPS_WWW_CRESIGN_CN_QR_CODE_TEST_QR_TYPE_SHAREPROD_T + token;
        return retResult.ok(CodeEnum.OK.getCode(), url);
    }


    private ApiResponse shareProdCode(String id_U, JSONObject prodJson) {
        // 校验权限先校验如果是rolex中有这家公司则可以直接拿当前他的权限，否则就是游客


        JSONObject rolex = MongoUtils.getRolex(id_U, prodJson.getString("id_C"), mongoTemplate);

        JSONArray viewArray;
        // 游客权限
        if (ObjectUtils.isEmpty(rolex)) {
            viewArray = authCheck.getUserSelectAuth(prodJson.getString("create_id_U"), prodJson.getString("id_C"),"1099",
                    "lSProd",prodJson.getString("grp"),"card");
        } else {
            viewArray = authCheck.getUserSelectAuth(prodJson.getString("create_id_U"), prodJson.getString("id_C"),"1000",
                            "lSProd",prodJson.getString("grp"),"card");
        }


        Query query = new Query(new Criteria("_id").is(prodJson.getString("id_P"))
                .and("info.id_C").is(prodJson.getString("id_C")));
        for (Object key : viewArray) {
            query.fields().include(key.toString());

        }
        Prod prod = mongoTemplate.findOne(query, Prod.class);
        System.out.println("prod = " + JSONObject.toJSON(prod));
        return retResult.ok(CodeEnum.OK.getCode(),prod);
    }

    private ApiResponse shareOrderCode(String id_U, JSONObject prodJson,String listType) {
        // 校验权限先校验如果是rolex中有这家公司则可以直接拿当前他的权限，否则就是不给访问
        Query userQ = new Query(
                new Criteria("_id").is(id_U)
                        .and("rolex.objComp.id_C").is(prodJson.getString("id_C"))
        );
        //userQ.fields().include("rolex.objComp.$");
        User user = mongoTemplate.findOne(userQ, User.class);

        //用户对象不存证明不是这家公司的员工
        if (user == null){

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, SearchEnum.USER_IS_NO_FOUND.getCode(), null);

        }

        JSONArray viewArray;

        viewArray = authCheck.getUserSelectAuth(prodJson.getString("create_id_U"), prodJson.getString("id_C"),"1000",
                    listType,prodJson.getString("grp"),"card");



        Query query = new Query(new Criteria("_id").is(prodJson.getString("id"))
                .and("info.id_C").is(prodJson.getString("id_C")));
        for (Object key : viewArray) {
            query.fields().include(key.toString());
        }
        Order order = mongoTemplate.findOne(query, Order.class);

        return retResult.ok(CodeEnum.OK.getCode(),order);
    }

    private ApiResponse shareUserCode(String id_U,JSONObject prodJson) {


        Query query = new Query(new Criteria("_id").is(id_U));
        User user1 = mongoTemplate.findOne(query, User.class);
        return retResult.ok(CodeEnum.OK.getCode(),user1);
    }

    private ApiResponse shareCompCode(String id_U,JSONObject prodJson) {


        Query query = new Query(new Criteria("_id").is(prodJson.getString("id_C")));
        Comp comp = mongoTemplate.findOne(query, Comp.class);
        return retResult.ok(CodeEnum.OK.getCode(),comp);
    }


}