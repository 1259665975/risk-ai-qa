package com.gm.riskaiqa.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gm.riskaiqa.dto.DashboardChartsVO;
import com.gm.riskaiqa.dto.DashboardStatsVO;
import com.gm.riskaiqa.entity.QaLog;
import com.gm.riskaiqa.entity.SysCategory;
import com.gm.riskaiqa.entity.SysDocument;
import com.gm.riskaiqa.entity.SysUser;
import com.gm.riskaiqa.mapper.QaLogMapper;
import com.gm.riskaiqa.mapper.SysCategoryMapper;
import com.gm.riskaiqa.mapper.SysDocumentMapper;
import com.gm.riskaiqa.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("MM-dd");

    private final SysDocumentMapper sysDocumentMapper;
    private final SysUserMapper sysUserMapper;
    private final SysCategoryMapper sysCategoryMapper;
    private final QaLogMapper qaLogMapper;

    public DashboardStatsVO stats() {
        long documents = countOrZero(sysDocumentMapper.selectCount(null));
        long users = countOrZero(sysUserMapper.selectCount(null));
        long categories = countOrZero(sysCategoryMapper.selectCount(null));
        long qaCount = countOrZero(qaLogMapper.selectCount(null));
        return DashboardStatsVO.builder()
                .documents(documents)
                .users(users)
                .qaCount(qaCount)
                .categories(categories)
                .build();
    }

    public DashboardChartsVO charts() {
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.plusDays(1).atStartOfDay();
            Long count = qaLogMapper.selectCount(new LambdaQueryWrapper<QaLog>()
                    .ge(QaLog::getCreatedAt, start)
                    .lt(QaLog::getCreatedAt, end));
            dates.add(day.format(DAY_FMT));
            counts.add(countOrZero(count));
        }

        Map<String, List<?>> trend = new HashMap<>();
        trend.put("dates", dates);
        trend.put("counts", counts);

        List<Map<String, Object>> categoryDistribution = new ArrayList<>();
        for (SysCategory category : sysCategoryMapper.selectList(null)) {
            Long count = sysDocumentMapper.selectCount(new LambdaQueryWrapper<SysDocument>()
                    .eq(SysDocument::getCategoryId, category.getId()));
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getName());
            item.put("value", countOrZero(count));
            categoryDistribution.add(item);
        }

        return DashboardChartsVO.builder()
                .trend(trend)
                .categoryDistribution(categoryDistribution)
                .build();
    }

    private long countOrZero(Long count) {
        return count == null ? 0L : count;
    }
}
