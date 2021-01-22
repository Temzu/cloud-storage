package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class Out extends ChannelOutboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Out.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        String message = (String) msg;
        LOG.debug("received message: {}", message);
        ByteBuf buf = ctx.alloc().directBuffer();
        buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
        buf.retain();
        ctx.writeAndFlush(buf);
    }
}
