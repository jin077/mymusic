import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getNotices } from '../storage'

/**
 * 공지사항 게시판 — 글 목록.
 *
 * 글쓰기 버튼은 관리자(ADMIN)에게만 보인다.
 * (지금은 브라우저에 저장하므로 진짜 차단은 아니다.
 *  백엔드에 공지 API를 만들 때 서버에서 권한을 막아야 한다.)
 */
export default function Notice() {
  const { role } = useAuth()
  const [notices] = useState(() => getNotices())

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

        {notices.length === 0 ? (
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
    </main>
  )
}
