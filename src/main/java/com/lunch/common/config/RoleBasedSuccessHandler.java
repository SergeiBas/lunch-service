package com.lunch.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String target = "/";
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            switch (authority.getAuthority()) {
                case "ROLE_ORDER_MANAGER" -> target = "/manager/test";
                case "ROLE_COLLECTOR_MANAGER" -> target = "/collector/rooms";
                case "ROLE_COURIER" -> target = "/courier/test";
                default -> { }
            }
        }
        response.sendRedirect(request.getContextPath() + target);
    }
}