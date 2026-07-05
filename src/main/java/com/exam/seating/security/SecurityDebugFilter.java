package com.exam.seating.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityDebugFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        System.out.println("🌐 REQUEST: " + req.getMethod() + " " + req.getRequestURI());
        System.out.println("🌐 Session exists: " + (session != null));

        if (session != null) {
            System.out.println("🌐 Session ID: " + session.getId());
        }

        System.out.println("🌐 Auth: " + SecurityContextHolder.getContext().getAuthentication());

        chain.doFilter(request, response);
    }
}
