/**
 * 프로필 동그라미.
 *
 * 사진이 있으면 사진을, 없으면 아이디 첫 글자를 보여준다.
 * 헤더·홈·마이페이지 세 곳에서 쓰이므로 컴포넌트로 분리했다.
 *   → 나중에 모양을 바꿀 때 이 파일 하나만 고치면 된다.
 *
 * size : 'big'(64px) | 'xl'(110px) | 없으면 기본 30px
 */
export default function Avatar({ src, name, size }) {
  const className = `avatar${size ? ` ${size}` : ''}`

  if (src) {
    return <img className={className} src={src} alt="" />
  }
  return <span className={className}>{name?.[0]?.toUpperCase() ?? '?'}</span>
}
