package com.example.hy_backend.controller;

import com.example.hy_backend.entity.SysUser;
import com.example.hy_backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器 - 登录/个人信息/退出登录
 */
@RestController
@CrossOrigin
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> param, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String username = param.get("username");
        String password = param.get("password");

        Integer userId = sysUserService.login(username, password);
        if (userId != null) {
            session.setAttribute("loginUserId", userId);
            result.put("code", 200);
            result.put("msg", "登录成功");
            result.put("data", userId);
        } else {
            result.put("code", 500);
            result.put("msg", "账号不存在/密码错误/账号已禁用");
            result.put("data", null);
        }
        return result;
    }
    /**
     * 查询个人信息（大厅展示用）
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute("loginUserId");
        if (userId == null) {
            result.put("code", 401);
            result.put("msg", "未登录");
            result.put("data", null);
            return result;
        }
        SysUser userInfo = sysUserService.getUserInfoById(userId);
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", userInfo);
        return result;
    }
 
    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "退出成功");
        return result;
    }
 
    /**
     * 获取用户列表（管理员功能）
     */
    @GetMapping("/list")
    public Map<String, Object> getUserList(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
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
    @PostMapping("/add")
    public Map<String, Object> addUser(@RequestBody SysUser user) {
        Map<String, Object> result = new HashMap<>();
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
    @PostMapping("/edit")
    public Map<String, Object> editUser(@RequestBody SysUser user) {
        Map<String, Object> result = new HashMap<>();
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
    @PostMapping("/delete")
    public Map<String, Object> deleteUser(@RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
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

    

    

}