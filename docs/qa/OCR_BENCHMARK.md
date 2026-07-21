# OCR 품질 벤치마크

오늘도 신선의 실제 라벨 사진을 앱과 동일한 ML Kit 한국어 OCR·날짜 파서로 반복 평가하는 QA 기준이다. 측정하지 않은 값은 성과나 배포 근거로 쓰지 않는다.

## 현재 상태 (2026-07-14)

- 실제 포장 사진 및 정답 데이터: 공개 데이터셋에서 육안 검수한 61장 + 한국 라벨 보완 세트 20장
- 수정 전: 정확 일치 3/61(4.92%), 후보 탐지 45/61(73.77%)
- 수정 후: 정확 일치 23/61(37.70%), 후보 탐지 48/61(78.69%)
- 최대 실패 유형: `wrong_date` 42건 → 25건
- 수정 범위: 구분자가 있는 `DD/MM/YYYY`, `DD/MM/YY`의 일 우선 해석 1건
- 동일 표본 회귀: sample_id 61개 일치, 정확 일치 +20, 탐지 +3, 기존 정답 회귀 0
- Android 검증: 단위 테스트 17개, `lintDebug`, `assembleDebug`, `assembleDebugAndroidTest`, 61장 instrumentation 벤치마크 통과
- 한국 보완 세트: 정확 일치 13/20(65%), 후보 탐지 18/20(90%). 50장 미만이므로 릴리스 기준선이 아닌 보완 관측값이다.
- v1.1 배포 가능 여부: **품질 기준 보류(No-Go)**. 빌드·회귀 게이트는 통과했지만 공개 기준선 정확 일치율이 37.70%이고 한국 보완 세트도 65%에 머문다.

Google Play·원스토어 최종 제출은 수행하지 않았다. 공개 표본은 영어권 포장 중심이므로 한국 보완 세트 결과를 별도로 병기하며 두 결과를 합산해 하나의 정확도처럼 발표하지 않는다.

## 독립 한국 50장 확장 준비 상태 (2026-07-21)

- 코딩 정본: `C:\Users\joji\Documents\취준자료\project-repos\Fridge-D-Day`
- 작업 브랜치/기준 커밋: `codex/fridge-ocr-qa-baseline` / `e8917ae`
- 기존 한국 사진: manifest 20행, 파일 20개, SHA-256 고유값 20개, 동일 파일 중복 0건. 육안으로도 서로 다른 제품·포장임을 확인했다.
- 새 한국 사진: 0장. 기존 20장과 독립성을 비교할 신규 입력이 아직 없다.
- 기존 20장 분류 보완: 조명·각도에 더해 재질 9종, 인쇄 품질 6종, 고유 `independence_key`, `cohort=existing_20`을 로컬 manifest에 기록했다.
- 누락 표기: 한글 `YYYY년 MM월 DD일`과 연속 숫자 `YYYYMMDD`·`YYMMDD` 표본이 없다.
- 대표성 주의: `korean_012`는 설명 오버레이가 있는 이미지이고 `korean_017`은 여러 제품이 함께 보이는 가격표형 사진이다. 두 장은 원본 촬영 통제가 약한 한계로 최종 결과에 명시한다.

따라서 이 상태에서는 한국 50장 정확도나 v1.1 배포 수치를 산출하지 않는다. 서로 다른 실제 제품 30장 이상과 사람이 확인한 날짜 정답이 들어온 뒤 아래 `-ReleaseBaseline` 검증을 통과해야 새 기준선을 실행한다.

러너는 새 기준선에서 다음을 추가로 보장한다.

- 파일 SHA-256과 `independence_key` 중복을 모두 거부한다.
- 50장 이상, `existing_20=20`·`new_30_plus>=30`, 실제 재질, 인쇄 품질 분류를 릴리스 입력 조건으로 검사한다.
- 결과 CSV에 이미지 SHA-256을 남기고 수정 전후 사진·정답·분류·평가일이 모두 같은지 비교한다.
- 실패를 `ocr_error`, `no_text`, `no_date_candidate`, `candidate_out_of_window`, `wrong_date`로 분리하고 상위 3개를 출력한다.
- 후보 탐지는 파서가 만든 날짜 후보가 한 개 이상인 경우로 계산한다. 과거 결과의 `predicted_date != null` 기준과 혼합 비교하지 않는다.

## 측정 데이터셋

