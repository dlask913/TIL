# (vue3) props 와 emits

> 부모 자식 Component 간 데이터 주고받는데 사용.

<br>

## props ( [공식 문서](https://vuejs.org/guide/components/props.html) )

####  부모 Component에서 자식 Component로 데이터 전달 
1. props 선언 및 전달
- Props 정의 : 자식 컴포넌트에서 'props' 옵션을 사용하여 받고자 하는 props 를 정의한다. ( 타입, 기본값, 필수 여부 등 )
<br>ex> TodoList.vue ( 자식 )
```javascript
<script>
	export default {
	    props: {
	        todos: {
	            type: Array,
	            required: true
	        }
		}
	}
</script>
```
- props 전달 : 부모 컴포넌트에서 자식 컴포넌트를 사용할 때, HTML 속성처럼 props를 전달할 수 있다.
<br>ex> App.vue ( 부모 )
```javascript
<template>
  <div class="container">
	  ..
    <TodoList :todos="todos" /> // 데이터 전달
  </div>
</template>

<script>
	import { ref } from 'vue';
	import TodoList from './components/TodoList.vue';
	export default {
	  components: {
	    TodoList,
	   },
	  setup(){
	    const todos = ref([]);
		..
		return{
	      todos,
	    };
	  }
	}
</script>
```


2. 단방향 바인딩
- 부모 컴포넌트의 데이터를 자식 컴포넌트에서 읽을 수 있지만 직접 수정은 불가. ( 단방향 )
- 상위 요소가 업데이트될 때마다 하위 구성 요소의 모든 속성이 최신 값으로 업데이트

3. props 검증
- ```defineProps()``` 함수로 props 을 정의하여 props 의 타입, 필수 여부, 기본값 등을 검증할 수 있게 해준다

<br>

## emits ( [공식문서](https://vuejs.org/guide/components/events.html) )
#### 자식 Component에서 부모 Component로 이벤트 전달.
1. Emits 정의 및 전달
- Emits 정의 : 자식 컴포넌트에서 emits 을 사용하여 발생시킬 이벤트를 정의할 수 있다.
<br>ex> TodoList.vue ( 자식 )
```javascript
<script>
	export default {
	
		emits: ['toggle-todo', 'delete-todo'], // 선언
	
	    setup(props, { emit }) { // 구조 해제를 통한 중복 코드 제거
	        const toggleTodo = (index) => {
	            emit('toggle-todo', index);
	        };
	        const deleteTodo = (index) => {
	            emit('delete-todo', index);
	        };
	        return {
	            toggleTodo,
	            deleteTodo,
	        }
	    }
	}
</script>
```
- Emits 사용 ( 자식 ) : 자식 컴포넌트 내에서 ```$emit``` 을 사용하여 이벤트를 발생시킬 수 있다.
```javascript
		<button
            class="btn btn-danger btn-sm"
            @click="$emit('deleteTodo',index)"
        >
			Delete</button>
```
- Emits 사용 ( 부모 ) : 부모 컴포넌트에서는 자식 컴포넌트의 이벤트를 리스닝하고 적절한 메소드나 액션을 지정하여 처리할 수 있다.
```javascript
<template>
	..
    <TodoList
      :todos="todos"
      @toggle-todo="toggleTodo"
      @delete-todo="deleteTodo"
    />

</template>
```
<br>

## 참고 
[인프런 - 프로젝트로 배우는 Vue.js 3](https://inf.run/XZ5f) 