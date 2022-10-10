package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.common.Constants;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.pojo.po.LogFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tangzejin
 * @updated 2019/6/27
 * @ver 1.0.0
 * ##description: 通用工具类
 */
@Service
@Slf4j
public class Ut {

    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    /**
     * 定义正则表达式
     */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    @Autowired
    private  MongoTemplate mongoTemplate;

    /**
     * 判断数字integer是否为空
     * @param integer	数字
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/9/15 10:59
     */
    public static int isNull(Integer integer){
        return integer==null?0:integer;
    }

    /**
     * 判断字符串是否为空，是返回true
     * @param name	判断的字符串
     * @return boolean  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:29
     */
    public static boolean isNull(String name) {
        return null == name || Constants.STRING_EMPTY.equals(name) || name.length() == 0;
    }

    private String nullFix(String str){
        if (null == str) {
            return "";
        } else {
            return str;
        }
    }


    /**
     * 平均半径,单位：m；不是赤道半径。赤道为6378左右
     */
    private static final double EARTH_RADIUS = 6371393;


    /**
     * 反余弦计算两个经纬度的差
     * @param lat1	精度1
     * @param lng1	纬度1
     * @param lat2	精度2
     * @param lng2	纬度2
     * @return double  返回结果: 结果米数
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:18
     */
    public static double getDistance(Double lat1,Double lng1,Double lat2,Double lng2) {
        // 经纬度（角度）转弧度。弧度用作参数，以调用Math.cos和Math.sin
        // A经弧度
        double radiansAx = Math.toRadians(lng1);
        // A纬弧度
        double radiansAy = Math.toRadians(lat1);
        // B经弧度
        double radiansBx = Math.toRadians(lng2);
        // B纬弧度
        double radiansBy = Math.toRadians(lat2);

        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAy) * Math.cos(radiansBy) * Math.cos(radiansAx - radiansBx)
                + Math.sin(radiansAy) * Math.sin(radiansBy);
        // 反余弦值
        double acos = Math.acos(cos);
        // 最终结果
        return EARTH_RADIUS * acos;
    }

