/**
 * 이벤트 안내 페이지.
 *
 * 아직 이벤트 데이터를 담을 곳(백엔드 테이블·API)이 없다.
 * 공지사항처럼 만들면 되므로, 필요해지면 Notice 구조를 그대로 따라가면 된다.
 */
export default function Event() {
  return (
    <main className="page">
      <h2 className="page-title">이벤트</h2>
      <p className="page-desc">진행 중인 이벤트를 안내합니다.</p>

      <div className="empty">진행 중인 이벤트가 없습니다.</div>
    </main>
  )
}
