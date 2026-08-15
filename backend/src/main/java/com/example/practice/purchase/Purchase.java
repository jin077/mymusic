package com.example.practice.purchase;

import java.time.LocalDateTime;

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
 * 곡 구매(다운로드) 기록.
 *
 * ⭐ 결제한 금액(price)을 기록해 두는 이유
 *   나중에 곡 값이 200원으로 바뀌어도, 과거에 100원에 산 기록은 100원이어야 한다.
 *   "지금 가격"을 보고 계산하면 지난 내역이 전부 틀어진다.
 *   → 거래 시점의 값을 그대로 박아둔다. (주문·결제 기록의 기본 원칙)
 *
 * ⭐ 실제 음원 파일은 제공하지 않는다.
 *   우리가 가진 것은 Apple의 30초 미리듣기 주소뿐이고, 그것을 파일로 내려받게 하면
 *   Apple 약관에 어긋난다. 여기서 '다운로드'는 소장 표시(구매 이력)까지만 뜻한다.
 */
@Entity
@Table(name = "purchases",
       uniqueConstraints = @UniqueConstraint(columnNames = {"username", "trackId"}),
       indexes = @Index(name = "idx_purchase_user", columnList = "username"))
@Getter
@Setter
public class Purchase {

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

    /** 거래 시점의 가격(원) */
    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private LocalDateTime purchasedAt = LocalDateTime.now();
}
