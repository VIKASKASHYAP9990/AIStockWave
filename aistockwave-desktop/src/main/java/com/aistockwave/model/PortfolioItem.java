package com.aistockwave.model;

public class PortfolioItem {
    private String symbol;
    private String companyName;
    private int quantity;
    private double avgBuyPrice;
    private double currentPrice;

    public PortfolioItem() {}

    public PortfolioItem(String symbol, String companyName, int quantity, double avgBuyPrice, double currentPrice) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.currentPrice = currentPrice;
    }

    public double getTotalInvestment() {
        return quantity * avgBuyPrice;
    }

    public double getCurrentValue() {
        return quantity * currentPrice;
    }

    public double getProfitLoss() {
        return getCurrentValue() - getTotalInvestment();
    }

    public double getReturnPercent() {
        if (avgBuyPrice == 0) return 0;
        return ((currentPrice - avgBuyPrice) / avgBuyPrice) * 100;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getAvgBuyPrice() { return avgBuyPrice; }
    public void setAvgBuyPrice(double avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}
