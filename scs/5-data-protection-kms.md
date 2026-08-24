## (aws) Data Protection - KMS (Key Management Service)
> KMS, KMS Key Types, KMS 키의 소유권 및 관리 주체에 따른 3가지 유형, Key Material Origin, Multi-Region Keys, Envelope Encryption, Encryption SDK, KMS Symmetric API, KMS Key Rotation, KMS Key Deletion

<br>

## KMS (Key Management Service)
- AWS 서비스에서 "encyption" 은 거의 KMS 담당
- 데이터 접근을 쉽게 제어할 수 있는 방법이며, AWS가 키를 직접 관리해 준다
- 어떤 IAM 사용자나 역할(Role)이 특정 KMS 키를 사용해 데이터를 암/복호화할 수 있는지 IAM 정책 및 키 정책(Key Policy)으로 세밀하게 제어할 수 있다
- KMS 키는 리전 종속적(Regional)이며, 생성된 리전 내에서만 사용 가능하다
- 다중 리전에서 동일한 키를 사용해야 하는 경우 Multi-Region Key 기능을 별도로 사용해야 한다
- Seamlessly integrated into:
```
Amazon EBS: 볼륨 암호화 지원
Amazon S3: 객체 서버 측 암호화(SSE-KMS) 지원
Amazon Redshift: 데이터베이스 데이터 암호화 지원
Amazon RDS: DB 인스턴스 및 스냅샷 암호화 지원
Amazon SSM: Parameter Store의 보안 문자열(SecureString) 암호화 지원
Etc: 기타 수많은 AWS 서비스와 기본 연동됨
```

<br>

### KMS Key Types
#### 대칭키 (Symmetric - AES-256 keys)
- 하나의 동일한 암호화 키로 데이터를 암호화하고 복호화한다
- KMS와 통합되는 모든 AWS 서비스는 Symmetric KMS 키를 사용한다
- Data Key를 생성하고 보호하는 Envelope Encryption 에 필수적으로 사용된다 
- KMS 키의 평문 데이터에는 절대 직접 접근할 수 없으며 반드시 KMS API 를 호출하여 암복호화를 수행해야 한다 
#### 비대칭키 (Asymmetric - RSA & ECC key pairs)
- 공개키(Public Key, 암호화용)와 개인키(Private Key, 복호화용) 한 쌍으로 구성된다
- 데이터 암/복호화뿐만 아니라 전자서명 생성 및 검증(Sign/Verify) 작업에도 사용된다
- 공개키는 외부로 다운로드하여 활용할 수 있지만, 개인키는 KMS 내부에서만 보호되며 평문으로 유출/접근할 수 없다
- KMS API를 직접 호출할 수 없는 AWS 외부 사용자나 시스템에서 데이터를 암호화하여 AWS로 전송해야 할 때 활용한다

<br>

### KMS 키의 소유권 및 관리 주체에 따른 3가지 유형
#### 고객 관리형 키 (Customere Managed Keys)
- 사용자가 직접 생성, 관리, 사용하며 언제든지 활성화/비활성화 할 수 있다 
- 주기적 키 자동 교체 정책을 활성화할 수 있다 (90 – 2560 days (365 default))
- 키 정책(Key Policy, 리소스 정책)을 직접 수정할 수 있으며, CloudTrail을 통해 사용 기록을 감사할 수 있다
- 데이터 키를 보호하는 Envelope Encryption에 활용된다
#### AWS 관리형 키 (AWS Managed Keys)
- AWS 서비스(예: `aws/s3`, `aws/ebs`, `aws/redshift`)가 기본으로 생성하여 사용하는 키
- AWS가 관리하며 1년마다 자동으로 키 교체가 일어난다
- 키 정책(Key Policy)을 조회할 수 있고, CloudTrail을 통해 사용 이력을 추적할 수 있다
#### AWS 소유 키 (AWS Owned Keys)
- AWS가 자사 서비스를 보호하기 위해 자체적으로 생성하고 관리하는 키
- 여러 AWS 계정 전반에서 공동으로 사용되지만, 고객의 AWS 계정 내에는 존재하지 않는다
- 고객이 이 키를 직접 조회, 사용, 추적, 감사(CloudTrail 기록 확인)할 수 없다

<br>

