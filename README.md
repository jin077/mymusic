# MyMusic — 음악 검색·스트리밍 학습 프로젝트

Spring Boot + React로 만든 음악 서비스입니다.
회원 인증(JWT)부터 외부 음악 API 연동, nginx 리버스 프록시 배포까지 **직접 구성했습니다.**

> 학습용 프로젝트입니다. 화면 구성은 국내 음악 서비스(멜론)의 레이아웃을 참고했으며,
> 로고·이미지·CSS 등 저작물은 사용하지 않았습니다. 음원은 Apple이 공개 제공하는 30초 미리듣기를 사용합니다.

---

## 주요 기능

| 기능 | 설명 |
|---|---|
| 회원가입 · 로그인 | JWT 토큰 기반 인증 (서버 무상태 / Stateless) |
| 권한 분리 | 일반 사용자(USER) / 관리자(ADMIN) 역할 기반 접근 제어 |
| 실시간 차트 | Apple Music 한국 인기곡 30곡 |
| 곡 검색 | 곡명·가수 검색 (한국 스토어 기준) |
| 앨범 목록 | 차트 기반 앨범 커버 |
| 30초 미리듣기 | **페이지를 이동해도 재생이 끊기지 않는 전역 플레이어** |

---

## 기술 스택

**백엔드**
- Java 17, Spring Boot 4.1
- Spring Security + JWT (jjwt) · BCrypt
- Spring Data JPA / Hibernate
- MariaDB
- Gradle (fat jar)

**프론트엔드**
- React 19, Vite
- React Router (SPA 라우팅)
- Axios (요청 인터셉터로 토큰 자동 첨부)
- Context API (전역 플레이어 · 인증 상태)

**인프라**
- nginx (리버스 프록시 · 정적 파일 서빙 · SPA fallback)
- Docker · Docker Compose (백엔드 · nginx · MariaDB 3계층 컨테이너화)

**외부 API** (둘 다 인증 키 불필요)
- iTunes Search API — 곡 검색, 미리듣기 주소
- Apple Music RSS — 한국 인기곡 차트

---

## 프로젝트 구조

```
practice/
├─ backend/                  Spring Boot
│  ├─ src/main/java/com/example/practice/
│  │  ├─ AuthController        로그인 → JWT 발급
│  │  ├─ JwtAuthenticationFilter  요청마다 토큰 검사
│  │  ├─ SecurityConfig        인증·인가 규칙
│  │  ├─ WebConfig             모든 REST API에 /api 접두사 일괄 적용
│  │  ├─ MusicController       음악 API (공개)
│  │  ├─ MusicService          외부 API 조합 + 캐싱 + 오류 변환
│  │  ├─ TrackDto / AlbumDto / MemberDto   응답 형식(엔티티와 분리)
│  │  └─ ...
│  └─ application-secret.properties.example   비밀값 양식
├─ frontend/                 React
│  └─ src/
│     ├─ music.js              외부 데이터 창구 (의존성 격리)
│     ├─ player/PlayerContext  전역 오디오 플레이어
│     ├─ auth/AuthContext      로그인 상태
│     ├─ components/           Layout · Header · PlayerBar · TrackList
│     └─ pages/                Home · Chart · Albums · Ticket · Search · Profile · Login
├─ nginx/nginx.conf          배포용 리버스 프록시 설정
└─ README.md
```

---

