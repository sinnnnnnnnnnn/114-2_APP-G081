package com.example;

/**
 * 海況觀測資料模型
 */
public class SeaCondition {
    private String stationName;
    private String stationLocation;
    private String obsTime;
    private double waveHeight; // 浪高 (公尺)
    private double windSpeed;  // 風速 (m/s)
    private double gust;       // 陣風 (m/s)
    private String windDirection; // 風向
    private double tideHeight; // 潮高 (公尺)
    private double seaTemp;    // 海溫 (℃)
    private double pressure;   // 氣壓 (hPa)

    public SeaCondition() {
        this.stationName = "無資料";
        this.stationLocation = "未知";
        this.obsTime = "-";
        this.waveHeight = 0.0;
        this.windSpeed = 0.0;
        this.gust = 0.0;
        this.windDirection = "無";
        this.tideHeight = 0.0;
        this.seaTemp = 0.0;
        this.pressure = 0.0;
    }

    public SeaCondition(String stationName, String stationLocation, String obsTime, 
                        double waveHeight, double windSpeed, double gust, 
                        String windDirection, double tideHeight, double seaTemp, double pressure) {
        this.stationName = stationName;
        this.stationLocation = stationLocation;
        this.obsTime = obsTime;
        this.waveHeight = waveHeight;
        this.windSpeed = windSpeed;
        this.gust = gust;
        this.windDirection = windDirection;
        this.tideHeight = tideHeight;
        this.seaTemp = seaTemp;
        this.pressure = pressure;
    }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStationLocation() { return stationLocation; }
    public void setStationLocation(String stationLocation) { this.stationLocation = stationLocation; }

    public String getObsTime() { return obsTime; }
    public void setObsTime(String obsTime) { this.obsTime = obsTime; }

    public double getWaveHeight() { return waveHeight; }
    public void setWaveHeight(double waveHeight) { this.waveHeight = waveHeight; }

    public double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(double windSpeed) { this.windSpeed = windSpeed; }

    public double getGust() { return gust; }
    public void setGust(double gust) { this.gust = gust; }

    public String getWindDirection() { return windDirection; }
    public void setWindDirection(String windDirection) { this.windDirection = windDirection; }

    public double getTideHeight() { return tideHeight; }
    public void setTideHeight(double tideHeight) { this.tideHeight = tideHeight; }

    public double getSeaTemp() { return seaTemp; }
    public void setSeaTemp(double seaTemp) { this.seaTemp = seaTemp; }

    public double getPressure() { return pressure; }
    public void setPressure(double pressure) { this.pressure = pressure; }
}
