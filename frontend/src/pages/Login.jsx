import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth/AuthContext'

/**
 * 로그인 / 회원가입 페이지.
 *
 * 한 컴포넌트가 두 주소를 담당한다.
 *   /login  → 로그인 폼
 *   /signup → 회원가입 폼
 *
 * ⭐ 왜 상태(useState)가 아니라 주소로 모드를 정하는가?
 *   - 회원가입 화면에서 새로고침해도 회원가입 화면이 유지된다
 *   - "회원가입" 링크를 어디서든 만들 수 있다 (<Link to="/signup">)
 *   - 입력 폼이 거의 같아 파일을 나누면 코드가 중복된다 → 한 파일에서 분기
 *
 * 흐름: POST /api/login → 백엔드가 JWT 발급 → localStorage 저장 → 이후 요청에 자동 첨부
 */
export default function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const { login } = useAuth()
  const navigate = useNavigate()
  const { pathname, state } = useLocation()

  const isLogin = pathname !== '/signup'

  // 회원가입 직후 /login으로 보내면서 넘긴 표시
  const [message, setMessage] = useState(
    state?.joined ? '회원가입이 완료되었습니다. 로그인해 주세요.' : ''
  )

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
      await api.post('/signup', { username, password })
      navigate('/login', { state: { joined: true } })
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
          <Link className="link-btn" to={isLogin ? '/signup' : '/login'}>
            {isLogin ? '회원가입' : '로그인'}
          </Link>
        </p>

        {message && <p className="msg">{message}</p>}
      </div>
    </main>
  )
}
