package com.cresign.action.common;

import com.cresign.tools.mongo.MongoUtils;

/**
 * ##ClassName: ActionConstants
 * ##description: chat静态变量定义类
 * @author tang
 * ##Updated: 2020/8/7 14:55
 * @ver 1.0.0
 */
public class ActionConstants {

    /**
     * HashMap默认设置大小
     * 详情参考:https://blog.csdn.net/l18848956739/article/details/85998121
     */
    public static final int HASH_MAP_DEFAULT_LENGTH = 16;

    public static final String Action_Id = MongoUtils.GetObjectId();
}
