## (aws) Data Protection - KMS (Key Management Service) 2
> KMS Key Policies, Key Grants, AWS Service Usage, Can’t Start an EC2 Instance with Encrypted EBS Volume, Condition Keys, KMS Key Authorization Process, Asymmetric Encryption, KMS API Calls Limits & Data Key Caching

<br>

## KMS Key Policies
- S3 Bucket Policy 와 유사한 리소스 기반 정책이지만, KMS에서는 Key Policy 없이는 접근 제어가 불가능하다 (IAM 정책만으로는 권한 부여 불가)
- Cross-Account 접근 : 다른 AWS 계정의 IAM 사용자/역할에 키 접근 권한을 부여할 때 주로 사용한다 

<br>

### Default Key Policy
- KMS 키 생성 시 별도의 키 정책을 직접 작성해 넣지 않으면 기본 키 정책(Default Key Policy)이 자동으로 생성된다
- 별도 정책을 지정하지 않으면 KMS를 생성한 Root 사용자에게 키에 대한 모든 권한을 부여한다
- 이 정책 덕분에, 계정 관리자는 별도로 KMS 키 정책을 매번 복잡하게 수정하지 않고도 IAM Policy를 통해 개별 IAM 사용자나 역할에게 KMS 사용 권한을 부여할 수 있게 된다 
- 키 사용을 위해 계정 관리자가 A 계정의 IAM Policy에 `kms:Decrypt` 권한을 추가해 주면 된다

<br>

### Custom Key Policy 
- 사용자가 직접 작성하는 사용자 지정 키 정책(Custom Key Policy)을 이용하면, 키에 접근할 수 있는 IAM 사용자나 역할(Role)을 명시적으로 지정할 수 있다
- 키를 암호화/복호화에 사용하는 Key Users 와 키를 수정/삭제할 수 있는 Key Administrators 권한을 분리하여 정의할 수 있다
- 다른 AWS 계정(Cross-Account)에 속한 유저나 역할에게 내 KMS 키 접근 권한을 열어줄 때 필수적으로 사용된다
#### Allow Admins
- KMS 키 관리자는 키의 생성, 삭제 예약, 정책 변경, 비활성화 등 관리 작업 권한을 갖는다 (`kms:Create*`, `kms:Describe*`, `kms:Enable*`, `kms:PutKeyPolicy` 등)
- 관리자 권한을 가진 사용자라 할지라도 `kms:Encrypt`, `kms:Decrypt` 같은 암호화 권한은 기본적으로 포함되지 않는다
#### Allow Users
- IAM Policy를 거치지 않고 KMS Key Policy 자체에 특정 IAM User나 Role을 직접 명시하여 암복호화 권한을 주는 방식
- KMS 키와 IAM Principal(User/Role)가 동일한 AWS 계정 내에 있다면 IAM Policy에 KMS 권한을 별도로 추가하지 않아도 된다 (Key Policy 에서 허용해줬기 때문)
- KMS Key Policy 문 내에서 특정 IAM 주체(`Principal`)를 **명시적으로 지정하여 권한을 승인**한다
```
Principal: { AWS: "arn:aws:iam::123456789012:user/Alice" }
```
- Alternative is Default KMS Key + IAM Policy

<br>

### Key Policy Comparison
- AWS Owned Keys : 사용자는 Key Policy를 조회할 수도 없고, 수정할 수도 없다
- AWS Managed Keys : 사용자가 Key Policy를 조회할 수는 있지만, 수정할 수는 없다
- AWS Customer Managed Keys : 사용자가 Key Policy 조회 및 수정 모두 가능하다 

<br>

