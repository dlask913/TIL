# (redis) Redis 특수 명령어
> Expiration, SET NX/XX, Pub/Sub, Transaction 등 ( [공식문서](https://redis.io/commands/) 확인 )

<br>

## Redis 특수 명령어
### 1. 데이터 만료 ( Expiration ) 
- 데이터를 특정시간 이후에 만료시키는 기능
- TTL ( Time To Live ) : 데이터가 유효한 시간(초 단위)
- 데이터 조회 요청 시에 만료된 데이터는 조회되지 않고 만료로 표시했다가 백그라운드에서 주기적으로 삭제
```shell
$ EXPIRE [key] [seconds]
$ TTL [key] # -1 이 반환되면, 만료 설정 X. -2가 반환되면, 만료 O
$ SETEX [key] [seconds] [value] # set 생성할 때 만료 시간 지정
```

### 2. SET NX/XX
- NX : 해당 Key 가 존재하지 않는 경우에만 SET
- XX : 해당 Key 가 이미 존재하는 경우에만 SET
- Null Reply : SET 이 동작하지 않은 경우 (nil) 응답
```shell
$ SET [key] [value] NX
$ SET [key] [value] XX
```

### 3. Pub/Sub
- Publisher 와 Subscriber 가 서로 알지 못해도 통신이 가능하도록 decoupling 된 패턴
- Publisher 는 Channel 에 Publish
- Subscriber 는 관심이 있는 Channel 을 필요에 따라 Subscribe 하여 메시지 수신
- Stream 은 메시지가 보관되지만 Pub/Sub은 Subscribe 하지 않을 때 발행된 메시지 수신 불가
```shell
$ SUBSCRIBE [channel1] [channel2] .. # 구독
$ PUBLISH [channel] [message] #발행
```

### 4. Pipeline ( Pipelining )
- 다수의 명령들을 한 번에 요청하여 네트워크 성능을 향상시키는 기술. Round-Trip Times 최소화
- Round-Trip Times : Request / Responses 모델에서 발생하는 네트워크 지연 시간

### 5. Transaction
- 다수의 명령을 하나의 트랜잭션으로 처리 -> 원자성 보장 ( All or Nothing )
- 중간에 에러가 발생하면 모든 작업 Rollback
- 하나의 트랜잭션이 처리되는 동안 다른 요청이 중간에 끼어들 수 없음.
```shell
$ MULTI # 트랜잭션 시작
$ INCR [key] # key 값 1 증가
$ DISCARD # 트랜잭션 취소
$ EXEC # 트랜잭션 실행
```

<br>

## 참고
[실전! Redis 활용](https://inf.run/BQH4z)