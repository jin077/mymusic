import { useEffect, useState } from 'react'
import { getChart } from '../music'
import TrackList from '../components/TrackList'

/**
 * 차트 페이지.
 *
 * 탭(TOP100 · 일간 · 주간)은 지금 화면만 있고 데이터는 하나다.
 *   → 백엔드 /api/music/chart 가 "지금 시점 차트" 한 종류만 주기 때문.
 *   → 일간·주간을 진짜로 나누려면 매일 차트를 DB에 저장해 두고
 *     기간별로 뽑아주는 API가 필요하다. (내일 백엔드 작업)
 */
const TABS = [
  { key: 'top100', label: 'TOP100' },
  { key: 'daily', label: '일간' },
  { key: 'weekly', label: '주간' },
]

export default function Chart() {
  const [tracks, setTracks] = useState([])
  const [loading, setLoading] = useState(true)
  const [tab, setTab] = useState('top100')

  useEffect(() => {
    getChart()
      .then(setTracks)
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="page">
      <h2 className="page-title">차트</h2>
      <p className="page-desc">인기 곡 목록입니다. 곡 오른쪽 버튼으로 듣기·담기·다운을 선택할 수 있습니다.</p>

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

      {loading
        ? <div className="empty">불러오는 중...</div>
        : <TrackList tracks={tracks} actions />}
    </main>
  )
}
