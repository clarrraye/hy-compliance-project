package com.example.hy_backend.dto;

import lombok.Data;

@Data
public class SpeciesIdentifyResultDTO {
    // 识别出的物种名称
    private String speciesName;
    // 识别置信度 (0-1)
    private Double confidence;
    // 保护级别 (如：国家一级保护动物)
    private String protectLevel;
    // 合规状态：0-违规/禁捕，1-合规/需申报，2-警告/注意
    private Integer complianceStatus;
    // 合规说明/建议 (如：严禁捕捞，需立即放生)
    private String complianceAdvice;
    // 物种图片URL (如果是OSS存储的话，这里存URL，否则可能用Base64或者不存)
    private String imageUrl;
}
