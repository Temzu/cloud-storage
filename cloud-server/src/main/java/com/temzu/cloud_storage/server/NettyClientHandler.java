package com.temzu.cloud_storage.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClientHandler.class);

    private Path rootFolder;
    private AuthServerDb authServerDb;

    public NettyClientHandler(Path rootFolder, AuthServerDb authServerDb) {
        this.rootFolder = rootFolder;
        this.authServerDb = authServerDb;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client accepted!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.debug("Client disconnected!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

    }
}
