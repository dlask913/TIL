## (k8s) Pod Node Scheduling
>  Node Scheduling( NodeName, NodeSelector, NodeAffinity, Pod Affinity, Anti-Affinity, Toleration / Taint )

<br>

## Node Scheduling

### Node 선택 기능
#### 1. NodeName
- 스케줄러와 상관없이 Node 이름을 지정하여 Pod 를 특정 Node 에 할당할 수 있다. 
- Node 는 삭제, 재생성될 경우 이름이 바뀌기 때문에 잘 사용하지 않는다. 

#### 2. NodeSelector 
- Node 에 설정된 Label 을 기준으로 Pod 를 특정 Node 에 할당한다.
- NodeSelector 설정을 하면 스케줄러에 의해 같은 Label 이 있는 여러 Node 들 중 자원이 많은 Node 로 Pod 를 할당한다. 
- Label 의 key: value 가 정확히 일치해야 하며, 일치하는 Node 가 없다면 할당이 되지 않고 에러가 발생한다. 

#### 3. NodeAffinity
- Label 의 key 만 설정하여 key 가 같은 Node 에 할당할 수 있다. 
- 스케줄러에 의해 같은 key 를 가진 여러 Node 들 중 자원이 많은 Node 로 Pod 를 할당한다.
- key 가 일치하는 Node 가 없어도 스케줄러가 판단을 해서 자원이 많은 Node 에 할당되도록 옵션을 줄 수도 있다. 

<br>

### Pod 간 집중/분산 기능
> 여러 Pod 들을 한 Node 에 집중해서 할당하거나 Pod 들 간 겹치는 Node 없이 분산해서 할당해야 하는 경우

#### 1. Pod Affinity
- 여러 Pod 들을 한 Node 에 할당하고 싶을 때 사용한다. ( Web 과 Server 관계 등 )
- 예를 들면, Pod1 이 스케줄러에 의해 Node 에 할당되고 Pod2 에 Pod Affinity 설정을 주고 Pod 1 의 Label 을 지정하면 같은 Node 에 할당된다. 

#### 2. Anti-Affinity
- 여러 Pod를 서로 다른 Node에 분산 배치하고 싶을 때 사용한다. ( Master 와 Slave 처럼 백업을 해줘야하는 관계 등 )
- 예를 들면, Pod1 이 스케줄러에 의해 Node 에 할당되고 Pod2 에 Anti-Affinity 설정을 주고 Pod1 의 Label 을 지정하면 서로 다른 Node 에 할당된다. 

<br>

### Node 에 할당 제한
> 특정 Node 에 Pod 를 제한하는 경우

#### 1. Toleration / Taint
- Taint 를 Node 에 설정하면 Toleration 설정이 있는 Pod 들만 할당이 가능하다. 
- Taint Node 에 스케줄러나 사용자에 의해 Pod 할당이 불가능하다. 

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg)