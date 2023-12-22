## (js) 변수 선언

### 1. var 
- 전역적  및 지역적으로 사용 가능
- 재선언과 재할당(수정) 가능 
```javascript
// 값 수정 가능, 에러 X
var greeter = "hey hi";
var greeter = "say Hello instead";
```
- 변수 호이스팅. ( 선언된 위치와 상관없이 스코프 최상단으로 끌어올려짐 )
```javascript
// 코드
console.log (greeter);
var greeter = "say hello"

// 실제 동작
var greeter;
console.log(greeter); // greeter is undefined
greeter = "say hello"
```


## 2. let
- ES6 부터 도입
- 지역적 사용 ( 블록 범위 )
```javascript
 let greeting = "say Hi";
    if (true) {
        let greeting = "say Hello instead";
        console.log(greeting); // "say Hello instead"
    }
    console.log(greeting); // "say Hi"
```
- 재선언 불가, 재할당(수정) 가능
```javascript
    let greeting = "say Hi";
    greeting = "say Hello instead";
```
- 호이스팅되지만 undefined 로 초기화되지 않으며, 선언 이전에 let 변수를 사용할 시 Reference Error (참조 오류) 발생


## 3. const
-  ES6 부터 도입
- 지역적 사용 ( 블록 범위 )
- 재선언 불가, 재할당(수정) 불가 -> 객체의 속성은 수정 가능
```javascript
    const greeting = {
        message: "say Hi",
        times: 4
    }

	// 재할당 불가
	greeting = {
        words: "Hello",
        number: "five"
    } // error:  Assignment to constant variable.

	// 객체의 속성은 가능
	greeting.message = "say Hello instead";
```

## 참고
	https://www.freecodecamp.org/korean/news/var-let-constyi-caijeomeun/  

