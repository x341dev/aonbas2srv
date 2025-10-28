package dev.x341.aonbas2srv.services;

import com.google.gson.Gson;
import dev.x341.aonbas2srv.dto.MetroDto;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class HttpServerHandlerTest {
    private MetroService metroService;
    private TramService tramService;
    private EmbeddedChannel channel;
    private static final Gson GSON = new Gson();

    @BeforeEach
    void setup() {
        metroService = mock(MetroService.class);
        tramService = mock(TramService.class);

        channel = new EmbeddedChannel(new HttpServerHandler(metroService, tramService));
    }

    @Test
    void testStatus() {
        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/status");
        channel.writeInbound(req);
        FullHttpResponse resp = channel.readOutbound();

        assertEquals(HttpResponseStatus.OK, resp.status());
        String content = resp.content().toString(CharsetUtil.UTF_8);
        assertEquals("Server is running", content);
    }

    @Test
    void testMetroLines() throws Exception {
        MetroDto dto = new MetroDto();
        when(metroService.getLinesDto()).thenReturn(dto);
        when(metroService.getLinesJson()).thenReturn("{\"lines\":[]}");

        // App client
        FullHttpRequest reqApp = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/metro/lines");
        reqApp.headers().set("X-Client-Type", "app");
        channel.writeInbound(reqApp);
        FullHttpResponse respApp = channel.readOutbound();
        assertEquals(HttpResponseStatus.OK, respApp.status());
        assertEquals(GSON.toJson(dto), respApp.content().toString(io.netty.util.CharsetUtil.UTF_8));

        // Regular GET
        FullHttpRequest reqHttp = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/metro/lines");
        channel.writeInbound(reqHttp);
        FullHttpResponse respHttp = channel.readOutbound();
        assertEquals(HttpResponseStatus.OK, respHttp.status());
        assertEquals("{\"lines\":[]}", respHttp.content().toString(io.netty.util.CharsetUtil.UTF_8));
    }

    @Test
    void testMetroLineById() throws Exception {
        MetroDto lineDto = new MetroDto();
        when(metroService.getStationForLineDto("1a5")).thenReturn(lineDto);
        when(metroService.getStationForLine("1a5")).thenReturn("{\"stations\":[]}");

        // App
        FullHttpRequest reqApp = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/metro/line/1a5");
        reqApp.headers().set("X-Client-Type", "app");
        channel.writeInbound(reqApp);
        FullHttpResponse respApp = channel.readOutbound();
        assertEquals(HttpResponseStatus.OK, respApp.status());
        assertEquals(GSON.toJson(lineDto), respApp.content().toString(io.netty.util.CharsetUtil.UTF_8));

        // HTTP
        FullHttpRequest reqHttp = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/metro/line/1a5");
        channel.writeInbound(reqHttp);
        FullHttpResponse respHttp = channel.readOutbound();
        assertEquals(HttpResponseStatus.OK, respHttp.status());
        assertEquals("{\"stations\":[]}", respHttp.content().toString(io.netty.util.CharsetUtil.UTF_8));
    }

    @Test
    void testMetroLineStationTimes() throws Exception {
        String trainTimesJson = "{\"trains\":[{\"code\":\"319a325\",\"time\":12345}]}";
        when(metroService.getTrainTimes("319a325")).thenReturn(trainTimesJson);

        FullHttpRequest req = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/metro/line/3/station/319a325");
        channel.writeInbound(req);
        FullHttpResponse resp = channel.readOutbound();

        assertEquals(HttpResponseStatus.OK, resp.status());
        assertEquals(trainTimesJson, resp.content().toString(io.netty.util.CharsetUtil.UTF_8));
    }

}
