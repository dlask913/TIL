## (k8s) Resource Requirements
> Resource Requests, Resource Limits, Default Behavior, LimitRange, Resource Quotas

<br>

## Resource Requirements
![image](https://github.com/user-attachments/assets/bdbc5df3-c6fc-4867-b802-33438933dfdc)

- Pod 에 있는 Container 들은 Kubernetes 스케줄러에 의해 가장 자원이 충분한 Node 로 할당된다. 
- 어느 Node 에도 사용 가능한 자원이 없다면 스케줄러는 할당을 멈추고, 사용자는 Pod 상세 조회시 `Insufficient cpu` 가 발생했음을 알 수 있다. 
- Pod 생성 시 컨테이너 별로 최소 요구 자원과 제한을 설정할 수 있다.

<br>

### Resource Requests
- Pod 생성 시 컨테이너에 필요한 resource requests 를 정의할 수 있다. 
- 이를 통해 스케줄러는 자원이 충분한 Node 로 할당하며, Node 는 requests 를 토대로 최소 필요한 자원을 보장한다. 
- resource requests 를 정의하지 않으면, CPU 와 메모리는 제한없이 Node 에서 사용할 수 있을만큼 사용한다. → 메모리가 부족하면, 다른 실행중인 프로세스를 죽여서 반납한 자원을 이용하게 된다.
- pod 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: simple-app
spec:
  containers:
  - name: ..
    image: ..
    resources:
      requests: # 최소 필요한 자원 정의
       memory: "4Gi" # 단위와 함께 입력 ( ex> G, M, K, Gi, Mi, Ki )
       cpu: 2 # Gi 단위, 최소 1m 부터 설정 가능
```

<br>

### Resource Limits
- Pod 생성 시 Node 에서 사용할 수 있는 리소스를 제한할 수 있다. 
- 컨테이너에서 정의한 cpu  limit 을 초과한다면 CPU 사용률이 강제로 낮아지며(throttling) 성능 저하가 발생할 수 있다. 
- 컨테이너에서 정의한 memory limit 을 초과하면 컨테이너가 OOM(Out Of Memory) 상태로 간주되어 즉시 종료된다. 
- pod 생성 yaml 파일 예시
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: simple-app
spec:
  containers:
  - name: ..
    image: ..
    resources:
      requests:
        memory: "1Gi"
        cpu: 1
      limits: # 최대 사용할 수 있는 자원 정의
        memory: "2Gi"
        cpu: 2
```

<br>

### Default Behavior
#### 1. No Requests / No Limits 
- 아무런 설정을 하지 않으면 필요한 만큼 Node 의 자원을 제한없이 사용한다. 
- 메모리 부족 시 Node 에 의해 컨테이너가 OOM 으로 종료될 수 있다.
#### 2. No Requests / Limits 
- limits 도 requests 와 동일한 값으로 간주한다. ( requests = limits )
#### 3. Requests / Limits 
- 이상적으로 보일 수 있으나 그렇지 않다. 
- 트래픽이 특정 Pod 로 몰리게 되고 다른 Pod 는 CPU 를 필요로 하지 않을 때 특정 Pod 에서 limit 이상의 자원을 쓸 수 없게 되기 때문이다. 
#### 4. Requests / No Limits 
- limits 이 없을 때 requests 를 정의하지 않으면 자원이 점유 당할 수 있기 때문에 requests 를 설정하여 리소스가 보장되도록 한다. 
- 메모리의 경우 다른 메모리를 점유하기 위해 프로세스를 죽여서 반납된 자원을 사용하기 때문에 꼭 requests 설정이 필요하다. 

<br>

### LimitRange
- 모든 Pod 가 생성될 때 requests 와 limits 설정이 없는 경우 디폴트 설정을 정의할 수 있다. 
- Namespace 레벨에서 동작하며 수정을 해도 기존의 Pod 들에 영향을 미치지 않는다. ( 신규 생성된 pod 부터 적용 ) 
- LimitRange 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: LimitRange
metadata:
  name: cpu-resource-constraint
spec:
  limits:
  - default:
      cpu: 500m
    defaultRequest:
      cpu: 500m
    max:
      cpu: "1"
    min:
      cpu: 100m
    type: Container
```

<br>

### Resource Quotas
- Namespace 단위로 사용할 수 있는 전체 리소스를 제한한다. 
- 이를 통해 특정 Namespace 가 과도한 리소스를 점유하는 것을 방지할 수 있다.
- ResourceQuota 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: myresource-quota
spec:
  hard:
    requests.cpu: 4
    requests.memory: 4Gi
    limits.cpu: 10
    limits.memory: 10Gi
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)