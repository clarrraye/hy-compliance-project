package com.example.hy_backend.service.impl;

import com.example.hy_backend.entity.FishingBanRule;
import com.example.hy_backend.entity.SeaArea;
import com.example.hy_backend.entity.Species;
import com.example.hy_backend.service.ComplianceQueryService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap; // 新增导入，否则HashMap标红

/**
 * ComplianceQueryService接口实现类
 * 基于SqlSessionTemplate实现真实数据库查询，适配已创建的数据库表
 */
@Service
public class ComplianceQueryServiceimpl implements ComplianceQueryService {

    // 注入MyBatis的SqlSessionTemplate，用于执行XML中的SQL语句
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * 海域列表查询 - 调用ComplianceMapper.xml的listSeaArea SQL
     */
    @Override
    public List<SeaArea> listSeaArea() {
        return sqlSessionTemplate.selectList("com.example.hy_backend.mapper.ComplianceMapper.listSeaArea");
    }

    /**
     * 物种列表查询 - 调用ComplianceMapper.xml的listSpecies SQL
     */
    @Override
    public List<Species> listSpecies() {
        return sqlSessionTemplate.selectList("com.example.hy_backend.mapper.ComplianceMapper.listSpecies");
    }

    /**
     * 禁渔规则联表查询 - 调用ComplianceMapper.xml的selectBanRuleWithName SQL
     * 多条件封装为Map传递，适配XML中的动态条件判断
     */
    @Override
    public List<FishingBanRule> queryBanRule(Integer seaId, String month, Integer speciesId) {
        // 用HashMap，允许值为null，适配非必填参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("seaId", seaId);
        paramMap.put("month", month);
        paramMap.put("speciesId", speciesId);
        // 传递HashMap参数
        return sqlSessionTemplate.selectList(
                "com.example.hy_backend.mapper.ComplianceMapper.selectBanRuleWithName",
                paramMap
        );
    }

    /**
     * 渔具合规全量查询 - 调用ComplianceMapper.xml的listFishingGear SQL
     * 返回Map适配前端字段，无需新增实体类
     */
    @Override
    public List<Map<String, Object>> listFishingGear() {
        return sqlSessionTemplate.selectList("com.example.hy_backend.mapper.ComplianceMapper.listFishingGear");
    }

    /**
     * 渔业政策模糊查询 - 调用ComplianceMapper.xml的searchPolicy SQL
     * 拼接%实现模糊匹配，关键词封装为Map传递
     */
    @Override
    public List<Map<String, Object>> searchPolicy(String keyword) {
        return sqlSessionTemplate.selectList(
                "com.example.hy_backend.mapper.ComplianceMapper.searchPolicy",
                Map.of("keyword", "%" + keyword + "%")
        );
    }
}
