import { Outlet } from 'react-router-dom'
import Header from './Header'
import PlayerBar from './PlayerBar'

/**
 * 모든 페이지가 공유하는 공통 틀.
 *
 * <Outlet /> 자리에 현재 주소에 맞는 페이지가 끼워진다.
 * → 헤더와 플레이어는 그대로 있고 가운데 내용만 바뀐다.
 *   이것이 페이지를 이동해도 음악이 끊기지 않는 구조의 핵심이다.
 */
export default function Layout() {
  return (
    <>
      <Header />
      <Outlet />
      <PlayerBar />
    </>
  )
}
