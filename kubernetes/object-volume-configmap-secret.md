## (k8s) 기본 오브젝트 - Volume, ConfigMap 과 Secrets
> Volume (emptyDir, hostPath, PVC/PV), ConfigMap 과 Secrets (Env-Literal, Env-File, Volume Mount-File)

<br>

## Object - Volume ( [공식문서](https://kubernetes.io/docs/concepts/storage/volumes/) )

### emptyDir
- Pod 내 Container 들끼리 데이터를 공유하는 장소 
- Volume 은 생성 시 항상 비어있으며, Pod 가 재생성될 때 Volume 도 초기화된다.
- 용도 : 일시적인 활용 목적에 의한 데이터만 저장한다. 
- Pod 생성 yaml 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-volume-1
spec:
  containers:
    - name: container1
      image: [사용할 이미지명]
      volumeMounts:
        - name: empty-dir
          mountPath: /mount1 # 컨테이너 내부에서의 mountPath
    - name: container2
      image: [사용할 이미지명]
      volumeMounts:
        - name: empty-dir
          mountPath: /mount2
  volumes:
    - name: empty-dir
      emptyDir: {} # Pod 내에서 공유되는 빈 디렉토리
```

### hostPath
- Pod 가 아닌 Node 의 path 를 사용하여 Pod 들이 죽어도 Volume 데이터를 유지할 수 있다. 
- Pod 가 재생성될 때 해당 Node 에 생성되지 않을 수 있고 다른 Node 에 같은 이름의 경로를 만들어서 각 Node 의 path 끼리 마운트 시켜줄 수 있지만 운영자가 별도 설정을 해야하기 때문에 추천하지 않는다. 
- 용도 : Pod 데이터를 저장하는 것이 아니라 Node 에 있는 데이터( 호스트 데이터 )를 읽거나 써야할 때 사용.
- Pod 생성 yaml 파일 예시 
```yaml 
apiVersion: v1
kind: Pod
metadata:
  name: pod-volume-1
spec:
  containers:
    - name: container1
      image: [사용할 이미지명]
      volumeMounts:
        - name: host-path
          mountPath: /mount1
  volumes:
    - name: host-path
      hostPath:
        path: /node-v # 호스트의 실제 경로
        type: Directory # 호스트 경로가 디렉토리
```

### PVC / PV

![image](https://miro.medium.com/v2/resize:fit:828/format:webp/1*hYuhPT326a55b4Vf7LkJJQ.png)

- Pod 에 영속성있는 Volume 을 제공하기 위해 PV (Persistent Volume) 을 원격으로 연결할 수 있다. 
- Pod 는 User 와 Admin 의 역할을 분리하기 위해 PVC (Persistent Volume Claim) 을 통해 PV 와 연결한다. ( User : Pod → PVC , Admin : PV 정의 및 Local Volume 관리 )
- 동작 과정

1. Admin 이 PV 정의 및 생성
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv-01
spec:
  capacity:
    storage: 1G
  accessModes:
    - ReadWriteOnce
  ...
```
  2. User 가 PVC 정의 및 생성
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: pvc-01
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 1G
  storageClassName: "" # Empty string must be explicitly set otherwise default StorageClass will be set
  ...
```
  3. 쿠버네티스에서 capacity 와 accessMode 속성을 근거로 PVC 에 맞는 PV 연결
  4. Pod 생성 시 PVC 마운팅
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: pod-volume-01
spec:
  containers:
  - name: container
    image: [사용할 이미지명]
    volumeMounts:
    - name: pvc-pv
      mountPath: /volume
  volumes:
  - name: pvc-pv
    persistentVolumeClaim: 
      claimName: pvc-01
```

<br>

