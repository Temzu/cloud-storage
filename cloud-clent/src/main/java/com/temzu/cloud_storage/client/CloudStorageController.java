package com.temzu.cloud_storage.client;

import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.util.AuthUserUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class CloudStorageController implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(CloudStorageController.class);

    private String login;
    private String password;
    private CloudStorageNetworkClient networkClient;
    private FileTransfer fileTransfer;
    private AuthUserUtil authUserUtil;

    @FXML
    private Button btnLogin;

    @FXML
    private TextField loginText;

    @FXML
    private PasswordField passText;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        networkClient = new CloudStorageNetworkClient("localhost", 8189);
        authUserUtil = networkClient.getAuthUserUtil();
        fileTransfer = networkClient.getFileTransfer();
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
        LOG.debug("sdf");
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

}
