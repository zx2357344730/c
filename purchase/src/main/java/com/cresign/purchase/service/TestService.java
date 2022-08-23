package com.cresign.purchase.service;

import com.cresign.tools.apires.ApiResponse;

/**
 * @ClassName TestService
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/8/17
 * @Version 1.0.0
 */
public interface TestService {

    /**
     * 验证appId，并且返回AUN—ID
     * @param id_APP	应用编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/19
     */
    ApiResponse verificationAUN(String id_APP);

}
