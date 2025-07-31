package com.jb.afrostyle.salon.modal;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@Table(name = "salon", indexes = {
    @Index(name = "idx_salon_city", columnList = "city"),
    @Index(name = "idx_salon_name", columnList = "name"),
    @Index(name = "idx_salon_address", columnList = "address")
})
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    private List<String> images;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    // Coordonnées GPS pour Google Maps
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Informations additionnelles pour Google Maps
    @Column(name = "google_place_id")
    private String googlePlaceId;

    @Column(name = "formatted_address")
    private String formattedAddress;
}