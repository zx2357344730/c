package com.cresign.tools.advice;

import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName QdKey
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2022/8/3
 * @Version 1.0.0
 */
@Slf4j
public class QdKey {

    public static String client_Public_Key;
    public synchronized static String getSetPublicKey(String key,boolean isG){
        System.out.println("client_Public_Key:"+isG);
        System.out.println(client_Public_Key);
        if (isG) {
            client_Public_Key = key;
            System.out.println("set_client_Public_Key:");
            System.out.println(client_Public_Key);
            return null;
        } else {
            System.out.println("get_client_Public_Key:");
            System.out.println(client_Public_Key);
            return client_Public_Key;
        }
    }
    public static void setClient_Public_Key(String key){
        log.info("赋值");
        System.out.println("赋值");
        client_Public_Key = key;
        System.out.println("赋值成功");
        log.info("赋值成功");
    }
    public static String getClient_Public_Key(){
        log.info("获取赋值");
        System.out.println("获取赋值");
        return client_Public_Key;
    }

}
