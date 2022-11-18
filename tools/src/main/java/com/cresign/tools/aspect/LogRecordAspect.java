//package com.cresign.tools.aspect;
//
//import com.cresign.tools.ip.GetUserIpUtil;
//import com.cresign.tools.uuid.UUID19;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.MDC;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//
//import javax.servlet.http.HttpServletRequest;
//
//@Aspect
//@Configuration//定义一个切面
//@Slf4j
//public class LogRecordAspect {
//
//    @Autowired
//    private HttpServletRequest request;
//
//    // 定义切点Pointcut
//    @Pointcut("execution(* com.cresign.*.controller..*.*(..))")
//    public void excludeService() {
//    }
//
//
////    @Around("excludeService()")
////    public Object apiAround(ProceedingJoinPoint pjp) throws Throwable {
////
////        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
////        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
////        HttpServletRequest request = sra.getRequest();
////
////        String url = request.getRequestURL().toString();
////        String method = request.getMethod();
////        String queryString = request.getQueryString();
////        Object[] args = pjp.getArgs();
////        String params = "";
////
////
////        List<Map<String, Object>> headerList = new ArrayList<>();
////        // 获取请求头信息
////        Enumeration<String> headNames = request.getHeaderNames();
////        while (headNames.hasMoreElements()) {
////            String headerName = headNames.nextElement();
////            Map<String, Object> headerMap = new HashMap<>();
////            headerMap.put("headerName", headerName);
////            headerMap.put("headerVal", request.getHeader(headerName));
////            headerList.add(headerMap);
////        }
////
////
////        log.info("请求开始===地址:" + url);
////        log.info("请求开始===类型:" + method);
////
////
////        //获取请求参数集合并进行遍历拼接
////        if (args.length > 0) {
////            if ("POST".equals(method)) {
////                Object object = args[0];
//////                Map map = getKeyAndValue(object);
//////                params = JSON.toJSONString(map);
////
////
////                log.info("请求开始===头部:" + headerList);
////                log.info("请求开始===参数:" + object);
////            } else if ("GET".equals(method)) {
////                params = queryString;
////                log.info("请求开始===参数:" + params);
////            }
////        }
////
////
////        // result的值就是被拦截方法的返回值
////        Object result = pjp.proceed();
////
////        log.info("请求结束===返回值:" + JSONObject.toJSON(result));
//////        MDC.clear();
////        return result;
////    }
//
//    @Around("excludeService()")
//    public Object apiAround(ProceedingJoinPoint pjp) throws Throwable {
//
//        MDC.put("uniqueId", UUID19.uuid());
//        MDC.put("clientIp", GetUserIpUtil.getIpAddress(request));
//
//
//        Object result = pjp.proceed();
//
//        MDC.clear();
//        return result;
//
//    }
//
//
////    @Pointcut("execution(* com.cresign.*.service.impl.*.*(..)))")
////    public void pointcut() {
////    }
////
////    @Around("pointcut()")
////    public Object around(ProceedingJoinPoint pjp) throws Throwable {
////        Signature signature = pjp.getSignature();
////        MethodSignature methodSignature = (MethodSignature) signature;
////        Method targetMethod = methodSignature.getMethod();
//////        ("classname:" + targetMethod.getDeclaringClass().getName());
//////        ("superclass:" + targetMethod.getDeclaringClass().getSuperclass().getName());
//////        ("isinterface:" + targetMethod.getDeclaringClass().isInterface());
//////        ("target:" + pjp.getTarget().getClass().getName());
//////        ("proxy:" + pjp.getThis().getClass().getName());
//////        ("method:" + targetMethod.getName());
////
////        Class[] parameterTypes = new Class[pjp.getArgs().length];
////        Object[] args = pjp.getArgs();
////        for (int i = 0; i < args.length; i++) {
////            if (args[i] != null) {
////                parameterTypes[i] = args[i].getClass();
////            } else {
////                parameterTypes[i] = null;
////            }
////        }
////        //获取代理方法对象
////        String methodName = pjp.getSignature().getName();
//////        Method method = pjp.getSignature().getDeclaringType().getMethod(methodName, parameterTypes);
////
//////        if(method.isAnnotationPresent(Log.class)){
//////            ("存在1");
//////        }
////        //获取实际方法对象,可以获取方法注解等
////        Method realMethod = pjp.getTarget().getClass().getDeclaredMethod(signature.getName(), targetMethod.getParameterTypes());
////
////        if (realMethod.isAnnotationPresent(Log.class)) {
////            realMethod.getAnnotation(Log.class).value();
////
////        }
////
////        saveLog(pjp, System.currentTimeMillis());
////        //执行该方法
////        Object object = pjp.proceed();
////
////        return object;
////    }
////
////    private void saveLog(ProceedingJoinPoint joinPoint, long time) {
////        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
////        Method method = signature.getMethod();
////        SysLog sysLog = new SysLog();
////        Log logAnnotation = method.getAnnotation(Log.class);
////        if (logAnnotation != null) {
////            // 注解上的描述
////            sysLog.setOperation(logAnnotation.value());
////        }
////        // 请求的方法名
////        String className = joinPoint.getTarget().getClass().getName();
////        String methodName = signature.getName();
////        sysLog.setMethod(className + "." + methodName + "()");
////        // 请求的方法参数值
////        Object[] args = joinPoint.getArgs();
////        // 请求的方法参数名称
////        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
////        String[] paramNames = u.getParameterNames(method);
////        if (args != null && paramNames != null) {
////            String params = "";
////            for (int i = 0; i < args.length; i++) {
////                params += "  " + paramNames[i] + ": " + args[i];
////            }
////            sysLog.setParams(params);
////        }
////
////        // 设置IP地址
////        sysLog.setIp(GetUserIpUtil.getIpAddress(request));
////        // 模拟一个用户名
////        sysLog.setUsername("mrbird");
////        sysLog.setTime((int) time);
////        sysLog.setCreateTime(new Date());
////        // 保存系统日志
////        log.info(JSON.toJSONString(sysLog));
////    }
////
////
////    public static Map<String, Object> getKeyAndValue(Object obj) {
////        Map<String, Object> map = new HashMap<>();
////        // 得到类对象
////        Class userCla = (Class) obj.getClass();
////        /* 得到类中的所有属性集合 */
////        Field[] fs = userCla.getDeclaredFields();
////        for (int i = 0; i < fs.length; i++) {
////            Field f = fs[i];
////            f.setAccessible(true); // 设置些属性是可以访问的
////            Object val = new Object();
////            try {
////                val = f.get(obj);
////                // 得到此属性的值
////                map.put(f.getName(), val);// 设置键值
////            } catch (IllegalArgumentException e) {
////                e.printStackTrace();
////            } catch (IllegalAccessException e) {
////                e.printStackTrace();
////            }
////
////        }
////
////        return map;
////    }
//}