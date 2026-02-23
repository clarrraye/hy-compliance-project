package com.example.hy_backend.mapper;

import com.example.hy_backend.entity.Species;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

// 保留@Mapper注解（让Spring扫描到），删除extends BaseMapper<Species>（脱离MyBatis-Plus）
@Mapper
public interface SpeciesMapper {
    // 手动声明selectById方法，用注解写SQL（替代MyBatis-Plus自动生成，无需XML）
    @Select("SELECT species_id AS speciesId, species_name AS speciesName, protect_level AS protectLevel, is_catch AS isCatch FROM species WHERE species_id = #{speciesId}")
    Species selectById(Integer speciesId);

    // 保留你原有listAll方法的注解SQL，无需修改
    @Select("SELECT species_id AS speciesId, species_name AS speciesName, protect_level AS protectLevel, is_catch AS isCatch FROM species ORDER BY species_id")
    List<Species> listAll();
}