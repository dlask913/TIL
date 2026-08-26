## (aws) Data Protection - KMS, S3, EBS, EFS, Secrets Management
> KMS Encryption Context, Changing The KMS Key For An Encrypted EBS Volume, Automate Cross-Account EBS KMS-Encrypted Snapshot Copies, EBS, EFS Encryption, ABAC with KMS, Secret Management, S3 Encryption, S3 Batch, S3 Lock

<br>

## KMS Encryption Context
- 암호화할 때 데이터를 설명하는 추가적인 라벨/태그(ex> `"department": "finance"`, `"account_id": "1234"`)를 달아주는 기능
- KMS는 Encryption Context 를 추가 인증 데이터(AAD)로 사용한다
- 암호화할 때 Encryption Context를 사용했다면, 복호화할 때도 완전히 동일한 Context를 제공해야 한다 (동일한 암호화 키를 사용하더라도 마찬가지)
- 암호화된 데이터의 무결성과 진위 여부(인증)를 검증하는 데 사용된다
- 애플리케이션 버그나 잘못된 데이터 참조를 방지하는 데 유용하다
- 이 값은 CloudWatch Logs나 CloudTrail 감사 로깅에 평문으로 남기 때문에, 민감한 정보를 넣으면 안된다

<br>

## Changing The KMS Key For An Encrypted EBS Volume
- 이미 생성된 EBS 볼륨이 사용중인 암호화 키는 직접 변경할 수 없다 
- EBS 스냅샷을 생성한 후, 해당 스냅샷으로 새 EBS 볼륨을 만들 때 새로운 KMS 키를 지정해야 한다 
- EBS 암호화 키 변경 = 스냅샷 생성 후 새 키로 볼륨 복원

<br>

## Automate Cross-Account EBS KMS-Encrypted Snapshot Copies
#### 1. Account A (Source)
- Account A 의 EBS 스냅샷은 KMS Key - A로 암호화되어 있음
- 스냅샷을 Account B와 공유
- 이때 KMS Key - A의 Key Policy에 Account B가 이 키를 사용할 수 있도록 접근 권한을 부여해야 한다 
#### 2. Account B (Target)
- Account B는 공유받은 스냅샷을 자신의 계정 영역으로 복사한다 
- 복사 과정에서 Decrypt: CMK - A 작업과 Encrypt: CMK - B 작업이 동시에 일어난다 
- Grant 관련 권한 IAM Policy
```json
{
  "Effect": "Allow",
  "Action": [
    "kms:RevokeGrant",
    "kms:CreateGrant",
    "kms:ListGrants"
  ],
  "Resource": [
    "arn:aws:kms:us-east-1:111111111111:key/1234abcd-...", // Source 계정(A)의 키
    "arn:aws:kms:us-east-1:222222222222:key/4567dcba-..."  // Target 계정(B)의 키
  ],
  "Condition": {
    "Bool": {
      "kms:GrantIsForAWSResource": "true"
    }
  }
}
```
- 암호화/복호화 관련 권한 IAM Policy
```json
{
  "Effect": "Allow",
  "Action": [
    "kms:Encrypt",
    "kms:Decrypt",
    "kms:ReEncrypt*",
    "kms:GenerateDataKey*",
    "kms:DescribeKey"
  ],
  "Resource": [
    "arn:aws:kms:us-east-1:111111111111:key/1234abcd-...", // Source 계정(A)의 키
    "arn:aws:kms:us-east-1:222222222222:key/4567dcba-..."  // Target 계정(B)의 키
  ]
}
```

<br>

## EBS, EFS Encryption
### EBS Encryption - Account level setting
- 개별 EBS 볼륨을 만들 때마다 암호화 옵션을 체크하는 대신, 계정 설정에서 기본적으로 암호화가 되도록 지정하는 기능
- 기본적으로 EBS 생성 시 암호화 옵션이 기본적으로 비활성화되어 있다
- 이 설정을 활성화하면 EC2나 EBS를 새로 만들 때 암호화 옵션을 따로 선택하지 않아도 모든 신규 EBS 볼륨과 스냅샷이 자동으로 KMS 키로 암호화되어 생성된다 
- 이 설정은 리전별(per-region)로 각각 활성화해주어야 한다 

### Encrypt Un-encrypted EFS File System
- 암호화되어 있지 않은 기존 Elastic File System 을 암호화 상태로 전환하는 마이그레이션 방법
- EBS와 마찬가지로 EFS 역시 생성 당시 암호화를 켜지 않았다면 사후 변경이 불가능하다 
- 암호화 옵션을 켠 새 EFS 파일 시스템을 생성한 후, AWS DataSync를 사용해 기존 파일들을 마이그레이션한다
- **EBS 암호화 전환에는 스냅샷을 활용**하고, **EFS 암호화 전환에는 AWS DataSync를 활용**하여 새 EFS로 데이터를 옮긴다

