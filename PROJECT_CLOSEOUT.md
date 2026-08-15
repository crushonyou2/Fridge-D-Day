# 오늘도 신선 프로젝트 종료 인계

## 최종 상태

**v1.0.2 Released (원스토어) / 활성 개발 없음**

- v1.0을 원스토어에 출시했다.
- 출시 후 v1.1 후보에 독립 한국 식품 라벨 55장 QA와 릴리스 게이트를 적용했고, 잔여 오답과 표본 공백을 근거로 **배포를 보류(No-Go)**했다. 이는 프로젝트 전체의 No-Go나 “정확도가 낮아 포기”한 결과가 아니라, 측정과 회귀 기준으로 검증되지 않은 업데이트를 차단한 릴리스 판단이다.
- **보류 사유를 정확도가 아닌 제품 구조로 해결하고 2026-08-12에 v1.0.2를 원스토어에 배포했다.** 공개 배포 이력은 v1.0 → v1.0.2 2건이다. 상세는 아래 「6. 보류 이후 — v1.0.2」.
- 현재 추가 OCR 정확도 개선, 실사진 수집, 사용자 모집·베타는 하지 않는다. 보존에 필요한 최소 유지보수만 한다.

> **2026-08-15 개정 이력**: 이 문서는 2026-07-22 종료 시점 판정(`v1.0 Released / v1.1 QA No-Go / Archived`)을 담고 있었다.
> 2026-08-12 v1.0.2 배포로 그 판정이 더 이상 현재 상태가 아니므로 최종 상태와 검증 사실을 갱신했다.
> **3·4절의 측정 기록과 5절의 릴리스 판단은 그 시점 기록으로 보존한다** — 소급 수정하지 않는다.

## 문제 → 측정 → 개선 → 회귀 자동화 → 릴리스 판단

### 1. 문제

v1.0의 촬영 입력은 수동 입력 부담을 줄이지만 OCR 결과가 라벨 형식, 조명, 각도, 재질과 평가 시점에 따라 달라진다. 샘플 수나 조건을 고정하지 않은 체감 평가로는 v1.1 배포 근거를 만들 수 없었다.

### 2. 측정

서로 다른 한국 식품 라벨 사진 55장(`existing_20=20`, `new_30_plus=35`)을 고정했다. 같은 사진·정답·환경 분류를 사용하고 평가일만 정답 기준 D-30과 D-180으로 고정해 민감도를 분리했다.

| 지표 | 종료 기준선 |
|---|---:|
| D-30 정확 일치 | 40/55 (72.73%) |
| D-180 정확 일치 | 39/55 (70.91%) |
| 정답 후보 재현 | 43/55 (78.18%) |
| 최대 실패 유형 | `wrong_date` 14 → 11 |
| D-30 오답 | 15/55 |
| D-180 오답 | 16/55 |

정확 일치는 최종 선택 날짜가 사람이 확인한 정답과 같은 비율이고, 정답 후보 재현은 정답 날짜가 OCR·파서 후보 집합에 포함된 비율이다. 이 값들은 고정된 55장과 평가일 시나리오에 종속된 회귀 기준선이다. 실사용 전체 정확도, 사용자 성공률 또는 사용자 규모로 일반화하지 않는다.

### 3. 개선

수정 전 가장 큰 실패 유형인 `wrong_date` 하나만 대상으로 삼았다. OCR 숫자 조각을 합성한 저신뢰 후보와 실제 연속 6/8자리 날짜 후보의 신뢰도를 분리해, D-30 정확 일치를 37/55에서 40/55로 높이고 `wrong_date`를 14건에서 11건으로 줄였다. 첫 수정안은 기존 게이트를 통과하지 못해 기각했으며 최종 파서만 기준선으로 고정했다.

### 4. 회귀 자동화

- D-30과 D-180을 한 명령에서 연속 실행해 한쪽만 통과한 변경을 릴리스 성공으로 인정하지 않는다.
- 전체 정확 일치와 정답 후보 재현뿐 아니라 샘플별 exact·정답 후보 퇴행을 차단한다.
- 사진 SHA-256, 정답, 환경 분류, 평가일과 평가 시나리오가 같은 입력인지 검사한다.
- 기준선과 결과 CSV의 동일 경로 사용, 기존 결과명 재사용, 회귀 실행의 덮어쓰기를 instrumentation 전에 거부한다.
- 검증 시 unit 19개, `lintDebug`, Debug/Release 빌드, AndroidTest 빌드와 instrumentation을 통과했다. _(이 19개는 v1.1 QA 시점 값이다. v1.0.2 리팩터링 이후 현재는 unit 29개·instrumentation 15개다 — 6절 참조)_ 종료 변경의 `INTERNET` 권한 제거 후에도 동일 55장 D-30/D-180 회귀를 다시 실행해 각각 8분 21초·8분 3초에 기준선 대비 전체·샘플별 변화 0으로 통과했다.

### 5. 릴리스 판단

자동화 통과는 배포 충분조건이 아니다. D-30 오답 15장, D-180 오답 16장이 남았고 다음 표본 공백이 있어 v1.1을 **Release No-Go**로 판정했다.

