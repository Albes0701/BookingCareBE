package com.bookingcare.clinic.controller;

import com.bookingcare.clinic.dto.ClinicBranchDoctorDTO;
import com.bookingcare.clinic.dto.ClinicBranchResponseDTO;
import com.bookingcare.clinic.dto.ClinicPackageResponse;
import com.bookingcare.clinic.dto.ClinicBranchRequestDTO;
import com.bookingcare.clinic.dto.ClinicPatchRequestDTO;
import com.bookingcare.clinic.dto.ClinicRejectionRequestDTO;
import com.bookingcare.clinic.dto.ClinicRequestDTO;
import com.bookingcare.clinic.dto.ClinicResponseDTO;
import com.bookingcare.clinic.service.ClinicService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    //========================================== PUBLIC =========================================//
    

    // Public endpoint: list approved clinics with optional search and paging.
    @GetMapping
    public ResponseEntity<Page<ClinicResponseDTO>> getApprovedClinics(
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<ClinicResponseDTO> clinics = clinicService.getApprovedClinics(query, pageable);
        return ResponseEntity.ok(clinics);
    }

    // Public endpoint: fetch details for a single approved clinic.
    @GetMapping("/{id}")
    public ResponseEntity<ClinicResponseDTO> getApprovedClinicDetail(@PathVariable("id") String clinicId) {
        ClinicResponseDTO clinic = clinicService.getApprovedClinicDetail(clinicId);
        return ResponseEntity.ok(clinic);
    }


    // Public endpoint: list visible branches for an approved clinic.
    @GetMapping("/{clinicId}/branches")
    public ResponseEntity<List<ClinicBranchResponseDTO>> getClinicBranches(
            @PathVariable String clinicId
    ) {
        List<ClinicBranchResponseDTO> branches = clinicService.getClinicBranches(clinicId);
        return ResponseEntity.ok(branches);
    }

    
    // Public endpoint: fetch details for a single clinic branch.
    @GetMapping("/{clinicId}/branches/{branchId}")
    public ResponseEntity<ClinicBranchResponseDTO> getClinicBranchDetail(
            @PathVariable String clinicId,
            @PathVariable String branchId
    ) {
        ClinicBranchResponseDTO branch = clinicService.getApprovedClinicBranchDetail(clinicId, branchId);
        return ResponseEntity.ok(branch);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ClinicResponseDTO> getApprovedClinicDetailBySlug(@PathVariable("slug") String clinicSlug) {
        ClinicResponseDTO clinic = clinicService.getApprovedClinicDetailBySlug(clinicSlug);
        return ResponseEntity.ok(clinic);
    }

    @GetMapping("/branches/package/{clinicBranchId}")
    public List<ClinicPackageResponse> getClinicBranchesByPackageId(
            @PathVariable String clinicBranchId
    ) { 
        List<ClinicPackageResponse> branches = clinicService.getPackageByClinicBranchId(clinicBranchId);
        return branches;
    }





    //========================================== DOCTOR =========================================//

    // Doctor endpoint: create a new clinic owned by the authenticated user.
    @PostMapping("/doctor")
    public ResponseEntity<ClinicResponseDTO> createClinic(@Valid @RequestBody ClinicRequestDTO request) {
        ClinicResponseDTO clinic = clinicService.createClinic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(clinic);
    }

    // Doctor endpoint: patch mutable fields for a draft/rejected clinic.
    @PatchMapping("/doctor/{id}")
    public ResponseEntity<ClinicResponseDTO> patchClinic(
            @PathVariable("id") String clinicId,
            @Valid @RequestBody ClinicPatchRequestDTO request
    ) {
        ClinicResponseDTO clinic = clinicService.patchClinic(clinicId, request);
        return ResponseEntity.ok(clinic);
    }

    // Doctor endpoint: submit a clinic for review (draft/rejected -> pending).
    @PostMapping("/doctor/{id}:submit")
    public ResponseEntity<ClinicResponseDTO> submitClinic(@PathVariable("id") String clinicId) {
        ClinicResponseDTO clinic = clinicService.submitClinic(clinicId);
        return ResponseEntity.ok(clinic);
    }

    // Doctor endpoint: hard delete a clinic when it is still draft/rejected.
    @DeleteMapping("/doctor/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable("id") String clinicId) {
        clinicService.deleteClinic(clinicId);
        return ResponseEntity.noContent().build();
    }

    // Doctor endpoint: list clinics owned by the authenticated user with filters.
    @GetMapping("/doctor/mine")
    public ResponseEntity<Page<ClinicResponseDTO>> getMyClinics(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<ClinicResponseDTO> clinics = clinicService.getMyClinics(status, query, pageable);
        return ResponseEntity.ok(clinics);
    }

    // Doctor endpoint: create a new branch under the authenticated user's clinic.
    @PostMapping("/doctor/{clinicId}/branches")
    public ResponseEntity<ClinicBranchResponseDTO> createClinicBranch(
            @PathVariable String clinicId,
            @Valid @RequestBody ClinicBranchRequestDTO request
    ) {
        ClinicBranchResponseDTO branch = clinicService.createClinicBranch(clinicId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(branch);
    }

    // Doctor endpoint: update branch metadata for the user's clinic.
    @PatchMapping("/doctor/{clinicId}/branches/{branchId}")
    public ResponseEntity<ClinicBranchResponseDTO> updateClinicBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId,
            @Valid @RequestBody ClinicBranchRequestDTO request
    ) {
        ClinicBranchResponseDTO branch = clinicService.updateClinicBranch(clinicId, branchId, request);
        return ResponseEntity.ok(branch);
    }

    // Doctor endpoint: hard delete a branch from the user's clinic.
    @DeleteMapping("/doctor/{clinicId}/branches/{branchId}")
    public ResponseEntity<Void> deleteClinicBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId
    ) {
        clinicService.deleteClinicBranch(clinicId, branchId);
        return ResponseEntity.noContent().build();
    }


    //========================================== ADMIN =========================================//

    // Admin endpoint: list all pending clinics awaiting moderation.
    @GetMapping("/admin/pending")
    public ResponseEntity<Page<ClinicResponseDTO>> getPendingClinics(
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<ClinicResponseDTO> clinics = clinicService.getPendingClinics(query, pageable);
        return ResponseEntity.ok(clinics);
    }

    // Admin endpoint: list all branches (including hidden) under a clinic.
    @GetMapping("/admin/{clinicId}/branches")
    public ResponseEntity<List<ClinicBranchResponseDTO>> getClinicBranchesForAdmin(
            @PathVariable String clinicId
    ) {
        List<ClinicBranchResponseDTO> branches = clinicService.getClinicBranchesForAdmin(clinicId);
        return ResponseEntity.ok(branches);
    }

    // Admin endpoint: list doctor assignments for a branch with optional hidden filter.
    @GetMapping("/admin/branches/{branchId}/doctors")
    public ResponseEntity<List<ClinicBranchDoctorDTO>> getClinicBranchDoctorsForAdmin(
            @PathVariable String branchId,
            @RequestParam(value = "isDeleted", required = false) Boolean isDeleted
    ) {
        List<ClinicBranchDoctorDTO> doctors = clinicService.getClinicBranchDoctorsForAdmin(branchId, isDeleted);
        return ResponseEntity.ok(doctors);
    }

    // Admin endpoint: assign (or restore) a doctor to a branch using doctor userId.
    @PostMapping("/admin/branches/{branchId}/doctors/by-user/{userId}")
    public ResponseEntity<ClinicBranchDoctorDTO> assignDoctorToClinicBranch(
            @PathVariable String branchId,
            @PathVariable String userId
    ) {
        ClinicBranchDoctorDTO doctor = clinicService.assignDoctorToClinicBranch(branchId, userId);
        return ResponseEntity.ok(doctor);
    }

    // Admin endpoint: soft delete a doctor assignment from a branch.
    @PostMapping("/admin/branches/{branchId}/doctors/{doctorId}:archive")
    public ResponseEntity<ClinicBranchDoctorDTO> archiveClinicBranchDoctor(
            @PathVariable String branchId,
            @PathVariable String doctorId
    ) {
        ClinicBranchDoctorDTO doctor = clinicService.archiveClinicBranchDoctor(branchId, doctorId);
        return ResponseEntity.ok(doctor);
    }

    // Admin endpoint: restore a previously archived doctor assignment.
    @PostMapping("/admin/branches/{branchId}/doctors/{doctorId}:restore")
    public ResponseEntity<ClinicBranchDoctorDTO> restoreClinicBranchDoctor(
            @PathVariable String branchId,
            @PathVariable String doctorId
    ) {
        ClinicBranchDoctorDTO doctor = clinicService.restoreClinicBranchDoctor(branchId, doctorId);
        return ResponseEntity.ok(doctor);
    }

    @PostMapping("/admin/{clinicId}/branches/{branchId}:archive")
    public ResponseEntity<ClinicBranchResponseDTO> archiveClinicBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId
    ) {
        ClinicBranchResponseDTO branch = clinicService.archiveClinicBranch(clinicId, branchId);
        return ResponseEntity.ok(branch);
    }

    @PostMapping("/admin/{clinicId}/branches/{branchId}:restore")
    public ResponseEntity<ClinicBranchResponseDTO> restoreClinicBranch(
            @PathVariable String clinicId,
            @PathVariable String branchId
    ) {
        ClinicBranchResponseDTO branch = clinicService.restoreClinicBranch(clinicId, branchId);
        return ResponseEntity.ok(branch);
    }

    // Admin endpoint: fetch details for any clinic regardless of status.
    @GetMapping("/admin/{id}")
    public ResponseEntity<ClinicResponseDTO> getClinicDetail(@PathVariable("id") String clinicId) {
        ClinicResponseDTO clinic = clinicService.getClinicDetailForAdmin(clinicId);
        return ResponseEntity.ok(clinic);
    }

    // Admin endpoint: approve a pending clinic.
    @PostMapping("/admin/{id}:approve")
    public ResponseEntity<ClinicResponseDTO> approveClinic(@PathVariable("id") String clinicId) {
        ClinicResponseDTO clinic = clinicService.approveClinic(clinicId);
        return ResponseEntity.ok(clinic);
    }

    // Admin endpoint: reject a pending clinic with a reason.
    @PostMapping("/admin/{id}:reject")
    public ResponseEntity<ClinicResponseDTO> rejectClinic(
            @PathVariable("id") String clinicId,
            @Valid @RequestBody ClinicRejectionRequestDTO request
    ) {
        ClinicResponseDTO clinic = clinicService.rejectClinic(clinicId, request);
        return ResponseEntity.ok(clinic);
    }

    // Admin endpoint: soft delete a clinic (set is_deleted = true).
    @PostMapping("/admin/{id}:archive") // soft delete
    public ResponseEntity<ClinicResponseDTO> archiveClinic(@PathVariable("id") String clinicId) {
        ClinicResponseDTO clinic = clinicService.archiveClinic(clinicId);
        return ResponseEntity.ok(clinic);
    }

    // Admin endpoint: restore a soft-deleted clinic.
    @PostMapping("/admin/{id}:restore")
    public ResponseEntity<ClinicResponseDTO> restoreClinic(@PathVariable("id") String clinicId) {
        ClinicResponseDTO clinic = clinicService.restoreClinic(clinicId);
        return ResponseEntity.ok(clinic);
    }






    




}
