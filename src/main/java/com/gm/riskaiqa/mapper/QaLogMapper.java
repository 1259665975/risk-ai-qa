package com.gm.riskaiqa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gm.riskaiqa.entity.QaLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus mapper for {@link QaLog}. CRUD is provided by {@link BaseMapper}.
 */
@Mapper
public interface QaLogMapper extends BaseMapper<QaLog> {
}
