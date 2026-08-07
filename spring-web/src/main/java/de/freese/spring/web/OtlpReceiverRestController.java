package de.freese.spring.web;

import java.util.HexFormat;

import jakarta.servlet.http.HttpServletRequest;

import com.google.protobuf.InvalidProtocolBufferException;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Thomas Freese
 * @since 05.08.26
 */
@RestController
// @RequestMapping("/")
public class OtlpReceiverRestController {
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();
    private static final Logger LOGGER = LoggerFactory.getLogger(OtlpReceiverRestController.class);
    private static final ObjectWriter OBJECT_WRITER = JSON_MAPPER.writer();

    private static String getAttribute(final Span span, final String attributeKey) {
        return span.getAttributesList().stream()
                .filter(attr -> attr.getKey().equals(attributeKey))
                .findFirst()
                .map(attr -> attr.getValue().getStringValue())
                .orElse(null);
    }

    private static void logTrace(final ExportTraceServiceRequest request) {
        request.getResourceSpansList().forEach(resourceSpans ->
                resourceSpans.getScopeSpansList().forEach(scopeSpans ->
                        scopeSpans.getSpansList().forEach(span ->
                                LOGGER.atInfo().log("span name={}; http.url={}; traceId={}; spanId={}; parent={}; durationMs={}",
                                        span.getName(),
                                        getAttribute(span, "http.url"),
                                        toHex(span.getTraceId()),
                                        toHex(span.getSpanId()),
                                        toHex(span.getParentSpanId()),
                                        (span.getEndTimeUnixNano() - span.getStartTimeUnixNano()) / 1_000_000L)
                        )
                )
        );
    }

    private static String toHex(final com.google.protobuf.ByteString bs) {
        return HEX_FORMAT.formatHex(bs.toByteArray());
    }

    @RequestMapping("/**")
    public void handleAllRequests(final HttpServletRequest request) {
        LOGGER.info("DEBUG - Request erhalten auf Pfad: {}", request.getRequestURI());
        LOGGER.info("DEBUG - Content-Type war: {}", request.getContentType());
    }

    @PostMapping(
            value = "/v1/traces",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> receiveTracesJson(@RequestBody final String jsonPayload) {
        // 1. JSON deserialisieren.
        final ExportTraceServiceRequest.Builder builder = JSON_MAPPER.readValue(jsonPayload, ExportTraceServiceRequest.Builder.class);
        final ExportTraceServiceRequest request = builder.build();

        // 2. Über die Hierarchie iterieren: Resource -> Scope -> Span.
        logTrace(request);

        // 3. Pflicht: Dem Sender Erfolg (200 OK) mit leerem ExportTraceServiceResponse zurückgeben.
        return ResponseEntity.ok(OBJECT_WRITER.writeValueAsString(ExportTraceServiceResponse.getDefaultInstance()));
    }

    @PostMapping(
            value = "/v1/traces",
            consumes = MediaType.APPLICATION_PROTOBUF_VALUE,
            produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public ResponseEntity<byte[]> receiveTracesProtoBuf(@RequestBody final byte[] body) throws InvalidProtocolBufferException {
        // 1. Protobuf deserialisieren.
        final ExportTraceServiceRequest request = ExportTraceServiceRequest.parseFrom(body);

        // 2. Über die Hierarchie iterieren: Resource -> Scope -> Span.
        logTrace(request);

        // 3. Pflicht: Dem Sender Erfolg (200 OK) mit leerem ExportTraceServiceResponse zurückgeben.
        return ResponseEntity.ok(ExportTraceServiceResponse.getDefaultInstance().toByteArray());
    }
}
