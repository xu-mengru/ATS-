package com.hr.ats.config;

import com.hr.ats.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class PositionScheduler {

    private final PositionService positionService;

    /** 每小时检查一次过期岗位并自动关闭 */
    @Scheduled(fixedRate = 3600000)
    public void autoCloseExpired() {
        int closed = positionService.autoCloseExpiredPositions();
        if (closed > 0) {
            log.info("定时任务: 自动关闭 {} 个过期岗位", closed);
        }
    }
}
