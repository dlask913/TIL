## (aws) Identity and Access Management - IAM Policy
> Structure, NotAction with Allow&Deny, Restrict to One Region (NotAction), Principal Options

<br>

## IAM Policy Structure
- `Version` : 사용하려는 IAM Policy Language 버전 정의
- `Id` : 정책을 식별하기 위해 부여하는 식별자 (Optional)
- `Statement` : 하나 이상의 개별 권한 statement 를 배열 형태로 묶어 관리하는 정책의 필수 요소
- `Sid` : 개별 구문을 구별하기 위해 부여하는 임의 ID (Optional)
- `Effect` : 해당 구문의 요청 처리 방식을 지정. Allow 또는 Deny 중 하나 설정
- Principal : 접근 허용 또는 거부 대상이 되는 사용자, 계정, 역할 등의 보안 주체를 지정. 리소스 기반 정책 등에서 필수로 요구된다 
- `Action` : 정책을 통해 허용하거나 거부할 수행 작업 목록
- `Resource` : Action 이 적용될 AWS 리소스의 ARN(Amazon Resource Name) 목록
- `Condition` : 해당 정책 구문이 적용되기 위한 구체적인 조건 정의 (Optional)

```json
{
  "Version": "2012-10-17",
  "Id": "S3-Account-Permissions",
  "Statement": [
    {
      "Sid": "1",
      "Effect": "Allow",
      "Principal": {
        "AWS": [
          "arn:aws:iam::123456789012:root"
        ]
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject"
      ],
      "Resource": [
        "arn:aws:s3:::mybucket/*"
      ],
      "Condition": {
        "StringEquals": {
          "aws:username": "johndoe"
        }
      }
    }
  ]
}
```

<br>

### NotAction with Allow
- `NotAction` 과 `"Effect": "Allow"` 를 함께 사용할 때, `NotAction` 에 명시된 작업을 제외한 모든 AWS 서비스 동작을 허용한다
- `NotAction` : 지정한 작업을 제외한 모든 작업을 선택한다. 
- 보안상 NotAction 에 적히지 않은 다른 모든 서비스의 작업을 의도치 않게 허용하게 될 수 있으니 매우 주의하여 사용해야한다. 
#### CASE1. IAM 관련 모든 작업을 제외한 작업을 허용
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "NotAction": "iam:*",
      "Resource": "*"
    }
  ]
}
```
#### CASE2. 리소스 범위와 조합하여 특정 작업만 제외
- S3 리소스에 대해 버킷 삭제 작업만 제외하고 나머지 모든 S3 작업(조회, 업로드, 객체 삭제 등)을 허용
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "NotAction": "s3:DeleteBucket",
      "Resource": "arn:aws:s3:::*"
    }
  ]
}
```

<br>

### NotAction With Deny
- NotAction 에 명시된 작업을 제외한 나머지 모든 작업의 접근을 명시적으로 거부할 떄 사용한다
- AWS 에서는 **Explicit Deny가 모든 Allow보다 항상 우선**하기 때문에 NotAction 목록에 없는 모든 작업은 다른 정책에서 Allow 를 주었더라도 막힌다
- NotAction 에 지정된 작업(`iam:*`)은 Deny 되지 않을 뿐 자동으로 Allow 되는 게 아니기 때문에 사용하려면 별도 Allow 정책이 반드시 존재해야 한다 
```json
/* IAM 권한만 열어두고, 다른 모든 서비스 작업은 MFA 인증 전까지 전부 차단 */
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Deny",
      "NotAction": "iam:*",
      "Resource": "*",
      "Condition": {
        "BoolIfExists": {
          "aws:MultiFactorAuthPresent": "false"
        }
      }
    }
  ]
}
```

<br>

### Restrict to One Region (NotAction)
- NotAction 과 Deny 를 조합하여 특정 Region 으로 접근을 제한할 수 있다 
- 글로벌 서비스(IAM, CloudFront 등)나 특정 서비스(S3 등)가 리전 제약 때문에 마비되는 현상을 방지하기 위해 사용
#### CASE1. eu-central-1 외 모든 리전 차단 (글로벌 서비스 제외)
- 글로벌 서비스는 특정 리전에 종속되지 않기 때문에 NotAction 으로 예외처리 하지 않으면 리전 제한 조건 때문에 콘솔 접속이나 관리 작업이 먹통이 된다. 
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Deny",
      "NotAction": [
        "cloudfront:*",
        "iam:*",
        "route53:*",
        "support:*"
      ],
      "Resource": "*",
      "Condition": {
        "StringNotEquals": {
          "aws:RequestedRegion": ["eu-central-1"]
        }
      }
    }
  ]
}
```
#### CASE2. Amazon S3를 제외하고 eu-central-1 외 모든 리전 차단
- S3 일부 요청은 글로벌 엔드포인트를 경유하기 때문에 리전 차단 시 예외 목록으로 빼줘야 한다.
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Deny",
      "NotAction": ["s3:*"],
      "Resource": "*",
      "Condition": {
        "StringNotEquals": {
          "aws:RequestedRegion": ["eu-central-1"]
        }
      }
    }
  ]
}
```

<br>

### Principal Options in IAM Policies
#### AWS Account and Root User
- 지정한 AWS 계정 또는 해당 계정의 root 전체에 대한 접근 지정
```json
"Principal": { "AWS": "123456789012" }

"Principal": { "AWS": "arn:aws:iam::123456789012:root" }
```
#### IAM Roles
- 특정 계정 내 정의된 특정 IAM 역할을 보안 주체로 지정
```json
"Principal": { "AWS": "arn:aws:iam::123456789012:role/role-name" }
```
#### IAM Role Sessions & Federated
- IAM 역할을 수임(Assume Role)하여 생성된 특정 역할 세션만을 주체로 지정
```json
"Principal": { "AWS": "arn:aws:sts::123456789012:assumed-role/role-name/role-session-name" }
```
- Amazon Cognito 자격 증명 풀이나 SAML 2.0 기반 자격 증명 공급자(IdP)를 통해 자격 증명이 연동(Federated)된 외부 사용자를 보안 주체로 지정
```json
"Principal": { "Federated": "cognito-identity.amazonaws.com" }

"Principal": { "Federated": "arn:aws:iam::123456789012:saml-provider/provider-name" }
```
#### IAM Users
- AWS 계정 내 개별 IAM 사용자(user-name) 지정
```json
"Principal": { "AWS": "arn:aws:iam::123456789012:user/user-name" }
```
- AWS STS 를 통해 임시 자격 증명을 발급받아 활동중인 연동 사용자 세션 지정
```json
"Principal": { "AWS": "arn:aws:sts::123456789012:federated-user/user-name" }
```
#### AWS Services
- AWS 서비스 자체가 다른 AWS 리소스에 접근할 수 있도록 주체로 지정
```json
"Principal": { "Service": [ "ecs.amazonaws.com", "elasticloadbalancing.amazonaws.com" ] }
```
#### All Principals
```json
"Principal": "*"
or
"Principal": { "AWS": "*" }
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)