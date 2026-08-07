## (aws) Infrastructure Security - Route53 (DNSSEC), Network Firewall
> DNSSEC, Enable DNSSEC on a hosted zone, Chain of Trust, Network Firewall Deployment Architectures

<br>

## Route53 - DNS Security Extensions (DNSSEC)
- DNS 응답 데이터의 위변조 여부(DNS Integrity)와 올바른 출처에서 왔는지(Origin Verification)을 암호화 서명(Signing)으로 검증하는 보안 프로토콜
- DNS 응답을 조작하여 가짜 사이트로 연결시키는 DNS 스푸핑이나 DNS Cache Poisoning 공격 방지
- DNSSEC Signing 은 인터넷에 공개된 Public Hosted Zones 에서만 작동한다 
- Route 53 은 DNSSEC 관련 Domain Registration 과 Signing 두 가지 역할을 모두 수행할 수 있다 
- DNSSEC 은 이중 키 체계이다
  - Managed by you: Key-signing Key (KSK) - based on an asymmetric CMK(Customer Managed Key) in AWS KMS 
  - Managed by AWS: Zone-signing Key (ZSK)
- DNSSEC 서명이 활성화되면 Route 53은 호스팅 영역 내 레코드의 최대 TTL 을 1주일로 제한한다.

<br>

### Enable DNSSEC on a hosted zone
#### 1. Prepare for DNSSEC signing
- 현재 호스팅 영역의 정상 작동 상태 점검
- 레코드의 TTL 시간을 미리 낮춰 오류가 발생했을 때 잘못된 DNS 정보가 외부로 오래 캐싱되지 않도록 한다 (권장은 1시간)
- SOA(Start Of Authority) 레코드의 Negative Caching(존재하지 않는 레코드에 대한 응답 캐시) 을 5분으로 단축하여 오류 수정을 빠르게 반영한다
#### 2. Enable DNSSEC signing and create a KSK
- AWS 콘솔이나 CLI 를 통해 해당 호스팅 영역의 DNSSEC signing 기능 활성화
- KSK 를 생성하고 이를 AWS KMS 의 고객관리형 비대칭 CMK 와 연결한다 
#### 3. Establish chain of trust
- 부모 존에 KSK 의 공개키 해시값이 포함된 DS(Delegation Signer) 레코드를 등록한다
- 도메인을 등록한 레지스트라(Registrar)를 통해 상위 등록기관에 DS 레코드 정보를 전달, 등록한다. 
#### 4. Monitor for errors using CloudWatch Alarms 
- DNSSECInternalFailure : Route 53 내부에서 DNSSEC 서명 처리 중 오류가 발생했을 때 알림
- DNSSECKeySigningKeysNeedingAction : KMS 키 권한 문제나 키 상태 변경 등으로 KSK 조치가 필요할 때 알림

<br>

### DNSSEC – Chain of Trust
```
                 [ Root DNS Server ]
                           │
                           │  Trust (DS Record)
                           ▼
                 [ TLD DNS Server (.com) ]
                           │
                           │  Trust (DS Record)
                           ▼
            [ Registrar (Amazon Route 53) ]
                           │
                           │  Trust
                           ▼
             [ Hosted Zone (example.com) ]
              ├─ KSK (AWS KMS CMK 기반) ──► ZSK를 서명
              └─ ZSK (AWS 자동 관리)   ──► DNS 레코드 서명
                            │
                            │  Trust
                            ▼
            [ Hosted Zone (sub.example.com) ]
```
- Root DNS 서버부터 최하위 서브도메인까지, 상위 존이 하위 존의 공개키 해시(DS 레코드)를 암호화 서명으로 보증하여 신뢰 체인을 형성하는 구조
- 클라이언트 동작 : DNSSEC을 지원하는 리졸버/클라이언트는 각 계층의 공개키(Public Key)를 활용해 최상위 Root부터 역추적하며 DNS 응답의 위변조 여부를 최종 검증

<br>

