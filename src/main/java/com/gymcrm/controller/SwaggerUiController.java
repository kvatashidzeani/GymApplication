package com.gymcrm.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Swagger UI page for Gym CRM (loads /v3/api-docs).
 */
@Controller
public class SwaggerUiController {

    private static final String HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <title>Gym CRM API - Swagger UI</title>
              <link rel="stylesheet" type="text/css" href="/webjars/swagger-ui/swagger-ui.css">
              <style>html { box-sizing: border-box; overflow-y: scroll; } body { margin: 0; background: #fafafa; }</style>
            </head>
            <body>
              <div id="swagger-ui"></div>
              <script src="/webjars/swagger-ui/swagger-ui-bundle.js"></script>
              <script src="/webjars/swagger-ui/swagger-ui-standalone-preset.js"></script>
              <script>
                window.onload = function () {
                  if (typeof SwaggerUIBundle === 'undefined') {
                    document.body.innerHTML = '<h2 style="font-family:sans-serif;padding:2rem">'
                      + 'Swagger UI scripts failed to load.<br>'
                      + 'Open <a href="/v3/api-docs">/v3/api-docs</a> to verify the API is up,'
                      + ' then restart the app after Maven reload.</h2>';
                    return;
                  }
                  window.ui = SwaggerUIBundle({
                    url: '/v3/api-docs',
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [
                      SwaggerUIBundle.presets.apis,
                      SwaggerUIStandalonePreset
                    ],
                    layout: 'StandaloneLayout'
                  });
                };
              </script>
            </body>
            </html>
            """;

    @GetMapping({"/swagger-ui.html", "/docs"})
    public void swaggerUi(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(HTML);
    }
}
