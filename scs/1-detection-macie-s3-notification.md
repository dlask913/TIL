## (aws) Detection - Macie, S3 Event Notifications
> Macie - Data Identifiers/Findings/Finding Types/Multi-Account Strategy, S3 Event Notifications

<br>

## Macie
![image](https://d2908q01vomqb2.cloudfront.net/fc074d501302eb2b93e2554793fcaf50b3bf7291/2021/07/15/Macie-diagram.png)

- 완전 관리형 data security and data privacy service 로 , S3 를 분석하여 민감한 데이터 personally identifiable information (PII) 를 식별한다.
- 원하는 S3 버킷 지정하여 Macie 를 활성화 할 수 있다

<br>

### Data Identifiers
#### Managed Data Identifier 
- 특정 유형의 민감한 데이터를 감지하도록 설계된 기본 내장 기준 
- Ex> credit cards numbers, AWS Credentials, bank accounts
#### Custom Data Identifier 
- 사용자가 정의한 민감한 데이터를 감지하도록 설계된 기준
- Regular expression, keywords, proximity rule 사용 가능 
- Ex> employee IDs, customer account number → Regex: [A-Z]-\d{8} 
#### Allow Lists
- allow lists 를 사용하여 무시할 패턴을 정의할 수 있다.

<br>

### Findings
- Macie 가 발견한 잠재적 이슈나 민감한 데이터에 대한 기록
- 각 조사 결과는 severity rating, affected resource, datetime 을 포함한다
- 억제 규칙(Suppression Rules) — 특정 속성(Attribute) 기반의 필터 기준을 세워 탐지된 보안 결과(Findings)를 자동으로 보관 처리(Archive)하는 기능
- Macie 콘솔에서 90일동안 저장 및 확인 가능
-  AWS Console, EventBridge, Security Hub 에서 확인할 수 있다
#### Sensitive Data Discovery Result
- S3 분석에 대한 세부 정보를 기록하는 레코드
- Macie가 결과를 S3에 저장하도록 구성한 다음 Athena를 사용하여 쿼리한다

<br>

### Finding Types
#### Policy Findings
- 정책 위반이나 S3 보안 위협에 대한 기록
- Ex: default encryption is disabled, bucket is public, .. 
- Policy: IAMUser/S3BucketEncryptionDisabled, Policy:IAMUser/S3BucketPublic
- Macie 를 활성화해야 감지가 가능하다 
#### Sensitive Data Findings
- S3 에서 찾은 민감한 데이터 기록
- Ex: Credentials (private keys), Financial (credit card numbers), .. 

<br>

### Multi-Account Strategy
- Organization 을 통해 multiple accounts 관리할 수 있다
- Macie 초대장을 보낼 수 있고 Delegated Admin 가 가능하다.

<br>

## S3 Event Notifications
- S3 에서 SNS, SQS, Lamba Function 으로 원하는만큼 이벤트를 보낼 수 있다
- \*.jpg 와 같이 Object name 필터링도 가능하다 
- Use case : generate thumbnails of images uploaded to s3
- 이벤트 알림은 수초내로 가는데 1분 이상 걸릴 수도 있다

<br>

### IAM Permissions
- 이벤트가 목적지인 SNS, SQS, Lambda 등으로 잘 전달되려면 Resource(Access) Policy 설정을 해야한다
- SNS Topic 으로 이벤트 보낼 때 예시
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowS3ToPublishToTopic",
      "Effect": "Allow",
      "Principal": {
        "Service": "s3.amazonaws.com"
      },
      "Action": "SNS:Publish",
      "Resource": "arn:aws:sns:region:account-id:your-topic-name",
      "Condition": {
        "ArnLike": {
          "aws:SourceArn": "arn:aws:s3:::your-bucket-name"
        }
      }
    }
  ]
}
```
- AWS CLI 명령어 예시
```bash
aws lambda add-permission \
  --function-name your-function-name \
  --statement-id AllowS3Event \
  --action "lambda:InvokeFunction" \
  --principal s3.amazonaws.com \
  --source-arn arn:aws:s3:::your-bucket-name \
  --source-account your-account-id
```

<br>

### with Amazon EventBridge

```
[다양한 소스/액션]
  │ (예: 파일 업로드, 삭제 등)
  ▼
┌────────────────────────────────────────────────────────┐
│ 1. Amazon S3 Bucket                                    │
│    └─ EventBridge 알림 활성화 (Send notifications)      │
└─────────────────────────┬──────────────────────────────┘
                          │
                          │ (자동 연동 / IAM 권한 불필요)
                          ▼
┌────────────────────────────────────────────────────────┐
│ 2. Amazon EventBridge (Default Event Bus)              │
│    │                                                   │
│    ├─► [ Rule A ] (예: .jpg 파일 생성만 필터링)         │
│    ├─► [ Rule B ] (예: 삭제 이벤트만 필터링)             │
│    └─► [ Rule C ] (예: 모든 이벤트 매칭)                │
└─────────────────────────┬──────────────────────────────┘
                          │
                          │ (각 Rule당 최대 5개의 Target 지정 가능)
                          ▼
┌────────────────────────────────────────────────────────┐
│ 3. Destinations (18+ AWS Services)                     │
│    ├─► Lambda (서버리스 코드 실행)                     │
│    ├─► SQS / SNS (메시지 대기열 및 푸시 알림)         │
│    ├─► Step Functions (워크플로우/오케스트레이션)      │
│    ├─► Kinesis Data Firehose (로그/분석 데이터 스트림) │
│    ├─► ECS Task (컨테이너 작업 실행)                  │
│    └─► API Destination (외부 서드파티 웹훅/API 호출)   │
└────────────────────────────────────────────────────────┘
```

- JSON rules 을 통한 정밀 필터링 ( metadata, object size, name .. )
- multiple destinations ( ex> step functions, kinesis streams / firehose .. ) 
- Eventbridge 고유 기능들 - 이벤트 보관 (archive), 이벤트 재처리 (replay events), 신뢰성 있는 전달 (reliable delivery)

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)