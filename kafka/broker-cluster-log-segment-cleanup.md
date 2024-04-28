# 카프카 기본 개념-1
> 브로커와 클러스터, 로그와 세그먼트, 삭제 주기 및 옵션

<br>

## 카프카 브로커와 클러스터
![image](https://github.com/dlask913/TIL/assets/79985588/a431f9ae-0eb4-40dd-8160-39a6e6958dd2)
- **주키퍼** : 카프카 클러스터를 운영하기 위해 반드시 필요한 애플리케이션, 카프카 3.x 부터는 주키퍼를 사용하지 않아도 되나 완벽하게 대체하지는 못한다. 그래서 주키퍼가 있는 카프카 클러스터를 운영한다. 
- **클러스터** : 한 개의 클러스터는 여러개의 브로커가 존재할 수 있다. 최소 세 개의 브로커가 일반적이고 데이터량이 많고 확장해야하는 경우는 50개~100개의 브로커로 운영. 여러 브로커가 모여 구성된 Kafka 시스템의 전체 집합이다.
- **브로커** : 데이터를 여러 대의 브로커에 분산 저장한다. Kafka 시스템의 기본 서버 단위로, 데이터를 안전하게 보관하고 처리하기 위해서는 3개 이상의 서버를 한 대의 클러스터로 묶어야한다. 

<br>

## 브로커의 역할
### 1. 컨트롤러
: 다수의 브로커 중 한 대가 컨트롤러의 역할을 한다. 다른 브로커들의 상태 체크, 다른 브로커가 클러스터에서 빠지는 경우 해당 브로커에 존재하는 리더 파티션을 재분배한다. 컨트롤러 역할을 하는 브로커에 장애가 생기면 다른 브로커가 컨트롤러 역할을 한다.

### 2. 데이터 삭제
: 컨슈머가 데이터를 가져가더라도 토픽의 데이터는 삭제되지 않고 오직 브로커만이 데이터를 삭제할 수 있다. 데이터 삭제는 파일 단위로 이루어지는데 이 단위를 '로그 세그먼트' 라고 부른다. 이 세그먼트에서는 다수의 데이터가 들어있기 떄문에 일반적인 데이터베이스처럼 특정 데이터를 선별해서 삭제할 수 없다. 

### 3. 컨슈머 오프셋 저장
: 컨슈머 그룹은 토픽이 특정 파티션으로부터 데이터를 가져가서 처리하고 이 파티션의 어느 레코드까지 가져갔는 지 확인하기 위해 오프셋을 커밋한다. 커밋한 오프셋은 ```__
consumer__offsets``` 토픽에 저장. 컨슈머가 장애가 발생하거나 혹은 버전 업그레이드를 통해 재시작을 해야할 때 다음으로 처리해야할 데이터를 바라봐야할 때 이 오프셋을 바탕으로 데이터를 가져간다.

### 4. 그룹 코디네이터 
: 컨슈머 그룹의 상태를 체크하고 파티션을 컨슈머와 매칭되도록 분배하는 역할을 한다. 컨슈머에 문제가 생겨 그룹에서 빠졌을 때 매칭되지 않은 파티션을 정상 동작하는 컨슈머로 할당하여 지속적으로 데이터가 처리될 수 있도록 돕는다. 이렇게 파티션을 컨슈머로 재할당하는 과정을 리밸런스(rebalance) 라고 한다.

### 5. 데이터의 저장
: 카프카를 실행할 때 config/server.properties 의 log.dir 옵션에 정의한 디렉토리에 데이터를 저장한다. 토픽 이름과 파티션 번호의 조합으로 하위 디렉토리를 생성하여 데이터를 저장한다. 예를 들어, hello.kafka 토픽의 0번 파티션이 있다면 디렉토리에는 .index, .log, .timeindex 의 파일이 있다. log 에는 메시지와 메타데이터를 저장한다. index 는 메시지의 오프셋을 인덱싱한 정보를 담은 파일이다. timeindex 파일에는 메시지에 포함된 timestamp 값을 기준으로 인덱싱한 정보가 담겨있다. 

<br>

## 로그와 세그먼트
- **로그** : 데이터의 불변 순차적 기록을 의미한다. 프로듀서가 보낸 메시지들을 순서대로 저장하고 컨슈머가 읽을 수 있도록 한다. 디스크에 지속적으로 저장되며 특정 시간 및 크기에 도달할 때까지만 보관되어 나중에 삭제될 수 있다. 
- **세그먼트** : 로그 파일은 세그먼트(segment) 라고 하는 더 작은 파일로 나뉜다. 로그 세그먼트는 카프카가 데이터를 디스크에 저장하는 방식을 최적화하는 데 도움을 준다. 새 메시지가 로그에 추가될 때 active 세그먼트 파일에 이 메시지를 쓰다가 설정된 크기에 도달하면 새로운 세그먼트 파일을 생성한다. 
- 최초의 offset 번호가 log 파일 이름이 된다. ( 000010.log, 0000020.log, .. )
- log.segment.bytes : 바이트 단위의 최대 세그먼트 크기 지정. 기본 값은 1GB
- log.roll.ms(hours) : 세그먼트가 신규 생성된 이후 다음 파일로 넘어가는 시간 주기. 기본 값은 7일. 그래서 레코드가 충분히 쌓이지 않은 상태에서 종료될 수 있기 때문에 세그먼트 크기가 더 작아질 수 있다. 
- 액티브 세그먼트 : 가장 마지막 세그먼트 파일 ( 쓰기가 일어나고 있는 파일 ) . 브로커의 삭제 대상에 포함되지 않는다. 액티브 세그먼트를 제외한 세그먼트는 retention 옵션에 따라 삭제 대상으로 지정된다.

<br>

## 세그먼트와 삭제 주기
### 1. cleanup.policy = delete
- retention.ms(minutes, hours) : 세그먼트를 보유할 최대 기간. 기본 값은 7일 ( 일반적으로 3일정도로 설정? )
- retention.bytes : 파티션당 로그 적재 바이트 값. 기본값은 -1 ( 지정하지 않음 )
- log.retention.check.interval.ms : 세그먼트가 삭제 영역에 들어왔는 지 확인하는 간격. 기본 값은 5분.
- 카프카에서 데이터는 세그먼트 단위로 삭제되기 때문에 로그 단위(레코드 단위) 로 개별 삭제는 불가능하다.
- 이미 적재되어있는 데이터 수정 불가능하다.
- 데이터를 적재할 때 (프로듀서) 또는 데이터를 사용할 때(컨슈머) 데이터를 검증하는 것이 좋다. 

### 2. cleanup.policy = compact
![image](https://github.com/dlask913/TIL/assets/79985588/90938725-b290-4c64-95dc-9542918a3c64)
- topic 단위로 설정하게 되면 메시지 키 별로 해당 메시지 키의 가장 오래된 데이터를 삭제하는 정책. 그렇기 때문에 delete 와 달리 일부 레코드만 삭제가 될 수 있다. 압축은 액티브 세그먼트를 제외한 데이터를 대상으로 한다.
- 테일 영역 : 압축 정책에 의해 압축이 완료된 레코드들을 클린 로그 라고도 부른다. 중복 메시지 키가 없다.
- 헤드 영역 : 압축 정책이 되기 전 레코드들. 더티로그라고도 부른다. 
- min.cleanable.dirty.ratio : 데이터의 압축 시작시점을 설정한다. 액티브 세그먼트를 제외한 세그먼트에 남아있는 테일 영역의 레코드 개수와 헤드 영역의 레코드 개수의 비율을 뜻한다. 0.9로 설정하게 되면 테일 영역이 90%가 되었을 때 압축을 하게 된다. 

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5) <br>
http://cloudurable.com/blog/kafka-architecture-log-compaction/index.html