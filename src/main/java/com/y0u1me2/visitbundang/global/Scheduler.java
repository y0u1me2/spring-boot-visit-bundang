package com.y0u1me2.visitbundang.global;

import com.y0u1me2.visitbundang.domain.pharmacy.PharmacyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class Scheduler {
    @Autowired
    private PharmacyService pharmacyService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void runDailyTask() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Scheduled task starts at : {}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // DB에 저장되어 있는 약국 정보를 삭제하고 다시 로드한다.
        // API 데이터 갱신 & 약국의 요일별 영업시간 갱신 목적
        pharmacyService.deletePharmacyData();
        pharmacyService.loadPharmacyData();
    }
}
