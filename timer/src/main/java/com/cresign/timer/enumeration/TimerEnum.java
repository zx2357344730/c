package com.cresign.timer.enumeration;

import lombok.Getter;

/**
 * ##description:
 * ##author: JackSon
 * ##updated: 2020/8/11 10:26
 * ##version: 1.0
 */
@Getter
public enum TimerEnum {
    //jobName已存在
    JOBNAME_IS_EXIST("070001"),
    //jobName不存在
    JOBNAME_NOT_EXIST("070002")

    ;
    /**
     * 异常状态码
     */
    private final String code;

    /**
     * 带参构造方法
     * ##Params: code  异常状态码
     */
    TimerEnum(String code){
        this.code = code;

    }

}
