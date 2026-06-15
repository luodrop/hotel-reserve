package com.hotelreserve.repository;

import com.hotelreserve.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    @Query("SELECT h FROM Hotel h WHERE h.name LIKE %:keyword% OR h.city LIKE %:keyword% OR h.address LIKE %:keyword% ORDER BY h.createdAt DESC")
    List<Hotel> searchByKeyword(String keyword);

    List<Hotel> findAllByOrderByCreatedAtDesc();
}
