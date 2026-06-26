package com.gm.riskaiqa.controller.api.admin;

import com.gm.riskaiqa.common.Result;
import com.gm.riskaiqa.dto.DashboardChartsVO;
import com.gm.riskaiqa.dto.DashboardStatsVO;
import com.gm.riskaiqa.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AdminDashboard")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(dashboardService.stats());
    }

    @GetMapping("/charts")
    public Result<DashboardChartsVO> charts() {
        return Result.success(dashboardService.charts());
    }
}
