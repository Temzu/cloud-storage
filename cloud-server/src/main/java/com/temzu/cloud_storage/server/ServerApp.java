package com.temzu.cloud_storage.server;

import com.temzu.cloud_storage.server.storage.NettyNetwork;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerApp {

  private static final int PORT = 8189;
  public static Path rootFolder;

  static {
    try {
      rootFolder = Paths.get(ServerApp.class.getResource("/root_folder").toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new NettyNetwork(PORT, rootFolder).run();
  }
}
