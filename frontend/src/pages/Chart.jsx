import { useEffect, useState } from 'react'
import { getChart, getDailyChart, getWeeklyChart } from '../music'
import TrackList from '../components/TrackList'

/**
 * 차트 페이지 — 실시간 / 일간 / 주간.
 *
 * ⭐ 셋의 차이
 *   실시간 : Apple이 지금 주는 차트를 그대로 (서버 캐시 10분)
 *   일간   : 서버가 매일 새벽에 저장해 둔 그날의 기록 → 하루 동안 고정
 *   주간   : 최근 7일 기록을 곡별 평균 순위로 다시 정렬
 *
 * ⚠️ 일간·주간은 서버에 쌓인 만큼만 나온다.
 *   오늘 처음 켰다면 하루치뿐이라 주간이 일간과 비슷해 보인다. 날짜가 지나면 갈라진다.
 */
const TABS = [
  { key: 'live', label: '실시간', load: getChart },
  { key: 'daily', label: '일간', load: getDailyChart },
  { key: 'weekly', label: '주간', load: getWeeklyChart },
]

export default function Chart() {
  const [tracks, setTracks] = useState([])
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState('live')

  // 탭이 바뀌면 그 탭에 맞는 API를 부른다
  useEffect(() => {
    const current = TABS.find((t) => t.key === tab)
    setLoading(true)
    current.load()
      .then(setTracks)
      .catch(() => setTracks([]))
      .finally(() => setLoading(false))
  }, [tab])

  const desc = {
    live: 'Apple이 제공하는 지금 이 순간의 인기곡입니다.',
    daily: '매일 새벽에 저장한 그날의 차트입니다.',
    weekly: '최근 7일간의 평균 순위입니다.',
  }[tab]

  return (
    <main className="page">
      <h2 className="page-title">차트</h2>
      <p className="page-desc">{desc}</p>

      <div className="chart-tabs">
        {TABS.map((t) => (
          <button
            key={t.key}
            className={`chart-tab${tab === t.key ? ' on' : ''}`}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="empty">불러오는 중...</div>
      ) : tracks.length === 0 ? (
        <div className="empty">
          {tab === 'live'
            ? '표시할 곡이 없습니다.'
            : '아직 저장된 기록이 없습니다. 서버가 매일 새벽에 차트를 저장합니다.'}
        </div>
      ) : (
        <>
          {/* 주간은 평균 순위와 차트에 머문 날 수를 함께 보여준다 */}
          {tab === 'weekly' && (
            <div className="chart-note">
              평균 순위가 낮을수록 상위입니다. 괄호 안은 최근 7일 중 차트에 오른 날 수입니다.
            </div>
          )}
          <TrackList tracks={tracks} actions extra={tab === 'weekly' ? weeklyExtra : null} />
        </>
      )}
    </main>
  )
}

/** 주간 탭에서 각 줄 오른쪽에 덧붙일 정보 */
function weeklyExtra(t) {
  if (t.avgRank == null) return null
  return `평균 ${t.avgRank.toFixed(1)}위 (${t.days}일)`
}
