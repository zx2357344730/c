package com.cresign.tools.jwt;

import com.cresign.tools.dbTools.Qt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

//JwtUtil.java

@Data
@Component
public class JwtUtil {

//    @Value("jwt.config.key")
    private String key = "yMxSCwTD9kUYT4VB5Z5ZOBehioUaIUoRNc";

//    @Value("jwt.config.ttl")
    private long ttl = 36000;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private Qt qt;


    /**
     * 生成JWT
     *
     * @return
     */
    public String createJWT(String uuid, String audience) {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)                   // 设置发布时间
                .signWith(SignatureAlgorithm.HS256, key)    // 设置加密方式
                .setSubject(uuid)
                .setAudience(audience);


//        if (ttl > 0) {
//            builder.setExpiration( new Date( nowMillis + ttl));
//        }-------------------------------------------------------------------

//        redisTemplate0.opsForValue().set(keyName + builder.compact(),id_U, 2, TimeUnit.HOURS);

        return builder.compact();
    }
//
//    /**
//     * 解析JWT
//     * @param jwtStr
//     * @return
//     */
//    public Claims parseJWT(String jwtStr){
//        return  Jwts.parser()
//                .setSigningKey(key)
//                .parseClaimsJws(jwtStr)
//                .getBody();
//    }
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
//            return qt.hasRDKey(clientType + "Token", jwtStr);
//
//    }

}
