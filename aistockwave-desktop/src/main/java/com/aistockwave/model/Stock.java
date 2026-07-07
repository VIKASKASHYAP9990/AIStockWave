package com.aistockwave.model;

public class Stock {
    private String symbol;
    private String companyName;
    private double currentPrice;
    private double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;
    private long volume;
    private double marketCap; // in billions/millions
    private double peRatio;
    private double eps;
    private double dividendYield; // e.g. 1.25 for 1.25%
    private double high52Week;
    private double low52Week;

    public Stock() {}

    public Stock(String symbol, String companyName, double currentPrice, double openPrice, double closePrice,
                 double highPrice, double lowPrice, long volume, double marketCap, double peRatio,
                 double eps, double dividendYield, double high52Week, double low52Week) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
        this.marketCap = marketCap;
        this.peRatio = peRatio;
        this.eps = eps;
        this.dividendYield = dividendYield;
        this.high52Week = high52Week;
        this.low52Week = low52Week;
    }

    public double getChangePercent() {
        if (openPrice == 0) return 0;
        return ((currentPrice - openPrice) / openPrice) * 100;
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public double getOpenPrice() { return openPrice; }
    public void setOpenPrice(double openPrice) { this.openPrice = openPrice; }

    public double getClosePrice() { return closePrice; }
    public void setClosePrice(double closePrice) { this.closePrice = closePrice; }

    public double getHighPrice() { return highPrice; }
    public void setHighPrice(double highPrice) { this.highPrice = highPrice; }

    public double getLowPrice() { return lowPrice; }
    public void setLowPrice(double lowPrice) { this.lowPrice = lowPrice; }

    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }

    public double getMarketCap() { return marketCap; }
    public void setMarketCap(double marketCap) { this.marketCap = marketCap; }

    public double getPeRatio() { return peRatio; }
    public void setPeRatio(double peRatio) { this.peRatio = peRatio; }

    public double getEps() { return eps; }
    public void setEps(double eps) { this.eps = eps; }

    public double getDividendYield() { return dividendYield; }
    public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

    public double getHigh52Week() { return high52Week; }
    public void setHigh52Week(double high52Week) { this.high52Week = high52Week; }

    public double getLow52Week() { return low52Week; }
    public void setLow52Week(double low52Week) { this.low52Week = low52Week; }
}
