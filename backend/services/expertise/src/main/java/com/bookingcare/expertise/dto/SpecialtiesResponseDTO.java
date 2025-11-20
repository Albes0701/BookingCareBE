package com.bookingcare.expertise.dto;

import java.time.Instant;
import java.util.UUID;

public record SpecialtiesResponseDTO(
        String id,                       // ID của chuyên khoa
        String name,                   // Tên chuyên khoa hiển thị
        String code,                   // Mã định danh duy nhất
        String slug,                   // Slug URL thân thiện
        String specialtyDetailInfor,   // Mô tả chi tiết chuyên khoa
        String image,                  // Ảnh minh hoạ (URL)
        boolean deleted,               // Trạng thái bị xoá (soft delete)
        Instant createdAt,             // Thời gian tạo
        Instant updatedAt              // Thời gian cập nhật cuối

) {
}
