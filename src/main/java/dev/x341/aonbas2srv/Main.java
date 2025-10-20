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
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;

public class Main {

    public static void main(String[] args) throws Exception {
        AOBLogger.log(AOBConstants.NAME + ". By: " + AOBConstants.CREATOR + ". Version: " + AOBConstants.getFullVersion());


        Injector injector = Guice.createInjector(new ServerModule());
        AOBLogger.log("Guice Injector initialized");

        int bossThreads = 1;
        int workerThreads = Math.max(1, Runtime.getRuntime().availableProcessors() * 2);

        EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreads, new DefaultThreadFactory("aon-boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads, new DefaultThreadFactory("aon-worker"));

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));

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
