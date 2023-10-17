//package com.cresign.chat.service.fallback;
//
//import com.cresign.chat.client.LoginClient;
//import feign.hystrix.FallbackFactory;
//import org.springframework.stereotype.Service;
//
///**
// * @author tang
// * @Description 作者很懒什么也没写
// * @ClassName LoginFallbackFactory
// * @Date 2023/5/17
// * @ver 1.0.0
// */
//@Service
//public class LoginFallbackFactory implements FallbackFactory<LoginClient> {
//    @Override
//    public LoginClient create(Throwable throwable) {
//        return (id_U, id_C,refreshToken,web,token) -> null;
//    }
//}
