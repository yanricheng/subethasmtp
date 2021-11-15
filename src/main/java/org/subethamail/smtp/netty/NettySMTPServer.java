package org.subethamail.smtp.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.netty.auth.AuthHandlerFactory;
import org.subethamail.smtp.netty.auth.EasyAuthHandlerFactory;
import org.subethamail.smtp.netty.auth.UsernameAndPsdValidator;
import org.subethamail.smtp.netty.auth.ext.MemoryBaseNameAndPsdValidator;

import java.util.Optional;

/**
 * @author yrc
 */
public class NettySMTPServer {
    private final int port;
    private final Logger logger = LoggerFactory.getLogger(NettySMTPServer.class);
    private SMTPServerConfig smtpServerConfig;

    public NettySMTPServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = 10000;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new NettySMTPServer(port).run();
    }

    public void run() throws Exception {
        SMTPServerConfig serverConfig =  new SMTPServerConfig();
        serverConfig.setHostName("netty smtp server");
        UsernameAndPsdValidator usernameAndPsdValidator = new MemoryBaseNameAndPsdValidator();
        usernameAndPsdValidator.add("yrc@yanrc.net","123456");
        AuthHandlerFactory authHandlerFactory = new EasyAuthHandlerFactory(usernameAndPsdValidator);
        serverConfig.setAuthHandlerFactory(Optional.of(authHandlerFactory));

        // (1)
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // (2)
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    // (3)
                    .channel(NioServerSocketChannel.class)
                    // (4)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("lineEncoder", new LineEncoder(LineSeparator.UNIX, CharsetUtil.UTF_8));
                            ch.pipeline().addLast("frameDecoder", new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast("smtpCommandDecoder", new SMTPCommandDecoder(CharsetUtil.UTF_8));
                            ch.pipeline().addLast(new SMTPServerHandler(serverConfig));
                        }
                    })
                    // (5)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // (6)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            // (7)
            ChannelFuture f = b.bind(port).sync();
            logger.info("smtp server is started! listening port:{}", port);
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
