package com.aistockwave.service;

import com.aistockwave.model.Stock;

public class AiAnalysisService {

    public static class StockAnalysisReport {
        private final String rating; // STRONG BUY, BUY, HOLD, SELL, etc.
        private final String summary;
        private final String strengths;
        private final String risks;
        private final String educationalInsight;

        public StockAnalysisReport(String rating, String summary, String strengths, String risks, String educationalInsight) {
            this.rating = rating;
            this.summary = summary;
            this.strengths = strengths;
            this.risks = risks;
            this.educationalInsight = educationalInsight;
        }

        public String getRating() { return rating; }
        public String getSummary() { return summary; }
        public String getStrengths() { return strengths; }
        public String getRisks() { return risks; }
        public String getEducationalInsight() { return educationalInsight; }
    }

    public StockAnalysisReport generateAnalysis(Stock stock) {
        if (stock == null) {
            return new StockAnalysisReport("N/A", "No stock data available.", "N/A", "N/A", "N/A");
        }

        double pe = stock.getPeRatio();
        double div = stock.getDividendYield();
        double change = stock.getChangePercent();
        double currentPrice = stock.getCurrentPrice();
        double high52 = stock.getHigh52Week();
        double low52 = stock.getLow52Week();
        double eps = stock.getEps();

        String rating;
        StringBuilder summary = new StringBuilder();
        StringBuilder strengths = new StringBuilder();
        StringBuilder risks = new StringBuilder();
        StringBuilder education = new StringBuilder();

        // 1. Determine Rating and General Summary
        if (change > 2.0 && pe < 30.0 && eps > 0) {
            rating = "STRONG BUY";
            summary.append(stock.getCompanyName()).append(" is displaying exceptionally strong bullish momentum. ")
                   .append("With a daily price increase of ").append(String.format("%.2f", change)).append("%, ")
                   .append("the stock is trading at ₹").append(currentPrice).append(" under heavy buying interest. ")
                   .append("Strong financial indicators and a positive chart trend suggest high investor confidence.");
        } else if (change >= 0 && pe < 40.0) {
            rating = "BUY";
            summary.append(stock.getCompanyName()).append(" exhibits a stable market profile. ")
                   .append("Current trading price is ₹").append(currentPrice).append(". ")
                   .append("Moderate valuation levels coupled with positive earnings support a constructive forecast ")
                   .append("over the short to medium term.");
        } else if (pe > 60.0) {
            rating = "UNDERPERFORM / SELL";
            summary.append(stock.getCompanyName()).append(" appears highly valued at the current level of ₹").append(currentPrice).append(". ")
                   .append("A high Price-to-Earnings (P/E) ratio of ").append(pe).append(" implies that the stock price ")
                   .append("is reflecting aggressive growth expectations, exposing it to downward corrections if earnings disappoint.");
        } else {
            rating = "HOLD";
            summary.append(stock.getCompanyName()).append(" is consolidating within its standard trading range. ")
                   .append("With current price at ₹").append(currentPrice).append(", the market is awaiting catalyst news ")
                   .append("or earnings reports. Proximity to support levels indicates a balanced risk-to-reward ratio.");
        }

        // 2. Compute Strengths
        if (pe < 25.0 && pe > 0) {
            strengths.append("• Attractive Valuation: The P/E ratio is ").append(pe).append(", which is below the tech sector average, offering a margin of safety.\n");
        } else if (pe <= 0) {
            strengths.append("• Asset-Heavy Base: Value-focused model despite low short-term earnings multiples.\n");
        }
        if (eps > 5.0) {
            strengths.append("• Exceptional Profitability: An Earnings Per Share (EPS) of ").append(eps).append(" demonstrates high core profitability.\n");
        }
        if (div > 0) {
            strengths.append("• Yield Generosity: Provides a dividend yield of ").append(div).append("%, securing consistent cash return for passive income portfolios.\n");
        }
        if (currentPrice > (high52 + low52) / 2) {
            strengths.append("• Momentum Strength: Stock is trading in the upper half of its 52-week range, reflecting a long-term uptrend.\n");
        }
        if (strengths.length() == 0) {
            strengths.append("• High Liquidity: Large daily volume supports easy transaction entries and exits with low slippage.\n");
        }

        // 3. Compute Risks
        if (pe > 45.0) {
            risks.append("• Premium Valuation: The P/E ratio is elevated at ").append(pe).append(". This high multiple requires sustained high growth rate to justify.\n");
        }
        if (change < -2.0) {
            risks.append("• Bearish Momentum: Heavy short-term selling pressure (daily drop of ").append(String.format("%.2f", change)).append("%) indicates near-term volatility.\n");
        }
        if (currentPrice < low52 * 1.15) {
            risks.append("• Near 52-Week Lows: Trading close to its annual low (₹").append(low52).append("). Suggests operational challenges or weak industry sentiments.\n");
        }
        if (div == 0) {
            risks.append("• No Yield: The company pays no dividends, requiring the investor to rely solely on capital appreciation.\n");
        }
        if (risks.length() == 0) {
            risks.append("• General Market Beta: Subject to systematic macro interest rate shifts and tech sector volatility.\n");
        }

        // 4. Educational Insights
        education.append("💡 Paper Trading Insights:\n")
                .append("1. P/E Ratio (Price-to-Earnings): ").append(stock.getCompanyName()).append(" has a P/E of ").append(pe).append(". ")
                .append("This means investors are paying ₹").append(pe).append(" for every ₹1 of company earnings. High P/E stocks are considered 'growth' stocks; low P/E are 'value' stocks.\n")
                .append("2. EPS (Earnings Per Share): With an EPS of ₹").append(eps).append(", this is the portion of profit allocated to each share of stock. Higher EPS is generally a sign of financial strength.\n")
                .append("3. 52-Week Range: The stock is currently trading at ₹").append(currentPrice).append(" against a 52-week low of ₹").append(low52).append(" and a high of ₹").append(high52).append(". ")
                .append("Buying near the high implies momentum trading, while buying near the low is contrarian/value trading.");

        return new StockAnalysisReport(
                rating,
                summary.toString(),
                strengths.toString(),
                risks.toString(),
                education.toString()
        );
    }
}
