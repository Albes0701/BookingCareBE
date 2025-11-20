package com.bookingcare.account.entity;

import jakarta.persistence.*;
import lombok.*;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Table(name = "users")
public class Users {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String id;
//    private String fullname;
//    @Column(name = "email", unique = true, nullable = false)
//    private String email;
//    @Column(name = "phonenumber", nullable = false)
//    private String phone;
//    private String address;
//
//    @Enumerated(EnumType.STRING)
//    private Gender gender;
//
//    private String image;
//    private LocalDate birthdate;
//    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
//    private boolean deleted;
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String fullname;
    private String email;
    @Column(name = "phonenumber")
    private String phone;
    private String address;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String image;
    private LocalDate birthdate;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;
}
