# (redis) Spring Boot + Redis 실습
> Spring Boot 프로젝트 세팅, Redis 세팅 추가하기, Redis 적용하기 전후 성능 비교 (Postman)

<br>

## Spring Boot 프로젝트 세팅
### Spring Boot 
1. 시나리오를 구현하기 위한 의존성을 주입하여 Generate ( start.spring.io )
- Gradle - Groovy
- jdk 17
- Spring Boot 3.4.1 ( 3.x )
- Dependencies : Spring Boot DevTools, Spring Web, Spring Data JPA, MySQL Driver

2. application.yml 작성
```yml
spring:  
  profiles:  
    default: local  
  datasource:  
    url: jdbc:mysql://localhost:3306/mydb  
    username: root  
    password: 1234  
    driver-class-name: com.mysql.cj.jdbc.Driver  
  jpa:  
    hibernate:  
      ddl-auto: update  
    show-sql: true
```

3. Board Entity 에 대한 pagination 로직 추가
```java
..
public class BoardService {  
    private BoardRepository boardRepository;  // JPA Repository
	
    public List<Board> getBoards(int page, int size) {  
        Pageable pageable = PageRequest.of(page - 1, size);  
        Page<Board> pageOfBoards = boardRepository.findAllByOrderByCreatedAtDesc(pageable);  
        return pageOfBoards.getContent();  
    }  
}
```

### MySQL
1. mydb database 생성
```sql
CREATE DATABASE mydb default CHARACTER SET UTF8; 
-- SHOW DATABASES; 로 결과 확인
```

2. 더미 데이터를 추가하기 전에 높은 반복 횟수를 허용하도록 설정
```sql
SET SESSION cte_max_recursion_depth = 1000000; 
```

3. Board Entity 에 대한 더미 데이터 생성 ( sql 8.0 이상, [기본적인 Spring Boot 프로젝트 셋팅하기](https://inf.run/Pupon) )
> 더미 데이터 생성 전, 애플리케이션을 한 번 실행하여 DDL Script 가 실행되도록 한다.

```sql
-- USE mydb;
INSERT INTO boards (title, content, created_at)
WITH RECURSIVE cte (n) AS
(
  SELECT 1
  UNION ALL
  SELECT n + 1 FROM cte WHERE n < 1000000 -- 생성하고 싶은 더미 데이터의 개수
)
SELECT
    CONCAT('Title', LPAD(n, 7, '0')) AS title, 
    CONCAT('Content', LPAD(n, 7, '0')) AS content,
    TIMESTAMP(DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 3650 + 1) DAY) + INTERVAL FLOOR(RAND() * 86400) SECOND) AS created_at
FROM cte;
```

<br>

## Redis 세팅 추가하기

#### 1. build.gradle 내 redis 의존성 추가
```groovy
// Redis 의존성 추가  
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

#### 2. application.yml 내 redis 설정 및 cache logging 설정 추가
```yml
spring:
..
  data:  
    redis:  
      host: localhost  
      port: 6379

logging: # `org.springframework.cache` 에서 발생하는 로그를 trace 레벨로 출력
  level:  
    org.springframework.cache: trace
```

#### 3. Redis 연결을 위한 config/RedisConfig 클래스 생성
> Spring Data Redis 는 기본적으로 Lettuce 클라이언트를 사용하도록 설정

```java
@Configuration  
public class RedisConfig {  
    @Value("${spring.data.redis.host}")  
    private String host;  
    @Value("${spring.data.redis.port}")  
    private int port;  
  
    @Bean  
    public LettuceConnectionFactory redisConnectionFactory() {  
        // Lettuce 라이브러리를 활용해 Redis 연결을 관리하는 객체를 생성하고 Redis 서버에 대한 정보(host, port) 를 설정
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));  
    }
}
```

#### 4. Redis 를 캐시 저장소로 활용하기 위한 설정 추가
- CacheManager 내 RedisCacheConfiguration을 설정하여 Redis에 Key와 Value 저장 시의 직렬화 방식과 TTL을 정의할 수 있다.
```java
@Configuration  
@EnableCaching // Spring Boot 의 캐싱 설정 활성화  
public class RedisCacheConfig {  
    @Bean  
    public CacheManager boardCacheManager(RedisConnectionFactory redisConnectionFactory) {  
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration  
                .defaultCacheConfig()  
                .serializeKeysWith( // Key 를 저장할 때 String 으로 직렬화
                        RedisSerializationContext.SerializationPair.fromSerializer(  
                                new StringRedisSerializer()  
                        )  
                )  
                .serializeValuesWith( // Value 를 저장할 때 String 으로 직렬화
                        RedisSerializationContext.SerializationPair.fromSerializer(  
//                                new GenericJackson2JsonRedisSerializer() // Java 의 클래스 이름까지 저장  
                                new Jackson2JsonRedisSerializer<Object>(Object.class)  
                        )  
                )  
                .entryTtl(Duration.ofMinutes(1L)); // 데이터 만료기간(TTL) 설정  
  
        return RedisCacheManager  
                .RedisCacheManagerBuilder  
                .fromConnectionFactory(redisConnectionFactory)  
                .cacheDefaults(redisCacheConfiguration)  
                .build();  
    }  
}
```

#### 5. Pagination 로직에 CacheManger 적용
- `@Cacheable` : Cache Aside 전략으로 캐싱이 적용된다.
- 위에서 정의한 cacheManger 를 등록하여 캐시를 적용한다. 
```java
..
public class BoardService {  
    private BoardRepository boardRepository;  
    
