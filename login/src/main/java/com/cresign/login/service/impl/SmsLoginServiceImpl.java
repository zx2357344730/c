package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.SmsLoginService;
import com.cresign.login.utils.LoginResult;
import com.cresign.login.utils.RegisterUserUtils;
import com.cresign.login.utils.tencentcloudapi.sms.SMSTencent;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.SMSTemplateEnum;
import com.cresign.tools.enumeration.SMSTypeEnum;
import com.cresign.tools.enumeration.manavalue.ClientEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.User;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * 短信登录接口实现类
 * ##description: 短信登录接口实现类
 * @author JackSon
 * @updated 2020/7/31 23:02
 * @ver 1.0
 */
@Service
public class SmsLoginServiceImpl implements SmsLoginService {


    @Autowired
    private StringRedisTemplate redisTemplate1;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LoginResult loginResult;

    @Autowired
    private RegisterUserUtils registerUserUtils;


    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse getSmsLoginNum(String phone)  {

        try {
            String[] phones = {phone};

            SMSTencent.sendSMS(phones, 6, SMSTemplateEnum.LOGIN.getTemplateId(), SMSTypeEnum.LOGIN.getSmsType());

        } catch (RuntimeException  e) {

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, LoginEnum.SMS_SEND_CODE_ERROR.getCode(), null);
        }

        return retResult.ok(CodeEnum.OK.getCode(), null);

    }

    @Override
    public ApiResponse smsLogin(String phone, String smsNum, String clientType) {

        // 判断是否存在这个 key
        if (redisTemplate1.hasKey(SMSTypeEnum.LOGIN.getSmsType() + phone)) {

            // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
            if (smsNum.equals(redisTemplate1.opsForValue().get(SMSTypeEnum.LOGIN.getSmsType() + phone))) {

                Query query = new Query(new Criteria("info.mbn").is(phone));

                User user = mongoTemplate.findOne(query, User.class);

                if (ObjectUtils.isEmpty(user)) {
                    throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
LOGIN_NOTFOUND_USER.getCode(), null);
                }

                // 返回json数据给前端
                JSONObject result = loginResult.allResult(user, clientType, "sms");

                return retResult.ok(CodeEnum.OK.getCode(), result);
            } else {
                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
SMS_CODE_NOT_CORRECT.getCode(), null);

            }

        } else {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
SMS_CODE_NOT_FOUND.getCode(), null);
        }

    }

    @Override
    public ApiResponse getSmsRegisterNum(String phone) {

        try {
            String[] phones = {phone};

            SMSTencent.sendSMS(phones, 6, SMSTemplateEnum.REGISTER.getTemplateId(), SMSTypeEnum.REGISTER.getSmsType());

        } catch (RuntimeException  e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, LoginEnum.SMS_SEND_CODE_ERROR.getCode(), null);
        }

        return retResult.ok(CodeEnum.OK.getCode(), null);
     }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse smsRegister(String phone, Integer phoneType, String smsNum,
                                   String clientType, String id_APP, String pic, JSONObject wrdN) throws IOException {
        // 判断是否存在这个 key
        if (redisTemplate1.hasKey(SMSTypeEnum.REGISTER.getSmsType() + phone)) {

            // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
            if (smsNum.equals(redisTemplate1.opsForValue().get(SMSTypeEnum.REGISTER.getSmsType() + phone))) {

                Query mbnQue = new Query(new Criteria("info.mbn").is(phone));

                User user = mongoTemplate.findOne(mbnQue, User.class);

                //存在则不是注册，返回个人信息
                if (ObjectUtils.isNotEmpty(user)) {

                    // 返回json数据给前端
                    JSONObject result = loginResult.allResult(user, clientType, "sms");

                    // Check id_APP == user's id_APP?
                    // because you could be using web to register/login, so it maybe different
                    // if not, update current DB
                    if (result.getString("id_APP") == null || !result.getString("id_APP").equals(id_APP))
                    {
                        result.put("id_APP", id_APP);
                        mongoTemplate.updateFirst(mbnQue, new Update().set("info.id_APP", id_APP), User.class);
                    }

                    return retResult.ok(CodeEnum.OK.getCode(), result);
                }


                // 设置info信息
                //JSONObject infoJson = new JSONObject();
                JSONObject info = new JSONObject();
                info.put("wrdN",wrdN);
                info.put("pic",pic);
                info.put("mbn", phone);
                info.put("phoneType", phoneType);
                info.put("tmk", DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));
                info.put("tmd", DateUtils.getDateNow(DateEnum.DATE_YYYYMMMDDHHMMSS.getDate()));

                // 判断
                if (ClientEnum.APP_CLIENT.getClientType().equals(clientType)) {

                    // 判断clientID是否为空
                    if ("".equals(id_APP) || null == id_APP) {
                        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
                    }
                    info.put("id_APP", id_APP);

                }

                // 调用注册用户方法
                registerUserUtils.registerUser(info);

                redisTemplate1.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum);

                return retResult.ok(CodeEnum.OK.getCode(), null);

            } else {
                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
SMS_CODE_NOT_FOUND.getCode(), null);
            }

        } else {
            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
SMS_CODE_NOT_FOUND.getCode(), null);
        }

    }


}