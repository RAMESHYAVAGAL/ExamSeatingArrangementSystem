package com.exam.seating.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

        HttpSession session = request.getSession();

        session.setAttribute("USER_ID", user.getUserId());
        session.setAttribute("ROLE", user.getRole());
        session.setAttribute("USER_EMAIL", user.getUsername());

        switch (user.getRole()) {
            case "ADMIN":
                response.sendRedirect("/admin-dashboard");
                break;
            case "INVIGILATOR":
                response.sendRedirect("/invigilator-dashboard");
                break;
            case "STUDENT":
                response.sendRedirect("/student-dashboard");
                break;
            default:
                response.sendRedirect("/");
                break;
        }
    }
}