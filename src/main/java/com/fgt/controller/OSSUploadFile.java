package com.fgt.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.fgt.commons.AliYunOSSUtil;
import com.fgt.commons.DownLoad;
import com.fgt.commons.FileType;
import com.fgt.commons.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.UUID;

@Controller
@Slf4j
public class OSSUploadFile {

    @Autowired
    private AliYunOSSUtil aliYunOSSUtil;

    @Autowired
    private FileType fileType;

    /**
     * 上传文件
     *
     * @param multipartFile 需要上传的文件
     * @return 返回url可以直接下载
     */
    @PostMapping("/upload")
    @ResponseBody
    public Response uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        System.out.println(multipartFile);
        URL url = null;
        OSS ossClient = null;
        if (multipartFile.isEmpty()) {
            return Response.fail("上传文件内容不能为空");
        }
        try {

            String originalFilename = multipartFile.getOriginalFilename();//获得文件原始名称
            String fileEnd = originalFilename.substring(originalFilename.lastIndexOf("."));//截取后缀
            String time = aliYunOSSUtil.getSimpleDateFormat().format(new Date());

            String fileName = time + "/" + UUID.randomUUID().toString().replace("-", "") + fileEnd;
            ossClient = aliYunOSSUtil.getOssClient();//获的链接
            //通过可直接访问
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(fileType.getFileType(fileEnd));
            objectMetadata.setContentDisposition("inline");
            objectMetadata.setCacheControl("no-cache");
            objectMetadata.setContentEncoding("utf-8");
            ossClient.putObject(aliYunOSSUtil.getBUCKET(), fileName, new ByteArrayInputStream(multipartFile.getBytes()), objectMetadata);
            // 设置URL过期时间为1小时。
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
            url = ossClient.generatePresignedUrl(aliYunOSSUtil.getBUCKET(), fileName, expiration);
            log.info(url.toString());


        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("上传文件失败！！！");
        } finally {
            ossClient.shutdown();
        }
        return Response.success(url);

    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @param file     文件夹
     * @return
     */

    @DeleteMapping("/delete/{file}/{fileName}")
    @ResponseBody
    public Response delete(@PathVariable("fileName") String fileName, @PathVariable("file") String file) {
        // 获取oss的Bucket名称
        OSS ossClient = null;
        String bucketName = aliYunOSSUtil.getBUCKET();
        // 日期目录
        // 应该是从数据库获得file的
        //String file = "20201219";

        try {
            /**
             *在实际项目中，不需要删除OSS文件服务器中的文件，
             * 只需要删除数据库存储的文件路径即可！
             */
            //获得链接
            ossClient = aliYunOSSUtil.getOssClient();
            // 根据BucketName,filetName删除文件
            // 删除目录中的文件，如果是最后一个文件fileoath目录会被删除。
            String fileKey = file + "/" + fileName;
            //判断文件是否存在
            if (!ossClient.doesObjectExist(bucketName, fileKey)) {
                log.info("文件不存在");
                return Response.fail("文件不存在");
            } else {
                ossClient.deleteObject(bucketName, fileKey);
                log.info("文件删除成功");
                return Response.success("文件删除成功！！！！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件删除失败！！！！");
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 修改(官方文档)
     * 修改文件
     * OSS暂时不支持直接在线上修改文件或文件目录，您需要通过以下方式修改：
     * 修改文件
     * 您需要将文件下载到本地之后在本地修改，之后将修改后的文件上传到相同的位置覆盖原文件即可。
     * <p>
     * 修改文件目录名
     * 您需要先创建一个新的文件目录，之后将原目录中的文件拷贝到新的目录中，最后删除原目录及目录中的文件即可。
     *
     * @param fileName
     * @param file
     * @return
     */

    @PutMapping("update/{file}/{fileName}")
    @ResponseBody
    public Response update(@PathVariable("fileName") String fileName, @PathVariable("file") String file, @RequestParam("file") MultipartFile multipartFile) {
        // 获取oss的Bucket名称
        OSS ossClient = null;
        String bucketName = aliYunOSSUtil.getBUCKET();

        try {
            //获得链接
            ossClient = aliYunOSSUtil.getOssClient();
            String fileKey = file + "/" + fileName;
            //判断文件是否存在
            if (!ossClient.doesObjectExist(bucketName, fileKey)) {
                log.info("要修改的文件不存在");
                return Response.fail("文件不存在");
            } else {
                ossClient.deleteObject(bucketName, fileKey);
                uploadFile(multipartFile);
                return Response.success("文件修改成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件修改失败！！！！");
        } finally {
            ossClient.shutdown();
        }

    }

    /**
     * 文件下载
     *
     * @param fileName
     * @param file
     * @return
     */
    @GetMapping("/download/{file}/{fileName}")
    @ResponseBody
    public Response download(@PathVariable("fileName") String fileName, @PathVariable("file") String file, HttpServletResponse response) {
        // 获取oss的Bucket名称
        OSS ossClient = null;
        String bucketName = aliYunOSSUtil.getBUCKET();

        try {
            //获得链接
            ossClient = aliYunOSSUtil.getOssClient();
            String fileKey = file + "/" + fileName;
            //判断文件是否存在
            if (!ossClient.doesObjectExist(bucketName, fileKey)) {
                log.info("文件为空");
                return Response.fail("下载文件不存在");
            } else {
                OSSObject object = ossClient.getObject(bucketName, fileKey);
                InputStream inputStream = object.getObjectContent();
                BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                //BufferedOutputStream outputStream =new BufferedOutputStream(new FileOutputStream(new File("")));
                //ossClient.getObject(new GetObjectRequest(bucketName, fileKey), new File("F:\\" + fileName));
                //设置浏览器下载方式
                response.setHeader("Content-Disposition", "attachment;fileKey=" + URLEncoder.encode(fileKey, "utf-8"));
                DownLoad.down(inputStream, outputStream);
                return Response.success("文件下载成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件下载失败！！！！");
        } finally {
            ossClient.shutdown();
        }

    }

    @GetMapping("find/{file}/{fileName}")
    @ResponseBody
    public Response find(@PathVariable("fileName") String fileName, @PathVariable("file") String file) {
        // 获取oss的Bucket名称
        OSS ossClient = null;
        URL url;
        String bucketName = aliYunOSSUtil.getBUCKET();
        try {
            //获得链接
            ossClient = aliYunOSSUtil.getOssClient();
            String fileKey = file + "/" + fileName;
            String fileEnd = fileName.substring(fileName.lastIndexOf("."));
            //判断文件是否存在
            if (!ossClient.doesObjectExist(bucketName, fileKey)) {
                log.info("文件不存在");
                return Response.fail("文件不存在");
            } else {
                Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
                url = ossClient.generatePresignedUrl(bucketName, fileKey, expiration);
                log.info(url.toString());
                return Response.success(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件查找失败！！！！");
        } finally {
            ossClient.shutdown();
        }

    }

}
