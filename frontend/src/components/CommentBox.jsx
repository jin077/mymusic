import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getComments, addComment, removeComment } from '../storage'
import UserTag from './UserTag'

/**
 * 공지 아래 붙는 댓글(문의) 영역.
 *
 * 읽기는 누구나, 쓰기는 로그인한 회원만.
 * 삭제 버튼은 본인 글과 관리자에게만 보이는데, 이건 화면 편의일 뿐이고
 * 실제 차단은 서버가 작성자를 확인해서 한다(403).
 */
export default function CommentBox({ noticeId }) {
  const { isLoggedIn, username, role } = useAuth()
  const [comments, setComments] = useState([])
  const [content, setContent] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    getComments(noticeId).then(setComments).catch(() => setComments([]))
  }, [noticeId])

  const onSubmit = async (e) => {
    e.preventDefault()
    if (!content.trim()) return
    try {
      const saved = await addComment(noticeId, content.trim())
      setComments((prev) => [...prev, saved])   // 목록 맨 뒤에 붙인다(대화 순서)
      setContent('')
      setMessage('')
    } catch {
      setMessage('댓글 등록에 실패했습니다.')
    }
  }

  const onDelete = async (id) => {
    if (!confirm('댓글을 삭제할까요?')) return
    try {
      await removeComment(id)
      setComments((prev) => prev.filter((c) => c.id !== id))
    } catch (err) {
      setMessage(err.response?.status === 403
        ? '본인이 쓴 댓글만 삭제할 수 있습니다.'
        : '삭제에 실패했습니다.')
    }
  }

  const canDelete = (c) => role === 'ADMIN' || c.writer === username

  return (
    <section className="comment-box">
      <div className="section-title">
        <span>문의 · 댓글 ({comments.length})</span>
      </div>

      {comments.length === 0 ? (
        <div className="empty">첫 댓글을 남겨 보세요.</div>
      ) : (
        <div className="comment-list">
          {comments.map((c) => (
            <div className="comment-row" key={c.id}>
              <div className="comment-head">
                <UserTag username={c.writer} />
                <span className="comment-date">{c.date}</span>
                {canDelete(c) && (
                  <button className="link-btn comment-del" onClick={() => onDelete(c.id)}>삭제</button>
                )}
              </div>
              <div className="comment-content">{c.content}</div>
            </div>
          ))}
        </div>
      )}

      {isLoggedIn ? (
        <form className="comment-form" onSubmit={onSubmit}>
          <textarea
            rows={3}
            placeholder="문의나 의견을 남겨 주세요."
            value={content}
            onChange={(e) => setContent(e.target.value)}
          />
          <button type="submit" className="auth-btn primary">등록</button>
        </form>
      ) : (
        <div className="empty">
          댓글을 쓰려면 <Link to="/login" style={{ color: '#17c964' }}>로그인</Link>이 필요합니다.
        </div>
      )}

      {message && <p className="msg">{message}</p>}
    </section>
  )
}
