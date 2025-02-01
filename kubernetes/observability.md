## (k8s) Observability
> Readiness and Liveness Probes, Container Logging, Metric Servers

<br>

## Readiness and Liveness Probes
### Pod LifeCycle
#### 1. Pod Status
- Pending : Pod 가 생성되고 Node 에 배치되기 전 상태
- ContainerCreating : Node 에 배치되고 Container 가 모두 생성되기 전 상태
- Running : Pod 안 Container 들이 모두 시작한 상태
#### 2. Pod Conditions
- Pod Status 에 대한 추가 정보를 나타낸다.
- PodScheduled : Pod 가 노드에 스케줄링되었는지 여부
- Initialized : 모든 init 컨테이너가 완료되었는지 여부
- ContainersReqdy : 모든 컨테이너가 준비되었는지 여부
- Ready : Pod 가 서비스 트래픽을 받을 준비가 되었는지 여부
- `PodScheduled`, `Initialized`, `ContainersReady`, `Ready` 상태를 true / fale 로 나타낸다.

### Readiness Probes
- 컨테이너가 시작하고 실제로 애플리케이션에 사용자가 접근할 때까지 시간이 걸리는데 이 기간동안 Pod 는 running 상태이므로 트래픽을 받을 준비가 되어있다고 판단한다. 
- 애플리케이션이 시작하는 동안 서비스는 이용할 수 없기 때문에 Pod Status 와 별개로 user들은 정상 응답을 받을 수 없다. 
- readiness probes 설정을 통해 애플리케이션이 트래픽을 받을 준비가 되었는지 확인할 수 있다.
- Service 에 연결된 여러 Pod 중 새로운 Pod가 추가될 때, 애플리케이션이 완전히 올라오기 전에 트래픽을 받지 않도록 설정이 가능하다.

### Liveness Probes
- 컨테이너는 alive 상태인데 애플리케이션이 제대로 동작하지 않는 경우 사용자는 정상 응답을 받지 못하게 된다.
- 주기적으로 probe test 를 하여 애플리케이션이 실제로 건강한 지 체크하고 test 실패 시 컨테이너를 restart 혹은 destory 시킨다.

<br>

### 활용 방법
#### 1. HTTP Test
- API 서버에 http 요청을 보내 API Server 가 정상적으로 응답하는 지 확인한다.
```yaml
spec:
  containers:
  - name: ..
    image: ..
    ports:
      - containerPort: 8080
    readinessProbe: # or livenessProbe:
      httpGet:
        path: /api/ready
        port: 8080
```

#### 2. TCP Test
- DB 서버의 경우 TCP Socket 이 정상적으로 응답하는 지 확인한다.
```yaml
spec:
  containers:
  ..
    readinessProbe: # or livenessProbe:
      tcpSocket:
        port: 3306
```

#### 3. Exec Command
- Custom Script 를 통해 command 를 실행하여 성공적으로 수행하는 지 확인한다.
```yaml
spec:
  containers:
  .. 
    readinessProbe: # or livenessProbe:
      exec:
        command:
          - cat
          - /app/is_ready
```

#### 추가 옵션 
- `initialDelaySeconds` : 컨테이너가 시작된 후 Probe 실행 전 대기 시간 (기본값 0)
- `periodSeconds` : probe 실행 주기를 정의 (기본값 10초)
- `failureThreshold` : probe 테스트 실패 허용 횟수를 정의 (기본값 3)
```yaml
readinessProbe: # or livenessProbe:
  initialDelaySeconds: 10
  periodSeconds: 5
  failureThreshold: 8
```

<br>

## Logging, Metric Servers
### Logging
- 컨테이너가 1개일 때, 실시간 로그 보기
```shell
$ kubectl logs -f <pod-name>
```
- Multi-Containers 일 때, 실시간 로그 보기
```shell
$ kubectl logs -f <pod-name> <container-name>
```

<br>

### Metric Servers
- Kubernetes 클러스터 내에서 Node 및 Pod의 실시간 리소스 사용량을 메모리에 수집한다. → 기본적으로 과거 데이터를 저장하지 X
- 각 Node 에는 Kubernetes API Master Server 로부터 지시를 받는 kubelet agent 가 있으며, 그 안에 cAdvisor 가 Pod metrics 를 kubelet API 를 통해 Metric Server 로 내보낸다. 

#### kubectl
> metrics-server 설치한 경우 조회 가능
- 각 node 에 대한 memory, cpu 사용량 조회
```shell
$ kubectl top node
```
- 각 pod 에 대한 memory, cpu 사용량 조회
```shell
$ kubectl top pod
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)