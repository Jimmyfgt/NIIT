package com.fgt.commons;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/**
 * 阿里云oss工具类
 */
@Component
@Slf4j
public class AliYunOSSUtil {

    //注意有空格都不行
    private static final String ENDPOINT = "oss-cn-beijing.aliyuncs.com";//区域节点
    private static final String ACCESSKEYID = "LTAI4GJmE1Pym3XULF89LyBG"; //秘钥
    private static final String ACCESSKEYSECRET = "N3Gsd2uTkxnkqX1Vv5I2XLUX1en790";
    private static final String BUCKET = "fgt-test";  //仓库
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYYMMdd"); //格式化日期

    public SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public String getBUCKET() {
        return BUCKET;
    }

    //获取oss连接
    public OSS getOssClient() {

        OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESSKEYID, ACCESSKEYSECRET);

        if (ossClient.doesBucketExist(BUCKET)) {
            log.info("bucket已存在");
        } else {
            log.info("bucket不存在，创建bucket" + BUCKET);
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(BUCKET);
            createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);//设置权限公共可读
            ossClient.createBucket(createBucketRequest);//通过反射创建
            ossClient.shutdown();
        }
        return ossClient;

    }

}
