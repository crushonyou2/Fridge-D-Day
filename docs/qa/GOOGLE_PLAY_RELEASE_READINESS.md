# Google Play Release Readiness

확인일: 2026-07-31 (Asia/Seoul)

> **이 문서는 2026-07-31 시점 기록이다. 이후 상태가 바뀌었다.**
> 여기서 검토한 Release Candidate 계열 변경(날짜 확정 흐름, Room 파괴적 fallback 제거, API 36)은
> 2026-08-12 **v1.0.2로 원스토어에 배포됐다**. 현재 상태는 `v1.0.2 Released (원스토어) / 공개 배포 이력 2건`이며
> 아래 본문의 `Archived (Maintenance only)` 표기는 더 이상 현재 상태가 아니다.
> Google Play 미제출과 Paid Closed Test No-Go 판정 자체는 그대로 유효하다.
> 아래 판정 기록은 시점 기록으로 보존하며 소급 수정하지 않는다. 현재 사실은
> [QA_RELEASE_RECORD.md](../../QA_RELEASE_RECORD.md)를 정본으로 본다.

## 현재 판정

**로컬 Release Candidate 보완·검증 완료. Paid Closed Test No-Go.**

이 판정은 Google Play 프로덕션 출시 여부가 아니라 유료 비공개 테스트에 진입할
준비가 되었는지를 대상으로 한다. API 36 전환, 날짜 확정 흐름, 파괴적 Room
fallback 제거, 자동화와 API 36 에뮬레이터 검증은 완료했다. Play에서 내려받은
서명된 code 2 범용 APK와 Room schema, 기존 업로드 키 인증서도 확인했다.
그러나 업로드 키로 서명된 code 3 AAB, 실제 code 2 바이너리에서의 업데이트,
API 26·35, 실제 Android 기기, 독립 코드 리뷰가 확인되지 않아 No-Go를 유지한다.

확인일 시점의 기존 상태인 `v1.0 Released / v1.1 QA No-Go / Archived (Maintenance only)`는
그때 기준으로 유효했다(2026-08-12 v1.0.2 배포로 갱신됨 — 위 상단 주석 참조). 이번 후보는 기존 v1.1 OCR 정확도 개선판을 다시 출시하는 것이 아니라,
Google Play 배포 안전성과 사용자의 날짜 확인을 보완하는 별도 Release Candidate다.

| Go 조건 | 현재 상태 |
|---|---|
| code 3 / 1.0.1, API 36 | 완료 |
| 날짜 미확정 저장 차단·OCR 확인 | 완료 |
| Room v2 구조 검증·파괴적 fallback 제거 | 완료 |
| 자동 품질 게이트·API 36 에뮬레이터 | 완료 |
| D-30·D-180 OCR 회귀 | 완료, 기준선 대비 퇴행 0 |
| 업로드 키로 서명된 AAB | 미완료 |
| Play 서명 code 2 범용 APK·Room schema | 완료 |
| Play 앱 서명·업로드 인증서 지문 | 완료 |
| 기존 업로드 비공개 키 보유 | 완료 |
| 실제 code 2→3 기기 업데이트 | 미완료 |
| API 26·35 에뮬레이터 | 미완료(로컬 이미지 없음) |
| 실제 Android 기기 | 미완료 |
| 독립 코드 리뷰·P0/P1 0건 | 미완료 |

## Phase 0 감사

### Git 기준

| 항목 | 확인 결과 |
|---|---|
| 기준 브랜치 | `main` |
| 기준 SHA | `66f62c13f3b69577a5c0ada414b4610788afbc6a` |
| `origin/main` | 기준 SHA와 일치 |
| 시작 작업트리 | clean |
| 종료 변경 | `c9cab4a53e79e2d56345a7bea28d491f9345b4b6` 존재 |
| 작업 브랜치 | `codex/google-play-release-readiness` |

push, PR 생성, `main` 병합은 수행하지 않는다.

### 앱·빌드 계약

