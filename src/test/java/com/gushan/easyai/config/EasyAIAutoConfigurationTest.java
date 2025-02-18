package com.gushan.easyai.config;

import com.gushan.easyai.service.AIService;
import com.gushan.easyai.service.impl.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EasyAIAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(EasyAIAutoConfiguration.class));

    @Test
    void whenEnabledAndProviderIsOpenAI_thenOpenAIServiceIsCreated() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=openai"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context.getBean(AIService.class)).isInstanceOf(OpenAIServiceImpl.class);
                });
    }

    @Test
    void whenEnabledAndProviderIsBaidu_thenBaiduServiceIsCreated() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=baidu"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context.getBean(AIService.class)).isInstanceOf(BaiduAIServiceImpl.class);
                });
    }

    @Test
    void whenEnabledAndProviderIsXunfei_thenXunfeiServiceIsCreated() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=xunfei"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context.getBean(AIService.class)).isInstanceOf(XunfeiAIServiceImpl.class);
                });
    }

    @Test
    void whenEnabledAndProviderIsClaude_thenClaudeServiceIsCreated() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=claude"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context.getBean(AIService.class)).isInstanceOf(ClaudeAIServiceImpl.class);
                });
    }

    @Test
    void whenEnabledAndProviderIsGemini_thenGeminiServiceIsCreated() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=gemini"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context.getBean(AIService.class)).isInstanceOf(GeminiAIServiceImpl.class);
                });
    }

    @Test
    void whenEnabledAndProviderIsDeepseek_thenDeepseekServiceIsCreated() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=deepseek"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context.getBean(AIService.class)).isInstanceOf(DeepSeekAIServiceImpl.class);
                });
    }

    @Test
    void whenDisabled_thenNoServiceIsCreated() {
        contextRunner
                .withPropertyValues("easyai.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AIService.class);
                });
    }

    @Test
    void whenInvalidProvider_thenThrowsException() {
        contextRunner
                .withPropertyValues(
                        "easyai.enabled=true",
                        "easyai.provider=invalid"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context).getFailure()
                            .hasMessageContaining("Unsupported AI provider: invalid");
                });
    }
} 