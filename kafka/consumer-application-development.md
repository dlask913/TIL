# 카프카 컨슈머 애플리케이션 개발
> 컨슈머 애플리케이션 기본 동작, 수동 커밋, 리밸런스 리스너, 파티션 명시적 할당, 리소스 정리 <br>
> ( kafka-clients:2.5.0 라이브러리 기준 )

<br> 

## 컨슈머 애플리케이션 기본 동작
- 필수 옵션을 지정하고 컨슈머 인스턴스를 생성하여 한 개 이상의 토픽을 구독하고 poll() 을 호출하여 토픽으로부터 메시지를 수신한다.
- poll() 을 너무 오랜 시간 동안 호출하지 않으면 리밸런싱이 일어난다. 
```java
public class SimpleConsumer {  
    private final static Logger logger = LoggerFactory.getLogger(SimpleConsumer.class);  
    private final static String TOPIC_NAME = "test"; // 토픽 이름
    private final static String BOOTSTRAP_SERVERS = "my-kafka:9092";  
    private final static String GROUP_ID = "test-group";  
  
    public static void main(String[] args) {  
        Properties configs = new Properties();  
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);  
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);  
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());  
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());  
  
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);  
  
        consumer.subscribe(Arrays.asList(TOPIC_NAME)); // 토픽 구독
  
        while (true) {  
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));  // 레코드 가져오기
            for (ConsumerRecord<String, String> record : records) {  // 레코드 순차 처리
                logger.info("record:{}", record);  
            }  
        }  
    }  
}
```
### 실행 결과 ( kafka-console-producer )
1. 프로듀서 콘솔을 실행하여 test 토픽으로 메시지를 보낸다.
```powershell
> .\bin\windows\kafka-console-producer.bat --bootstrap-server my-kafka:9092 --topic test
```
2. 컨슈머 콘솔에서 응답받은 레코드가 출력되는 것을 확인한다.
```console
[main] INFO com.example.SimpleConsumer - record:ConsumerRecord(topic = test, partition = 6, leaderEpoch = 0, offset = 0, CreateTime = 1727767904151, serialized key size = -1, serialized value size = 5, headers = RecordHeaders(headers = [], isReadOnly = false), key = null, value = hello)
```

<br>

## 수동 커밋하는 컨슈머

### 자동 커밋 컨슈머( default ) 
- enable.auto.commit=true 가 기본 옵션으로, Kafka 는 자동으로 오프셋을 커밋한다.
- auto.commit.interval.ms 로 제어된다.
- poll() 을 호출할 때마다 Kafka 는 주기적으로 컨슈머가 처리한 마지막 오프셋을 커밋한다.
```java
	..  
    public static void main(String[] args) {  
        ..
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true); // 디폴트 설정
        configs.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 60000);
  
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);  
        consumer.subscribe(Arrays.asList(TOPIC_NAME));  
  
        while (true) {  
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));  
            for (ConsumerRecord<String, String> record : records) {  
                logger.info("record:{}", record);  
            }  
        }  
    }  
```

### 1. 동기 오프셋 커밋 컨슈머 ( commitSync() )
- poll() 호출 이후 commitSync() 를 호출하여 오프셋 커밋을 명시적으로 수행할 수 있다. 
- commitSync() 은 poll() 로 가져온 메시지를 모두 처리한 후, 마지막으로 처리된 오프셋을 커밋한다. 이는 안전하게 메시지를 처리하고 커밋할 수 있지만 커밋이 완료될 때까지 기다리기 때문에 성능에 영향을 미칠 수 있다. 
- poll() 로 받은 모든 레코드의 처리가 끝난 이후 commitSync() 를 호출해야 한다.
```java
	..
    public static void main(String[] args) {
	    ..  
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // 자동 커밋 false
  
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);  
        consumer.subscribe(Arrays.asList(TOPIC_NAME));  
  
        while (true) {  
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));  
            for (ConsumerRecord<String, String> record : records) {  
                logger.info("record:{}", record);  
            }  
            consumer.commitSync(); // poll() → 레코드 데이터 처리 → commitSync()
        }  
    }  
```

### 2. 비동기 오프셋 커밋 컨슈머 ( commitAsync() )
- 비동기 커밋은 오프셋 커밋 요청을 비동기적으로 처리한다. 커밋 요청이 완료될 때까지 기다리지 않으므로 더 빠르게 메시지를 처리할 수 있다. 
- 특정 상황에서 커밋 실패가 발생할 수 있으므로 `offsetCommitCallback`을 사용하여 실패 시 처리할 로직을 구현하는 것이 중요하다. 
- commitAsync() 를 호출하여 사용한다.
```java
	..  
    public static void main(String[] args) {  
        ..
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  
  
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);  
        consumer.subscribe(Arrays.asList(TOPIC_NAME));  
  
        while (true) {  
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));  
            for (ConsumerRecord<String, String> record : records) {  
                logger.info("record:{}", record);  
            }
            consumer.commitAsync(new OffsetCommitCallback() { // 비동기 커밋
                public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {  
                    if (e != null) // 커밋이 실패한 경우
                        System.err.println("Commit failed");  
                    else  // 커밋이 성공한 경우
                        System.out.println("Commit succeeded");  
                    if (e != null)  // 커밋이 실패한 경우 예외 확인
                        logger.error("Commit failed for offsets {}", offsets, e);  
                }  
            });  
        }  
    }  
}
```

<br>

