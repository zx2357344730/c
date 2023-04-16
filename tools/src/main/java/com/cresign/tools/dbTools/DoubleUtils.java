package com.cresign.tools.dbTools;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DoubleUtils {

    /**
     * 加
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Return double
     * @Card
     **/
    public static double add(double num1, double num2) {
        BigDecimal decimal1 = new BigDecimal(Double.toString(num1));
        BigDecimal decimal2 = new BigDecimal(Double.toString(num2));
        return decimal1.add(decimal2).doubleValue();
    }

    /**
     * 减
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Return double
     * @Card
     **/
    public static double subtract(double num1, double num2) {
        BigDecimal decimal1 = new BigDecimal(Double.toString(num1));
        BigDecimal decimal2 = new BigDecimal(Double.toString(num2));
        return decimal1.subtract(decimal2).doubleValue();
    }

    /**
     * 乘
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Return double
     * @Card
     **/
    public static double multiply(double num1, double num2) {
        BigDecimal decimal1 = new BigDecimal(Double.toString(num1));
        BigDecimal decimal2 = new BigDecimal(Double.toString(num2));
        return decimal1.multiply(decimal2).doubleValue();
    }

    /**
     * 除
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Return double
     * @Card
     **/
    public static double divide(double num1, double num2) {
        BigDecimal decimal1 = new BigDecimal(Double.toString(num1));
        BigDecimal decimal2 = new BigDecimal(Double.toString(num2));
        return decimal1.divide(decimal2).doubleValue();
    }

    /**
     * 除
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Param scale 保留小数位数
     * @Param roundingMode 模式: 0:正数向大舍入，负数向小舍入 / 1:正数向小舍入，负数向大舍入 / 2:向大舍入 / 3:向小舍入 / 4:四舍五入 / 5:五舍六入 / 6：向最接近的舍入
     * @Return double
     * @Card
     **/
    public static double divide(double num1, double num2, int scale, int roundingMode) {
        BigDecimal decimal1 = new BigDecimal(Double.toString(num1));
        BigDecimal decimal2 = new BigDecimal(Double.toString(num2));
        return decimal1.divide(decimal2, scale, roundingMode).doubleValue();
    }

    /**
     * 比较大小: -1:小于 / 0:等于 / 1:大于
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Return int
     * @Card
     **/
    public static int compareTo(double num1, double num2) {
        BigDecimal decimal1 = new BigDecimal(Double.toString(num1));
        BigDecimal decimal2 = new BigDecimal(Double.toString(num2));
        return decimal1.compareTo(decimal2);
    }
}
