## (k8s) Solutions
> Commands And Arguments, ConfigMaps, Secrets, Security Contexts, Service Account, Resource Requirements, Taints and Tolerations, Node Affinity

<br>

## Commands And Arguments
- container command 정의하기
```yaml
spec:
  containers:
  - name: ubuntu
    image: ubuntu
    command: ["sleep 5000"] # 1. Array format 
    command: # 2. list values
    - "sleep"
    - "5000"
```
- container command and arguemnt 정의하기
```yaml
spec:
  containers:
  - name: ubuntu
    image: ubuntu
    command: [ "sleep" ]
    args: [ "5000" ]
```
- command 내 모든 요소들은 string 이어야 한다.
- 실행중인 Pod 의 command 변경하고 적용하기
```shell
# 1. Edit
$ kubectl edit pod <pod-name>
# 2. command 변경 후 save
# 3. 적용
$ kubectl replace --force -f <edit-pod-yaml-name>.yaml
```
- container arguments 설정하기
```shell
$ kubectl run <pod-name> --image=<image-name> -- --<key> <value>
# kubectl run webapp-green --image=nginx -- --color green
```
- container 실행 시 command 설정하기 ( overried )
```shell
$ kubectl run <pod-name> --image=<image-name> --command -- <command1> <args1>
```

<br>

## ConfigMaps
- container environmet 조회하기
```shell
$ kubectl describe pod <pod-name>
# Containers > Environment
```
- configmap 모두 조회
```shell
$ kubectl get cm
```
- configmap 상세 조회
```shell
$ kubectl describe cm <configmap-name>
```
- configmap 생성
```shell
$ kubectl create cm <configmap-name> --from-literal=<key>=<value>
```
- pod 에 configmap 적용하기 ( `env` 사용한 경우 ) → `envFrom` 이 더 편함
```yaml
spec:
  containers:
  - env:
    - name: APP_COLOR
      valueFrom: 
        configMapKeyRef: 
          name: <configmap-name>
          key: APP_COLOR
```

<br>

## Secrets
- secrets 모두 조회
```shell
$ kubectl get secrets
```
- secret 상세 조회 ( type 및 data 조회 가능 )
```shell
$ kubectl describe secret <secret-name>
```
- secret 생성하기
```shell
$ kubectl create secret <type> <secret-name> --from-literal=<key1>=<value1>
# kubectl create secret generic db-secret --from-literal=DB_Host=sql01 .. 
```
- pod 에 secret 적용하기
```yaml
spec:
  containers:
  - name: ..
    image: ..
    envFrom:
      - secretRef:
          name: <secret-name>
```

<br>

## Security Contexts
- 어떤 user 로 로그인 했는 지 확인하기
```shell
$ whoami
```
- pod 실행한 user 조회하기
```shell
$ kubectl exec <pod-name> -- whoami
```
- pod 에 userID (1010) 정의하기
```yaml
spec:
  securityContext: # pod level
    runAsUser: 1010
  containers:
  - name: ..
	image: 
    securityContext: # container level ( 우선순위가 더 높음 )
      runsAsUser: .. 
```
- container 에 capabilities 적용하기
```yaml
spec:
  containers:
  - name: ..
    image: .. 
    securityContext:
      capabilities: 
        add: ["SYS_TIME"] # Array Format
```

<br>

## Service Accounts
- serviceaccount 모두 조회
```shell
$ kubectl get sa
```
- serviceaccount 상세 조회
```shell
$ kubectl describe sa <sa-name> 
# Token 정보 조회 가능
```
- serviceaccount 생성
```shell
$ kubectl create sa <sa-name>
```
- serviceaccount token 생성
```shell
$ kubectl create token <sa-name>
```
- deployment 에 serviceaccount 정의하기
```yaml
spec:
  template:
    ..
    spec:
      serviceAccountName: <sa-name>
      containers: ..
```

<br>

## Resource Requirements
- container memory requests and limits 설정하기
```yaml
spec:
  containers:
  - name: ..
    image: ..
    resources:
      limits:
        memory: 20Mi
      requests:
        memory: 5Mi
```

<br>

## Taints and Tolerations
- node 모두 조회
```shell
$ kubectl get nodes
```
- node 상세 조회
```shell
$ kubectl describe node <node-name>
# Taints: 조회 가능
```
- node 에 taint 생성
```shell
$ kubectl taint node <node-name> <key>=<value>:effect
# kubectl taint node node01 spray=mortein:NoSchedule
```
- pod 에 toleration 설정
```yaml
spec:
  containers: ..
  tolerations:
  - key: "spray"
    operator: "Equal"
    value: "mortein"
    effect: "NoSchedule"
```
- node 에 taint 제거
```shell
$ kubectl taint node <node-name> <taint>-
# kubectl taint node node01 node-1/master:NoSchedule-
```

<br>

## Node Affinity
- node 에 label 적용하기 
```shell
$ kubectl label node <node-name> <key>=<value>
# kubectl label node node01 color=blue
```
- deployment 에 node affinity 설정하기
```shell
spec:
  template:
    spec: # Pod level
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: color
                operator: In
                values: # operator: Exist 인 경우 values 없음.
                - blue
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)