## 리밸런스 리스너를 가진 컨슈머
- 리밸런스 발생을 감지하기 위해 카프카 라이브러리는 `ConsumerRebalanceListener` 인터페이스를 지원한다.
- `ConsumerRebalanceListener` 로 구현된 클래스는 onPartitionAssigned() 와 onPartitionRevoked() 로 이루어져 있다.
- `onPartitionAssgined()` : 리밸런스가 끝난 뒤에 파티션이 할당 완료되면 호출딘다.
- `onPartitionRevoked()` : 리밸런스가 시작되기 직전에 호출된다. 마지막으로 처리한 레코드를 기준으로 커밋을 하기 위해서는 리밸런스가 시작하기 직전 커밋을 하면 되므로 이 메서드에 커밋을 구현하여 처리할 수 있다. 
### RebalanceListener
```java
public class RebalanceListener implements ConsumerRebalanceListener {  
    private final static Logger logger = LoggerFactory.getLogger(RebalanceListener.class);  
  
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {  
        logger.warn("Partitions are assigned : " + partitions.toString());    
    }  
  
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {  
        logger.warn("Partitions are revoked : " + partitions.toString());  
    }  
}
```
### ConsumerApplication ( main() )
```java
private static KafkaConsumer<String, String> consumer;
consumer = new KafkaConsumer<>(configs);  
 // subscribe 호출 시, 리밸런스 리스너 추가
consumer.subscribe(Arrays.asList(TOPIC_NAME), new RebalanceListener()); 
```
### 실행 결과
1. 위 ConsumerApplication 을 실행시키고 assgin 된 파티션을 콘솔 로그로 확인
```console
[main] WARN com.example.RebalanceListener - Partitions are assigned : [test-1, test-0, test-3, test-2, test-5, test-4, test-7, test-6, test-9, test-8]
```
2. Allow multiple instances 옵션을 추가하여 프로세스 하나 더 실행
![image](https://github.com/user-attachments/assets/a0bf05a6-c556-401c-8d6c-214f3f9fecf3)
3. 처음 실행한 ConsumerApplication 프로세스 콘솔 확인
```console
[main] WARN com.example.RebalanceListener - Partitions are revoked : [test-1, test-0, test-3, test-2, test-5, test-4, test-7, test-6, test-9, test-8]

[main] WARN com.example.RebalanceListener - Partitions are assigned : [test-1, test-0, test-3, test-2, test-4]
```
4. 다음으로 실행한 ConsumerApplication 프로세스 콘솔 확인
```console
[main] WARN com.example.RebalanceListener - Partitions are assigned : [test-5, test-7, test-6, test-9, test-8]
```

<br>

## 파티션 할당 컨슈머
- 특정 토픽에 대해 특정 파티션을 명시적으로 직접 할당할 수 있다. 
- 특정 파티션에서만 데이터를 읽어와야 하는 경우 주로 사용하고 많이 사용하는 방식은 아니다.
```java
private final static int PARTITION_NUMBER  = 0; // 할당할 파티션 번호

KafkaConsumer<String, String> consumer = new KafkaConsumer<>(configs);  
consumer.assign(Collections.singleton(new TopicPartition(TOPIC_NAME, PARTITION_NUMBER))); // assign() 을 통해 특정 토픽의 특정 파티션을 직접 할당

/** 레코드 처리 **/
```

<br>

## 컨슈머의 안전한 종료
- 정상적으로 종료되지 않은 컨슈머는 세션 타임아웃이 발생할 때까지 컨슈머 그룹에 남게 된다. 
- 컨슈머를 안전하게 종료하기 위해 wakeup() 을 실행하여 KafkaConsumer 인스턴스를 종료한다.
- wakeup() 이후 poll() 이 호출되면 WakeupException 이 발생하는데, 이 예외를 받은 뒤에 데이터 처리를 위해 사용한 자원들을 해제하면 된다. 

### 동작 순서
1. 애플리케이션이 실행 중일 때, 컨슈머는 메시지를 소비하면서 주기적으로 poll() 호출
2. 애플리케이션이 종료되면 ShutdownThread 가 실행
3. consumer.wakeup() 이 호출되어 컨슈머가 깨어나며 poll() 은 WakeupException 발생
4. 이후 컨슈머의 close() 를 호출하거나 종료 처리 로직을 수행 ( finally )

```java
public class ConsumerWithSyncOffsetCommit {  
    ..
    public static void main(String[] args) {  
	    // JVM이 종료될 때 실행할 작업을 정의 ( ShutdownThread 추가 )
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        ..
        consumer = new KafkaConsumer<>(configs);  
        consumer.subscribe(Arrays.asList(TOPIC_NAME));  
  
        try {  
            while (true) {  
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1)); // 애플리케이션 종료 시, WakeupException 발생
                for (ConsumerRecord<String, String> record : records) {  
                    logger.info("{}", record);  
                }  
                consumer.commitSync();  
            }  
        } catch (WakeupException e) {  
            logger.warn("Wakeup consumer");  
        } finally {  
            logger.warn("Consumer close");  
            consumer.close(); // WakeupException 이후 자원 해제
        }  
    }
    
	// ShutdownHook 스레드를 정의한 클래스로, run() 메서드에 종료 시 실행할 작업을 지정힌디.
    static class ShutdownThread extends Thread {  
        public void run() {  
            logger.info("Shutdown hook");  
            consumer.wakeup(); // KafkaConsumer 깨우기
        }  
    }  
}
```

<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)