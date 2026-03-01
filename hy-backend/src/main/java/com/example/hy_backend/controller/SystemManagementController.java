package com.example.hy_backend.controller;

import com.example.hy_backend.entity.FishingLog;
import com.example.hy_backend.entity.SysUser;
import com.example.hy_backend.service.FishingLogService;
import com.example.hy_backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器 - 管理员功能
 * 包含用户管理和捕捞日志管理
 */
@RestController
@CrossOrigin
@RequestMapping("/system")
public class SystemManagementController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private FishingLogService fishingLogService;

    /**
     * 检查当前用户是否为管理员
     */
    private boolean isAdmin(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("loginUserId");
        if (userId == null) {
            return false;
        }
        SysUser user = sysUserService.getUserInfoById(userId);
        return user != null && user.getRole() == 2; // role=2表示管理员
    }

    /**
     * 获取所有用户列表（管理员功能）
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String keyword,
                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isAdmin(session)) {
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return result;
        }

        List<SysUser> userList = sysUserService.getUserList(page, size, keyword);
        Integer total = sysUserService.getUserCount(keyword);
        
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", userList);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    /**
     * 添加用户（管理员功能）
     */
    @PostMapping("/users/add")
    public Map<String, Object> addUser(@RequestBody SysUser user, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isAdmin(session)) {
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return result;
        }

        boolean success = sysUserService.addUser(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "添加成功");
        } else {
            result.put("code", 500);
            result.put("msg", "添加失败，用户名可能已存在");
        }
        return result;
    }

    /**
     * 编辑用户（管理员功能）
     */
    @PostMapping("/users/edit")
    public Map<String, Object> editUser(@RequestBody SysUser user, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isAdmin(session)) {
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return result;
        }

        boolean success = sysUserService.editUser(user);
        if (success) {
            result.put("code", 200);
            result.put("msg", "编辑成功");
        } else {
            result.put("code", 500);
            result.put("msg", "编辑失败");
        }
        return result;
    }

    /**
     * 删除用户（管理员功能）
     */
    @PostMapping("/users/delete")
    public Map<String, Object> deleteUser(@RequestBody Map<String, Integer> param, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isAdmin(session)) {
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return result;
        }

        Integer userId = param.get("userId");
        boolean success = sysUserService.deleteUser(userId);
        if (success) {
            result.put("code", 200);
            result.put("msg", "删除成功");
        } else {
            result.put("code", 500);
            result.put("msg", "删除失败");
        }
        return result;
    }

    /**
     * 获取所有用户的捕捞日志（管理员功能）
     */
    @GetMapping("/fishingLogs")
    public Map<String, Object> getAllFishingLogs(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String fishingDate,
                                                @RequestParam(required = false) Integer seaId,
                                                @RequestParam(required = false) Integer isCompliant,
                                                HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isAdmin(session)) {
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return result;
        }

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

    /**
     * 删除捕捞日志（管理员功能）
     */
    @PostMapping("/fishingLogs/delete")
    public Map<String, Object> deleteFishingLog(@RequestBody Map<String, Long> param, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isAdmin(session)) {
            result.put("code", 403);
            result.put("msg", "无权限访问");
            return result;
        }

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

