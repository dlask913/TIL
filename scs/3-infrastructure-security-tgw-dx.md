## (aws) Infrastructure Security - Transit Gateway, Direct Connect
> Transit Gateway, Direct Connect(DX) - DXGW, Connection Types, Encryption, Resiliency, as a backup

<br>

## Transit Gateway
- 기존 VPC Peering 방식은 VPC 가 늘어날때마다 1:1 Mesh 구조로 연결해야했는데 Transit Gateway 는 TGW 를 중앙 Hub 로 두고 각 VPC 와 온프레미스를 spoke 로 연결하는 별 모양 구조 (hub and spoke) -> 수천 개의 VPC 와 온프레미스 네트워크를 중앙에서 관리
- 특정 AWS 리전에 생성되는 리소스
- AWS RAM(Resource Access Manager) 를 사용하면 하나의 AWS 계정에 만든 TGW 를 다른 AWS 계정들에게 공유할 수 있다
- 서로 다른 리전에 위치한 TGW 끼리 직접 피어링 연결을 맺을 수 있다
- TGW 내부 라우팅 테이블을 이용해 통신 범위를 제어할 수 있다
- Works with Direct Connect Gateway, VPN Connections
- 다른 네트워크 서비스(일반 VPC, VPC Peering 등)는 멀티캐스트를 지원하지 않지만 TGW는 IP 멀티캐스트를 공식 지원한다

### Site-to-Site VPN 에서 ECMP 를 활용한 네트워크 대역폭 확장

![image](https://docs.aws.amazon.com/images/whitepapers/latest/aws-vpc-connectivity-options/images/transit-gateway-and-site-to-site-vpn.png)

- ECMP (Equal-cost multi-path) : 네트워크에서 특정 목적지로 가는 비용이 동일한 경로가 여러 개 존재할 때 트래픽을 이 경로들로 분산(로드 밸런싱)하여 전송하는 라우팅 방식 -> 라우터가 "모두 최적의 경로" 라고 판단되어 복수의 경로로 패킷을 나누어 전송 (효율적)
- AWS Site-to-Site VPN 연결은 1개당 최대 1.25 Gbps 의 대역폭 제한을 가지기 때문에 단일 VPN 연결의 처리량 제한 문제점이 있음
- Usecase : 여러 개의 VPN 연결을 동시에 뚫고 ECMP 를 적용하여 1.25 Gbps 이상으로 네트워크 대역폭을 선형적으로 늘릴 수 있다

<br>

## Direct Connect(DX)
- 인터넷을 거치지 않고 고객의 데이터 센터와 AWS 를 연결하는 전용선 서비스
- 원거리 네트워크(사무실/데이터센터)에서 사용자의 vpc 로 연결되는 dedicated private connection 을 제공한다
- 사용자의 데이터센터와 AWS Direct Connect location 사이에 dedicated connection 을 구축해야 한다
- 사용자 VPC 에 Virtual Private Gateway 를 설정해야 한다
- 동일한 연결선 하나로 public resources(S3) 와 private(EC2) 에 모두 접근할 수 있다
- IPv4 와 IPv6 모두 지원한다
- Usecase
```
1. 매일 terabyte 단위 이상의 데이터를 전송할 때, 인터넷 전송 대비 데이터 전송 요금이 저렴해진다 
2. 인터넷은 사용자가 몰리면 랙(Latency)이 걸리지만 DX 는 일관된 대역폭을 포장한다. 실시간 금융 트레이딩, 음성/영상 스트리밍처럼 지연 시간이 일정해야 하는 시스템에 적용
3. 하이브리드 환경 구성 : 기업 내부 온프레미스 데이터센터와 AWS 클라우드를 하나의 내부망처럼 묶어 사용할 수 있다
```

<br>

### Direct Connect Gateway
![image](https://docs.aws.amazon.com/images/directconnect/latest/UserGuide/images/dx-gateway.png)
- 서로 다른 리전에 위치한 하나 이상의 VPC 로 Direct Connect 를 연결하려면 DXGW 를 사용해야 한다 

<br>

### Connection Types
#### 전용 연결 (Dedicated Connections)
- 1Gbps ~400Gbps 까지 지원
- AWS 가 제공하는 가장 높은 대역폭 옵션으로, 초고속/대용량 트래픽을 처리할 때 선택하는 물리 포트 방식
- 타 고객과 물리 포트를 공유하지 않는 독점 방식으로, 고객이 AWS 콘솔을 통해 신청하면 물리적인 회선 케이블 연결 공사를 진행한다.
- 구축까지 몇 주에서 1개월 정도 소요
#### 호스트 연결 (Hosted Connections)
- 50Mps ~25Gbs 까지 지원
- 파트너사가 미리 구축해둔 대용량 물리 회선의 일부 대역폭을 논리적으로 쪼개서 대여받는 방식
- AWS Direct Connect 파트너사를 통해 신청
- 물리 케이블 재공사 없이 설정 변경만으로 대역폭을 빠르게 업그레이드/다운그레이드 가능

<br>

### Encryption
![image](https://docs.aws.amazon.com/images/whitepapers/latest/aws-vpc-connectivity-options/images/aws-direct-connect-transit-gateway-site-to-site-vpn-public-vif.png)
- 전송중인 데이터는 암호화되지 않지만 공용 인터넷망을 타지 않아 외부 접근으로부터 안전하다
- DX 에 VPN 을 결합하면 IPsec 으로 암호화된 private 연결을 제공한다 
- 추가적인 보안을 확보하기에 좋지만 DX 설정 외에도 VPN 터널링, BGP 라우팅, 키 교환 등 관리하기 복잡해진다

<br>

### Resiliency (회복탄력성)
- **Maximum** Resiliency for Critical Workloads (SLA 99.99%)
  - 2개의 서로 다른 DX 로케이션에 각각 2개씩 총 4개의 전용 회선을 생성

- High Resiliency for Critical Workloads (SLA 99.9%)
  - 2개의 서로 다른 DX 로케이션에 각각 1개씩 총 2개의 전용 회선을 생성

![image](https://d2908q01vomqb2.cloudfront.net/5b384ce32d8cdef02bc3a139d4cac0a22bb029e8/2020/08/10/Screen-Shot-2020-08-10-at-2.33.49-PM-1024x552.png)

<br>

### Site-to-Site VPN connection as a backup
- DX 회선에 장애가 발생하는 경우를 대비해 백업용 DX 회선(expensive)을 추가로 구성하거나 Site-to-Site VPN 연결을 백업으로 구성할 수 있다 
- Site-to-Site VPN 백업 구성 : 평소에는 DX 전용선으로 통신하고 인터넷망을 통해 AWS 와 연결되는 IPsec VPN 터널을 백업 통로로 만들어둔다.

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)