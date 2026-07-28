import api from './api'
import { MOCK_TRACKS, MOCK_ALBUMS } from './mock'

/**
 * ===== 음악 데이터를 가져오는 창구 =====
 *
 * 화면(컴포넌트)은 "데이터가 어디서 오는지" 몰라도 되게 만든다.
 * 지금은 mock에서 오지만, 백엔드 Spotify 연동이 끝나면
 * USE_BACKEND 를 true 로만 바꾸면 실제 데이터로 교체된다.
 *
 * 이렇게 외부 의존성을 한 파일에 모아두는 이유:
 *   - 나중에 Spotify가 아닌 다른 API로 바꿔도 이 파일만 고치면 된다.
 *   - 화면 코드는 손대지 않는다. (실무에서 쓰는 '의존성 격리')
 *
 * ⭐ 중요: 프론트는 Spotify를 직접 부르지 않는다.
 *    반드시 우리 백엔드(/api/music/...)를 거친다.
 *    → Spotify Client Secret이 브라우저에 노출되지 않게 하기 위함.
 */
// ⭐ 백엔드 연동 완료 → true
//   화면 코드(컴포넌트)는 한 줄도 고치지 않고 이 한 줄만 바꿨다.
//   이것이 데이터 창구를 한 파일로 모아둔 이유(의존성 격리)의 효과다.
const USE_BACKEND = true

/** 곡 검색 */
export async function searchTracks(keyword) {
  const k = keyword.trim()
  if (!k) return []

  if (!USE_BACKEND) {
    const lower = k.toLowerCase()
    return MOCK_TRACKS.filter((t) =>
      (t.title + t.artist + t.album).toLowerCase().includes(lower)
    )
  }
  const res = await api.get('/music/search', { params: { q: k } })
  return res.data
}

/** 차트(인기곡) 목록 */
export async function getChart() {
  if (!USE_BACKEND) return MOCK_TRACKS
  const res = await api.get('/music/chart')
  return res.data
}

/** 앨범 목록 */
export async function getAlbums() {
  if (!USE_BACKEND) return MOCK_ALBUMS
  const res = await api.get('/music/albums')
  return res.data
}
