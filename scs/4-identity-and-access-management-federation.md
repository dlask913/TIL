## (aws) SAML 2.0 Federation, Web Identity Federation
> AWS Console Access with SAML 2.0, Custom Identity Broker App, Configuring SAML 2.0-based Federation, Web Identity Federation with Cognito

<br>

## SAML 2.0 Federation
- AWS 계정에 별도의 IAM User를 만들지 않고도 사내 계정으로 AWS 콘솔이나 CLI에 로그인할 수 있도록 연결해 주는 표준 인증 기술
- ADFS, Okta, Ping Identity, Azure AD 등 SAML 2.0을 지원하는 외부 사내 인증 시스템(IdP)이라면 무엇이든 AWS와 연동할 수 있다 
- 사내 로그인에 성공하면 AWS STS 를 통해 만료시간이 있는 임시 AWS 자격증명을 받아 AWS 리소스를 사용한다
- 모든 직원에 대해 IAM User를 생성할 필요가 없다
- AWS IAM 과 SAML 2.0 자격 증명 제공자(IdP) 간 양방향 신뢰 관계 설정 필요
- 내부적으로 STS API 인 `AssumeRoleWithSAML` 을 사용한다
- SAML 2.0 연동은 old way 로, 현재는 중앙 집중식으로 멀티 계정 SSO를 제공하는 **AWS IAM Identity Center**를 쓰는 것이 권장된다

<br>

### AWS Console Access 동작 순서
1. User Logs into Portal: 사용자가 사내 포털/IdP 웹 페이지에 접속하여 로그인을 시도
2. Authenticate: IdP 서버가 사내 LDAP과 통신해 사용자 신원을 검증
3. SAML Assertion: 인증 성공 시 IdP가 SAML Assertion(전자 서명된 토큰)을 브라우저로 전달
4. Post to AWS Sign-in: 브라우저가 SAML Assertion을 AWS의 SAML 엔드포인트로 HTTP POST 전송
5. Request Temporary Security Credentials: AWS SAML 엔드포인트가 전달받은 SAML Assertion을 검증하고 STS에 임시 자격 증명(Session Token)을 요청해 받아온다
6. Sign-in URL for AWS Console: AWS가 이 임시 자격 증명을 기반으로 콘솔에 로그인할 수 있는 일회성 Sign-in URL(단인 인증 토큰 포함)을 생성해 브라우저에 응답
7. Redirect: 브라우저가 해당 URL로 리다이렉트되면서, 별도의 AWS 계정 ID/비밀번호 입력 없이 AWS Management Console 화면에 로그인된 상태로 진입

<br>

### Custom Identity Broker Application
- 자격 증명 제공자(IdP)가 SAML 2.0과 호환되지 않는 경우에만 사용
- 자격 증명 브로커가 사용자를 인증하고 AWS 로부터 임시 자격 증명을 요청한다
- 로그인한 사용자의 사내 직급, 권한 등의 정보를 확인한 뒤 그에 맞는 AWS IAM Role을 선택해 주는 로직을 브로커 프로그램 내에 직접 작성해야 한다
- STS API인 `AssumeRole` 또는 `GetFederationToken`을 사용한다

<br>

### Configuring SAML 2.0-based Federation
> 사내 IdP 와 AWS IAM 간 서로를 신뢰하도록 양방향 메타데이터 교환 및 IAM Role 을 설정

1. AWS 의 SAML 메타데이터 XML 파일을 다운로드하여 사내 IdP에 서비스 제공자로 등록
2. 사내 IdP에서 자격 증명 제공자 정보가 담긴 메타데이터 XML 파일을 생성 및 추출
3. 사내 IdP에서 생성한 메타데이터 XML 파일을 AWS IAM 의 SAML Identity Provider 항목에 업로드하여 등록
4. AWS IAM 에서 해당 SAML Provider 를 principal 로 지정하여 외부 연동 사용자가 assume할 IAM Role 및 truest pollicy 를 생성

<br>

### Troubleshooting
#### Error: Response signature invalid 
- (service: AWSSecurityTokenService; status code: 400; error code: InvalidIdentityToken)
- 사내 IdP의 메타데이터(X.509 인증서 등)와 AWS IAM에 등록된 SAML Provider의 메타데이터 정보가 서로 일치하지 않기 때문
- 사내 IdP 측의 인증서가 만료되어 새로 갱신되었으나, AWS IAM 측에는 구형 인증서 정보가 그대로 남아있는 경우가 대표적
#### 해결방안
1. 사내 IdP 시스템에서 최신 SAML 2.0 메타데이터 XML 파일을 새로 다운로드
2. AWS CLI 명령어를 사용하여 AWS IAM에 등록된 SAML Provider 정보를 최신 파일로 업데이트한다
```console
aws iam update-saml-provider
```

<br>

## Web Identity Federation with Cognito
- 소셜 로그인이나 OIDC 인증 서비스를 이용해 웹/모바일 클라이언트 앱이 AWS 리소스에 안전하게 접근하는 권장 아키텍처
- 필요한 최소 권한을 가질 수 있도록 Cognito 를 이용해 IAM Role 을 새엇ㅇ한다
- OIDC 기반 자격 증명 제공자(IdP)와 AWS 간에 신뢰 관계를 형성한다 
#### Cognito benetifs
- Guest 사용자 접근을 지원한다 
- MFA 를 지원한다
- 사용자 상태 및 환경설정 데이터 동기화를 지원한다
- 과거 Token Vending Machine 역할을 대체한다

IdP별 정책 변수 예시
- **`cognito-identity.amazonaws.com:sub`**: Amazon Cognito Identity ID
- **`[www.amazon.com](https://www.amazon.com):user_id`**: Amazon 로그인 사용자 ID
- **`graph.facebook.com:id`**: 페이스북 사용자 ID
- **`accounts.google.com:sub`**: 구글 로그인 사용자 ID (Subject)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::myBucket",
      "Condition": {
        "StringLike": {
          "s3:prefix": "Amazon/mynumbersgame/${www.amazon.com:user_id}/*"
        }
      }
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject"
      ],
      "Resource": [
        "arn:aws:s3:::myBucket/amazon/mynumbersgame/${www.amazon.com:user_id}",
        "arn:aws:s3:::myBucket/amazon/mynumbersgame/${www.amazon.com:user_id}/*"
      ]
    }
  ]
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)