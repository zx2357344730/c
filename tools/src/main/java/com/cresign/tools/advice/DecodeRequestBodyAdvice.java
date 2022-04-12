package com.cresign.tools.advice;


import com.cresign.tools.annotation.SecurityParameter;
import com.cresign.tools.encrypt.AesEncryptUtils;
import com.cresign.tools.encrypt.RSAUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * ##Author: jackson
 * @desc 请求数据解密
 */
@Slf4j
@ControllerAdvice
public class DecodeRequestBodyAdvice implements RequestBodyAdvice {

//    private static final Logger logger = LoggerFactory.getLogger(DecodeRequestBodyAdvice.class);


    @Value("${encyptKey.private_key}")
    private String server_private_key;

//    @Value("${aes.private.key}")
//    private String AES_PRIVATE_KEY;

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
//                    log.info("注解SecurityParameter,对方法method :【" + methodParameter.getMethod().getName() + "】返回数据进行解密");
                    return new MyHttpInputMessage(inputMessage);
                }
            }
                return inputMessage;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("对方法method :【" + methodParameter.getMethod().getName() + "】返回数据进行解密出现异常："+e.getMessage());
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
        public InputStream getBody() throws IOException {
            return body;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        /**
         *
         * ##Params: requestData
         * ##return:
         */
        public String easpString(String requestData) {

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
                    String content = null ;
                    String aseKey = null;
                    try {
                        aseKey = RSAUtils.decryptDataOnJava(encrypted,server_private_key);
                    }catch (Exception e){
                        throw  new RuntimeException("参数【aseKey】解析异常！");
                    }
                    try {
                        content  = AesEncryptUtils.decrypt(data, aseKey);
                    }catch (Exception e){
                        throw  new RuntimeException("参数【content】解析异常！");
                    }
                    if (StringUtils.isEmpty(content) || StringUtils.isEmpty(aseKey)){
                        throw  new RuntimeException("参数【requestData】解析参数空指针异常!");
                    }
                    return content;
                }
            }
            throw new RuntimeException("参数【requestData】不合法异常！");
        }
    }
}