# (vue3) 조건부 렌더링

> 조건에 따라 DOM 요소를 숨기거나 보여주는 기능. ( v-for, v-if, v-show )

<br>

## v-for
v-for 은 배열 뿐 아니라 객체의 속성을 반복하는 데에도 사용할 수 있다.  ```:key``` 는 Vue 에게 각 DOM 요소가 유일하게 구별되어야 함을 알리는 데 사용.
```javascript
<div
  v-for="todo in todos"
  :key="todo.id"
  class="card mt-2"
>
  <div class="card-body p-2">
	{{ todo.subject }}
  </div>
</div>
```

또한 두 번째 인자를 사용하여 배열의 인덱스나 객체의 키를 얻을 수 있다. 
```javascript
v-for="(todo,index) in todos")
```

<br>

## v-show
v-show 는 특정 조건에 따라 HTML 요소의 표시 여부를 제어하는 데 사용된다. 조건이 참(true) 일 때 요소를 보이게 하고 거짓(false) 일 때 요소를 숨긴다. 중요한 점은 CSS의 'display' 속성을 사용하여 요소를 보이거나 숨기기 때문에 요소는 항상 DOM 에 존재한다.
```javascript
<div v-show="toggle">true</div>
<div v-show="!toggle">false</div>
<button @click="onToggle">Toggle</button>
```
실행 결과 

![Pasted image 20231227105104](https://github.com/dlask913/TIL/assets/79985588/257c9d4e-1c49-4092-b0cf-4105c3249eee)

<br>

## v-if
v-if 는 주어진 조건에 따라 DOM 요소를 생성하거나 제거하는 데 사용. 이 조건은 참일 때 요소를 표시하고 거짓을 때 요소를 DOM 에서 완전히 제거
```javascript
<div v-if="toggle">true</div>
<div v-else>false</div>
<button @click="onToggle">Toggle</button>
```
실행 결과

![Pasted image 20231227105559](https://github.com/dlask913/TIL/assets/79985588/88cbdd24-938c-49bc-90a5-92c664877fec)

<br>

## v-if 와 v-show 비교 ( [공식문서](https://vuejs.org/guide/essentials/conditional.html#v-if-vs-v-show) )
> 빈번하게 요소의 표시 여부를 변경해야 하는 경우에는 `v-show` 가, 조건이 런타임 동안에 자주바뀌지 않을 것으로 예상되는 경우에는 `v-if` 가 더 적합
1. ```v-if```
- "진짜" 조건부 렌더링을 제공. 이는 조건이 변할 때 이벤트 리스너와 자식 컴포넌트가 적절하게 파괴되고 다시 생성되도록 보장.
- 지연 렌더링. 즉, 초기 렌더링 시 조건이 거짓이면 아무것도 렌더링 X. 조건이 처음으로 참이 될 때까지 조건부 블록은 렌더링되지 않는다.
- 토글 비용이 더 높다. 조건이 런타임에 변경될 가능성이 낮은 경우 사용하는 것이 좋다.
2. ```v-show```
- 훨씬 더 간단하다. 요소는 초기 조건에 관계없이 항상 렌더링되며, CSS 를 기반으로 표시 여부가 결정.
- 초기 렌더링 비용이 더 높다. 빈번하게 토글해야 하는 경우 사용하는 것이 좋다.

