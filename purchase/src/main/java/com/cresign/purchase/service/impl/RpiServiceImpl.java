package com.cresign.purchase.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cresign.purchase.client.WSClient;
import com.cresign.purchase.common.ChatEnum;
import com.cresign.purchase.service.RpiService;
import com.cresign.tools.advice.RetResult;
import com.cresign.tools.apires.ApiResponse;
import com.cresign.tools.dbTools.Qt;
import com.cresign.tools.enumeration.CodeEnum;
import com.cresign.tools.exception.ErrorResponseException;
import com.cresign.tools.pojo.po.Asset;
import com.cresign.tools.pojo.po.LogFlow;
import com.cresign.tools.uuid.UUID19;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @ClassName RpiServiceImpl
 * @Description 作者很懒什么也没写
 * @authortang
 * @Date 2022/8/18
 * @ver 1.0.0
 */
@Service
public class RpiServiceImpl implements RpiService {

    /**
     * 树莓派gpio的redis存储前缀
     */
    public static final String PI_GPIO = "pi:gp_";
    public static final String PI_GPIO_H = "gp_";
    /**
     * 树莓派的redis存储前缀
     */
    public static final String PI = "pi:p_";
    public static final String PI_Q = "pi";
    public static final String PI_H = "p_";
    /**
     * 二维码前缀
     */
    public static final String RPI_URL_PREFIX = "https://www.cresign.cn/qrCodeTest?qrType=rpi&t=";

//    /**
//     * 注入redis数据库下标1模板
//     */
//    @Resource
//    private StringRedisTemplate redisTemplate0;

    @Resource
    private RetResult retResult;

    @Autowired
    private Qt qt;

//    @Autowired
//    private Ws ws;

    @Autowired
    private WSClient wsrpi;

