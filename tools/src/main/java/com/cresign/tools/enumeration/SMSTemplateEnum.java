package com.cresign.tools.enumeration;

import lombok.Getter;

/**
 * ##description:    短信模板id
 * ##author: JackSon
 * ##updated: 2020/9/15 13:53
 * ##version: 1.0
 */
@Getter
public enum SMSTemplateEnum {


    PURCHASE("632877"),

    //REGISTER("567688"),

    REGISTER("1018872"),

    LOGIN("554912")


    ;

    private String templateId;


    SMSTemplateEnum(String templateId) {
        this.templateId = templateId;
    }

}
