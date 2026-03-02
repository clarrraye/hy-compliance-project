package com.example.hy_backend.service;

import com.example.hy_backend.dto.FishingLogDTO;
import com.example.hy_backend.vo.ChartDataVO;
import com.example.hy_backend.vo.FishingLogVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface FishingLogService {
    Long saveFishingLog(FishingLogDTO logDTO);

    // 同步新增3个参数，和ServiceImpl保持一致
    List<FishingLogVO> getLogList(Integer userId, String fishingDate, Integer seaId, Integer isCompliant, Integer pageNum, Integer pageSize);

    // 同步新增3个参数，和ServiceImpl保持一致
    Integer getLogCount(Integer userId, String fishingDate, Integer seaId, Integer isCompliant);

    ChartDataVO getChartData(Integer userId, String timeRange);

    void exportReport(Integer userId, HttpServletResponse response);

    // 在FishingLogService接口中新增
    void deleteFishingLog(Long logId);
        /**
     * 获取所有用户的捕捞日志（管理员功能）
     */
    java.util.List<com.example.hy_backend.entity.FishingLog> getAllLogs(Integer page, Integer size, 
                                                                      String fishingDate, Integer seaId, 
                                                                      Integer isCompliant);
    /**
     * 获取所有捕捞日志总数（管理员功能）
     */
    Integer getAllLogsCount(String fishingDate, Integer seaId, Integer isCompliant);
    /**
     * 删除捕捞日志（管理员功能）
     */
    boolean deleteLog(Long logId);
        /**
     * 获取所有用户的捕捞日志VO（管理员功能）
     */
    List<FishingLogVO> getAllLogsVO(Integer page, Integer size, String fishingDate, Integer seaId, Integer isCompliant);
}