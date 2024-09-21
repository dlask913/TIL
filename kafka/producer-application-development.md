# 카프카 프로듀서 애플리케이션 개발
> 프로듀서 애플리케이션 기본 동작, 메시지 키 지정, 파티션 번호 지정, 커스텀 파티셔너, 레코드 전송 결과 확인, 리소스 정리 <br>
> ( kafka-clients:2.5.0 라이브러리 기준 )

<br> 

## 프로듀서 애플리케이션 기본 동작
-  필수 옵션을 지정하고 프로듀서 인스턴스를 생성하여 레코드를 전송한다. 
```java
public class SimpleProducer {  
    private final static String TOPIC_NAME = "test"; // 토픽 이름  
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";  
  
    public static void main(String[] args) {  
        Properties configs = new Properties();  
        // 3 개의 필수 옵션 ( bootstrap server, key 직렬화 옵션, value 직렬화 옵션 )        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);  
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  
  
        // 직렬화 옵션에 맞게 producer 인스턴스 생성  
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);  
  
        String messageValue = "testMessage";  
  
        /** ProducerRecord → send() → Partitioner → Accumulator **/  
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, messageValue); // 프로듀서 레코드 생성 (토픽에 메시지 전송 준비)  
        producer.send(record); // 레코드 비동기적으로 전송 (Kafka 브로커에 전송)  
        producer.flush(); // (선택) Accumulator 에 있는 모든 레코드를 강제로 전송  
        producer.close(); // 프로듀서 리소스 정리 및 종료  
    }  
}
```
### 실행 결과 ( kafka-console-consumer )
```powershell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning

# testMessage 출력 확인
```

<br> 

## 메시지 키를 가진 레코드를 전송하는 프로듀서
- ProducerRecord 생성 시 파라미터로 키를 추가한다. 
- 토픽 이름, 메시지 키, 메시지 값을 순서대로 파라미터로 넣고 생성하면 메시지 키가 지정된다.
```java
// 키를 포함하여 레코드 생성 ( 토픽, 메시지 키, 메시지 값 순서 )
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Pangyo", "Pangyo");  
producer.send(record);
ProducerRecord<String, String> record2 = new ProducerRecord<>(TOPIC_NAME, "Busan", "Busan");  
producer.send(record2);
```
### key-value 로 데이터 확인하기 ( kafka-console-consumer )
```powershell
> .\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --property print.key=true --property key.separator="-" --from-beginning

Busan-Busan
Pangyo-Pangyo
```

<br> 

## 파티션 번호를 지정한 프로듀서
- 토픽 이름, 파티션 번호, 메시지 키, 메시지 값을 순서대로 넣고 레코드를 생성하여 파티션을 직접 지정할 수 있다. 
- 파티션 번호는 토픽에 존재하는 파티션 번호로 설정해야 한다. 
```java
// 파티션 번호를 포함하여 레코드 생성 ( 토픽, 파티션 번호, 키, 값 순서 )
int partitionNo = 0; // 0 번 파티션으로 지정
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, partitionNo, "Pangyo", "Pangyo");  
producer.send(record);
```

<br> 

## 커스텀 파티셔너를 가지는 프로듀서
- 파티션이 여러 개 있을 때, 특정 데이터를 가지는 레코드를 특정 파티션으로 보내야 하는 경우 사용자 정의 파티셔너를 사용할 수 있다. 
- 토픽의 파티션 개수가 변경되더라도 특정 메시지 키를 가진 데이터는 특정 파티션 번호에 적재될 수 있도록 한다. 
- 기본 설정 파티셔너를 사용할 경우 메시지 키의 해시값을 파티션에 매칭하여 전송하므로 어느 파티션에 들어가는 지 알 수 없다.
```java
Properties configs = new Properties();  
configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);  
configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());  
configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

/** 커스텀 파티셔너 옵션 추가  **/
configs.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class);  

KafkaProducer<String, String> producer = new KafkaProducer<>(configs);  
```
### CustomPartitioner
```java
public class CustomPartitioner implements Partitioner {  
    @Override  
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {  
  
        if (keyBytes == null) {  
            throw new InvalidRecordException("Need message key");  
        }  
        if (((String)key).equals("Pangyo")) // 해당 키가 Pangyo 라면, 
            return 0; // 0 번 파티션에 적재한다.

        // 그 외의 키는 해시값으로 변환하여, 데이터를 전송하도록 한다.
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);  
        int numPartitions = partitions.size();  
        return Utils.toPositive(Utils.murmur2(keyBytes)) % numPartitions;  
    }  
  
    @Override  
    public void configure(Map<String, ?> configs) {}  
  
    @Override  
    public void close() {}  
}
```

<br> 

## 레코드의 전송 결과를 확인하는 프로듀서
- KafkaProducer 의 send() 는 Future 객체를 반환하는데, 이 객체는 RecordMetadata 의 비동기 결과를 표현한다. ( 레코드가 브로커에 정상적으로 적재되었는 지 )
- Future 객체의 get() 을 사용하면 프로듀서로 보낸 데이터의 결과를 동기적으로 가져올 수 있다. 
- 일반적으로 Exception 일 때만 로그로 확인한다.
```java
ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, "Pangyo", "Pangyo");  
try {  
    RecordMetadata metadata = producer.send(record).get(); // 전송 결과 데이터
    logger.info(metadata.toString()); // 데이터 로그로 확인 가능 
} catch (Exception e) {  
    logger.error(e.getMessage(),e);  
} finally {  
    producer.flush();  
    producer.close();  
}
```
### 실행 결과 ( 로그 확인 ) 
```log
[main] INFO com.example.ProducerWithSyncCallback - test-7@2
```
- 데이터가 test 토픽 7번 파티션의 2번 오프셋에 저장되었음을 알 수 있다. 
- acks=1 로 설정되어있기 때문에 리더 파티션에 데이터가 전송되었음을 알 수 있다. 
- acks 옵션을 0으로 설정하게 되면 오프셋을 -1 ( 존재하지 않는 값 ) 로 출력한다. 

<br> 

## 프로듀서의 안전한 종료
- close() 를 사용하여 Accumulator 에 저장되어 있는 모든 데이터를 카프카 클러스터로 전송해야 한다. 
- flush() 도 함께 사용하면 좋다. 
```java
producer.flush();
producer.close();
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)