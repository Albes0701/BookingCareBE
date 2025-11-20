package com.bookingcare.expertise.entity;

public enum SpecialtyCode {
    OPHTHALMOLOGY("Nhãn khoa"),
    PEDIATRICS("Khoa nhi"),
    ORTHOPEDICS("Khoa chỉnh hình"),
    DERMATOLOGY("Da liễu"),
    DENTISTRY("Răng hàm mặt"),
    CARDIOLOGY("Tim mạch");

    private final String defaultDisplayName;
    SpecialtyCode(String defaultDisplayName) { this.defaultDisplayName = defaultDisplayName; }
    public String displayName() { return defaultDisplayName; }
}
