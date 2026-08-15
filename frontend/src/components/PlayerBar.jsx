import { usePlayer } from '../player/PlayerContext'

/** 초 → 0:00 형식 */
function fmt(sec) {
  if (!sec || Number.isNaN(sec)) return '0:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

/**
 * 화면 맨 아래 고정된 플레이어.
 *
 * 이 컴포넌트는 '표시'만 담당한다.
 * 실제 소리를 내는 Audio 객체는 PlayerProvider가 들고 있으므로,
 * 페이지를 이동해도 재생이 끊기지 않는다.
 */
export default function PlayerBar() {
  const { current, playing, time, duration, notice, toggle, seek } = usePlayer()

  const onBarClick = (e) => {
    const box = e.currentTarget.getBoundingClientRect()
    seek((e.clientX - box.left) / box.width)
  }

  // 곡을 고르기 전에는 재생바를 아예 그리지 않는다.
  //   null을 리턴하면 화면에 아무것도 남지 않는다(React에서 "안 그림"을 뜻함).
  //   한 번 재생한 뒤에는 Provider가 current를 계속 들고 있으므로 페이지를 옮겨도 유지된다.
  if (!current) return null

  const ratio = duration ? (time / duration) * 100 : 0

  return (
    <div className="player">
      {current.albumImage
        ? <img className="thumb" src={current.albumImage} alt="" />
        : <div className="thumb" />}
      <div>
        <div className="player-title">{current.title}</div>
        <div className="player-artist">{current.artist}</div>
      </div>

      <div className="player-main">
        <button className="player-toggle" onClick={toggle}>
          {playing ? '❚❚' : '▶'}
        </button>
        <div className="player-bar" onClick={onBarClick}>
          <div style={{ width: `${ratio}%` }} />
        </div>
        <span className="player-time">{fmt(time)} / {fmt(duration)}</span>
      </div>

      {notice && <span className="player-empty">{notice}</span>}
    </div>
  )
}
