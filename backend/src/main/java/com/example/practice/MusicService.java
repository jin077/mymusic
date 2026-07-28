package com.example.practice;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;

import tools.jackson.databind.ObjectMapper;

/**
 * ===== 음악 데이터 서비스 =====
 *
 * 외부 음악 API를 호출해 우리 형식(TrackDto)으로 정리해서 돌려준다.
 *
 * ⭐ 왜 프론트가 외부 API를 직접 부르지 않고 이 서버를 거치는가?
 *   1) 응답 정규화 : 외부 응답 필드 30여 개 중 화면에 필요한 6개만 골라 보낸다.
 *   2) 여러 API 조합 : 차트는 [차트 목록 API] + [상세 조회 API]를 두 번 불러야 완성된다.
 *                      서버가 합쳐서 주면 프론트는 한 번만 호출한다.
 *   3) 캐싱 : 외부 API는 요청 속도 제한이 있다(인증 키가 없어 인증으로 통제할 수 없음).
 *            차트는 하루 한 번 바뀌므로 서버가 저장해두고 재사용한다.
 *   4) 의존성 격리 : 외부 API가 정책을 바꿔도(2026년 Spotify 사례) 이 파일만 고치면 된다.
 *
 * 사용하는 외부 API (둘 다 인증 키 불필요):
 *   - iTunes Search API : https://itunes.apple.com/search , /lookup
 *   - Apple Music RSS   : https://rss.marketingtools.apple.com/api/v2/kr/music/most-played/...
 */
@Service
public class MusicService {

    private static final Logger log = LoggerFactory.getLogger(MusicService.class);

    /** 외부 서버와 통신할 도구. 주소 앞부분(호스트)을 미리 지정해둔다. */
    private final RestClient itunes = RestClient.create("https://itunes.apple.com");
    private final RestClient appleRss = RestClient.create("https://rss.marketingtools.apple.com");

    /**
     * JSON 문자열 → 자바 객체 변환기.
     *
     * ⭐ 왜 직접 변환하는가? (실제로 겪은 문제)
     *   보통은 .body(ItunesResponse.class) 한 줄로 스프링이 알아서 변환해준다.
     *   그런데 iTunes API는 응답 헤더를 Content-Type: text/javascript 로 보낸다(오래된 방식).
     *   스프링은 "JSON이 아니다"라고 판단해 변환기를 붙이지 못하고 실패한다.
     *     → 오류: no suitable HttpMessageConverter found ... content type [text/javascript]
     *   그래서 응답을 '문자열'로 받아 우리가 직접 JSON으로 해석한다.
     *   (외부 API는 규격을 우리 마음대로 바꿀 수 없으므로, 받아주는 쪽이 맞춰야 한다.)
     */
    private final ObjectMapper mapper;

    public MusicService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private static final int CHART_SIZE = 30;   // 차트에 담을 곡 수
    private static final int SEARCH_SIZE = 25;  // 검색 결과 개수

    // ─────────────────────────────────────────────────────────
    // 캐시 (외부 API 호출 횟수를 줄이는 장치)
    // ─────────────────────────────────────────────────────────
    private static final Duration TTL = Duration.ofMinutes(10); // 저장 유효시간
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /** 캐시에 담긴 값 + 만료시각 */
    private record CacheEntry(List<?> value, Instant expiresAt) {
    }

    /**
     * 캐시에 있으면 그것을 쓰고, 없거나 오래됐으면 새로 불러와 저장한다.
     *
     * Supplier : "필요할 때 실행할 코드"를 넘겨받는 도구.
     *            캐시가 살아있으면 이 코드는 아예 실행되지 않는다(=외부 호출 안 함).
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> cached(String key, Supplier<List<T>> loader) {
        CacheEntry entry = cache.get(key);
        if (entry != null && Instant.now().isBefore(entry.expiresAt())) {
            return (List<T>) entry.value();
        }
        List<T> fresh = loader.get();
        cache.put(key, new CacheEntry(fresh, Instant.now().plus(TTL)));
        return fresh;
    }

    // ─────────────────────────────────────────────────────────
    // 공개 기능
    // ─────────────────────────────────────────────────────────

    /** 곡 검색 */
    public List<TrackDto> search(String keyword) {
        String key = "search:" + keyword.trim().toLowerCase();
        return cached(key, () -> {
            ItunesResponse res = getJson(itunes,
                    b -> b.path("/search")
                            .queryParam("term", keyword)
                            .queryParam("country", "KR")   // 한국 스토어 기준
                            .queryParam("media", "music")
                            .queryParam("entity", "song")
                            .queryParam("limit", SEARCH_SIZE)
                            .build(),
                    ItunesResponse.class);

            if (res == null || res.results() == null) return List.of();
            return res.results().stream()
                    .filter(t -> t.trackId() != null)
                    .map(this::toTrack)
                    .toList();
        });
    }

