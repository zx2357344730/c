package com.cresign.tools.pojo.po.chkin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName Hr
 * @Date 2023/8/15
 * @ver 1.0.0
 */
@Data
//生成全参数构造函数
@AllArgsConstructor
//注解在类上，为类提供一个无参的构造方法
@NoArgsConstructor
public class Hr {
    /**
     * 变动原岗位
     */
    private String grpUNow;
    /**
     * 招聘人数
     */
    private String wn0Hire;
    /**
     * 变动后岗位
     */
    private String grpUNew;
    /**
     * 申请人
     */
    private String id_UA;
    /**
     * 申请备注
     */
    private String wrddesc;
    /**
     * 批准备注
     */
    private String wrdappv;
    /**
     * 招聘部门
     */
    private String dep;
}
