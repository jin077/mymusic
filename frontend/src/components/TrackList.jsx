import { useNavigate } from 'react-router-dom'
import { usePlayer } from '../player/PlayerContext'
import { useAuth } from '../auth/AuthContext'
import { addToPlaylist } from '../storage'

/**
 * 곡 목록 (차트·검색결과에서 공통으로 사용)
 *
 * 같은 화면 조각을 여러 페이지에서 재사용하기 위해 컴포넌트로 분리했다.
 *
 * actions=true 로 주면 오른쪽에 [듣기][담기][다운] 버튼이 나온다. (차트 페이지용)
 * 기본값은 재생 버튼 하나만 (홈 오른쪽 칸처럼 좁은 곳용)
 */
export default function TrackList({ tracks, showRank = true, actions = false }) {
  const { current, playing, playTrack } = usePlayer()
  const { isLoggedIn, username } = useAuth()
  const navigate = useNavigate()

  if (!tracks.length) {
    return <div className="empty">표시할 곡이 없습니다</div>
  }

  /** 담기 : 내 플레이리스트에 추가 (로그인 필요) */
  const onAdd = (t) => {
    if (!isLoggedIn) {
      if (confirm('담기는 로그인이 필요합니다. 로그인하러 갈까요?')) navigate('/login')
      return
    }
    const added = addToPlaylist(username, t)
    alert(added ? '내 플레이리스트에 담았습니다.' : '이미 담긴 곡입니다.')
  }

  // 다운로드는 음원 파일을 우리가 가지고 있지 않아 제공할 수 없다(미리듣기 30초만 있음).
  const notReady = (name) => alert(`${name} 기능은 아직 준비 중입니다.`)

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

            {actions ? (
              <div className="track-actions">
                <button onClick={() => playTrack(t)}>
                  {isCurrent && playing ? '정지' : '듣기'}
                </button>
                <button onClick={() => onAdd(t)}>담기</button>
                <button onClick={() => notReady('다운')}>다운</button>
              </div>
            ) : (
              <button
                className={`play-btn${isCurrent && playing ? ' on' : ''}`}
                onClick={() => playTrack(t)}
                title="재생"
              >
                {isCurrent && playing ? '❚❚' : '▶'}
              </button>
            )}
          </div>
        )
      })}
    </div>
  )
}
