package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fishing_ban_rule") // 严格匹配你的禁渔规则表名
public class FishingBanRule {
    @TableId(type = IdType.AUTO)
    private Integer ruleId; // 你的主键是rule_id，不是ban_id
    private Integer seaId; // 关联海域ID
    private Integer speciesId; // 关联物种ID
    private String banStartTime; // 禁渔开始时间
    private String banEndTime; // 禁渔结束时间
    private String specRequire; // 规格要求
    private String punishDesc; // 违规处罚
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 联表查询用：海域名称（非数据库字段，用于前端展示）
    private String seaName;
    // 联表查询用：物种名称（非数据库字段，用于前端展示）
    private String speciesName;
}