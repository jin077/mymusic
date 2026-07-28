import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

/**
 * 사이트 상단 헤더.
 *
 * 검색창을 헤더에 두는 이유:
 *   어느 페이지에 있어도(그리고 음악이 재생 중이어도) 바로 검색할 수 있게 하기 위함.
 *   검색하면 /search?q=키워드 주소로 이동한다.
 */
export default function Header() {
  const [keyword, setKeyword] = useState('')
  const navigate = useNavigate()
  const { isLoggedIn, username, logout } = useAuth()

  const onSearch = (e) => {
    e.preventDefault()
    const k = keyword.trim()
    if (!k) return
    navigate(`/search?q=${encodeURIComponent(k)}`)
  }

  return (
    <header className="header">
      <div className="header-inner">
        <NavLink to="/" className="logo">MyMusic</NavLink>

        <nav className="gnb">
          <NavLink to="/" end>홈</NavLink>
          <NavLink to="/chart">차트</NavLink>
          <NavLink to="/albums">앨범</NavLink>
          <NavLink to="/ticket">이용권</NavLink>
          <NavLink to="/profile">프로필</NavLink>
        </nav>

        <form className="search-form" onSubmit={onSearch}>
          <input
            placeholder="곡·가수 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit">검색</button>
        </form>

        {isLoggedIn ? (
          <button className="auth-btn" onClick={logout}>
            {username ?? '사용자'} 로그아웃
          </button>
        ) : (
          <NavLink to="/login" className="auth-btn">로그인</NavLink>
        )}
      </div>
    </header>
  )
}
