package com.cloudticket.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体：所有接口返回该结构，前端按 code 判断业务成败
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;   // 1 成功，0 失败
    private String msg;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 1;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 0;
        result.msg = msg;
        return result;
    }
}
