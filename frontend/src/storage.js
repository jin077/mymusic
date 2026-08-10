/**
 * ===== 임시 저장소 (브라우저 localStorage) =====
 *
 * 플레이리스트와 공지사항은 아직 백엔드에 저장할 곳이 없다.
 * 그래서 지금은 브라우저에 저장해 두고, 화면이 먼저 완성되게 한다.
 *
 * ⭐ 저장소를 건드리는 코드를 이 파일 하나에 모아둔 이유
 *    나중에 백엔드 API가 생기면 이 파일의 함수 속만 axios 호출로 바꾸면 되고,
 *    화면 컴포넌트는 한 줄도 고치지 않아도 된다. (music.js와 같은 방식)
 *
 * ⚠️ localStorage의 한계 — 면접에서 말할 수 있는 지점
 *    - 브라우저에만 남는다 → 다른 기기·다른 브라우저에서는 안 보인다
 *    - 사용자가 직접 지우거나 고칠 수 있다 → 신뢰할 수 없는 데이터
 *    그래서 "진짜 내 데이터"는 결국 서버 DB에 있어야 한다.
 */

function read(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch {
    return fallback
  }
}

function write(key, value) {
  localStorage.setItem(key, JSON.stringify(value))
}

// ───────── 플레이리스트 (사용자별로 따로 보관) ─────────

const playlistKey = (username) => `playlist:${username}`

export function getPlaylist(username) {
  if (!username) return []
  return read(playlistKey(username), [])
}

/** 담기. 이미 있으면 넣지 않는다. 담겼으면 true */
export function addToPlaylist(username, track) {
  const list = getPlaylist(username)
  if (list.some((t) => t.id === track.id)) return false
  write(playlistKey(username), [...list, track])
  return true
}

export function removeFromPlaylist(username, trackId) {
  const list = getPlaylist(username).filter((t) => t.id !== trackId)
  write(playlistKey(username), list)
  return list
}

// ───────── 공지사항 게시판 ─────────

const NOTICE_KEY = 'notices'

/** 처음 방문했을 때 보여줄 기본 글 */
const SEED = [
  { id: 3, title: '메인 화면 개편 안내', writer: 'admin', date: '2026-08-10',
    content: '메인 화면을 앨범·차트 중심으로 새로 구성했습니다.' },
  { id: 2, title: '서비스 실행 환경 변경 안내', writer: 'admin', date: '2026-08-04',
    content: '서비스 실행 환경을 컨테이너로 전환했습니다.' },
  { id: 1, title: 'MyMusic 서비스 오픈', writer: 'admin', date: '2026-07-29',
    content: 'MyMusic 서비스를 시작합니다.' },
]

export function getNotices() {
  return read(NOTICE_KEY, SEED)
}

export function getNotice(id) {
  return getNotices().find((n) => String(n.id) === String(id)) ?? null
}

export function addNotice({ title, content, writer }) {
  const list = getNotices()
  const nextId = list.length ? Math.max(...list.map((n) => n.id)) + 1 : 1
  const notice = {
    id: nextId,
    title,
    content,
    writer,
    date: new Date().toISOString().slice(0, 10), // YYYY-MM-DD
  }
  write(NOTICE_KEY, [notice, ...list]) // 최신 글이 위로
  return notice
}

export function removeNotice(id) {
  write(NOTICE_KEY, getNotices().filter((n) => String(n.id) !== String(id)))
}
