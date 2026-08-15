import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getNotice, removeNotice } from '../storage'
import CommentBox from '../components/CommentBox'
import UserTag from '../components/UserTag'

/**
 * 공지사항 글 하나 보기.
 *
 * useParams() : 주소 /notice/3 의 "3"을 꺼낸다. (App.jsx의 path="/notice/:id"와 짝)
 */
export default function NoticeDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { role } = useAuth()

  const [notice, setNotice] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getNotice(id)
      .then(setNotice)
      .catch(() => setNotice(null))   // 없는 글이면 서버가 404
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return <main className="page"><div className="empty">불러오는 중...</div></main>
  }

  if (!notice) {
    return (
      <main className="page">
        <div className="empty">
          없는 글입니다. <Link to="/notice" style={{ color: '#17c964' }}>목록으로</Link>
        </div>
      </main>
    )
  }

  const onDelete = async () => {
    if (!confirm('이 글을 삭제할까요?')) return
    try {
      await removeNotice(id)
      navigate('/notice')
    } catch {
      alert('삭제에 실패했습니다.')
    }
  }

  return (
    <main className="page">
      <div className="post">
        <h2 className="post-title">{notice.title}</h2>
        <div className="post-meta">
          <UserTag username={notice.writer} /> · {notice.date}
        </div>
        <div className="post-content">{notice.content}</div>
      </div>

      <div className="post-buttons">
        <Link className="auth-btn" to="/notice">목록</Link>
        {role === 'ADMIN' && (
          <>
            <Link className="auth-btn" to={`/notice/${id}/edit`}>수정</Link>
            <button className="auth-btn" onClick={onDelete}>삭제</button>
          </>
        )}
      </div>

      <CommentBox noticeId={notice.id} />
    </main>
  )
}
