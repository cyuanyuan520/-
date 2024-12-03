package com.itheima.test;

import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.model.mongo.Movement;
import com.tanhua.server.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class MovementApiTest {

    @DubboReference
    private MovementApi movementApi;

    @Test
    public void testPublish() {
        Movement movement = new Movement();
        movement.setUserId(106L);
        movement.setTextContent("你的急拔好大");
        
        List<String> list = new ArrayList<>();
        list.add("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/tanhua/avatar_1.png");
        list.add("https://tanhua-dev.oss-cn-zhangjiakou.aliyuncs.com/images/tanhua/avatar_2.png");
        movement.setMedias(list);

        movement.setLatitude("40.066355");
        movement.setLongitude("116.350426");
        movement.setLocationName("中国北京市昌平区建材城西路16号");

        movementApi.publish(movement);
    }
}