- 출처: Mendeley Data, [Food Packaging OCR Dataset v2](https://data.mendeley.com/datasets/3cpx2fmn3r/2), DOI `10.17632/3cpx2fmn3r.2`
- 라이선스: CC BY 4.0
- 원본 ZIP: 2,561,540,317 bytes
- 원본 SHA-256: `74b288e6da78c29b31812e3e23ff3fe19b634a3b0e659dea8b86b8de0a04e5fd`
- 선정 방식: 공개 detection split의 실제 포장 사진 중 날짜 표기와 정답을 육안으로 확인할 수 있는 61장
- 표본 분포: 조명 `normal` 55, `dark` 4, `bright` 2 / 방향 `upright` 58, `skewed` 2, `upside_down` 1
- 재질: 원 데이터가 재질 정답을 제공하지 않아 추측하지 않고 전부 `unspecified_public_dataset`으로 기록
- 평가 날짜: 과거 라벨을 현재 날짜 필터로 버리지 않도록 각 정답 날짜 30일 전으로 고정. 실제 앱의 현재 날짜 동작을 바꾼 것이 아니라 과거 표본 재현을 위한 테스트 입력이다.

원본 ZIP, 추출 파일, 선정 사진, manifest, sample별 원시 결과는 모두 `qa-private/`에만 있으며 Git에 커밋하지 않는다.

## 실제 측정 결과

| 지표 | 수정 전 | 수정 후 | 변화 |
|---|---:|---:|---:|
| 정확 일치율 | 3/61 (4.92%) | 23/61 (37.70%) | +20장, +32.78%p |
| 후보 탐지율 | 45/61 (73.77%) | 48/61 (78.69%) | +3장, +4.92%p |
| `wrong_date` | 42 | 25 | -17 |
| `no_candidate` | 16 | 13 | -3 |

환경별 실패율은 다음과 같다. 작은 환경 그룹은 일반화하지 않고 관측값으로만 기록한다.

| 환경 | 표본 | 수정 전 실패율 | 수정 후 실패율 |
|---|---:|---:|---:|
| 조명-normal | 55 | 52/55 (94.55%) | 34/55 (61.82%) |
| 조명-dark | 4 | 4/4 (100%) | 3/4 (75%) |
| 조명-bright | 2 | 2/2 (100%) | 1/2 (50%) |
| 방향-upright | 58 | 55/58 (94.83%) | 35/58 (60.34%) |
| 방향-skewed | 2 | 2/2 (100%) | 2/2 (100%) |
| 방향-upside_down | 1 | 1/1 (100%) | 1/1 (100%) |
| 재질-unspecified | 61 | 58/61 (95.08%) | 38/61 (62.30%) |

가장 큰 실패 유형 `wrong_date` 안에서 일 우선 숫자 날짜가 연 우선으로 오해되는 원인 하나만 수정했다. 개선된 20장은 `dd/mm/yyyy` 15장, `dd/mm/yy` 3장, `dd.mm.yy` 2장이며 기존 정확 일치 3장은 모두 유지됐다. 월 이름 날짜, 기울어진 사진, OCR 자체 미탐지는 이번 변경 범위에서 제외했다.

### 한국 라벨 보완 세트 20장

사용자가 제공한 JPG 18장과 WebP 2장을 육안 검수했다. 날짜 종류는 포장에 `유통기한`이라고 명시된 경우 `유통기한`, `소비기한`이라고 명시되거나 사용자가 해당 종류로 확인한 경우 `소비기한`으로 보존했다. `korean_019`는 이미지에 `2024 12 27`이 명확해 제공된 표기 원문의 `2024 12 17`만 정정했다. 사진·정답 원문·원시 결과는 `qa-private/korean-labels`와 `qa-private/results`에만 있다.

| 지표 | 한국 20장 결과 |
|---|---:|
| 정확 일치율 | 13/20 (65%) |
| 후보 탐지율 | 18/20 (90%) |
| `wrong_date` | 5 |
| `no_candidate` | 2 |

| 환경 | 표본 | 실패율 |
|---|---:|---:|
| 조명-normal | 14 | 3/14 (21.43%) |
| 조명-glare | 4 | 3/4 (75%) |
| 조명-bright | 1 | 0/1 (0%) |
| 조명-dark | 1 | 1/1 (100%) |
| 방향-upright | 10 | 4/10 (40%) |
| 방향-skewed | 8 | 3/8 (37.50%) |
| 방향-rotated_90 | 2 | 0/2 (0%) |

실패 sample_id는 `korean_001`, `008`, `009`, `010`, `011`, `012`, `020`이다. 이 중 연도 없는 `MM.DD`는 2/2 실패했고 반사 조명은 3/4 실패했다. 그룹이 매우 작으므로 원인 확정이나 일반화에는 사용하지 않는다. 이번 작업은 공개 61장에서 가장 큰 실패 원인 하나만 수정한다는 범위를 이미 완료했으므로 한국 세트 결과를 근거로 추가 파서 수정은 하지 않았다.

## 개인정보와 Git 경계

실제 사진, 정답 manifest, 원시 측정 CSV는 모두 저장소 루트의 `qa-private/` 아래에만 둔다. 이 경로 전체는 `.gitignore`에 등록되어 있다. `docs/qa/manifest.example.csv`와 `docs/qa/ocr-benchmark.csv`는 열 구조만 보여 주는 빈 예시이며 실제 측정 데이터가 아니다.

실행 전후 다음 명령으로 사진이나 실측 CSV가 Git 대상이 아닌지 확인한다.

```powershell
git status --short --ignored qa-private
git check-ignore -v qa-private/ocr-benchmark/images/sample_001.jpg
```

## 로컬 데이터 준비

```text
qa-private/
├─ ocr-benchmark/
│  ├─ manifest.csv
│  └─ images/
│     ├─ sample_001.jpg
│     └─ ...
└─ results/
   ├─ baseline-public-v2.csv
   └─ after-day-first-parser.csv
```

manifest를 만들고 사진 파일을 배치한다.

```powershell
New-Item -ItemType Directory -Force qa-private/ocr-benchmark/images
Copy-Item docs/qa/manifest.example.csv qa-private/ocr-benchmark/manifest.csv
```

manifest 열은 다음과 같다. 쉼표가 들어간 값은 허용하지 않는다.

| 열 | 의미 |
|---|---|
| `sample_id` | 사진과 결과를 연결하는 비식별 ID. 영문·숫자·`_`·`-`만 사용 |
| `image_file` | `qa-private/ocr-benchmark` 기준 상대 경로 |
| `expected_date` | 사람이 라벨을 확인한 정답, `YYYY-MM-DD` |
| `lighting` | `bright`, `dark`, `glare` 등 촬영 조명 |
| `orientation` | `upright`, `rotated_90`, `skewed` 등 라벨 방향 |
| `material` | `paper`, `plastic`, `can`, `cap` 등 재질 |
| `date_format` | 실제 인쇄 형식: `yyyy.mm.dd`, `yy.mm.dd`, `mm.dd`, `compact` 등 |
| `print_quality` | `clear`, `low_contrast`, `faded`, `broken_inkjet`, `smudged`, `embossed` 등 인쇄 상태 |
| `independence_key` | 같은 실제 제품·포장을 묶는 비식별 키. 릴리스 표본에서는 사진마다 고유해야 함 |
| `cohort` | 기존 표본은 `existing_20`, 새 독립 표본은 `new_30_plus` |
| `base_rotation` | 비트맵을 바로 세우는 회전값: `0`, `90`, `180`, `270` |
| `evaluation_date` | 연도 없는 날짜 추론을 고정할 평가 기준일, `YYYY-MM-DD` |

같은 사진은 같은 `sample_id`, 정답, 환경 라벨, `evaluation_date`를 유지한다. 실패 사진도 삭제하지 않는다.

새 사진을 받은 직후 기기 없이 입력만 검증한다.

```powershell
.\scripts\run-ocr-benchmark.ps1 `
  -DatasetDir qa-private/korean-labels-50 `
  -ReleaseBaseline `
  -ValidateOnly
```

## 실행 환경과 명령

요구사항은 JDK 17, Android SDK 35, 정확히 1대의 연결된 Android 기기 또는 에뮬레이터다. 2026-07-14 측정은 `Medium_Phone_API_36.0` AVD(Android 16), 기기 모델 `sdk_gphone64_x86_64`, 앱 `1.0.0-debug`에서 수행했다. Windows 한글 상위 경로에서 Gradle 8.2 테스트 클래스패스가 깨지는 문제를 피하기 위해 스크립트가 저장소를 임시 ASCII 드라이브로 매핑하고 종료 시 해제한다.

일반 Android 품질 게이트:

```powershell
.\scripts\android-quality.ps1
.\scripts\android-quality.ps1 -Tasks @("assembleDebugAndroidTest")
```

최초 기준선 측정:

```powershell
.\scripts\run-ocr-benchmark.ps1 `
  -RunName korean-labels-50-baseline `
  -DatasetDir qa-private/korean-labels-50 `
  -ReleaseBaseline
```

한국 보완 세트 측정:

```powershell
.\scripts\run-ocr-benchmark.ps1 `
  -RunName korean-labels-20 `
  -DatasetDir qa-private/korean-labels
```

가장 큰 실패 유형 하나만 수정한 뒤 동일 표본 회귀:

```powershell
.\scripts\run-ocr-benchmark.ps1 `
  -RunName korean-labels-50-after `
  -DatasetDir qa-private/korean-labels-50 `
  -ReleaseBaseline `
  -BaselineCsv qa-private/results/korean-labels-50-baseline.csv `
  -TargetFailureType <수정_전_최상위_실패_유형>
```

결과 회수 경로만 빠르게 확인할 때는 성과 측정에 포함하지 않는 스모크 옵션을 쓴다.

```powershell
.\scripts\run-ocr-benchmark.ps1 -RunName smoke-output-path -SampleLimit 1
```

러너는 manifest·사진 존재 여부, `sample_id`·파일 경로·SHA-256·독립성 키 중복, 연결 기기 수를 먼저 검사한다. 앱의 원본/90도·반전/반전 90도 OCR 경로를 실행하고 `qa-private/results/<run-name>.csv`를 만든다. 회귀 비교는 표본 수와 `sample_id`뿐 아니라 사진 해시·정답·모든 환경 분류·평가일까지 같아야 통과한다. 정확 일치와 후보 탐지가 하락하면 실패하며, `-TargetFailureType`은 수정 전 최상위 유형이어야 하고 건수가 실제로 줄어야 통과한다.

## 지표와 실패 분류

- 정확 일치율 = `predicted_date == expected_date`인 사진 / 전체 사진
- 날짜 후보 탐지율 = 날짜 후보를 하나 이상 반환한 사진 / 전체 사진
- 환경별 실패율 = 해당 조명·방향·재질·표기 그룹에서 정확 일치하지 않은 사진 / 해당 그룹 전체
- `ocr_error`: 네 가지 OCR 변형이 모두 처리 오류로 끝남
- `no_text`: OCR 처리는 성공했지만 인식 문자열이 없음
- `no_date_candidate`: 문자는 인식했지만 날짜 파서 후보가 없음
- `candidate_out_of_window`: 날짜 후보는 있으나 평가일 허용 범위를 통과한 후보가 없음
- `wrong_date`: 후보를 선택했지만 최종 날짜가 정답과 다름

2026-07-14의 공개 61장·한국 20장 역사 결과는 종전 `no_candidate` 분류를 사용한다. 새 한국 50장 수정 전·후 결과는 위 세분화된 동일 러너 버전끼리만 비교한다.

표본이 50장 미만이면 스크립트가 결과를 표시하되 v1.1 릴리스 기준선으로 인정하지 않는 경고를 낸다. 50장 이상이면 정확 일치율, 후보 탐지율, 실패 유형 및 조명·방향·재질·표기별 실패율을 모두 출력한다.

## 수정과 배포 판정 규칙

1. 50장 이상의 기준선을 먼저 측정한다.
2. 실패 건수를 `failure_type`으로 집계해 가장 큰 유형 하나를 선택한다.
3. 그 유형만 겨냥한 최소 변경을 한다. 측정 전에는 파서 개선을 추측해 적용하지 않는다.
4. 같은 기기·같은 사진·같은 manifest로 다시 실행한다.
5. 전체 정확 일치·후보 탐지가 하락하지 않고 대상 실패 유형이 개선됐는지 CSV 차이로 확인한다.
6. 단위 테스트·lint·debug 빌드가 모두 통과하고 목표 사용자 라벨의 대표성이 확보돼야 v1.1 배포 가능으로 판정한다.

실제 전후 수치는 이 문서의 현재 상태에 기록하되, 사진·sample별 정답·원시 결과는 계속 `qa-private/`에만 둔다. Google Play 또는 원스토어 제출은 이 절차에 포함하지 않는다.

## 남은 리스크와 다음 단계

- 공개 61장은 50장 판정선을 충족하지만 영어권·일 우선 표기가 많아 한국 유통기한 라벨을 대표하지 않는다.
- 한국 20장은 국내 적합성 보완에는 유효하지만 50장 미만이고 `YYYYMMDD`, 한글 `YYYY년 MM월 DD일` 표기가 없어 독립 릴리스 기준선으로는 부족하다.
- 다음 확장 시 서로 다른 한국 제품 30장 이상을 추가해 한국 세트도 50장을 채우고, 누락된 숫자 연속·한글 날짜 표기를 포함한다.
- 사진·정답·원시 결과는 계속 `qa-private/`에만 저장하고 공개 문서에는 집계만 남긴다.
- 월 이름 날짜 21장과 기울기 2장은 현재 실패율이 높다. 이번 작업은 최대 실패 원인 하나만 수정한다는 원칙 때문에 추가 수정하지 않았다.
