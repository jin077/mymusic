package com.example.practice.ticket;

/**
 * 이용권 상품 목록.
 *
 * ⭐ 왜 DB 테이블이 아니라 enum인가
 *   상품이 서너 개로 고정이고 운영 중에 자주 바뀌지 않는다면 코드에 두는 편이 단순하다.
 *   (테이블·관리화면·CRUD API가 필요 없다)
 *   상품을 화면에서 추가·수정해야 한다면 그때 테이블로 옮기면 된다.
 *
 * ⭐ 가격을 서버에만 두는 것이 핵심이다.
 *   프론트가 가격을 보내는 구조면 1원짜리 요청으로 이용권을 살 수 있다.
 */
public enum TicketPlan {

    FREE("무료 체험", 0, 7, "30초 미리듣기"),
    STREAMING("스트리밍", 7900, 30, "무제한 듣기"),
    STREAMING_DOWNLOAD("스트리밍 + 다운로드", 11900, 30, "듣기 + 저장 30곡");

    private final String label;
    private final long price;   // 원
    private final int days;     // 이용 기간(일)
    private final String description;

    TicketPlan(String label, long price, int days, String description) {
        this.label = label;
        this.price = price;
        this.days = days;
        this.description = description;
    }

    public String label() { return label; }
    public long price() { return price; }
    public int days() { return days; }
    public String description() { return description; }
}