    /**
     * 한국 인기곡 차트.
     *
     * 외부 API를 두 번 호출해 합친다:
     *   ① Apple Music RSS  → 순위·곡명·가수·앨범이미지 (미리듣기 주소는 없음)
     *   ② iTunes lookup    → ①에서 받은 곡 id들로 미리듣기 주소·앨범명 조회
     */
    public List<TrackDto> chart() {
        return cached("chart", () -> {
            ChartResponse res = getJson(appleRss,
                    b -> b.path("/api/v2/kr/music/most-played/{size}/songs.json").build(CHART_SIZE),
                    ChartResponse.class);

            if (res == null || res.feed() == null || res.feed().results() == null) {
                return List.of();
            }
            List<ChartItem> items = res.feed().results();

            // ②에서 받은 상세정보를 곡 id로 빨리 찾을 수 있게 Map으로 만들어둔다
            Map<String, ItunesTrack> details = lookupDetails(
                    items.stream().map(ChartItem::id).toList());

            return items.stream()
                    .map(item -> {
                        ItunesTrack d = details.get(item.id());
                        return new TrackDto(
                                item.id(),
                                item.name(),
                                item.artistName(),
                                d != null ? d.collectionName() : null,
                                bigImage(item.artworkUrl100()),
                                d != null ? d.previewUrl() : null);
                    })
                    .toList();
        });
    }

    /**
     * 앨범 목록.
     * 차트 결과에서 앨범 기준으로 중복을 제거해 만든다.
     * → 외부 API를 추가로 부르지 않아 호출 횟수를 아낀다.
     */
    public List<AlbumDto> albums() {
        // LinkedHashMap : 넣은 순서(차트 순위)를 유지하면서 중복 제거
        Map<String, AlbumDto> unique = new LinkedHashMap<>();
        for (TrackDto t : chart()) {
            if (t.album() == null) continue;
            unique.putIfAbsent(t.album(), new AlbumDto(t.album(), t.artist(), t.albumImage()));
        }
        return List.copyOf(unique.values());
    }

    // ─────────────────────────────────────────────────────────
    // 내부 도우미
    // ─────────────────────────────────────────────────────────

    /** 곡 id 목록으로 상세정보(미리듣기 주소 등)를 한 번에 조회 */
    private Map<String, ItunesTrack> lookupDetails(List<String> ids) {
        if (ids.isEmpty()) return Map.of();

        // 여러 id를 콤마로 이어 한 번에 요청 → 호출 횟수 절약
        String joined = String.join(",", ids);
        ItunesResponse res = getJson(itunes,
                b -> b.path("/lookup")
                        .queryParam("id", joined)
                        .queryParam("country", "kr")
                        .queryParam("entity", "song")
                        .build(),
                ItunesResponse.class);

        if (res == null || res.results() == null) return Map.of();
        return res.results().stream()
                .filter(t -> t.trackId() != null)
                .collect(Collectors.toMap(
                        t -> String.valueOf(t.trackId()),
                        t -> t,
                        (a, b) -> a));   // 같은 id가 중복되면 먼저 온 것을 사용
    }

    /** iTunes 곡 정보 → 우리 응답 형식으로 변환 */
    private TrackDto toTrack(ItunesTrack t) {
        return new TrackDto(
                String.valueOf(t.trackId()),
                t.trackName(),
                t.artistName(),
                t.collectionName(),
                bigImage(t.artworkUrl100()),
                t.previewUrl());
    }

    /**
     * 앨범 이미지 크기 키우기.
     * 외부 API는 100x100을 주는데, 주소 규칙상 숫자를 바꾸면 더 큰 이미지를 받을 수 있다.
     */
    private static String bigImage(String url) {
        return url == null ? null : url.replace("100x100bb", "300x300bb");
    }

    /**
     * 외부 API를 호출해 JSON 문자열로 받고, 우리 객체로 변환한다.
     *
     * 응답을 String으로 받는 이유는 위 mapper 설명 참고(Content-Type이 text/javascript).
     */
    private <T> T getJson(RestClient client, Function<UriBuilder, URI> uriFunction, Class<T> type) {
        String body = call(() -> client.get()
                .uri(uriFunction)
                .retrieve()
                .body(String.class));

        if (body == null || body.isBlank()) return null;
        try {
            return mapper.readValue(body, type);
        } catch (RuntimeException e) {
            log.error("external API response parse failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 음악 API 응답을 해석할 수 없습니다", e);
        }
    }

    /**
     * 외부 API 호출을 감싸는 공통 처리.
     *
     * 외부 서버는 우리가 통제할 수 없다(장애·차단·응답 지연).
     * 그대로 터지면 500(서버 오류)이 나가 "우리 서버가 고장난 것"처럼 보인다.
     * → 502(Bad Gateway)로 바꿔 "뒤쪽 외부 서버 문제"임을 분명히 알린다.
     */
    private <T> T call(Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException e) {
            // 실패 원인을 서버 로그에 남긴다.
            //   외부 호출 실패는 "왜 실패했는지"를 기록해두지 않으면 나중에 추적이 불가능하다.
            log.error("external API call failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 음악 API 호출에 실패했습니다", e);
        }
    }

    // ─────────────────────────────────────────────────────────
    // 외부 응답을 담는 그릇
    //   필요한 필드만 선언하면 된다.
    //   응답에 있는 나머지 필드(수십 개)는 무시된다.
    // ─────────────────────────────────────────────────────────

    /** iTunes search / lookup 응답 */
    record ItunesResponse(int resultCount, List<ItunesTrack> results) {
    }

    record ItunesTrack(
            Long trackId,
            String trackName,
            String artistName,
            String collectionName,
            String artworkUrl100,
            String previewUrl) {
    }

    /** Apple Music RSS 차트 응답: { "feed": { "results": [ ... ] } } */
    record ChartResponse(Feed feed) {
    }

    record Feed(List<ChartItem> results) {
    }

    record ChartItem(
            String id,
            String name,
            String artistName,
            String artworkUrl100) {
    }
}