## Network Firewall
### 기존 AWS 보안 솔루션 개요
- NACLs & Security Groups: Subnet 및 EC2 인스턴스 단위의 basic L4(IP/포트) 방화벽 역할
- AWS WAF: L7(웹 애플리케이션) 계층에서 SQL Injection, XSS 등 악의적인 HTTP/HTTPS 요청 차단
- AWS Shield & Shield Advanced: L3/L4 및 L7 DDoS 공격 방어 서비스
- AWS Firewall Manager: 여러 AWS 계정 전반의 WAF, Shield, Security Group 보안 규칙을 중앙에서 모니터링 및 일괄 관리

<br>

### Network Firewall
- VPC 전체를 대상으로 L3~L7 계층까지 심층 패킷 검사(Deep Packet Inspection) 및 방화벽 기능을 제공한다 
- 아래 모든 방향의 트래픽 검사가 가능하다
```
- VPC to VPC
- Outbound to internet
- Inbound from internet
- To / from Direct Connect & Site-to-Site VPN : 온프레미스 네트워크와 VPC 간 연결 트래픽
```
- 내부적으로 AWS Gateway Load Balancer 기술을 기반으로 작동하여, 트래픽 양에 따라 자동으로 auto-scaling 된다
-  AWS Firewall Manager 와 연동하여 여러 AWS 계정 및 VPC 에 방화벽 규칙을 중앙에서 관리할 수 있다 

 #### Fine Grained Controls
- IP&port, Protocol, 상태 저장 도메인, 정규표현식 등 수천 개 이상의 규칙 지원
- 규칙에 매칭되는 트래픽에 대해 Allow, Drop, Alert 중 하나의 동작을 수행하도록 설정
- 네트워크 위협으로부터 보호하기위해 Active flow insepction 및 Intrusion-prevention (IPS/IDS 기능) 수행
- 규칙에 차단되거나 감지된 트래픽 로그를 S3, CloudWatch Logs, Data Firehose 로 전송하여 중앙 집계 및 보안 분석을 수행할 수 있다 

<br>

### Deployment Architectures
#### case 1.
![image](https://d2908q01vomqb2.cloudfront.net/5b384ce32d8cdef02bc3a139d4cac0a22bb029e8/2021/09/10/anfw-deploymentmodels-withRoutingEnhancements-figure4.png)

1. Inbound 트래픽 제어: 인터넷 게이트웨이(IGW)에 Ingress Route Table을 붙여서, 외부에서 들어오는 트래픽이 목적지(ALB/EC2)로 직행하지 못하게 막고 Network Firewall Endpoint(vpce-id)로 먼저 강제 라우팅
2. Outbound 트래픽 제어: 내부 EC2/NAT Gateway에서 인터넷으로 나가는 트래픽 역시 Route Table을 통해 방화벽 엔드포인트를 거친 후에만 IGW를 타도록 라우팅

#### case 2.
![image](https://d2908q01vomqb2.cloudfront.net/22d200f8670dbdb3e253a90eee5098477c95c23d/2021/03/07/GuardDuty-Network-Firewall-2021-2.png)

1. 위협 감지 및 수집 (GuardDuty -> Security Hub -> EventBridge)
- Security Hub 에서 수신한 위협 이벤트를 EventBridge 를 통해 Step Functions Trigger
2. 자동 대응 및 차단 로직 (Step Functions 및 Lambda)
- 감지된 IP 가 이미 차단 목록에 있는지 등을 확인하고 차단 대상이 맞다면 악성 IP 차단 규칙 신규 등록 (CheckIPInDB -> BlockTraffic)
- 차단 처리 결과를 알림 전송 (SNS Topic)
3. VPC 방화벽 자동 적용 (Network Firewall)
- Lambda 에 의해 방화벽 정책이 업데이트되면 즉시 VPC 내 Network Firewall Endpoint 에 반영되어 트래픽 자동 차단

<br>

### Encrypted Traffic
- HTTPS 는 일반적인 방화벽으로 패킷 바디를 읽을 수 없는데 이 암호화된 패킷 내부까지 열어보는 Deep Packet Inspection(DPI) 지원
- Decrypts -> Inspects/Blocks -> Re-encrypts (TLS 복호화 및 재암호화)
- 트래픽을 복호화하고 다시 암호화하는 과정에서 인증서 처리가 필요한데 AWS Certificate Manager(ACM)에 등록된 인증서 연동이 가능하다. 

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)