package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.SmsLoginService;
import com.cresign.login.utils.LoginResult;
import com.cresign.login.utils.RegisterUserUtils;
import com.cresign.login.utils.tencentcloudapi.sms.SMSTencent;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.SMSTemplateEnum;
import com.cresign.tools.enumeration.SMSTypeEnum;
import com.cresign.tools.enumeration.manavalue.ClientEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.pojo.po.Asset;
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

import javax.annotation.Resource;
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
    private Qt qt;

    @Autowired
    private RetResult retResult;

    @Resource
    private CoupaUtil coupaUtil;

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

                        JSONObject rolex = user.getRolex();
//                        // 定义存储返回结果对象
//                        JSONObject result = new JSONObject();
                        // 定义存储异常信息集合
                        JSONArray err = new JSONArray();
                        // 遍历rolex卡片的objComp字段
                        rolex.getJSONObject("objComp").keySet().forEach(id_C -> {
//            if (id_C.equals("6076a1c7f3861e40c87fd294")) {
                            // 指定测试公司id
                            if (id_C.equals("6141b6797e8ac90760913fd0")||id_C.equals("6076a1c7f3861e40c87fd294")) {
                                // 根据公司编号获取对应ref的assetId
                                String aId = coupaUtil.getAssetId(id_C, "a-auth");
                                // 根据assetId获取asset的flowControl信息
//                    Asset asset = coupaUtil.getAssetById(aId, Collections.singletonList("flowControl"));
                                Asset asset = qt.getMDContent(aId,"flowControl",Asset.class);
                                // 定义存储异常信息，默认无异常
                                boolean isErr = true;
                                // 定义字段
                                JSONObject flowControl = null;
                                JSONArray objData = null;
                                // 判断
                                if (null == asset) {
                                    System.out.println("公司该资产为空");
                                    setErrJson(err,id_C,aId,"公司该资产为空");
                                    isErr = false;
                                } else {
                                    flowControl = asset.getFlowControl();
                                    if (null == flowControl) {
                                        setErrJson(err,id_C,aId,"公司该资产的flowControl卡片为空");
                                        isErr = false;
                                    } else {
                                        objData = flowControl.getJSONArray("objData");
                                        if (null == objData) {
                                            setErrJson(err,id_C,aId,"公司该资产的flowControl卡片内objData数据为空");
                                            isErr = false;
                                        }
                                    }
                                }
                                // 判断无异常
                                if (isErr) {
                                    // 定义存储异常信息第二个，默认异常
                                    boolean isErr2 = false;
                                    // 遍历卡片数据
                                    for (int i = 0; i < objData.size(); i++) {
                                        // 根据下标获取卡片数据
                                        JSONObject dataZ = objData.getJSONObject(i);
                                        // 获取日志用户集合
                                        JSONArray objUser = dataZ.getJSONArray("objUser");
                                        if (null == objUser) {
                                            setErrJson(err,id_C,aId,"公司该资产的flowControl卡片内objData内的objUser数据为空:"+i);
                                        } else {
                                            // 遍历用户集合
                                            for (int i1 = 0; i1 < objUser.size(); i1++) {
                                                // 根据下标获取每个用户信息
                                                JSONObject userZ = objUser.getJSONObject(i1);
                                                // 获取用户id
                                                String id_UN = userZ.getString("id_U");
                                                if (null == id_UN) {
                                                    setErrJson(err,id_C,aId,"公司该资产的flowControl卡片内objData内的objUser数据内的id_U为空"+i);
                                                } else {
                                                    // 判断用户id等于传入用户id
                                                    if (id_UN.equals(user.getId())){
                                                        // 更新appId
                                                        userZ.put("id_APP",id_APP);
                                                        objUser.set(i1,userZ);
                                                        isErr2 = true;
                                                        break;
                                                    }
                                                }
                                            }
                                            if (isErr2) {
                                                dataZ.put("objUser",objUser);
                                                objData.set(i,dataZ);
                                            }
                                        }
                                    }
                                    // 判断无异常
                                    if (isErr2) {
//                            // 写入日志flowControl
//                            flowControl.put("objData",objData);
//                            // 定义存储flowControl字典
//                            JSONObject mapKey = new JSONObject();
//                            // 设置字段数据
//                            mapKey.put("flowControl",flowControl);
                                        // 更新数据库
//                            coupaUtil.updateAssetByKeyAndListKeyVal("id",aId,mapKey);
                                        qt.setMDContent(aId,qt.setJson("flowControl.objData",objData),Asset.class);
                                    }
                                }
                            } else {
                                System.out.println("跳过公司:"+id_C);
                            }
                        });
                        if (err.size() > 0) {
                            result.put("t_type",1);
                            result.put("t_desc","内部出现错误");
                            result.put("t_errList",err);
                        } else {
                            result.put("t_type",0);
                            result.put("t_desc","操作成功!");
                        }
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

    /**
     * 写入异常信息到err集合
     * @param err   异常信息集合
     * @param id_C  公司编号
     * @param id_A  asset编号
     * @param desc  错误信息
     */
    private void setErrJson(JSONArray err,String id_C,String id_A,String desc){
        JSONObject reZ = new JSONObject();
        reZ.put("id_C",id_C);
        reZ.put("id_A",id_A);
        reZ.put("desc",desc);
        err.add(reZ);
    }

}