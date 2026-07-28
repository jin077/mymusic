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

/** 앨범 목록: 곡 목록에서 앨범 기준으로 중복을 제거해 만든다 */
export const MOCK_ALBUMS = [...new Map(
  MOCK_TRACKS.map((t) => [t.album, { album: t.album, artist: t.artist, albumImage: t.albumImage }])
).values()]