## ConfigMap ( [공식문서](https://kubernetes.io/docs/concepts/configuration/configmap/) ), Secrets ( [공식문서](https://kubernetes.io/docs/concepts/configuration/secret/) )
- 환경별로 달라지는 보안 설정(User, Key 등)을 외부에서 정의하고 관리하여 이미지를 환경에 종속되지 않도록 한다.
- Pod 생성 시 `ConfigMap` 과 `Secret` 을 연결하면 컨테이너가 환경 변수, 파일, 또는 볼륨으로 해당 값을 읽을 수 있다.
- `ConifgMap` : 환경 변수와 같은 일반적인 상수를 관리한다. 
- `Secret` : 민감 정보( 비밀번호, API 키 등)들을 암호화된 형태로 관리한다.

### 1. Env ( Literal )
- ConfigMap 은 Key-Value 로 구성되며 무한히 넣을 수 있다. 
- Secret 도 Key-Value 형태로 구성되며 Value 는 Base64 로 Encoding 되어 저장한다. ( 단순 규칙으로, Pod 에서 읽을 때는 자동으로 Decoding 되어 원래 값을 읽어온다. )
- Secret은 메모리에 저장되어 데이터베이스에 저장되는 ConfigMap보다 보안에 유리하다. ( 시스템 자원에 영향을 줄 수 있으므로 크기는 1MB 로 제한된다. )
- ConfigMap 생성 yaml 파일 예시
```yaml 
apiVersion: v1
kind: ConfigMap
metadata:
  name: cm-dev
data:
  SSH: 'false'
  User: dev
```
- Secret 생성 yaml 파일 예시
```yaml 
apiVersion: v1
kind: Secret
metadata:
  name: sec-dev
data:
  Key: MTlzN.. # Base64 Encoding 값
```
- ConfigMap 과 Secret 를 연결한 Pod 생성 yaml 파일 예시
```yaml 
apiVersion: v1
kind: Pod
metadata:
  name: pod-1
spec:
  containers:
    - name: container
      image: [사용할 이미지명]
      envFrom: 
      - configMapRef:
        name: cm-dev
      - secretRef:
        name: sec-dev
```

### 2. Env ( File )
- 파일 자체를 ConfigMap 과 Secret 에 담을 수 있는데 파일 이름이 Key, 파일 안의 내용이 Value 가 된다. 
- 대시보드에서 지원하지 않기 때문에 직접 마스터의 콘솔에서 kubectl 명령을 실행해야 한다. 
- Secret 의 경우 위 명령을 통해 파일 내용이 Base64 로 Encoding 된다. 
- ConfigMap 이나 Secret 의 파일 내용이 변경되면, Pod 가 죽어서 재생성 되어야 변경된 값이 반영된다.
- kubectl 명령 예시 
```shell
# ConfigMap 생성
kubectl create configmap cm-file --from-file=./file.txt

# Secret 생성
kubectl create secret generic sec-file --from-file=./file.txt
```
- Pod 생성 yaml 파일 예시
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: file
spec:
  containers:
    - name: container
      image: [사용할 이미지명]
      env:
        - name: file
          valueFrom: 
            configMapKeyRef:
              name: cm-file
              key: file.txt
        ..
```

### 3. Volume Mount ( File )
- ConfigMap 과 Secret 을 담은 파일을 생성하는 것까지는 Env ( File ) 방식과 동일하다. ( kubectl 명령 )
- Pod 생성 시 Container 안에 Mount path 를 정의하고 그 path 안에 파일을 Mount 할 수 있다. → Mount 된 ConfigMap 이나 Secret 이 변경되면 자동 반영 ★
- Pod 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: mount
spec:
  containers:
    - name: container
      image: [사용할 이미지명]
      volumeMounts:
      - name: file-volume
        mountPath: /mount # Mount Path 정의
  volumes:
  - name: file-volume
    configMap:
      name: cm-file
```

<br>

## 참고
[인프런 - 대세는 쿠버네티스 [초급~중급]](https://inf.run/uATqg) <br>

https://kamsjec.medium.com/kubernetes-persistent-volumes-and-persistent-volume-claim-5148338120e4