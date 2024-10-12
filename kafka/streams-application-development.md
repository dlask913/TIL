# 카프카 스트림즈 애플리케이션 개발
> 필터링 스트림즈 애플리케이션, KStream & KTable 조인 스트림즈 애플리케이션, KStream & GlobalKTable 조인 스트림즈 애플리케이션 <br>
> kafka-streams:2.5.0 라이브러리 기준

<br>

## 필터링 스트림즈 애플리케이션 ( 기본 동작 )
> stream_log 토픽 → 소스 프로세서 : stream() → 스트림 프로세서 : filter() → 싱크 프로세서 : to() → stream_log_filter 토픽 (새로운 토픽) 에 저장

- 메시지 키 또는 메시지 값을 필터링하여 특정 조건에 맞는 데이터를 골라내고 싶을 때 filter() 메서드를 사용할 수 있다. 
- filter() 는 스트림즈 DSL 에서 사용 가능한 필터링 스트림 프로세서이다. 

### 실습
#### 1. 토픽 생성
```powershell
> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --topic stream_log
> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --topic stream_log_filter
```
#### 2. 스트림즈 애플리케이션 실행
```java
public class StreamsFilter {
    private static String APPLICATION_NAME = "streams-filter-application";  
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";  
    private static String STREAM_LOG = "stream_log";  
    private static String STREAM_LOG_FILTER = "stream_log_filter";
  
    public static void main(String[] args) {
        Properties props = new Properties();  
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);  
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);  
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());  
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());  

        StreamsBuilder builder = new StreamsBuilder();
        // 1. 소스 프로세서로 스트림 데이터를 가져온다.
        KStream<String, String> streamLog = builder.stream(STREAM_LOG);
        // 2. 스트림 프로세서로 필터링하기 : 메시지 값의 길이가 5 초과인 경우 필터링하여 새로운 스트림으로 저장
        KStream<String, String> filteredStream = streamLog.filter(  
                (key, value) -> value.length() > 5);
        // 3. to() 를 통해 새로운 토픽에 저장
        filteredStream.to(STREAM_LOG_FILTER);  
  
        KafkaStreams streams;
        streams = new KafkaStreams(builder.build(), props);  
        streams.start(); // 스트림즈 실행
    }  
}
```

#### 3. 데이터 삽입 및 확인
- 데이터 삽입 ( stream_log 토픽 )
```powershell
> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic stream_log
> 0
> 123456
```
- 데이터 확인 ( stream_log_filter 토픽 )
```powershell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic stream_log_filter
> 123456 # 필터링된 데이터 출력 확인
```

<br>

## KStream & KTable 조인 스트림즈 애플리케이션
> 소스 프로세서 : stream(), table() → 스트림 프로세서 : join() → 싱크 프로세서 : to()
- KTable 과 KStream 은 메시지 키를 기준으로 조인할 수 있다. ( join() )
- 대부분 데이터베이스는 정적으로 저장된 데이터를 조인하여 사용했지만 카프카에서는 **실시간으로 들어오는 데이터**들을 조인할 수 있다.
- 사용자의 이벤트 데이터를 데이터베이스에 저장하지 않고도 조인하여 스트리밍 처리할 수 있다는 장점이 있다. → 이벤트 기반 스트리밍 데이터 파이프라인 구성 가능
### KTable & Kstream join() 예제
> KTable 은 동일한 메시지 키에 대해 가장 마지막의 레코드 데이터를 보여주기 때문에 최신 데이터로 join() 이 가능하다. → 실시간 처리

1. KStream 데이터 - "userA":"iPhone", "userB":"Galaxy" / KTable 데이터 - "userA":"Seoul", "userB":"Busan"
2. join()
3. KStream 데이터 - "userA":"iPhone to Seoul", "userB": "Galaxy to Busan"
### 실습
#### 1. 스트림 데이터 join 을 위한 토픽 생성
```powershell
> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --partitions 3 --topic address # for KTable

> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --partitions 3 --topic order # for KStream

> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --partitions 3 --topic order_join # for join() 완료된 데이터 저장
```

