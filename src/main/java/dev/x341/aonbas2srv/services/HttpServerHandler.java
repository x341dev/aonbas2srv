package dev.x341.aonbas2srv.services;

import com.google.inject.Inject;
import dev.x341.aonbas2srv.util.AOBLogger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import static io.netty.buffer.Unpooled.copiedBuffer;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final MetroService metroService;

    @Inject
    public HttpServerHandler(MetroService metroService) {
        this.metroService = metroService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            String uri = req.uri();

            String content = "Not Found";
            HttpResponseStatus status = HttpResponseStatus.NOT_FOUND;
            String contentType = "text/plain";

            String[] segments = uri.substring(1).split("/");


            if (uri.equals("/status")) {
                content = "Server is running";
                status = HttpResponseStatus.OK;
            } else if (uri.equals("/metro/lines") && req.method().equals(HttpMethod.GET)) {
                content = metroService.getLines();
                status = HttpResponseStatus.OK;
                contentType = "application/json";
            } else if (segments.length == 3 && segments[0].equals("metro")
                    && segments[1].equals("line"))
            {
                String lineCode = segments[2];

                AOBLogger.log("Route /metro/line/{lineCode} detected: L=" + lineCode);

                content = metroService.getStationForLine(lineCode);
                status = HttpResponseStatus.OK;
                contentType = "application/json";
            }



            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, status,
                    copiedBuffer(content, CharsetUtil.UTF_8));
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            ctx.writeAndFlush(response);
            if (!HttpUtil.isKeepAlive(req)) {
                ctx.close();
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        AOBLogger.error("Handler error", cause);
        ctx.close();
    }
}
