package com.cresign.login.utils.tencentcloudapi.sms;

import com.cresign.tools.advice.RetResult;
import com.cresign.tools.enumeration.CodeEnum;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * ##Author: JackSon
 * ##version: 1.0
 * ##description: 腾讯云短信工具类
 * ##updated: 2020-03-16 14:02
 */
@Component
public class SMSTencent {


    /**
     * secretId
     */
    private static String SECRETID;



    /**
     * secretKey
     */

    private static String SECRETKey;

    /**
     * appID
     */

    private static String APPID;

    @Value("${tencent.sms.secretId}")
    public  void setSECRETID(String SECRETID) {
        SMSTencent.SECRETID = SECRETID;
    }

    @Value("${tencent.sms.secretKey}")
    public  void setSECRETKey(String SECRETKey) {
        SMSTencent.SECRETKey = SECRETKey;
    }

    @Value("${tencent.sms.appId}")
    public  void setAPPID(String APPID) {
        SMSTencent.APPID = APPID;
    }

    @Autowired
    public void setRedisTemplate1(StringRedisTemplate redisTemplate1) {
        SMSTencent.redisTemplate1 = redisTemplate1;
    }

    private static StringRedisTemplate redisTemplate1;



    /**
     *##description:
     *##Params:            phones : 发送的手机号码 ,templateParam : 模版参数，从前往后对应的是模版的{1}、{2}等,见《创建短信签名和模版》小节
     *##Return:
     *##author:           JackSon
     *##updated:             2020/3/16 14:36
     */
    public static String sendSMS(String [] phones, int smsCodeSize, String templateId, String smsType)  {


        Random random = new Random();

        String smsNum = "";

        for (int i = 0; i < smsCodeSize; i++) {
            smsNum += random.nextInt(10);
        }

        String[] smsNums = {smsNum};

        // 实例化要请求产品(以cvm为例)的client对象
        Credential cred = new Credential(SECRETID, SECRETKey);
        ClientProfile clientProfile = new ClientProfile();

        clientProfile.setSignMethod(ClientProfile.SIGN_TC3_256);
        //第二个ap-chongqing 填产品所在的区
        SmsClient smsClient = new SmsClient(cred, "ap-guangzhou");
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        //appId ,见《创建应用》小节
        sendSmsRequest.setSmsSdkAppid(APPID);

        sendSmsRequest.setPhoneNumberSet(phones);
        //模版id,见《创建短信签名和模版》小节
        sendSmsRequest.setTemplateID(templateId);

        sendSmsRequest.setTemplateParamSet(smsNums);
        //签名内容，不是填签名id,见《创建短信签名和模版》小节
        sendSmsRequest.setSign("佛山创设贸易有限公司");

        try {
            //发送短信
            smsClient.SendSms(sendSmsRequest);

            for (int i = 0; i < phones.length; i++) {
                //原1分钟，先改为5
                redisTemplate1.opsForValue().set(smsType + phones[i], smsNum, 5, TimeUnit.MINUTES);
            }

        } catch (TencentCloudSDKException e) {
            return RetResult.errorJsonResult(HttpStatus.INTERNAL_SERVER_ERROR, CodeEnum.INTERNAL_SERVER_ERROR.getCode(), null);
        }

        return null;

    }

}