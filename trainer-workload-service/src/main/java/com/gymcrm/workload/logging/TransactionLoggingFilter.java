package com.gymcrm.workload.logging;

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
 * Transaction-level logging. Reuses {@code X-Transaction-Id} from Gym CRM when present.
 */
@Component
@Order(1)
public class TransactionLoggingFilter implements Filter {

    private static final Logger TX_LOG = LoggerFactory.getLogger("com.gymcrm.workload.logging.Transaction");
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

        String endpoint = wrappedRequest.getMethod() + " " + wrappedRequest.getRequestURI();
        long started = System.currentTimeMillis();
        TX_LOG.info("Transaction started transactionId={} endpoint={}", transactionId, endpoint);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            String requestSummary = buildRequestSummary(wrappedRequest);
            String responseBody = readBody(wrappedResponse.getContentAsByteArray());
            String responseMessage = (status >= 200 && status < 300)
                    ? (responseBody.isBlank() ? "OK" : truncate(responseBody))
                    : (responseBody.isBlank() ? "HTTP " + status : truncate(responseBody));
            long durationMs = System.currentTimeMillis() - started;

            if (status >= 400) {
                TX_LOG.warn(
                        "Transaction finished transactionId={} endpoint={} request={} status={} responseMessage={} durationMs={}",
                        transactionId, endpoint, requestSummary, status, responseMessage, durationMs);
            } else {
                TX_LOG.info(
                        "Transaction finished transactionId={} endpoint={} request={} status={} responseMessage={} durationMs={}",
                        transactionId, endpoint, requestSummary, status, responseMessage, durationMs);
            }

            wrappedResponse.copyBodyToResponse();
            TransactionContext.clear();
        }
    }

    private static boolean shouldSkip(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/actuator/");
    }

    private static String buildRequestSummary(ContentCachingRequestWrapper request) {
        String query = request.getQueryString();
        String body = readBody(request.getContentAsByteArray());
        if ((query == null || query.isEmpty()) && body.isBlank()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            sb.append("query=").append(query);
        }
        if (!body.isBlank()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append("body=").append(truncate(body));
        }
        return sb.toString();
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
}
