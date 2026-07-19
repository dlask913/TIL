## (aws) Infrastructure Security - Bastion Hosts, NAT Gateway, VPN
> Bastion Hosts, NATGW, Site-to-Site VPN, Client VPN

<br>

## Bastion Hosts
```
[ Internet ] 
     │  
     │  ▲ Inbound (Port 22) ── 제한된 사내 IP 대역만 허용
     ▼
┌────────────────────────────────────────────────────────┐
│ [ VPC ]                                                │
│  ▼ Public Subnet                                       │
│ ┌──────────────────────┐                               │
│ │   Bastion Host       │ ── 인터넷 게이트웨이와 연결    │
│ └──────────┬───────────┘                               │
│            │                                           │
│            │  ▲ Inbound (Port 22) ── Bastion SG만 허용 │
│            ▼                                           │
│  ▼ Private Subnet                                      │
│ ┌──────────────────────┐                               │
│ │     Private EC2      │ ── 인터넷과 격리된 실제 서버    │
│ └──────────────────────┘                               │
└────────────────────────────────────────────────────────┘
```
- private 서브넷에 있는 EC2 인스턴스에 접속하기 위해 사용하는 방식
- Bastion Host 를 사용하여 private 서브넷에 있는 EC2 인스턴스에 SSH 로 접속할 수 있다
- Bastion 은 public 서브넷에 위치하며 다른 모든 private 서브넷과 연결되어있다
- Bastion Host 의 보안그룹은 신뢰할 수 있는 제한된 CIDR(회사 공인IP대역)로부터 들어오는 22번 포트의 인바운드 트래픽을 허용해야한다.
- private EC2 의 보안 그룹은 Bastion Host 의 보안 그룹 또는 Bastion Host 의 private IP 로부터 들어오는 트래픽만 허용해야 한다

<br>