| 항목 | 현재 값 |
|---|---|
| application ID | `app.fridgedday` |
| versionCode | `2` |
| versionName | `1.0.0` |
| minSdk | 26 |
| compileSdk | 35 |
| targetSdk | 35 |
| AGP | 8.2.0 |
| Gradle | 8.2 |
| Kotlin | 1.9.22 |
| Compose Compiler | 1.5.10 |
| JDK | 17 |

AGP 8.2.0은 API 35에도 공식 검증 범위를 벗어났다는 빌드 경고가 발생한다.
Android 공식 호환표의 API 36 최소 버전은 AGP 8.9.1이며, AGP 8.9의 최소
Gradle 버전은 8.11.1이다. Kotlin 1.9.22와 Compose Compiler 1.5.10은 공식
호환 조합이므로 API 36 전환만을 위해 동시에 변경할 필요는 없다.

### Room DB와 업데이트

- DB 버전은 2다.
- `AppDatabase.kt`와 `ItemEntity.kt`는 초기 커밋
  `89c7c0bda8ab42c7ec0e7e8e85584525338202c3` 이후 변경되지 않았다.
- code 3에서 스키마를 변경하지 않으면 DB 버전 증가와 마이그레이션은 필요하지
  않다.
- 현재 `fallbackToDestructiveMigration()`이 활성화되어 있다. 이후 스키마
  버전 불일치가 발생하면 데이터가 삭제될 수 있으므로 Release Candidate에서
  제거해야 한다.
- `exportSchema = false`이며 저장소에 Room 스키마 이력이 없다. code 2 원본
  바이너리에서 code 3으로 실제 업데이트하는 검증이 필요하다.

### 서명과 Play App Signing

- Gradle에는 release `signingConfig`가 없다.
- 2026-07-31 기준으로 로컬에서 생성한
  `app/build/outputs/apk/release/app-release-unsigned.apk`는 서명되지 않았다.
- 코드 주석의 “Play App Signing 사용” 문구만으로 실제 Play Console 설정을
  확인할 수 없다.
- Play App Signing 등록 여부, 앱 서명 인증서와 업로드 인증서는 사용자가
  Play Console의 **설정 > 앱 무결성**에서 확인해야 한다.
- 키 저장소, 비밀번호, OTP, 복구코드는 저장소나 대화에 기록하지 않는다.

### 기존 바이너리와 소스 관계

저장소 안에는 기존 Google Play 또는 원스토어 code 2 AAB/APK가 없다.
versionCode 2와 versionName 1.0.0을 처음 포함한 커밋은 초기 커밋
`89c7c0bda8ab42c7ec0e7e8e85584525338202c3`이다. 따라서 이 커밋이 기존
v1.0 바이너리에 가장 가까운 공개 소스 후보지만, 정확한 빌드 커밋이라고
확정할 증거는 없다.

Google Play code 2와 원스토어 code 2의 동일성은 다음 순서로 확인한다.

1. 양 콘솔에서 원본 AAB를 내려받아 파일명, 크기와 SHA-256을 기록한다.
2. SHA-256이 같으면 바이트 단위로 동일한 파일이다.
3. 다르면 `bundletool dump manifest`로 package, versionCode, versionName,
   minSdk, targetSdk를 각각 확인하고 `jarsigner`로 서명 인증서를 확인한다.
4. 메타데이터와 인증서가 같아도 SHA-256이 다르면 동일 바이너리라고 단정하지
   않는다.

Play가 기기 배포용으로 다시 서명한 split APK와 원본 업로드 AAB는 직접 SHA-256
비교 대상으로 사용하지 않는다.

### 2026-08-03 Play 배포 code 2 정적 검증

사용자가 Play Console의 `2.aab (1.0.0)` 상세 화면과 해당 버전의
`Signed, universal APK`를 제공했다. APK를 저장소에 복사하거나 Git에 추가하지
않고 외부 파일 상태로 검사했다.

