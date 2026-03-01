package com.example.hy_backend.mapper;

import com.example.hy_backend.entity.FishingLog;
import com.example.hy_backend.vo.FishingLogVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

// 核心修改：移除 extends BaseMapper<FishingLog>，改为原生接口
public interface FishingLogMapper {
    // 手动声明insert方法（替代MyBatis-Plus自动生成的方法）
    int insert(FishingLog fishingLog);

    // 原有自定义分页查询方法（保留不变）
    List<FishingLogVO> selectLogList(@Param("userId") Integer userId,
                                     @Param("fishingDate") String fishingDate,
                                     @Param("seaId") Integer seaId,
                                     @Param("isCompliant") Integer isCompliant,
                                     @Param("pageNum") Integer pageNum,
                                     @Param("pageSize") Integer pageSize);
    // 原有自定义统计方法（保留不变）
    Integer selectLogCount(@Param("userId") Integer userId,
                           @Param("fishingDate") String fishingDate,
                           @Param("seaId") Integer seaId,
                           @Param("isCompliant") Integer isCompliant);

    // 新增：3个统计方法（对应XML）
    List<Map<String, Object>> countCompliance(@Param("userId") String userId);
    List<Map<String, Object>> countSeaComplianceRate(@Param("userId") String userId);
    List<Map<String, Object>> countSpeciesCatchTotal(@Param("userId") String userId);

    // 新增：更新日志主表（对应XML的updateById）
    int updateById(FishingLog fishingLog);
    int deleteLogById(@Param("logId") Long logId);
    int deleteLogSpeciesByLogId(@Param("logId") Long logId);

        /**
     * 获取所有用户的捕捞日志（管理员功能）
     */
    List<FishingLog> selectAllLogs(@Param("offset") Integer offset,
                                   @Param("size") Integer size,
                                   @Param("fishingDate") String fishingDate,
                                   @Param("seaId") Integer seaId,
                                   @Param("isCompliant") Integer isCompliant);
    /**
     * 获取所有捕捞日志总数（管理员功能）
     */
    Integer selectAllLogsCount(@Param("fishingDate") String fishingDate,
                               @Param("seaId") Integer seaId,
                               @Param("isCompliant") Integer isCompliant);
}