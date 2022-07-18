package com.cresign.purchase.utils;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * ##ClassName: HttpClientUtils
 * ##description: HttpClient工具类
 * ##Author: tang
 * ##Updated: 2020/6/19 19:14
 * ##version: 1.0
 */
public class HttpClientUtils {

    private static final RequestConfig requestConfig;

    static
    {
        // 设置请求和传输超时时间
        requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
    }

    /**
     * 根据xml格式进行post请求
     * ##Params: url	请求地址
     * ##Params: reDataXml	请求的xml数据
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 11:35
     */
    public static String doPostByXml(String url,String reDataXml){
        CloseableHttpClient httpClient;
        CloseableHttpResponse httpResponse;

        // 创建httpClient连接对象
        httpClient = HttpClients.createDefault();
        // 创建post请求连接
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(15000)   // 连接服务器主机超时时间
                .setConnectionRequestTimeout(60000) // 连接请求超时时间
                .setSocketTimeout(60000)    // 设置读取响应数据超时时间
                .build();

        // 为HTTPPOST请求设置参数
        httpPost.setConfig(requestConfig);

        // 将上传参数存放到entity属性中
        httpPost.setEntity(new StringEntity(reDataXml,"UTF-8"));

        // 添加头信息
        httpPost.addHeader("Content-Type","text/xml");

        String result = "";
        try {
            // 发送请求
            httpResponse = httpClient.execute(httpPost);
            // 从响应对象中获取返回内容
            HttpEntity httpEntity = httpResponse.getEntity();
            result = EntityUtils.toString(httpEntity, "UTF-8");
        } catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }

    /**
     * post请求传输json参数
     * ##Params: url	url地址
     * ##Params: jsonParam	json数据
     * ##return: java.lang.String  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 11:35
     */
    public static String httpPost(String url, Map<String,Object> jsonParam)
    {
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String resultStr = null;
        HttpPost httpPost = new HttpPost(url);
        // 设置请求和传输超时时间
        httpPost.setConfig(requestConfig);
        try
        {
            if (null != jsonParam)
            {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            // 请求发送成功，并得到响应
            if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                try
                {
                    // 读取服务器返回过来的json字符串数据
                    resultStr = EntityUtils.toString(result.getEntity(), "utf-8");
                }
                catch (Exception e)
                {

                }
            }
        }
        catch (IOException e)
        {

        }
        finally
        {
            httpPost.releaseConnection();
        }
        return resultStr;
    }

    /**
     * post请求传输String参数 例如：name=Jack&sex=1&type=2
     * Content-type:application/x-www-form-urlencoded
     * ##Params: url	url地址
     * ##Params: strParam	参数
     * ##return: com.alibaba.fastjson.JSONObject  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 11:36
     */
    public static JSONObject httpPost(String url, String strParam)
    {
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        try
        {
            if (null != strParam)
            {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(strParam, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/x-www-form-urlencoded");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            // 请求发送成功，并得到响应
            if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                String str = "";
                try
                {
                    // 读取服务器返回过来的json字符串数据
                    str = EntityUtils.toString(result.getEntity(), "utf-8");
                    // 把json字符串转换成json对象
                    jsonResult = JSONObject.parseObject(str);
                }
                catch (Exception e)
                {

                }
            }
        }
        catch (IOException e)
        {

        }
        finally
        {
            httpPost.releaseConnection();
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     * ##Params: url	路径
     * ##return: com.alibaba.fastjson.JSONObject  返回结果: 结果
     * ##Author: tang
     * ##version: 1.0.0
     * ##Updated: 2020/8/6 11:36
     */
    public static JSONObject httpGet(String url)
    {
        // get请求返回结果
        JSONObject jsonResult = null;
        CloseableHttpClient client = HttpClients.createDefault();
        // 发送get请求
        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);
        try
        {
            CloseableHttpResponse response = client.execute(request);

            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                // 读取服务器返回过来的json字符串数据
                HttpEntity entity = response.getEntity();
                String strResult = EntityUtils.toString(entity, "utf-8");
                // 把json字符串转换成json对象
                jsonResult = JSONObject.parseObject(strResult);
            }
            else
            {

            }
        }
        catch (IOException e)
        {

        }
        finally
        {
            request.releaseConnection();
        }
        return jsonResult;
    }


}
