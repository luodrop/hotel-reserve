package com.hotelreserve.controller;

import com.hotelreserve.entity.User;
import com.hotelreserve.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            HttpSession session, Model model) {
        if ("true".equals(error)) {
            session.setAttribute("flashMessage", "用户名或密码错误，请重试");
            session.setAttribute("flashType", "error");
        }
        if ("true".equals(logout)) {
            session.setAttribute("flashMessage", "您已成功退出登录");
            session.setAttribute("flashType", "success");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes redirectAttrs) {
        var opt = userRepo.findByUsername(username);
        if (opt.isPresent() && passwordEncoder.matches(password, opt.get().getPassword())) {
            User u = opt.get();
            session.setAttribute("userId", u.getId());
            session.setAttribute("role", u.getRole());
            session.setAttribute("username", u.getUsername());

            if ("ADMIN".equals(u.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/";
        }

        session.setAttribute("flashMessage", "用户名或密码错误，请重试");
        session.setAttribute("flashType", "error");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password,
                           @RequestParam(required = false) String confirmPassword,
                           @RequestParam(required = false) String realName,
                           @RequestParam(required = false) String phone,
                           HttpSession session) {
        if (username.isEmpty() || password.isEmpty()) {
            session.setAttribute("flashMessage", "用户名和密码不能为空");
            session.setAttribute("flashType", "error");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            session.setAttribute("flashMessage", "两次密码输入不一致");
            session.setAttribute("flashType", "error");
            return "redirect:/register";
        }
        if (password.length() < 6) {
            session.setAttribute("flashMessage", "密码至少6位");
            session.setAttribute("flashType", "error");
            return "redirect:/register";
        }

        if (userRepo.findByUsername(username).isPresent()) {
            session.setAttribute("flashMessage", "用户名已存在");
            session.setAttribute("flashType", "error");
            return "redirect:/register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        userRepo.save(user);

        session.setAttribute("flashMessage", "注册成功，请登录");
        session.setAttribute("flashType", "success");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}