#### 2. 스트림즈 애플리케이션 실행
```java
public class KStreamJoinKTable {  
    private static String APPLICATION_NAME = "order-join-application";  
    private static String BOOTSTRAP_SERVERS = "my-kafka:9092";  
    private static String ADDRESS_TABLE = "address";  
    private static String ORDER_STREAM = "order";  
    private static String ORDER_JOIN_STREAM = "order_join";  
  
    public static void main(String[] args) {  
        Properties props = new Properties();  
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_NAME);  
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);  
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());  
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());  
  
        StreamsBuilder builder = new StreamsBuilder();  
        // 1. KTable 과 KStream 소스 프로세서 생성
        KTable<String, String> addressTable = builder.table(ADDRESS_TABLE);
        KStream<String, String> orderStream = builder.stream(ORDER_STREAM);
  
        // 2. join() : KStream.join(KTable, (KStream 값, KTable 값 ) -> .. ).to(데이터 저장할 토픽) → 키가 같은 경우에만 조인 가능
        orderStream.join(addressTable, (order, address) -> order + " send to " + address).to(ORDER_JOIN_STREAM);  
  
        KafkaStreams streams;  
        streams = new KafkaStreams(builder.build(), props);  
        streams.start();  
    }  
}
```

#### 3. KTable 및 KStream 용도의 address, order 토픽 데이터 추가 
```powershell
> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic address --property "parse.key=true" --property "key.separator=:"
> userA:Seoul
> userB:Busan

> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
> userA:iPhone
> userB:Galaxy
```

#### 4. join() 결과 확인
```powershell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic order_join --property "print.key=true" --property "key.separator=:" --from-beginning
# 실행 결과
userA:iPhone send to Seoul
userB:Galaxy send to Busan
```

<br>

## KStream & GlobalKTable 조인 스트림즈 애플리케이션
> ※ 코파티셔닝되지 않은 데이터를 조인하는 경우
> 1. 리파티셔닝을 수행한 이후에 코파티셔닝이 된 상태로 join()
> 2. Ktable 로 사용하는 토픽을 GlobalKTable 로 선언하여 사용

- 리파티셔닝은 토픽의 개수를 맞추기 위해 토픽 생성 및 데이터 중복 추가가 필요하기 때문에 데이터가 많지 않은 경우 GlobalKTable 을 선언하여 사용한다. 

### 실습
#### 1. 코파티셔닝 되어있지 않은 address_v2 토픽 생성
- 위에서 생성한 order topic 과 코파티셔닝되지 않게 하기 위해 새로운 address_v2 토픽의 파티션 개수를 2로 설정하여 생성한다.
```powershell
> .\bin\windows\kafka-topics.bat --create --bootstrap-server my-kafka:9092 --partitions 2 --topic address_v2 # for KTable
```

#### 2. 스트림즈 애플리케이션 실행
```java
public class KStreamJoinGlobalKTable {  
    ..
    private static String ADDRESS_GLOBAL_TABLE = "address_v2";
    public static void main(String[] args) {  
  
        StreamsBuilder builder = new StreamsBuilder();  
        // 1. GlobalKTable 로 생성
        GlobalKTable<String, String> addressGlobalTable = builder.globalTable(ADDRESS_GLOBAL_TABLE);  
        KStream<String, String> orderStream = builder.stream(ORDER_STREAM);  
  
        /* 2. join() : 
        KStream.join(GlobalKTable,
				   (KStream key, Kstream value) -> key or value 어느 기준으로 조인할 지 키 지정,
				   (위에서 선택한 키, GlobalKTable value) -> ..)
			.to(저장할 토픽);
		**/
        orderStream.join(addressGlobalTable,  
                        (orderKey, orderValue) -> orderKey,  
                        (order, address) -> order + " send to " + address)  
                .to(ORDER_JOIN_STREAM);  
  
        KafkaStreams streams;  
        streams = new KafkaStreams(builder.build(), props);  
        streams.start();  
    }  
}
```

#### 3. GlobalKTable 및 KStream 용도의 address_v2, order 토픽에 데이터 추가
```powershell
> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic address_v2 --property "parse.key=true" --property "key.separator=:"
> userA:Jeju
> userB:Incheon

> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic order --property "parse.key=true" --property "key.separator=:"
> userA:iPhone
> userB:Galaxy
```

#### 4. join() 결과 확인
```powershell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server my-kafka:9092 --topic order_join --property "print.key=true" --property "key.separator=:" --from-beginning
# 실행 결과 ( 이전 데이터 제외 )
userA:iPhone send to Jeju
userB:Galaxy send to Incheon
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)