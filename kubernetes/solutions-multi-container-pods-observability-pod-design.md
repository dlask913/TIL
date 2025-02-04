## (k8s) Solutions
> Multi-Container Pods, InitContainers, Readiness and Liveness Probes, Logging, POD Design

<br>

## Multi-Container Pods
- Pod 에 컨테이너 여러 개 정의하기
```yaml
spec:
  containers:
  - image: ..
    name: ..
  - image: ..
    name: ..
```

- 컨테이너 간 volume mount 하기
```yaml
spec:
  containers:
  - image: sidecar
    name: sidecar
    volumeMounts:
    - mountPath: /var/log/event-simulator/
      name: log-volume # 같은 이름 설정
  - image: app
    name: app
    volumeMounts:
    - mountPath: /log
      name: log-volume # 같은 이름 설정
```

<br>

## InitContainers
- initContainer 정의하기
```yaml
spec:
  initContainers:
    - image: ..
      name: red-initContainer
      command:
        - "sleep"
        - "20"
```

<br>

## Readiness and Liveness Probes
- httpGet readiness probe 설정하기
```yaml
spec:
  containers:
  - image: ..
    name: ..
    readinessProbe:
      httpGet:
        path: /ready
        port: 8080
```
- initial delay 와 period seconds 추가하여 liveness probe 설정하기
```yaml
spec:
  containers:
  - image: ..
    name: ..
    livenessProbe:
      httpGet: 
        path: /ready
        port: 8080
      initialDelaySeconds: 80
      periodSeconds: 1
```

<br>

## Logging
- pod 내 애플리케이션 로그 보기
```shell
$ kubectl logs -f <pod-name>
```
- 특정 컨테이너 로그 보기
```shell
$ kubectl logs -f <pod-name> <container-name>
```

<br>

## Labels and Selectors 
- 특정 라벨을 가진 pod 조회하기
```shell
$ kubectl get pods --selector <key>=<value>
```
- 특정 라벨을 가진 pod 개수 조회하기
```shell
$ kubectl get pods --selector <key>=<value> --no-headers | wc -l 
```
- 특정 라벨을 가진 모든 Object 조회하기
```shell
$ kubectl get all --selector <key>=<value> --no-headers
```
- 라벨 여러개로 모든 Object 조회하기
```shell
$ kubectl get all --selector <key>=<value>,<key>=<value>, ..
```

<br>

## Rolling Updates
- deployment image 변경하기
```shell
$ kubectl set image deploy <deploy-name> <container-name>=<변경할-image-name>
```
- deployment strategy recreate 정의하기
```yaml
spec:
  strategy:
    type: Recreate
```

<br>

## Deployment Strategies
- deployment replicas 수 조정하기
```shell
$ kubectl scale deployment --replicas=1 <deploy-name>
```

<br>

## Jobs and Cronjobs
- job 생성하기
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: my-job
spec:
  template:
    spec:
      containers:
      - name: my-job
        image: my-job
      restartPolicy: Never
  backoffLimit: 25 # attemp 25 times to get completions
  completions: 3 # 원하는 job 성공 횟수
  parallelism: 3 # 한 번에 생성하는 Job 개수
```
- cronJob 생성하기
```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: my-cronjob
spec:
  schedule: "* * * * *"
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: my-app
            image: my-app
          restartPolicy: Never
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)
