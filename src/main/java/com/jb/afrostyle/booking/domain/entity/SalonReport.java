package com.jb.afrostyle.booking.domain.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalonReport {

    private String salonName;
    private BigDecimal totalEarnings;
    private Integer totalBookings;
    private Integer cancelledBookings;
    private BigDecimal totalRefund;
}