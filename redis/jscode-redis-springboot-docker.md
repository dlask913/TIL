## (redis) Docker Compose 로 Redis, Spring Boot 띄우기
> 로컬 및 AWS 환경에서 Docker Compose 로 Redis, Spring Boot 실행하기

<br>

## 로컬에서 Docker Compose 로 Redis, Spring Boot 실행하기

1. Spring Boot + Redis 프로젝트 가장 상위에 Dockerfile 생성
```Dockerfile
FROM openjdk:17-jdk  
  
COPY build/libs/*SNAPSHOT.jar app.jar  
  
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

2. 같은 위치에 compose.yml 생성
```yaml
services:  
  api-server:  
    build: . # 도커파일의 경로  
    ports: # 포트 매핑  
      - 8080:8080  
    depends_on:  
      cache-server: # 캐시 서버 실행 된 후에 서비스 애플리케이션 실행되도록 설정  
        condition: service_healthy  
  cache-server:  
    image: redis  
    ports:  
      - 6379:6379  
    healthcheck: # cache-server 가 실행되었다는 조건 설정  
      test: ["CMD", "redis-cli", "ping"]  
      interval: 5s  
      retries: 10
```

3. application.yml  수정
> 컨테이너 내 mysql 설치가 안되어있기 때문에 변경

```yaml
spring:  
  profiles:  
    default: local  
  datasource:  
    url: jdbc:mysql://host.docker.internal:3306/mydb # ★ localhost 수정
    username: root  
    password: 1234  
    driver-class-name: com.mysql.cj.jdbc.Driver  
  jpa:  
    hibernate:  
      ddl-auto: update  
    show-sql: true  
  data:  
    redis:  
      host: cache-server  # ★ compose.yml 에서 정의한 service 명으로 변경
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
    url: jdbc:mysql://{AWS RDS 엔드포인트 주소}/mydb  
    username:   
    password: 
```

4. 애플리케이션 빌드
```shell
./gradlew clean build -x test
```

5. docker compose 를 사용하여 컨테이너 빌드 및 백그라운드로 실행
```shell
docker compose up --build -d 
```

6. 실행 확인
```shell
docker ps # spring boot 및 redis 컨테이너 2개 확인

docker compose logs -f # 실시간 로그 조회
```

<br>

## AWS 에서 Docker Compose 로 Redis, Spring Boot  실행하기
> 로컬과 운영 환경의 설정이 다르기 때문에 파일을 분리한다.

1. Dockerfile-prod 작성
```yml
FROM openjdk:17-jdk  
  
COPY build/libs/*SNAPSHOT.jar app.jar  
  
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app.jar"]
```

2. compose-prod.yml 작성
```yaml
services:  
  api-server:  
    build:  
      context: .  
      dockerfile: ./Dockerfile-prod  # 파일 명시
    ports:  
      - 8080:8080  
    depends_on:  
      cache-server:  
        condition: service_healthy  
  cache-server:  
    image: redis  
    ports:  
      - 6379:6379  
    healthcheck:  
      test: ["CMD", "redis-cli", "ping"]  
      interval: 5s  
      retries: 10
```

3. git push & pull
```shell
# 로컬에서 변경 사항을 push 하고 EC2 인스턴스 콘솔로 접근하여 pull
$ git pull origin main
```

4. EC2 인스턴스에서 도커 설치
```shell
$ sudo apt-get update && \ sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common && \ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add - && \ sudo apt-key fingerprint 0EBFCD88 && \ sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" && \ sudo apt-get update && \ sudo apt-get install -y docker-ce && \ sudo usermod -aG docker ubuntu && \ newgrp docker && \ sudo curl -L "https://github.com/docker/compose/releases/download/2.27.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose && \ sudo chmod +x /usr/local/bin/docker-compose && \ sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

$ docker -v # 설치 확인
```

5. 기존에 실행 중인 redis 가 있다면 종료 
```shell
$ sudo systemctl stop redis

$ sudo systemctl status redis # Acive 상태가 inactive 인 지 확인
```

6. 애플리케이션 빌드 
```shell
$ ./gradlew cleans build -x test
```

7. compose 파일명 지정하여 백그라운드 실행 ( `compose-prod.yml` )
```shell
$ docker compose -f compose-prod.yml up --build -d
```

6. 실행 확인 
```shell
docker ps # spring boot 및 redis 컨테이너 2개 확인

docker compose logs -f # 실시간 로그 조회 → API 요청 시 캐시 응답 확인
```

<br>

## 참고
[인프런 - 비전공자도 이해할 수 있는 Redis 입문/실전 (조회 성능 최적화편)](https://inf.run/Pupon)