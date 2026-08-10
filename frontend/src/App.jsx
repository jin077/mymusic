import { Routes, Route, Link } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Chart from './pages/Chart'
import Albums from './pages/Albums'
import Ticket from './pages/Ticket'
import Search from './pages/Search'
import Login from './pages/Login'
import MyPage from './pages/MyPage'
import Admin from './pages/Admin'
import Notice from './pages/Notice'
import NoticeDetail from './pages/NoticeDetail'
import NoticeWrite from './pages/NoticeWrite'

/**
 * ===== 주소(URL)와 페이지를 연결하는 표 =====
 *
 * 예전에는 App.jsx에서 if(!token) 으로 화면을 갈라 보여줬지만,
 * 이제는 주소별로 페이지를 나눈다(React Router).
 *
 * ⭐ 어제 nginx에 넣은 try_files 설정이 여기서 의미를 갖는다.
 *   브라우저가 /chart 를 직접 요청하면 서버에는 그런 파일이 없다.
 *   nginx가 없는 경로를 index.html로 돌려주기 때문에 React가 /chart 페이지를 그릴 수 있다.
 *   (그 설정이 없으면 새로고침 시 404가 난다 — SPA 배포의 필수 설정)
 */
function NotFound() {
  return (
    <main className="page">
      <div className="empty">
        페이지를 찾을 수 없습니다. <Link to="/" style={{ color: '#17c964' }}>홈으로</Link>
      </div>
    </main>
  )
}

export default function App() {
  return (
    <Routes>
      {/* Layout(헤더 + 플레이어)을 공유하고, 그 안쪽 내용만 주소에 따라 바뀐다 */}
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/chart" element={<Chart />} />
        <Route path="/albums" element={<Albums />} />
        <Route path="/ticket" element={<Ticket />} />
        <Route path="/search" element={<Search />} />

        {/* 로그인·회원가입: 한 컴포넌트가 주소로 모드를 나눈다 */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Login />} />

        <Route path="/mypage" element={<MyPage />} />
        <Route path="/admin" element={<Admin />} />

        {/* 공지 게시판: 목록 · 글쓰기 · 상세 */}
        <Route path="/notice" element={<Notice />} />
        <Route path="/notice/write" element={<NoticeWrite />} />
        <Route path="/notice/:id" element={<NoticeDetail />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
