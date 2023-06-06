package com.y0u1me2.visitbundang.domain.pharmacy.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Pharmacy {
    @Id
    private String id;

    @Column(length = 50)
    private String name;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String phone;

    private Double latitude;

    private Double longitude;

    private Integer openTime;

    private Integer closeTime;

}
