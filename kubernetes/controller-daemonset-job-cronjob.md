## (k8s) 컨트롤러 - DaemonSet, Job, CronJob
>  DaemonSet, Job, CronJob 동작 및 사용방법

<br>

## Controller - DaemonSet
### DaemonSet 동작
- ReplicaSet 은 스케줄러가 각 Node 의 자원 상태에 따라 Pod 를 자유롭게 배치할 수 있지만 DaemonSet 은 각 Node 에 하나의 Pod 를 고정적으로 배치한다. 
- Kubernetes 자체에서도 네트워킹 관리를 위해 각 Node 에 DemonSet 으로 프록시 역할을 하는 Pod 를 생성한다. 
- 용도 
1. 성능 수집 및 모니터링 도구 ( ex> Prometheus )
2. 로그 수집 ( ex> Fluentd )
3. 스토리지

### 사용방법
- DemonSet 은 selector 와 template 을 사용하여 모든 Node 에 동일한 Pod 를 생성하고 배포한다.
- DemonSet 에 nodeSelector 라벨을 지정하여 라벨이 없는 Node 에 Pod 가 생성되지 않도록 설정할 수 있다. 
- 한 Node 에 여러 개의 Pod 를 배치하거나 Node 별로 다른 설정을 적용할 수는 없다.
- DemonSet 생성 yaml 파일 예시 
```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: daemonset-1
spec:
  selector:
    matchLabels:  # DaemonSet이 관리할 Pod의 Label
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      nodeSelector:  # Pod 를 특정 Node 에만 생성하고 싶을 때 사용
        os: centos
      containers:
      - name: my-container
        image: [사용할 이미지명]
        ports:
        - containerPort: 8080   # 컨테이너 포트
          hostPort: 18080       # Node의 호스트 포트
```

<br>

## Controller - Job
### Job 동작
- Pod 를 수동 생성하거나 ReplicaSet 혹은 Job Controller 로 생성할 수 있다. 
- 수동 생성한 Pod 는 Node 가 다운되면 함께 종료되지만 Controller 로 생성한 Pod 는 Node 장애 감지 시 다른 Node 에서 자동으로 재생성(Recreate)된다. 
- ReplicaSet 은 지속적인 서비스 유지가 필요한 경우에 사용되고 Pod 가 일을 하지 않으면 컨테이너를 재시작(Restart) 하여 서비스를 계속 유지한다.
- Job 은 특정 작업을 완료한 후 종료되는 일회성 작업에 사용되고 작업이 끝난 Pod 는 종료 상태로 유지되어 자원을 낭비하지 않는다. 
### 사용방법
- 특정 작업만 하고 종료가 되는 일을 하는 Pod 를 template 에 정의하고 selector 의 경우 직접 만들지 않아도 Job 이 생성해준다. 
- `completions` : 작업 완료를 위한 총 Pod 실행 수로, 정의한 수만큼 순차적으로 실행되어야 Job 이 종료된다.
- `parallelism` : 동시에 실행할 Pod 수 정의
- `activeDeadlineSeconds` : Job 의 최대 실행 시간 정의, 작업이 너무 오래 걸리거나 무한 루프에 빠지는 것을 방지하기 위해 사용한다.
- Job 생성 yaml 파일 예시 
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: job-1
spec:
  completions: 6 # 총 6개의 작업 수행
  parallelism: 2 # 최대 2개의 Pod 동시 실행
  activeDeadlineSeconds: 30 # 30초 내에 Job 종료
  template:
    spec:
      restartPolicy: Never # Pod 실패 시 재시작하지 않음 (or OnFailure)
      containers:
      - name: my-container
        image: [사용할 이미지명]
```

<br>

## Controller - CronJob 
### CronJob 이란
- Job 을 주기적으로 실행하도록 스케줄링한다.
- 대체로 Job 은 CronJob 으로 만들어 특정 시간에 반복적으로 실행할 목적으로 사용된다.
- DB 백업, 주기적인 업데이트, Messaging 등의 용도로 사용한다. 
### 사용방법
- Job template 을 통해 Job 을 만들고 정의한 schedule (Cron 포맷) 에 따라 Job 이 실행한다.
- `concurencyPolicy` : 여러 Job 이 동시에 실행될 때의 동작을 설정
1. Allow(default) - 이전 Job 상태와 상관없이 새로운 Job을 실행
2. Forbid - 이전 Job이 실행 중이면 새로운 Job은 실행 X
3. Replace - 이전 Job이 실행 중이면 이전 Job 의 연결을 새로운 Pod 로 교체
- CronJob 생성 yaml 파일 예시 
```yaml 
apiVersion: batch/v1
kind: CronJob
metadata:
  name: cron-job
spec:
  schedule: "*/1 * * * *" # 매 분마다 실행
  concurrencyPolicy: Allow # 동시에 여러 Job 실행 허용
  jobTemplate:
    spec:
      template:
        spec:
          restartPolicy: Never # Pod 재시작 정책
          containers:
          - name: my-container
            image: [사용할 이미지명]
```

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg)