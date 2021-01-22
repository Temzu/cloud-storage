package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstIn extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(FirstIn.class);


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
        ByteBuf buf = (ByteBuf) msg;
        try {
            LOG.debug("received message {}", buf);
            StringBuilder str = new StringBuilder();
            while (buf.isReadable()) {
                str.append((char) buf.readByte());
            }
            String message = str.toString();
            LOG.debug("converted to string {}", message);
            ctx.fireChannelRead(message);
        } finally {
            ReferenceCountUtil.release(buf);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("e=", cause);
    }
}