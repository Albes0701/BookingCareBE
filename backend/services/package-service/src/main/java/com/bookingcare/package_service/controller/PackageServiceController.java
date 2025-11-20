package com.bookingcare.package_service.controller;

import com.bookingcare.package_service.dto.HealthCheckPackageDetailResponse;
import com.bookingcare.package_service.dto.HealthCheckPackageRequest;
import com.bookingcare.package_service.dto.HealthCheckPackageSpecialtyLinkRequest;
import com.bookingcare.package_service.dto.HealthCheckPackageSpecialtyResponse;
import com.bookingcare.package_service.dto.HealthCheckPackageResponse;
import com.bookingcare.package_service.dto.MedicalServiceDetailResponse;
import com.bookingcare.package_service.dto.MedicalServiceRequest;
import com.bookingcare.package_service.dto.MedicalServiceResponse;
import com.bookingcare.package_service.dto.ModerationDecisionRequest;
import com.bookingcare.package_service.dto.PackageTypeResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceRequest;
import com.bookingcare.package_service.dto.SpecificMedicalServiceHealthCheckPackageResponse;
import com.bookingcare.package_service.dto.SpecificMedicalServiceLinkRequest;
import com.bookingcare.package_service.dto.SpecificMedicalServiceResponse;
import com.bookingcare.package_service.service.MedicalTaxonomyService;
import com.bookingcare.package_service.service.PackageMedicalCommandService;
import com.bookingcare.package_service.service.PackageMedicalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/packages-services")
@RequiredArgsConstructor
public class PackageServiceController {

    private final PackageMedicalService packageMedicalService;
    private final PackageMedicalCommandService packageMedicalCommandService;
    private final MedicalTaxonomyService medicalTaxonomyService;



    // ============================ PUBLIC ENDPOINTS ============================ //

    /**
     * Returns the list of approved health check packages for catalog screens.
     * Retains `/medical-services` and `/mediacal-services` for backward compatibility.
     */
    @GetMapping({"/packages"})
    public ResponseEntity<List<HealthCheckPackageResponse>> getApprovedPackages() {
        return ResponseEntity.ok(packageMedicalService.getApprovedPackages());
    }

    /**
     * Admin endpoint to list all medical service groups (non-deleted records).
     */
    @GetMapping("/medical-services")
    public ResponseEntity<List<MedicalServiceResponse>> listMedicalServices() {
        return ResponseEntity.ok(medicalTaxonomyService.listMedicalServices());
    }

