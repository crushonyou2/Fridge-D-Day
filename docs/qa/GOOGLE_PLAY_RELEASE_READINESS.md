# Google Play Release Readiness

확인일: 2026-07-31 (Asia/Seoul)

## 현재 판정

**추가 보완 필요. Paid Closed Test No-Go.**

이 판정은 Google Play 프로덕션 출시 여부가 아니라 유료 비공개 테스트에 진입할
준비가 되었는지를 대상으로 한다. 현재 기준선 빌드는 성공하지만 API 36, 날짜
확정 흐름, 데이터 보존, 서명, 실제 업데이트 검증이 완료되지 않았다.

기존 상태인 `v1.0 Released / v1.1 QA No-Go / Archived (Maintenance only)`는
유효하다. 이번 후보는 기존 v1.1 OCR 정확도 개선판을 다시 출시하는 것이 아니라,
Google Play 배포 안전성과 사용자의 날짜 확인을 보완하는 별도 Release Candidate다.

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

## Release Candidate 계약 초안

### 포함 범위

- 기본 versionCode 3
- versionName은 사용자 결정 전 미확정(권고안: 1.0.1)
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
