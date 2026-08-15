import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAlbums, getNewAlbums } from '../music'

/**
 * 앨범 목록 — 인기 / 최신 두 가지.
 *
 * 두 목록은 같은 원본(Apple 앨범 차트)에서 나온다.
 *   인기 : 차트 순위 순
 *   최신 : 같은 목록을 발매일 순으로 정렬한 것
 *          (Apple이 신보 전용 피드를 제공하지 않아 이렇게 만든다)
 */
export default function Albums() {
  const [tab, setTab] = useState('popular')   // 'popular' | 'new'
  const [albums, setAlbums] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    const load = tab === 'new' ? getNewAlbums : getAlbums
    load()
      .then(setAlbums)
      .catch(() => setAlbums([]))
      .finally(() => setLoading(false))
  }, [tab])   // 탭이 바뀌면 다시 불러온다

  return (
    <main className="page">
      <h2 className="page-title">앨범</h2>
      <p className="page-desc">
        {tab === 'new' ? '최근 발매된 순서입니다.' : '한국 인기 앨범 순위입니다.'}
      </p>

      <div className="chart-tabs">
        <button
          className={`chart-tab${tab === 'popular' ? ' on' : ''}`}
          onClick={() => setTab('popular')}
        >
          인기 앨범
        </button>
        <button
          className={`chart-tab${tab === 'new' ? ' on' : ''}`}
          onClick={() => setTab('new')}
        >
          최신 앨범
        </button>
      </div>

      {loading ? (
        <div className="empty">불러오는 중...</div>
      ) : (
        <div className="album-grid">
          {/* 앨범을 누르면 수록곡 화면으로. 이미 갖고 있는 앨범 정보를 state로 함께 넘겨
              상세 화면이 같은 정보를 다시 조회하지 않게 한다. */}
          {albums.map((a) => (
            <Link className="album-card" key={a.id} to={`/album/${a.id}`} state={a}>
              <div className="album-cover-box">
                {a.albumImage
                  ? <img className="album-cover" src={a.albumImage} alt="" />
                  : <div className="album-cover" />}
                {/* 순위를 값으로 들고 다니므로, 발매일순으로 정렬해도 원래 인기 순위가 유지된다 */}
                {tab === 'popular' && <span className="album-rank">{a.rank}</span>}
              </div>
              <div className="album-name">{a.album}</div>
              <div className="album-artist">{a.artist}</div>
              {a.releaseDate && <div className="album-date">{a.releaseDate}</div>}
            </Link>
          ))}
        </div>
      )}
    </main>
  )
}
