package com.cresign.chat.utils;

import org.springframework.stereotype.Service;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName WsId
 * @Date 2023/8/22
 * @ver 1.0.0
 */
@Service
public class WsId {
    public static final String ws = "2";
    public static final String topic = "wsTopic"+ws;
    public static final String tap = "wsTap"+ws;
    public static final String group = "wsGroup"+ws;
}
