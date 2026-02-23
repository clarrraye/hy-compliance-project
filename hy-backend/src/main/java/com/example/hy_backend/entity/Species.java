package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("species") // 匹配你的表名
public class Species {
    @TableId(type = IdType.AUTO)
    private Integer speciesId; // 物种ID，对应species_id
    private String speciesName; // 物种名称，对应species_name
    private String protectLevel; // 保护级别，对应protect_level
    private Integer isCatch; // 是否可捕，对应is_catch
    private String recognizeFeature; // 识别特征，对应recognize_feature
    private String emergencyGuide; // 应急指引，对应emergency_guide
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}