---
type: clarify
subject: Endpoint 검색 결과 정렬 순서 변경
date: 2026-10-21
ticket: _해당 없음_
branch: develop
requester: daejoon
---

# Endpoint 검색 결과 정렬 순서 변경 — Clarify

> Clarify · 요구사항 정의

## 📋 요약

| 항목 | 값 |
|------|----|
| 날짜 | 2026-10-21 |
| 티켓 | `_해당 없음_` |
| 브랜치 | `develop` |
| 요청자 | daejoon |
| 성공 기준 | 8건 |
| 에지 케이스 | 7건 |
| 미해소/가정 | 1건 |
| 다음 단계 | code-writer (바로 구현) |

## 🎯 목표

검색한 Endpoint 목록 정렬 기준을 기존 'HTTP Method 사전순 → 경로 길이순'에서 '매칭 정확도 → URL 경로명 → HTTP Method' 순으로 변경함. 검색어와 가장 잘 맞는 경로가 목록 위에 오도록 함.

## 📦 범위

### ✅ 포함

- `RequestMappingItemProvider`의 이름 정렬(`RequestMappingItemProvider.kt:69`) 기준 교체
- `PopupPath` 비교 로직을 새 3단계 기준으로 재작성 (매칭 정확도 계산은 검색어를 받아야 하므로 비교자 구성 방식 변경 포함)
- 다중 Method 이름(`{GET, POST} /path`)에서 정렬용 Method·경로 파싱 보정
- Goto 팝업(`GoToRequestMappingAction`)과 Search Everywhere(`RequestMappingGoToContributor`) 양쪽에 같은 순서 반영
- 정렬 기준 단위 테스트 추가

### ❌ 제외

- 검색 매칭 대상 판정(`matches()`) 결과 변경 — 어떤 항목이 검색되는지는 그대로
- 다중 Method 항목의 표시 텍스트·아이콘 변경 및 Method별 항목 분리
- 경로 변수 값 입력(`/users/1`)으로 `{id}` 매칭하는 기능
- 같은 이름(Method+경로 동일) 항목 간 순서 변경

## 🔒 제약조건

