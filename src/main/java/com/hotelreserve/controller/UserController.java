package com.hotelreserve.controller;

import com.hotelreserve.entity.Reservation;
import com.hotelreserve.entity.Room;
import com.hotelreserve.entity.User;
import com.hotelreserve.repository.ReservationRepository;
import com.hotelreserve.repository.RoomRepository;
import com.hotelreserve.repository.UserRepository;
import com.hotelreserve.service.AlipayService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepo;
    private final RoomRepository roomRepo;
    private final ReservationRepository reservationRepo;
    private final AlipayService alipayService;

    public UserController(UserRepository userRepo, RoomRepository roomRepo,
                          ReservationRepository reservationRepo, AlipayService alipayService) {
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.reservationRepo = reservationRepo;
        this.alipayService = alipayService;
    }

    @GetMapping("/user/home")
    public String userHome(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepo.findById(userId).orElse(null);
        List<Object[]> reservations = reservationRepo.findByUserIdWithDetails(userId);
        model.addAttribute("user", user);
        model.addAttribute("reservations", reservations);
        return "user/home";
    }

    @GetMapping("/user/reservations")
    public String userReservations(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        List<Object[]> reservations = reservationRepo.findByUserIdWithDetails(userId);
        model.addAttribute("reservations", reservations);
        return "user/reservations";
    }

    @PostMapping("/user/reserve")
    public String userReserve(@RequestParam Long roomId,
                              @RequestParam String checkIn,
                              @RequestParam String checkOut,
                              @RequestParam(defaultValue = "1") Integer guests,
                              @RequestParam(required = false) String note,
                              @RequestParam String phone,
                              HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (roomId == null || checkIn.isEmpty() || checkOut.isEmpty() || phone.isEmpty()) {
            session.setAttribute("flashMessage", "请填写完整信息");
            session.setAttribute("flashType", "error");
            return "redirect:/";
        }

        LocalDate ci, co;
        try {
            ci = LocalDate.parse(checkIn);
            co = LocalDate.parse(checkOut);
        } catch (Exception e) {
            session.setAttribute("flashMessage", "日期格式错误");
            session.setAttribute("flashType", "error");
            return "redirect:/";
        }

        if (!co.isAfter(ci)) {
            session.setAttribute("flashMessage", "退房日期必须在入住日期之后");
            session.setAttribute("flashType", "error");
            return "redirect:/";
        }

        long nights = ChronoUnit.DAYS.between(ci, co);
        var roomOpt = roomRepo.findById(roomId);
        if (roomOpt.isEmpty()) {
            session.setAttribute("flashMessage", "房型不存在");
            session.setAttribute("flashType", "error");
            return "redirect:/";
        }

        Room room = roomOpt.get();
        double total = room.getPrice() * nights;

        Reservation resv = new Reservation();
        resv.setUserId(userId);
        resv.setRoomId(roomId);
        resv.setCheckIn(ci);
        resv.setCheckOut(co);
        resv.setGuests(guests);
        resv.setTotalAmount(total);
        resv.setStatus("UNPAID");
        resv.setNote(note);
        resv.setPhone(phone);
        resv.setCreatedAt(LocalDateTime.now());
        reservationRepo.save(resv);

        // Redirect to payment page
        return "redirect:/user/pay?reservationId=" + resv.getId();
    }

    @GetMapping("/user/pay")
    public String userPay(@RequestParam Long reservationId, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        var opt = reservationRepo.findById(reservationId);
        if (opt.isEmpty() || !opt.get().getUserId().equals(userId)) {
            session.setAttribute("flashMessage", "预约不存在");
            session.setAttribute("flashType", "error");
            return "redirect:/user/home";
        }
        Reservation resv = opt.get();
        if (!"UNPAID".equals(resv.getStatus())) {
            return "redirect:/user/home";
        }

        model.addAttribute("reservation", resv);
        model.addAttribute("totalAmount", String.format("%.2f", resv.getTotalAmount()));

        String orderId = "RSV" + resv.getId();
        String subject = "酒店预约 - #" + resv.getId();
      try {
          String payHtml = alipayService.createPayPage(orderId, String.format("%.2f", resv.getTotalAmount()), subject);
          model.addAttribute("payHtml", payHtml);
      } catch (Exception e) {
           log.error("Alipay pay page creation failed for order {}: {}", orderId, e.getMessage(), e);
          model.addAttribute("payHtml", "");
          model.addAttribute("alipayError", true);
           model.addAttribute("alipayErrorMessage", "支付网关连接超时，您稍后可以在【我的预约】中完成支付。");
      }

        return "alipay-pay";
    }

    @PostMapping("/user/cancel/{resvId}")
    public String userCancel(@PathVariable Long resvId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        reservationRepo.findById(resvId).ifPresent(r -> {
            if (r.getUserId().equals(userId)) {
                r.setStatus("CANCELLED");
                reservationRepo.save(r);
            }
        });
        session.setAttribute("flashMessage", "预约已取消");
        session.setAttribute("flashType", "success");
        return "redirect:/user/reservations";
    }

    @GetMapping("/user/profile")
    public String userProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userRepo.findById(userId).orElse(null);
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/user/profile")
    public String userProfileUpdate(@RequestParam(required = false) String realName,
                                    @RequestParam(required = false) String phone,
                                    HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        userRepo.findById(userId).ifPresent(u -> {
            u.setRealName(realName);
            u.setPhone(phone);
            userRepo.save(u);
        });
        session.setAttribute("flashMessage", "个人信息已更新");
        session.setAttribute("flashType", "success");
        return "redirect:/user/profile";
    }
}
