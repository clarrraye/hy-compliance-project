package com.example.hy_backend.mapper;

import com.example.hy_backend.entity.SeaArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SeaAreaMapper {
    // 补充listAll方法，与Service调用匹配
    @Select("SELECT sea_id AS seaId, sea_name AS seaName, parent_sea_id AS parentSeaId, province FROM sea_area ORDER BY sea_id")
    List<SeaArea> listAll();
}