| 항목 | 확인 결과 |
|---|---|
| 파일 크기 | 50,036,980 bytes |
| 파일 SHA-256 | `087350944f00daad95320ab00a4bfe963ca57f254cff4f3cc0e97354b8f1d2e5` |
| package | `app.fridgedday` |
| versionCode / versionName | 2 / 1.0.0 |
| minSdk / compileSdk / targetSdk | 26 / 35 / 35 |
| APK 서명 | v2·v3 검증 성공, signer 1개 |
| 앱 서명 인증서 SHA-256 | `907a3cf5bc68c64a88887a2acb30cf4c46e2aacf5f0a7a652aacba9c006964f6` |
| signer | `CN=Android, OU=Android, O=Google Inc., L=Mountain View, ST=California, C=US` |
| Source Stamp | 검증 성공 |
| 비공개 QA 경로 | 미검출 |

Play가 생성한 범용 APK의 Google signer와 Source Stamp가 검증되므로 이 파일을
실제 Google Play 배포 code 2 기준 바이너리로 사용한다. 배포 APK에는
`INTERNET`와 `com.android.vending.CHECK_LICENSE` 권한이 포함돼 있다. 이는
code 2 APK의 관측 사실이며 원스토어 code 2 또는 원본 업로드 AAB의 권한·바이트
동일성을 의미하지 않는다.

DEX 정적 검사에서 실제 code 2의 Room 정보도 확인했다.

- DB 파일명: `fridgedday_database`
- Room identity hash: `7f8f5cf3df30923b938f00c97c561eb2`
- `items` 테이블과 `index_items_expiryDate` 정의가 저장소의 v2 schema와 일치

따라서 code 2와 code 3의 Room schema·identity hash가 동일하다는 정적 증거가
확보됐다. 다만 실제 Play 서명 code 2를 설치한 기기에서 code 3으로 업데이트해
사용자 행이 유지되는 동적 시험을 대체하지 않는다.

### 2026-08-03 Play 인증서 대조

Play Console의 앱 서명 화면에서 업로드 인증서 지문을 확인했다.

| 인증서 | SHA-256 |
|---|---|
| 앱 서명 인증서 | `90:7A:3C:F5:BC:68:C6:4A:88:88:7A:2A:CB:30:CF:4C:46:E2:AA:CF:5F:0A:7A:65:2A:AC:BA:9C:00:69:64:F6` |
| 업로드 인증서 | `6A:AA:21:41:35:BF:5A:0C:F6:D1:22:0E:02:3A:CC:D7:C2:4B:A1:90:CF:F9:E0:7E:D6:1D:BC:24:AC:E5:E8:B6` |

Digital Asset Links JSON의 앱 서명 지문은 Play 배포 code 2 APK에서 직접 추출한
signer SHA-256과 정확히 일치한다. 따라서 앱 서명 인증서와 업로드 인증서는
확정됐다. 사용자가 로컬 keystore의 alias `fresh-today-key`를 `keytool`로
검사했으며 SHA-256이 업로드 인증서와 정확히 일치했다. 따라서 기존 업로드
비공개 키 보유도 확인됐고 `업로드 키 재설정 요청`은 필요하지 않다. keystore
경로와 비밀번호는 문서·저장소·대화에 기록하지 않는다.

### 현재 main에 포함된 v1.1 QA No-Go 범위

초기 커밋 이후 main에는 OCR 파서 분리·보완, 단위 테스트, 비공개 55장
instrumentation 벤치마크, D-30/D-180 회귀 게이트, QA 자료의 Git·Release 제외,
`INTERNET` 권한 제거, 종료 문서가 추가됐다. versionCode와 versionName,
Room DB 스키마는 변경되지 않았다.

기존 고정 55장 결과는 D-30 정확 일치 40/55, D-180 정확 일치 39/55,
정답 후보 재현 43/55다. 이 수치는 실사용 전체 정확도가 아니며, v1.1은 남은
오답과 표본 공백으로 No-Go였다. 새 후보는 이 판정을 폐기하거나 정확도
향상을 주장하지 않는다.

### 기준선 빌드·테스트

2026-07-31에 다음 명령을 실행했다.

```powershell
.\scripts\android-quality.ps1 `
  -Tasks @("testDebugUnitTest","lintDebug","assembleDebug","assembleRelease","assembleDebugAndroidTest")
