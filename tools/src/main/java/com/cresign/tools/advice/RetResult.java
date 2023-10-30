package com.cresign.tools.advice;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.apires.LocalMessage;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangzejin
 * @updated 2019/8/23
 * @ver 1.0.0
 * ##description: 作者很懒, 什么也没写...
 */
@Component
public class RetResult {

    public static HttpServletRequest request;

    public static StringRedisTemplate redisTemplate0;

    @Autowired
    public void setRetResult(HttpServletRequest request,StringRedisTemplate redisTemplate0){
        RetResult.request = request;
        RetResult.redisTemplate0 = redisTemplate0;
    }


    @Autowired
    private LocalMessage localMessage;

    public static final String RED_KEY = "key:k_";

    /**
     * 返回加密數據給前端
     * @param httpStatus  web响应码
     * @param code        自定义状态码
     * @param message     返回数据
     * @author JackSon
     * @updated 2020/7/29 9:53
     * @return java.lang.String
     */
    public static String jsonResultEncrypt(HttpStatus httpStatus, String code, Object message
//            ,String qdKey
    ){

        if (ObjectUtils.isNotEmpty(message)) {
            // 根据异常信息抛出信息
            throw new ResponseException(httpStatus, code, JSONObject.toJSONString(encodeAesRsa(message)));
            //throw new ResponseException(httpStatus, code, JSONObject.toJSONString(message));
        } else {
            // 根据异常信息抛出信息
            throw new ResponseException(httpStatus, code, "");
        }

    }

    /**
     * 返回沒有加密的數據
     * @author JackSon
     * @param httpStatus
     * @param code
     * @param message
     * @ver 1.0
     * @updated 2020/8/25 14:17
     * @return java.lang.String
     */
    public static String jsonResult(HttpStatus httpStatus, String code, Object message){

        if (ObjectUtils.isNotEmpty(message)) {


            // 根据异常信息抛出信息
            throw new ResponseException(httpStatus, code, JSONObject.toJSONString(message));
        } else {
            // 根据异常信息抛出信息
            throw new ResponseException(httpStatus, code, "");
        }

    }

    /**
     * 返回沒有加密的错误數據
     * @author JackSon
     * @param httpStatus
     * @param code
     * @param message
     * @ver 1.0
     * @updated 2020/8/25 14:17
     * @return java.lang.String
     */
    public static String errorJsonResult(HttpStatus httpStatus, String code, Object message){

        if (ObjectUtils.isNotEmpty(message)) {

            // 根据异常信息抛出信息
            throw new ErrorResponseException(httpStatus, code, JSONObject.toJSONString(message));
        } else {
            // 根据异常信息抛出信息
            throw new ErrorResponseException(httpStatus, code, "");
        }

    }


    public ApiResponse error(String code, Object message){

        if (ObjectUtils.isNotEmpty(message)) {
            return new ApiResponse("500", JSONObject.toJSONString(encodeAesRsa(message
            )), localMessage.getLocaleMessage(code, "", null));
        } else {
            return new ApiResponse("500", "", localMessage.getLocaleMessage(code, "", null));
        }
    }


    public ApiResponse ok(String code, Object message){

        if (ObjectUtils.isNotEmpty(message)) {

            return new ApiResponse(code, JSONObject.toJSONString(encodeAesRsa(message
            )), localMessage.getLocaleMessage(code, "", null));
        } else {
            return new ApiResponse(code, "", localMessage.getLocaleMessage(code, "", null));
        }
    }

    public ApiResponse okNoEncode(String code, Object message){

        if (ObjectUtils.isNotEmpty(message)) {
            return new ApiResponse(code, JSONObject.toJSONString(message), localMessage.getLocaleMessage(code, "", null));
        } else {
            return new ApiResponse(code, "", localMessage.getLocaleMessage(code, "", null));
        }
    }




    /**
     * 混合加密
     * @param body      传入加密内容
     * @author         JackSon
     * @updated     2020/7/29 9:56
     * @return         java.lang.Object
     */
    private static Object encodeAesRsa(Object body
//            ,String qdKey
    ){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
//            System.out.println("request:");
//            System.out.println(JSON.toJSONString(request.getParts()));
            String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            String uuId = request.getHeader("uuId");
//            String isDecrypt = request.getHeader("isDecrypt");
//            System.out.println("加密的=uuId:");
//            System.out.println(uuId);
//            uuId = "e82697e7-cc5f-9c5e-ae32-76d9b7c4cfbb";
            String s = redisTemplate0.opsForValue().get(RED_KEY + uuId);
//            System.out.println("s:");
//            System.out.println(s);
            JSONObject re = JSONObject.parseObject(s);
//            System.out.println("re:");
//            System.out.println(JSON.toJSONString(re));
            if (null != re) {
                String qdKey = re.getString("qdKey");
//                System.out.println("前端公钥:");
//                System.out.println(qdKey);
                // 生成aes秘钥
//                String aseKey = getRandomString(16);
                String aseKey = AesUtil.getKey();
                // rsa加密
//                String encrypted = RSAUtils.encryptedDataOnJava(aseKey, client_Public_Key);
//                String encrypted = RsaTest.publicEncrypt(aseKey,RsaTest.getPublicKey(client_Public_Key));

                String encrypted;
//                if (isDecrypt.equals("false")) {
//                    encrypted = Base64.encodeBase64String(RsaUtilF.
//                            encryptByPublicKey(aseKey.getBytes(), QD_Key));
//                } else {
                    encrypted = Base64.encodeBase64String(RsaUtilF.
                            encryptByPublicKey(aseKey.getBytes(), qdKey));
//                }

                // aes加密
//                String requestData = AesEncryptUtils.encrypt(result, aseKey);
                String requestData = AesUtil.encrypt(result, aseKey);
                Map<String, String> map = new HashMap<>();
                map.put("encrypted", encrypted);
                map.put("requestData", requestData);
                map.put("err","0");
                return map;
            } else {
//                log.error("id对应秘钥信息为空");
                Map<String, String> map = new HashMap<>();
                map.put("err","1");
                map.put("desc","id对应秘钥信息为空");
                return new ApiResponse("043004", "", "");

//                return map;
            }
        } catch (Exception e) {
//            log.error("对方法method :【" +"】返回数据进行解密出现异常：" + e.getMessage());
            Map<String, String> map = new HashMap<>();
            map.put("err","1");
            map.put("desc","对方法method :【" +"】返回数据进行解密出现异常：" + e.getMessage());
            return map;
//            e.printStackTrace();
//            throw  new RuntimeException("id对应秘钥为空!");
        }
//        return body;
    }

}
