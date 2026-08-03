## (aws) Infrastructure Security - WAF, Shield, Firewall Manager
> WAF, Shield: protect from DDoS attack, Firewall Manager, WAF vs Firewall Manager vs Shield, AWS Best Practices for DDoS Resiliency

<br>

## WAF
- 사용자가 보내는 HTTP/HTTPS 웹 요청의 실제 내용까지 정밀 검사
- SQL Injection, XSS, malicious bots 으로부터 application 보호
- 웹 트래픽이 들어오는 AWS 진입점 리소스에 Web ACL 형태로 결합하여 동작
```
Amazon CloudFront, ALB, AppSync, Cognito User Pool, App Runner, Verified Access, Amplify
```
- WAF 는 Layer 3,4 의 DDos 공격 방어용이 아님 -> AWS Shield 사용

### Protection Pack (Web ACL)
- WAF 의 핵심 구성 단위로, 웹 요청을 어떻게 처리할지 정의한 보호 패키지
- Web ACL (= Web Access Control List) : 들어오는 웹 요청을 평가하여 허용할지, 차단할지 판단하는 기준 목록 역할
- WAF 에 Web ACL 을 결합해야지만 대상 리소스를 보호할 수 있다
- IP 주소, HTTP 헤더, HTTP 바디, URI 문자열, 쿠키 등에 대한 규칙을 정의
- 크기 제약 조건, 지리적(국가) 매칭, ASN 매칭, 레이블 매칭, 정규식 매칭 조건 설정 가능
- Rate 기반 규칙 : 5분 Evaluation window 동안 동일한 IP 에서 지정한 횟수 이상의 HTTP 요청이 유입될 경우 해당 IP 를 자동으로 일시 차단
- Rule Actions Include : Allow, Block, Count, CAPTCHA, Challenge
- 동일한 scope 내의 여러 리소스에서 재사용 가능 : Global (CloudFront) vs Regional (ALB, Cognito, API Gateway…)
- ClodWatch Logs log group, S3, Data Firehose 로 로그를 보낼 수 있다
#### usecase
- 동일한 IP 주소에서 5분 동안 100회를 초과하는 요청이 들어올 경우 차단 (Rate-based Rule)
- 특정 국가에서 유입되는 요청만 Cognito User Pool에 접근할 수 있도록 허용 (Geo Match Rule)
- User-Agent HTTP 헤더를 활용해 특정 기기/브라우저에서의 요청만 허용 (Match Rule)
#### pricing
- Web ACL: $5 per month
- Rule: $1 per month
- Requests: $0.6 per 1 million requests
#### managed rules
- Baseline Rule Groups : 일반적이고 흔한 위협으로부터 전반적인 보호 제공 
- Use-case Specific Rule Groups : 다양한 AWS WAF 활용 목적에 맞춘 전용 보호
- IP Reputation Rule Groups : 요청 출처(ex> 악성 IP)를 기반으로 요청 차단
- Bot Control Managed Rule Group : 봇(Bot)의 요청을 차단하고 관리

<br>

## Shield: protect from DDoS attack
#### Standard
- 모든 AWS 고객에게 활성화되는 무료 서비스
- SYN/UDP Flood, Reflection 공격 및 기타 Layer 3/Layer 4 공격으로부터 보호 제공
#### Advanced
- 개별 계정 단위가 아닌 AWS Organization 단위로 월 $3000
- Amazon EC2, ELB, Amazon CloudFront, AWS Global Accelerator, Route 53에 대한 더 정교한 공격 방어
- AWS DDoS 대응 팀(DRP)에 24시간 7일 내내 연락 가능
- DDoS 공격으로 인한 사용량 급증 시 과도한 요금 청구로부터 보호
- 사람이 직접 개입하지 않아도 AWS WAF 규칙을 스스로 생성하고 적용하여 차단

<br>

## Firewall Manager
- **단일 계정이 아닌 여러 AWS 계정(Organizations) 전체**의 보안 정책과 방화벽 규칙을 한 곳에서 관리할 수 있게 해준다
- 정책은 Region 레벨에서 생성된다
- 규칙은 조직 내 기존 계정 뿐 아니라 향후 생성될 모든 계정에서 새 리소스가 생성될 때 자동으로 적용된다 (good for compliance)

<br>

## WAF vs Firewall Manager vs Shield
- 개별 리소스에 대한 정밀하고 세부적인 보호가 목적이라면, WAF 단독 사용
- 여러 계정에 걸쳐 AWS WAF를 사용하거나 새 리소스의 보호를 자동화하고 싶다면 AWS WAF와 함께 Firewall Manager를 사용
- L3/L4 대규모 네트워크 DDoS를 막고 싶을 때 AWS Shield 사용
- 디도스(DDoS) 공격에 자주 노출되는 환경이라면 Shield Advanced 구매 권장

<br>

### AWS Best Practices for DDoS Resiliency
- 공격 표면 축소 (EC2 IP 직접 공격 방지) → EC2를 Private Subnet에 배치하고 **CloudFront / ALB** 뒤로 숨김
- TCP/UDP 프로토콜 DDoS 방어 → **AWS Global Accelerator** + Shield
- DNS 서비스 마비 방지 → **Amazon Route 53** (Edge 기반 분산 해독)
- L7 공격 및 악성 IP/봇 차단 → **AWS WAF** (Rate-based Rules, Managed Rules)
- 트래픽 폭주 시 자동 대응 → **Auto Scaling** + **ELB**
- 전 세계 멀티 계정 중앙 보안 적용 → **AWS Firewall Manager**

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)