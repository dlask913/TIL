## (k8s) State Persistence
> Volume, PV, PVC

<br>

## Volume
- Pod 가 종료될 때마다 데이터가 같이 사라지기 때문에 데이터를 보존하기 위해 volume 을 사용한다. ( on the Node )
- Node 가 여러개일 때 권장되지 않는데, 서로 다른 Node 에 같은 데이터를 저장하길 기대하기 때문이다.
- Pod 내 Container volume 을 Node volume 으로 mount 한다.
- 다른 외부 저장소 솔루션을 권장한다. ( AWS, NFS, GlusterFS, .. )
```yaml
volumes:
- name: data-volume
  awsElasticBlockStore:
    volumeID: <volume-id>
```
- volume 정의 예시 
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
  - image: alpine
    name: alpine
    volumeMounts: # container level
    - mountPath: /opt
      name: data-volume
  volumes:
  - name: data-volume
    hostPath: # node level
      path: /data
      type: Directory
```

<br>

## Persistence Volumes
- 관리해야 하는 Pod 가 많을 때 중앙에서 쉽게 storage 를 cluster 로 관리할 수 있다. 
- administrator 에 의해 생성되며 volume 은 user 에 의해 배포된다.
- `accessModes` 는 볼륨이 어떻게 mount 될 수 있는 지를 정의한다. 
- PV 생성 yaml 파일 예시
```yaml
apiVersion: v1
kind: PersistenceVolume
metadata:
  name: pv-vol1
spec:
  accessModes:
      - ReadWriteOnce # or ReadOnlyMany or ReadWriteMany
  capacity:
    storage: 1Gi
  hostPath:
    path: /tmp/data
```

<br>

## Persistence Volume Claims
- node 에서 storage 를 이용할 수 있게 하며, user 에 의해 생성된다.
- PVC 가 생성되면 Kubernetes 에서 claim 에 맞는 PV 와 일대일로 binding 한다.
- 모든 조건이 부합하는 PV 가 여러개라면 더 큰 capacity 를 갖는 PV 와 binding 하고, PV 가 없다면 PVC 는 Pending 상태가 된다. 
- `persistentVolumeReclaimPolicy` 옵션을 정의하여 PVC 가 삭제되었을 때 PV 를 어떻게 할 지 설정할 수 있다. ( Retain or Delete or Recycle )
- PVC 생성 yaml 파일 예시 
```yaml
apiVersion: v1
kind: PersistenceVolumeClaim
metadata:
  name: myclaim
spec:
  accessModes:
      - ReadWriteOnce
  resources:
      requests: 
        storage: 500Mi
```

<br>

## 참고
[Kubernetes Certified Application Developer (CKAD) with Tests](https://www.udemy.com/share/1013BQ3@FHcQPh5fdtPOTP1ZXYZVcotPtN9ZvIN1IS37fa49ax7L0Kti3Q1cVKrL8WjJxV0YjA==/)