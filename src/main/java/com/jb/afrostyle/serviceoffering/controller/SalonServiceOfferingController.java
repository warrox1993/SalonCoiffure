package com.jb.afrostyle.serviceoffering.controller;

import com.jb.afrostyle.serviceoffering.modal.ServiceOffering;
import com.jb.afrostyle.serviceoffering.payload.dto.ServiceDTO;
import com.jb.afrostyle.serviceoffering.service.ServiceOfferingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/service-offering/salon-owner")
public class SalonServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<ServiceOffering> createService(@RequestBody ServiceDTO serviceDTO){
        try {
            ServiceOffering created = serviceOfferingService.createService(serviceDTO);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SALON_OWNER')")
    public ResponseEntity<ServiceOffering> updateService(
            @PathVariable Long id,
            @RequestBody ServiceOffering serviceOffering
    ) throws Exception {
        ServiceOffering updatedService = serviceOfferingService
                .updateService(id, serviceOffering);

        return ResponseEntity.ok(updatedService);
    }
}