```

결과:

- unit 19개 통과
- `lintDebug` 통과(오류 0, 경고 40)
- Debug APK 빌드 통과
- Release APK 빌드 통과
- AndroidTest APK 빌드 통과
- instrumentation은 이번 감사에서 실행하지 않음

주요 lint 경고는 오래된 target API, AGP와 Compose lint 검사 비호환, 오래된
AndroidX 의존성이다. lint 성공만으로 API 36 동작을 증명하지 않는다.

현재 단위 테스트는 날짜 유틸리티, D-Day 상태, OCR 날짜 파서만 검증한다.
Home UI 4개, DAO 6개와 OCR benchmark 1개의 instrumentation 소스가 있지만
다음 릴리스 위험은 직접 검증하지 않는다.

- OCR 결과 확인·수정·취소
- OCR 오답의 자동 확정 방지
- 날짜 미확정 저장 차단
- 항목 추가·수정 ViewModel
- code 2에서 code 3 업데이트와 데이터 보존
- 카메라·알림 권한 거부와 재허용
- 백업·복원, WorkManager, 위젯
- API 35·36 edge-to-edge와 회전

### 기준선 임시 Release APK

아래 파일은 감사 중 재빌드한 **서명되지 않은 기준선 APK**이며 Release
Candidate 산출물이 아니다.

| 항목 | 값 |
|---|---|
| 파일 | `app/build/outputs/apk/release/app-release-unsigned.apk` |
| 크기 | 49,802,500 bytes |
| SHA-256 | `4c8523004bb7961a5d4274b2af550178ad9bbc193723ad122714aba7a3d8100e` |
| package | `app.fridgedday` |
| versionCode / versionName | 2 / 1.0.0 |
| minSdk / targetSdk | 26 / 35 |
| 서명 | 없음 |
| `INTERNET` 권한 | 없음 |
| QA 비공개 경로·사진 | 패키지 목록에서 미검출 |

이 digest는 기준선 감사 증거일 뿐이며 최종 후보 digest로 재사용하지 않는다.

## Phase 1~4 실행 결과

### 구현 기준

| 항목 | 결과 |
|---|---|
| 구현 커밋 | `c470a2a11c5aa871a93422e1df18fc0d2d1c629b` |
| application ID | `app.fridgedday` |
| versionCode / versionName | 3 / 1.0.1 |
| minSdk / compileSdk / targetSdk | 26 / 36 / 36 |
| AGP / Gradle / JDK | 8.9.1 / 8.11.1 / 17 |
| Kotlin / Compose Compiler | 1.9.22 / 1.5.10 |
| Room DB | version 2 유지, schema export 활성화 |

사용자가 versionCode 3과 versionName 1.0.1을 확정했다. DB entity와 version은
바꾸지 않았으며 `fallbackToDestructiveMigration()`을 제거했다.

### 적용한 최소 보완

- 신규 항목 날짜 초기값을 제거하고 날짜 선택·확인 전 저장을 차단했다.
- OCR 날짜는 후보로만 보관하며 `인식된 날짜가 맞나요?` 대화상자의 확인을 거쳐야
  저장값이 된다. 수정과 취소 경로 및 인식 실패 시 수동 선택 안내를 추가했다.
- 항목 수정 시 원래의 생성일, 보관 상태와 소비 완료일을 보존한다.
- Activity 재생성에도 추가·수정 ViewModel 상태가 유지되도록 수명주기를 연결했다.
- 날짜 선택기의 UTC 변환으로 시간대에 따른 날짜 이동 가능성을 제거했다.
- Room v2 schema를 저장소에 기록하고 파괴적 fallback을 제거했다.
- 백업 형식 v2에서 수량 0, 보관 항목, 소비 완료일과 생성일을 보존하고 v1 읽기
  호환성을 유지했다.
- API 36 edge-to-edge와 카메라 시스템 바 inset을 적용했다.
- 알림·카메라 권한 거부 시 앱 설정 진입 경로를 제공하고 앱 복귀 시 알림 권한을
  재조회한다.
- 알림 발행 전 권한을 검사하고 알림 PendingIntent의 불필요한 data URI를 제거했다.
- OCR 원문·예외 stack trace 출력을 제거하고 Release 개인정보 경계를 CI에 추가했다.
- CameraX의 정상 `ImageProxy.toBitmap()` 경로를 가리던 잘못된 변환 확장을 제거했다.

새 OCR 정확도 개선, 새 기능, `INTERNET` 권한, 서버·분석·광고 SDK는 추가하지
않았다.

### 자동화 결과

2026-07-31에 최종 구현 커밋 대상으로 다음 품질 게이트를 실행했다.

```powershell
.\scripts\android-quality.ps1 `
  -Tasks @(
    "testDebugUnitTest",
    "lintDebug",
    "assembleDebug",
    "assembleRelease",
    "bundleRelease",
    "assembleDebugAndroidTest"
  )
```

