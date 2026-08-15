import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api'
import { useAuth } from '../auth/AuthContext'
import { usePlayer } from '../player/PlayerContext'
import {
  getPlaylist, removeFromPlaylist, moveTrackToFolder,
  getFolders, createFolder, renameFolder, deleteFolder,
  getRecentPlays, getTopPlays, clearPlayHistory,
  getMyComments, removeComment,
  getPurchases, chargeCash, getTrackPrice,
  getTicketPurchases,
  getMessages, readMessage, removeMessage,
} from '../storage'
import Avatar from '../components/Avatar'
import MessageModal from '../components/MessageModal'

/**
 * 마이페이지 = 내 정보 + 왼쪽 메뉴로 고르는 내 활동 목록.
 *
 * ⭐ 내 정보를 토큰이 아니라 서버(GET /api/members/me)에서 가져오는 이유
 *   토큰에는 아이디와 권한만 들어 있다. 닉네임·이메일·사진은 없다.
 *   토큰에 다 담으면 정보를 고쳐도 다시 로그인하기 전까지 옛 값이 보인다.
 *   → 자주 바뀌는 정보는 토큰이 아니라 서버에서 그때그때 조회한다.
 *
 * ⭐ 왼쪽 메뉴는 페이지 이동이 아니라 "이 화면 안에서 내용만 교체"한다.
 *   주소를 나누지 않은 이유는 내 정보 카드가 항상 위에 남아야 하기 때문이다.
 *   (주소로 나누고 싶다면 Layout의 Outlet처럼 중첩 라우트로 만들면 된다)
 */

/** 왼쪽 목차 구성. heading은 누를 수 없는 큰 제목 줄이다. */
const MENU = [
  { key: 'edit', label: '회원수정' },
  { key: 'favorites', label: '즐겨찾기' },
  { key: 'messages', label: '쪽지함' },
  { key: 'purchases', label: '구매내역' },
  { key: 'event', label: '이벤트', to: '/event' },
  { heading: '활동내역' },
  { key: 'played', label: '내가 재생한 곡' },
  { key: 'most', label: '내가 가장 많이 듣던 곡' },
  { key: 'comments', label: '내가 쓴 댓글' },
]

/**
 * 활동내역 한 줄 — 순위 · 앨범 이미지 · 곡/가수/앨범 · (재생 횟수) · 듣기
 *
 * 담기/빼기가 없어서 TrackList를 그대로 쓰지 않고 따로 만들었다.
 * 최근 재생과 많이 들은 곡이 같은 모양이라 둘이 공유한다.
 */
function HistoryRow({ track, no, onPlay, count }) {
  return (
    <div className="track-row">
      <div className="track-rank">{no}</div>
      {track.albumImage
        ? <img className="thumb" src={track.albumImage} alt="" />
        : <div className="thumb" />}
      <div className="track-info">
        <div className="track-title">{track.title}</div>
        <div className="track-artist">{track.artist} · {track.album}</div>
      </div>
      {count != null && <span className="play-count">{count}회</span>}
      <div className="track-actions">
        <button onClick={() => onPlay(track)}>듣기</button>
      </div>
    </div>
  )
}

