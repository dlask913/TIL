## (docker) 도커 컴포즈
> 도커 컴포즈, 도커 컴포즈 실습 ( 단일 애플리케이션 및 이중화 DB, 공통 변수 활용 )

<br>

## Docker Compose
- 도커 컴포즈는 여러 개의 Docker 컨테이너들을 하나의 파일 ( `docker-compose.yml` ) 에 정의하고 복잡한 애플리케이션 구조를 파일로 관리할 수 있다. 
- 도커 데스크탑 설치 시 기본으로 설치된다. 
- 한 번의 명령어로 여러 개의 컨테이너를 한 번에 실행하거나 종료할 수 있다. 
- 로컬 개발 환경에서 활용하기 편리하다. 

### YML
- 도커 컴포즈를 통해 관리할 컨테이너(서비스) 를 `docker-compose.yml` 에 정의한다. 
- yml 은 JSON 과 같이 데이터를 작성하기 위한 양식으로, JSON 과 유사하지만 간결하고 가독성이 더 뛰어나다. 
- JSON 은 {} 와 "", 가 필수인 반면 YAML 은 띄어쓰기 기반으로 정보를 구분한다. 
```yml
name: Gildong
age: 10
hobbies:
  - reading
  - coding
address:
  street: Main St
  city: Anytown
```

### Docker Compose 명령어
- `docker compose up -d` : YAML 파일에 정의된 서비스 생성 및 시작 ( 백그라운드 )
- `docker compose ps` : 현재 실행중인 서비스 상태 표시
- `docker compose build` : 현재 실행중인 서비스의 이미지만 빌드
- `docker compose logs` : 실행 중인 서비스의 로그 표시
- `docker compose down` : YAML 파일에 정의된 서비스 종료 및 제거
- `docker compose up -d --build` : 로컬에 이미지가 있어도 다시 이미지 빌드
- `docker compose down -v` : YAML 파일에 정의된 서비스 볼륨까지 제거

<br>

## Docker Compose 실습 1
> hitchecker 애플리케이션 활용

- hitchecker 는 접속 시도 횟수를 외부 캐시 저장소인 Redis 에 저장한다.
- hidchecker 가 종료 또는 재생성 되어도 Redis 에 접속 시도 횟수가 저장되어 있다. 
- docker compose 를 사용해서 한 번에 hitchecker 와 Redis 서버를 구성한다. 

#### 1. docker-compose.yml
```yml
version: '3' # API 버전 정의
services: # 실행할 서비스들 정의
  hitchecker: # 사용자가 개발한 애플리케이션이기 때문에 실행 시 빌드 필요
    build: ./app # Dockerfile 이 있는 경로
    image: hitchecker:1.0.0 # 있는 경우 그대로 사용하고 없다면 build 경로의 Dockerfile 을 통해 이미지 빌드
    ports:
      - "5000:5000"
  redis: # 외부의 이미지를 다운받아 실행
    image: "redis:alpine"
```

#### 2. 이미지 빌드
```bash
$ docker compose build
```
- hitchecker 이미지를 조회하여 확인
```bash
$ docker image ls hitchecker
```

#### 3. 컨테이너 데몬으로 실행
```bash
$ docker compose up -d
```
*충돌이 나는 경우 포트 포워딩의 앞 부분을 사용하지 않는 포트로 수정*

#### 4. localhost:5000 접속하여 동작 확인 및 로그 확인
```bash
$ docker compose logs

07hitchecker-hitchecker-1  | 172.20.0.1 - - [03/Nov/2024 07:22:59] "GET / HTTP/1.1" 200 -
07hitchecker-hitchecker-1  | 172.20.0.1 - - [03/Nov/2024 07:22:59] "GET / HTTP/1.1" 200 -
07hitchecker-hitchecker-1  | 172.20.0.1 - - [03/Nov/2024 07:22:59] "GET / HTTP/1.1" 200 -
07hitchecker-hitchecker-1  | 172.20.0.1 - - [03/Nov/2024 07:22:59] "GET / HTTP/1.1" 200 -
07hitchecker-hitchecker-1  | 172.20.0.1 - - [03/Nov/2024 07:23:00] "GET / HTTP/1.1" 200 -
07hitchecker-hitchecker-1  | 172.20.0.1 - - [03/Nov/2024 07:23:00] "GET / HTTP/1.1" 200 -

$ docker ps

CONTAINER ID   IMAGE              COMMAND                   CREATED         STATUS         PORTS                    NAMES
ac35aba143a0   redis:alpine       "docker-entrypoint.s…"   2 minutes ago   Up 2 minutes   6379/tcp                 07hitchecker-redis-1
3b2281f5565f   hitchecker:1.0.0   "python app.py"           2 minutes ago   Up 2 minutes   0.0.0.0:5000->5000/tcp   07hitchecker-hitchecker-1
```

