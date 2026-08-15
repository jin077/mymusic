import { Outlet } from 'react-router-dom'
import Header from './Header'
import PlayerBar from './PlayerBar'
import { usePlayer } from '../player/PlayerContext'

/**
 * 모든 페이지가 공유하는 공통 틀.
 *
 * <Outlet /> 자리에 현재 주소에 맞는 페이지가 끼워진다.
 * → 헤더와 플레이어는 그대로 있고 가운데 내용만 바뀐다.
 *   이것이 페이지를 이동해도 음악이 끊기지 않는 구조의 핵심이다.
 *
 * 하단 여백을 여기서 주는 이유:
 *   재생바는 화면 아래에 고정(fixed)이라 내용 위에 겹친다.
 *   그래서 재생바 높이만큼 여백이 필요한데, 재생바가 없을 때는 그 여백이
 *   빈 공간으로 남는다. → 재생 중일 때만 여백을 준다.
 */
export default function Layout() {
  const { current } = usePlayer()

  return (
    <div className={current ? 'with-player' : ''}>
      <Header />
      <Outlet />
      <PlayerBar />
    </div>
  )
}
