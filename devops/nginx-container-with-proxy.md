## (docker) 프록시를 이용한 Nginx 서버 구성
> 프록시를 이용해 Nginx 서버를 구성하여 3 Tier 아키텍처 구성하기.

<br>

## 3 Tier 아키텍처 구성
> 백엔드 애플리케이션이 외부에 노출되어 있을 경우 개발자가 의도하지 않은 API 를 호출할 위험이 있어, Nginx 프록시를 사용하여 외부 접근을 차단할 수 있다. ( Proxy : 특정 경로로 온 요청을 정해진 경로로 전달 )

- Nginx 의 프록시 기술을 활용해 보안에 뛰어난 3Tier 아키텍처를 구성할 수 있다.
- Nginx 는 특정 경로로 온 요청(/api로 시작하는 경로) 를 지정한 서버로 전달한다. 
- Nginx 를 프록시 서버로 활용하여 보안 향상, 부하 관리 및 API 응답 캐싱을 활용할 수 있다. 

<br>

## 실습하기
### Nginx 서버 설정 추가
- api.js 수정
```js
import axios from 'axios';

const api = axios.create({
    // api 경로 설정 disable
    // baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
});

export default api;
```
- ningx 설정 파일 추가 ( nginx.conf )
```conf
server {
  listen  80;
  server_name _;
  
  location / {
    root  /usr/share/nginx/html;
    index index.html index.htm;
  }
  
  location /api/ {
    proxy_pass  http://[백엔드-컨테이너명]:8080;
  }
  
  error_page  500 502 503 506 /50x.html;
  location = /50x.html {
    root  /usr/share/nginx/html;
  }
}
```

- Dockerfile 내 nignx 설정 파일 COPY 명령어 추가
```Dockerfile
# 빌드 이미지로 node:14 지정 =====================
FROM node:14 AS build
  
WORKDIR /app
  
# 라이브러리 설치에 필요한 파일만 복사
COPY package.json .
COPY package-lock.json .
  
# 라이브러리 설치
RUN npm ci
  
# 소스코드 복사 ==================================
COPY . /app
  
# 소스코드 빌드
RUN npm run build

# 런타임 이미지로 nginx 1.21.4 지정
FROM nginx:1.21.4-alpine

# ★ nginx 설정 파일 COPY 
COPY nginx.conf /etc/nginx/conf.d/default.conf
  
# 빌드 이미지에서 생성된 dist 폴더를 nginx 이미지로 복사
COPY --from=build /app/dist /usr/share/nginx/html
  
EXPOSE 80
ENTRYPOINT ["nginx"]
CMD ["-g", "daemon off;"]
```

### 동작
1. 프론트엔드 컨테이너는 HostOS 의 80포트로 접속
2. 백엔드 컨테이너는 HostOS 의 80 포트의 /api 경로로 접근 ( Nginx 서버에서 프록시되어 외부에서 접근 불가 )
3. 데이터베이스 컨테이너는 포트포워딩이 없어 외부에서 접근 불가 ( 내부 DNS 서버에 등록된 레코드를 통해 백엔드 컨테이너에서  DBMS 로 접속 )
- 기존 구성과의 차이점 : 백엔드 컨테이너의 포트 포워딩이 불필요. 오로지 Nginx 서버를 통해 접근 가능하다.

<br>

## 동적 서버 설정
### 배경
- Nginx 서버 설정에 백엔드 애플리케이션의 주소가 고정되어 있다. 
- 환경 별로 Nginx 가 프록시 해야 하는 주소가 바뀔 수 있다. 
- 프록시 설정의 주소를 바꾸기 위해서는 이미지를 다시 빌드해야 한다.

### 방안
- 환경 별로 달라지는 정보는 시스템 환경 변수로 처리하면 컨테이너 실행 시 결정할 수 있다. 

### 실습
- nginx 설정 파일 ( nginx.conf ) 내 proxy_pass 변경
```conf
location /api/ {
	proxy_pass http://${BACKEND_HOST}:${BACKEND_PORT};
}
```
- Dockerfile
```Dockerfile
..  
# 프로덕션 스테이지
FROM nginx:1.21.4-alpine
# ===============추가===============
COPY nginx.conf /etc/nginx/conf.d/default.conf.template
ENV BACKEND_HOST [디폴트-백엔드-컨테이너명]
ENV BACKEND_PORT [백엔드-포트]
  
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh
# ==================================
  
# 빌드 이미지에서 생성된 dist 폴더를 nginx 이미지로 복사
COPY --from=build /app/dist /usr/share/nginx/html
  
EXPOSE 80
# ENTRYPOINT 수정
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["-g", "daemon off;"]
```
- docker-entrypoint.sh 작성
```bash
# 오류가 발생했을 때 중단
set -e
  
# default.conf.template 파일의 환경 변수를 실제 값으로 변경해서 default.conf 에 저장
envsubst '${BACKEND_HOST} ${BACKEND_PORT}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
  
# 다음 명령어를 실행
exec "$@"
```
- 프론트 컨테이너 실행 ( 앞서 db 및 백엔드 컨테이너 실행했다는 가정 )
```sh
$ docker run -d -e BACKEND_HOST=[백엔드-컨테이너명] -p 80:80 --name [실행할-컨테이너명] --network [네트워크명] [이미지명]
```
- 생성한 컨테이너의 nginx.conf 파일의 내용 확인
```bash
$ docker exec [프론트-컨테이너명] cat etc/nginx/conf.d/default.conf
```
- 실행 결과

![image](https://github.com/user-attachments/assets/642db391-17b2-40e5-b060-1267a550fc2a)

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 