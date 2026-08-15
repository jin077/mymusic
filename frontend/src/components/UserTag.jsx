import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import MessageModal from './MessageModal'

/**
 * 아이디 표시 + 마우스를 올리면 나오는 작은 메뉴(쪽지 보내기).
 *
 * 공지 작성자·댓글 작성자처럼 "아이디가 보이는 자리"에 공통으로 쓴다.
 *   → 자리마다 같은 코드를 복사하지 않으려고 컴포넌트로 뺐다. (TrackList와 같은 이유)
 *
 * 쪽지 버튼이 안 보이는 경우
 *   - 로그인하지 않았을 때 → 대신 [로그인] 버튼을 보여준다
 *   - 자기 자신일 때 (자기에게 보낼 이유가 없고, 서버도 400으로 막는다)
 */
export default function UserTag({ username }) {
  const { isLoggedIn, username: me } = useAuth()
  const [open, setOpen] = useState(false)
  const navigate = useNavigate()

  const canSend = isLoggedIn && username !== me

  return (
    <span className="user-tag">
      <span className="user-name">{username}</span>

      {/* 평소엔 숨어 있고, 아이디에 마우스를 올리면 나타난다 (CSS로 처리) */}
      {canSend && (
        <span className="user-menu">
          <button type="button" onClick={() => setOpen(true)}>쪽지</button>
        </span>
      )}
      {!isLoggedIn && (
        <span className="user-menu">
          <button type="button" onClick={() => navigate('/login')}>로그인</button>
        </span>
      )}

      {open && <MessageModal to={username} onClose={() => setOpen(false)} />}
    </span>
  )
}
