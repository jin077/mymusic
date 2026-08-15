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

/** 실시간 차트 (외부 API를 그대로) */
export async function getChart() {
  if (!USE_BACKEND) return MOCK_TRACKS
  const res = await api.get('/music/chart')
  return res.data
}

/**
 * 일간 차트 — 서버가 매일 저장해 둔 그날의 기록.
 * 실시간 차트와 달리 "하루 동안 고정"이라 순위가 흔들리지 않는다.
 */
export async function getDailyChart() {
  if (!USE_BACKEND) return MOCK_TRACKS
  const res = await api.get('/music/chart/daily')
  return res.data
}

/**
 * 주간 차트 — 최근 7일 기록을 곡별 평균 순위로 정렬한 것.
 * 각 곡에 avgRank(평균 순위)와 days(차트에 오른 날 수)가 함께 온다.
 */
export async function getWeeklyChart() {
  if (!USE_BACKEND) return MOCK_TRACKS
  const res = await api.get('/music/chart/weekly')
  return res.data
}

/** 인기 앨범 (Apple 앨범 차트 순위 순) */
export async function getAlbums() {
  if (!USE_BACKEND) return MOCK_ALBUMS
  const res = await api.get('/music/albums')
  return res.data
}

/** 앨범 수록곡 */
export async function getAlbumTracks(albumId) {
  if (!USE_BACKEND) return MOCK_TRACKS
  const res = await api.get(`/music/albums/${albumId}/tracks`)
  return res.data
}

/** 최신 앨범 (같은 목록을 발매일 순으로 정렬한 것) */
export async function getNewAlbums() {
  if (!USE_BACKEND) {
    // mock일 때도 백엔드와 같은 규칙으로 정렬해 화면이 똑같이 동작하게 한다
    return [...MOCK_ALBUMS].sort((a, b) => (b.releaseDate ?? '').localeCompare(a.releaseDate ?? ''))
  }
  const res = await api.get('/music/albums/new')
  return res.data
}
