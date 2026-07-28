import { usePlayer } from '../player/PlayerContext'

/**
 * 곡 목록 (차트·검색결과에서 공통으로 사용)
 *
 * 같은 화면 조각을 여러 페이지에서 재사용하기 위해 컴포넌트로 분리했다.
 */
export default function TrackList({ tracks, showRank = true }) {
  const { current, playing, playTrack } = usePlayer()

  if (!tracks.length) {
    return <div className="empty">표시할 곡이 없습니다</div>
  }

  return (
    <div className="track-list">
      {tracks.map((t, i) => {
        const isCurrent = current?.id === t.id
        return (
          <div key={t.id} className={`track-row${isCurrent ? ' playing' : ''}`}>
            {showRank && <div className="track-rank">{i + 1}</div>}

            {/* 앨범 이미지 자리: 주소가 있으면 이미지, 없으면 회색 박스 */}
            {t.albumImage
              ? <img className="thumb" src={t.albumImage} alt="" />
              : <div className="thumb" />}

            <div className="track-info">
              <div className="track-title">{t.title}</div>
              <div className="track-artist">{t.artist} · {t.album}</div>
            </div>

            <button
              className={`play-btn${isCurrent && playing ? ' on' : ''}`}
              onClick={() => playTrack(t)}
              title="재생"
            >
              {isCurrent && playing ? '❚❚' : '▶'}
            </button>
          </div>
        )
      })}
    </div>
  )
}
