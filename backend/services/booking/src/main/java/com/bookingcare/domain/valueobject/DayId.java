package com.bookingcare.domain.valueobject;

public enum DayId {
  MON("Monday"),
  TUE("Tuesday"),
  WED("Wednesday"),
  THU("Thursday"),
  FRI("Friday"),
  SAT("Saturday"),
  SUN("Sunday"); 

  private String displayName;

  DayId(String displayName) {
    this.displayName = displayName;
  }

  public static DayId fromString(String dayId) {
    for (DayId day : DayId.values()) {
      if (day.name().equalsIgnoreCase(dayId)) {
        return day;
      }
    }
    throw new IllegalArgumentException("Invalid DayId: " + dayId);
  }

  public String getDisplayName() {
    return displayName;
  }
}
