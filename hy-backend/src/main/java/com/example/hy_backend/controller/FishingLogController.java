package com.example.hy_backend.controller;

import com.example.hy_backend.dto.FishingLogDTO;
import com.example.hy_backend.entity.FishingLog;
import com.example.hy_backend.service.FishingLogService;
import com.example.hy_backend.vo.ChartDataVO;
import com.example.hy_backend.vo.FishingLogVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fishingLog")
public class FishingLogController {

    @Autowired
    private FishingLogService fishingLogService;

    // 保存捕捞日志
    @PostMapping("/save")
    public Map<String, Object> saveLog(@RequestBody FishingLogDTO logDTO) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long logId = fishingLogService.saveFishingLog(logDTO);
            result.put("code", 200);
            result.put("msg", "日志保存成功");
            result.put("logId", logId);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "保存失败：" + e.getMessage());
        }
        return result;
    }

    // 分页查询日志：修复空指针报错，保留核心打印，正常返回数据
    @GetMapping("/list")
    public Map<String, Object> getLogList(@RequestParam Integer userId,
                                          @RequestParam(required = false) String fishingDate,
                                          @RequestParam(required = false) Integer seaId,
                                          @RequestParam(required = false) Integer isCompliant,
                                          @RequestParam Integer pageNum,
                                          @RequestParam Integer pageSize) {
        Map<String, Object> result = new HashMap<>();

        // 打印入参，确认前端传参正确（保留）
        System.out.println("===== 前端传过来的查询参数 =====");
        System.out.println("userId：" + userId);
        System.out.println("fishingDate：" + fishingDate);
        System.out.println("seaId：" + seaId);
        System.out.println("isCompliant：" + isCompliant);
        System.out.println("pageNum：" + pageNum);
        System.out.println("pageSize：" + pageSize);

        try {
            // 调用Service查询列表
            List<FishingLogVO> list = fishingLogService.getLogList(userId, fishingDate, seaId, isCompliant, pageNum, pageSize);
            // 只打印列表长度，不操作列表元素，避免空指针
            System.out.println("===== 从数据库查到的日志列表 =====");
            System.out.println("列表长度（有几条日志）：" + (list == null ? 0 : list.size()));

            // 调用Service查询总数
            Integer total = fishingLogService.getLogCount(userId, fishingDate, seaId, isCompliant);
            System.out.println("===== 日志总数 =====");
            System.out.println("total：" + total);

            // 正常返回数据，即使list里是null对象也返回
            result.put("code", 200);
            result.put("data", list);
            result.put("total", total);
        } catch (Exception e) {
            // 打印异常详情，方便排查其他问题
            System.out.println("===== 查询日志时出错了 =====");
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
        }

        return result;
    }

    // 获取可视化数据
    @GetMapping("/chartData")
    public Map<String, Object> getChartData(@RequestParam Integer userId,
                                            @RequestParam String timeRange) {
        Map<String, Object> result = new HashMap<>();
        ChartDataVO data = fishingLogService.getChartData(userId, timeRange);
        result.put("code", 200);
        result.put("data", data);
        return result;
    }

    // 导出报告
    @GetMapping("/export")
    public void exportReport(@RequestParam Integer userId, HttpServletResponse response) {
        fishingLogService.exportReport(userId, response);
    }

    // 替换原有删除接口代码
    @DeleteMapping("/delete/{logId}")
    public Map<String, Object> deleteFishingLog(@PathVariable Long logId) {
        Map<String, Object> result = new HashMap<>(); // 和其他接口统一用Map返回
        try {
            fishingLogService.deleteFishingLog(logId);
            result.put("code", 200); // 成功状态码
            result.put("msg", "删除成功"); // 成功提示
            result.put("success", true); // 新增success标识，方便前端判断
        } catch (Exception e) {
            result.put("code", 500); // 失败状态码
            result.put("msg", "删除失败：" + e.getMessage()); // 失败提示
            result.put("success", false);
        }
        return result; // 返回Map（JSON格式），而非纯字符串
    }
    // 在FishingLogController.java中添加管理员功能
// 管理员功能：获取所有用户的捕捞日志
@GetMapping("/admin/all")
public Map<String, Object> getAllFishingLogs(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String fishingDate,
                                            @RequestParam(required = false) Integer seaId,
                                            @RequestParam(required = false) Integer isCompliant) {
    Map<String, Object> result = new HashMap<>();
    
    // 管理员可以查看所有用户的日志，所以不需要指定userId
    List<FishingLog> logList = fishingLogService.getAllLogs(page, size, fishingDate, seaId, isCompliant);
    Integer total = fishingLogService.getAllLogsCount(fishingDate, seaId, isCompliant);
    
    result.put("code", 200);
    result.put("msg", "查询成功");
    result.put("data", logList);
    result.put("total", total);
    result.put("page", page);
    result.put("size", size);
    return result;
}

// 管理员功能：删除捕捞日志
@PostMapping("/admin/delete")
public Map<String, Object> deleteFishingLog(@RequestBody Map<String, Long> param) {
    Map<String, Object> result = new HashMap<>();

    Long logId = param.get("logId");
    boolean success = fishingLogService.deleteLog(logId);
    if (success) {
        result.put("code", 200);
        result.put("msg", "删除成功");
    } else {
        result.put("code", 500);
        result.put("msg", "删除失败");
    }
    return result;
}
}