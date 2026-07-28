/**
 * 이용권 페이지.
 * 결제 기능은 만들지 않는다(학습 범위를 벗어남). 화면 구성만 잡아둔다.
 */
const PLANS = [
  { name: '무료 체험', price: '0원', desc: '30초 미리듣기', best: false },
  { name: '스트리밍', price: '월 7,900원', desc: '무제한 듣기', best: true },
  { name: '스트리밍 + 다운로드', price: '월 11,900원', desc: '듣기 + 저장 30곡', best: false },
]

export default function Ticket() {
  return (
    <main className="page">
      <h2 className="page-title">이용권</h2>
      <p className="page-desc">화면 구성 예시입니다. 실제 결제는 연동하지 않았습니다.</p>

      <div className="ticket-grid">
        {PLANS.map((p) => (
          <div key={p.name} className={`ticket-card${p.best ? ' best' : ''}`}>
            <div style={{ fontWeight: 700 }}>{p.name}</div>
            <div className="ticket-price">{p.price}</div>
            <div style={{ fontSize: 14, color: '#888' }}>{p.desc}</div>
            <button className="ticket-buy" onClick={() => alert('학습용 화면입니다')}>
              구매하기
            </button>
          </div>
        ))}
      </div>
    </main>
  )
}
