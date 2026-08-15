package com.example.practice.purchase;

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

import com.example.practice.music.TrackDto;

/**
 * 구매 API. 로그인한 사람 본인의 구매만 다룬다.
 *
 * 곡 값은 서버 상수(PurchaseService.TRACK_PRICE)로 정한다.
 * 프론트에는 "얼마인지"만 알려주고, 실제 차감 금액은 서버가 결정한다.
 */
@RestController
@RequestMapping("/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    /** 내 구매내역  →  GET /api/purchases */
    @GetMapping
    public List<PurchaseDto> myPurchases(Authentication authentication) {
        return purchaseService.findMine(authentication.getName());
    }

    /** 곡 가격 안내  →  GET /api/purchases/price (화면에 "100원"을 띄우기 위한 것) */
    @GetMapping("/price")
    public Map<String, Long> price() {
        return Map.of("price", PurchaseService.TRACK_PRICE);
    }

    /**
     * 구매(다운로드)  →  POST /api/purchases   본문은 곡 정보
     *
     * 실패 응답
     *   409 이미 구매한 곡
     *   402 캐시 부족
     */
    @PostMapping
    public ResponseEntity<PurchaseDto> purchase(@RequestBody TrackDto track,
                                                Authentication authentication) {
        PurchaseDto saved = purchaseService.purchase(authentication.getName(), track);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
