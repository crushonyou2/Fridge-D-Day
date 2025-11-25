# 오늘도 신선 (Fridge D-Day)

<div align="center">

**지속가능 소비를 위한 스마트 식품 유통기한 관리 앱**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-8.0+-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.10-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

[한국어](#-한국어) | [English](#-english)

</div>

---

## 📱 한국어

### 프로젝트 소개

**오늘도 신선**은 가정 내 식품 및 생활용품의 유통기한을 체계적으로 관리하여 식품 낭비를 줄이고 지속가능한 소비를 돕는 Android 네이티브 애플리케이션입니다.

**Physical AI 응용 프로젝트**로서, 스마트폰 카메라와 ML 기반 OCR 기술을 활용하여 누구나 쉽게 유통기한을 등록하고 관리할 수 있도록 설계되었습니다.

#### 개발 배경

- 국내 연간 식품 폐기물 **500만톤 이상** 발생
- 가정 내 폐기 식품의 **60%**가 유통기한 관리 실패로 인한 것
- 기존 앱의 문제점: 복잡한 입력 과정, 개인정보 수집 우려, 인터넷 연결 필수

#### 프로젝트 목표

- ✅ **ML 기반 OCR**로 유통기한 자동 인식
- ✅ **로컬 데이터 처리**로 프라이버시 완전 보장
- ✅ **직관적인 UX**로 누구나 쉽게 사용
- ✅ **최신 Android 기술** 실무 수준 습득

---

### ✨ 주요 기능

#### 1️⃣ ML 기반 OCR 자동 인식
- **Google ML Kit** Text Recognition API 활용
- 카메라로 유통기한 촬영 → 자동 날짜 추출 및 입력
- 다양한 폰트와 조명 환경 대응
- 이미지 회전, 색상 반전 등 전처리로 인식률 향상

#### 2️⃣ 실시간 D-Day 계산 및 시각화
- **색상 코드 시스템**으로 신선도 한눈에 파악
  - 🟢 **초록**: D-8 이상 (안전)
  - 🟡 **노랑**: D-2 ~ D-7 (주의)
  - 🔴 **빨강**: D-1 이하 (위험/만료)
- 오늘 날짜 기준 남은 일수 자동 계산

#### 3️⃣ 스마트 알림 시스템
- **WorkManager** 기반 배터리 효율적 백그라운드 스케줄링
- 매일 사용자 설정 시간에 임박 식품 알림
- 유통기한 당일 별도 긴급 알림
- Doze 모드에서도 정확한 알림 전송

#### 4️⃣ 홈 화면 위젯
- Android App Widget으로 앱 실행 없이 곧 만료될 식품 확인
- D-Day 자동 갱신으로 실시간 정보 제공

#### 5️⃣ 소비 패턴 통계 및 분석
- 카테고리별 소비/폐기 비율 분석
- 월별 소비 추이 시각화
- 가장 많이 버리는 식품 TOP 3 파악
- 데이터 기반 인사이트로 낭비 감소 유도

#### 6️⃣ 백업 & 복원
- **JSON 형식** 로컬 백업
- 기기 변경 시 간편 데이터 복원
- **클라우드 없이** 완전 로컬 관리로 프라이버시 보호

#### 7️⃣ 다크모드 지원
- Material Design 3 기반 테마 시스템
- 시스템 설정 연동 또는 수동 전환
- 접근성 및 눈의 피로 감소

---

### 🎯 차별화 전략

#### 프라이버시 우선 설계
- **개인정보 미수집**: 이름, 이메일, 위치 등 일체 수집 안 함
- **서버 전송 없음**: 모든 데이터는 기기 내 Room Database에만 저장
- **완전한 오프라인 동작**: 인터넷 연결 불필요
- **최소 권한**: CAMERA (OCR용), POST_NOTIFICATIONS (알림용)만 사용

#### 사용자 경험 우선
- **직관적인 한글 UI**: 한국 사용자를 위한 명확한 용어
- **퀵 버튼**: "오늘", "+3일", "+7일" 등 빠른 날짜 입력
- **큰 터치 영역**: 최소 48dp 보장으로 접근성 향상
- **Material Design 3**: Google 최신 디자인 시스템 적용

#### ML 기반 자동화
- **OCR 자동 인식**: 수동 입력 대비 빠르고 편리한 등록
- **스마트 날짜 추출**: YYYY.MM.DD, YY.MM.DD, MM.DD 등 다양한 형식 지원
- **전처리 최적화**: 회전, 색상 반전으로 인식률 극대화

---

### 🛠️ 기술 스택

#### 개발 환경
- **언어**: Kotlin 1.9.22
- **빌드 도구**: Gradle 8.x (Kotlin DSL)
- **IDE**: Android Studio
- **버전 관리**: Git
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

#### 주요 라이브러리

**UI/UX**
- Jetpack Compose (선언형 UI 프레임워크)
- Material Design 3 (최신 디자인 시스템)
- Navigation Compose (화면 전환 및 딥링크)

**데이터 계층**
- Room Database 2.6.1 (SQLite 기반 ORM)
- Kotlin Flow (반응형 데이터 스트림)
- DataStore Preferences (설정 저장)

**백그라운드 작업**
- WorkManager 2.9.0 (배터리 최적화 스케줄링)
- Android Notifications (POST_NOTIFICATIONS 권한)

**ML/AI**
- Google ML Kit Text Recognition Korean 16.0.0
- CameraX 1.3.1 (카메라 통합)

**기타**
- Kotlin Coroutines (비동기 처리)
- ViewModel (UI 상태 관리)
- Vico Charts (통계 시각화)
- Gson (JSON 직렬화/역직렬화)

---

### 🏗️ 아키텍처

#### MVVM + Repository Pattern

```
┌─────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)             │
│  - HomeScreen, AddEditScreen            │
│  - StatisticsScreen, SettingsScreen     │
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
│  - ItemRepository                       │
│  - SettingsRepository                   │
│  (데이터 소스 추상화)                     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  Data Layer                             │
│  - Room Database (AppDatabase)          │
│  - DAO: ItemDao                         │
│  - Entity: ItemEntity                   │
│  - DataStore (설정)                      │
└─────────────────────────────────────────┘
```

**장점**
- UI와 비즈니스 로직 명확한 분리
- 테스트 가능성 향상
- 유지보수 용이
- 확장성 확보

---

### 📁 프로젝트 구조

```
Fridge-D-Day/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/app/fridgedday/
│   │   │   │   ├── data/                    # 데이터 계층
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   └── ItemDao.kt
│   │   │   │   │   │   ├── entity/
│   │   │   │   │   │   │   └── ItemEntity.kt
│   │   │   │   │   │   └── converter/
│   │   │   │   │   ├── pref/
│   │   │   │   │   │   ├── AppSettings.kt
│   │   │   │   │   │   └── SettingsDataStore.kt
│   │   │   │   │   └── repo/
│   │   │   │   │       ├── ItemRepository.kt
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
│   │   │   │   │       ├── DatePickerField.kt
│   │   │   │   │       ├── TimePickerDialog.kt
│   │   │   │   │       └── CameraPreview.kt
│   │   │   │   │
│   │   │   │   ├── util/                    # 유틸리티
│   │   │   │   │   ├── DateUtils.kt
│   │   │   │   │   ├── DDayState.kt
│   │   │   │   │   ├── NotificationUtils.kt
│   │   │   │   │   ├── PermissionUtils.kt
│   │   │   │   │   ├── ocr/
│   │   │   │   │   │   └── TextRecognitionHelper.kt
│   │   │   │   │   └── backup/
│   │   │   │   │       └── BackupManager.kt
│   │   │   │   │
│   │   │   │   ├── worker/                  # WorkManager
│   │   │   │   │   ├── ExpiryCheckWorker.kt
│   │   │   │   │   └── WorkScheduler.kt
│   │   │   │   │
│   │   │   │   ├── widget/                  # App Widget
│   │   │   │   │   └── ExpiryWidget.kt
│   │   │   │   │
│   │   │   │   └── MainActivity.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap-*/
│   │   │   │   ├── values/
│   │   │   │   ├── xml/
│   │   │   │   └── AndroidManifest.xml
│   │   │   │
│   │   └── test/                            # 단위 테스트
│   │       ├── DateUtilsTest.kt
│   │       └── DDayStateTest.kt
│   │
│   └── build.gradle.kts                     # 앱 레벨 빌드 설정
│
├── gradle/                                  # Gradle Wrapper
├── build.gradle.kts                         # 프로젝트 레벨 빌드 설정
├── settings.gradle.kts
├── playstore_assets/                        # Play Store 에셋
├── presentation/                            # 발표 자료
├── privacy_policy.html                      # 개인정보처리방침
├── PROJECT_OVERVIEW.md                      # 프로젝트 종합 문서
└── README.md                                # 본 문서
```

---

### 🚀 시작하기

#### 필수 요구사항

- Android Studio Hedgehog (2023.1.1) 이상
- JDK 17
- Android SDK 35 (Android 15)
- Gradle 8.x

#### 설치 및 실행

1. **리포지토리 클론**
   ```bash
   git clone https://github.com/yourusername/Fridge-D-Day.git
   cd Fridge-D-Day
   ```

2. **Android Studio에서 프로젝트 열기**
   - File → Open → 프로젝트 디렉토리 선택
   - Gradle 동기화 자동 실행 대기

3. **빌드 및 실행**
   - 에뮬레이터 또는 실제 기기 연결
   - Run 버튼 클릭 (Shift + F10)

#### APK 빌드

**Debug APK**
```bash
./gradlew assembleDebug
# 출력: app/build/outputs/apk/debug/app-debug.apk
```

**Release AAB** (Play Store 업로드용)
```bash
./gradlew bundleRelease
# 출력: app/build/outputs/bundle/release/app-release.aab
```

---

### 🔒 보안 및 프라이버시

#### 데이터 보호

- ✅ **로컬 저장**: 모든 데이터는 기기 내 Room Database에만 저장
- ✅ **서버 전송 없음**: 네트워크 통신 0건
- ✅ **개인정보 미수집**: 이름, 이메일, 위치 등 일체 수집 안 함
- ✅ **권한 최소화**: CAMERA, POST_NOTIFICATIONS만 사용

#### Play Store Data Safety 선언

> "이 앱은 사용자 데이터를 수집하거나 공유하지 않습니다"

- 완전한 오프라인 동작
- 광고 없음
- 추적 없음

---

### 📊 성과 및 평가

#### 기술적 성과

✅ **안정성**
- 테스트 기간 중 충돌 0건
- Room Database 트랜잭션 안전성 확보

✅ **성능**
- Jetpack Compose 최적화로 즉각적 반응
- WorkManager로 배터리 효율적 알림

✅ **코드 품질**
- Kotlin 코딩 관례 준수
- MVVM 아키텍처로 테스트 가능성 확보
- ProGuard 적용으로 코드 난독화

#### 개발 완성도

✅ **계획 대비 100% 달성**
- 모든 핵심 기능 구현 완료
- UI/UX 완성도 높음

✅ **Play Store 출시 기준 충족**
- Target SDK 35 (최신)
- 보안 정책 준수
- 데이터 안전 정책 만족

⚠️ **출시 보류 사유**
- 2024년 정책 변경: 프로덕션 출시 전 비공개 테스트 필수
- 요건: 12명 이상 테스터 + 14일 이상 기간
- 기술적으로는 출시 가능, 정책 요건 미충족

---

### 🚀 향후 발전 방향

#### 단기 (3개월)

**1. AI 기반 소비 패턴 분석**
- 사용자 구매 패턴 학습
- 유통기한 임박 식품 활용 레시피 추천
- 낭비 감소 예측 및 피드백

**2. 가족 공유 기능**
- Firebase 기반 클라우드 동기화
- 가족 구성원 간 냉장고 공유
- 역할 기반 권한 관리

#### 중장기 (6개월+)

**1. Physical AI 확장 - IoT 연동**
- 스마트 냉장고 센서 연동
- 온습도 센서 기반 최적 보관 조건 제안
- 냉장고 도어 개폐 패턴 학습

**2. 커머스 연계**
- 부족한 식품 자동 주문 제안
- 할인 정보 실시간 알림

---

### 👤 개발자

**Jigwan Joe**
- Email: crushonyou223@gmail.com
- GitHub: [@crushonyou2](https://github.com/crushonyou2)

---

### 📚 참고 자료

#### 프로젝트 문서
- [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) - 프로젝트 종합 문서
- [PLAYSTORE_GUIDE.md](PLAYSTORE_GUIDE.md) - Play Store 제출 가이드
- [privacy_policy.html](privacy_policy.html) - 개인정보처리방침
- [presentation/](presentation/) - 발표 자료 및 대본

#### 기술 문서
- [Jetpack Compose 공식 문서](https://developer.android.com/jetpack/compose)
- [Room Database 가이드](https://developer.android.com/training/data-storage/room)
- [WorkManager 문서](https://developer.android.com/topic/libraries/architecture/workmanager)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition)

---

### 🙏 감사의 글

이 프로젝트는 Physical AI 응용 프로젝트로 시작하여, 실용적이고 완성도 높은 앱으로 발전했습니다. Android 개발 첫 경험이었지만, 최신 기술을 실무 수준으로 습득하고 지속가능한 소비 문화에 기여할 수 있는 도구를 만들 수 있어서 보람찼습니다.

---

<div align="center">

**Made with ❤️ by Jigwan Joe**

</div>

---

## 📱 English

### Project Introduction

**Fridge D-Day** (Fresh Today) is an Android native application that systematically manages expiration dates of food and household products at home, helping to reduce food waste and promote sustainable consumption.

As a **Physical AI application project**, it leverages smartphone cameras and ML-based OCR technology, designed for anyone to easily register and manage expiration dates.

#### Background

- Over **5 million tons** of food waste annually in South Korea
- **60%** of household food waste is due to expiration date management failures
- Existing app problems: Complex input process, privacy concerns, internet connection required

#### Project Goals

- ✅ **ML-based OCR** for automatic expiration date recognition
- ✅ **Local data processing** for complete privacy protection
- ✅ **Intuitive UX** for easy usage by anyone
- ✅ **Latest Android technology** practical skill acquisition

---

### ✨ Key Features

#### 1️⃣ ML-based OCR Automatic Recognition
- Utilizes **Google ML Kit** Text Recognition API
- Camera capture → automatic date extraction and input
- Supports various fonts and lighting environments
- Enhanced recognition rate with preprocessing (rotation, color inversion)

#### 2️⃣ Real-time D-Day Calculation and Visualization
- **Color code system** for at-a-glance freshness
  - 🟢 **Green**: D-8+ (Safe)
  - 🟡 **Yellow**: D-2 ~ D-7 (Warning)
  - 🔴 **Red**: D-1 or less (Danger/Expired)
- Automatic calculation of remaining days from today

#### 3️⃣ Smart Notification System
- **WorkManager**-based battery-efficient background scheduling
- Daily notifications of expiring items at user-set time
- Separate urgent notifications on expiration day
- Accurate notifications even in Doze mode

#### 4️⃣ Home Screen Widget
- Android App Widget for checking expiring items without opening the app
- Real-time information with automatic D-Day updates

#### 5️⃣ Consumption Pattern Statistics and Analysis
- Category-based consumption/disposal ratio analysis
- Monthly consumption trend visualization
- TOP 3 most discarded items identification
- Data-driven insights to reduce waste

#### 6️⃣ Backup & Restore
- **JSON format** local backup
- Easy data restoration when changing devices
- **Without cloud** complete local management for privacy protection

#### 7️⃣ Dark Mode Support
- Material Design 3-based theme system
- System settings integration or manual toggle
- Accessibility and reduced eye strain

---

### 🎯 Differentiation Strategy

#### Privacy-First Design
- **No personal information collection**: No name, email, location collection
- **No server transmission**: All data stored only in device's Room Database
- **Complete offline operation**: No internet connection required
- **Minimal permissions**: Only CAMERA (for OCR), POST_NOTIFICATIONS (for alerts)

#### User Experience First
- **Intuitive Korean UI**: Clear terminology for Korean users
- **Quick buttons**: "Today", "+3 days", "+7 days" for fast date input
- **Large touch areas**: Minimum 48dp for accessibility
- **Material Design 3**: Google's latest design system

#### ML-based Automation
- **OCR automatic recognition**: Fast and convenient registration vs manual input
- **Smart date extraction**: Supports various formats (YYYY.MM.DD, YY.MM.DD, MM.DD)
- **Preprocessing optimization**: Rotation, color inversion to maximize recognition rate

---

### 🛠️ Tech Stack

#### Development Environment
- **Language**: Kotlin 1.9.22
- **Build Tool**: Gradle 8.x (Kotlin DSL)
- **IDE**: Android Studio
- **Version Control**: Git
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

#### Major Libraries

**UI/UX**
- Jetpack Compose (Declarative UI framework)
- Material Design 3 (Latest design system)
- Navigation Compose (Screen transitions and deep links)

**Data Layer**
- Room Database 2.6.1 (SQLite-based ORM)
- Kotlin Flow (Reactive data streams)
- DataStore Preferences (Settings storage)

**Background Tasks**
- WorkManager 2.9.0 (Battery-optimized scheduling)
- Android Notifications (POST_NOTIFICATIONS permission)

**ML/AI**
- Google ML Kit Text Recognition Korean 16.0.0
- CameraX 1.3.1 (Camera integration)

**Others**
- Kotlin Coroutines (Asynchronous processing)
- ViewModel (UI state management)
- Vico Charts (Statistics visualization)
- Gson (JSON serialization/deserialization)

---

### 🚀 Getting Started

#### Prerequisites

- Android Studio Hedgehog (2023.1.1) or higher
- JDK 17
- Android SDK 35 (Android 15)
- Gradle 8.x

#### Installation and Running

1. **Clone repository**
   ```bash
   git clone https://github.com/yourusername/Fridge-D-Day.git
   cd Fridge-D-Day
   ```

2. **Open project in Android Studio**
   - File → Open → Select project directory
   - Wait for automatic Gradle sync

3. **Build and run**
   - Connect emulator or real device
   - Click Run button (Shift + F10)

#### Building APK

**Debug APK**
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

**Release AAB** (for Play Store upload)
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

---

### 👤 Developer

**Jigwan Joe**
- Email: crushonyou223@gmail.com
- GitHub: [@crushonyou2](https://github.com/crushonyou2)

---

<div align="center">

**Made with ❤️ by Jigwan Joe**

</div>
