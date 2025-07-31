package com.jb.afrostyle.salon.repository;

import com.jb.afrostyle.salon.modal.Salon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SalonRepository extends JpaRepository<Salon, Long> {
    
    // MONO-SALON: Méthodes simplifiées pour salon unique
    // Plus de méthodes multi-salon (findByOwnerId, findAllByOwnerId, etc.)
    // Le salon unique utilise l'ID fixe = 1L
}