    /**
     * Returns details for a single medical service including its specific medical services.
     */
    @GetMapping("/services/{id}")
    public ResponseEntity<MedicalServiceDetailResponse> getMedicalServiceDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(packageMedicalService.getMedicalServiceDetail(id));
    }

    /**
     * Returns details for a single health check package including its specific medical services.
     */
    @GetMapping("/packages/{id}")
    public ResponseEntity<HealthCheckPackageDetailResponse> getPackageDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(packageMedicalService.getPackageDetail(id));
    }

      /**
     * Public endpoint to list every specific medical service.
     */
    @GetMapping("/specific-medical-services")
    public ResponseEntity<List<SpecificMedicalServiceResponse>> listSpecificMedicalServices() {
        return ResponseEntity.ok(medicalTaxonomyService.listSpecificMedicalServices());
    }

    /**
     * Public endpoint to list every package type.
     */
    @GetMapping("/package-types")
    public ResponseEntity<List<PackageTypeResponse>> listPackageTypes() {
        return ResponseEntity.ok(packageMedicalService.getPackageTypes());
    }

    @GetMapping("/packages/slug/{slug}")
    public ResponseEntity<HealthCheckPackageResponse> getPackageDetailBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(packageMedicalService.getPackageDetailBySlug(slug));
    }






    // ============================ DOCTOR ENDPOINTS ============================ //

    /**
     * Endpoint to create a medical service group.
     */
    @PostMapping("/doctor/medical-services")
    public ResponseEntity<MedicalServiceResponse> createMedicalService(
            @Valid @RequestBody MedicalServiceRequest request
    ) {
        return ResponseEntity.ok(medicalTaxonomyService.createMedicalService(request));
    }


    /**
     * Attach a specific medical service to a service.
     */
    @PostMapping("/doctor/services/{id}/specifics")
    public ResponseEntity<SpecificMedicalServiceHealthCheckPackageResponse> addSpecific(
            @PathVariable UUID id,
            @Valid @RequestBody SpecificMedicalServiceLinkRequest request
    ) {
        return ResponseEntity.ok(packageMedicalCommandService.addSpecific(id, request));
    }

     /**
     * Doctor endpoint to create a new package (mirrors service creation for compatibility).
     */
    @PostMapping("/doctor/packages")
    public ResponseEntity<HealthCheckPackageResponse> createPackage(@Valid @RequestBody HealthCheckPackageRequest request) {
        return ResponseEntity.ok(packageMedicalCommandService.createService(request));
    }

    /**
     * Doctor endpoint to list packages owned by the current user.
     */
    @GetMapping("/doctor/packages/mine")
    public ResponseEntity<List<HealthCheckPackageResponse>> getMyPackages() {
        return ResponseEntity.ok(packageMedicalCommandService.getMyPackages());
    }

    /**
     * Doctor endpoint to update a package while it is editable.
     */
    @PutMapping("/doctor/packages/{id}")
    public ResponseEntity<HealthCheckPackageResponse> updatePackage(
            @PathVariable UUID id,
            @Valid @RequestBody HealthCheckPackageRequest request
    ) {
        return ResponseEntity.ok(packageMedicalCommandService.updateService(id, request));
    }

    /**
     * Doctor endpoint to submit a package for moderation.
     */
    @PostMapping("/doctor/packages/{id}/submit")
    public ResponseEntity<HealthCheckPackageResponse> submitPackage(@PathVariable UUID id) {
        return ResponseEntity.ok(packageMedicalCommandService.submitService(id));
    }

     /**
     * Doctor endpoint to soft-delete an editable package.
     */
    @DeleteMapping("/doctor/packages/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable UUID id) {
        packageMedicalCommandService.softDeleteService(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Doctor endpoint to pull a pending package back to draft.
     */
    @PostMapping("/doctor/packages/{id}/unsubmit")
    public ResponseEntity<HealthCheckPackageResponse> unsubmitPackage(@PathVariable UUID id) {
        return ResponseEntity.ok(packageMedicalCommandService.unsubmitService(id));
    }

     /**
     * Doctor endpoint to attach a specific medical service to a package.
     */
    @PostMapping("/doctor/packages/{id}/specifics")
    public ResponseEntity<SpecificMedicalServiceHealthCheckPackageResponse> addSpecificToPackage(
            @PathVariable UUID id,
            @Valid @RequestBody SpecificMedicalServiceLinkRequest request
    ) {
        return ResponseEntity.ok(packageMedicalCommandService.addSpecific(id, request));
    }

     /**
     * Doctor endpoint to detach a specific medical service from a package.
     */
    @DeleteMapping("/doctor/packages/{id}/specifics/{specificId}")
    public ResponseEntity<Void> removeSpecificFromPackage(@PathVariable UUID id, @PathVariable UUID specificId) {
        packageMedicalCommandService.removeSpecific(id, specificId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Doctor endpoint to attach a specialty to a package.
     */
    @PostMapping("/doctor/packages/{id}/specialties")
    public ResponseEntity<HealthCheckPackageSpecialtyResponse> addSpecialty(
            @PathVariable UUID id,
            @Valid @RequestBody HealthCheckPackageSpecialtyLinkRequest request
    ) {
        return ResponseEntity.ok(packageMedicalCommandService.addSpecialty(id, request));
    }

    /**
     * Doctor endpoint to remove a specialty link from a package.
     */
    @DeleteMapping("/doctor/packages/{id}/specialties/{specialtyId}")
    public ResponseEntity<Void> removeSpecialty(@PathVariable UUID id, @PathVariable UUID specialtyId) {
        packageMedicalCommandService.removeSpecialty(id, specialtyId);
        return ResponseEntity.noContent().build();
    }




    // ============================ ADMIN ENDPOINTS ============================ //

    /**
     * Admin endpoint to list packages awaiting moderation.
     */
    @GetMapping("/admin/packages/pending")
    public ResponseEntity<List<HealthCheckPackageResponse>> getPendingPackages() {
        return ResponseEntity.ok(packageMedicalCommandService.getPendingPackages());
    }

     /**
     * Admin endpoint to approve a pending package.
     */
    @PutMapping("/admin/packages/{id}/approve")
    public ResponseEntity<HealthCheckPackageResponse> approvePackage(@PathVariable UUID id) {
        return ResponseEntity.ok(packageMedicalCommandService.approvePackage(id));
    }

    /**
     * Admin endpoint to reject a pending package with an optional reason.
     */
    @PutMapping("/admin/packages/{id}/reject")
    public ResponseEntity<HealthCheckPackageResponse> rejectPackage(
            @PathVariable UUID id,
            @RequestBody(required = false) ModerationDecisionRequest request
    ) {
        String reason = request != null ? request.reason() : null;
        return ResponseEntity.ok(packageMedicalCommandService.rejectPackage(id, reason));
    }

     /**
     * Admin endpoint to list packages that were rejected by moderators.
     */
    @GetMapping("/admin/packages/rejected")
    public ResponseEntity<List<HealthCheckPackageResponse>> getRejectedPackages() {
        return ResponseEntity.ok(packageMedicalCommandService.getRejectedPackages());
    }

    // CRUD endpoints for medical services

    /**
     * Admin endpoint to list every medical service, including deleted ones.
     */
    @GetMapping("/admin/medical-services")
    public ResponseEntity<List<MedicalServiceResponse>> listAllMedicalServicesForAdmin() {
        return ResponseEntity.ok(medicalTaxonomyService.listAllMedicalServices());
    }

    /**
     * Admin endpoint to create a medical service group.
     */
    @PostMapping("/admin/medical-service")
    public ResponseEntity<MedicalServiceResponse> createMedicalServiceForAdmin(
            @Valid @RequestBody MedicalServiceRequest request
    ) {
        return ResponseEntity.ok(medicalTaxonomyService.createMedicalService(request));
    }

    /**
     * Admin endpoint to soft-delete a medical service group.
     */
    @DeleteMapping("/admin/medical-services/{id}")
    public ResponseEntity<Void> deleteMedicalService(@PathVariable UUID id) {
        medicalTaxonomyService.deleteMedicalService(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Admin endpoint to update a medical service group.
     */
    @PutMapping("/admin/medical-services/{id}")
    public ResponseEntity<MedicalServiceResponse> updateMedicalService(
            @PathVariable UUID id,
            @Valid @RequestBody MedicalServiceRequest request
    ) {
        return ResponseEntity.ok(medicalTaxonomyService.updateMedicalService(id, request));
    }


   


    /**
     * Admin endpoint to list services that were rejected by moderators.
     */
    @GetMapping("/admin/services/rejected")
    public ResponseEntity<List<HealthCheckPackageResponse>> getRejectedServices() {
        return ResponseEntity.ok(packageMedicalCommandService.getRejectedServices());
    }

    

    // CRUD endpoint for medical service specifics

    /**
     * Admin endpoint to list every specific medical service, including deleted ones.
     */
    @GetMapping("/admin/specific-medical-services")
    public ResponseEntity<List<SpecificMedicalServiceResponse>> listAllSpecificMedicalServicesForAdmin() {
        return ResponseEntity.ok(medicalTaxonomyService.listAllSpecificMedicalServices());
    }
    /**


     /**
     * Admin endpoint to create a specific medical service.
     */
    @PostMapping("/admin/specific-medical-services")
    public ResponseEntity<SpecificMedicalServiceResponse> createSpecificMedicalService(
            @Valid @RequestBody SpecificMedicalServiceRequest request
    ) {
        return ResponseEntity.ok(medicalTaxonomyService.createSpecificMedicalService(request));
    }

    /**
     * Remove a specific medical service from a package.
     */
    @DeleteMapping("/admin/packages/{id}/specifics/{specificId}")
    public ResponseEntity<Void> removeSpecific(@PathVariable UUID id, @PathVariable UUID specificId) {
        packageMedicalCommandService.removeSpecific(id, specificId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin endpoint to soft-delete a specific medical service.
     */
    @DeleteMapping("/admin/specific-medical-services/{id}")
    public ResponseEntity<Void> deleteSpecificMedicalService(@PathVariable UUID id) {
        medicalTaxonomyService.deleteSpecificMedicalService(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin endpoint to update a specific medical service.
     */
    @PutMapping("/admin/specific-medical-services/{id}")
    public ResponseEntity<SpecificMedicalServiceResponse> updateSpecificMedicalService(
            @PathVariable UUID id,
            @Valid @RequestBody SpecificMedicalServiceRequest request
    ) {
        return ResponseEntity.ok(medicalTaxonomyService.updateSpecificMedicalService(id, request));
    }


   
    
}
