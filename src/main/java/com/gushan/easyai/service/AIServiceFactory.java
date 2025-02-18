package com.gushan.easyai.service;

import com.gushan.easyai.config.EasyAIProperties;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AIServiceFactory {
    private final List<AIServiceProvider> providers;
    private final Map<String, AIServiceProvider> providerMap;

    public AIServiceFactory(List<AIServiceProvider> providers) {
        this.providers = providers;
        this.providerMap = providers.stream()
            .collect(Collectors.toMap(
                provider -> provider.getName().toLowerCase(),
                provider -> provider
            ));
    }

    public AIService getService(String provider, EasyAIProperties properties) {
        AIServiceProvider serviceProvider = providerMap.get(provider.toLowerCase());
        if (serviceProvider == null) {
            throw new IllegalArgumentException("Unsupported AI provider: " + provider);
        }
        return serviceProvider.createService(properties);
    }

    public List<String> getAvailableProviders() {
        return providers.stream()
            .map(AIServiceProvider::getName)
            .collect(Collectors.toList());
    }
} 