결과:

- 단위 테스트 29/29 통과
- lint 오류 0, 경고 41
- Debug APK, Release APK, Release AAB, AndroidTest APK 빌드 통과
- API 36 `Medium_Phone_API_36.0` AVD 연결 테스트 14/14 통과
- 연결 테스트에는 Home UI, DAO, OCR 확인 대화상자와 Room v2 데이터 보존이 포함됨

lint 경고 41건의 주요 구성은 의존성 업데이트 제안 24건과 미사용 리소스 8건이다.
컴파일 시 기존 `Sort` 아이콘 API 폐기 예고 1건이 발생하지만 차단 오류는 아니다.

한글 상위 경로에서 Gradle을 직접 실행하면 JVM 테스트 클래스패스가 깨지는 환경
제약이 재현됐다. 정식 결과는 저장소 품질 스크립트가 임시 ASCII 드라이브를
매핑해 실행한 값이다.

### API 36 에뮬레이터 검증

API 36 AVD에서 다음을 확인했다.

- 신규 항목은 날짜가 비어 있고 미확정 저장이 차단됨
- 날짜를 수동 선택·확정한 뒤 저장됨
- 제품명과 확정 날짜가 세로↔가로 Activity 재생성 후 유지됨
- 홈·추가·카메라 화면의 시스템 바 inset과 edge-to-edge가 겹치지 않음
- 라이트·다크 모드 및 글자 크기 130%에서 주요 홈 화면 문구와 동작이 잘리지 않음
- 알림 권한 거부 후 안내 유지, 앱 설정 진입, 재허용 후 복귀 즉시 안내 제거
- 카메라 권한 거부 후 안내 Snackbar와 올바른 앱 설정 진입

API 26·35 AVD는 로컬 SDK에 system image가 없고 `sdkmanager`를 포함한 Android
command-line tools도 없어 실행하지 못했다. 이는 제품 통과가 아니라 검증 환경
공백으로 기록한다.

### OCR 릴리스 회귀

Git 제외된 동일 한국 라벨 55장으로 API 36 AVD에서 실행했다.

| 시나리오 | 결과 | 기준선 대비 |
|---|---|---|
| D-30 | 정확 40/55(72.73%), 정답 후보 43/55(78.18%), 변형 실패 0 | 변화 0 |
| D-180 | 정확 39/55(70.91%), 정답 후보 43/55(78.18%), 변형 실패 0 | 변화 0 |

개별 정확 일치와 정답 후보 퇴행도 0건이다. 비공개 결과는
`qa-private/results/google-play-rc-code3-20260731-{d30,d180}.csv`에만 있으며
`.gitignore` 적용을 확인했다. 이 결과는 v1.1의 기존 No-Go를 변경하거나 실사용
OCR 정확도를 주장하는 근거가 아니다.

### 최종 로컬 산출물

아래 파일은 구현 검증용 **미서명 로컬 산출물**이다. Play 업로드 후보로 사용할
수 없으며 업로드 키로 서명한 뒤 digest를 새로 기록해야 한다.

| 파일 | 크기 | SHA-256 |
|---|---:|---|
| `app/build/outputs/apk/debug/app-debug.apk` | 61,500,937 bytes | `9e91f9fdb3ae61adf0bab30c1e4add56850bf479f96a183a0a23d4a8081f7fbd` |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | 49,283,233 bytes | `e5fef984ddbdef82eb3c8179a5d5489882d6fdb770f1b0cfad900e86b8998690` |
| `app/build/outputs/bundle/release/app-release.aab` | 39,567,515 bytes | `a721184813bd5afd58d13177498ded7731bd5264d33b46919a30ec48a9983683` |

