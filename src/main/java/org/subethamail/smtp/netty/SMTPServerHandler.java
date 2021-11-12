package org.subethamail.smtp.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SMTPServerHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(SMTPServerHandler.class);

    @Override
    // (1)
    public void channelActive(final ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        System.out.println("IP:" + channel.localAddress().getHostString());
        System.out.println("Port:" + channel.localAddress().getPort());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " 接收到消息：" + msg);
        ctx.writeAndFlush("hi I'm ok");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exceptionCaught:", cause);
        super.exceptionCaught(ctx, cause);
    }
}