<br>

## ABAC with KMS
- KMS 키에 대한 접근 권한을 속성/태그 기반(ABAC, Attribute-Based Access Control)으로 제어할 수 있다 
- 태그(Tags) 및 별칭(Aliases)을 기반으로 KMS 키에 대한 접근을 제어한다
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "kms:Decrypt",
        "kms:Encrypt",
        "kms:GenerateDataKey*",
        "kms:DescribeKey"
      ],
      "Resource": "arn:aws:kms:*:123456789012:key/*",
      "Condition": {
        "StringEquals": {
          "aws:ResourceTag/Environment": "Prod"
        }
      }
    }
  ]
}
```

<br>

## Secret Management
### AWS Systems Manager(SSM) Parameter Store
- SSM Parameter Store는 SecureString 타입의 파라미터 값을 암호화/복호화하기 위해 KMS를 사용한다
- Two types of Secure String Parameters:
  - Standard: 모든 파라미터가 동일한 KMS 키로 직접 암호화된다
  - Advanced: 각 파라미터가 고유한 데이터 키(Data Key)로 암호화(Envelope Encryption)
- 직접 만든 고객 관리형 키(CMK)를 지정하거나, AWS 관리형 키(aws/ssm)를 사용할 수 있다
- 대칭형(Symmetric) KMS 키에서만 동작 (비대칭형 키 지원 안 함)
- 실제 암호화 및 복호화 처리 과정은 AWS KMS 서비스 내부에서 일어난다
- 파라미터를 조회하여 사용하려면 SSM Parameter 접근 권한과 KMS 키 접근(복호화) 권한 두 가지가 모두 있어야 한다

### Secret Manager
- 비밀번호나 API 키 같은 데이터베이스/서비스 보안 정보를 안전하게 보관하고 자동 교체(Rotation)해주는 전용 관리 서비스
- SSM Parameter Store보다 나중에 나온 서비스로, 단순 저장을 넘어 비밀번호 관리 최적화에 특화되어 있다
- 주기적으로(X일마다) 보안 정보의 강제 교체(Rotation) 기능을 제공한다
- 비밀번호 교체 시 Lambda 함수를 사용하여 새로운 무작위 비밀번호 생성 및 업데이트를 자동화한다
- Lambda 함수는 Secrets Manager와 데이터베이스 두 곳 모두에 네트워크로 접근할 수 있어야 한다 (Lambda가 VPC 프라이빗 서브넷에 있다면 VPC 엔드포인트나 NAT 게이트웨이를 사용해야 함)
- Amazon RDS 및 Aurora와 연동된다
- 저장된 모든 보안 정보는 AWS KMS를 통해 암호화되어 보호된다
- 주로 RDS 데이터베이스 연결 정보 관리용으로 활용

#### 비교
- SSM Parameter Store: 단순 설정 값 및 민감 정보 저장이 주목적 (비용 저렴, 자동 교체 기능 없음)
- AWS Secrets Manager: AWS Lambda를 활용한 비밀번호 자동 교체가 필요할 때 선택 (RDS 연동 특화, 비용 발생)

<br>

### Multi-Region Secrets
- 단일 리전에만 보안 정보(Secret)를 두는 것이 아니라, 여러 AWS 리전으로 동일한 Secret을 복제하여 글로벌 애플리케이션 및 재해 복구(DR) 환경을 지원하는 방식
- 주 리전(Primary)에서 생성한 비밀번호/인증 정보를 보조 리전(Secondary)으로 자동 복사한다
- Secrets Manager는 읽기 전용 복제본을 주 보안 정보(Primary Secret)와 계속 동기화 상태로 유지한다 
- 읽기 전용 복제본 Secret을 독립된(Standalone) Secret으로 Promote시킬 수 있는 기능을 제공한다
- Use cases: multi-region apps, disaster recovery strategies, multi-region DB...

<br>

### KMS with Secrets Manager
- Secrets Manager 는 모든 버전의 모든 Secret 값을 암호화/복호화하기 위해 KMS를 사용한다 
- 각 Secret 값은 고유한 Data Key로 암호화된다 (Envelope Encryption)
- 사용자 정의 KMS 키(CMK)를 직접 지정하거나 기본 제공되는 AWS 관리형 키(aws/secretsmanager)를 사용할 수 있다
- Symmetric KMS 키에서만 동작한다
- 암호화 프로세스가 Secrets Manager 내부에서 수행된다 
- Secrets Manager의 Secret 접근 권한과 KMS 키 복호화 권한 둘 다 가지고 있어야 평문 데이터를 읽을 수 있다 

<br>

### Resource Policy
- 보안 정보(Secret)에 접근할 수 있는 주체(Who)와 해당 IAM 보안 주체가 수행할 수 있는 작업(Action)을 정의한다 

#### Use cases
- 여러 사용자에게 하나의 Secret에 대한 접근 권한을 한곳에서 부여할 때
- 특정 Secret에 명시적 거부(Deny) 구문을 추가하여 강한 접근 제어를 적용할 때
- 다른 AWS 계정(Cross-Account) 간에 Secret을 공유할 때
```json 
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "secretsmanager:*",
      "Principal": {
        "AWS": "arn:aws:iam::123456789012:user/Mary"
      },
      "Resource": "*"
    }
  ]
}
```

<br>

## S3 Encryption
### Object Encryption
#### SSE-S3 (Server-Side Encryption with Amazon S3-Managed Keys)
- S3가 자체 관리하는 키를 사용하여 S3 서버에 저장되는 시점에 서버 측에서 암호화한다
- 기본적으로 활성화되어 있어 별도 설정 없이 자동 적용되며, 키 관리에 신경 쓸 필요가 없다 
- Encryption type is AES-256
- Must set header `"x-amz-server-side-encryption": "AES256"`

#### SSE-KMS (Server-Side Encryption with KMS Keys)
- AWS KMS에 저장된 키를 활용하여 서버 측에서 암호화한다 
- 키의 생성, 교체, 접근 권한을 KMS를 통해 세밀하게 제어할 수 있으며 사용 이력이 CloudTrail에 로깅된다 
- Must set header `"x-amz-server-side-encryption": "aws:kms"`
- SSE-KMS로 암호화된 객체는 Public으로 설정하더라도 익명의 사용자가 읽을 수 없다 (kms 권한이 없기 때문)
- 대용량 파일을 올릴 때 멀티파트 업로드를 해야하며, 필수 권한은 `kms:GenerateDataKey` + `kms:Decrypt` 이다 
- 단일 업로드 시 필수 권한은 `kms:GenerateDataKey` 이다 

#### SSE-C (Server-Side Encryption with Customer-Provided Keys)
- 고객이 직접 생성하고 관리하는 암호화 키를 S3에 제공하여 서버 측에서 암호화한다
- S3가 키를 저장하지 않으므로 요청할 때마다 암호화 키를 함께 전송해야 한다 
- 암호화 키를 헤더로 전송하기 때문에 반드시 HTTPS를 사용해야 한다

#### Client-Side Encryption
- 데이터를 S3로 업로드하기 전에 클라이언트에서 미리 암호화하여 전송한다 
- Amazon S3 Client-Side Encryption Library와 같은 클라이언트 전용 라이브러리를 사용한다

#### Glacier
- Amazon Glacier에 저장되는 모든 데이터는 기본적으로 AES-256으로 자동 암호화되며, 키는 AWS가 관리한다

<br>

### SSE-KMS Limitation
- SSE-KMS를 사용할 경우 KMS의 초당 처리량 제한(API Limits)의 영향을 받을 수 있다 
- S3에 객체를 업로드할 때마다 KMS의 `GenerateDataKey` API가 실행된다
- S3에서 객체를 다운로드할 때마다 KMS의 `Decrypt` API가 실행된다
- 이 API 호출들은 리전별 초당 KMS 처리량 할당량(KMS Quotas per second)에 합산된다 -> 한계치에 도달하면 `ThrottlingException` 발생
- 처리량이 부족할 경우 AWS Service Quotas 콘솔을 통해 KMS 초당 요청 한도 증액을 요청할 수 있다

<br>

### Force Encryption in Transit
- S3 Bucket Policy를 이용해 암호화되지 않은 HTTP 요청을 차단하고 오직 HTTPS 통신만 강제
```json
{
  "Effect": "Deny",
  "Principal": "*",
  "Action": "s3:GetObject",
  "Resource": "arn:aws:s3:::my-bucket/*",
  "Condition": {
    "Bool": {
      "aws:SecureTransport": "false"
    }
  }
}
```

<br>

### Default Encryption vs Bucket Policies
- S3 버킷에 저장되는 새 객체에는 SSE-S3 encryption 이 기본적으로 자동 적용된다
- 기본 제공되는 SS3-S3 외에, 특정 암호화 헤더(SSE-KMS 또는 SSE-C)가 없는 업로드 요청을 버킷 정책으로 Deny하여 특정 암호화 방식을 강제할 수 있다 
- NOTE! **Bucket Policy 가 S3 의 Default Encryption 적용보다 먼저** 이루어진다 
- SSE-KMS 강제하기
```json
"StringNotEquals": { "s3:x-amz-server-side-encryption": "aws:kms" }
```
- SSE-C 강제하기
```json
"Null": { "s3:x-amz-server-side-encryption-customer-algorithm": "true" }
```

<br>

### S3 Bucket Key for SSE-KMS Encryption
- S3 에서 KMS 로 보내는 API 호출 수와 KMS 암호화 비용을 최대 99%까지 절감해주는 기능
- 데이터 키 구조를 활용하며, S3 Bucket Key 가 별도로 생성된다 
- 생성된 S3 Bucket Key가 S3 내부에서 새로운 Data Key들을 암호화하는 데 사용된다 → 객체마다 매번 KMS 서버로 넘어가 키를 받아오는 대신, KMS에서 생성한 단일 버킷 키를 S3 메모리 내에 유효 기간 동안 보관하면서 개별 객체 암호화용 데이터 키를 S3 자체적으로 만들어낸다
- CloudTrail 에 기록되는 KMS 관련 이벤트 수가 크게 줄어든다 

<br>

## S3 Batch
- 단 한 번의 요청으로 기존 S3 객체들에 대해 대규모 일괄 작업을 수행한다
- S3 default encryption 을 켜더라도 설정 이전에 업로드된 기존 객체들은 자동으로 암호화되지 않는데, 이를 일괄 암호화 처리할 때 S3 Batch 작업을 사용한다 
- S3 Inventory - 버킷 내 모든 객체 목록과 메타데이터 리스트(보고서)를 생성한다 
- Athena - S3 Inventory 가 생성한 보고서를 쿼리하여 암호화되지 않은 객체만 필터링한 Manifest 파일을 뽑아낸다 
- Athena가 추출해낸 미암호화 객체 목록을 input으로 삼아 S3 Batch 작업(Copy 또는 PutObject)을 실행함으로써 모든 객체에 암호화를 일괄 적용
- S3 배치 작업 역할(IAM Role)은 S3 버킷 접근 권한뿐만 아니라 KMS 키 권한(`kms:GenerateDataKey`, `kms:Decrypt` 등)도 함께 부여받아야 작동한다

<br>

## S3 Lock
### Glacier Vault Lock
- Adopt a WORM (Write Once Read Many) model → 한 번 저장된 데이터는 일정 기간 동안 삭제나 수정이 절대 불가능하며 오직 읽기만 가능한 상태로 고정된다
- 특정 기간 동안 데이터 삭제를 금지하는 Vault Lock Policy 를 작성하여 적용할 수 있다 
- 향후 수정을 막기 위해 정책 자체를 Lock 하면, AWS Root 계정이나 관리자 권한으로도 해당 정책을 수정하거나 삭제할 수 없다 
- 규정 준수 및 데이터 보존 정책 요구사항을 충족하는데 유용하다 
#### Vault Policies
- 각 Glacier Vault 는 단 1개의 Vault Acces Policy와 단 1개의 Vault Lock Policy만 가질 수 있다 
- 두 정책 모두 IAM 정책과 동일하게 JSON 형식으로 작성된다 
- Vault Access Policy (일반 접근 정책) : 특정 사용자나 계정의 접근 권한을 제어하며, 필요할 때 언제든지 수정이나 삭제가 가능
- Vault Lock Policy (규정 준수 잠금 정책) : 한 번 잠그면 절대 수정하거나 삭제할 수 없다

<br>

### Object Lock (versioning must be enabled)
- S3 객체 잠금 기능으로, Versioning이 필수적으로 활성화되어 있어야 사용 가능하다 
- WORM 모델을 적용하여 지정된 기간 동안 객체 버전의 삭제 및 덮어쓰기를 방지한다 
- Retention Mode - Compliance : 루트 계정을 포함한 그 누구도 객체 버전을 변경할 수 없으며 잠금 모드 변경이나 보존 기간 단축이 불가능하다(가장 엄격한 보존 모드)
- Retention Mode - Governance : 대부분의 사용자는 객체 삭제/수정이 불가하지만, 특수 권한(`s3:BypassGovernanceRetention`)을 가진 사용자는 보존 기간 변경 및 객체 삭제가 가능하다
- Retention Period (보존 기간) : 정해진 기간 동안 객체를 보호하며, 기간을 연장하는 것은 가능하다
- Legal Hold (법적 보존) : 보존 기간과 별개로 무기한으로 객체를 보호하며, `s3:PutObjectLegalHold` 권한이 있다면 언제든 자유롭게 설정 및 해제할 수 있다 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)