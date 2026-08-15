package com.example.practice.history;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 재생 기록 한 줄. 곡을 재생할 때마다 한 줄씩 쌓인다.
 *
 * ⭐ 플레이리스트와 다른 점
 *   플레이리스트 : 사람이 "담기"를 눌러야 생기고, 같은 곡은 한 번만 (중복 금지)
 *   재생 기록     : 재생할 때마다 자동으로 생기고, 같은 곡이 여러 번 쌓인다 (중복 허용)
 *   → 그래서 "몇 번 들었는지"를 셀 수 있고, 그게 '많이 들은 곡'의 근거가 된다.
 *
 * ⭐ 인덱스를 건 이유
 *   조회가 언제나 "내 것을, 최근 순으로"라서 (username, playedAt) 조합으로 찾는다.
 *   기록은 계속 쌓이기만 하므로 행이 빠르게 늘어난다. 인덱스가 없으면
 *   전체를 훑어야 해서 시간이 지날수록 느려진다.
 */
@Entity
@Table(name = "play_history",
       indexes = @Index(name = "idx_history_user_time", columnList = "username, playedAt"))
@Getter
@Setter
public class PlayHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String trackId;

    private String title;
    private String artist;
    private String album;

    @Column(length = 500)
    private String albumImage;

    @Column(length = 500)
    private String previewUrl;

    @Column(nullable = false)
    private LocalDateTime playedAt = LocalDateTime.now();
}
