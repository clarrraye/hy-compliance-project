package com.example.hy_backend.controller;

import com.example.hy_backend.common.Result;
import com.example.hy_backend.dto.SpeciesIdentifyResultDTO;
import com.example.hy_backend.service.SpeciesIdentifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/species")
public class SpeciesIdentifyController {

    @Autowired
    private SpeciesIdentifyService identifyService;

    @PostMapping("/identify")
    public Result<SpeciesIdentifyResultDTO> identify(@RequestParam("file") MultipartFile file) {
        try {
            // 调用业务逻辑
            SpeciesIdentifyResultDTO result = identifyService.identifySpecies(file);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("识别失败：" + e.getMessage());
        }
    }
}
