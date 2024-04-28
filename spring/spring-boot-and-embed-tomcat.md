## (spring) 스프링 부트와 내장 톰캣
> WAR 배포 방식의 단점, 내장 톰캣 + 부트 클래스 만들기, 실행 가능한 JAR 및 JAR 분석

<br>

## WAR 배포 방식의 단점
- 톰캣같은 WAS 를 별도로 설치해야 한다.
- 애플리케이션 코드를 WAR로 빌드해야 한다.
- 빌드한 WAR 파일을 WAS 에 배포해야 한다.<br>
→ JAR 를 사용하면 JAR 안에 다양한 라이브러리들과 WAS 라이브러리가 포함되어 main() 메서드를 실행해서 동작한다. ( 내장 톰캣 )

<br>

## 내장 톰캣 + 부트 클래스 만들기
main() 을 실행하면 아래와 같이 동작한다. ( 스프링 연동 )
```java
public class MySpringApplication {  
    /**  
     * @param configClass ( 스프링 설정 )  
     * @param args  
     */  
    public static void run(Class configClass, String[] args) {  
        System.out.println("MySpringApplication.main args=" + List.of(args));  
        //1. 톰캣 설정 : 내장 톰캣을 생성해서 8080 으로 연결
        Tomcat tomcat = new Tomcat();  
        Connector connector = new Connector();  
        connector.setPort(8080);  
        tomcat.setConnector(connector);  
  
        //2. 스프링 컨테이너 생성 : 필요한 빈들 모두 등록
        AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();  
        appContext.register(configClass);  
  
        //3. 스프링 MVC 디스패처 서블릿 생성, 스프링 컨테이너 연결  
        DispatcherServlet dispatcher = new DispatcherServlet(appContext);  
  
        //4. 디스패처 서블릿을 내장 톰캣에 등록
        Context context = tomcat.addContext("", "/");  
        // org.apache.catalina.LifecycleException: Failed to start 발생하여 추가  
//        File docBaseFile = new File(context.getDocBase());  
//        if (!docBaseFile.isAbsolute()) {  
//            docBaseFile = new File(((org.apache.catalina.Host) context.getParent()).getAppBaseFile(), docBaseFile.getPath());  
//        }  
//        docBaseFile.mkdirs();
        tomcat.addServlet("", "dispatcher", dispatcher);  
        context.addServletMappingDecoded("/", "dispatcher");  

		//5. 내장 톰캣 실행
        try {  
            tomcat.start();  
        } catch (LifecycleException e) {  
            throw new RuntimeException(e);  
        }  
  
    }  
}
```

나만의 부트 클래스를 만들기위해 컴포넌트 스캔 기능이 추가된 애노테이션을 직접 만든다.
```java
@Target(ElementType.TYPE)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented  
@ComponentScan  
public @interface MySpringBootApplication {  
}
```

가장 상위 패키지에 MSpringBootMain 클래스를 만들어 직접 만든 애노테이션을 추가한다.
> 상위 패키지에 위치한 이유는 컴포넌트 스캔의 기본 동작이 해당 애노테이션이 붙은 클래스의 현재 패키지부터 그 하위 패키지를 대상으로 하기 때문이다. 
```java
@MySpringBootApplication  
public class MySpringBootMain {  
    public static void main(String[] args) {  
        System.out.println("MySpringBootMain.main");  
        MySpringApplication.run(MySpringBootMain.class,args);  
    }  
}
```

<br>

## 스프링 부트
지금까지 만든 것을 라이브러리로 만들어서 배포하면 그것이 스프링 부트이다. 스프링 부트를 사용하면 스프링 컨테이너를 생성하고 WAS(내장 톰캣)을 생성하는 일을 자동으로 해준다.
- 일반적인 스프링 부트 사용법
```java
@SpringBootApplication  
public class BootApplication {  
    public static void main(String[] args) {  
       SpringApplication.run(BootApplication.class, args);  
    }  
}
```

### 빌드와 배포
1. build : 빌드를 하게되면 /build/libs 내 jar 파일이 생긴 것을 알 수 있다. 
```shell
> gradlew clean build
```
2. 배포 ( 실행 )
```shell
> java -jar [파일이름].jar
```

### 실행 가능한 JAR ( Executable Jar )
원래 jar 내부에 jar 를 포함할 수 없는데 스프링 부트는 이를 해결하기 위해 특별한 구조의 jar 를 만들어 내부에 jar 를 포함해서 실행할 수 있게 했다.
-  어떤 라이브러리가 포함되어있는 지 쉽게 확인할 수 있다.
- 같은 경로에 있더라도 파일명 중복을 해결하여 모든 파일을 읽을 수 있다.

### JAR 분석
**- 압축 풀기**
```shell
> jar -xvf [파일이름].jar
```
**- 내부 구조 분석**
  - META-INF
    - MANIFEST.MF
  - org/springframework/boot/loader
    - JarLauncher.class: 스프링 부트 main() 실행 클래스
  - BOOT-INF
    - classes : 우리가 개발한 class 파일과 리소스 파일
    - libs : 외부 라이브러리 ( .jar )
    - classpath.idx : 외부 라이브러리 경로
    - layers.idx : 스프링 부트 구조 경로

**- 실행 과정** 
1. java -jar xxx.jar
2. MANIFEST.MF 인식<br>
```text
Manifest-Version: 1.0
Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: hello.boot.BootApplication
Spring-Boot-Version: 3.0.2
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
Spring-Boot-Layers-Index: BOOT-INF/layers.idx
Build-Jdk-Spec: 17
```
3. JarLauncher.main() 실행 → BOOT-INF/classes/ 및 BOOT-INF/lib/ 인식<br>
4. BootApplication.main() 실행<br>
<br>

> IDE 에서 직접 실행할 때는 BootApplication.main() 을 바로 실행한다. IDE 가 필요한 라이브러리를 모두 인식할 수 있게 도와주기 때문에 JarLauncher 가 필요하지 않다.

<br>

## 참고
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 
