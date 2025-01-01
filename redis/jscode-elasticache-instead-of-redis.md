## (redis) AWS 환경에서 ElastiCache 사용하기
> EC2 에서 Redis 를 쓰지 않고 ElastiCache 를 쓰는 이유, AWS ElastiCache 세팅하기, ElastiCache 사용하기

<br>

## EC2 에서 Redis 를 쓰지 않고 ElastiCache 를 쓰는 이유 
- Redis 를 쓰게 되면 수동 설치 및 설정, 클러스터 구성, 패치 및 유지보수를 직접 수행해야 하지만 ElastiCache 는 AWS 관리형 서비스로 패치, 복제, 장애 조치 등을 자동으로 처리해준다.
- EC2 에 redis 와 srping boot 를 같이 두면 리소스  경합으로 성능 저하가 발생할 수 있기 때문에 ElastiCache 는 EC2 인스턴스와 분리해서 인프라를 구성한다.

<br>

## AWS ElastiCache 세팅하기
-  Redis OSS 캐시 생성

### 1단계
- 구성 : 자체 캐시 설계 / 클러스터( 여러 캐시 서버(Node)를 이루는 한 단위의 그룹 ) 캐시
- 클러스터 모드 : 비활성화 ( 대규모 트래픽인 경우에만 활성화 )
- 위치 : AWS 클라우드, 테스트이기 때문에 다중 AZ 제거 ( 다중 AZ 의 경우 재난 상황이 잘 일어나지않는데 비용이 들기 때문에 불가피한 경우에 사용한다. )
- 클러스터 설정 : 노드 유형 - cache.t3.micro, 복제본 개수 - 0 (Failover 동작 X)으로 변경 ( 복제본 개수는 늘어날수록 비용이 늘어난다. )
- 연결 : 새 서브넷 그룹 생성 

### 2단계
- 보안 : 6379 포트를 허용하는 보안 그룹 생성하여 선택
> 0.0.0.0/0 으로 설정하더라도 같은 VPC 에 있는 IP 만 접근이 허용된다. ( EC2 인스턴스와 같아야 접근이 가능하다 )

![image](https://github.com/user-attachments/assets/d013cd4c-e221-4249-89b6-fd16a7b70026)

- 백업 : 옵션 해제 

<br>

### EC2 인스턴스에서 ElastiCache 접속 테스트
```shell
$ redis-cli -h {포트를 제외한 ElastiCache 기본 엔드포인트 주소}
> ping
#PONG
```

<br>


## ElastiCache 사용하기

#### 1. spring boot elastiCache 연결하기
- application.yml 수정
```yaml
spring:  
  profiles:  
    default: local  
  datasource:  
    url: jdbc:mysql://host.docker.internal:3306/mydb  
    username: root  
    password: 1234  
    driver-class-name: com.mysql.cj.jdbc.Driver  
  jpa:  
    hibernate:  
      ddl-auto: update  
    show-sql: true  
  data:  
    redis:  
      host: cache-server  
      port: 6379  
  
logging:  
  level:  
    org.springframework.cache: trace  
  
---  
spring:  
  config:  
    activates:  
      on-profile: prod  
  datasource:  
    url: jdbc:mysql://instagram-db.cwrugiklrcv4.ap-northeast-2.rds.amazonaws.com:3306/mydb  
    username: admin  
    password: password  
  data:  
    redis:  
      host: {포트 번호를 제외한 elasticCache 기본 엔드포인트 주소} # ★
      port: 6379
```


#### 2. git push & pull

```shell
# 로컬에서 변경 사항을 push 하고 EC2 인스턴스 콘솔로 접근하여 pull
$ git pull origin main
```

#### 3. 애플리케이션 빌드 및 실행

```shell
docker compose down # 실행중인 리소스가 있다면 종료

./gradlew build clean -x test # 애플리케이션 빌드

java -jar -Dspring.profiles.active=prod {}.jar # 애플리케이션 실행
```

#### 4. EC2 인스턴스에서 ElastiCache 에 접근

```shell
redis-cli -h {ElastiCache 엔드포인트 주소 }

> keys * # 캐시 데이터 조회
```

<br>

## 참고
[인프런 - 비전공자도 이해할 수 있는 Redis 입문/실전 (조회 성능 최적화편)](https://inf.run/Pupon)