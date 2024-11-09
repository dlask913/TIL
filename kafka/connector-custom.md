# 커넥터 커스텀
> 커스텀 소스 커넥터, Importance 설정 기준, 커스텀 싱크 커넥터

<br>

## 커스텀 소스 커넥터 ( Producer 역할 )
- 소스 애플리케이션 또는 소스 파일로부터 데이터를 가져와 토픽으로 넣는 역할을 한다. 
- 오픈소스의 라이선스 문제나 로직이 요구사항과 맞지 않는 경우 카프카 커넥트 라이브러리에서 제공하는 SourceConnector 와 SourceTask 클래스를 상속받아 직접 소스 커넥터를 구현할 수 있다. 
- 직접 구현한 소스 커넥터를 빌드하여 jar 파일로 만들어서 커넥트 실행 시 플러그인으로 추가하여 사용할 수 있다. 
### 커스텀 소스 커넥터 dependency 
```groovy
dependencies {
	compile 'org.apache.kafka:connect-api:2.5.0'
}
```

### SourceConnector 와 Source Task
#### 1. SourceConnector
- 태스크를 실행하기 전 커넥터 설정 파일을 초기화하고 어떤 태스크 클래스를 사용할 것인지 정의하는 데에 사용한다. 
- 실질적으로 데이터를 다루지 않는다. 
#### 2. SourceTask
- 소스 애플리케이션 또는 소스 파일로부터 데이터를 가져와서 토픽으로 데이터를 보내는 역할을 수행한다. ( 실질적으로 데이터를 다루는 부분 )
- 토픽에서 사용하는 오프셋이 아닌 자체적인 오프셋을 사용하고, 이 오프셋은 소스 애플리케이션 또는 소스 파일을 어디까지 읽었는 지 저장한다. 
- 위 오프셋을 통해 데이터를 중복해서 보내는 것을 방지할 수 있다. 

### 소스 커넥터 구현 시 주의사항
- 소스 커넥터를 구현하기 전에 build.gradle 에 connect-api 라이브러리와 빌드된 파일을 jar 로 압축하기 위한 스크립트를 작성한다. 
- 커넥터를 커스텀하여 플러그인으로 커넥트에 추가할 때 사용자가 직접 작성한 클래스 뿐 아니라 참조하는 라이브러리도 함께 빌드하여 jar 로 압축해야한다. ( 그렇지 않을 경우 `ClassNotFoundException` 이 발생 )
```groovy
jar {
	from {
		configurations.compile.collect {it.isDirecotry() ? it : zipTress(it)}
	}
}
```

<br>

## 중요도(Importance) 지정 기준
- 커넥터를 개발할 때 옵션값의 중요도를 Importance enum 클래스로 지정할 수 있다. → `HIGH`, `MEDIUM`, `LOW`
- 옵션을 정하는 명확한 기준은 없으나 사용자에게 명시적으로 표시하기 위한 문서로 사용한다. 
- 커넥터에서 반드시 사용자가 입력한 설정이 필요한 값은 `HIGH`, 사용자의 입력값이 없더라도 기본값이 있는 옵션을 `MEDIUM`, 사용자의 입력값이 없어도 되는 옵션을 `LOW` 로 구준한다. 

<br>

## 커스텀 싱크 커넥터 ( Consumer 역할 )
- 토픽의 데이터를 타깃 애플리케이션 또는 타깃 파일로 저장하는 역할을 한다. 
- 카프카 커넥트 라이브러리에서 제공하는 `SinkConnector` 와 `SinkTask` 클래스를 상속받아 직접 싱크 커넥터를 구현할 수 있다. 
- 직접 구현한 싱크 커넥터를 빌드하여 jar 로 만들어서 커넥트의 플러그인으로 추가하여 사용할 수 있다. 


<br>

## 참고
[아파치 카프카 애플리케이션 프로그래밍](https://inf.run/uCwV5)