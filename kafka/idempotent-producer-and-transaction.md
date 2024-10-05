# 멱등성 프로듀서 및 트랜잭션 프로듀서와 컨슈머
> 멱등성 프로듀서, 트랜잭션 프로듀서와 컨슈머 동작 및 설정 ( JAVA 공식 라이브러리 기준 )

<br> 

## 멱등성 프로듀서
> 멱등성 : 여러 번 연산을 수행하더라도 동일한 결과를 나타내는 것을 말한다.

### 전달 신뢰성
- 멱등성 프로듀서는 동일한 데이터를 여러 번 전송하더라도 카프카 클러스터에 단 한번만 저장됨을 의미한다. 
- 기본 프로듀서의 동작 방식은 적어도 한 번 전달( at least once delivery ) 를 지원하며, 적어도 한 번 이상 데이터를 적재할 수 있고 데이터가 유실되지 않음을 뜻한다.
- 네트워크 이슈 등 문제가 발생했을 경우, 두 번 이상 적재할 가능성이 있으므로 데이터의 중복이 발생할 수 있다. 
1. At least once : 적어도 한 번 이상 전달
2. At most once : 최대 한 번 전달
3. Exactly once : 정확히 한 번 전달 → 멱등성 프로듀서로 운영해야 한다. 

### 멱등성 프로듀서
- 프로듀서가 보내는 데이터의 중복 적재를 막기 위해 0.11.0 이후 버전부터는 `enable.idempotence` 옵션을 사용하여 정확히 한 번 전달 ( exactly once delivery )을 지원한다. 
- `enable.idempotence` 옵션의 기본값은 false ( 카프카 3.0.0 부터는 true, acks=all 로 자동 변경 ) 이며 정확히 한 번 전달을 위해서는 true 로 옵션값을 변경하여 멱등성 프로듀서로 동작하게 만들어야 한다.
```java
configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
```
### 멱등성 프로듀서 동작
- 기본 프로듀서와 달리 데이터를 브로커로 전달할 때 프로듀서 PID 와 시퀀스 넘버를 함께 전달한다. 
  - PID (Producer unique ID) : 프로듀서의 고유한 ID
  - SID(Sequence ID) : 레코드의 전달 번호 ID
- 브로커는 프로듀서의 PID 와 시퀀스 넘버를 확인하여 동일한 메시지의 적재 요청이 오더라도 단 한 번만 데이터를 적재함으로서 프로듀서의 데이터는 정확히 한 번 브로커에 적재되도록 동작한다.
### 멱등성 프로듀서가 아닌 경우 
 : 흔치않은 경우이기 때문에 알고만 있어도 괜찮다. 
1. 프로듀서 `send()` 이후 리더 파티션에 데이터 적재
2. 네트워크 이슈로 인해 적재가 완료되었다는 응답 값을 보내지 못함
3. 프로듀서는 데이터 적재가 되지 않았다고 판단하여 다시 `send()` 호출
4. 데이터 중복 적재

### 멱등성 프로듀서의 한계
- 동일한 세션( PID 의 생명주기 )에서만 정확히 한 번 전달을 보장한다. → PID 가 달라지면 동일한 데이터여도 중복되었다고 판단하지 않는다. 
- 장애가 발생하지 않을 경우에만 정확히 한 번 적재하는 것을 보장한다는 점을 고려해야 한다.

### 멱등성 프로듀서로 설정할 경우 옵션
- `enable.idempotence` 를 true 로 설정한다. ( 3.0.0 부터 default )
- 프로듀서의 데이터 재전송 횟수를 정하는 `retries` 는 Integer.MAX_VALUE 로 설정되고, `acs` 옵션은 all 로 설정된다. 
- 정확히 한 번 전송하는 게 아니라 상황에 따라 프로듀서가 여러 번 전송하되 브로커가 여러 번 전송된 데이터를 확인하고 중복된 데이터는 적재하지 않는 것이다. 

### OutOfOrderSequenceException
: SID 는 0부터 시작하여 숫자를 1씩 더한 값이 전달되는데 이 시퀀스 넘버가 일정하지 않은 경우 ( ex> 0 → 2 등 ) 위 예외가 발생할 수 있다. 그렇기 때문에 순서가 중요한 데이터를 전송하는 프로듀서는 대응 방안을 고려해야 하며 흔한 상황은 아니다. 

<br> 

## 트랜잭션 프로듀서와 컨슈머
### 트랜잭션 프로듀서 동작
- 카프카에서 트랜잭션은 다수의 파티션에 데이터를 저장할 때 모든 데이터에 대해 동일한 원자성을 만족시키기 위해 사용된다.
- 다수의 데이터를 동일 트랜잭션으로 묶음으로써 컨슈머가 전체 데이터를 처리하거나 전체 데이터를 처리하지 않도록 한다.
- 사용자가 보낸 데이터를 레코드로 파티션에 저장하고 트랜잭션의 시작과 끝을 표혀나기 위해 트랜잭션 레코드(COMMIT)를 한 개 더 보낸다.
### 트랜잭션 컨슈머 동작
- 파티션에 저장된 트랜잭션 레코드를 보고 트랜잭션이 완료(COMMIT) 되었음을 확인하고 데이터를 가져간다. 
- 트랜잭션 레코드는 실질적인 데이터는 가지고 있지 않으며 트랜잭션이 끝난 상태를 표시하는 정보만 갖는다. 
### 트랜잭션 프로듀서 설정
- `transactional.id` 를 설정해야 하고 프로듀서별로 고유한 ID 값을 사용해야 한다. 
- init, begin, commit 순서대로 수행되어야 한다. 
```java
config.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, UUID.randomUUID());

Producer<String, String> producer = new KafkaProducer<>(configs);

producer.initTransactions(); // 1
  
producer.beginTransaction(); // 2
producer.send(new ProducerRecord<>(TOPIC, "전달하는 메시지 값"));  
producer.commitTransaction(); // 3
  
producer.close();
```
### 트랜잭션 컨슈머 설정
- 커밋이 완료된 레코드들만 읽기 위해 `isolation.level` 옵션을 `read_committed` 로 설정한다. 
- 기본 값은 `read_uncommitted` 로서 커밋여부와 상관없이 모든 레코드를 읽는다. 
```java
configs.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)