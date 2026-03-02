package com.example.hy_backend.service;

import com.example.hy_backend.dto.SpeciesIdentifyResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface SpeciesIdentifyService {
    /**
     * AI物种识别与合规校验
     * @param file 上传的图片文件
     * @return 识别结果与合规建议
     */
    SpeciesIdentifyResultDTO identifySpecies(MultipartFile file);
}
