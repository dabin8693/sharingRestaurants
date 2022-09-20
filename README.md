
###TODO LIST###

##바로 할것##
1. map으로 저장한거 다시 data class로 바꿔서 저장방식으로 바꾸기
2. util패키지에 StringData클래스 생성(string타입의 상수 모음)
3. 퍼미션 함수 util패키지로 이전하기
4. 자주 쓰이거나 따로 기능 분리가 가능한 함수드 util 패키지로
5. 사진 처리 하나의 클래스로 api레벨에 따라 달라지는건 delegate패턴사용( class pictureProcessing(val camera(bitmap: Bitmap): Camera): Camera by camera{파일저장, 사이즈조절 등 fun()} // class camerApi28(bitmap: Bitmap): Camera{})
6. 모든 화면 방향 portrait고정하기 예외처리 보일러코드 불편
7. 버그 3, 기능 1, 2, 3, 4, 5번 작업 완료하기

##자주 실수하는 것##
1. 생명주기 차이로 인한 메모리leak(뷰모델 화면회전, 프래그먼트 뷰 주의, 쓰레드(비동기함수)에러로 오래 남는경우(이너, 익명클래스쓸때)(lamda,sam,중첩 권장)), observer중복 호출 체크, 비동기 함수들 예외처리 체크, 익명클래스 쓸때 외부 로컬변수 쓸때 체크!!
2. 코틀린 get/set 컴파일시 자동생성됨으로써 내가 get/set함수 만들면 함수이름 겹쳐서 오류 남
3. 파이어베이스 toObject 쓸때 필드 한글이면 에러남, FieldValue.serverTimestamp()이거 쓸려면 data class말고 map으로 저장
4. mutable변수는 private로 뷰모델에서 정해진 로직대로 수정가능하게 최대한 바꾸기 //캡슐화
5. SOLID원칙 최대한 적용해보기 //기능 완성한 후에

##버그##
1. 개인 로컬 저장소에 쓰지 않는 사진 삭제(글 수정, 삭제 후 등)
2. 로그아웃 후 로그인시 해당 프레그먼트의 유저 사진로드가 실패하는 버그
3. 기존 사용하던 거리재기 함수 엉터리 naverMap Api 거리재기로 변경(이것도 직선거리 임)
4. 로그인 도중 뒤로가기, 중복 클릭으로 인한 문제 해결
5. 특정 사진 불러올때 에러 있음
6. 파이어베이스 실패, 인터넷 끊김, gps 끊김 등 예외처리
7. 보일러 코드 제거, 자주 사용하는 중복코드 따로 클래스화, 객체 의존성 관리

##고려할 것##
1. 파이어베이스 리스너 중복호출 문제, 파이어베이스auth리스너에 activity들어가는데 이게 싱글톤인 firebaseAuth에 계속 참조됨 leak카나리로 체크 해봐야 됨//우선 java.lang.ref.WeakReference(activity).get()로 약한참조

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
14. 무한스크롤 기능 추가(페이징3 적용)

##마무리##
15. 프로그래스바 필요한것들 추가
16. try/catch, api레벨, 레거시함수, support라이브러리, 화면 해상도에 따른 레이아웃 체크
17. 디자인 디테일 신경쓰기
18. 터치시 디자인 강조(내정보 탭 클릭시 내정보 이미지 색칠 등)
19. 패키지, 리소스, 클래스, 변수, 메소드 정리, mvvm모델에 맞게 기능 분리
20. 대거 힐트 적용