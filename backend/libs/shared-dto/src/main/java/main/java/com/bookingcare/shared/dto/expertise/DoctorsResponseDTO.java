package main.java.com.bookingcare.shared.dto.expertise;



public record DoctorsResponseDTO(
        String id,
        String userId,
        String doctorDetailsInfor,
        String shortDoctorInfor,
        String slug,
        boolean isDeleted
) {
}
