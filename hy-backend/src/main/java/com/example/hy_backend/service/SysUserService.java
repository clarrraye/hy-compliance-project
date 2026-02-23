package com.example.hy_backend.service;

import com.example.hy_backend.entity.SysUser;

/**
 * 用户服务接口
 * 仅提供登录验证、个人信息查询功能
 */
public interface SysUserService {
    /**
     * 登录验证：账号+密码+启用状态校验
     * @param username 登录账号
     * @param password 登录密码
     * @return 校验成功返回用户ID，失败返回null（账号不存在/密码错/账号禁用）
     */
    Integer login(String username, String password);

    /**
     * 按用户ID查询个人信息（大厅展示用，无敏感信息）
     * @param userId 用户主键ID
     * @return 个人信息实体（无password），查询失败返回null
     */
    SysUser getUserInfoById(Integer userId);
}