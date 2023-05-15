package com.cresign.action.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.action.service.CusService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.DateUtils;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.dbTools.Ws;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.pojo.po.Order;
import com.cresign.tools.pojo.po.User;
import com.cresign.tools.pojo.po.orderCard.OrderCusmsg;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;

/**
 * @author tang
 * @Description 作者很懒什么也没写
 * @ClassName CusServiceImpl
 * @Date 2023/4/22
 * @ver 1.0.0
 */
@Service
public class CusServiceImpl implements CusService {

    @Autowired
    private Qt qt;

    @Autowired
    private Ws ws;

    @Autowired
    private RetResult retResult;

//    @Override
//    public ApiResponse getCreateCus(String id_CCus,String id_U,String id_O) {
//        return createCus(id_CCus,id_U,id_O);
//    }

//    @Override
//    public ApiResponse sendUserCus(LogFlow logFlow) {
//        JSONObject data = logFlow.getData();
//        String subType = logFlow.getSubType();
//        boolean isHost = data.getBoolean("isHost");
//        String id_UCus = data.getString("id_UCus");
//        String id_CCus = logFlow.getId_C();
//        String id_uPointTo = data.getString("id_UPointTo");
//        String id_uThis = logFlow.getId_U();
//        String resultDesc;
//        String id_O = logFlow.getId_O();
//        if (isHost) {
//            if ("score".equals(subType)) {
//                Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//                if (null != asset) {
//                    JSONObject flowControl = asset.getFlowControl();
//                    if (null != flowControl) {
//                        JSONObject cusAsset = flowControl.getJSONObject("cus");
//                        if (null != cusAsset) {
//                            JSONObject id_uThisInfo = cusAsset.getJSONObject(id_uThis);
//                            JSONObject userAll = id_uThisInfo.getJSONObject("userAll");
//                            JSONObject id_uPo = userAll.getJSONObject(id_uPointTo);
//                            id_uPo.put("type",2);
//                            userAll.put(id_uPointTo,id_uPo);
//                            id_uThisInfo.put("userAll",userAll);
//                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_uThis,id_uThisInfo), Asset.class);
//                            JSONObject foUp = new JSONObject();
//                            foUp.put("img","111.jpg");
//                            foUp.put("type",1);
//                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.cusFoUp."+id_uPointTo,foUp), Asset.class);
//                            sendMsgOneNew(id_uPointTo,logFlow);
//                            sendMsgOneNew(id_uThis,logFlow);
//                            return retResult.ok(CodeEnum.OK.getCode(), "1");
//                        } else {
//                            resultDesc = "资产客服信息为空";
//                        }
//                    } else {
//                        resultDesc = "资产消息信息为空";
//                    }
//                } else {
//                    resultDesc = "资产信息为空";
//                }
//                sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
//                return retResult.ok(CodeEnum.OK.getCode(), "0");
//            } else if ("foUp".equals(subType)) {
//                User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
//                if (null != user) {
//                    JSONObject rolex = user.getRolex();
//                    if (null != rolex) {
//                        JSONObject cus = rolex.getJSONObject("cus");
//                        if (null != cus) {
//                            JSONObject cusUser = cus.getJSONObject(id_CCus);
//                            if (null == cusUser) {
//                                resultDesc = "无法回访该用户!";
//                            } else {
//                                int state = cusUser.getInteger("state");
//                                if (state == 0) {
//                                    resultDesc = "用户已拒收!";
//                                } else {
//                                    int cusFoUp = cusUser.getInteger("cusFoUp");
//                                    if (cusFoUp > 0) {
//                                        sendMsgOneNew(id_uThis,logFlow);
//                                        sendMsgOneNew(id_uPointTo,logFlow);
//                                        cusFoUp--;
//                                        cusUser.put("cusFoUp",cusFoUp);
//                                        qt.setMDContent(id_uPointTo,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//                                        return retResult.ok(CodeEnum.OK.getCode(), "1");
//                                    } else {
//                                        resultDesc = "回访次数已达上限!";
//                                    }
//                                }
//                            }
//                        } else {
//                            resultDesc = "用户客服信息为空";
//                        }
//                    } else {
//                        resultDesc = "用户权限信息为空";
//                    }
//                } else {
//                    resultDesc = "用户信息为空";
//                }
//                sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
//                return retResult.ok(CodeEnum.OK.getCode(), "0");
//            } else {
//                User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
//                if (null != user && null != user.getRolex()) {
//                    JSONObject rolex = user.getRolex();
//                    JSONObject cus = rolex.getJSONObject("cus");
//                    if (null != cus) {
//                        JSONObject cusUser = cus.getJSONObject(id_CCus);
//                        if (null != cusUser) {
//                            int state = cusUser.getInteger("state");
//                            if (state == 0) {
//                                resultDesc = "用户已拒收!";
//                            } else {
//                                sendMsgOneNew(id_uThis,logFlow);
//                                sendMsgOneNew(id_uPointTo,logFlow);
//                                return retResult.ok(CodeEnum.OK.getCode(), "1");
//                            }
//                        } else {
//                            resultDesc = "该用户客服信息无本公司!";
//                        }
//                    } else {
//                        resultDesc = "该用户客服信息异常!";
//                    }
//                } else {
//                    resultDesc = "该用户信息为空!";
//                }
//                sendMsgNotice(id_uThis,id_CCus,resultDesc,null, id_O);
//            }
//        } else {
//            if (null != id_UCus && !"".equals(id_UCus)) {
//                if ("rejection".equals(subType)) {
//                    JSONObject cusUser = new JSONObject();
//                    cusUser.put("state",0);
//                    cusUser.put("id_UCus",null);
//                    cusUser.put("type",null);
//                    cusUser.put("cusFoUp",0);
//                    qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//                    sendMsgOneNew(id_uThis,logFlow);
//                    sendMsgNotice(id_uThis,id_CCus,"已拒收该公司消息",id_uThis,id_O);
//                    return retResult.ok(CodeEnum.OK.getCode(), "1");
//                } else if ("del".equals(subType)) {
//                    User user = qt.getMDContent(id_uThis, "rolex", User.class);
//                    if (null != user) {
//                        JSONObject rolex = user.getRolex();
//                        if (null != rolex) {
//                            JSONObject cus = rolex.getJSONObject("cus");
//                            if (null != cus) {
//                                cus.remove(id_CCus);
//                                qt.setMDContent(id_uThis,qt.setJson("rolex.cus",cus), User.class);
//                                sendMsgOneNew(id_uThis,logFlow);
//                                sendMsgNotice(id_uThis,id_CCus,"已删除该公司信息",id_uThis,id_O);
//                                return retResult.ok(CodeEnum.OK.getCode(), "1");
//                            } else {
//                                resultDesc = "用户客服信息为空";
//                            }
//                        } else {
//                            resultDesc = "用户权限信息为空";
//                        }
//                    } else {
//                        resultDesc = "用户信息为空";
//                    }
//                    sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
//                    return retResult.ok(CodeEnum.OK.getCode(), "0");
//                } else if ("score".equals(subType)) {
//                    User user = qt.getMDContent(id_uThis, "rolex", User.class);
//                    String id_uCusNew;
//                    String score;
//                    JSONObject cusUser;
//                    if (null != user) {
//                        JSONObject rolex = user.getRolex();
//                        if (null != rolex) {
//                            JSONObject cus = rolex.getJSONObject("cus");
//                            if (null != cus) {
//                                cusUser = cus.getJSONObject(id_CCus);
//                                id_uCusNew = cusUser.getString("id_UCus");
//                                score = data.getString("score");
//                            } else {
//                                sendMsgNotice(id_uThis,id_CCus,"用户客服信息为空", null,id_O);
//                                return retResult.ok(CodeEnum.OK.getCode(), "0");
//                            }
//                        } else {
//                            sendMsgNotice(id_uThis,id_CCus,"用户权限信息为空", null,id_O);
//                            return retResult.ok(CodeEnum.OK.getCode(), "0");
//                        }
//                    } else {
//                        sendMsgNotice(id_uThis,id_CCus,"用户信息为空", null,id_O);
//                        return retResult.ok(CodeEnum.OK.getCode(), "0");
//                    }
//
//                    Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//                    if (null != asset) {
//                        JSONObject flowControl = asset.getFlowControl();
//                        if (null != flowControl) {
//                            JSONObject cusAsset = flowControl.getJSONObject("cus");
//                            JSONObject id_uCusInfo = cusAsset.getJSONObject(id_uCusNew);
//                            int scoreCus = id_uCusInfo.getInteger(score);
//                            scoreCus++;
//                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_uCusNew+".score."+score,scoreCus), Asset.class);
//                            cusUser.put("id_UCus","");
//                            cusUser.put("cusFoUp",cusUser.getInteger("cusFoUp"));
//                            cusUser.put("state",cusUser.getInteger("state"));
//                            qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
//                            sendMsgOneNew(id_uThis,logFlow);
//                            sendMsgOneNew(id_uCusNew,logFlow);
//                            return retResult.ok(CodeEnum.OK.getCode(), "1");
//                        } else {
//                            resultDesc = "资产消息信息为空";
//                        }
//                    } else {
//                        resultDesc = "资产信息为空";
//                    }
//                    sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
//                    return retResult.ok(CodeEnum.OK.getCode(), "0");
//                } else {
//                    sendMsgOneNew(id_uThis,logFlow);
//                    sendMsgOneNew(id_uPointTo,logFlow);
//                }
//            } else {
//                return createCus(id_CCus,id_uThis,id_O);
//            }
//        }
//        return retResult.ok(CodeEnum.OK.getCode(), "0");
//    }

//    @Override
//    public ApiResponse acceptCus(String id_CCus, String id_U, String id_O, int index) {
//        String resultDesc;
//        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
//        if (null != order) {
//            JSONObject cusmsg = order.getCusmsg();
//            if (null != cusmsg) {
//                JSONArray objCus = cusmsg.getJSONArray("objCus");
//                if (null != objCus) {
//                    JSONObject objCusSon = objCus.getJSONObject(index);
//                    String id_uPropose = objCusSon.getString("id_UPropose");
//                    objCusSon.put("id_U",id_U);
//                    objCusSon.put("bcdStatus",51);
//                    objCus.set(index,objCusSon);
//                    qt.setMDContent(id_O,qt.setJson("cusmsg.objCus", objCus), Order.class);
//
//                    Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
//                    if (null != asset && null != asset.getFlowControl()) {
//                        JSONObject flowControl = asset.getFlowControl();
//                        JSONObject cus = flowControl.getJSONObject("cus");
//                        if (null != cus) {
//                            JSONObject userAll = cus.getJSONObject("userAll");
//                            JSONObject id_uProInfo = new JSONObject();
//                            id_uProInfo.put("img","111.jpg");
//                            id_uProInfo.put("type",1);
//                            userAll.put(id_uPropose,id_uProInfo);
//                            qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_U+".userAll", userAll), Asset.class);
//
//                            sendMsgNotice(id_U,id_CCus,"已接受",id_U,id_O);
//                            sendMsgNotice(id_uPropose,id_CCus,"已被接受",id_U,id_O);
//                            return retResult.ok(CodeEnum.OK.getCode(), "1");
//                        } else {
//                            resultDesc = "该公司没有客服!";
//                        }
//                    } else {
//                        resultDesc = "该公司权限为空!";
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
//        sendMsgNotice(id_U,id_CCus,resultDesc,null,id_O);
//        return retResult.ok(CodeEnum.OK.getCode(), "0");
//    }

