import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth/AuthContext'

/**
 * 프로필 페이지.
 *
 * - 로그인한 사용자 정보는 JWT 토큰 안의 내용에서 꺼내 보여준다(서버 호출 없이).
 * - 관리자(ADMIN)만 전체 회원 목록을 조회할 수 있다.
 *   ⭐ 버튼을 숨기는 것은 화면 편의일 뿐, 실제 차단은 백엔드가 한다.
 *      (SecurityConfig의 /api/admin/** hasRole("ADMIN"))
 *      프론트의 숨김은 우회할 수 있으므로 권한 검사는 반드시 서버에서 해야 한다.
 */
export default function Profile() {
  const { isLoggedIn, username, role, logout } = useAuth()
  const [members, setMembers] = useState([])
  const [message, setMessage] = useState('')

  if (!isLoggedIn) {
    return (
      <main className="page">
        <h2 className="page-title">프로필</h2>
        <div className="empty">
          로그인이 필요합니다. <Link to="/login" style={{ color: '#17c964' }}>로그인하기</Link>
        </div>
      </main>
    )
  }

  const loadMembers = async () => {
    try {
      const res = await api.get('/admin/members')
      setMembers(res.data)
      setMessage(`회원 ${res.data.length}명 조회 성공`)
    } catch (err) {
      setMessage(
        err.response?.status === 403
          ? '권한 없음(403): 관리자만 조회할 수 있습니다'
          : '조회 실패'
      )
    }
  }

  return (
    <main className="page">
      <h2 className="page-title">프로필</h2>
      <p className="page-desc">로그인한 계정 정보입니다.</p>

      <div className="member-row">아이디 : <b>{username}</b></div>
      <div className="member-row">권한 : <b>{role ?? '-'}</b></div>
      <button className="auth-btn" style={{ marginTop: 16 }} onClick={logout}>로그아웃</button>

      {role === 'ADMIN' && (
        <>
          <div className="section-title">관리자 기능 · 전체 회원 목록</div>
          <button className="auth-btn" onClick={loadMembers}>회원 목록 불러오기</button>
          <div style={{ marginTop: 12 }}>
            {members.map((m) => (
              <div key={m.id} className="member-row">
                #{m.id} · <b>{m.username}</b> ({m.role})
              </div>
            ))}
          </div>
        </>
      )}

      {message && <p className="msg">{message}</p>}
    </main>
  )
}
