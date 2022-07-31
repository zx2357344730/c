package com.cresign.gateway.filter;


import com.cresign.gateway.config.GatewayAuthConfig;
import com.cresign.gateway.utils.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
    private JwtUtil jwtUtil;

    @Autowired
    private GatewayAuthConfig gatewayAuthConfig;


    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // api请求的url
        String url = exchange.getRequest().getURI().getPath();

        // 跳过不需要验证的路径
        if(Arrays.asList(gatewayAuthConfig.getSkipUrls()).contains(url)){
            return chain.filter(exchange);
        }
//        else if(url.startsWith("/chat/pay/")||url.startsWith("/chat/wsU/")||url.startsWith("/chat/login/")||url.startsWith("/chat/pi/")){
        else if(url.startsWith("/chat/wsU/")){

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

        // 校验token是否正确
        boolean valid_is = jwtUtil.validJWT(clientType, token);

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
