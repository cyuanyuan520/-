package com.itheima.test;

import com.tanhua.dubbo.api.RecommendUserApi;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.server.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = AppServerApplication.class)
@RunWith(SpringRunner.class)
public class RecommondUserTest {

    @DubboReference
    private RecommendUserApi recommendUserApi;

    @Test
    public void testRecommondUser() {
        RecommendUser recommendUser = recommendUserApi.getRecommendUser(106L);
        System.out.println(recommendUser);
    }

}