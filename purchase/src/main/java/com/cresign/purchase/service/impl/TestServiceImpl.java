package com.cresign.purchase.service.impl;

import com.cresign.purchase.service.TestService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @ClassName TestServiceImpl
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2022/8/17
 * @ver 1.0.0
 */
@Service
@Slf4j
public class TestServiceImpl implements TestService {

    @Resource
    private CoupaUtil coupaUtil;
    @Autowired
    private RetResult retResult;

    @Resource
    RedisTemplate<String,Object> redisTemplate;

    /**
     * 验证appId，并且返回AUN—ID
     * @param id_APP	应用编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    @Override
    public ApiResponse verificationAUN(String id_APP) {
//        JedisCluster jedis = RedisClusterUtils.getJRedis();
//        if (jedis!=null){
//            System.out.println("正常");
//            jedis.set("user","张三");
//            System.out.println("user = " + jedis.get("user"));
//        }
//        return retResult.ok(CodeEnum.OK.getCode(),"测试成功");

        redisTemplate.opsForValue().set("test","test");
        System.out.println("test = " + redisTemplate.opsForValue().get("test"));
        return retResult.ok(CodeEnum.OK.getCode(),"测试成功");

//        User user = coupaUtil.getUserByKeyAndVal("info.id_APP", id_APP, Collections.singletonList("info"));
//        if (null != user) {
//            return retResult.ok(CodeEnum.OK.getCode(),user.getInfo().getId_AUN());
//        }
//        throw new ErrorResponseException(HttpStatus.OK, "userIsNull","1");
    }

}
