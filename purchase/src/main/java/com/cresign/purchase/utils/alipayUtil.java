package com.cresign.purchase.utils;

/**
 * ##ClassName: alipayUtil
 * ##description: 作者很懒什么也没写
 * @author tang
 * ##Updated: 2020/9/23 9:21
 * @ver 1.0.0
 */
public class alipayUtil {

//    public void doPost (HttpServletRequest httpRequest,
//                            HttpServletResponse httpResponse)   throws ServletException, IOException {
//        AlipayClient alipayClient =  new DefaultAlipayClient( "https://openapi.alipay.com/gateway.do"
//                , APP_ID
//                , APP_PRIVATE_KEY
//                , FORMAT
//                , CHARSET
//                , ALIPAY_PUBLIC_KEY
//                , SIGN_TYPE);  //获得初始化的AlipayClient
//        AlipayTradePagePayRequest alipayRequest =  new AlipayTradePagePayRequest(); //创建API对应的request
//        alipayRequest.setReturnUrl( "http://domain.com/CallBack/return_url.jsp" );
//        alipayRequest.setNotifyUrl( "http://domain.com/CallBack/notify_url.jsp" ); //在公共参数中设置回跳和通知地址
//        alipayRequest.setBizContent( "{"  +
//                "    \"out_trade_no\":\"20150320010101001\","  +
//                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\","  +
//                "    \"total_amount\":88.88,"  +
//                "    \"subject\":\"Iphone6 16G\","  +
//                "    \"body\":\"Iphone6 16G\","  +
//                "    \"passback_params\":\"merchantBizType%3d3C%26merchantBizNo%3d2016010101111\","  +
//                "    \"extend_params\":{"  +
//                "    \"sys_service_provider_id\":\"2088511833207846\""  +
//                "    }" +
//                "  }" ); //填充业务参数
//        String form= "" ;
//        try  {
//            form = alipayClient.pageExecute(alipayRequest).getBody();  //调用SDK生成表单
//        }  catch  (AlipayApiException e) {
//            e.printStackTrace();
//        }
//        httpResponse.setContentType( "text/html;charset="  + CHARSET);
//        httpResponse.getWriter().write(form); //直接将完整的表单html输出到页面
//        httpResponse.getWriter().flush();
//        httpResponse.getWriter().close();
//    }


}