- 한글 `YYYY년 MM월 DD일` 실사진 없음
- 연속 `YYYYMMDD`·`YYMMDD` 실사진 없음
- 실제 촬영일 기반 표본 없음

따라서 v1.0의 출시 사실과 v1.1의 미출시 판단을 분리한다. v1.1 APK/AAB를 스토어에 제출하지 않았다.

_(2026-07-22 시점 기록. 이후 v1.0.2 배포로 원스토어 등록 버전이 갱신됐다 — 아래 6절.)_

### 6. 보류 이후 — v1.0.2 (2026-08-12 배포)

v1.1을 보류한 사유는 **D-30 기준 오답 15/55**였다. 정확도를 더 끌어올리는 대신 **지켜야 할 대상을 다시 정의했다** — 인식 정확도가 아니라 "잘못 읽힌 날짜가 저장되지 않는 것"이다.

- **저장 경로 차단**: 인식한 날짜를 사용자가 확인해야 저장되도록 바꿨다(`OcrDateConfirmationDialog`). 날짜가 확정되지 않으면 저장이 차단된다. 이로써 OCR이 틀려도 그 값이 그대로 기록되는 경우가 사라진다.
- **Room 파괴적 마이그레이션 제거**: `exportSchema = true`로 스키마 이력을 커밋하고, 업데이트 시 기존 데이터가 보존되는지 `DatabaseUpdateTest`가 버전 2 DB에 행을 넣고 실제 Room 빌더로 다시 열어 단언한다.
- **백업 포맷 v2**: v1 하위호환을 유지하고 직렬화·역직렬화를 분리해 테스트 가능하게 리팩터링했다.
- **API 36 대응**: AGP 8.9.1 · Gradle 8.11.1 · CameraX 1.5.3. `targetSdk = 36`, `versionCode = 4`, `versionName = 1.0.2`.
- 이 리팩터링으로 **unit 19 → 29개, instrumentation 11 → 15개**가 됐다.

검증:

- instrumentation 15건 전부 통과(에뮬레이터 `google_apis_playstore` API 36).
- **원스토어가 재서명한 실제 배포 APK를 직접 검사해** `INTERNET` 권한 부재와 비공개 QA 자료 미포함을 확인했다. 5절 시점의 "배포 바이너리로 일반화하지 않는다"는 한계는 이 검사로 해소됐다.
- CI 그린. 기본 브랜치에 ruleset을 걸어 PR 없이는 머지되지 않고 `unit-test-and-lint` 통과가 머지 조건이다.

한계:

- v1.0.2 검증은 에뮬레이터에서만 했고 **실기기 검증은 하지 않았다.**
- v1.0.2는 **OCR 인식 정확도를 개선한 버전이 아니다.** 55장 기준선 수치는 v1.1 QA 시점 값 그대로이며, 달라진 것은 잘못된 값이 저장되는 경로다.

## 재현 명령

요구 환경은 JDK 17, Android SDK 35, 정확히 한 대의 Android 기기 또는 에뮬레이터다. 사진과 정답 원본이 필요한 benchmark/instrumentation은 공개 clone만으로 재현할 수 없으며 승인된 로컬 `qa-private/` 자료가 있어야 한다.

일반 품질 게이트:

```powershell
.\scripts\android-quality.ps1 `
  -Tasks @("testDebugUnitTest","lintDebug","assembleDebug","assembleRelease","assembleDebugAndroidTest")
```

고정 입력만 검증:

```powershell
.\scripts\run-ocr-benchmark.ps1 `
  -DatasetDir qa-private/korean-labels-55 `
  -ReleaseBaseline `
  -ValidateOnly
```

D-30/D-180 이중 릴리스 회귀:

```powershell
.\scripts\run-ocr-release-regression.ps1 `
  -RunNamePrefix <새-결과-접두사> `
  -DatasetDir qa-private/korean-labels-55 `
  -D30BaselineCsv qa-private/results/korean-labels-55-v2-d30.csv `
  -D180BaselineCsv qa-private/results/korean-labels-55-v2-d180.csv `
  -TargetFailureType wrong_date
