## (devops) 로그 수집
> LogStash 를 통해 Elasticsearch 로 로그 남기기

<br>

## 로그 수집
- 보통 서비스에서 Logback 을 통해 파일로 로그를 남기고 개발자는 ssh 접속을 통해 로그를 확인한다. 그런데 이 서버가 늘어날수록 로그를 확인하는 것이 비효율적
- 따라서 각각의 서버에서 발생한 로그들을 중앙화된 저장소에 모아서 관리하는 게 필요 → 로그 수집 → Elasticsearch 활용

<br>

### Logstash 를 통해 Elasticsearch 로 로그 남기기
#### 1. Elasticsearch 컨테이너 실행
```bash
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "xpack.security.http.ssl.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.10.0
```

#### 2. Logstash 의존성 및 Appender 추가 ( Spring App )
- 의존성 추가 ( pom.xml )
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```
- Appender 추가 ( logback.xml )
```xml
<configuration>  
    <!-- Logstash로 전송할 Appender -->    
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">  
        <destination>localhost:5044</destination>  
        <encoder class="net.logstash.logback.encoder.LogstashEncoder" />  
    </appender>
 
   <!-- Logger 설정 -->  
    <root level="info">
        <appender-ref ref="LOGSTASH" />  
    </root>
</configuration>
```

#### 3. Logstash 실행 파일 설정
- Spring App src 하위에 `logstash.conf` 생성
- input : logstash 로 어떤 입력들이 들어오는가를 정의
- output : input 으로 들어온 입력들을 내보낼 output 정의
```conf
input {
    tcp {
        port => 5044
        codec => json
    }
}

output {
    elasticsearch {
        hosts => ["http://elasticsearch:9200"]
        index => "application-logs-%{+YYYY.MM.dd}"
    }
}
```

#### 4. Logstash 실행 ( window os 기준 )
```bash
docker run -d --name logstash -p 5044:5044 -p 9600:9600 -v .\logstash.conf:/usr/share/logstash/pipeline/logstash.conf docker.elastic.co/logstash/logstash:8.10.0
```

#### 5. 컨테이너 간 통신을 위한 도커 네트워크 생성
- logstash 와 elasticsearch 간 통신을 위해 같은 네트워크로 묶어야 함
```bash
docker network create elastic-network
docker network connect elastic-network elasticsearch
docker network connect elastic-network logstash
```

<br>

## 참고
[개발자에게 필요한 로그 관리](https://inf.run/WgepX)