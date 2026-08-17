# 오늘도 신선 (Fridge D-Day) - 프로젝트 종합 문서

> **최종 상태: v1.0.2 Released (원스토어, 2026-08-12) — 공개 배포 이력 2건**
> 이 문서는 초기 기획과 구현 범위를 함께 보존하는 역사 문서다. 검증된 사실과 릴리스 판단 근거는 [QA_RELEASE_RECORD.md](QA_RELEASE_RECORD.md)를 정본으로 사용한다. 품질 기록에서 다시 확인되지 않은 수치·효과·평가는 당시 계획 또는 자체 판단이며 포트폴리오 성과로 인용하지 않는다.

## 📌 프로젝트 개요

### 1. 프로젝트 주제

**"지속가능 소비를 위한 Smart Pantry Assistant - Physical AI 응용 프로젝트"**

가정 내 식품 및 생활용품의 유통기한을 체계적으로 관리하고, ML 기반 OCR과 스마트 알림을 통해 식품 낭비를 줄이는 Android 네이티브 모바일 애플리케이션.

### 배경

- **문제 인식**: 국내 연간 식품 폐기물 500만톤 이상, 가정 내 폐기 식품의 60%가 유통기한 관리 실패로 발생
- **기존 앱 문제점**: 복잡한 입력 과정, 개인정보 수집 우려, 인터넷 연결 필수
- **목표**: ESG·지속가능성 트렌드에 부합하는 실용적 솔루션 개발

---

## 🎯 프로젝트 범위 및 의사결정

### 초기 구상 vs 실제 개발

**초기 구상 (Physical AI 중점)**
- 스마트 냉장고 센서 연동
- IoT 기반 온습도 모니터링
- 도어 개폐 패턴 학습
- 대규모 Physical AI 프로젝트

**실제 개발 (현실적 조정)**
- ML Kit OCR 기반 텍스트 인식
- 로컬 데이터 처리 중심
- 완성도에 집중한 CRUD + ML 앱

**조정 이유**
1. 졸업 준비와 병행하는 스케줄 제약
2. Android 네이티브 개발 첫 경험
3. **확실한 완성**을 위한 현실적 규모 설정
4. Physical AI 초기 단계로 위치 설정, 향후 확장 기반 마련

---

## ✨ 핵심 기능

### 1. ML 기반 OCR 자동 인식
- **기술**: Google ML Kit Text Recognition API
- **기능**: 카메라로 유통기한 촬영 → 자동 날짜 추출 및 입력
- **장점**: 수동 입력 대비 빠른 등록, 사용 편의성 극대화

### 2. 실시간 D-Day 계산 및 시각화
- **색상 코드 시스템**:
  - 🟢 초록: D-8 이상 (안전)
  - 🟡 노랑: D-2 ~ D-7 (주의)
  - 🔴 빨강: D-1 이하 (위험/만료)
- **자동 계산**: 오늘 날짜 기준 남은 일수 실시간 갱신

### 3. 스마트 알림 시스템
- **WorkManager 활용**: 배터리 최적화된 백그라운드 스케줄링
- **알림 유형**:
  - 매일 사용자 설정 시간에 임박 식품 알림
  - 유통기한 당일 별도 긴급 알림
- **Doze 모드 대응**: 저전력 상태에서도 정확한 알림

### 4. 홈 화면 위젯
- **Android App Widget**: 앱 실행 없이 곧 만료될 식품 확인
- **실시간 업데이트**: D-Day 자동 갱신

### 5. 소비 패턴 통계 및 분석
- **카테고리별 분석**: 채소, 과일, 육류, 유제품 등 소비/폐기 비율
- **월별 추이**: 소비율 시계열 시각화
- **인사이트**: 가장 많이 버리는 식품 TOP 3 파악

### 6. 백업 & 복원
- **로컬 백업**: JSON 형식으로 기기 저장
- **복원 기능**: 기기 변경 시 간편 데이터 이전
- **프라이버시**: 클라우드 없이 완전 로컬 관리

### 7. 다크모드 지원
- **Material Design 3**: 시스템 설정 연동 또는 수동 전환
- **접근성**: 눈의 피로 감소

---

## 📋 교수님 권고사항 대응 (역사적 계획 기록)

### 제출한 추진계획서 내용

#### 1. 과제명(주제)
- **제출**: "지속가능 소비를 위한 Smart Pantry Assistant (Fridge D-Day)"
- **Physical AI 연계**: 카메라 OCR 기반 유통기한 자동 인식
- **ESG 부합**: 식품 낭비 감소를 통한 환경 기여

