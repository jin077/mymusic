import { useState } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

/**
 * 사이트 상단 헤더 (2단 구조).
 *
 *   윗줄 : 로고 + 검색창 + 마이메뉴(로그인 상태에 따라 달라짐)
 *   아랫줄 : 메뉴(GNB)
 *
 * 오른쪽에 무엇이 보이는가
 *   로그인 전 : [로그인] [회원가입]   ※ 단, 홈에는 오른쪽 칸에 로그인 박스가 따로 있어 숨긴다
 *   로그인 후 : (프로필 동그라미) 닉네임 · 마이페이지 · 로그아웃  ← 모든 페이지에 표시
 *
 * 검색창을 헤더에 두는 이유:
 *   어느 페이지에 있어도(그리고 음악이 재생 중이어도) 바로 검색할 수 있게 하기 위함.
 */
export default function Header() {
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()
  const { pathname } = useLocation()   // 지금 보고 있는 주소
  const { isLoggedIn, username, role, logout } = useAuth()

  const isHome = pathname === '/'

  const onSearch = (e) => {
    e.preventDefault()
    const k = keyword.trim()
    if (!k) return
    navigate(`/search?q=${encodeURIComponent(k)}`)
  }

  const onLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="header">
      {/* 윗줄: 로고와 검색창을 같은 라인에 둔다 */}
      <div className="header-top">
        <NavLink to="/" className="logo">MyMusic</NavLink>

        <form className="search-form" onSubmit={onSearch}>
          <input
            placeholder="곡·가수 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit">검색</button>
        </form>

        {isLoggedIn ? (
          /* ── 마이메뉴 ── */
          <div className="my-menu">
            <span className="avatar">{username?.[0]?.toUpperCase() ?? '?'}</span>
            <span className="my-name">{username}</span>
            <NavLink to="/mypage" className="my-link">마이페이지</NavLink>
            {role === 'ADMIN' && <NavLink to="/admin" className="my-link">관리자</NavLink>}
            <button className="my-link" onClick={onLogout}>로그아웃</button>
          </div>
        ) : (
          /* 홈에는 오른쪽에 로그인 박스가 있으므로 헤더에서는 숨긴다 */
          !isHome && (
            <div className="my-menu">
              <NavLink to="/login" className="auth-btn">로그인</NavLink>
              <NavLink to="/signup" className="auth-btn">회원가입</NavLink>
            </div>
          )
        )}
      </div>

      {/* 아랫줄: 메뉴 */}
      <div className="header-nav">
        <nav className="gnb">
          <NavLink to="/chart">차트</NavLink>
          <NavLink to="/albums">앨범</NavLink>
          <NavLink to="/ticket">이용권</NavLink>
          <NavLink to="/notice">공지사항</NavLink>
        </nav>
      </div>
    </header>
  )
}
