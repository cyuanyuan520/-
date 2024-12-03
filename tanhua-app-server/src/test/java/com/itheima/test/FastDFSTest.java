package com.itheima.test;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.server.AppServerApplication;
import org.aspectj.weaver.ast.Var;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

@SpringBootTest(classes = AppServerApplication.class)
@RunWith(SpringRunner.class)
public class FastDFSTest {

    /**
     * fastDFS文件上传
     */
    //用于文件上传或下载
    @Autowired
    private FastFileStorageClient client;
    @Autowired
    private FdfsWebServer webServer;

    @Test
    public void uploadTest() throws Exception {
        try {
            //1.指定文件
            File file = new File("C:\\Users\\nvnej\\Videos\\2024-02-01-11-56-31.mp4");

            // 先检查文件是否存在
            if (!file.exists()) {
                System.out.println("文件不存在：" + file.getAbsolutePath());
                return;
            }

            //2.文件上传
            StorePath path = client.uploadFile(
                    new BufferedInputStream(new FileInputStream(file)),
                    file.length(),
                    "mp4",
                    null
            );

            //3.拼接路径
            String fullPath = path.getFullPath();
            System.out.println("上传成功，fullPath: " + fullPath);

            String url = webServer.getWebServerUrl() + fullPath;
            System.out.println("文件URL: " + url);
        } catch (Exception e) {
            System.out.println("上传失败：");
            e.printStackTrace();
        }
    }
}
