package com.cresign.tools.dbTools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cresign.tools.enumeration.DateEnum;
import com.cresign.tools.md5.MD5Util;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.cvm.v20170312.models.AssociateSecurityGroupsResponse;
import com.tencentcloudapi.vpc.v20170312.VpcClient;
import com.tencentcloudapi.vpc.v20170312.models.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CosUpload {

    private static final String bucketName = "cresign-1253919880";

    private static final String bucketName2 = "cfiles-1253919880";


    @Value("${cosBrowser.appId}")
    private static final String secretId = "AKIDCG7nGXPTGrFSSFPCTYRDm8II3d3Ij2Wk";

    @Value("${cosBrowser.secretKey}")
    private static final String secretKey = "7zc2DF2ZcR1QaVIvIPHtFLCoJqBWcT5V";

    // 1 初始化用户身份信息(secretId, secretKey，可在腾讯云后台中的API密钥管理中查看！
    public static COSCredentials cred = new BasicCOSCredentials(secretId,secretKey);

    // 2 设置bucket的区域, COS地域的简称请参照
    // https://cloud.tencent.com/document/product/436/6224，根据自己创建的存储桶选择地区
    public static Region region = new Region("ap-guangzhou");
    public static ClientConfig clientConfig = new ClientConfig(region);

    /**
     * 文件上传cos
     * @Author Rachel
     * @Date 2021/10/16
     * ##param file 文件
     * ##param path 路径
     * @Return java.lang.String
     * @Card
     **/
    public static String uploadCresignPic(File file, String path, String name) throws IOException {
        COSClient cosClient = new COSClient(cred, clientConfig);
        String fileName = file.getName();
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        if (name == null) {
            String md5 = MD5Util.getFileMD5(file);
            fileName = path + md5 + prefix;
        } else {
            fileName = path + name + prefix;
        }
        boolean bool = cosClient.doesObjectExist(bucketName, fileName);
//        String url = "https://" + bucketName + ".cos.ap-guangzhou.myqcloud.com/" + fileName;
        String url = fileName;

        if (!bool) {
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            cosClient.putObject(request);
        }
        cosClient.shutdown();
        file.delete();
        return url;
    }

    public static JSONObject uploadCresignStat(File file, String path, String name) {
        COSClient cosClient = new COSClient(cred, clientConfig);
        String fileName = file.getName();
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        if (name == null) {
            String md5 = "";
            try {
                md5 = MD5Util.getFileMD5(file);
            } catch (Exception e)
            {

            }
            fileName = path + md5 + prefix;
        } else {
            fileName = path + name + prefix;
        }
        boolean bool = cosClient.doesObjectExist(bucketName, fileName);
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("url", "https://" + bucketName + ".cos.ap-guangzhou.myqcloud.com/" + fileName);
//        jsonResult.put("url", fileName);

        if (bool) {
            jsonResult.put("size", 0);
        } else {
            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            cosClient.putObject(request);
            jsonResult.put("size", file.length());
        }
        cosClient.shutdown();
        file.delete();
        return jsonResult;
    }

    public static JSONObject uploadCFiles(File file, String path, String name) throws IOException {
        COSClient cosClient = new COSClient(cred, clientConfig);
        String fileName = file.getName();
        String prefix = fileName.substring(fileName.lastIndexOf("."));
        if (name == null) {
            String md5 = MD5Util.getFileMD5(file);
            fileName = path + md5 + prefix;
        } else {
            fileName = path + name + prefix;
        }
        boolean bool = cosClient.doesObjectExist(bucketName2, fileName);
        JSONObject jsonResult = new JSONObject();
//        jsonResult.put("url", "https://" + bucketName2 + ".cos.ap-guangzhou.myqcloud.com" + fileName);
        jsonResult.put("url", fileName);
        if (bool) {
            jsonResult.put("size", 0);
        } else {
            PutObjectRequest request = new PutObjectRequest(bucketName2, fileName, file);
            cosClient.putObject(request);
            jsonResult.put("size", file.length());
        }
        return jsonResult;
    }


    /**
     * 删除单个文件
     * ##Params: delKey
     */
    public static long delCresign(String keyPath) {
        System.out.println("keyPath=" + keyPath);
        if (keyPath != null && !keyPath.equals("")) {
            // 3 生成cos客户端
            COSClient cosclient = new COSClient(cred, clientConfig);
            boolean bool = cosclient.doesObjectExist(bucketName, keyPath);
            if (bool) {
                long size = getCresignSize(keyPath);
                cosclient.deleteObject(bucketName, keyPath);
                cosclient.shutdown();
                return size;
            }
        }
        return 0;
    }
    public static long delCFiles(String keyPath) {
        System.out.println("keyPath=" + keyPath);
        if (keyPath != null && !keyPath.equals("")) {
            // 3 生成cos客户端
            COSClient cosclient = new COSClient(cred, clientConfig);
            boolean bool = cosclient.doesObjectExist(bucketName2, keyPath);
            if (bool) {
                long size = getCFilesSize(keyPath);
                cosclient.deleteObject(bucketName2, keyPath);
                cosclient.shutdown();
                return size;
            }
        }
        return 0;
    }

    public static String getCFiles(String keyPath) {
        COSClient cosClient = new COSClient(cred, clientConfig);
        //1小时过期
        Date expirationTime = new Date(System.currentTimeMillis() + 1000 * 60 * 60);
        String url = cosClient.generatePresignedUrl(bucketName2, keyPath, expirationTime, HttpMethodName.GET).toString();
        return url;
    }

    public static long getCresignSize(String keyPath){
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // Object是否存在
        boolean booResult = cosclient.doesObjectExist(bucketName, keyPath);
        if (booResult){
            ObjectMetadata objectMetadata = cosclient.getObjectMetadata(bucketName, keyPath);
            //获取文件字节大小
            return objectMetadata.getContentLength();
        }
        return 0;
    }

    public static long getCFilesSize(String keyPath){
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // Object是否存在
        boolean booResult = cosclient.doesObjectExist(bucketName2, keyPath);
        if (booResult){
            ObjectMetadata objectMetadata = cosclient.getObjectMetadata(bucketName2, keyPath);
            //获取文件字节大小
            return objectMetadata.getContentLength();
        }
        return 0;
    }

    public static void copyCresign(String sourceObjectName,String destinationObjectName){
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 设置高级接口的分块上传阈值和分块大小为10MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(10 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(10 * 1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(region, "cfiles-1253919880", sourceObjectName,
                "cfiles-1253919880", destinationObjectName);
        try {

            //Copy copy = transferManager.copy(copyObjectRequest, srcCOSClient, null);

            // 返回一个异步结果 copy, 可同步的调用 waitForCopyResult 等待 copy 结束, 成功返回 CopyResult, 失败抛出异常.
            //CopyResult copyResult = copy.waitForCopyResult();
            // 获取拷贝生成对象的CRC64
            //String crc64Ecma = copyResult.getCrc64Ecma();
            // 关闭 TransferManger
            cosClient.shutdown();

        } catch (CosClientException  e) {
            e.printStackTrace();
        }

    }

    public static void copyCFiles(String sourceObjectName,String destinationObjectName){
        COSClient cosClient = new COSClient(cred, clientConfig);
        // 线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);
        // 设置高级接口的分块上传阈值和分块大小为10MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(10 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(10 * 1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);



        CopyObjectRequest copyObjectRequest =
                new CopyObjectRequest(region, "cfiles-1253919880", sourceObjectName,
                        "cfiles-1253919880", destinationObjectName);
        try {

            //Copy copy = transferManager.copy(copyObjectRequest, srcCOSClient, null);

            // 返回一个异步结果 copy, 可同步的调用 waitForCopyResult 等待 copy 结束, 成功返回 CopyResult, 失败抛出异常.
            //CopyResult copyResult = copy.waitForCopyResult();
            // 获取拷贝生成对象的CRC64
            //String crc64Ecma = copyResult.getCrc64Ecma();
            // 关闭 TransferManger
            cosClient.shutdown();

        } catch (CosClientException  e) {
            e.printStackTrace();
        }

    }

    /**--------------------------------------------------------------------------------------*/


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
//        try {
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
     * 查看文件是否存在
     * ##author: Jevon
     * ##Params: keyPath
     * ##version: 1.0
     * ##updated: 2021/3/17 14:14
     * ##Return: boolean
     */
    public static boolean doesObjectExist (String keyPath){
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        // Object是否存在
        boolean booResult = cosclient.doesObjectExist(bucketName2, keyPath);



        return booResult ;

    }


    public static File downloadCresign(String path) throws Exception {
        COSClient cosClient = new COSClient(cred, clientConfig);
        //创建 TransferManager
        TransferManager transferManager = new TransferManager(cosClient);
        String[] pathSplit = path.split("\\.");
        String prefix = pathSplit[pathSplit.length - 1];
//        String pathFile = "/Users/fe2/Desktop/";
        String pathFile = "/home/jar/images/";

        File file = new File(pathFile + System.currentTimeMillis() + "." + prefix);
        GetObjectRequest request = new GetObjectRequest(bucketName, path);
        try {
            //返回一个异步结果 Download
            Download download = transferManager.download(request, file);
            //等待下载完成
            download.waitForCompletion();
            return file;
        } catch (InterruptedException e) {
            throw new Exception();
        } finally {
            transferManager.shutdownNow();
        }
    }


    //删除聊天时上传的文件，删除指定一个文件夹
    public static void  delDownload(String date){

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
        // 3 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);

        //参考文档
        //https://www.alibabacloud.com/help/zh/doc-detail/84842.htm?spm=a2c63.p38356.879954.6.785955d4edpmZ2#t22290.html
        //https://cloud.tencent.com/document/product/436/35215#.E5.88.A0.E9.99.A4.E6.96.87.E4.BB.B6.E5.A4.B9.E5.8F.8A.E5.85.B6.E6.96.87.E4.BB.B6
        // 填写不包含Bucket名称在内的目录完整路径。例如Bucket下testdir目录的完整路径为testdir/。
        final String prefix = "temp/"+date+"/";

        // 删除目录及目录下的所有文件。
        String nextMarker = null;
        ObjectListing objectListing = null;
        do {
            // deliter表示分隔符, 设置为/表示列出当前目录下的object, 设置为空表示列出所有的object（包括文件夹下面的所有）
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName,prefix,nextMarker,"",1000)
                    .withPrefix(prefix)
                    .withMarker(nextMarker);

            objectListing = cosClient.listObjects(listObjectsRequest);
            if (objectListing.getObjectSummaries().size() > 0) {
                ArrayList<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
                for (COSObjectSummary  s : objectListing.getObjectSummaries()) {
                    keys.add(new DeleteObjectsRequest.KeyVersion(s.getKey()));
                }
                DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keys);
                cosClient.deleteObjects(deleteObjectsRequest);
            }
            nextMarker = objectListing.getNextMarker();
        } while (objectListing.isTruncated());

        // 关闭OSSClient。
        cosClient.shutdown();
    }




    /**
     * 删除单个文件
     * ##Params: delKey
     */
    public static void delFile(String delKey) {

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        // Object是否存在
        boolean booResult = cosclient.doesObjectExist(bucketName2, delKey);


        if (booResult){

            cosclient.deleteObject(bucketName2, delKey);

            cosclient.shutdown();

        }

    }


    /**
     * OSS(阿里接口)拷贝文件  只能拷贝1G以下
     * ##author: Jevon
     * ##Params: sourceObjectName       原路径
     * ##Params: destinationObjectName  目标路径
     * ##version: 1.0
     * ##updated: 2021/2/27 14:33
     * ##Return: void
     */
    public static void ossCopyFile(String sourceObjectName,String destinationObjectName) {

        COSCredentials cred = new BasicCOSCredentials("AKIDCG7nGXPTGrFSSFPCTYRDm8II3d3Ij2Wk", "7zc2DF2ZcR1QaVIvIPHtFLCoJqBWcT5V");

        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);

        // Object是否存在
        boolean booResult = cosclient.doesObjectExist(bucketName2, sourceObjectName);


        if (booResult){
            cosclient.copyObject("cfiles-1253919880", sourceObjectName,
                    "cfiles-1253919880", destinationObjectName);
        }

        //关闭OSSClient。
        cosclient.shutdown();

    }

    /**
     * cos(腾讯接口)拷贝文件  Copy 接口支持根据对象大小自动选择简单复制或者分块复制，用户无需关心复制的文件大小
     * ##author: Jevon
     * ##Params: sourceObjectName       原路径
     * ##Params: destinationObjectName  目标路径
     * ##version: 1.0
     * ##updated: 2021/2/22 15:33
     * ##Return: void
     */
    public static void cosCopyFile(String sourceObjectName,String destinationObjectName){

        COSCredentials cred = new BasicCOSCredentials("AKIDCG7nGXPTGrFSSFPCTYRDm8II3d3Ij2Wk", "7zc2DF2ZcR1QaVIvIPHtFLCoJqBWcT5V");

        Region srcBucketRegion = new Region("ap-guangzhou");

        COSClient srcCOSClient = new COSClient(cred, new ClientConfig(srcBucketRegion));

        // 线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(srcCOSClient, threadPool);
        // 设置高级接口的分块上传阈值和分块大小为10MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(10 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(10 * 1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);



        CopyObjectRequest copyObjectRequest =
                new CopyObjectRequest(srcBucketRegion, "cfiles-1253919880", sourceObjectName,
                        "cfiles-1253919880", destinationObjectName);
        try {

            //Copy copy = transferManager.copy(copyObjectRequest, srcCOSClient, null);

            // 返回一个异步结果 copy, 可同步的调用 waitForCopyResult 等待 copy 结束, 成功返回 CopyResult, 失败抛出异常.
            //CopyResult copyResult = copy.waitForCopyResult();
            // 获取拷贝生成对象的CRC64
            //String crc64Ecma = copyResult.getCrc64Ecma();
            // 关闭 TransferManger
            srcCOSClient.shutdown();

        } catch (CosClientException  e) {
            e.printStackTrace();
        }

    }


    public static long selectList(String path) {
        // 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(path);
        listObjectsRequest.setDelimiter("");
        listObjectsRequest.setMaxKeys(1000);
        ObjectListing objectListing = null;
        long size = 0L;
        do {
            try {
                objectListing = cosclient.listObjects(listObjectsRequest);
            } catch (CosServiceException e) {
                e.printStackTrace();
            } catch (CosClientException e) {
                e.printStackTrace();
            }
            // common prefix表示表示被delimiter截断的路径, 如delimter设置为/, common prefix则表示所有子目录的路径
            List<String> commonPrefixs = objectListing.getCommonPrefixes();

            // object summary表示所有列出的object列表
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                // 文件的路径key
                String key = cosObjectSummary.getKey();
                // 文件的etag
                String etag = cosObjectSummary.getETag();
                // 文件的长度
                long fileSize = cosObjectSummary.getSize();
                // 文件的存储类型
                String storageClasses = cosObjectSummary.getStorageClass();

                size += fileSize;
                System.out.println("路径与文件名："+key);
                System.out.println("etag："+etag);
                System.out.println("文件大小:"+fileSize);
                System.out.println("类型："+storageClasses);
                System.out.println();
            }
            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());
        return size;
    }

    public static JSONObject getScurity() throws TencentCloudSDKException {
        Credential cred = new Credential(secretId, secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("vpc.tencentcloudapi.com");
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        VpcClient client = new VpcClient(cred, "ap-guangzhou", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        DescribeSecurityGroupPoliciesRequest req = new DescribeSecurityGroupPoliciesRequest();
        req.setSecurityGroupId("sg-caueeii1");
        // 返回的resp是一个DescribeSecurityGroupPoliciesResponse的实例，与请求对象对应
        DescribeSecurityGroupPoliciesResponse resp = client.DescribeSecurityGroupPolicies(req);
        // 输出json格式的字符串回包
        System.out.println(DescribeSecurityGroupPoliciesResponse.toJsonString(resp));
        return JSON.parseObject(AssociateSecurityGroupsResponse.toJsonString(resp));
    }

    public static Object updateScurity(Long index, String ip, String key) throws TencentCloudSDKException {
        Credential cred = new Credential(secretId, secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("vpc.tencentcloudapi.com");
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        VpcClient client = new VpcClient(cred, "ap-guangzhou", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        ReplaceSecurityGroupPolicyRequest req = new ReplaceSecurityGroupPolicyRequest();
        req.setSecurityGroupId("sg-caueeii1");
        SecurityGroupPolicySet securityGroupPolicySet1 = new SecurityGroupPolicySet();

        SecurityGroupPolicy[] securityGroupPolicys1 = new SecurityGroupPolicy[1];
        SecurityGroupPolicy securityGroupPolicy1 = new SecurityGroupPolicy();
        securityGroupPolicy1.setPolicyIndex(index);
        securityGroupPolicy1.setProtocol("ALL");
        securityGroupPolicy1.setPort("ALL");
        securityGroupPolicy1.setCidrBlock(ip);
        securityGroupPolicy1.setAction("ACCEPT");
        securityGroupPolicy1.setPolicyDescription(key);
        securityGroupPolicy1.setModifyTime(DateUtils.getDateNow(DateEnum.DATE_TIME_FULL.getDate()));
        securityGroupPolicys1[0] = securityGroupPolicy1;

        securityGroupPolicySet1.setIngress(securityGroupPolicys1);

        req.setSecurityGroupPolicySet(securityGroupPolicySet1);

        // 返回的resp是一个ReplaceSecurityGroupPolicyResponse的实例，与请求对象对应
        ReplaceSecurityGroupPolicyResponse resp = client.ReplaceSecurityGroupPolicy(req);
        // 输出json格式的字符串回包
        System.out.println(ReplaceSecurityGroupPolicyResponse.toJsonString(resp));
        return ReplaceSecurityGroupPolicyResponse.toJsonString(resp);
    }
}
