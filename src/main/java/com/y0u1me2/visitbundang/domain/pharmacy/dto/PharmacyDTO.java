package com.y0u1me2.visitbundang.domain.pharmacy.dto;

import com.y0u1me2.visitbundang.domain.pharmacy.entity.Pharmacy;
import lombok.*;

@Data
public class PharmacyDTO {
    private String name;
    private String address;
    private String phone;
    private Double latitude;
    private Double longitude;
    private String openTime;
    private String closeTime;

    public PharmacyDTO(Pharmacy pharmacy) {
        name = pharmacy.getName();
        address = pharmacy.getAddress();
        phone = pharmacy.getPhone();
        latitude = pharmacy.getLatitude();
        longitude = pharmacy.getLongitude();
        openTime = String.format("%02d:%02d", pharmacy.getOpenTime() / 60, pharmacy.getOpenTime() % 60);
        closeTime = String.format("%02d:%02d", pharmacy.getCloseTime() / 60, pharmacy.getCloseTime() % 60);
    }

}