#### 2. 추진 내용과 주요 일정

**현재 수준**
- 웹/서버(React/TS, Spring Boot) 및 AI 프로젝트 경험 보유
- Android 네이티브(Kotlin, Jetpack Compose) 실습 단계

**도전 목표**
- Room + WorkManager 기반 유통기한 관리 앱 구현
- 개인정보 미수집·오프라인 우선 설계로 차별화
- ~~Google Play 스토어 출시~~ (비공개 테스트 요건 미충족으로 보류)

**차별화 전략**
- 서비스: 로컬 저장·무계정·오프라인 동작(프라이버시 극대화)
- UX: 초간편 입력(퀵 날짜/카테고리), 접근성(큰 터치·다크모드)
- 확장: Physical AI(OCR), 가족 공유, 클라우드 백업/동기화

**실제 진행 일정**
- 1주차: 요구사항 확정, DB 스키마/네비 설계, UI 프로토타입
- 2주차: Room+Compose 통합, 아이템 CRUD 완성
- 3주차: WorkManager 알림, 설정 화면, UX 마감
- 4주차: QA/버그픽스, 스토어 에셋 제작, Play Console 등록 시도

#### 3. Key Result

**최종 결과물**
- ✅ Android 앱 구현 및 이후 원스토어 v1.0 출시
- ⚠️ Google Play는 출시하지 않음
- ✅ GitHub 리포지토리, AAB 빌드, 스크린샷, 메타데이터 완비

**보조 산출물**
- GitHub 리포지토리
- README(아키텍처/권한·Data Safety)
- Play Store 설명/스크린샷 8장
- 발표 자료(PPT) 및 대본

#### 4. 목표 마켓

- **당시 목표**: Google Play 스토어
- **실제 결과**: 원스토어 v1.0 출시, Google Play 미출시
- **당시 아이디어**: 학회·캡스톤 논문화 검토(실행·성과 없음)

---

## 🛠️ 기술 스택 및 아키텍처

### 개발 환경

```yaml
언어: Kotlin 1.9.22
빌드 도구: Gradle 8.x (Kotlin DSL)
IDE: Android Studio
버전 관리: Git

Android:
  Min SDK: 26 (Android 8.0 Oreo)
  Target SDK: 35 (Android 15)
  Compile SDK: 35
```

### 주요 기술

#### UI/UX
- **Jetpack Compose**: 선언형 UI 프레임워크
- **Material Design 3**: Google 최신 디자인 시스템
- **Navigation Compose**: 화면 전환 및 딥링크
- **Accompanist**: System UI Controller (상태바 색상 등)

#### 데이터 계층
- **Room Database**: SQLite 기반 ORM
- **Kotlin Flow**: 반응형 데이터 스트림
- **DataStore Preferences**: 설정 저장 (SharedPreferences 대체)

#### 백그라운드 작업
- **WorkManager**: 배터리 최적화된 백그라운드 스케줄링
- **알림(Notification)**: Android 13+ POST_NOTIFICATIONS 권한 처리

#### ML/AI
- **Google ML Kit**: Text Recognition API
- **CameraX**: 카메라 통합 및 권한 관리

#### 기타
- **Kotlin Coroutines**: 비동기 처리
- **ViewModel**: UI 상태 관리
- **Gson**: JSON 직렬화/역직렬화 (백업/복원)

### 아키텍처 패턴

**MVVM (Model-View-ViewModel) + Repository Pattern**

```
┌─────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)             │
│  - Screens: HomeScreen, AddEditScreen,  │
│    StatisticsScreen, SettingsScreen     │
│  - Components: ItemCard, DatePicker     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  ViewModel Layer                        │
│  - HomeViewModel                        │
│  - AddEditViewModel                     │
│  - StatisticsViewModel                  │
│  - SettingsViewModel                    │
│  (StateFlow로 UI 상태 관리)              │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  Repository Layer                       │
│  - FoodItemRepository                   │
│  - ConsumedItemRepository               │
│  - SettingsRepository                   │
│  (데이터 소스 추상화)                     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  Data Layer                             │
│  - Room Database (FoodDatabase)         │
│  - DAO: FoodItemDao, ConsumedItemDao    │
│  - Entity: FoodItem, ConsumedItem       │
│  - DataStore (설정)                      │
└─────────────────────────────────────────┘
```

