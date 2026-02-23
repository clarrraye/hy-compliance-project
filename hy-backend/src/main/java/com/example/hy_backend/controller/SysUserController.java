package com.example.hy_backend.controller;

import com.example.hy_backend.entity.SysUser;
import com.example.hy_backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
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
     * 获取个人信息
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute("loginUserId");

        if (userId == null) {
            result.put("code", 401);
            result.put("msg", "未登录，请先登录");
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
    @GetMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        session.removeAttribute("loginUserId");
        result.put("code", 200);
        result.put("msg", "退出登录成功");
        result.put("data", null);
        return result;
    }
}