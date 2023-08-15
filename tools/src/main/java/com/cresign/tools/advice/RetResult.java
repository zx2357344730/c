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

//    @Autowired
//    private HttpServletRequest request;
//    /**
//     * 注入redis数据库下标1模板
//     */
//    @Resource
//    private StringRedisTemplate redisTemplate0;

    public static HttpServletRequest request;

    public static StringRedisTemplate redisTemplate0;

    @Autowired
    public void setRetResult(HttpServletRequest request,StringRedisTemplate redisTemplate0){
        RetResult.request = request;
        RetResult.redisTemplate0 = redisTemplate0;
    }

//    @Value("${encyptKey.public_key}")
//    public void setClient_Public_Key(String client_Public_Key) {
//       RetResult.client_Public_Key = client_Public_Key;
//    }
//
//    // 加密的key
//    private static String client_Public_Key;

    // 加密的key
//    private static final String client_Public_Key = RsaUtilF.getPublicKey();

//    private static final String client_Public_Key = RSAUtils.getPublicKey();

//    private static String client_Public_Key;
//    public synchronized static String getSetPublicKey(String key,boolean isG){
//        System.out.println("client_Public_Key:"+isG);
//        System.out.println(client_Public_Key);
//        if (isG) {
//            client_Public_Key = key;
//            System.out.println("set_client_Public_Key:");
//            System.out.println(client_Public_Key);
//            return null;
//        } else {
//            System.out.println("get_client_Public_Key:");
//            System.out.println(client_Public_Key);
//            return client_Public_Key;
//        }
//    }
//    public static void setClient_Public_Key(String key){
//        log.info("赋值");
//        System.out.println("赋值");
//        client_Public_Key = key;
//        System.out.println("赋值成功");
//        log.info("赋值成功");
//    }
//    public static String getClient_Public_Key(){
//        log.info("获取赋值");
//        System.out.println("获取赋值");
//        return client_Public_Key;
//    }

    @Autowired
    private LocalMessage localMessage;

//    private static String QD_Key = "\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCmZ22+NactiEwYmw06yU/Tiy1L\npenHWgbPtcjdSGIqCzZdZF9pP7X/q1XLknm6yt3lh0KQTka7MoHlK/5rauZ4Y6Jw\ny6bqXwdjRO+EmvFY/UN/fj2bczWf4XJ2WtvBZTnOzk5buk4Gp3sTkbRLpOPwvNAz\n4DrAFD1biUZF62/vyQIDAQAB\n";
//
//    static {
//        // 字符串转换
//        String s = QD_Key.replaceAll(",", "/");
//        s = s.replaceAll("%0A","\n");
//        s = s.replaceAll("%2C","/");
//        s = s.replaceAll("%2B","+");
//        QD_Key = s.replaceAll("%3D","=");
//    }

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
                return map;
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

    /**
     * 创建指定位数的随机字符串
     * @param length 表示生成字符串的长度
     * @return 字符串
     */
//    private static String getRandomString(int length) {
//        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
//        Random random = new Random();
//        StringBuffer sb = new StringBuffer();
//        for (int i = 0; i < length; i++) {
//            int number = random.nextInt(base.length());
//            sb.append(base.charAt(number));
//        }
//        return sb.toString();
//    }

    // 以后可能会用到
//    /**
//     * 用来新增日志，与抛出异常信息
//     * @param id_C  公司id
//     * @param id_U  用户id
//     * @param logType   日志类型
//     * @param zcndesc 日志描述
//     * @param data  具体数据
//     * @param my    异常信息
//     * @param message   异常具体信息
//     * @param logService    新增日志接口
//     * @return  异常结果
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
//            logService.addLog2(log,TY.getDateNow(DateEnum.DATE_ONLY.getDate()));
//
//        }
//
//        // 根据异常信息抛出信息
//        throw new MyException(my.getCode(), message, my.getMethod(), null);
//    }
}
