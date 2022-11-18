package com.cresign.tools.enumeration;

import lombok.Getter;

/**
 * ##description:    短信模板id
 * @author JackSon
 * @updated 2020/9/15 13:53
 * @ver 1.0
 */
@Getter
public enum SMSTypeEnum {


    LOGIN("sms_login_"),

    PURCHASE("sms_purchase_"),

    REGISTER("sms_register_");

    private String smsType;


    SMSTypeEnum(String smsType) {
        this.smsType = smsType;
    }

}
