package com.cresign.gateway.filter;


//import com.cresign.gateway.config.GatewayAuthConfig;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
public class GatewayFilter implements GlobalFilter, Ordered {

//    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

//    @Value("${jwt.secret.key}")
//    private String secretKey;
//
//    @Value("${auth.urls}")
//    private String[] skipAuthUrls;
//
//    @Value("${jwt.blacklist.key.format}")
//    private String jwtBlacklistKeyFormat;

    @Autowired
    private StringRedisTemplate redisTemplate0;




    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // api请求的url
        String url = exchange.getRequest().getURI().getPath();

        // 跳过不需要验证的路径
//        if(Arrays.asList(gatewayAuthConfig.getSkipUrls()).contains(url)){
//            return chain.filter(exchange);
//        }
        System.out.println("进入gateway:"+url);
//        System.out.println(JSON.toJSONString(exchange));
        if(url.startsWith("/chat/wsU/") || url.startsWith("/chat2/wsU/")
                || url.equals("/login/init/v1/getInit")
                || url.equals("/login/refreshToken/v1/refreshToken")
                || url.equals("/login/refreshToken/v1/refreshToken2")
                || url.equals("/login/refreshToken/v1/login")
                || url.equals("/login/wx/v1/getwxWebLogin")
                || url.equals("/login/linked/v1/LinkedWebLogin")
                || url.equals("/login/wx/v1/decodeUserInfo")
                || url.equals("/login/wx/v1/wxAsLogin")
                || url.equals("/login/wx/v1/wxWebLogin")
                || url.equals("/login/wx/v1/wxmp_register")
                || url.equals("/login/account/v1/logout")
                || url.equals("/login/account/v1/login") //default login
                || url.equals("/login/sms/v1/getSmsLoginNum")
                || url.equals("/login/sms/v1/smsLogin")
                || url.equals("/login/sms/v1/getSmsRegisterNum")
                || url.equals("/login/sms/v1/smsRegister")
                || url.equals("/login/wx/v1/wechatRegister")
                || url.equals("/login/facebook/v1/faceBookLogin")
                || url.equals("/login/facebook/v1/faceBookRegister")
                || url.equals("/file/cos/v1/get_objurl_token")
                || url.equals("/file/cos/v1/get_multi_token")

                || url.equals("/action/cus/v1/sendUserCusCustomer")
                || url.equals("/action/cus/v1/sendUserCusService")
                || url.equals("/action/cus/v1/cusOperate")

                || url.equals("/action/test/v1/testFill")

                || url.equals("/chat/log/v1/getIp")

//                || url.equals("/login/redirect/v1/scanLogCode")
//                || url.equals("/file/picture/v1/picUpload")
        ){
            return chain.filter(exchange);
        }

        // 从请求头中取出token
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        // 设置返回信息
        ServerHttpResponse originalResponse = exchange.getResponse();

        // 未携带token或token在黑名单内
        if (token == null ||
                token.isEmpty()) {


            originalResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
            originalResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            byte[] response = "{\"code\": \"401\",\"message\": \"401 Unauthorized.\"}"
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = originalResponse.bufferFactory().wrap(response);
            return originalResponse.writeWith(Flux.just(buffer));

        }

        String clientType = exchange.getRequest().getHeaders().getFirst("clientType");

        boolean valid_is = redisTemplate0.hasKey(clientType+"Token:"+token);

        // 校验成功
        if (valid_is) {

            ServerWebExchange mutableExchange = exchange.mutate().build();
            return chain.filter(mutableExchange);

        } else {

            // 设置返回状态
            originalResponse.setStatusCode(HttpStatus.UNAUTHORIZED);

            // 设置返回头
            originalResponse.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

            // 设置返回json信息
            byte[] response = "{\"code\":\"401\",\"message\":\"\",\"data\":\"\"}"
                    .getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = originalResponse.bufferFactory().wrap(response);
            return originalResponse.writeWith(Flux.just(buffer));

        }
    }

}
