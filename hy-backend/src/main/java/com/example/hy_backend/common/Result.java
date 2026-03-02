package com.example.hy_backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 统一返回结果
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    // 状态码：200=成功，500=失败
    private Integer code;
    // 提示信息
    private String msg;
    // 返回数据
    private T data;

    // 成功返回（带数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功返回（无数据）
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 失败返回
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}
