package com.example.hy_backend.controller;

import com.example.hy_backend.entity.FishingBanRule;
import com.example.hy_backend.entity.SeaArea;
import com.example.hy_backend.entity.Species;
import com.example.hy_backend.service.ComplianceQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin; // 可选，若前端跨域可加上，和用户控制器保持一致
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // 🔥 核心添加：必须加这个注解

import java.util.List;
import java.util.Map;

// 🔥 修复后：@RestController + @RequestMapping，SpringBoot才能识别为接口控制器
@RestController
@CrossOrigin // 建议加上，和SysUserController保持一致，避免跨域问题（可选但推荐）
@RequestMapping("/compliance")
public class ComplianceQueryController {

    // 仅保留原有核心Service注入，删除所有冗余Service注入
    @Autowired
    private ComplianceQueryService complianceQueryService;

    // 1. 海域列表查询 - 原有方法，完全保留
    @GetMapping("/seaArea/list")
    public List<SeaArea> listSeaArea() {
        return complianceQueryService.listSeaArea();
    }

    // 2. 物种列表查询 - 原有方法，完全保留
    @GetMapping("/species/list")
    public List<Species> listSpecies() {
        return complianceQueryService.listSpecies();
    }

    // 3. 禁渔规则查询 - 原有方法，完全保留（参数非必填，兼容多条件组合）
    @GetMapping("/banRule/query")
    public List<FishingBanRule> queryBanRule(
            @RequestParam(required = false) Integer seaId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer speciesId
    ) {
        return complianceQueryService.queryBanRule(seaId, month, speciesId);
    }

    // 4. 渔具列表查询 - 保留原有方法，删除重复定义，直接调用原有Service
    @GetMapping("/fishingGear/list")
    public List<Map<String, Object>> listFishingGear() {
        return complianceQueryService.listFishingGear();
    }

    // 5. 政策搜索 - 保留原有方法，删除重复定义，直接调用原有Service
    @GetMapping("/policy/search")
    public List<Map<String, Object>> searchPolicy(@RequestParam String keyword) {
        return complianceQueryService.searchPolicy(keyword);
    }
}