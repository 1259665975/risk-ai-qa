package com.gm.riskaiRagent.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 * <p>通过 {@code @RestControllerAdvice} 统一捕获各层抛出的异常，转换为统一的
 * {@link Result} 响应格式，避免将堆栈信息直接暴露给前端。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常，返回对应的业务错误码（HTTP 200，业务码由枚举定义）。
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusiness(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 捕获 JSR-303 / Spring 参数校验失败异常，拼接所有字段的错误信息返回。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    /**
     * AI 服务调用失败（如 Embedding / Chat API 404、鉴权失败等）。
     */
    @ExceptionHandler(NonTransientAiException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleAi(NonTransientAiException ex) {
        log.error("AI service error", ex);
        String msg = ex.getMessage();
        if (msg != null && msg.contains("404")) {
            msg = "向量化服务调用失败（Embedding API 404），请检查 DashScope 配置与 API Key";
        }
        return Result.error(ResultCode.AI_SERVICE_ERROR.getCode(), msg);
    }

    /**
     * 兜底异常捕获，处理所有未预期异常并返回 500 状态码。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return Result.error(ResultCode.INTERNAL_ERROR.getCode(), ex.getMessage());
    }
}