export default function MyPage() {
  const { isLoggedIn, username, refreshProfile, logout } = useAuth()
  const { playTrack } = usePlayer()
  const navigate = useNavigate()

  const [me, setMe] = useState(null)
  const [list, setList] = useState([])
  const [folders, setFolders] = useState([])   // 즐겨찾기 폴더
  const [folderId, setFolderId] = useState(null)  // null=전체, 0=미분류, 숫자=폴더
  const [recent, setRecent] = useState([])     // 최근 재생한 곡
  const [top, setTop] = useState([])           // 많이 들은 곡
  const [myComments, setMyComments] = useState([])  // 내가 쓴 댓글
  const [purchases, setPurchases] = useState([])    // 구매내역
  const [price, setPrice] = useState(null)          // 곡 한 곡 값 (서버가 정함)
  const [ticketBuys, setTicketBuys] = useState([])  // 이용권 구매내역
  const [messages, setMessages] = useState([])      // 받은 쪽지
  const [openMsg, setOpenMsg] = useState(null)      // 펼쳐서 보고 있는 쪽지 id
  const [replyTo, setReplyTo] = useState(null)      // 답장 쓰는 중인 쪽지
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [menu, setMenu] = useState('favorites')   // 지금 고른 메뉴
  const [form, setForm] = useState({ nickname: '', email: '' })
  const [message, setMessage] = useState('')
  const fileRef = useRef(null)   // 숨겨둔 파일 선택창을 코드로 열기 위한 손잡이

  useEffect(() => {
    if (!isLoggedIn) return
    api.get('/members/me').then((res) => {
      setMe(res.data)
      setForm({ nickname: res.data.nickname ?? '', email: res.data.email ?? '' })
    })
    getFolders().then(setFolders).catch(() => setFolders([]))
  }, [isLoggedIn])

  // 고른 폴더가 바뀌면 그 폴더의 곡만 다시 불러온다
  useEffect(() => {
    if (!isLoggedIn) return
    getPlaylist(folderId).then(setList).catch(() => setList([]))
  }, [folderId, isLoggedIn])

  // 활동내역은 그 메뉴를 골랐을 때만 불러온다(필요할 때만 요청).
  useEffect(() => {
    if (!isLoggedIn) return
    if (menu === 'played') getRecentPlays().then(setRecent).catch(() => setRecent([]))
    if (menu === 'most') getTopPlays().then(setTop).catch(() => setTop([]))
    if (menu === 'comments') getMyComments().then(setMyComments).catch(() => setMyComments([]))
    if (menu === 'purchases') {
      getPurchases().then(setPurchases).catch(() => setPurchases([]))
      getTicketPurchases().then(setTicketBuys).catch(() => setTicketBuys([]))
      getTrackPrice().then(setPrice).catch(() => {})
    }
    if (menu === 'messages') getMessages().then(setMessages).catch(() => setMessages([]))
  }, [menu, isLoggedIn])

  if (!isLoggedIn) {
    return (
      <main className="page">
        <h2 className="page-title">마이페이지</h2>
        <div className="empty">
          로그인이 필요합니다. <Link to="/login" style={{ color: '#17c964' }}>로그인하기</Link>
        </div>
      </main>
    )
  }

  const onRemove = async (id) => {
    await removeFromPlaylist(id)
    setList((prev) => prev.filter((t) => t.id !== id))
    refreshFolders()
  }

  // ───────── 즐겨찾기 폴더 ─────────

  const refreshFolders = () => getFolders().then(setFolders).catch(() => {})

  const onCreateFolder = async () => {
    const name = prompt('새 폴더 이름을 입력하세요.')
    if (!name?.trim()) return
    try {
      await createFolder(name.trim())
      refreshFolders()
    } catch {
      setMessage('폴더를 만들지 못했습니다.')
    }
  }

  const onRenameFolder = async (f) => {
    const name = prompt('폴더 이름을 수정하세요.', f.name)
    if (!name?.trim() || name.trim() === f.name) return
    try {
      await renameFolder(f.id, name.trim())
      refreshFolders()
    } catch {
      setMessage('이름을 바꾸지 못했습니다.')
    }
  }

  const onDeleteFolder = async (f) => {
    if (!confirm(`'${f.name}' 폴더를 삭제할까요?\n담긴 곡은 미분류로 이동합니다.`)) return
    try {
      await deleteFolder(f.id)
      if (folderId === f.id) setFolderId(null)   // 보고 있던 폴더가 사라지면 전체로
      refreshFolders()
      getPlaylist(folderId === f.id ? null : folderId).then(setList)
    } catch {
      setMessage('폴더를 삭제하지 못했습니다.')
    }
  }

  /** 곡을 다른 폴더로 옮기기 */
  const onMoveTrack = async (trackId, value) => {
    const target = value === '' ? null : Number(value)
    try {
      await moveTrackToFolder(trackId, target)
      // 특정 폴더를 보고 있었다면 옮긴 곡은 목록에서 빠져야 한다
      if (folderId != null) {
        setList((prev) => prev.filter((t) => t.id !== trackId))
      } else {
        setList((prev) => prev.map((t) => (t.id === trackId ? { ...t, folderId: target } : t)))
      }
      refreshFolders()
    } catch {
      setMessage('폴더를 옮기지 못했습니다.')
    }
  }

  /**
   * 비밀번호 변경.
   *
   * 새 비밀번호 확인은 화면에서만 검사한다(서버에는 두 번 보낼 필요가 없다).
   * 현재 비밀번호가 맞는지는 서버가 판단한다 → 틀리면 403.
   */
  const onChangePassword = async (e) => {
    e.preventDefault()
    if (pw.newPassword !== pw.confirm) {
      setMessage('새 비밀번호가 서로 다릅니다.')
      return
    }
    try {
      await api.put('/members/me/password', {
        currentPassword: pw.currentPassword,
        newPassword: pw.newPassword,
      })
      setPw({ currentPassword: '', newPassword: '', confirm: '' })
      setMessage('비밀번호가 변경되었습니다.')
    } catch (err) {
      const status = err.response?.status
      setMessage(
        status === 403 ? '현재 비밀번호가 일치하지 않습니다.'
          : status === 400 ? (err.response?.data?.message ?? '입력을 확인해 주세요.')
            : '변경에 실패했습니다.'
      )
    }
  }

  /** 캐시 충전 — 실제 결제 없이 잔액만 늘린다(학습용) */
  const onCharge = async () => {
    const input = prompt('충전할 금액을 입력하세요. (원)', '1000')
    if (!input) return
    const amount = Number(input)
    if (!Number.isFinite(amount) || amount <= 0) {
      setMessage('금액을 올바르게 입력해 주세요.')
      return
    }
    try {
      const updated = await chargeCash(amount)
      setMe(updated)
      setMessage(`${amount.toLocaleString()}원 충전되었습니다.`)
      refreshProfile()
    } catch {
      setMessage('충전에 실패했습니다.')
    }
  }

  /** 쪽지 펼치기 — 처음 여는 것이면 읽음으로 표시한다 */
  const onOpenMessage = async (m) => {
    setOpenMsg(openMsg === m.id ? null : m.id)
    if (!m.read) {
      await readMessage(m.id)
      setMessages((prev) => prev.map((x) => (x.id === m.id ? { ...x, read: true } : x)))
      refreshProfile()   // 홈의 "쪽지 N" 갱신
    }
  }

  const onDeleteMessage = async (id) => {
    if (!confirm('쪽지를 삭제할까요?')) return
    await removeMessage(id)
    setMessages((prev) => prev.filter((m) => m.id !== id))
    refreshProfile()
  }

  const onDeleteMyComment = async (id) => {
    if (!confirm('댓글을 삭제할까요?')) return
    await removeComment(id)
    setMyComments((prev) => prev.filter((c) => c.id !== id))
  }

  const onSave = async (e) => {
    e.preventDefault()
    try {
      const res = await api.put('/members/me', form)
      setMe(res.data)
      setMessage('저장되었습니다.')
      refreshProfile()          // 헤더의 이름도 함께 갱신
    } catch {
      setMessage('저장에 실패했습니다.')
    }
  }

  /**
   * 프로필 사진 업로드.
   *
   * ⭐ JSON이 아니라 FormData로 보낸다.
   *   이미지는 글자가 아니라 이진 데이터라 JSON에 그대로 담을 수 없다.
   *   FormData로 보내면 axios가 multipart/form-data 형식과 경계 문자열을
   *   알아서 붙여준다. (Content-Type을 직접 지정하면 오히려 깨진다)
   */
  const onPickImage = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    const formData = new FormData()
    formData.append('file', file)        // 백엔드의 @RequestParam("file")과 이름이 같아야 한다

    try {
      const res = await api.post('/members/me/profile-image', formData)
      setMe(res.data)
      setMessage('사진이 변경되었습니다.')
      refreshProfile()                   // 헤더·홈의 동그라미도 함께 갱신
    } catch (err) {
      const status = err.response?.status
      setMessage(
        status === 413 ? '파일이 너무 큽니다 (2MB 이하)'
          : status === 400 ? (err.response?.data?.message ?? '이미지 파일만 올릴 수 있습니다')
            : '업로드에 실패했습니다.'
      )
    } finally {
      e.target.value = ''   // 같은 파일을 다시 골라도 onChange가 뜨게 초기화
    }
  }

  const onRemoveImage = async () => {
    if (!confirm('프로필 사진을 삭제할까요?')) return
    const res = await api.delete('/members/me/profile-image')
    setMe(res.data)
    setMessage('사진이 삭제되었습니다.')
    refreshProfile()
  }

  const onMenu = (key) => setMenu(key)

  const onClearHistory = async () => {
    if (!confirm('재생 기록을 모두 삭제할까요?')) return
    await clearPlayHistory()
    setRecent([])
    setTop([])
  }

  /** 회원 탈퇴 — 되돌릴 수 없으므로 두 번 확인한다 */
  const onWithdraw = async () => {
    if (!confirm('정말 탈퇴하시겠습니까?\n담아둔 곡과 프로필 사진이 모두 삭제됩니다.')) return
    if (!confirm('되돌릴 수 없습니다. 그래도 탈퇴할까요?')) return
    try {
      await api.delete('/members/me')
      logout()          // 토큰을 지우고 로그인 상태 해제
      navigate('/')
    } catch {
      setMessage('탈퇴에 실패했습니다.')
    }
  }

  return (
    <main className="page mypage">
      {/* ── 왼쪽 목차 ── */}
      <aside className="mypage-side">
        <div className="mypage-side-inner">
          <nav className="side-menu">
            {MENU.map((m) =>
              m.heading ? (
                <div className="side-menu-title" key={m.heading}>{m.heading}</div>
              ) : m.to ? (
                <Link className="side-menu-item" key={m.key} to={m.to}>{m.label}</Link>
              ) : (
                <button
                  key={m.key}
                  className={`side-menu-item${menu === m.key ? ' on' : ''}`}
                  onClick={() => onMenu(m.key)}
                >
                  {m.label}
                </button>
              )
            )}
          </nav>
        </div>
      </aside>

      {/* ── 오른쪽 내용 ── */}
      <div className="mypage-main">
        <h2 className="page-title">마이페이지</h2>

        {/* 내 정보 카드는 어느 메뉴에서도 항상 위에 있다 */}
        <div className="profile-card">
          {/* 왼쪽 : 사진 + 변경/삭제 */}
          <div className="profile-photo">
            <div className="avatar-picker" onClick={() => fileRef.current?.click()} title="사진 변경">
              <Avatar src={me?.profileImage} name={username} size="xl" />
            </div>
            <input
              type="file"
              accept="image/*"
              ref={fileRef}
              onChange={onPickImage}
              style={{ display: 'none' }}
            />
            <div className="photo-buttons">
              <button className="link-btn" onClick={() => fileRef.current?.click()}>변경</button>
              {me?.profileImage && (
                <>
                  <span className="photo-sep">|</span>
                  <button className="link-btn" onClick={onRemoveImage}>삭제</button>
                </>
              )}
            </div>
          </div>

          {/* 오른쪽 : 인사말 + 정보 */}
          {/* 카드는 언제나 '보기' 전용. 수정은 왼쪽 [회원수정] 메뉴에서 한다. */}
          <div className="profile-info">
            <div className="profile-name">
              {me?.nickname || username} 님 환영합니다
              <span className={`role-tag${me?.role === 'ADMIN' ? ' admin' : ''}`}>{me?.role}</span>
            </div>

            <div className="profile-sub">
              성인 인증 :{' '}
              <b className={me?.adultVerified ? 'ok' : ''}>
                {me?.adultVerified ? '인증 완료' : '미인증'}
              </b>
            </div>
            <div className="profile-sub">이메일 : {me?.email || '미설정'}</div>

            <div className="profile-sub">
              이용권 :{' '}
              {me?.ticketDaysLeft != null
                ? <b className="ok">{me.ticketName} · {me.ticketDaysLeft}일 남음</b>
                : <Link to="/ticket" className="none-link">없음</Link>}
            </div>

            <div className="profile-sub">
              보유 캐시 : <b className="ok">{(me?.balance ?? 0).toLocaleString()}원</b>
              <button className="link-btn charge-link" onClick={onCharge}>충전</button>
            </div>

            <div className="profile-bottom">
              <span>가입일 : {me?.createdAt ?? '-'}</span>
              <span>
                <button className="link-btn charge-link" onClick={() => onMenu('edit')}>정보 수정</button>
                <span className="photo-sep">|</span>
                <button className="link-btn withdraw" onClick={onWithdraw}>회원 탈퇴</button>
              </span>
            </div>
          </div>
        </div>
        {message && <p className="msg" style={{ color: '#17c964' }}>{message}</p>}

        {/* 메뉴에 따라 바뀌는 부분 */}
        {menu === 'favorites' && (
          <>
            <div className="section-title">
              <span>즐겨찾기 ({list.length}곡)</span>
              <Link to="/chart" className="more">차트에서 담기 &gt;</Link>
            </div>

            {/* 폴더 탭 : 전체 · 미분류 · 내가 만든 폴더들 */}
            <div className="folder-tabs">
              <button
                className={`folder-tab${folderId === null ? ' on' : ''}`}
                onClick={() => setFolderId(null)}
              >
                전체
              </button>
              <button
                className={`folder-tab${folderId === 0 ? ' on' : ''}`}
                onClick={() => setFolderId(0)}
              >
                미분류
              </button>

              {folders.map((f) => (
                <span className="folder-chip" key={f.id}>
                  <button
                    className={`folder-tab${folderId === f.id ? ' on' : ''}`}
                    onClick={() => setFolderId(f.id)}
                  >
                    {f.name} ({f.count})
                  </button>
                  <button className="folder-edit" onClick={() => onRenameFolder(f)} title="이름 수정">✎</button>
                  <button className="folder-edit" onClick={() => onDeleteFolder(f)} title="폴더 삭제">✕</button>
                </span>
              ))}

              <button className="folder-add" onClick={onCreateFolder}>+ 폴더 추가</button>
            </div>

            {list.length === 0 ? (
              <div className="empty">담은 곡이 없습니다. 차트에서 [담기]를 눌러 보세요.</div>
            ) : (
              <div className="track-list">
                {list.map((t, i) => (
                  <div className="track-row" key={t.id}>
                    <div className="track-rank">{i + 1}</div>
                    {t.albumImage
                      ? <img className="thumb" src={t.albumImage} alt="" />
                      : <div className="thumb" />}
                    <div className="track-info">
                      <div className="track-title">{t.title}</div>
                      <div className="track-artist">{t.artist} · {t.album}</div>
                    </div>

                    {/* 이 곡이 들어갈 폴더 고르기 */}
                    <select
                      className="folder-select"
                      value={t.folderId ?? ''}
                      onChange={(e) => onMoveTrack(t.id, e.target.value)}
                    >
                      <option value="">미분류</option>
                      {folders.map((f) => (
                        <option key={f.id} value={f.id}>{f.name}</option>
                      ))}
                    </select>

                    <div className="track-actions">
                      <button onClick={() => playTrack(t)}>듣기</button>
                      <button onClick={() => onRemove(t.id)}>빼기</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {menu === 'edit' && (
          <>
            <div className="section-title"><span>회원수정</span></div>

            <div className="edit-grid">
              {/* 기본 정보 */}
              <form className="edit-card" onSubmit={onSave}>
                <h3 className="edit-title">기본 정보</h3>

                <label className="edit-label">아이디</label>
                <input value={username} disabled />
                <p className="edit-hint">아이디는 변경할 수 없습니다.</p>

                <label className="edit-label">닉네임</label>
                <input
                  placeholder="화면에 표시될 이름"
                  value={form.nickname}
                  onChange={(e) => setForm({ ...form, nickname: e.target.value })}
                />

                <label className="edit-label">이메일</label>
                <input
                  placeholder="example@mail.com"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />

                <button type="submit" className="auth-btn primary">저장</button>
              </form>

              {/* 비밀번호 변경 */}
              <form className="edit-card" onSubmit={onChangePassword}>
                <h3 className="edit-title">비밀번호 변경</h3>

                <label className="edit-label">현재 비밀번호</label>
                <input
                  type="password"
                  value={pw.currentPassword}
                  onChange={(e) => setPw({ ...pw, currentPassword: e.target.value })}
                />
                <p className="edit-hint">본인 확인을 위해 현재 비밀번호를 함께 입력합니다.</p>

                <label className="edit-label">새 비밀번호</label>
                <input
                  type="password"
                  placeholder="4자 이상"
                  value={pw.newPassword}
                  onChange={(e) => setPw({ ...pw, newPassword: e.target.value })}
                />

                <label className="edit-label">새 비밀번호 확인</label>
                <input
                  type="password"
                  value={pw.confirm}
                  onChange={(e) => setPw({ ...pw, confirm: e.target.value })}
                />

                <button type="submit" className="auth-btn primary">변경</button>
              </form>
            </div>
          </>
        )}

        {/* 아래 세 가지는 아직 저장하는 곳이 없다.
            재생 기록·댓글 테이블이 생기면 이 자리에 목록을 그리면 된다. */}
        {menu === 'messages' && (
          <>
            <div className="section-title">
              <span>쪽지함 ({messages.filter((m) => !m.read).length}통 안 읽음)</span>
            </div>
            {/* 답장 창 — 받는 사람은 보낸 사람, 제목은 "RE: 원래 제목"으로 채워둔다 */}
            {replyTo && (
              <MessageModal
                to={replyTo.sender}
                subject={replyTo.title.startsWith('RE: ') ? replyTo.title : `RE: ${replyTo.title}`}
                onClose={() => setReplyTo(null)}
              />
            )}

            {messages.length === 0 ? (
              <div className="empty">받은 쪽지가 없습니다.</div>
            ) : (
              <div className="table">
                {messages.map((m) => (
                  <div key={m.id}>
                    <div
                      className={`table-row msg-row${m.read ? '' : ' unread'}`}
                      onClick={() => onOpenMessage(m)}
                    >
                      <span className="col-title">
                        {!m.read && <span className="msg-dot" />}
                        {m.title}
                      </span>
                      <span className="col-name">{m.sender}</span>
                      <span className="col-date">{m.date}</span>
                    </div>
                    {openMsg === m.id && (
                      <div className="msg-body">
                        {m.content}
                        <div className="post-buttons">
                          {/* 보낸 사람이 나 자신인 경우는 없지만, 서버가 막으므로 화면도 맞춰둔다 */}
                          {m.sender !== username && (
                            <button className="link-btn" onClick={() => setReplyTo(m)}>답장</button>
                          )}
                          <button className="link-btn" onClick={() => onDeleteMessage(m.id)}>삭제</button>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {menu === 'purchases' && (
          <>
            <div className="section-title">
              <span>구매내역</span>
              <button className="link-btn" onClick={onCharge}>캐시 충전</button>
            </div>

            <div className="cash-box">
              보유 캐시 <b>{(me?.balance ?? 0).toLocaleString()}원</b>
              {price != null && (
                <span className="cash-note">곡 다운로드 1곡당 {price.toLocaleString()}원</span>
              )}
            </div>

            {/* 이용권 구매내역 */}
            <div className="sub-section-title">이용권 ({ticketBuys.length})</div>
            {ticketBuys.length === 0 ? (
              <div className="empty">
                구매한 이용권이 없습니다. <Link to="/ticket" style={{ color: '#17c964' }}>이용권 보러가기</Link>
              </div>
            ) : (
              <div className="table">
                {ticketBuys.map((t) => (
                  <div className="table-row" key={t.id}>
                    <span className="col-title">{t.plan}</span>
                    <span className="col-name">{t.price.toLocaleString()}원</span>
                    <span className="col-date">~ {t.expiresAt}</span>
                    <span className="col-date">{t.purchasedAt}</span>
                  </div>
                ))}
              </div>
            )}

            {/* 곡 구매내역 */}
            <div className="sub-section-title">곡 ({purchases.length})</div>
            {purchases.length === 0 ? (
              <div className="empty">
                구매한 곡이 없습니다. <Link to="/chart" style={{ color: '#17c964' }}>차트</Link>에서 [다운]을 눌러 보세요.
              </div>
            ) : (
              <div className="track-list">
                {purchases.map((p, i) => (
                  <div className="track-row" key={p.id}>
                    <div className="track-rank">{i + 1}</div>
                    {p.albumImage
                      ? <img className="thumb" src={p.albumImage} alt="" />
                      : <div className="thumb" />}
                    <div className="track-info">
                      <div className="track-title">{p.title}</div>
                      <div className="track-artist">{p.artist} · {p.album}</div>
                    </div>
                    <div className="purchase-meta">
                      <div className="purchase-price">{p.price.toLocaleString()}원</div>
                      <div className="purchase-date">{p.purchasedAt}</div>
                    </div>
                    <div className="track-actions">
                      <button onClick={() => playTrack(p)}>듣기</button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {menu === 'played' && (
          <>
            <div className="section-title">
              <span>내가 재생한 곡 ({recent.length}곡)</span>
              {recent.length > 0 && (
                <button className="link-btn" onClick={onClearHistory}>기록 삭제</button>
              )}
            </div>
            {recent.length === 0 ? (
              <div className="empty">재생한 곡이 없습니다. 곡을 들으면 여기에 쌓입니다.</div>
            ) : (
              <div className="track-list">
                {recent.map((t, i) => (
                  <HistoryRow key={t.id} track={t} no={i + 1} onPlay={playTrack} />
                ))}
              </div>
            )}
          </>
        )}

        {menu === 'most' && (
          <>
            <div className="section-title"><span>내가 가장 많이 듣던 곡</span></div>
            {top.length === 0 ? (
              <div className="empty">아직 집계할 기록이 없습니다.</div>
            ) : (
              <div className="track-list">
                {top.map((t, i) => (
                  <HistoryRow
                    key={t.id}
                    track={t}
                    no={i + 1}
                    onPlay={playTrack}
                    count={t.playCount}
                  />
                ))}
              </div>
            )}
          </>
        )}

        {menu === 'comments' && (
          <>
            <div className="section-title"><span>내가 쓴 댓글 ({myComments.length})</span></div>
            {myComments.length === 0 ? (
              <div className="empty">
                아직 쓴 댓글이 없습니다. <Link to="/notice" style={{ color: '#17c964' }}>공지사항</Link>에서 남겨 보세요.
              </div>
            ) : (
              <div className="comment-list">
                {myComments.map((c) => (
                  <div className="comment-row" key={c.id}>
                    <div className="comment-head">
                      {/* 어느 글에 쓴 댓글인지 — 누르면 그 글로 이동 */}
                      <Link to={`/notice/${c.noticeId}`} className="comment-notice">
                        {c.noticeTitle}
                      </Link>
                      <span className="comment-date">{c.date}</span>
                      <button className="link-btn comment-del" onClick={() => onDeleteMyComment(c.id)}>
                        삭제
                      </button>
                    </div>
                    <div className="comment-content">{c.content}</div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </main>
  )
}
