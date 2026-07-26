## (aws) Infrastructure Security - VPC Endpoints
> VPC Endpoint Gateway, VPC Endpoints Interface, VPC Endpoint Policy, Examples

<br>

## VPC Endpoints
- VPC 엔드포인트를 사용하면 퍼블릭 인터넷을 거치지 않고 AWS 사설 네트워크를 통해 AWS 서비스(S3, DynamoDB, SQS 등)에 비공개로 안전하게 연결할 수 있다
- AWS 완전 관리형으로, 수평적으로 자동 확장되며 다중 가용 영역(AZ) 기반 이중화되어있다 (redundant)
- IGW, NAT 와 같은 외부 인터넷 접속 장비가 필요 없다
- VPC Endpoint Gateway (S3 & DynamoDB) : Route Table 에 대상 경로를 추가하여 동작
- VPC Endpoint Interface (all services) : 서브넷 내에 ENI 를 생성하여 통신
- **In case of issues**: Check DNS Setting Resolution in your VPC / Check Route Tables
  - Interface Endpoint 사용 시 VPC 설정에서 enableDnsHostnames 와 enableDnsSupport 가 모두 True 켜져 있는지 확인
  - Gateway Endpoint 사용 시 해당 프라이빗 서브넷의 Route Table 에 S3/DynamoDB 엔드포인트 타깃이 올바르게 등록되어 있는지 확인
- cross-region endpoint 를 지원한다

<br>

### VPC Endpoint Gateway
> EC2 -> Route table -> VPC Endpoint Gateway -> S3

![image](https://docs.aws.amazon.com/images/vpc/latest/privatelink/images/without-gateway-endpoints.png)

- 오직 S3 와 DynamoDB 에서 동작하며 VPC 당 하나의 gateway 를 생성해야 한다
- route tables 에는 대상 항목을 꼭 업데이트해야 하며 Gateway Endpoint 자체에는 보안 그룹을 적용할 수 없다 (no security groups)
- Gateway 는 특정 서브넷 안에 종속되어 생성되는 것이 아니라 VPC level 에서 정의된다
- VPC 설정에서 DNS resolution 이 꼭 활성화되어있어야 한다
- S3 에 접근할 때 기존에 사용하던 동일한 퍼블릭 엔드포인트 호스트 이름(예: `s3.us-east-1.amazonaws.com`)을 그대로 사용할 수 있다
- Gateway endpoint 는 VPC 외부로 확장될 수 없고 VPC 내부에서 직접 출발하는 트래픽만 처리할 수 있다 
- 무료이다

<br>

### VPC Endpoints Interface
![image](https://docs.aws.amazon.com/images/vpc/latest/privatelink/images/use-cases.png)

- VPC 내부 서브넷에 private endpoint interface hostname 을 갖는 ENI(Elastic Network Interface, 가상 랜카드)가 직접 배포되며, 이를 통해 AWS 서비스와 통신한다
- Gateway 방식과 달리 ENI 에 보안 그룹을 붙여 특정 IP/인스턴스만 접속할 수 있도록 인바운드 트래픽을 제어할 수 있다 
- Private DNS (엔드포인트 생성 시 설정 가능) : AWS 표준 서비스 도메인이 자동으로 엔드포인트 ENI 의 프라이빗 IP 를 가리키도록 Route 53 private 호스팅 영역이 백엔드에서 작동
  - 해당 서비스의 public hostname(ex: athena.us-east-1.amazonaws.com)이 private endpoint interface hostname 으로 자동 해석된다.
  - Private DNS 기능이 동작하기위해 PC 설정에서 `Enable DNS hostnames`와 `Enable DNS Support` 두 항목이 모두 True 여야한다
  - Example of Athena
```
- vpce-0b7d2995e9dfe5418-mwr ths3x.athena.us-east-1.vpce.amazonaws.com
- vpce-0b7d2995e9dfe5418-mwr ths3x-us-east-1a.athena.us-east-1.vpce.amazonaws.com 
- vpce-0b7d2995e9dfe5418-mwr ths3x-us-east-1b.athena.us-east-1.vpce.amazonaws.com
- athena.us-east-1.amazonaws.com (private DNS name)
```
- 인터페이스 엔드포인트는 AWS Direct Connect(DX)나 Site-to-Site VPN을 통해 온프레미스(사무실/IDC) 환경에서도 접근할 수 있다.

<br>

### VPC Endpoint Policy
- AWS 서비스에 접근하기위해 어떤 AWS principals(AWS accounts, IAM users, IAM Roles) 가 VPC Endpoint 를 사용할 것인지 조정
- Interface Endpoint 와 Gateway Enpoint 모두에 붙일 수 있다
- 특정 리소스에 특정 API 호출만 허용/거부하도록 제한할 수 있다
- IAM 사용자/역할 정책(Identity-based)이나 서비스 보안 정책(예: S3 버킷 정책)을 덮어쓰거나 대체하지 않는다
- `aws:PrincipalOrgId` 를 사용하여 조직 계정에서만 사용되도록 접근을 제한할 수 있다

#### Authorization logic. VPC Endpoint Policy + Identity-based Policy
1. IAM User (Explicit Allow, No Explicit Deny)
  - S3 서비스 전반에 대한 읽기/쓰기 기본 권한 허용
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowS3Actions",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": "*" <!-- 모든 버킷 -->
    }
  ]
}
```
2. VPC Endpoint Policy (Explicit Allow)
  - Endpoint 를 통과할 수 있는 대상을 오직 회사 전용 버킷으로 제한
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowOnlyCompanyBucketThroughEndpoint",
      "Effect": "Allow",
      "Principal": "*",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [ <!-- 버킷 제한 -->
        "arn:aws:s3:::company-data-bucket",
        "arn:aws:s3:::company-data-bucket/*"
      ]
    }
  ]
}
```

