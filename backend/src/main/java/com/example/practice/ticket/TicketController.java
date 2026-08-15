package com.example.practice.ticket;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이용권 API.
 *
 * 상품 목록은 누구나 볼 수 있어야 한다(로그인 전에도 가격을 봐야 하므로)
 *   → SecurityConfig에서 GET /api/tickets/plans 를 열어둔다.
 * 구매·내역은 로그인 필요.
 */
@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** 상품 목록  →  GET /api/tickets/plans (공개) */
    @GetMapping("/plans")
    public List<TicketPlanDto> plans() {
        return ticketService.plans();
    }

    /** 내 이용권 구매내역  →  GET /api/tickets/purchases */
    @GetMapping("/purchases")
    public List<TicketPurchaseDto> myPurchases(Authentication authentication) {
        return ticketService.findMine(authentication.getName());
    }

    /**
     * 구매  →  POST /api/tickets/purchases   본문 {"plan":"STREAMING"}
     *
     * 가격은 보내지 않는다. 상품 코드만 받고 금액은 서버가 정한다.
     *
     * 실패 응답
     *   400 없는 상품 코드
     *   402 캐시 부족
     */
    @PostMapping("/purchases")
    public ResponseEntity<TicketPurchaseDto> purchase(@RequestBody Map<String, String> body,
                                                      Authentication authentication) {
        TicketPlan plan = parsePlan(body.get("plan"));
        TicketPurchaseDto saved = ticketService.purchase(authentication.getName(), plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private static TicketPlan parsePlan(String value) {
        try {
            return TicketPlan.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            // 없는 코드가 오면 400으로 답한다 (GlobalExceptionHandler가 변환)
            throw new IllegalArgumentException("알 수 없는 이용권입니다: " + value);
        }
    }
}
