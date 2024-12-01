## (vue3) 프로젝트 구성
> 개발 환경 구성, Vue3 프로젝트 구성, ESLint 및 Prettier 설정

<br>

## 개발환경 구성
- vuejs devtools 설치 ( [접속](https://chromewebstore.google.com/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd?pli=1) )
- VScode Extensions install 
  - Auto Rename Tag (formulahendry.auto-rename-tag) : 태그 수정 시 자동으로 변경
  - Vue - Official (Vue.volar) : Vue 개발을 위한 공식 확장 도구
  - Vue VSCode Snippets (sdras.vue-vscode-snippets) : 자주 사용하는 코드 스니펫 제공
  - EditorConfig for VS Code (EditorConfig.EditorConfig) : 코드 스타일 일관성 유지
  - ESLint (dbaeumer.vscode-eslint) : 코드 문법 오류 및 스타일 검사
  - Prettier - Code formatter (esbenp.prettier-vscode) : 자동 코드 포맷팅

<br>

## Vue3 프로젝트 구성
> 사전에 영문 [공식문서](https://vuejs.org/guide/quick-start.html) 확인하기

1. vue 프로젝트 생성
```shell
npm create vue@latest # 최신 버전
```
2. 옵션 선택 ( 필요에 따라 )
![image](https://github.com/user-attachments/assets/43bf8b1f-608f-41ca-b0de-4fb0c7e5d6b6)

3. 권장하는 확장 프로그램 확인
- 프로젝트 내 .vscode > extensions.json
```json
// Extensions 에서 @recommended 로 검색하여 확인 가능
{
  "recommendations": [ 
    "Vue.volar",
    "dbaeumer.vscode-eslint",
    "EditorConfig.EditorConfig",
    "esbenp.prettier-vscode"
  ]
}
```

4. 루트 디렉에서 의존성 패키지 설치
```shell
npm install
```

<br>


## ESLint, Prettier 설정
### 1. ESLint
-  .vscode > `setting.json` 에서 자동 수정 기능 활성화 
```json
  "editor.codeActionsOnSave": {
    "source.fixAll": "explicit"
  },
```
- `eslint.config.js` 설정 ( 이전에 사용하던 `.eslintrc` 는 중단 )
```js
import js from '@eslint/js' // 기본 JavaScript용 ESLint 설정
import pluginVue from 'eslint-plugin-vue' // Vue.js 전용 ESLint 플러그인
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting' // Prettier와 충돌 방지 설정
  
export default [
  {
    name: 'app/files-to-lint', // ESLint 를 적용할 파일 지정
    files: ['**/*.{js,mjs,jsx,vue}'],
    rules: {
      // 커스텀 규칙 설정
      'no-console': 'warn', // console.log 시 경고
      'vue/no-undef-components': 'error', // 정의되지 않은 Vue 컴포넌트 사용 금지
    },
  },
  
  {
    name: 'app/files-to-ignore', // ESLint 를 무시할 파일 지정
    ignores: ['**/dist/**', '**/dist-ssr/**', '**/coverage/**'],
  },
  
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  skipFormatting,
]
```
- 전체 파일에 룰 적용하기
```shell
npm run lint
```

### 2. Prettier 
- .vscode > `setting.json` 에서 코드 포맷팅 활성화
```json
  "editor.formatOnSave": true,
```
- `.prettierrc.json` 설정 ( 팀 컨벤션 룰에 따라서 )
```json
{
  "$schema": "https://json.schemastore.org/prettierrc",
  "semi": false, // 명령문 끝 세미콜론 추가 여부
  "singleQuote": true, // 작은따옴표 사용여부
  "arrowParens": "avoid", //화살표 함수에서 매개변수가 하나일 경우 괄호 생략
  "tabWidth": 2, // 탭 대신 사용할 공백 수
  "trailingComma": "all", // 마지막 요소 뒤 쉼표 추가 여부
  "printWidth": 80, // 한 줄 최대 길이
  "bracketSpacing": true, // 객체 리터럴에서 괄호 사이 공백 추가 여부
  "endOfLine": "auto" // 파일의 줄 끝 문자 방식 설정
}
```

- 전체 파일에 룰 적용하기
```shell
npm run format
```


### 3. vite.config.js 와 jsconfig.js 의 Alias 
- `vite.config.js` Alias : Vite 가 번들링 및 빌드 시 경로를 실제로 매핑 ( 런타임 시점 )
- `jsconfig.json` Alias : IDE 가 코드를 이해하고 개발 중 편리한 경로 탐색 및 코드 자동 완성 시 사용
- 같은 Alias 를 정의하면 개발 환경(IDE) 와 빌드 환경(Vite) 간 일관성 유지 가능

<br>

## 참고
[인프런 - 최신 Vue 3 완벽 가이드: 프로젝트 설정 & 스펙 총정리](https://inf.run/aB6Bz) 