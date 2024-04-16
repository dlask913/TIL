## (spring) Spring 과 Spring Boot 및 WAR 빌드와 배포
> Spring 과 Spring boot 및 WAR 빌드와 배포

<br>

## spring 과 spring boot
### spring 
- DI 컨테이너, MVC, DB 접근 기술 등 수많은 기능을 제공하고 다양한 문제 해결
- 다양한 라이브러리들을 편리하게 사용할 수 있도록 통합
- 기능과 라이브러리가 점점 많아져 스프링으로 프로젝트를 시작할 때 필요한 설정이 점점 늘어남
### spring boot
- 시작을 위한 복잡한 설정 과정을 해결
- WAS: Tomcat 같은 웹 서버를 내장해서 별도의 웹 서버 설치 안해도 됨
- 스타터 종속성 제공 및 스프링과 외부 라이브러리 버전을 자동으로 관리
- 프로젝트 시작에 필요한 스프링과 외부 라이브러리 빈 자동 등록
- 환경에 따라 달라져야 하는 외부 설정 공통화
- 메트릭 및 상태 확인 기능 제공 <br>
→ 스프링 프레임워크를 쉽게 사용할 수 있게 도와주는 도구 <br>
→ 본질은 스프링 프레임워크

<br>

## WAS 설치 및 WAR 빌드와 배포 
최근에는 스프링 부트가 내장 톰캣을 포함하고 있지만 기존에는 WAS를 설치하고 war 를 배포하였었다. 
### 1. 톰캣 설치 및 실행 방법
a. [공식 홈페이지](https://tomcat.apache.org/download-10.cgi) 내 Core 탭에 zip 파일 다운로드 <br>
b. 압축 풀고 bin 폴더 내 startup.bat 실행하여 서버 시작 ( window 기준 ) <br>
c. bin 폴더 내 shutdown.bat 실행하여 서버 중지

 ### 2. WAR 빌드 및 배포
 a. 해당 프로젝트 내에서 build 하기
```shell
> ./grdlew build
```
b. 프로젝트 > libs 폴더 내 project-name-0.0.1-SNAPSHOT.war 파일 생성 확인<br>
<br>
c. 압축 풀어서 디렉토리 확인하기
```shell
> jar -xvf project-name-0.0.1-SNAPSHOT.war
```
d. 실행 결과 <br>
![image](https://github.com/dlask913/TIL/assets/79985588/142424a7-3d9a-44e1-9986-75ff0b05ee6f)


### 3. IntelliJ 톰캣 설정 ( 유료 ) 하여 서버 실행하기
a. 우측 상단에서 Configuration Edit 클릭 <br>
<img src="https://github.com/dlask913/TIL/assets/79985588/02cb6ddd-71f5-46e4-959b-d8daa15f8e03" alt="Configuration Edit" width="600">


b. 좌측 + 버튼 클릭하여 Tomcat  Server -local 추가 <br>
<img src="https://github.com/dlask913/TIL/assets/79985588/5c0e0370-e1ab-4895-adf1-5244aa6a3668" alt="Configuration Edit" width="600">

c. Server 탭 내 tomcat 폴더 선택<br>
<br>
d. Deployment 탭 내 + 버튼 클릭하여 .war (exploded) 파일 선택<br>
<img src="https://github.com/dlask913/TIL/assets/79985588/821bebea-3568-4a1e-939a-9b54ee4dcf62" alt="Configuration Edit" width="600">
<br>
** 파일 선택 이후 하위 Application context 비우기<br>
<br>
e. 실행하여 정상 동작 확인


<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 