package com.cresign.purchase.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.region.Region;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class QRCodeUtil {

    // 二维码颜色==黑色
    private static final int BLACK = 0xFF000000;
    // 二维码颜色==白色
    private static final int WHITE = 0xFFFFFFFF;
    // 二维码图片格式==jpg和png两种
    private static final List<String> IMAGE_TYPE = new ArrayList<>();

    static {
        IMAGE_TYPE.add("jpg");
        IMAGE_TYPE.add("png");
    }

    /**
     * zxing方式生成二维码
     * 注意：
     * 1,文本生成二维码的方法独立出来,返回image流的形式,可以输出到页面
     * 2,设置容错率为最高,一般容错率越高,图片越不清晰, 但是只有将容错率设置高一点才能兼容logo图片
     * 3,logo图片默认占二维码图片的20%,设置太大会导致无法解析
     *
     * ##Params: content  二维码包含的内容，文本或网址
     * ##Params: path     生成的二维码图片存放位置
     * ##Params: size     生成的二维码图片尺寸 可以自定义或者默认（250）
     * ##Params: logoPath logo的存放位置
     */
    public static boolean zxingCodeCreate(String content, String path, Integer size, String logoPath) {
        try {
            //图片类型
            String imageType = "jpg";
            //获取二维码流的形式，写入到目录文件中
            BufferedImage image = getBufferedImage(content, size, logoPath);
            //获得随机数
            Random random = new Random();
            //生成二维码存放文件
            File file = new File(path+random.nextInt(1000)+".jpg");
            if (!file.exists()) {
                file.mkdirs();
            }
            ImageIO.write(image, imageType, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * zxing方式生成二维码
     * 注意：
     * 1,文本生成二维码的方法独立出来,返回image流的形式,可以输出到页面
     * 2,设置容错率为最高,一般容错率越高,图片越不清晰, 但是只有将容错率设置高一点才能兼容logo图片
     * 3,logo图片默认占二维码图片的20%,设置太大会导致无法解析
     *
     * ##Params: content  二维码包含的内容，文本或网址
     * ##Params: path     生成的二维码图片存放位置
     * ##Params: size     生成的二维码图片尺寸 可以自定义或者默认（250）
     * ##Params: logoPath logo的存放位置
     */
    public static String zxingCodeCreateTang(String content, String path, Integer size, String logoPath, String id) {
        try {
            //图片类型
            String imageType = "jpg";
            //获取二维码流的形式，写入到目录文件中
            BufferedImage image = getBufferedImage(content, size, logoPath);
            //生成二维码存放文件
            File file = File.createTempFile(id, ".jpg");
            ImageIO.write(image, imageType, file);
            // 调用腾讯云工具上传文件
            // 并返回url给前端
            CosUpload.uploadPE(file, path+"/", id, 0, null);
            if (file.exists()) {
                file.delete();
            }
            return "https://cfiles-1253919880.cos.ap-guangzhou.myqcloud.com/"+path+"/"+id+".jpg?"+getImg(path + "/" + id + ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getImg(String key){
        String secretId = "AKIDCG7nGXPTGrFSSFPCTYRDm8II3d3Ij2Wk";
        String secretKey = "7zc2DF2ZcR1QaVIvIPHtFLCoJqBWcT5V";

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        //设置过期时间 3分钟
        Date expiredTime = new Date(System.currentTimeMillis() + 360000L * 1000L);

        // 要签名的 key, 生成的签名只能用于对应此 key 的上传 这里路径拼接为上传至COS的head文件夹中

        return cosclient.generatePresignedUrl("cfiles-1253919880", key, expiredTime, HttpMethodName.GET).getQuery();
    }

    /**
     * 二维码流的形式，包含文本内容
     *
     * ##Params: content  二维码文本内容
     * ##Params: size     二维码尺寸
     * ##Params: logoPath logo的存放位置
     * ##return:
     */
    public static BufferedImage getBufferedImage(String content, Integer size, String logoPath) {
        if (size == null || size <= 0) {
            size = 250;
        }
        BufferedImage image = null;
        try {
            // 设置编码字符集
            Map<EncodeHintType, Object> hints = new HashMap<>();
            //设置编码
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            //设置容错率最高
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            // 1、生成二维码
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            // 2、获取二维码宽高
            int codeWidth = bitMatrix.getWidth();
            int codeHeight = bitMatrix.getHeight();
            // 3、将二维码放入缓冲流
            image = new BufferedImage(codeWidth, codeHeight, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < codeWidth; i++) {
                for (int j = 0; j < codeHeight; j++) {
                    // 4、循环将二维码内容定入图片
                    image.setRGB(i, j, bitMatrix.get(i, j) ? BLACK : WHITE);
                }
            }
            //判断是否写入logo图片
            if (logoPath != null && !"".equals(logoPath)) {
                File logoPic = new File(logoPath);
                if (logoPic.exists()) {
                    Graphics2D g = image.createGraphics();
                    BufferedImage logo = ImageIO.read(logoPic);
                    int widthLogo = logo.getWidth(null) > image.getWidth() * 2 / 10 ? (image.getWidth() * 2 / 10) : logo.getWidth(null);
                    int heightLogo = logo.getHeight(null) > image.getHeight() * 2 / 10 ? (image.getHeight() * 2 / 10) : logo.getHeight(null);
                    int x = (image.getWidth() - widthLogo) / 2;
                    int y = (image.getHeight() - heightLogo) / 2;
                    // 开始绘制图片
                    g.drawImage(logo, x, y, widthLogo, heightLogo, null);
                    g.drawRoundRect(x, y, widthLogo, heightLogo, 15, 15);
                    //边框宽度
                    g.setStroke(new BasicStroke(2));
                    //边框颜色
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, widthLogo, heightLogo);
                    g.dispose();
                    logo.flush();
                    image.flush();
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    /**
     * 给二维码图片添加Logo
     *
     * ##Params: qrPic   二维码图片
     * ##Params: logoPic logo图片
     * ##Params: path    合成后的图片存储目录
     */
    public static boolean zxingCodeCreate(File qrPic, File logoPic, String path) {
        try {
            String imageType = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
            if (!IMAGE_TYPE.contains(imageType)) {
                return false;
            }

            if (!qrPic.isFile() && !logoPic.isFile()) {
                return false;
            }

            //读取二维码图片，并构建绘图对象

            BufferedImage image = ImageIO.read(qrPic);
            Graphics2D g = image.createGraphics();
            //读取Logo图片
            BufferedImage logo = ImageIO.read(logoPic);
            //设置logo的大小,最多20%0
            int widthLogo = logo.getWidth(null) > image.getWidth() * 2 / 10 ? (image.getWidth() * 2 / 10) : logo.getWidth(null);
            int heightLogo = logo.getHeight(null) > image.getHeight() * 2 / 10 ? (image.getHeight() * 2 / 10) : logo.getHeight(null);
            // 计算图片放置位置，默认在中间
            int x = (image.getWidth() - widthLogo) / 2;
            int y = (image.getHeight() - heightLogo) / 2;
            // 开始绘制图片
            g.drawImage(logo, x, y, widthLogo, heightLogo, null);
            g.drawRoundRect(x, y, widthLogo, heightLogo, 15, 15);
            //边框宽度
            g.setStroke(new BasicStroke(2));
            //边框颜色
            g.setColor(Color.WHITE);
            g.drawRect(x, y, widthLogo, heightLogo);
            g.dispose();
            logo.flush();
            image.flush();
            File newFile = new File(path);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            ImageIO.write(image, imageType, newFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 二维码的解析方法
     *
     * ##Params: path 二维码图片目录
     * ##return:
     */
    public static Result zxingCodeAnalyze(String path) {
        try {
            MultiFormatReader formatReader = new MultiFormatReader();
            File file = new File(path);
            if (file.exists()) {
                BufferedImage image = ImageIO.read(file);
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                Binarizer binarizer = new HybridBinarizer(source);
                BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
                Map hints = new HashMap();
                hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                Result result = formatReader.decode(binaryBitmap, hints);
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
