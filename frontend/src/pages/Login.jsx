import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth/AuthContext'

/**
 * 로그인 / 회원가입 페이지. (기존 App.jsx의 로직을 페이지로 옮긴 것)
 *
 * 흐름: POST /api/login → 백엔드가 JWT 발급 → localStorage 저장 → 이후 요청에 자동 첨부
 */
export default function Login() {
  const [mode, setMode] = useState('login') // 'login' | 'signup'
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()

  const isLogin = mode === 'login'

  const handleLogin = async (e) => {
    e.preventDefault()
    try {
      const res = await api.post('/login', { username, password })
      login(res.data.token) // 토큰 저장 + 로그인 상태로 전환
      navigate('/') // 홈으로 이동
    } catch {
      setMessage('로그인 실패: 아이디/비밀번호를 확인하세요')
    }
  }

  const handleSignup = async (e) => {
    e.preventDefault()
    try {
      // role은 보내지 않는다 → 백엔드(MemberService)가 USER로 강제 고정
      const res = await api.post('/signup', { username, password })
      setMessage(res.data)
      setMode('login')
      setPassword('')
    } catch {
      setMessage('회원가입 실패: 이미 있는 아이디이거나 입력을 확인하세요')
    }
  }

  return (
    <main className="page">
      <div className="form-box">
        <h2 className="page-title">{isLogin ? '로그인' : '회원가입'}</h2>
        <form onSubmit={isLogin ? handleLogin : handleSignup}>
          <input
            placeholder={isLogin ? '아이디' : '사용할 아이디'}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="password"
            placeholder={isLogin ? '비밀번호' : '사용할 비밀번호'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button type="submit">{isLogin ? '로그인' : '회원가입'}</button>
        </form>

        <p style={{ marginTop: 14, fontSize: 14 }}>
          {isLogin ? '계정이 없나요? ' : '이미 계정이 있나요? '}
          <button
            type="button"
            className="link-btn"
            onClick={() => { setMode(isLogin ? 'signup' : 'login'); setMessage('') }}
          >
            {isLogin ? '회원가입' : '로그인'}
          </button>
        </p>

        {message && <p className="msg">{message}</p>}
      </div>
    </main>
  )
}
