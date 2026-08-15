package com.example.practice.music;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * 하루치 차트 기록 한 줄.
 *
 * ⭐ 왜 저장해야 하는가
 *   Apple은 "지금 이 순간" 차트 한 장만 준다. 과거 순위를 조회하는 기능이 없다.
 *   그래서 일간·주간 차트를 만들려면 우리가 매일 받아 쌓아두는 수밖에 없다.
 *   → 외부 API를 우리 데이터로 축적하는 구조.
 *
 * ⭐ (date, trackId) 유니크
 *   하루에 같은 곡이 두 번 기록되면 주간 집계(평균 순위)가 어긋난다.
 *   저장 전에 코드로도 확인하지만, 스케줄러가 두 번 도는 상황까지 막으려면
 *   DB 제약이 있어야 확실하다.
 */
@Entity
@Table(name = "chart_snapshots",
       uniqueConstraints = @UniqueConstraint(columnNames = {"snapshotDate", "trackId"}),
       indexes = @Index(name = "idx_snapshot_date", columnList = "snapshotDate"))
@Getter
@Setter
public class ChartSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어느 날짜의 차트인지. 'date'는 여러 DB에서 예약어라 이름을 피했다. */
    @Column(nullable = false)
    private LocalDate snapshotDate;

    @Column(nullable = false)
    private int rank;          // 그날의 순위 (1위부터)

    @Column(nullable = false)
    private String trackId;

    private String title;
    private String artist;
    private String album;

    @Column(length = 500)
    private String albumImage;

    @Column(length = 500)
    private String previewUrl;
}
