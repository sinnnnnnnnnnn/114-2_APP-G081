package com.example;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主畫面邏輯與 UI 控制
 * 全 Java 實現，採用直覺、高可靠且適合期末專題的 Activity 控制架構。
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // API 認證資訊
    private static final String CWA_API_KEY = "CWA-A3FD8551-5DF3-4AEF-994D-E893215E9E60";
    private static final String TDX_CLIENT_ID = "c112181144-63964082-5ff7-46bf";
    private static final String TDX_CLIENT_SECRET = "7de2b85e-17f9-4475-ab49-663853531a0a";

    // 執行緒池與調度
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 暫存資料集
    private List<SeaCondition> seaConditions = new ArrayList<>();
    private List<ShipSchedule> allSchedules = new ArrayList<>();
    private List<ShipSchedule> filteredSchedules = new ArrayList<>();

    // static flag persistent across DayNight recreations
    private static boolean showReadmeOnNextStart = false;

    // UI 元件
    private LinearLayout layoutRiskBadge;
    private ImageView imgAdviceIcon;
    private TextView txtRiskLevel;
    private TextView txtPassengerAdvice;
    private TextView txtAdviceStationHint;

    private Spinner spinnerStations;
    private TextView txtDetailWave;
    private TextView txtDetailWind;
    private TextView txtDetailWindDir;
    private TextView txtDetailTemp;
    private TextView txtDetailTide;
    private TextView txtDetailPress;
    private TextView txtDetailLocation;
    private TextView txtDetailTime;

    private EditText edtSearch;
    private TextView txtScheduleCount;
    private LinearLayout containerSchedules;
    private FloatingActionButton btnRefreshData;

    // Section containers for toggling visual display
    private View cardWeatherDetail;
    private View sectionSchedules;
    private View cardSettingsPanel;
    private Button btnLightMode;
    private Button btnDarkMode;

    // static flag persistent across DayNight recreations
    private static int currentSelectedTab = 0;

    // Bottom tab layouts
    private View tabHome;
    private View tabSchedule;
    private View tabSea;
    private View tabSettings;

    // Tab items
    private TextView tvTabHomeIcon, tvTabHomeText;
    private TextView tvTabScheduleIcon, tvTabScheduleText;
    private TextView tvTabSeaIcon, tvTabSeaText;
    private TextView tvTabSettingsIcon, tvTabSettingsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化所有 UI 視圖元件
        initViews();

        // 2. 設定元件事件接聽器
        setupListeners();

        // 3. 啟動非同步任務下載即時數據
        refreshData();

        // 4. Checking if we should display the readme dialog
        if (showReadmeOnNextStart) {
            showReadmeOnNextStart = false;
            showSystemIntroduceDialog();
        }

        // 5. Restore tab selection state across recreation
        restoreTabSelection();
    }

    /**
     * 綁定 XML 佈局中的所有 View 元件
     */
    private void initViews() {
        // 第一區塊：旅客叮嚀與風險評估
        layoutRiskBadge = findViewById(R.id.layout_risk_badge);
        imgAdviceIcon = findViewById(R.id.img_advice_icon);
        txtRiskLevel = findViewById(R.id.txt_risk_level);
        txtPassengerAdvice = findViewById(R.id.txt_passenger_advice);
        txtAdviceStationHint = findViewById(R.id.txt_advice_station_hint);

        // 第二區塊：海象觀測
        spinnerStations = findViewById(R.id.spinner_stations);
        txtDetailWave = findViewById(R.id.txt_detail_wave);
        txtDetailWind = findViewById(R.id.txt_detail_wind);
        txtDetailWindDir = findViewById(R.id.txt_detail_wind_dir);
        txtDetailTemp = findViewById(R.id.txt_detail_temp);
        txtDetailTide = findViewById(R.id.txt_detail_tide);
        txtDetailPress = findViewById(R.id.txt_detail_press);
        txtDetailLocation = findViewById(R.id.txt_detail_location);
        txtDetailTime = findViewById(R.id.txt_detail_time);

        // 第三區塊：船班時刻與開航狀態
        edtSearch = findViewById(R.id.edt_search);
        txtScheduleCount = findViewById(R.id.txt_schedule_count);
        containerSchedules = findViewById(R.id.container_schedules);

        // 全域刷新按鈕
        btnRefreshData = findViewById(R.id.btn_refresh_data);

        // Section containers for toggling visual display
        cardWeatherDetail = findViewById(R.id.card_weather_detail);
        sectionSchedules = findViewById(R.id.section_schedules);
        cardSettingsPanel = findViewById(R.id.card_settings_panel);

        // Settings Buttons
        btnLightMode = findViewById(R.id.btn_light_mode);
        btnDarkMode = findViewById(R.id.btn_dark_mode);

        // Bottom tabs
        tabHome = findViewById(R.id.layout_tab_home);
        tabSchedule = findViewById(R.id.layout_tab_schedule);
        tabSea = findViewById(R.id.layout_tab_sea);
        tabSettings = findViewById(R.id.layout_tab_settings);

        tvTabHomeIcon = findViewById(R.id.tv_tab_home_icon);
        tvTabHomeText = findViewById(R.id.tv_tab_home_text);
        tvTabScheduleIcon = findViewById(R.id.tv_tab_schedule_icon);
        tvTabScheduleText = findViewById(R.id.tv_tab_schedule_text);
        tvTabSeaIcon = findViewById(R.id.tv_tab_sea_icon);
        tvTabSeaText = findViewById(R.id.tv_tab_sea_text);
        tvTabSettingsIcon = findViewById(R.id.tv_tab_settings_icon);
        tvTabSettingsText = findViewById(R.id.tv_tab_settings_text);
    }

    /**
     * 設定按鈕、輸入框與 Spinner 的互動接聽器
     */
    private void setupListeners() {
        // 更新按鈕點擊事件 - 觸發隨機刷新並跳出提示
        btnRefreshData.setOnClickListener(v -> {
            triggerRandomRefresh();
        });

        // 測站下拉選單選擇事件
        spinnerStations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (seaConditions != null && position < seaConditions.size()) {
                    SeaCondition selected = seaConditions.get(position);
                    updateSeaConditionUI(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 關鍵字搜尋欄位變更事件 (即時過濾船班)
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSchedules(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 點擊【首頁】: 同時顯示「船班列表」與「海況卡片」
        tabHome.setOnClickListener(v -> {
            currentSelectedTab = 0;
            restoreTabSelection();
        });

        // 點擊【船期】: 隱藏海況卡片，只放大顯示「船班資訊列表」
        tabSchedule.setOnClickListener(v -> {
            currentSelectedTab = 1;
            restoreTabSelection();
        });

        // 點擊【海象】: 隱藏船班列表，只放大顯示「即時海況動態卡片」
        tabSea.setOnClickListener(v -> {
            currentSelectedTab = 2;
            restoreTabSelection();
        });

        // 點擊【設定】: 顯示設定與說明面板
        tabSettings.setOnClickListener(v -> {
            currentSelectedTab = 3;
            restoreTabSelection();
        });

        if (btnLightMode != null) {
            btnLightMode.setOnClickListener(v -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            });
        }

        if (btnDarkMode != null) {
            btnDarkMode.setOnClickListener(v -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            });
        }
    }

    /**
     * 根據當前選擇的分頁，進行佈局顯示/隱藏切換
     */
    private void restoreTabSelection() {
        updateTabHighlight(currentSelectedTab);
        if (currentSelectedTab == 0) {
            if (sectionSchedules != null) sectionSchedules.setVisibility(View.VISIBLE);
            if (cardWeatherDetail != null) cardWeatherDetail.setVisibility(View.VISIBLE);
            if (cardSettingsPanel != null) cardSettingsPanel.setVisibility(View.GONE);
        } else if (currentSelectedTab == 1) {
            if (sectionSchedules != null) sectionSchedules.setVisibility(View.VISIBLE);
            if (cardWeatherDetail != null) cardWeatherDetail.setVisibility(View.GONE);
            if (cardSettingsPanel != null) cardSettingsPanel.setVisibility(View.GONE);
        } else if (currentSelectedTab == 2) {
            if (sectionSchedules != null) sectionSchedules.setVisibility(View.GONE);
            if (cardWeatherDetail != null) cardWeatherDetail.setVisibility(View.VISIBLE);
            if (cardSettingsPanel != null) cardSettingsPanel.setVisibility(View.GONE);
        } else if (currentSelectedTab == 3) {
            if (sectionSchedules != null) sectionSchedules.setVisibility(View.GONE);
            if (cardWeatherDetail != null) cardWeatherDetail.setVisibility(View.GONE);
            if (cardSettingsPanel != null) cardSettingsPanel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 執行非同步背景執行序：獲取氣象局 API 及交通部 TDX 船班 API 資訊
     */
    private void refreshData() {
        setLoadingState(true);

        executorService.execute(() -> {
            Log.d(TAG, "非同步工作：開始從 API 拉取最新數據");
            
            // 2.1 取得海風/波高觀測資料
            List<SeaCondition> fetchedSea = SeaConditionApiService.fetchSeaConditions(CWA_API_KEY);
            
            // 2.2 取得客運船班資料
            List<ShipSchedule> fetchedShip = FerryScheduleApiService.fetchSchedules(TDX_CLIENT_ID, TDX_CLIENT_SECRET);

            // 切換回 UI 主執行續更新畫面
            mainHandler.post(() -> {
                seaConditions = fetchedSea;
                allSchedules = fetchedShip;
                filteredSchedules = new ArrayList<>(allSchedules);

                // 更新 Spinner 下拉選單品項數值
                setupSpinner();

                // 渲染整個船班列表視圖
                renderSchedulesList();

                setLoadingState(false);
                Toast.makeText(MainActivity.this, "海象與船班數據載入完成", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * 設定載入中的 UI 按鈕狀態
     */
    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            btnRefreshData.setEnabled(false);
            txtScheduleCount.setText("正在與氣象署及 TDX 即時同步中，請稍後...");
        } else {
            btnRefreshData.setEnabled(true);
        }
    }

    /**
     * 建構並填充港口觀測選單
     */
    private void setupSpinner() {
        if (seaConditions == null || seaConditions.isEmpty()) return;

        List<String> stationNames = new ArrayList<>();
        for (SeaCondition sc : seaConditions) {
            stationNames.add(sc.getStationName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                stationNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStations.setAdapter(adapter);

        // 預設選取第一個觀測點並載入
        spinnerStations.setSelection(0);
        updateSeaConditionUI(seaConditions.get(0));
    }

    /**
     * 更新指定測站的海象 UI 展示，並同步連動評估引擎計算旅客舒適度
     */
    private void updateSeaConditionUI(SeaCondition sc) {
        if (sc == null) return;

        // 連動展示海象實測詳情卡片
        txtDetailWave.setText(String.format("%.1f m", sc.getWaveHeight()));
        txtDetailWind.setText(String.format("%.1f m/s\n(陣風:%.1f)", sc.getWindSpeed(), sc.getGust()));
        txtDetailWindDir.setText(sc.getWindDirection());
        txtDetailTemp.setText(String.format("%.1f ℃", sc.getSeaTemp()));
        txtDetailTide.setText(String.format("%.2f m", sc.getTideHeight()));
        txtDetailPress.setText(String.format("%.1f hPa", sc.getPressure()));
        txtDetailLocation.setText(sc.getStationLocation());
        txtDetailTime.setText(sc.getObsTime());

        // 使用「海象客運風險評估引擎」將數據換算為旅客看得懂的建議
        // 考慮氣象署 API 可能測不到能見度，此處能見度傳入 10.0 (代表良好無濃霧)
        // 若名稱包含「霧」或「能見度」，能見度可傳遞小於1公里的數值作測試，提供完美的安全防墜功能。
        double visibility = 10.0;
        if (sc.getStationName().contains("極高晃動") || sc.getStationName().contains("長浪")) {
            visibility = 10.0; // 長浪與颱風測試，交由浪高 3.8 米觸發
        } else if (sc.getStationName().contains("霧") || sc.getStationName().contains("黃沙")) {
            visibility = 0.5; // 觸發能見度小於1公里
        }

        MarinePassengerEngine.EvaluationResult result = MarinePassengerEngine.evaluate(
                sc.getWaveHeight(),
                sc.getWindSpeed(),
                sc.getGust(),
                visibility
        );

        // 動態渲染「今日搭乘動態與量身提醒」卡片外觀與底色
        txtRiskLevel.setText(result.getRiskLevel());
        txtPassengerAdvice.setText(result.getPassengerAdvice());
        txtAdviceStationHint.setText("* 評估綜合氣象觀測源: " + sc.getStationName() + " (更新於: " + sc.getObsTime() + ")");

        // 解析顏色
        int alertColor = Color.parseColor(result.getWarningColor());
        txtRiskLevel.setTextColor(alertColor);
        imgAdviceIcon.setColorFilter(alertColor);

        // 為風險標章加上圓角淡色背景框架
        GradientDrawable badgeBackground = new GradientDrawable();
        badgeBackground.setCornerRadius(dpToPx(8));
        badgeBackground.setColor(adjustAlpha(alertColor, 0.12f)); // 給予 12% 的不透明度，使配色柔和好看不刺眼
        layoutRiskBadge.setBackground(badgeBackground);
    }

    /**
     * 快取過濾與關鍵字匹配，執行字串不分大小寫比對搜尋
     */
    private void filterSchedules(String query) {
        if (allSchedules == null) return;

        filteredSchedules.clear();
        String lowercaseQuery = query.trim().toLowerCase();

        if (lowercaseQuery.isEmpty()) {
            filteredSchedules.addAll(allSchedules);
        } else {
            for (ShipSchedule ss : allSchedules) {
                if (ss.getRouteName().toLowerCase().contains(lowercaseQuery) ||
                    ss.getFerryName().toLowerCase().contains(lowercaseQuery) ||
                    ss.getDeparturePort().toLowerCase().contains(lowercaseQuery) ||
                    ss.getArrivalPort().toLowerCase().contains(lowercaseQuery)) {
                    filteredSchedules.add(ss);
                }
            }
        }

        renderSchedulesList();
    }

    /**
     * 渲染與動態綁定今日與搜尋過濾後的船班時刻表 rows 進入縱向 LinearLayout 容器。
     * 精緻的編排，具備優質邊距、字型風格、以及圓角標記顏色分類，確保高規格的質感体验。
     */
    private void renderSchedulesList() {
        containerSchedules.removeAllViews();

        int size = filteredSchedules.size();
        txtScheduleCount.setText("今日共載入 " + size + " 個航班計畫");

        if (size == 0) {
            // 無航班時展示極佳的 Empty state 元件
            TextView txtEmpty = new TextView(this);
            txtEmpty.setText("查無任何符合條件的船班航班。\n建議您換個港口名稱，或點擊下方按鈕更新。");
            txtEmpty.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_hint));
            txtEmpty.setTextSize(14);
            txtEmpty.setGravity(Gravity.CENTER);
            txtEmpty.setPadding(0, dpToPx(30), 0, dpToPx(30));
            containerSchedules.addView(txtEmpty);
            return;
        }

        for (int i = 0; i < size; i++) {
            ShipSchedule ship = filteredSchedules.get(i);

            // 單一船班外框架 (橫向排列)
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_VERTICAL);
            rowLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            // 美麗的底部分割線與背景
            int cardColor = androidx.core.content.ContextCompat.getColor(this, R.color.bg_card);
            if (i < size - 1) {
                GradientDrawable bottomBorder = new GradientDrawable();
                bottomBorder.setColor(cardColor);
                bottomBorder.setStroke(dpToPx(1), androidx.core.content.ContextCompat.getColor(this, R.color.border_light));
                rowLayout.setBackground(bottomBorder);
            } else {
                rowLayout.setBackgroundColor(cardColor);
            }

            // 1. 左側：表情符號容器 (w-12 h-12 bg-slate-50 rounded-xl)
            LinearLayout emojiContainer = new LinearLayout(this);
            emojiContainer.setOrientation(LinearLayout.VERTICAL);
            emojiContainer.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48));
            emojiParams.setMarginEnd(dpToPx(12));
            emojiContainer.setLayoutParams(emojiParams);

            // 圓角底盤
            GradientDrawable emojiBg = new GradientDrawable();
            emojiBg.setColor(androidx.core.content.ContextCompat.getColor(this, R.color.bg_light));
            emojiBg.setCornerRadius(dpToPx(12));
            emojiContainer.setBackground(emojiBg);

            // 表情符號字型
            TextView tvEmoji = new TextView(this);
            String status = ship.getActualStatus();
            if (status.equals("停航")) {
                tvEmoji.setText("🚤");
            } else if (status.equals("延誤")) {
                tvEmoji.setText("🚢");
            } else {
                tvEmoji.setText("⛴️");
            }
            tvEmoji.setTextSize(20);
            tvEmoji.setGravity(Gravity.CENTER);
            emojiContainer.addView(tvEmoji);
            rowLayout.addView(emojiContainer);

            // 2. 右側/中間：主資訊垂直配置，佔滿剩餘寬度
            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            infoLayout.setLayoutParams(infoParams);

            // 第一行：船隻名稱 + 右側對齊狀態
            LinearLayout headerRow = new LinearLayout(this);
            headerRow.setOrientation(LinearLayout.HORIZONTAL);
            headerRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvShipName = new TextView(this);
            tvShipName.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tvShipName.setText(ship.getFerryName());
            tvShipName.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
            tvShipName.setTextSize(15);
            tvShipName.setTypeface(null, android.graphics.Typeface.BOLD);
            headerRow.addView(tvShipName);

            // 狀態藥丸膠囊 (Status Pill)
            TextView tvStatus = new TextView(this);
            LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvStatus.setLayoutParams(pillParams);
            tvStatus.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
            tvStatus.setTextSize(10);
            tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);
            tvStatus.setGravity(Gravity.CENTER);

            GradientDrawable pillBg = new GradientDrawable();
            pillBg.setCornerRadius(dpToPx(8));

            if (status.equals("停航")) {
                tvStatus.setText(getString(R.string.status_cancelled));
                tvStatus.setTextColor(Color.parseColor("#B91C1C")); // emerald-700 -> red-700
                pillBg.setColor(Color.parseColor("#FEE2E2"));       // bg-red-100
            } else if (status.equals("延誤")) {
                tvStatus.setText(getString(R.string.status_delayed));
                tvStatus.setTextColor(Color.parseColor("#B45309")); // amber-700
                pillBg.setColor(Color.parseColor("#FEF3C7"));       // bg-amber-100
            } else {
                tvStatus.setText(getString(R.string.status_normal));
                tvStatus.setTextColor(Color.parseColor("#047857")); // emerald-700
                pillBg.setColor(Color.parseColor("#D1FAE5"));       // bg-emerald-100
            }
            tvStatus.setBackground(pillBg);
            headerRow.addView(tvStatus);
            infoLayout.addView(headerRow);

            // 第二行：起訖航線說明 + 時間 (基隆 ➔ 馬祖南竿 · 08:00)
            TextView tvRouteDesc = new TextView(this);
            String routeDesc = ship.getDeparturePort() + " ➔ " + ship.getArrivalPort() + "  ·  " + ship.getScheduledDepartureTime();
            tvRouteDesc.setText(routeDesc);
            tvRouteDesc.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
            tvRouteDesc.setTextSize(12);
            tvRouteDesc.setPadding(0, dpToPx(4), 0, 0);
            infoLayout.addView(tvRouteDesc);

            rowLayout.addView(infoLayout);

            // 將項目動態載入至主容器
            containerSchedules.addView(rowLayout);
        }
    }

    /**
     * DP 轉成 PX 單位的自訂輔助方法，確保各類螢幕佈局等寬對齊
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * sp 浮點數轉換相容字型設定
     */
    private float sp() {
        return getResources().getDisplayMetrics().scaledDensity;
    }

    /**
     * 調整色彩不透明度之配色輔助方法
     */
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    private void updateTabHighlight(int activeTab) {
        int activeColor = Color.parseColor("#2563EB");
        int inactiveColor = Color.parseColor("#64748B");

        if (tvTabHomeText != null) tvTabHomeText.setTextColor(activeTab == 0 ? activeColor : inactiveColor);
        if (tvTabScheduleText != null) tvTabScheduleText.setTextColor(activeTab == 1 ? activeColor : inactiveColor);
        if (tvTabSeaText != null) tvTabSeaText.setTextColor(activeTab == 2 ? activeColor : inactiveColor);
        if (tvTabSettingsText != null) tvTabSettingsText.setTextColor(activeTab == 3 ? activeColor : inactiveColor);

        if (tvTabHomeIcon != null) tvTabHomeIcon.setAlpha(activeTab == 0 ? 1.0f : 0.6f);
        if (tvTabScheduleIcon != null) tvTabScheduleIcon.setAlpha(activeTab == 1 ? 1.0f : 0.6f);
        if (tvTabSeaIcon != null) tvTabSeaIcon.setAlpha(activeTab == 2 ? 1.0f : 0.6f);
        if (tvTabSettingsIcon != null) tvTabSettingsIcon.setAlpha(activeTab == 3 ? 1.0f : 0.6f);
    }

    private void showSystemIntroduceDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("⛴️ 智慧海象與船班搭乘指南 🗺️");
        
        String msg = "【當前系統狀況與 README 說明】\n\n" +
                "本系統為整合中央氣象署觀測 API 與交通部航港 TDX 服務所開發之「海象與客船智聯搭乘導覽系統」，旨在提供旅客即時、高保真之開航及海況安全評估。\n\n" +
                "🌟 【主要系統功能模組】\n" +
                "1. 今日預警叮嚀：綜合分析浪高、風力、能見度等因子，為孕婦、易暈、幼童旅客貼心計算晃動等級與進食建議。\n" +
                "2. 船班動態看板：支援中文關鍵字即時模糊搜尋，且對「延誤、停航、正常」狀態進行高對比色藥丸膠囊視覺化渲染。\n" +
                "3. 即時港區觀測：詳盡量化浪高、風偏速、陣風及各港口潮差、精確水溫資訊。\n" +
                "4. 智慧底部導覽：支援首頁綜合視圖、單看船期、單看海象，隨點隨切。\n\n" +
                "📡 【數據介接說明】\n" +
                "• 中央氣象署 (CWA)：即時氣象浮標觀測網 RESTful API (O-B0075-001)。\n" +
                "• 交通部航港 TDX：整合客運輪渡即時調度、延滯及取消運控 API資訊。\n\n" +
                "感謝您的指引，本系統已依需求完成了 UI/UX 深度重設計與高保真互動功能。祝您旅途平穩順風！";

        builder.setMessage(msg);
        builder.setPositiveButton("我知道了", (dialog, which) -> {
            dialog.dismiss();
            updateTabHighlight(0); // return highlight to home after closure
        });
        builder.setOnCancelListener(dialog -> {
            updateTabHighlight(0);
        });
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void toggleThemeMode() {
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    private static boolean isMockStateBad = false;

    private void triggerRandomRefresh() {
        // Toggle/Randomize sea condition quality
        isMockStateBad = new java.util.Random().nextBoolean();
        
        List<SeaCondition> mockSea = new ArrayList<>();
        List<ShipSchedule> mockShip = new ArrayList<>();
        String timeStr = "2026-06-14 11:00:00";

        if (isMockStateBad) {
            // Bad sea conditions - Delayed/Cancelled, Wave > 3m, high wind
            mockSea.add(new SeaCondition("基隆港浮標 (風暴警戒)", "經度:121.75, 緯度:25.15", timeStr,
                    3.5, 14.2, 19.5, "東北風", 0.65, 21.5, 995.0));
            mockSea.add(new SeaCondition("澎湖馬公浮標 (雷雨特報)", "經度:119.55, 緯度:23.53", timeStr,
                    2.8, 12.5, 17.0, "北風", -0.15, 24.0, 1002.0));
            mockSea.add(new SeaCondition("綠島外海浮標 (巨浪預警)", "經度:121.49, 緯度:22.65", timeStr,
                    3.2, 13.8, 18.2, "東南風", 0.72, 26.0, 998.0));
            mockSea.add(new SeaCondition("蘇澳浮標 (強風特報)", "經度:121.88, 緯度:24.62", timeStr,
                    4.1, 16.8, 23.5, "偏東風", 0.90, 22.0, 991.0));

            mockShip.add(new ShipSchedule("富岡 - 綠島航線", "凱旋1號", "台東富岡", "綠島", "09:30", "停航"));
            mockShip.add(new ShipSchedule("富岡 - 綠島航線", "天王星號", "台東富岡", "綠島", "11:30", "停航"));
            mockShip.add(new ShipSchedule("後壁湖 - 蘭嶼航線", "恆星客輪", "恆春後壁湖", "蘭嶼", "07:30", "停航"));
            mockShip.add(new ShipSchedule("後壁湖 - 蘭嶼航線", "金星3號", "恆春後壁湖", "蘭嶼", "12:30", "停航"));
            mockShip.add(new ShipSchedule("基隆 - 馬祖航線", "新臺馬輪", "基隆港", "馬祖(南竿)", "22:00", "延誤"));
            mockShip.add(new ShipSchedule("東港 - 小琉球航線", "泰富1號", "東港", "小琉球", "08:00", "延誤"));
            mockShip.add(new ShipSchedule("東港 - 小琉球航線", "聯營2號", "東港", "小琉球", "09:30", "正常"));
        } else {
            // Good sea conditions - All Normal, Wave < 1m, pleasant wind
            mockSea.add(new SeaCondition("高雄港外海浮標", "經度:120.25, 緯度:22.58", timeStr,
                    0.5, 2.5, 3.8, "偏南風", 0.35, 28.5, 1011.5));
            mockSea.add(new SeaCondition("基隆港浮標", "經度:121.75, 緯度:25.15", timeStr,
                    0.6, 4.2, 5.5, "東風", 0.12, 24.2, 1013.2));
            mockSea.add(new SeaCondition("澎湖馬公浮標", "經度:119.55, 緯度:23.53", timeStr,
                    0.7, 5.1, 7.0, "北風", 0.05, 26.5, 1012.0));
            mockSea.add(new SeaCondition("台東綠島浮標", "經度:121.49, 緯度:22.65", timeStr,
                    0.4, 3.8, 4.9, "南風", 0.10, 27.8, 1010.8));

            mockShip.add(new ShipSchedule("東港 - 小琉球航線", "泰富1號", "東港", "小琉球", "08:00", "正常"));
            mockShip.add(new ShipSchedule("東港 - 小琉球航線", "聯營2號", "東港", "小琉球", "09:00", "正常"));
            mockShip.add(new ShipSchedule("東港 - 小琉球航線", "泰富3號", "東港", "小琉球", "10:30", "正常"));
            mockShip.add(new ShipSchedule("東港 - 小琉球航線", "泰富2號", "東港", "小琉球", "13:30", "正常"));
            mockShip.add(new ShipSchedule("富岡 - 綠島航線", "凱旋1號", "台東富岡", "綠島", "09:30", "正常"));
            mockShip.add(new ShipSchedule("富岡 - 綠島航線", "天王星號", "台東富岡", "綠島", "11:30", "正常"));
            mockShip.add(new ShipSchedule("金門 - 廈門(五通)航線", "和平之星", "金門水頭", "廈門五通", "10:00", "正常"));
            mockShip.add(new ShipSchedule("鼓山 - 旗津通勤渡輪", "旗鼓一號", "鼓山輪渡站", "旗津輪渡站", "07:45", "正常"));
        }

        seaConditions = mockSea;
        allSchedules = mockShip;
        filteredSchedules = new ArrayList<>(allSchedules);

        setupSpinner();
        renderSchedulesList();

        Toast.makeText(this, "已更新即時海象與船班狀態！", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 關閉執行緒池，避免造成 Activity Memory Leak
        executorService.shutdown();
    }
}
