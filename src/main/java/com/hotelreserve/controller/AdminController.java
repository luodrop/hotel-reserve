package com.hotelreserve.controller;

import com.hotelreserve.entity.Hotel;
import com.hotelreserve.entity.Room;
import com.hotelreserve.repository.HotelRepository;
import com.hotelreserve.repository.ReservationRepository;
import com.hotelreserve.repository.RoomRepository;
import com.hotelreserve.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class AdminController {

    private final HotelRepository hotelRepo;
    private final RoomRepository roomRepo;
    private final ReservationRepository reservationRepo;
    private final UserRepository userRepo;

    public AdminController(HotelRepository hotelRepo, RoomRepository roomRepo,
                           ReservationRepository reservationRepo, UserRepository userRepo) {
        this.hotelRepo = hotelRepo;
        this.roomRepo = roomRepo;
        this.reservationRepo = reservationRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        long totalHotels = hotelRepo.count();
        long totalReservations = reservationRepo.count();
        long totalUsers = userRepo.count();
        long unpaid = reservationRepo.countByStatus("UNPAID");
        long pending = reservationRepo.countByStatus("PENDING");
        long confirmed = reservationRepo.countByStatus("CONFIRMED");
        long cancelled = reservationRepo.countByStatus("CANCELLED");
        var recent = reservationRepo.findRecentWithDetails(10);

        model.addAttribute("total_hotels", totalHotels);
        model.addAttribute("total_reservations", totalReservations);
        model.addAttribute("total_users", totalUsers);
        model.addAttribute("unpaid_count", unpaid);
        model.addAttribute("pending_count", pending);
        model.addAttribute("confirmed_count", confirmed);
        model.addAttribute("cancelled_count", cancelled);
        model.addAttribute("recent_reservations", recent);
        return "admin/dashboard";
    }

    @GetMapping("/admin/hotels")
    public String hotels(Model model) {
        model.addAttribute("hotels", hotelRepo.findAllByOrderByCreatedAtDesc());
        return "admin/hotels";
    }

    @PostMapping("/admin/hotels/save")
    public String hotelsSave(@RequestParam(required = false) Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String city,
                             @RequestParam(required = false) String description,
                             @RequestParam(defaultValue = "0") Double rating,
                             @RequestParam(required = false) String images,
                             HttpSession session) {
        Hotel hotel;
        if (id != null) {
            hotel = hotelRepo.findById(id).orElse(new Hotel());
        } else {
            hotel = new Hotel();
            hotel.setCreatedAt(LocalDateTime.now());
        }
        hotel.setName(name);
        hotel.setAddress(address);
        hotel.setCity(city);
        hotel.setDescription(description);
        hotel.setRating(rating);
        hotel.setImages(images);
        hotelRepo.save(hotel);

        session.setAttribute("flashMessage", "酒店已保存");
        session.setAttribute("flashType", "success");
        return "redirect:/admin/hotels";
    }

    @PostMapping("/admin/hotels/delete/{hid}")
    public String hotelsDelete(@PathVariable Long hid, HttpSession session) {
        roomRepo.deleteByHotelId(hid);
        hotelRepo.deleteById(hid);
        session.setAttribute("flashMessage", "酒店已删除");
        session.setAttribute("flashType", "success");
        return "redirect:/admin/hotels";
    }

    @GetMapping("/admin/rooms")
    public String rooms(@RequestParam Long hotelId, HttpSession session, Model model) {
        var opt = hotelRepo.findById(hotelId);
        if (opt.isEmpty()) {
            session.setAttribute("flashMessage", "请选择酒店");
            session.setAttribute("flashType", "error");
            return "redirect:/admin/hotels";
        }
        model.addAttribute("hotel", opt.get());
        model.addAttribute("rooms", roomRepo.findByHotelIdOrderByPriceAsc(hotelId));
        return "admin/rooms";
    }

    @PostMapping("/admin/rooms/save")
    public String roomsSave(@RequestParam(required = false) Long id,
                            @RequestParam Long hotelId,
                            @RequestParam String typeName,
                            @RequestParam(defaultValue = "0") Double price,
                            @RequestParam(defaultValue = "1") Integer capacity,
                            @RequestParam(required = false) String description,
                            @RequestParam(required = false) String image,
                            HttpSession session) {
        Room room;
        if (id != null) {
            room = roomRepo.findById(id).orElse(new Room());
        } else {
            room = new Room();
        }
        room.setHotelId(hotelId);
        room.setTypeName(typeName);
        room.setPrice(price);
        room.setCapacity(capacity);
        room.setDescription(description);
        room.setImage(image);
        roomRepo.save(room);

        session.setAttribute("flashMessage", "房型已保存");
        session.setAttribute("flashType", "success");
        return "redirect:/admin/rooms?hotelId=" + hotelId;
    }

    @PostMapping("/admin/rooms/delete/{rid}")
    public String roomsDelete(@PathVariable Long rid,
                              @RequestParam Long hotelId,
                              HttpSession session) {
        roomRepo.deleteById(rid);
        session.setAttribute("flashMessage", "房型已删除");
        session.setAttribute("flashType", "success");
        return "redirect:/admin/rooms?hotelId=" + hotelId;
    }

    @GetMapping("/admin/reservations")
    public String reservations(Model model) {
        model.addAttribute("reservations", reservationRepo.findAllWithDetails());
        return "admin/reservations";
    }

    @PostMapping("/admin/reservations/confirm/{rid}")
    public String reservationsConfirm(@PathVariable Long rid, HttpSession session) {
        reservationRepo.findById(rid).ifPresent(r -> {
            r.setStatus("CONFIRMED");
            reservationRepo.save(r);
        });
        session.setAttribute("flashMessage", "预约已确认");
        session.setAttribute("flashType", "success");
        return "redirect:/admin/reservations";
    }

    @PostMapping("/admin/reservations/cancel/{rid}")
    public String reservationsCancel(@PathVariable Long rid, HttpSession session) {
        reservationRepo.findById(rid).ifPresent(r -> {
            r.setStatus("CANCELLED");
            reservationRepo.save(r);
        });
        session.setAttribute("flashMessage", "预约已取消");
        session.setAttribute("flashType", "success");
        return "redirect:/admin/reservations";
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }
}
