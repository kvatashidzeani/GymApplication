package com.gymcrm.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonLogoutSuccessHandlerTest {

    private final JsonLogoutSuccessHandler handler = new JsonLogoutSuccessHandler();

    @Test
    void onLogoutSuccess_returns200() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "Ani.Smith", "n/a", List.of());
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onLogoutSuccess(new MockHttpServletRequest(), response, authentication);

        assertEquals(200, response.getStatus());
    }

    @Test
    void onLogoutSuccess_withoutPrincipal_returns200() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onLogoutSuccess(new MockHttpServletRequest(), response, null);

        assertEquals(200, response.getStatus());
    }
}
