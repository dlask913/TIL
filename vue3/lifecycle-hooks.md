# (vue3) 라이프사이클 훅 ( Lifecycle Hooks )

> 컴포넌트 라이프 사이클은 컴포넌트의 생성부터 소멸까지 다양한 단계를 거치며 각 단계에서 특정한 작업을 수행할 수 있는 기회 제공 <br>( Composition API )

<br>

## 라이프사이클 다이어그램 ( [공식문서](https://vuejs.org/guide/essentials/lifecycle) )
<div align="center">
	<img src="https://vuejs.org/assets/lifecycle.DLmSwRQE.png" width="500" height="750">
</div>
<br>

## 주요 라이프사이클 훅

1. **`setup()`**: 컴포넌트의 가장 초기 단계에서 호출되며 props와 context를 인자로 받는다. `setup()` 은 Composition API에서 컴포넌트 로직을 구성하는 주요 함수로 다른 라이프 사이클 훅들이 실행되기 전에 실행된다. 이 단계에서 상태, 속성, 메서드 등을 정의할 수 있다.
    
2. **`onBeforeMount()`**: 컴포넌트가 DOM에 마운트되기 직전에 호출된다. ( 컴포넌트가 DOM에 적용되지 않은 상태 )
    
3. **`onMounted()`**: 컴포넌트가 DOM에 마운트된 직후에 호출된다. DOM 요소에 접근하거나 DOM이 완전히 준비된 상태에서 실행해야 하는 작업을 수행할 수 있다.
    
4. **`onBeforeUpdate()`**: 반응형 데이터가 변경되어 DOM이 업데이트되기 직전에 호출된다. 컴포넌트가 다시 렌더링되기 전에 필요한 사전 처리를 수행할 수 있다.
    
5. **`onUpdated()`**: 컴포넌트와 그 자식 컴포넌트가 업데이트된 후에 호출된다. 모든 데이터 변경 후 DOM이 업데이트되고 나서 실행해야 하는 후처리 작업을 수행할 수 있다.
    
6. **`onBeforeUnmount()`**: 컴포넌트가 제거되기 바로 직전에 호출된다. 이벤트 리스너 제거, 타이머 정지와 같은 정리 작업을 수행할 수 있다.
    
7. **`onUnmounted()`**: 컴포넌트가 제거된 후에 호출된다. 컴포넌트가 완전히 제거된 상태에서 필요한 정리 작업을 수행할 수 있다.

<br>

## 정리
Vue 3 의 Composition API 를 사용하여 이러한 훅들을 setup() 내에 직접 호출하여 사용할 수 있다. 이를 통해 컴포넌트가 LifeCycle 의 여러 단계에서 원하는 시점에 로직을 실행할 있게 해준다.

<br>

## 참고
[Vue3 공식문서](https://vuejs.org/api/composition-api-lifecycle.html) <br>
[인프런 - 프로젝트로 배우는 Vue.js 3](https://inf.run/XZ5f) 