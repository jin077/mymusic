import { useEffect, useState } from 'react'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { addNotice, getNotice, updateNotice } from '../storage'

/**
 * 공지 작성 / 수정 (관리자 전용).
 *
 * 한 컴포넌트가 두 주소를 담당한다.
 *   /notice/write      → 새 글
 *   /notice/3/edit     → 3번 글 수정
 * 입력 폼이 완전히 같아서 파일을 나누면 코드가 중복된다.
 * (Login.jsx가 로그인·회원가입을 함께 담당하는 것과 같은 방식)
 *
 * 관리자가 아니면 화면 자체를 보여주지 않는다.
 * 다만 이것은 화면 차단일 뿐이고, 실제 차단은 SecurityConfig가 한다.
 */
export default function NoticeWrite() {
  const { role, username } = useAuth()
  const { id } = useParams()          // 수정일 때만 값이 있다
  const isEdit = Boolean(id)

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [message, setMessage] = useState('')
  const navigate = useNavigate()

  // 수정 모드면 기존 글을 불러와 입력칸을 채운다
  useEffect(() => {
    if (!isEdit) return
    getNotice(id)
      .then((n) => { setTitle(n.title); setContent(n.content) })
      .catch(() => setMessage('글을 불러오지 못했습니다.'))
  }, [id, isEdit])

  if (role !== 'ADMIN') {
    return (
      <main className="page">
        <h2 className="page-title">{isEdit ? '공지 수정' : '글쓰기'}</h2>
        <div className="empty">
          관리자만 글을 쓸 수 있습니다. <Link to="/notice" style={{ color: '#17c964' }}>목록으로</Link>
        </div>
      </main>
    )
  }

  // 작성자(writer)는 보내지 않는다. 서버가 토큰에서 꺼내 채운다.
  const onSubmit = async (e) => {
    e.preventDefault()
    if (!title.trim() || !content.trim()) return
    const body = { title: title.trim(), content: content.trim() }

    try {
      const saved = isEdit ? await updateNotice(id, body) : await addNotice(body)
      navigate(`/notice/${saved.id}`)
    } catch {
      setMessage(isEdit ? '수정에 실패했습니다.' : '등록에 실패했습니다.')
    }
  }

  return (
    <main className="page">
      <h2 className="page-title">{isEdit ? '공지 수정' : '공지 작성'}</h2>

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
          <Link className="auth-btn" to={isEdit ? `/notice/${id}` : '/notice'}>취소</Link>
          <button type="submit" className="auth-btn primary">{isEdit ? '수정' : '등록'}</button>
        </div>
      </form>

      {message && <p className="msg">{message}</p>}
    </main>
  )
}
