package com.temzu.cloud_storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CloudStorageClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/CloudStorageLayout.fxml"));
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}