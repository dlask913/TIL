# (redis) Redis 사용 시 주의사항
>  Redis 사용 시 주의사항

<br>

## O(N) 명령어
- 대부분의 명령어는 O(1) 시간 복잡도를 갖지만, 일부 명령어의 경우 O(N)
- Redis 는 Single Thread 로 명령어를 순차적으로 수행하기 때문에 오래 걸리는 O(N) 명령어 수행 시 성능이 저하될 수 있다.
1. **KEYS** : 지정된 패턴과 일치하는 모든 key 조회 -> SCAN 명령어로 대체
2. **SMEMBERS** : Set 의 모든 member 반환 ( N = Set Cardinality, 10000개 이상의 아이템 추가 X )
3. **HGETALL** : Hash 의 모든 field 반환 ( N = Size of Hash )
4. **SORT** : List, Set, ZSet 의 item 정렬하여 반환

<br>

## Thundering Herd Problem 
- 병렬 요청이 공유 자원에 대해서 접근할 때 급격한 과부하가 발생하는 문제.

<br>

## Stale Cache Invalidation
- 캐시의 유효성이 손실되었거나 변경되었을 때 캐시를 변경하거나 삭제하는 기술

<br>

## 보안 ( [공식문서](https://redis.io/docs/management/security/) )
- Redis 는 인증없이 누구나 접근할 수 있으므로 보안 설정이 필요.
- requirepass 설정을 통해 인증되지 않은 클라이언트 쿼리를 거부할 수 있다.
- TLS 를 사용하여 데이터 전송을 암호화할 수 있다.

<br>

## 참고
[실전! Redis 활용](https://inf.run/7ctks)