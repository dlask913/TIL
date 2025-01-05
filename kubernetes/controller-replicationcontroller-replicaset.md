## (k8s) 컨트롤러 - ReplicationController (Deprecated), ReplicaSet
>  Controller, ReplicationController 와 ReplicaSet (Template, Replicas, Selector)

<br>

## Controller 
- Auto Healing : Pod가 비정상 상태가 되면 새로운 Pod를 생성하여 복구한다.
- Auto Scaling : Pod 의 리소스가 limit 상태가 되었을 때 Pod 를 하나 더 만들어 부하를 분산 시켜준다. 
- Software Update : 여러 Pod 들에 대한 버전을 한 번에 쉽게 업그레이드 할 수 있고 도중에 문제가 생기면 롤백이 가능하다. 
- Job : 일시적인 작업을 해야 할 경우 Pod 를 일시적으로 생성하여 해당 작업을 이행하고 삭제하여 효율적인 자원 활용이 가능하다. 
- Controller 와 Pod 는 Service 처럼 label 과 selector 로 연결한다.

<br>

## ReplicationController (Deprecated), ReplicaSet
### Template
- Controller 생성 시 Template 으로 Pod 의 정보를 저장한다. 
- Pod 가 다운됐을 때 이 Template 의 정보로 Pod 를 재성성하기 때문에 Template 정보를 업데이트하여 수동으로 버전 등의 업그레이드가 가능하다. 

### Replicas
- replicas 옵션에 설정한 수만큼 Pod 의 개수가 관리된다. 
- replicas 수를 늘리면 그 수만큼 Pod 가 늘어나며 Scale out 되고 Pod 를 삭제하면 이 수만큼 Pod 를 생성해준다. 
- Pod 없이 Controller 를 생성하면 replicas 수만큼 template 에 맞춰 Pod 가 생성된다. 
- Controller 생성 yaml 파일 예시 
```yaml 
apiVersion: v1
kind: ReplicationController
metadata:
  name: replication-1
spec:
  replicas: 1
  selector:
    type: web
  template:
    metadata:
      name: pod-1
      labels:
        type: web # 연결할 Pod 의 라벨
    spec:
      containers:
      - name: container
        image: [사용할 이미지명]
```

### Selector 
- Replication Controller : label 의 key 와 value 가 모두 같은 Pod 들만 연결
- Replicaset - matchLabels : label 의 key 와 value 가 모두 같은 Pod 들만 연결
- Replicaset - matchExpressions : operator 옵션을 통해 key 와 value 롤 좀 더 디테일하게 설정할 수 있다. ( Exists, DoesNotExist, In, NotIn )
- Replicaset Selector 설정 yaml 파일 예시 
```yaml 
apiVersion: apps/v1
kind: ReplicaSet
metadata:
  name: replica-1
spec:
  replicas: 3
  selector:
    matchLabels:
      type: web # key가 "type"이고 값이 "web"인 Pod 들과 연결
    matchExpressions:
    - key: ver
      operator: Exists # key가 "ver"인 모든 Pod 들과 연결
  template:
    metadata:
      labels:
        type: web
        ver: v1
    spec:
      containers:
  ..
```

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg)