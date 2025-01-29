## (k8s) Taint/Tolerations and Node Affinity
> Taint and Tolerations, NodeSelector, Node Affinity, Taints/Tolerations and Node Affinity

<br>

## Taint and Tolerations
- 특정 Node 에 특정 Pod 를 할당하고 싶을 때 사용하며 클러스터 보안과는 상관이 없다.
- 제한과 설정이 없다면 스케줄러는 모든 Node 에 걸쳐 균형있게 Pod 를 할당한다. 
- Taint 설정은 Node 에 하고 Toleration 설정은 Pod 에 하여 key=value 가 일치하는 경우에 Pod 가 Node 에 할당되도록 할 수 있다. 
- 일반적으로, Toleration 이 없는 Pod 는 Taint 가 있는 Node 에 할당될 수 없다. 
- Master Node 에 Pod 가 할당되지 않는 이유는 Kubernetes 에서 자동으로 Taint 설정을 하기 때문이다.

### Configuration
- Node Taint 설정
> ※ taint-effect 옵션은 Toleration 이 없는 Pod 가 취할 액션을 정의
> 1. NoSchedule : Node 에 할당하지 않는다.
> 2. PreferNoSchedule : Node 에 할당을 피할 것을 요청하지만 강제는 아니다.
> 3. NoExecute : 기존에 실행되고 있는 Pod 도 제거하고 Node 에 할당하지 않는다.
```shell
$ kubectl taint nodes <node-name> key=value:taint-effect
```
- Pod Toleration 설정
```yaml
apiVersion: v1
kind: Pod
metadata: 
  name: myapp-pod
spec:
  containers:
  - name: nginx-contianer
    image: nginx
  tolerations: # 모든 값들은 "" 으로 인코딩되어야 한다.
  - key: "app"
    operator: "Equal"
    value: "blue"
    effect: "NoSchedule"
```
- Master Node Taint 조회
```shell
$ kubectl describe node kubemaster | grep Taint
```

<br>

## Node Selector 
- Pod 를 원하는 Node 로 쉽고 간단하게 할당할 수 있다. 
- Node 에 label 을 생성하고 할당을 원하는 Pod 에 `nodeSelector` 을 정의하여 동일한 label 을 지정하면 Pod 생성 시 그 Node 로 할당된다.
- 복잡한 요구 사항은 해결할 수 없다. → `nodeAffinity` 활용
### 활용 방법
![image](https://github.com/user-attachments/assets/064d56be-e9e5-4464-b40f-2b20145e2a20)

1. Node 에 Label 생성
```shell
$ kubectl label nodes <node-name> <label-key>=<label-value>
# kubectl label nodes node=1 size=Large
```
2. Pod 에 nodeSelector 설정
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
spec:
  containers:
  - name: ..
    image: ..
  nodeSelector:
    size: Large # Node 에 설정한 label
```
3. Pod 생성
```shell
$ kubectl create -f pod-definition.yaml
```

<br>

## Node Affinity
- 특정 Node 에 Pod 를 할당하는 것을 목적으로 하며, 복잡한 요구 사항을 정의할 수 있다. 
- Node Affinity 정의 예시 
> ※ operator 옵션 종류
> 1. In : 해당 label 을 가진 Node 에 할당
> 2. NotIn : 해당 label 을 가진 Node 에 할당하지 않음
> 3. Exist : key (ex>size) 에 대한 label 이 있는 지만 체크
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: ..
spec:
  containers: ..
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution: # type
        nodeSelectorTerms:
        - matchExpressions:
          - key: size
            operator: In # In / NotIn / Exist
            values:
            - Large
            - Medium
```

### type 에 따른 동작 방식
#### 1. requiredDuringSchedulingIgnoredDuringExecution ( available )
- 해당 label 을 가진 Node 가 없으면 Pod 는 스케줄되지 않는다. ( required )
- Node 의 label 이 변경되었을 때, running 중인 Pod 들은 affinity rule 에 영향을 받지 않는다. ( ignored )

#### 2. preferredDuringSchedulingIgnoredDuringExecution ( available )
- 해당 label 을 가진 Node 가 없으면 affinity rule 을 무시하고 Scheduler 에 의해 할당된다. ( preffered )
- Node 의 label 이 변경되었을 때, running 중인 Pod 들은 affinity rule 에 영향을 받지 않는다. ( ignored )

#### 3. requiredDuringSchdulingRequiredDuringExecution ( planned )
- 해당 label 을 가진 Node 가 없으면 Pod 는 스케줄되지 않는다. ( required )
- Node 의 label 이 변경되었을 때, running 중인 Pod 가 affinity rule 에 맞지 않으면 제거된다. ( required )

<br>

## Taints/Tolerations and Node Affinity
![image](https://github.com/user-attachments/assets/cbf3d6df-7533-4d0a-ad12-36acd73b9751)

#### Taints and Toleration without Node Affinity
- Toleration 이 없는 Pod 가 할당되지 않도록 제한할 수 있다.
- Pod 에 Toletaion 설정이 있어도 Taint 가 없는 Node 에 할당될 수 있다. 

#### Node Affinity without Taints/Tolerations
- Pod 가 label 있는 Node 에 할당되도록 한다.
- 다른 Pod 들이 Node 로 할당되는 것을 제한할 수는 없다.

#### Taints/Tolerations and Node Affinity
- Toleration 설정이 없는 Pod 들이 Taint Node 에 할당되지 않도록 한다. - Taints/Tolerations
- 원하는 Pod 들을 특정 Node 에 할당되도록 한다. - Node Affinity
- **특정 Pod 를 원하는 Node 에 정확히 할당하기 위해 Taints/Tolerations 과 Node Affinity 를 같이 사용할 수 있다.**


<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)