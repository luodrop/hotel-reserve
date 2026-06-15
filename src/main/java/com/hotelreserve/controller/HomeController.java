package com.hotelreserve.controller;

import com.hotelreserve.entity.Hotel;
import com.hotelreserve.entity.Room;
import com.hotelreserve.repository.HotelRepository;
import com.hotelreserve.repository.RoomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final HotelRepository hotelRepo;
    private final RoomRepository roomRepo;

    public HomeController(HotelRepository hotelRepo, RoomRepository roomRepo) {
        this.hotelRepo = hotelRepo;
        this.roomRepo = roomRepo;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String keyword,
                        HttpSession session, Model model) {
        java.util.List<Hotel> hotels;
        if (keyword != null && !keyword.trim().isEmpty()) {
            hotels = hotelRepo.searchByKeyword(keyword.trim());
        } else {
            hotels = hotelRepo.findAllByOrderByCreatedAtDesc();
        }
        model.addAttribute("hotels", hotels);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "index";
    }

    @GetMapping("/hotel/{hotelId}")
    public String hotelDetail(@PathVariable Long hotelId, HttpSession session, Model model) {
        var opt = hotelRepo.findById(hotelId);
        if (opt.isEmpty()) {
            session.setAttribute("flashMessage", "酒店不存在");
            session.setAttribute("flashType", "error");
            return "redirect:/";
        }
        Hotel hotel = opt.get();
        java.util.List<Room> rooms = roomRepo.findByHotelIdOrderByPriceAsc(hotelId);
        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", rooms);
        return "hotel-detail";
    }
}
