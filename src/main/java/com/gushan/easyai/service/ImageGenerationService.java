package com.gushan.easyai.service;

import com.gushan.easyai.model.ImageGenerationRequest;
import com.gushan.easyai.model.ImageGenerationResponse;

public interface ImageGenerationService {
    /**
     * 生成图像
     *
     * @param request 图像生成请求
     * @return 图像生成响应
     */
    ImageGenerationResponse generateImage(ImageGenerationRequest request);
} 