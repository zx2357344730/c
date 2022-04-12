package com.cresign.timer.utils;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.util.Date;

@Service
public class CosUpload {

    private static final String bucketName = "cresign-1253919880";

    private static final String bucketName2 = "cfiles-1253919880";

    // secretId
    private static final String secretId = "AKIDCG7nGXPTGrFSSFPCTYRDm8II3d3Ij2Wk";

    private static final String secretKey = "7zc2DF2ZcR1QaVIvIPHtFLCoJqBWcT5V";

    // 1 初始化用户身份信息(secretId, secretKey，可在腾讯云后台中的API密钥管理中查看！
    private static COSCredentials cred = new BasicCOSCredentials(secretId,secretKey);

    // 2 设置bucket的区域, COS地域的简称请参照
    // https://cloud.tencent.com/document/product/436/6224，根据自己创建的存储桶选择地区
    private static ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));

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

    /**
     * 删除临时文件
     * ##Params: files
     */
    public static void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 文件上传cos
     * @Author Rachel
     * @Date 2021/10/16
     * ##param file 文件
     * ##param path 路径
     * @Return java.lang.String
     * @Card
     **/
    public static String uploadFile(File file, String id_A) {
        COSClient cosClient = new COSClient(cred, clientConfig);
        String fileName = file.getName();
        String path = id_A + "/sum00s/" + fileName;
        PutObjectRequest request = new PutObjectRequest(bucketName2, path, file);
        cosClient.putObject(request);
        URL url = cosClient.generatePresignedUrl(new GeneratePresignedUrlRequest(bucketName, path, HttpMethodName.GET));
        cosClient.shutdown();
        file.delete();
        return url.toString();
    }
}