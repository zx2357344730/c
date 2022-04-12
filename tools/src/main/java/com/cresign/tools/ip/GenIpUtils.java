package com.cresign.tools.ip;


/**
*##description:      通过 ip 获取 国家省份城市
*##Params:
*##Return:
*##author:           JackSon
*##updated:             2020/5/26 11:45
*/
public class GenIpUtils {
//
//	public static void main(String[] args) throws Exception {
//        String ip = "112.96.176.184";
////		String ip = "59.38.64.114";
//
//		GenIpUtils.getCityByIP(ip);
//	}

//	public static void main(String[] args) {
//		TimeZone timeZone1 = TimeZone.getDefault();//获取当前服务器时区
//		("本地服务器偏移量：" +  timeZone1.getRawOffset());
//
//
//
//
//
//
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		(sdf.format(new Date()));
//		//获取当前时间戳,也可以是你自已给的一个随机的或是别人给你的时间戳(一定是long型的数据)
//		long timeStamp = 1590476291575L;
//		("timeStamp = " + timeStamp);
//		//这个是你要转成后的时间的格式
//		SimpleDateFormat sdff=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		// 时间戳转换成时间
//		String sd = sdff.format(new Date(timeStamp));
//		(sd);//打印出你要的时间
//	}

//	public static void main(String[] args) {
//		String dateStr1 = "1949-10-01";
//		String dateStr2 = "2016-08-15";
//		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
//		SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
//		try {
//			Date date1 = format1.parse(dateStr1);
//			Date date2 = format2.parse(dateStr2);
//
//			getDatePoor(date1, date2)
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

//	public static String getDatePoor(Date endDate, Date nowDate) {
//
//		long nd = 1000 * 24 * 60 * 60;
//		long nh = 1000 * 60 * 60;
//		long nm = 1000 * 60;
//		// long ns = 1000;
//		// 获得两个时间的毫秒时间差异
//		long diff = endDate.getTime() - nowDate.getTime();
//		// 计算差多少天
//		long day = diff / nd;
//		// 计算差多少小时
//		long hour = diff % nd / nh;
//		// 计算差多少分钟
//		long min = diff % nd % nh / nm;
//		// 计算差多少秒//输出结果
//		// long sec = diff % nd % nh % nm / ns;
//		return day + "天" + hour + "小时" + min + "分钟";
//	}




//	public static void getCityByIP(String ip) throws Exception{
//		  // 创建 GeoLite2 数据库
//	      File database = new File("J:\\CompanyProject\\GeoLite2-City_20200519\\GeoLite2-City_20200519\\GeoLite2-City.mmdb");
//	      // 读取数据库内容
//	      DatabaseReader reader = new DatabaseReader.Builder(database).build();
//	      InetAddress ipAddress = InetAddress.getByName(ip);
//
//	      // 获取查询结果
//	      CityResponse response = reader.city(ipAddress);
//
//	      // 获取国家信息
//	      Country country = response.getCountry();
//	      ("国家code:"+country.getIsoCode());
//	      ("国家:"+country.getNames().get("zh-CN"));
//
//	      // 获取省份
//	      Subdivision subdivision = response.getMostSpecificSubdivision();
//	      ("省份code:"+subdivision.getIsoCode());
//	      ("省份:"+subdivision.getNames().get("zh-CN"));
//
//	      //城市
//	      City city = response.getCity();
//	      ("城市code:"+city.getGeoNameId());
//	      ("城市:"+city.getName());
//
//	      // 获取城市
//	      Location location = response.getLocation();
//	      ("经度:"+location.getLatitude());
//	      ("维度:"+location.getLongitude());
//
//
//		Continent continent = response.getContinent();
//		("continent = " + continent);
//
//		String es = continent.getNames().get("es");
//		(continent.getNames().get("es"));
//
//
//		Date date = new Date();
//		Locale locale = Locale.CHINA;
//		DateFormat shortDf = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL, locale);
//		shortDf.setTimeZone(TimeZone.getTimeZone("Asia/Hong_Kong"));//Asia/Chongqing
//		(TimeZone.getDefault().getID());
//		("中国当前日期时间：" + shortDf.format(date));
//
//		locale = Locale.ENGLISH;
//		shortDf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM, locale);
//		shortDf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
//		("英国当前日期时间："+shortDf.format(date));
//
//		((System.currentTimeMillis()));
//	}
}
