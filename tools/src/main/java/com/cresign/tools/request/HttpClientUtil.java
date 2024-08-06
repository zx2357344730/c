package com.cresign.tools.request;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

/*
 * 利用HttpClient进行post请求的工具类(https发送)
 */
public class HttpClientUtil {
//    public static String doPost(String url,Map<String,String> map,String charset){
//        HttpClient httpClient = null;
//        HttpPost httpPost = null;
//        String result = null;
//        try{
//            httpClient = new SSLClient();
//            httpPost = new HttpPost(url);
//            //设置参数
//            List<NameValuePair> list = new ArrayList<NameValuePair>();
//            Iterator iterator = map.entrySet().iterator();
//            while(iterator.hasNext()){
//                Entry<String,String> elem = (Entry<String, String>) iterator.next();
//                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
//            }
//            if(list.size() > 0){
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
//                httpPost.setEntity(entity);
//            }
//            HttpResponse response = httpClient.execute(httpPost);
//            if(response != null){
//                HttpEntity resEntity = response.getEntity();
//                if(resEntity != null){
//                    result = EntityUtils.toString(resEntity,charset);
//                }
//            }
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
//        return result;
//    }
//    public static String doPostJson(String url, JSONObject map, String charset){
//        HttpClient httpClient = null;
//        HttpPost httpPost = null;
//        String result = null;
//        try{
//            httpClient = new SSLClient();
//            httpPost = new HttpPost(url);
//            //设置参数
//            List<NameValuePair> list = new ArrayList<NameValuePair>();
//            Iterator iterator = map.entrySet().iterator();
//            System.out.println("iterator:");
//            System.out.println(JSON.toJSONString(iterator));
//            while(iterator.hasNext()){
//                Entry<String,String> elem = (Entry<String, String>) iterator.next();
//                System.out.println("elem:");
//                System.out.println(JSON.toJSONString(elem));
//                list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
//                System.out.println("list:");
//                System.out.println(JSON.toJSONString(list));
//            }
//            if(list.size() > 0){
//                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,charset);
//                httpPost.setEntity(entity);
//            }
//            HttpResponse response = httpClient.execute(httpPost);
//            if(response != null){
//                HttpEntity resEntity = response.getEntity();
//                if(resEntity != null){
//                    result = EntityUtils.toString(resEntity,charset);
//                }
//            }
//        }catch(Exception ex){
//            ex.printStackTrace();
//        }
//        return result;
//    }

    public static String sendPost(JSONObject json, String URL
//            ,String token
    ) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(URL);
        System.out.println("started");
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Authorization", "Basic YWRtaW46");
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setSocketTimeout(80000).build();
        post.setConfig(requestConfig);
//        post.setHeader("x-access-token",token);
        String result;
        try {

            StringEntity s = new StringEntity(json.toString(), "utf-8");
            s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(s);
            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, StandardCharsets.UTF_8));
            StringBuilder strBer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                strBer.append(line).append("\n");
            inStream.close();
            result = strBer.toString();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                System.out.println("请求服务器成功，做相应处理");
            } else {
                System.out.println("请求服务端失败" + result);
            }
        } catch (Exception e) {
            System.out.println("请求异常："+e.getMessage());
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String sendPostHeader(JSONObject json, String URL,JSONObject newHeader) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(URL);
        System.out.println("started");
        post.setHeader("Content-Type", "application/json");
//        post.setHeader("Authorization", "Basic YWRtaW46");
        for (String key : newHeader.keySet()) {
            post.setHeader(key, newHeader.getString(key));
        }
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setSocketTimeout(80000).build();
        post.setConfig(requestConfig);
        String result;
        try {

            StringEntity s = new StringEntity(json.toString(), "utf-8");
            s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(s);
            // 发送请求
            HttpResponse httpResponse = client.execute(post);
            // 获取响应输入流
            InputStream inStream = httpResponse.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, StandardCharsets.UTF_8));
            StringBuilder strBer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                strBer.append(line).append("\n");
            inStream.close();
            result = strBer.toString();
            if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                System.out.println("请求服务器成功，做相应处理");
                return result;
            } else {
                System.out.println("请求服务端失败" + result);
                return "";
            }
        } catch (Exception e) {
            System.out.println("请求异常："+e.getMessage());
            throw new RuntimeException(e);
        }
