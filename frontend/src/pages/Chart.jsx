import { useEffect, useState } from 'react'
import { getChart } from '../music'
import TrackList from '../components/TrackList'

export default function Chart() {
  const [tracks, setTracks] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getChart()
      .then(setTracks)
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="page">
      <h2 className="page-title">차트</h2>
      <p className="page-desc">인기 곡 목록입니다. 곡 오른쪽 재생 버튼을 누르면 하단 플레이어에서 재생됩니다.</p>
      {loading ? <div className="empty">불러오는 중...</div> : <TrackList tracks={tracks} />}
    </main>
  )
}
