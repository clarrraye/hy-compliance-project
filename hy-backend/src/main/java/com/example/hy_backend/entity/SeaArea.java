package com.example.hy_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sea_area") // 匹配你的表名
public class SeaArea {
    @TableId(type = IdType.AUTO)
    private Integer seaId; // 海域ID，对应sea_id
    private String seaName; // 海域名称，对应sea_name
    private Integer parentSeaId; // 上级海域ID，对应parent_sea_id
    private String province; // 所属省份，对应province
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
