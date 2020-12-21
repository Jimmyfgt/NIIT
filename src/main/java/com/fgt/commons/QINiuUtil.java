package com.fgt.commons;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Client;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.storage.model.FileListing;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 七牛云工具类
 */
@Component
@Slf4j
public class QINiuUtil {
    @Value("${qiniu.AK}")
    private String accessKey;
    @Value("${qiniu.SK}")
    private String secretKey;
    @Value("${qiniu.bucketName}")
    private String bucketName;
    @Value("${qiniu.url}")
    private String url;

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getUrl() {
        return url;
    }

    private UploadManager uploadManager;
    private BucketManager bucketManager;
    private Configuration configuration;
    private Client client;
    // 密钥配置
    private Auth auth;

    public Client getClient() {
        if (client == null) {
            client = new Client(new Configuration(Zone.zone1()));
        }
        return client;
    }

    public BucketManager getBucketManager() {
        if (bucketManager == null) {
            bucketManager = new BucketManager(getAuth(), new Configuration(Zone.zone1()));
        }
        return bucketManager;
    }

    public UploadManager getUploadManager() {
        if (uploadManager == null) {
            uploadManager = new UploadManager(new Configuration(Zone.zone1()));
        }
        return uploadManager;
    }

    //如需配置地域
//        public Configuration getConfiguration() {
//            if (configuration == null) {
//                //华北是zone1, 华东z0
//              configuration = new Configuration(Zone.zone1());
//            }
//            return configuration;
//        }

    public Auth getAuth() {
        if (auth == null) {
            auth = Auth.create(getAccessKey(), getSecretKey());
        }
        return auth;
    }

    public String getUpToken(String fileKey) {
        return getAuth().uploadToken(getBucketName(), fileKey);
    }

    /**
     * 本地文件上传
     *
     * @param fileName 文件全程
     * @param fileKey  上传到七牛后保存的文件路径名称
     * @return
     */
    public DefaultPutRet upload(String fileName, String fileKey) {


        DefaultPutRet defaultPutRet = null;
        try {
            com.qiniu.http.Response put = getUploadManager().put(fileName, fileKey, getUpToken(fileKey));
            // 解析上传成功的结果
            if (put.isOK()) {
                defaultPutRet = new Gson().fromJson(put.bodyString(), DefaultPutRet.class);
                return defaultPutRet;
            } else {
                log.info("七牛云发生错误！！");
                return null;
            }
        } catch (QiniuException e) {
            e.printStackTrace();
        }

        return defaultPutRet;
    }

    /**
     * 字节流上传
     *
     * @param data
     * @param fileKey
     * @return
     * @throws IOException
     */
    public DefaultPutRet upload(byte[] data, String fileKey) throws IOException {
        DefaultPutRet defaultPutRet = null;
        Response put = getUploadManager().put(data, fileKey, getUpToken(fileKey));
        // 解析上传成功的结果
        if (put.isOK()) {
            defaultPutRet = new Gson().fromJson(put.bodyString(), DefaultPutRet.class);
            return defaultPutRet;
        } else {
            log.info("七牛云发生错误！！");
            return null;
        }

    }

    /**
     * 判断文件是否存在
     *
     * @param fileKey
     * @return
     */

    public Boolean iseABoolean(String fileKey) {
        try {
            //
            FileListing files = getBucketManager().listFiles(bucketName, fileKey, "", 1, "");
            FileInfo[] items = files.items;
            int length = items.length;
            if (length == 1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String fileKey) {
        Response delete = null;
        try {
            if (iseABoolean(fileKey)) {
                delete = getBucketManager().delete(this.getBucketName(), fileKey);
                return delete.isOK() && delete != null ? true : false;
            }
        } catch (QiniuException e) {
            log.info("文件不存在");
        }
        return false;
    }

    /**
     * 获取公共空间文件
     *
     * @param fileKey
     * @return
     */
    public String getFile(String fileKey) {
        try {
            if (iseABoolean(fileKey)) {
                String replace = URLEncoder.encode(fileKey, "utf-8").replace("+", "%20");
                String format = String.format("%s/%s", url, replace);
                return format;
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


}