## API

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/signup` | ✕ | 회원가입 (권한은 서버가 USER로 고정) |
| POST | `/api/login` | ✕ | 로그인 → JWT 발급 |
| GET | `/api/music/chart` | ✕ | 인기곡 차트 |
| GET | `/api/music/search?q=` | ✕ | 곡 검색 |
| GET | `/api/music/albums` | ✕ | 앨범 목록 |
| GET | `/api/admin/members` | **ADMIN** | 전체 회원 목록 |
| DELETE | `/api/admin/members/{id}` | **ADMIN** | 회원 삭제 |

음악 조회는 공개 정보이므로 인증을 요구하지 않고, 회원 기능만 인증을 요구합니다.

---

## 실행 방법

### 방법 A. Docker (권장)

**Docker Desktop만 있으면 됩니다.** JDK·Node·MariaDB를 설치하지 않아도 되고,
스키마·계정 생성도 필요 없습니다.

```bash
cp .env.example .env      # DB 계정과 JWT 서명키를 채운다
docker compose up -d      # → http://localhost
```

MariaDB → 백엔드 → nginx 순서로 뜹니다.
백엔드는 DB가 실제로 접속을 받을 준비가 될 때까지(healthcheck) 기다렸다가 시작합니다.

```bash
docker compose ps          # 상태 확인
docker compose logs -f     # 로그 실시간
docker compose down        # 정지 (DB 데이터는 볼륨에 남음)
```

아래 방법 B의 준비 과정 5단계가 명령 두 줄로 줄어듭니다.

---

### 방법 B. 직접 실행

**1. 사전 준비**
- JDK 17, Node.js, MariaDB
- MariaDB에 `practice` 스키마 생성 (utf8mb4)

**2. 비밀값 설정**
```bash
cd backend
cp application-secret.properties.example application-secret.properties
# 파일을 열어 DB 계정과 JWT 서명키를 채운다
```

**3. 백엔드 실행**
```bash
cd backend
./gradlew bootJar
java -jar build/libs/practice-0.0.1-SNAPSHOT.jar    # http://localhost:8080
```

**4. 프론트엔드 실행 (개발)**
```bash
cd frontend
npm install
npm run dev                                          # http://localhost:3000
```

**5. nginx로 배포 실행**
```bash
cd frontend && npm run build                         # dist 생성
# nginx/nginx.conf 를 nginx 설치 폴더의 conf/ 에 복사 후
nginx.exe                                            # http://localhost
```

---

## 설계 판단

### 1. 왜 프론트가 외부 API를 직접 부르지 않는가

프론트 → **우리 백엔드** → 외부 API 순서로 호출합니다.

| 이유 | 내용 |
|---|---|
| 응답 정규화 | 외부 응답 필드 30여 개 중 화면에 필요한 6개만 DTO로 정리 |
| 여러 API 조합 | 차트는 [차트 목록] + [상세 조회] 두 번 호출해야 완성 → 서버가 합쳐서 한 번에 응답 |
| 캐싱 | 외부 API는 요청 속도 제한이 있음 → 10분 캐시로 호출 횟수 절감 |
| 의존성 격리 | 외부 API 정책이 바뀌어도 `MusicService` 한 곳만 수정 |

### 2. /api 접두사를 컨트롤러마다 쓰지 않은 이유

`WebConfig`에서 `addPathPrefix("/api", ...)`로 모든 `@RestController`에 일괄 적용했습니다.
컨트롤러가 늘어나도 규칙이 한 곳에서 관리되고, 접두사를 바꿀 때 한 줄만 수정하면 됩니다.

### 3. 엔티티를 그대로 응답하지 않는 이유

`Member` 엔티티를 그대로 반환하면 비밀번호 해시가 응답 JSON에 노출됩니다.
`MemberDto`로 필요한 필드(id·username·role)만 담아 내보냅니다.

### 4. 컨테이너 구성에서 판단한 것

| 판단 | 이유 |
|---|---|
| **외부에 여는 포트는 80 하나** | 백엔드(8080)·DB(3306)는 `ports`를 지정하지 않아 컨테이너 네트워크 안에서만 접근됩니다. nginx를 앞에 둔 이유가 여기서 완성됩니다 |
| **프론트는 멀티스테이지 빌드** | Node로 빌드한 뒤 결과물만 nginx 이미지로 옮깁니다. 최종 이미지에 Node·node_modules가 남지 않아 **26MB**입니다 |
| **DB는 공식 이미지 + 환경변수** | 설치·스키마 생성·계정 부여가 환경변수 4줄로 대체됩니다 |
| **DB 데이터는 볼륨에 보관** | 컨테이너를 지웠다 다시 만들어도 회원 데이터가 유지됩니다 |
| **비밀값은 이미지에 넣지 않음** | 이미지는 레지스트리로 공유되므로 비밀이 함께 퍼집니다. 실행 시 `.env`로 주입합니다 |
| **컨테이너 간 통신은 서비스 이름으로** | 컨테이너 안에서 `localhost`는 자기 자신을 가리킵니다. `backend:8080`처럼 이름을 씁니다 |

### 5. 비밀값을 코드에서 분리한 방법

`application.properties`에는 비밀값을 두지 않고, 실행 폴더의 `application-secret.properties`를 읽습니다.

```properties
spring.config.import=optional:file:./application-secret.properties
```

- `file:./` → jar 내부가 아닌 실행 폴더에서 읽음 → **비밀값이 jar에 포함되지 않음**
- `optional:` → 파일이 없어도 기동 가능 → 배포 환경에서는 환경변수로 주입

---

## 트러블슈팅

### CORS 오류 — 설정 추가가 아니라 구조 변경으로 해결

개발 중에는 화면(3000)과 API(8080)가 다른 포트라 브라우저가 다른 출처로 판단해 요청을 차단했습니다.
허용 목록을 추가하면 동작하지만, 환경이 바뀔 때마다 수정해야 하는 구조였습니다.

nginx를 리버스 프록시로 두고 화면과 API를 **하나의 출처(80포트)로 통합**했습니다.

```nginx
location /api/ { proxy_pass http://localhost:8080; }   # API는 백엔드로 전달
location /     { try_files $uri $uri/ /index.html; }   # 나머지는 dist 서빙 (SPA fallback)
```

브라우저가 단일 출처만 인식하므로 CORS 자체가 발생하지 않습니다.
개발 환경도 Vite 프록시로 동일한 구조를 맞춰, 개발과 배포의 요청 경로를 일치시켰습니다.

### 403 오류가 진짜 원인을 덮고 있던 문제

음악 API를 공개(`permitAll`) 설정했는데도 403이 반환됐습니다. 로그를 따라가니 원인은 권한이 아니었습니다.

```
Secured GET /api/music/chart      ← 권한 통과 (설정은 정상)
   ↓ 서비스에서 오류 발생
Securing GET /error               ← 오류 처리 경로로 내부 전달
Http403ForbiddenEntryPoint: Rejecting access   ← /error가 인증을 요구해 차단
```

스프링은 처리 중 오류가 나면 `/error`로 내부 전달하는데, 이때 JWT 필터가 실행되지 않아 비인증 상태가 됩니다.
`/error`가 인증을 요구하면 **원래 오류가 403으로 덮입니다.**

```java
.requestMatchers("/error").permitAll()
```

이 한 줄로 실제 원인(502)이 드러났습니다.

### 외부 API가 JSON을 `text/javascript`로 선언한 문제

`/error`를 열고 나서 확인한 실제 오류입니다.

```
no suitable HttpMessageConverter found for response type [ItunesResponse]
and content type [text/javascript;charset=utf-8]
```

내용은 JSON인데 응답 헤더가 `text/javascript`였습니다. JSONP를 지원했던 시절의 호환성 때문입니다.
스프링은 선언된 형식을 기준으로 변환기를 선택하므로 처리에 실패합니다.

응답을 문자열로 받아 직접 변환하도록 수정했습니다.

```java
String body = client.get().uri(...).retrieve().body(String.class);
ItunesResponse res = mapper.readValue(body, ItunesResponse.class);
```

같은 API를 프론트에서 호출할 때는 문제가 없었습니다. axios는 선언된 형식과 무관하게 JSON 파싱을 시도하기 때문입니다.
**같은 호출이라도 실행 위치에 따라 검증 기준이 다르다**는 것을 확인했습니다.

### 외부 API 정책 변경 대응

당초 Spotify Web API를 사용할 계획이었으나, 2026년 2월 정책 변경으로
Client Credentials 방식의 메타데이터 조회가 제한되고 개발 모드에 유료 구독이 필요해졌습니다.

인증 키가 필요 없는 Apple API로 교체했습니다. 프론트엔드는 데이터 호출을 `music.js` 한 파일로 모아두었기 때문에
**화면 코드는 수정하지 않고** 백엔드와 해당 파일만 변경해 대응했습니다.

---

## 앞으로

- [x] Docker · Docker Compose로 컨테이너화
- [ ] 리눅스 서버 배포 (클라우드)
- [ ] 재생목록 저장 (회원 기능 확장)
- [ ] 응답 캐시 정책 개선
