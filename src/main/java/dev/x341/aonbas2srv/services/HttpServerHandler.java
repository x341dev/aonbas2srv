package dev.x341.aonbas2srv.services;

import com.google.gson.Gson;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.util.AOBLogger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Arrays;

/**
 * Refactored HTTP server handler with clean dispatching and centralized response logic.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final MetroService metroService;
    private final OtpService otpService;
    private final TramService tramService;
    private static final Gson GSON = new Gson();

    @Inject
    public HttpServerHandler(MetroService metroService, OtpService otpService, TramService tramService) {
        this.metroService = metroService;
        this.otpService = otpService;
        this.tramService = tramService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (!(msg instanceof FullHttpRequest req)) return;

        String uri = req.uri();
        String path = uri.split("\\?")[0];
        String[] segments = path.substring(1).split("/");

        // Default response
        String content = "Not Found";
        HttpResponseStatus status = HttpResponseStatus.NOT_FOUND;
        String contentType = "text/plain";

        try {
            if (path.equals("/status")) {
                content = "Server is running";
                status = HttpResponseStatus.OK;

            } else if (path.equals("/metro/lines") && req.method().equals(HttpMethod.GET)) {
                content = metroService.getLines();
                status = HttpResponseStatus.OK;
                contentType = "application/json";

            } else if (segments.length > 1 && "metro".equals(segments[0])) {
                content = handleMetroRoutes(req, segments);
                if (content != null) {
                    status = HttpResponseStatus.OK;
                    contentType = "application/json";
                }

            } else if (segments.length > 0 && "otp".equals(segments[0])) {
                HttpResult otpResult = handleOtpRoutes(req, segments);
                content = otpResult.content();
                status = otpResult.status();
                contentType = "application/json";

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

    private String handleMetroRoutes(FullHttpRequest req, String[] seg) {
        if (seg.length == 3 && "line".equals(seg[1])) {
            return metroService.getStationForLine(seg[2]);
        } else if (seg.length == 5 && "line".equals(seg[1]) && "station".equals(seg[3])) {
            return metroService.getTrainTimes(seg[4]);
        } else if (seg.length == 6 && "line".equals(seg[1]) && "station".equals(seg[3]) && "corresp".equals(seg[5])) {
            return metroService.getInterchanges(seg[2], seg[4]);
        }
        return null;
    }

    private HttpResult handleOtpRoutes(FullHttpRequest req, String[] seg) {
        if (req.method().equals(HttpMethod.POST) && seg.length == 1) {
            String body = req.content().toString(CharsetUtil.UTF_8);
            try {
                OtpCreateRequest r = GSON.fromJson(body, OtpCreateRequest.class);
                if (r == null || r.type == null || r.payload == null) {
                    return new HttpResult(HttpResponseStatus.BAD_REQUEST, "{\"error\":\"INVALID_REQUEST\",\"message\":\"type and payload required\"}");
                }
                var dto = otpService.createOtp(r.type, r.payload);
                return new HttpResult(HttpResponseStatus.CREATED, dto.toJson());
            } catch (Exception e) {
                AOBLogger.error("Error creating OTP", e);
                return new HttpResult(HttpResponseStatus.INTERNAL_SERVER_ERROR, "{\"error\":\"SERVER_ERROR\",\"message\":\"Failed to create OTP\"}");
            }
        } else if (req.method().equals(HttpMethod.GET) && seg.length == 2) {
            var dto = otpService.getOtp(seg[1]);
            if (dto == null) {
                return new HttpResult(HttpResponseStatus.NOT_FOUND, "{\"error\":\"NOT_FOUND\",\"message\":\"OTP not found\"}");
            }
            return new HttpResult(HttpResponseStatus.OK, dto.toJson());
        }
        return new HttpResult(HttpResponseStatus.NOT_FOUND, "{\"error\":\"NOT_FOUND\",\"message\":\"Endpoint not found\"}");
    }

    private String handleTramRoutes(String[] seg) throws IOException {
        if (seg.length == 1) return tramService.getLines();
        if (seg.length == 3 && "line".equals(seg[1])) return tramService.getStopsForLines(seg[2]);
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

    private static class OtpCreateRequest {
        String type;
        String payload;
    }
}
