package com.hotelreserve.config;

import com.hotelreserve.entity.Hotel;
import com.hotelreserve.entity.Room;
import com.hotelreserve.entity.User;
import com.hotelreserve.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class SeedDataConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo, HotelRepository hotelRepo,
                                    RoomRepository roomRepo) {
        return args -> {
            if (userRepo.count() == 0) {
                PasswordEncoder encoder = passwordEncoder();

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRealName("系统管理员");
                admin.setPhone("13800000000");
                admin.setRole("ADMIN");
                admin.setCreatedAt(LocalDateTime.now());
                userRepo.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setPassword(encoder.encode("user123"));
                user.setRealName("测试用户");
                user.setPhone("13900000000");
                user.setRole("USER");
                user.setCreatedAt(LocalDateTime.now());
                userRepo.save(user);

                System.out.println("Default users created: admin/admin123, user/user123");
            }

            if (hotelRepo.count() == 0) {
                Hotel h1 = createHotel("海景大酒店", "三亚市天涯区滨海路88号", "三亚",
                    "坐落于三亚湾核心地带，拥有绝佳海景视野。配备无边泳池、SPA中心、多个餐厅及宴会厅，是度假和商务出行的理想之选。所有客房均面朝大海。",
                    4.8, "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800");
                h1 = hotelRepo.save(h1);
                roomRepo.save(createRoom(h1.getId(), "海景大床房", 688.00, 2,
                    "42㎡ 海景大床房，落地窗正对海景，配备舒适大床、智能客房系统",
                    "https://images.unsplash.com/photo-1590490360182-c33d57733427?w=400"));
                roomRepo.save(createRoom(h1.getId(), "豪华海景套房", 1288.00, 3,
                    "68㎡ 豪华套房，独立客厅+卧室，270度环幕海景，管家服务",
                    "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=400"));
                roomRepo.save(createRoom(h1.getId(), "海景双床房", 558.00, 2,
                    "36㎡ 舒适双床房，适合朋友或家庭入住，观景阳台",
                    "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=400"));

                Hotel h2 = createHotel("园林温泉度假酒店", "南京市汤山温泉度假区温泉路8号", "南京",
                    "占地200亩的园林式温泉度假酒店，拥有天然温泉入户。日式枯山水庭院、露天风吕、私汤别墅一应俱全。远离城市喧嚣，尽享宁静与放松。",
                    4.6, "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=800");
                h2 = hotelRepo.save(h2);
                roomRepo.save(createRoom(h2.getId(), "温泉大床房", 568.00, 2,
                    "38㎡ 配备私汤泡池，温泉直引入室，舒适大床",
                    "https://images.unsplash.com/photo-1560185007-cde436f6a4d0?w=400"));
                roomRepo.save(createRoom(h2.getId(), "温泉别墅", 1688.00, 4,
                    "120㎡ 独栋别墅，私家花园+露天温泉池，烧烤区",
                    "https://images.unsplash.com/photo-1582719508461-905c673771fd?w=400"));
                roomRepo.save(createRoom(h2.getId(), "日式榻榻米房", 458.00, 2,
                    "28㎡ 日式风格榻榻米房，竹帘纸窗，禅意十足",
                    "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=400"));

                Hotel h3 = createHotel("城市便捷酒店（市中心店）", "上海市黄浦区南京东路100号", "上海",
                    "位于南京路步行街核心位置，距离外滩仅500米。交通便利，周边商圈繁华。现代化的装修风格，性价比极高的城市商务酒店选择。",
                    4.3, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=800");
                h3 = hotelRepo.save(h3);
                roomRepo.save(createRoom(h3.getId(), "商务大床房", 348.00, 2,
                    "28㎡ 商务大床房，书桌办公区，高速WiFi",
                    "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400"));
                roomRepo.save(createRoom(h3.getId(), "标准双床房", 298.00, 2,
                    "26㎡ 标准双床房，简约舒适，城市景观",
                    "https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=400"));
                roomRepo.save(createRoom(h3.getId(), "家庭套房", 498.00, 4,
                    "45㎡ 家庭套房，一室一厅格局，适合家庭出游",
                    "https://images.unsplash.com/photo-1595576508898-0ad5c879a061?w=400"));

                System.out.println("Sample hotel data created (3 hotels + 9 room types)");
            }
        };
    }

    private Hotel createHotel(String name, String address, String city, String description, Double rating, String images) {
        Hotel h = new Hotel();
        h.setName(name);
        h.setAddress(address);
        h.setCity(city);
        h.setDescription(description);
        h.setRating(rating);
        h.setImages(images);
        h.setCreatedAt(LocalDateTime.now());
        return h;
    }

    private Room createRoom(Long hotelId, String typeName, Double price, Integer capacity, String description, String image) {
        Room r = new Room();
        r.setHotelId(hotelId);
        r.setTypeName(typeName);
        r.setPrice(price);
        r.setCapacity(capacity);
        r.setDescription(description);
        r.setImage(image);
        return r;
    }
}
