## (k8s) POD Design
> Labels, Selectors and Annotations, Rolling Updates & RollBacks in Deployments, Deployment Strategies ( Blue/Green, Canary ), Job and CronJob

<br>

## Labels, Selectors and Annotations
#### 1. Label 을 Object 에 붙여 필요에 따라 Selector 로 필터링 할 수 있다. 
- Pod label 생성 예시
```yaml
apiVersion: v1
kind: Pod
metadata: 
  name: myapp
  labels: # <key>: <value> 형태의 Array 
    apps: App1
    dev: local
```
- `--selector` 로 특정 label 가진 Pod 조회하기
```shell
$ kubectl get pods --selector <key>=<value>
```

#### 2. 내부적으로 label 과 selector 를 이용하여 서로 다른 Object 를 연결할 수 있다.
- replicaset 에 pod 를 grouping 하기 위해 selector 를 사용한다. 
- pod 의 label 을 replicaset 의 matchLabels 에 정의하여 pod 와 연결한다.
- 다른 기능을 하는 동일한 label 을 가진 pod 가 있다면 label 을 추가로 정의하여 올바른 pod 와 연결할 수 있도록 한다. 
- ReplicaSet 생성 예시
```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: simple-webapp
  labels:
    app: App1
    function: Front-end
spec:
  replicas: 3
  selector: # required
    matchLabels: # pod label 과 match
      app: App1
  template:
    metadata:
      labels:
        app: App1
        function: Front-end
    ..
```
- 추가로, pod label 을 service 에 정의하여 연결할 수 있다.
```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector: 
    app: App1
  ..
```
#### annotations
- name, version, build information 등의 통합 목적을 위한 다른 세부 사항을 정의한다.
```yaml
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  annotations:
    buildVersion: 1.34
```

<br>

## Rolling Updates & RollBacks in Deployments
- 배포를 하면 rollout 이 트리거되고 새로운 배포 revision 이 생성된다.
- revision 을 통해 과거 변화를 추적할 수 있고 이전 버전으로 rollback 이 가능하다.
- 업그레이드 전 이전 버전의 인스턴스를 모두 제거하고 새로운 인스턴스를 생성하여 배포할 수 있는데, down time 이 발생한다. ( **ReCreate**, not default )
- 이전 버전의 인스턴스를 하나 제거하고 새로운 인스턴스를 하나씩 추가하여 Rolling Update 방식으로 배포한다. ( **RollingUpdate**, default )
#### kubectl
- rollout 조회
```shell
$ kubectl rollout status <deploy-name>
```
- revision 및 rollout 히스토리 조회
```shell
$ kubectl rollout history <deploy-name>
```

### RollingUpdates & RollBacks
- Deployment 에 새로운 ReplicaSet 을 생성하고, 이전 버전의 ReplicaSet 내 Pod 를 삭제할 때마다 새로운 버전의 Pod 를 새로운 ReplicaSet 에 배포한다. 
- Rollback 할 경우, 새로운 RepllicaSet 의 Pod 들을 모두 down 시키고 이전 ReplicaSet 에 Pod 를 다시 올린다. 
#### kubectl
- deployment rollback 하기
```shell
$ kubectl rollout undo deployment/<deploy-name>
```
- replicaset 을 조회하여 Pod 숫자를 확인할 수 있다.
```shell
$ kubectl get replicasets
```

### Deployment Update 
#### 1. yaml 파일 변경하기
- deployment yaml 파일을 변경하여 `apply` 하기
```shell
$ kubectl apply -f <deploy-yaml>.yaml
# rollout 이 트리거 되어 새로운 revision 이 생성
```

#### 2. command 로 변경하기
- `set` 을 이용하여 이미지 변경하기
- 기존 yaml 파일 구성과 달라지기 때문에 주의해야 한다. 
```shell
$ kubectl set image <deploy-name> <container-name>=<image-name> 
```

<br>

## Deployment Strategies ( Blue/Green, Canary )
### Blue/Green Deployment
- Green(new) Deployment 에서 테스트가 모두 성공하면 Blue(old) Deployment 에서 한 번에 트래픽을 옮기는 배포 전략이다. 
#### 동작
1. Service 와 Blue Deployment 에 version: v1 label 을 지정한다. 
- Service 생성
```yaml 
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector: 
    version: v1 # labeling
```
- Deployment 생성
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-blue
spec:
  template:
    metadata:
      name: myapp-pod
    labels:
      version: v1 # labeling
    spec: 
      containers:
      - name: app-container
        image: image:1.0
  replicas: 5
  selector:
    matchLabels:
      version: v1 # labeling
