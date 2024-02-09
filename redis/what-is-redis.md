# (redis) Redis 란?
> 다수의 서버가 공유하는 해시 테이블

<br>

## Redis 란? ( [공식문서](https://redis.io/docs/about/))
```Remote Dictionary Server```의 약자로 오픈 소스 In-memory 데이터 구조를 갖는 저장소이다. 널리 사용되는 고성능 키-값 데이터베이스로 캐싱, 세션 관리, 실시간 애플리케이션 등 다양한 용도로 활용된다. Redis 는 메모리 내에서 데이터를 저장하기 때문에 디스크 기반 데이터베이스보다 빠른 읽기 및 쓰기 작업을 제공한다.

<br>

## Redis 특징
1. In-Memory : 모든 데이터를 RAM 에 저장
2. Single Threaded : 단일 thread 에서 모든 task 처리 ( Redis 6.0 부터 멀티스레드 I/O 기능 도입 )
3. Cluster Mode 다중 노드에 데이터를 분산 저장하여 안정성 & 고가용성 제공
4. Persistence (영속성) : RDB(Redis Database) + AOF(Append only file ) 통해 영속성 옵션 제공<br>
    - RDB : Point-in-time Snapshot -> 재난 복구 또는 복제에 주로 사용. 일부 데이터 유실 위험이 있고 스냅샷 생성 중 클라이언트 요청 지연 발생
    - AOF : Redis 에 적용되는 Write 작업을 모두 log 로 저장. 데이터 유실의 위험이 적지만, 재난 복구 시 Write 작업을 다시 적용하기 때문에 RDB 보다 느림
    - 함께 사용하는 옵션 제공
5. [Pub/Sub](https://redis.io/docs/interact/pubsub/) : Pub/Sub 패턴을 통해 비동기 메시징 패러다임을 제공

<br>

## Redis 장점
- 모든 데이터를 메모리에 저장하기 때문에 빠른 읽기/쓰기 속도 보장
- 다양한 Data Type 지원
- Python, Java, JavaScript 등 다양한 언어로 작성된 클라이언트 라이브러리 지원
- 마스터-슬레이브 노드 설정을 통해 데이터 복제를 지원하고 마스터 노드가 업데이트될 때 슬레이브 노드도 자동으로 업데이트 가능
- 명령어들을 큐에 추가하고 EXEC 이 실행되면 큐에 저장된 모든 명령어를 한 번에 실행할 수 있는 트랜잭션을 지원

<br>

## Redis 사용 사례 
- Caching 
- Rate Limiter
- Message Broker
- 실시간 분석 / 계산
- 실시간 채팅 

<br>

## 참고
https://marketsplash.com/tutorials/redis/redis-5-vs-6/#link4 <br>
https://redis.com/blog/diving-into-redis-6/ <br>
https://dzone.com/articles/10-traits-of-redis <br>
[실전! Redis 활용](https://inf.run/BQH4z)
