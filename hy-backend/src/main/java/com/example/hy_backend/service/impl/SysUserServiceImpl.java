package com.example.hy_backend.service.impl;

import com.example.hy_backend.entity.SysUser;
import com.example.hy_backend.mapper.SysUserMapper;
import com.example.hy_backend.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 适配user表的登录校验逻辑，严格校验启用状态
 */
@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 登录核心校验：1.账号存在 2.密码匹配 3.账号启用（is_enable=1）
     */
    @Override
    public Integer login(String username, String password) {
        // 1. 按账号查询用户
        SysUser sysUser = sysUserMapper.selectUserByUsername(username);
        // 2. 校验：账号不存在/密码不匹配/账号禁用，均返回null
        if (sysUser == null
                || !sysUser.getPassword().equals(password)
                || sysUser.getIsEnable() != 1) {
            return null;
        }
        // 3. 校验成功，返回用户ID（后续用于查询个人信息/会话存储）
        return sysUser.getUserId();
    }

    /**
     * 查询个人信息（大厅展示用，无密码）
     */
    @Override
    public SysUser getUserInfoById(Integer userId) {
        return sysUserMapper.selectUserInfoById(userId);
    }
}