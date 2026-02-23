package com.example.hy_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hy_backend.entity.FishingLogSpecies;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FishingLogSpeciesMapper extends BaseMapper<FishingLogSpecies> {
    // 批量插入物种明细
    @Insert("<script>" +
            "INSERT INTO fishing_log_species (log_id, species_id, catch_num, catch_spec, is_compliant, uncompliant_reason) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','> " +
            "(#{item.logId}, #{item.speciesId}, #{item.catchNum}, #{item.catchSpec}, #{item.isCompliant}, #{item.uncompliantReason}) " +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("list") List<FishingLogSpecies> list);
}