### Key Material Origin
- KMS 키를 생성할 때 해당 키의 암호화 키 자재(Key Material)를 **어디서 가져와 생성했는지 출처를 구분**하며, 한 번 설정하면 생성 후 변경할 수 없다
#### KMS (AWS_KMS) – default
- AWS KMS가 자체 키 저장소 내에서 직접 키 자재를 생성하고 관리한다 
- 가장 흔하게 사용하는 표준 방식이며, 키 생성을 KMS가 알아서 한다 
#### External (`EXTERNAL`) - 외부 키 가져오기
- 사용자가 온프레미스 등 외부에서 직접 만든 키 자재를 KMS 키로 Import하여 사용한다 
- AWS 외부에서 이 키 자재의 보안과 관리를 직접 책임져야 한다
- 대칭키(Symmetric) 및 비대칭키(Asymmetric) KMS 키를 모두 지원한다
- 커스텀 키 스토어(CloudHSM) 방식과는 동시에 함께 사용할 수 없다
- 자동 키 교체를 지원하지 않으므로, 키 교체 시 사용자가 직접 새 키를 가져와 수동으로 변경해야 한다 
```
1. Create KMS key (EXTERNAL): KMS 콘솔/API에서 출처를 EXTERNAL로 지정하여 키 껍데기를 생성합니다.

2. Download (Public Key & Import Token): KMS로부터 안전한 전달을 위한 공개키(Public Key)와 가져오기 토큰(Import Token)을 다운로드합니다.

3. Encrypt Key Material: 다운로드한 공개키를 사용해 사내에서 준비한 키 자재를 암호화합니다.

4. Import (Encrypted Key Material & Import Token): 암호화된 키 자재와 가져오기 토큰을 KMS로 업로드하여 가져오기를 완료합니다.
```
#### Custom Key Store (`AWS_CLOUDHSM`)
- AWS KMS가 연결된 단독 점유 하드웨어인 **CloudHSM 클러스터 내부에 키 자재를 생성**한다
- KMS의 편의성(API/IAM 연동)을 유지하면서, 실제 키 자재(Key Material) 저장과 암호화 연산은 고객 소유의 단독 점유 하드웨어(CloudHSM)에서 수행하도록 두 서비스를 연결한 구조이다
- KMS 와 CloudHSM 클러스터를 커스텀 키 스토어로 연동한다
- 실제 키 자재는 고객이 직접 소유하고 관리하는 CloudHSM 클러스터 내부 하드웨어에 저장된다 
- 실제 암/복호화 연산 작업 역시 KMS SW가 아닌 CloudHSM 하드웨어 장비 내부에서 실행된다 
- HSM 장비 및 암호화 키에 대한 완전한 직접 통제권이 필요하거나 KMS 키가 멀티테넌트가 아닌 반드시 **단독 점유(Dedicated) 하드웨어 장비**에 보관되어야 할 때 사용한다

<br>

### Multi-Region Keys
- 서로 다른 AWS 리전에 **동일한 키 자재(Key Material)와 키 ID를 공유하는 KMS 키**를 배치하여, Cross-Region 환경에서 암/복호화를 원활하게 처리하는 기능
- 서로 다른 리전에 배치되어 서로 교차 사용 가능한 동일한 KMS 키 세트이다
- A 리전에서 암호화한 데이터를 B 리전에서 별도의 재암호화(Re-encryption)나 리전 간 KMS API 호출 없이 즉시 복호화할 수 있다
- 동일한 키 ID, 키 자재(Key Material), 자동 교체(Automatic Rotation) 정책 등을 공유한다
- 전역(Global) 단일 키가 아니라 **1개의 Primary 키와 여러 개의 Replica 키**로 구성되며, 각 키는 각 리전에서 독립적으로 관리된다
- Primary 키는 한 번에 하나만 존재하지만, 필요시 Replica 키를 독립적인 Primary 키로 Promote할 수 있다
#### Use Cases
- Disaster Recovery : 타 리전으로 데이터 복구 시 별도 키 변환 없이 데이터 복호화
- DynamoDB Global Tables 등 다중 리전 데이터베이스 암호화
- 여러 리전에 동시 배포되어 구동되는 애플리케이션 환경
- 여러 리전에서 동일한 서명 키를 활용하는 시스템

