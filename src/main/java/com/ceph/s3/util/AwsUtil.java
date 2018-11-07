package com.ceph.s3.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.StringUtils;
import com.ceph.s3.config.AwsConfig;
import com.xiaoleilu.hutool.io.FileUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * aws-java-sdk 对接ceph 文件上传下载工具
 */
public class AwsUtil {

    /**
     * S3连接
     * 无参数时，默认使用内网IP的方式
     *
     * @return
     */
    public static AmazonS3 s3Client() {
        AmazonS3 amazonS3 = AwsUtil.s3Client(AwsConfig.getHosts_Intranet(), AwsConfig.getAccessKey(), AwsConfig.getSecretKey());
        return amazonS3;
    }

    /**
     * S3 连接 ，自定义参数
     *
     * @param hosts
     * @param accessKey
     * @param secretKey
     * @return
     */
    public static AmazonS3 s3Client(final String hosts, final String accessKey, final String secretKey) {
        try {
            AWSCredentialsProvider credentialsProvider = new AWSCredentialsProvider() {
                //返回调用者可用于授权AWS请求的AWSCredentials。
                public AWSCredentials getCredentials() {
                    return new BasicAWSCredentials(accessKey, secretKey);
                }
                //强制此凭据提供程序刷新其凭据。
                @Override
                public void refresh() {
                }
            };
            //客户端配置选项，例如代理设置，用户代理字符串，最大重试次数等。
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            //设置协议为http
            clientConfiguration.setProtocol(Protocol.HTTP);

            /* com.amazonaws.client.builder.AwsClientBuilder: 所有特定于服务的客户端构建器的基类*/
            // 创建用于向服务（服务端点和签名区域）提交请求所需的配置容器
            AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(hosts, null);
            AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withEndpointConfiguration(endpointConfiguration).withClientConfiguration(clientConfiguration).build();
            return amazonS3;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 文件上传，自定义参数
     * @param hosts         ip+端口
     * @param accessKey     s3用户秘钥
     * @param secretKey     s3用户秘钥
     * @param bucketName    桶名称
     * @param multipartFile 文件
     * @return
     */
    public static HaoCangServerResponse upload(final String hosts, final String accessKey, final String secretKey, String bucketName, MultipartFile multipartFile) {
        AmazonS3 amazonS3 = s3Client(hosts, accessKey, secretKey);
        try {
            if (amazonS3 != null) {
                List<Bucket> bucketList = amazonS3.listBuckets();
                if (StringUtils.isNullOrEmpty(bucketName)) {
                    return HaoCangServerResponse.createByError("bucketName不能为空");
                }
                boolean isExistBucket = isExistBucket(bucketList, bucketName);
                //bucketName 的格式有要求，所以不能让用户随意创建操作，需要创建操作的话，必须要有格式化验证
                if (!isExistBucket) {
                    //amazonS3.createBucket(bucketName);
                    return HaoCangServerResponse.createByError(" bucketName参数异常");
                }
                String key = getkey(multipartFile.getOriginalFilename());
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(multipartFile.getSize());
                //上传
                amazonS3.putObject(bucketName, key, multipartFile.getInputStream(), metadata);
                //设置文件公有，用于url外网地址访问下载
                amazonS3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
                //获取永久生效的url
                URL url = getUrl(amazonS3,bucketName,key);
                Map<String, Object> result = new HashMap<>();
                result.put("bucketName", bucketName);
                result.put("key", key);
                result.put("url", url);
                return HaoCangServerResponse.createBySucess("上传成功", result);
            }
            return HaoCangServerResponse.createByError("S3连接异常");
        } catch (Exception e) {
            return HaoCangServerResponse.createByError("上传异常");
        } finally {
            shutDown(amazonS3);
        }
    }

    /**
     * 文件上传，使用默认配置，上传连接使用内网，返回外网的url
     * @param multipartFile
     * @return
     */
    public static HaoCangServerResponse upload(MultipartFile multipartFile){
        HaoCangServerResponse result = AwsUtil.upload(AwsConfig.getHosts_Intranet(), AwsConfig.getAccessKey(), AwsConfig.getSecretKey(), AwsConfig.getBucketName(), multipartFile);
        if(result.isSucess()){
            Map<String, Object> map = (Map<String, Object>) result.getData();
            Map<String, Object> newMap = replaceUrl(map,AwsConfig.getHosts_Intranet(),AwsConfig.getHosts_network());
            result.setData(newMap);
        }
        return result;
    }

    /**
     * 文件上传，直接对接File，用于本地测试
     * @param file
     * @return
     */
    public static HaoCangServerResponse upload(File file) {
        HaoCangServerResponse result = AwsUtil.upload(AwsConfig.getHosts_Intranet(),AwsConfig.getAccessKey(),AwsConfig.getSecretKey(),AwsConfig.getBucketName(),file);
        return result;
    }

    /**
     * 文件上传
     * @param hosts
     * @param accessKey
     * @param secretKey
     * @param bucketName
     * @param file
     * @return
     */
    public static HaoCangServerResponse upload(final String hosts, final String accessKey, final String secretKey, String bucketName,File file) {
        AmazonS3 amazonS3 = s3Client(hosts, accessKey, secretKey);
        try {
            if (amazonS3 != null) {
                List<Bucket> bucketList = amazonS3.listBuckets();
                if (StringUtils.isNullOrEmpty(bucketName)) {
                    return HaoCangServerResponse.createByError("bucketName不能为空");
                }
                boolean isExistBucket = isExistBucket(bucketList, bucketName);
                //bucketName 的格式有要求，所以不能让用户随意创建操作，需要创建操作的话，必须要有格式化验证
                if (!isExistBucket) {
                    //amazonS3.createBucket(bucketName);
                    return HaoCangServerResponse.createByError(" bucketName参数异常");
                }
                String name = file.getName();
                String key =name;
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.length());
                //上传
                amazonS3.putObject(bucketName, key, file);
                //设置文件公有，用于url外网地址访问下载
                amazonS3.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
                URL url = amazonS3.getUrl(bucketName, key);//获取永久可以访问的url
                Map<String, Object> result = new HashMap<>();
                result.put("bucketName", bucketName);
                result.put("key", key);
                result.put("url", url);
                return HaoCangServerResponse.createBySucess("上传成功", result);
            }
            return HaoCangServerResponse.createByError("S3连接异常");
        } catch (Exception e) {
            return HaoCangServerResponse.createByError("上传异常");
        } finally {
            shutDown(amazonS3);
        }

    }


    /**
     * 判断是否已存在 bucketName ，方便后续操作
     *
     * @param bucketList
     * @param bucketName
     * @return
     */
    private static boolean isExistBucket(List<Bucket> bucketList, String bucketName) {
        boolean isExistBucket = false;
        for (Bucket bucket : bucketList) {
            if (bucket.getName().equals(bucketName)) {
                isExistBucket = true;
                break;
            }
        }
        return isExistBucket;
    }

    /**
     * 拼接上传文件的key，前缀+“_”+uuid+ 后缀（根据具体要求实现）
     * @param fileName
     * @return
     */
    private static String getkey(String fileName){
        if(fileName == null){
            return null;
        }else{
            int index = fileName.lastIndexOf(".");
            if(index == -1){
                return "";
            } else{
                String prefix = FileUtil.mainName(fileName);
                String suffix = FileUtil.extName(fileName);
                StringBuffer keybf = new StringBuffer(prefix);
                keybf.append("_").append(UUID.randomUUID().toString()).append(".").append(suffix);
                return keybf.toString();
            }
        }

    }

    /**
     * 获取永久有效的url下载地址
     * @param amazonS3
     * @param bucketName
     * @param key
     * @return
     */
    private static URL getUrl(AmazonS3 amazonS3, String bucketName, String key){
        if(amazonS3 == null || bucketName == null || key == null){
            return null;
        }else {
            URL url = amazonS3.getUrl(bucketName, key);
            return url;
        }
    }


    /**
     * 获取文件下载的临时url，一段时间之后会失效
     * @param amazonS3
     * @param bucketName
     * @param key
     * @return
     */
    private static URL getTempUrl(AmazonS3 amazonS3,String bucketName, String key){
        if(amazonS3 == null || bucketName == null || key == null){
            return null;
        }else{
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key);
            URL url = amazonS3.generatePresignedUrl(request);
            return url;
        }

    }

