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

/**
 * Application entry point for the server.
 *
 * Notes:
 * - This class currently uses NIO EventLoop groups for portability. Some Netty
 *   distributions may provide platform-specific implementations (Epoll, IO_URING) which
 *   offer better performance on Linux. If you add the native transport dependency, prefer
 *   to construct the platform-specific EventLoopGroup when available.
 */
public class Main {

    /**
     * Create the server and start listening on the configured port.
     * This method constructs two EventLoopGroup instances (boss and worker) and
     * shuts them down gracefully on exit.
     *
     * @param args command-line args (unused)
     * @throws Exception if the server fails to start
     */
    public static void main(String[] args) throws Exception {
        AOBLogger.log(AOBConstants.NAME + ". By: " + AOBConstants.CREATOR + ". Version: " + AOBConstants.getFullVersion());


        Injector injector = Guice.createInjector(new ServerModule());
        AOBLogger.log("Guice Injector initialized");

        int bossThreads = 1;
        int workerThreads = Math.max(1, Runtime.getRuntime().availableProcessors() * 2);

        EventLoopGroup bossGroup = createEventLoopGroup(bossThreads, "aon-boss");
        EventLoopGroup workerGroup = createEventLoopGroup(workerThreads, "aon-worker");

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

    /**
     * Helper to construct an EventLoopGroup. We suppress the deprecation annotation locally
     * to avoid IDE warnings while keeping runtime behavior unchanged. If you add a native
     * transport (epoll, io_uring), replace this implementation to return the platform-optimized
     * EventLoopGroup when available.
     *
     * @param threads number of threads
     * @param namePrefix thread name prefix
     * @return constructed EventLoopGroup
     */
    @SuppressWarnings("deprecation")
    private static EventLoopGroup createEventLoopGroup(int threads, String namePrefix) {
        return new NioEventLoopGroup(threads, new DefaultThreadFactory(namePrefix));
    }
}
