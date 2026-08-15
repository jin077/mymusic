package com.example.practice.music;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 차트를 정해진 시각에 자동으로 저장·정리한다.
 *
 * ⭐ cron 표현식 읽는 법 (초 분 시 일 월 요일)
 *   "0 10 4 * * *"  → 매일 04시 10분 00초
 *   새벽으로 잡은 이유: 사용자가 적은 시간대에 외부 API를 호출하기 위해서다.
 *   정각(0분)을 피한 이유: 많은 서비스가 정각에 작업을 몰아 넣어 외부 API가 붐빈다.
 *
 * ⭐ 서버가 꺼져 있으면 그 시각의 작업은 그냥 지나간다.
 *   그래서 앱이 뜰 때도 한 번 확인한다(아래 onStartup).
 *   "이미 저장했으면 건너뛴다"로 만들어 두어 여러 번 불려도 안전하다.
 *
 * ⚠️ 서버를 여러 대로 늘리면 모든 대수가 같은 시각에 실행된다.
 *   그때는 ShedLock 같은 장치로 "한 대만 실행"하게 막아야 한다.
 *   지금은 한 대라 문제되지 않는다.
 */
@Component
public class ChartScheduler {

    private final ChartSnapshotService snapshotService;

    public ChartScheduler(ChartSnapshotService snapshotService) {
        this.snapshotService = snapshotService;
    }

    /** 매일 새벽 4시 10분 — 그날의 차트를 저장 */
    @Scheduled(cron = "0 10 4 * * *")
    public void saveDailyChart() {
        snapshotService.saveToday();
    }

    /** 매일 새벽 4시 30분 — 보관 기간이 지난 기록 정리 */
    @Scheduled(cron = "0 30 4 * * *")
    public void cleanOldSnapshots() {
        snapshotService.deleteOld();
    }

    /**
     * 앱이 다 뜬 뒤 한 번 확인한다.
     *
     * ApplicationReadyEvent를 쓴 이유: DB·외부 호출 준비가 끝난 뒤에 실행되어야 하기 때문이다.
     * (생성자나 @PostConstruct에서 하면 아직 준비가 안 된 상태일 수 있다)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        snapshotService.saveToday();
    }
}
