package com.temzu.cloud_storage.client.controller;

import com.temzu.cloud_storage.client.network.NetworkClient;
import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.util.AuthUserUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    private String login;
    private String password;
    private NetworkClient networkClient;
    private FileTransfer fileTransfer;
    private AuthUserUtil authUserUtil;

    @FXML
    private Button btnLogin;

    @FXML
    private TextField loginText;

    @FXML
    private PasswordField passText;

    @FXML
    private HBox loginPane;

    @FXML
    private TableView clientFilesList;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        networkClient = new NetworkClient("localhost", 8189);
        authUserUtil = networkClient.getAuthUserUtil();
        fileTransfer = networkClient.getFileTransfer();

        authUserUtil.setLogInCallback(objects -> {
            Platform.runLater(() -> {
                btnLogin.setText("Log out");
                loginText.setDisable(true);
                passText.setDisable(true);
            });
        });
    }

    private void showMessage(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void btnLoginOnAction(ActionEvent actionEvent) {
        login = loginText.getText().trim();
        password = passText.getText().trim();
        if (login.isEmpty() || password.isEmpty()){
            showMessage(Alert.AlertType.WARNING, "Warning", "The \"Login\" or \"Password\" fields is empty!");
            return;
        }
        if (networkClient == null) {
            return;
        }
        networkClient.getCurrentChannel().writeAndFlush(authUserUtil.singIn(login, password));
        LOG.debug("Attempt to authorize...");
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
    }

    public void btnRefreshOnAction(ActionEvent actionEvent) {
    }

    public void btnDownloadOnAction(ActionEvent actionEvent) {
    }

    public void btnUploadOnAction(ActionEvent actionEvent) {
    }

    public void selectDiskOnAction(ActionEvent actionEvent) {
    }

    public void btnPathUpAction(ActionEvent actionEvent) {
    }

    public void btnDeleteClientOnAction(ActionEvent actionEvent) {
    }

    public void btnRefreshClientOnAction(ActionEvent actionEvent) {
    }

    public void btnDeleteCloudOnAction(ActionEvent actionEvent) {
    }

    public void msossldf(MouseEvent mouseEvent) {

    }
}
