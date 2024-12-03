package com.tanhua.autoconfig.template;

import com.baidu.aip.face.AipFace;
import com.tanhua.autoconfig.properties.AipFaceProperties;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;


public class AipFaceTemplate {

    private AipFaceProperties properties;

    @Autowired
    private AipFace client;

    public AipFaceTemplate(AipFaceProperties properties) {
        this.properties = properties;
    }

    /**
     * 检测图片中是否包含人脸
     * @return
     */
    public boolean detect(String imageUrl) {
        // 调用接口
        String imageType = "URL";

        //人脸识别参数
        HashMap<String, String> options = new HashMap<>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(imageUrl, imageType, options);
        System.out.println(res);
        Integer error_code = (Integer) res.get("error_code");
        return error_code == 0;
    }


}
