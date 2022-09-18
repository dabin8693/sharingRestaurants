
###TODO LIST###
map으로 저장한거 다시 data class로 바꿔서 저장방식으로 바꾸기
util패키지에 StringData클래스 생성(string타입의 상수 모음)

##버그##
//개인 로컬 저장소에 쓰지 않는 사진 삭제(글 수정, 삭제 후 등)
//로그아웃 후 로그인시 해당 프레그먼트의 유저 사진로드가 실패하는 버그
//기존 사용하던 거리재기 함수 엉터리 naverMap Api 거리재기로 변경(이것도 직선거리 임)
//로그인 도중 뒤로가기, 중복 클릭으로 인한 문제 해결
//특정 사진 불러올때 에러 있음
//파이어베이스 실패, 인터넷 끊김, gps 끊김 등 예외처리
//생명주기 차이로 인한 메모리leak, observer중복 호출, 보일러 코드 제거, 자주 사용하는 중복코드 따로 클래스화

##기능 추가##
1. 글 작성 기능에 우선은 카메라 기능 빼고 제작
2. 글 보기 기능 추가
3. 정렬 스피너 기능 추가
4. 검색 기능 추가
5. 닉네임 변경 기능 추가
6. 글 작성 기능에 사진 기능 추가(카메라intent는 내장 저장소에 파일로 저장후 파이어스토리지에 업로드(단. 등록 버튼 눌렀을때 행한다.))
7. 글 추천기능 추가
8. 추천탭 기능 추가
9. 댓글, 답글 기능 추가
10. 내정보에서 내가 쓴 글, 댓글, 답글 보기 기능 추가
11. 글 안에서 길찾기 기능 버튼 추가(외부 지도앱으로 인텐트)
12. off라인 글 on라인으로 올리기 기능 추가
13. 내정보 탭에 오픈소스, 개발자 이메일 보내기 기능 추가
14. 무한스크롤 기능 추가
    ##마무리##
15. 프로그래스바 필요한것들 추가
16. 디자인 디테일 신경쓰기
17. 터치시 디자인 강조(내정보 탭 클릭시 내정보 이미지 색칠 등)
18. 패키지, 클래스, 변수, 메소드 정리, mvvm모델에 맞게 기능 분리
19. 대거 힐트 적용