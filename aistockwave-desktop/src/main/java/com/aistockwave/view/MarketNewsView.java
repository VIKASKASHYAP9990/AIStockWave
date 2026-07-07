package com.aistockwave.view;

import com.aistockwave.database.NewsDAO;
import com.aistockwave.model.NewsItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;

import java.util.List;

public class MarketNewsView {
    private final ScrollPane scrollPane;
    private final VBox container;
    private final NewsDAO newsDAO = new NewsDAO();

    public MarketNewsView() {
        container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #0B0E14;");

        scrollPane = new ScrollPane(container);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        Label headline = new Label("📰 Financial Market News & Events");
        headline.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        VBox listContainer = new VBox(15);
        listContainer.getStyleClass().add("card");
        listContainer.setPadding(new Insets(25));

        List<NewsItem> newsList = newsDAO.getAllNews();
        if (newsList.isEmpty()) {
            Label emptyLbl = new Label("No financial news articles found in database.");
            emptyLbl.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 14;");
            listContainer.getChildren().add(emptyLbl);
        } else {
            for (int i = 0; i < newsList.size(); i++) {
                NewsItem item = newsList.get(i);
                VBox card = createNewsCard(item);
                listContainer.getChildren().add(card);

                // Add divider between articles (except the last one)
                if (i < newsList.size() - 1) {
                    Separator divider = new Separator();
                    listContainer.getChildren().add(divider);
                }
            }
        }

        container.getChildren().addAll(headline, listContainer);
    }

    public ScrollPane getView() {
        return scrollPane;
    }

    private VBox createNewsCard(NewsItem item) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);

        Label title = new Label(item.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold;");
        title.setWrapText(true);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label source = new Label(item.getSource().toUpperCase());
        source.setStyle("-fx-text-fill: #2962FF; -fx-font-weight: bold; -fx-font-size: 11;");
        Label separator = new Label("•");
        separator.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 11;");
        Label date = new Label(item.getDate());
        date.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 11;");
        metaRow.getChildren().addAll(source, separator, date);

        Label desc = new Label(item.getDescription());
        desc.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 13.5; -fx-line-spacing: 1.35;");
        desc.setWrapText(true);

        card.getChildren().addAll(title, metaRow, desc);
        return card;
    }
}
