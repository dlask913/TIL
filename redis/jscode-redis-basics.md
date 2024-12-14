## (redis) 레디스 기본
> Redis 사용 사례, 설치 방법, 기본 명령어, Key 네이밍 컨벤션

<br>

## Redis
- 키-값 구조의 비정형 데이터를 저장하고 관리하기 위한 오픈 소스 기반의 비관계형 DBMS
- 데이터 처리 속도가 엄청 빠른 NoSQL ( Key-Value 형태 ) 데이터베이스
- 인메모리(in-memory) 에 모든 데이터를 저장하기 때문에 속도가 빠르다. 
  ↔ MySQL 과 같은 RDBMS 는 대부분 디스크에 데이터를 저장

### 사용 사례
- 캐싱 ( ★ 가장 많이 사용 ) : 데이터 조회 성능 향상
- 세션 관리 
- 실시간 분석 및 통계
- 메시지 큐
- 지리공간 인덱싱
- 속도 제한
- 실시간 채팅 및 메시징

### 설치 방법
1. msi 확장자의 Redis 설치 프로그램 다운로드 ( [링크](https://github.com/microsoftarchive/redis/releases) ) : 따로 바꾸는 설정 없음
2. C:\Program Files\Redis 내 redis-cli.exe 실행하여 동작 테스트
```cli
127.0.0.1:6379> ping
PONG // 정상 출력
```

### 기본 명령어
1. 데이터 저장 : `set`
```cli
127.0.0.1:6379> set limnj:name "limnj park" // set [key] [value]
OK
```
2. 데이터 조회 : `get`
```cli
127.0.0.1:6379> get limnj:name // get [key]
"limnj park"
```
3. 모든 키 값 조회 : `keys *`
```cli
127.0.0.1:6379> keys *
1) "limnj:name"
2) "limnj:hobby"
```
4. 데이터 삭제 : `del`
```cli
127.0.0.1:6379> del limnj:hobby // del [key]
(integer) 1

127.0.0.1:6379> get limnj:hobby
(nil) // 데이터가 없는 경우
```
5. 데이터 만료 시간(TTL) 설정 : `set ~ ex`
```cli
127.0.0.1:6379> set limnj:pet dog ex 30 // set [key] [value] ex [seconds]
OK
```
6. 데이터 만료 시간(TTL) 확인 : `til`
```cli
127.0.0.1:6379> ttl limnj:pet // ttl [key] : 만료시간 확인
(integer) 26
// -2 인 경우 만료되었음을, -1 인 경우 ttl 이 설정되지 않았음을 의미
```
7. 모든 키 값 삭제 : `flushall`
```cli
127.0.0.1:6379> flushall
OK

127.0.0.1:6379> keys *
(empty list or set)
```

<br>

## Redis Key 네이밍 컨벤션
> 콜론 (:) 을 활용해 계층적으로 의미를 구분해서 사용하자.

### 장점
1. 가독성 : 데이터의 의미와 용도를 쉽게 파악할 수 있다. 
2. 일관성 : 코드의 일관성이 높아지고 유지보수가 쉬워진다.
3. 검색 및 필터링 용이성 : 특정 유형의 key 를 쉽게 찾을 수 있다. 
4. 확장성 : 서로 다른 key 와 이름이 겹쳐 충돌할 일이 적어진다. 

### 예시
- `uers:100:profile` : 사용자들(users) 중에서 PK 가 100인 사용자의 프로필(profile)
- `products:123:details` : 상품들(products) 중에서 PK 가 123인 상품의 세부사항(details)

<br>

## 참고
[인프런 - 비전공자도 이해할 수 있는 Redis 입문/실전 (조회 성능 최적화편)](https://inf.run/Pupon)