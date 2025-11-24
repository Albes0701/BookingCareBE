package com.bookingcare.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clinic_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uq_clinic_account_mapping", columnNames = {"clinic_id", "account_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicAccount {
    
    @Id
    private String id;
    
    @Column(name = "clinic_id", nullable = false)
    private String clinicId;
    
    @Column(name = "account_id", nullable = false)
    private String accountId;
    
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
