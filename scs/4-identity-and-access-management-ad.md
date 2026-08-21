## (aws) Identity and Access Management - AD
> Microsoft Active Directory, ADFS, AWS Directory Services, Connect to on-premises AD, Active Directory Replication

<br>

## Microsoft Active Directory
- Windows Server OS 에 AD Domain Services 를 설치하면 해당 서버가 도메인 컨트롤러(DC)가 되어 Active Directory 기능을 수행한다 
- Database of objects: User Accounts, Computers, Printers, File Shares, Security Groups
- PC마다 계정을 따로 만들 필요 없이, AD에서 계정을 하나 만들면 네트워크 내 모든 승인된 PC에 로그인할 수 있고 권한도 중앙에서 제어한다 
- 도메인을 계층적인 트리 형태로 구성하여 관리 영역을 나눈다. 

<br>

### ADFS (Active Directory Federation Services)
- 여러 애플리케이션에 걸쳐 Single Sign-On을 제공한다 
- SAML across 3rd party: AWS Console, Dropbox, Office365, etc…

<br>

### AWS Directory Services
#### AWS Managed Microsoft AD
- AWS VPC 내 실제 AD를 직접 구축하고 AWS 가 관리해주는 서비스 
- 사용자를 AWS AD 내부에서 직접 관리 가능하며 MFA 를 지원한다 
- 온프레미스 AD와 신뢰 관계를 맺어 서로의 계정을 공유/연동할 수 있다
- EC2 Windows 인스턴스가 이 도메인에 조인하여 SharePoint 등 기존 AD 기반 애플리케이션을 실행할 수 있다
- RDS for SQL Server, Amazon WorkSpaces, QuickSight 등 다양한 AWS 서비스와 기본 통합이 가능하다
- 2개 AZ에 Multi-AZ 배포, 트래픽 확장을 위해 도메인 컨트롤러(DC) 수 증설 가능이 가능하다
#### AD Connector
- 계정 정보를 AWS에 저장하지 않고 인증 요청만 온프레미스 AD로 전달해주는 디렉터리 Proxy 서비스 
- 계정 및 사용자 관리는 기존 온프레미스 AD에서 100% 전담
- AWS 캐싱이나 데이터 복제 없이 온프레미스로 인증 redirect 만 수행하며 MFA를 지원한다 
- 요청을 사내 AD 로 안전하게 전달해야 하므로 AWS 와 온프레미스 간 DX 혹은 VPN 터널링이 필수적이다 
- SQL Server와 연동 불가, Seamless Domain Join 불가, 디렉터리 공유 불가
  - EC2 Seamless Domain Join은 EC2 인스턴스를 생성(부팅)하거나 운영할 때 수동 명령어나 재부팅 없이 자동으로 AD 도메인에 가입시키는 기능을 말한다
#### Simple AD
- Samba 4 기반의 AD 호환용 소규모 독립 디렉터리 서비스 
- 온프레미스 연동 불가, AWS 전용으로 간단한 LDAP 인증 서버가 필요하거나 최소한의 AD 기능만 필요할 때 비용 효율적이다
- MFA, RDS SQL Server, AWS SSO 미지원
- Small: 500 users, large: 5000 users
- No trust relationship

<br>

### Connect to on-premises AD
- 온프레미스 AD 를 AWS Managed Microsoft AD 에 연결하는 기능
- Direct Connect(DX) 혹은 VPN 연결을 반드시 생성해야 한다 
- Can setup three kinds of forest trust: 단방향, 역방향, 양방향 모두 가능
- forest trust 는 데이터를 동기화하는 것이 아니라 인증 요청이 발생했을 때 상대방 AD로 인증을 위임(Pass-through Authentication)하는 방식을 말한다 

<br>

### Solution Architecture: Active Directory Replication
> on-premises AD ↔ Microsoft AD on EC2 ↔ AWS Managed Microsoft AD
 
- AWS 연결 시 인증 Latency을 줄이고 DX/VPN 장애에 대비하기 위해 EC2에 추가 도메인 컨트롤러(DC)를 직접 구축하여 복제하는 패턴을 말한다
- EC2 에 직접 구축한 AD와 AWS Managed Microsoft AD 간 trust 관계를 맺는다 
- **On-premises Microsoft AD**: 사내 도메인 (`onpremAD.example.com`)
- **Microsoft AD on EC2 (Self Managed Replica)**: AWS VPC 내부 EC2 인스턴스에 온프레미스 AD의 복제본(Additional DC)을 직접 설치한 형태. 온프레미스 AD와 AD 데이터 복제를 지속 수행
- **AWS Managed Microsoft AD DC**: AWS 전용 도메인 (`awsAD.example.com`). EC2에 띄운 온프레미스 AD 복제본과 양방향 트러스트(Two-way Trust) 관계를 맺어 계정 인증을 서로 연동

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)