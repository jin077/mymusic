import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getChart, getAlbums } from '../music'
import TrackList from '../components/TrackList'

export default function Home() {
  const [tracks, setTracks] = useState([])
  const [albums, setAlbums] = useState([])

  // 화면이 처음 그려질 때 데이터를 불러온다
  useEffect(() => {
    getChart().then((list) => setTracks(list.slice(0, 5)))
    getAlbums().then((list) => setAlbums(list.slice(0, 6)))
  }, [])

  return (
    <main className="page">
      <h2 className="page-title">MyMusic</h2>
      <p className="page-desc">멜론 UI 구조를 참고해 만든 학습용 음악 서비스입니다.</p>

      <div className="section-title">
        <span>실시간 차트 TOP 5</span>
        <Link to="/chart" style={{ fontSize: 14, color: '#888' }}>전체보기 &gt;</Link>
      </div>
      <TrackList tracks={tracks} />

      <div className="section-title">
        <span>최신 앨범</span>
        <Link to="/albums" style={{ fontSize: 14, color: '#888' }}>전체보기 &gt;</Link>
      </div>
      <div className="album-grid">
        {albums.map((a) => (
          <div key={a.album}>
            {a.albumImage
              ? <img className="album-cover" src={a.albumImage} alt="" />
              : <div className="album-cover" />}
            <div className="album-name">{a.album}</div>
            <div className="album-artist">{a.artist}</div>
          </div>
        ))}
      </div>
    </main>
  )
}
