package com.temzu.cloud_storage.server.storage;

import com.temzu.cloud_storage.server.database.ServerAuthDb;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyNetwork {

  private static final Logger LOG = LoggerFactory.getLogger(NettyNetwork.class);

  private final int PORT;
  private Path rootFolder;
  private final ServerAuthDb serverAuthDb;

  public NettyNetwork(int PORT, Path rootFolder) {
    this.PORT = PORT;
    this.rootFolder = rootFolder;
    this.serverAuthDb = ServerAuthDb.getInstance();
  }

  public void run() {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  ch.pipeline().addLast(new NettyClientHandler(rootFolder));
                }
              });
      ChannelFuture future = b.bind(PORT).sync();
      LOG.debug("Server started on PORT = " + PORT);
      future.channel().closeFuture().sync();
    } catch (InterruptedException e) {
      LOG.error("e=", e);
    } finally {
      serverAuthDb.closeConnection();
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }
}
