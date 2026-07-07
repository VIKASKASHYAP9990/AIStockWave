package com.aistockwave.model;

public class NewsItem {
    private int id;
    private String title;
    private String description;
    private String source;
    private String date; // Formatted date string, e.g., "July 8, 2026"

    public NewsItem() {}

    public NewsItem(int id, String title, String description, String source, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.source = source;
        this.date = date;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
