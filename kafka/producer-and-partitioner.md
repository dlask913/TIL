# 카프카 프로듀서와 파티셔너
> 토픽 생성 방법, 카프카 프로듀서와 파티셔너

<br>

## 토픽을 생성하는 두가지 방법

1. 카프카 컨슈머 또는 프로듀서가 카프카 브로커에 생성되지 않은 토픽 데이터를 요청

2. 커맨드 라인 툴로 명시적으로 토픽을 생성
- 토픽을 효과적으로 유지보수하기 위해서 추천하는 방법
- 토픽마다 처리되어야 하는 데이터의 특성이 다르며, 특성에 따라 옵션을 달리 설정 할 수 있다. <br>
  ex> 동시 데이터 처리량이 많은 경우 : 파티션 개수를 100으로 설정 / 단기간 데이터 처리만 필요한 경우 : 토픽에 들어온 데이터의 보관기간 옵션을 짧게 설정할 수 있다. 

> **※ 주의사항** <br>
> 카프카 브로커와 로컬 커맨드 라인 툴 버전이 맞지 않으면 명령어가 정상적으로 실행되지 않을 수 있다. 

<br>

## 카프카 프로듀서 
- 카프카에 필요한 데이터를 선언하고 브로커의 특정 토픽 파티션에 전송한다.
- 데이터를 전송할 때 리더 파티션을 가지고 있는 카프카 브로커와 직접 통신한다.
- 카프카 브로커로 데이터를 전송할 때 내부적으로 파티셔너, 배치 생성 단계를 거친다.

### 프로듀서 내부 구조
- ProducerRecord : 프로듀서에서 생성하는 레코드 ( 토픽, 파티션, 타임스탬프, 메시지 키, 메시지 값 ), 오프셋은 미포함.
- send() : 레코드 전송 **요청** 메서드.
- Partitioner : 어느 파티션으로 전송할 지 지정하는 파티셔너. ( 기본값은 DefaultPartitioner 로 설정 )
- Accumulator : 배치로 묶어 전송할 데이터를 모으는 버퍼
- 동작 순서 : ProducerRecord → send() → Partitioner → Accumulator ( 토픽A-배치0, 배치1 / 토픽B-배치0, 배치1 ) → Sender → 카프카 클러스터

<br>

## 파티셔너 ( Java Library 기준 )
: 프로듀서API 를 사용하면 'UniformStickyPartitioner' 와 'RoundRobinPartitioner' 2개 파티셔너를 제공한다. ( 카프카 클라이언트 2.5.0 에서는 UniformStickyPartitioner 가 디폴트 ) <br>
### 메시지 키가 있는 경우 ( not null )
- UniformStickyPartitioner 와 RoundRoubinPartitioner 둘 다 메시지 키가 있을 때는 메시지 키의 해시값과 파티션을 매칭하여 레코드를 전송
- 동일한 메시지 키가 존재하는 레코드는 동일한 파티션 번호에 전달된다. 
- 파티션 개수가 변경될 경우에는 메시지 키와 파티션 번호 매칭은 깨지게 된다. → 그래서 파티션 개수를 여유있게 잡아두는 것이 가장 좋다. ( 50, 100 개.. )

### 메시지 키가 없는 경우 ( null )
 - 파티션에 최대한 동일하게 분배하는 로직이 들어있는데 UniformStickyPartitioner 와 RoundRoubinPartitioner 의 동작이 다르다. 

**1. RoundRobinPartitioner**
- ProducerRecord 가 들어오는 대로 파티션을 순회하면서 전송
- Accumulator 에서 묶이는 정도가 적기 때문에 전송 성능이 낮다.

**2. UniformStickyPartitioner**
- Accumulator 에서 레코드들이 배치로 묶일 때까지 기다렸다가 전송
- 배치로 묶일 뿐 결국 파티션을 순회하면서 보내기 때문에 모든 파티션에 분배되어 전송된다. 
- RoundRobinPartitioner 에 비해 성능이 좋다. 

### 커스텀 파티셔너
- 카프카 클라이언트 라이브러리에서는 사용자 지정 파티셔너를 생성하기 위한 Partitioner 인터페이스를 제공한다. 
- Partitioner 인터페이스를 상속받은 사용자 정의 클래스에서 메시지 키 또는 메시지 값에 따른 파티션 지정 로직을 생성하여 적용할 수 있다. 
- 파티셔너를 통해 파티션이 지정된 데이터는 Accumulator 에 버퍼로 쌓이고, Sender 스레드는 Accumulator 에 쌓인 배치 데이터를 가져가 카프카 브로커로 전송한다.

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)