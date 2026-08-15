import { useNavigate } from 'react-router-dom'
import { usePlayer } from '../player/PlayerContext'
import { useAuth } from '../auth/AuthContext'
import { addToPlaylist, purchaseTrack, getTrackPrice } from '../storage'

/**
 * 곡 목록 (차트·검색결과에서 공통으로 사용)
 *
 * 같은 화면 조각을 여러 페이지에서 재사용하기 위해 컴포넌트로 분리했다.
 *
 * actions=true 로 주면 오른쪽에 [듣기][담기][다운] 버튼이 나온다. (차트 페이지용)
 * 기본값은 재생 버튼 하나만 (홈 오른쪽 칸처럼 좁은 곳용)
 *
 * extra : 곡을 받아 문자열을 돌려주는 함수를 넘기면 그 값을 오른쪽에 덧붙인다.
 *   주간 차트의 "평균 3.2위 (7일)" 같은 목록별 부가 정보를 위해 열어둔 자리다.
 *   이런 식으로 props를 하나 더 두면 컴포넌트를 복사하지 않고 재사용 범위를 넓힐 수 있다.
 */
export default function TrackList({ tracks, showRank = true, actions = false, extra = null }) {
  const { current, playing, playTrack } = usePlayer()
  const { isLoggedIn, refreshProfile } = useAuth()
  const navigate = useNavigate()

  if (!tracks.length) {
    return <div className="empty">표시할 곡이 없습니다</div>
  }

  /** 담기 : 내 플레이리스트에 추가 (로그인 필요) */
  const onAdd = async (t) => {
    if (!isLoggedIn) {
      if (confirm('담기는 로그인이 필요합니다. 로그인하러 갈까요?')) navigate('/login')
      return
    }
    try {
      const added = await addToPlaylist(t)
      alert(added ? '내 플레이리스트에 담았습니다.' : '이미 담긴 곡입니다.')
    } catch {
      alert('담기에 실패했습니다.')
    }
  }

  /**
   * 다운(구매) : 캐시에서 곡값을 차감하고 구매내역에 남긴다.
   *
   * ⚠️ 실제 음원 파일은 내려주지 않는다.
   *   우리가 가진 건 Apple의 30초 미리듣기 주소뿐이라, 파일로 제공하면 약관 위반이다.
   *   여기서 '다운'은 소장 표시(구매 이력)까지를 뜻한다.
   */
  const onDownload = async (t) => {
    if (!isLoggedIn) {
      if (confirm('다운로드는 로그인이 필요합니다. 로그인하러 갈까요?')) navigate('/login')
      return
    }
    const price = await getTrackPrice()   // 가격은 서버가 정한다
    if (!confirm(`'${t.title}'을(를) ${price.toLocaleString()}원에 다운로드할까요?`)) return

    try {
      await purchaseTrack(t)
      alert('구매했습니다. 마이페이지 > 구매내역에서 볼 수 있습니다.')
      refreshProfile()      // 헤더·마이페이지의 잔액 표시 갱신
    } catch (err) {
      const status = err.response?.status
      if (status === 409) {
        alert('이미 구매한 곡입니다.')
      } else if (status === 402) {
        // 402 = 잔액 부족. 충전으로 안내한다.
        if (confirm(`${err.response.data.message}\n충전하러 갈까요?`)) navigate('/mypage')
      } else {
        alert('구매에 실패했습니다.')
      }
    }
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

            {/* extra : 목록마다 다른 부가 정보를 붙일 수 있는 자리 (예: 주간 차트의 평균 순위) */}
            {extra && <span className="track-extra">{extra(t)}</span>}

            {actions ? (
              <div className="track-actions">
                <button onClick={() => playTrack(t)}>
                  {isCurrent && playing ? '정지' : '듣기'}
                </button>
                <button onClick={() => onAdd(t)}>담기</button>
                <button onClick={() => onDownload(t)}>다운</button>
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
