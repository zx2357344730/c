package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.cresign.tools.dbTools.DbUtils;
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
import com.cresign.tools.pojo.po.userCard.UserInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.StringTokenizer;

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
    private StringRedisTemplate redisTemplate0;

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

    @Autowired
    private DbUtils dbUtils;

    @Override
    public ApiResponse getSmsLoginNum(String phone)  {

        try {
            Query mbnQue = new Query(new Criteria("info.mbn").is(phone+"_off"));
            JSONArray es = qt.getES("lNUser", qt.setESFilt("mbn", phone + "_off"));
            JSONObject userLn = null;
            if (null != es && es.size() > 0) {
                userLn = es.getJSONObject(0);
            }
            if (ObjectUtils.isNotEmpty(mongoTemplate.findOne(mbnQue, User.class))||null!=userLn) {
                JSONObject result = new JSONObject();
                result.put("t_type", 2);
                result.put("t_desc", "该账户正在注销中！！！");
                return retResult.ok(CodeEnum.OK.getCode(), result);
            }
            String[] phones = {phone};

            SMSTencent.sendSMS(phones, 6, SMSTemplateEnum.LOGIN.getTemplateId(), SMSTypeEnum.LOGIN.getSmsType());

        } catch (RuntimeException  e) {

            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, LoginEnum.SMS_SEND_CODE_ERROR.getCode(), null);
        }
        JSONObject result = new JSONObject();
        result.put("t_type", 1);
        result.put("t_desc", "操作成功");
        return retResult.ok(CodeEnum.OK.getCode(), result);

    }

    @Override
    public ApiResponse smsLogin(String phone, String smsNum, String clientType) {

        Query mbnQue = new Query(new Criteria("info.mbn").is(phone+"_off"));
        JSONArray es = qt.getES("lNUser", qt.setESFilt("mbn", phone + "_off"));
        JSONObject userLn = null;
        if (null != es && es.size() > 0) {
            userLn = es.getJSONObject(0);
        }
        if (ObjectUtils.isNotEmpty(mongoTemplate.findOne(mbnQue, User.class))||null!=userLn) {
            JSONObject result = new JSONObject();
            result.put("t_type", 2);
            result.put("t_desc", "该账户正在注销中！！！");
            return retResult.ok(CodeEnum.OK.getCode(), result);
        }
        // 判断是否存在这个 key
        if (redisTemplate0.hasKey(SMSTypeEnum.LOGIN.getSmsType() + phone)) {

            // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
            if (smsNum.equals(redisTemplate0.opsForValue().get(SMSTypeEnum.LOGIN.getSmsType() + phone))) {

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
            Query mbnQue = new Query(new Criteria("info.mbn").is(phone+"_off"));
            JSONArray es = qt.getES("lNUser", qt.setESFilt("mbn", phone + "_off"));
            JSONObject userLn = null;
            if (null != es && es.size() > 0) {
                userLn = es.getJSONObject(0);
            }
            if (ObjectUtils.isNotEmpty(mongoTemplate.findOne(mbnQue, User.class))||null!=userLn) {
                JSONObject result = new JSONObject();
                result.put("t_type", 2);
                result.put("t_desc", "该账户正在注销中！！！");
                return retResult.ok(CodeEnum.OK.getCode(), result);
            }
            String[] phones = {phone};

            SMSTencent.sendSMS(phones, 6, SMSTemplateEnum.REGISTER.getTemplateId(), SMSTypeEnum.REGISTER.getSmsType());

        } catch (RuntimeException  e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, LoginEnum.SMS_SEND_CODE_ERROR.getCode(), null);
        }
        JSONObject result = new JSONObject();
        result.put("t_type", 1);
        result.put("t_desc", "操作成功");
        return retResult.ok(CodeEnum.OK.getCode(), result);
//        return retResult.ok(CodeEnum.OK.getCode(), null);
     }

    @Override
    @Transactional(noRollbackFor = ResponseException.class)
    public ApiResponse smsRegister(String phone, Integer phoneType, String smsNum,
                                   String clientType, String id_APP, String pic, JSONObject wrdN) throws IOException {
        // 判断是否存在这个 key
            if (redisTemplate0.hasKey(SMSTypeEnum.REGISTER.getSmsType() + phone)) {

                // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
                if (smsNum.equals(redisTemplate0.opsForValue().get(SMSTypeEnum.REGISTER.getSmsType() + phone))) {

                    Query mbnQue = new Query(new Criteria("info.mbn").is(phone));

                    User user = mongoTemplate.findOne(mbnQue, User.class);
//                    JSONObject userLn = qt.getES("lNUser", qt.setESFilt("mbn", phone)).getJSONObject(0);
                    //存在则不是注册，返回个人信息
                    if (ObjectUtils.isNotEmpty(user)
//                            || null!=userLn
                    ) {

                        // 返回json数据给前端
                        JSONObject result = loginResult.allResult(user, clientType, "sms");

                        // Check id_APP == user's id_APP?
                        // because you could be using web to register/login, so it maybe different
                        // if not, update current DB
                        if (null == result.getString("id_APP") || "".equals(result.getString("id_APP"))
                                || !result.getString("id_APP").equals(id_APP)) {
                            result.put("id_APP", id_APP);
//                            mongoTemplate.updateFirst(mbnQue, new Update().set("info.id_APP", id_APP), User.class);
                            qt.setMDContent(user.getId(), qt.setJson("info.id_APP", id_APP), User.class);
                            qt.setES("lNUser", qt.setESFilt("id_U",user.getId()), qt.setJson("id_APP",id_APP));

                            JSONObject rolex = user.getRolex();

                            // 定义存储异常信息集合
                            JSONArray err = new JSONArray();
                            // 遍历rolex卡片的objComp字段
                            rolex.getJSONObject("objComp").keySet().forEach(id_C -> {
                                // 指定测试公司id
                                if (id_C.equals("6141b6797e8ac90760913fd0") || id_C.equals("6076a1c7f3861e40c87fd294")) {
                                    // 根据公司编号获取对应ref的assetId

                                    Asset asset = qt.getConfig(id_C, "a-auth", "flowControl");

                                    // 定义存储异常信息，默认无异常
                                    boolean isErr = true;
                                    // 定义字段
                                    JSONObject flowControl = null;
                                    JSONArray objData = null;
                                    // 判断
                                    if (asset.getId().equals("none")) {
                                        System.out.println("公司该资产为空");
                                        setErrJson(err, id_C, asset.getId(), "公司该资产为空");
                                        isErr = false;
                                    } else {
                                        flowControl = asset.getFlowControl();
                                        if (null == flowControl) {
                                            setErrJson(err, id_C, asset.getId(), "公司该资产的flowControl卡片为空");
                                            isErr = false;
                                        } else {
                                            objData = flowControl.getJSONArray("objData");
                                            if (null == objData) {
                                                setErrJson(err, id_C, asset.getId(), "公司该资产的flowControl卡片内objData数据为空");
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
                                                setErrJson(err, id_C, asset.getId(), "公司该资产的flowControl卡片内objData内的objUser数据为空:" + i);
                                            } else {
                                                // 遍历用户集合
                                                for (int i1 = 0; i1 < objUser.size(); i1++) {
                                                    // 根据下标获取每个用户信息
                                                    JSONObject userZ = objUser.getJSONObject(i1);
                                                    // 获取用户id
                                                    String id_UN = userZ.getString("id_U");
                                                    if (null == id_UN) {
                                                        setErrJson(err, id_C, asset.getId(), "公司该资产的flowControl卡片内objData内的objUser数据内的id_U为空" + i);
                                                    } else {
                                                        // 判断用户id等于传入用户id
                                                        if (id_UN.equals(user.getId())) {
                                                            // 更新appId
                                                            userZ.put("id_APP", id_APP);
                                                            objUser.set(i1, userZ);
                                                            isErr2 = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (isErr2) {
                                                    dataZ.put("objUser", objUser);
                                                    objData.set(i, dataZ);
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
                                            qt.setMDContent(asset.getId(), qt.setJson("flowControl.objData", objData), Asset.class);
                                        }
                                    }
                                } else {
                                    System.out.println("跳过公司:" + id_C);
                                }
                            });
                            if (err.size() > 0) {
                                result.put("t_type", 1);
                                result.put("t_desc", "内部出现错误");
                                result.put("t_errList", err);
                            } else {
                                result.put("t_type", 0);
                                result.put("t_desc", "操作成功!");
                            }
                        }

                        return retResult.ok(CodeEnum.OK.getCode(), result);
                    } else {
                        mbnQue = new Query(new Criteria("info.mbn").is(phone+"_off"));
                        JSONArray es = qt.getES("lNUser", qt.setESFilt("mbn", phone + "_off"));
                        JSONObject userLn = null;
                        if (null != es && es.size() > 0) {
                            userLn = es.getJSONObject(0);
                        }
                        if (ObjectUtils.isNotEmpty(mongoTemplate.findOne(mbnQue, User.class))||null!=userLn) {
                            JSONObject result = new JSONObject();
                            result.put("t_type", 2);
                            result.put("t_desc", "该账户正在注销中！！！");
                            return retResult.ok(CodeEnum.OK.getCode(), result);
                        }
                    }


                    // 设置info信息
                    //JSONObject infoJson = new JSONObject();
                    JSONObject info = new JSONObject();
                    info.put("wrdN", wrdN);
                    info.put("pic", pic);
                    info.put("mbn", phone);
                    info.put("phoneType", phoneType);
                    info.put("tmk", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
                    info.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

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

                    redisTemplate0.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum);

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

    @Override
    public ApiResponse logOffUser(String id_U) {
        System.out.println("进入注销输出:");
        User user = qt.getMDContent(id_U, "info", User.class);
        String[] s = user.getInfo().getMbn().split("_");
        if (user.getId().equals("5f28bf314f65cc7dc2e60262")) {
            return retResult.ok(CodeEnum.OK.getCode(), "操作失败!");
        }
        if (s.length == 1) {
            String mbn = user.getInfo().getMbn()+"_off";
            qt.setMDContent(id_U,qt.setJson("info.mbn", mbn),User.class);
            System.out.println("user:");
            System.out.println(JSON.toJSONString(user));
            JSONObject userLn = qt.getES("lNUser", qt.setESFilt("id_U", id_U)).getJSONObject(0);
            System.out.println("userLn:");
            System.out.println(JSON.toJSONString(userLn));
            qt.setES("lNUser", qt.setESFilt("id_U", id_U), qt.setJson("mbn", mbn));
            JSONObject userLb = qt.getES("lBUser", qt.setESFilt("id_U", id_U)).getJSONObject(0);
            System.out.println("userLb:");
            System.out.println(JSON.toJSONString(userLb));
            qt.setES("lBUser", qt.setESFilt("id_U", id_U), qt.setJson("mbn", mbn));
            return retResult.ok(CodeEnum.OK.getCode(), "操作成功");
        } else {
            return retResult.ok(CodeEnum.OK.getCode(), "已申请注销！！");
        }
    }

    @Override
    public ApiResponse secureLogOffUser(String mbn) {
        System.out.println("进入解除注销输出:");
        JSONArray es = qt.getES("lNUser", qt.setESFilt("mbn", mbn + "_off"));
        JSONObject userLn;
        if (null != es && es.size() > 0) {
            userLn = es.getJSONObject(0);
            User user = qt.getMDContent(userLn.getString("id_U"), "info", User.class);
//        StringTokenizer st = new StringTokenizer(user.getInfo().getMbn(),"_");
            String[] st = user.getInfo().getMbn().split("_");
            if (st.length == 1) {
                return retResult.ok(CodeEnum.OK.getCode(), "2");
            }
            if (st[1].equals("off")) {
                qt.setMDContent(userLn.getString("id_U"),qt.setJson("info.mbn", mbn),User.class);
                qt.setES("lNUser", qt.setESFilt("id_U", userLn.getString("id_U")), qt.setJson("mbn", mbn));
                qt.setES("lBUser", qt.setESFilt("id_U", userLn.getString("id_U")), qt.setJson("mbn", mbn));
                return retResult.ok(CodeEnum.OK.getCode(), "1");
            } else {
                return retResult.ok(CodeEnum.OK.getCode(), "3");
            }
        } else {
            return retResult.ok(CodeEnum.OK.getCode(), "3");
        }
    }

    String hz = "Test";
    String zj = "tangTang";
    String kara = "test_kara";
    String ld = "LD";
    String kevin = "admin_K";
    @Override
    public ApiResponse setTestUser(String name,String type) {
        JSONObject result = new JSONObject();
        String id_U = null;
        String[] re = new String[0];
        String[] reKey = new String[]{"info.mbn","info.id_WX","info.id_APP","info.id_AUN"};
        String[] reKeyEs = new String[]{"mbn","id_WX","id_APP","id_AUN"};
        boolean isSet = true;
        delFan(name);
        if(kara.equals(name)){
            id_U = "62318c9a890df37b8079952d";
            re = getInfo(id_U,type);
        } else if (zj.equals(name)) {
            id_U = "6256789ae1908c03460f906f";
            re = getInfo(id_U,type);
        } else if (ld.equals(name)) {
            id_U = "6229913cf890c1140c720b71";
            re = getInfo(id_U,type);
        } else if (kevin.equals(name)) {
//            isSet = false;
            id_U = "5f28bf314f65cc7dc2e60386";
            re = getInfo(id_U,type);
        } else {
            isSet = false;
            result.put("t_type", 3);
            result.put("t_desc", "不识别");
        }
        if (isSet) {
            if (re.length == 0) {
                result.put("t_type", 2);
                result.put("t_desc", name+" --- 操作失败!已经是:"+type+",状态！");
            } else {
                JSONObject setObj = new JSONObject();
                JSONObject setObjEs = new JSONObject();
                for (int i = 0; i < re.length; i++) {
                    if (!re[i].equals("null")) {
                        setObj.put(reKey[i],re[i]);
                        setObjEs.put(reKeyEs[i],re[i]);
                    }
                }
//                qt.setMDContent(id_U,qt.setJson("info.mbn", re[0],"info.id_WX",re[1],"info.id_APP"
//                        ,re[2],"info.id_AUN",re[3]),User.class);
                qt.setMDContent(id_U,setObj,User.class);
//                qt.setES("lNUser", qt.setESFilt("id_U", id_U), qt.setJson("mbn", re[0]
//                        ,"id_WX",re[1],"id_APP",re[2],"id_AUN",re[3]));
                qt.setES("lNUser", qt.setESFilt("id_U", id_U), setObjEs);
//                qt.setES("lBUser", qt.setESFilt("id_U", id_U), qt.setJson("mbn", re[0],"id_WX",re[1],"id_APP",re[2],"id_AUN",re[3]));
                result.put("t_type", 1);
                result.put("t_desc", name+" --- 操作成功!");
            }
        }
        return retResult.ok(CodeEnum.OK.getCode(), result);
    }

    @Override
    public ApiResponse delTestUser(String name) {
        return retResult.ok(CodeEnum.OK.getCode(), delFan(name));
    }

    private JSONObject delFan(String name){
        JSONObject result = new JSONObject();
        if (kara.equals(name)) {
            String mbn = "+8618682131169";
            Query mbnQue = new Query(new Criteria("info.mbn").is(mbn));
            User user = mongoTemplate.findOne(mbnQue, User.class);
            if (null != user && !"62318c9a890df37b8079952d".equals(user.getId())) {
//                qt.delMD(user.getId(),User.class);
//                JSONObject userLn = qt.getES("lNUser", qt.setESFilt("id_U", user.getId())).getJSONObject(0);
//                JSONObject userLb = qt.getES("lBUser", qt.setESFilt("id_U", user.getId())).getJSONObject(0);
//                qt.delES("lNUser",userLn.getString("id_ES"));
//                qt.delES("lBUser",userLb.getString("id_ES"));
                delFanCore(user.getId());
                result.put("t_type", 1);
                result.put("t_desc", name+" --- 操作成功!");
            } else {
                result.put("t_type", 2);
                result.put("t_desc", name+" --- 操作失败!");
            }
        } else if (ld.equals(name)) {
            String mbn = "+8618200806197";
            Query mbnQue = new Query(new Criteria("info.mbn").is(mbn));
            User user = mongoTemplate.findOne(mbnQue, User.class);
            if (null != user && !"6229913cf890c1140c720b71".equals(user.getId())) {
//                qt.delMD(user.getId(),User.class);
//                JSONObject userLn = qt.getES("lNUser", qt.setESFilt("id_U", user.getId())).getJSONObject(0);
//                JSONObject userLb = qt.getES("lBUser", qt.setESFilt("id_U", user.getId())).getJSONObject(0);
//                qt.delES("lNUser",userLn.getString("id_ES"));
//                qt.delES("lBUser",userLb.getString("id_ES"));
                delFanCore(user.getId());
                result.put("t_type", 1);
                result.put("t_desc", name+" --- 操作成功!");
            } else {
                result.put("t_type", 2);
                result.put("t_desc", name+" --- 操作失败!");
            }
        } else if (zj.equals(name)) {
            String mbn = "+8619906364962";
            Query mbnQue = new Query(new Criteria("info.mbn").is(mbn));
            User user = mongoTemplate.findOne(mbnQue, User.class);
            if (null != user && !"6256789ae1908c03460f906f".equals(user.getId())) {
                delFanCore(user.getId());
                result.put("t_type", 1);
                result.put("t_desc", name+" --- 操作成功!");
            } else {
                result.put("t_type", 2);
                result.put("t_desc", name+" --- 操作失败!");
            }
        } else if (kevin.equals(name)) {
            String mbn = "+8613929900723";
            Query mbnQue = new Query(new Criteria("info.mbn").is(mbn));
            User user = mongoTemplate.findOne(mbnQue, User.class);
            if (null != user && !"5f28bf314f65cc7dc2e60386".equals(user.getId())) {
                delFanCore(user.getId());
                result.put("t_type", 1);
                result.put("t_desc", name+" --- 操作成功!");
            } else {
                result.put("t_type", 2);
                result.put("t_desc", name+" --- 操作失败!");
            }
        } else {
            result.put("t_type", 3);
            result.put("t_desc", name+" --- 操作失败!");
        }
        return result;
    }

    private void delFanCore(String id_U){
        qt.delMD(id_U,User.class);
        JSONArray esLn = qt.getES("lNUser", qt.setESFilt("id_U", id_U));
        if (null != esLn && esLn.size() > 0) {
            JSONObject userLn = qt.getES("lNUser", qt.setESFilt("id_U", id_U)).getJSONObject(0);
            qt.delES("lNUser",userLn.getString("id_ES"));
        }
        JSONArray esLb = qt.getES("lBUser", qt.setESFilt("id_U", id_U));
        if (null != esLb && esLb.size() > 0) {
            JSONObject userLb = qt.getES("lBUser", qt.setESFilt("id_U", id_U)).getJSONObject(0);
            qt.delES("lBUser",userLb.getString("id_ES"));
        }
    }

    private String[] getInfo(String id_U,String type){
        User user = qt.getMDContent(id_U, "info", User.class);
        UserInfo info = user.getInfo();
        String[] s = info.getMbn().split("_");
        String[] re;
        if ("test".equals(type)&&s.length==1) {
            re = new String[4];
            re[0] = (info.getMbn()+"_"+hz);
            re[1] = info.getId_WX()==null?"null":(info.getId_WX()+"_"+hz);
            re[2] = info.getId_APP()==null?"null":(info.getId_APP()+"_"+hz);
            re[3] = info.getId_AUN()==null?"null":(info.getId_AUN()+"_"+hz);
        } else if ("ok".equals(type) && s.length > 1&&s[1].equals(hz)
//                && s.length > 1 && s[1].equals(hz)
        ) {
            re = new String[4];
            re[0] = info.getMbn().split("_")[0];
            if (null != info.getId_WX()) {
                String[] wxS = info.getId_WX().split("_");
                re[1] = "";
                for (int i = 0; i < wxS.length-1; i++) {
                    re[1] += wxS[i];
                    System.out.println(re[1]);
                    if (i != wxS.length - 2) {
                        re[1] += "_";
                        System.out.println(re[1]);
                    }
                }
            } else {
                re[1] = "null";
            }
            re[2] = info.getId_APP()==null?"null":(info.getId_APP().split("_")[0]);
            re[3] = info.getId_AUN()==null?"null":(info.getId_AUN().split("_")[0]);
        } else {
            re = new String[0];
        }
        return re;
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