package com.fgt.controller;

import com.fgt.commons.DownLoad;
import com.fgt.commons.QINiuUtil;
import com.fgt.commons.Response;
import com.qiniu.storage.model.DefaultPutRet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/qiniu")
public class QiNiuUpload {

    @Autowired
    private QINiuUtil qiNiuUtil;

    /**
     * sahngchaun
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    public Response upload(@RequestParam("file") MultipartFile multipartFile) {
        if (multipartFile.isEmpty()) {
            return Response.fail("上传文件内容不能为空");
        }
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            String fileEnd = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString().replace("-", "") + fileEnd;
            //开始上传,使用字节流形式上传，提升效率
            DefaultPutRet upload = qiNiuUtil.upload(multipartFile.getBytes(), fileName);

            if (upload == null) {
                return Response.fail("错误");
            } else {
                //返回可直接预览的url
                String url = qiNiuUtil.getFile(fileName);
                return Response.success(url);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件上传失败");
        }
    }

    /**
     * 公共空间文件下载
     *
     * @param fileName
     * @param response
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        InputStream inputStream = null;
        BufferedOutputStream outputStream = null;

        if (!qiNiuUtil.iseABoolean(fileName)) {
            return;
        }
        try {
            String privateFile = qiNiuUtil.getFile(fileName);
            log.info("文件下载地址：" + privateFile);
            URL url = new URL(privateFile);
            inputStream = url.openStream();
            outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setHeader("Content-Disposition", "attachment;fileKey=" + URLEncoder.encode(fileName, "utf-8"));
            DownLoad.down(inputStream, outputStream);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 删除
     *
     * @param fileName
     * @return
     */

    @DeleteMapping("/delete/{fileName}")
    public Response deleteFile(@PathVariable("fileName") String fileName) {
        try {
            boolean result = qiNiuUtil.delete(fileName);
            if (result) {
                return Response.success("文件删除成功");
            } else {
                return Response.fail("要删除的文件不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件删除失败");
        }

    }

    @GetMapping("/find/{fileName}")
    public Response find(@PathVariable("fileName") String fileName) {


        try {
            String file = qiNiuUtil.getFile(fileName);
            if (file == null) {
                return Response.fail("文件不存在");
            }
            return Response.success(file);

        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件查询失败");
        }

    }

    @PutMapping("/update/{fileName}")
    public Response update(@PathVariable("fileName") String fileName, @RequestParam("file") MultipartFile multipartFile) {
        String file = qiNiuUtil.getFile(fileName);
        if (file == null) {
            return Response.fail("文件不存在");
        }
        try {
            deleteFile(fileName);
            Response upload = upload(multipartFile);
            if (upload.getCode() == 500) {
                return Response.fail("错误");
            } else {
                return Response.success(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("文件更新失败");
        }

    }

}
