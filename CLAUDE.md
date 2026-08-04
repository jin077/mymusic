# MyMusic

Spring Boot + React 음악 검색 서비스 (학습용). 프로젝트 소개·API 목록은 @README.md

## 실행

- **백엔드**: `./gradlew bootRun` — **cwd를 `backend/`로 할 것**
  (`application-secret.properties`를 실행 폴더에서 읽음. 루트에서 실행하면 DB 연결 실패)
- **프론트**: `npm --prefix frontend run dev` → http://localhost:3000
  (스크립트명은 `start`가 아니라 **`dev`**. Vite)
- **nginx**: `C:\nginx\nginx.exe` 실행 / 종료 `C:\nginx\nginx.exe -s stop`
- **빌드**: `./gradlew bootJar -p backend` → `backend/build/libs/*.jar`
  / `npm --prefix frontend run build` → `frontend/dist/`

개발은 Vite(3000)가, 배포는 nginx(80)가 화면 서빙 + `/api` 프록시를 담당한다. 둘은 동시에 쓰지 않는다.

## 규칙

- **API 경로에 `/api`를 직접 쓰지 말 것.** `WebConfig`가 모든 `@RestController`에
  자동으로 붙인다 (컨트롤러엔 `/login`, 실제 주소는 `/api/login`)
- 새 엔드포인트를 추가하면 `SecurityConfig`의 `requestMatchers`도 함께 확인할 것
- **응답에 엔티티를 그대로 반환하지 말 것** → DTO 사용
  (`AdminController`가 `Member`를 반환해 password 해시가 노출된 적 있음 → `MemberDto` 도입)
- **커밋 메시지에 `Co-Authored-By`를 넣지 말 것**

## 함정 (실제로 겪은 것)

- **PowerShell 5.1은 `curl.exe`에 큰따옴표를 제대로 넘기지 못한다.**
  `-d '{"a":"b"}'`가 서버엔 `{a:b}`로 도착해 400이 난다.
  JSON 본문은 파일에 쓰고 `--data-binary "@file"`로 보낼 것.
- **`/error`는 반드시 `permitAll`로 열어둘 것.** 스프링은 오류 시 `/error`로 ERROR 디스패치하는데,
  `OncePerRequestFilter`는 이때 기본적으로 재실행되지 않아 익명 상태가 된다.
  `/error`가 인증을 요구하면 원래 오류(502 등)가 403으로 덮여 원인 추적이 불가능해진다.
- 한글 쿼리 테스트는 퍼센트 인코딩으로 (`?q=%EC%95%84%EC%9D%B4%EC%9C%A0`)
- 음악 API 첫 호출이 20초 넘게 걸리는 것은 정상 — `MusicService`의 10분 TTL 캐시가 비어 있어서
  Apple API를 2단(차트 + 곡 상세)으로 실제 호출하기 때문. 두 번째부터 0.1초.

## 건드리지 말 것

- `backend/application-secret.properties` — gitignore 대상, **커밋 금지**
  (DB 계정·`jwt.secret`이 들어 있음. 커밋되는 건 `.example` 양식뿐)
- `이력서_자소서_초안.md` — gitignore 대상, 공개 금지
