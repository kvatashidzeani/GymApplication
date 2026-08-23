package com.gymcrm.config;

import com.gymcrm.security.JsonAuthenticationEntryPoint;
import com.gymcrm.security.JsonLogoutSuccessHandler;
import com.gymcrm.security.JwtAuthenticationFilter;
import com.gymcrm.security.JwtLogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security with JWT Bearer tokens and CORS.
 * <p>
 * Assignment rules:
 * <ul>
 *   <li>Create Trainee/Trainer profile — public; username ({@code FirstName.LastName[+n]})
 *       and password (10 chars) are generated server-side (previous module).</li>
 *   <li>All other business operations require Trainee/Trainer authentication
 *       ({@code Authorization: Bearer <JWT>} from register or {@code GET /login}).</li>
 * </ul>
 * Also public (non-business): CORS preflight {@code OPTIONS}, login (to obtain JWT),
 * Swagger/OpenAPI docs.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonLogoutSuccessHandler logoutSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLogoutHandler jwtLogoutHandler;

    public SecurityConfig(JsonAuthenticationEntryPoint authenticationEntryPoint,
                          JsonLogoutSuccessHandler logoutSuccessHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtLogoutHandler jwtLogoutHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.logoutSuccessHandler = logoutSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtLogoutHandler = jwtLogoutHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/trainees/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/trainers/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/login").permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/docs",
                                "/v3/api-docs",
                                "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .addLogoutHandler(jwtLogoutHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(logoutSuccessHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
