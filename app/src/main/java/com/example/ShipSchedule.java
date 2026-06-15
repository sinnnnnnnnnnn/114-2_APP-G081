package com.example;

/**
 * 船班資料模型 (航港 TDX API)
 */
public class ShipSchedule {
    private String routeName;             // 航線名稱
    private String ferryName;             // 船名 & 船班名稱
    private String departurePort;         // 出發港
    private String arrivalPort;           // 抵達港
    private String scheduledDepartureTime; // 表定開航時間
    private String actualStatus;           // 實際狀態（"正常", "停航", "延誤" 等）

    public ShipSchedule() {
        this.routeName = "未知航線";
        this.ferryName = "未知船名";
        this.departurePort = "-";
        this.arrivalPort = "-";
        this.scheduledDepartureTime = "-";
        this.actualStatus = "正常";
    }

    public ShipSchedule(String routeName, String ferryName, String departurePort, 
                        String arrivalPort, String scheduledDepartureTime, String actualStatus) {
        this.routeName = routeName;
        this.ferryName = ferryName;
        this.departurePort = departurePort;
        this.arrivalPort = arrivalPort;
        this.scheduledDepartureTime = scheduledDepartureTime;
        this.actualStatus = actualStatus;
    }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public String getFerryName() { return ferryName; }
    public void setFerryName(String ferryName) { this.ferryName = ferryName; }

    public String getDeparturePort() { return departurePort; }
    public void setDeparturePort(String departurePort) { this.departurePort = departurePort; }

    public String getArrivalPort() { return arrivalPort; }
    public void setArrivalPort(String arrivalPort) { this.arrivalPort = arrivalPort; }

    public String getScheduledDepartureTime() { return scheduledDepartureTime; }
    public void setScheduledDepartureTime(String scheduledDepartureTime) { this.scheduledDepartureTime = scheduledDepartureTime; }

    public String getActualStatus() { return actualStatus; }
    public void setActualStatus(String actualStatus) { this.actualStatus = actualStatus; }
}
