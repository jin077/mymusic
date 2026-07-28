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

  if (!current) {
    return (
      <div className="player">
        <span className="player-empty">재생할 곡을 선택해 주세요</span>
      </div>
    )
  }

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
