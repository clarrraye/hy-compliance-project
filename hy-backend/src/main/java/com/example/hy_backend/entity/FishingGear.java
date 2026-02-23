package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

// 对应数据库表：fishing_gear
@TableName("fishing_gear")
@Data // 依赖你现有Lombok，自动生成get/set
public class FishingGear {
    @TableId(type = IdType.AUTO) // 主键自增
    private Integer gearId; // 对应表中gear_id（驼峰转下划线，MyBatis自动映射）
    private String gearName; // 渔具名称：gear_name
    private Integer isAllow; // 是否允许：is_allow（1=允许，0=禁用）
    private String remark; // 备注/规格：remark
    private String punishDesc; // 违规处罚：punish_desc
    private LocalDateTime createTime; // 创建时间：create_time
}
