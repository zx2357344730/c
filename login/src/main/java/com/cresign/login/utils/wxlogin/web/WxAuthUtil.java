package com.cresign.login.utils.wxlogin.web;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class WxAuthUtil {



    @Value("${wx.web.appId}")
    public void setAppId(String appId) {
        WxAuthUtil.appId = appId;
    }

    @Value("${wx.web.appSecret}")
    public void setAppSecret(String appSecret) {
        WxAuthUtil.appSecret = appSecret;
    }

    /**
     * 微信开发者平台的 APPID
     */
    public static String appId;

    /**
     * 微信开发者平台的 APPSECRET
     */

    public static String appSecret;

    /**
     * 该方法用来请求微信url，并且返回JSON
     * @param url
     * @author JackSon
     * @updated 2020/7/29 9:43
     * @return com.alibaba.fastjson.JSONObject
     */
    public static JSONObject doGetJson(String url) {

        try {
            // 声明json对象
            JSONObject jsonObject = null;

            // 声明client连接对象
//        DefaultHttpClient client = new DefaultHttpClient();

            // 什么get请求方式
            HttpGet httpGet = new HttpGet(url);

            HttpClient client = HttpClientBuilder.create().build();

            // 声明response返回
            HttpResponse response = client.execute(httpGet);

            // 获取返回内容
            HttpEntity entity = response.getEntity();

            // 判断是否为空，是 则返回NULL，否返回JSON对象
            if (entity != null) {

                // 编码格式utf-8
                String result = EntityUtils.toString(entity, "UTF-8");

                // 获取其返回内容并且转换为json对象
                jsonObject = JSONObject.parseObject(result);

            }

            // 释放连接
            httpGet.releaseConnection();

            return jsonObject;

        }
        catch (Exception e)
        {
            return null;

        }
    }

}