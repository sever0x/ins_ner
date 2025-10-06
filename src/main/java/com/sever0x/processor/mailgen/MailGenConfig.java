package com.sever0x.processor.mailgen;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MailGenProperties.class)
public class MailGenConfig {}