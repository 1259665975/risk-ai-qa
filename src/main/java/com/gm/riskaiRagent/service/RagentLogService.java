package com.gm.riskaiRagent.service;

import com.gm.riskaiRagent.entity.RagentLog;
import com.gm.riskaiRagent.mapper.RagentLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 问答日志持久化服务。
 * <p>将 {@link RagentLog} 通过 MyBatis-Plus 写入 MySQL {@code ragent_log} 表。
 * 日志写入必须不影响主流程，任何持久化异常仅记录 warn 日志。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagentLogService {

    private final RagentLogMapper ragentLogMapper;

    /**
     * 保存一条问答日志。
     *
     * @param ragentLog 日志实体，写入失败不会抛异常
     */
    public void save(RagentLog ragentLog) {
        try {
            ragentLogMapper.insert(ragentLog);
        } catch (Exception e) {
            log.warn("Failed to persist RAgent log, traceId={}", ragentLog.getTraceId(), e);
        }
    }
}
