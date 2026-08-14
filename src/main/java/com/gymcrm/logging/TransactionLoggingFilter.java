package com.gymcrm.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Two-level logging for every REST call:
 * <ol>
 *   <li>Transaction level — assigns / propagates {@code transactionId} (MDC + response header)</li>
 *   <li>REST call details — endpoint, request payload, status and response message</li>
 * </ol>
 * Registered automatically by Spring Boot as a servlet {@link Filter} bean.
 */
@Component
@Order(1)
public class TransactionLoggingFilter implements Filter {

    /** Transaction-level logger (correlated via MDC transactionId). */
    private static final Logger TX_LOG = LoggerFactory.getLogger("com.gymcrm.logging.Transaction");

    /** REST call detail logger. */
    private static final Logger REST_LOG = LoggerFactory.getLogger("com.gymcrm.logging.RestCall");

    private static final int MAX_BODY_LOG_CHARS = 2000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        if (shouldSkip(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String transactionId = TransactionContext.getOrCreate(
                httpRequest.getHeader(TransactionContext.HEADER));
        TransactionContext.set(transactionId);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        wrappedResponse.setHeader(TransactionContext.HEADER, transactionId);

        long started = System.currentTimeMillis();
        TX_LOG.info("Transaction started transactionId={}", transactionId);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            String requestSummary = buildRequestSummary(wrappedRequest);
            String responseBody = readBody(wrappedResponse.getContentAsByteArray());
            String responseMessage = resolveResponseMessage(status, responseBody);
            long durationMs = System.currentTimeMillis() - started;

            if (status >= 400) {
                REST_LOG.warn(
                        "REST call endpoint={} {} request={} status={} responseMessage={} durationMs={}",
                        wrappedRequest.getMethod(),
                        wrappedRequest.getRequestURI(),
                        requestSummary,
                        status,
                        responseMessage,
                        durationMs);
                TX_LOG.warn("Transaction finished transactionId={} status={} durationMs={}",
                        transactionId, status, durationMs);
            } else {
                REST_LOG.info(
                        "REST call endpoint={} {} request={} status={} responseMessage={} durationMs={}",
                        wrappedRequest.getMethod(),
                        wrappedRequest.getRequestURI(),
                        requestSummary,
                        status,
                        responseMessage,
                        durationMs);
                TX_LOG.info("Transaction finished transactionId={} status={} durationMs={}",
                        transactionId, status, durationMs);
            }

            wrappedResponse.copyBodyToResponse();
            TransactionContext.clear();
        }
    }

    private static boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && (path.startsWith("/webjars/")
                || path.equals("/swagger-ui.html")
                || path.startsWith("/swagger-ui"));
    }

    private static String buildRequestSummary(ContentCachingRequestWrapper request) {
        StringBuilder sb = new StringBuilder();
        String query = SensitiveDataMasker.maskQuery(request.getQueryString());
        if (query != null && !query.isEmpty()) {
            sb.append("query=").append(query);
        }
        String body = SensitiveDataMasker.maskBody(readBody(request.getContentAsByteArray()));
        if (body != null && !body.isBlank()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("body=").append(truncate(body));
        }
        return sb.length() == 0 ? "(empty)" : sb.toString();
    }

    private static String resolveResponseMessage(int status, String body) {
        if (status >= 200 && status < 300) {
            if (body == null || body.isBlank()) {
                return "OK";
            }
            return truncate(body);
        }
        if (body == null || body.isBlank()) {
            return HttpStatusText.of(status);
        }
        return truncate(body);
    }

    private static String readBody(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String truncate(String value) {
        if (value.length() <= MAX_BODY_LOG_CHARS) {
            return value;
        }
        return value.substring(0, MAX_BODY_LOG_CHARS) + "...(truncated)";
    }

    /** Minimal status text without depending on Spring HttpStatus in the filter helper. */
    private static final class HttpStatusText {
        static String of(int status) {
            return switch (status) {
                case 400 -> "Bad Request";
                case 401 -> "Unauthorized";
                case 404 -> "Not Found";
                case 405 -> "Method Not Allowed";
                case 415 -> "Unsupported Media Type";
                case 500 -> "Internal Server Error";
                default -> "HTTP " + status;
            };
        }
    }
}
