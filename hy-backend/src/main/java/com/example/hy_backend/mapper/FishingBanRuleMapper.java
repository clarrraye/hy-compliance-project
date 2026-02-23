package com.example.hy_backend.mapper;

import com.example.hy_backend.entity.FishingBanRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 禁渔规则Mapper（原生MyBatis实现，脱离MyBatis-Plus）
 * 保留原有联表方法 + 补全Service调用的selectOne方法
 */
@Mapper
public interface FishingBanRuleMapper {

    /**
     * 按海域ID+物种ID查询单条禁渔规则（替代MyBatis-Plus自动生成的selectOne）
     * 匹配Service层合规校验的调用逻辑
     */
    @Select("SELECT rule_id AS ruleId, sea_id AS seaId, species_id AS speciesId, " +
            "ban_start_time AS banStartTime, ban_end_time AS banEndTime, " +
            "spec_require AS specRequire, punish_desc AS punishDesc " +
            "FROM fishing_ban_rule WHERE sea_id = #{seaId} AND species_id = #{speciesId}")
    FishingBanRule selectOne(
            @Param("seaId") Integer seaId,
            @Param("speciesId") Integer speciesId
    );

    /**
     * 联表查询禁渔规则（关联海域/物种表，返回名称，适配前端展示）
     * 原有方法保留不变，对应XML中的selectBanRuleWithName标签
     */
    List<FishingBanRule> selectBanRuleWithName(
            @Param("seaId") Integer seaId,
            @Param("speciesId") Integer speciesId,
            @Param("month") String month
    );
}