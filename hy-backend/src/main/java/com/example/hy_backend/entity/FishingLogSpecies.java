package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("fishing_log_species")
public class FishingLogSpecies {
    @TableId(type = IdType.AUTO)
    private Long detailId;          // 明细ID
    private Long logId;             // 关联日志ID
    private Integer speciesId;      // 关联物种ID
    private Integer catchNum;       // 捕捞数量
    private String catchSpec;       // 捕捞规格
    private Integer isCompliant;    // 是否合规：1=合规，0=违规
    private String uncompliantReason; // 违规原因
}