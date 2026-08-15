package com.example.practice.purchase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.common.DuplicateException;
import com.example.practice.common.InsufficientBalanceException;
import com.example.practice.common.NotFoundException;
import com.example.practice.member.Member;
import com.example.practice.member.MemberRepository;
import com.example.practice.music.TrackDto;

/** 곡 구매 로직. */
@Service
public class PurchaseService {

    /** 곡 한 곡 값(원). 상품별로 다르게 하려면 곡 정보에 가격을 두면 된다. */
    public static final long TRACK_PRICE = 100;

    private final PurchaseRepository purchaseRepository;
    private final MemberRepository memberRepository;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           MemberRepository memberRepository) {
        this.purchaseRepository = purchaseRepository;
        this.memberRepository = memberRepository;
    }

    public List<PurchaseDto> findMine(String username) {
        return purchaseRepository.findByUsernameOrderByIdDesc(username).stream()
                .map(PurchaseDto::from)
                .toList();
    }

    /**
     * 곡 구매 — 잔액에서 값을 빼고 기록을 남긴다.
     *
     * ⭐ 가격을 요청에서 받지 않는다.
     *   프론트가 price를 보내는 구조면 {"price":0}으로 바꿔 공짜로 살 수 있다.
     *   금액은 언제나 서버가 정한다.
     *
     * ⭐ @Transactional 로 묶은 이유
     *   "잔액 차감"과 "구매 기록"은 둘 다 되거나 둘 다 안 돼야 한다.
     *   중간에 실패했는데 돈만 빠지면 사용자는 돈을 잃고 곡도 못 받는다.
     *   트랜잭션 안에서는 예외가 나면 앞선 변경까지 함께 되돌아간다(롤백).
     */
    @Transactional
    public PurchaseDto purchase(String username, TrackDto track) {
        if (track == null || track.id() == null) {
            throw new IllegalArgumentException("곡 정보가 없습니다.");
        }
        if (purchaseRepository.existsByUsernameAndTrackId(username, track.id())) {
            throw new DuplicateException("이미 구매한 곡입니다: " + track.title());
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: " + username));

        if (member.getBalance() < TRACK_PRICE) {
            throw new InsufficientBalanceException(
                    "캐시가 부족합니다. (보유 " + member.getBalance() + "원 / 필요 " + TRACK_PRICE + "원)");
        }

        // 잔액 차감 — 영속 상태의 엔티티라 값만 바꿔두면 트랜잭션이 끝날 때 UPDATE 된다
        member.setBalance(member.getBalance() - TRACK_PRICE);

        Purchase purchase = new Purchase();
        purchase.setUsername(username);
        purchase.setTrackId(track.id());
        purchase.setTitle(track.title());
        purchase.setArtist(track.artist());
        purchase.setAlbum(track.album());
        purchase.setAlbumImage(track.albumImage());
        purchase.setPreviewUrl(track.previewUrl());
        purchase.setPrice(TRACK_PRICE);   // 지금 가격을 그대로 기록

        return PurchaseDto.from(purchaseRepository.save(purchase));
    }
}
