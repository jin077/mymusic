package com.example.practice.ticket;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.practice.common.InsufficientBalanceException;
import com.example.practice.common.NotFoundException;
import com.example.practice.member.Member;
import com.example.practice.member.MemberRepository;

/** 이용권 구매 로직. */
@Service
public class TicketService {

    private final TicketPurchaseRepository purchaseRepository;
    private final MemberRepository memberRepository;

    public TicketService(TicketPurchaseRepository purchaseRepository,
                         MemberRepository memberRepository) {
        this.purchaseRepository = purchaseRepository;
        this.memberRepository = memberRepository;
    }

    /** 판매 중인 상품 목록 */
    public List<TicketPlanDto> plans() {
        return java.util.Arrays.stream(TicketPlan.values())
                .map(TicketPlanDto::from)
                .toList();
    }

    /** 내 이용권 구매내역 (최신순) */
    public List<TicketPurchaseDto> findMine(String username) {
        return purchaseRepository.findByUsernameOrderByIdDesc(username).stream()
                .map(TicketPurchaseDto::from)
                .toList();
    }

    /**
     * 이용권 구매.
     *
     * ⭐ 이미 이용권이 있으면 "덮어쓰지 않고 이어 붙인다".
     *   남은 20일이 있는데 30일권을 사면 50일이 되어야 한다.
     *   만료일을 그냥 오늘+30으로 바꾸면 남아 있던 20일이 사라져 사용자가 손해를 본다.
     *
     * ⭐ 잔액 차감 + 만료일 연장 + 기록 저장을 한 트랜잭션으로 묶는다.
     *   중간에 실패했는데 돈만 빠지면 안 되기 때문이다.
     */
    @Transactional
    public TicketPurchaseDto purchase(String username, TicketPlan plan) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: " + username));

        if (member.getBalance() < plan.price()) {
            throw new InsufficientBalanceException(
                    "캐시가 부족합니다. (보유 " + member.getBalance() + "원 / 필요 " + plan.price() + "원)");
        }

        // 남은 기간이 있으면 그 뒤에, 없으면 오늘부터 센다
        LocalDate today = LocalDate.now();
        LocalDate base = (member.getTicketExpiresAt() != null
                && member.getTicketExpiresAt().isAfter(today))
                ? member.getTicketExpiresAt()
                : today;
        LocalDate expiresAt = base.plusDays(plan.days());

        member.setBalance(member.getBalance() - plan.price());
        member.setTicketName(plan.label());
        member.setTicketExpiresAt(expiresAt);

        TicketPurchase purchase = new TicketPurchase();
        purchase.setUsername(username);
        purchase.setPlan(plan);
        purchase.setPrice(plan.price());   // 지금 가격을 그대로 기록
        purchase.setExpiresAt(expiresAt);

        return TicketPurchaseDto.from(purchaseRepository.save(purchase));
    }
}