**장점**
- UI와 비즈니스 로직 분리
- 테스트 가능성 향상
- 유지보수 용이
- 확장성 확보

---

## 📁 초기 설계 구조 스케치 (현재 구현 정본 아님)

아래 상세 트리와 코드 조각은 초기 설계 기록이라 현재 클래스명·패키지와 일부 다르다. 실제 구현 구조는 소스 트리와 [README.md](README.md)를, 검증된 종료 사실은 [QA_RELEASE_RECORD.md](QA_RELEASE_RECORD.md)를 기준으로 하며 아래 예시를 현재 구현 증거로 인용하지 않는다.

### 디렉토리 구조

```
Fridge-D-Day/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/app/fridgedday/
│   │   │   │   ├── data/                    # 데이터 계층
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── FoodDatabase.kt
│   │   │   │   │   │   ├── FoodItemDao.kt
│   │   │   │   │   │   ├── ConsumedItemDao.kt
│   │   │   │   │   │   ├── FoodItem.kt      # Entity
│   │   │   │   │   │   └── ConsumedItem.kt  # Entity
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── FoodItemRepository.kt
│   │   │   │   │       ├── ConsumedItemRepository.kt
│   │   │   │   │       └── SettingsRepository.kt
│   │   │   │   │
│   │   │   │   ├── ui/                      # UI 계층
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   └── Type.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── NavGraph.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   ├── addedit/
│   │   │   │   │   │   ├── AddEditScreen.kt
│   │   │   │   │   │   └── AddEditViewModel.kt
│   │   │   │   │   ├── statistics/
│   │   │   │   │   │   ├── StatisticsScreen.kt
│   │   │   │   │   │   └── StatisticsViewModel.kt
│   │   │   │   │   ├── settings/
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   │   └── components/
│   │   │   │   │       ├── ItemCard.kt
│   │   │   │   │       ├── QuickDatePicker.kt
│   │   │   │   │       ├── TimePickerDialog.kt
│   │   │   │   │       └── CategorySelector.kt
│   │   │   │   │
│   │   │   │   ├── util/                    # 유틸리티
│   │   │   │   │   ├── DDayCalculator.kt
│   │   │   │   │   ├── DDayState.kt
│   │   │   │   │   ├── DateFormatter.kt
│   │   │   │   │   └── NotificationHelper.kt
│   │   │   │   │
│   │   │   │   ├── worker/                  # WorkManager
│   │   │   │   │   └── NotificationWorker.kt
│   │   │   │   │
│   │   │   │   ├── widget/                  # App Widget
│   │   │   │   │   └── ExpiryWidgetReceiver.kt
│   │   │   │   │
│   │   │   │   └── MainActivity.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   └── ...
│   │   │   │   ├── mipmap-*/
│   │   │   │   │   └── ic_launcher.png (여러 해상도)
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── xml/
│   │   │   │   │   └── expiry_widget_info.xml
│   │   │   │   └── AndroidManifest.xml
│   │   │   │
│   │   │   └── assets/ (없음)
│   │   │
│   │   └── androidTest/ (Room DAO·홈 화면·OCR benchmark instrumentation)
│   │
│   ├── build.gradle.kts                      # 앱 레벨 빌드 설정
│   ├── proguard-rules.pro                    # ProGuard 난독화 규칙
│   └── build/outputs/                         # 로컬 생성물, Git 미추적
│
├── gradle/                                   # Gradle Wrapper
├── build.gradle.kts                          # 프로젝트 레벨 빌드 설정
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
│
├── playstore_assets/                         # Play Store 에셋
│   ├── app_icon_512.svg
│   ├── feature_graphic_1024x500.svg
│   └── feature_graphic_1024x500.png
│
├── privacy_policy.html                       # 개인정보처리방침
├── PLAYSTORE_GUIDE.md                        # Play Store 제출 가이드
├── PROJECT_OVERVIEW.md                       # 본 문서
└── README.md                                 # 프로젝트 소개
```

### 주요 파일 설명

#### 데이터 모델

**FoodItem.kt** (식품 아이템)
```kotlin
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                 // 식품명
    val expiryDate: LocalDate,        // 유통기한
    val category: String,             // 카테고리
    val memo: String = "",            // 메모
    val createdAt: LocalDateTime      // 등록일시
)
```