Release APK에서 package, versionCode 3, versionName 1.0.1, minSdk 26,
compileSdk·targetSdk 36을 확인했다. 권한 목록에 `INTERNET`가 없고 패키지
목록에서 `qa-private`, 한국 라벨, OCR benchmark, `local.properties`를
검출하지 않았다. Release APK와 AAB는 모두 `jar is unsigned`다.

### 미완료·차단 항목

- 사용자의 업로드 키로 code 3 AAB 서명 및 서명 후 SHA-256 기록
- Google Play code 2 원본 AAB 확보 가능 여부와 원스토어 code 2 동일성 확인
- 실제 code 2 설치 데이터가 유지되는 code 2→3 업데이트 시험
- API 26·35 에뮬레이터 시험
- 실제 Android 기기의 카메라, Photo Picker, 알림, WorkManager, 위젯,
  백업·복원, 회전·재시작 시험
- 독립 코드 리뷰와 미해결 P0/P1 0건 확인

Room v2 schema 기반 데이터 보존 instrumentation과 실제 Play code 2 APK의 DEX
schema 일치 검사는 통과했다. 그러나 Play 서명 code 2 설치 상태에서 code 3으로
업데이트하지 않았으므로 실제 업데이트 검증을 대체하지 않는다.

## API 36 검토 범위

Google 공식 문서 기준으로 다음 변경을 검토한다.

- API 36용 AGP·Gradle과 compileSdk·targetSdk
- Android 16의 강제 edge-to-edge와 시스템 바 inset
- predictive back(현재 앱의 직접 back interception은 미검출)
- 대화면 회전·리사이즈 시 Compose 상태 보존
- Android 16 JobScheduler quota가 적용되는 WorkManager 동작
- 명시적 intent와 deep link 일치
- 알림, 백업·복원, 위젯, CameraX, 시스템 Photo Picker

확인한 공식 자료:

- [Google Play target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en-GB)
- [Android Gradle plugin/API compatibility](https://developer.android.com/build/releases/about-agp)
- [Set up the Android 16 SDK](https://developer.android.com/about/versions/16/setup-sdk)
- [Android 16 changes for target 36](https://developer.android.com/about/versions/16/behavior-changes-16)
- [Android 16 changes for all apps](https://developer.android.com/about/versions/16/behavior-changes-all)
- [Compose/Kotlin compatibility](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)

2026-08-31부터 일반 Android 신규 앱과 업데이트는 API 36 이상을 대상으로 해야
한다. 이번 후보는 유료 비공개 테스트 이후 일정 지연 가능성을 고려해 API 36을
목표로 한다.

## Release Candidate 계약

### 포함 범위

- versionCode 3
- versionName 1.0.1
- API 36과 공식 지원 빌드 도구
- OCR 결과를 저장값으로 자동 확정하지 않는 확인·수정·취소 흐름
- 신규 항목의 날짜 초기값 제거와 명시적 선택 전 저장 차단
- Room 파괴적 fallback 제거와 code 2→3 데이터 보존 검증
- 권한·백업·WorkManager·위젯·Release manifest 핵심 검증
- 자동화, API 26에 가까운 환경·API 35·API 36 에뮬레이터, 실제 기기 검증
- 독립 코드 리뷰와 finding 재검증

### 제외 범위

- OCR 정확도 추가 개선과 새 제품 기능
- 계정, 서버, 광고, 분석, 추적 SDK와 `INTERNET` 권한
- 원스토어 업데이트
- Play Console 업로드, 유료 결제, 테스터 등록, 프로덕션 접근 신청
- 실제 식품 사진과 `qa-private/`의 Git 추가

### 종료 조건

유료 비공개 테스트 Go는 계획에 정의된 자동화·에뮬레이터·실제 Android 기기
검증, 서명된 Release artifact digest, 독립 최종 리뷰와 미해결 P0/P1 0건이
모두 충족된 뒤에만 선언한다. 그 전까지 이 문서의 판정은 No-Go다.
