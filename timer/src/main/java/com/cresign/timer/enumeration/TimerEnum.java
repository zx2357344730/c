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

    //统计分组字段为空
    EXCELFIELD_IS_NULL("070001"),
    //统计数据大于1000条
    STAT_LENGTH_GT("070002"),
    //文件名小于3字
    FILENAME_LENGTH_LT("070003"),


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
