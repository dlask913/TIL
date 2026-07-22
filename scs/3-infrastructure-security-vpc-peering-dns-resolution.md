## (aws) Infrastructure Security - VPC Peering, DNS Resolution in VPC
> VPC Peering, DNS Resolution in VPC

<br>

## VPC Peering
![image](https://docs.aws.amazon.com/ko_kr/prescriptive-guidance/latest/integrate-third-party-services/images/p2_vpc-peering.png)
- VPC peering 은 인터넷, VPN 연결 또는 별도 물리적 하드웨어에 의존하지 않고 AWS 의 글로벌 백본 네트워크 인프라를 통해 VPC 간 통신을 수행한다
- AWS 의 네트워크망을 이용하여 두 개의 VPC 를 사설(private) 방식으로 연결한다
- 두 VPC 가 마치 하나의 동일한 네트워크 안에 있는 것처럼 동작하게 한다
- 연결하려는 VPC 간 IP 주소 대역(CIDR)이 중복되면 안된다
- VPC Peering connection is NOT transitive → 서로 통신해야 하는 모든 VPC 쌍마다 피어링을 직접 맺어야 함
- EC2 인스턴스 간 통신이 실제로 이루어지려면 각 VPC 서브넷의 라우팅 테이블을 직접 업데이트해야 한다

#### 정리
**IP 대역이 중복되지 않는 두 VPC**를 AWS 전용망으로 **직접 연결**하여 사설 통신을 가능하게 해주는 기술이며 **라우팅 테이블을 수동 설정**해야 하고 **중계 통신(Transitive)이 불가능**하다는 규칙을 가진다

### Good to know
- 서로 다른 계정이나 서로 다른 리전에 위치한 VPC 간에도 VPC Peering 생성 가능
- 피어링된 상대방 VPC 의 보안 그룹을 내 보안 그룹의 규칙에서 직접 참조할 수 있다
- 위에서 말하는 참조는, SG 접근 허용 규칙을 만들 때 IP 주소 대신 `특정 보안 그룹의 ID` 를 적어넣는 것을 의미
- 다른 계정의 보안 그룹을 참조할 때는 **`[상대방 AWS 계정 ID]/[상대방 SG ID]`** 형태로 입력해야 제대로 참조가 적용

<br>

## DNS Resolution in VPC
#### DNS Resolution (enableDnsSupport)
- VPC 내에서 Route 53 Resolver 서버를 통한 DNS resolution 이 지원되는지 여부 결정 => VPC 내부 자원들이 AWS 기본 DNS 서버를 이용해 도메인을 IP 로 변환할 수 있는지
- True 가 기본값으로, 169.254.169.253 또는 VPC IPv4 네트워크 대역의 기본(Base) IP 주소에 2를 더한 예약된 IP 주소(ex> 10.0.0.2)에 위치한 Amazon Provided DNS Server에 쿼리를 전송
- AWS VPC 를 생성하면 Amazon Provided DNS / Route 53 Resolver 가 기본적으로 제공되는 내장 DNS 서버이다
- 회사 내부 전용 도메인을 처리하기 위해 VPC 내장 DNS 대신 자체 DNS 서버만 바라보도록 고정하고 싶을 때 DNS Support를 끈다
#### DNS Hostnames (enableDnsHostnames)
- 처음에 기본으로 제공되는 Default VPC 는 True 로 켜져있으나 사용자가 VPC 를 직접 새로 만들면(Custom VPC) 이 값이 False 로 꺼져 있다
- enableDnsSupport 가 True 가 아니라면 아무런 동작도 하지 않는다
- True 로 설정되어 있고 EC2 인스턴스가 퍼블릭 IPv4 를 가지고 있다면 해당 인스턴스에 퍼블릭 DNS 호스트이름을 자동으로 할당한다

#### VPC 내부용 사설 도메인(Private Hosted Zone)

```
[ EC2 Instance ] 
   │ 
   │  1. 질의: "db.internal.local IP가 뭐야?"
   ▼
[ AmazonProvidedDNS (Route 53 Resolver) ]
   │ (169.254.169.253 또는 VPC CIDR + 2)
   │
   ├─► [체크 1] enableDnsSupport = True 인가?  ──► (NO: 조회 불가 / 에러)
   ├─► [체크 2] enableDnsHostnames = True 인가? ──► (NO: Private Zone 참조 불가 / 에러)
   │
   ▼ (YES)
[ Route 53 Private Hosted Zone ] 
   │
   └─► "10.0.1.50 이다!" (Private IP 응답)
```

- 만약 private hosted zone 에 있는 cusotm DNS domain name 을 사용한다면 반드시 enableDnsSupport & enableDnsHostname 을 true 로 설정해야 한다

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)