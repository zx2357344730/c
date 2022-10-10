package com.cresign.login.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.login.enumeration.LoginEnum;
import com.cresign.login.service.WxLoginService;
import com.cresign.login.utils.LoginResult;
import com.cresign.login.utils.RegisterUserUtils;
import com.cresign.login.utils.wxlogin.applets.WXAesCbcUtil;
import com.cresign.login.utils.wxlogin.web.WxAuthUtil;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.enumeration.SMSTypeEnum;
import com.cresign.tools.enumeration.manavalue.ClientEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import com.cresign.tools.mongo.MongoUtils;
import com.cresign.tools.pojo.es.lBUser;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.userCard.UserInfo;
import com.cresign.tools.request.HttpClientUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/7/29 9:28
 * @ver 1.0
 */
@Service
@RefreshScope
public class WxLoginServiceImpl implements WxLoginService {


    @Autowired
    private HttpServletRequest request;

    /**
     * code 凭证
     */
    public static final String CODE = "code";

    /**
     * iv 加密
     */
    public static final String IV = "iv";

    /**
     * encryptedData 加密数据
     */
    public static final String ENCRYPTED_DATA = "encryptedData";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LoginResult loginResult;

    @Autowired
    private StringRedisTemplate redisTemplate0;

    @Autowired
    private RetResult retResult;


    /**
     * 小程序唯一标识 (在微信小程序管理后台获取)
     */
    @Value("${wx.applets.appId}")
    private String wxSpAppId;

    /**
     * 小程序的 app secret (在微信小程序管理后台获取)
     */
    @Value("${wx.applets.appSecret}")
    private String wxSpSecret;

    /**
     * 授权（必填）
     */
    @Value("${wx.applets.grant_type}")
    private String grant_type;


