import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getChart, getAlbums, getNewAlbums } from '../music'
import { getUnreadCount } from '../storage'
import { useAuth } from '../auth/AuthContext'
import TrackList from '../components/TrackList'
import Avatar from '../components/Avatar'

/** 장르 박스 — 아직 장르 API가 없어 검색어로 연결해 둔다 */
const GENRES = [
  { name: '국내', q: '가요' },
  { name: '해외', q: 'pop' },
  { name: 'OST', q: 'OST' },
  { name: '팝송', q: 'pop song' },
  { name: '발라드', q: '발라드' },
]

export default function Home() {
  const [tracks, setTracks] = useState([])
  const [albums, setAlbums] = useState([])        // 인기 앨범
  const [newAlbums, setNewAlbums] = useState([])  // 최신 앨범
  const { isLoggedIn, username, logout, displayName, profileImage, profile } = useAuth()

  // 이용권은 내 정보(profile)에 함께 온다. 쪽지 수만 따로 조회한다.
  const [messageCount, setMessageCount] = useState(0)

  useEffect(() => {
    if (!isLoggedIn) { setMessageCount(0); return }
    getUnreadCount().then(setMessageCount).catch(() => setMessageCount(0))
  }, [isLoggedIn])

  // 화면이 처음 그려질 때 데이터를 불러온다
  useEffect(() => {
    getChart().then((list) => setTracks(list.slice(0, 10)))
    getAlbums().then(setAlbums)
    getNewAlbums().then(setNewAlbums)
  }, [])

  return (
    <main className="page home">
      {/* ── 왼쪽: 앨범과 장르 ── */}
      <div className="home-main">
        <div className="section-title">
          <span>최신 앨범</span>
          <Link to="/albums" className="more">전체보기 &gt;</Link>
        </div>
        <div className="album-grid">
          {newAlbums.slice(0, 5).map((a) => (
            <AlbumCard key={a.id} album={a} showDate />
          ))}
        </div>

        <div className="section-title">
          <span>인기 앨범</span>
          <Link to="/albums" className="more">전체보기 &gt;</Link>
        </div>
        <div className="album-grid">
          {albums.slice(0, 5).map((a) => (
            <AlbumCard key={a.id} album={a} showRank />
          ))}
        </div>

        <div className="section-title">
          <span>장르</span>
        </div>
        <div className="genre-grid">
          {GENRES.map((g) => (
            <Link key={g.name} className="genre-box" to={`/search?q=${encodeURIComponent(g.q)}`}>
              {g.name}
            </Link>
          ))}
        </div>
      </div>

      {/* ── 오른쪽: 로그인과 차트 ── */}
      <aside className="home-side">
        {isLoggedIn ? (
          /* ── 로그인 후: 내 정보 요약 ── */
          <div className="side-box member-box">
            <div className="member-top">
              {/* 사진을 누르면 마이페이지로 */}
              <Link to="/mypage" className="member-photo">
                <Avatar src={profileImage} name={username} size="big" />
              </Link>

              <div className="member-info">
                <div className="member-line">
                  <Link to="/mypage" className="member-name">{displayName}</Link>
                  <button className="link-btn member-logout" onClick={logout}>로그아웃</button>
                </div>

                <div className="member-line sub">
                  <span>이용권</span>
                  {profile?.ticketDaysLeft != null
                    ? <span className="side-value">{profile.ticketDaysLeft}일 남음</span>
                    : <Link to="/ticket" className="side-value none">없음</Link>}
                </div>
              </div>
            </div>

            <div className="member-links">
              <Link to="/mypage">쪽지 {messageCount}</Link>{/* 안 읽은 개수 */}
              <Link to="/mypage">마이페이지</Link>
              <Link to="/event">이벤트</Link>
            </div>
          </div>
        ) : (
          /* ── 로그인 전 ── */
          <div className="side-box login-box">
            <div className="login-hello">로그인하고 이용해 보세요</div>
            <Link className="side-btn" to="/login">로그인</Link>
            <Link className="link-btn sub-link" to="/signup">회원가입</Link>
          </div>
        )}

        <div className="side-box">
          <div className="section-title small">
            <span>실시간 차트</span>
            <Link to="/chart" className="more">더보기 &gt;</Link>
          </div>
          <TrackList tracks={tracks} />
        </div>
      </aside>
    </main>
  )
}

/**
 * 앨범 카드 한 장 (최신·인기에서 같이 쓴다)
 *
 * 같은 카드지만 최신 앨범에는 발매일을, 인기 앨범에는 순위를 보여준다.
 * → 차이를 props로 받는다 (TrackList의 showRank·actions와 같은 방식)
 */
function AlbumCard({ album, showRank = false, showDate = false }) {
  return (
    <Link className="album-card" to={`/album/${album.id}`} state={album}>
      <div className="album-cover-box">
        {album.albumImage
          ? <img className="album-cover" src={album.albumImage} alt="" />
          : <div className="album-cover" />}
        {showRank && <span className="album-rank">{album.rank}</span>}
      </div>
      <div className="album-name">{album.album}</div>
      <div className="album-artist">{album.artist}</div>
      {showDate && album.releaseDate && (
        <div className="album-date">{album.releaseDate}</div>
      )}
    </Link>
  )
}
