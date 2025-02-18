package com.gushan.easyai.config;

import com.gushan.easyai.util.HttpUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestHttpConfig {
    
    @Bean
    @Primary
    public HttpUtils httpUtils() {
        return Mockito.mock(HttpUtils.class);
    }
} 