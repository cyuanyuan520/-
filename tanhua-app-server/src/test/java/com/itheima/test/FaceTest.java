package com.itheima.test;

import com.baidu.aip.face.AipFace;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.server.AppServerApplication;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FaceTest {

    // 设置APPID/AK/SK
    public static final String APP_ID = "116222251";
    public static final String API_KEY = "pwSEvE54uOamUjl4KqlWlM1M";
    public static final String SECRET_KEY = "Z6MzKZUOrkfwR9x0IVeGIh7QGxdGFf2r";

    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    @Test
    public  void checkTest() {
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 调用接口
        String image = "https://thjy010.oss-cn-chengdu.aliyuncs.com/2024/11/13/383f3fa3-ddf9-4ebf-8cce-6faa940f4a07.jpg";
        String imageType = "URL";

        //人脸识别参数
        HashMap<String, String> options = new HashMap<>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(image, imageType, options);
        System.out.println(res.toString(2));
    }


    @Test
    public void ApiDetect() {
        String url = "https://thjy010.oss-cn-chengdu.aliyuncs.com/2024/11/13/a49f58b1-5f9a-4432-be4b-e3983ecb0551.png";
        boolean detect = aipFaceTemplate.detect(url);
        System.out.println(detect);
    }


}
