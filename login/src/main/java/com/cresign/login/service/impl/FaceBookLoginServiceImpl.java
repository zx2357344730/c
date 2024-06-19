package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.service.FaceBookLoginService;
import com.cresign.login.utils.RegisterUserUtils;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.ErrEnum;
import com.cresign.tools.enumeration.SMSTypeEnum;
import com.cresign.tools.enumeration.manavalue.ClientEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/9/22 14:08
 * @ver 1.0
 */
@Service
public class FaceBookLoginServiceImpl implements FaceBookLoginService {

//    @Autowired
//    private MongoTemplate mongoTemplate;

//    @Autowired
//    private StringRedisTemplate redisTemplate0;

    @Autowired
    private RegisterUserUtils registerUserUtils;

    @Autowired
    private RetResult retResult;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private Qt qt;

    @Override
    public ApiResponse faceBookLogin(String id_fb, String clientType) {

//        Query query = new Query(new Criteria("info.id_fb").is(id_fb));
//        //进行查询并返回结果
//        User user = mongoTemplate.findOne(query, User.class);
        JSONArray es = qt.getES("lNUser", qt.setESFilt("id_fb", id_fb));
        if (null != es && es.size() > 0) {
//            //判断公司id是否为空
//            if (user.getLSComp() != null){
//
//                // 获取用户登录的数据
//                JSONObject result = loginResult.allResult(user, clientType, "facebook");
//
//                // 重新设值用户的默认登录的公司信息到redis中
////                oauth.setCompMenuAndRole(user.getInfo().get("def_C").toString());
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            }
        }
//        // 判断用户是否存在
//        if (user!=null){
//
////            //判断公司id是否为空
////            if (user.getLSComp() != null){
////
////                // 获取用户登录的数据
////                JSONObject result = loginResult.allResult(user, clientType, "facebook");
////
////                // 重新设值用户的默认登录的公司信息到redis中
//////                oauth.setCompMenuAndRole(user.getInfo().get("def_C").toString());
////                return retResult.ok(CodeEnum.OK.getCode(), result);
////            }
//
//
//        }
        throw new ErrorResponseException(HttpStatus.OK, ErrEnum.WX_NOT_BIND.getCode(), null);
      }

    @Override
    public ApiResponse faceBookRegister(String id_fb, String wcnN, String email, String pic, String phone, String phoneType, String smsNum, String clientID, String clientType) throws IOException {
        // 判断是否存在这个 key
        if (qt.hasRDKey(SMSTypeEnum.REGISTER.getSmsType(),phone)) {

            // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
            if (smsNum.equals(qt.getRDSetStr(SMSTypeEnum.REGISTER.getSmsType(), phone))) {

//                Query id_WXQue = new Query(new Criteria("info.id_fb").is(id_fb));
//
//                User userFb = mongoTemplate.findOne(id_WXQue, User.class);
                JSONArray es = qt.getES("lNUser", qt.setESFilt("id_fb", id_fb));

//                if (ObjectUtils.isNotEmpty(userFb)) {
//
//                    throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
//REGISTER_USER_IS_HAVE.getCode(), null);
//                }
                if (null == es || es.size() == 0) {

                    throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
                            REGISTER_USER_IS_HAVE.getCode(), null);
                }

//                Query mbnQue = new Query(new Criteria("info.mbn").is(phone));
//
//                User user = mongoTemplate.findOne(mbnQue, User.class);
                JSONArray esMbn = qt.getES("lNUser", qt.setESFilt("mbn", "exact", phone));

//                if (ObjectUtils.isNotEmpty(user)) {
//
//                    Update update = new Update();
//                    update.set("info.id_fb", id_fb);
//                    update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//
//                    mongoTemplate.updateFirst(mbnQue, update, User.class);
//
//                    redisTemplate0.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 3, TimeUnit.MINUTES);
//
//                    return retResult.ok(CodeEnum.OK.getCode(), null);
//                }
                if (null == esMbn || esMbn.size() == 0) {

//                    Update update = new Update();
//                    update.set("info.id_fb", id_fb);
//                    update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//
//                    mongoTemplate.updateFirst(mbnQue, update, User.class);
                    qt.setES("lNUser",es.getJSONObject(0).getString("id_ES")
                            ,qt.setJson("info.id_fb",id_fb,"info.tmd"
                                    ,DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate())));
                    qt.setMDContent(es.getJSONObject(0).getString("id_U")
                            ,qt.setJson("info.id_fb",id_fb,"info.tmd"
                                    ,DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate())), User.class);

//                    redisTemplate0.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 3, TimeUnit.MINUTES);
                    qt.setRDSet(SMSTypeEnum.LOGIN.getSmsType(), phone, smsNum, 180L);

                    return retResult.ok(CodeEnum.OK.getCode(), null);
                }

                Map<String, String> wrdN = new HashMap<>(1);
                wrdN.put(request.getHeader("lang"), wcnN);
                // 设置info信息
                JSONObject infoJson = new JSONObject();
                infoJson.put("wrdN", wrdN);
                infoJson.put("pic", pic);
                infoJson.put("id_fb", id_fb);
                infoJson.put("email", email);
                infoJson.put("mbn", phone);
                infoJson.put("phoneType", phoneType);
                infoJson.put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                infoJson.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

                // 判断
                if (ClientEnum.APP_CLIENT.getClientType().equals(clientType)) {

                    // 判断clientID是否为空
                    if ("".equals(clientID) || null == clientID) {
                        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
                    }
                    infoJson.put("clientID", clientID);

                }

                // 调用注册用户方法
                registerUserUtils.registerUser(infoJson);

//                redisTemplate0.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 3, TimeUnit.MINUTES);
                qt.setRDSet(SMSTypeEnum.LOGIN.getSmsType(), phone, smsNum, 180L);

                return retResult.ok(CodeEnum.OK.getCode(), null);

            } else {

                throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
SMS_CODE_NOT_CORRECT.getCode(), null);
            }

        } else {

            throw new ErrorResponseException(HttpStatus.OK, ErrEnum.
SMS_CODE_NOT_FOUND.getCode(), null);
        }
    }
}