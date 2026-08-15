import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getTicketPlans, purchaseTicket } from '../storage'

/**
 * 이용권 페이지.
 *
 * ⭐ 가격표를 화면에 적어두지 않고 서버에서 받아온다.
 *   가격이 바뀌면 서버와 화면이 어긋난다("7,900원입니다" 하고 다른 금액이 빠지는 상황).
 *   가격의 주인은 서버 한 곳이어야 한다.
 *
 * ⭐ 결제는 캐시(포인트) 차감 방식이다.
 *   실제 결제 연동(PG)은 사업자 등록이 필요해 넣지 않았다.
 *   충전 자리에 PG를 붙이면 그대로 이어진다.
 */
export default function Ticket() {
  const { isLoggedIn, profile, refreshProfile } = useAuth()
  const [plans, setPlans] = useState([])
  const [message, setMessage] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    getTicketPlans().then(setPlans).catch(() => setPlans([]))
  }, [])

  const onBuy = async (plan) => {
    if (!isLoggedIn) {
      if (confirm('이용권 구매는 로그인이 필요합니다. 로그인하러 갈까요?')) navigate('/login')
      return
    }
    if (!confirm(`'${plan.name}'을(를) ${plan.price.toLocaleString()}원에 구매할까요?\n${plan.days}일 이용할 수 있습니다.`)) return

    try {
      const saved = await purchaseTicket(plan.id)
      setMessage(`구매 완료. ${saved.expiresAt}까지 이용할 수 있습니다.`)
      refreshProfile()        // 홈·마이페이지의 이용권 표시 갱신
    } catch (err) {
      const status = err.response?.status
      if (status === 402) {
        if (confirm(`${err.response.data.message}\n충전하러 갈까요?`)) navigate('/mypage')
      } else {
        setMessage('구매에 실패했습니다.')
      }
    }
  }

  return (
    <main className="page">
      <h2 className="page-title">이용권</h2>
      <p className="page-desc">보유 캐시로 구매합니다. 실제 결제(PG)는 연동하지 않았습니다.</p>

      {isLoggedIn && (
        <div className="cash-box">
          보유 캐시 <b>{(profile?.balance ?? 0).toLocaleString()}원</b>
          <span className="cash-note">
            {profile?.ticketDaysLeft != null
              ? `현재 ${profile.ticketName} · ${profile.ticketDaysLeft}일 남음`
              : '이용 중인 이용권 없음'}
          </span>
        </div>
      )}

      <div className="ticket-grid">
        {plans.map((p) => (
          <div key={p.id} className={`ticket-card${p.id === 'STREAMING' ? ' best' : ''}`}>
            <div style={{ fontWeight: 700 }}>{p.name}</div>
            <div className="ticket-price">
              {p.price === 0 ? '무료' : `${p.price.toLocaleString()}원`}
            </div>
            <div style={{ fontSize: 14, color: '#888' }}>{p.description}</div>
            <div style={{ fontSize: 13, color: '#888', marginTop: 4 }}>{p.days}일 이용</div>
            <button className="ticket-buy" onClick={() => onBuy(p)}>구매하기</button>
          </div>
        ))}
      </div>

      {message && <p className="msg" style={{ color: '#17c964' }}>{message}</p>}

      <p className="pw-note" style={{ marginTop: 24 }}>
        구매내역은 <Link to="/mypage" style={{ color: '#17c964' }}>마이페이지 &gt; 구매내역</Link>에서 볼 수 있습니다.
      </p>
    </main>
  )
}
