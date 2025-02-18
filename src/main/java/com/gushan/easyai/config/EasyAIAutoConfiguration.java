package com.gushan.easyai.config;

import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.AIServiceFactory;
import com.gushan.easyai.service.AIServiceProvider;
import com.gushan.easyai.service.ImageGenerationService;
import com.gushan.easyai.service.impl.*;
import com.gushan.easyai.limit.TokenRateLimiter;
import com.gushan.easyai.cache.ResponseCache;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties(EasyAIProperties.class)
@ConditionalOnProperty(prefix = "easyai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EasyAIAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenRateLimiter tokenRateLimiter() {
        return new TokenRateLimiter();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseCache responseCache(EasyAIProperties properties) {
        return new ResponseCache(
            properties.getCache().getMaximumSize(),
            properties.getCache().getExpireAfterWrite()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public AIServiceFactory aiServiceFactory(ObjectProvider<List<AIServiceProvider>> providers) {
        return new AIServiceFactory(providers.getIfAvailable(List::of));
    }

    @Bean
    @ConditionalOnMissingBean
    public AIService aiService(EasyAIProperties properties, AIServiceFactory factory) {
        return factory.getService(properties.getProvider(), properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public List<AIServiceProvider> defaultAIServiceProviders() {
        return List.of(
            new OpenAIServiceProvider(),
            new BaiduAIServiceProvider(),
            new XunfeiAIServiceProvider(),
            new ClaudeAIServiceProvider(),
            new GeminiAIServiceProvider(),
            new DeepSeekAIServiceProvider()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "easyai", name = "image-generation.enabled", havingValue = "true")
    public ImageGenerationService imageGenerationService(EasyAIProperties properties) {
        // TODO: 实现图像生成服务
        return null;
    }
} 