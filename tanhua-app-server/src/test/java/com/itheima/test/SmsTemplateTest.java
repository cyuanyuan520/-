package com.itheima.test;

import com.tanhua.autoconfig.template.SmsTemplate;
import com.tanhua.server.AppServerApplication;
import com.tanhua.server.service.UserService;
import javafx.application.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class SmsTemplateTest {

    @Autowired
    private SmsTemplate smsTemplate;
    @Autowired
    private UserService userService;

    @Test
    public void testSms(){
        smsTemplate.sendMessage("482734085@qq.com", "8848");
    }

    @Test
    public void testPhoneTrans(){
        userService.sendMsg("00482734085");
    }



}
