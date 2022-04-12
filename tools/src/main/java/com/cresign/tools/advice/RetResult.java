package com.cresign.tools.advice;

import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.apires.LocalMessage;
import com.cresign.tools.encrypt.AesEncryptUtils;
import com.cresign.tools.encrypt.RSAUtils;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * ##author: tangzejin
 * ##updated: 2019/8/23
 * ##version: 1.0.0
 * ##description: 作者很懒, 什么也没写...
 */
@Slf4j
@Component
public class RetResult {


    @Value("${encyptKey.public_key}")
    public void setClient_Public_Key(String client_Public_Key) {
       RetResult.client_Public_Key = client_Public_Key;
    }

    // 加密的key
    private static String client_Public_Key;

    @Autowired
    private LocalMessage localMessage;



    /**
     * 返回加密數據給前端
     * ##Params: httpStatus  web响应码
     * ##Params: code        自定义状态码
     * ##Params: message     返回数据
     * ##author: JackSon
     * ##updated: 2020/7/29 9:53
     * ##Return: java.lang.String
     */
    public static String jsonResultEncrypt(HttpStatus httpStatus, String code, Object message){

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
     * ##author: JackSon
     * ##Params: httpStatus
     * ##Params: code
     * ##Params: message
     * ##version: 1.0
     * ##updated: 2020/8/25 14:17
     * ##Return: java.lang.String
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
     * ##author: JackSon
     * ##Params: httpStatus
     * ##Params: code
     * ##Params: message
     * ##version: 1.0
     * ##updated: 2020/8/25 14:17
     * ##Return: java.lang.String
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

    public ApiResponse ok(String code, Object message){

        if (ObjectUtils.isNotEmpty(message)) {
            return new ApiResponse(code, JSONObject.toJSONString(encodeAesRsa(message)), localMessage.getLocaleMessage(code, "", null));
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
     * ##Params: body      传入加密内容
     * ##author:         JackSon
     * ##updated:     2020/7/29 9:56
     * ##Return:         java.lang.Object
     */
    private static Object encodeAesRsa(Object body){


        ObjectMapper objectMapper = new ObjectMapper();

//        String client_Public_Key = (String) redisTemplate0.opsForHash().get("serverEncyptKey", "public_key");

        try {
            String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
            // 生成aes秘钥
            String aseKey = getRandomString(16);
            // rsa加密
            String encrypted = RSAUtils.encryptedDataOnJava(aseKey, client_Public_Key);
            // aes加密
            String requestData = AesEncryptUtils.encrypt(result, aseKey);
            Map<String, String> map = new HashMap<>();
            map.put("encrypted", encrypted);
            map.put("requestData", requestData);
            return map;
        } catch (Exception e) {
            log.error("对方法method :【" +"】返回数据进行解密出现异常：" + e.getMessage());
        }
        return body;
    }

    /**
     * 创建指定位数的随机字符串
     * ##Params: length 表示生成字符串的长度
     * ##return: 字符串
     */
    private static String getRandomString(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    // 以后可能会用到
//    /**
//     * 用来新增日志，与抛出异常信息
//     * ##Params: id_C  公司id
//     * ##Params: id_U  用户id
//     * ##Params: logType   日志类型
//     * ##Params: zcndesc 日志描述
//     * ##Params: data  具体数据
//     * ##Params: my    异常信息
//     * ##Params: message   异常具体信息
//     * ##Params: logService    新增日志接口
//     * ##return:  异常结果
//     */
//    public static String exBZ2ByDesc(String id_C, String id_U, String logType
//            , String zcndesc
//            , Map<String,Object> data
//            , MyExEnum my, String message
//            , LogService logService
//            , LogEnum logEnum){
//
//        if (logEnum.getType().equals("1")) {
//
//            // 获取日志对象
//            Log log = TY.setLogRZ(id_C,id_U,logType,data,my);
//
//            // 判断日志内容不为空
//            if (TY.StringISnull(zcndesc)) {
//
//                // 设置日志内容
//                log.setZcndesc(zcndesc);
//            }
//
//            // 新增日志信息
//            logService.addLog2(log,TY.getDateByT(DateEnum.DATE_ONE.getDate()));
//
//        }
//
//        // 根据异常信息抛出信息
//        throw new MyException(my.getCode(), message, my.getMethod(), null);
//    }
}