#### Authorization logic. VPC Endpoint Policy + Resource-based Policy + IAM Role
1. IAM Role (Explicit Allow, No Explicit Deny)
  - 애플리케이션이 `prod-data-bucket` 버킷에 접근할 수 있도록 권한 허용
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowAppAccessToProdBucket",
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [ <!-- 버킷 제한 -->
        "arn:aws:s3:::prod-data-bucket",
        "arn:aws:s3:::prod-data-bucket/*"
      ]
    }
  ]
}
```

2. VPC 엔드포인트 정책 (Explicit Allow)
  - `aws:PrincipalOrgId` 조건을 사용하여 외부 타사 AWS 계정 요청을 차단하고 사내 OU 소속 계정만 허용
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "AllowOnlyOurOrganizationPrincipals",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": "*",
      "Condition": {
        "StringEquals": { 
          "aws:PrincipalOrgId": "o-a1b2c3d4e5"
        }
      }
    }
  ]
}
```

3. S3 버킷 정책 (Explicit Allow, No Explicit Deny)
  - `aws:sourceVpce` 조건을 이용해 지정된 특정 VPC 엔드포인트를 통하는 요청만 허용
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "RestrictAccessToSpecificVPCEndpoint",
      "Effect": "Allow",
      "Principal": "*",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::prod-data-bucket",
        "arn:aws:s3:::prod-data-bucket/*"
      ],
      "Condition": {
        "StringEquals": {
          "aws:sourceVpce": "vpce-1234567890abcdef0"
        }
      }
    }
  ]
}
```

<br>

## VPC Endpoint Examples
### CodeDeploy ([공식문서](https://docs.aws.amazon.com/codedeploy/latest/userguide/vpc-endpoints.html))
- EC2 에서 VPC Interface Endpoint 를 통해 CodeDeploy 에 직접 명령을 보낼 수 있다
- EC2 는 codedelploy 와 agent 와 관련된 vpc endpoint 2개에 대해 접근해야한다
```
1. com.amazonaws.<region>.codedeploy : 배포 제어 및 컨트롤 플레인 API 호출용
2. com.amazonaws.<region>.codedeploy-commands-secure : EC2 내부에서 실행되는 CodeDeploy Agent가 배포 명령을 수신하고 폴링(polling)하는 전용 엔드포인트
```
- ECS 와 lambda 는 codedploy endpoint 에만 접근하면 된다

<br>

### Secrets Manager
```
┌────────────────────────────────────────────────────────────────────────┐
│ Amazon VPC                                                             │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Private Subnet                                                 │   │
│   │                                                                │   │
│   │                     (1) connect                                │   │
│   │                ┌─────────────────► [ Amazon RDS ]              │   │
│   │                │                    (DB Instance)              │   │
│   │                │                                               │   │
│   │   [ Lambda ] ──┤                                               │   │
│   │   Function     │                                               │   │
│   │                │ (2) get DB password                           │   │
│   │                └─────────────────► [ VPC Interface Endpoint ]  │   │
│   │                                      com.amazonaws.<region>.   │   │
│   │                                      secretsmanager            │   │
│   │                                                  │             │   │
│   └──────────────────────────────────────────────────┼─────────────┘   │
│                                                      │                 │
└──────────────────────────────────────────────────────┼─────────────────┘
                                  AWS Internal Network │ 
                                                       ▼ (No Internet/NAT)
                                                   [ AWS Secrets Manager ]
