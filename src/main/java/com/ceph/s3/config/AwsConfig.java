package com.ceph.s3.config;

import com.ceph.s3.util.PropertiesUtil;



/**
 * 后面如果是SpringBoot项目可以直接对接yml配置文件
 */

public class AwsConfig {
    private static String hosts_Intranet = PropertiesUtil.getProperty("s3.hosts_Intranet");//内网
    private static String hosts_network = PropertiesUtil.getProperty("s3.hosts_network");//外网
    private static String accessKey = PropertiesUtil.getProperty("s3.accessKey");
    private static String secretKey = PropertiesUtil.getProperty("s3.secretKey");
    private static String bucketName = PropertiesUtil.getProperty("s3.bucketName");

    public static String getHosts_Intranet() {
        return hosts_Intranet;
    }

    public static void setHosts_Intranet(String hosts_Intranet) {
        AwsConfig.hosts_Intranet = hosts_Intranet;
    }

    public static String getHosts_network() {
        return hosts_network;
    }

    public static void setHosts_network(String hosts_network) {
        AwsConfig.hosts_network = hosts_network;
    }

    public static String getAccessKey() {
        return accessKey;
    }

    public static void setAccessKey(String accessKey) {
        AwsConfig.accessKey = accessKey;
    }

    public static String getSecretKey() {
        return secretKey;
    }

    public static void setSecretKey(String secretKey) {
        AwsConfig.secretKey = secretKey;
    }

    public static String getBucketName() {
        return bucketName;
    }

    public static void setBucketName(String bucketName) {
        AwsConfig.bucketName = bucketName;
    }
}
