/**
 * 쪽 번호 표시.
 *
 * 서버가 준 { page, totalPages, first, last }를 받아 그린다.
 * 쪽이 많아도 화면이 넘치지 않게 현재 쪽 주변 5개만 보여준다.
 */
export default function Pagination({ page, totalPages, first, last, onChange }) {
  if (totalPages <= 1) return null   // 한 쪽뿐이면 그릴 필요가 없다

  // 현재 쪽을 가운데 두고 최대 5개. 앞뒤가 모자라면 반대쪽으로 밀어 채운다.
  const size = Math.min(5, totalPages)
  let start = Math.max(0, page - 2)
  if (start + size > totalPages) start = totalPages - size
  const numbers = Array.from({ length: size }, (_, i) => start + i)

  return (
    <div className="pagination">
      <button className="page-btn" disabled={first} onClick={() => onChange(page - 1)}>이전</button>

      {numbers.map((n) => (
        <button
          key={n}
          className={`page-btn${n === page ? ' on' : ''}`}
          onClick={() => onChange(n)}
        >
          {n + 1}
        </button>
      ))}

      <button className="page-btn" disabled={last} onClick={() => onChange(page + 1)}>다음</button>
    </div>
  )
}
