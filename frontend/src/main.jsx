import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './auth/AuthContext'
import { PlayerProvider } from './player/PlayerContext'

/**
 * 앱의 시작점. 바깥쪽부터 감싸는 순서가 중요하다.
 *
 *   BrowserRouter  : 주소(URL)를 관리
 *     AuthProvider : 로그인 상태를 앱 전체에 공유
 *       PlayerProvider : ⭐ Audio 객체를 여기서 딱 하나 만들어 계속 유지
 *         App      : 주소별 페이지
 *
 * PlayerProvider가 App(=페이지들) '바깥'에 있기 때문에
 * 페이지가 바뀌어도 Provider는 그대로 살아있고, 음악이 끊기지 않는다.
 */
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <PlayerProvider>
          <App />
        </PlayerProvider>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
)