    @Override
    public ApiResponse sendUserCusCustomer(LogFlow logFlow) {
        JSONObject data = logFlow.getData();
        String subType = logFlow.getSubType();
//        boolean isHost = data.getBoolean("isHost");
        String id_UCus = data.getString("id_UCus");
        String id_CCus = logFlow.getId_C();
        String id_uPointTo = data.getString("id_UPointTo");
        String id_uThis = logFlow.getId_U();
        String resultDesc;
        String id_O = logFlow.getId_O();
        if (null != id_UCus && !"".equals(id_UCus)) {
            if ("rejection".equals(subType)) {
                JSONObject cusUser = new JSONObject();
                cusUser.put("state",0);
                cusUser.put("id_UCus",null);
                cusUser.put("cusFoUp",0);
                cusUser.put("id_O",id_O);
                cusUser.put("index",null);
                qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
                sendMsgOneNew(id_uThis,logFlow);
                sendMsgNotice(id_uThis,id_CCus,"已拒收该公司消息",id_uThis,id_O);
                return retResult.ok(CodeEnum.OK.getCode(), "2");
            } else if ("del".equals(subType)) {
                User user = qt.getMDContent(id_uThis, "rolex", User.class);
                if (null != user) {
                    JSONObject rolex = user.getRolex();
                    if (null != rolex) {
                        JSONObject cus = rolex.getJSONObject("cus");
                        if (null != cus) {
                            cus.remove(id_CCus);
                            qt.setMDContent(id_uThis,qt.setJson("rolex.cus",cus), User.class);
                            sendMsgOneNew(id_uThis,logFlow);
                            sendMsgNotice(id_uThis,id_CCus,"已删除该公司信息",id_uThis,id_O);
                            return retResult.ok(CodeEnum.OK.getCode(), "1");
                        } else {
                            resultDesc = "用户客服信息为空";
                        }
                    } else {
                        resultDesc = "用户权限信息为空";
                    }
                } else {
                    resultDesc = "用户信息为空";
                }
                sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
                return retResult.ok(CodeEnum.OK.getCode(), "3");
            } else if ("score".equals(subType)) {
                User user = qt.getMDContent(id_uThis, "rolex", User.class);
                String id_uCusNew;
                String score;
                JSONObject cusUser;
                if (null != user) {
                    JSONObject rolex = user.getRolex();
                    if (null != rolex) {
                        JSONObject cus = rolex.getJSONObject("cus");
                        if (null != cus) {
                            cusUser = cus.getJSONObject(id_CCus);
                            id_uCusNew = cusUser.getString("id_UCus");
                            score = data.getString("score");
                        } else {
                            sendMsgNotice(id_uThis,id_CCus,"用户客服信息为空", null,id_O);
                            return retResult.ok(CodeEnum.OK.getCode(), "0");
                        }
                    } else {
                        sendMsgNotice(id_uThis,id_CCus,"用户权限信息为空", null,id_O);
                        return retResult.ok(CodeEnum.OK.getCode(), "0");
                    }
                } else {
                    sendMsgNotice(id_uThis,id_CCus,"用户信息为空", null,id_O);
                    return retResult.ok(CodeEnum.OK.getCode(), "0");
                }

                Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
                if (null != asset) {
                    JSONObject flowControl = asset.getFlowControl();
                    if (null != flowControl) {
                        JSONObject cusAsset = flowControl.getJSONObject("cus");
                        JSONObject id_uCusInfo = cusAsset.getJSONObject(id_uCusNew);
                        int scoreCus = id_uCusInfo.getInteger(score);
                        scoreCus++;
                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_uCusNew+".score."+score,scoreCus), Asset.class);
                        cusUser.put("id_UCus","");
                        cusUser.put("cusFoUp",cusUser.getInteger("cusFoUp"));
                        cusUser.put("state",cusUser.getInteger("state"));
                        qt.setMDContent(id_uThis,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
                        sendMsgOneNew(id_uThis,logFlow);
                        sendMsgOneNew(id_uCusNew,logFlow);
                        return retResult.ok(CodeEnum.OK.getCode(), "1");
                    } else {
                        resultDesc = "资产消息信息为空";
                    }
                } else {
                    resultDesc = "资产信息为空";
                }
                sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
                return retResult.ok(CodeEnum.OK.getCode(), "4");
            } else {
                sendMsgOneNew(id_uThis,logFlow);
                sendMsgOneNew(id_uPointTo,logFlow);
                return retResult.ok(CodeEnum.OK.getCode(), "1");
            }
        } else {
            return createCus(id_CCus,id_uThis,id_O);
        }
    }

    @Override
    public ApiResponse sendUserCusService(LogFlow logFlow) {
        JSONObject data = logFlow.getData();
        String subType = logFlow.getSubType();
//        boolean isHost = data.getBoolean("isHost");
//        String id_UCus = data.getString("id_UCus");
        String id_CCus = logFlow.getId_C();
        String id_uPointTo = data.getString("id_UPointTo");
        String id_uThis = logFlow.getId_U();
        String resultDesc;
        String id_O = logFlow.getId_O();
        if ("score".equals(subType)) {
            Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
            if (null != asset) {
                JSONObject flowControl = asset.getFlowControl();
                if (null != flowControl) {
                    JSONObject cusAsset = flowControl.getJSONObject("cus");
                    if (null != cusAsset) {
                        JSONObject id_uThisInfo = cusAsset.getJSONObject(id_uThis);
                        JSONObject userAll = id_uThisInfo.getJSONObject("userAll");
                        JSONObject id_uPo = userAll.getJSONObject(id_uPointTo);
                        id_uPo.put("type",2);
                        userAll.put(id_uPointTo,id_uPo);
                        id_uThisInfo.put("userAll",userAll);
                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_uThis,id_uThisInfo), Asset.class);
                        JSONObject foUp = new JSONObject();
                        foUp.put("img","111.jpg");
                        foUp.put("type",1);
                        qt.setMDContent(asset.getId(),qt.setJson("flowControl.cusFoUp."+id_uPointTo,foUp), Asset.class);
                        sendMsgOneNew(id_uPointTo,logFlow);
                        sendMsgOneNew(id_uThis,logFlow);
                        return retResult.ok(CodeEnum.OK.getCode(), "2");
                    } else {
                        resultDesc = "资产客服信息为空";
                    }
                } else {
                    resultDesc = "资产消息信息为空";
                }
            } else {
                resultDesc = "资产信息为空";
            }
            sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
            return retResult.ok(CodeEnum.OK.getCode(), "0");
        } else if ("foUp".equals(subType)) {
            User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
            if (null != user) {
                JSONObject rolex = user.getRolex();
                if (null != rolex) {
                    JSONObject cus = rolex.getJSONObject("cus");
                    if (null != cus) {
                        JSONObject cusUser = cus.getJSONObject(id_CCus);
                        if (null == cusUser) {
                            resultDesc = "无法回访该用户!";
                        } else {
                            int state = cusUser.getInteger("state");
                            if (state == 0) {
                                resultDesc = "用户已拒收!";
                            } else {
                                int cusFoUp = cusUser.getInteger("cusFoUp");
                                if (cusFoUp > 0) {
                                    sendMsgOneNew(id_uThis,logFlow);
                                    sendMsgOneNew(id_uPointTo,logFlow);
                                    cusFoUp--;
                                    cusUser.put("cusFoUp",cusFoUp);
                                    qt.setMDContent(id_uPointTo,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
                                    return retResult.ok(CodeEnum.OK.getCode(), "3");
                                } else {
                                    resultDesc = "回访次数已达上限!";
                                }
                            }
                        }
                    } else {
                        resultDesc = "用户客服信息为空";
                    }
                } else {
                    resultDesc = "用户权限信息为空";
                }
            } else {
                resultDesc = "用户信息为空";
            }
            sendMsgNotice(id_uThis,id_CCus,resultDesc, null,id_O);
            return retResult.ok(CodeEnum.OK.getCode(), "0");
        } else {
            User user = qt.getMDContent(id_uPointTo, "rolex", User.class);
            if (null != user && null != user.getRolex()) {
                JSONObject rolex = user.getRolex();
                JSONObject cus = rolex.getJSONObject("cus");
                if (null != cus) {
                    JSONObject cusUser = cus.getJSONObject(id_CCus);
                    if (null != cusUser) {
                        int state = cusUser.getInteger("state");
                        if (state == 0) {
                            resultDesc = "用户已拒收!";
                        } else {
                            sendMsgOneNew(id_uThis,logFlow);
                            sendMsgOneNew(id_uPointTo,logFlow);
                            return retResult.ok(CodeEnum.OK.getCode(), "1");
                        }
                    } else {
                        resultDesc = "该用户客服信息无本公司!";
                    }
                } else {
                    resultDesc = "该用户客服信息异常!";
                }
            } else {
                resultDesc = "该用户信息为空!";
            }
            sendMsgNotice(id_uThis,id_CCus,resultDesc,null, id_O);
        }
        return retResult.ok(CodeEnum.OK.getCode(), "0");
    }

    @Override
    public ApiResponse cusOperate(String id_CCus, String id_U, String id_O, int index, int bcdStatus) {
        String resultDesc = null;
        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
        if (null != order) {
            JSONObject cusmsg = order.getCusmsg();
            if (null != cusmsg) {
                JSONArray objCus = cusmsg.getJSONArray("objCus");
                if (null != objCus) {
                    if (bcdStatus == 51) {
                        JSONObject objCusSon = objCus.getJSONObject(index);
                        String id_uPropose = objCusSon.getString("id_UPropose");
                        objCusSon.put("id_U",id_U);
                        objCusSon.put("bcdStatus",bcdStatus);
                        objCus.set(index,objCusSon);
                        qt.setMDContent(id_O,qt.setJson("cusmsg.objCus", objCus), Order.class);

                        Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
                        if (null != asset && null != asset.getFlowControl()) {
                            JSONObject flowControl = asset.getFlowControl();
                            JSONObject cus = flowControl.getJSONObject("cus");
                            if (null != cus) {
                                JSONObject userAll = cus.getJSONObject("userAll");
                                JSONObject id_uProInfo = new JSONObject();
                                id_uProInfo.put("img","111.jpg");
                                id_uProInfo.put("type",1);
                                userAll.put(id_uPropose,id_uProInfo);
                                qt.setMDContent(asset.getId(),qt.setJson("flowControl.cus."+id_U+".userAll", userAll), Asset.class);

                                User user = qt.getMDContent(id_uPropose, "rolex", User.class);
                                boolean isUserOk = false;
                                JSONObject cusUser = null;
                                if (null != user) {
                                    JSONObject rolex = user.getRolex();
                                    if (null != rolex) {
                                        cusUser = rolex.getJSONObject("cus");
                                        if (null != cusUser) {
                                            isUserOk = true;
                                        } else {
                                            resultDesc = "用户客服信息为空";
                                        }
                                    } else {
                                        resultDesc = "用户权限信息为空";
                                    }
                                } else {
                                    resultDesc = "用户信息为空";
                                }
                                if (!isUserOk) {
                                    sendMsgNotice(id_U,id_CCus,resultDesc, null,id_O);
                                    return retResult.ok(CodeEnum.OK.getCode(), "0");
                                }
                                cusUser.put("id_UCus",id_U);
                                cusUser.put("cusFoUp",3);
                                qt.setMDContent(id_uPropose,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);

                                sendMsgNotice(id_U,id_CCus,"已接受",id_U,id_O);
                                sendMsgNotice(id_uPropose,id_CCus,"已被接受",id_U,id_O);
                                return retResult.ok(CodeEnum.OK.getCode(), "1");
                            } else {
                                resultDesc = "该公司没有客服!";
                            }
                        } else {
                            resultDesc = "该公司权限为空!";
                        }
                    } else if (bcdStatus == 52) {
                        JSONObject objCusSon = objCus.getJSONObject(index);
                        String id_uPropose = objCusSon.getString("id_UPropose");
                        objCusSon.put("id_U",id_U);
                        objCusSon.put("bcdStatus",bcdStatus);
                        objCus.set(index,objCusSon);
                        qt.setMDContent(id_O,qt.setJson("cusmsg.objCus", objCus), Order.class);

                        sendMsgNotice(id_U,id_CCus,"已完成",id_U,id_O);
                        sendMsgNotice(id_uPropose,id_CCus,"已被完成",id_U,id_O);
                        return retResult.ok(CodeEnum.OK.getCode(), "2");
                    } else {
                        resultDesc = "不识别状态！";
                    }
                } else {
                    resultDesc = "订单客服内信息为空！";
                }
            } else {
                resultDesc = "订单客服信息为空！";
            }
        } else {
            resultDesc = "订单信息为空！";
        }
        sendMsgNotice(id_U,id_CCus,resultDesc,null,id_O);
        return retResult.ok(CodeEnum.OK.getCode(), "0");
    }

    @Override
    public ApiResponse restoreCusLog(String id_O, String id_CCus) {
        JSONArray result = qt.getES("cusmsg", qt.setESFilt("id_C",id_CCus,"id_O",id_O));
        return null;
    }

    public ApiResponse createCus(String id_CCus,String id_U,String id_O){
        String resultDesc;
        Order order = qt.getMDContent(id_O, "cusmsg", Order.class);
        if (null != order) {
            JSONObject cusmsg = order.getCusmsg();
            if (null != cusmsg) {
                JSONArray objCus = cusmsg.getJSONArray("objCus");
                if (null != objCus) {
                    OrderCusmsg orderCusmsg = new OrderCusmsg();
                    orderCusmsg.setId_UPropose(id_U);
                    orderCusmsg.setId_O(id_O);
                    orderCusmsg.setBcdStatus(50);
                    orderCusmsg.setIndex(objCus.size());
                    objCus.add(JSONObject.parseObject(JSON.toJSONString(orderCusmsg)));
                    qt.setMDContent(id_O,qt.setJson("cusmsg.objCus", objCus), Order.class);
                    Asset asset = qt.getConfig(id_CCus,"a-auth","flowControl");
                    if (null != asset && null != asset.getFlowControl()) {
                        JSONObject flowControl = asset.getFlowControl();
                        JSONObject cus = flowControl.getJSONObject("cus");
                        if (null != cus) {
                            sendMsgNotice(id_U,id_CCus,"客服请求发起成功",id_U,id_O);
                            JSONObject cusUser = new JSONObject();
                            cusUser.put("state",1);
                            cusUser.put("id_UCus",null);
                            cusUser.put("cusFoUp",0);
                            cusUser.put("id_O",id_O);
                            cusUser.put("index",orderCusmsg.getIndex());
                            qt.setMDContent(id_U,qt.setJson("rolex.cus."+id_CCus,cusUser), User.class);
                            cus.keySet().forEach(id_UCus -> sendMsgNotice(id_UCus,id_CCus
                                    ,"顾客"+id_U+"需要服务!",id_U,id_O));
                            return retResult.ok(CodeEnum.OK.getCode(), "1");
                        } else {
                            resultDesc = "该公司没有客服!";
                        }
                    } else {
                        resultDesc = "该公司权限为空!";
                    }
                } else {
                    resultDesc = "订单客服内信息为空！";
                }
            } else {
                resultDesc = "订单客服信息为空！";
            }
        } else {
            resultDesc = "订单信息为空！";
        }
        sendMsgNotice(id_U,id_CCus,resultDesc,null,id_O);
        return retResult.ok(CodeEnum.OK.getCode(), "0");
    }

    public void sendMsgNotice(String sendUser,String id_CCus,String desc,String logUser,String id_O){
        JSONObject dataNew = new JSONObject();
        dataNew.put("id_CCus",id_CCus);
        dataNew.put("id_UPointTo",sendUser);
        LogFlow logFlow = getNullLogFlow("cusmsg","notice"
                ,desc,id_CCus,logUser,dataNew,id_O);
        sendMsgOne(logFlow);
    }
    public void sendMsgOne(LogFlow logFlow){
        ws.sendWS(logFlow);
    }
    public void sendMsgOneNew(String sendUser,LogFlow logFlow){
        JSONObject data = logFlow.getData();
        data.put("id_UPointTo",sendUser);
        logFlow.setData(data);
        ws.sendWS(logFlow);
    }
    private LogFlow getNullLogFlow(String logType,String subType,String desc,String id_C,String id_U
            ,JSONObject data,String id_O){
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setLogType(logType);
        logFlow.setSubType(subType);
        logFlow.setZcndesc(desc);
        logFlow.setTmd(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        logFlow.setId_C(id_C);
        logFlow.setId_U(id_U);
        logFlow.setData(data);
        logFlow.setId_O(id_O);
        return logFlow;
    }
}