**ConsumedItem.kt** (소비 완료 아이템)
```kotlin
@Entity(tableName = "consumed_items")
data class ConsumedItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalName: String,
    val category: String,
    val expiryDate: LocalDate,
    val consumedAt: LocalDateTime,    // 소비 일시
    val isExpired: Boolean            // 만료 후 소비 여부
)
```

#### 핵심 로직

**DDayCalculator.kt**
```kotlin
object DDayCalculator {
    fun calculate(expiryDate: LocalDate): Long {
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, expiryDate)
    }

    fun getDDayState(daysUntil: Long): DDayState {
        return when {
            daysUntil > 7 -> DDayState.SAFE       // 초록
            daysUntil >= 2 -> DDayState.WARNING   // 노랑
            else -> DDayState.EXPIRED             // 빨강
        }
    }
}
```

**NotificationWorker.kt**
```kotlin
class NotificationWorker(context: Context, params: WorkerParameters)
    : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Room에서 임박 식품 조회
        // 알림 생성 및 전송
        return Result.success()
    }
}
```

---

## 🎨 UI/UX 특징

### Material Design 3 적용

- **Dynamic Color**: 시스템 테마 연동 (Android 12+)
- **다크모드**: 완전 지원
- **접근성**:
  - 큰 터치 영역 (최소 48dp)
  - 명확한 색상 대비
  - 스크린 리더 지원 준비

### 화면 구성

**1. 홈 화면 (HomeScreen)**
- 식품 목록 (LazyColumn)
- D-Day 색상 표시
- FAB로 식품 추가
- 정렬/필터 기능

**2. 식품 추가/수정 (AddEditScreen)**
- 식품명 입력
- 카테고리 선택 (Chip)
- 유통기한 입력 (DatePicker + QuickDatePicker)
- OCR 스캔 버튼 (CameraX + ML Kit)
- 메모 입력

**3. 통계 (StatisticsScreen)**
- 월별 소비율 그래프
- 카테고리별 비율 (파이 차트 - Canvas)
- TOP 3 폐기 식품

**4. 설정 (SettingsScreen)**
- 다크모드 설정
- 알림 시간 설정
- 백업/복원
- 앱 정보

---

## 🔒 보안 및 프라이버시

### 데이터 보호

- **로컬 저장**: 모든 데이터는 기기 내 Room Database에만 저장
- **서버 전송 없음**: 네트워크 통신 0건
- **개인정보 미수집**: 이름, 이메일, 위치 등 일체 수집 안 함
- **사용자 승인 권한 최소화**:
  - CAMERA (OCR용)
  - POST_NOTIFICATIONS (알림용)
- **네트워크 차단**: Release APK에서 `INTERNET` 권한 제거

### Play Store Data Safety 선언

- "이 앱은 사용자 데이터를 수집하거나 공유하지 않습니다"
- 완전한 오프라인 동작
- 광고 없음

---

## 📊 성과 및 평가

### 기술적 성과

✅ **검증**
- 단위 테스트 19개, lint, Debug/Release build, instrumentation 통과
- 독립 한국 식품 라벨 55장 D-30/D-180 회귀 기준선 고정

✅ **성능**
- Jetpack Compose 최적화로 즉각적 반응
- WorkManager로 배터리 효율적 알림

✅ **코드 품질**
- Kotlin 관례 준수
- MVVM 아키텍처로 테스트 가능성 확보
- ProGuard 적용으로 코드 난독화

### 개발 완성도

✅ **출시와 후속 릴리스 판단**
- v1.0 원스토어 출시 완료
- Google Play 미출시
- 출시 후 독립 QA 결과에 따라 v1.1 Release No-Go, 미제출 (2026-07-22 판단)
- 보류 사유를 제품 구조 변경으로 해결하고 v1.0.2를 원스토어에 배포 (2026-08-12). 공개 배포 이력 2건

### 학습 성과

✅ **새로운 기술 습득**
- Kotlin + Jetpack Compose (첫 경험 → 실무 수준)
- Room Database 설계 및 최적화
- ML Kit 통합
- WorkManager 백그라운드 작업
- Android App Widget 개발

✅ **프로젝트 관리**
- 현실적 범위 설정 능력
- 일정 관리 및 우선순위 결정
- 완성도 중심 접근

---

## 🚀 역사적 확장 아이디어 (현재 비활성)

