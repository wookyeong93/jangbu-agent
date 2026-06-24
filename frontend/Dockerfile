# 빌드 스테이지
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
RUN npm run build

# 실행 스테이지 — ssr:false라 Nitro가 SPA 셸 + 정적 자원을 서빙하는 node-server 프리셋으로 뜬다.
FROM node:22-alpine
WORKDIR /app

COPY --from=build /app/.output ./.output

ENV PORT=3000
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
