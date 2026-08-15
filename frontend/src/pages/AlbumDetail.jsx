import { useEffect, useState } from 'react'
import { useParams, useLocation, Link } from 'react-router-dom'
import { getAlbumTracks } from '../music'
import TrackList from '../components/TrackList'

/**
 * 앨범 상세 — 수록곡 목록.
 *
 * ⭐ 앨범 정보(제목·가수·커버)를 다시 조회하지 않고 목록 화면에서 넘겨받는다.
 *   <Link state={album}> 으로 보내면 useLocation().state 로 받을 수 있다.
 *   주소를 직접 입력해 들어오면 state가 없으므로, 그때는 수록곡의 앨범명으로 대신 채운다.
 */
export default function AlbumDetail() {
  const { id } = useParams()
  const { state } = useLocation()      // 목록에서 넘겨준 앨범 정보 (없을 수 있다)

  const [tracks, setTracks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    getAlbumTracks(id)
      .then(setTracks)
      .catch(() => setTracks([]))
      .finally(() => setLoading(false))
  }, [id])

  // 목록에서 온 정보가 없으면 첫 곡에서 앨범 이름을 빌려온다
  const cover = state?.albumImage ?? tracks[0]?.albumImage
  const albumName = state?.album ?? tracks[0]?.album ?? '앨범'
  const artist = state?.artist ?? tracks[0]?.artist ?? ''

  return (
    <main className="page">
      <div className="album-header">
        {cover
          ? <img className="album-header-cover" src={cover} alt="" />
          : <div className="album-header-cover" />}
        <div>
          <h2 className="page-title" style={{ margin: '0 0 6px' }}>{albumName}</h2>
          <div className="album-artist">{artist}</div>
          {state?.releaseDate && <div className="album-date">발매일 {state.releaseDate}</div>}
          {state?.rank && <div className="album-date">인기 {state.rank}위</div>}
          <Link to="/albums" className="more" style={{ display: 'inline-block', marginTop: 10 }}>
            &lt; 앨범 목록
          </Link>
        </div>
      </div>

      <div className="section-title">
        <span>수록곡 ({tracks.length})</span>
      </div>

      {loading
        ? <div className="empty">불러오는 중...</div>
        : <TrackList tracks={tracks} actions />}
    </main>
  )
}
