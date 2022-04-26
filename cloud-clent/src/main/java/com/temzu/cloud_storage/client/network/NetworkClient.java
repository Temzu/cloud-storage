package com.temzu.cloud_storage.client.network;

import com.temzu.cloud_storage.file.FileTransfer;
import com.temzu.cloud_storage.util.AuthUserUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkClient {
  private static final Logger LOG = LoggerFactory.getLogger(NetworkClient.class);

  private String serverName;
  private int serverPort;
  private AuthUserUtil authUserUtil;
  private Channel currentChannel;
  private FileTransfer fileTransfer;

  public NetworkClient(String serverName, int serverPort) {
    this.serverName = serverName;
    this.serverPort = serverPort;
    authUserUtil = new AuthUserUtil();
    fileTransfer = new FileTransfer();
    Thread t = new Thread(this::start);
    t.setDaemon(true);
    t.start();
  }

  public void start() {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap clientBootstrap = new Bootstrap();
      clientBootstrap
          .group(group)
          .channel(NioSocketChannel.class)
          .remoteAddress(new InetSocketAddress(serverName, serverPort))
          .handler(
              new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                  currentChannel = socketChannel;
                  socketChannel.pipeline().addLast(new ServerHandler(authUserUtil, fileTransfer));
                }
              });
      ChannelFuture channelFuture = clientBootstrap.connect().sync();
      LOG.debug("Connection to server is successful");
      channelFuture.channel().closeFuture().sync();
    } catch (Exception e) {
      LOG.error("e = " + e);
    } finally {
      LOG.debug("Close connection.");
      closeConnection();
      group.shutdownGracefully();
    }
  }

  public void closeConnection() {
    currentChannel.close();
  }

  public AuthUserUtil getAuthUserUtil() {
    return authUserUtil;
  }

  public Channel getCurrentChannel() {
    return currentChannel;
  }

  public FileTransfer getFileTransfer() {
    return fileTransfer;
  }
}
