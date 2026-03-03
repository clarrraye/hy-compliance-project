package com.example.hy_backend.vo;

import lombok.Data;
import java.util.List;

@Data
public class ChartDataVO {
    private Integer compliantCount;  // 合规数量
    private Integer uncompliantCount;  // 不合规数量
    
    // 鱼种重量数据
    private List<SpeciesCatchVO> speciesWeightData;
    
    // 原来的物种捕捞数据（保留供其他用途）
    private List<SpeciesCatchVO> speciesCatchData;
    
    // 各海域合规率数据
    private List<SeaCompliantRateVO> compliantRateBySea;
    
    @Data
    public static class SpeciesCatchVO {
        private String name;
        private Double value;  // 改为Double类型以支持小数重量
    }
    
    @Data
    public static class SeaCompliantRateVO {
        private String name;
        private Double rate;
    }
}