    /**
     * 删除机器绑定公司接口
     * @param rname 树莓派id
     * @param id_C  公司编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    @Override
    public ApiResponse delPi(String rname, String id_C) {
//        String rname = can.getString("rname");
//        String id_C = can.getString("id_C");
        // 调用获取树莓派状态方法，并接收状态信息
        int status = piNameSta(rname, id_C);
        // 判断状态为正常，等于0就是正常
        if (status == 0) {
            // 调用获取公司的资产的树莓派信息方法
            JSONObject rpiNameData = getRNames(id_C,true);
            // 获取内部状态信息
            Integer statusInside = rpiNameData.getInteger("status");
            // 判断状态为正常
            if (statusInside == 0) {
                JSONObject rpi = rpiNameData.getJSONObject("rpi");
                JSONObject rnames = rpiNameData.getJSONObject("rnames");
                JSONObject pinfo = rpiNameData.getJSONObject("pinfo");
                String assetId = rpiNameData.getString("assetId");
                // 根据rname获取公司内的对应信息
                JSONObject rnameData = rnames.getJSONObject(rname);
                // 判断信息为空，抛出异常
                if (null == rnameData) {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_RPI_R_NAME_K.getCode(), "该公司rpi卡片没有对应的rname，请新增");
                }
                // 删除对应的ranme信息
                pinfo.remove(rname);
                rnames.remove(rname);
                // 更新信息
                rpi.put("rnames",rnames);
                rpi.put("pinfo",pinfo);

                // 更新数据库
                qt.setMDContent(assetId,qt.setJson("rpi",rpi), Asset.class);

                // 获取redis对应的rname信息
//                String rnameRedisDataStr = redisTemplate0.opsForValue().get(PI + rname);
                String rnameRedisDataStr = qt.getRDSetStr(PI_Q,PI_H+rname);
                // 将字符串转换成json对象
                JSONObject rnameRedisData = JSONObject.parseObject(rnameRedisDataStr);
                // 获取piSon字段信息,piSon = 所有gpio信息记录
                JSONObject piSon = rnameRedisData.getJSONObject("piSon");
                // 定义存储值集合
                Collection<String> keys = new ArrayList<>();
                // 遍历所有gpio信息记录的值信息
                piSon.values().forEach(v -> keys.add(PI_GPIO + v.toString()));
                // 根据值信息删除redis对应的gpio信息
//                redisTemplate0.delete(keys);
                qt.delRDByCollection(keys);
                // 情况rname的redis信息
                rnameRedisData.put("id_C","");
                rnameRedisData.put("piSon",new JSONObject());
//                redisTemplate0.opsForValue().set(PI + rname, JSON.toJSONString(rnameRedisData));
                qt.setRDSet(PI_Q,PI_H+rname,JSON.toJSONString(rnameRedisData),300L);
                return retResult.ok(CodeEnum.OK.getCode(), "删除公司绑定成功");
            } else {
                // 否则输出异常信息
                if (statusInside == 5) {
                    throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PI_X_NO.getCode(), "rpi卡片异常-rpi-基础信息为空");
                } else {
                    return errResult(statusInside);
                }
            }
        } else {
            // 为异常状态则，调用异常状态判断输出方法
            return errResultPi(status);
        }
//        String s = redisTemplate0.opsForValue().get(PI + rname);
//        if (null == s || "".equals(s)) {
//            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PI_X_K.getCode(), "机器信息为空，操作失败");
//        } else {
//            if (id_C.equals(s)) {
//                redisTemplate0.opsForValue().set(PI + rname,"");
//                return retResult.ok(CodeEnum.OK.getCode(), "机器解绑公司成功");
//            } else {
//                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PI_B_NO.getCode(), "该机器不属于你们公司，操作失败");
//            }
//        }
    }

    /**
     * 获取生成二维码数据api接口
     * @param rname 树莓派id
     * @param id_C  公司编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 16个gpio二维码数据
     * @author tang
     * @version 1.0.0
     * @date 2022/8/18
     */
    @Override
    public ApiResponse rpiCode(String rname,String id_C) {
        // 获取rname的redis信息
//        String rpiData = redisTemplate0.opsForValue().get(PI + rname);
        String rpiData = qt.getRDSetStr(PI_Q,PI_H+rname);
//        String id_C = can.getString("id_C");
        // 判断信息为空
        if (null == rpiData || "".equals(rpiData)) {
            // 调用方法
            return rpiCodeCore(id_C,rname);
        } else {
            JSONObject piDa = JSONObject.parseObject(rpiData);
            String id_C_pi = piDa.getString("id_C");
            if (null == id_C_pi || "".equals(id_C_pi)) {
                // 调用方法
                return rpiCodeCore(id_C,rname);
            } else {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PI_B_BIND.getCode(), "机器已经被绑定");
            }
        }
    }

    /**
     * 生成树莓派机器二维码的核心方法
     * @param id_C	公司编号
     * @param rname	树莓派编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/18
     */
    private ApiResponse rpiCodeCore(String id_C,String rname){
//        String id_C = can.getString("id_C");
        // 获取公司的资产的树莓派信息
        JSONObject rpiNameData = getRNames(id_C,false);
        // 获取内部状态信息
        Integer statusInside = rpiNameData.getInteger("status");
        if (statusInside == 0) {
            JSONObject rnames = rpiNameData.getJSONObject("rnames");
//            JSONObject rpi = rpiNameData.getJSONObject("rpi");
            String assetId = rpiNameData.getString("assetId");
            // 根据rname获取公司内的对应信息
            JSONObject rnameData = rnames.getJSONObject(rname);
            // 判断信息为空，抛出异常
            if (null == rnameData) {
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_RPI_R_NAME_K.getCode(), "该公司rpi卡片没有对应的rname，请新增");
            }
            // 定义存储树莓派的所有gpio信息
            JSONArray resultArr = new JSONArray();
            // 定义存储树莓派信息
            JSONObject piData = new JSONObject();
            piData.put("id_C",id_C);
            // 定义存储gpio和对应的token
            JSONObject piDataSon = new JSONObject();
            // 设置所有gpio口
            List<String> gpList = new ArrayList<>(
                    Arrays.asList("4","5","6","12","13","17","18","19","20","21","22","23","24","25","26","27"));
            // 遍历所有gpio
            gpList.forEach(gp -> {
                // 定义存储树莓派信息json对象
                JSONObject redisJ = new JSONObject();
                // 添加信息
                redisJ.put("rname",rname);
                redisJ.put("gpio",gp);
                // 设置信息
                updateRedJ(redisJ,"", "", "", 0, new JSONObject(), 0, ""
                        , 0, "", "", "", 0, "", new JSONObject()
                        , new JSONObject(), new JSONObject(), false);
                // 获取token
                String token = UUID19.uuid();
                // 添加gpio以及对应的token
                piDataSon.put(gp,token);
                // 生成二维码数据信息
                String url = RPI_URL_PREFIX + token;
                // 定义存储结果
                JSONObject result = new JSONObject();
                // 添加gpio信息
                result.put("gpio",gp);
                result.put("url",url);
                resultArr.add(result);
                rnameData.put(gp,token);
                // 设置树莓派gpio信息
//                redisTemplate0.opsForValue().set(PI_GPIO + token,JSON.toJSONString(redisJ));
                qt.setRDSet(PI_Q,PI_GPIO_H+token,JSON.toJSONString(redisJ),300L);
            });
            piData.put("piSon",piDataSon);
            // 设置树莓派信息
//            redisTemplate0.opsForValue().set(PI + rname,JSON.toJSONString(piData));
            qt.setRDSet(PI_Q,PI_H + rname,JSON.toJSONString(piData),300L);
            rnames.put(rname,rnameData);
//            rpi.put("rnames",rnames);
//            // 定义存储flowControl字典
//            JSONObject mapKey = new JSONObject();
//            // 设置字段数据
//            mapKey.put("rpi",rpi);
//            coupaUtil.updateAssetByKeyAndListKeyVal("id",assetId,mapKey);
            qt.setMDContent(assetId,qt.setJson("rpi.rnames",rnames),Asset.class);
            return retResult.ok(CodeEnum.OK.getCode(), resultArr);
        } else {
            return errResult(statusInside);
        }
    }

    /**
     * RPI二维码扫码后请求的api，获取RPI的绑定状态接口
     * @param token gpio的token
     * @param id_C  公司编号
     * @param id_U  操作用户编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 请求状态
     * @author tang
     * @version 1.0.0
     * @date 2022/8/18
     */
    @Override
    public ApiResponse requestRpiStatus(String token,String id_C,String id_U) {
//        String token = can.getString("token");
        // 获取gpio对应的token的redis信息
//        String gpioDataStr = redisTemplate0.opsForValue().get(PI_GPIO + token);
        String gpioDataStr = qt.getRDSetStr(PI_Q,PI_GPIO_H+token);
        if (null == gpioDataStr) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_T_DATA_NO.getCode(), "rpi的token数据不存在");
        }
