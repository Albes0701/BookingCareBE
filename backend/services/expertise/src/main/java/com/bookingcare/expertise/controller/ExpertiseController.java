package com.bookingcare.expertise.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookingcare.expertise.dto.CredentialReviewActionRequestDTO;
import com.bookingcare.expertise.dto.CredentialTypeResponseDTO;
import com.bookingcare.expertise.dto.DoctorCredentialFileRequestDTO;
import com.bookingcare.expertise.dto.DoctorCredentialRequestDTO;
import com.bookingcare.expertise.dto.DoctorCredentialWithFilesResponseDTO;
import com.bookingcare.expertise.dto.DoctorProfileResponseDTO;
import com.bookingcare.expertise.dto.DoctorsRequestDTO;
import com.bookingcare.expertise.dto.SpecialtiesResponseDTO;
import com.bookingcare.expertise.service.ExpertiseService;

import jakarta.validation.Valid;
import main.java.com.bookingcare.shared.dto.expertise.DoctorsResponseDTO;

@RestController
@RequestMapping("/api/v1/expertise")
public class ExpertiseController {

    private final ExpertiseService expertiseService;

    public ExpertiseController(ExpertiseService expertiseService) {
        this.expertiseService = expertiseService;
    }

    // ========== PUBLIC ========== //

    // Doctors
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorsResponseDTO>> getAllDoctors() {
        List<DoctorsResponseDTO> doctors = expertiseService.getAllDoctors();
        return ResponseEntity.ok().body(doctors);
    }

    @GetMapping("/doctors/{idOrSlug}")
    public ResponseEntity<DoctorsResponseDTO> getDoctorByIdOrSlug(@PathVariable String idOrSlug) {
        DoctorsResponseDTO doctor = expertiseService.getDoctorByIdOrSlug(idOrSlug);
        return ResponseEntity.ok().body(doctor);
    }

    @GetMapping("/doctors/{idOrSlug}/credentials")
    public ResponseEntity<List<DoctorCredentialWithFilesResponseDTO>> getDoctorCredentialsWithFiles(@PathVariable String idOrSlug) {
        List<DoctorCredentialWithFilesResponseDTO> credentials = expertiseService.getApprovedCredentials(idOrSlug);
        return ResponseEntity.ok().body(credentials);
    }



    // Specialties
    @GetMapping("/specialties")
    public ResponseEntity<List<SpecialtiesResponseDTO>> getAllSpecialties() {
        List<SpecialtiesResponseDTO> specialties = expertiseService.getAllSpecialties();
        return ResponseEntity.ok().body(specialties);
    }

    @GetMapping("/specialties/{slugOrCode}")
    public ResponseEntity<SpecialtiesResponseDTO> getSpecialtyBySlugOrCode(@PathVariable String slugOrCode) {
        SpecialtiesResponseDTO specialty = expertiseService.getSpecialtyBySlugOrCode(slugOrCode);
        return ResponseEntity.ok().body(specialty);
    }

