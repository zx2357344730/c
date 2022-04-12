package com.cresign.login.utils;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Log4j2
public class UserAgentUtils {
	
	// 匹配到设备型号
	private static String modePattern = ";\\s?(\\S*?\\s?\\S*?)\\s?(Build)?/";
	
	/**
	 * 根据http获取userAgent信息
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:45
	 * ##Return: java.lang.String
	 */
	public static String getUserAgent(HttpServletRequest request) {
		String userAgent=request.getHeader("User-Agent");
		return userAgent;
	}
	
	/**
	 * 根据userAgent解析出osVersion
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:45
	 * ##Return: java.lang.String
	 */
	public static String getOsVersion(String userAgent) {
		String osVersion = "";
		if(StringUtils.isBlank(userAgent)) {
			return osVersion;
		}
		String[] strArr = userAgent.substring(userAgent.indexOf("(")+1,
				userAgent.indexOf(")")).split(";");
		if(null == strArr || strArr.length == 0) {
			return osVersion;
		}
		
		osVersion = strArr[1];
		log.info("osVersion is:{}", osVersion);
		return osVersion;
	}
	
	/**
	 * 获取操作系统对象
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:45
	 * ##Return: eu.bitwalker.useragentutils.OperatingSystem
	 */
	private static OperatingSystem getOperatingSystem(String userAgent) {
		UserAgent agent = UserAgent.parseUserAgentString(userAgent);
		OperatingSystem operatingSystem = agent.getOperatingSystem();
		return operatingSystem;
	}
	
	/**
	 * 获取os：Windows/ios/Android
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:45
	 * ##Return: java.lang.String
	 */
	public static String getOs(String userAgent) {
		OperatingSystem operatingSystem =  getOperatingSystem(userAgent);
		String os = operatingSystem.getGroup().getName();
		log.info("os is:{}", os);
		return os;
	}

	/**
	 * 获取deviceType
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getDevicetype(String userAgent) {
		OperatingSystem operatingSystem =  getOperatingSystem(userAgent);
		String deviceType = operatingSystem.getDeviceType().toString();
		log.info("deviceType is:{}", deviceType);
		return deviceType;
	}

	/**
	 * 获取操作系统的名字
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getOsName(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getOsName(userAgent);
	}

	/**
	 * 获取操作系统的名字
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getOsName(String userAgent) {
		OperatingSystem operatingSystem =  getOperatingSystem(userAgent);
		String osName = operatingSystem.getName();
		log.info("osName is:{}", osName);
		return osName;
	}


	/**
	 * 获取device的生产厂家
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getDeviceManufacturer(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getDeviceManufacturer(userAgent);
	}

	/**
	 * 获取device的生产厂家
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getDeviceManufacturer(String userAgent) {
		OperatingSystem operatingSystem =  getOperatingSystem(userAgent);
		String deviceManufacturer = operatingSystem.getManufacturer().toString();
		log.info("deviceManufacturer is:{}", deviceManufacturer);
		return deviceManufacturer;
	}

	/**
	 * 获取浏览器对象
	 * ##author: JackSon
	 * ##Params: agent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: eu.bitwalker.useragentutils.Browser
	 */
	public static Browser getBrowser(String agent) {
		UserAgent userAgent = UserAgent.parseUserAgentString(agent);
		Browser browser = userAgent.getBrowser();
		return browser;
	}


	/**
	 * 获取browser name
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getBorderName(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getBorderName(userAgent);
	}

	/**
	 * 获取browser name
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getBorderName(String userAgent) {
		Browser browser =  getBrowser(userAgent);
		String borderName = browser.getName();
		log.info("borderName is:{}", borderName);
		return borderName;
	}


	/**
	 * 获取浏览器的类型
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getBorderType(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getBorderType(userAgent);
	}

	/**
	 * 获取浏览器的类型
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getBorderType(String userAgent) {
		Browser browser =  getBrowser(userAgent);
		String borderType = browser.getBrowserType().getName();
		log.info("borderType is:{}", borderType);
		return borderType;
	}

	/**
	 * 获取浏览器组： CHROME、IE
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getBorderGroup(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getBorderGroup(userAgent);
	}

	/**
	 * 获取浏览器组： CHROME、IE
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:44
	 * ##Return: java.lang.String
	 */
	public static String getBorderGroup(String userAgent) {
		Browser browser =  getBrowser(userAgent);
		String browerGroup = browser.getGroup().getName();
		log.info("browerGroup is:{}", browerGroup);
		return browerGroup;
	}

