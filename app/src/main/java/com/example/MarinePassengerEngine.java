package com.example;

/**
 * 海象連動搭乘風險評估引擎
 * 將氣象數值轉化為一般旅客易懂的「乘船舒適度與晃動預警」及關懷叮嚀。
 */
public class MarinePassengerEngine {

    public static class EvaluationResult {
        private final String riskLevel;       // 乘船風險等級/舒適度 (例如：舒適、輕度晃動、高度晃動、建議不要搭乘/注意停航)
        private final String warningColor;     // 視覺標示顏色 (Hex string, 例如: #4CAF50, #FFC107, #FF9800, #F44336)
        private final String passengerAdvice;  // 貼心叮嚀與防暈乘客關懷文字

        public EvaluationResult(String riskLevel, String warningColor, String passengerAdvice) {
            this.riskLevel = riskLevel;
            this.warningColor = warningColor;
            this.passengerAdvice = passengerAdvice;
        }

        public String getRiskLevel() { return riskLevel; }
        public String getWarningColor() { return warningColor; }
        public String getPassengerAdvice() { return passengerAdvice; }
    }

    /**
     * 依據海況觀測數值評估搭乘狀況
     * 
     * @param waveHeight 浪高 (公尺)
     * @param windSpeed  風速 (m/s)
     * @param gust       陣風 (m/s)
     * @param visibility 能見度 (公里, 預設可用 10.0 代表良好)
     * @return 評估結果數據
     */
    public static EvaluationResult evaluate(double waveHeight, double windSpeed, double gust, double visibility) {
        // 規則 1: 浪高 > 3.0 公尺 或 風速 > 15.0 m/s 或 陣風 > 20.0 m/s -> 停航風險極高
        if (waveHeight > 3.0 || windSpeed > 15.0 || gust > 20.0) {
            return new EvaluationResult(
                "建議不要搭乘 / 注意停航 (停航風險極高)",
                "#F44336", // 紅色
                "【重要警示】目前海域出現大浪或強風，極可能隨時宣布停航或已停航。建議您在出發前「務必」先行取消非必要行程，並立即聯繫船公司確認退票或辦理改期手續。"
            );
        }

        // 規則 3: 能見度 < 1.0 公里 -> 濃霧影響
        if (visibility > 0 && visibility < 1.0) {
            return new EvaluationResult(
                "濃霧影響 / 延誤預警 (高度晃動)",
                "#FF9800", // 橙色
                "【能見度極低】港區及周邊海域受濃霧籠罩（能見度不足1公里）。船隻航行將降速前進，船班極有可能因而延遲或推遲起航。請旅客前往港口前，密切注意廣播與港務更新資訊。"
            );
        }

        // 規則 2: 浪高介於 1.5 至 3.0 公尺 之間 或 風速 > 10.0 m/s -> 海象不佳，高度晃動
        if ((waveHeight >= 1.5 && waveHeight <= 3.0) || windSpeed > 10.0 || gust > 15.0) {
            return new EvaluationResult(
                "正常開航但可能高度晃動 (高度眩暈預警)",
                "#FFC107", // 黃色
                "【強烈晃動提醒】雖然船班正常開航，但波浪與風力已使艙體預期產生劇烈搖晃。強烈建議「容易暈船」之旅客，在登船前 30 分鐘先服用暈船藥！乘船時請盡量選擇「船尾中軸線」座位，此處擺幅最小，可減輕不適。"
            );
        }

        // 規則 5: 輕度晃動 (例如 浪高 0.8 到 1.5 公尺)
        if (waveHeight >= 0.8 && waveHeight < 1.5) {
            return new EvaluationResult(
                "適宜航行 / 輕度晃動",
                "#8BC34A", // 淺綠
                "【輕微搖晃提醒】目前海上風浪微增，航行中可能伴隨輕微起伏，整體搭乘狀況依然穩定。建議易感不適的旅客仍可選擇中後排靠窗位置，閉目養神或遠眺地平線。"
            );
        }

        // 規則 4: 海象良好 -> 適宜航行
        return new EvaluationResult(
            "適宜航行 (海象平穩)",
            "#4CAF50", // 綠色
            "【海象良好】今日海象格外平穩、微風無浪，是極佳的乘船天氣！祝您擁有一段舒適愜意且愉快的海上旅程！"
        );
    }
}
