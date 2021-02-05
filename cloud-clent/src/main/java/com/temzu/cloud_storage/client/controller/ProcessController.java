package com.temzu.cloud_storage.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ProcessController {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
    private static Stage progress;

    @FXML
    public ProgressBar progressBar;

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public static ProcessController openProgress(String title) {
        ProcessController processController = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ProcessController.class.getResource("/ProcessLayout.fxml"));
            Parent root = fxmlLoader.load();
            progress = new Stage();
            progress.setTitle(title);
            progress.setScene(new Scene(root));
            progress.show();
            processController = fxmlLoader.getController();
            progress.setOnCloseRequest(e -> {
                progress = null;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processController;
    }

}