```

종료 시점의 세부 실행 환경과 러너 제한은 [`docs/qa/OCR_BENCHMARK.md`](docs/qa/OCR_BENCHMARK.md)에 고정한다.

## 원본 결과 digest

아래 SHA-256은 비공개 원시 CSV가 바뀌지 않았음을 검증하기 위한 공개 증거다. CSV 자체와 sample별 정답은 공개하지 않는다.

| 로컬 파일 | SHA-256 |
|---|---|
| `qa-private/results/korean-labels-55-baseline.csv` | `f3da66618c7703cd9d12308d357800fdf7fe5e0e38fb412ca5a26a9e4d087894` |
| `qa-private/results/korean-labels-55-after.csv` (기각안) | `edceb8feddac706a79e1551dffc9ba597e77d9adf3ae181898fdc10d308a7af7` |
| `qa-private/results/korean-labels-55-after-final.csv` | `7f0e3babf757193eed1801236e8ed60ec29587abfd77c0376eb194e8a78cc210` |
| `qa-private/results/korean-labels-55-v2-d30.csv` | `e7d2639b80fecc171a70691107b02c719b620fee1bc3ff52383b7254cb3fb9ff` |
| `qa-private/results/korean-labels-55-v2-d180.csv` | `d896caa5702aaa559f1ba377131b1ebeca2e29fb86f85419931dfdc6253ff2f9` |
| `qa-private/results/korean-labels-55-v3-guard-regression-d30.csv` | `e7d2639b80fecc171a70691107b02c719b620fee1bc3ff52383b7254cb3fb9ff` |
| `qa-private/results/korean-labels-55-v3-guard-regression-d180.csv` | `d896caa5702aaa559f1ba377131b1ebeca2e29fb86f85419931dfdc6253ff2f9` |

종료 점검에서 위 로컬 파일 digest가 문서 값과 일치함을 다시 확인한다.

## 공개·비공개 경계

- 실제 라벨 사진, 원본 HEIC/JPEG, 정답 manifest, sample별 원시 결과와 변환 매핑은 `qa-private/`에만 보관한다.
- `qa-private/` 전체와 `local.properties`는 `.gitignore` 대상이며 추적하거나 이동·삭제하지 않는다.
- 공개 `docs/qa/manifest.example.csv`와 `docs/qa/ocr-benchmark.csv`는 열 구조 예시이며 실측 데이터가 아니다.
- Release APK는 `androidTest` 소스·자산을 패키징하지 않으며, 종료 점검에서 `qa-private/`·라벨·manifest·`local.properties` 관련 항목이 없음을 확인했다.
- 앱은 ML Kit 전이 manifest가 추가하는 `INTERNET` 권한을 명시적으로 제거하며, 앱 수준의 광고·분석·추적 SDK를 추가하지 않았다. QA 과정에서도 스토어 제출을 수행하지 않았다.

## 알려진 한계

- D-30과 D-180은 과거 사진에 부여한 고정 평가 시나리오이며 실제 촬영일이 아니다.
- 55장에 한글 날짜와 연속 숫자 날짜 실사진이 없어 해당 형식 성능을 알 수 없다.
- 제조일자와 소비기한이 함께 인식될 때 평가일과 가까운 제조일자를 선택할 수 있다.
- `clear_dot_matrix`, `embossed_low_contrast`, `dark`, 연도 없는 `MM.DD`의 작은 그룹에서 실패가 집중됐지만 그룹 크기가 작아 전체 사용자 환경으로 일반화할 수 없다.
- 사용자 베타를 실행하지 않았으므로 사용자 수, 재사용 의향, 실제 사용 성공률을 주장하지 않는다.
- `INTERNET` 권한 부재는 종료 변경 후 현재 저장소에서 재빌드한 Release APK로 검증했다. 기존 원스토어 배포 바이너리는 이 작업에서 내려받아 별도 검사하거나 업데이트하지 않았으므로, 그 바이너리의 권한 목록으로 일반화하지 않는다.

이 한계는 후속 개발 목록이 아니라 종료 시점의 해석 경계다.

## 포트폴리오 인계용 검증 사실

다음 항목은 저장소 코드·문서·로컬 digest와 검증 로그로 뒷받침된다. 문장으로 과장하지 않고 사실 단위로만 재사용한다.

- Kotlin·Jetpack Compose 기반 오프라인 우선 Android 앱을 단독 개발하고 원스토어에 배포했다. 공개 배포 이력 2건(v1.0 → v1.0.2, 2026-08-12).
- Release APK에서 `INTERNET` 권한을 제거했고 앱 수준의 광고·분석·추적 SDK를 추가하지 않았으며 식품 기록은 Room/DataStore에 로컬 저장한다.
- 출시 후 서로 다른 한국 식품 라벨 55장으로 D-30/D-180 조건부 OCR 기준선을 측정했다.
- D-30 정확 일치 37/55에서 40/55로 개선했고 최대 실패 유형 `wrong_date`를 14건에서 11건으로 줄였다.
- D-30 40/55, D-180 39/55, 정답 후보 재현 43/55를 고정했으며 실사용 정확도로 일반화하지 않았다.
- 사진·정답·분류·평가 시나리오 동일성을 검사하고 샘플별 exact·정답 후보 회귀를 막는 이중 릴리스 게이트를 자동화했다.
- 기준선·결과 CSV 덮어쓰기 보호와 비공개 QA 자료의 Git·Release APK 제외를 검증했다.
- unit 29개, instrumentation 15개, lint, Debug/Release build를 통과했다. (v1.1 QA 시점에는 unit 19개·instrumentation 11개였다.)
- 남은 오답과 표본 공백을 근거로 v1.1 배포를 No-Go로 판정해 기준 미달 후보의 스토어 업데이트를 차단했다.
- 그 보류 사유를 정확도가 아닌 저장 경로 차단(인식 결과 사용자 확인 후 저장)으로 해결하고, Room 데이터 보존과 배포 APK 권한을 검증한 뒤 v1.0.2를 배포했다.

자소서 문장, 사용자 규모, 실사용 전체 정확도, 식품 폐기 감소량은 이 문서에서 만들거나 추정하지 않는다.
