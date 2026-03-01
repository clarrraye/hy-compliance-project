package com.example.hy_backend.mapper;

import com.example.hy_backend.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper接口
 * 严格适配user表，仅实现登录/个人信息查询所需方法
 */
@Mapper
public interface SysUserMapper {
    /**
     * 按登录账号查询用户（登录验证用，返回含密码的完整信息）
     * @param username 登录账号（数据库username字段）
     * @return 完整用户信息（含password，仅登录时使用）
     */
    SysUser selectUserByUsername(@Param("username") String username);

    /**
     * 按用户ID查询个人信息（大厅展示用，不含密码，避免敏感信息泄露）
     * @param userId 用户主键ID
     * @return 个人信息（无password，仅展示用）
     */
    SysUser selectUserInfoById(@Param("userId") Integer userId);
        /**
     * 查询用户列表（分页）
     * @param offset 偏移量
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    java.util.List<SysUser> selectUserList(@Param("offset") Integer offset, 
                                          @Param("size") Integer size, 
                                          @Param("keyword") String keyword);
    /**
     * 查询用户总数
     * @param keyword 搜索关键词
     * @return 用户总数
     */
    Integer selectUserCount(@Param("keyword") String keyword);
    /**
     * 添加用户
     * @param user 用户信息
     * @return 影响行数
     */
    Integer insertUser(SysUser user);
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 影响行数
     */
    Integer updateUser(SysUser user);
    /**
     * 删除用户
     * @param userId 用户ID
     * @return 影响行数
     */
    Integer deleteUser(@Param("userId") Integer userId);
}