package com.jb.afrostyle.serviceoffering.repository;

import com.jb.afrostyle.serviceoffering.modal.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {

    /**
     * Trouve tous les services actifs
     */
    List<ServiceOffering> findByActiveTrue();
}