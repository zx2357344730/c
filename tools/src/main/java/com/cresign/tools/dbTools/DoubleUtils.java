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
        if (num2 == 0.0)
        {
            return 0.0;
        }
        return decimal1.divide(decimal2, 6, BigDecimal.ROUND_DOWN).doubleValue();
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
     * 加
     * @Author Rachel
     * @Date 2022/09/14
     * @Param num1
     * @Param num2
     * @Return double
     * @Card
     **/
    public static double addAll(double... num) {
        BigDecimal result = new BigDecimal(Double.toString(num[0]));
        int length = num.length;
        for (int i = 1; i < length; i++) {
            BigDecimal decimal = new BigDecimal(Double.toString(num[i]));
            result = result.add(decimal);
        }
        return result.doubleValue();
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
    public static double subtractAll(double... num) {
        BigDecimal result = new BigDecimal(Double.toString(num[0]));
        int length = num.length;
        for (int i = 1; i < length; i++) {
            BigDecimal decimal = new BigDecimal(Double.toString(num[i]));
            result = result.subtract(decimal);
        }
        return result.doubleValue();
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
    public static double multiplyAll(double... num) {
        BigDecimal result = new BigDecimal(Double.toString(num[0]));
        int length = num.length;
        for (int i = 1; i < length; i++) {
            BigDecimal decimal = new BigDecimal(Double.toString(num[i]));
            result = result.multiply(decimal);
        }
        return result.doubleValue();
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
    public static double divideAll(int scale, int roundingMode, double... num) {
        BigDecimal result = new BigDecimal(Double.toString(num[0]));
        int length = num.length;
        for (int i = 1; i < length; i++) {
            BigDecimal decimal = new BigDecimal(Double.toString(num[i]));
            result = result.divide(decimal, scale, roundingMode);
        }
        return result.doubleValue();
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
    public static boolean doubleEquals(double num1, double num2) {
        boolean bool = compareTo(num1, num2) == 0 ? true: false;
        return bool;
    }
    public static boolean doubleGt(double num1, double num2) {
        boolean bool = compareTo(num1, num2) == 1 ? true: false;
        return bool;
    }
    public static boolean doubleGte(double num1, double num2) {
        boolean bool = compareTo(num1, num2) != -1 ? true: false;
        return bool;
    }
}
