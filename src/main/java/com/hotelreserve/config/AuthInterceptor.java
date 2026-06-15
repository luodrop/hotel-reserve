package com.hotelreserve.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String path = request.getRequestURI();

        if (path.startsWith("/user/") || path.startsWith("/admin/")) {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                session.setAttribute("flashMessage", "请先登录");
                session.setAttribute("flashType", "error");
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }

            String role = (String) session.getAttribute("role");
            if (path.startsWith("/admin/") && !"ADMIN".equals(role)) {
                session.setAttribute("flashMessage", "无权限访问此页面");
                session.setAttribute("flashType", "error");
                response.sendRedirect(request.getContextPath() + "/");
                return false;
            }

            if (path.startsWith("/user/") && !"USER".equals(role)) {
                session.setAttribute("flashMessage", "无权限访问此页面");
                session.setAttribute("flashType", "error");
                response.sendRedirect(request.getContextPath() + "/");
                return false;
            }
        }

        return true;
    }
}
