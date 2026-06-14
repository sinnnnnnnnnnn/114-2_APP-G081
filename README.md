# 114-2 APP 開發 - 期末專題 (海事與海洋應用)

## 專題概述
本專案為[「114-2 APP 開發」](https://github.com/pychang-ai/114-2_APPDEV)課程的期末專題模板。本學期的專題主題為**「海事與海洋」**，請各組發揮創意，結合本學期學到的 Android 開發技術（UI 佈局、Room 資料庫、網路 API 串接、Google Maps 定位等）與 AWS 雲端後端服務，開發一款具備實際應用價值的 Android App。

---

## 🚀 組長第一步：如何使用此模板
1. 點選本頁面右上角的 **「Use this template」** > **「Create a new repository」**。
2. Repository name 請命名為：`114-2_APP-G01`（請將 `01` 替換為你們的組別編號）。
3. 隱私權設定請保持 **Public**。
4. 建立後，進入 Settings > Collaborators，邀請以下成員加入（權限設為 Write）：
   * 所有小組成員
   * 授課教師與助教帳號

---

## 📁 專案資料夾結構

請依照以下結構存放你們的專題檔案，以利期末評分與 AI 自動化貢獻度分析：

```text
├── README.md                 ← 本說明檔（各組請替換為你們的 App 介紹）
├── proposal/                 ← 專題提案繳交區
│   └── proposal.md           
├── my-topics/                ← 個人題目探索區 (第 4 週繳交)
│   ├── topic_A11218001.md
│   └── topic_A11218002.md
├── app/                      ← Android Studio 專案原始碼放這裡
│   ├── src/
│   ├── build.gradle
│   └── ...
├── backend/                  ← AWS EC2 後端 API 程式碼 (若有使用)
│   ├── app.py / index.js
│   └── requirements.txt
└── docs/                     ← 期末報告與發表文件
    ├── report.md             ← 期末書面報告
    └── slides.pdf            ← 期末發表投影片
```

---

## 💡 專題題目方向參考

各組可從以下方向延伸，或自行發想與「海事、海洋、漁業、航運、水上活動」相關的題目：

**1. 航運物流與船舶追蹤**
* **港口船舶即時動態 App**：串接 AIS 資料，在地圖上顯示周邊船舶位置，並可推播特定航班進港通知。
* **碼頭貨櫃查詢導航**：結合 Google Maps，引導貨車司機前往正確的碼頭與貨櫃儲位。

**2. 海洋休閒與水上安全**
* **衝浪/海釣海象預報 App**：整合氣象署資料，顯示潮汐、風浪，並可設定「最佳浪況」推播提醒。
* **海上緊急求救定位 App**：一鍵獲取當前精確 GPS 座標，透過後端 API 快速發送給救援單位。

**3. 海洋環境保護與社群**
* **淨灘熱點標記與揪團 App**：發現海洋廢棄物時拍照標記 GPS 上傳，並可在 App 內發起淨灘活動。
* **海洋生物圖鑑與紀錄 App**：內建 Room 資料庫圖鑑，使用者可拍照上傳自己遇見的海洋生物並在地圖上打卡。

**4. 漁業資訊與市場**
* **當日漁市報價走勢 App**：串接漁產品交易行情 API，展示今日價格並繪製歷史價格走勢圖。

---

## 🔗 推薦公開資料來源 (Open Data)

開發 App 需要真實資料，請善用以下免費 API 與資料集：

| 資料來源 | 網址 | 可應用資料 |
|---------|------|----------|
| 政府資料開放平臺 | [data.gov.tw](https://data.gov.tw/) | 漁產品交易行情、海岸淨灘資訊 |
| 中央氣象署 | [opendata.cwa.gov.tw](https://opendata.cwa.gov.tw/) | 潮汐預報、近海海面天氣預報、波浪觀測 |
| 交通部航港局 | [motcmpb.gov.tw](https://www.motcmpb.gov.tw/) | 船舶進出港即時資訊、航班資訊 |
| MarineTraffic | [marinetraffic.com](https://www.marinetraffic.com/) | 全球船舶 AIS 軌跡資料 (需申請 API) |

---

## 🎯 專題核心技術要求 (評分標準)

為確保專題具備一定的技術深度，你們的 App 必須**至少包含以下 4 項技術實作**：

- [ ] **多頁面與 UI 佈局**：使用 RecyclerView 呈現動態列表，並有流暢的 Activity/Fragment 切換。
- [ ] **本機資料儲存**：使用 Room Database 或 SharedPreferences 儲存使用者設定或最愛清單。
- [ ] **網路 API 串接**：使用 Retrofit 抓取外部 JSON 資料並正確解析呈現。
- [ ] **硬體或地圖功能**：成功呼叫相機、GPS 定位或整合 Google Maps API。
- [ ] **雲端後端整合**：(加分項) 成功串接部署於 AWS EC2 的自建後端 API 或資料庫。

## 📅 重要時程
* **第 3 週**：組長建立此 Repo，並加入所有組員。
* **第 4 週**：每位組員於 `my-topics/` 提出個人構想。
* **第 5 週**：小組討論決議，完成並繳交 `proposal/proposal.md`。
* **第 17 週**：繳交期末報告 `docs/report.md` 與投影片。
* **第 18 週**：期末實機 Demo 與口頭發表。
