## (aws) Infrastructure Security - IAM Policy 3
> IAM Credentials Report, Roles for Services, Roles Anywhere, Trust Policies, Security Tools, Access Analyzer

<br>

## IAM Credentials Report
- AWS 계정 내 모든 IAM User 의 보안 관련 설정과 상태를 볼 수 있다
- 누가 비밀번호를 언제 바꿨는지, Access Key를 마지막으로 언제 썼는지, MFA를 등록했는지 등을 모아서 보여준다
- IAM 콘솔, AWS API, CLI, SDK를 사용해 다운로드할 수 있다
- 감사 및 컴플라이언스를 준수하는 데 유용하다 
- 최대 4시간에 한 번 생성된다 

### Managing Aged Access Keys through AWS Config Remediations
#### 1. AWS Config (감지)
- Account A 에서 Config 가 IAM 액세스 키 상태 감지 (`access-keys-rotated`)
- 액세스 키가 생성된 지 90일 이상 되었으면 Non-compliant 로 판단하여 다음 단계 트리거
#### 2. SSM Automation (자동 조치)
- AWS Conifg 에 설정된 Remediation 기능이 SSM Automation 을 실행한다 
- SSM 은 Rotate Access Keys 작업을 자동으로 처리
#### 3. SNS / Jira / Slack (알림 및 외부 연동)
- SSM 이 이벤트를 마스터 계정의 SNS Topic 으로 보낸다
- SNS 는 이 메시지를 받아 SQS Queue 로 전달하거나 담당자에게 이메일을 보낸다
- 동시에 SSM 은 Jira, Slack, API Endpoints 로 교체 내역 및 알림을 전송한다 

<br>

## IAM Roles for Services
- 사람이 아닌 AWS 서비스에 권한을 부여하기 위해 사용하는 IAM Role
- EC2 가 S3 에 파일을 저장하거나 Lambda 함수가 DB를 조회하는 등
- 서버 코드 안에 액세스 키를 직접 입력하는 것은 위험하므로 IAM Role 을 씌워주는 방식 사용
- Common roles:
  - EC2 Instance Roles
  - Lambda Function Roles
  - Roles for CloudFormation
- `iam:PassRole` 권한을 사용해 AWS 서비스에 IAM 역할을 넘겨줄 수 있다 
- PassRole 자체의 CloudTrail 로그는 찍히지 않으므로 감사를 할 때는 해당 역할을 전달받아 생성된 EC2 나 Lambda 실행 API 의 로그를 확인한다
```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Action": [
      "iam:GetRole",
      "iam:PassRole"
    ],
    "Resource": "arn:aws:iam::123456789012:role/EC2-roles-for-*"
  }]
}
```

<br>

## IAM Roles Anywhere
- AWS 외부에서 동작하는 서버나 애플리케이션도 AWS 내부 서비스처럼 IAM 역할을 사용할 수 있게 해주는 기능
- AWS 외부 서버가 AWS API 를 호출해야 할 때 임시 토큰을 통해 접근할 수 있도록 지원
- 온프레미스 서버, Docker 컨테이너, 멀티클라우드 워크로드 등을 대상으로 한다
- AWS 내/외부 워크로드에 모두 동일한 IAM 역할 및 정책을 사용할 수 있다
- AWS Access Key 와 같은 영구적인 자격 증명을 발급 받아 관리할 필요가 없다 -> PKI 사용

### IAM Roles Anywhere vs IAM Users For On-Premises server access to AWS
#### IAM Users
- 액세스 키는 만료 기간이 없다 
- 코드나 설정 파일에 하드코딩하여 GitHub 에 올리거나 로그 등에 남아 유출되기 쉽다 
- 키를 교체하는 과정이 번거롭다
- 수많은 서버에 어떤 키가 들어가 있는지 추적해야하기 때문에 보안 감사 관리가 복잡하다
- Made for real human users
#### IAM Roles Anywhere (권장)
- 신원을 증명하는 X.509 디지털 인증서로 임시 자격 증명을 요청해 받아온다
- 발급받은 AWS 자격 증명은 자동으로 만료된다
- 만료 후 인증서를 교체하거나 사설 CA 를 통해 자동으로 갱신한다
- Made for On-Premises servers

<br>

## IAM Trust Policies
- Trust Policy (신뢰 정책): "누가 이 역할을 맡을 수 있는가?"를 정의(`principal`)
- Permission Policy (권한 정책): "이 역할을 맡은 주체가 무엇을 할 수 있는가?"를 정의(`action`)
#### Trust Policy Example
- 특정 AWS 계정의 사용자에게 역할 위임하기 (MFA 필수 조건 포함)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::111122223333:root"
      },
      "Action": "sts:AssumeRole",
      "Condition": {
        "Bool": {
          "aws:MultiFactorAuthPresent": "true"
        }
      }
    }
  ]
}
```
- EC2 에 역할 부여
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```
#### Permission Policy Example
- S3 버킷 읽기전용 권한
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::my-company-bucket",
        "arn:aws:s3:::my-company-bucket/*"
      ]
    }
  ]
}
```

<br>

## IAM Security Tools
- 계정의 보안 상태를 점검하고 사용자에게 과도한 권한이 주어지지 않았는지 감시할 수 있도록 한다 
#### IAM Credentials Report (account-level)
- 계정 전체에 존재하는 모든 사용자와 그들의 다양한 자격 증명 상태를 나열한 보고서
- 비밀번호 변경 시기, 액세스 키 생성/사용 시기, MFA 등록 여부 등 보안 자격 증명 현황 확인
#### IAM Access Advisor (user-level)
- 계정 전체가 아닌 특정 사용자, 그룹, Role 개별 단위로 부여된 권한과 사용 이력 분석
- 사용자에게 부여된 서비스 권한과 서비스에 마지막으로 접근한 시점을 보여준다 
- 위 정보를 통해 IAM 정책을 수정/개선할 수 있다 

<br>

## IAM Access Analyzer
- 내 AWS 자원 중 외부에 노출되거나 의도치 않게 공유된 리소스가 있는지 자동으로 분석해주는 보안 도구
- 정상으로 간주할 신뢰 영역(Zone of Trust)을 단일 AWS 계정 또는 AWS Organization 으로 정의한다
- 신뢰 영역 외부에서의 접근을 감지하면 결과(Findings) 항목으로 경고를 생성한다
- 대상 리소스 목록
```
S3 Buckets: S3 저장소 버킷
IAM Roles: IAM 역할
KMS Keys: 암호화 키
Lambda Functions and Layers: 람다 함수 및 레이어
SQS queues: SQS 메시지 큐
Secrets Manager Secrets: 시크릿 매니저 보안 정보(비밀번호/API키 등)
```
#### IAM Access Analyzer Policy Validation (정책 검증)
- IAM 정책 문법과 권장 모범 사례에 맞게 작성되었는지 검증한다
- 일반 경고, 보안 경고, 오류, 제안 사항 등을 구분하여 보여준다
- 즉시 조치할 수 있는 실행 가능한 권장 사항을 제공한다
#### IAM Access Analyzer Policy Generation (정책 자동 생성)
- 실제 접근 활동(사용 이력)을 기반으로 IAM 정책을 자동 생성한다
- 사용자가 실제로 호출한 API 기록(CloudTrail)을 추적해 꼭 필요한 작업만 허용하는 최소 권한의 원칙 정책을 생성한다
- 최대 90일 동안의 CloudTrail 로그를 분석한다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)