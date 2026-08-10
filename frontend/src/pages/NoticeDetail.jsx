import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getNotice, removeNotice } from '../storage'

/**
 * 공지사항 글 하나 보기.
 *
 * useParams() : 주소 /notice/3 의 "3"을 꺼낸다. (App.jsx의 path="/notice/:id"와 짝)
 */
export default function NoticeDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { role } = useAuth()
  const notice = getNotice(id)

  if (!notice) {
    return (
      <main className="page">
        <div className="empty">
          없는 글입니다. <Link to="/notice" style={{ color: '#17c964' }}>목록으로</Link>
        </div>
      </main>
    )
  }

  const onDelete = () => {
    if (!confirm('이 글을 삭제할까요?')) return
    removeNotice(id)
    navigate('/notice')
  }

  return (
    <main className="page">
      <div className="post">
        <h2 className="post-title">{notice.title}</h2>
        <div className="post-meta">{notice.writer} · {notice.date}</div>
        <div className="post-content">{notice.content}</div>
      </div>

      <div className="post-buttons">
        <Link className="auth-btn" to="/notice">목록</Link>
        {role === 'ADMIN' && (
          <button className="auth-btn" onClick={onDelete}>삭제</button>
        )}
      </div>
    </main>
  )
}
