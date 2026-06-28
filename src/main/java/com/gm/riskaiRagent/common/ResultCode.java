package com.gm.riskaiRagent.common;

import lombok.Getter;

/**
 * 业务返回码枚举。
 * <p>统一约定的 code / message 映射，所有 API 响应均通过此枚举表达业务状态。
 * 200 系成功，4xx 系客户端错误，5xxx 系服务端错误。</p>
 */
@Getter
public enum ResultCode {

    /** 请求成功。 */
    SUCCESS(200, "success"),
    /** 请求参数错误。 */
    BAD_REQUEST(400, "bad request"),
    /** 未登录或 token 无效。 */
    UNAUTHORIZED(401, "unauthorized"),
    /** 无权限。 */
    FORBIDDEN(403, "forbidden"),
    /** 资源不存在。 */
    NOT_FOUND(404, "resource not found"),
    /** 请求被限流拦截。 */
    TOO_MANY_REQUESTS(429, "too many requests, please try again later"),
    /** 业务逻辑异常。 */
    BUSINESS_ERROR(500, "business error"),
    /** 服务端内部错误。 */
    INTERNAL_ERROR(5000, "internal server error"),
    /** AI 大模型服务异常。 */
    AI_SERVICE_ERROR(5001, "AI service error"),
    /** 向量检索服务异常。 */
    VECTOR_STORE_ERROR(5002, "vector store error"),
    /** 文档解析异常。 */
    DOC_PARSE_ERROR(5003, "document parse error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
