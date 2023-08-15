package com.cresign.purchase.service;

import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.pojo.po.LogFlow;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName ZjTestService
 * @Date 2023/8/10
 * @ver 1.0.0
 */
public interface ZjTestService {
    ApiResponse getMdSetEs(String key,String esIndex,String condition,String val);
    ApiResponse sendLog(LogFlow logFlow);
}