	/**
	 * 获取浏览器的生产厂商
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:43
	 * ##Return: java.lang.String
	 */
	public static String getBrowserManufacturer(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getBrowserManufacturer(userAgent);
	}


	/**
	 * 获取浏览器的生产厂商
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:43
	 * ##Return: java.lang.String
	 */
	public static String getBrowserManufacturer(String userAgent) {
		Browser browser =  getBrowser(userAgent);
		String browserManufacturer = browser.getManufacturer().getName();
		log.info("browserManufacturer is:{}", browserManufacturer);
		return browserManufacturer;
	}
	
	/**
	 * 获取浏览器使用的渲染引擎
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:43
	 * ##Return: java.lang.String
	 */
	public static String getBorderRenderingEngine(String userAgent) {
		Browser browser =  getBrowser(userAgent);
		String renderingEngine = browser.getRenderingEngine().name();
		return renderingEngine;
	}


	/**
	 * 获取浏览器版本
	 * ##author: JackSon
	 * ##Params: request
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:43
	 * ##Return: java.lang.String
	 */
	public static String getBrowserVersion(HttpServletRequest request) {
		String userAgent = getUserAgent(request);
		return getBrowserVersion(userAgent);
	}

	/**
	 * 获取浏览器版本
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:43
	 * ##Return: java.lang.String
	 */
	public static String getBrowserVersion(String userAgent) {
		Browser browser =  getBrowser(userAgent);
		String borderVersion = browser.getVersion(userAgent).toString();
		return borderVersion;
	}

	/**
	 * 获取设备型号
	 * ##author: JackSon
	 * ##Params: userAgent
	 * ##version: 1.0
	 * ##updated: 2020/9/8 10:47
	 * ##Return: java.lang.String
	 */
	public static String getEquipmentModel(String userAgent) {
		Pattern pattern = Pattern.compile(modePattern);
		Matcher matcher = pattern.matcher(userAgent);
		String model = null;
		if (matcher.find()) {
			model = matcher.group(1).trim();
		}
		
		return model;
	}
	

	public static void main(String[] args) {
//		String winUserAgent = "Mozilla/5.0 (Linux; Android 8.0; LON-AL00 Build/HUAWEILON-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/6.2 TBS/044204 Mobile Safari/537.36 V1_AND_SQ_7.7.8_908_YYB_D QQ/7.7.8.3705 NetType/WIFI WebP/0.3.0 Pixel/1440";
//		String winUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16A366 QQ/7.7.8.421 V1_IPH_SQ_7.7.8_1_APP_A Pixel/750 Core/UIWebView Device/Apple(iPhone 6s) NetType/WIFI QBWebViewType/1";
		String winUserAgent = "Mozilla/5.0 (Linux; Android 10; LYA-AL00 Build/HUAWEILYA-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/76.0.3809.89 Mobile Safari/537.36 T7/11.26 SP-engine/2.22.0 baiduboxapp/11.26.5.10 (Baidu; P1 10) NABar/1.0";
//		String winUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.83 Safari/537.36";


////		String agent=request.getHeader("User-Agent");
////解析agent字符串
//		UserAgent userAgent = UserAgent.parseUserAgentString(winUserAgent);
////获取浏览器对象
//		Browser browser = userAgent.getBrowser();
////获取操作系统对象
//		OperatingSystem operatingSystem = userAgent.getOperatingSystem();
//
////		("agent:"+agent);
//		("浏览器名:"+browser.getName());
//		("浏览器类型:"+browser.getBrowserType());
//		("浏览器家族:"+browser.getGroup());
//		("浏览器生产厂商:"+browser.getManufacturer());
//		("浏览器使用的渲染引擎:"+browser.getRenderingEngine());
//		("浏览器版本:"+userAgent.getBrowserVersion());
//		DeviceType deviceType = operatingSystem.getDeviceType();
//
//
//		("\n操作系统名:"+operatingSystem.getName());
//		("访问设备类型:"+operatingSystem.getDeviceType());
//		("操作系统家族:"+operatingSystem.getGroup());
//
//		("操作系统生产厂商:"+operatingSystem.getManufacturer());
	}

}