//        return result;
    }

    public static String sendPostHeaderAndAsync(JSONObject json, String URL,JSONObject newHeader,JSONObject resultObj
            ,String keyOutside) {
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        String result;
        try {
            httpclient.start();
            HttpPost request = new HttpPost(URL);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setSocketTimeout(80000).build();
            request.setConfig(requestConfig);
            request.setHeader("Content-Type", "application/json");
            for (String key : newHeader.keySet()) {
                request.setHeader(key, newHeader.getString(key));
            }
            StringEntity s = new StringEntity(json.toString(), "utf-8");
            s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            request.setEntity(s);
            Future<HttpResponse> future = httpclient.execute(request, null);
            // 获取结果
            HttpResponse response = future.get();
            // 获取响应输入流
            InputStream inStream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inStream, StandardCharsets.UTF_8));
            StringBuilder strBer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                strBer.append(line).append("\n");
            inStream.close();
            result = strBer.toString();
            httpclient.close();
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                System.out.println("请求服务器成功，做相应处理");
                JSONObject objResult = JSONObject.parseObject(result);
                String content = objResult.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getString("content");
//                System.out.println(descObj.getString(key)+"-翻译后-"+content);
                String modifiedString = content.replace("```json", "");
                modifiedString = modifiedString.replace("```","");
                System.out.println(modifiedString);
                resultObj.put(keyOutside,JSONObject.parseObject(modifiedString).getString("re"));
                return result;
            } else {
                System.out.println("请求服务端失败" + result);
                return "";
            }
        } catch (Exception e) {
            try {
                httpclient.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.out.println("请求异常："+e.getMessage());
            throw new RuntimeException(e);
        }
//        return result;
    }

    public static String sendPostAsync(JSONObject json, String URL,JSONObject newHeader,JSONObject resultObj
            ,String keyOutside){
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
        try {
            httpclient.start();
            HttpPost request = new HttpPost(URL);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000).setSocketTimeout(80000).build();
            request.setConfig(requestConfig);
            request.setHeader("Content-Type", "application/json");
            for (String key : newHeader.keySet()) {
                request.setHeader(key, newHeader.getString(key));
            }
            StringEntity s = new StringEntity(json.toString(), "utf-8");
            s.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            request.setEntity(s);
            Future<HttpResponse> execute = httpclient.execute(request, new FutureCallback<HttpResponse>() {
                public void completed(HttpResponse response) {
                    try {
                        // 获取响应输入流
                        InputStream inStream = response.getEntity().getContent();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(
                                inStream, StandardCharsets.UTF_8));
                        StringBuilder strBer = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null)
                            strBer.append(line).append("\n");
                        inStream.close();
                        String result = strBer.toString();
                        httpclient.close();
                        JSONObject objResult = JSONObject.parseObject(result);
                        String content = objResult.getJSONArray("choices").getJSONObject(0)
                                .getJSONObject("message").getString("content");
                        String modifiedString = content.replace("```json", "");
                        modifiedString = modifiedString.replace("```", "");
                        System.out.println(modifiedString);
                        resultObj.put(keyOutside, JSONObject.parseObject(modifiedString).getString("re"));
                        System.out.println("请求成功:");
                        // 处理响应
//                        System.out.println(response.getStatusLine());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void failed(Exception ex) {
                    // 处理异常
                    System.out.println(ex.getMessage());
                }

                public void cancelled() {
                    // 处理取消事件
                    System.out.println("请求取消");
                }
            });
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "2";
        }
    }
 
    /**
     * 发送get请求
     * @param url       链接地址
     * @param charset   字符编码，若为null则默认utf-8
     * @return
     */
    public static String doGet(String url,String charset){
        if(null == charset){
            charset = "utf-8";
        }
        HttpClient httpClient = null;
        HttpGet httpGet= null;
        String result = null;
 
        try {
            httpClient = new SSLClient();
            httpGet = new HttpGet(url);
            httpGet.setProtocolVersion(HttpVersion.HTTP_1_0);
            httpGet.addHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
 
            HttpResponse response = httpClient.execute(httpGet);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
 
 
}  