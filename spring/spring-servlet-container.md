## (spring) 서블릿 컨테이너 초기화
> JAR와 WAR, 서블릿 컨테이너 초기화

<br>

## JAR 와 WAR
### JAR ( Java Archive )
: 자바의 여러 클래스와 리소스를 묶은 압축 파일, 이 파일은 JVM 위에서 직접 실행되거나 다른 곳에서 사용하는 라이브러리로 제공된다. 직접 실행하는 경우 main() 메서드가 필요하고 MANIFEST.MF 파일에 실행할 메인 메서드가 있는 클래스를 지정해두어야 한다. ( <b>JVM 위에서 실행</b> ) <br>

### WAR ( Web Application Archive )
: WAS 에 배포할 때 사용하는 파일, 정적 리소스와 클래스 파일을 모두 함께 포함하기 때문에 구조가 좀 더 복잡하다. ( <b>WAS 위에서 실행</b> ) <br>
#### ※ 구조 ※
- WEB-INF 
  - classes : 실행 클래스 모음
  - lib : 라이브러리 모음
  - web.xml : 웹 서버 배치 설정 파일 ( 생략 가능 )
- index.html : 정적 리소스

<br>

## 용어정리
### Servlet
: 요청을 처리하고 응답으로 응답하는 클래스. 서블릿을 사용하여 HTML 양식을 통해 사용자로부터 요청을 받고 DB에서 쿼리하고 웹 페이지를 동적으로 생성할 수 있다. <br>
→ 웹 서버에서 실행 중인 애플리케이션이 요청을 받으면 요청을 서블릿 컨테이너에 전달하고 서블릿 컨테이너는 이를 대상 서블릿에 전달한다. 
### Dispatcher Servlet
: 모든 HTTP 요청을 수신하고 이를 컨트롤러 클래스에 위임한다. 

<br>

## 서블릿 컨테이너 초기화 및 등록
WAS 를 실행하는 시점에 필요한 초기화 작업들을 하나씩 수행해보자.
### 1. 서블릿 컨테이너 초기화 개발
서블릿은 ServletContainerInitializer 라는 초기화 인터페이스를 제공하며, 이는 서블릿 컨테이너를 초기화 하는 기능이 있다. 서블릿 컨테이너는 실행 시점에 초기화 메서드인 onStartup() 을 호출해주고 여기서 애플리케이션에 필요한 기능들을 초기화하거나 등록할 수 있다. 
```java
public class MyContainerInitV1 implements ServletContainerInitializer {  
    /**  
	 * @param c : @HandlesType 에 애플리케이션 초기화 인터페이스를 지정하면,
	 * 이 파라미터를 통해 애플리케이션 초기화 인터페이스의 구현체들을 모두 전달받음
	 *  
	 * @param ctx : 필터나 서블릿 등록 가능
	 */
    @Override  
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {  
        System.out.println("MyContainerInitV1.onStartup");  
        System.out.println("MyContainerInitV1 c = " + c); 
        System.out.println("MyContainerInitV1 ctx = " + ctx);
    }  
}
```

서버 실행 전에 추가로 WAS 에게 실행할 초기화 클래스를 알려준다.
- resources/META-INF/services/jakarta.servlet.ServletContainerInitializer 생성
```text
hello.container.MyContainerInitV1
```

### 2. 서블릿 등록
서블릿 컨테이너 초기화 시점에 서블릿 등록하기.
```java
public class HelloServlet extends HttpServlet {  
    @Override  
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {  
        System.out.println("HelloServlet.service");  
        resp.getWriter().println("hello servlet!");  
    }  
}
```

### 3. 애플리케이션 초기화 
애플리케이션 초기화를 위해 AppInit 인터페이스를 만든다.
```java
public interface AppInit {  
    void onStartup(ServletContext servletContext);  
}
```

그리고 위 인터페이스를 구현해서 프로그래밍 방식으로 HelloServlet 서블릿을 서블릿 컨테이너에 직접 등록한다. ( WebServlet 애노테이션 사용도 가능 )
```java
public class AppInitV1Servlet implements AppInit{  
    @Override  
    public void onStartup(ServletContext servletContext) {  
        System.out.println("AppInitV1Servlet.onStartup");  
  
        // 순수 서블릿 코드 등록  
        ServletRegistration.Dynamic helloServlet =  
                servletContext.addServlet("helloServlet", new HelloServlet());  
        helloServlet.addMapping("/hello-servlet");  
    }  
}
```

<br>

## 참고
https://www.javatpoint.com/container <br>
https://www.baeldung.com/java-servlets-containers-intro <br>
https://www.baeldung.com/spring-boot-dispatcherservlet-web-xml <br>
[인프런 - 스프링 부트 핵심 원리와 활용](https://inf.run/7VBBx) 
