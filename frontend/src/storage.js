import api from './api'

/**
 * ===== 플레이리스트 · 공지사항 데이터 창구 =====
 *
 * ⭐ 예전에는 이 파일이 브라우저 localStorage를 읽고 썼다.
 *    백엔드에 API가 생기면서 이 파일 안쪽만 axios 호출로 바꿨다.
 *    함수 이름과 역할이 그대로라, 화면 컴포넌트는 부르는 방식만 유지하면 된다.
 *    (music.js에서 USE_BACKEND를 true로 바꾼 것과 같은 방식)
 *
 * ⭐ localStorage에서 서버로 옮긴 이유
 *    - 브라우저에만 남아서 다른 기기·다른 브라우저에서는 안 보였다
 *    - 사용자가 직접 값을 고칠 수 있어 신뢰할 수 없었다
 *    - "내가 담은 곡"은 계정에 딸린 데이터이므로 계정과 함께 서버에 있어야 한다
 *
 * ⭐ 누구의 플레이리스트인지 보내지 않는 이유
 *    서버가 토큰에서 사용자를 꺼내 쓴다. 프론트가 아이디를 보내는 구조였다면
 *    남의 아이디를 넣어 남의 목록을 조회·삭제할 수 있다.
 */

// ───────── 플레이리스트 ─────────

/**
 * 내가 담은 곡 목록.
 *   folderId 없음 → 전체 / 0 → 미분류 / 숫자 → 그 폴더
 */
export async function getPlaylist(folderId) {
  const res = await api.get('/playlist', {
    params: folderId == null ? {} : { folderId },
  })
  return res.data
}

/** 곡을 폴더로 옮기기 (folderId가 null이면 미분류로) */
export async function moveTrackToFolder(trackId, folderId) {
  await api.put(`/playlist/${trackId}/folder`, { folderId })
}

// ───────── 즐겨찾기 폴더 ─────────

export async function getFolders() {
  const res = await api.get('/playlist/folders')
  return res.data
}

export async function createFolder(name) {
  const res = await api.post('/playlist/folders', { name })
  return res.data
}

export async function renameFolder(id, name) {
  const res = await api.put(`/playlist/folders/${id}`, { name })
  return res.data
}

export async function deleteFolder(id) {
  await api.delete(`/playlist/folders/${id}`)
}

/** 담기. 이미 담긴 곡이면(409) false */
export async function addToPlaylist(track) {
  try {
    await api.post('/playlist', track)
    return true
  } catch (err) {
    if (err.response?.status === 409) return false
    throw err
  }
}

/** 빼기 */
export async function removeFromPlaylist(trackId) {
  await api.delete(`/playlist/${trackId}`)
}

// ───────── 구매 (다운로드) ─────────

/** 내 구매내역 */
export async function getPurchases() {
  const res = await api.get('/purchases')
  return res.data
}

/**
 * 곡 한 곡 값(원) — 화면에 안내하려고 서버에서 받아온다.
 *
 * ⭐ 값을 프론트에 적어두지 않는 이유
 *   가격이 바뀌면 서버와 화면이 어긋난다("100원입니다" 하고 200원이 빠지는 상황).
 *   가격의 주인은 서버 한 곳이어야 한다.
 *
 * 한 번 받아오면 기억해 둔다(화면마다 다시 묻지 않게).
 */
let priceCache = null
export async function getTrackPrice() {
  if (priceCache != null) return priceCache
  const res = await api.get('/purchases/price')
  priceCache = res.data.price
  return priceCache
}

/**
 * 곡 구매.
 * 가격은 보내지 않는다 — 얼마를 뺄지는 서버가 정한다.
 */
export async function purchaseTrack(track) {
  const res = await api.post('/purchases', track)
  return res.data
}

/** 캐시 충전 (실제 결제 없이 잔액만 늘리는 학습용 기능) */
export async function chargeCash(amount) {
  const res = await api.post('/members/me/charge', { amount })
  return res.data
}

// ───────── 재생 기록 ─────────

/**
 * 재생 기록 남기기.
 *
 * ⭐ 실패해도 조용히 넘어간다.
 *   기록은 부가 기능이다. 기록 저장이 안 됐다고 음악 재생을 막으면 안 된다.
 *   (로그인하지 않았으면 401이 오는데, 그것도 정상 흐름이다)
 */
export async function recordPlay(track) {
  try {
    await api.post('/history', track)
  } catch {
    // 무시
  }
}

/** 최근 재생한 곡 (같은 곡은 한 번만) */
export async function getRecentPlays() {
  const res = await api.get('/history/recent')
  return res.data
}

/** 많이 들은 곡 (재생 횟수 순) */
export async function getTopPlays() {
  const res = await api.get('/history/top')
  return res.data
}

/** 재생 기록 전체 삭제 */
export async function clearPlayHistory() {
  await api.delete('/history')
}

// ───────── 공지사항 ─────────

/**
 * 공지 목록 (한 쪽씩).
 * 돌려주는 모양: { content: [...], page, size, totalPages, totalElements, first, last }
 */
export async function getNotices(page = 0, size = 10) {
  const res = await api.get('/notices', { params: { page, size } })
  return res.data
}

export async function getNotice(id) {
  const res = await api.get(`/notices/${id}`)
  return res.data
}

export async function addNotice({ title, content }) {
  const res = await api.post('/notices', { title, content })
  return res.data
}

export async function updateNotice(id, { title, content }) {
  const res = await api.put(`/notices/${id}`, { title, content })
  return res.data
}

export async function removeNotice(id) {
  await api.delete(`/notices/${id}`)
}

// ───────── 이용권 ─────────

/** 상품 목록 (로그인 없이도 볼 수 있다) */
export async function getTicketPlans() {
  const res = await api.get('/tickets/plans')
  return res.data
}

/** 이용권 구매. 가격은 보내지 않는다 — 상품 코드만 보낸다. */
export async function purchaseTicket(plan) {
  const res = await api.post('/tickets/purchases', { plan })
  return res.data
}

/** 내 이용권 구매내역 */
export async function getTicketPurchases() {
  const res = await api.get('/tickets/purchases')
  return res.data
}

// ───────── 쪽지 ─────────

/** 내가 받은 쪽지 */
export async function getMessages() {
  const res = await api.get('/messages')
  return res.data
}

/** 안 읽은 쪽지 수 */
export async function getUnreadCount() {
  const res = await api.get('/messages/unread')
  return res.data.count
}

export async function readMessage(id) {
  await api.put(`/messages/${id}/read`)
}

export async function removeMessage(id) {
  await api.delete(`/messages/${id}`)
}

/** 쪽지 보내기 (관리자만) */
export async function sendMessage({ receiver, title, content }) {
  const res = await api.post('/messages', { receiver, title, content })
  return res.data
}

// ───────── 댓글 ─────────

/** 한 공지의 댓글 목록 (로그인 없이도 볼 수 있다) */
export async function getComments(noticeId) {
  const res = await api.get('/comments', { params: { noticeId } })
  return res.data
}

/** 내가 쓴 댓글 (어느 글에 썼는지 제목 포함) */
export async function getMyComments() {
  const res = await api.get('/comments/me')
  return res.data
}

export async function addComment(noticeId, content) {
  const res = await api.post('/comments', { noticeId, content })
  return res.data
}

export async function removeComment(id) {
  await api.delete(`/comments/${id}`)
}
