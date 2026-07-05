package com.exam.seating.config;

import com.exam.seating.security.RoleBasedLoginSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final RoleBasedLoginSuccessHandler successHandler;

    public SecurityConfig(RoleBasedLoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .securityContext(securityContext -> securityContext
                .requireExplicitSave(false)
            )

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/",
                        "/app/**",
                        "/api/auth/**",
                        "/api/admin-access/**",
                        "/api/admin/register",
                        "/api/auth/update-password",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                    ).permitAll()

                    .requestMatchers(
                        "/admin-dashboard",
                        "/students",
                        "/exams",
                        "/rooms",
                        "/invigilators",
                        "/generate-seating",
                        "/generate-roll-hall",
                        "/view-seating",
                        "/charts",
                        "/api/hall-tickets"
                    ).hasAuthority("ADMIN")

                    .requestMatchers("/api/admin/**")
                    .hasAuthority("ADMIN")

                    .requestMatchers(
                        "/invigilator-dashboard",
                        "/invigilator-profile",
                        "/assigned-exams"
                    ).hasAuthority("INVIGILATOR")

                    .requestMatchers(
                            "/api/student/**",
                            "/student-dashboard",
                            "/student-profile"
                    ).hasAuthority("STUDENT")

                    .anyRequest().authenticated()
                )

                .formLogin(form -> form
                    .loginPage("/")
                    .loginProcessingUrl("/api/auth/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .successHandler(successHandler)
                    .permitAll()
                )

                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    })
                )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }
}
