package com.example.hy_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class FishingLogDTO {
    private Integer seaId;
    private String fishingDate;
    private String fishingGear;
    private Integer userId;
    private List<FishingLogSpeciesDTO> speciesList;

    // 把内部类改为public，让外部可以访问
    @Data
    public static class FishingLogSpeciesDTO {
        private Integer speciesId;
        private Integer catchNum;
        private String catchSpec;
    }
}