## (aws) Data Protection - S3 Lifecycle, RDS & Aurora Security
> S3 Lifecycle Rules, RDS & Aurora Security

<br>

## S3 Lifecycle Rules
#### Transition Actions (전환 작업)
- 데이터의 수명에 따라 다른 스토리지 클래스로 이동시키는 설정 (ex> 생성 60일 후 Standard-IA로 이동, 6개월(180일) 후 Glacier로 아카이빙 이동)
#### Expiration actions (만료/삭제 작업)
- 지정된 기간이 지나면 객체를 자동으로 삭제(만료)하는 설정
- 액세스 로그: 365일이 지난 접근 로그 파일 삭제.
- 구버전 파일: **S3 버전 관리(Versioning)가 켜진 경우, 이전 버전의 파일(Noncurrent Versions)만 골라 삭제 가능**.
- 미완료 멀티파트 업로드: 업로드하다 중단된 데이터 조각(Incomplete Multi-Part Uploads)을 자동으로 지워 불필요한 스토리지 비용 방지.
#### 규칙 적용 대상 지정 (Filter)
  - Prefix (접두사): 특정 경로/폴더 이하의 객체에만 적용 (예: s3://mybucket/mp3/*).
  - Object Tags (태그): 특정 태그가 붙은 객체에만 적용 (예: Department: Finance).

### S3 Anlytics - Storage Class Anlysis
- 객체를 언제, 어떤 스토리지 클래스로 전환(Transition)할지 결정하는 데 도움을 준다
- **S3 Standard -> Standard-IA로의 이동 시점**에 대한 권장 사항을 제공한다
- 리포트는 **매일 업데이트**되며, 데이터 분석 결과가 처음 출력되기까지 24~48시간이 소요된다
- 수명 주기 규칙을 처음 만들거나 기존 규칙을 개선할 때 가장 먼저 실행하기 좋은 사전 단계

### S3 Replication (CRR & SRR)
- 소스(Source) 버킷과 대상(Destination) 버킷 모두 버전 관리(Versioning)가 필수로 활성화되어 있어야 한다
- CRR (교차 리전 복제): 서로 다른 AWS 리전 간에 객체를 복제
- SRR (동일 리전 복제): 같은 AWS 리전 내의 다른 버킷으로 객체를 복제
- 소스 버킷과 대상 버킷이 서로 다른 AWS 계정 소유여도 복제가 가능하다
- 객체 복제는 비동기(Asynchronous) 방식으로 백그라운드에서 진행된다 (실시간 즉시 복제가 아니므로 약간의 지연이 존재할 수 있음)
- S3 서비스가 소스 버킷에서 객체를 읽고 대상 버킷에 쓸 수 있도록 S3 전용 IAM 역할(Role)을 올바르게 부여해야 한다
- 기능을 켜면 설정 이후에 새로 생성된 객체만 자동 복제된다
- 기존에 존재하던 객체나 과거에 복제에 실패했던 객체들은 S3 Batch Replication을 사용하여 수동/일괄로 복제할 수 있다
- 특정 버전 ID(Version ID)를 명시하여 영구 삭제하는 작업은 복제되지 않는다

<br>

## RDS & Aurora Security
- At-rest encryption (저장 시 암호화)
  - DB 마스터 및 읽기 전용 복제본(Read Replica)은 AWS KMS 키를 사용하여 암호화하며, 인스턴스 생성 시점에 지정해야 한다
  - 마스터 DB가 암호화되어 있지 않으면 읽기 전용 복제본도 암호화할 수 없다
  - DB Snapshot을 생성한 후, 해당 스냅샷을 복원할 때 암호화 옵션을 적용하여 새 DB 인스턴스로 복원해야 한다
- In-flight encryption (전송 중 암호화) : 기본적으로 TLS 통신을 지원하며, 클라이언트 단에서 AWS TLS 루트 인증서를 사용하여 연결을 암호화한다 
- Security Groups (보안 그룹) : RDS/Aurora DB로 들어오는 네트워크 접근(포트/IP)을 통제
- 일반적으로 RDS 인스턴스에는 SSH 직접 접속이 불가능하다 (단, OS에 대한 관리 권한을 제공하는 RDS Custom 서비스만 예외적으로 SSH 접속 가능)
- DB Audit Logs를 활성화하고 CloudWatch Logs로 전송하여 장기 보관 및 분석이 가능하다

### IAM 데이터베이스 인증(IAM Database Authentication)
- IAM DB 인증은 MariaDB, MySQL, PostgreSQL 엔진에서 지원
- DB 데이터베이스 비밀번호가 필요 없으며, IAM 및 RDS API 호출을 통해 발급받은 인증 토큰(Auth Token)을 비밀번호 대신 사용한다
- Auth token has a lifetime of 15 minutes
- 네트워크 입출력 데이터는 반드시 SSL/TLS로 암호화되어 전송된다
- EC2 인스턴스나 Lambda 함수 등에 IAM Role을 부여하면, 코드 내에 DB 비밀번호를 하드코딩하지 않고도 DB에 안전하게 자동 접속할 수 있다

### Aurora 미암호화 DB를 암호화하는 표준 순서
- Aurora의 경우, 미암호화 스냅샷을 '복사(Copy)'하는 과정에서 바로 암호화 옵션을 켜서 암호화 스냅샷으로 만드는 것이 불가능 (일반 RDS와의 차이점)
1. 기존 미암호화 Aurora DB의 스냅샷 생성 (미암호화 스냅샷 생성됨)
2. 해당 스냅샷을 복원(Restore)할 때 AWS KMS 암호화 옵션 활성화
3. 암호화가 적용된 새로운 Aurora 클러스터 생성 완료

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)