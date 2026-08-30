package com.gymcrm.workload.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionLoggingFilterTest {

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void assignsTransactionIdAndExposesHeader() throws Exception {
        TransactionLoggingFilter filter = new TransactionLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/workload/Mike.Brown");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);
        doAnswer(inv -> {
            assertNotNull(TransactionContext.get());
            response.setStatus(200);
            return null;
        }).when(chain).doFilter(any(), any());

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader(TransactionContext.HEADER));
        assertNull(TransactionContext.get());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void reusesIncomingTransactionId() throws Exception {
        TransactionLoggingFilter filter = new TransactionLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/workload/Mike.Brown");
        request.addHeader(TransactionContext.HEADER, "incoming-tx-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> response.setStatus(200);
        filter.doFilter(request, response, chain);

        assertEquals("incoming-tx-id", response.getHeader(TransactionContext.HEADER));
    }

    @Test
    void skipsActuatorHealth() throws Exception {
        TransactionLoggingFilter filter = new TransactionLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(response.getHeader(TransactionContext.HEADER));
    }
}
