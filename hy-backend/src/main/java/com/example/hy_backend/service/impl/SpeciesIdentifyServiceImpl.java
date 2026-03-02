package com.example.hy_backend.service.impl;

import com.example.hy_backend.dto.SpeciesIdentifyResultDTO;
import com.example.hy_backend.entity.Species;
import com.example.hy_backend.mapper.SpeciesMapper;
import com.example.hy_backend.service.SpeciesIdentifyService;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionContentPart;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionContentPart.ChatCompletionContentPartImageURL;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class SpeciesIdentifyServiceImpl implements SpeciesIdentifyService {

    @Autowired
    private ArkService arkService;

    @Value("${volcengine.ark.model}")
    private String model;

    @Autowired
    private SpeciesMapper speciesMapper;

    @Override
    public SpeciesIdentifyResultDTO identifySpecies(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传图片为空");
        }

        try {
            // 1. 图片转Base64
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String imageUrl = "data:image/jpeg;base64," + base64Image; // 假设是jpeg，Ark SDK会自动处理或者需要根据类型处理

            // 2. 构建请求
            List<ChatCompletionContentPart> contentParts = new ArrayList<>();
            contentParts.add(ChatCompletionContentPart.builder()
                    .type("text")
                    .text("请识别这张图片中的海洋生物（鱼类/虾蟹/贝类等）。请只返回最可能的**一个**物种的标准中文名称，不要包含任何标点符号、解释性文字或前缀后缀（例如不要说'这是一条...'，直接返回'大黄鱼'）。")
                    .build());
            
            contentParts.add(ChatCompletionContentPart.builder()
                    .type("image_url")
                    .imageUrl(new ChatCompletionContentPartImageURL(imageUrl))
                    .build());

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .multiContent(contentParts)
                    .build());

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(100) // 只需要名称，不需要太长
                    .temperature(0.1) // 降低随机性
                    .build();

            // 3. 调用API
            String aiName = "";
            try {
                aiName = arkService.createChatCompletion(request)
                        .getChoices()
                        .get(0)
                        .getMessage()
                        .getContent()
                        .toString()
                        .trim(); // 确保去除空白
                
                // 去除可能的标点 (句号等)
                aiName = aiName.replace("。", "").replace("，", "").replace(".", "");
                
            } catch (Exception ex) {
                log.error("Volcengine API调用失败", ex);
                throw new RuntimeException("AI识别服务异常：" + ex.getMessage());
            }

            if (aiName.isEmpty()) {
                throw new RuntimeException("AI未能识别出任何内容");
            }
            log.info("AI识别结果: {}", aiName);

            // 4. 构建DTO (Volcengine API 不像百度直接返回Score，所以这里暂时置信度给个默认值或者尝试让AI返回置信度? 
            // 简单的Chat Completion通常不返回置信度，除非请求logprobs。为了兼容旧逻辑，我们设为0.9或者高一点，因为是"最好"的匹配)
            // 如果需要置信度，可以尝试让Prompt返回 JSON {"name": "xxx", "confidence": 0.9}，但这里为了简单先只取名。
            
            SpeciesIdentifyResultDTO dto = new SpeciesIdentifyResultDTO();
            dto.setSpeciesName(aiName);
            dto.setConfidence(0.95); // 默认高置信度，因为是生成模型确定的结果
            
            // 5. 数据库查重与合规判断
            Species dbSpecies = speciesMapper.selectByName(aiName);
            
            // 尝试模糊匹配 (如果除了全名还有别名)
            if (dbSpecies == null) {
                // TODO: 可以加一个模糊搜索或者别名搜索
            }

            if (dbSpecies != null) {
                dto.setProtectLevel(dbSpecies.getProtectLevel() != null ? dbSpecies.getProtectLevel() : "未定级");
                if (dbSpecies.getIsCatch() == 0 || (dbSpecies.getProtectLevel() != null && dbSpecies.getProtectLevel().contains("一级"))) {
                    dto.setComplianceStatus(0); 
                    dto.setComplianceAdvice("⚠️ 严禁捕捞！该物种为" + dto.getProtectLevel() + "，误捕后请立即放生并上报。");
                } else if (dbSpecies.getProtectLevel() != null && dbSpecies.getProtectLevel().contains("二级")) {
                    dto.setComplianceStatus(2); 
                    dto.setComplianceAdvice("⚠️ 限制捕捞！该物种为" + dto.getProtectLevel() + "，需持有特许证件，否则属违法行为。");
                } else {
                    dto.setComplianceStatus(1); 
                    dto.setComplianceAdvice("✅ 合规物种。请注意遵守相关规格尺寸及禁渔期规定。");
                }
            } else {
                dto.setProtectLevel("未收录");
                dto.setComplianceStatus(2); 
                dto.setComplianceAdvice("数据库未收录此物种(" + aiName + ")，建议人工核实。");
            }

            return dto;

        } catch (Exception e) {
            log.error("识别过程发生异常", e);
            throw new RuntimeException("识别服务暂时不可用：" + e.getMessage());
        }
    }
}
