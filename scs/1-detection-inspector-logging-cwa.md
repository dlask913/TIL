## (aws) Detection - Inspector, Logging, CWA
> Amazon Inspector, Logging, Unified CloudWatch Agent (procstat Plugins)

<br>

## Amazon Inspector
- 자동적으로 보안 평가 수행
- EC2, ECR, Lambda 에 대한 CVE 기준 패키지 취약점 진단
- 인프라가 변경될때마다 자동으로 진단이 수행되며 risk score 를 매긴다
- For EC2 instances
	- AWS system Manager (SSM) agent 활용
	- 의도하지않은 네트워크 접근성 분석
	- 실행중인 OS 의 알려진 취약점 분석
- For Container Images push to Amazon ECR
	- 푸쉬할때마다 컨테이너 이미지 평가
- For Lambda Functions
	- 실행되는 코드와 패키지 종속성의 소프트웨어 취약점 식별
	- 배포될때마다 함수 평가
- AWS Security Hub 에 결과를 기록하고 EventBridge 로 보낼 수도 있다

<br>

## Logging 
#### Service Logs 
- CloudTrail trails - 모든 API 호출 추적
- Config Rules - 지속적인 설정 및 컴플라이언스 관리
- CloudWatch Logs - for full data retention (전체 데이터 보존)
- VPC Flow Logs - VPC 내 IP 트래픽 검토
- ELB Access Logs - 로드 밸런서에 대한 요청의 메타데이터 제공
- CloudFront Logs - 웹 배포 시 발생 로그 
- WAF Logs - 자신을 통과하는 모든 웹 요청을 분석한 뒤 남긴 전체 로깅
#### Strategy
- S3 에 저장한 로그들은 AWS Athena 를 통해 분석할 수 있다
- 로그를 암호화하여 S3에 저장하는 것이 좋고 IAM & Buckte Policies, MFA 를 통해 접근을 제어할 수 있다
- 로그를 오래 보관해야하는 경우 비용 절감을 위해 Glacier 로 옮긴다 ( can view more than 7 years )

<br>

## Unified CloudWatch Agent
- 가상 서버용이다 (EC2 instances, on-premises servers, ..)
- RAM, 프로세스, 디스크 사용 공간 등과 같은 추가적인 시스템 레벨 메트릭 수집
- 기본적으로 EC2 모니터링 대시보드를 보면 CPU 사용량은 잘 나오는데 RAM 사용량이나 디스크 용량은 보이지 않는다 
- 설치 자체는 무료고 `[인스턴스 ID + OS 종류 + 메트릭 이름]` 조합 당 1개의 지표가 되어 처음 10개 지표는 기본 무료고 이후 1개당 한 달에 $0.30
- 서버에서 수집한 로그를 CloudWatch Logs 로 보낸다
- SSM Parameter Store를 사용하여 EC2, 온프레미스 로그를 한 곳에서 중앙 집중식으로 관리할 수 있다
- 로그와 메트릭을 push 하는 IAM 권한이 부여되어야 한다
- agnet 가 수집한 메트릭은 CWAgent 네임스페이스에 있으며 변경이 가능하다

### procstat Plugin
- 서버의 각 프로세스 사용량을 모니터링하고 메트릭을 수집
- Linux 와 Windows 지원
- ex> amount of time the process uses CPU, amount of memory the process uses, ..
- 지표를 통해 모니터 할 프로세스를 선택할 수 있다
```
1. pid_file : pid 가 있는 파일
2. exe : 실행 프로세스의 이름 (RegEX)
3. pattern : 프로세스를 실행한 command lines (RegEX)
```
- procstat plugin 에서 수집한 메트릭들은 `procstat` 으로 시작한다 ( procstat_cpu_time, ..)

<br>

## 참고
[Ultimate AWS Certified Security Specialty [NEW 2026] SCS-C03](https://www.udemy.com/share/1084Uy3@vtr5jBSWAvNzuXvuNSDo7WChACAEgUkcrlE2b4Fcu_fDAjT1Rm9Amazz5GvnNTZtEQ==/)