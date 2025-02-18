# EasyAI Spring Boot Starter

[![Build Status](https://github.com/gushan1898/easyai-spring-boot-starter/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/gushan1898/easyai-spring-boot-starter/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.gushan/easyai-spring-boot-starter.svg)](https://search.maven.org/artifact/com.gushan/easyai-spring-boot-starter)
[![License](https://img.shields.io/github/license/gushan1898/easyai-spring-boot-starter.svg)](LICENSE)
[![codecov](https://codecov.io/gh/gushan1898/easyai-spring-boot-starter/branch/main/graph/badge.svg)](https://codecov.io/gh/gushan1898/easyai-spring-boot-starter)

EasyAI Spring Boot Starter 是一个用于快速集成主流AI服务的Spring Boot starter组件。支持多种AI服务提供商，提供统一的接口和简单的配置方式。

## 特性

- 开箱即用的AI服务集成
- 支持多种主流AI服务提供商
- 统一的API接口
- 灵活的配置选项
- 完善的错误处理
- 支持历史对话
- 支持自定义扩展
- 支持图像生成
- 内置令牌限流机制
- 响应缓存机制
- WebSocket实时对话支持

## 支持的AI服务提供商

- OpenAI (GPT-3.5/4)
- 百度文心一言
- 讯飞星火
- Anthropic Claude
- Google Gemini
- DeepSeek

## 快速开始

### Maven依赖

```xml
<dependency>
    <groupId>com.gushan</groupId>
    <artifactId>easyai-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基础配置

在 `application.yml` 中添加配置：

```yaml
easyai:
  enabled: true
  provider: openai  # 可选值: openai, baidu, xunfei, claude, gemini, deepseek, custom
  
  # OpenAI配置
  openai:
    api-key: ${OPENAI_API_KEY}  # 建议使用环境变量
    model: gpt-3.5-turbo
    base-url: https://api.openai.com/v1
    
  # 缓存配置
  cache:
    enabled: true
    maximum-size: 1000  # 最大缓存条目数
    expire-after-write: 30  # 缓存过期时间(分钟)
    
  # 限流配置
  rate-limit:
    enabled: true
    tokens-per-second: 10.0  # 每秒请求次数限制
    
  # 百度文心一言配置
  baidu:
    access-key: your-access-key
    secret-key: your-secret-key
    model: ERNIE-Bot
    timeout: 30000
    
  # 讯飞星火配置
  xunfei:
    app-id: your-app-id
    api-key: your-api-key
    api-secret: your-api-secret
    model: v3.5
    timeout: 30000
    
  # Claude配置
  claude:
    api-key: your-api-key
    model: claude-3-opus-20240229
    base-url: https://api.anthropic.com/v1
    timeout: 30000
    
  # Gemini配置
  gemini:
    api-key: your-api-key
    model: gemini-pro
    base-url: https://generativelanguage.googleapis.com/v1
    timeout: 30000
    
  # DeepSeek配置
  deepseek:
    api-key: your-api-key
    model: deepseek-chat
    base-url: https://api.deepseek.com/v1
    timeout: 30000
    temperature: 0.7
    max-tokens: 2000
```

### 使用示例

1. 简单对话：

```java
@Autowired
private AIService aiService;

public void simpleChat() {
    String response = aiService.chat("你好，请介绍一下你自己");
    System.out.println(response);
}
```

2. 带历史记录的对话：

```java
@Autowired
private AIService aiService;

public void chatWithHistory() {
    ChatRequest request = ChatRequest.builder()
        .prompt("继续我们的对话")
        .history(List.of(
            ChatRequest.Message.builder()
                .role("user")
                .content("你好")
                .build(),
            ChatRequest.Message.builder()
                .role("assistant")
                .content("你好！有什么我可以帮你的吗？")
                .build()
        ))
        .build();
        
    ChatResponse response = aiService.chat(request);
    System.out.println(response.getContent());
}
```

## 扩展机制

### 1. 实现服务提供者接口

```java
@Component  // 注册为Spring Bean
public class CustomAIServiceProvider implements AIServiceProvider {
    @Override
    public String getName() {
        return "custom";  // 提供者标识，与配置中的provider值对应
    }

    @Override
    public AIService createService(EasyAIProperties properties) {
        return new CustomAIServiceImpl(properties);  // 创建服务实例
    }
}
```

### 2. 实现AI服务接口

```java
public class CustomAIServiceImpl implements AIService {
    private final EasyAIProperties properties;

    public CustomAIServiceImpl(EasyAIProperties properties) {
        this.properties = properties;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        // 实现聊天逻辑
        return ChatResponse.builder()
                .content("自定义响应")
                .messageId("custom-" + System.currentTimeMillis())
                .model("custom-model")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    @Override
    public String chat(String prompt) {
        return chat(ChatRequest.builder().prompt(prompt).build()).getContent();
    }
}
```

### 3. 添加自定义配置（可选）

```java
@Data
public class EasyAIProperties {
    // ... 其他配置 ...
    
    /**
     * 自定义AI服务配置
     */
    private Custom custom = new Custom();
    
    @Data
    public static class Custom {
        private String apiKey;
        private String model = "custom-model";
        private String baseUrl = "https://api.custom.ai/v1";
        private int timeout = 30000;
        // 其他自定义配置项
    }
}
```

### 4. 使用自定义服务

```yaml
easyai:
  enabled: true
  provider: custom  # 使用自定义服务
  custom:
    api-key: your-custom-key
    model: custom-model
    base-url: https://api.custom.ai/v1
    timeout: 30000
```

### 5. 获取可用服务提供商

```java
@Autowired
private AIServiceFactory aiServiceFactory;

public void listProviders() {
    List<String> providers = aiServiceFactory.getAvailableProviders();
    System.out.println("可用的AI服务提供商: " + providers);
}
```

## 内置服务提供商

当前支持以下AI服务提供商：

| 提供商 | 配置键 | 特点 |
|-------|--------|------|
| OpenAI | openai | GPT-3.5/4 |
| 百度文心一言 | baidu | 中文优化 |
| 讯飞星火 | xunfei | 流式对话 |
| Claude | claude | 强大的理解能力 |
| Gemini | gemini | 多模态支持 |
| DeepSeek | deepseek | 开源模型 |

## 最佳实践

### 1. 配置管理
```yaml
easyai:
  provider: ${AI_PROVIDER:openai}  # 支持环境变量，默认使用openai
  openai:
    api-key: ${OPENAI_API_KEY}     # API密钥使用环境变量
    base-url: ${OPENAI_BASE_URL:https://api.openai.com/v1}  # 支持自定义代理
```

### 2. 错误处理
```java
@Slf4j
public class AIServiceWrapper {
    private final AIService aiService;
    
    public String chatWithRetry(String prompt, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return aiService.chat(prompt);
            } catch (AIException e) {
                if (i == maxRetries - 1) throw e;
                log.warn("AI服务调用失败，准备重试: {}", e.getMessage());
                Thread.sleep(1000 * (i + 1));  // 退避重试
            }
        }
        throw new AIException("重试次数已用完");
    }
}
```

### 3. 自定义扩展示例

完整的自定义实现示例可以参考项目中的 `examples/custom-provider` 目录。

## 版本说明

### 1.0.0-SNAPSHOT
- 初始版本
- 支持主流AI服务提供商（OpenAI、百度、讯飞、Claude、Gemini、DeepSeek）
- 统一的对话接口
- 支持历史对话
- 灵活的配置选项

## 开发计划

### 近期计划
- [ ] 图像生成功能支持
- [ ] 流式响应支持
- [ ] 完善单元测试覆盖率
- [ ] 添加集成测试

### 中期计划
- [ ] 支持更多AI服务提供商
- [ ] 内置令牌限流机制
- [ ] 响应缓存机制
- [ ] WebSocket支持

### 长期计划
- [ ] 支持函数调用
- [ ] 支持语音转文本
- [ ] 支持文本转语音
- [ ] 支持向量数据库集成

## 常见问题

### Q: 如何切换AI服务提供商？
A: 只需在配置文件中修改 `easyai.provider` 的值即可，无需修改代码。

### Q: 是否支持自定义模型？
A: 支持。可以在配置文件中通过 `model` 参数指定使用的模型。

### Q: 如何处理API调用失败？
A: 所有API调用失败都会抛出 `AIException`，可以通过try-catch捕获处理。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交改动：`git commit -m 'Add some AmazingFeature'`
4. 推送分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

### 开发规范
- 遵循阿里巴巴Java开发规范
- 所有新功能必须包含单元测试
- 保持代码覆盖率在80%以上
- 提交信息要清晰明了

## 许可证

本项目采用 [MIT License](LICENSE) 开源许可证。

## 联系方式

- 作者：gushan
- Email：gushan1898@gmail.com
- GitHub：https://github.com/gushan1898/easyai-spring-boot-starter
- 项目地址：https://github.com/gushan1898/easyai-spring-boot-starter

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=gushan1898/easyai-spring-boot-starter&type=Date)](https://star-history.com/#gushan1898/easyai-spring-boot-starter&Date)

## 功能使用指南

### 1. 基础对话

```java
@Autowired
private AIService aiService;

// 简单对话
String response = aiService.chat("你好");

// 带上下文的对话
ChatRequest request = ChatRequest.builder()
    .prompt("继续上一个话题")
    .history(List.of(
        ChatRequest.Message.builder()
            .role("user")
            .content("你好")
            .build(),
        ChatRequest.Message.builder()
            .role("assistant")
            .content("你好！有什么我可以帮你的吗？")
            .build()
    ))
    .build();

ChatResponse response = aiService.chat(request);
```

### 2. 流式响应（WebSocket）

1. 前端代码：
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/chat');

ws.onmessage = function(event) {
    const response = JSON.parse(event.data);
    if (response.content) {
        console.log('收到消息:', response.content);
    }
};

ws.send(JSON.stringify({
    prompt: "讲个故事",
    options: {
        temperature: 0.7
    }
}));
```

2. 后端处理（自动配置，无需额外代码）

### 3. 图像生成

```java
ImageGenerationRequest request = ImageGenerationRequest.builder()
    .prompt("一只可爱的猫咪")
    .width(512)
    .height(512)
    .numberOfImages(1)
    .style("realistic")
    .build();

ImageGenerationResponse response = aiService.generateImage(request);
List<String> imageUrls = response.getImageUrls();
```

### 4. 缓存和限流

缓存和限流机制默认启用，可通过配置调整或禁用：

```yaml
easyai:
  cache:
    enabled: true  # 启用缓存
    maximum-size: 1000
    expire-after-write: 30
    
  rate-limit:
    enabled: true  # 启用限流
    tokens-per-second: 10.0
```

手动使用缓存和限流：

```java
@Service
@RequiredArgsConstructor
public class ChatService {
    private final AIService aiService;
    private final ResponseCache cache;
    private final TokenRateLimiter rateLimiter;
    
    public ChatResponse chat(ChatRequest request) {
        // 检查缓存
        String cacheKey = generateCacheKey(request);
        ChatResponse cachedResponse = cache.get(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }
        
        // 限流控制
        rateLimiter.acquire("openai", 10.0);
        
        // 调用AI服务
        ChatResponse response = aiService.chat(request);
        
        // 存入缓存
        cache.put(cacheKey, response);
        return response;
    }
    
    private String generateCacheKey(ChatRequest request) {
        return request.getPrompt() + "-" + 
               Optional.ofNullable(request.getHistory())
                   .map(h -> h.hashCode())
                   .orElse(0);
    }
}
```

## 高级特性

### 1. 自定义服务提供商

实现自定义AI服务提供商：

```java
@Component
public class CustomAIServiceProvider implements AIServiceProvider {
    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public AIService createService(EasyAIProperties properties) {
        return new CustomAIServiceImpl(properties);
    }
}
```

### 2. 错误处理

```java
try {
    ChatResponse response = aiService.chat(request);
} catch (AIException e) {
    log.error("AI服务调用失败", e);
    // 处理异常
}
```

## 配置参考

完整的配置选项：

```yaml
easyai:
  enabled: true
  provider: openai
  
  # OpenAI配置
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-3.5-turbo
    base-url: https://api.openai.com/v1
    timeout: 30000
    
  # 百度文心一言配置
  baidu:
    access-key: ${BAIDU_ACCESS_KEY}
    secret-key: ${BAIDU_SECRET_KEY}
    model: ERNIE-Bot
    timeout: 30000
    
  # 讯飞星火配置
  xunfei:
    app-id: ${XUNFEI_APP_ID}
    api-key: ${XUNFEI_API_KEY}
    api-secret: ${XUNFEI_API_SECRET}
    model: v3.5
    timeout: 30000
    
  # 缓存配置
  cache:
    enabled: true
    maximum-size: 1000
    expire-after-write: 30
    
  # 限流配置
  rate-limit:
    enabled: true
    tokens-per-second: 10.0
    
  # 图像生成配置
  image-generation:
    enabled: false
    default-width: 512
    default-height: 512
    max-images: 4
    timeout: 60000
```

## 注意事项

1. API密钥安全：建议使用环境变量或配置中心管理API密钥
2. 限流配置：根据实际需求和API提供商的限制调整限流参数
3. 缓存策略：对于高频率重复查询，建议启用缓存
4. WebSocket：确保服务器支持WebSocket连接
