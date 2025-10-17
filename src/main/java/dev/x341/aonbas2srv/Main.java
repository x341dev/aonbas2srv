package dev.x341.aonbas2srv;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.x341.aonbas2srv.services.HttpServerHandler;
import dev.x341.aonbas2srv.services.ServerModule;
import dev.x341.aonbas2srv.util.AOBConstants;
import dev.x341.aonbas2srv.util.AOBLogger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class Main {

    public static void main(String[] args) throws Exception {
        Injector injector = Guice.createInjector(new ServerModule());
        AOBLogger.log("Guice Injector initialized");

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new HttpResponseEncoder());

                            ch.pipeline().addLast(injector.getInstance(HttpServerHandler.class));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            int port = AOBConstants.DEFAULT_PORT;
            ChannelFuture f = b.bind(port).sync();
            AOBLogger.log("Server started on port " + port);

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            AOBLogger.log("Server shut down.");
        }
    }
}
