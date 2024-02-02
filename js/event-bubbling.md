## (js+vue3) 이벤트 버블링(Event Bubbling)
> DOM 이벤트가 발생할 때 해당 이벤트가 발생한 가장 하위의 요소부터 상위 요소들로 전파되는 방식

웹 페이지에 버튼이 있고 이 버튼이 어떤 div 요소 내부에 위치한다고 가정하면 이 버튼에 클릭 이벤트가 발생했을 때 이벤트 버블링에 의해 해당 버튼에서 이벤트가 처리된 후 그 다음에는 상위 요소인 div, 그리고 계속해서 상위 요소로 이벤트가 전파된다.

아래 코드를 예를 들어보자. 
특정 요소를 삭제하고 싶어 Delete 버튼을 클릭하게 되면 우리가 기대하는 이벤트는 오로지 삭제이다. 그런데 이 때 페이지가 /todos/{todoId} 로 이동하는 이벤트까지 수행하게 된다. 

```javascript
<template>
..
		<div
        class="card-body p-2 d-flex align-items-center"
        @click="moveToPage(todo.id)" // 2. moveToPage
      >
        // ..생략
        </div>
        <div>
          <button
            class="btn btn-danger btn-sm"
            @click="$emit('deleteTodo',index)" // 1. deleteTodo
          >
            Delete</button>
        </div>
      </div>
</template>

<script>
import { useRouter } from 'vue-router';
export default {
    // ..
    setup(props, { emit }) {
      const router = useRouter();
      const deleteTodo = (index) => {
          emit('delete-todo', index);
      };
      const moveToPage = (todoId) => {
        router.push('/todos/' + todoId);
      };
      return {
          deleteTodo,
          moveToPage,
      }
    }
}
</script>
```
<br>

## 해결 방안 
이벤트가 상위 요소로 전달하는 것을 막는 방법은 매우 간단한데 javascript 와 vue3의 방법이 다르다. javascript 에서는 ```event.stopPropagation()'``` 으로 vue3에서는  ```.stop``` 을 사용할 수 있다.
- javascript
```html
<body onclick="alert('Delete!')">
  <button onclick="event.stopPropagation()">Delete</button> <!-- ★ 이벤트 버블링 해결 -->
</body>
```
- vue3
```html
		<div>
          <button
            class="btn btn-danger btn-sm"
            @click.stop="$emit('deleteTodo',index)"
          > <!-- ★ 이벤트 버블링 해결 -->
            Delete</button>
        </div>
```

<br>

## 이벤트 버블링의 필요성
이벤트 버블링으로 인해 원하지 않게 상위 요소의 이벤트들이 모두 실행되는 것이 어떤 이점이 있을까해서 필요성을 찾아보았다.

가장 공통적이면서도 큰 이유는 <b>이벤트 핸들러의 중앙 집중화</b>였다.
이벤트 버블링을 통해 개발자는 여러 자식 요소에 각각 이벤트 핸들러를 추가하는 대신, 공통의 상위 요소에 단 하나의 이벤트 핸들러를 설정하여 모든 이벤트를 처리할 수 있게 된다. 이를 이벤트 위임(Event Delegation)이라 하고 코드의 양을 줄일 수 있으며 유지 보수를 용이하게 한다.

<br>

## 참고
https://javascript.info/bubbling-and-capturing <br />
[인프런 - 프로젝트로 배우는 Vue.js 3](https://inf.run/XZ5f) 