## Key Grants
- Key Policy 나 IAM Policy 를 직접 수정하지 않고 API 호출(`CreateGrant`)을 통해 특정 IAM Principal에게 KMS 키 사용 권한을 임시로 부여하는 메커니즘
- 다른 AWS 계정이나 내 계정 내부의 IAM User/Role에게 특정 KMS 키에 대한 접근 권한을 부여할 수 있다 
- encrypt, decrypt, sign, and verify 등 다양한 암호화 작업뿐만 아니라, 다른 Grant를 생성할 수 있는 권한까지 지정해 만들 수 있다
- **하나의 KMS 키**에 대해 하나 이상의 IAM Principal을 대상으로 지정하여 발급된다
- Grant 가 부여되면 해당 IAM Principal은 Grant에 명시된 Operation들을 실행할 수 있다 
- Grant 는 시간이 지난다고 **자동으로 만료되지 않는다**. 필요 없을 때는 수동으로 직접 회수해야 한다 (`RevokeGrant` 또는 `RetireGrant`)
- KMS Key Policy나 IAM Policy 등 기존 **정책 문서를 전혀 수정할 필요가** 없다
#### Creating a KMS Key Grant
- KMS Grant는 AWS CLI의 `aws kms create-grant` 명령어나 AWS SDK(Java, Python 등) 코드를 통해 생성한다
- 임시 작업이 끝나면 `aws kms revoke-grant` 또는 `aws kms retire-grant` 명령어로 반드시 **수동 삭제/철회**를 해줘야한다
- AWS Management Console 웹 화면에서는 Grant를 생성하거나 직접 목록을 관리하는 메뉴 UI를 기본 지원하지 않으며, CLI나 SDK/API로만 다뤄야 한다 
```bash
# Grant 생성 (CLI)
aws kms create-grant \
  --key-id 1234abcd-12ab-34cd-56ef-1234567890ab \
  --grantee-principal arn:aws:iam::123456789012:role/MyRole \
  --operations "Decrypt" "GenerateDataKey"

# Grant 철회 (사용 완료 후)
aws kms revoke-grant \
  --key-id 1234abcd-12ab-34cd-56ef-1234567890ab \
  --grant-id <생성된_Grant_ID>
```

<br>

### AWS Service Usage
- 저장 데이터(Data at rest)를 암호화하기 위해 KMS와 연동되는 AWS 서비스들(EBS, S3, DynamoDB 등)이 내부적으로 Grant를 매우 흔하게 사용한다
- AWS 서비스는 계정 내 사용자를 대리하여 Grant를 생성하고, 부여받은 권한으로 필요한 작업을 완료한 후 Grant를 반납한다
- AWS 서비스가 사용자를 대신해 Grant를 만들려면, 정작 명령을 내리는 **사용자(IAM Principal) 본인의 IAM Policy에 `kms:CreateGrant` 권한이 허용**되어 있어야 한다
```json
{
  "Sid": "Allow attachment of persistent resources",
  "Effect": "Allow",
  "Principal": {
    "AWS": "arn:aws:iam::123456789012:user/ExampleUser"
  },
  "Action": [
    "kms:CreateGrant",
    "kms:ListGrants",
    "kms:RevokeGrant"
  ],
  "Resource": "*",
  "Condition": {
    "Bool": {
      "kms:GrantIsForAWSResource": true
    }
  }
}
```

<br>

### Troubleshooting: Can’t Start an EC2 Instance with Encrypted EBS Volume
> 암호화된 EBS 볼륨이 연결된 EC2 인스턴스가 시작되지 않을 때 

#### 원인
- EBS 볼륨 암호화에 사용된 KMS 키가 Disabled dr Deleted 상태인 경우
- EBS 서비스가 해당 KMS 키를 사용할 권한(KMS Grant 생성 권한 등)을 부여받지 못한 경우
#### 해결 방법
- KMS 키가 실제로 존재하고 Enabled 상태인지 먼저 확인
- 명령을 내리는 IAM Principal을 대리하여 EBS 서비스가 KMS Grant를 생성할 수 있는 권한이 제대로 허용되어 있는지 확인
- `kms:GrantIsForAWSResource: true` 조건을 추가하여 AWS 서비스가 사용자를 대신해 Grant를 정상 생성할 수 있도록 허용한다

<br>

## Condition Keys
- `kms:ViaService` : 지정한 특정 AWS 서비스에서 들어오는 요청으로 제한한다
```json
"Condition": {
  "StringEquals": {
    "kms:ViaService": [
      "ec2.us-west-2.amazonaws.com",
      "rds.us-west-2.amazonaws.com"
    ]
  }
}
```
- `kms:CallerAccount` : 특정 AWS 계정에 속한 모든 자격 증명의 KMS 키 접근 권한을 한 번에 허용하거나 거부한다. (ex> API를 호출한 주체가 반드시 계정ID 123456789012 소속이어야 한다)
```json
{
  "Effect": "Allow",
  "Principal": {
    "AWS": "*"
  },
  "Action": [
    "kms:Encrypt",
    "kms:Decrypt",
    "kms:ReEncrypt*",
    "kms:GenerateDataKey*",
    "kms:CreateGrant",
    "kms:DescribeKey"
  ],
  "Resource": "*",
  "Condition": {
    "StringEquals": {
      "kms:CallerAccount": "123456789012",
      "kms:ViaService": "ec2.us-west-2.amazonaws.com"
    }
  }
}
```

