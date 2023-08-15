package com.cresign.tools.apires;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;

/**
 * ##description:
 * @author JackSon
 * @updated 2021-04-26 10:57
 * @ver 1.0
 */
@Component
public class LocalMessage {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HttpServletRequest request;

    /**
     * 获取国际化异常信息
     */
    public String getLocaleMessage(String code, String defaultMsg, Object[] params) {
        String language = request.getParameter("lang");
        Locale locale = Objects.nonNull(language) ? new Locale(language) : Locale.getDefault();

        try {
            return messageSource.getMessage(code, params, locale);
        } catch (Exception e) {
            if (code != "200") {
//                log.warn("Warning: {}, {}", code, params);
            }
            return defaultMsg;
        }
    }

}