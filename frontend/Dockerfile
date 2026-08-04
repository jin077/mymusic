# ─────────────────────────────────────────────────────────────
# 프론트엔드(React) + nginx 컨테이너 이미지  — 멀티스테이지 빌드
#
# 멀티스테이지 = 이미지를 두 단계로 나눠 만들고, 최종 이미지에는 결과물만 남기는 방식.
#   1단계(build) : Node로 React를 빌드해서 dist 생성   → 이 단계는 최종 이미지에 안 남음
#   2단계(nginx) : dist만 가져와서 nginx로 서빙
#
# 왜 이렇게 하나?
#   - Node·node_modules(수백 MB)가 최종 이미지에 안 들어감 → 이미지가 ~50MB로 작아짐
#   - 내 PC에 Node가 없어도 빌드됨 → 저장소만 clone하면 누구나 동일하게 만들 수 있음
#     (dist는 gitignore 대상이라, 미리 빌드된 dist에 의존하면 남이 못 만든다)
# ─────────────────────────────────────────────────────────────

# ── 1단계: React 빌드 ──
FROM node:24-alpine AS build

WORKDIR /app

# ⭐ package 파일만 먼저 복사해서 의존성 설치.
#   소스만 바뀌었을 때 npm ci 단계가 캐시로 재사용되어 재빌드가 빨라진다.
#   (COPY . . 을 먼저 하면 소스 한 글자만 고쳐도 npm ci를 다시 함)
COPY package.json package-lock.json ./

# npm install 이 아니라 npm ci :
#   lock 파일 그대로 설치 → 언제 어디서 빌드해도 같은 버전이 깔린다(재현성)
RUN npm ci

# 나머지 소스 복사 후 빌드
COPY . .
RUN npm run build          # → /app/dist 생성

# ── 2단계: nginx로 서빙 ──
FROM nginx:alpine

# ⭐ 1단계에서 만든 dist만 가져온다. Node도 node_modules도 안 따라온다.
COPY --from=build /app/dist /usr/share/nginx/html

# server 블록 설정을 넣는다.
#   nginx 공식 이미지는 /etc/nginx/conf.d/*.conf 를 자동으로 읽어들이므로
#   전체 설정(events·http)을 다시 쓸 필요 없이 server 블록만 주면 된다.
COPY default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# CMD 를 안 적는 이유: nginx 공식 이미지가 이미
#   CMD ["nginx", "-g", "daemon off;"] 를 갖고 있다.
#   daemon off = 백그라운드로 빠지지 말고 포그라운드로 실행하라는 뜻.
#   컨테이너는 PID 1이 끝나면 종료되므로, nginx가 뒤로 빠지면 컨테이너가 죽는다.
