package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fishing_log") // 匹配你的捕捞日志表
public class FishingLog {
    @TableId(type = IdType.AUTO)
    private Long logId; // 日志ID，BIGINT类型
    private Integer userId; // 关联用户ID
    private Integer seaId; // 关联海域ID
    private LocalDate fishingDate; // 捕捞日期
    private String fishingGear; // 渔具类型（核心：用于渔具查询）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isCompliant; // 1=合规，0=违规
    private String uncompliantReason; // 违规原因汇总
}
