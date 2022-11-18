package com.cresign.tools.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@Getter
/**
 * 返回异常状态码
 * @author JackSon
 * @updated 2020/7/25 10:31
 * @return
 */
public class ResponseException extends RuntimeException{

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


    public ResponseException(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}