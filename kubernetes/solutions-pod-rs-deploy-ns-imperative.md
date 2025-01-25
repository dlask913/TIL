## (k8s) Soutions
> Pod, ReplicaSet, Deployment, Namespace, Imperative Command

<br>

**--help 옵션 잘 활용하기**

## Pods
- pod 모두 조회 ( current namespace 기준)
```shell
$ kubectl get pods
```
- run 명령으로 pod 생성하기
```shell
$ kubectl run nginx --image=nginx # run <pod-name> --image=<image-name>
```
- pod 상세 조회
```shell
$ kubectl describe pod <pod-name>
# Node: 속성에서 배치된 Node 확인
# Containers: 속성에서 image 확인
```
- pod List 더 자세히 조회하기 ( IP, Node 등 조회 가능 )
```shell
$ kubectl get pods -o wide
```
- pod 삭제하기
```shell
$ kubectl delete pod <pod-name>
```
- run 명령 사용 시, yaml 포맷으로 출력하기
```shell
$ kubectl run nginx --image=nginx --dry-run=client -o yaml
```
- run 명령 사용 시, yaml 파일 생성하기
```shell
$ kubectl run nginx --image=nginx --dry-run=client -o yaml > nginx.yaml
```
- yaml 파일로 pod 생성하기
```shell
$ kubectl create -f nginx.yaml
```
- Edit Pods : pod 생성 후, 동일한 pod 에 변경 사항 적용하기 
```shell
$ kubectl apply -f nginx.yaml
```
- Edit Pods : yaml 파일이 없는 경우 추출해서 사용하기
```shell
$ kubectl get pod <pod-name> -o yaml > pod-definition.yaml
```
- Edit Pods : properties 변경하기 
> spec.containers[＊].image, spec.initContainers[＊].image, spec.activeDeadlineSeconds, spec.tolerations, spec.terminationGracePeriodSeconds

```shell
$ kubectl edit pod <pod-name>
```
- pod 모두 삭제하기
```shell
$ kubectl delete pod --all
```

<br>

## ReplicaSets
- replicaset 모두 조회
```shell
$ kubectl get replicaset
```
- replicaset 상세보기
```shell
$ kubectl describe replicaset <replicaset-name> # or rs <replicaset-name>
```
- replicaset 옵션 설명보기
```shell
$ kubectl explain replicaset
```
- `selector` 의 `matchLabels` 와 `template` 의 `labels` 이 매칭되어야 한다. 
- replicaset 삭제하기
```shell
$ kubectl delete rs <replicaset-name> 
```
- replicaset 의 template image 변경하기
```shell
$ kubectl edit rs <replicaset-name> 
# yaml 파일 변경 후 저장
```
- scale 로 pod 개수 늘리기 ( or edit 으로 replicas 변경 )
```shell
$ kubectl scale rs <replicaset-name> --replicas=5
```

<br>

## Deployments
- deployment 모두 조회
```shell
$ kubectl get deployments # or get deploy
```
- `create` 명령으로 deployment 생성하기
> kubectl create deployment --help 
```shell
$ kubectl create deployment <deployment-name> --replicas=<> --image=<> # 원하는 옵션 추가
```
- create 이후 항상 READY 상태 확인하기

<br>

## Namespaces
- namespace 모두 조회 
```shell
$ kubectl get namespaces # or ns
```
- 다른 namespace 에 있는 pod list 조회하기
```shell
$ kubectl get pods --namespace=<namespace-name> # or -n=<>
```
- 특정 namespace 에 pod 생성하기
```shell
$ kubectl run <pod-name> --image=<image-name> --n=<namespace-name>
```
- 모든 namespace 에 있는 pod 조회
```shell
$ kubectl get pods --all-namespaces # or -A
```
- 특정 namespace 에 있는 service list 조회하기
```shell
$ kubectl get svc -n=<namespace-name>
```
- 다른 namespace 에 있는 db service 연결할 때 host name 규칙
```shell
<svc-name>.<namespace-name>.svc.cluster.local
```

<br>

## Imperative Commands

### kubectl [command] [TYPE] [NAME] -o <output_format> 
- `-o` 옵션과 원하는 포맷을 지정하여 그 포맷으로 출력할 수 있다. 
1. `-o json` : JSON 포맷으로 출력
2. `-o name` : resource name 출력
3. `-o wide` : 추가 정보와 함께 출력
4. `-o yaml` : YAML 포맷으로 출력

### Imperative Commands
1. `--dry-run` : 리소스를 생성할 수 있다. 
2. `--dry-run=client` : 리소스를 생성하지 않고 command 가 맞는 지만 확인한다. 
3. `-o yaml` : 출력 리소스 포맷을 YAML 로 지정한다. 

- pod yaml 파일로 출력하고 명령어 맞는 지 확인하기
```shell
$ kubectl run nginx --image=nginx --port=8080 --dry-run=client -o yaml
```
- deployment yaml 파일로 생성하기 
```shell
$ kubectl create deployment nginx --image=nginx --dry-run -o yaml > nginx-deployment.yaml
# kubectl create deploy <deploy-name> --image=<image-name> --dry-run=client -o yaml > [yaml파일명].yaml
```
- pod 를 ClusterIP 서비스로 노출하기 (내부 네트워크에서만 접근 가능)
```shell
$ kubectl expose pod <pod-name> --port <pod-port> --name <service-name> 
```
- `expose` 옵션 사용하여 pod 와 관련된 clusterip svc 생성하여 target port 지정하기
```shell
$ kubectl run <pod/svc-name> --image=<image-name> --port=<target-port> --expose=true
```

> ※ kubectl expose
> - `kubectl expose` 명령어를 사용하면 기본적으로 ClusterIP 서비스가 생성되며 `--type` 옵션을 사용하여 `NodePort`, `LoadBalancer`와 같은 타입으로도 변경할 수 있다.

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)