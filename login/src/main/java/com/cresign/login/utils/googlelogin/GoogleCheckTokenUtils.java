package com.cresign.login.utils.googlelogin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.request.HttpClientUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;

/**
 * google验证token工具类
 *
 * @author sy
 * ##Updated: 2019/8/27 14:10
 */
public class GoogleCheckTokenUtils implements Serializable {
 
  /**
   * 验证token的地址
   */
//  private static final String GOOGLE_URL = "https://oauth2.googleapis.com/tokeninfo";
  public static String GOOGLE_URL;
  @Value("${thisConfig.GOOGLE_URL}")
  public void setGoogleUrl(String googleUrl){
    GoogleCheckTokenUtils.GOOGLE_URL = googleUrl;
  }

  /**
   * 开发者账号上申请的应用的client_id
   */
//  private static final String CLIENT_ID = "271760780779-2n7ftjqfdj7pgr59krtbst1b5b1eemml.apps.googleusercontent.com";
  public static String CLIENT_ID;
  @Value("${thisConfig.CLIENT_ID}")
  public void setClientId(String clientId){
    GoogleCheckTokenUtils.CLIENT_ID = clientId;
  }

  /**
   * 默认编码为utf-8
   */
  private static final String charset = "utf-8";
 
 
  /**
   * slf4j打印日志
   */
  private static Logger logger = LoggerFactory.getLogger(GoogleCheckTokenUtils.class);
 
 
  /**
   * 判断id_token是否生效,
   */
  public static JSONObject checkGoogleToken(String id_Token) {

    //构建url和参数
    StringBuffer sb = new StringBuffer();
    sb.append(GOOGLE_URL);
    sb.append("?id_token=");
    sb.append(id_Token);


    String result = HttpClientUtil.doGet(sb.toString(), charset);
    logger.info("google token check result is : {} ",result);

    // 初始化返回错误
    JSONObject resultJson = new JSONObject();


    // 判断返回的值是否为空，为空则不对
    if (StringUtils.isBlank(result)) {

      resultJson.put("code", "404");

    } else {

      //转成Object对象
      resultJson = JSON.parseObject(result, JSONObject.class);

      //比较aud,判断是否请求来源你的程序
      if (resultJson != null && resultJson.getString("aud").equals(CLIENT_ID)) {

        resultJson.put("code", "200");

      } else {

        resultJson.put("code", "403");

      }

    }
    return resultJson;
  }

  public static void main(String[] args) {
    JSONObject jsonObject = checkGoogleToken("eyJhbGciOiJSUzI1NiIsImtpZCI6ImZiOGNhNWI3ZDhkOWE1YzZjNjc4ODA3MWU4NjZjNmM0MGYzZmMxZjkiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXpwIjoiMjcxNzYwNzgwNzc5LTJuN2Z0anFmZGo3cGdyNTlrcnRic3QxYjViMWVlbW1sLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiYXVkIjoiMjcxNzYwNzgwNzc5LTJuN2Z0anFmZGo3cGdyNTlrcnRic3QxYjViMWVlbW1sLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTAwNjc1Njg3ODY1OTA1OTQ5MDUzIiwiZW1haWwiOiJxcTg1MjY0OTM0OEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IldWNlpfaDdXbGY5bVBXd1o1VzB4TFEiLCJuYW1lIjoi5L2V5a626L6JIiwicGljdHVyZSI6Imh0dHBzOi8vbGg2Lmdvb2dsZXVzZXJjb250ZW50LmNvbS8tYjZ3SHBuUjhyN28vQUFBQUFBQUFBQUkvQUFBQUFBQUFBQUEvQU1adXVja0Y4ZEVJTVM2MlFoTEJ1eWVkemRrT0JnOF9SUS9zOTYtYy9waG90by5qcGciLCJnaXZlbl9uYW1lIjoi5a626L6JIiwiZmFtaWx5X25hbWUiOiLkvZUiLCJsb2NhbGUiOiJ6aC1DTiIsImlhdCI6MTU5MDk3MzE3NSwiZXhwIjoxNTkwOTc2Nzc1LCJqdGkiOiJlZTNhNTA0MzNiN2JjOTI1ZTYwYzJhZTUxNjMwNWQ3MjE1NTNiZTRiIn0.wewlF5-GcnOK-Q5D_eNhL0QFo4HDogoauWqIKokZ3JtqhxNtvzusnt8Vg4weAWFsZ1PWCQUL0AQfhY7rgqD1Eu8taWfe7Hwo5xrxgxpCqIwBU9ZmDtDNwBMg1BCMastWYSQJB3huMK8RSyAQNGoRixGAh-jVoGGMeijRAH0XTW2g1mw5Pi4jzWtD3rHV-2ZGkrUJMzg6s4pSkeOkbRUlVgG7kQ3bBmXKFDgal2HN1WCBiWWdxSv-3LsvOP-ZXMrpyR5ulLFvf-kr1b4efAhndvWutz7uVAZew3No3V79NrJjEednS0qzZphOZWsE1khBXBVehGxlzyx9d1aINGn5aA");

  }

 
}