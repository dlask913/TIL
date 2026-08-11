## (aws) Infrastructure Security - ABAC, MFA
> Attribute-Based Access Control, ABAC vs RBAC

<br>

## ABAC - Attribute-Based Access Control
- 속성을 기반으로 리소스에 대한 접근 권한을 정의하는 권한 부여 전략으로 Tag 형식으로 관리한다
- principal 이나 리소스에 연결된 attribute 값에 따라 세분화된(fine-grained) 접근 제어 조건 적용 (ex> department, job role, team name, .. )
- 팀별로 매번 별도의 IAM Role 이나 정책을 새로 생성하지 않고 속성 집합을 그룹화하여 해당 유저 그룹이 어떤 리소스에 접근할 수 있는지 범용적으로 식별하고 제어한다 
- 요청을 보내는 주체(Principal)의 태그 값과 요청 대상이 되는 리소스의 태그 값이 서로 일치할 때 작업을 허용하는 방식으로 정책 조건을 구성한다 
- 새로운 팀원이나 리소스가 추가되더라도 정책 변경 없이 태그 부여만으로 자동 적용되므로 급격히 확장되는 환경에 유용하다 
#### 적용 예시 
- `"aws:ResourceTag/Owner": "${aws:username}"`: 리소스의 `Owner` 태그 값이 요청자의 IAM `username` 과 일치해야 한다
- `"aws:ResourceTag/AccessProject": "${aws:PrincipalTag/AccessProject}"`: 리소스의 `AccessProject` 태그 값이 보안 주체(Principal)의 `AccessProject` 태그 값과 일치해야 한다
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "ec2:StartInstances",
        "ec2:StopInstances"
      ],
      "Resource": "arn:aws:ec2:*:123456789012:instance/*",
      "Condition": {
        "StringEquals": {
          "aws:ResourceTag/Owner": "${aws:username}",
          "aws:ResourceTag/AccessProject": "${aws:PrincipalTag/AccessProject}"
        }
      }
    }
  ]
}
```

<br>

### ABAC vs RBAC
#### RBAC(Role-Based Access Control) - 역할 기반 접근 제어
- 사용자의 역할이나 직무를 기준으로 권한을 정의하는 전통적인 방식
- Example: Administrator, DB Admins, DevOps, …
- 직무마다 서로 다른 IAM Policy 를 만들어 각각 부여한다
- 새로운 리소스가 생성될 때마다 해당 리소스에 접근할 역할을 찾아 정책을 일일이 수정해야한다는 단점이 있다
#### ABAC(Attribute-Based Access Control) - 속성 기반 접근 제어
- 새로운 리소스가 추가되어도 정책 수정 없이 올바른 태그만 붙이면 기존 정책 조건에 따라 접근 권한이 자동으로 처리된다 
- 사용자(Principal)와 리소스 태그 값이 일치하면 권한이 자동으로 부여된다 
- 직무마다 별개의 정책을 만들지 않고 태그를 비교하는 범용 정책 하나로 여러 팀과 직무를 일괄 제어할 수 있어 관리해야 정책 수가 줄어든다
- corporate directory(Okta, Azure AD 등 SAML 2.0/Web IdP)에서 관리하는 사용자 속성(부서, 팀명 등)을 AWS로 연동하여 그대로 권한 제어 태그로 활용할 수 있다

<br>

## MFA
### S3 MFA Delete
- S3 에서 중요한 작업을 수행하기전 MFA 를 입력하도록 강제하는 기능
- MFA Delete 는 버전 관리 기능과 결합되어 동작하므로, 해당 S3 버킷에 Versioning 이 먼저 활성화되어 있어야 한다 
- MFA Delete 활성화 및 비활성화 설정은 오직 버킷 소유자인 AWS Root 계정으로만 가능하다.
- MFA will be requied to 
  - 객체 버전을 영구적으로 삭제할 때
  - 버킷의 버전 관리 기능을 일시 중단할 때 
- MFS won't be required to
  - 버전 관리 기능을 활성화할 때
  - 삭제된 객체 버전 목록을 조회할 때
#### MFA 인증 없이 인스턴스 중지 및 종료 Deny
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ec2:*",
      "Resource": "*"
    },
    {
      "Effect": "Deny",
      "Action": [
        "ec2:StopInstances",
        "ec2:TerminateInstances"
      ],
      "Resource": "*",
      "Condition": {
        "BoolIfExists": {
          "aws:MultiFactorAuthPresent": false
        }
      }
    }
  ]
}
```
#### MFA 인증 완료 후 만료 시간 정의
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "ec2:*",
      "Resource": "*",
      "Condition": {
        "BoolIfExists": {
          "aws:MultiFactorAuthPresent": true
        },
        "NumericLessThan": {
          "aws:MultiFactorAuthAge": "300" <!-- 300초 미만 -->
        }
      }
    }
  ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)