    /**
     * url 内网替换成外网返回，用于文件的预览和下载
     * @param map
     * @param hostsIntranet
     * @param hostsNetwork
     * @return
     */
    private static Map<String, Object> replaceUrl(Map<String, Object> map,String hostsIntranet, String hostsNetwork){
        if(map == null || hostsIntranet == null || hostsNetwork == null){
            return null;
        }else{
            Object url = map.get("url");
            if(url != null ){
                String urlStr = url.toString();
                if(urlStr.contains(hostsIntranet)){
                    urlStr =  urlStr.replace(hostsIntranet,hostsNetwork);
                    map.put("url",urlStr);
                }
                return map;
            }else{
                return null;
            }
        }
    }

    /**
     * 关闭 s3连接
     * @param s3
     */
    private static void shutDown(AmazonS3 s3) {
        if (s3 != null) {
            s3.shutdown();
        }
    }

    /**
     * 文件下载，返回url 走外网方式
     * @param bucketName
     * @param key
     * @return
     */
    public static HaoCangServerResponse downloadUrl(final String bucketName, final String key){
        AmazonS3 amazonS3 = s3Client(AwsConfig.getHosts_network(),AwsConfig.getAccessKey(),AwsConfig.getSecretKey());
        if(amazonS3 != null){
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, key);
            URL url = amazonS3.generatePresignedUrl(request);
            Map<String,Object> result = new HashMap<>();
            result.put("url",url);
            return HaoCangServerResponse.createBySucess(result);
        }
        return HaoCangServerResponse.createByError("参数异常");
    }

    public static void main(String[] args) throws IOException {

      /*  String filepath = "C://Users/linj/Desktop/temp/nginx/test.txt";
        File file = new File(filepath);

        FileInputStream input = new FileInputStream(file);
         MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "text/plain", IOUtils.toByteArray(input));
        HaoCangServerResponse result = AwsUtils.upload(multipartFile);
        System.out.println(result.getData().toString());*/


    /*    String s = "192.168.6.130:7480192.168.6.130:7480192.168.6.130:7480192.168.6.130:7480";
        System.out.println(s.replace("192.168.6.130:7480", "100.124.21.1:7480"));
        System.out.println(s.replaceAll("192.168.6.130:7480", "100.124.21.1:7480"));
        System.out.println(s.replaceFirst("192.168.6.130:7480", "100.124.21.1:7480"));*/
    }


}