<br>

## KMS Key Authorization Process
- **동일 계정**: Key Policy에서 IAM 위임을 해두었다면, **IAM Policy만 허용되어도 OK**
- **다른 계정 (Cross-Account)**: Key Policy에서 상대 계정을 허용(위임)해 주는 것은 기본이고, **상대 계정 내부의 IAM Policy에서도 반드시 허용을 해줘야만 최종 접근 가능**
- 전체 평가 로직
```
1. SCP, VPC Endpoint Policy, Key Policy 등에 단 하나라도 DENY 구문이 있으면 즉시 거부
2. KMS 키 자율성으로 인해, Key Policy 또는 Key Grant에서 먼저 허용해주어야 한다
3.
Key Policy에서 유저를 직접 허용했거나 Grant가 허용한 경우 → 동일 계정일 때 ALLOW
Key Policy가 계정에 권한을 위임한 경우 → 호출자의 IAM Policy에서도 추가로 허용해 줘야 최종 ALLOW
```

<br>

### KMS Key Cross-Account Access
#### 기본 Cross-Account 접근 (직접 권한 위임)
- Account B의 IAM User/Role이 Account A의 KMS 키를 직접 사용하여 파일/데이터를 암복호화하는 경우
- **KMS Key Policy (Account A)**: `Principal`을 `arn:aws:iam::123456789012:root` (Account B 전체)로 지정하여 Account B에게 권한을 위임한다
- **IAM Policy (Account B)**: Account B 내부의 IAM User/Role에 Account A의 KMS 키 ARN을 `Resource`로 명시한 암복호화 권한을 허용한다
- Key Policy(외부 위임)와 IAM Policy(내부 허용) **두 곳 모두 설정**되어야 작동한다
#### Cross-Account + AWS 서비스 사용
- Account B의 IAM User/Role이 Account A의 KMS 키로 암호화된 EBS Volume 같은 AWS 서비스 리소스를 다루는 경우
- `kms:CreateGrant`, `kms:ListGrants`, `kms:RevokeGrant` 권한이 **Key Policy와 IAM Policy 양쪽에 모두 포함**되어야 EBS 서비스가 사용자를 대리해 Grant를 만들어 볼륨을 복호화할 수 있다
#### IAM Role AssumeRole를 통한 Cross-Account 접근
- Account A의 KMS Key Policy를 수정하여 타 계정을 등록하는 대신, **Account B의 유저가 Account A 내부의 IAM Role을 임시로 맡아서(AssumeRole)** 키를 사용하는 패턴
- **KMS Key Policy (Account A)**: 자기 계정(`111122223333:root`)만 허용하는 일반적인 정책을 유지
- **IAM Role (Account A)**: Account B의 유저가 이 Role을 맡을 수 있도록 신뢰 관계를 맺고, 이 Role에 KMS 키 사용 권한(IAM Policy)을 붙여둔다
- **Account B User**: `sts:AssumeRole`을 호출하여 Account A의 Role 자격 증명을 얻은 뒤 KMS 키를 조작한다
#### Sharing KMS Encrypted RDS DB Snapshots
- KMS 고객 관리형 키(CMK)로 암호화된 RDS DB 스냅샷을 다른 AWS 계정과 공유할 수 있다 
- 스냅샷을 넘겨주기 전에 반드시 KMS Key Policy를 사용해 암호화에 쓰인 CMK 권한을 Target Account인 Account B(`444455556666`)에 먼저 공유해 두어야 한다
```json
{
  "Version": "2012-10-17",
  "Id": "key-policy-share-rds-snapshot",
  "Statement": [
    {
      "Sid": "AllowAccountBToUseCMK",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::444455556666:root"
      },
      "Action": [
        "kms:Encrypt",
        "kms:Decrypt",
        "kms:ReEncrypt*",
        "kms:GenerateDataKey*",
        "kms:DescribeKey",
        "kms:CreateGrant",
        "kms:ListGrants",
        "kms:RevokeGrant"
      ],
      "Resource": "*"
    }
  ]
}
```
- Account B의 IAM User/Role이 Account A의 CMK를 사용해 스냅샷으로부터 RDS DB를 복원할 수 있도록 허용한다
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowUseAccountAKMSKeyForRDS",
      "Effect": "Allow",
      "Action": [
        "kms:Encrypt",
        "kms:Decrypt",
        "kms:ReEncrypt*",
        "kms:GenerateDataKey*",
        "kms:DescribeKey",
        "kms:CreateGrant",
        "kms:ListGrants",
        "kms:RevokeGrant"
      ],
      "Resource": "arn:aws:kms:us-east-1:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab"
    }
  ]
}
```
- AWS Managed Key(ex> aws/rds)로 암호화된 스냅샷은 타 계정과 직접 공유할 수 없다

<br>

## Asymmetric Encryption
- 연관된 한 쌍의 키인 공개키(Public Key)와 개인키(Private Key)를 사용해 데이터를 암호화하고 복호화하는 방식
- 공개키는 누구나 사용할 수 있도록 **외부에 공유**할 수 있고, 개인키는 절대로 유출되지 않도록 **안전하게 비밀로 보관**해야 한다
- KMS supports 3 types of asymmetric KMS keys:
  - RSA KMS Keys : 데이터 암호화/복호화, 디지털 서명/검증 목적으로 사용
  - Elliptic Curve (ECC) KMS Keys : 오직 디지털 서명 및 검증 목적으로 사용
  - SM2 KMS Keys (China Regions only) : 중국 리전 전용, 암복호화 및 서명/검증 지원
- 개인키(Private Key)를 사용한 복호화나 서명은 반드시 **KMS 내부에서만 실행**된다

### Digital Signing
- 서로 다른 시스템 간에 전송되는 메시지나 파일의 무결성(Integrity)을 검증할 수 있도록 도와준다
- 전송 과정에서 파일이 중간에 **조작되거나 위변조(Tampered)되지 않았음**을 입증한다
- **서명(Signing)**: KMS 내부의 Private Key(개인키)를 사용해 디지털 서명을 생성힌디
- **검증(Verification)**: 외부에 공개된 Public Key(공개키)를 사용해 누구나 서명의 유효성을 검증할 수 있다
- Use cases: document e-signing, secure messaging, authentication/authorization tokens, trusted source code, e-commerce transactions, …
- 동작 순서
```
1. 큰 용량의 파일 전체를 KMS로 보낼 수 없으므로, 파일의 SHA-256 해시값을 먼저 계산
2. 계산된 해시값을 KMS API(Sign)로 전달하면, KMS는 하드웨어 내부에 안전하게 보관된 Private Key로 해시값을 암호화(서명)하여 서명 데이터(Signature)를 반환
3. 수신자는 Public Key를 다운로드받아 KMS API(Verify)를 호출하거나 로컬에서 직접 서명을 검증
```

<br>

## KMS API Calls Limits & Data Key Caching
- 매번 KMS API를 호출해 데이터 키를 새로 생성하는 대신, 발급받은 데이터 키를 메모리에 캐싱하여 재사용할 수 있다
- 애플리케이션에서 KMS API를 대량으로 호출하여 초당 요청 수 제한 에러가 발생하는 상황을 전제로 한다 
- **Data Key Caching**을 도입하면 데이터를 암/복호화할 때 발급받았던 데이터 키를 메모리에 저장해두고 재사용할 수 있다
- AWS CLI나 일반 SDK가 아닌, 애플리케이션 클라이언트 라이브러리인 **AWS Encryption SDK**를 사용해 구현한다
- 암호화 보안 모범 사례(Best Practice)에서는 동일한 데이터 키를 너무 자주 재사용하는 것을 권장하지 않는다. 비용/성능과 보안 수준 간의 타협 필요
- AWS Encryption SDK에서 Data Key Caching을 적용할 때는 키의 남용을 막기 위해 Max Age, Max Bytes, Max Messages을 설정하여 일정 시간/용량이 지나면 캐시를 자동 파기하도록 구성한다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)