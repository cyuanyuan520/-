package com.tanhua.autoconfig.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "tanhua.sms")
public class SmsProperties {
    private String topic;
}
