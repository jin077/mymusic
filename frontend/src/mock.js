/**
 * 임시(mock) 데이터.
 *
 * 왜 필요한가?
 *   백엔드 Spotify 연동이 아직 없어도 화면을 먼저 완성하기 위해서다.
 *   실무에서도 프론트와 백엔드를 동시에 개발할 때 이렇게 가짜 데이터로 화면을 먼저 만든다.
 *   나중에 music.js의 USE_BACKEND만 true로 바꾸면 실제 데이터로 교체된다.
 *
 * 데이터 모양(=백엔드가 앞으로 돌려줄 응답 형식)을 미리 정해두는 것이 핵심이다.
 *   { id, title, artist, album, albumImage, previewUrl }
 *   - albumImage : 앨범 이미지 주소 (없으면 회색 박스로 표시)
 *   - previewUrl : 30초 미리듣기 음원 주소 (없으면 재생 불가 안내)
 */
export const MOCK_TRACKS = [
  { id: 't1', title: '첫 번째 곡', artist: '가수 A', album: '앨범 하나', albumImage: null, previewUrl: null },
  { id: 't2', title: '두 번째 곡', artist: '가수 B', album: '앨범 둘', albumImage: null, previewUrl: null },
  { id: 't3', title: '세 번째 곡', artist: '가수 C', album: '앨범 셋', albumImage: null, previewUrl: null },
  { id: 't4', title: '네 번째 곡', artist: '가수 A', album: '앨범 하나', albumImage: null, previewUrl: null },
  { id: 't5', title: '다섯 번째 곡', artist: '가수 D', album: '앨범 넷', albumImage: null, previewUrl: null },
  { id: 't6', title: '여섯 번째 곡', artist: '가수 B', album: '앨범 둘', albumImage: null, previewUrl: null },
  { id: 't7', title: '일곱 번째 곡', artist: '가수 E', album: '앨범 다섯', albumImage: null, previewUrl: null },
  { id: 't8', title: '여덟 번째 곡', artist: '가수 C', album: '앨범 셋', albumImage: null, previewUrl: null },
  { id: 't9', title: '아홉 번째 곡', artist: '가수 F', album: '앨범 여섯', albumImage: null, previewUrl: null },
  { id: 't10', title: '열 번째 곡', artist: '가수 D', album: '앨범 넷', albumImage: null, previewUrl: null },
]

/**
 * 앨범 목록.
 *   { id, rank, album, artist, albumImage, releaseDate }
 *   - rank        : 인기 순위 (값으로 들고 다녀야 정렬해도 순위가 안 깨진다)
 *   - releaseDate : 발매일 (최신 앨범 정렬에 쓴다)
 */
export const MOCK_ALBUMS = [
  { id: 'a1', rank: 1, album: '앨범 하나', artist: '가수 A', albumImage: null, releaseDate: '2026-08-14' },
  { id: 'a2', rank: 2, album: '앨범 둘', artist: '가수 B', albumImage: null, releaseDate: '2026-07-30' },
  { id: 'a3', rank: 3, album: '앨범 셋', artist: '가수 C', albumImage: null, releaseDate: '2026-08-09' },
  { id: 'a4', rank: 4, album: '앨범 넷', artist: '가수 D', albumImage: null, releaseDate: '2026-06-21' },
  { id: 'a5', rank: 5, album: '앨범 다섯', artist: '가수 E', albumImage: null, releaseDate: '2026-08-02' },
  { id: 'a6', rank: 6, album: '앨범 여섯', artist: '가수 F', albumImage: null, releaseDate: '2026-05-15' },
]
