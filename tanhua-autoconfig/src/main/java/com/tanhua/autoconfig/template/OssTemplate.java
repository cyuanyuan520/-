package com.tanhua.autoconfig.template;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.autoconfig.properties.OssProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class OssTemplate {

    private OssProperties properties;

    public OssTemplate(OssProperties properties) {
        this.properties = properties;
    }

    /**
     * 文件上传
     * 1.文件名称
     * 2.字节缓冲输入流
     */
    public String upload(String filename, BufferedInputStream bis) {
        //filename:用户上传上来的图片名(1.jpg)
        //fileName:阿里云oss存的图片名字(2024/11/13/1c546bd0-78a1-48bb-a6ef-96a3f6b82740.jpg)

        //构造图片在oss中存放的路径
        String fileName = new SimpleDateFormat("yyyy/MM/dd/")
                .format(new Date()) + UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."));


        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = properties.getEndpoint();

        // 阿里云主账号的AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = properties.getAccessKey();
        String accessKeySecret = properties.getSecret();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject(properties.getBucketName(), fileName, bis);

        // 关闭OSSClient。
        ossClient.shutdown();

        String url = properties.getUrl() +"/" + fileName;
        return url;
    }


}
