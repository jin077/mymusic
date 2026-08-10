import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { addNotice } from '../storage'

/**
 * 공지사항 글쓰기 (관리자 전용).
 *
 * 관리자가 아니면 화면 자체를 보여주지 않는다.
 * 다만 이것은 화면 차단일 뿐이므로, 백엔드에 API를 만들 때
 * 서버에서도 ADMIN만 쓰기가 되도록 막아야 한다.
 */
export default function NoticeWrite() {
  const { role, username } = useAuth()
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const navigate = useNavigate()

  if (role !== 'ADMIN') {
    return (
      <main className="page">
        <h2 className="page-title">글쓰기</h2>
        <div className="empty">
          관리자만 글을 쓸 수 있습니다. <Link to="/notice" style={{ color: '#17c964' }}>목록으로</Link>
        </div>
      </main>
    )
  }

  const onSubmit = (e) => {
    e.preventDefault()
    if (!title.trim() || !content.trim()) return
    const saved = addNotice({ title: title.trim(), content: content.trim(), writer: username })
    navigate(`/notice/${saved.id}`)
  }

  return (
    <main className="page">
      <h2 className="page-title">공지 작성</h2>

      <form className="write-form" onSubmit={onSubmit}>
        <input
          placeholder="제목"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          placeholder="내용"
          rows={12}
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <div className="post-buttons">
          <Link className="auth-btn" to="/notice">취소</Link>
          <button type="submit" className="auth-btn primary">등록</button>
        </div>
      </form>
    </main>
  )
}
