import axios from 'axios'

/**
 * 백엔드와 통신할 axios "도구" 하나를 만들어 앱 전체에서 재사용한다.
 * (JwtUtil이 백엔드의 토큰 도구상자였다면, 이건 프론트의 통신 도구상자)
 */
const api = axios.create({
  // ⭐ 상대주소 '/api': "지금 화면이 떠 있는 그 주소(같은 출처)"로 요청을 보낸다.
  //   - 배포: 브라우저 → nginx(localhost:80)/api/... → nginx가 백엔드(8080)로 전달
  //   - 개발: 브라우저 → vite(localhost:3000)/api/... → vite가 백엔드(8080)로 전달
  //   둘 다 브라우저 눈엔 "같은 출처"라 CORS가 아예 발생하지 않는다.
  //   (예: api.post('/login') → 실제로는 /api/login 으로 나감)
  baseURL: '/api',
})

/**
 * ⭐ 요청 인터셉터: 요청이 서버로 "나가기 직전"에 가로채서 손보는 곳.
 *
 * 여기서 localStorage에 저장된 토큰을 꺼내
 *   Authorization: Bearer <토큰>  헤더에 자동으로 실어준다.
 *
 * → 백엔드의 JwtAuthenticationFilter가 바로 이 헤더를 읽어서 인증한다.
 *   (프론트가 "붙이고" ↔ 백엔드가 "읽고"  = 짝꿍!)
 * → 덕분에 API를 호출할 때마다 매번 토큰을 직접 붙일 필요가 없다.
 */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
