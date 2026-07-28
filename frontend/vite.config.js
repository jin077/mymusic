import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000, // 개발서버 포트 고정
    // ⭐ 개발용 리버스 프록시: '/api'로 시작하는 요청은 vite가 백엔드(8080)로 대신 전달한다.
    //    → 브라우저는 vite(3000) 한 곳만 보므로 same-origin = CORS 불필요.
    //    (배포 때 nginx가 하는 일을, 개발 땐 vite 개발서버가 똑같이 해줌)
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