| 구분 | 내용 |
|------|------|
| 기술 | IntelliJ Platform 2024.3, Kotlin. 정렬·점수 계산 루프에서도 `indicator.checkCanceled` 유지. 매칭 점수는 IntelliJ `NameUtil`/`MinusculeMatcher.matchingDegree` 사용. 정렬 중 패턴 파싱 예외가 탐색 흐름을 중단시키지 않아야 함 |
| 비즈니스 | _해당 없음_ |
| 규칙 | 코어 계층에 프레임워크별 분기 금지(core AGENTS.md). 정렬 기준 변경 시 기존 경로 탐색 경험 회귀 확인(core AGENTS.md Do's). 검색 성능에 영향 주는 불필요한 객체 생성 지양 — 항목별 정렬 키는 1회 계산 |

## ✅ 성공 기준

> **검증 시점**: 각 항목은 관찰 가능한 결과여야 함. 구현 완료 후 한 줄씩 체크.

- [ ] **AC-1** 검색어 `users` 입력 시 경로 `/users`(완전 일치)가 `/users/{id}`(앞부분 일치)보다, `/users/{id}`가 `/admin/users`(부분 일치)보다, `/admin/users`가 `/u/s/e/r/s` 같은 퍼지 일치보다 위에 표시됨
- [ ] **AC-2** 같은 매칭 단계 안에서는 `matchingDegree` 점수가 높은 항목이 위에 표시됨
- [ ] **AC-3** 매칭 단계와 점수가 같으면 Method 뒤 문자열(경로 + params)의 대소문자 무시 사전순으로 표시되고, 대소문자 무시로 같으면 대소문자 구분 비교로 순서가 확정됨
- [ ] **AC-4** 경로까지 같으면 Method가 GET > POST > PUT > PATCH > DELETE > HEAD > OPTIONS 순으로 표시되고, 그 외 Method는 맨 뒤에 표시됨
- [ ] **AC-5** 검색어가 비었거나 `/`만 입력하면 매칭 정확도를 생략하고 경로명 → Method 순으로만 정렬됨
- [ ] **AC-6** `{GET, POST} /path` 항목은 경로 `/path`, Method `GET`(첫 Method) 기준으로 정렬되고, 단일 `GET /path`와 같으면 단일 Method 항목이 앞에 옴
- [ ] **AC-7** `GET /a`가 `GET /a params=x`보다 앞에 표시됨
- [ ] **AC-8** Goto 팝업과 Search Everywhere의 Request mapping 그룹 결과 순서가 같음

## ⚠️ 에지 케이스

| 상황 | 처리 방식 |
|------|----------|
| 검색어에 중복 중괄호(`{{id}}`)가 포함됨 | 기존 `collapseRepeatedCurlyBrackets` 정규화를 적용한 패턴으로 매칭 단계·점수 계산 |
| 검색어 선행 `/` 유무·대소문자가 경로와 다름 (`users` vs `/Users`) | 양쪽 모두 소문자화하고 선행 `/`를 제거한 뒤 완전/앞부분/부분 일치 단계 판정 |
| 검색어가 비었거나 `/`만 입력 | 모든 항목을 같은 정확도로 보고 경로명 → Method 순 정렬 |
| 다중 Method 이름 `{GET, POST} /path` | `}`까지를 Method 토큰으로 파싱해 경로 `/path` 추출, 정렬 Method는 첫 값 `GET`. 단일 Method와 동률이면 단일이 앞 |
| 관례 목록에 없는 Method 값 | Method 순서 맨 뒤, 그 안에서는 문자열 사전순 |
| 이름 형식이 예상과 달라 파싱 실패(공백 없음 등) | 예외를 전파하지 않고 전체 문자열을 경로로 간주해 정렬 계속 |
| matchingDegree 계산 중 matcher 생성 실패(특수문자 패턴 등) | 예외 없이 점수 0·퍼지 단계로 처리해 정렬 계속 |

## 📝 합의된 결정 사항

> AskUserQuestion 라운드에서 사용자가 명시적으로 선택한 옵션을 기록. `(개발 판단)` 표기 항목은 사용자 선택이 아닌 코드 관례 기반 결정. 이후 spec/plan에서 같은 질문을 재차 묻지 않기 위함.

- **매칭 정확도 판정 기준** → 단계 + 점수 — 완전 일치 > 앞부분 일치 > 연속 부분 문자열 > 퍼지, 같은 단계 안에서는 IntelliJ matchingDegree 높은 순
- **URL 경로명 비교 방식** → 대소문자 무시 사전순, 같으면 대소문자 구분 비교로 확정. 기존 경로 길이 기준은 제거
- **HTTP Method 순서** → 관례 순서 GET > POST > PUT > PATCH > DELETE > HEAD > OPTIONS, 그 외 맨 뒤
- **다중 Method(`{GET, POST}`) 처리 범위** → 정렬 파싱만 보정. 표시·아이콘은 현재 그대로
- **매칭 단계 판정 시 비교 정규화** → 대소문자·선행 `/` 무시, 중복 중괄호는 기존처럼 단일 중괄호로 정규화
- **빈 검색어·`/` 입력 시 정렬** → 매칭 정확도 생략, 경로명 → Method 순
- **Spring params 처리** → Method 뒤 문자열 전체(경로 + params)로 비교·단계 판정
- **다중 Method 항목의 Method 정렬 위치** → 첫 Method 기준, 단일 Method와 동률이면 단일이 앞
- **Search Everywhere 반영 방식 (개발 판단)** → provider 정렬만 변경 — 근거: 2024.3 `AbstractGotoSEContributor.getElementPriority`가 상수 50을 반환해 provider 전달 순서가 유지됨(바이트코드 확인). 실제 UI 순서는 runIde 수동 검증으로 확인
- **같은 이름 항목 간 순서 (개발 판단)** → 기존 수집 순서 유지 — 근거: `processByNames`가 이름 단위로 요소를 내보내며 이번 범위 밖
- **구현 배치 (개발 판단)** → 정렬 키 계산은 `model/PopupPath` 및 코어 provider에 두고 프레임워크 분기 없음 — 근거: core AGENTS.md

## ❓ 미해소 항목 / 가정

> 질문으로 확정하지 못해 잠정 가정으로 진행하는 항목. 후속 단계(spec/plan/task/code-writer) 착수 전 검증 필요.

| 항목 | 상태 / 검증 |
|------|------------|
| Search Everywhere UI가 같은 가중치 항목을 provider 전달 순서대로 표시한다는 전제 | 바이트코드상 가중치 동일은 확인됨, UI 리스트 모델의 동률 처리 순서는 미확인 — runIde 수동 검증에서 확인 |

## 🔗 다음 단계

변경이 `RequestMappingItemProvider`·`PopupPath` 중심의 단일 논리 변경이라 code-writer로 바로 착수 권장 (경로 A)

<!--
원칙:
- 5개 영역(목표·범위·제약·성공기준·에지) 구조 고정. 비어 있는 항목은 _해당 없음_ 표기.
- 성공 기준 체크박스는 `[ ]` 정적 상태. 실제 진행도 체크는 task 문서에서.
- 결정 사항 섹션은 AskUserQuestion 답변을 누적해 spec/plan/task로 컨텍스트 이어주기 위한 기록.
- 미해소 항목/가정 섹션은 확답 불가해 `가정`으로 진행한 항목을 가시화해 후속 단계에서 검증받기 위한 기록. 비면 _해당 없음_.
- 인접 nav: 빌더는 다음 문서 링크를 만들지 않음(후속 파일명 타임스탬프를 미리 알 수 없음). docs-writer가 첫 후속 문서 생성 시 배너 줄 아래에 `> 관련 문서: [... →](...)` 한 줄을 추가. 빌더 재생성(--force) 시 이 줄은 이월됨.
- frontmatter는 docs-writer가 메타데이터 추출에 사용.
-->
