package dev.paul.cartlink.merchant.dto;

public class DashboardStatsResponse {
    private double totalSales;
    private int totalOrders;
    private double todaySales;
    private int totalCustomers;
    private Analytics analytics;

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(double todaySales) {
        this.todaySales = todaySales;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public Analytics getAnalytics() {
        return analytics;
    }

    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }

    public static class Analytics {
        private double totalSalesChange;
        private double totalOrdersChange;
        private double todaySalesChange;
        private double totalCustomersChange;

        public double getTotalSalesChange() {
            return totalSalesChange;
        }

        public void setTotalSalesChange(double totalSalesChange) {
            this.totalSalesChange = totalSalesChange;
        }

        public double getTotalOrdersChange() {
            return totalOrdersChange;
        }

        public void setTotalOrdersChange(double totalOrdersChange) {
            this.totalOrdersChange = totalOrdersChange;
        }

        public double getTodaySalesChange() {
            return todaySalesChange;
        }

        public void setTodaySalesChange(double todaySalesChange) {
            this.todaySalesChange = todaySalesChange;
        }

        public double getTotalCustomersChange() {
            return totalCustomersChange;
        }

        public void setTotalCustomersChange(double totalCustomersChange) {
            this.totalCustomersChange = totalCustomersChange;
        }
    }
}