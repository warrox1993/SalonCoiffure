package com.jb.afrostyle.booking.service;

import com.jb.afrostyle.booking.domain.enums.BookingStatus;
import com.jb.afrostyle.booking.domain.entity.Booking;
import com.jb.afrostyle.booking.domain.entity.SalonReport;
import com.jb.afrostyle.booking.dto.BookingRequest;
import com.jb.afrostyle.salon.payload.dto.SalonDTO;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.user.dto.UserDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface BookingService {

    /**
     * Crée une nouvelle réservation
     */
    Booking createBooking(BookingRequest booking,
                          UserDTO user,
                          SalonDTO salon,
                          Set<ServiceDTO> serviceDTOSet) throws Exception;

    /**
     * Récupère les réservations d'un client
     */
    List<Booking> getBookingsByCustomer(Long customerId);

    /**
     * Récupère toutes les réservations
     */
    List<Booking> getAllBookings();

    /**
     * Récupère une réservation par son ID
     */
    Booking getBookingById(Long bookingId) throws Exception;

    /**
     * Met à jour une réservation avec un nouvel objet Booking
     */
    Booking updateBooking(Booking existingBooking, Booking updatedBooking);

    /**
     * Met à jour le statut d'une réservation
     */
    Booking updateBooking(Long bookingId, BookingStatus status) throws Exception;

    /**
     * Récupère les réservations par date
     */
    List<Booking> getBookingsByDate(LocalDate date);

    /**
     * Génère un rapport pour le salon
     */
    SalonReport getSalonReport();
}