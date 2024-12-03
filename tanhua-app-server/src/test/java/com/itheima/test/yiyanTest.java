package com.itheima.test;

import com.tanhua.server.AppServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.junit4.statements.ProfileValueChecker;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;

public class yiyanTest {
    @Test
    public void testYiyan(){
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject("https://uapis.cn/api/say", String.class);
        System.out.println(forObject);
    }
}
