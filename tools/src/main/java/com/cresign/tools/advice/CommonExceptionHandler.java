package com.cresign.tools.advice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.exception.ResponseException;
import feign.FeignException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JackSon
 * @updated 2020/7/25 10:34
 */
@ControllerAdvice  //不指定包默认加了@Controller和@RestController都能控制
public class CommonExceptionHandler {

//    @Autowired
//    private LocalMessage localMessage;
//


    /**
     * 捕获自定义异常返回出去给前端
     * @param reEx
     * @author JackSon
     * @updated 2020/7/25 10:34
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */
    @ResponseBody
    @ExceptionHandler(value = ResponseException.class)
    public ResponseEntity<String> myExceptionHandler(ResponseException reEx){

        Map<String,Object> map  = new HashMap<String,Object>(3);

        Object [] params = new Object[]{"params"};

        if (null != reEx) {

            map.put("code",reEx.getCode());

            map.put("message",reEx.getMessage());

//            map.put("des", getLocaleMessage(map.get("code").toString(), "", params));
            map.put("des", "成功");

            return ResponseEntity.status(reEx.getStatus())
                    .body(JSON.toJSONString(map));
        }

        // code == 500
        map.put("code", CodeEnum.INTERNAL_SERVER_ERROR.getCode());

        map.put("message", "i am in  exception 1"+JSON.toJSONString(map));

        map.put("des", JSON.toJSONString(map));
        System.out.println("i am in  exception 1");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(JSON.toJSONString(map));

    }

    /**
     * 捕获自定义异常返回出去给前端
     * @param reEx
     * @author JackSon
     * @updated 2020/7/25 10:34
     * @return java.util.Map<java.lang.String, java.lang.Object>
     */
    @ResponseBody
    @ExceptionHandler(value = ErrorResponseException.class)
    public ResponseEntity<String> myExceptionErrorHandler(ErrorResponseException reEx){

        Map<String,Object> map  = new HashMap<String,Object>(4);

        Object [] params = new Object[]{"params"};

        if (null != reEx) {

            map.put("code",reEx.getCode());

            map.put("message",reEx.getMessage());

            map.put("tid",reEx.getTid());

//            map.put("des", localMessage.getLocaleMessage(map.get("code").toString(), "", params));
            map.put("des", reEx.getDes());
            return ResponseEntity.status(reEx.getStatus())
                    .body(JSON.toJSONString(map));
        }

        map.put("code", CodeEnum.INTERNAL_SERVER_ERROR.getCode());

        map.put("message", "i am in  exception 2"+JSON.toJSONString(map));

        map.put("des", JSON.toJSONString(map));
        System.out.println("i am in  exception 2");

        reEx.printStackTrace();

        System.out.println(reEx);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(JSON.toJSONString(map));

    }

    @ExceptionHandler(value = {FeignException.class})
    public ResponseEntity<String> feignException(FeignException e) {

        Map<String,Object> map  = new HashMap<String,Object>(3);

        Object [] params = new Object[]{"params"};



        if (StringUtils.isNoneEmpty(e.contentUTF8())) {

            JSONObject jsonObject = JSONObject.parseObject(e.contentUTF8());

            map.put("code", jsonObject.getString("code"));

            map.put("message", jsonObject.getString("message"));

//            map.put("des", localMessage.getLocaleMessage(map.get("code").toString(), "", params));
            map.put("des", "");


            return ResponseEntity.status(e.status())
                    .body(JSON.toJSONString(map));
        }

        map.put("code", CodeEnum.OK.getCode());

        e.printStackTrace();

        map.put("message", "feignException");

//        map.put("des", localMessage.getLocaleMessage(map.get("code").toString(), "", params));
        map.put("des", JSON.toJSONString(e));
        System.out.println("i am in  feignexception 1");



//        log.error(JSON.toJSONString(map));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(JSON.toJSONString(map));

    }








}

