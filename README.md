# 智慧海象船班搭乘指南與停航通知 App

本專案為 Android App，整合即時海象觀測與航港船班狀態，協助旅客查詢港口海況、船班資訊與停航風險提醒。

## 專案結構

```text
.
├── README.md
├── proposal/
│   └── proposal.md
├── my-topics/
│   ├── topic_A11218001.md
│   └── topic_A11218002.md
├── app/
│   ├── src/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── backend/
│   └── README.md
└── docs/
    └── report.md
```

## App 功能

- 即時港口海象觀測資料顯示
- 即時航港船班狀態查詢
- 船班關鍵字搜尋
- 搭乘風險等級與旅客提醒
- 淺色與深色模式切換

## Android 專案位置

Android App 原始碼已放在 `app/` 目錄中，主要程式入口為：

```text
app/src/main/java/com/example/MainActivity.java
```

## 開發環境

- Android Studio
- Gradle Kotlin DSL
- Java / Kotlin Android 專案結構

