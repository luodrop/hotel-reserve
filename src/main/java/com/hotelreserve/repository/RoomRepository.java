package com.hotelreserve.repository;

import com.hotelreserve.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelIdOrderByPriceAsc(Long hotelId);
    void deleteByHotelId(Long hotelId);
}
