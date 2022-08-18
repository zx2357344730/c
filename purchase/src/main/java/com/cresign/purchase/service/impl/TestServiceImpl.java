package com.cresign.purchase.service.impl;

import com.cresign.purchase.service.TestService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.CoupaUtil;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * @ClassName TestServiceImpl
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/8/17
 * @Version 1.0.0
 */
@Service
@Slf4j
public class TestServiceImpl implements TestService {

    @Resource
    private CoupaUtil coupaUtil;
    @Autowired
    private RetResult retResult;

    @Override
    public ApiResponse verificationAUN(String id_APP) {
        User user = coupaUtil.getUserByKeyAndVal("info.id_APP", id_APP, Collections.singletonList("info"));
        if (null != user) {
            return retResult.ok(CodeEnum.OK.getCode(),user.getInfo().getId_AUN());
        }
        throw new ErrorResponseException(HttpStatus.OK, "userIsNull","1");
    }

}
