package com.aistockwave.view;

import com.aistockwave.service.FinancialApiService.ChartDataPoint;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.List;

public class CandlestickChartComponent extends Pane {

    public CandlestickChartComponent() {
        // Set standard styling
        this.setStyle("-fx-background-color: #131722; -fx-border-color: #2A2E39; -fx-border-width: 1; -fx-border-radius: 6;");
        this.setMinHeight(280);
        this.setPrefHeight(280);
    }

    public void setChartData(List<ChartDataPoint> data) {
        this.getChildren().clear();
        if (data == null || data.isEmpty()) {
            Text emptyText = new Text("No chart data available.");
            emptyText.setFill(Color.web("#B2B5BE"));
            emptyText.setX(100);
            emptyText.setY(100);
            this.getChildren().add(emptyText);
            return;
        }

        double width = this.getWidth();
        double height = this.getHeight();
        if (width <= 0) width = 500; // safety fallback if layout is not finished
        if (height <= 0) height = 280;

        // Leave margins
        double topMargin = 20;
        double bottomMargin = 30;
        double leftMargin = 15;
        double rightMargin = 65; // margin on right for the Y-Axis labels

        double chartWidth = width - leftMargin - rightMargin;
        double chartHeight = height - topMargin - bottomMargin;

        // Find absolute Min and Max across all Highs and Lows to scale
        double maxPrice = Double.MIN_VALUE;
        double minPrice = Double.MAX_VALUE;

        for (ChartDataPoint dp : data) {
            if (dp.getHigh() > maxPrice) maxPrice = dp.getHigh();
            if (dp.getLow() < minPrice) minPrice = dp.getLow();
        }

        // Add 5% padding to top and bottom of price range
        double priceRange = maxPrice - minPrice;
        if (priceRange == 0) priceRange = 1.0;
        maxPrice += priceRange * 0.05;
        minPrice -= priceRange * 0.05;
        priceRange = maxPrice - minPrice;

        // Draw horizontal grid lines & Y-axis labels
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            double ratio = (double) i / gridLines;
            double y = topMargin + chartHeight * (1 - ratio);
            double priceValue = minPrice + priceRange * ratio;

            // Gridline
            Line gridLine = new Line(leftMargin, y, leftMargin + chartWidth, y);
            gridLine.setStroke(Color.web("#2A2E39"));
            gridLine.getStrokeDashArray().addAll(3.0, 3.0);
            this.getChildren().add(gridLine);

            // Price label
            Text label = new Text(String.format("₹%,.2f", priceValue));
            label.setFill(Color.web("#B2B5BE"));
            label.setStyle("-fx-font-size: 10px;");
            label.setX(leftMargin + chartWidth + 5);
            label.setY(y + 4);
            this.getChildren().add(label);
        }

        // Draw candles
        int n = data.size();
        double candleWidth = chartWidth / n;
        double bodySpacing = Math.max(2, candleWidth * 0.15); // margin gap between candles

        for (int i = 0; i < n; i++) {
            ChartDataPoint dp = data.get(i);

            double xCenter = leftMargin + (i * candleWidth) + (candleWidth / 2);
            double xLeft = leftMargin + (i * candleWidth) + (bodySpacing / 2);
            double itemWidth = candleWidth - bodySpacing;

            // Scale coordinates
            double yOpen = topMargin + chartHeight * (1 - ((dp.getOpen() - minPrice) / priceRange));
            double yClose = topMargin + chartHeight * (1 - ((dp.getClose() - minPrice) / priceRange));
            double yHigh = topMargin + chartHeight * (1 - ((dp.getHigh() - minPrice) / priceRange));
            double yLow = topMargin + chartHeight * (1 - ((dp.getLow() - minPrice) / priceRange));

            boolean isGreen = dp.getClose() >= dp.getOpen();
            Color candleColor = isGreen ? Color.web("#26A69A") : Color.web("#EF5350");

            // 1. Draw Wick line
            Line wick = new Line(xCenter, yHigh, xCenter, yLow);
            wick.setStroke(candleColor);
            wick.setStrokeWidth(1.5);
            this.getChildren().add(wick);

            // 2. Draw Body rectangle
            double yTop = Math.min(yOpen, yClose);
            double bodyHeight = Math.max(1.5, Math.abs(yOpen - yClose)); // ensure visible size even if close=open

            Rectangle body = new Rectangle(xLeft, yTop, itemWidth, bodyHeight);
            body.setFill(candleColor);
            body.setStroke(candleColor);
            body.setStrokeWidth(0.5);
            this.getChildren().add(body);

            // 3. Draw X-Axis labels (only show alternate to avoid overcrowding on 1M / 6M charts)
            if (n <= 12 || i % (n / 6 + 1) == 0 || i == n - 1) {
                Text timeLabel = new Text(dp.getTimeLabel());
                timeLabel.setFill(Color.web("#B2B5BE"));
                timeLabel.setStyle("-fx-font-size: 10px;");
                // Center text
                timeLabel.setX(xCenter - (timeLabel.getLayoutBounds().getWidth() / 2));
                timeLabel.setY(height - 10);
                this.getChildren().add(timeLabel);

                // Tick mark on bottom line
                Line tick = new Line(xCenter, topMargin + chartHeight, xCenter, topMargin + chartHeight + 4);
                tick.setStroke(Color.web("#2A2E39"));
                this.getChildren().add(tick);
            }
        }

        // Draw border lines for axis
        Line bottomAxis = new Line(leftMargin, topMargin + chartHeight, leftMargin + chartWidth, topMargin + chartHeight);
        bottomAxis.setStroke(Color.web("#2A2E39"));
        this.getChildren().add(bottomAxis);
    }
}
