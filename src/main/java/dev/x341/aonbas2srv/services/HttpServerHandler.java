package dev.x341.aonbas2srv.services;

import com.google.gson.Gson;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.dto.MetroDto;
import dev.x341.aonbas2srv.util.AOBLogger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final MetroService metroService;
    private final TramService tramService;
    private static final Gson GSON = new Gson();

    @Inject
    public HttpServerHandler(MetroService metroService, TramService tramService) {
        this.metroService = metroService;
        this.tramService = tramService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (!(msg instanceof FullHttpRequest req)) return;

        String uri = req.uri();
        String path = uri.split("\\?")[0];
        String[] segments = path.substring(1).split("/");

        boolean isApp = "app".equals(req.headers().get("X-Client-Type")); // <-- detect app
        String content = "Not Found";
        HttpResponseStatus status = HttpResponseStatus.NOT_FOUND;
        String contentType = "text/plain";

        try {
            if (path.equals("/status")) {
                content = "Server is running";
                status = HttpResponseStatus.OK;

            } else if (path.equals("/metro/lines") && req.method().equals(HttpMethod.GET)) {
                if (isApp) {
                    MetroDto dto = metroService.getLinesDto();
                    content = dto != null ? GSON.toJson(dto) : "{}"; // app recibe DTO
                } else {
                    content = metroService.getLinesJson(); // GET HTTP recibe JSON
                }
                status = HttpResponseStatus.OK;
                contentType = "application/json";

            } else if (segments.length > 1 && "metro".equals(segments[0])) {
                content = handleMetroRoutes(req, segments, isApp);
                if (content != null) {
                    status = HttpResponseStatus.OK;
                    contentType = "application/json";
                }

            } else if (segments.length > 0 && "tram".equals(segments[0])) {
                content = handleTramRoutes(segments);
                if (content != null) {
                    status = HttpResponseStatus.OK;
                    contentType = "application/json";
                }
            }
        } catch (Exception e) {
            AOBLogger.error("Handler error", e);
            status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            content = "{\"error\":\"SERVER_ERROR\",\"message\":\"Internal error\"}";
            contentType = "application/json";
        }

        sendResponse(ctx, req, content, status, contentType);
    }

    private String handleMetroRoutes(FullHttpRequest req, String[] seg, boolean isApp) throws IOException {
        if (seg.length == 3 && "line".equals(seg[1])) {
            return isApp
                    ? GSON.toJson(metroService.getStationForLineDto(seg[2]))
                    : metroService.getStationForLine(seg[2]);

        } else if (seg.length == 5 && "line".equals(seg[1]) && "station".equals(seg[3])) {
            if (isApp) {
                return GSON.toJson(metroService.getTrainTimesDto(seg[4]));
            } else {
                return metroService.getTrainTimes(seg[4]);
            }

        } else if (seg.length == 6 && "line".equals(seg[1]) && "station".equals(seg[3]) && "corresp".equals(seg[5])) {
            if (isApp) {
                return GSON.toJson(metroService.getInterchangesDto(seg[2], seg[4]));
            } else {
                return metroService.getInterchanges(seg[2], seg[4]);
            }
        }
        return null;
    }

    private String handleTramRoutes(String[] seg) throws IOException {
        if (seg.length == 1) return tramService.getLinesJson();
        if (seg.length == 3 && "line".equals(seg[1])) return GSON.toJson(tramService.getStopsForLine(seg[2]));
        if (seg.length == 5 && "line".equals(seg[1]) && "stop".equals(seg[3])) return tramService.getStopTimes(seg[4]);
        if (seg.length == 2 && "codes".equals(seg[1])) return GSON.toJson(tramService.listAllGtfsCodes());
        if (seg.length == 3 && "check-missing".equals(seg[1])) return GSON.toJson(tramService.findMissingStaticStopsInGtfsRt(seg[2]));
        if (seg.length == 2 && "raw-stops".equals(seg[1])) return "";
        return null;
    }

    private void sendResponse(ChannelHandlerContext ctx, FullHttpRequest req, String content, HttpResponseStatus status, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8)
        );
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(req)) ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        AOBLogger.error("Handler error", cause);
        ctx.close();
    }

    private record HttpResult(HttpResponseStatus status, String content) {}
    private static class OtpCreateRequest { String type; String payload; }
}