```
2. version: v2 label 을 지정한 Green Deployment 를 생성하여 트래픽이 없는 상태에서 테스트한다. 
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-green
spec:
  template:
    metadata:
      name: myapp-pod
    labels:
      version: v2 # labeling
    spec: 
      containers:
      - name: app-container
        image: image:2.0 # Update
  replicas: 5
  selector:
    matchLabels:
      version: v2 # labeling
```
3. Service 의 label 을 v2 로 변경하여 트래픽을 스위칭한다.
```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector: 
    version: v2 # Update
```

### Canary 
- 조금씩 새로운 Deployment 로 트래픽을 라우팅하여 배포하는 전략이다.
- 배포 중에, 이전 Deployment 와 새로운 Deployment 모두 트래픽이 라우팅되어야 한다.
- 동등하게 아닌 조금씩 트래픽이 라우팅되어야 하는데 이 퍼센트는 지정할 수 없고 각 Deployment 의 Pod 숫자에 의해 결정된다. → 새로운 Deployment 의 Pod 수를 적게 지정한다. 
- Istio 를 사용하면 정확한 퍼센테이지를 정의하여 트래픽을 분산시킬 수도 있다. 

#### 동작
1. Deployment 에 각 version 에 대한 label 외에 동일한 다른 label 을 붙인다. 
- Service 생성
```yaml 
apiVersion: v1
kind: Service
metadata:
  name: my-service
spec:
  selector: 
    app: front-end # labeling
```
- Deployment 생성
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp
spec:
  template:
    metadata:
      name: myapp-pod
    labels:
      version: v1 
      app: front-end # labeling
    spec: 
      containers:
      - name: app-container
        image: image:1.0
  replicas: 5
  selector:
    matchLabels:
      app: front-end # labeling
```
2. 새로운 Deployment 에 replicas 수를 적게 조정하고 위와 동일한 label 을 붙여 생성한다.
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-canary
spec:
  template:
    metadata:
      name: myapp-pod
    labels:
      version: v2
      app: front-end # labeling
    spec: 
      containers:
      - name: app-container
        image: image:2.0 # Update
  replicas: 1 # 낮은 트래픽 유도를 위해 작게 설정
  selector:
    matchLabels:
      app: front-end # labeling
```
3. 이전 Deployment replicas 를 0으로 하고 새로은 Deployment 의 replicas 수를 조정하여 배포를 완료한다.

<br>

## Job and CronJob
### Job
- 한 번 수행하고 끝나야하는 작업들을 Pod 에 정의하면 Pod 는 Completed 상태로 종료되고, Kubernetes 는 반복적으로 재실행하게 된다. 
- Pod 는 `restartPolicy: Always` 가 디폴트이며 변경이 가능하다.
- Job 을 실행하면 주어진 작업을 수행하고 정상적으로 종료한다. ( Completed )
- 이미지를 다운로드해서 volume 에 저장하거나 이메일을 전송하는 등의 작업을 수행할 수 있다. 
- completions 옵션을 추가하여 원하는 성공 횟수를 정의할 수 있고 순차적으로 수행된다. 실패하면 성공 횟수를 만족할 때까지 재시도한다. 
- parallelism 옵션을 추가하여 한 번에 생성하길 원하는 job 개수를 정의할 수 있다.
- Job 생성 yaml 파일 예시
```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: math-add-job
spec:
  template:
    spec:
      completion: 3
      parallelism: 3
      containers:
        - name: math-add
          image: ubuntu
          command: ['expr', '2', '+', '2']
      restartPolicy: Never
```
#### kubectl
- job 조회하기
```shell
$ kubectl get jobs
```
- job 으로 생성된 pod 들 조회하기
```shell
$ kubectl get pods
```
- job 으로 생성된 pod 로그 보기
```shell
$ kubectl logs <pod-name>
```
- job 삭제하기
```shell
$ kubectl delete job <job-name>
```

### CronJob
- 정기적으로 수행하기 위해 스케줄을 정의한 Job 을 말한다.
- cronjob 생성 yaml 파일 예시 
```yaml
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: cron-job
spec:
  schedule: "*/1 * * * *" # 스케줄 정의
  jobTemplate: # 수행할 job 정의
    spec:
      completions: 3
      parallelism: 3
      template: 
        spec:
          containers:
            - name: reporting-tool
              image: reporting-tool
        restartPolicy: Neveer
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)