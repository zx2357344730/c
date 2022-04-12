package com.cresign.tools.apires;

import lombok.Data;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2021-04-23 13:08
 * ##version: 1.0
 */
@Data
public class ApiResponse {

    /**
     * 异常状态码
     */
    private String code;

    /**
     * 返回信息
     */
    private String message;

    /**
     * 描述信息
     */
    private String des;

    /**
     * traceId skyWalking返回id
     */
    private String tid;

    public ApiResponse(String code, String message, String des) {
        this.code = code;
        this.message = message;
        this.des = des;
        this.tid = TraceContext.traceId();
    }
}