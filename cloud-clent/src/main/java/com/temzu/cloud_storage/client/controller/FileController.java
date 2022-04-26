package com.temzu.cloud_storage.client.controller;

import com.temzu.cloud_storage.file.FileInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class FileController {

  private Path clientRootPath;
  private ListView<FileInfo> clientList;
  private ListView<String> serverList;

  public void fillClientCells(ListView<FileInfo> filesList, TextField pathField) {
    filesList.setCellFactory(
        new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
          @Override
          public ListCell<FileInfo> call(ListView<FileInfo> param) {
            return new ListCell<FileInfo>() {
              @Override
              protected void updateItem(FileInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                  setText(null);
                  setStyle("");
                } else {
                  String formattedFileLength;
                  long len = item.getLength() / 1024;
                  if (len > 0) {
                    formattedFileLength = String.format("%,d kb", len);
                  } else {
                    formattedFileLength = String.format("%,d byte", item.getLength());
                  }
                  String formattedFilename = String.format("%-30s", item.getFileName());
                  if (item.getLength() == -2L) {
                    formattedFileLength = "";
                  }
                  if (item.getLength() == -1L) {
                    formattedFileLength = String.format("%s", "[ DIR ]");
                  }
                  String text = String.format("%s %-20s", formattedFilename, formattedFileLength);
                  setText(text);
                }
              }
            };
          }
        });
    goToPath(clientRootPath, filesList, pathField);
  }

  private void goToPath(Path path, ListView<FileInfo> filesList, TextField pathField) {
    clientRootPath = path;
    System.out.println(clientRootPath.toString());
    pathField.setText(clientRootPath.toAbsolutePath().toString());
    filesList.getItems().clear();
    filesList.getItems().addAll(new FileInfo(FileInfo.UP_TOKEN, -2L));
    filesList.getItems().addAll(scanFiles(path));
    filesList
        .getItems()
        .sort(
            new Comparator<FileInfo>() {
              @Override
              public int compare(FileInfo o1, FileInfo o2) {
                if (o1.getFileName().equals(FileInfo.UP_TOKEN)) {
                  return -1;
                }
                if ((int) Math.signum(o1.getLength()) == (int) Math.signum(o2.getLength())) {
                  return o1.getFileName().compareTo(o2.getFileName());
                }
                return new Long(o1.getLength() - o2.getLength()).intValue();
              }
            });
  }

  public List<FileInfo> scanFiles(Path root) {
    try {
      return Files.list(root).map(FileInfo::new).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException("Files scan exception: " + root);
    }
  }

  public void refresh(ListView<FileInfo> filesList, TextField pathField) {
    goToPath(clientRootPath, filesList, pathField);
  }

  public Path filesListClicked(
      MouseEvent mouseEvent, ListView<FileInfo> filesList, TextField pathField) {
    if (mouseEvent.getClickCount() == 2) {
      FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
      if (fileInfo != null) {
        if (fileInfo.isDirectory()) {
          Path pathTo = clientRootPath.resolve(fileInfo.getFileName());
          goToPath(pathTo, filesList, pathField);
        }
        if (fileInfo.isUpDirectory()) {
          Path pathTo = clientRootPath.toAbsolutePath().getParent();
          goToPath(pathTo, filesList, pathField);
        }
      }
    }
    return clientRootPath;
  }

  public void clientRename(String rename) {
    TextInputDialog dialog =
        new TextInputDialog(clientList.getSelectionModel().getSelectedItem().getFileName());
    dialog.setTitle(rename);
    dialog.setHeaderText(null);
    dialog.setGraphic(null);
    dialog.setContentText("New name");
    Optional<String> res = dialog.showAndWait();
    if (res.isPresent()) {
      try {
        Path paths =
            Paths.get(
                clientRootPath.toString(),
                clientList.getSelectionModel().getSelectedItem().getFileName());
        Files.move(paths, paths.resolveSibling(res.get()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void setClientRootPath(Path clientRootPath) {
    this.clientRootPath = clientRootPath;
  }

  public void setClientList(ListView<FileInfo> clientList) {
    this.clientList = clientList;
  }

  public void setServerList(ListView<String> serverList) {
    this.serverList = serverList;
  }

  public String serverRename(String rename) {
    TextInputDialog dialog = new TextInputDialog(serverList.getSelectionModel().getSelectedItem());
    dialog.setTitle(rename);
    dialog.setHeaderText(null);
    dialog.setGraphic(null);
    dialog.setContentText("New name");
    Optional<String> res = dialog.showAndWait();
    return res.orElse(null);
  }
}