//        /**
//     *将字符串格式yyyy/MM/dd的字符串转为日期，格式"yyyy-MM"
//     * @param date 日期字符串
//     * @return 返回格式化的日期
//     * @throws ParseException 分析时意外地出现了错误异常
//     * Jevon
//     */
//    public static String strOndToDateFormat(String date) throws ParseException {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
//        formatter.setLenient(false);
//        Date newDate= formatter.parse(date);
//        formatter = new SimpleDateFormat("yyyy-MM");
//        return formatter.format(newDate);
//    }

    /**
     * 获取文件大小
     * @author Jevon
     * @param size
     * @ver 1.0
     * @updated 2020/9/22 9:50
     * @return java.lang.String
     */
    public static String fileSizeString(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    public static <T> T jsonTo(Object data, Class<T> classType){

        return JSONObject.parseObject(JSON.toJSONString(data), classType);
    }

    public static JSONObject toJson(Object data){

        return JSONObject.parseObject(JSON.toJSONString(data));
    }


    /**
     * 转换
     * @author Jevon
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return  网络请求字符串
     * ##exception:
     */
//    public static String net(String strUrl, Map params,String method) throws Exception {
//        HttpURLConnection conn = null;
//        BufferedReader reader = null;
//        String rs = null;
//        try {
//            StringBuffer sb = new StringBuffer();
//            if(method==null || method.equals("GET")){
//                strUrl = strUrl+"?"+urlencode(params);
//            }
//            URL url = new URL(strUrl);
//            conn = (HttpURLConnection) url.openConnection();
//            if(method==null || method.equals("GET")){
//                conn.setRequestMethod("GET");
//            }else{
//                conn.setRequestMethod("POST");
//                conn.setDoOutput(true);
//            }
//            conn.setRequestProperty("User-agent", userAgent);
//            conn.setUseCaches(false);
//            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
//            conn.setReadTimeout(DEF_READ_TIMEOUT);
//            conn.setInstanceFollowRedirects(false);
//            conn.connect();
//            if (params!= null && method.equals("POST")) {
//                try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
//                    out.writeBytes(urlencode(params));
//                }
//            }
//            InputStream is = conn.getInputStream();
//            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
//            String strRead = null;
//            while ((strRead = reader.readLine()) != null) {
//                sb.append(strRead);
//            }
//            rs = sb.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (reader != null) {
//                reader.close();
//            }
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//        return rs;
//    }

    /**
     * 将map型转为请求参数型
     * @author Jevon
     * @param data
     * @ver 1.0
     * @updated 2020/11/16 22:23
     * @return java.lang.String
     */
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue()+"","UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    /**
     * 判断str是否为数字
     * @param str	字符串
     * @return boolean  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:19
     */
    public static boolean isNum(String str) {
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        return isNum.matches();
    }

    /**
     * 用于去掉s里面小数点后面不需要的0
     * @param s	数字字符串
     * @return java.lang.String  返回结果: 结果字符串
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:19
     */
    public static String trimZero(String s) {
        BigDecimal value = new BigDecimal(s);
        BigDecimal noZeros = value.stripTrailingZeros();
        return noZeros.toPlainString();
    }


    /**
     * String转double类型
     * @param s	String数据
     * @return double  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:20
     */
    public static double getDouble(String s) {
        return Double.parseDouble(s);
    }

    /**
     * 将数字补零,只限用于时间
     * @param b	需要补零的数字
     * @return java.lang.String  返回结果: 补零结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:20
     */
    public static String addZero(int b) {
        if (b > 9) {
            return b + "";
        } else {
            return "0" + b;
        }
    }

    /**
     * 获取0到shu的随机数
     * @param shu	随机数的最大值
     * @return int  返回结果: 之间的随机数
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:20
     */
    public static int getMathRandom(int shu){
        return (int)(Math.random()*shu);
    }



    /**
     * 获取s的长度
     * @param s	数组
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:21
     */
    public static int getLength(String[] s) {
        if (s.length == Constants.INT_TWO) {
            return 1;
        } else if (s.length == Constants.INT_THREE) {
            return 1;
        } else {
            return 0;
        }
    }



    /**
     * 获取d保留两位小数并四舍五入
     * @param d	数值
     * @return double  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:25
     */
    public static double round(double d, int digit) {
        String length;
        switch (digit) {
            case 1:
                length = "#.0";
                break;
            case 2:
                length = "#.00";
                break;
            case 3:
                length = "#.000";
                break;
            case 4:
                length = "#.0000";
                break;
            case 5:
                length = "#.00000";
                break;
            case 6:
                length = "#.000000";
                break;
            case 7:
                length = "#.0000000";
                break;
            case 8:
                length = "#.00000000";
                break;
            default:
                length = "#.000000000";

        }

        DecimalFormat df = new DecimalFormat(length);
        return Double.parseDouble(df.format(d));
    }

    /**
     * 获取f保留position位小数并四舍五入
     * @param f	数值
     * @param position	小数位
     * @return float  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:25
     */
    public static float getFloatByDigitAndRounding(float f, int position) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(position, BigDecimal.ROUND_HALF_UP).floatValue();
    }







    /**
     * 将obj转换成int
     * @param obj	需要转换的数值
     * @return java.lang.Integer  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:29
     */
    public static Integer objToInteger(Object obj) {
        if (obj != null) {
            return Integer.parseInt(obj.toString());
        }
        return 0;
    }

    /**
     * 将obj转换成double
     * @param obj	需要转换的数值
     * @return java.lang.Double  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:29
     */
    public static Double objToDouble(Object obj) {
        if (obj != null) {
            return Double.parseDouble(obj.toString());
        }
        return 0.0;
    }



