## (k8s) Docker Images
>  Define build and modify container images, Commands And Arguments in Docker

<br>

## Define build and modify container images
### 응용 프로그램 수동 배포 예시
1. OS 세팅하기
2. Update apt repo : 패키지 소스 업데이트
3. Install dependencies using apt : apt 패키지를 이용한 의존성 설치
4. Install Python dependencies using pip : pip 를 이용한 의존성 설치
5. Copy source code to /opt folder : 소스 코드 /opt 로 복사
6. Run the web server using "flask" command : 웹 서버 실행

### Dockerfile 로 배포하기
1. Dockerfile 작성
```Dockerfile
FROM Ubuntu 

RUN apt-get update
RUN apt-get install python

RUN pip install flask
RUN pip install flask-mysql

COPY . /opt/source-code

ENTRYPOINT FLASK_APP=/opt/source-code/app.py flask run
```
2. 빌드
```shell
dockere build Dockerfile -t [계정/이미지명]
```

### Layerd Architecture
- 각 컨테이너 이미지는 여러 레이어로 구성되며, 이전 레이어의 변화를 기반으로 새로운 레이어가 추가된다.
- `docker history [이미지명]` 을 통해 이미지의 레이어 구조 및 크기를 조회할 수 있다. 
- 빌드 실패 시, 캐시를 이용하여 변경된 Layer 부터 다시 빌드할 수 있다. 

<br>

## Commands And Arguments in Docker
- 단순히 `docker run ubuntu` 를 실행하면 컨테이너가 바로 종료되는 것을 볼 수 있는데 컨테이너는 VM 과 달리 운영 체제를 호스팅하도록 되어있지 않기 때문이다. 
- 컨테이너는 특정 작업이나 프로세스를 실행하도록 만들어진다. 
- 컨테이너 안 웹서비스가 멈추거나 충돌하면 컨테이너는 종료된다. 
- 기본적으로 Docker 는 실행 중일 때 컨테이너에 터미널을 연결하지 않는다. → 실행 시 정의해야 한다.
- 컨테이너를 running 상태로 유지하기 위해 명령어를 정의해야 한다. 

<br>

### Command 와 EntryPoint
#### 1. 컨테이너 실행 시 Command 정의하기
- run 명령어에 command 추가하기
```shell
docker run ubuntu sleep 5
```
- Dockerfile 에 CMD 정의하기
```Dockerfile
FROM Ubuntu
CMD sleep 5   
# CMD command param1 or CMD ["command", "param1"]
```

#### 2. 컨테이너 실행 시 Entrypoint 정의하기
- Dockerfile 에 Entrypoint 정의하기 : param 없이 command 지정
```Dockerfile
FROM Ubuntu
ENTRYPOINT ["sleep"]
# docker run ubuntu 10 으로 실행
```
- runtime 중에 entrypoint 수정하기
```shell
docker run --entrypoint sleep2.0 ubuntu 10
```

#### 3. Command 와 Entrypoint 모두 정의하기
- ENTRYPOINT 는 고정 명령어, CMD 는 기본 파라미터를 지정한다.
```Dockerfile 
FROM Ubuntu
ENTRYPOINT ["sleep"]
CMD ["5"]
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)