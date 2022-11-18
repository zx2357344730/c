package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.RefreshTokenService;
import com.cresign.login.utils.LoginResult;
import com.cresign.login.utils.Oauth;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.jwt.JwtUtil;
import com.cresign.tools.pojo.po.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * ##description: 注销账户实现类
 * @author JackSon
 * @updated 2020/9/5 14:23
 * @ver 1.0
 */
@Service
@Log4j2
public class RefreshTokenServiceImpl implements RefreshTokenService {


    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LoginResult loginResult;

    @Autowired
    private Qt qt;

    @Autowired
    private Oauth oauth;


    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse refreshTokenLogin(String refreshToken, String clientType) {

        // 判断参数是否为空
        if (StringUtils.isNoneEmpty(refreshToken) || StringUtils.isNotEmpty(clientType)) {

            // 判断在redis中用户是否还存在RefreshToken
            if (qt.hasRDKey(clientType+ "RefreshToken", refreshToken))
            {
                String id_U = qt.getRDSetStr(clientType+"RefreshToken", refreshToken);

                // 通过id_U查询该用户
                Query query = new Query(new Criteria("_id").is(id_U));
                query.fields().include("info").include("rolex.objComp");
                User user = mongoTemplate.findOne(query, User.class);

                // 获取登录数据给返回给前端
                JSONObject allResult = loginResult.allResult(user, clientType, "refreshToken");

                return retResult.ok(CodeEnum.OK.getCode(), allResult);
            }

            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
REFRESHTOKEN_NOT_FOUND.getCode(), null);
        }

        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
    }

    @Override
    public ApiResponse logout(String refreshToken, String clientType) {

        // 判断参数是否为空
        if (StringUtils.isNoneEmpty(refreshToken) || StringUtils.isNotEmpty(clientType)) {

            if (qt.hasRDKey(clientType + "RefreshToken", refreshToken))
            {
                try {
                    qt.delRD(clientType + "RefreshToken", refreshToken);

                } catch(Exception e) {
                    throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGOUT_ERROR.getCode(), null);
                }
                return retResult.ok(CodeEnum.OK.getCode(), "");
            }

            return retResult.ok(CodeEnum.OK.getCode(), "");

        }
        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
    }


    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ApiResponse refreshToken(String refreshToken, String id_C, String clientType, String id_U) {

        // 判断 传过来的参数是否为空
        if (StringUtils.isNotEmpty(refreshToken)) {

//            // 从redis 中查询出该用户的 refreshToken
            String refreshTokenResult = null;
//
//            if (clientType.equals(ClientEnum.WX_CLIENT.getClientType())) {
//
//                refreshTokenResult = redisTemplate0.opsForValue().get("wxRefreshToken:" + refreshToken);
//
//            } else if (clientType.equals(ClientEnum.APP_CLIENT.getClientType())) {
//
//                refreshTokenResult = redisTemplate0.opsForValue().get("appRefreshToken:" + refreshToken);
//
//            } else {

//                refreshTokenResult = redisTemplate0.opsForValue().get("webRefreshToken:" + refreshToken);
                refreshTokenResult = qt.getRDSetStr(clientType+"RefreshToken", refreshToken);

//            }

            // 判断 refreshToken 是否为空
            if (StringUtils.isNotEmpty(refreshTokenResult)) {

                // 不为空则判断 传过来的 refreshToken 是否与 redis中的 refreshToken一致
                if (refreshTokenResult.equals(id_U)) {

                    // 通过id_U查询该用户
                    Query query = new Query(new Criteria("_id").is(id_U));
                    query.fields().include("info").include("rolex.objComp."+ id_C);
                    User user = mongoTemplate.findOne(query, User.class);

                   // System.out.println("user is"+ user);

                    String token = "";

                    token = oauth.setToken(
                            user,
                            id_C,
                            user.getRolex().getJSONObject("objComp").getJSONObject(id_C).getString("grpU"),
                            user.getRolex().getJSONObject("objComp").getJSONObject(id_C).getString("dep"),
                            clientType);

                    return retResult.ok(CodeEnum.OK.getCode(), token);

                } else {

                    throw new ErrorResponseException(HttpStatus.UNAUTHORIZED, LoginEnum.JWT_USER_VALIDATE_ERROR.getCode(), null);

                }

            }

            throw new ErrorResponseException(HttpStatus.UNAUTHORIZED, LoginEnum.JWT_USER_OVERDUE.getCode(), null);
        }

        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
    }

}