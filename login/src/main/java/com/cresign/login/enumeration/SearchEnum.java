package com.cresign.login.enumeration;

import lombok.Getter;

/**
 * @author Jeovn
 * @updated 2020/5/23
 * @ver 1.1.0
 * ##description: 通用异常枚举类
 */
@Getter
public enum SearchEnum {


    //传入的参数错误
    KEY_IS_NOT_FOUND("060001"),
    // 查找的数据为空
    DATA_IS_NULL("060002"),
    //init_java不存在
    INIT_IS_NULL("060003"),
    //assset不存在
    ASSET_NOT_FOUND("060004"),
    //用户不存在
    USER_IS_NO_FOUND("060005"),
    //没有找到该公司
    COMP_NOT_FOUND("060006"),
    //用户已加入到该公司
    USER_JOIN_IS_HAVE("060007"),
    //用户加入公司错误
    USER_JOIN_COMP_ERROR("060008"),
    //不能删除默认组别
    DEFAULT_KEY("060009"),
    //统计分组字段为空
    EXCELFIELD_IS_NULL("060010"),
    //统计数据大于1000条
    STAT_LENGTH_GT("060011"),
    //文件名小于3字
    FILENAME_LENGTH_LT("060012"),

    /**
        QR
     */
    //二维码防伪失败
    QR_CODE_Anti_ERROR("061001"),
    //加入公司二维码已过期
    JOIN_COMP_CODE_OVERDUE("061002"),
    //加入公司二维码已存在
    JOINCOMP_CODE_EXIST("061003"),
    //产品二维码已经存在
    PROD_CODE_IS_EXIT("061004"),
    //产品二维码已过期
    PROD_CODE_OVERDUE("061005"),
    //统计结果为空
    STAT_IS_NULL("060013"),
    ;
    /**
     * 异常状态码
     */
    private String code;

    /**
     * 带参构造方法
     * @param code  异常状态码
     */
    SearchEnum(String code){
        this.code = code;

    }




}