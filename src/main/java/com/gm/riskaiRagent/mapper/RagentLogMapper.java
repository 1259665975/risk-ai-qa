package com.gm.riskaiRagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gm.riskaiRagent.entity.RagentLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for {@link RagentLog}. CRUD is provided by {@link BaseMapper}.
 */
@Mapper
public interface RagentLogMapper extends BaseMapper<RagentLog> {
}