//        String id_C = can.getString("id_C");
        // 转换为JSON对象
        JSONObject gpioData = JSON.parseObject(gpioDataStr);
        // 调用根据rname获取树莓派机器的id_C绑定状态方法，获取状态
        int status = piNameSta(gpioData.getString("rname"), id_C);
        if (status == 0) {
            // 获取绑定状态
            boolean isBinding = gpioData.getBoolean("isBinding");
//            String id_U = can.getString("id_U");
            // 判断绑定状态，并给出返回状态
            if (!isBinding) {
                return retResult.ok(CodeEnum.OK.getCode(), "1");
            } else {
                if (gpioData.getString("id_U").equals(id_U)) {
                    return retResult.ok(CodeEnum.OK.getCode(), "2");
                } else {
                    return retResult.ok(CodeEnum.OK.getCode(), "3");
                }
            }
        } else {
            // 调用异常状态判断输出方法
            return errResultPi(status);
        }
    }

    /**
     * 绑定RPI接口
     * @param token gpio的token
     * @param id_C  公司编号
     * @param id_U  操作用户编号
     * @param grpU  用户组别
     * @param oIndex    订单对应下标
     * @param wrdNU 名称
     * @param imp   未知，
     * @param id_O  订单编号
     * @param tzone 未知，
     * @param lang  语言？
     * @param id_P  产品编号
     * @param pic   图片
     * @param wn2qtynow 数量
     * @param grpB  产品组别？
     * @param fields    未知，
     * @param wrdNP 名称
     * @param wrdN  名称
     * @param dep   部门
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/18
     */
    @Override
    public ApiResponse bindingRpi(String token,String id_C,String id_U,String grpU,Integer oIndex
            ,JSONObject wrdNU,Integer imp,String id_O,Integer tzone,String lang,String id_P
            ,String pic,Integer wn2qtynow,String grpB,JSONObject fields,JSONObject wrdNP
            ,JSONObject wrdN,String dep) {
        // 获取gpio对应的token的redis信息
//        String gpioDataStr = redisTemplate0.opsForValue().get(PI_GPIO + token);
        String gpioDataStr = qt.getRDSetStr(PI_Q,PI_GPIO_H+token);
        if (null == gpioDataStr) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_T_DATA_NO.getCode(), "rpi的token数据不存在");
        }
        // 转换为JSON对象
        JSONObject gpioData = JSON.parseObject(gpioDataStr);
        String rname = gpioData.getString("rname");
        // 调用根据rname获取树莓派机器的id_C绑定状态方法
        int status = piNameSta(rname, id_C);
        if (status == 0) {
            String gpio = gpioData.getString("gpio");
            // 调用设置树莓派存储信息方法
            updateRedJ(gpioData,id_U, id_C, grpU, oIndex, wrdNU, imp, id_O, tzone, lang
                    , id_P, pic, wn2qtynow, grpB, fields, wrdNP, wrdN, true);
            // 获取创建日志对象
            LogFlow logFlow = getLogF(id_C,id_U,rname);
            // 设置日志信息
            logFlow.setLogType("binding");
            logFlow.setZcndesc("绑定gpIo成功");
            logFlow.setGrpU(grpU);
            logFlow.setIndex(oIndex);
            logFlow.setWrdNU(wrdNU);
            logFlow.setImp(imp);
            logFlow.setSubType("binding");
            logFlow.setId_O(id_O);
            logFlow.setTzone(tzone);
            logFlow.setLang(lang);
            logFlow.setId_P(id_P);
            // 设置日志的data信息
            JSONObject data = new JSONObject();
            data.put("gpio",gpio);
            data.put("rname",rname);
            data.put("pic",pic);
            data.put("wn2qtynow",wn2qtynow);
            data.put("grpB",grpB);
            data.put("fields",fields);
            data.put("wrdNP",wrdNP);
            data.put("wrdN",wrdN);
            data.put("id_O",id_O);
            data.put("index",oIndex);
//            data.put("dep",can.getString("dep"));
            data.put("dep",dep);
            data.put("grpU",grpU);
            data.put("wrdNU",wrdNU);
            data.put("id_C",id_C);
            logFlow.setData(data);
            // 发送日志
            wsrpi.sendWSPi(logFlow);
            System.out.println("发送消息:");
            System.out.println(JSON.toJSONString(logFlow));
            // 更新redis
//            redisTemplate0.opsForValue().set(PI_GPIO + token,JSON.toJSONString(gpioData));
            qt.setRDSet(PI_Q,PI_GPIO_H+token,JSON.toJSONString(gpioData),300L);
            return retResult.ok(CodeEnum.OK.getCode(), "绑定gpIo成功");
        } else {
            return errResultPi(status);
        }
    }

    /**
     * 解除绑定RPI接口
     * @param token	gpio的token
     * @param id_C	公司编号
     * @param id_U	操作用户编号
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @version 1.0.0
     * @date 2022/8/18
     */
    @Override
    public ApiResponse relieveRpi(String token,String id_C,String id_U) {
//        String token = can.getString("token");
//        String id_C = can.getString("id_C");
        // 获取gpio对应的token的redis信息
//        String gpioDataStr = redisTemplate0.opsForValue().get(PI_GPIO + token);
        String gpioDataStr = qt.getRDSetStr(PI_Q,PI_GPIO_H+token);
        if (null == gpioDataStr) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_T_DATA_NO.getCode(), "rpi的token数据不存在");
        }
        // 转换为JSON对象
        JSONObject gpioData = JSON.parseObject(gpioDataStr);
        String rname = gpioData.getString("rname");
        int status = piNameSta(rname, id_C);
        if (status == 0) {
            String gpio = gpioData.getString("gpio");
            // 调用设置树莓派存储信息方法
            updateRedJ(gpioData,"", "", "", 0, new JSONObject(), 0, ""
                    , 0, "", "", "", 0, "", new JSONObject()
                    , new JSONObject(), new JSONObject(), false);
//            LogFlow logFlow = getLogF(id_C, can.getString("id_U"),rname);
            // 获取创建日志对象
            LogFlow logFlow = getLogF(id_C, id_U,rname);
            JSONObject data = new JSONObject();
            data.put("gpio",gpio);
            data.put("rname",rname);
            logFlow.setLogType("unbound");
            logFlow.setZcndesc("解绑gpIo成功");
            logFlow.setData(data);
            // 发送日志
            wsrpi.sendWSPi(logFlow);
            System.out.println("发送消息:");
            System.out.println(JSON.toJSONString(logFlow));
            // 更新redis
//            redisTemplate0.opsForValue().set(PI_GPIO + token,JSON.toJSONString(gpioData));
            qt.setRDSet(PI_Q,PI_GPIO_H+token,JSON.toJSONString(gpioData),300L);
            return retResult.ok(CodeEnum.OK.getCode(), "解除绑定gpIo成功");
        } else {
            return errResultPi(status);
        }
    }

    /**
     * 根据rname获取树莓派机器的id_C绑定状态
     * @param rname	树莓派编号-id
     * @param id_C	公司编号
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    private int piNameSta(String rname,String id_C){
        // 获取树莓派机器信息
//        String rpiDataStr = redisTemplate0.opsForValue().get(PI + rname);
        String rpiDataStr = qt.getRDSetStr(PI_Q,PI_H+rname);
        // 判断树莓派机器信息为空
        if (null == rpiDataStr || "".equals(rpiDataStr)) {
            return 1;
        } else {
            // 转换树莓派机器信息
            JSONObject rpiData = JSONObject.parseObject(rpiDataStr);
            // 获取树莓派绑定公司编号
            String id_C_pi = rpiData.getString("id_C");
            // 判断绑定公司编号为空
            if (null == id_C_pi || "".equals(id_C_pi)) {
                return 1;
            } else {
                // 判断绑定公司等于当前操作公司
                if (id_C.equals(id_C_pi)) {
                    // 返回正常结果
                    return 0;
                } else {
                    return 2;
                }
            }
        }
    }

    /**
     * 异常状态判断输出
     * @param status	状态信息
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    private ApiResponse errResultPi(int status){
        // 判断状态，并输出对应错误信息
        if (status == 1) {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PI_X_K.getCode(), "机器信息为空，操作失败");
        } else {
            throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_PI_B_NO.getCode(), "该机器不属于你们公司，操作失败");
        }
    }

    /**
     * 根据状态，返回对应的错误信息方法
     * @param status    错误状态
     * @return com.cresign.tools.apires.ApiResponse  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    private ApiResponse errResult(int status){
        // 判断状态并返回对应的错误信息
        switch (status) {
            case 1:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ASSET_ID.getCode(), "该公司没有assetId");
            case 2:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_ASSET.getCode(), "该公司没有asset");
            case 3:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_NO_RPI_K.getCode(), "该公司没有rpi卡片");
            case 4:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_RPI_K.getCode(), "该公司rpi卡片异常");
            default:
                throw new ErrorResponseException(HttpStatus.OK, ChatEnum.ERR_WZ.getCode(), "接口未知异常");
        }
    }

    /**
     * 获取公司的资产的树莓派信息
     * @param id_C	公司编号
     * @param isInfo	是否携带info信息
     * @return com.alibaba.fastjson.JSONObject  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    private JSONObject getRNames(String id_C,boolean isInfo){
        // 创建存储返回结果json对象
        JSONObject result = new JSONObject();
        // 根据公司编号和模块获取公司资产编号
        Asset asset = qt.getConfig(id_C, "a-core", "rpi");
//        System.out.println("assetId:"+assetId);
        if (null == asset.getId()) {
            result.put("status",1);
            return result;
        }
        // 根据公司资产编号获取对应的资产信息

        if (null == asset) {
            result.put("status",2);
            return result;
        }
//        System.out.println(JSON.toJSONString(asset));
        JSONObject rpi = asset.getRpi();
        if (null == rpi) {
            result.put("status",3);
            return result;
        }
        JSONObject rnames = rpi.getJSONObject("rnames");
        if (null == rnames) {
            result.put("status",4);
            return result;
        }
        if (isInfo) {
            JSONObject pinfo = rpi.getJSONObject("pinfo");
            if (null == pinfo) {
                result.put("status",5);
                return result;
            }
            result.put("pinfo",pinfo);
        }
        result.put("status",0);
        result.put("rpi",rpi);
        result.put("assetId",asset.getId());
        result.put("rnames",rnames);
        return result;
    }

    /**
     * 设置树莓派存储信息方法
     * @param redJ	存储信息json对象
     * @param id_U	用户编号
     * @param id_C	公司编号
     * @param grpU	用户组别
     * @param oIndex	订单对应的下标
     * @param wrdNU	名称
     * @param imp	未知，
     * @param id_O	订单编号
     * @param tzone	未知，
     * @param lang	语言？
     * @param id_P	产品编号
     * @param pic	图片
     * @param wn2qtynow	数量
     * @param grpB	组别？
     * @param fields	未知
     * @param wrdNP	名称
     * @param wrdN	名称
     * @param isBinding	是否绑定
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    private void updateRedJ(JSONObject redJ,String id_U,String id_C,String grpU,Integer oIndex,JSONObject wrdNU
            ,Integer imp,String id_O,Integer tzone,String lang,String id_P,String pic,Integer wn2qtynow
            ,String grpB,JSONObject fields,JSONObject wrdNP,JSONObject wrdN,Boolean isBinding){
        redJ.put("id_U",id_U);
        redJ.put("id_C",id_C);
        redJ.put("grpU",grpU);
        redJ.put("oIndex",oIndex);
        redJ.put("wrdNU",wrdNU);
        redJ.put("imp",imp);
        redJ.put("id_O",id_O);
        redJ.put("tzone",tzone);
        redJ.put("lang", lang);
        redJ.put("id_P", id_P);
        redJ.put("pic", pic);
        redJ.put("wn2qtynow", wn2qtynow);
        redJ.put("grpB", grpB);
        redJ.put("fields", fields);
        redJ.put("wrdNP", wrdNP);
        redJ.put("wrdN", wrdN);
        redJ.put("isBinding",isBinding);
    }

    /**
     * 生成日志方法
     * @param id_C	公司编号
     * @param id_U	用户编号
     * @param rname	树莓派编号
     * @return com.cresign.tools.pojo.po.LogFlow  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * @date 2022/8/18
     */
    private LogFlow getLogF(String id_C,String id_U,String rname){
        LogFlow logFlow = LogFlow.getInstance();
        logFlow.setId_C(id_C);
        logFlow.setId_U(id_U);
        logFlow.setId(rname);
        return logFlow;
    }

}
