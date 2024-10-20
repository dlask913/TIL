# 카프카 커넥트
> 카프카 커넥트와 커넥터, 커넥트 배포 및 운영 ( 단일 모드와 분산 모드 )

<br>

## 카프카 커넥트 ( kafka connect )
> 소스 애플리케이션 → 카프카 커넥트 : 소스 커넥터 ( 프로듀서 역할 ) → 카프카 토픽  → 카프카 커넥트 : 싱크 커넥터 ( 컨슈머 역할 ) → 싱크 애플리케이션

- 카프카 오픈소스에 포함된 툴 중 하나로 데이터 파이프라인 생성 시 반복 작업을 줄이고 효율적인 전송을 이루기 위한 애플리케이션
- 커넥트(connect) 는 특정한 작업 형태를 템플릿으로 만들어 놓은 커넥터(connector) 를 실행함으로서 반복 작업을 줄일 수 있다.

### 커넥트 내부 구조
- 사용자가 커넥트에 커넥터 생성 명령을 내리면 커넥트는 내부에 커넥터와 태스크를 생성한다.
- **커넥터( connector )** : 태스크들을 관리한다.
- **태스크( task )** : 커넥터에 종속되는 개념으로 실질적인 데이터 처리를 한다. 그러므로 데이터 처리를 확인하기 위해서는 각 태스크의 상태를 확인해야 한다.

<br>

## 커넥터 ( connector )
### 소스 커넥터와 싱크 커넥터
- 커넥터는 **프로듀서 역할을 하는 소스 커넥터와 컨슈머 역할을 하는 싱크 커넥터** 2가지로 나뉜다.
- 일정한 프로토콜을 가진 소스 애플리케이션이나 싱크 애플리케이션이 있다면 커넥터를 통해 카프카로 데이터를 보내거나 카프카에서 데이터를 가져올 수 있다. ( ex> 파일 주고 받기 )
- MySQL, S3, MongoDB 등과 같은 저장소를 대표적인 싱크 애플리케이션, 소스 애플리케이션이라 볼 수 있다. 
- MySQL 에서 카프카로 데이터를 보낼 때와 카프카에서 데이터를 MySQL 로 저장할 때 JDBC 커넥터를 사용하여 파이프라인을 생성할 수 있다. 
### 커넥터 플러그인
- 카프카 2.6 에 포함된 커넥트를 실행할 경우 클러스터 간 토픽 미러링을 지원하는 미러메이커2 커넥터와 파일 싱크 커네거, 파일 소스 커넥터를 기본 플러그인으로 제공한다. 
- 이외에 추가적인 커넥터를 사용하고 싶다면 플러그인 형태로 커넥터 jar 파일을 추가하여 사용할 수 있다. 
- 커넥터 jar 파일에는 커넥터를 구현하는 클래스를 빌드한 파일이 포함되어 있다. 
- 커넥터 플러그인을 추가하고 싶다면 직접 커넥터 플러그인을 만들거나 이미 인터넷상에 존재하는 커넥터 플러그인을 가져다 쓸 수도 있다. 
### 오픈소스 커넥터 ( [참고자료](https://www.confluent.io/hub/) )
- 직접 커넥터를 만들 필요가 없으며 커넥터 jar 파일을 다운로드하여 사용할 수 있다. 
- HDFS 커넥터, AWS S3 커넥터, JDBC 커넥터, 엘라스틱서치 커넥트 등 100개가 넘는 커넥터들이 공개되어 있다. 
- 모두 무료로 제한없이 사용할 수 있는 것은 아니라 라이선스를 참고해야한다.
### 컨버터와 트랜스폼
- 사용자가 커넥터를 사용하여 파이프라인을 생성할 때 컨버터와 트랜스폼 기능을 옵션으로 추가할 수 있다. 
- **컨버터( converter )** :  데이터 처리를 하기 전에 스키마를 변경하도록 도와준다. JsonConverter, String Converter, ByteArrayConerter 를 지원하고 커스텀 컨버터를 사용할 수도 있다. 
- **트랜스폼 ( transform )** : 데이터 처리 시 각 메시지 단위로 데이터를 간단하게 변환하기 위한 용도로 사용된다. 예를 들어, JSON 데이터를 커넥터에서 사용할 때 특정 키를 삭제하거나 추가할 수 있다. 기본 제공 트랜스폼으로 Cast, Drop, ExtractField 등이 있다. 