    // ========== DOCTORS ========== //
    @GetMapping("/doctors/by-user/{userId}")
    public ResponseEntity<DoctorsResponseDTO> getDoctorByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(expertiseService.getDoctorByUserId(userId));
    }

    // Lấy thông tin profile của bác sĩ theo userId
    @GetMapping("/doctors/by-user/{userId}/profile")
    public ResponseEntity<DoctorProfileResponseDTO> getDoctorProfile(@PathVariable String userId) {
        return ResponseEntity.ok(expertiseService.getDoctorProfileByUserId(userId));
    }


    // Lấy loại chứng chỉ
    @GetMapping("/credential-types")
    public ResponseEntity<List<CredentialTypeResponseDTO>> getCredentialTypes() {
        return ResponseEntity.ok(expertiseService.getAllCredentialTypes());
    }

    // THÔNG TIN BÁC SĨ

    // Tạo mới thông tin bác sĩ
    @PostMapping("/internal/doctors")
    public ResponseEntity<DoctorsResponseDTO> createDoctor(@RequestBody DoctorsRequestDTO request) {
        DoctorsResponseDTO doctor = expertiseService.createDoctor(request);
        return ResponseEntity.ok().body(doctor);
    }

    // Cập nhật thông tin bác sĩ
    @PutMapping("/internal/doctors/{idOrSlug}")
    public ResponseEntity<DoctorsResponseDTO> updateDoctor(@PathVariable String idOrSlug,
                                                           @RequestBody DoctorsRequestDTO request) {
        DoctorsResponseDTO updatedDoctor = expertiseService.updateDoctor(idOrSlug, request);
        return ResponseEntity.ok().body(updatedDoctor);
    }

    // Xóa bác sĩ (soft delete)
    @DeleteMapping("/internal/doctors/{idOrSlug}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable String idOrSlug) {
        expertiseService.deleteDoctor(idOrSlug);
        return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).build();
    }

    // CHỨNG CHỈ BÁC SĨ

    // INTERNAL: List every credential (including pending/draft) for the selected doctor
    @GetMapping("/internal/doctors/{doctorId}/credentials")
    public ResponseEntity<List<DoctorCredentialWithFilesResponseDTO>> getDoctorCredentials(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(expertiseService.getDoctorCredentials(doctorId));
    }

    // INTERNAL: Create a new credential entry tied to the doctor (files uploaded separately)
    @PostMapping("/internal/doctors/credentials")
    public ResponseEntity<DoctorCredentialWithFilesResponseDTO> createDoctorCredential(
            @RequestBody @Valid DoctorCredentialRequestDTO request) {
        var created = expertiseService.createDoctorCredential(request.doctorId(), request);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(created);
    }

     // INTERNAL: Attach or replace supporting document for a credential; returns current snapshot
    @PostMapping("/internal/credentials/{credentialId}/files")
    public ResponseEntity<DoctorCredentialWithFilesResponseDTO> addFileToDoctorCredential(
            @PathVariable String credentialId,
            @RequestBody @Valid DoctorCredentialFileRequestDTO request) {
        var updated = expertiseService.addFileToDoctorCredential(credentialId, request);
        return ResponseEntity.ok(updated);
    }

    // INTERNAL: Update core credential metadata; status stays server-controlled (PENDING→…)
    @PutMapping("/internal/doctors/credentials/{credentialId}")
    public ResponseEntity<DoctorCredentialWithFilesResponseDTO> updateDoctorCredential(
            @PathVariable String credentialId,
            @RequestBody @Valid DoctorCredentialRequestDTO request) {
        var updated = expertiseService.updateDoctorCredential(request.doctorId(), credentialId, request);
        return ResponseEntity.ok(updated);
    }

    // INTERNAL: Soft-delete credential and cascade delete its document if present
    @DeleteMapping("/internal/doctors/{doctorId}/credentials/{credentialId}")
    public ResponseEntity<Void> deleteDoctorCredential(
            @PathVariable String doctorId,
            @PathVariable String credentialId) {
        expertiseService.deleteDoctorCredential(doctorId,credentialId);
        return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).build();
    }

    // INTERNAL: Hard-delete an uploaded credential file by id (used when re-uploading)
    @DeleteMapping("/internal/credentials/files/{fileId}")
    public ResponseEntity<Void> deleteDoctorCredentialFile(@PathVariable String fileId) {
        expertiseService.deleteFileFromDoctorCredential(fileId);
        return ResponseEntity.status(HttpStatus.SC_NO_CONTENT).build();
    }


    // INTERNAL: Doctor finalises a draft credential and sends it for review
    @PostMapping("/internal/credentials/{credentialId}/submit")
    public ResponseEntity<DoctorCredentialWithFilesResponseDTO> submitMyCredential(
            @PathVariable String credentialId) {
        var response = expertiseService.submitDoctorCredential(credentialId);
        return ResponseEntity.ok(response);
    }


    
    // ========== ADMIN ========== //
    // ADMIN: Approve a pending credential (PENDING → APPROVED) and log the action
    @PutMapping("/admin/credentials/{credentialId}/approve")
    public ResponseEntity<DoctorCredentialWithFilesResponseDTO> approveCredential(
            @PathVariable String credentialId,
            @RequestBody @Valid CredentialReviewActionRequestDTO request) {
        var response = expertiseService.approveDoctorCredential(
                credentialId,
                UUID.fromString(request.actorId()),
                request.comment());
        return ResponseEntity.ok(response);
    }

    // ADMIN: Reject a pending credential (PENDING → REJECTED) and persist the reason
    @PutMapping("/admin/credentials/{credentialId}/reject")
    public ResponseEntity<DoctorCredentialWithFilesResponseDTO> rejectCredential(
            @PathVariable String credentialId,
            @RequestBody @Valid CredentialReviewActionRequestDTO request) {
        var response = expertiseService.rejectDoctorCredential(
                credentialId,
                UUID.fromString(request.actorId()),
                request.comment());
        return ResponseEntity.ok(response);
    }

    // ADMIN: List pending credentials with optional filters (credential type code, doctor id, expiry cutoff)
    @GetMapping("/admin/credentials/pending")
    public ResponseEntity<List<DoctorCredentialWithFilesResponseDTO>> listPendingCredentials(
            @RequestParam(required = false) String credentialTypeCode,
            @RequestParam(required = false) String doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDateBefore) {
        var response = expertiseService.getPendingCredentials(credentialTypeCode, doctorId, expiryDateBefore);
        return ResponseEntity.ok(response);
    }







}
