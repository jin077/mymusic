import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth/AuthContext'
import MessageModal from '../components/MessageModal'

/**
 * 관리자 페이지 — 전체 회원 목록 조회 / 삭제.
 *
 * ⭐ 화면에서 메뉴를 숨기는 것은 '편의'일 뿐 '보안'이 아니다.
 *    프론트 코드는 브라우저에 그대로 내려가므로 누구나 열어볼 수 있고,
 *    주소창에 /admin을 직접 쳐서 들어올 수도 있다.
 *    실제 차단은 백엔드가 한다 — SecurityConfig 의 /api/admin/** hasRole("ADMIN").
 *    그래서 관리자가 아닌 사람이 이 화면에 들어와도 API가 403을 돌려준다.
 */
export default function Admin() {
  const { isLoggedIn, role } = useAuth()
  const [members, setMembers] = useState([])
  const [message, setMessage] = useState('')
  const [msgTo, setMsgTo] = useState(null)   // 쪽지 보낼 상대 아이디

  useEffect(() => {
    if (isLoggedIn) load()
  }, [isLoggedIn])

  const load = async () => {
    try {
      const res = await api.get('/admin/members')
      setMembers(res.data)
      setMessage('')
    } catch (err) {
      const status = err.response?.status
      setMessage(
        status === 401 ? '인증 필요(401): 로그인이 필요합니다'
          : status === 403 ? '권한 없음(403): 관리자만 조회할 수 있습니다'
            : '조회 실패'
      )
    }
  }

  const onDelete = async (m) => {
    if (!confirm(`${m.username} 회원을 삭제할까요?`)) return
    try {
      await api.delete(`/admin/members/${m.id}`)
      setMembers((list) => list.filter((x) => x.id !== m.id))
    } catch (err) {
      setMessage(err.response?.status === 404 ? '이미 삭제된 회원입니다(404)' : '삭제 실패')
    }
  }

  /** 권한 변경 — USER ↔ ADMIN 전환 */
  const onChangeRole = async (m) => {
    const next = m.role === 'ADMIN' ? 'USER' : 'ADMIN'
    if (!confirm(`${m.username}의 권한을 ${next}로 바꿀까요?`)) return
    try {
      const res = await api.put(`/admin/members/${m.id}`, { role: next })
      setMembers((list) => list.map((x) => (x.id === m.id ? res.data : x)))
    } catch {
      setMessage('권한 변경 실패')
    }
  }

  if (!isLoggedIn) {
    return (
      <main className="page">
        <h2 className="page-title">관리자</h2>
        <div className="empty">
          로그인이 필요합니다. <Link to="/login" style={{ color: '#17c964' }}>로그인하기</Link>
        </div>
      </main>
    )
  }

  return (
    <main className="page">
      <h2 className="page-title">관리자 · 회원 관리</h2>
      <p className="page-desc">
        전체 회원을 조회하고 삭제할 수 있습니다. (현재 권한 : {role})
      </p>

      <div className="table">
        <div className="table-head">
          <span className="col-id">번호</span>
          <span className="col-name">아이디</span>
          <span className="col-name">닉네임</span>
          <span className="col-role">권한</span>
          <span className="col-act">관리</span>
        </div>

        {members.length === 0 ? (
          <div className="empty">표시할 회원이 없습니다.</div>
        ) : (
          members.map((m) => (
            <div className="table-row" key={m.id}>
              <span className="col-id">{m.id}</span>
              <span className="col-name"><b>{m.username}</b></span>
              <span className="col-name">{m.nickname || '-'}</span>
              <span className="col-role">
                <span className={`role-tag${m.role === 'ADMIN' ? ' admin' : ''}`}>{m.role}</span>
              </span>
              <span className="col-act">
                <div className="track-actions">
                  <button onClick={() => setMsgTo(m.username)}>쪽지</button>
                  <button onClick={() => onChangeRole(m)}>
                    {m.role === 'ADMIN' ? 'USER로' : 'ADMIN으로'}
                  </button>
                  <button onClick={() => onDelete(m)}>삭제</button>
                </div>
              </span>
            </div>
          ))
        )}
      </div>

      {/* 쪽지 쓰기 창 — 공지·댓글에서 쓰는 것과 같은 컴포넌트 */}
      {msgTo && <MessageModal to={msgTo} onClose={() => setMsgTo(null)} />}

      {message && <p className="msg">{message}</p>}
    </main>
  )
}
