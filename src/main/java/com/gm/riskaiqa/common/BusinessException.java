package com.gm.riskaiqa.common;

import lombok.Getter;

/**
 * 业务异常基类。
 * <p>继承 {@link RuntimeException}，携带业务错误码 {@code code}，由
 * {@link GlobalExceptionHandler} 统一捕获并返回结构化的 {@link Result}。</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 使用 ResultCode 构建，message 取自枚举的默认文案。
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用 ResultCode + 自定义 message。
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 直接指定 code + message，适用于需要前端特殊处理的场景。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