<br>

## 카프카 커넥트 배포 및 운영
### 커넥트 실행 방법
#### 1. 단일 모드 커넥트 ( standalone mode kafka ) 
- 단일 애플리케이션으로 실행, 1개 프로세스만 실행된다.
- 커넥트를 정의하는 파일을 작성하고 해당 파일을 참조하는 단일 모드 커넥트를 실행함으로서 파이프라인을 생성할 수 있다. 
- 고가용성이 구성되지 않아 단일 장애점( SPOF : Single Point Of Failure ) 이 될 수 있다.
- 주로 개발환경이나 중요도가 낮은 파이프라인을 운영할 때 사용된다. 

#### 2. 분산 모드 커넥트 ( distributed mode kafka connect ) 
- 2 대 이상의 서버에서 클러스터 형태로 운영함으로서 안전하게 운영할 수 있다. 
- 2개 이상의 커넥트가 클러스터로 묶이면 1개의 커넥트가 중단되더라도 남은 2개의 커넥트가 파이프라인을 지속적으로 처리할 수 있다. 
- 커넥트가 실행되는 서버 개수를 늘림으로서 무중단으로 스케일 아웃하여 데이터 처리량의 변화에도 유연하게 대응할 수 있다. 
- 상용환경에서 커넥트를 운영한다면 분산 모드 커넥트를 2대 이상으로 구성하고 설정하는 것이 좋다. 
### 커넥트 REST API 인터페이스 ( [참고자료](https://docs.confluent.io/platform/current/connect/references/restapi.html) )
: REST API 를 사용하면 현재 실행 중인 커넥트의 커넥터 플러그인 종류, 태스크 상태, 커넥터 상태 등을 조회할 수 있다. 8083 포트로 호출할 수 있으며 HTTP 메서드 기반 API 를 제공한다. 

<br>

## 단일 모드 커넥트 실행하기
1. 커넥트 파일 설정 : `connect-standalone.properties`
```properties
bootstrap.servers=localhost:9092

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter.schemas.enable=true

# 오프셋 정보를 파일로 저장
offset.storage.file.filename=/tmp/connect.offsets
offset.flush.interval.ms=10000

# Note: symlinks will be followed to discover dependencies or plugins.
# Examples:
# plugin.path=/usr/local/share/java,/usr/local/share/kafka/plugins,/opt/connectors,
```
2. 커넥터 파일 설정 : `connect-file-source.properties`
```properties
name=local-file-source
connector.class=FileStreamSource
tasks.max=1
file=test.txt
topic=connect-test
```
3. 파라미터로 커넥트 설정 파일과 커넥터 설정 파일을 차례로 넣는다.
```powershell
> bin/windows/connect-standalone.bat config/connect-standalone.properties config/connect-file-source.properties
```

<br>

## 분산 모드 커넥트 실행하기
1. 커넥트 파일 설정 : `connect-distributed.properties`
```properties
bootstrap.servers=localhost:9092
group.id=connect-cluster # 동일한 클러스터에 묶인 커넥트들은 같은 groupId 를 가짐.

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter.schemas.enable=true

# 그룹id별로 다르게 미리 생성 및 자동 생성되도록 설정해야 한다. 
# 커넥트가 재시작되더라도 데이터는 남아있다.
offset.storage.topic=connect-offsets
offset.storage.replication.factor=1
config.storage.topic=connect-configs
config.storage.replication.factor=1
status.storage.topic=connect-status
status.storage.replication.factor=1

offset.flush.interval.ms=10000

# plugin.path=/usr/local/share/java,/usr/local/share/kafka/plugins,/opt/connectors,
```
2. 실행하기
```powershell
> bin/windows/connect-distributed.bat config/connect-distributed.properties
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)