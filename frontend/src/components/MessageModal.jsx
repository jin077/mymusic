import { useState } from 'react'
import { sendMessage } from '../storage'

/**
 * 쪽지 쓰기 창.
 *
 * 두 곳에서 쓴다.
 *   UserTag  : 공지·댓글의 아이디에 마우스를 올려 [쪽지]를 눌렀을 때
 *   MyPage   : 받은 쪽지에서 [답장]을 눌렀을 때
 * → 같은 창을 두 번 만들지 않으려고 컴포넌트로 분리했다.
 *
 * props
 *   to      : 받는 사람 아이디
 *   subject : 제목 초기값 (답장이면 "RE: 원래 제목")
 *   onClose : 닫을 때 부를 함수
 *   onSent  : 보내고 나서 부를 함수 (목록 갱신 등)
 */
export default function MessageModal({ to, subject = '', onClose, onSent }) {
  const [form, setForm] = useState({ title: subject, content: '' })
  const [message, setMessage] = useState('')
  const [sending, setSending] = useState(false)

  const onSubmit = async (e) => {
    e.preventDefault()
    if (!form.title.trim() || !form.content.trim()) {
      setMessage('제목과 내용을 입력해 주세요.')
      return
    }

    // 보내는 동안 버튼을 잠근다 — 두 번 눌러 같은 쪽지가 두 통 가는 것을 막는다
    setSending(true)
    try {
      await sendMessage({
        receiver: to,
        title: form.title.trim(),
        content: form.content.trim(),
      })
      alert(`${to}님에게 쪽지를 보냈습니다.`)
      onSent?.()
      onClose()
    } catch (err) {
      setMessage(err.response?.data?.message ?? '보내기에 실패했습니다.')
    } finally {
      setSending(false)
    }
  }

  return (
    /* 화면 전체를 덮는 어두운 배경. 바깥을 누르면 닫힌다. */
    <div className="modal-back" onClick={onClose}>
      {/* stopPropagation : 창 안을 눌렀을 때 배경의 '닫기'가 실행되지 않게 막는다 */}
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={onSubmit}>
        <h3 className="modal-title">{to}님에게 쪽지</h3>

        <input
          placeholder="제목"
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          autoFocus
        />
        <textarea
          rows={5}
          placeholder="내용"
          value={form.content}
          onChange={(e) => setForm({ ...form, content: e.target.value })}
        />

        {message && <p className="msg">{message}</p>}

        <div className="post-buttons">
          <button type="button" className="auth-btn" onClick={onClose}>취소</button>
          <button type="submit" className="auth-btn primary" disabled={sending}>
            {sending ? '보내는 중...' : '보내기'}
          </button>
        </div>
      </form>
    </div>
  )
}
