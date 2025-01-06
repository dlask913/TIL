## (k8s) 컨트롤러 - Deployment
>  Deployment 방식(ReCreate, Rolling Update, Blue/Green, Canary), Deployment 활용 (ReCreate, Rolling Update)

<br>

## Deployment 방식
> Deployment 생성 시 v1 의 Pod 들이 2개 생성되고 Pod 하나당 하나의 자원을 사용한다고 가정

#### 1. ReCreate
- v1 의 Pod 들을 먼저 모두 삭제하고 v2 에 대한 Pod 들을 생성한다. 
- 서비스에 대한 downtime 이 발생하기 때문에 일시적인 정지가 가능한 서비스인 경우에만 사용한다. 
#### 2. Rolling Update
- v2 의 Pod 를 하나 만들고 v1 의 Pod 를 하나 삭제하고 v2 Pod 를 또 하나 만들고 v1 Pod 를 삭제하여 배포한다. 
- 배포 중 추가적인 자원을 요구하고 사용자가 v1 과 v2 어느 Pod 에도 접속이 가능하다. 
- downtime 이 발생하지 않는다. 
#### 3. Blue/Green
- replicas 를 관리하는 모든 Controller 에서 사용 가능하다. 
- 기존 Controller, v1 의 Pod들, Service 가 아닌 새로운 Controller 로 v2 의 Pod 를 생성하고 Service 의 selector 에서 v2 의 라벨로 변경하여 배포한다. 
- v2 에서 문제가 생기면 v1 으로 라벨을 변경하여 쉬운 롤백이 가능하다. 
- 자원 사용량이 기존의 2배가 되며 순간적이기 때문에 downtime 은 발생하지 않는다.
#### 4. Canary 
- 카나리아 같은 실험체를 통해 위험을 검증하고 위험이 없다는 게 확인되면 정식 배포하는 방식
- Pod 에 `ty: app` 등의 label 로 서비스를 연결하고 v2 의 Controller, `ty: app` 을 가진 Pod 를 생성해서 새 버전에 대한 테스트를 할 수 있다. ( 문제가 발생하면 v2 Controller 의 replicas 를 0 으로 설정하여 기존 서비스로 전환 )
- v1 과 v2 에 대한 Pod 및 Service 를 생성하고 Ingress Controller 로 유입되는 트래픽을 URL Path 로 각 Service 에 연결하여 테스트한다. 
- downtime 은 발생하지 않으며 추가 자원량은 replicas 수에 따라 증가한다.

<br>

## Deployment 활용
### ReCreate 방식
- Deployment 에 selector, replicas, template 을 설정하고 ReplicaSet 에 연결하여 ReplicaSet 의 값들을 지정한다. 
- Deployment 의 template 에서 Pod version 을 업데이트하면 ReplicaSet 의 replicas 를 0 으로 만들고 새로운 v2 template 의 ReplicaSet 을 만든다.
- Deployment 생성 yaml 파일 예시
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: deployment-1
spec:
  replicas: 2  # Deployment의 Pod 개수
  selector:
    matchLabels:
      type: app  # Service와 연결될 Pod의 Label
  strategy:
    type: Recreate  # Deployment 전략
  revisionHistoryLimit: 1  # 유지할 이전 ReplicaSet의 최대 개수
  template:
    metadata:
      labels:
        type: app  # Pod의 Label
    spec:
      containers:
        - name: container
          image: [사용할 이미지명]
```

### Rolling Update (default) 방식
- Deployment 의 template 을 새로운 v2 로 업데이트하면, replicas 가 1인 새로운 ReplicaSet 을 생성하여 새로운 Pod 가 생성되며 Service 가 연결된다. → v1 과 v2 으로 트래픽이 분산된다. 
- v2 의 ReplicaSet 의 replicas 가 하나씩 늘어날 때마다 v1 의 ReplicaSet 의 replicas 가 하나씩 줄어든다.
- Deployment 생성 yaml 파일 예시 
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: deployment-1
spec:
  replicas: 2  # Deployment의 Pod 개수
  selector:
    matchLabels:
      type: app  # Service와 연결될 Pod의 Label
  strategy:
    type: RollingUpdate  # Deployment 전략
  minReadySeconds: 10  # Pod 생성 및 삭제 간 유지해야 하는 최소 시간
  template:
    metadata:
      labels:
        type: app  # Pod의 Label
    spec:
      containers:
        - name: container
          image: [사용할 이미지명]
```

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg)