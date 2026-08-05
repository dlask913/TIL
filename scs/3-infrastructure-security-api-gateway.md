## (aws) Infrastructure Security - API Gateway
> Integrations High Level, Endpoint Types, Security, Resource Policy, Cross VPC Same-Region Access, Throttling

<br>

## AWS API Gateway
- 서버리스, 관리할 인프라가 없음
- AWS Lambda + API Gateway : API Gateway가 클라이언트의 HTTP 요청을 받아 AWS Lambda 함수를 Trigger하고 결과를 반환
- Websocket 프로토콜 지원
- Handling API versioning (v1, v2, .. ), different environments (dev, test, prod..)
- Swagger / OpenAPI 가져오기(Import)를 통해 API를 신속하게 정의
- API 응답 캐싱
<br>

### Integrations High Level
- Lambda : 가장 대표적인 서버리스 패턴으로, API Gateway URL 로 HTTP 요청을 보내면 Lambda 함수 실행
- HTTP : 속도 제한(Rate limiting), 캐싱(Caching), 사용자 인증(User authentications), API 키 발급 등의 기능을 추가하기 위해 백엔드의 기존 HTTP 엔드포인트를 외부에 노출 (HTTP API, ALB 등)
- AWS Service : API Gateway를 통해 모든 AWS 서비스의 API를 외부에 노출

<br>

### Endpoint Types
#### Edge-Optimized (기본값)
- 전 세계 클라이언트를 위한 유형으로, 요청이 CloudFront Edge Location을 거쳐 라우팅된다
- API Gateway 자체는 여전히 단 하나의 Region 에만 존재한다
#### Regional
- 동일 리전 내에 있는 클라이언트를 위한 유형
- CloudFront  와 수동으로 조합할 수 있다
#### Private
- 인터페이스 VPC 엔드포인트(ENI)를 사용하여 사용자의 VPC 내부에서만 접근 가능
- 접근 권한을 정의하기 위해 리소스 정책을 사용

<br>

### Security
#### 사용자 인증
- IAM 역할/역할 권한 (내부 애플리케이션에 유용)
- Cognito (외부 사용자 식별/인증)
- Custom 

#### 커스텀 도메인 & SSL/TLS 인증서
- API Gateway 기본 주소 대신 커스텀 도메인에 HTTPS 를 적용하려면 AWS Certificate Manager(ACM) 서비스와 연동해야 한다 
- API Gateway 엔드포인트 유형이 Edge-Optimized(CloudFront 활용)라면 ACM 인증서를 반드시 us-east-1 리전에서 발급받아야 한다. 
- API Gateway 엔드포인트 유형이 Regional 이라면 ACM 인증서를 API Gateway 가 배포된 동일한 리전에서 발급받아야한다 
- 커스텀 도메인 주소로 들어온 트래픽이 API Gateway 로 찾아갈 수 있도록 Route53(DNS 서비스)에 레코드를 등록해야 한다 

<br>

### Resource Policy
- API 전체에 접근할 수 있는 클라이언트를 IP 주소나 VPC 등으로 제한
- 특정 퍼블릭 IP 주소 허용 / 차단
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "arn:aws:execute-api:region:account-id:api-id/*"
    },
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "arn:aws:execute-api:region:account-id:api-id/*",
      "Condition": {
        "NotIpAddress": {
          "aws:SourceIp": [
            "1.2.3.4/32",
            "203.0.113.0/24"
          ]
        }
      }
    }
  ]
}
```
- 특정 VPC Endpoint(VPCE)에서만 접근 허용 (Private API Gateway)
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "arn:aws:execute-api:region:account-id:api-id/*"
    },
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "execute-api:Invoke",
      "Resource": "arn:aws:execute-api:region:account-id:api-id/*",
      "Condition": {
        "StringNotEquals": {
          "aws:sourceVpce": "vpce-0123456789abcdef0"
        }
      }
    }
  ]
}
```

<br>

### Cross VPC Same-Region Access
- **동일한 AWS 리전**에 있는 서로 다른 VPC 간 API Gateway 를 통해 private 통신을 수행하는 아키텍처 패턴
- AWS 내부 네트워크만 사용하여 VPC A 에서 VPC B 의 API Gateway 로 안전하게 호출하기 위해 사용
![image](https://d2908q01vomqb2.cloudfront.net/1b6453892473a467d07372d45eb05abc2031647a/2022/12/14/Screen-Shot-2022-12-14-at-10.08.47-AM.png)

<br>

### Throttling
- Account Limit : 계정 및 리전당 기본적으로 1000 RPS(Requests Per Second)까지만 처리
- AWS Service Quotas 콘솔이나 지원 센터 문의를 통해 한도 증액 신청 가능(Soft Limit)
- 한도를 초과하여 트래픽이 거절될 경우, HTTP 429(Too Many Requests) 응답 코드 반환
- 특정 API 의 Stage 나 메서드(ex> GET /users, ..) 단위로 트래픽 한도를 개별 설정할 수 있다
- API Key 를 기반으로 고객별 트래픽을 격리하여 제어할 수 있다
- 제한을 걸지 않은 특정 API 에 트래픽이 폭주하면 같은 계정 내의 전혀 다른 API 들까지 먹통될 수 있다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)