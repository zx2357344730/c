//package com.cresign.gateway.utils.jwt;
//
//import lombok.Data;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
////JwtUtil.java
//
//@Data
//@Component
//public class JwtUtil {
////    @Value("jwt.config.key")
//    private String key = "yMxSCwTD9kUYT4VB5Z5ZOBehioUaIUoRNc";
//
////    @Value("jwt.config.ttl")
//    private long ttl = 36000;
//
//    @Autowired
//    private StringRedisTemplate redisTemplate0;
//
//
////
////    /**
////     * 生成JWT
////     *
////     * @return
////     */
////    public String createJWT(String uuid, String audience) {
////
////        long nowMillis = System.currentTimeMillis();
////        Date now = new Date(nowMillis);
////        JwtBuilder builder = Jwts.builder()
////                .setIssuedAt(now)                   // 设置发布时间
////                .signWith(SignatureAlgorithm.HS256, key)    // 设置加密方式
////                .setSubject(uuid)
////                .setAudience(audience);
////
////
//////        if (ttl > 0) {
//////            builder.setExpiration( new Date( nowMillis + ttl));
//////        }-------------------------------------------------------------------
////
//////        redisTemplate1.opsForValue().set(keyName + builder.compact(),id_U, 2, TimeUnit.HOURS);
////
////        return builder.compact();
////    }
//
////    /**
////     * 解析JWT
////     * @param jwtStr
////     * @return
////     */
////    public Claims parseJWT(String jwtStr){
////        return  Jwts.parser()
////                .setSigningKey(key)
////                .parseClaimsJws(jwtStr)
////                .getBody();
////    }
//
//    /**
//    *##description:      校验jwt是否正确
//    *@param
//    *@return
//    *@author           JackSon
//    *@updated             2020/5/15 16:45
//    */
//    public boolean validJWT(String clientType, String jwtStr) {
//
//        // 拼接key
//        String accessToken =  clientType + "Token:" + jwtStr;
//
//        // 去redis中获取key是否匹配
//
//        return redisTemplate0.hasKey(accessToken);
//
////        return aBoolean;
//
//    }
//
//}
