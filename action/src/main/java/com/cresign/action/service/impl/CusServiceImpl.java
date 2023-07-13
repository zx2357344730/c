//package com.cresign.action.service.impl;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cresign.action.service.CusService;
//import com.cresign.tools.advice.RetResult;
//import com.cresign.tools.apires.ApiResponse;
//import com.cresign.tools.dbTools.DateUtils;
//import com.cresign.tools.dbTools.Qt;
//import com.cresign.tools.dbTools.Ws;
//import com.cresign.tools.enumeration.CodeEnum;
//import com.cresign.tools.enumeration.DateEnum;
//import com.cresign.tools.pojo.po.Asset;
//import com.cresign.tools.pojo.po.LogFlow;
//import com.cresign.tools.pojo.po.Order;
//import com.cresign.tools.pojo.po.User;
//import com.cresign.tools.pojo.po.orderCard.OrderCusmsg;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
///**
// * @author tang
// * @Description 作者很懒什么也没写
// * @ClassName CusServiceImpl
// * @Date 2023/4/22
// * @ver 1.0.0
// */
//@Service
//public class CusServiceImpl implements CusService {
//
//    @Autowired
//    private Qt qt;
//
//    @Autowired
//    private Ws ws;
//
//    @Autowired
//    private RetResult retResult;
//
//    @Override
//    public ApiResponse getCusListByUser(String id_U,JSONArray types) {
//        JSONObject result = new JSONObject();
//        String resultDesc;
//        User user = qt.getMDContent(id_U, "rolex", User.class);
//        if (null != user) {
//            JSONObject rolex = user.getRolex();
//            if (null != rolex) {
//                JSONObject cus = rolex.getJSONObject("cus");
//                if (null != cus) {
//                    JSONArray resultArr = new JSONArray();
//                    JSONArray orderErrList = new JSONArray();
//                    cus.keySet().forEach(cusKey -> {
//                        List<Integer> indexList = new ArrayList<>();
//                        JSONObject object = cus.getJSONObject(cusKey);
//                        if (null != object) {
//                            object.keySet().forEach(key -> {
//                                int integer = object.getInteger(key);
//                                for (int i = 0; i < types.size(); i++) {
//                                    int type = types.getInteger(i);
//                                    if (integer==type)
//                                        indexList.add(Integer.parseInt(key));
//                                }
//                            });
//
//                            Order order = qt.getMDContent(cusKey, "cusmsg", Order.class);
//                            if (null != order) {
//                                JSONObject cusmsg = order.getCusmsg();
//                                if (null != cusmsg) {
//                                    System.out.println("输出cusmsg:");
//                                    System.out.println(JSON.toJSONString(cusmsg));
//                                    System.out.println(JSON.toJSONString(indexList));
//                                    JSONArray objCusMsg = cusmsg.getJSONArray("objCusMsg");
//                                    indexList.forEach(index -> resultArr.add(objCusMsg.getJSONObject(index)));
//                                } else {
//                                    System.out.println("订单:"+cusKey+" 的cusmsg卡片为空");
//                                    JSONObject orderErr = new JSONObject();
//                                    orderErr.put("desc",cusKey+":订单的cusmsg卡片为空");
//                                    orderErrList.add(orderErr);
//                                }
//                            } else {
//                                System.out.println("订单:"+cusKey+" 为空");
//                                JSONObject orderErr = new JSONObject();
//                                orderErr.put("desc",cusKey+":订单为空");
//                                orderErrList.add(orderErr);
//                            }
//                        }
//                    });
//                    result.put("type",1);
//                    result.put("orderErrList",orderErrList);
//                    result.put("resultArr",resultArr);
//                    System.out.println("输出结果:");
//                    System.out.println(JSON.toJSONString(result));
//                    return retResult.ok(CodeEnum.OK.getCode(), result);
//                } else {
//                    resultDesc = "用户客服信息为空";
//                }
//            } else {
//                resultDesc = "用户权限信息为空";
//            }
//        } else {
//            resultDesc = "用户信息为空";
//        }
//        result.put("type",3);
//        result.put("desc",resultDesc);
//        return retResult.ok(CodeEnum.OK.getCode(), result);
//    }
//
//    @Override
//    public ApiResponse getCusListByCusUser(String id_U, String id_O,JSONArray types) {
//        JSONObject result = new JSONObject();
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        String resultDesc;
//        if (null != order) {
//            JSONObject cusmsg = order.getCusmsg();
//            if (null != cusmsg) {
//                JSONObject cus = cusmsg.getJSONObject("cus");
//                JSONObject cusUser = cus.getJSONObject(id_U);
//                List<Integer> indexList = new ArrayList<>();
//                cusUser.keySet().forEach(index -> {
//                    for (int i = 0; i < types.size(); i++) {
//                        int type = types.getInteger(i);
//                        if (cusUser.getInteger(index) == type)
//                            indexList.add(Integer.parseInt(index));
//                    }
//                });
//                JSONArray resultArr = new JSONArray();
//                JSONArray objCusMsg = cusmsg.getJSONArray("objCusMsg");
//                for (Integer index : indexList) {
//                    resultArr.add(objCusMsg.getJSONObject(index));
//                }
//                result.put("type",1);
//                result.put("resultArr",resultArr);
//                result.put("desc","获取列表成功");
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            } else {
//                resultDesc = "订单cusmsg卡片为空";
//            }
//        } else {
//            resultDesc = "订单为空";
//        }
//        result.put("desc",resultDesc);
//        result.put("type",2);
//        return retResult.ok(CodeEnum.OK.getCode(), result);
//    }
//
//    /**
//     * 顾客发送日志api
//     * @param logFlow	日志信息
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/4/24
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse sendUserCusCustomer(LogFlow logFlow) {
//        String subType = logFlow.getSubType();
//        JSONObject result = new JSONObject();
//        String id_O = logFlow.getId_O();
//        String id_CCus = logFlow.getId_C();
//        Integer index = logFlow.getIndex();
//        JSONArray id_Us = new JSONArray();
//        String id_uThis = logFlow.getId_U();
//        id_Us.add(id_uThis);
//
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        int cusOrder = getCusOrder(order, null, 1);
//        if (cusOrder != 0) {
//            result.put("errType",1);
//            result.put("type",cusOrder);
//            result.put("desc","订单异常");
//            sendMsgNotice(id_CCus,"订单异常:异常码="+cusOrder, null,id_O,500,id_Us,-1);
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        }
//        String id_UCus = order.getCusmsg().getJSONArray("objCusMsg").getJSONObject(index).getString("id_UCus");
//        if ("msg".equals(subType)) {
//            sendMsgOneNew(id_UCus,logFlow);
//            result.put("errType",0);
//            result.put("type",4);
//            result.put("desc","发送信息-操作成功");
//            return retResult.ok(CodeEnum.OK.getCode(), "1");
//        } else if ("score".equals(subType)) {
//            qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".score"
//                    ,logFlow.getData().getInteger("score")), Order.class);
//            sendMsgOneNew(id_UCus,logFlow);
//            result.put("errType",0);
//            result.put("type",3);
//            result.put("desc","评分-操作成功");
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        } else {
////            String id_UCus = data.getString("id_UCus");
//            User user = qt.getMDContent(id_uThis, "rolex", User.class);
//            int cusUser = getCusUser(user, id_O);
//            if (cusUser != 0) {
//                result.put("errType",2);
//                result.put("type",cusUser);
//                result.put("desc","用户异常");
//                sendMsgNotice(id_CCus,"用户异常:异常码="+cusUser, null,id_O,500,id_Us,-1);
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            }
//            if ("rejection".equals(subType)) {
//
//                qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_O+"."+(index+""),1), User.class);
//
//                qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".bcdStatus",55), Order.class);
//                sendMsgOneNew(id_UCus,logFlow);
//                sendMsgNotice(id_CCus,"已拒收该消息",id_uThis,id_O,55,id_Us,index);
//                result.put("errType",0);
//                result.put("type",1);
//                result.put("desc","拒收该消息-操作成功");
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//
////            JSONObject cusUser = new JSONObject();
////            cusUser.put("state",0);
////            cusUser.put("id_UCus",null);
////            cusUser.put("cusFoUp",0);
////            cusUser.put("id_O",id_O);
////            cusUser.put("index",null);
////            qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
////            sendMsgOneNew(id_uThis,logFlow);
////            sendMsgNotice(id_CCus,"已拒收该公司消息",id_uThis,id_O,55,id_Us);
////            return retResult.ok(CodeEnum.OK.getCode(), result);
//            } else if ("del".equals(subType)) {
////                User user = qt.getMDContent(id_uThis, "rolex", User.class);
////                if (null != user) {
////                    JSONObject rolex = user.getRolex();
////                    if (null != rolex) {
////                        JSONObject cus = rolex.getJSONObject("cus");
////                        if (null != cus) {
////                            qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_O+"."+index.toString(),2), User.class);
////
////                            qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".bcdStatus",54), Order.class);
////
////                            sendMsgOneNew(id_uThis,logFlow);
////                            sendMsgNotice(id_CCus,"已删除该公司信息",id_uThis,id_O,54,id_Us);
////                            return retResult.ok(CodeEnum.OK.getCode(), "1");
////                        } else {
////                            resultDesc = "用户客服信息为空";
////                        }
////                    } else {
////                        resultDesc = "用户权限信息为空";
////                    }
////                } else {
////                    resultDesc = "用户信息为空";
////                }
////                sendMsgNotice(id_CCus,resultDesc, null,id_O,500,id_Us);
////                return retResult.ok(CodeEnum.OK.getCode(), "3");
//
//                qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_O+"."+(index+""),2), User.class);
//
//                qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".bcdStatus",54), Order.class);
//
//                sendMsgOneNew(id_UCus,logFlow);
//                sendMsgNotice(id_CCus,"已删除该信息",id_uThis,id_O,54,id_Us,index);
//                result.put("errType",0);
//                result.put("type",2);
//                result.put("desc","删除该信息-操作成功");
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            } else if ("normal".equals(subType)) {
//                qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_O+"."+(index+""),0), User.class);
//
//                qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".bcdStatus",53), Order.class);
//
//                sendMsgOneNew(id_UCus,logFlow);
//                sendMsgNotice(id_CCus,"恢复正常",id_uThis,id_O,53,id_Us,index);
//                result.put("errType",0);
//                result.put("type",5);
//                result.put("desc","恢复正常-操作成功");
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            }
////            else if ("score".equals(subType)) {
//////                User user = qt.getMDContent(id_uThis, "rolex", User.class);
//////                String id_uCusNew;
//////                String score;
//////                JSONObject cusUser;
//////                if (null != user) {
//////                    JSONObject rolex = user.getRolex();
//////                    if (null != rolex) {
//////                        JSONObject cus = rolex.getJSONObject("cus");
//////                        if (null != cus) {
//////                            cusUser = cus.getJSONObject(id_CCus);
//////                            id_uCusNew = cusUser.getString("id_UCus");
//////                            score = data.getString("score");
//////                        } else {
//////                            sendMsgNotice(id_CCus,"用户客服信息为空", null,id_O,500,id_Us);
//////                            return retResult.ok(CodeEnum.OK.getCode(), "0");
//////                        }
//////                    } else {
//////                        sendMsgNotice(id_CCus,"用户权限信息为空", null,id_O,500,id_Us);
//////                        return retResult.ok(CodeEnum.OK.getCode(), "0");
//////                    }
//////                } else {
//////                    sendMsgNotice(id_CCus,"用户信息为空", null,id_O,500,id_Us);
//////                    return retResult.ok(CodeEnum.OK.getCode(), "0");
//////                }
//////
//////                Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//////                if (null != asset) {
//////                    JSONObject flowControl = asset.getFlowControl();
//////                    if (null != flowControl) {
//////                        JSONObject cusAsset = flowControl.getJSONObject("cus");
//////                        JSONObject id_uCusInfo = cusAsset.getJSONObject(id_uCusNew);
//////                        int scoreCus = id_uCusInfo.getInteger(score);
//////                        scoreCus++;
//////                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_uCusNew+".score."+score,scoreCus), Asset.class);
//////                        cusUser.put("id_UCus","");
//////                        cusUser.put("cusFoUp",cusUser.getInteger("cusFoUp"));
//////                        cusUser.put("state",cusUser.getInteger("state"));
//////                        qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//////                        sendMsgOneNew(id_uThis,logFlow);
//////                        sendMsgOneNew(id_uCusNew,logFlow);
//////                        return retResult.ok(CodeEnum.OK.getCode(), "1");
//////                    } else {
//////                        resultDesc = "资产消息信息为空";
//////                    }
//////                } else {
//////                    resultDesc = "资产信息为空";
//////                }
//////                sendMsgNotice(id_CCus,resultDesc, null,id_O,500,id_Us);
//////                return retResult.ok(CodeEnum.OK.getCode(), "4");
////
////                qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".score"
////                        ,logFlow.getData().getInteger("score")), Order.class);
////                sendMsgOneNew(id_UCus,logFlow);
////                result.put("errType",0);
////                result.put("type",3);
////                result.put("desc","评分-操作成功");
////                return retResult.ok(CodeEnum.OK.getCode(), result);
////            }
//            else {
//                sendMsgNotice(id_CCus, "不识别子类型", id_uThis, id_O, 500, id_Us,-1);
//                result.put("errType", 3);
//                result.put("type", 0);
//                result.put("desc", "不识别子类型");
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            }
//        }
//    }
//
//    /**
//     * 客服发送日志api
//     * @param logFlow	日志信息
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/4/24
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse sendUserCusService(LogFlow logFlow) {
//        JSONObject result = new JSONObject();
//        String subType = logFlow.getSubType();
//        String id_CCus = logFlow.getId_C();
//        String id_uThis = logFlow.getId_U();
//        String id_O = logFlow.getId_O();
//        Integer index = logFlow.getIndex();
//        JSONArray id_Us = new JSONArray();
//        id_Us.add(id_uThis);
//
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        int cusOrder = getCusOrder(order, null, 1);
//        if (cusOrder != 0) {
//            result.put("errType",1);
//            result.put("type",cusOrder);
//            result.put("desc","订单异常");
//            sendMsgNotice(id_CCus,"订单异常:异常码="+cusOrder, null,id_O,500,id_Us,-1);
//            return retResult.ok(CodeEnum.OK.getCode(), result);
//        }
//        String id_U = order.getCusmsg().getJSONArray("objCusMsg").getJSONObject(index).getString("id_U");
//        if ("score".equals(subType)) {
////            Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
////            if (null != asset) {
////                JSONObject flowControl = asset.getFlowControl();
////                if (null != flowControl) {
////                    JSONObject cusAsset = flowControl.getJSONObject("cus");
////                    if (null != cusAsset) {
////                        JSONObject id_uThisInfo = cusAsset.getJSONObject(id_uThis);
////                        JSONObject userAll = id_uThisInfo.getJSONObject("userAll");
////                        JSONObject id_uPo = userAll.getJSONObject(id_uPointTo);
////                        id_uPo.put("type",2);
////                        userAll.put(id_uPointTo,id_uPo);
////                        id_uThisInfo.put("userAll",userAll);
////                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_uThis,id_uThisInfo), Asset.class);
////                        JSONObject foUp = new JSONObject();
////                        foUp.put("img","111.jpg");
////                        foUp.put("type",1);
////                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.cusFoUp."+id_uPointTo,foUp), Asset.class);
////                        sendMsgOneNew(id_uPointTo,logFlow);
////                        sendMsgOneNew(id_uThis,logFlow);
////                        return retResult.ok(CodeEnum.OK.getCode(), "2");
////                    } else {
////                        resultDesc = "资产客服信息为空";
////                    }
////                } else {
////                    resultDesc = "资产消息信息为空";
////                }
////            } else {
////                resultDesc = "资产信息为空";
////            }
////            sendMsgNotice(id_CCus,resultDesc, null,id_O,500,id_Us);
////            return retResult.ok(CodeEnum.OK.getCode(), "0");
//
//            sendMsgOneNew(id_U,logFlow);
//            result.put("errType",0);
//            result.put("type",1);
//            result.put("desc","申请评分");
////            return retResult.ok(CodeEnum.OK.getCode(), result);
//        } else {
//            JSONObject indexInfo = order.getCusmsg().getJSONArray("objCusMsg").getJSONObject(index);
//            Integer bcdStatus = indexInfo.getInteger("bcdStatus");
//            if (bcdStatus == 54) {
//                result.put("errType",2);
//                result.put("type",1);
//                result.put("desc","foUp".equals(subType)?"无法回访该用户!":"消息发送失败!");
//                sendMsgNotice(id_CCus,"foUp".equals(subType)?"无法回访该用户!":"消息发送失败!"
//                        , null,id_O,500,id_Us,index);
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            } else if (bcdStatus == 55) {
//                result.put("errType",2);
//                result.put("type",2);
//                result.put("desc","用户已拒收!");
//                sendMsgNotice(id_CCus,"用户已拒收!", null,id_O,500,id_Us,index);
//                return retResult.ok(CodeEnum.OK.getCode(), result);
//            }
//            if ("foUp".equals(subType)) {
////                User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
////                if (null != user) {
////                    JSONObject rolex = user.getRolex();
////                    if (null != rolex) {
////                        JSONObject cus = rolex.getJSONObject("cus");
////                        if (null != cus) {
////                            JSONObject cusUser = cus.getJSONObject(id_CCus);
////                            if (null == cusUser) {
////                                resultDesc = "无法回访该用户!";
////                            } else {
////                                int state = cusUser.getInteger("state");
////                                if (state == 0) {
////                                    resultDesc = "用户已拒收!";
////                                } else {
////                                    int cusFoUp = cusUser.getInteger("cusFoUp");
////                                    if (cusFoUp > 0) {
////                                        sendMsgOneNew(id_uThis,logFlow);
////                                        sendMsgOneNew(id_uPointTo,logFlow);
////                                        cusFoUp--;
////                                        cusUser.put("cusFoUp",cusFoUp);
////                                        qt.setMDContent(id_uPointTo,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
////                                        return retResult.ok(CodeEnum.OK.getCode(), "3");
////                                    } else {
////                                        resultDesc = "回访次数已达上限!";
////                                    }
////                                }
////                            }
////                        } else {
////                            resultDesc = "用户客服信息为空";
////                        }
////                    } else {
////                        resultDesc = "用户权限信息为空";
////                    }
////                } else {
////                    resultDesc = "用户信息为空";
////                }
////                sendMsgNotice(id_CCus,resultDesc, null,id_O,500,id_Us);
////                return retResult.ok(CodeEnum.OK.getCode(), "0");
////
////                User user = qt.getMDContent(id_U, "rolex", User.class);
////                int cusUser = getCusUser(user, id_O);
////                if (cusUser != 0) {
////                    result.put("errType",2);
////                    result.put("type",cusUser);
////                    result.put("desc","用户异常");
////                    sendMsgNotice(id_CCus,"用户异常:异常码="+cusUser, null,id_O,500,id_Us);
////                    return retResult.ok(CodeEnum.OK.getCode(), result);
////                }
//
//
//                Integer cusFoUp = indexInfo.getInteger("cusFoUp");
//                if (cusFoUp <= 0) {
//                    result.put("errType",2);
//                    result.put("type",2);
//                    result.put("desc","回访次数已达上限!");
//                    sendMsgNotice(id_CCus,"回访次数已达上限!", null,id_O,500,id_Us,index);
//                    return retResult.ok(CodeEnum.OK.getCode(), result);
//                }
//                cusFoUp--;
//                qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".cusFoUp",cusFoUp), Order.class);
//                sendMsgOneNew(id_U,logFlow);
//
//                result.put("type",2);
//                result.put("desc","回访客户");
//            } else {
////                User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
////                if (null != user && null != user.getRolex()) {
////                    JSONObject rolex = user.getRolex();
////                    JSONObject cus = rolex.getJSONObject("cus");
////                    if (null != cus) {
////                        JSONObject cusUser = cus.getJSONObject(id_CCus);
////                        if (null != cusUser) {
////                            int state = cusUser.getInteger("state");
////                            if (state == 0) {
////                                resultDesc = "用户已拒收!";
////                            } else {
////                                sendMsgOneNew(id_uThis,logFlow);
////                                sendMsgOneNew(id_uPointTo,logFlow);
////                                return retResult.ok(CodeEnum.OK.getCode(), "1");
////                            }
////                        } else {
////                            resultDesc = "该用户客服信息无本公司!";
////                        }
////                    } else {
////                        resultDesc = "该用户客服信息异常!";
////                    }
////                } else {
////                    resultDesc = "该用户信息为空!";
////                }
////                sendMsgNotice(id_CCus,resultDesc,null, id_O,500,id_Us);
//
//                sendMsgOneNew(id_U,logFlow);
//                result.put("type",3);
//                result.put("desc","发送消息");
//            }
//            result.put("errType",0);
//
//        }
//        return retResult.ok(CodeEnum.OK.getCode(), result);
//    }
//
//    /**
//     * 客服操作api
//     * @param id_CCus	公司编号
//     * @param id_UCus	客服、负责人编号
//     * @param id_O	订单编号
//     * @param index	订单下标
//     * @param bcdStatus	操作状态
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/4/24
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse cusOperate(String id_CCus, String id_UCus, String id_O, int index, int bcdStatus) {
//        String resultDesc;
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        JSONArray id_Us = new JSONArray();
//        id_Us.add(id_UCus);
//        if (null != order) {
//            JSONObject cusmsg = order.getCusmsg();
//            if (null != cusmsg) {
//                JSONArray objCusMsg = cusmsg.getJSONArray("objCusMsg");
//                if (null != objCusMsg) {
//                    if (bcdStatus == 51) {
//                        JSONObject objCusSon = objCusMsg.getJSONObject(index);
//                        String id_UCusNew = objCusSon.getString("id_UCus");
//                        if (null != id_UCusNew) {
//                            resultDesc = "已经被接受！";
//                            sendMsgNotice(id_CCus,resultDesc,null,id_O,500,id_Us,-1);
//                            return retResult.ok(CodeEnum.OK.getCode(), "0");
//                        }
//                        String id_U = objCusSon.getString("id_U");
//                        objCusSon.put("id_UCus",id_UCus);
//                        objCusSon.put("id_UCusImg","222.jpg");
//                        objCusSon.put("bcdStatus",bcdStatus);
//                        qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index, objCusSon), Order.class);
//
//                        qt.setMDContent(id_O,qt.setJson("cusmsg.cus."+id_UCus+"."+(index+""), 0), Order.class);
//                        id_Us.add(id_U);
//                        sendMsgNotice(id_CCus,"已接受",id_UCus,id_O,51,id_Us,index);
//                        return retResult.ok(CodeEnum.OK.getCode(), "1");
//                    } else if (bcdStatus == 52) {
//                        JSONObject objCusSon = objCusMsg.getJSONObject(index);
//                        String id_U = objCusSon.getString("id_U");
//                        qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+index+".bcdStatus", bcdStatus), Order.class);
//                        qt.setMDContent(id_O,qt.setJson("cusmsg.cus."+id_UCus+"."+(index+""), 1), Order.class);
//                        qt.setMDContent(id_O,qt.setJson("cusmsg.cusFoUp."+(index+""), 0), Order.class);
//                        id_Us.add(id_U);
//                        sendMsgNotice(id_CCus,"已完成",id_UCus,id_O,52,id_Us,index);
//                        return retResult.ok(CodeEnum.OK.getCode(), "2");
//                    } else {
//                        resultDesc = "不识别状态！";
//                    }
//                } else {
//                    resultDesc = "订单客服内信息为空！";
//                }
//            } else {
//                resultDesc = "订单客服信息为空！";
//            }
//        } else {
//            resultDesc = "订单信息为空！";
//        }
//        sendMsgNotice(id_CCus,resultDesc,null,id_O,500,id_Us,-1);
//        return retResult.ok(CodeEnum.OK.getCode(), "0");
//    }
//
//    @Override
//    public ApiResponse restoreCusLog(String id_O, String id_CCus,Integer index) {
//        JSONArray result = qt.getES("cusmsg", qt.setESFilt("id_C",id_CCus,"id_O",id_O,"index",index));
//        return retResult.ok(CodeEnum.OK.getCode(), result);
//    }
//
//    /**
//     * 根据id_C获取公司的聊天群信息
//     * @param id_C	公司编号
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse getLogList(String id_C) {
//        JSONObject result = new JSONObject();
//        Asset asset = qt.getConfig(id_C,"a-auth","flowControl");
//        if (null != asset && null != asset.getFlowControl()) {
//            JSONObject flowControl = asset.getFlowControl();
//            JSONArray objData = flowControl.getJSONArray("objData");
//            if (null != objData) {
//                result.put("isOk",1);
//                result.put("logList",objData);
//                return retResult.ok("200",result);
//            } else {
//                result.put("err","该公司权限内日志为空!");
//                result.put("isOk",0);
//            }
//        } else {
//            result.put("err","该公司权限为空!");
//            result.put("isOk",0);
//        }
//        return retResult.error("500",result);
//    }
//
//    /**
//     * 更新修改群关联信息
//     * @param id_C	公司编号
//     * @param logId	群编号
//     * @param glId	关联信息
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse updateLogListGl(String id_C, String logId, JSONArray glId) {
//        JSONObject result = new JSONObject();
//        Asset asset = qt.getConfig(id_C,"a-auth","flowControl");
//        if (null != asset && null != asset.getFlowControl()) {
//            JSONObject flowControl = asset.getFlowControl();
//            JSONArray objData = flowControl.getJSONArray("objData");
//            if (null != objData) {
//                // 存储控制结束外层循环
//                boolean isEndGlId = false;
//                // 存储关联的信息数量
//                int jiGlId = glId.size();
//                String id_O = "";
//                for (int i = 0; i < objData.size(); i++) {
//                    JSONObject dataSon = objData.getJSONObject(i);
//                    String id = dataSon.getString("id");
//                    // 判断当前日志编号等于，关联的主日志编号
//                    if (id.equals(logId)) {
//                        dataSon.put("glId",glId);
//                        id_O = dataSon.getString("id_O");
//                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.objData."+i,dataSon), Asset.class);
//                        break;
//                    }
//                }
//                for (int i = 0; i < objData.size(); i++) {
//                    JSONObject dataSon = objData.getJSONObject(i);
//                    String id = dataSon.getString("id");
//                    for (int j = 0; j < glId.size(); j++) {
//                        String glIdSon = glId.getString(j);
//                        // 判断关联日志编号，等于当前被关联的日志编号
//                        if (id.equals(glIdSon)) {
//                            JSONArray array = new JSONArray();
//                            array.add(logId);
//                            dataSon.put("glId",array);
//                            dataSon.put("id_O",id_O);
//                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.objData."+i,dataSon), Asset.class);
//                            // 被关联的日志总数累减
//                            jiGlId--;
//                            // 为0则结束外循环
//                            if (jiGlId == 0) {
//                                isEndGlId = true;
//                            }
//                            break;
//                        }
//                    }
//                    if (isEndGlId) {
//                        break;
//                    }
//                }
//
//                result.put("isOk",1);
//                result.put("desc","成功");
//                return retResult.ok("200",result);
//            } else {
//                result.put("err","该公司权限内日志为空!");
//                result.put("isOk",0);
//            }
//        } else {
//            result.put("err","该公司权限为空!");
//            result.put("isOk",0);
//        }
//        return retResult.error("500",result);
//    }
//
//    /**
//     * 获取公司的日志权限信息
//     * @param id_C	公司编号
//     * @param grpUW	外层组别
//     * @param grpUN	内层组别
//     * @param type	获取类型
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse getLogAuth(String id_C, String grpUW, String grpUN,String type) {
//        JSONObject result = new JSONObject();
//        Asset asset = qt.getConfig(id_C,"a-auth","role");
//        if (null != asset && null != asset.getRole()) {
//            JSONObject role = asset.getRole();
//            JSONObject objData = role.getJSONObject("objData");
//            if (null != objData) {
//                JSONObject grpUWRole = objData.getJSONObject(grpUW);
//                if (null != grpUWRole) {
//                    JSONObject typeRole = grpUWRole.getJSONObject(type);
//                    if (null != typeRole) {
//                        JSONObject grpUNRole = typeRole.getJSONObject(grpUN);
//                        if (null != grpUNRole) {
//                            JSONObject log = grpUNRole.getJSONObject("log");
//                            if (null !=log) {
//                                result.put("isOk",1);
//                                result.put("data",log);
//                                System.out.println("返回结果:");
//                                System.out.println(JSON.toJSONString(log));
//                                return retResult.ok("200",result);
//                            } else {
//                                result.put("err","该公司权限内日志log为空!");
//                                result.put("isOk",0);
//                            }
//                        } else {
//                            result.put("err","该公司权限内日志grpUN为空!");
//                            result.put("isOk",0);
//                        }
//                    } else {
//                        result.put("err","该公司权限内日志您选择的type为空!");
//                        result.put("isOk",0);
//                    }
//                } else {
//                    result.put("err","该公司权限内日志grpUW为空!");
//                    result.put("isOk",0);
//                }
//            } else {
//                result.put("err","该公司权限内日志为空!");
//                result.put("isOk",0);
//            }
//        } else {
//            result.put("err","该公司权限为空!");
//            result.put("isOk",0);
//        }
//        return retResult.error("500",result);
//    }
//
//    @Override
//    public ApiResponse renewCusUser(String id_C, JSONArray indexS, JSONArray ids, Integer type) {
//        Asset asset = qt.getConfig(id_C,"a-auth","flowControl");
//        JSONObject result = new JSONObject();
//        String resultDesc;
//        if (null != asset) {
//            JSONObject flowControl = asset.getFlowControl();
//            if (null != flowControl) {
//                JSONArray objData = flowControl.getJSONArray("objData");
//                if (null != objData) {
//                    if (type == 1) {
//                        JSONArray errOrder = new JSONArray();
//                        for (int i = 0; i < objData.size(); i++) {
//                            JSONObject object = objData.getJSONObject(i);
//                            String logType = object.getString("type");
//                            if ("cusmsg".equals(logType)) {
//                                String id_O = object.getString("id_O");
//                                JSONArray objUser = object.getJSONArray("objUser");
//                                JSONObject renewOrder = renewOrder(objUser, id_O);
//                                if (renewOrder.getInteger("errType") == 1) {
//                                    errOrder.add(renewOrder);
//                                }
//                            }
//                        }
//                        result.put("errType",0);
//                        result.put("desc","操作成功");
//                        result.put("errOrder",errOrder);
//                        return retResult.ok(CodeEnum.OK.getCode(), result);
//                    } else if (type == 2) {
//                        JSONArray errOrder = new JSONArray();
//                        for (int i = 0; i < indexS.size(); i++) {
//                            Integer index = indexS.getInteger(i);
//                            JSONObject object = objData.getJSONObject(index);
//                            String logType = object.getString("type");
//                            if ("cusmsg".equals(logType)) {
//                                String id_O = object.getString("id_O");
//                                JSONArray objUser = object.getJSONArray("objUser");
//                                JSONObject renewOrder = renewOrder(objUser, id_O);
//                                if (renewOrder.getInteger("errType") == 1) {
//                                    errOrder.add(renewOrder);
//                                }
//                            }
//                        }
//                        result.put("errType",0);
//                        result.put("desc","操作成功");
//                        result.put("errOrder",errOrder);
//                        return retResult.ok(CodeEnum.OK.getCode(), result);
//                    } else if (type == 3) {
//                        JSONArray errOrder = new JSONArray();
//                        for (int i = 0; i < objData.size(); i++) {
//                            JSONObject object = objData.getJSONObject(i);
//                            String logType = object.getString("type");
//                            String idNew = object.getString("id");
//                            int removeIndex = -1;
//                            for (int j = 0; j < ids.size(); j++) {
//                                String id = ids.getString(j);
//                                if (idNew.equals(id)) {
//                                    if ("cusmsg".equals(logType)) {
//                                        String id_O = object.getString("id_O");
//                                        JSONArray objUser = object.getJSONArray("objUser");
//                                        JSONObject renewOrder = renewOrder(objUser, id_O);
//                                        if (renewOrder.getInteger("errType") == 1) {
//                                            errOrder.add(renewOrder);
//                                        }
//                                    }
//                                    removeIndex = j;
//                                    break;
//                                }
//                            }
//                            if (removeIndex > 0) {
//                                ids.remove(removeIndex);
//                            }
//                        }
//                        result.put("errType",0);
//                        result.put("desc","操作成功");
//                        result.put("errOrder",errOrder);
//                        return retResult.ok(CodeEnum.OK.getCode(), result);
//                    }
//                    result.put("errType",2);
//                    result.put("desc","不识别类型");
//                    return retResult.ok(CodeEnum.OK.getCode(), result);
//                } else {
//                    resultDesc = "资产群信息为空";
//                }
//            } else {
//                resultDesc = "资产消息信息为空";
//            }
//        } else {
//            resultDesc = "资产信息为空";
//        }
//        result.put("errType",1);
//        result.put("desc","资产出现问题:"+resultDesc);
//        return retResult.ok(CodeEnum.OK.getCode(), result);
//    }
//
//    /**
//     * 创建客服请求方法
//     * @param id_CCus	客服公司编号
//     * @param id_U	用户编号
//     * @param id_O	日志订单编号
//     * @return 返回结果: {@link ApiResponse}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    @Override
//    public ApiResponse createCus(String id_CCus,String id_U,String id_O){
//        String resultDesc;
//        JSONArray id_Us = new JSONArray();
//        id_Us.add(id_U);
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        if (null != order) {
//            JSONObject cusmsg = order.getCusmsg();
//            if (null != cusmsg) {
//                JSONArray objCusMsg = cusmsg.getJSONArray("objCusMsg");
//                if (null != objCusMsg) {
//                    OrderCusmsg orderCusmsg = new OrderCusmsg();
//                    orderCusmsg.setId_U(id_U);
//                    orderCusmsg.setId_O(id_O);
//                    orderCusmsg.setBcdStatus(50);
//                    orderCusmsg.setIndex(objCusMsg.size());
//                    orderCusmsg.setId_UImg("111.jpg");
//                    orderCusmsg.setCusFoUp(3);
////                    orderCusmsg.setIsRejection(0);
//                    orderCusmsg.setScore(0);
//
//                    qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg."+orderCusmsg.getIndex()
//                            , JSONObject.parseObject(JSON.toJSONString(orderCusmsg))), Order.class);
//
//                    qt.setMDContent(id_U,qt.setJson("rolex.cus."+id_O+"."
//                            +orderCusmsg.getIndex().toString(),0), User.class);
////                            sendMsgNotice(id_U,id_CCus,"客服请求发起成功",id_U,id_O);
////                            cus.keySet().forEach(id_UCus -> sendMsgNotice(id_UCus,id_CCus
////                                    ,"顾客"+id_U+"需要服务!",id_U,id_O));
//                    JSONObject cus = cusmsg.getJSONObject("cus");
//                    id_Us.addAll(cus.keySet());
//                    sendMsgNotice(id_CCus,"客服请求发起成功",id_U,id_O,50,id_Us,orderCusmsg.getIndex());
//                    return retResult.ok(CodeEnum.OK.getCode(), "1");
//                } else {
//                    resultDesc = "订单客服内信息为空！";
//                }
//            } else {
//                resultDesc = "订单客服信息为空！";
//            }
//        } else {
//            resultDesc = "订单信息为空！";
//        }
//        sendMsgNotice(id_CCus,resultDesc,id_U,id_O,500,id_Us,-1);
//        return retResult.ok(CodeEnum.OK.getCode(), "0");
//    }
//
//    /**
//     * 发送通知日志方法
//     * @param id_CCus	公司编号
//     * @param desc	消息内容
//     * @param logUser	发送用户编号
//     * @param id_O	日志订单编号
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    public void sendMsgNotice(String id_CCus,String desc,String logUser
//            ,String id_O,int bcdStatus,JSONArray id_Us,Integer index){
//        JSONObject dataNew = new JSONObject();
//        dataNew.put("id_CCus",id_CCus);
//        dataNew.put("id_UPointTo",id_Us);
//        dataNew.put("bcdStatus",bcdStatus);
//        LogFlow logFlow = getNullLogFlow("cusmsg","notice"
//                ,desc,id_CCus,logUser,dataNew,id_O,index);
////        JSONArray id_Us = new JSONArray();
////        id_Us.add(logUser);
////        id_Us.add(sendUser);
//        logFlow.setId_Us(id_Us);
//        ws.sendWSNew(logFlow,0);
//    }
//
////    /**
////     * 发送日志到rocketMQ方法
////     * @param logFlow	日志信息
////     * @author tang
////     * @date 创建时间: 2023/5/29
////     * @ver 版本号: 1.0.0
////     */
////    public void sendMsgOne(LogFlow logFlow){
////        ws.sendWSNew(logFlow);
////    }
//
//    /**
//     * 发送日志信息到指定的id_U方法
//     * @param sendUser	指定的id_U（用户编号）
//     * @param logFlow	日志信息
//     * @author tang
//     * @date 创建时间: 2023/5/30
//     * @ver 版本号: 1.0.0
//     */
//    public void sendMsgOneNew(String sendUser,LogFlow logFlow){
////        JSONObject data = logFlow.getData();
////        data.put("id_UPointTo",sendUser);
//        if (null != sendUser) {
//            logFlow.setId_Us(qt.setArray(sendUser, logFlow.getId_U()));
//        } else {
//            logFlow.setId_Us(qt.setArray(logFlow.getId_U()));
//        }
//        JSONObject data = logFlow.getData();
//        data.put("id_UPointTo",logFlow.getId_Us());
//        logFlow.setData(data);
////        logFlow.setData(data);
//        ws.sendWSNew(logFlow,0);
//    }
//
//    /**
//     * 获取清空并重新赋值的日志信息
//     * @param logType	日志类型
//     * @param subType	日志子类型
//     * @param desc	日志内容
//     * @param id_C	公司编号
//     * @param id_U	用户编号
//     * @param data	日志详细信息
//     * @param id_O	日志订单编号
//     * @return 返回结果: {@link LogFlow}
//     * @author tang
//     * @date 创建时间: 2023/5/29
//     * @ver 版本号: 1.0.0
//     */
//    private LogFlow getNullLogFlow(String logType,String subType,String desc,String id_C,String id_U
//            ,JSONObject data,String id_O,Integer index){
//        LogFlow logFlow = LogFlow.getInstance();
//        logFlow.setLogType(logType);
//        logFlow.setSubType(subType);
//        logFlow.setZcndesc(desc);
//        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
//        logFlow.setId_C(id_C);
//        logFlow.setId_U(id_U);
//        logFlow.setData(data);
//        logFlow.setId_O(id_O);
//        logFlow.setIndex(index);
//        return logFlow;
//    }
//
//    private int getCusOrder(Order order,String id_U,int type){
//        if (null != order) {
//            JSONObject cusmsg = order.getCusmsg();
//            if (null != cusmsg) {
//                if (type == 5) {
//                    return 0;
//                }
//                if (type == 1) {
//                    JSONArray objCusMsg = cusmsg.getJSONArray("objCusMsg");
//                    if (null != objCusMsg) {
//                        return 0;
//                    } else {
//                        return 3;
//                    }
//                } else if (type == 2) {
//                    JSONObject cus = cusmsg.getJSONObject("cus");
//                    if (null != cus) {
//                        JSONObject id_UInfo = cus.getJSONObject(id_U);
//                        if (null != id_UInfo) {
//                            return 0;
//                        } else {
//                            return 6;
//                        }
//                    } else {
//                        return 5;
//                    }
//                } else {
//                    JSONObject cusFoUp = cusmsg.getJSONObject("cusFoUp");
//                    if (null != cusFoUp) {
//                        return 0;
//                    } else {
//                        return 8;
//                    }
//                }
//            } else {
//                return 2;
//            }
//        } else {
//            return 1;
//        }
//    }
//
//    private int getCusUser(User user,String id_O){
//        if (null != user) {
//            JSONObject rolex = user.getRolex();
//            if (null != rolex) {
//                JSONObject cus = rolex.getJSONObject("cus");
//                if (null != cus) {
//                    JSONObject id_OInfo = cus.getJSONObject(id_O);
//                    if (null != id_OInfo) {
//                        return 0;
//                    } else {
//                        return 4;
//                    }
//                } else {
//                    return 3;
//                }
//            } else {
//                return 2;
//            }
//        } else {
//            return 1;
//        }
//    }
//
//    private JSONObject renewOrder(JSONArray objUser,String id_O){
//        JSONObject result = new JSONObject();
//        List<String> list = new ArrayList<>();
//        for (int j = 0; j < objUser.size(); j++) {
//            JSONObject jsonObject = objUser.getJSONObject(j);
//            list.add(jsonObject.getString("id_U"));
//        }
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        int cusOrder = getCusOrder(order, null, 5);
//        if (cusOrder != 0) {
//            result.put("errType",1);
//            result.put("type",cusOrder);
//            result.put("desc",id_O+"订单异常");
//            return result;
//        }
//        JSONObject cus = order.getCusmsg().getJSONObject("cus");
//        if (null == cus) {
//            cus = new JSONObject();
//        }
//        for (String s : list) {
//            JSONObject cusByS = cus.getJSONObject(s);
//            if (null == cusByS) {
//                cus.put(s,new JSONObject());
//            }
//        }
//        qt.setMDContent(id_O,qt.setJson("cusmsg.cus",cus), Order.class);
//        JSONObject cusFoUp = order.getCusmsg().getJSONObject("cusFoUp");
//        if (null == cusFoUp) {
//            qt.setMDContent(id_O,qt.setJson("cusmsg.cusFoUp",new JSONObject()), Order.class);
//        }
//        JSONArray objCusMsg = order.getCusmsg().getJSONArray("objCusMsg");
//        if (null == objCusMsg) {
//            qt.setMDContent(id_O,qt.setJson("cusmsg.objCusMsg",new JSONArray()), Order.class);
//        }
//        result.put("errType",0);
//        return result;
//    }
//}
