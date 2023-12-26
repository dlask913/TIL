## (vue3) ref 와 reactive

> ```ref``` 와 ```reactive``` 는 데이터를 반응형으로 만들어 Vue 컴포넌트에서 자동으로 업데이트되는 뷰를 생성할 수 있게 한다.

<br>

## 0. 변경 전 코드 
- App.vue
```javascript
<template> // html
	<div class="name">
	  {{ name }}
	</div>
	<button
	  class="btn btn-primary"
	  v-on:click="updateName">
	  Click
  </button>
</template>

<script> // js
export default {
  setup(){
    let name = 'Kossie Coder1';
    const updateName = () => {
      name = 'Kossie Coder2';
    };
    
    return{
      name,
      updateName
    };
  }
}
</script>

<style> // css
.name{
  color: red;
}
</style>
```

### 예상 동작 및 결과
- button 을 클릭하면 script 영역에서 정의한 upateName() 이 실행.
- name 값이 'Kossie Coder1' 에서 'Kossie Coder2' 로 변경되어 출력.

### 실제 결과
- 아무런 변화 없음.

<br>

## 1. ref
- 기본 자료형과  object, array 를 반응형으로 만드는데 사용
- ```.value``` 속성을 통해 접근 및 수정
- 하나의 값에 대한 반응형 참조 생성
- 내부적으로 ```reactive``` 를 사용하여 값을 감싸고 템플릿에서 (.value 가 아닌) 직접 사용할 수 있도록 해준다.
- App.vue
```javascript
<script> // js
import { ref } from 'vue'; // 1
export default {
  setup(){
    const name = ref('Kossie Coder1'); // 2
    
    const updateName = () => {
      name.value = 'Kossie Coder2'; // 3
    };
    return{
      name,
      updateName
    };
  }
}
</script>
```
<br>

## 2. reactive
- 기본 자료형은 사용을 못하고 object, array 만 가능
- value 속성을 사용하지 않고 직접 접근이 가능
- 객체 내부의 모든 중첩된 속성들까지도 반응형으로 변환
```javascript
<script>
import { reactive } from 'vue'; // 1
export default {
  setup(){
    const name = reactive({ 
      id: 1
    }); // 2
    
    const updateName = () => {
      name.id = 2; // 3
    };
    return{
      name,
      updateName
    };
  }
}
</script>
```
<br>

## 참고 
[인프런 - 프로젝트로 배우는 Vue.js 3 - 섹션 1. To-Do 추가 폼 만들기 - 07. ref vs reactive](https://inf.run/XZ5f) 