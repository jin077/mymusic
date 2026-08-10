import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { usePlayer } from '../player/PlayerContext'
import { getPlaylist, removeFromPlaylist } from '../storage'

/**
 * 마이페이지 = 내 정보 + 내 플레이리스트.
 *
 * 내 정보는 서버에 묻지 않고 JWT 토큰 안의 내용에서 꺼낸다.
 *   → 토큰에 들어 있는 건 아이디(sub)와 권한(role)뿐이다.
 *   → 이메일·닉네임은 아직 백엔드 Member 테이블에 컬럼이 없어 '미설정'으로 둔다.
 *
 * 플레이리스트는 차트에서 [담기]로 넣은 곡이다. (지금은 브라우저에 저장)
 */
export default function MyPage() {
  const { isLoggedIn, username, role } = useAuth()
  const { playTrack } = usePlayer()
  const [list, setList] = useState(() => getPlaylist(username))

  if (!isLoggedIn) {
    return (
      <main className="page">
        <h2 className="page-title">마이페이지</h2>
        <div className="empty">
          로그인이 필요합니다. <Link to="/login" style={{ color: '#17c964' }}>로그인하기</Link>
        </div>
      </main>
    )
  }

  const onRemove = (id) => setList(removeFromPlaylist(username, id))

  return (
    <main className="page">
      <h2 className="page-title">마이페이지</h2>

      {/* ── 내 정보 ── */}
      <div className="profile-card">
        <span className="avatar big">{username?.[0]?.toUpperCase()}</span>
        <div className="profile-info">
          <div className="profile-name">
            {username} <span className={`role-tag${role === 'ADMIN' ? ' admin' : ''}`}>{role}</span>
          </div>
          <div className="profile-sub">닉네임 : 미설정</div>
          <div className="profile-sub">이메일 : 미설정</div>
        </div>
        <button className="auth-btn" onClick={() => alert('회원정보 수정은 준비 중입니다.')}>
          정보 수정
        </button>
      </div>

      {/* ── 내 플레이리스트 ── */}
      <div className="section-title">
        <span>내 플레이리스트 ({list.length}곡)</span>
        <Link to="/chart" className="more">차트에서 담기 &gt;</Link>
      </div>

      {list.length === 0 ? (
        <div className="empty">담은 곡이 없습니다. 차트에서 [담기]를 눌러 보세요.</div>
      ) : (
        <div className="track-list">
          {list.map((t, i) => (
            <div className="track-row" key={t.id}>
              <div className="track-rank">{i + 1}</div>
              {t.albumImage
                ? <img className="thumb" src={t.albumImage} alt="" />
                : <div className="thumb" />}
              <div className="track-info">
                <div className="track-title">{t.title}</div>
                <div className="track-artist">{t.artist} · {t.album}</div>
              </div>
              <div className="track-actions">
                <button onClick={() => playTrack(t)}>듣기</button>
                <button onClick={() => onRemove(t.id)}>빼기</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </main>
  )
}
