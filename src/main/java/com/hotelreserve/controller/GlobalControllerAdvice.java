package com.hotelreserve.controller;

import com.hotelreserve.entity.User;
import com.hotelreserve.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepo;

    public GlobalControllerAdvice(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @ModelAttribute
    public void addGlobalAttributes(HttpSession session, Model model) {
        // Current user
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            userRepo.findById(userId).ifPresent(u -> model.addAttribute("currentUser", u));
        }

        // Flash messages from session
        String flashMessage = (String) session.getAttribute("flashMessage");
        String flashType = (String) session.getAttribute("flashType");
        if (flashMessage != null) {
            model.addAttribute("flashMessage", flashMessage);
            model.addAttribute("flashType", flashType != null ? flashType : "info");
            session.removeAttribute("flashMessage");
            session.removeAttribute("flashType");
        }

        // Helper methods
        model.addAttribute("fmt", (java.util.function.Function<Object, String>) val -> {
            if (val == null) return "\u00a50.00";
            double amount = ((java.lang.Number) val).doubleValue();
            return String.format("\u00a5%,.2f", amount);
        });

        model.addAttribute("nights", (java.util.function.BiFunction<Object, Object, Long>) (checkIn, checkOut) -> {
            if (checkIn == null || checkOut == null) return 0L;
            java.time.LocalDate ci;
            java.time.LocalDate co;
            if (checkIn instanceof java.sql.Timestamp) {
                ci = ((java.sql.Timestamp) checkIn).toLocalDateTime().toLocalDate();
            } else
            if (checkIn instanceof java.sql.Date) {
                ci = ((java.sql.Date) checkIn).toLocalDate();
            } else {
                ci = (java.time.LocalDate) checkIn;
            }
            if (checkOut instanceof java.sql.Timestamp) {
                co = ((java.sql.Timestamp) checkOut).toLocalDateTime().toLocalDate();
            } else
            if (checkOut instanceof java.sql.Date) {
                co = ((java.sql.Date) checkOut).toLocalDate();
            } else {
                co = (java.time.LocalDate) checkOut;
            }
            long days = java.time.temporal.ChronoUnit.DAYS.between(ci, co);
            return Math.max(0, days);
        });

        model.addAttribute("abbreviate", (java.util.function.BiFunction<String, Integer, String>) (text, length) -> {
            if (text == null) return "";
            int len = length != null ? length : 80;
            return text.length() > len ? text.substring(0, len) + "..." : text;
        });
    }
}
