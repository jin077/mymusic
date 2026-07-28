import { useEffect, useState } from 'react'
import { getAlbums } from '../music'

export default function Albums() {
  const [albums, setAlbums] = useState([])

  useEffect(() => {
    getAlbums().then(setAlbums)
  }, [])

  return (
    <main className="page">
      <h2 className="page-title">앨범</h2>
      <p className="page-desc">차트에 오른 곡들의 앨범입니다.</p>

      <div className="album-grid">
        {albums.map((a) => (
          <div key={a.album}>
            {/* 이미지 주소가 있으면 앨범 커버, 없으면 회색 박스 */}
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
