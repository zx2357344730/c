package com.cresign.purchase.utils;

import com.cresign.tools.md5.MD5Util;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.util.Date;

@Component
public class CosUpload {

    private static final String bucketName = "cresign-1253919880";

    private static final String bucketName2 = "cfiles-1253919880";

//    @Value("${cosBrowser.appId}")
    private static String secretId = "AKIDCG7nGXPTGrFSSFPCTYRDm8II3d3Ij2Wk";

//    @Value("${cosBrowser.secretKey}")
    private static String secretKey = "7zc2DF2ZcR1QaVIvIPHtFLCoJqBWcT5V";

    // 1 初始化用户身份信息(secretId, secretKey，可在腾讯云后台中的API密钥管理中查看！
    public static COSCredentials cred = new BasicCOSCredentials(secretId,secretKey);

    // 2 设置bucket的区域, COS地域的简称请参照
    // https://cloud.tencent.com/document/product/436/6224，根据自己创建的存储桶选择地区
    public static ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));

    /**
     * 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口 大文件上传请参照 API 文档高级 API 上传
     *
     * ##Params: localFile
     */
    public static String uploadPE(File localFile, String path, String name, int nameIS, Date expiration) throws CosClientException{

        // 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        // 获取文件名称
        String fileName = localFile.getName();

        URL url = null;

        try {

            // 获取图片后缀名
            String substring = fileName.substring(fileName.lastIndexOf("."));

            // 指定要上传到 COS 上的路径
            fileName = path + name + substring;

            // 放入文件
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName2, fileName , localFile);

            // 上传腾讯云cos服务器
            cosclient.putObject(putObjectRequest);

            if (nameIS == 1) {
                // 获取到图片的httpUrl
                if (null != expiration) {

                    url = cosclient.generatePresignedUrl(bucketName2, fileName, expiration);

                } else {

                    Date expirationTime = new Date(System.currentTimeMillis() + 1000*24*365*100 );

                    url = cosclient.generatePresignedUrl(bucketName2, fileName, expirationTime);

                }
            } else {
                return name + substring;
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            // 关闭客户端(关闭后台线程)
            cosclient.shutdown();
        }

        if (null != url) {
            return url.toString();
        }
        return null;
    }

//    /**
//     * 图片上传
//     * ##Params: localFile
//     * ##Params: filePath
//     * ##Params: expiration
//     * ##Params: type              是大图片还是小图片
//     * ##Params: name              图片名称
//     * ##return:
//     * @throws CosClientException
//     */
//    public static String upload2(File localFile, String filePath, Date expiration, String type, String name) throws CosClientException{
//
//        // 生成cos客户端
//        COSClient cosclient = new COSClient(cred, clientConfig);
//
//        // 获取文件名称
//        String fileName = localFile.getName();
//
//        COSObject object = null;
//
//        try {
//
//            // 获取图片后缀名
//            String substring = fileName.substring(fileName.lastIndexOf("."));
//
//            String Md5Img = MD5Util.getFileMD5(localFile);
//
//            if (type.equals("reg")) {
//
//                // 指定要上传到 COS 上的路径
//                fileName = filePath + "/"+ Md5Img + substring;
//
//            } else {
//
//                fileName = filePath + "/"+ name;
//
//            }
//
//            // 放入文件
//            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName , localFile);
//
//            // 上传腾讯云cos服务器
//            cosclient.putObject(putObjectRequest);
//
//
//
//            if (null != expiration) {
//
//                object = cosclient.getObject(bucketName, fileName);
//
//            } else {
//
//                object = cosclient.getObject(bucketName, fileName);
//
//            }
//
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        } finally {
//
//            // 关闭客户端(关闭后台线程)
//            cosclient.shutdown();
//        }
//
//        return object.getKey();
//    }
//
//
//    /**
//     * 删除临时文件
//     * ##Params: files
//     */
//    public static void deleteFile(File... files) {
//        for (File file : files) {
//            if (file.exists()) {
//                file.delete();
//            }
//        }
//    }

}
