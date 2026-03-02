package com.example.hy_backend.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.Date;

@Data
public class FishingLogVO {
    private Long logId;              // 日志ID
    private Integer userId;          // 用户ID
    private String seaName;          // 海域名称
    private LocalDate fishingDate;   // 捕捞日期
    private String fishingGear;      // 渔具类型
    private Integer isCompliant;     // 整体合规状态
    private String uncompliantReason;// 违规原因
    private Date createTime;
}