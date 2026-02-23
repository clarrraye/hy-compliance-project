package com.example.hy_backend.entity;

import lombok.Data;
import java.util.Date;

/**
 * 系统用户实体类
 * 严格映射用户数据库表，字段名与数据库完全一致
 */
@Data
public class SysUser {
    private Integer userId;       // 主键：user_id（数据库）
    private String username;      // 登录账号：username（唯一，登录用）
    private String password;      // 登录密码：password（登录验证用）
    private String realName;      // 真实姓名：real_name（大厅个人信息展示）
    private Integer role;         // 角色：role（tinyint，后续系统管理模块用，0=普通用户/1=管理员）
    private String phone;         // 手机号：phone（大厅个人信息展示）
    private Integer isEnable;     // 启用状态：is_enable（1=启用/0=禁用，登录时校验）
    private Date createTime;      // 创建时间：create_time
    private Date updateTime;      // 更新时间：update_time
}