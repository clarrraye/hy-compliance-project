package com.example.hy_backend.service;

import com.example.hy_backend.entity.FishingBanRule;
import com.example.hy_backend.entity.SeaArea;
import com.example.hy_backend.entity.Species;

import java.util.List;
import java.util.Map;

/**
 * 海渔合规查询核心接口
 * 定义海域、物种、禁渔规则、渔具、政策的查询规范
 */
public interface ComplianceQueryService {
    // 海域列表查询
    List<SeaArea> listSeaArea();

    // 物种（鱼种）列表查询
    List<Species> listSpecies();

    // 禁渔规则联表查询（多条件非必填）
    List<FishingBanRule> queryBanRule(Integer seaId, String month, Integer speciesId);

    // 渔具合规全量查询
    List<Map<String, Object>> listFishingGear();

    // 渔业政策关键词模糊查询
    List<Map<String, Object>> searchPolicy(String keyword);
}
