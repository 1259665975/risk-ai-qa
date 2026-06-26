package com.gm.riskaiqa.service;

import com.gm.riskaiqa.entity.QaLog;
import com.gm.riskaiqa.mapper.QaLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 问答日志持久化服务。
 * <p>将 {@link QaLog} 通过 MyBatis-Plus 写入 MySQL {@code qa_log} 表。
 * 日志写入必须不影响主流程，任何持久化异常仅记录 warn 日志。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaLogService {

    private final QaLogMapper qaLogMapper;

    /**
     * 保存一条问答日志。
     *
     * @param qaLog 日志实体，写入失败不会抛异常
     */
    public void save(QaLog qaLog) {
        try {
            qaLogMapper.insert(qaLog);
        } catch (Exception e) {
            log.warn("Failed to persist QA log, traceId={}", qaLog.getTraceId(), e);
        }
    }
}
