package dev.paul.cartlink.link.dto;

import java.util.List;
import java.util.Map;

public class LinkAnalyticsResponse {
    private double averageDurationSeconds;
    private List<Map<String, Object>> mostClicked;
    private List<Map<String, Object>> sourcesByPercentage;
    private double bounceRate;
    private List<Map<String, Object>> geoDistribution;
    private List<Map<String, Object>> deviceTypes;
    private int totalSources;
    private int totalClicks;

    public double getAverageDurationSeconds() {
        return averageDurationSeconds;
    }

    public void setAverageDurationSeconds(double averageDurationSeconds) {
        this.averageDurationSeconds = averageDurationSeconds;
    }

    public List<Map<String, Object>> getMostClicked() {
        return mostClicked;
    }

    public void setMostClicked(List<Map<String, Object>> mostClicked) {
        this.mostClicked = mostClicked;
    }

    public List<Map<String, Object>> getSourcesByPercentage() {
        return sourcesByPercentage;
    }

    public void setSourcesByPercentage(List<Map<String, Object>> sourcesByPercentage) {
        this.sourcesByPercentage = sourcesByPercentage;
    }

    public double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public List<Map<String, Object>> getGeoDistribution() {
        return geoDistribution;
    }

    public void setGeoDistribution(List<Map<String, Object>> geoDistribution) {
        this.geoDistribution = geoDistribution;
    }

    public List<Map<String, Object>> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<Map<String, Object>> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public int getTotalSources() {
        return totalSources;
    }

    public void setTotalSources(int totalSources) {
        this.totalSources = totalSources;
    }

    public int getTotalClicks() {
        return totalClicks;
    }

    public void setTotalClicks(int totalClicks) {
        this.totalClicks = totalClicks;
    }
}