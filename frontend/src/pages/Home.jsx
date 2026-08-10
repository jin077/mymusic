import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getChart, getAlbums } from '../music'
import { useAuth } from '../auth/AuthContext'
import TrackList from '../components/TrackList'

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
  const [albums, setAlbums] = useState([])
  const { isLoggedIn, username, logout } = useAuth()

  // 화면이 처음 그려질 때 데이터를 불러온다
  useEffect(() => {
    getChart().then((list) => setTracks(list.slice(0, 10)))
    getAlbums().then((list) => setAlbums(list))
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
          {albums.slice(0, 5).map((a) => (
            <AlbumCard key={a.album} album={a} />
          ))}
        </div>

        <div className="section-title">
          <span>인기 앨범</span>
          <Link to="/albums" className="more">전체보기 &gt;</Link>
        </div>
        <div className="album-grid">
          {albums.slice(0, 5).map((a) => (
            <AlbumCard key={a.album} album={a} />
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
        <div className="side-box login-box">
          {isLoggedIn ? (
            <>
              <div className="login-hello">
                <span className="avatar">{username?.[0]?.toUpperCase()}</span>
                <b>{username}</b> 님
              </div>
              <Link className="side-btn" to="/mypage">마이페이지</Link>
              <button className="link-btn sub-link" onClick={logout}>로그아웃</button>
            </>
          ) : (
            <>
              <div className="login-hello">로그인하고 이용해 보세요</div>
              <Link className="side-btn" to="/login">로그인</Link>
              <Link className="link-btn sub-link" to="/signup">회원가입</Link>
            </>
          )}
        </div>

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

/** 앨범 카드 한 장 (최신·인기에서 같이 쓴다) */
function AlbumCard({ album }) {
  return (
    <div>
      {album.albumImage
        ? <img className="album-cover" src={album.albumImage} alt="" />
        : <div className="album-cover" />}
      <div className="album-name">{album.album}</div>
      <div className="album-artist">{album.artist}</div>
    </div>
  )
}
