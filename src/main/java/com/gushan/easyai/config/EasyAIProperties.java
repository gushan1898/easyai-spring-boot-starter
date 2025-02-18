package com.gushan.easyai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "easyai")
public class EasyAIProperties {
    /**
     * 是否启用EasyAI
     */
    private boolean enabled = true;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * AI服务提供商
     */
    private String provider = "openai";

    /**
     * OpenAI配置
     */
    private OpenAI openai = new OpenAI();

    /**
     * 百度文心一言配置
     */
    private Baidu baidu = new Baidu();

    /**
     * 讯飞星火配置
     */
    private Xunfei xunfei = new Xunfei();

    /**
     * Claude配置
     */
    private Claude claude = new Claude();

    /**
     * Gemini配置
     */
    private Gemini gemini = new Gemini();

    /**
     * DeepSeek配置
     */
    private DeepSeek deepseek = new DeepSeek();

    /**
     * Custom配置
     */
    private Custom custom = new Custom();

    /**
     * 图像生成配置
     */
    private ImageGeneration imageGeneration = new ImageGeneration();

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class OpenAI {
        private String apiKey;
        private String model = "gpt-3.5-turbo";
        private String baseUrl = "https://api.openai.com/v1";
        private int timeout = 30000;
    }

    @Data
    public static class Baidu {
        private String accessKey;
        private String secretKey;
        private String model = "ERNIE-Bot";
        private int timeout = 30000;
    }

    @Data
    public static class Xunfei {
        private String appId;
        private String apiKey;
        private String apiSecret;
        private String model = "v3.5";
        private int timeout = 30000;
    }

    @Data
    public static class Claude {
        private String apiKey;
        private String model = "claude-3-opus-20240229";
        private String baseUrl = "https://api.anthropic.com/v1";
        private int timeout = 30000;
    }

    @Data
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-pro";
        private String baseUrl = "https://generativelanguage.googleapis.com/v1";
        private int timeout = 30000;
    }

    @Data
    public static class DeepSeek {
        private String apiKey;
        private String model = "deepseek-chat";
        private String baseUrl = "https://api.deepseek.com/v1";
        private int timeout = 30000;
        private double temperature = 0.7;
        private int maxTokens = 2000;
    }

    @Data
    public static class Custom {
        private String apiKey;
        private String model = "custom-model";
        private String baseUrl = "https://api.custom.ai/v1";
        private int timeout = 30000;
    }

    @Data
    public static class ImageGeneration {
        private boolean enabled = false;
        private int defaultWidth = 512;
        private int defaultHeight = 512;
        private int maxImages = 4;
        private int timeout = 60000;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private int maximumSize = 1000;
        private int expireAfterWrite = 30;
    }

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private double tokensPerSecond = 10.0;
    }
} 