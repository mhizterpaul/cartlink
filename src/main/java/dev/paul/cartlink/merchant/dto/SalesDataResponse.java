package dev.paul.cartlink.merchant.dto;

import java.util.List;

public class SalesDataResponse {
    private List<SalesData> data;

    public List<SalesData> getData() {
        return data;
    }

    public void setData(List<SalesData> data) {
        this.data = data;
    }

    public static class SalesData {
        private String startDate;
        private String endDate;
        private double totalSales;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public double getTotalSales() {
            return totalSales;
        }

        public void setTotalSales(double totalSales) {
            this.totalSales = totalSales;
        }
    }
}