//    /**
//     * 动态切换 Redis 数据库
//     *
//     * @param num 数据库下标
//     */
//    public static void setDataBase(int num, StringRedisTemplate redisTemplate0) {
//
//        // LettuceConnectionFactory 工厂类
//        // 获取当前 redisTemplate0 连接配置
//        LettuceConnectionFactory connectionFactory = (LettuceConnectionFactory) redisTemplate0.getConnectionFactory();
//
//        // 判断当前 连接是否为空
//        if (connectionFactory != null && num != connectionFactory.getDatabase()) {
//
//            // 1. 否， 重新设置数据库下标
//            connectionFactory.setDatabase(num);
//
//            // 重新将当前 redisTemplate0 连接配置设置
//            redisTemplate0.setConnectionFactory(connectionFactory);
//
//            // 重置基础共享连接，以便在下次访问时重新初始化
//            connectionFactory.resetConnection();
//        }
//    }

    /**
     * 对list集合进行排序
     * @param list	集合
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:32
     */
    public static void listSort(List<LogFlow> list) {

        sortIs(Constants.INT_ONE, list, DateEnum.DATE_TIME_FULL.getDate());

    }

    /**
     * 将list根据dateType按照is进行排序
     * @param is	模式
     * @param list	集合
     * @param dateType	排序条件
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:32
     */
    public static void sortIs(int is, List<LogFlow> list, String dateType) {
        //重写原list的排序方法
        list.sort((o1, o2) -> {

            //创建日期格式化对象，并添加格式化日期的格式
            SimpleDateFormat format = new SimpleDateFormat(dateType);

            //捕捉异常
            try {

                //将第一个对象的tmd字段转换成日期
                Date dt1 = format.parse(o1.getTmd());

                //将第二个对象的tmd字段转换成日期
                Date dt2 = format.parse(o2.getTmd());

                return listSortResult(is, dt1, dt2);
            } catch (Exception e /*捕捉所有异常*/) {

                //抛出异常信息
                log.debug("出现错误：" + e.getMessage());
            }

            //返回结果
            return 0;
        });

    }

    /**
     * 对dt1和dt2进行判断并返回值
     * @param is	需要的返回结果：1是降序，2是升序
     * @param dt1	比较值1
     * @param dt2	比较值2
     * @return int  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:33
     */
    public static int listSortResult(int is, Date dt1, Date dt2) {
        if (is == Constants.INT_ONE) {
            //判断第一个时间戳小于第二个时间戳
            if (dt1.getTime() < dt2.getTime()) {

                //返回结果
                return 1;

                //判断第一个时间戳大于第二个时间戳
            } else if (dt1.getTime() > dt2.getTime()) {

                //返回结果
                return -1;
            }
        } else {
            //判断第一个时间戳小于第二个时间戳
            if (dt1.getTime() > dt2.getTime()) {

                //返回结果
                return 1;

                //判断第一个时间戳大于第二个时间戳
            } else if (dt1.getTime() < dt2.getTime()) {

                //返回结果
                return -1;
            }
        }
        return 0;
    }

    /**
     * 对list集合进行排序
     * @param list	集合
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:33
     */
    public static void listSort2(List<LogFlow> list) {
        sortIs(Constants.INT_TWO, list, DateEnum.DATE_TIME_FULL.getDate());
    }

    /**
     * 对list集合进行排序
     * @param list	集合
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 11:33
     */
    public static void listSortOb(List<String> list) {

        //重写原list的排序方法
        list.sort((o1, o2) -> {

            //捕捉异常
            try {

                return o2.compareTo(o1);

            } catch (Exception e /*捕捉所有异常*/) {

                //抛出异常信息
                e.printStackTrace();
            }

            //返回结果
            return 0;
        });
    }


    /**
     //     * 为筛选接口优化代码而写的工具
     //     *
     //     * @param listType 列表类型
     //     * @param find     查询结果
     //     * @return Jevon
     //     */
    public List<Object> contentMap(String listType, List<Object> find) {
        List<Object> contentList = new LinkedList<>();

        for (int i = 0; i < find.size(); i++){

            //  把id的数据换成id_P的数据
            JSONObject contentMap = (JSONObject) JSONObject.toJSON(find.get(i));
//
//            if (contentMap.getJSONObject("wrdN") != null){
//                contentMap.put("wrdN", contentMap.getJSONObject("wrdN").getString(request.getHeader("lang")));
//            } if (contentMap.getJSONObject("wrddesc") != null){
//                contentMap.put("wrddesc", contentMap.getJSONObject("wrddesc").getString(request.getHeader("lang")));
//            } if (null != contentMap.getJSONObject("wrdNC")) {
//                contentMap.put("wrdNC", contentMap.getJSONObject("wrdNC").getString(request.getHeader("lang")));
//            }  if (null != contentMap.getJSONObject("wrdNCB")){
//                contentMap.put("wrdNCB", contentMap.getJSONObject("wrdNCB").getString(request.getHeader("lang")));
//            }

            if (listType.equals("lBProd") || listType.equals("lSProd")) {
                contentMap.put("id", contentMap.get("id_P"));
            }else if (listType.equals("lBOrder") || listType.equals("lSOrder") ) {
                contentMap.put("id", contentMap.get("id_O"));
            } else if (listType.equals("lBComp")) {
                contentMap.put("id", contentMap.get("id_C"));
            } else if (listType.equals("lBUser")) {
                contentMap.put("id", contentMap.get("id_U"));
            } else if (listType.equals("lSComp")) {
                contentMap.put("id", contentMap.get("id_CB"));
            } else if (listType.equals("lSAsset")) {
                contentMap.put("id", contentMap.get("id_A"));
            }

            contentList.add(contentMap);
        }

        return contentList;
    }

