package dev.x341.aonbas2srv.services;

import com.google.gson.Gson;
import com.google.inject.Inject;
import dev.x341.aonbas2srv.util.AOBLogger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * HTTP server handler implementing a small REST-like API used by the project.
 *
 * Supported endpoints (JSON):
 * - GET  /status                  -> returns plain text server status
 * - GET  /metro/lines             -> returns metro lines JSON
 * - GET  /metro/line/{lineCode}   -> stations for a line (JSON)
 * - GET  /metro/line/{line}/station/{stationCode} -> trains for a station (JSON)
 * - GET  /metro/line/{line}/station/{station}/corresp -> interchanges (JSON)
 * - POST /otp                     -> create an OTP. Request JSON: {"type":"...","payload":"..."}
 *                                     Response: 201 Created with OTP JSON
 * - GET  /otp/{id}                -> retrieve previously created OTP JSON or 404
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final MetroService metroService;
    private final OtpService otpService;
    private static final Gson GSON = new Gson();

    @Inject
    public HttpServerHandler(MetroService metroService, OtpService otpService) {
        this.metroService = metroService;
        this.otpService = otpService;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (!(msg instanceof FullHttpRequest)) {
            return; // We only handle aggregated FullHttpRequest objects
        }

        FullHttpRequest req = (FullHttpRequest) msg;
        String uri = req.uri();

        String content = "Not Found";
        HttpResponseStatus status = HttpResponseStatus.NOT_FOUND;
        String contentType = "text/plain";

        String path = uri;
        String query = null;
        int qidx = uri.indexOf('?');
        if (qidx >= 0) {
            path = uri.substring(0, qidx);
            query = uri.substring(qidx + 1);
        }

        String[] segments = path.length() > 1 ? path.substring(1).split("/") : new String[0];

        if (path.equals("/status")) {
            content = "Server is running";
            status = HttpResponseStatus.OK;
        } else if (path.equals("/metro/lines") && req.method().equals(HttpMethod.GET)) {
            content = metroService.getLines();
            status = HttpResponseStatus.OK;
            contentType = "application/json";
        } else if (segments.length > 0 && "metro".equals(segments[0])) {
            if (segments.length == 3 && "line".equals(segments[1])) {
                String lineCode = segments[2];
                AOBLogger.debug("Route /metro/line/{lineCode} detected: L=" + lineCode);
                content = metroService.getStationForLine(lineCode);
                status = HttpResponseStatus.OK;
                contentType = "application/json";
            } else if (segments.length == 5 && "line".equals(segments[1]) && "station".equals(segments[3])) {
                String lineCode = segments[2];
                String stationCode = segments[4];
                AOBLogger.debug("Calling station with station line " + lineCode + " and station " + stationCode);
                content = metroService.getTrainTimes(stationCode);
                status = HttpResponseStatus.OK;
                contentType = "application/json";
            } else if (segments.length == 6 && "line".equals(segments[1]) && "station".equals(segments[3]) && "corresp".equals(segments[5])) {
                String lineCode = segments[2];
                String stationCode = segments[4];
                AOBLogger.debug("Calling corresp with line " + lineCode + " and station " + stationCode);
                content = metroService.getInterchanges(lineCode, stationCode);
                status = HttpResponseStatus.OK;
                contentType = "application/json";
            }
        } else if (path.equals("/otp") && req.method().equals(HttpMethod.POST)) {
            // POST /otp -> create OTP from JSON body { "type":..., "payload":... }
            String body = req.content().toString(CharsetUtil.UTF_8);
            try {
                // parse minimal payload
                OtpCreateRequest r = GSON.fromJson(body, OtpCreateRequest.class);
                if (r == null || r.type == null || r.payload == null) {
                    status = HttpResponseStatus.BAD_REQUEST;
                    content = "{\"error\":\"INVALID_REQUEST\",\"message\":\"type and payload required\"}";
                    contentType = "application/json";
                } else {
                    dev.x341.aonbas2srv.otp.OtpDto dto = otpService.createOtp(r.type, r.payload);
                    content = dto.toJson();
                    status = HttpResponseStatus.CREATED;
                    contentType = "application/json";
                }
            } catch (Exception e) {
                AOBLogger.error("Error creating OTP", e);
                status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                content = "{\"error\":\"SERVER_ERROR\",\"message\":\"Failed to create OTP\"}";
                contentType = "application/json";
            }
        } else if (segments.length == 2 && "otp".equals(segments[0]) && req.method().equals(HttpMethod.GET)) {
            // GET /otp/{id}
            String id = segments[1];
            dev.x341.aonbas2srv.otp.OtpDto dto = otpService.getOtp(id);
            if (dto == null) {
                status = HttpResponseStatus.NOT_FOUND;
                content = "{\"error\":\"NOT_FOUND\",\"message\":\"OTP not found\"}";
                contentType = "application/json";
            } else {
                status = HttpResponseStatus.OK;
                content = dto.toJson();
                contentType = "application/json";
            }
        }

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.writeAndFlush(response);
        if (!HttpUtil.isKeepAlive(req)) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        AOBLogger.error("Handler error", cause);
        ctx.close();
    }

    // Small helper POJO used to parse OTP creation request body.
    private static class OtpCreateRequest {
        String type;
        String payload;
    }
}
