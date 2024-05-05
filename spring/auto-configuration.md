## (spring) 자동 구성 라이브러리 만들기 
> 모든 구성이 자동으로 처리되도록 라이브러리를 직접 만들어 프로젝트에 추가해보기.

<br>

## 스프링 부트 자동 구성 (Auto Configuration - [공식문서](https://docs.spring.io/spring-boot/docs/current/reference/html/auto-configuration-classes.html) )
: 수많은 빈들을 자동으로 등록해주는 기능, 반복적이고 복잡한 빈 등록과 설정을 최소화해줌
- <b>@AutoConfiguration</b> : 자동 구성 사용, 자동 구성이 실행되는 순서를 지정할 수 있다.
- <b>@Conditional  ( @ConditionalOnXXX )</b> : IF문과 유사한 기능으로 해당 클래스가 있는 경우에만 동작.
- <b>@Import</b> : 스프링에서 자바 설정을 추가할 때 사용

<br>

## 자동 구성 라이브러리 만들기
### 1. 스프링 부트 플러그인을 사용하게 되면 실행 가능한 Jar 구조를 기본으로 하기 때문에 사용 X. 
```kotlin
plugins {  
    id 'java'  
//  id 'org.springframework.boot' version '3.0.12'  
//  id 'io.spring.dependency-management' version '1.1.3'  
}
```

### 2. 원하는 기능을 개발하고 자동 구성 설정
: 실행 시점에 memory=on 설정을 넣었을 때만 빈 등록하도록 자동 구성 설정
```java
@AutoConfiguration  
@ConditionalOnProperty(  
    name = {"memory"},  
    havingValue = "on"  
)  
public class MemoryAutoConfig {  
    public MemoryAutoConfig() {  
    }  
  
    @Bean  
    public MemoryController memoryController() {  
        return new MemoryController(this.memoryFinder());  
    }  
  
    @Bean  
    public MemoryFinder memoryFinder() {  
        return new MemoryFinder();  
    }  
}
```

### 3. 자동 구성 대상 지정
> 스프링 부트는 시작 시점에 org.springframework.boot.autoconfigure.AutoConfiguration.imports 의 정보를 읽어 자동 구성으로 사용 <br>

: src/main/resources/META-INF/spring/ org.springframework.boot.autoconfigure.AutoConfiguration.imports 내 앞서 생성한 Config 클래스를 패키지 포함하여 지정.
```text
memory.MemoryAutoConfig
```

### 4. 빌드하기
```shell
./gradlew clean build
```

### 5. /build/libs 내 -jar 파일을 이 기능을 추가할 다른 프로젝트에 libs 폴더를 만들어 복사
![image](https://github.com/team-archivist/archivist-backend/assets/79985588/2d137d33-c3b4-4c17-b81e-149361300a70)

### 6. build.gradle 내 라이브러리 추가
```kotlin
dependencies {  
    implementation files('libs/memory-v2.jar')
    ..
}
```

<br>

## 스프링 부트 자동 구성 동작
: @SpringBootApplication → @EnableAutoConfiguration → @Import(AutoConfigurationImportSelector.class )
```java
@SpringBootApplication  
public class ProjectV2Application {  
    public static void main(String[] args) {  
        SpringApplication.run(ProjectV2Application.class, args);  
    }  
}
```

<br>

## 참고 
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 