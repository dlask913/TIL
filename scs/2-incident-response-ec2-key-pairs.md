## (aws) Incident Response - EC2 Key Pairs
> EC2 Key Pairs, EC2 Serial Console, When Lost SSH Key Pairs 

<br>

## EC2 Key Pairs
```
1. 사용자는 EC2 인스턴스를 생성하며 EC2 Key Pair 를 생성
2. priate key 는 사용자가 다운로드하고 AWS 에서는 삭제된다. 
   public key 는 EC2 EBS Volume(Root) ~/.ssh/authorized_keys 에 저장된다.
3. 사용자는 private key 로 EC2 에 SSH 접속한다.
```
- priate key 는 잃어버리면 복구할 수 없다 
- AWS 밖에서 key pair 를 생성하여 업로드 할 수도 있다 
- ED25519 과 2048-bit SSH-2 RSA keys 를 지원한다
- EC2 Console 에서 key pair 를 지워도 EBS root volume 에서 지워지지 않는다 
- old public key 가 있는 Prebuilt AMI 로 EC2 를 시작하면, 시작할 때 만든 새로운 EC2 key pair 둘 다 보관된다 

<br>

### Remediaing Exposed EC2 Key Pairs
- ~/.ssh/authorized_keys 파일에서 모든 public keys를 지운다 
- 새로운 키를 만들고 ~/.ssh/authorized_keys 파일에 새로운 public key 를 추가한다
- public key 를 추가하고 지우는 과정을 SSM Run Command 를 통해 자동화할 수 있다

<br>

### EC2 Instance Connect Browser Based SSH 
#### 1. 사용자는 EC2 Console 혹은 Connect CLI 를 통해 접속 시도 
- EC2 Instance Connect API 가 내부적으로 일회성 키 쌍 생성
#### 2. 임시 퍼블릭 키 전송
- 생성된 임시 퍼블릭 키를 EC2 인스턴스 메타데이터로 전송
- 이 키는 60초 동안만 유효
#### 3. EC2 내부의 키 Fetch
- EC2 내부에 설치된 Instance Connect Agent 가 인스턴스 메타데이터를 확인하여 방금 전송된 임시 퍼블릭 키를 가져온다 
#### 4. SSH 접속
- EC2 Instnace Connect API 가 AWS 특정 IP 대역을 소스로 하여 대상 EC2 에 실제 SSH 접속 시도
- 인스턴스의 SG 인바운드 규칙에 특정 IP 대역 22번 포트 접속이 허용되어 있어야 한다 

<br>

## EC2 Serial Console
- EC2 에 네트워크 연결이 완전히 끊어졌거나 부팅이 실패했을 때 인스턴스의 시리얼 포트에 다이렉트로 접근할 수 있게 해주는 디버깅 도구 
- Nitro 기반 EC2 만 지원 가능
- OS user 와 password 가 미리 설정되어 있어야 한다 
- EC2 인스턴스 한 대당 오직 하나의 활성화된 세션만 허용
- 보안을 위해 기본적으로는 비활성화 되어있음
#### USECASE
- 부팅 중 시스템이 멈추거나 에러가 발생할 때 (troubleshooting)
- 네트워크 설정 오류 해결
- 재부팅 관련 문제 분석

<br>

## When Lost SSH Key Pair
### Linux EC2 Instance
#### 1. Using EC2 User Data
- 새로운 key pair 를 만들어서 instance 를 멈추고 EC2 User Data(cloud-config format) 을 업데이트 한다 
- 이전 User Data 는 삭제한다
#### 2. Using Systems Manager
- 서버 내에 SSM 에이전트만 켜져 있다면, `AWSSupport-ResetAccess` 기능을 통해 서버를 끄지 않고도 새 SSH 키를 안전하게 재발급 받아 파라미터 스토어에서 확인한 뒤 다시 접속할 수 있다
- windows 도 지원한다
#### 3. Using EC2 Instance Connect
- EC2 Connect API 임시 세션을 통해 EC2 에 접근하여 새로운 pubic key 를 저장한다 
- EC2 인스턴스 내부에 EC2 Instance Connect 에이전트가 설치되어 있어야 한다
#### 4. Using EC2 Serial Console
#### 5. Using EBS Volume Swap
- 기존 인스턴스의 EBS 루트 볼륨을 임시 인스턴스에 추가 장착하여 새 공개키를 심은 뒤, 원래 인스턴스에 되돌려 부팅한다

<br>

### Windows EC2 Instance
#### 1. Using EC2Launch v2
- 루트 볼륨을 다른 서버에 붙여 초기화 플래그 파일(`.run-once`)을 강제로 지운 뒤, 원래대로 돌려놓고 부팅시켜 새 비밀번호를 설정한다
#### 2. Using EC2Config
- Windows Server 2016 미만 구형 서버의 루트 볼륨을 다른 인스턴스에 붙여 `config.xml` 파일 내 `EC2SetPassword` 값을 `Enabled`로 직접 수정하여 새 비밀번호를 발급받는 옛날 방식이다
#### 3. Using EC2Launch
- Windows Server 2016 이상 버전이지만 최신 에이전트(EC2Launch v2)가 설치되어 있지 않은 경우
- Windows Server 2016 이상 서버의 루트 볼륨을 임시 서버에 붙인 뒤, AWS 공식 가이드 프로그램(`EC2Rescue`)의 클릭 메뉴를 이용해 복잡한 파일 수정 없이 안전하게 패스워드를 초기화한다
#### 4. Using Systems Manager
- AWS Systems Manager(SSM)를 활용해 원격으로 윈도우/리눅스 패스워드를 초기화한다
- EC2Rescue 런 커맨드 문서 활용 (Windows 전용)
- `AWSSupport-ResetAccess` 시스템 관리자 자동화 문서 활용 (Linux & Windows 공통)
- `AWS-RunPowerShellScript` 파워쉘 스크립트 직접 실행 (Windows 전용 하드코딩)

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)