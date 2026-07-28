import { createContext, useContext, useState } from 'react'

/**
 * ===== 로그인 상태를 앱 전체에서 공유 =====
 *
 * 토큰은 localStorage에 저장한다(새로고침해도 유지).
 * 화면 갱신을 위해 React 상태로도 함께 들고 있는다.
 */
const AuthContext = createContext(null)

/**
 * JWT 토큰 안의 내용(payload)을 꺼내 본다.
 *
 * JWT는 [헤더].[내용].[서명] 세 조각을 점으로 이어 붙인 문자열이고,
 * 가운데 '내용'은 암호화가 아니라 Base64로 인코딩된 것이라 누구나 읽을 수 있다.
 *   → 그래서 토큰에 비밀번호 같은 민감정보를 넣지 않는다.
 *   → 위조는 '서명'으로 막는다. 서명 검증은 백엔드(JwtUtil)가 한다.
 */
function readPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(decodeURIComponent(escape(atob(base64))))
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'))

  const login = (newToken) => {
    localStorage.setItem('token', newToken)
    setToken(newToken)
  }

  const logout = () => {
    localStorage.removeItem('token')
    setToken(null)
  }

  const payload = token ? readPayload(token) : null

  const value = {
    token,
    login,
    logout,
    isLoggedIn: !!token,
    username: payload?.sub ?? null,
    role: payload?.role ?? null,
  }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth는 AuthProvider 안에서만 사용할 수 있습니다')
  return ctx
}
