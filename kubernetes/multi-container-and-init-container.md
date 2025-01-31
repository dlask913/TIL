## (k8s) Multi-Container Pods and InitContainers
> Multi-Container Pods, InitContainers

<br>

## Multi-Container Pods
- 여러 컨테이너로 구성된 MSA 환경에서 두 개 이상의 서비스가 함께 관리될 필요가 있을 때 사용한다.
- 코드를 합칠 필요 없이 별도의 서비스로 개발 및 배포하며 같은 Pod 에서 실행 가능하다.
- 같은 life cycle 을 공유한다. ( 같이 생성되고 같이 종료 )
- 같은 네트워크 네임스페이스를 사용하여 localhost 로 서로 통신 가능하다.
- 같은 storage volume 을 공유하여 데이터 저장 및 접근이 가능하다.
- Multi-Container Pod 생성 예시
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: simple-webapp
  labels:
    name: simple-webapp
spec:
  containers: # 여러개 정의
  - name: simple-webapp
    image: simple-webapp
    ports:
      - containerPort: 8080
  - name: log-agent
    image: log-agent
```

### Design Patterns
#### 1. Sidecar Pattern
- 메인 컨테이너와 같이 보조 컨테이너를 실행하여 기능을 확장하는 패턴
- 메인 컨테이너가 종료되면 보조 컨테이너도 함께 종료된다.
- 로깅, 모니터링, 프록시 등의 역할을 수행한다. ( ex> web server + logging service )
#### 2. Adapter Pattern
- 애플리케이션과 외부 시스템(DB, API 등) 간의 데이터 변환 역할을 하는 패턴
- 여러 애플리케이션에서 다른 포맷의 로그를 중앙 서버로 전송하기 전에 공통 포맷으로 변환하는 역할이 가능하다.
#### 3. Ambassador Pattern
- 애플리케이션과 외부 서비스 간 네트워크 요청을 관리하는 패턴
- Ambassador 컨테이너가 프록시 역할을 하며 요청을 적절한 서비스로 라우팅한다.
- 예를 들어, 애플리케이션과 DB 연결 시 DB connectivity 설정을 라우팅하여 올바른 환경으로 가도록 한다.

<br>

## InitContainer
- 메인 컨테이너를 실행하기 전에 소스를 pull 하거나 DB 혹은 외부 서비스가 up 될 때까지 기다리는 등의 작업이 필요할 때 사용한다. 
- Pod 실행 시 한 번만 순차적으로 실행되며, 모든 initContainer 가 완료된 후에 메인 컨테이너가 시작된다.
- 작업이 실패할 경우 Pod 는 반복적으로 재시작된다.
- InitConatiner 설정 예시
```yaml
spec:
  containers:
  - name: ..
    image: ..
  initContainers: # Array Format
  - name: init-myservice
    image: busybox
    command: ['sh', '-c', 'git clone <repository> ;']
  - name: init-mydb
    image: mysql
    command: ['sh', '-c', '..;']
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)