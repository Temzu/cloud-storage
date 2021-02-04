package com.temzu.cloud_storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

public class ClientApp extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(ClientApp.class);

    private Stage primaryStage;
    private Stage processStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/CloudStorageLayout.fxml"));
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root, 900, 500));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e->{
            LOG.debug("Close application.");
            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (Thread t : threads) {
                t.interrupt();
            }
        });
    }

    public static void main(String[] args) {
        LOG.debug("Started client application.");
        launch(args);
    }

    public void openProcessStage() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ProcessLayout.fxml"));
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root, 300, 300));
        primaryStage.show();
    }

}
