package com.aistockwave.service;

import com.aistockwave.database.StockDAO;
import com.aistockwave.model.Stock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FinancialApiService {
    private final StockDAO stockDAO = new StockDAO();
    private ScheduledExecutorService scheduler;
    private static final Random rand = new Random();

    // DTO for historical chart points
    public static class ChartDataPoint {
        private final String timeLabel;
        private final double open;
        private final double high;
        private final double low;
        private final double close;

        public ChartDataPoint(String timeLabel, double open, double high, double low, double close) {
            this.timeLabel = timeLabel;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        public String getTimeLabel() { return timeLabel; }
        public double getOpen() { return open; }
        public double getHigh() { return high; }
        public double getLow() { return low; }
        public double getClose() { return close; }
    }

    public void startSimulator() {
        if (scheduler != null && !scheduler.isShutdown()) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "StockPriceSimulatorThread");
            t.setDaemon(true); // Let it terminate when UI closes
            return t;
        });

        scheduler.scheduleAtFixedRate(this::simulateMarketTicks, 3, 3, TimeUnit.SECONDS);
        System.out.println("Live Stock Market price simulator started.");
    }

    public void stopSimulator() {
        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("Live Stock Market price simulator stopped.");
        }
    }

    private void simulateMarketTicks() {
        try {
            List<Stock> stocks = stockDAO.getAllStocks();
            for (Stock s : stocks) {
                // Apply a small random percentage change (-0.7% to +0.8%, slight positive drift)
                double pctChange = (rand.nextDouble() * 1.5 - 0.7) / 100.0;
                double oldPrice = s.getCurrentPrice();
                double newPrice = oldPrice * (1.0 + pctChange);

                // Round to 2 decimal places
                newPrice = Math.round(newPrice * 100.0) / 100.0;

                // Adjust high/low prices
                double high = s.getHighPrice();
                double low = s.getLowPrice();
                if (newPrice > high) high = newPrice;
                if (newPrice < low) low = newPrice;

                // Add random volume increment
                long newVolume = s.getVolume() + rand.nextInt(3500) + 150;

                // Update database
                stockDAO.updateStockPrice(s.getSymbol(), newPrice, s.getOpenPrice(), s.getClosePrice(), high, low, newVolume);
            }
        } catch (Exception e) {
            System.err.println("Error running stock simulator tick: " + e.getMessage());
        }
    }

    // Fetches live API data if config is set, otherwise mock wrapper
    public String fetchExternalStockData(String symbol, String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return null;
        }
        String endpoint = "https://finnhub.io/api/v1/quote?symbol=" + symbol.toUpperCase() + "&token=" + apiKey;
        try {
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    return content.toString();
                }
            }
        } catch (Exception e) {
            System.err.println("API Request failed for " + symbol + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Generates a list of realistic historical data points for rendering charts.
     * Uses a backward random walk starting from the stock's current price to ensure
     * that the chart terminates exactly at the live price.
     */
    public List<ChartDataPoint> getHistoricalData(String symbol, String interval) {
        Stock stock = stockDAO.getStockBySymbol(symbol);
        if (stock == null) return Collections.emptyList();

        double currentPrice = stock.getCurrentPrice();
        int points = 0;
        double volatility = 0.015; // standard volatility factor
        String[] labels;

        switch (interval) {
            case "1D":
                points = 8; // hourly points (9:30 AM to 4:30 PM)
                volatility = 0.005;
                labels = new String[]{"09:30", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "16:00"};
                break;
            case "1W":
                points = 5;
                volatility = 0.012;
                labels = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri"};
                break;
            case "1M":
                points = 20;
                volatility = 0.025;
                labels = generateLabels(20, "Day ");
                break;
            case "6M":
                points = 26; // weekly details
                volatility = 0.06;
                labels = generateLabels(26, "Wk ");
                break;
            case "1Y":
                points = 12; // monthly details
                volatility = 0.10;
                labels = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                break;
            case "5Y":
                points = 10; // bi-annual details
                volatility = 0.25;
                labels = new String[]{"Y1-H1", "Y1-H2", "Y2-H1", "Y2-H2", "Y3-H1", "Y3-H2", "Y4-H1", "Y4-H2", "Y5-H1", "Y5-H2"};
                break;
            default:
                points = 10;
                labels = generateLabels(10, "Pt ");
                break;
        }

        List<ChartDataPoint> list = new ArrayList<>();
        double price = currentPrice;

        // Perform a backward random walk, then reverse the list
        for (int i = points - 1; i >= 0; i--) {
            // Apply geometric progression
            double changePercent = (rand.nextGaussian() * volatility);
            double open = price / (1.0 + changePercent);
            
            // Re-round to 2 decimals
            open = Math.round(open * 100.0) / 100.0;
            double close = Math.round(price * 100.0) / 100.0;

            double high = Math.max(open, close) * (1.0 + (rand.nextDouble() * 0.005));
            double low = Math.min(open, close) * (1.0 - (rand.nextDouble() * 0.005));

            high = Math.round(high * 100.0) / 100.0;
            low = Math.round(low * 100.0) / 100.0;

            String label = (i < labels.length) ? labels[i] : ("Pt " + (i + 1));
            list.add(new ChartDataPoint(label, open, high, low, close));

            // Set current working price to the open of this period for the next loop (going backward)
            price = open;
        }

        Collections.reverse(list);
        
        // Correct the last element to end exactly at current price
        if (!list.isEmpty()) {
            ChartDataPoint last = list.remove(list.size() - 1);
            String lastLabel = (labels.length > 0) ? labels[labels.length - 1] : last.getTimeLabel();
            
            double lastOpen = last.getOpen();
            double lastHigh = Math.max(lastOpen, currentPrice) * (1.0 + (rand.nextDouble() * 0.002));
            double lastLow = Math.min(lastOpen, currentPrice) * (1.0 - (rand.nextDouble() * 0.002));
            
            list.add(new ChartDataPoint(
                    lastLabel,
                    lastOpen,
                    Math.round(lastHigh * 100.0) / 100.0,
                    Math.round(lastLow * 100.0) / 100.0,
                    Math.round(currentPrice * 100.0) / 100.0
            ));
        }

        return list;
    }

    private String[] generateLabels(int count, String prefix) {
        String[] arr = new String[count];
        for (int i = 0; i < count; i++) {
            arr[i] = prefix + (i + 1);
        }
        return arr;
    }
}