//    public Object getMongoOneFields(String id, List<String> listField, Class<?> classType) {
//        Query query = new Query(new Criteria("_id").is(id));
//        listField.forEach(query.fields()::include);
//        return mongoTemplate.findOne(query, classType);
//    }


    /**
     * 检查公司ref是否唯一
     * @param ref 编号
     * @return
     * Jevon
     */
    public String chkRef(String ref, String id_C, String listType , Class<?> classType) {
//        public static <T> T jsonTo(Object data, Class<T> classType){

//        public <T> T save(     T objectToSave )

        //TODO Rachel
        // 1.get from lSBxxx, filter id_C
        // use ES don't use MDB
        // if found, return String false,
        // else return ref
        Query query = new Query();

        // 创建查询条件
        query.addCriteria(
                new Criteria("info").exists(true));

        List<?> comps = mongoTemplate.find(query, classType);

        boolean judge = false;

        for (int i = 0; i < comps.size(); i++) {

            // 获取 查询出来的整个详细信息
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(comps.get(i));

            // 只拿出其中的info
            JSONObject info = (JSONObject) jsonObject.get("info");

            if (info.get("ref") == null || info.get("ref").equals("")) {
                continue;
            }
            // 获取 info 中的 ref
            String refResult = info.get("ref").toString();

        /*
            进行判断 前端传入的参数 ref 是否 和 后端数据库查询出来的ref 相同
            1. 如果相同则提示不可使用
            2. 如果不相同或者为空则提示可用
            有相同的马上跳出
         */
            if (refResult.equals(ref)) {
                judge = true;
                break;
            } else {
                judge = false;
            }
        }
        //如果等于true证明有相同，否则就是没有
        if (judge) {
            return ref;
        } else {
            return "";
        }
    }





    /**
     * @描述 两个数组取差集, 从多的里面取。。。
     * @参数 [fids, pids] fids是多的数组；pids是少的数组
     * @返回值 java.lang.String
     * @创建人 jackson
     * @创建时间 2020/7/3
     **/
//    public static List<Set<String>> getDifSet(List<String> mores, List<String> lesss) {
//        //将多转换为set fid 肯定不是小的
//        Set<String> set = new HashSet<String>(mores);
//        for (String p : lesss) {
//            // 如果集合里有相同的就删掉，如果没有就将值添加到集合
//            if (set.contains(p)) {
//                set.remove(p);
//            } else {
//                set.add(p);
//            }
//        }
//        return Arrays.asList(set);
//    }
//
//    public static void main(String[] args){
//        List<String> objArray = new ArrayList();
//        JSONArray objArray2 = new JSONArray();
//         objArray2.add(0,"info");
//        objArray.add(0,"info");
//        objArray.add(1,"view");
//        objArray.add(2,"task00s");
//
//        objArray.removeAll(objArray2);
//
//    }

    /**
     * 根据getUserIdForToken和request获取用户id
     * @param getUserIdByToken	获取用户id类
     * @param request	请求
     * @return java.lang.String  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/9/2 16:04
     */
//    public static String getUserId(GetUserIdByToken getUserIdByToken, HttpServletRequest request){
//        return getUserIdByToken.getTokenOfUserId(request.getHeader(ChatConstants.HEAD_AUTHORIZATION)
//                , request.getHeader(ChatConstants.W_S_W_S_STRING_CLIENT_TYPE));
//    }

