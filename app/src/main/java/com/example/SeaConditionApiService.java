package com.example;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * 處理中央氣象署海況觀測資料 API 串接與解析
 */
public class SeaConditionApiService {
    private static final String TAG = "SeaConditionApiService";
    private static final String API_URL = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/O-B0075-001?Authorization=";

    /**
     * 從中央氣象署 API 取得即時海況資料
     * 必在非主執行緒(Background Thread)中呼叫
     */
    public static List<SeaCondition> fetchSeaConditions(String apiKey) {
        List<SeaCondition> list = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        String url = API_URL + apiKey;

        Log.d(TAG, "開始請求氣象署海況觀測資料: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e(TAG, "氣象署 API 回傳失敗 Code: " + response.code());
                return getFallbackSeaConditions("網路錯誤 " + response.code());
            }

            String responseData = response.body().string();
            JSONObject root = new JSONObject(responseData);
            
            String success = root.optString("success", root.optString("Success", "false"));
            if (!"true".equalsIgnoreCase(success)) {
                Log.e(TAG, "氣象署 API 回傳 success != true，回應: " + responseData.substring(0, Math.min(responseData.length(), 300)));
                return getFallbackSeaConditions("API 回傳失敗");
            }

            JSONObject records = root.optJSONObject("records");
            if (records == null) {
                records = root.optJSONObject("Records");
            }
            if (records == null) {
                Log.e(TAG, "氣象署 API 缺少 Records 節點");
                return getFallbackSeaConditions("API 格式異動");
            }

            JSONArray locations = null;
            JSONObject seaSurfaceObs = records.optJSONObject("SeaSurfaceObs");
            if (seaSurfaceObs != null) {
                locations = seaSurfaceObs.optJSONArray("Location");
            }
            if (locations == null) {
                locations = records.optJSONArray("location");
            }
            if (locations == null) {
                Log.e(TAG, "氣象署 API 找不到 Location/location 陣列");
                return getFallbackSeaConditions("API 格式異動");
            }

            for (int i = 0; i < locations.length(); i++) {
                try {
                    JSONObject loc = locations.getJSONObject(i);
                    JSONObject station = loc.optJSONObject("Station");
                    String stationId = station != null ? station.optString("StationID", "未知測站") : loc.optString("locationName", "未知測站");
                    String stationName = station != null
                            ? station.optString("StationName", "海象測站 " + stationId)
                            : loc.optString("locationName", "海象測站 " + stationId);
                    
                    // 經緯度座標
                    String lat = loc.optString("latitude", station != null ? station.optString("StationLatitude", "-") : "-");
                    String lon = loc.optString("longitude", station != null ? station.optString("StationLongitude", "-") : "-");
                    String stationLocation = "測站代碼:" + stationId + ", 經度:" + lon + ", 緯度:" + lat;

                    // 觀測時間
                    JSONObject timeObj = loc.optJSONObject("time");
                    String obsTime = timeObj != null ? timeObj.optString("obsTime", "-") : "-";

                    double waveHeight = 0.0;
                    double windSpeed = 0.0;
                    double gust = 0.0;
                    String windDir = "無";
                    double tideHeight = 0.0;
                    double seaTemp = 0.0;
                    double pressure = 0.0;

                    JSONObject latestElements = null;
                    JSONObject stationObsTimes = loc.optJSONObject("StationObsTimes");
                    if (stationObsTimes != null) {
                        JSONArray obsArray = stationObsTimes.optJSONArray("StationObsTime");
                        if (obsArray != null && obsArray.length() > 0) {
                            JSONObject latestObs = obsArray.getJSONObject(0);
                            obsTime = latestObs.optString("DateTime", obsTime);
                            latestElements = latestObs.optJSONObject("WeatherElements");
                        }
                    }

                    if (latestElements != null) {
                        waveHeight = parseDouble(latestElements.optString("WaveHeight"));
                        tideHeight = parseDouble(latestElements.optString("TideHeight"));
                        seaTemp = parseDouble(latestElements.optString("SeaTemperature"));
                        pressure = parseDouble(latestElements.optString("StationPressure"));

                        JSONObject anemometer = latestElements.optJSONObject("PrimaryAnemometer");
                        if (anemometer != null) {
                            windSpeed = parseDouble(anemometer.optString("WindSpeed"));
                            gust = parseDouble(anemometer.optString("MaximumWindSpeed"));
                            windDir = anemometer.optString("WindDirectionDescription", anemometer.optString("WindDirection", "無"));
                        }
                    } else {
                        JSONArray elements = loc.optJSONArray("weatherElement");
                        if (elements == null) {
                            elements = loc.optJSONArray("WeatherElement");
                        }
                        if (elements == null) {
                            Log.w(TAG, "測站 " + stationId + " 缺少 WeatherElements，略過");
                            continue;
                        }

                        for (int j = 0; j < elements.length(); j++) {
                            JSONObject element = elements.getJSONObject(j);
                            String elName = element.optString("elementName", "").toLowerCase();
                            String elValStr = element.optString("elementValue", "0.0");
                            double elValue = parseDouble(elValStr);

                            if (elName.contains("wave") || elName.contains("wvheight") || elName.contains("波高") || elName.contains("浪高")) {
                                waveHeight = elValue;
                            } else if (elName.contains("windspeed") || elName.contains("wind_speed") || elName.contains("風速") || elName.contains("speed")) {
                                windSpeed = elValue;
                            } else if (elName.contains("gust") || elName.contains("陣風")) {
                                gust = elValue;
                            } else if (elName.contains("dir") || elName.contains("風向") || elName.contains("wd")) {
                                windDir = elValStr != null ? elValStr : "無";
                            } else if (elName.contains("tide") || elName.contains("潮位") || elName.contains("潮高")) {
                                tideHeight = elValue;
                            } else if (elName.contains("temp") || elName.contains("溫度") || elName.contains("sea_temp")) {
                                seaTemp = elValue;
                            } else if (elName.contains("press") || elName.contains("氣壓") || elName.contains("bar")) {
                                pressure = elValue;
                            }
                        }
                    }

                    // 判定資料合理性，若全為 0 則可能是該站目前沒有定時報，給予合理預設，避免旅客看到空值
                    if (waveHeight == 0.0 && windSpeed == 0.0 && seaTemp == 0.0) {
                        continue; // 跳過無效測站
                    }

                    SeaCondition condition = new SeaCondition(
                        stationName, stationLocation, obsTime, 
                        waveHeight, windSpeed, gust, windDir, tideHeight, seaTemp, pressure
                    );
                    list.add(condition);
                } catch (Exception e) {
                    Log.e(TAG, "解析單一測站資料錯誤: " + e.getMessage());
                }
            }

            if (list.isEmpty()) {
                Log.w(TAG, "解析後的有效氣象觀測列表為空，提供預設資料");
                return getFallbackSeaConditions("無觀測報");
            }

            Log.i(TAG, "成功解析 CWA 測站資料數量: " + list.size());
            return list;

        } catch (Exception e) {
            Log.e(TAG, "請求或解析氣象署 API 出現異常", e);
            return getFallbackSeaConditions("系統異常: " + e.getMessage());
        }
    }

    private static double parseDouble(String value) {
        if (value == null) return 0.0;
        String normalized = value.trim();
        if (normalized.isEmpty()
                || normalized.equals("-")
                || normalized.equalsIgnoreCase("None")
                || normalized.equalsIgnoreCase("NaN")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 建立本地預設/模擬海象資料 (在網路失敗或 API 金鑰失效時提供，確保 App 在離線時仍能向旅客展示完整的防暈與提示操作)
     */
    public static List<SeaCondition> getFallbackSeaConditions(String reason) {
        Log.i(TAG, "載入本地防墜海況觀測資料集。原因: " + reason);
        List<SeaCondition> list = new ArrayList<>();
        String nowTime = "2026-06-14 10:00:00"; // 依系統目前時間約略模擬

        // 高雄港 (良好)
        list.add(new SeaCondition(
                "高雄港外海浮標", "經度:120.25, 緯度:22.58 (模擬資料: " + reason + ")", nowTime,
                0.6, 3.4, 4.8, "偏南風", 0.42, 28.5, 1010.5
        ));

        // 基隆港 (輕度晃動)
        list.add(new SeaCondition(
                "基隆港浮標", "經度:121.75, 緯度:25.15 (模擬資料: " + reason + ")", nowTime,
                1.1, 7.2, 9.5, "東北風", 0.15, 22.1, 1012.3
        ));

        // 澎湖馬公 (高度晃動 - 浪大)
        list.add(new SeaCondition(
                "澎湖馬公浮標", "經度:119.55, 緯度:23.53 (模擬資料: " + reason + ")", nowTime,
                2.2, 12.1, 16.2, "北風", -0.22, 25.8, 1011.0
        ));

        // 綠島外海 (舒適波動)
        list.add(new SeaCondition(
                "台東綠島浮標", "經度:121.49, 緯度:22.65 (模擬資料: " + reason + ")", nowTime,
                0.8, 4.2, 5.5, "東南風", 0.12, 27.2, 1009.8
        ));
        
        // 蘇澳外海 (極高晃動 - 警戒)
        list.add(new SeaCondition(
                "蘇澳浮標 (颱風環流/長浪測試)", "經度:121.88, 緯度:24.62 (模擬資料: " + reason + ")", nowTime,
                3.8, 16.5, 22.1, "東北風", 0.82, 23.5, 998.5
        ));

        return list;
    }
}