#### DynamoDB Global Tables and KMS Multi- Region Keys Client-Side encryption
1. `us-east-1` (클라이언트): Primary MRK를 사용해 SSN attribute를 클라이언트 측에서 암호화 수행
2. Put encrypted attribute: 암호화된 데이터를 `us-east-1` DynamoDB 테이블에 저장
3. Global Table Replication: DynamoDB가 `ap-southeast-2` 리전의 DynamoDB 테이블로 데이터를 자동 복제
4. `ap-southeast-2` (클라이언트): 복제된 암호화 데이터를 DynamoDB에서 읽어온다
5. Decrypt attribute with replica MRK: 자기 리전(`ap-southeast-2`)의 KMS Replica MRK를 호출하여 데이터를 복호화

<br>

### Envelope Encryption
- KMS의 `kms:Encrypt` API는 데이터 크기에 명확한 제약(4KB)이 있기 때문에, 큰 파일이나 대용량 데이터를 암호화할 때 반드시 사용하는 표준 방식
- 봉투 암호화를 구현하기 위해 호출하는 필수 API는 `GenerateDataKey` 로, 이 API를 호출하면 KMS는 데이터를 암호화할 Plaintext Data Key와 이를 KMS 키로 암호화한 **Encrypted Data Key** 두 가지를 반환한다
```
1. GenerateDataKey API 호출로 데이터 키(Data Key)를 발급받음
2. 평문 데이터 키로 실제 파일/데이터(>4KB)를 로컬에서 암호화
3. 암호화가 완료되면 메모리에서 평문 데이터 키 삭제
4. 암호화된 데이터와 암호화된 데이터 키를 함께 보관
```

<br>

### Encryption SDK
- `GenerateDataKey` 호출 및 평문 키 관리 등 Envelope Encryption 절차를 SDK가 내부적으로 알아서 처리해준다
- **Data Key Caching** : 매 암호화 요청마다 KMS API 키를 호출해 새 Data Key를 새로 발급받는 대신, 이전에 발급받은 데이터 키를 재사용하는 기능
- `LocalCryptoMaterialsCache` 제어 조건: Max Age, Max Bytes, Max Number of Messages

<br>

### KMS Symmetric API 
- `Encrypt` : KMS 키를 사용해 **최대 4KB 이하의 데이터**를 직접 암호화한다
- **`GenerateDataKey`**: Envelope Encryption에 필요한 데이터 키(DEK)를 즉시 생성한다
  - 반환값: 평문 데이터 키(Plaintext DEK)와 KMS 키로 암호화된 데이터 키 2가지
  - 데이터를 지금 바로 즉시 암호화할 때 사용한다
- **`GenerateDataKeyWithoutPlaintext`**: 나중에 사용할 데이터 키(DEK)를 생성한다
  - 반환값: 암호화된 데이터 키(Encrypted DEK)만 돌려주며, 평문 키는 제공하지 않는다. 나중에 사용할 떄 `Decrypt` API를 이용하여 평문 키로 풀어야 한다
- **`Decrypt`**: KMS 키를 사용해 최대 4KB 이하의 암호화된 데이터 및 데이터 키(Encrypted DEK)를 복호화
- **`GenerateRandom`**: 암호학적으로 안전한 무작위 random byte string을 생성하여 반환

<br>

## KMS Key Rotation
### Automatic Key Rotation
- KMS 키를 교체할 때 KMS Key ID는 그대로 유지되며, 실제 내부 Backing Key(암호화 실행 키 자재)만 새것으로 변경된다 → 외부 애플리케이션은 Key ID만 참조
- AWS-managed KMS Keys: 365일마다 자동으로 교체되며, 사용자가 끄거나 주기 지정을 조정할 수 없다
- Customer-Managed Symmetric KMS Key
  - 자동 키 교체 여부를 선택(Optional)하여 활성화할 수 있다
  - 기본값은 365일이며, 90일에서 2560일 사이로 사용자가 교체 주기를 지정할 수 있다
  - 이전 백킹 키(Saved Backing Key)를 삭제하지 않고 Active 상태로 보관하므로, 과거에 그 키로 암호화되었던 데이터도 복호화할 수 있다 

<br>

### On-Demand Key Rotation
- 정해진 자동 교체 주기를 기다리지 않고 사용자가 원할 때 키를 교체할 수 있다 
- AWS 관리형 키(AWS-managed CMK)에는 사용할 수 없으며 Customer-Managed Symmetric KMS Key 전용이다 
- 자동 키 교체 옵션이 활성화되어 있지 않더라도 독립적으로 실행할 수 있다 
- KMS 키 당 최대 10회까지 실행 가능
- 자동 키 교체와 동일하게 **KMS Key ID는 변경되지 않고 그대로 유지**되며, 백킹 키(Backing Key)만 새것으로 즉시 바뀌고 기존 백킹 키는 옛날 데이터 복호화용(`Saved Backing Key`)으로 보관한다