//    /**
//     * 发送日志方法，带推送
//     * @param logL	需要发送的日志
//     * @return void  返回结果: 结果
//     * @author tang
//     * @ver 1.0.0
//     * ##Updated: 2020/8/6 9:26
//     */
//    @SuppressWarnings("unchecked")
//    public static void sendLog(Log1 logL, StringRedisTemplate redisTemplate1
//            , RestHighLevelClient client, MongoTemplate mongoTemplate){
//
//        System.out.println("进入这个方法------------");
////        Object logHeader = redisTemplate1.opsForHash().get("userPush_"+logL.getId_C(), logL.getId_C() + "_" + logL.getId() );
//        List<String> id_APPList = redisTemplate1.opsForList().range("userPush_" + logL.getId_C() + "_" + logL.getId(), 0, -1);
//        if (id_APPList != null) {
//            JSONObject objlogHeader = JSON.parseObject(id_APPList.toString());
//            JSONArray grpUarry = (JSONArray) objlogHeader.get("grpU");
//
//            //构建查询库
//            SearchRequest searchRequest = new SearchRequest("lbuser");
//
//            //构建搜索条件
//            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//            List<Object> result = new ArrayList<>();
//            //根据职位和公司id查出所有人
//            for (int j = 0; j < grpUarry.size(); j++) {
//
//                QueryBuilder queryBuilder = QueryBuilders.boolQuery()
//                        //条件1：当前公司id
//                        .must(QueryBuilders.termQuery("id_CB", logL.getId_C()))
//                        //条件2：grpU
//                        .must(QueryBuilders.termQuery("grpU", grpUarry.get(j)));
//                searchSourceBuilder.query(queryBuilder);
//                searchSourceBuilder.from(0);
//                searchSourceBuilder.size(10000);
//                //把构建对象放入，指定查那个对象，把查询条件放进去
//                searchRequest.source(searchSourceBuilder);
//
//                //执行请求
//                SearchResponse search;
//                try {
//                    search = client.search(searchRequest, RequestOptions.DEFAULT);
//                    for (SearchHit hit : search.getHits().getHits()) {
//                        result.add(hit.getSourceAsMap());
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            System.out.println("输出数组:");
//            System.out.println(JSON.toJSONString(result));
//            //再去拿User.info.id_APP
//            for (int j = 0; j < result.size(); j++) {
//                HashMap<String,Object> lBUser = (HashMap<String, Object>) result.get(j);
//
//                Query queryid_U = new Query(new Criteria("_id").is(lBUser.get("id_U")).and("info").exists(true));
//                User user = mongoTemplate.findOne(queryid_U, User.class);
//                if (user == null) {
//                    return;
//                } else {
//
//                    //当前用户和用户数组中某个一样的话，就不推送给此用户
//                    if (!logL.getId_U().equals(user.getId())) {
//                        if (user.getInfo().get("id_APP") == null) {
//                            continue;
//                        }
//                        boolean id_APP = AppPushUtil.getClientIdStatus((String) user.getInfo().get("id_APP"));
//                        if (id_APP) {
//                            //不在线     这里应该进入一个方法，这个方法是获取redis数据的，数据充当
//                            String title = logL.getLogType().getString(0);  //通知栏标题
//                            String text = logL.getZcndesc();   //内容
////                            String transmissionContent = "666";    //透传消息
//                            AppPushUtil.pushSingle((String) user.getInfo().get("id_APP"), title, text, null);
////                            ("发送推送...");
//                            System.out.println("发送推送...");
//                        } else {
//                            //在线，走websocket
////                            // 发送日志
////                            WebSocketServer.sendLog(logL);
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * 发送日志方法，带推送
     * @param logL	需要发送的日志
     * @return void  返回结果: 结果
     * @author tang
     * @ver 1.0.0
     * ##Updated: 2020/8/6 9:26
     */
////    @SuppressWarnings("unchecked")
//    public static void sendLog(LogFlow logL, StringRedisTemplate redisTemplate1
//            , RestHighLevelClient client, MongoTemplate mongoTemplate){
//
//
//        List<String> id_APPList = redisTemplate1.opsForList().range("userPush_" + logL.getId_C() + "_" + logL.getId(), 0, -1);
//
//        for (int i = 0; i < id_APPList.size(); i++) {
//
////            boolean clientID = AppPushUtil.getClientIdStatus(id_APPList.get(i));
////            if (!clientID) {
////                //不在线     这里应该进入一个方法，这个方法是获取redis数据的，数据充当
////                String title = logL.getLogType();  //通知栏标题
////                String text = logL.getZcndesc();   //内容
////                            String transmissionContent = "666";    //透传消息
////                AppPushUtil.pushSingle(id_APPList.get(i), title, text, null);
////                            ("发送推送...");
//                System.out.println("推送!!!--------------");
////            } else {
////                //在线，走websocket
////                // 发送日志
//////                            WebSocketServer.sendInfo(logL);
////            }
//        }
//    }

}