#### 5. 특정 컨테이너만 삭제된 경우
```bash
$ docker rm -f [삭제할 CONTAINER ID]
# hitchecker 애플리케이션 중지

$ docker compose up -d
[+] Running 2/2
 ✔ Container 07hitchecker-hitchecker-1  Started 0.1s 
 ✔ Container 07hitchecker-redis-1       Running    
# Running 상태의 Redis 는 그대로 있고, 중지된 hitchecker 가 실행된 것을 확인
```

<br>

## Docker Compose 실습 2
> 이중화 DB, 공통 변수 활용

#### 1. 이중화 DB 정의
```yml
version: '3'
services:
  postgres-primary-0:
    image: bitnami/postgresql-repmgr:15
    volumes:
      - postgres_primary_data:/bitnami/postgresql
    environment:
      POSTGRESQL_POSTGRES_PASSWORD: adminpassword # 공통
      POSTGRESQL_USERNAME: myuser # 공통
      POSTGRESQL_PASSWORD: mypassword # 공통
      POSTGRESQL_DATABASE: mydb # 공통
      REPMGR_PASSWORD: repmgrpassword # 공통
      REPMGR_PRIMARY_HOST: postgres-primary-0 # 공통
      REPMGR_PRIMARY_PORT: 5432 # 공통
      REPMGR_PARTNER_NODES: postgres-primary-0,postgres-standby-1:5432 # 공통
      REPMGR_NODE_NAME: postgres-primary-0
      REPMGR_NODE_NETWORK_NAME: postgres-primary-0
      REPMGR_PORT_NUMBER: 5432
  
  postgres-standby-1:
    image: bitnami/postgresql-repmgr:15
    volumes:
      - postgres_standby_data:/bitnami/postgresql
    environment:
      POSTGRESQL_POSTGRES_PASSWORD: adminpassword # 공통
      POSTGRESQL_USERNAME: myuser # 공통
      POSTGRESQL_PASSWORD: mypassword # 공통
      POSTGRESQL_DATABASE: mydb # 공통
      REPMGR_PASSWORD: repmgrpassword # 공통
      REPMGR_PRIMARY_HOST: postgres-primary-0 # 공통
      REPMGR_PRIMARY_PORT: 5432 # 공통
      REPMGR_PARTNER_NODES: postgres-primary-0,postgres-standby-1:5432 # 공통
      REPMGR_NODE_NAME: postgres-standby-1
      REPMGR_NODE_NETWORK_NAME: postgres-standby-1
      REPMGR_PORT_NUMBER: 5432
volumes:
  postgres_primary_data:
  postgres_standby_data:
```

#### 2. 공통변수 분리
```yml 
version: '3'
x-environment: &common_environment # 1. 여러 환경변수들의 변수그룹명 정의
  POSTGRESQL_POSTGRES_PASSWORD: adminpassword
  POSTGRESQL_USERNAME: myuser
  POSTGRESQL_PASSWORD: mypassword
  POSTGRESQL_DATABASE: mydb
  REPMGR_PASSWORD: repmgrpassword
  REPMGR_PRIMARY_HOST: postgres-primary-0
  REPMGR_PRIMARY_PORT: 5432
  REPMGR_PORT_NUMBER: 5432
  
services:
  postgres-primary-0:
    image: bitnami/postgresql-repmgr:15
    volumes:
      - postgres_primary_data:/bitnami/postgresql
    environment:
      <<: *common_environment # 2. 변수그룹 활용
      REPMGR_PARTNER_NODES: postgres-primary-0,postgres-standby-1:5432
      REPMGR_NODE_NAME: postgres-primary-0
      REPMGR_NODE_NETWORK_NAME: postgres-primary-0
  postgres-standby-1:
    image: bitnami/postgresql-repmgr:15
    volumes:
      - postgres_standby_data:/bitnami/postgresql
    environment:
      <<: *common_environment # 2. 변수그룹 활용
      REPMGR_PARTNER_NODES: postgres-primary-0,postgres-standby-1:5432
      REPMGR_NODE_NAME: postgres-standby-1
      REPMGR_NODE_NETWORK_NAME: postgres-standby-1
volumes:
  postgres_primary_data:
  postgres_standby_data:
```

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 