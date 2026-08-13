## (aws) Identity and Access Management - STS
> Security Token Servcie, Using STS to Assume a Role, Version 1 Version 2, Revoking IAM Role Temporary Credentials

<br>

## AWS STS - Security Token Servcie
- 사용자나 애플리케이션에게 단기 임시 보안 자격 증명(토큰)을 발급해 주는 서비스
- 영구적인 Access Key 대신 특정 권한만 가진 임시 키를 발급하여 보안 위험을 크게 줄여준다 
- 기본 유효시간은 최대 1시간으로, 만료 시 갱신이 필요하다 
- `AssumeRole` : 내 계정 내부에서 특정 역할을 맡아 임시 권한을 획득하거나 다른 계정의 역할을 맡아서 작업할 때 사용
- `AssumeRoleWithSAML` : SAML 기반 로그인 사용자용 역할 부여
- `AssumeRoleWithWebIdentity` : 웹 자격 증명(IdP) 로그인 사용자용 역할 부여, Cognito 사용 권장
- `GetSessionToken` : 세션 토큰 요청

<br>

### Using STS to Assume a Role
1. 임시로 권한을 빌려줄 IAM Role 을 내 계정이나 타계정에 정의한다
2. Trust Policy 를 통해 어떤 Principal 이 IAM 역할에 접근할 수 있는지 정의한다 
3. 사용자가 STS 에 AssumeRole API 요청을 보내면 STS 가 검증 후 해당 역할의 권한을 가진 Temporary security credential 을 발급해준다
4. 발급된 credential 은 15분~1시간 동안 유효하다

<br>

### Version 1 vs.Version 2
#### Version 1
- 기본적으로 STS 는 Global 단일 엔드포인트로 제공된다 
- 기본적으로 활성화되어 있는 AWS 리전만 지원하고 새로 추가되거나 별도 활성화가 필요한 시규 리전에서는 v1 토큰을 사용할 수 없다
- All Regions 를 지원하도록 옵션을 변경할 수 있다
#### Version 2
- 전역 주소 대신 각 리전별 STS 엔드포인트를 사용하여 가까운 주소로 요청을 보낸다
- 가까운 리전의 STS 서버를 이용하므로 통신 속도가 빨라지고 한쪽 리전에 장애가 발생해도 영향받지 않는다 
- 리전 엔트포인트에서 발급받은 STS 세션 토큰 v2 는 모든 AWS 리전에서 유효하다

<br>

### Revoking IAM Role Temporary Credentials
- 해킹이나 키 유출 사고 발생 시 이미 발급되어 활성화되어 있는 임시 세션 토큰을 즉시 무효화할 수 있다
- 한 번 AssumeRole 을 통해 임시 키를 받으면 기본 설정에 따라 최대 12시간 동안 권한이 유지된다 
- 특정 시간 이전에 발급된 IAM 역할의 모든 자격 증명 권한을 즉시 취소한다 
- IAM 콘솔에서 세션 취소 버튼을 누르면 AWS 가 해당 Role에 강제 Deny 정책을 줕여 기존 임시 키들을 먹통으로 만든다
- 세선을 취소한 이후에 IAM 역할을 맡는 사용자에게는 영향을 주지 않는다
```json
<!-- 토큰 발급 시간이 지정된 시간보다 이전인 경우 Deny -->
{
  "Version": "2012-10-17",
  "Statement": {
    "Effect": "Deny",
    "Action": "*",
    "Resource": "*",
    "Condition": {
      "DateLessThan": {
        "aws:TokenIssueTime": "2022-12-13T09:43:48.085Z"
      }
    }
  }
}
```

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)