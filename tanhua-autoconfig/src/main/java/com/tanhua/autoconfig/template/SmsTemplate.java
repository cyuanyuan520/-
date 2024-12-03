package com.tanhua.autoconfig.template;

import com.tanhua.autoconfig.properties.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;


@Slf4j
public class SmsTemplate {



    private SmsProperties smsProperties;

    //构造方法
    public SmsTemplate(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    public void sendMessage(String addressMail, String authCode){
        try {
            SimpleEmail mail = new SimpleEmail();
            mail.setHostName("smtp.163.com");//发送邮件的服务器
            mail.setAuthentication("cyuanyuan5201314@163.com","VFfKDBiTuAWpUsWP");//刚刚记录的授权码，是开启SMTP的密码
            mail.setFrom("cyuanyuan5201314@163.com", smsProperties.getTopic());  //发送邮件的邮箱和发件人
            mail.setSSLOnConnect(true); //使用安全链接
            mail.addTo(addressMail);//接收的邮箱
            //System.out.println("email"+email);
            mail.setSubject(smsProperties.getTopic() + "登录验证码");//设置邮件的主题
            mail.setMsg("尊敬的" + smsProperties.getTopic() + "用户:你好!\n 登录验证码为:" + authCode+"\n"+"     (有效期为5分钟), 未注册账户将自动创建注册!");//设置邮件的内容
            mail.send();//发送
        } catch (EmailException e) {
            e.printStackTrace();
            log.error("邮件系统出问题了....请运维人员快去修理一下");
        }
    }

}
