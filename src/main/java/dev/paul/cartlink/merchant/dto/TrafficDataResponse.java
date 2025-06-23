package dev.paul.cartlink.merchant.dto;

import java.util.List;

public class TrafficDataResponse {
    private List<TrafficData> data;

    public List<TrafficData> getData() {
        return data;
    }

    public void setData(List<TrafficData> data) {
        this.data = data;
    }

    public static class TrafficData {
        private String source;
        private int clicks;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public int getClicks() {
            return clicks;
        }

        public void setClicks(int clicks) {
            this.clicks = clicks;
        }
    }
}