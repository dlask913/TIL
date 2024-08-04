## (docker) 컨테이너 애플리케이션 구성 2
> 컨테이너 애플리케이션 구성 중 백엔드 및 프론트엔드 컨테이너 구성하기.

<br>

## 컨테이너 애플리케이션 구성하기
### [2] Spring Boot 백엔드 컨테이너 구성하기
1. OS 구성 및 Java Runtime 설치 - 실행 스테이지
2. 빌드 도구 설치 ( gradle ) - 빌드 스테이지
3. 소스코드 다운로드 - 빌드 스테이지
4. 의존성 라이브러리 설치 및 빌드 - 빌드 스테이지
5. 애플리케이션 실행 - 실행 스테이지

- 애플리케이션 빌드 스테이지 Dockerfile
```Dockerfile
FROM gradle:6.9.1 AS build

COPY src . 

RUN gradle clean build
```
- 애플리케이션 실행 스테이지 Dockerfile
```Dockerfile
FROM openjdk:11-jre-alpine

CMD ["java", "-jar", "애플리케이션명.jar"]
```
- 멀티 스테이지 Dockerfile
```Dockerfile
# 빌드 이미지로 OpenJDK 11 & Gradle 을 지정
FROM gradle:7.6.1-jdk11 AS build
  
# 소스코드를 복사할 작업 디렉토리 생성
WORKDIR /app
  
# 호스트 머신의 소스코드를 작업 디렉토리로 복사
COPY . /app
  
# Gradle 빌드를 실행하여 JAR 파일 생성
RUN gradle clean build --no-daemon
  
# 런타임 이미지로 OpenJDK 11 JRE-slim 지정
FROM openjdk:11-jre-slim
  
# 애플리케이션을 실행할 작업 디렉토리를 생성
WORKDIR /app
  
# 빌드 이미지에서 생성된 JAR 파일을 런타임 이미지로 복사
COPY --from=build /app/build/libs/*.jar /app/애플리케이션명.jar
  
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "애플리케이션명.jar"]
```
- 실행 결과
```bash
# 1. 빌드
$ docker build -t [레지스트리계정명]/[이미지명] .

# 2. 컨테이너 실행
$ docker run -d -p 8080:8080 -e DB_URL=[DB_URL] --name [컨테이너명] --network [네트워크명] [레지스트리계정명]/[이미지명]

# 3. 로그 확인
$ docker logs [컨테이너명]
```

<br>

### [3] Vue.js 프론트엔트 컨테이너 구성하기

1. OS 구성 및 Nginx 웹 서버 설치 - 실행 스테이지
2. 빌드 도구 설치 ( node.js, npm ) - 빌드 스테이지
3. 소스코드 다운로드 - 빌드 스테이지
4. 의존성 라이브러리 설치 및 빌드 - 빌드 스테이지
5. 빌드 결과 폴더를 /usr/share/nginx/html 폴더로 복사
6. 웹서버 실행 - 실행 스테이지

- 웹 서버 빌드 스테이지 Dockerfile
```Dockerfile
FROM node:14 AS build

COPY . .

RUN npm ci
RUN npm run build
```
- 웹 서버 실행 스테이지 Dockerfile
```Dockerfile
FROM nginx:1.21.4-alpine

CMD ["nginx", "-g", "daemon off;"]
```
- 멀티 스테이지 Dockerfile
```Dockerfile
# 빌드 이미지로 node:14 지정
FROM node:14 AS build
  
WORKDIR /app
  
# 빌드 컨텍스트의 소스코드를 작업 디렉토리로 복사, 라이브러리 설치 및 빌드
COPY . /app
RUN npm ci
RUN npm run build
  
# 런타임 이미지로 nginx 1.21.4 지정, /usr/share/nginx/html 폴더에 권한 추가
FROM nginx:1.21.4-alpine
  
# 빌드 이미지에서 생성된 dist 폴더를 nginx 이미지로 복사
COPY --from=build /app/dist /usr/share/nginx/html
  
EXPOSE 80
ENTRYPOINT ["nginx"]
CMD ["-g", "daemon off;"]
```
- 실행 결과
```bash
# 1. 빌드
$ docker build -t [레지스트리계졍명]/[이미지명] .

# 2. 컨테이너 실행
$ docker run -d -p 80:80 --name [컨테이너명] --network [네트워크명] [레지스트리계정명]/[이미지명]

# 3. localhost 접속하여 정상 동작 확인
```

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 