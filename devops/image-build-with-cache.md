## (docker) 캐싱을 활용한 빌드
> 레이어 관리 및 캐싱을 활용한 빌드 실습 

<br>

## 레이어 관리 
- Dockerfile 에 작성된 지시어 하나 당 레이어가 한 개 추가된다. ( 레이어가 추가되지 않는 지시어도 있음 )
- 불필요한 레이어가 많아지면 이미지의 크기가 늘어나고 빌드 속도가 느려질 수 있다. 
- **RUN** 지시어는 && 을 활용해 최대한 하나로 처리한다. → 불필요한 레이어 개수 ↓
- 이미지의 크기가 작을수록 네트워크/스토리지 비용 감소, 이를 위해 애플리케이션 크기를 작게 한다. 
- 베이스 이미지는 가능한 작은 이미지를 사용한다. ( apline OS 를 사용하는 것이 좋음 )
- .dockerignore 파일을 사용해서 불필요한 파일을 제거한다. 

<br>

## 캐싱을 활용한 빌드
> 캐싱 : 빌드 속도를 빠르게 만들어주는 기술

- Dockerfile 에 작성된 순서대로 결과 이미지의 레이어가 쌓인다.  → 각 단계의 결과 레이어 캐시
- 지시어가 변경되지 않으면 다음 빌드에서 레이어를 재사용하는데, **COPY, ADD** 의 경우에는 빌드 컨텍스트의 파일 내용이 변경되면 캐시를 사용하지 않는다. ( 다시 build ) 
- 레이어가 변경된 경우 그 레이어와 이후의 모든 레이어는 캐시를 사용하지 않고 새로운 레이어가 만들어진다. 
- 소스 코드 변경은 잦지만 라이브러리의 업데이트는 잦지 않기 때문에 그 때마다 새로 레이어를 생성하는 것보다 잘 변경되지 않는 파일들을 아래 레이어에 배치하여 캐시 활용 빈도 높일 수 있다. 
- **package.json, package-lock.json** 파일은 소스 코드가 의존하는 외부 라이브러리 정보가 있으며 개발 시 자주 변경되지 않는다. → npm ci 단계 ( 의존 라이브러리 설치 ) 까지 캐시 활용 가능
- Dockerfile 예시 
```Dockerfile
# 개발 시 거의 변경되지 않으므로 캐시 활용 ======
FROM node:14

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm ci

# 잦은 변경 단계 ======
COPY . .

RUN npm run build

CMD [ "npm", "start" ]
```

<br>

## 실습하기
### CACHE 와 NO-CACHE 비교하기
- 프론트엔드 Dockerfile 작성
```Dockerfile
# 빌드 이미지로 node:14 지정
FROM node:14 AS build
  
WORKDIR /app
  
# 소스코드 복사, 라이브러리 설치 및 빌드
COPY . /app
RUN npm ci
RUN npm run build
  
# 런타임 이미지로 nginx 1.21.4 지정
FROM nginx:1.21.4-alpine
  
# 빌드 이미지에서 생성된 dist 폴더를 nginx 이미지로 복사
COPY --from=build /app/dist /usr/share/nginx/html
  
EXPOSE 80
ENTRYPOINT ["nginx"]
CMD ["-g", "daemon off;"]
```
- cache 기능 없이 build 하기
```bash
/ docker build -t [생성할이미지태그명] . --no-cache

# 실행 결과 : [+] Building 49.2s (15/15) FINISHED  
```
- cache + build 하기 : 48초 감소 
```bash
/ docker build -t [생성할이미지태그명] .

# 실행 결과 : [+] Building 1.1s (13/13) FINISHED
```
- 실행 과정 확인 ( CACHED )

![image](https://github.com/user-attachments/assets/1abe24cd-4022-4939-832c-2867664a364e)

<br>

### 라이브러리 설치 부분 분리하기
- Dockerfile
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
  
# 빌드 이미지에서 생성된 dist 폴더를 nginx 이미지로 복사
COPY --from=build /app/dist /usr/share/nginx/html
  
EXPOSE 80
ENTRYPOINT ["nginx"]
CMD ["-g", "daemon off;"]
```

- cache 기능 없이 build 하기
```bash
/ docker build -t [생성할이미지태그명] . --no-cache

# 실행 결과 : [+] Building 47.0s (15/15) FINISHED   
```
-  애플리케이션 소스 변경하고, cache + build 하기 : 20초 감소 
```bash
/ docker build -t [생성할이미지태그명] .

# 실행 결과 : [+] Building 26.2s (17/17) FINISHED
```
- 실행 과정

![image](https://github.com/user-attachments/assets/a0e9ad32-c736-457b-8a57-adfb032ea2c0)

<br>

## 참고
[인프런 - 개발자를 위한 쉬운 도커](https://inf.run/wHHR8) 