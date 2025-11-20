package com.bookingcare.application.handler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ScheduleApplicationServiceDoctor {

    // @Override
    // @Transactional
    // public Boolean updateHealthCheckPackageSchedules(UpdateHealthCheckPackageSchedulesCommand command) {
    //     try {
    //         Boolean flag = true;

    //         List<HealthCheckPackageSchedule> schedulesToUpdate = new ArrayList<>();
    //         for (var i : command.schedules()) {
    //             var existingSchedule = _scheduleRepository.findById(i.scheduleId());

    //             if (existingSchedule.isEmpty()) {
    //                 log.warn("Schedule with ID {} not found.", i.scheduleId());
    //                 flag = false;
    //                 break;
    //             }

    //             var existingPackageSchedule = _healthCheckPackageSchedulesRepository
    //                     .findByHealthCheckPackageIdScheduleIdAndDate(
    //                             command.packageId(),
    //                             i.scheduleId(),
    //                             LocalDate.parse(i.scheduleDate()));

    //             HealthCheckPackageSchedule healthCheckPackageSchedule;

    //             if (existingPackageSchedule.isEmpty()) {
    //                 String rawPackageScheduleId = command.packageId().substring(6)
    //                         + i.scheduleId().substring(6).replace("_", "")
    //                         + i.scheduleDate().replace("-", "");

    //                 String encryptedPackageScheduleId = hashToSha256AndTruncate(rawPackageScheduleId);

    //                 healthCheckPackageSchedule = HealthCheckPackageSchedule.builder()
    //                         .packageScheduleId(encryptedPackageScheduleId)
    //                         .packageId(command.packageId())
    //                         .scheduleId(i.scheduleId())
    //                         .scheduleDate(LocalDate.parse(i.scheduleDate()))
    //                         .isDeleted(!i.isTicked())
    //                         .build();

    //                 log.info("Creating new package schedule: {}", encryptedPackageScheduleId);
    //             } else {
    //                 healthCheckPackageSchedule = existingPackageSchedule.get();
    //                 healthCheckPackageSchedule.setIsDeleted(!i.isTicked());

    //                 log.info("Updating existing package schedule: {}",
    //                         healthCheckPackageSchedule.getPackageScheduleId());
    //             }

    //             schedulesToUpdate.add(healthCheckPackageSchedule);
    //         }

    //         _healthCheckPackageSchedulesRepository.saveAll(schedulesToUpdate);

    //         return flag;
    //     } catch (Exception e) {

    //         log.error("Error updating health check package schedules: " + e.getMessage());
    //         throw e;
    //     }
    // }



    //   private String hashToSha256AndTruncate(String input) {
    //     try {
    //         MessageDigest digest = MessageDigest.getInstance("SHA-256");
    //         byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

    //         // Convert hash bytes to hexadecimal string
    //         StringBuilder hexString = new StringBuilder();
    //         for (byte hashByte : hashBytes) {
    //             String hex = Integer.toHexString(0xff & hashByte);
    //             if (hex.length() == 1) {
    //                 hexString.append('0');
    //             }
    //             hexString.append(hex);
    //         }

    //         // Return first 16 characters of the hash
    //         return hexString.toString().substring(0, 16);
    //     } catch (NoSuchAlgorithmException e) {
    //         log.error("SHA-256 algorithm not available", e);
    //         // Fallback to a simple hash if SHA-256 is not available
    //         return String.valueOf(input.hashCode()).replace("-", "").substring(0, Math.min(16, input.length()));
    //     }
    // }



    
}
