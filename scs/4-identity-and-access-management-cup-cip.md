## (aws) Identity and Access Management - CUP, CIP
> Cognito User Pools, Cognito Identity Pools, AWS Cognito Sync, Cognito User Pool Groups

<br>

## Cognito User Pools (CUP)
> 별도 사용자 DB 서버를 구축할 필요 없이, 서버리스 방식으로 사용자 인증 및 회원 관리를 전담해주는 AWS 서비스

#### User Features
- 웹 및 모바일 앱을 위한 서버리스 사용자 데이터베이스 생성
- 간편한 로그인 : username (or email)/ password combination
- 비밀번호 재설정, 이메일 및 전화번호 검증 기능을 제공한다
- MFA 를 지원한다
- 연동 가능한 자격 증명 : facebook, google, SAML 등의 사용자
- 고급 보안 기능 중 하나로 인터넷에 유출된 ID/비밀번호 조합을 감지하면 해당 사용자의 로그인을 자동으로 차단한다 
- 로그인 시 JWT 를 반환하여 클라이언트는 이 토큰을 사용해 백엔드 API 에 접근한다 

#### Integrations
- CUP integrates with API Gateway and Application Load Balancer

#### Lambda Trigers
- 람다 함수 처리가 끝날 때까지 Cognito 프로세스가 대기하며 Lambda 의 반환값에 따라 이후 흐름을 진행하거나 요청을 거부할 수 있다
```
Pre Authentication Lambda Trigger: 로그인 요청 승인/거부를 위한 커스텀 검증
Post Authentication Lambda Trigger: 커스텀 분석/로깅을 위한 이벤트 기록
Pre Token Generation Lambda Trigger: 토큰 클레임 확장 또는 억제

Pre Sign-up Lambda Trigger: 회원가입 요청 승인/거부를 위한 커스텀 검증
Post Confirmation Lambda Trigger: 커스텀 웰컴 메시지 전송 또는 커스텀 분석용 이벤트 로깅
Migrate User Lambda Trigger: 기존 사용자 디렉토리에서 User Pool로 사용자 이전
..
```

#### Hosted Authentication UI
- 개발자가 직접 화면을 구현할 필요 없이 AWS 가 제공하는 관리형 인증 웹 페이지를 그대로 가져다 쓰는 방식 (ex> 로그인, 회원가입, 비밀번호 찾기 등)
- 호스팅 UI를 사용하면 소셜 로그인, OIDC 또는 SAML 과의 연동을 위한 기반이 마련된다
- 커스텀 로고 및 커스텀 CSS를 통해 브랜딩에 맞게 변경할 수 있다

<br>

## Cognito Identity Pools (Federated Identities)
- CUP 가 회원가입/로그인 관리를 담당한다면, Identity Pools 는 인증된 사용자에게 AWS 리소스에 접근할 수 있는 **임시 AWS Role/Credentilas 을 발급**하는 역할
- 외부 인증이나 CUP 로 로그인한 사용자에게 AWS STS 를 거쳐 S3, DynamoDB 등에 접근할 수 있는 임시 IAM 자격 증명을 제공한다
- IAM 자격 증명은 `AssumeRoleForWebIdentity` STS API 호출을 통해 Cognito Identity Pools가 Security Token Service(STS) 로부터 취득한다
- identity pool 의 identity source 종류
```
- Public Providers (Login with Amazon, Facebook, Google, Apple): 소셜 로그인 제공자
- Users in an Amazon Cognito user pool: Cognito 사용자 풀에 등록된 회원
- OpenID Connect Providers & SAML Identity Providers: OIDC 및 SAML 기반 외부 인증 서버(Okta, Azure AD 등)
- Developer Authenticated Identities (custom login server): 개발자가 직접 구축한 커스텀 인증 서버
```
- 로그인하지 않은 게스트 사용자에게도 제한된 임시 AWS 권한을 부여할 수 있다 
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "s3:GetObject"
      ],
      "Effect": "Allow",
      "Resource": [
        "arn:aws:s3:::mybucket/assets/my_picture.jpg"
      ]
    }
  ]
}
```
- 자격 증명에 적용되는 IAM 정책은 Cognito 에서 정의된다 
- fine grained control 을 위해 user_id 를 기반으로 권한을 커스텀 할 수 있다 ( IAM 정책 내 변수를 활용해 S3의 `my-bucket/user_id/*` 경로처럼 사용자 본인의 전용 저장소 폴더에만 접근할 수 있도록 )
-  **CUP + CIP = manage user / password + access AWS services**

<br>

## AWS Cognito Sync
- Deprecated 되어, AppSync 를 사용하는 것이 권장된다
- 사용자의 테마 설정, 앱 환경설정 데이터, UI 상태 등을 사용자별로 저장하는 용도로 쓰인다
- 오프라인일 때 로컬에 저장했다가 인터넷이 연결되면 자동으로 서버와 동기화
- iOS, Android 등 모든 플랫폼 지원

<br>

## Cognito User Pool Groups
- CUP 내 논리적 그룹 단위로 사용자들을 모아놓은 집합 (ex> Admin, Editor, StandardUser)
- 그룹에 IAM Role 을 할당하여 그룹 내 사용자들의 권한을 정의한다
- 사용자는 여러 그룹에 동시 속할 수 있다 
- 각 그룹에 우선순위(Precedence) 값을 할당하여 사용자가 여러 그룹에 속해 있을 때 우선순위 수치가 가장 낮은 그룹의 IAM Role 이 최종 선택된다 (ex> 2> 4)
- 그룹 생성 시 적용할 IAM Role의 고유 식별자(ARN)를 직접 입력해 지정한다
- 하위(중첩) 그룹을 생성할 수 없다 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)