    @Autowired
    private RegisterUserUtils registerUserUtils;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private CoupaUtil coupaUtil;

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
        User user = coupaUtil.getUserByKeyAndVal("info.id_APP", id_APP, Collections.singletonList("info"));
        if (null != user) {
            return retResult.ok(CodeEnum.OK.getCode(),user.getInfo().getId_AUN());
        }
        throw new ErrorResponseException(HttpStatus.OK, LoginEnum.LOGIN_NOTFOUND_USER.getCode(),"1");
    }

    /**
     * 微信登录方法
     * @author JackSon
     * @param code  前端传入的code凭证
     * @ver 1.0
     * @updated 2020/8/8 10:03
     * @return java.lang.String
     */
    @Override
    public ApiResponse wxWebLogin(String code) throws IOException {


        // 获取返回的code凭据,code作为换取access_token的票据，
        // 每次用户授权带上的code将不一样，code只能使用一次，5分钟未被使用自动过期。

        // 重新把code凭证拼接
        String codeUrl =
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WxAuthUtil.appId +
                        "&secret=" + WxAuthUtil.appSecret +
                        "&code=" + code +
                        "&grant_type=authorization_code";

        // 把code凭据发送后，通过code换取网页授权access_token
        JSONObject jsonObject = WxAuthUtil.doGetJson(codeUrl);



        // openid 用户唯一标识，请注意，在未关注公众号时，用户访问公众号的网页，也会产生一个用户和公众号唯一的OpenID
        String openid = jsonObject.getString("openid");


        // 网页授权接口调用凭证,注意：此access_token与基础支持的access_token不同
        // token维持时间只有两小时，具体操作重新刷新token 看这个说明
        // https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140842
        String token = jsonObject.getString("access_token");

        // 获取用户信息的 url
        String infoUrl =
                "https://api.weixin.qq.com/sns/userinfo?access_token=" + token +
                        "&openid=" + openid +
                        "&lang=zh_CN";

        // 用户信息json
        JSONObject userInfo = WxAuthUtil.doGetJson(infoUrl);

        // 如果绑定了公众号则有unionid，如果没有绑定则用openid
        String unionid = jsonObject.getString("unionid");

        //创建查询对象
        Query query = new Query(Criteria.where("info.id_WX").is(unionid));

        // 创建Auth对象存放查询后的结果
        User user = mongoTemplate.findOne(query, User.class);

        // 判断两种情况：
        // 1. 账号已经绑定了
        // 2. 账号未绑定
        if (ObjectUtils.isEmpty(user)) {

             throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
WX_NOT_BIND.getCode(),unionid);

        }

        // 已经绑定了
        JSONObject result = loginResult.allResult(user, "web", "wx");
        return retResult.ok(CodeEnum.OK.getCode(),result);
    }

    /**
     * 解密用户敏感数据
     * @author tangzejin
     * @param reqJson 前端请求参数
     * @ver 1.0
     * @updated 2020/8/8 10:03
     * @return java.lang.String
     */
    @Override
    public ApiResponse decodeUserInfo(JSONObject reqJson) {


        //获取code
        String code = reqJson.getString(CODE);

        //获取iv
        String iv = reqJson.getString(IV);

        //获取encryptedData
        String encryptedData = reqJson.getString(ENCRYPTED_DATA);



        // 登录凭证不能为空
        if (code == null || code.length() == 0) {
            throw new ErrorResponseException(HttpStatus.BAD_REQUEST, CodeEnum.BAD_REQUEST.getCode(), null);
        }


        // 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid
        //
        // 请求参数
        String params = "appid=" + wxSpAppId + "&secret=" + wxSpSecret + "&js_code=" + code + "&grant_type="
                + grant_type;

        // 发送请求
//        String sr = HttpRequest.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);

        String sr = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session?" + params, "utf-8");

        // 解析相应内容（转换成json对象）
        JSONObject json = JSONObject.parseObject(sr);

        // 获取会话密钥（session_key）
        String session_key = json.get("session_key").toString();


        // 用户的唯一标识（openid）
        String unionId = (String) json.get("unionid");


        //创建Map接口实例HashMap对象
        Map<String, Object> userInfo = new HashMap(3);
        // 2、对encryptedData加密数据进行AES解密 ////////////////
        try {

            //获取结果解密
            String result = WXAesCbcUtil.decrypt(encryptedData, session_key, iv, "UTF-8");

            //判断结果是否为空
            if (null != result && result.length() > 0) {

                //创建JSONObject对象
                JSONObject userInfoJSON = JSONObject.parseObject(result);


                userInfo.put("phoneNumber", userInfoJSON.getString("phoneNumber"));
                userInfo.put("countryCode", userInfoJSON.getString("countryCode"));
                userInfo.put("unionId", unionId);

//                redisTemplate0.opsForValue().set("wxSession_key_" + userInfoJSON.get("unionId"), session_key);

            } else {

                throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);
            }

        } catch (Exception e) {
            throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);
        }

        return retResult.ok(CodeEnum.OK.getCode(), userInfo);
    }



    @Override
    public ApiResponse wXLoginByIdWx(String id_WX, String clientType) {

        //创建查询对象
        Query query = new Query();

        //添加查询条件
        // 判断是什么端的，然后查找那个 id
        query.addCriteria(new Criteria("info.id_WX").is(id_WX));


        //进行查询并返回结果
        User user = mongoTemplate.findOne(query, User.class);

        // 判断用户是否存在
        if (user!=null){

            // 获取用户登录的数据
            JSONObject result = loginResult.allResult(user, clientType, "wx");

            return retResult.ok(CodeEnum.OK.getCode(), result);

        }
        throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
WX_NOT_BIND.getCode(), null);
    }

    @Override
    public ApiResponse appLoginByIdWx(String id_AUN, String clientType) {

        //创建查询对象
        Query query = new Query();

        //添加查询条件
        // 判断是什么端的，然后查找那个 id
        query.addCriteria(new Criteria("info.id_AUN").is(id_AUN));


        //进行查询并返回结果
        User user = mongoTemplate.findOne(query, User.class);

        // 判断用户是否存在
        if (user!=null){

            // 获取用户登录的数据
            JSONObject result = loginResult.allResult(user, clientType, "wx");

            return retResult.ok(CodeEnum.OK.getCode(), result);

        }
        throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
WX_NOT_BIND.getCode(), null);
    }




    @Override
    public ApiResponse wxRegisterUser(JSONObject reqJson) throws IOException {

        boolean register_Is = false;

        Query userQ = new Query();
        JSONObject info = reqJson.getJSONObject("info");

        Map<String, String> wrdN = new HashMap<>(1);
        wrdN.put(request.getHeader("lang"), info.getString("wcnN"));

        info.put("wrdN", wrdN);

        if ( "id_WX".equals(reqJson.get("loginType").toString())){

            userQ.addCriteria(new Criteria("info.id_WX").is(info.get("id_WX")));

            User user = mongoTemplate.findOne(userQ, User.class);

            if (null != user) {
                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
REGISTER_USER_IS_HAVE.getCode(), null);
            } else {

                register_Is = true;

            }

        } else if (reqJson.get("loginType").toString().equals("id_APP")) {

            userQ.addCriteria(new Criteria("info.id_WX").is(info.get("clientId")));

            User user = mongoTemplate.findOne(userQ, User.class);

            if (null != user) {

                throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
REGISTER_USER_IS_HAVE.getCode(), null);

            } else {

                register_Is = true;

            }

        }

        if (register_Is) {

            registerUserUtils.registerUser(info);

            return retResult.ok(CodeEnum.OK.getCode(), null);
        }

        throw new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class,noRollbackFor = ResponseException.class)
    public ApiResponse wechatRegister(String phone, Integer phoneType, String smsNum, String wcnN, String clientType, String clientID, String pic, String id_WX) throws IOException {


        // 判断是否存在这个 key
        if (redisTemplate0.hasKey(SMSTypeEnum.REGISTER.getSmsType() + phone)) {

            // 判断redis中的 smsSum 是否与前端传来的 smsNum 相同
            if (smsNum.equals(redisTemplate0.opsForValue().get(SMSTypeEnum.REGISTER.getSmsType() + phone))) {

                Query id_WXQue = new Query(new Criteria("info.id_WX").is(id_WX));

                User userWX = mongoTemplate.findOne(id_WXQue, User.class);

                if (ObjectUtils.isNotEmpty(userWX)) {

                    throw new ErrorResponseException(HttpStatus.OK, LoginEnum.
REGISTER_USER_IS_HAVE.getCode(), null);

                }

                Query mbnQue = new Query(new Criteria("info.mbn").is(phone));

                User user = mongoTemplate.findOne(mbnQue, User.class);

                // 查到有手机号但是没有id_WX 则自动绑定
                if (ObjectUtils.isNotEmpty(user)) {

                    // 设置info信息
//                    JSONObject infoJson = new JSONObject();
//                    infoJson.put("id_WX", id_WX);
//                    infoJson.put("tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//
                    Update update = new Update();
                    update.set("info.id_WX", id_WX);
                    update.set("info.tmd", DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));

                    mongoTemplate.updateFirst(mbnQue, update, User.class);

//                    redisTemplate0.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 1, TimeUnit.MINUTES);


                    return retResult.ok(CodeEnum.OK.getCode(), null);
                }


                Map<String, String> wrdN = new HashMap<>(1);
                wrdN.put(request.getHeader("lang"), wcnN);
                // 设置info信息
                JSONObject infoJson = new JSONObject();
                infoJson.put("wrdN", wrdN);
                infoJson.put("pic", pic);
                infoJson.put("mbn", phone);
                infoJson.put("id_WX", id_WX);
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

                redisTemplate0.opsForValue().set(SMSTypeEnum.LOGIN.getSmsType() + phone, smsNum, 3, TimeUnit.MINUTES);
                return retResult.ok(CodeEnum.OK.getCode(), null);
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
    @Transactional(rollbackFor = RuntimeException.class,noRollbackFor = ResponseException.class)
    public ApiResponse wxmpRegister(String nickName, String avatarUrl, String unionId, Integer countryCode, String phoneNumber) {

        JSONObject wrdNMap = new JSONObject();
        wrdNMap.put("cn", nickName);


        // 1.先通过unionId查询是否已经有用户了,如果有则直接返回出去
        Query haveUserQ = new Query(new Criteria("info.id_WX").is(unionId));
        User haveUser = mongoTemplate.findOne(haveUserQ, User.class);


        // 判断是否有该用户
        if (ObjectUtils.isNotEmpty(haveUser)) {
            return retResult.ok( CodeEnum.OK.getCode(), null);
        }

        Query haveUserP = new Query(new Criteria("info.mbn").is(phoneNumber));
        User userP = mongoTemplate.findOne(haveUserP, User.class);

        if (ObjectUtils.isNotEmpty(userP)) {
            //Phone number exist, already registered by app or website
            // in this case I only need to add my pic, nickName, union ID, into that id_U
            userP.getInfo().setId_WX(unionId);
            userP.getInfo().setPic(avatarUrl);
            userP.getInfo().setWrdN(wrdNMap);

            Update update = new Update();
            update.set("info", userP.getInfo());
            mongoTemplate.updateFirst(haveUserP, update, User.class);

            return retResult.ok( CodeEnum.OK.getCode(), loginResult.allResult(userP,request.getHeader("clientType"), "wx"));

        }

        // 2.开始注册用户到mongodb中

        // 1) info 卡片
        //Map<String, Object> userInfoMap = new HashMap<>();




        // 2) rolex 卡片
        //Map<String, Object> rolexMap = new HashMap<>();
        JSONObject rolexMap = new JSONObject();
        //List<Map<String, Object>> objCompList = new ArrayList<>();
        JSONObject objCompDefault = new JSONObject();
        //Map<String, Object> comMap = new HashMap<>();
        JSONObject comMap = new JSONObject();
        comMap.put("id_C", "5f2a2502425e1b07946f52e9");
        comMap.put("grpU", "1099");

        //List<Map<String, Object>> objModList = new ArrayList<>();
        JSONArray objModArray = new JSONArray();
        //Map<String, Object> objModMap = new HashMap<>();
        JSONObject objModMap = new JSONObject();
        objModMap.put("ref", "a-core");
        objModMap.put("bcdState", 1);
        objModMap.put("bcdLevel", 4);
        objModMap.put("tfin", "-1");

        objModArray.add(objModMap);
        comMap.put("objMod", objModArray);
        objCompDefault.put("5f2a2502425e1b07946f52e9", comMap);
        rolexMap.put("objComp", objCompDefault);

        // 3) view 卡片
        //List<String> viewList = new ArrayList<>(1);
        JSONArray viewArray = new JSONArray();
        viewArray.add("Vinfo");
        viewArray.add("rolex");

        // 添加到user对象中
        User user = new User();

        String id_U = MongoUtils.GetObjectId();
        user.setId(id_U);
        UserInfo userInfo = new UserInfo(unionId,"",wrdNMap,null, null, "5f2a2502425e1b07946f52e9","cn","CNY",
                avatarUrl,"China","",phoneNumber,countryCode);
        user.setInfo(userInfo);
        user.setView(viewArray);
        user.setRolex(rolexMap);

        mongoTemplate.insert(user);

        // 3.插入ES lbuser索引中新增列表
        JSONObject wrdNCB = new JSONObject();
        wrdNCB.put("cn", "Cresign");

        lBUser addLBUser = new lBUser(id_U,"5f2a2502425e1b07946f52e9", wrdNMap, wrdNCB, null,null, "1099", phoneNumber, "", unionId, avatarUrl);

        IndexRequest indexRequest = new IndexRequest("lbuser");
        indexRequest.source(JSON.toJSONString(addLBUser), XContentType.JSON);
        try {
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {

            throw new ErrorResponseException(HttpStatus.OK, LoginEnum.USER_NOT_IN_COMP.getCode(), null);
        }

        return retResult.ok( CodeEnum.OK.getCode(), loginResult.allResult(user,request.getHeader("clientType"), "wx"));
    }
}