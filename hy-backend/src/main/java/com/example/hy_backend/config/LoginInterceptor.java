package com.example.hy_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 未登录拦截器 - 拦截需登录访问的接口，未登录返回统一JSON提示
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从会话中获取登录用户ID（与SysUserController中存储的key一致）
        HttpSession session = request.getSession();
        Integer loginUserId = (Integer) session.getAttribute("loginUserId");

        // 已登录：直接放行请求
        if (loginUserId != null) {
            return true;
        }

        // 未登录：直接拼接JSON字符串输出，无任何依赖
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(401);
        PrintWriter writer = response.getWriter();
        String json = "{\"code\":401,\"msg\":\"未登录，请先登录\",\"data\":null}";
        writer.write(json);
        writer.flush();
        writer.close();
        return false;
    }
}