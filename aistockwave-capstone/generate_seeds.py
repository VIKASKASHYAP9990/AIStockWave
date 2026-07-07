# Helper script to generate initial Excel seed files for the Capstone pipeline
import pandas as pd
import os

# Create folder if it doesn't exist
folder = r"C:\Users\vikas\.gemini\antigravity-ide\scratch\aistockwave-capstone"
if not os.path.exists(folder):
    os.makedirs(folder)

# 1. Seed Raw Stocks Data
raw_stocks = [
    {"Symbol": "AAPL", "Company Name": "Apple Inc.", "Price": 185.25, "Open": 184.00, "Close": 184.50, "High": 186.40, "Low": 183.50, "Volume": 52000000, "Market Cap ($B)": 2900.5, "PE Ratio": 28.5, "EPS": 6.5, "Div Yield (%)": 0.52, "52W High": 199.62, "52W Low": 164.08},
    {"Symbol": "MSFT", "Company Name": "Microsoft Corp.", "Price": 420.50, "Open": 418.00, "Close": 417.80, "High": 422.90, "Low": 416.30, "Volume": 23000000, "Market Cap ($B)": 3130.2, "PE Ratio": 35.8, "EPS": 11.75, "Div Yield (%)": 0.71, "52W High": 430.82, "52W Low": 315.18},
    {"Symbol": "GOOGL", "Company Name": "Alphabet Inc.", "Price": 172.30, "Open": 170.50, "Close": 170.10, "High": 173.50, "Low": 169.80, "Volume": 28000000, "Market Cap ($B)": 2150.1, "PE Ratio": 26.2, "EPS": 6.58, "Div Yield (%)": 0.46, "52W High": 177.43, "52W Low": 115.50},
    {"Symbol": "AMZN", "Company Name": "Amazon.com Inc.", "Price": 188.40, "Open": 187.00, "Close": 186.20, "High": 189.50, "Low": 185.30, "Volume": 35000000, "Market Cap ($B)": 1950.4, "PE Ratio": 40.5, "EPS": 4.65, "Div Yield (%)": 0.00, "52W High": 191.70, "52W Low": 120.43},
    {"Symbol": "TSLA", "Company Name": "Tesla Inc.", "Price": 175.80, "Open": 178.00, "Close": 179.20, "High": 180.50, "Low": 173.20, "Volume": 82000000, "Market Cap ($B)": 560.8, "PE Ratio": 48.2, "EPS": 3.65, "Div Yield (%)": 0.00, "52W High": 299.29, "52W Low": 138.80},
    {"Symbol": "NVDA", "Company Name": "NVIDIA Corp.", "Price": 910.20, "Open": 898.00, "Close": 895.00, "High": 922.00, "Low": 891.50, "Volume": 49000000, "Market Cap ($B)": 2270.3, "PE Ratio": 72.8, "EPS": 12.50, "Div Yield (%)": 0.02, "52W High": 974.00, "52W Low": 262.20},
    {"Symbol": "META", "Company Name": "Meta Platforms Inc.", "Price": 495.10, "Open": 492.00, "Close": 490.50, "High": 501.20, "Low": 488.30, "Volume": 18000000, "Market Cap ($B)": 1260.6, "PE Ratio": 24.3, "EPS": 20.37, "Div Yield (%)": 0.40, "52W High": 531.49, "52W Low": 229.85},
    {"Symbol": "NFLX", "Company Name": "Netflix Inc.", "Price": 620.15, "Open": 615.00, "Close": 612.40, "High": 625.30, "Low": 611.20, "Volume": 5000000, "Market Cap ($B)": 268.4, "PE Ratio": 38.6, "EPS": 16.07, "Div Yield (%)": 0.00, "52W High": 639.00, "52W Low": 315.62}
]

df_stocks = pd.DataFrame(raw_stocks)
stocks_file = os.path.join(folder, "raw_stocks_data.xlsx")
df_stocks.to_excel(stocks_file, index=False)
print(f"Generated raw stocks seed: {stocks_file}")

# 2. Seed Raw News Data
raw_news = [
    {"Title": "NVIDIA Hits Record High on Massive AI Chip Demand", "Description": "NVIDIA stock soared past $900 today as major tech companies continue to place multi-billion dollar orders for its next-generation Blackwell AI processors. Analysts project high margins for the upcoming quarters.", "Source": "Financial Times", "Date": "July 8, 2026"},
    {"Title": "Federal Reserve Hints at Possible Rate Cut Next Month", "Description": "The Federal Reserve chairman suggested inflation is moderating toward the 2% target, sparking a market-wide rally. Tech and growth sectors led gains following the announcement.", "Source": "Wall Street Journal", "Date": "July 7, 2026"},
    {"Title": "Apple Announces New Generative AI Integrations for iOS 20", "Description": "Apple unveiled its upcoming OS updates at a developer conference, detailing deep system integrations of advanced generative LLMs running on-device. The stock climbed 2% in afternoon trading.", "Source": "TechCrunch", "Date": "July 6, 2026"},
    {"Title": "Tesla Deliveries Beats Estimates, Stock Jumps 4%", "Description": "Tesla announced quarterly delivery numbers that slightly exceeded Wall Street expectations, showing strong growth in its Model Y production and international markets.", "Source": "Bloomberg", "Date": "July 5, 2026"},
    {"Title": "Global Markets Rally as Supply Chain Congestion Eases", "Description": "Shipping rates and port wait times are returning to pre-pandemic averages, relieving inflationary pressure on manufacturing and consumer goods companies globally.", "Source": "Reuters", "Date": "July 4, 2026"}
]

df_news = pd.DataFrame(raw_news)
news_file = os.path.join(folder, "raw_news_data.xlsx")
df_news.to_excel(news_file, index=False)
print(f"Generated raw news seed: {news_file}")
