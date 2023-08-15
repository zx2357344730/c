package com.cresign.tools.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ##description:
 * @author JackSon
 * @updated 2020/9/27 14:13
 * @ver 1.0
 */
@Getter
public class ErrorResponseException extends RuntimeException{


    /**
     * 响应码 (200,404)......
     */
    private HttpStatus status;

    /**
     * 异常状态码
     */
    private String code;

    /**
     * 返回信息
     */
    private String message;

    private String des;

    /**
     * traceId skyWalking返回id
     */
    private String tid;


    public ErrorResponseException(HttpStatus status, String code, String des) {
        this.status = status;
        this.code = code;
//        this.message = des;
        this.des = des == null ? "" : des;
//        this.tid = TraceContext.traceId();
    }

}