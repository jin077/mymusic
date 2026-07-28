import { createContext, useContext, useEffect, useRef, useState } from 'react'

/**
 * ===== 전역 음악 플레이어 =====
 *
 * 목표: 페이지를 이동해도 음악이 끊기지 않게 한다.
 *
 * 어떻게 가능한가?
 *   1) 우리 사이트는 SPA(Single Page Application)다.
 *      메뉴를 눌러도 브라우저가 새 문서를 받아오지 않고, React가 화면 일부만 바꿔 끼운다.
 *      → 페이지가 "새로 열리는" 게 아니므로 재생 중인 소리가 유지될 수 있다.
 *   2) 소리를 내는 Audio 객체를 페이지 컴포넌트 안이 아니라
 *      앱 최상단(Provider)에 딱 하나만 만들어 둔다.
 *      → 페이지가 바뀌며 사라져도 Audio는 그대로 살아있다.
 *
 * (반대로 일반 웹사이트에서 페이지 이동 시 음악이 끊기는 이유는
 *  이동할 때마다 브라우저가 문서를 새로 받아 화면 전체를 버리기 때문이다.)
 */
const PlayerContext = createContext(null)

export function PlayerProvider({ children }) {
  // ⭐ useRef: 화면이 다시 그려져도 값이 유지되는 보관함.
  //    Audio 객체를 여기 넣어 "앱 전체에서 딱 하나"만 쓰도록 한다.
  const audioRef = useRef(null)
  if (audioRef.current === null) audioRef.current = new Audio()

  const [current, setCurrent] = useState(null) // 현재 선택된 곡
  const [playing, setPlaying] = useState(false) // 재생 중인지
  const [time, setTime] = useState(0) // 현재 재생 위치(초)
  const [duration, setDuration] = useState(0) // 전체 길이(초)
  const [notice, setNotice] = useState('') // 안내 문구

  // Audio가 알려주는 사건들을 화면 상태로 옮긴다 (한 번만 등록)
  useEffect(() => {
    const audio = audioRef.current
    const onTime = () => setTime(audio.currentTime)
    const onMeta = () => setDuration(audio.duration || 0)
    const onEnded = () => setPlaying(false)

    audio.addEventListener('timeupdate', onTime)
    audio.addEventListener('loadedmetadata', onMeta)
    audio.addEventListener('ended', onEnded)
    return () => {
      audio.removeEventListener('timeupdate', onTime)
      audio.removeEventListener('loadedmetadata', onMeta)
      audio.removeEventListener('ended', onEnded)
    }
  }, [])

  /** 재생/일시정지 전환 */
  const toggle = () => {
    const audio = audioRef.current
    if (!current || !current.previewUrl) return
    if (audio.paused) {
      audio.play().then(() => setPlaying(true)).catch(() => setPlaying(false))
    } else {
      audio.pause()
      setPlaying(false)
    }
  }

  /** 곡을 눌렀을 때 */
  const playTrack = (track) => {
    // 같은 곡을 다시 누르면 재생/일시정지 토글
    if (current && current.id === track.id) {
      toggle()
      return
    }

    setCurrent(track)
    setTime(0)
    setDuration(0)

    // 미리듣기 음원이 없는 곡: 선택 표시만 하고 안내를 남긴다
    if (!track.previewUrl) {
      audioRef.current.pause()
      audioRef.current.removeAttribute('src')
      setPlaying(false)
      setNotice('미리듣기 음원이 없는 곡입니다')
      return
    }

    setNotice('')
    audioRef.current.src = track.previewUrl
    audioRef.current
      .play()
      .then(() => setPlaying(true))
      .catch(() => {
        setPlaying(false)
        setNotice('재생할 수 없습니다')
      })
  }

  /** 진행 바를 클릭해 재생 위치 이동 */
  const seek = (ratio) => {
    if (!duration) return
    audioRef.current.currentTime = duration * ratio
  }

  const value = { current, playing, time, duration, notice, playTrack, toggle, seek }
  return <PlayerContext.Provider value={value}>{children}</PlayerContext.Provider>
}

/** 어느 컴포넌트에서든 플레이어를 쓸 수 있게 해주는 도구 */
export function usePlayer() {
  const ctx = useContext(PlayerContext)
  if (!ctx) throw new Error('usePlayer는 PlayerProvider 안에서만 사용할 수 있습니다')
  return ctx
}