## NAT Gateway
![image](https://d2908q01vomqb2.cloudfront.net/5b384ce32d8cdef02bc3a139d4cac0a22bb029e8/2025/11/19/Picture1-17.png)

- VPC 내부에서 private 서버들이 외부 인터넷과 안전하게 통신할 수 있도록 경로를 열어주는 네트워크 주소 변환 게이트웨이 
- AWS 가 완전 관리형으로 제공하므로 우리가 서버를 관리할 필요가 없고 알아서 고가용성과 높은 대역폭을 보장한다 
- 생성해 둔 시간당 비용 + 통과하는 데이터 처리량(GB당 비용)이 합산되어 요금이 청구된다 
- 특정 가용 영역(AZ)에 종속되어 생성되며 외부 인터넷과 통신하기 위해 고정 공인IP 인 Elastic IP(EIP) 가 반드시 하나 매칭되어야 한다 
- NATGW 는 반드시 public 서브넷에 위치해야하며 자신이 속한 서브넷의 트래픽은 처리할 수 없다 → **private 서브넷에 있는 EC2 가 라우팅 테이블을 타고 public 서브넷에 있는 NATGW 를 거쳐서** 나가야한다 
- Requires an IGW (Private Subnet → NATGW → IGW → 외부 인터넷) : NATGW 혼자서는 인터넷으로 못 나가기때문에 최종 관문인 인터넷 게이트웨이(IGW) 가 public 서브넷에 연결되어있어야 한다 
- 기본 5Gbps 의 대역폭으로 시작하며 트래픽이 몰리면 AWS 가 최대 100Gbps 까지 자동으로 대역폭을 확장해준다. 
- NATGW 자체에는 보안 그룹을 붙이지 않는다 (필요없음)

#### High Availability 
- 하나의 AZ 안에서 AWS 가 자체적으로 장비를 이중화해 두었기 때문에 AZ 내부에서는 자체적인 회복 탄력성을 가진다 
- fault-tolerance(결함 허용=장애 방지)를 위해서는 여러 AZ 에 각각 별도의 NATGW 를 생성해야 한다 
- 만약 특정 AZ 가 다운되면 그 AZ 내의 자원들은 어차피 NAT 가 필요 없어지므로 다른 AZ 에 자원이 없는 경우 교차 AZ 장애 조차(Cross-AZ Failover)는 불필요하다 

<br>

## Site-to-Site VPN 
![image](https://docs.aws.amazon.com/ko_kr/whitepapers/latest/aws-vpc-connectivity-options/images/aws-managed-vpn.png)
- 온프레미스 데이터센터나 본사/지사 사무실 네크워크를 AWS VPC 와 안전한 암호화 터널로 연결하여 마치 하나의 거대한 사설 네트워크처럼 쓸 수 있게 해주는 서비스
#### Virtual Private Gateway (VGW)
- VPN 연결에서 AWS VPC 측에 위치하는 VPN Concentrator 
- 여러개의 암호화된 VPN 터널들이 VPC 안으로 진입할 때 이 트래픽들을 하나로 모아서 처리해주는 AWS 전용 가상 라우터
- Site-to-Site VPN 연결을 생성하고자 하는 대상 VPC 에 VGW 를 직접 생성하고 Attach해야 한다 
- 각 네트워크 영역을 식별하기 위한 고유 ID 인 ASN(Autonomous System Number)을 커스텀할 수 있다 (디폴트 값 ex>64512 를 이미 쓰고 있는 경우)
#### Customer Gateway (CGW)
- VPN 연결에서 고객 진영(사내 전산실/사무실 측)에 위치하는 SW 애플리케이션 또는 실제 물리 장비
- AWS 에 VPN 을 등록할 때 이 CGW 의 공인 IP 주소를 알려주어쟈 터널이 정상적으로 뚫린다 
- AWS 가 공식적으로 호환성 및 연동 테스트를 완료한 검증된 [CGW 장비 목록](https://docs.aws.amazon.com/vpn/latest/s2svpn/your-cgw.html#DevicesTested)

<br>

### Connections
- Customer Gateway Device (On-premise) IP 설정 규칙
```
1. 사내 CGW 장비의 인터넷 라우팅이 가능한 공인 IP 주소를 사용
2. 만약 장비가 NAT 장비 뒤에 숨어있고 NAT Traversal 기능이 활성화되어 있다면 그 NAT 장비의 공인 IP 주소를 사용 (가장 앞단에서 인터넷과 마주하는 NAT 장비의 공인IP)
```
- 서브넷과 연결된 라우팅 테이블에서 **VGW 의 Route Propagation(★) 을 활성화**해야 한다 → 안하면 통신 불가
- 만약 사내에서 AWS EC2 로 핑 테스트를 해보고싶다면 EC2 보안 그룹의 인바운드 규칙에 ICMP 프로토콜을 반드시 추가해야 한다 

<br>

### VPN CloudHub
- 본사, 지사 등 여러 사이트가 존재할 때 이들 간의 보안 통신을 제공하며 각 지점들을 연결하는 Low-cost hub-and-spoke 모델을 구축한다 
- 모든 지사가 오직 AWS 에 있는 단 하나의 VGW 를 향해서면 VPN 연결을 한다 
- 기본적으로 VPN 연결이므로 public 인터넷망을 경유하여 통신한다 -> 지연 시간에 조금씩 편차가 생길 수 있음
- 이를 구축하려면 동일한 VGW 에 여러개의 VPN 연결을 생성하고 동적 라우팅을 설정한 뒤 라우팅 테이블을 구성해야 한다
```
[ 🏢 서울 (CGW-1) ] ── (BGP VPN) ──┐
                                   ▼
[ 🏭 대전 (CGW-2) ] ── (BGP VPN) ──► [ 🔑하나의 AWS VGW ] ◄── VPC 내부 
                                                          (프라이빗 EC2)
                                   ▲
[ 🏢 부산 (CGW-3) ] ── (BGP VPN) ──┘
```

<br>

## Client VPN
```
[ 💻 노트북 ] ── (OpenVPN Client / 인터넷망) ──┐
                                              ▼
                                    [ 🔒 AWS Client VPN ]
                                              │ (VPC 내부 진입)
                                              ▼
                                    [ 🔑 AWS VPC 프라이빗 영역 ]
                                    - EC2 인스턴스 (10.0.1.5)
                                    - RDS 데이터베이스 (10.0.2.20)
```
- 엔지니어나 재택 근무자 개인의 노트북이나 컴퓨터에서 AWS 및 사내망과 직접 1:N 으로 안전하게 연결해주는 원격 접속 솔루션
- 사용자의 컴퓨터에서 OpenVPN 클라이언트를 사용하여 AWS 및 온프레미스 private 네트워크에 연결한다
- 마치 private VPC 네트워크 내부에 들어와 있는 것처럼 사내에서 연결하던 EC2 의 private IP 를 그대로 쳐서 다이렉트로 접속할 수 있다
- Goes over public Internet -> 인터넷이 연결되는 어느 곳에서든 사용할 수 있음

<br>

### Authentication Types
- Client VPN 을 구축할 때 사용자를 검증하는 인증 방식
#### 1. Active Directory Authentication (디렉토리 서비스 기반 인증)
- Microsoft AD 를 기반으로 User-Based 인증을 수행하며 MFA 를 지원한다
- 많은 기업들이 사내 직원 계정 관리용으로 MS AD 를 사용하는데 이 방식을 쓰면 직원은 새로운 계정을 만들 필요 없이, 본인이 평소 회사 컴퓨터 로그인할 때 쓰던 ID/PW 그대로 Client VPN 에 로그인할 수 있다
- AWS Managed Microsoft AD 를 바라보게 하거나 회사 전산실에 있는 기존 AD 를 바라보게 할 수도 있다 
#### 2. Mutual Authentication (상호 인증/인증서 기반)
- 인증서를 사용하여 신원을 검증하며, 사용자마다 각각 고유한 클라이언트 인증서를 발급받아 사용하는 것을 권장한다 
- ID/PW 를 치는 방식이 아닌 승인된 클라이언트 인증서 파일이 내 노트북 안에 물리적으로 설치되어 있어야 한다 
#### 3. Single Sign-On (SSO/SAML 2.0 기반 인증)
- SAML 2.0 기반의 IdP(자격 증명 공급자, ex>Okta)를 통해 유저 단위 인증을 수행하며, 한 번에 단 하나의 Identity Provider 만 연결할 수 있다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)