package com.gm.riskaiRagent.common;

import lombok.Data;

import java.io.Serializable;

/**
 * Unified API response envelope.
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
