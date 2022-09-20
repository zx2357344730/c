//package com.cresign.tools.md5;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.nio.MappedByteBuffer;
//import java.nio.channels.FileChannel;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//public class MD5Util {
//
//
//
//    /**
//     * ##description:用来实现MD5加密
//     * @return String
//     * @param inStr 要加密的字符串
//     *  思路，
//     *  实现32位的MD5码
//     */
//    public static String password(String inStr) {
//
//        //1.获取到MD5这个对象
//        MessageDigest md5 = null;
//        try {
//            md5 = MessageDigest.getInstance("MD5");
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//
//        //2.将字符串对象中的每一个字符转换为一个字符数组
//        char[] charArray = inStr.toCharArray();
//
//        //3.定义一个长度和字符数组一样的字节数组
//        byte[] byteArray = new byte[charArray.length];
//
//        //4.遍历字符数组 拿到每一个字符
//        for (int i = 0; i < charArray.length; i++) {
//            byteArray[i] = (byte) charArray[i];
//        }
//        if (md5 == null){
//            return null;
//        }
//
//        //5.把MD5找个对象对字节数组进行摘要，得到一个摘要字节数组
//        byte[] md5Byte = md5.digest(byteArray);
//
//        //6.把这个摘要数组当中的每一个字节转换成16进制，并且拼在一起就得到了MD5值
//        StringBuilder hexValue = new StringBuilder();
//
//        for (byte aMd5Byte : md5Byte) {
//
//            //7.把摘要字节数组中的每一个字节转换成16进制
//            int var = ((int) aMd5Byte) & 0xff;
//            if (var < 16) {
//
//                //如果生成的数字未满32位 需要在前面补0
//                hexValue.append("0");
//            }
//            hexValue.append(Integer.toHexString(var));
//        }
//        return hexValue.toString();
//    }
//
//    /**
//     * ##description:在MD5的基础上在进行一次加密，加盐加密
//     * @param inStr	要加密的字符串
//     * @return String
//     */
//    public static String twoPaswwWord(String inStr) {
//
//        //1.把要加密的字符串转换成字符数组
//        char[] charArray = inStr.toCharArray();
//        for (int i = 0; i < charArray.length; i++) {
//
//            //  ^ 异或运算符 如果charArray[i]和‘t’两个值不相同为 1 相同为 0
//            charArray[i] = (char)(charArray[i] ^ 't');
//        }
//        return new String(charArray);
//    }
//
//    /**
//     * ##description:与kL方法一致，但是是解密的方法，所谓负负得正
//     * @param inStr 解密字符串
//     * @return  结果
//     */
//    public static String jL(String inStr) {
//        char[] charArray = inStr.toCharArray();
//        for (int i = 0; i < charArray.length; i++) {
//
//            //  ^ 异或运算符 如果charArray[i]和‘t’两个值不相同为 1 相同为 0
//            charArray[i] = (char)(charArray[i] ^ 't');
//        }
//        return new String(charArray);
//    }
//
//    /**
//     * 把pwd一次性进行两次加密
//     * @param pwd   密码
//     * @return  加密后的密码
//     */
//    public static String pwdDisposableEncryption(String pwd){
//        String password = password(pwd);
//        if (password==null){
//            return null;
//        }
//        return twoPaswwWord(password);
//    }
//
//
//    /**
//     * 生成md5
//     * @param file 图片文件
//     * @return MD5值
//     */
//    public static String getFileMD5(File file) throws FileNotFoundException {
//
//        String value = null;
//
//        FileInputStream in = new FileInputStream(file);
//
//        try {
//
//            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
//
//            MessageDigest md5 = MessageDigest.getInstance("MD5");
//
//            md5.update(byteBuffer);
//
//            BigInteger bi = new BigInteger(1, md5.digest());
//
//            value = bi.toString(16);
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        } finally {
//
//            if(null != in) {
//
//                try {
//
//                    in.close();
//
//                } catch (IOException e) {
//
//                    e.printStackTrace();
//
//                }
//            }
//        }
//        return value;
//    }
//
//}