package com.example.hy_backend.vo;

import lombok.Data;
import java.util.List;

@Data
public class ChartDataVO {
    private Integer compliantCount;  // 合规日志数
    private Integer uncompliantCount;// 违规日志数
    private List<SpeciesCatchVO> speciesCatchData; // 物种捕捞数据
    private List<SeaCompliantRateVO> compliantRateBySea; // 海域合规率

    @Data
    public static class SpeciesCatchVO {
        private String name;  // 物种名称
        private Integer value; // 捕捞数量
    }

    @Data
    public static class SeaCompliantRateVO {
        private String name;  // 海域名称
        private Double rate;  // 合规率
    }
}