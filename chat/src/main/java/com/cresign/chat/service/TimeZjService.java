package com.cresign.chat.service;

/**
 * @ClassName TimeService
 * @Description 作者很懒什么也没写
 * @Author tang
 * @Date 2021/12/20 11:09
 * @Version 1.0.0
 */
public interface TimeZjService {
//    /**
//     * 根据teS，grpB，dep添加任务信息
//     * @param teS	当前时间戳
//     * @param grpB	组别
//     * @param dep	部门
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 21:03
//     */
//    void addTasks(Long teS,String grpB,String dep,String id_C);
//
//    /**
//     * 根据teS添加teS当天任务
//     * @param teS	当前时间戳
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 21:02
//     */
//    void addTasksAndOrder(Long teS,String id_C);
//
//    /**
//     * 根据teS添加teS当天任务2
//     * @param teS	当前时间戳
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 21:02
//     */
//    void addTasksAndOrder3(Long teS,String id_C);
//
//    /**
//     * 添加模拟订单方法
//     * @param teS	当前时间戳
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 21:01
//     */
//    void addOrder(Long teS);
//
//    /**
//     * 添加模拟订单方法
//     * @param teS	当前时间戳
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 21:01
//     */
//    void addOrder2(Long teS);
//
//    /**
//     * 添加模拟订单方法
//     * @param teS	当前时间戳
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 21:01
//     */
//    void addOrder3(Long teS);

    void getAtFirst(String id_O,Long teStart,String id_C,Integer wn0TPrior);

    void removeTime(String id_O,String id_C);

//    /**
//     * 获取当前时间戳方法
//     * @param random	当前唯一编号
//     * @param grpB	组别
//     * @param dep	部门
//     * @return java.lang.Long  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 23:59
//     */
//    Long getTeS(String random,String grpB,String dep);

//    /**
//     * 根据grpB，dep获取数据库职位总人数方法
//     * @param grpB	职位
//     * @param dep	部门
//     * @return java.lang.Integer  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 23:39
//     */
//    Integer getObjGrpUNum(String grpB,String dep,String id_C,String randomAll);

//    /**
//     * 获取全新的任务信息对象方法
//     * @param tePStart	预计开始时间
//     * @param tePFinish	预计完成时间
//     * @param id_O	订单编号
//     * @param index	零件位置，配合订单编号使用
//     * @param teDurTotal	总共时间
//     * @param priority	优先级
//     * @param wrdNs	用户名称
//     * @param prep	准备时间
//     * @param teDelayDate	延迟总时间
//     * @return com.cresign.tools.pojo.po.chkin.Task  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/18 16:58
//     */
//    Task getTask(Long tePStart, Long tePFinish, String id_O, Integer index
//            , Long teDurTotal, Integer priority, String wrdNs, Long prep, Long teDelayDate,String id_C);

//    /**
//     * 时间处理方法
//     * @param task	当前处理任务信息
//     * @param hTeStart	任务最初始开始时间
//     * @param grpB	组别
//     * @param dep	部门
//     * @param id_O	订单编号
//     * @param index	订单下标
//     * @param isT	保存是否是时间处理方法调用的跳天操作：isT == 0 不是、isT == 1 是
//     * @param random	当前唯一编号
//     * @param isK	控制只进入一次时间空插全流程处理：isK == 0 不能进入、isK == 1 可以进入
//     * @param teDate	当前处理的任务的所在日期对象
//     * @param teDaF	当前任务所在日期对象
//     * @param isC	isC = 0 获取数据库任务信息、isC = 1 获取镜像任务信息
//     * @param sho	用于存储判断镜像是否是第一个被冲突的产品
//     * @param isYx	保存是否出现任务为空异常:isYx = 0 正常操作未出现异常、isYx = 1 出现拿出任务为空异常镜像|数据库
//     * @param randomAll 全局唯一编号
//     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/18 22:27
//     */
//    JSONObject chkInJi(Task task, Long hTeStart, String grpB
//            , String dep, String id_O, Integer index, Integer isT, String random
//            , Integer isK, JSONObject teDate, JSONObject teDaF
//            , Integer isC, JSONObject sho, int isYx,int csSta,boolean isD, String randomAll);

//    /**
//     * 任务最后处理方法
//     * @param teDaF 时间冲突的副本
//     * // @param random 当前任务唯一参数
//     * @param randomAll 全局唯一编号
//     * @return void  返回结果: 结果
//     * @author tang
//     * @version 1.0.0
//     * @date 2022/2/17 19:26
//     */
//    void setZui(JSONObject teDaF
////            ,String random
//            ,String id_C, String randomAll);
}
