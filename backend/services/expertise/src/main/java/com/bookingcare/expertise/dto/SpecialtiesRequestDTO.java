package com.bookingcare.expertise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialtiesRequestDTO(
        @NotBlank(message = "Tên chuyên khoa không được để trống")
        @Size(max = 255, message = "Tên chuyên khoa không được vượt quá 255 ký tự")
        String name,

        @NotBlank(message = "Mã chuyên khoa (code) không được để trống")
        @Size(max = 64, message = "Mã chuyên khoa không được vượt quá 64 ký tự")
        String code,

        @Size(max = 10000, message = "Thông tin chi tiết chuyên khoa quá dài (tối đa 10.000 ký tự)")
        String specialtyDetailInfor,

        @Size(max = 2048, message = "Đường dẫn ảnh không được vượt quá 2048 ký tự")
        String image

) {
}
