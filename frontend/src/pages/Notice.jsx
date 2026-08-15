import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getNotices } from '../storage'
import Pagination from '../components/Pagination'

/**
 * 공지사항 게시판 — 글 목록.
 *
 * 조회는 로그인 없이도 된다(백엔드에서 GET만 permitAll).
 * 글쓰기 버튼은 관리자에게만 보이며, 실제 차단도 백엔드가 한다.
 *
 * ⭐ 목록을 한 번에 다 받지 않고 쪽 단위로 받는다.
 *   글이 많아져도 화면이 처음 뜨는 속도가 그대로다.
 */
const PAGE_SIZE = 10

export default function Notice() {
  const { role } = useAuth()
  const [data, setData] = useState(null)   // 서버가 준 쪽 정보 통째로
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    getNotices(page, PAGE_SIZE)
      .then(setData)
      .catch(() => setData(null))
      .finally(() => setLoading(false))
  }, [page])   // 쪽을 옮기면 다시 불러온다

  const notices = data?.content ?? []

  return (
    <main className="page">
      <div className="section-title" style={{ marginTop: 0 }}>
        <h2 className="page-title" style={{ margin: 0 }}>공지사항</h2>
        {role === 'ADMIN' && <Link to="/notice/write" className="auth-btn">글쓰기</Link>}
      </div>

      <div className="table">
        <div className="table-head">
          <span className="col-id">번호</span>
          <span className="col-title">제목</span>
          <span className="col-name">작성자</span>
          <span className="col-date">작성일</span>
        </div>

        {loading ? (
          <div className="empty">불러오는 중...</div>
        ) : notices.length === 0 ? (
          <div className="empty">등록된 공지가 없습니다.</div>
        ) : (
          notices.map((n) => (
            <Link className="table-row" key={n.id} to={`/notice/${n.id}`}>
              <span className="col-id">{n.id}</span>
              <span className="col-title">{n.title}</span>
              <span className="col-name">{n.writer}</span>
              <span className="col-date">{n.date}</span>
            </Link>
          ))
        )}
      </div>

      {data && (
        <Pagination
          page={data.page}
          totalPages={data.totalPages}
          first={data.first}
          last={data.last}
          onChange={setPage}
        />
      )}
    </main>
  )
}