<br>

### Manual Key Rotation
- 완전히 새로운 KMS 키를 직접 생성하여 교체하는 방법
- 자동/온디맨드 교체와 달리 완전히 새 KMS 키를 만드는 것이므로 KMS Key ID가 달라진다 
- Key ID가 바뀌면 애플리케이션 코드를 수정해야 하기 때문에 별칭(ex> `alias/MyCustomKey`)을 사용하고, 별칭이 가리키는 KMS 키(Key ID)만 새 키로 바꿔 연결하면 **애플리케이션 코드 변경 없이 키를 교체**할 수 있다
- 과거에 기존 키로 암호화한 데이터를 복호화해야 하므로 이전 키를 삭제하지 않고 Active 상태로 유지한다
- **비대칭 키(Asymmetric KMS Key)**, **외부에서 가져온 키(Imported Key Material / BYOK)** 등 자동 키 교체를 지원하지 않는 키 유형에 필수적인 해결책

<br>

## KMS Key Deletion
KMS 키는 삭제 후 복구가 불가능하면 해당 키로 암호화된 모든 데이터를 영구히 잃게 되므로, AWS는 강력한 안전장치(대기 기간)를 제공한다
#### Generated Keys (from within KMS)
- 만료 기간이 없다 
- 즉시 삭제 불가 (7~30일 Waiting Period)하며, 대기 기간 중에는 키 삭제를 취소할 수 있다 
- 대기 기간 동안에는 암/복호화 기능이 즉시 중단된다 
- **비활성화(Disable) 활용**: 키를 지우지 않고 임시로 사용을 막으려면 즉시 Disable 처리했다가 나중에 필요할 때 다시 Enable할 수 있다
#### Imported Keys
- 외부에서 가져온 Key Material는 만료 기간을 임의로 설정할 수 있으며, 만료되면 KMS가 Key Material만 삭제한다
- 즉시 Key Material 삭제가 가능하다 (메타데이터는 남아있어 나중에 동일한 키 자재를 **재업로드/Re-import**하여 복구 가능)
- 키 자재뿐만 아니라 메타데이터까지 완전히 삭제하려면 생성 키와 마찬가지로 삭제 예약을 진행한다
#### AWS managed keys or AWS owned keys
- 삭제 불가능

<br>

### KMS Key Deletion – CloudWatch Alarm
1. 거부된 API 호출 (KMS)
2. KMS의 API 실패 기록이 AWS CloudTrail에 로그로 남는다
3. CloudTrail 로그가 CloudWatch Logs로 전달되어, Metric Filter 를 통해 에러 문구 필터링
4. 조건이 충족되면 CloudWatch Alarm이 발동되어 Amazon SNS를 통해 관리자에게 경고 발송

<br>

### KMS Key Deletion – EventBridge
1. 사용자(User)가 KMS 키를 비활성화(`DisableKey`)하거나 삭제 예약(`ScheduleKeyDeletion`)
2. AWS CloudTrail을 통해 KMS API 호출 이벤트가 감지되어 Amazon EventBridge로 실시간 전달
3. Amazon SNS를 트리거하여 관리자에게 이메일 전송
4. 자동 복구 실행: AWS Systems Manager(SSM) Automation을 시작하여 `AWSConfigRemediation-CancelKeyDeletion` 런북을 실행합니다. 이 런북이 KMS에 `CancelKeyDeletion` API를 자동으로 날려 삭제 예약을 즉시 취소

<br>

### Multi Region Key Deletion
#### Replica Key Deletion
- Primary Key 만 살아있으면 언제든 해당 리전에 Replica Key 를 다시 생성할 수 있다 
- 일반 KMS 키와 마찬가지로 7~30일의 대기 기간(Scheduled Deletion)이 있음
#### Primary Key Deletion
- 연결된 **모든 Replica Key가 먼저 삭제 완료**되어야만 Primary Key를 삭제할 수 있다
- 만약 Primary Key는 지우고 싶지만 Replica Key들은 유지하고 싶다면, 먼저 특정 Replica Key를 새로운 Primary Key로 승격(Promote)시킨 뒤 기존의 이전 Primary Key를 삭제해야 한다
- 모든 Replica가 완전히 지워진 후, **7~30일의 대기 기간**을 거쳐 삭제 처리된다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)