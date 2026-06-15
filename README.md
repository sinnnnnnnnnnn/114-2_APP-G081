# 114-2 APP 開發 - 期末專題

## 專題名稱

智慧海象船班搭乘指南與停航通知 App

## 專題概述

本專案為「114-2 APP 開發」課程期末專題，主題方向屬於海事與海洋應用。App 整合即時海象觀測資料與航港船班狀態，協助旅客在搭船前快速掌握港口海況、船班資訊與停航風險提醒。

使用者可透過 App 查看浪高、風速、風向、海溫、潮位、測站位置與船班狀態，並依照系統提供的風險提示判斷是否適合前往港口或搭乘船班。

---

## 專案資料夾結構

本專案依照課程模板整理如下：

```text
├── README.md                 ← App 介紹與專案說明
├── build.gradle.kts          ← Android 專案根層 Gradle 設定
├── settings.gradle.kts       ← Android 專案模組設定
├── gradle/                   ← Gradle wrapper 與版本設定
├── gradlew / gradlew.bat     ← Gradle 執行腳本
├── proposal/                 ← 專題提案繳交區
│   └── proposal.md
├── my-topics/                ← 個人題目探索區
│   ├── topic_C112181102.md
│   ├── topic_C112181103.md
│   └── topic_C112181144.md
├── app/                      ← Android Studio 專案原始碼
│   ├── src/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── backend/                  ← AWS EC2 後端 API 程式碼，若有使用
│   └── README.md
└── docs/                     ← 期末報告與發表文件
    ├── report.md
    └── slides.pdf
```

---

## App 核心功能

- 即時港口海象觀測資料顯示
- 即時航港船班狀態查詢
- 船班關鍵字搜尋
- 搭乘風險等級與旅客提醒
- 測站切換與海況細節查看
- 淺色與深色模式切換

---

## 使用資料來源

| 資料來源 | 可應用資料 |
|---------|----------|
| 中央氣象署 Open Data | 波浪觀測、海溫、風速、風向、潮位等海象資料 |
| TDX 運輸資料流通服務 | 航港船班、船班狀態與相關交通資訊 |

---

## 專題核心技術要求

本專題預計對應課程模板要求，至少包含以下技術實作：

- [x] **多頁面與 UI 佈局**：以底部導覽切換首頁、船班、海象與設定頁面。
- [ ] **本機資料儲存**：可延伸使用 SharedPreferences 儲存深色模式或常用測站。
- [x] **網路 API 串接**：串接中央氣象署與 TDX API 取得即時資料。
- [ ] **硬體或地圖功能**：可延伸整合 GPS 或 Google Maps 顯示測站位置。
- [ ] **雲端後端整合**：目前尚未使用 AWS EC2 後端，後續可加入資料快取或推播服務。

---

## Android 專案位置

Android App 原始碼已放在 `app/` 目錄中，主要程式入口為：

```text
app/src/main/java/com/example/MainActivity.java
```

主要資料與邏輯類別包含：

- `SeaCondition.java`
- `ShipSchedule.java`
- `SeaConditionApiService.java`
- `FerryScheduleApiService.java`
- `TdxAuthService.java`
- `MarinePassengerEngine.java`

---

## 開發環境

- Android Studio
- Gradle Kotlin DSL
- Java / Kotlin Android 專案結構
- XML Layout

---

## 期末繳交文件

- 專題提案：`proposal/proposal.md`
- 個人題目探索：`my-topics/`
- 期末書面報告：`docs/report.md`
- 期末發表投影片：`docs/slides.pdf`