```
- VPC Private Subent에 위치한 Lambda 함수가 외부 인터넷 경로를 거치지 않고 Secrets Manager 에서 DB 비밀번호를 가져와 RDS 에 연결할 수 있다 
- IGW 나 NAT 없이 격리된 private network 안에서 정보를 안전하게 조회할 수 있다
- Lambda 가 VPC 내부 Interface Endpoint 로 요청을 보내면 트래픽이 ENI 를 통해 AWS 내부망으로 Secrets Manager 서비스에 전달
- Private DNS 기능 덕분에 Lambda 는 별도 URL 변경 없이 기존 엔드포인트를 이용
- VPC Interface Endpoint의 Security Group : Lambda 함수(또는 Lambda가 속한 Security Group)로부터의 **HTTPS (443 포트) Inbound 트래픽을 허용**해야 한다

<br>

### SSM Session Manager
- 인터넷이 차단된 private subent 의 EC2 인스턴스에 SSH/인바운드 포트 개방 없이 AWS SSM Session Manager 로 원격 접속이 가능하다 
- 필수 엔드포인트 
```
1. com.amazonaws.<region>.ssm : SSM 서비스 기본 엔드포인트
2. com.amazonaws.<region>.ssmmessages : Session Manager 전용 제어/데이터 채널 엔드포인트
3. com.amazonaws.<region>.ec2messages : SSM Agent와 AWS 시스템 간의 메시지 송수신 통로
```
- 보안 그룹 및 트래픽 방향 규칙
  - EC2 : Inbound 0개 / Outbound HTTPS 443 Allow
  - VPC Interface Endoint : Inbound HTTPS 443 Allow

<br>

### API Gateway
> VPC 내부 EC2 → VPC Interface Endpoint → API Gateway → Private API

- API Gateway 의 Private API 는 반드시 VPC Interface Endpoint 를 통해서만 접근할 수 있다 
- VPC Endpoint Policy 와 API Gateway Resource Policy 를 함께 조합하여 사용할 수 있다 
- API Gateway 리소스 정책에서 `aws:SourceVpc` 또는 `aws:SourceVpce` 조건 키를 사용하여 **특정 VPC나 특정 VPC 엔드포인트에서 들어오는 트래픽만 허용**하도록 제한할 수 있다
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "execute-api:/*"
    },
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "execute-api:/*",
      "Condition": { 
        "StringNotEquals": { <!-- 제한 -->
          "aws:SourceVpc": "vpc-1a2b3c4d"
        }
      }
    }
  ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)