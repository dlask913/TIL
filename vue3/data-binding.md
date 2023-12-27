# (vue3) 데이터 바인딩

> 데이터 소스와 UI 요소를 연결하는 방식으로, 데이터와 UI 의 동기화를 달성하는 것을 목적으로 한다.

<br>

## 단방향 데이터 바인딩
데이터가 UI로만 흐르기 때문에 데이터 모델의 변경 사항은 UI 에 반영되지만, UI 에서의 변경 사항은 데이터 모델에 반영되지 않는다.

1. Mustache 문법 ( ```{{ }}```)
- HTML 템플릿 내에서 중괄호를 사용하여 데이터 표시
- 예시
```javascript
<template>
	<div class="name">
	  {{ name }} // ★
	</div>
</template>

export default {
  setup(){
    const name = 'limnj';
    return{
      name,
    };
  }
}
</script>
```


2. ```v-bind``` 지시자
- HTML 요소의 속성을 Vue 인스턴스의 데이터 속성에 바인딩.
- v-vind 을 생략하고 ':' 만 써도 가능
- 예시
```javascript
<input :type="type" v-bind:value="name">
```

<br>

## 양방향 데이터 바인딩 
데이터와 UI 요소 간 양방향 연결. UI 에서의 변경 사항이 데이터 모델에 반영되고 데이터 모델의 변경 사항도 UI 에 자동으로 반영.

1. v-model 
-  폼 입력 요소 ( ```input```, ```textarea```, ```select``` 등 ) 에 적용되어 해당 요소의 값과 Vue 인스턴스의 데이터 속성을 연결
- v-model 사용하지 않았을 때 
```javascript
<template>
  <input
    type="text"
    :value="name"
    @input="updateName"
  >
</template>

<script>
import { ref } from 'vue';
export default {
  setup(){
    const name = ref("코더");
    const updateName = (e) => {
        name.value = e.target.value;
    };
    return{
      name,
      updateName,
    };
  }
}
</script>
```

- v-model 사용했을 때 
```javascript
<template>
  <input
    type="text"
    v-model="name"
  >
</template>

<script>
import { ref } from 'vue';
export default {
  setup(){
    const name = ref("코더");
    return{
      name,
    };
  }
}
</script>
```

<br>

## 참고 
[인프런 - 프로젝트로 배우는 Vue.js 3](https://inf.run/XZ5f) 