아래 항목은 초기 구상 기록이며 구현·사용자 모집·출시 계획이 아니다. 현재 추가 OCR 정확도 개선, 실사진 수집, 사용자 모집·베타는 진행하지 않고 보존에 필요한 최소 유지보수만 한다. 마지막 스토어 배포는 v1.0.2(원스토어, 2026-08-12)이며, 그 이후의 추가 기능 로드맵은 없다.

### 단기 (3개월)

**1. AI 기반 소비 패턴 분석**
- 사용자 구매 패턴 학습
- 유통기한 임박 식품 활용 레시피 추천
- 낭비 감소 예측 및 피드백

**2. 가족 공유 기능**
- Firebase 기반 클라우드 동기화
- 가족 구성원 간 냉장고 공유
- 역할 기반 권한 관리

### 중장기 (6개월+)

**1. Physical AI 확장 - IoT 연동**
- 스마트 냉장고 센서 연동
- 온습도 센서 기반 최적 보관 조건 제안
- 냉장고 도어 개폐 패턴 학습

**2. 커머스 연계**
- 부족한 식품 자동 주문 제안
- 할인 정보 실시간 알림

**3. 학회 논문화**
- 지속가능 소비 관련 논문 작성
- Physical AI 응용 사례 연구

---

## 📝 결론

### 프로젝트 의의

**1. Physical AI의 실용적 응용**
- 카메라 센서 + ML 기술로 실생활 문제 해결
- 향후 IoT 확장 기반 마련

**2. 기대 효과 (미측정)**
- 식품 낭비 감소를 제품 목표로 삼았지만 감소량이나 행동 변화는 측정하지 않음
- ESG·지속가능성 기여와 사회적 가치 창출을 성과로 주장하지 않음

**3. 프라이버시 우선 설계**
- 로컬 ML 처리로 개인정보 보호
- 사용자 신뢰도는 측정하지 않음

### 핵심 성과

✅ Kotlin·Compose·Room·ML Kit 기반 Android 앱 v1.0 원스토어 출시
✅ 독립 55장 QA, 샘플별 회귀 자동화와 기준선 보호 구축
✅ 잔여 오답과 표본 공백을 근거로 v1.1 기준 미달 배포 차단
✅ 보류 사유를 제품 구조로 해결해 v1.0.2 원스토어 배포 (공개 배포 이력 2건)

### 최종 평가

> v1.0 출시 후 독립 QA와 릴리스 게이트를 적용해 v1.1 후보를 조건부 수치로 평가했고, 대표성 공백과 잔여 오답을 근거로 배포하지 않았다(2026-07-22 판단).
> 이후 OCR 정확도를 더 올리는 대신 보호 대상을 다시 정의해, 인식한 날짜를 사용자가 확인해야 저장되도록 하는 `OcrDateConfirmationDialog`를 넣어 잘못 읽힌 날짜의 저장 경로 자체를 차단했다. 이 제품 변경으로 보류 사유를 해소하고 2026-08-12에 v1.0.2를 원스토어에 배포했다. 공개 배포 이력은 v1.0 → v1.0.2 2건이다.
> 현재는 추가 OCR 정확도 개선·실사진 수집·사용자 모집·베타를 진행하지 않고 보존에 필요한 최소 유지보수만 한다. 상세는 [QA_RELEASE_RECORD.md](QA_RELEASE_RECORD.md) 「6. 보류 이후 — v1.0.2」.

---

## 📚 참고 자료

### 문서
- [PLAYSTORE_GUIDE.md](PLAYSTORE_GUIDE.md) - Play Store 제출 가이드
- [privacy_policy.html](privacy_policy.html) - 개인정보처리방침

### 기술 문서
- [Jetpack Compose 공식 문서](https://developer.android.com/jetpack/compose)
- [Room Database 가이드](https://developer.android.com/training/data-storage/room)
- [WorkManager 문서](https://developer.android.com/topic/libraries/architecture/workmanager)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)

---

**문서 작성일**: 2025년 1월 (최종 갱신 2026-08-17)
**프로젝트 버전**: 1.0.2
**버전 코드**: 4
**Target SDK**: 36
**프로젝트 상태**: v1.0.2 Released (원스토어, 2026-08-12) / 공개 배포 이력 2건 / 유지보수만 수행

> 2026-07-22 종료 시점에는 `v1.0 Released / v1.1 QA No-Go / Archived`로 적혀 있었다. 2026-08-12 v1.0.2 배포로 그 판정이 더 이상 현재 상태가 아니어서 갱신했다. v1.1 No-Go 판단 자체는 실제 기록이므로 위 본문에 그대로 보존한다.
