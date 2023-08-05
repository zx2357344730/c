package com.cresign.tools.advice;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.annotation.SecurityParameter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

/**
 * @author jackson
 * @desc 请求数据解密
 */
@ControllerAdvice
public class DecodeRequestBodyAdvice implements RequestBodyAdvice {

//    private static final Logger logger = LoggerFactory.getLogger(DecodeRequestBodyAdvice.class);

    /**
     * 注入redis数据库下标1模板
     */
    @Resource
    private StringRedisTemplate redisTemplate0;

    public static final String RED_KEY = "key:k_";

//    @Value("${encyptKey.private_key}")
//    private String server_private_key;

//    private static String server_private_key = "RsaUtilF.getPrivateKeyX()";

//    @Value("${aes.private.key}")
//    private String AES_PRIVATE_KEY;

    private static String private_key = "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAJUXjT0F2nUpEsE//pQ+N49MUR3xsVrLN9GjqEevFRGWQtK9PYwxV44Pwn0Mxty38OmO7i5YhQFkLvmbARFEP6rwb2vi6ZFQ+iQN8vb1ZIbENmevXeLw2gjuYmX8P7ld54dq5b36ViEz3wBO0sk4RniEoW5UFCbmsc043cv10/PvAgMBAAECgYEAkEH9kzH6sqpPT1VQSrf4olrBkiut46AGHn4v8UxjImU1uxsIVoHXqclt8flO4XnJTPPTWlykNThui22DluVmg9Ayv/32aAWCuJbRVT2LczTUWjX4CCWXsxGlOAgNUtU0ie2H/XflW/zJLBj5l6QEavmCUtGRr+xHqbAaolp71wECQQDU4L1ZKvZLVbZXRBT7l44mzYHzsELFg3vWjn2QlJz3EB9rDfwJse3P1ijP/WEP5BouEvZlS0xsHkqUTeLLdVKhAkEAs0sNOqwn9x0lL2JnYcb1/2p72uZtU+Mi2PooFqRNA6UCvaSWY2mdbauhPpKM1yAdp/iopx25JAGqoIo3duNMjwJBAMGjhNl9aPhyCSEsPuH0pEvLmC/w32wHBDjQ+IrxhC6ArfOVjvPKtAXgStOXKhloZiAPA650ZhnbG//3MRvdpsECQQCOkukDNjFVtayDQLo7K68lG/U/vitEIQPuDQdh8ed4NXi3e7FHfo38zxWbH4i17UkH8JmUwvMd6eFYZnDyro+LAkEAyG5UQMupIIiZolYESFb2xIsQiqwC/mWQljr4Sjd01RIKtTjtJS2v5v8MRQLHyBTGtOcBz/nsPiKYrvY31MNFXw==";

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) throws IOException {

        try {
            if (methodParameter.getMethod().isAnnotationPresent(SecurityParameter.class)) {
                //获取注解配置的包含和去除字段
                SecurityParameter serializedField = methodParameter.getMethodAnnotation(SecurityParameter.class);
                //入参是否需要解密
                if(serializedField.inDecode()){
//                    System.out.println("inputMessage:");
//                    System.out.println(JSON.toJSONString(inputMessage));
//                    log.info("注解SecurityParameter,对方法method :【" + methodParameter.getMethod().getName() + "】返回数据进行解密");
                    return new MyHttpInputMessage(inputMessage);
                }
            }
                return inputMessage;
        } catch (Exception e) {
            e.printStackTrace();
//            log.error("对方法method :【" + methodParameter.getMethod().getName() + "】返回数据进行解密出现异常："+e.getMessage());
            return inputMessage;
        }

    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage httpInputMessage, MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return body;
    }

    class MyHttpInputMessage implements HttpInputMessage {
        private HttpHeaders headers;

        private InputStream body;

        public MyHttpInputMessage(HttpInputMessage inputMessage) throws Exception {
            this.headers = inputMessage.getHeaders();

            this.body = IOUtils.toInputStream(easpString(IOUtils.toString(inputMessage.getBody(),"utf-8")));
        }

        @Override
        public InputStream getBody() {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        /**
         *
         * @param requestData
         * @return
         */
        public String easpString(String requestData) {

//            System.out.println("easpString:");
//            System.out.println(requestData);

//            System.out.println("headers:");
            String uuId = Objects.requireNonNull(this.getHeaders().get("uuId")).get(0);
//            String isDecrypt = Objects.requireNonNull(this.getHeaders().get("isDecrypt")).get(0);
//            System.out.println("uuId:");
//            System.out.println(uuId);

//            JSONObject tokData = getUserToken.getTokenData(request.getHeader("authorization"), request.getHeader("clientType"));
//            System.out.println("tokData:");
//            System.out.println(tokData);

            //String server_private_key = (String) ServiceApp.ac.getBean(StringRedisTemplate.class).opsForHash().get("serverEncyptKey", "private_key");
//            String server_private_key = (String) redisTemplate0.opsForHash().get("serverEncyptKey", "private_key");

            if(requestData != null && !requestData.equals("")){
                Map<String,String> requestMap = new Gson().fromJson(requestData,new TypeToken<Map<String,String>>() {
                }.getType());
                // 密文
                String data = requestMap.get("requestData");
                // 加密的ase秘钥
                String encrypted = requestMap.get("encrypted");
                if(StringUtils.isEmpty(data) || StringUtils.isEmpty(encrypted)){
                    throw new RuntimeException("参数【requestData】缺失异常！");
                }else{
                    System.out.println(JSON.toJSONString(requestMap));
                    String content ;
                    String aesKey;
                    System.out.println("输出key:");
                    System.out.println(RED_KEY+uuId);
                    JSONObject re = JSONObject.parseObject(redisTemplate0.opsForValue().get(RED_KEY+uuId));
                    if (null == re) {
                        throw  new RuntimeException("id对应秘钥为空!");
                    }

//                    System.out.println("***********************************" + encodedData.length);
//                    String encodeData = bytesToString(encodedData);
//                    System.out.println("加密后文字：\r\n" + encodeData);
//                    byte[] dataq = stringToBytes(encodeData));
//                    System.out.println("***********************************" + dataq.length);
//                    byte[] decodedData = RSAUtil.decryptByPrivateKey(encodedData, privateKey);
//                    String target = new String(decodedData);
//                    System.out.println("解密后文字: \r\n" + target);
//                    System.out.println(JSON.toJSONString(re));

                    String server_private_key;
//                    if (isDecrypt.equals("false")) {
//                        server_private_key = private_key;
//                    } else {
                        server_private_key = re.getString("privateKey");
//                    }

//                    try {
////                        aseKey = RSAUtils.decryptDataOnJava(encrypted,server_private_key);
//                        System.out.println("私钥:");
//                        System.out.println(server_private_key);
//                        byte[] plaintext = RsaUtilF.decryptByPrivateKey(Base64.decodeBase64(encrypted)
//                                , server_private_key);
//                        aesKey = new String(plaintext);
//                    }catch (Exception e){
//                        throw  new RuntimeException("参数【aseKey】解析异常！");
//                    }

//                    System.out.println("私钥:");
//                    System.out.println(server_private_key);
//                    System.out.println("公钥:");
//                    System.out.println(re.getString("publicKey"));
//                    System.out.println("encryped");
//                    System.out.println(Base64.decodeBase64(encrypted));
                    byte[] plaintext ;
                    try {

//                        plaintext = RSAUtils.decryptByPrivateKey(Base64.decodeBase64(encrypted), server_private_key);

                        plaintext = RsaUtilF.decryptByPrivateKey(Base64.decodeBase64(encrypted), server_private_key);
//                        System.out.println("这里成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw  new RuntimeException("参数【aseKey】解析异常！");
                    }
//                    System.out.println("pla:");
//                    System.out.println(plaintext);
                    aesKey = new String(plaintext);
                    aesKey = aesKey.replace("\"", "");
//                    aesKey = Arrays.toString(plaintext);
//                    System.out.println("aes:");
//                    System.out.println(aesKey);
//                    System.out.println(aesKey.getBytes().length);

//                    try {
////                        content  = AesEncryptUtils.decrypt(data, aesKey);
////                        content  = AesEncryptUtils.decrypt(data, aesKey);
//                        //AES解密得到明文data数据
//                        content = AesUtil.decrypt(data, aesKey);
//                    }catch (Exception e){
//                        throw  new RuntimeException("参数【content】解析异常！");
//                    }

                    //AES解密得到明文data数据
                    try {
                        content = AesUtil.decrypt(data, aesKey);
                    } catch (Exception e) {
//                        e.printStackTrace();
                        throw  new RuntimeException("参数【content】解析异常！");
                    }

                    if (StringUtils.isEmpty(content) || StringUtils.isEmpty(aesKey)){
                        throw  new RuntimeException("参数【requestData】解析参数空指针异常!");
                    }
//                    System.out.println(content);
                    return content;
                }
            }
            throw new RuntimeException("参数【requestData】不合法异常！");
        }
    }
}