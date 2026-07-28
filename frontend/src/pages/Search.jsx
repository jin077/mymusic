import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { searchTracks } from '../music'
import TrackList from '../components/TrackList'

/**
 * 검색 결과 페이지.
 *
 * 검색어를 화면 상태가 아니라 주소(/search?q=...)에 담는 이유:
 *   - 결과 화면을 그대로 링크로 공유할 수 있다
 *   - 새로고침해도 검색어가 유지된다
 *   실무에서 검색 화면을 만드는 기본 방식이다.
 */
export default function Search() {
  const [params] = useSearchParams()
  const keyword = params.get('q') ?? ''
  const [tracks, setTracks] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!keyword) {
      setTracks([])
      return
    }
    setLoading(true)
    searchTracks(keyword)
      .then(setTracks)
      .catch(() => setTracks([]))
      .finally(() => setLoading(false))
  }, [keyword]) // 검색어가 바뀔 때마다 다시 조회

  return (
    <main className="page">
      <h2 className="page-title">검색 결과</h2>
      <p className="page-desc">
        {keyword ? `"${keyword}" 검색 결과 ${tracks.length}건` : '검색어를 입력해 주세요'}
      </p>
      {loading ? <div className="empty">검색 중...</div> : <TrackList tracks={tracks} showRank={false} />}
    </main>
  )
}
