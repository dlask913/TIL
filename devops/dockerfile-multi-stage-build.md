## (docker) 빌드 컨텍스트와 Dockerfile, 멀티 스테이지 빌드
> 빌드 컨텍스트와 Dockerfile, 멀티 스테이지 빌드 (Multi-Stage Build)

<br>

## 빌드 컨텍스트
: 이미지를 빌드할 때 사용되는 폴더로, 빌드 명령 시 도커 데몬에게 도커 파일과 빌드에 사용되는 파일들을 전달해 주는 폴더를 말한다. 

1. 도커 빌드 명령을 사용하면, 빌드 컨텍스트가 도커 데몬에게 전달된다.
2. 위 컨텍스트 안에 있는 Dockerfile 로 도커 데몬이 이미지를 빌드하여, FROM 의 베이스 이미지로 임시 컨테이너를 생성한다.
3. Dockerfile 에서 COPY 지시어를 사용하면 빌드 컨텍스트에 있는 파일이 빌드에 사용되는 컨테이너로 복사된다. ( 도커 데몬은 빌드 컨텍스트에 있는 파일만 COPY 명령으로 복사 가능 )

```shell
$ docker build -t my-image-name . # . (현재 위치) 를 빌드 컨텍스트로 지정
```

이미지 빌드에 필요하지 않은 파일의 경우 .dockerignore 파일로 관리할 수 있다. 특히 크기가 큰 파일의 경우 도커 데몬에 전달하는 것은 비효율적이기 때문에 ignore 파일에 이름을 적어 제외할 수 있다. 

<br>

## Dockerfile 지시어
### 빌드 종류 
-  애플리케이션 빌드
: 소스코드를 실행 가능한 프로그램으로 빌드 ( Source Code → Application ) = 애플리케이션 프로그램, 아티팩트
- 이미지 빌드 ( ⊃ 애플리케이션 빌드 )
: 도커파일을 이미지로 빌드 ( Dockerfile → Image )

### Dockerfile 지시어
- *FROM 이미지명* : 베이스 이미지를 지정
- *COPY 빌드-컨텍스트-경로 레이어-경로* : 빌드 컨텍스트의 파일을 레이어에 복사 ( 새로운 레이어 추가 )
- *RUN 명령어* : 명령어 실행 ( 새로운 레이어 추가 )
- *CMD ["명령어"]* : 컨테이너 실행 시 명령어 지정
- *WORKDIR 폴더명* : 작업 디렉토리를 지정 ≒ cd ( 새로운 레이어 추가, FROM 바로 다음에 작성하는 것이 좋음 )
- *USER 유저명* : 명령을 실행할 사용자 변경 ≒ su ( 새로운 레이어 추가 )
- *EXPOSE 포트번호* : 컨테이너가 사용할 포트를 명시
- *ARG 변수명 변수값* : 이미지 **빌드 시점**의 환경 변수 설정 ( *docker build --build-arg 변수명=변수값* 으로 덮어쓰기 가능 )
- *ENV 변수명 변수값* : 이미지 **빌드 및 컨테이너 실행 시점**의 환경 변수 설정 ( 새로운 레이어 추가, *docker run -e 변수명=변수값* 으로 덮어쓰기 가능 )
- *ENTRYPOINT ["명령어"]* : 고정된 명령어를 지정. 의도하지 않은 동작을 1차적으로 방지할 수 있다.

<br>

## 멀티 스테이지 빌드 (Multi-Stage Build)
- **단일 스테이지 빌드** : 실행 단계에 불필요한 소스코드, 애플리케이션 빌드 시 다운받았던 라이브러리, 실행 파일, Maven 도구 등이 포함되어있기 때문에 용량을 많이 차지한다.
- **멀티 스테이지 빌드** : 도커 파일에서 두 개의 베이스 이미지를 활용하는 방법으로, 실제로 애플리케이션이 실행되는 데 사용하지 않는 파일을 분리하여 빌드에 사용하는 이미지와 실행에 사용하는 이미지로 나누어 실제 애플리케이션이 실행되는 이미지의 크기를 줄일 수 있다. 

### Spring Boot Application 이미지 구성
1. OS 구성 및 Java Runtime 설치 - 애플리케이션 **실행** 과정
2. 빌드 도구 설치 ( mvn ) - 애플리케이션 **빌드** 과정
3. 소스 코드 다운로드 ( git clone .. ) - 애플리케이션 **빌드** 과정
4. 의존성 라이브러리 설치 및 빌드 ( mvn clean package ) - 애플리케이션 **빌드** 과정
5. 애플리케이션 실행 ( java -jar app.jar ) - 애플리케이션 **실행** 과정

- 애플리케이션 빌드용 Dockerfile 예시 ( 빌드 스테이지 )
```Dockerfile
FROM maven:3.6 AS build 

COPY pom.xml .
COPY src ./src

RUN mvn clean package
```
- 애플리케이션 실행용 Dockerfile 예시 ( 실행 스테이지 )
```Dockerfile
FROM openjdk:11-jre-slim

CMD ["java", "-jar", "app.jar"]
```
- 통합
```Dockerfile
# 1. 빌드 환경 설정
FROM maven:3.6-jdk-11
WORKDIR /app
  
# pom.xml 과 src/ 디렉토리 복사
COPY pom.xml .
COPY src ./src
  
# 애플리케이션 빌드
RUN mvn clean package
  
# 2. 실행 환경 설정
FROM openjdk:11-jre-slim
WORKDIR /app
  
# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/target/*.jar ./app.jar
  
# 애플리케이션 실행
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

- 실행 결과 ( 크기 비교 ) : 741MB → 241MB
![image](https://github.com/user-attachments/assets/2e41c2bc-b68f-4c56-a7f1-be7e72a99de7)

<br>

## Docker 명령어
- docker build **-f 빌드에-사용할-도커파일명** -t 이미지명 Dockerfile-경로 : 도커파일명이 Dockerfile 이 아닌 경우 별도 지정

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 
