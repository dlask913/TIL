## (aws) Identity and Access Management - STS
> How Authorization Works in Amazon S3, Bucket Operations vs Object Operations, Cross-Account Access to Objects in S3 Buckets, Force HTTPS In Flight, VPC Gateway Endpoint for S3, VPC Interface Endpoint for S3

<br>

## How Authorization Works in Amazon S3
#### User Context
- 1차적으로 요청자 본인의 계정에 붙은 IAM 정책(Identity-Based Policy)에 S3 접근 허용 구문이 있는지 확인한다
- 요청자의 계정이 버킷이나 객체를 소유하고 있다면, 버킷 정책/ACL 또는 객체 ACL을 평가한다
- 계정이 버킷/객체를 직접 소유하고 있다면, IAM Policy이나 리소스 기반 정책(Bucket Policy) 중 하나만 사용해서 권한을 부여해도 된다
#### Bucket Context
- 버킷을 소유한 AWS 계정의 정책들을 평가(check for Explicit Deny)
#### Object Context
- 요청자는 객체 소유자로부터 권한 허가를 받아야 한다 (Object ACL 사용)
- 버킷 소유자와 객체 소유자가 같다면, Bucket Policy 를 사용하여 접근이 허용된다 
- 버킷 소유자과 객체 소유자가 다르면, 객체 소유자의 Obejct ACL을 통해 접근 권한이 판정된다
- 버킷 안의 모든 객체를 버킷 소유자가 소유하고 버킷 정책 및 IAM 정책만 사용하여 권한을 관리하고 싶다면, Object Ownership 설정에서 Bucket Owner Enforced 를 활성화한다 → 버킷 및 객체의 ACL 기능 비활성화 (recommend)

<br>

### Bucket Operations vs Object Operations
- `s3:ListBucket`, `s3:GetBucketLocation` 등 버킷 수준 작업은 Resource 경로에 버킷 이름까지만 적는다 (`arn:aws:s3:::test`)
- `s3:GetObject`, `s3:PutObject`, `s3:DeleteObject` 등 객체 수준 작업은 버킷 내부 객체들에 대한 작업이므로 버킷 이름 뒤에 /* 를 붙여서 지정해야 한다 (`arn:aws:s3:::test/*`)
- S3 정책을 작성할 때는 **목록 조회(`ListBucket`)용 버킷 ARN**과 **파일 읽기/쓰기(`GetObject`/`PutObject`)용 객체 ARN(`/*`)** 두 가지 Statement를 나누어 지정하는 것이 권장된다

<br>

### Cross-Account Access to Objects in S3 Buckets
#### 1. IAM Policies and S3 Bucket Policy (IAM 정책 + S3 버킷 정책)
- 요청을 보내는 쪽(B 계정)에서는 IAM 정책으로 허용하고 버킷을 소유한 쪽(A 계정)에서는 S3 Bucket Policy 로 B 계정의 접근을 허용한다
#### 2. IAM Policies and Access Control Lists (ACLs) (IAM 정책 + ACL)
- S3 버킷/객체 ACL을 사용해 타 계정에 권한을 주는 방식
- ACL 방식은 'Bucket Owner Enforced(버킷 소유자 강제)' 설정이 비활성화되어 있을 때만 동작한다
- 기본적으로 새롭게 생성되는 모든 S3 버킷은 Bucket Owner Enforced가 활성화되어 있다
#### 3. Cross-Account IAM Roles (교차 계정 IAM 역할)
- B 계정의 사용자가 A 계정에 정의된 IAM Role 을 AssumeRole 하여 마치 A 계정 내부 사용자인 것처럼 S3에 접근하는 방식

<br>

### Canned ACL Object Permissions in Cross-Account Setting
- 타 계정(Cross-Account) 사용자가 S3 버킷에 파일을 올릴 때 버킷 소유자가 파일 제어권을 빼앗기지 않도록 `x-amz-acl` 헤더 조건을 강제할 수 있다
- Canned ACL (사전 정의된 ACL) : AWS 에서 자주 쓰이는 대표적인 권한 모음들을 정의 해둔 템플릿
- `x-amz-acl: bucket-owner-full-control` 를 강제하면 업로드된 파일의 제어권이 버킷 소유자에게 부여된다
- 복잡성과 보안 관리의 어려움으로 인해 AWS는 Canned ACL 사용을 지양한다
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "RequireBucketOwnerFullControl",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:PutObject",
      "Resource": "arn:aws:s3:::my-bucket/*",
      "Condition": {
        "StringNotEquals": {
          "s3:x-amz-acl": "bucket-owner-full-control"
        }
      }
    }
  ]
}
```

<br>

### Force HTTPS In Flight
- S3 버킷에 접근할 때 보안을 위해 암호화되지 않은 HTTP 요청은 차단하고 반드시 HTTPS만 사용하도록 강제할 수 있다 
```json
{
  "Version": "2012-10-17",
  "Statement": [
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
  ]
}
```

<br>

### VPC Gateway Endpoint for S3
- 비용이 발생하지 않는다 (무료)
- 해당 VPC 내부에 존재하는 EC2, Lambda 등의 리소스만 이 엔드포인트를 통해 인터넷 망을 거치지 않고 S3 로 안전하게 접근할 수 있다 
- VPC 의 `enableDnsSupport` 옵션이 `true` 여야만 한다
- 기존 S3 의 퍼블릭 DNS 주소를 그대로 계속 사용한다 
- EC2 에서 S3 로 나가는 요청이 차단되지 않도록 Security Group Outbound 에 S3 Prefix List 나 HTTPS 접근이 허용되어 있어야 한다 

<br>

### VPC Interface Endpoint for S3
- 서브넷마다 실제 private IP 를 갖는 가상 랜카드(ENI)가 생성되며, 여기에 Security Group 을 붙여 접근 제어를 할 수 있다 
- 온프레미스에서 VPN 이나 Direct Connect 를 통해 접근할 수 있다 
- AZ 당 시간당 $0.01의 비용이 발생한다
- 서비스의 public hostname 이 private interface 엔드포인트 hostname 으로 해석된다 
- VPC 설정인 "Enable DNS Hostnames"와 "Enable DNS Support" 가 모두 true 여야 한다
- S3용 VPC 인터페이스 엔드포인트에는 Private DNS name 전용 옵션이 제공되지 않는다 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)