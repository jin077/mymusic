# ─────────────────────────────────────────────────────────────
# 백엔드(Spring Boot) 컨테이너 이미지  — 멀티스테이지 빌드
#
# 만드는 법 :  docker build -t practice-backend:1.0 .     (backend 폴더에서)
# 실행하는 법:  docker run -d -p 8080:8080 practice-backend:1.0
#
# 멀티스테이지 = 이미지를 두 단계로 나눠 만들고, 최종 이미지에는 결과물만 남기는 방식.
#   1단계(build) : JDK로 jar를 빌드          → 이 단계는 최종 이미지에 안 남음
#   2단계(run)   : JRE에 jar만 가져와서 실행
#
# 왜 이렇게 하나?
#   - build/ 는 gitignore 대상이라, 미리 빌드된 jar에 의존하면
#     저장소를 clone한 다른 서버에서는 이미지를 만들 수 없다.
#     (실제로 배포 중 "libs/practice-0.0.1-SNAPSHOT.jar: not found" 로 겪었다)
#   - 최종 이미지에 JDK·Gradle·소스가 안 들어가 이미지가 작아진다.
#
# ⚠️ 비밀값(DB 계정·jwt.secret)은 이미지에 넣지 않는다.
#    이미지는 레지스트리로 공유되므로 비밀이 같이 퍼진다.
#    → 실행할 때 환경변수로 주입한다(application.properties의 relaxed binding 이용).
# ─────────────────────────────────────────────────────────────

# ── 1단계: jar 빌드 ──
#   jdk : 컴파일이 필요하므로 개발도구가 들어간 이미지를 쓴다.
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# ⭐ 빌드 설정만 먼저 복사해서 의존성을 내려받는다.
#   소스만 바뀌었을 때 이 층이 캐시로 재사용되어 재빌드가 빨라진다.
#   (소스를 먼저 복사하면 한 글자만 고쳐도 의존성을 다시 받는다)
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 는 리눅스에서 실행 권한이 필요하다(윈도우에서 커밋되면 빠질 수 있음).
RUN chmod +x gradlew

# 의존성만 먼저 내려받아 캐시 층으로 굳힌다.
# 실패해도 빌드를 멈추지 않는다(다음 단계에서 어차피 받는다).
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 후 jar 생성
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ── 2단계: 실행 ──
#   jre : 실행만 하면 되므로 개발도구(JDK)는 불필요 → 이미지가 작아진다.
FROM eclipse-temurin:17-jre

WORKDIR /app

# ⭐ 1단계에서 만든 jar만 가져온다. JDK도 Gradle도 소스도 안 따라온다.
#   파일명이 버전에 따라 바뀌므로 * 로 받는다.
COPY --from=build /app/build/libs/*.jar app.jar

# "이 컨테이너는 8080을 쓴다"는 문서용 표시.
# 실제로 포트를 여는 건 실행할 때의 -p 옵션(또는 compose의 ports)이다.
EXPOSE 8080

# 컨테이너가 켜지면 실행할 명령.
# 이 프로세스가 컨테이너 안의 PID 1이 되고,
# 이게 끝나면 컨테이너도 종료된다(서버라서 안 끝난다).
ENTRYPOINT ["java", "-jar", "app.jar"]
