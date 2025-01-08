## (k8s) Pod LifeCycle
>  Pod Status 구조, Pod LifeCyicle

<br>

## Pod Status 구조
1. Phase : Pod 의 전체 상태를 대표하는 속성으로 Pending, Running, Succeeded, Failed, Unknown 5가지가 있다. 

2. Conditions : Pod 가 생성되면서 실행하는 단계와 상태를 나타낸다. 
- 주요 Condition Type : Initialized, Ready, ContainersReady, PodScheduled
- Reason : Status 의 세부 내용을 알기위한 속성

3. ContainerStatuses : 각 Container 의 상태를 대표하는 속성으로 Wating, Running, Terminated 3가지가 있다. 
- Reason : Status 의 세부 내용을 알기위한 속성
- imageId 가 비어있을 경우 아직 이미지가 다운로드되지 않았음을 의미한다. 

<br>

## Pod LifeCyicle
#### 1. Pending
- Pod 의 최초 상태로, Pod 가 생성되었지만 모든 컨테이너가 아직 실행되지 않은 상태를 의미한다.
- 본 컨테이너를 기동하기 전에 보안이나 볼륨 등 사전 설정을 해야하는 경우 Init 컨테이너에 초기화 스크립트를 넣어 본 컨테이너 전에 작업을 수행할 수 있다. 
- Init 컨테이너가 성공적으로 실행하면 Initialized 상태가 True, 실패 시 False 가 된다. 

### 2. Waiting
- 컨테이너가 실행 준비는 되었지만 아직 실행되지 않은 상태를 의미한다.
- 주로 이미지를 다운로드하거나 다른 리소스 대기 중일 때 발생한다. 

### 3. Running
- 하나 이상의 컨테이너가 실행 중인 상태를 의미한다. 
- 컨테이너에 문제가 발생하여 재시작되더라도 Pod 의 Phase 는 Running 으로 유지된다.

### 4. Failed, Succeeded 
- Pod 가 작업을 마치고 더이상 일을 하지 않게 되면 Failed 나 Succeded 상태가 된다. 
- Container 작업이 하나라도 실패해서 Error 인 경우 Failed 가 된다. 
- Container 작업이 모두 성공적으로 작업을 완료했을 때 Succeeded 상태가 된다. 

### 5. Unknown
- API 서버가 Pod 의 상태를 알 수 없는 경우로, 주로 통신 장애나 상태 정보 누락이 원인이 된다. 
- 금방 해결이 되면 Running 으로 돌아가지만 Unknown 상태가 지속되면 Failed 가 되기도 한다. 

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg)