    @Cacheable(cacheNames = "getBoards", key = "'boards:page:' + #page + ':size:' + #size", cacheManager = "boardCacheManager")  
    public List<Board> getBoards(int page, int size) {  
        Pageable pageable = PageRequest.of(page - 1, size);  
        Page<Board> pageOfBoards = boardRepository.findAllByOrderByCreatedAtDesc(pageable);  
        return pageOfBoards.getContent();  
    }
    
	/** 속성 값 설명  
	 * - cacheNames : 캐시 이름 설정  
	 * - key : Redis 에 저장할 key 이름 설정  
	 * - cacheManager : 사용할 cacheManager 의 Bean 이름 설정  
	 */
```

#### 6. 실행 후 로그 분석
> 데이터 만료 전, redis-cli 를 통해 key 와 value, TTL 모두 조회할 수 있다.

- Cache Miss 

a. API 요청 시 데이터가 없어, Cache Miss 가 발생한다.
```console
No cache entry for key 'boards:page:1:size:10' in cache(s) [getBoards]
```
b. 데이터 베이스를 조회한다.
```console
Hibernate: select b1_0.id,b1_0.content,b1_0.created_at,b1_0.title from boards b1_0 order by b1_0.created_at desc limit ?  

Hibernate: select count(b1_0.id) from boards b1_0
```
c. DB 로부터 조회해 온 결과를 캐시에 저장한다.
```console
Creating cache entry for key 'boards:page:1:size:10' in cache(s) [getBoards]
```

- Cache Hit

a. 캐시에 있는 데이터를 응답한다.
```console
Cache entry for key 'boards:page:1:size:10' found in cache(s) [getBoards]
```

<br>

## Redis 적용하기 전후 성능 비교 (Postman)
1. `@Cacheable` 을 제거하고 Postman 을 활용해 API 요청하기.
```java
public class BoardService {  
    private BoardRepository boardRepository;  
//    @Cacheable(cacheNames = "getBoards", key = "'boards:page:' + #page + ':size:' + #size", cacheManager = "boardCacheManager")  
    public List<Board> getBoards(int page, int size) {..}
}
```
- 실행 결과 : 10번 연달아 실행했을 때 평균적으로 430ms 정도
![image](https://github.com/user-attachments/assets/42916442-0826-486c-a057-18e09c5e1fca)

2.  `@Cacheable` 적용된 상태에서 API 요청하기
```java
public class BoardService {  
    private BoardRepository boardRepository;  
    @Cacheable(cacheNames = "getBoards", key = "'boards:page:' + #page + ':size:' + #size", cacheManager = "boardCacheManager")  
    public List<Board> getBoards(int page, int size) {..}
}
```
- 실행 결과 : 초기 실행 이후, 10번 연달아 실행했을 때 평균적으로 7ms 정도
![image](https://github.com/user-attachments/assets/6f717090-2dca-4b61-9c50-da942243edd7)

<br>

## 참고
[인프런 - 비전공자도 이해할 수 있는 Redis 입문/실전 (조회 성능 최적화편)](https://inf.run/Pupon)