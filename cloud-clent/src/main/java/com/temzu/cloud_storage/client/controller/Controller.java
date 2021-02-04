package com.temzu.cloud_storage.client.controller;

import com.temzu.cloud_storage.client.network.NetworkClient;
import com.temzu.cloud_storage.file.FileInfo;
import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.operation.Command;
import com.temzu.cloud_storage.util.AuthUserUtil;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    private String login;
    private String password;
    private NetworkClient networkClient;
    private FileTransfer fileTransfer;
    private AuthUserUtil authUserUtil;
    private Path currentFolder;
    private FileController fileController;

    @FXML
    private Button btnLogin;

    @FXML
    private TextField loginText;

    @FXML
    private PasswordField passText;

    @FXML
    private TextField clientPathField;

    @FXML
    private ListView<FileInfo> clientFilesList;

    @FXML
    private ListView<String> serverFilesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentFolder = Paths.get(File.listRoots()[1].getAbsolutePath());

        fileController = new FileController(currentFolder);
        fileController.fillClientCells(clientFilesList, clientPathField);
        networkClient = new NetworkClient("localhost", 8189);
        authUserUtil = networkClient.getAuthUserUtil();
        fileTransfer = networkClient.getFileTransfer();

        initCallBack();

    }

    private void getFileListServer(){
        networkClient.getCurrentChannel().writeAndFlush(fileTransfer.requestFileList(new String()));
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

    private void initCallBack() {

        authUserUtil.setLogInCallback(objects -> {
            Platform.runLater(() -> {
                btnLogin.setText("Log out");
                loginText.setDisable(true);
                passText.setDisable(true);
                getFileListServer();
            });
        });

        fileTransfer.setGetFileListCallBack(args -> {
            Platform.runLater(()->{
                List<String> filesList = (List<String>) args[0];
                serverFilesList.getItems().clear();
                serverFilesList.getItems().addAll(filesList);
            });
        });

        fileTransfer.setDownloadFileCallback(args -> {
            Platform.runLater(() -> {
                fileController.refresh(clientFilesList, clientPathField);
            });
        });

    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {
    }

    public void btnRefreshOnAction(ActionEvent actionEvent) {
    }

    public void btnDownloadOnAction(ActionEvent actionEvent) {

    }

    public void btnUploadOnAction(ActionEvent actionEvent) {
        fileTransfer.setCurrentFolder(currentFolder.toString());
        ByteBuf buff = fileTransfer.requestUploadFile(clientFilesList.getFocusModel().getFocusedItem().getFileName());
        if(buff == null){
            return;
        }
        networkClient.getCurrentChannel().writeAndFlush(buff);
        new Thread(()->{fileTransfer.sendFile(networkClient.getCurrentChannel());}).start();
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

    public void filesListClicked(MouseEvent mouseEvent) {
        currentFolder = fileController.filesListClicked(mouseEvent, clientFilesList, clientPathField);
    }

    public void btnDownloadFromServer(ActionEvent actionEvent) {
        fileTransfer.setCurrentFolder(currentFolder.toAbsolutePath().toString());
        String downLoadFile = serverFilesList.getFocusModel().getFocusedItem();
        System.out.println(downLoadFile);
        networkClient.getCurrentChannel().writeAndFlush(fileTransfer.sendSomeMessage(downLoadFile, Command.DOWNLOAD_FILE));
    }
}
