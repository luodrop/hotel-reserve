package com.hotelreserve.repository;

import com.hotelreserve.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = "SELECT r.*, u.username, u.real_name, rm.type_name, rm.price as room_price, h.name as hotel_name " +
           "FROM reservations r " +
           "JOIN users u ON r.user_id = u.id " +
           "JOIN rooms rm ON r.room_id = rm.id " +
           "JOIN hotels h ON rm.hotel_id = h.id " +
           "ORDER BY r.created_at DESC LIMIT ?1", nativeQuery = true)
    List<Object[]> findRecentWithDetails(int limit);

    @Query(value = "SELECT r.*, u.username, u.real_name, rm.type_name, rm.price as room_price, h.name as hotel_name " +
           "FROM reservations r " +
           "JOIN users u ON r.user_id = u.id " +
           "JOIN rooms rm ON r.room_id = rm.id " +
           "JOIN hotels h ON rm.hotel_id = h.id " +
           "ORDER BY r.created_at DESC", nativeQuery = true)
    List<Object[]> findAllWithDetails();

    @Query(value = "SELECT r.*, u.username, u.real_name, rm.type_name, rm.price as room_price, h.name as hotel_name " +
           "FROM reservations r " +
           "JOIN users u ON r.user_id = u.id " +
           "JOIN rooms rm ON r.room_id = rm.id " +
           "JOIN hotels h ON rm.hotel_id = h.id " +
           "WHERE r.user_id = ?1 " +
           "ORDER BY r.created_at DESC", nativeQuery = true)
    List<Object[]> findByUserIdWithDetails(Long userId);

    long countByStatus(String status);
}
