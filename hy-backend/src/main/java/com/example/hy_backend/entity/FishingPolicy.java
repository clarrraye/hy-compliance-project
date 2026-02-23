package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

// 对应数据库表：fishing_policy
@TableName("fishing_policy")
@Data
public class FishingPolicy {
    @TableId(type = IdType.AUTO)
    private Integer policyId; // 政策ID：policy_id
    private String policyTitle; // 政策标题：policy_title
    private String publishUnit; // 发布单位：publish_unit
    private LocalDate publishTime; // 发布时间：publish_time（仅日期）
    private String officialContent; // 官方条文：official_content
    private String simpleContent; // 口语化解读：simple_content
    private LocalDateTime createTime; // 创建时间：create_time
}
